package com.inventario.sihcp.mobile.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Set;

/**
 * Serviço de armazenamento de fotos de coleta em disco.
 *
 * <h2>Estrutura de pastas</h2>
 * <pre>
 * data/fotos/
 *   inventario_{id}/
 *     {YYYY-MM}/
 *       patrimonio/
 *         coleta_{coletaId}_{numeroPatrimonio}.jpg
 *       sem_etiqueta/
 *         coleta_{coletaId}_SE.jpg
 *       divergencia/
 *         coleta_{coletaId}_{numeroPatrimonio}.jpg
 * </pre>
 *
 * <h2>Regras de nomenclatura</h2>
 * <ul>
 *   <li>Sempre {@code .jpg} — todas as imagens são convertidas para JPEG.</li>
 *   <li>{@code coleta_{coletaId}_{identificador}.jpg}
 *       onde {@code identificador} é o número do patrimônio ou {@code SE} para
 *       itens sem etiqueta.</li>
 *   <li>Tipos aceitos: {@code patrimonio}, {@code sem_etiqueta}, {@code divergencia}.</li>
 * </ul>
 *
 * <h2>Compressão</h2>
 * Todas as fotos são comprimidas para no máximo 100 KB antes de serem salvas,
 * redimensionando (máx. 800×600) e ajustando a qualidade JPEG progressivamente.
 *
 * @author Sistema de Inventário IFMT
 * @version 2.22
 */
@Service
public class FotoColetaStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FotoColetaStorageService.class);

    /** Diretório raiz para armazenamento de fotos. */
    private static final String FOTOS_BASE_DIR = "data/fotos";

    /** Tamanho máximo permitido no upload (antes da compressão): 5 MB. */
    private static final long MAX_UPLOAD_SIZE = 5 * 1024 * 1024;

    /** Tamanho alvo após compressão: 100 KB. */
    private static final long TAMANHO_ALVO = 100 * 1024;

    /** Largura máxima da imagem após redimensionamento. */
    private static final int LARGURA_MAX = 800;

    /** Altura máxima da imagem após redimensionamento. */
    private static final int ALTURA_MAX = 600;

    /** Extensões de imagem aceitas. */
    private static final String[] EXTENSOES_ACEITAS = {".jpg", ".jpeg", ".png", ".webp"};

    /** Tipos de coleta aceitos como subpasta. */
    private static final Set<String> TIPOS_ACEITOS = Set.of("patrimonio", "sem_etiqueta", "divergencia");

    /** Identificador usado para itens sem etiqueta. */
    public static final String ID_SEM_ETIQUETA = "SE";

    // ─── API pública ──────────────────────────────────────────────────────────

    /**
     * Salva uma foto de coleta no disco.
     *
     * <p>Estrutura gerada:</p>
     * <pre>
     *   data/fotos/inventario_{inventarioId}/{YYYY-MM}/{tipo}/coleta_{coletaId}_{identificador}.jpg
     * </pre>
     *
     * @param inputStream   stream da foto recebida
     * @param inventarioId  ID do inventário
     * @param coletaId      ID da coleta no banco
     * @param tipo          Tipo da coleta: {@code patrimonio}, {@code sem_etiqueta} ou {@code divergencia}
     * @param identificador Número do patrimônio ou {@value #ID_SEM_ETIQUETA} para sem etiqueta
     * @param nomeOriginal  nome original do arquivo (para extrair extensão e validar)
     * @param tamanho       tamanho do arquivo em bytes
     * @return caminho relativo da foto salva (para gravar no banco)
     * @throws IOException              se ocorrer erro de I/O
     * @throws IllegalArgumentException se o arquivo ou parâmetros forem inválidos
     */
    public String salvarFoto(InputStream inputStream, int inventarioId, int coletaId,
                             String tipo, String identificador,
                             String nomeOriginal, long tamanho) throws IOException {

        // Validar tamanho
        if (tamanho > MAX_UPLOAD_SIZE) {
            throw new IllegalArgumentException(
                    "Foto excede o tamanho máximo de upload (5 MB). Tamanho recebido: "
                    + String.format("%.1f MB", tamanho / (1024.0 * 1024.0)));
        }

        // Validar extensão
        String extensao = extrairExtensao(nomeOriginal);
        if (!extensaoAceita(extensao)) {
            throw new IllegalArgumentException(
                    "Formato de imagem não aceito: " + extensao
                    + ". Formatos aceitos: jpg, jpeg, png, webp");
        }

        // Validar e normalizar tipo
        String tipoNormalizado = normalizarTipo(tipo);

        // Sanitizar identificador (apenas alfanumérico + hífen + underscore)
        String idSanitizado = sanitizarIdentificador(identificador);

        // Ler e comprimir imagem
        byte[] bytesOriginais = inputStream.readAllBytes();
        long tamanhoOriginal = bytesOriginais.length;
        byte[] bytesComprimidos = comprimirParaTamanhoAlvo(bytesOriginais);
        long tamanhoFinal = bytesComprimidos.length;

        logger.info("Foto comprimida: {}KB → {}KB (redução de {}%)",
                tamanhoOriginal / 1024,
                tamanhoFinal / 1024,
                tamanhoOriginal > 0 ? (100 - (tamanhoFinal * 100 / tamanhoOriginal)) : 0);

        // Montar caminho:
        // data/fotos/inventario_{id}/{YYYY-MM}/{tipo}/coleta_{coletaId}_{identificador}.jpg
        String mesAno = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String diretorioRelativo = String.format("inventario_%d/%s/%s", inventarioId, mesAno, tipoNormalizado);
        String nomeArquivo = String.format("coleta_%d_%s.jpg", coletaId, idSanitizado);
        String caminhoRelativo = diretorioRelativo + "/" + nomeArquivo;

        Path diretorioCompleto = Paths.get(FOTOS_BASE_DIR, diretorioRelativo);
        Path arquivoCompleto   = Paths.get(FOTOS_BASE_DIR, caminhoRelativo);

        Files.createDirectories(diretorioCompleto);
        Files.write(arquivoCompleto, bytesComprimidos,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        logger.info("Foto salva: {} ({}KB) — Inventário: {}, Coleta: {}, Tipo: {}, ID: {}",
                caminhoRelativo, tamanhoFinal / 1024, inventarioId, coletaId, tipoNormalizado, idSanitizado);

        return caminhoRelativo;
    }

    /**
     * Sobrecarga de compatibilidade para código legado que não passa tipo/identificador.
     * Usa tipo {@code patrimonio} e identificador {@code coletaId} como fallback.
     *
     * @deprecated Use {@link #salvarFoto(InputStream, int, int, String, String, String, long)}.
     */
    @Deprecated
    public String salvarFoto(InputStream inputStream, int inventarioId, int coletaId,
                             String nomeOriginal, long tamanho) throws IOException {
        return salvarFoto(inputStream, inventarioId, coletaId,
                "patrimonio", String.valueOf(coletaId), nomeOriginal, tamanho);
    }

    /**
     * Lê uma foto do disco para download/visualização.
     *
     * @param caminhoRelativo caminho relativo da foto (gravado no banco)
     * @return bytes da foto
     * @throws IOException se o arquivo não existir ou não puder ser lido
     */
    public byte[] lerFoto(String caminhoRelativo) throws IOException {
        Path arquivo = Paths.get(FOTOS_BASE_DIR, caminhoRelativo);
        if (!Files.exists(arquivo)) {
            throw new IOException("Foto não encontrada: " + caminhoRelativo);
        }
        return Files.readAllBytes(arquivo);
    }

    /**
     * Verifica se uma foto existe no disco.
     */
    public boolean fotoExiste(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) return false;
        return Files.exists(Paths.get(FOTOS_BASE_DIR, caminhoRelativo));
    }

    /**
     * Remove uma foto do disco.
     */
    public boolean removerFoto(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) return false;
        try {
            Path arquivo = Paths.get(FOTOS_BASE_DIR, caminhoRelativo);
            boolean removido = Files.deleteIfExists(arquivo);
            if (removido) logger.info("Foto removida: {}", caminhoRelativo);
            return removido;
        } catch (IOException e) {
            logger.error("Erro ao remover foto: {}", caminhoRelativo, e);
            return false;
        }
    }

    /**
     * Retorna o content-type baseado na extensão do arquivo.
     */
    public String getContentType(String caminhoRelativo) {
        String ext = extrairExtensao(caminhoRelativo).toLowerCase();
        return switch (ext) {
            case ".png"  -> "image/png";
            case ".webp" -> "image/webp";
            default      -> "image/jpeg";
        };
    }

    // ─── Compressão ──────────────────────────────────────────────────────────

    private byte[] comprimirParaTamanhoAlvo(byte[] bytesOriginais) throws IOException {
        BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(bytesOriginais));
        if (imagem == null) throw new IOException("Não foi possível decodificar a imagem");

        imagem = redimensionar(imagem, LARGURA_MAX, ALTURA_MAX);

        float qualidade = 0.7f;
        byte[] resultado = comprimirJpeg(imagem, qualidade);
        if (resultado.length <= TAMANHO_ALVO) return resultado;

        for (float q : new float[]{0.5f, 0.35f, 0.25f, 0.15f, 0.10f}) {
            resultado = comprimirJpeg(imagem, q);
            if (resultado.length <= TAMANHO_ALVO) return resultado;
        }

        imagem = redimensionar(imagem, 640, 480);
        resultado = comprimirJpeg(imagem, 0.3f);
        if (resultado.length <= TAMANHO_ALVO) return resultado;

        imagem = redimensionar(imagem, 400, 300);
        return comprimirJpeg(imagem, 0.2f);
    }

    private BufferedImage redimensionar(BufferedImage original, int larguraMax, int alturaMax) {
        int w = original.getWidth(); int h = original.getHeight();
        if (w <= larguraMax && h <= alturaMax) return original;

        double escala = Math.min((double) larguraMax / w, (double) alturaMax / h);
        int nw = (int) (w * escala); int nh = (int) (h * escala);

        BufferedImage dest = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, nw, nh, null);
        g.dispose();
        return dest;
    }

    private byte[] comprimirJpeg(BufferedImage imagem, float qualidade) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) throw new IOException("Writer JPEG não disponível");

        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(qualidade);
            writer.write(null, new IIOImage(imagem, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    // ─── Auxiliares ──────────────────────────────────────────────────────────

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return ".jpg";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.')).toLowerCase();
    }

    private boolean extensaoAceita(String extensao) {
        for (String aceita : EXTENSOES_ACEITAS) {
            if (aceita.equals(extensao)) return true;
        }
        return false;
    }

    /**
     * Normaliza o tipo para um dos valores aceitos.
     * Retorna {@code patrimonio} se o tipo for desconhecido.
     */
    private String normalizarTipo(String tipo) {
        if (tipo == null) return "patrimonio";
        String t = tipo.trim().toLowerCase();
        return TIPOS_ACEITOS.contains(t) ? t : "patrimonio";
    }

    /**
     * Sanitiza o identificador para uso seguro em nome de arquivo.
     * Mantém apenas letras, dígitos, hífen e underscore. Máx. 50 chars.
     */
    private String sanitizarIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) return "X";
        String sanitizado = identificador.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return sanitizado.length() > 50 ? sanitizado.substring(0, 50) : sanitizado;
    }
}
