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

/**
 * Serviço de armazenamento de fotos de coleta em disco.
 *
 * <p>Organiza as fotos no sistema de arquivos seguindo a estrutura:</p>
 * <pre>
 *   data/fotos/inventario_{id}/{YYYY-MM}/coleta_{id}.jpg
 * </pre>
 *
 * <p>Todas as fotos são comprimidas automaticamente para no máximo 100 KB
 * antes de serem salvas, redimensionando e ajustando a qualidade JPEG
 * conforme necessário.</p>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
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

    /**
     * Salva uma foto de coleta no disco.
     *
     * @param inputStream  stream da foto recebida
     * @param inventarioId ID do inventário
     * @param coletaId     ID da coleta
     * @param nomeOriginal nome original do arquivo (para extrair extensão)
     * @param tamanho      tamanho do arquivo em bytes
     * @return caminho relativo da foto salva (para gravar no banco)
     * @throws IOException              se ocorrer erro de I/O
     * @throws IllegalArgumentException se o arquivo for inválido
     */
    public String salvarFoto(InputStream inputStream, int inventarioId, int coletaId,
                             String nomeOriginal, long tamanho) throws IOException {

        // Validar tamanho do upload
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

        // Ler imagem original
        byte[] bytesOriginais = inputStream.readAllBytes();
        long tamanhoOriginal = bytesOriginais.length;

        // Comprimir para no máximo 100 KB
        byte[] bytesComprimidos = comprimirParaTamanhoAlvo(bytesOriginais);
        long tamanhoFinal = bytesComprimidos.length;

        logger.info("Foto comprimida: {}KB → {}KB (redução de {}%)",
                tamanhoOriginal / 1024,
                tamanhoFinal / 1024,
                tamanhoOriginal > 0 ? (100 - (tamanhoFinal * 100 / tamanhoOriginal)) : 0);

        // Montar caminho: data/fotos/inventario_{id}/{YYYY-MM}/coleta_{id}.jpg
        String mesAno = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String diretorioRelativo = String.format("inventario_%d/%s", inventarioId, mesAno);
        String nomeArquivo = String.format("coleta_%d.jpg", coletaId); // Sempre .jpg após compressão
        String caminhoRelativo = diretorioRelativo + "/" + nomeArquivo;

        Path diretorioCompleto = Paths.get(FOTOS_BASE_DIR, diretorioRelativo);
        Path arquivoCompleto = Paths.get(FOTOS_BASE_DIR, caminhoRelativo);

        // Criar diretórios se não existirem
        Files.createDirectories(diretorioCompleto);

        // Salvar arquivo comprimido
        Files.write(arquivoCompleto, bytesComprimidos, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        logger.info("Foto salva: {} ({}KB) — Inventário: {}, Coleta: {}",
                caminhoRelativo, tamanhoFinal / 1024, inventarioId, coletaId);

        return caminhoRelativo;
    }

    /**
     * Comprime uma imagem para no máximo {@value #TAMANHO_ALVO} bytes (100 KB).
     *
     * <p>Estratégia em 2 etapas:</p>
     * <ol>
     *   <li>Redimensionar para no máximo 800x600 se necessário</li>
     *   <li>Reduzir qualidade JPEG progressivamente até caber em 100 KB</li>
     * </ol>
     *
     * @param bytesOriginais bytes da imagem original
     * @return bytes da imagem comprimida (JPEG)
     * @throws IOException se ocorrer erro no processamento
     */
    private byte[] comprimirParaTamanhoAlvo(byte[] bytesOriginais) throws IOException {
        BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(bytesOriginais));
        if (imagem == null) {
            throw new IOException("Não foi possível decodificar a imagem");
        }

        // Etapa 1: Redimensionar se necessário
        imagem = redimensionar(imagem, LARGURA_MAX, ALTURA_MAX);

        // Etapa 2: Comprimir com qualidade decrescente até caber em 100 KB
        float qualidade = 0.7f; // Começar com 70%
        byte[] resultado = comprimirJpeg(imagem, qualidade);

        // Se já está dentro do limite, retornar
        if (resultado.length <= TAMANHO_ALVO) {
            return resultado;
        }

        // Reduzir qualidade progressivamente
        float[] qualidades = {0.5f, 0.35f, 0.25f, 0.15f, 0.10f};
        for (float q : qualidades) {
            resultado = comprimirJpeg(imagem, q);
            if (resultado.length <= TAMANHO_ALVO) {
                return resultado;
            }
        }

        // Se ainda não coube, redimensionar mais agressivamente
        imagem = redimensionar(imagem, 640, 480);
        resultado = comprimirJpeg(imagem, 0.3f);
        if (resultado.length <= TAMANHO_ALVO) {
            return resultado;
        }

        // Último recurso: 400x300 com qualidade mínima
        imagem = redimensionar(imagem, 400, 300);
        return comprimirJpeg(imagem, 0.2f);
    }

    /**
     * Redimensiona uma imagem mantendo a proporção.
     */
    private BufferedImage redimensionar(BufferedImage original, int larguraMax, int alturaMax) {
        int larguraOriginal = original.getWidth();
        int alturaOriginal = original.getHeight();

        // Se já está dentro dos limites, não redimensionar
        if (larguraOriginal <= larguraMax && alturaOriginal <= alturaMax) {
            return original;
        }

        // Calcular nova dimensão mantendo proporção
        double escala = Math.min(
                (double) larguraMax / larguraOriginal,
                (double) alturaMax / alturaOriginal
        );

        int novaLargura = (int) (larguraOriginal * escala);
        int novaAltura = (int) (alturaOriginal * escala);

        BufferedImage redimensionada = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = redimensionada.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(original, 0, 0, novaLargura, novaAltura, null);
        g2d.dispose();

        return redimensionada;
    }

    /**
     * Comprime uma imagem para JPEG com a qualidade especificada.
     *
     * @param imagem    imagem a comprimir
     * @param qualidade qualidade JPEG (0.0 a 1.0)
     * @return bytes do JPEG comprimido
     */
    private byte[] comprimirJpeg(BufferedImage imagem, float qualidade) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("Writer JPEG não disponível");
        }

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
     *
     * @param caminhoRelativo caminho relativo da foto
     * @return true se o arquivo existe
     */
    public boolean fotoExiste(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return false;
        }
        return Files.exists(Paths.get(FOTOS_BASE_DIR, caminhoRelativo));
    }

    /**
     * Remove uma foto do disco.
     *
     * @param caminhoRelativo caminho relativo da foto
     * @return true se removida com sucesso
     */
    public boolean removerFoto(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return false;
        }
        try {
            Path arquivo = Paths.get(FOTOS_BASE_DIR, caminhoRelativo);
            boolean removido = Files.deleteIfExists(arquivo);
            if (removido) {
                logger.info("Foto removida: {}", caminhoRelativo);
            }
            return removido;
        } catch (IOException e) {
            logger.error("Erro ao remover foto: {}", caminhoRelativo, e);
            return false;
        }
    }

    /**
     * Retorna o content type baseado na extensão do arquivo.
     */
    public String getContentType(String caminhoRelativo) {
        String ext = extrairExtensao(caminhoRelativo).toLowerCase();
        return switch (ext) {
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    // ─── Métodos auxiliares ──────────────────────────────────────

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return ".jpg"; // padrão
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.')).toLowerCase();
    }

    private boolean extensaoAceita(String extensao) {
        for (String aceita : EXTENSOES_ACEITAS) {
            if (aceita.equals(extensao)) {
                return true;
            }
        }
        return false;
    }
}
