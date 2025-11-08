package com.inventario.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.inventario.dao.QRCodeDAO;
import com.inventario.model.Patrimonio;
import com.inventario.model.QRCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
// Formatação de datas centralizada em DateFormatUtils
import com.inventario.util.DateFormatUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para gerenciamento de QR Codes
 */
public class QRCodeService {
    
    private final QRCodeDAO qrCodeDAO;
    
    // Configurações padrão
    private static final int TAMANHO_PADRAO = 200;
    private static final String FORMATO_PADRAO = "PNG";
    private static final String CHARSET = "UTF-8";
    
    public QRCodeService() {
        this.qrCodeDAO = new QRCodeDAO();
    }
    
    /**
     * Gera QR Code para um patrimônio
     * @param patrimonio Patrimônio para gerar QR Code
     * @return QRCode gerado
     * @throws Exception
     */
    public QRCode gerarQRCode(Patrimonio patrimonio) throws Exception {
        return gerarQRCode(patrimonio, TAMANHO_PADRAO);
    }
    
    /**
     * Gera QR Code para um patrimônio com tamanho específico
     * @param patrimonio Patrimônio para gerar QR Code
     * @param tamanho Tamanho do QR Code em pixels
     * @return QRCode gerado
     * @throws Exception
     */
    public QRCode gerarQRCode(Patrimonio patrimonio, int tamanho) throws Exception {
        // Verifica se patrimônio já possui QR Code ativo
        if (qrCodeDAO.patrimonioTemQRCodeAtivo(patrimonio.getId())) {
            throw new IllegalStateException("Patrimônio já possui QR Code ativo. Desative o atual antes de gerar um novo.");
        }
        
        // Gera o conteúdo do QR Code
        String conteudoQR = gerarConteudoQR(patrimonio);
        
        // Gera o hash dos dados
        String hash = gerarHash(conteudoQR);
        
        // Cria o objeto QRCode
        QRCode qrCode = new QRCode();
        qrCode.setIdPatrimonio(patrimonio.getId());
        qrCode.setCodigoQR(conteudoQR);
        qrCode.setHashDados(hash);
        qrCode.setDataGeracao(new java.sql.Timestamp(System.currentTimeMillis()));
        qrCode.setFormato(FORMATO_PADRAO);
        qrCode.setTamanho(tamanho);
        qrCode.setAtivo(true);
        
        // Salva no banco de dados
        int id = qrCodeDAO.salvar(qrCode);
        qrCode.setId(id);
        
        return qrCode;
    }
    
    /**
     * Gera uma nova versão do QR Code (desativa o atual e cria um novo)
     * @param patrimonio Patrimônio para regenerar QR Code
     * @return Novo QRCode gerado
     * @throws Exception
     */
    public QRCode regenerarQRCode(Patrimonio patrimonio) throws Exception {
        return regenerarQRCode(patrimonio, TAMANHO_PADRAO);
    }
    
    /**
     * Gera uma nova versão do QR Code com tamanho específico
     * @param patrimonio Patrimônio para regenerar QR Code
     * @param tamanho Tamanho do QR Code em pixels
     * @return Novo QRCode gerado
     * @throws Exception
     */
    public QRCode regenerarQRCode(Patrimonio patrimonio, int tamanho) throws Exception {
        // Desativa QR Codes existentes
        qrCodeDAO.desativarPorPatrimonio(patrimonio.getId());
        
        // Gera novo QR Code
        return gerarQRCode(patrimonio, tamanho);
    }
    
    /**
     * Gera a imagem do QR Code
     * @param qrCode QR Code para gerar imagem
     * @return Array de bytes da imagem
     * @throws Exception
     */
    public byte[] gerarImagemQRCode(QRCode qrCode) throws Exception {
        return gerarImagemQRCode(qrCode.getCodigoQR(), qrCode.getTamanho());
    }
    
    /**
     * Gera a imagem do QR Code a partir do conteúdo
     * @param conteudo Conteúdo do QR Code
     * @param tamanho Tamanho da imagem em pixels
     * @return Array de bytes da imagem
     * @throws Exception
     */
    public byte[] gerarImagemQRCode(String conteudo, int tamanho) throws Exception {
        try {
            // Configurações do QR Code
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
            hints.put(EncodeHintType.MARGIN, 1);
            
            // Gera a matriz de bits
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho, hints);
            
            // Converte para imagem
            BufferedImage image = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_RGB);
            
            for (int x = 0; x < tamanho; x++) {
                for (int y = 0; y < tamanho; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            
            // Converte para array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, FORMATO_PADRAO, baos);
            
            return baos.toByteArray();
            
        } catch (WriterException | IOException e) {
            throw new Exception("Erro ao gerar imagem do QR Code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida um QR Code
     * @param qrCode QR Code a ser validado
     * @return true se válido
     */
    public boolean validarQRCode(QRCode qrCode) {
        if (qrCode == null || qrCode.getCodigoQR() == null || qrCode.getHashDados() == null) {
            return false;
        }
        
        try {
            // Verifica se o hash confere
            String hashCalculado = gerarHash(qrCode.getCodigoQR());
            return hashCalculado.equals(qrCode.getHashDados());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Busca QR Code por patrimônio
     * @param idPatrimonio ID do patrimônio
     * @return QR Code ativo ou null
     * @throws SQLException
     */
    public QRCode buscarPorPatrimonio(int idPatrimonio) throws SQLException {
        return qrCodeDAO.buscarPorPatrimonio(idPatrimonio);
    }
    
    /**
     * Busca QR Code por ID
     * @param id ID do QR Code
     * @return QR Code ou null
     * @throws SQLException
     */
    public QRCode buscarPorId(int id) throws SQLException {
        return qrCodeDAO.buscarPorId(id);
    }
    
    /**
     * Lista todos os QR Codes ativos
     * @return Lista de QR Codes ativos
     * @throws SQLException
     */
    public List<QRCode> listarAtivos() throws SQLException {
        return qrCodeDAO.listarAtivos();
    }
    
    /**
     * Lista QR Codes com paginação
     * @param offset Offset para paginação
     * @param limit Limite de registros
     * @return Lista de QR Codes
     * @throws SQLException
     */
    public List<QRCode> listarComPaginacao(int offset, int limit) throws SQLException {
        return qrCodeDAO.listarComPaginacao(offset, limit);
    }
    
    /**
     * Desativa QR Code
     * @param id ID do QR Code
     * @return true se desativado com sucesso
     * @throws SQLException
     */
    public boolean desativar(int id) throws SQLException {
        return qrCodeDAO.desativar(id);
    }
    
    /**
     * Conta total de QR Codes
     * @return Total de QR Codes
     * @throws SQLException
     */
    public int contarTotal() throws SQLException {
        return qrCodeDAO.contarTotal();
    }
    
    /**
     * Conta QR Codes ativos
     * @return Total de QR Codes ativos
     * @throws SQLException
     */
    public int contarAtivos() throws SQLException {
        return qrCodeDAO.contarAtivos();
    }
    
    /**
     * Verifica se patrimônio tem QR Code ativo
     * @param idPatrimonio ID do patrimônio
     * @return true se tem QR Code ativo
     * @throws SQLException
     */
    public boolean patrimonioTemQRCodeAtivo(int idPatrimonio) throws SQLException {
        return qrCodeDAO.patrimonioTemQRCodeAtivo(idPatrimonio);
    }
    
    /**
     * Gera o conteúdo do QR Code para um patrimônio
     * Formato: JSON simples com dados imutáveis
     * @param patrimonio Patrimônio
     * @return Conteúdo do QR Code
     */
    private String gerarConteudoQR(Patrimonio patrimonio) {
        // Formato JSON simples com dados imutáveis
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(patrimonio.getId()).append(",");
        sb.append("\"numero\":\"").append(escaparJSON(patrimonio.getNumero())).append("\",");
        sb.append("\"descricao\":\"").append(escaparJSON(patrimonio.getDescricao())).append("\",");
        
        // Data de aquisição formatada
        String dataAquisicao = "";
        if (patrimonio.getDataEntrada() != null) {
            dataAquisicao = patrimonio.getDataEntradaFormatada();
        } else if (patrimonio.getDataCarga() != null) {
            dataAquisicao = patrimonio.getDataCargaFormatada();
        }
        sb.append("\"dataAquisicao\":\"").append(dataAquisicao).append("\",");
        
        // Timestamp de geração
        String timestamp = DateFormatUtils.formatForApi(DateFormatUtils.nowAsDate());
        sb.append("\"geradoEm\":\"").append(timestamp).append("\"");
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * Escapa caracteres especiais para JSON
     * @param texto Texto a ser escapado
     * @return Texto escapado
     */
    private String escaparJSON(String texto) {
        if (texto == null) {
            return "";
        }
        
        return texto.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Gera hash SHA-256 dos dados
     * @param dados Dados para gerar hash
     * @return Hash SHA-256 em hexadecimal
     * @throws NoSuchAlgorithmException
     */
    private String gerarHash(String dados) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dados.getBytes());
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
    
    /**
     * Extrai dados do conteúdo do QR Code
     * @param conteudoQR Conteúdo do QR Code
     * @return Map com os dados extraídos
     */
    public Map<String, String> extrairDadosQR(String conteudoQR) {
        Map<String, String> dados = new HashMap<>();
        
        try {
            // Parse simples do JSON
            if (conteudoQR.startsWith("{") && conteudoQR.endsWith("}")) {
                String conteudo = conteudoQR.substring(1, conteudoQR.length() - 1);
                String[] pares = conteudo.split(",");
                
                for (String par : pares) {
                    String[] chaveValor = par.split(":", 2);
                    if (chaveValor.length == 2) {
                        String chave = chaveValor[0].trim().replace("\"", "");
                        String valor = chaveValor[1].trim().replace("\"", "");
                        dados.put(chave, valor);
                    }
                }
            }
        } catch (Exception e) {
            // Se não conseguir fazer parse, retorna mapa vazio
        }
        
        return dados;
    }
    
    /**
     * Gera estatísticas dos QR Codes
     * @return Map com estatísticas
     * @throws SQLException
     */
    public Map<String, Object> gerarEstatisticas() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalQRCodes", contarTotal());
        stats.put("qrCodesAtivos", contarAtivos());
        stats.put("qrCodesInativos", contarTotal() - contarAtivos());
        
        return stats;
    }
}