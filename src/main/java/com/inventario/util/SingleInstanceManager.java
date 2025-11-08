package com.inventario.util;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gerenciador de instância única da aplicação
 * Impede que múltiplas instâncias da aplicação sejam executadas simultaneamente
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class SingleInstanceManager {
    
    private static final String LOCK_FILE_NAME = "sistema-inventario.lock";
    private static final String APP_NAME = "Sistema de Inventário";
    
    private static RandomAccessFile lockFile;
    private static FileChannel fileChannel;
    private static FileLock fileLock;
    private static File lockFileHandle;
    
    /**
     * Verifica se já existe uma instância da aplicação em execução
     * @return true se esta é a primeira instância, false caso contrário
     */
    public static boolean isFirstInstance() {
        try {
            // Criar diretório temporário se não existir
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            
            // Criar arquivo de lock no diretório temporário
            lockFileHandle = new File(tempDir.toFile(), LOCK_FILE_NAME);
            
            // Tentar criar e bloquear o arquivo
            lockFile = new RandomAccessFile(lockFileHandle, "rw");
            fileChannel = lockFile.getChannel();
            
            // Tentar obter lock exclusivo
            fileLock = fileChannel.tryLock();
            
            if (fileLock == null) {
                // Não conseguiu obter o lock - outra instância está rodando
                cleanup();
                return false;
            }
            
            // Escrever informações da instância no arquivo
            lockFile.writeUTF("Sistema de Inventário - PID: " + ProcessHandle.current().pid());
            lockFile.writeUTF("Iniciado em: " + new java.util.Date().toString());
            
            // Configurar shutdown hook para limpar o lock ao fechar
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                releaseLock();
            }));
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Erro ao verificar instância única: " + e.getMessage());
            cleanup();
            return false;
        }
    }
    
    /**
     * Libera o lock da aplicação
     */
    public static void releaseLock() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
                fileLock = null;
            }
            
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
                fileChannel = null;
            }
            
            if (lockFile != null) {
                lockFile.close();
                lockFile = null;
            }
            
            // Remover arquivo de lock
            if (lockFileHandle != null && lockFileHandle.exists()) {
                lockFileHandle.delete();
                lockFileHandle = null;
            }
            
        } catch (IOException e) {
            System.err.println("Erro ao liberar lock: " + e.getMessage());
        }
    }
    
    /**
     * Limpa recursos em caso de erro
     */
    private static void cleanup() {
        try {
            if (fileChannel != null && fileChannel.isOpen()) {
                fileChannel.close();
            }
            if (lockFile != null) {
                lockFile.close();
            }
        } catch (IOException e) {
            // Ignorar erros de cleanup
        }
    }
    
    /**
     * Exibe mensagem informando que a aplicação já está em execução
     */
    public static void showAlreadyRunningMessage() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                "O " + APP_NAME + " já está em execução.\n\n" +
                "Apenas uma instância da aplicação pode ser executada por vez.\n" +
                "Verifique a barra de tarefas ou feche a instância anterior.",
                APP_NAME + " - Já em Execução",
                JOptionPane.WARNING_MESSAGE
            );
        });
    }
    
    /**
     * Obtém informações sobre o arquivo de lock
     * @return String com informações do lock ou null se não existir
     */
    public static String getLockInfo() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            File lockFile = new File(tempDir.toFile(), LOCK_FILE_NAME);
            
            if (lockFile.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(lockFile, "r")) {
                    StringBuilder info = new StringBuilder();
                    info.append("Arquivo de lock encontrado:\n");
                    info.append("Localização: ").append(lockFile.getAbsolutePath()).append("\n");
                    info.append("Tamanho: ").append(lockFile.length()).append(" bytes\n");
                    info.append("Última modificação: ").append(new java.util.Date(lockFile.lastModified())).append("\n");
                    
                    if (lockFile.length() > 0) {
                        try {
                            String pid = raf.readUTF();
                            String timestamp = raf.readUTF();
                            info.append("Conteúdo:\n");
                            info.append("  ").append(pid).append("\n");
                            info.append("  ").append(timestamp).append("\n");
                        } catch (Exception e) {
                            info.append("Erro ao ler conteúdo: ").append(e.getMessage()).append("\n");
                        }
                    }
                    
                    return info.toString();
                }
            }
            
            return null;
            
        } catch (Exception e) {
            return "Erro ao obter informações do lock: " + e.getMessage();
        }
    }
    
    /**
     * Força a remoção do arquivo de lock (usar com cuidado)
     * @return true se removido com sucesso
     */
    public static boolean forceRemoveLock() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            File lockFile = new File(tempDir.toFile(), LOCK_FILE_NAME);
            
            if (lockFile.exists()) {
                return lockFile.delete();
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao forçar remoção do lock: " + e.getMessage());
            return false;
        }
    }
}