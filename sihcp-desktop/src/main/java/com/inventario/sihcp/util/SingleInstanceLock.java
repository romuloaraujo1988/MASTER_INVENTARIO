package com.inventario.sihcp.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import javax.swing.JOptionPane;

/**
 * Classe utilitária para garantir que apenas uma instância do sistema seja executada por vez.
 * Utiliza um arquivo de lock para controlar a execução única.
 * 
 * <p>Funcionamento:</p>
 * <ul>
 *   <li>Cria um arquivo de lock no diretório temporário do sistema</li>
 *   <li>Usa FileChannel.tryLock() para obter um lock exclusivo no arquivo</li>
 *   <li>Se o lock não puder ser adquirido, significa que outra instância já está rodando</li>
 *   <li>O lock é automaticamente liberado quando o sistema é fechado (shutdown hook)</li>
 * </ul>
 * 
 * <p>Uso:</p>
 * <pre>
 * if (!SingleInstanceLock.tryLock()) {
 *     SingleInstanceLock.showInstanceAlreadyRunningMessage();
 *     System.exit(0);
 * }
 * </pre>
 * 
 * @author Sistema SIHCP
 * @version 1.0
 */
public class SingleInstanceLock {
    
    private static final String LOCK_FILE_NAME = ".sihcp_instance.lock";
    private static File lockFile;
    private static FileChannel channel;
    private static FileLock lock;
    private static RandomAccessFile randomAccessFile;
    
    /**
     * Tenta adquirir o lock de instância única.
     * 
     * @return true se conseguiu adquirir o lock (primeira instância), false caso contrário
     */
    public static boolean tryLock() {
        try {
            // Obter diretório temporário do sistema
            String tempDir = System.getProperty("java.io.tmpdir");
            lockFile = new File(tempDir, LOCK_FILE_NAME);
            
            // Criar o arquivo se não existir
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            
            // Tentar obter o lock exclusivo
            randomAccessFile = new RandomAccessFile(lockFile, "rw");
            channel = randomAccessFile.getChannel();
            lock = channel.tryLock();
            
            if (lock == null) {
                // Não conseguiu obter o lock - outra instância está rodando
                channel.close();
                randomAccessFile.close();
                return false;
            }
            
            // Adicionar shutdown hook para liberar o lock ao fechar
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                releaseLock();
            }));
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao tentar adquirir lock de instância única: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Libera o lock de instância única.
     */
    public static void releaseLock() {
        try {
            if (lock != null && lock.isValid()) {
                lock.release();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            if (lockFile != null && lockFile.exists()) {
                lockFile.delete();
            }
        } catch (IOException e) {
            System.err.println("Erro ao liberar lock de instância única: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Exibe uma mensagem de erro informando que outra instância já está rodando.
     */
    public static void showInstanceAlreadyRunningMessage() {
        JOptionPane.showMessageDialog(
            null,
            "O sistema SIHCP já está em execução!\n\n" +
            "Apenas uma instância do sistema pode ser executada por vez.\n" +
            "Por favor, feche a instância anterior antes de abrir uma nova.",
            "Sistema Já em Execução",
            JOptionPane.WARNING_MESSAGE
        );
    }
}
