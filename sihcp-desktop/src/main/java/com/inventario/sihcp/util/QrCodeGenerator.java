package com.inventario.sihcp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilitário standalone para geração de QR Codes em formato PNG usando ZXing 3.5.2.
 *
 * <p>Pode ser invocado via linha de comando pelo script de setup automatizado:
 * <pre>
 *   java -cp "bin/mobile-server.jar" com.inventario.sihcp.util.QrCodeGenerator \
 *       "http://192.168.1.10:8080/api/mobile" "qrcode/api-qrcode.png" 300
 * </pre>
 *
 * <p>Requisitos: 7.4
 */
public class QrCodeGenerator {

    /**
     * Gera um QR Code em formato PNG e o grava no caminho especificado.
     *
     * @param content    conteúdo a ser codificado no QR Code (ex.: URL da API)
     * @param outputPath caminho completo do arquivo PNG de saída
     * @param size       tamanho em pixels (largura e altura iguais)
     * @throws IllegalArgumentException se algum parâmetro for inválido
     * @throws WriterException          se ocorrer erro ao codificar o QR Code
     * @throws IOException              se ocorrer erro ao gravar o arquivo
     */
    public static void generate(String content, String outputPath, int size)
            throws WriterException, IOException {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("O conteúdo do QR Code não pode ser vazio.");
        }
        if (outputPath == null || outputPath.isBlank()) {
            throw new IllegalArgumentException("O caminho de saída não pode ser vazio.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("O tamanho deve ser um valor positivo. Recebido: " + size);
        }

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);

        Path path = FileSystems.getDefault().getPath(outputPath);

        // Criar diretórios intermediários se não existirem
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }

    /**
     * Ponto de entrada para invocação via linha de comando.
     *
     * <p>Uso:
     * <pre>
     *   java -cp "bin/mobile-server.jar" com.inventario.sihcp.util.QrCodeGenerator \
     *       &lt;conteúdo&gt; &lt;caminho-saída&gt; &lt;tamanho&gt;
     * </pre>
     *
     * @param args args[0] = conteúdo do QR Code (URL da API),
     *             args[1] = caminho do arquivo PNG de saída,
     *             args[2] = tamanho em pixels (ex.: 300)
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Uso: QrCodeGenerator <conteúdo> <caminho-saída> <tamanho>");
            System.err.println("Exemplo: QrCodeGenerator \"http://192.168.1.10:8080/api/mobile\" \"qrcode/api-qrcode.png\" 300");
            System.exit(1);
        }

        String content = args[0];
        String outputPath = args[1];
        int size;

        try {
            size = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Erro: o tamanho deve ser um número inteiro. Recebido: \"" + args[2] + "\"");
            System.exit(1);
            return;
        }

        try {
            generate(content, outputPath, size);
            System.out.println("QR Code gerado com sucesso: " + outputPath);
            System.out.println("Conteúdo: " + content);
            System.out.println("Tamanho: " + size + "x" + size + " px");
        } catch (IllegalArgumentException e) {
            System.err.println("Erro de parâmetro: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Erro ao gerar QR Code: " + e.getMessage());
            System.exit(1);
        }
    }
}
