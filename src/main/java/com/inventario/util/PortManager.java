package com.inventario.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

/**
 * Utilitário para gerenciar portas do sistema
 * Verifica e libera portas em uso quando necessário
 */
public class PortManager {
    
    private static final Logger logger = LoggerFactory.getLogger(PortManager.class);
    
    /**
     * Verifica se uma porta está em uso
     * 
     * @param port porta a verificar
     * @return true se a porta está em uso
     */
    public static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false; // Porta disponível
        } catch (IOException e) {
            return true; // Porta em uso
        }
    }
    
    /**
     * Libera uma porta matando o processo que a está usando
     * 
     * @param port porta a liberar
     * @return true se conseguiu liberar a porta
     */
    public static boolean freePort(int port) {
        try {
            logger.info("Tentando liberar porta {}", port);
            
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                return freePortWindows(port);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                return freePortUnix(port);
            } else {
                logger.warn("Sistema operacional não suportado para liberação automática de porta: {}", os);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao tentar liberar porta {}", port, e);
            return false;
        }
    }
    
    /**
     * Libera porta no Windows
     */
    private static boolean freePortWindows(int port) {
        try {
            logger.info("Procurando processos usando porta {} no Windows...", port);
            
            // Encontrar TODOS os PIDs usando a porta (pode haver múltiplos)
            String findCommand = "netstat -ano | findstr :" + port;
            Process findProcess = Runtime.getRuntime().exec(new String[]{"cmd", "/c", findCommand});
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            String line;
            java.util.Set<Integer> pids = new java.util.HashSet<>();
            
            while ((line = reader.readLine()) != null) {
                logger.debug("Linha netstat: {}", line);
                
                // Procurar por LISTENING ou ESTABLISHED
                if (line.contains("LISTENING") || line.contains("ESTABLISHED")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        try {
                            int pid = Integer.parseInt(parts[parts.length - 1]);
                            pids.add(pid);
                            logger.info("Encontrado PID {} usando porta {}", pid, port);
                        } catch (NumberFormatException e) {
                            logger.debug("Não foi possível parsear PID da linha: {}", line);
                        }
                    }
                }
            }
            
            findProcess.waitFor();
            reader.close();
            
            if (pids.isEmpty()) {
                logger.warn("Nenhum processo encontrado usando porta {}, mas porta está em uso", port);
                return false;
            }
            
            // Tentar matar todos os processos encontrados
            boolean allKilled = true;
            for (Integer pid : pids) {
                logger.info("Tentando terminar processo PID {}...", pid);
                
                try {
                    // Tentar taskkill /F /PID
                    String killCommand = "taskkill /F /PID " + pid;
                    Process killProcess = Runtime.getRuntime().exec(new String[]{"cmd", "/c", killCommand});
                    
                    // Ler output do taskkill
                    BufferedReader killReader = new BufferedReader(new InputStreamReader(killProcess.getInputStream()));
                    String killLine;
                    while ((killLine = killReader.readLine()) != null) {
                        logger.info("taskkill output: {}", killLine);
                    }
                    killReader.close();
                    
                    int exitCode = killProcess.waitFor();
                    
                    if (exitCode == 0) {
                        logger.info("✓ Processo {} terminado com sucesso", pid);
                    } else {
                        logger.error("✗ Falha ao terminar processo {} (exit code: {})", pid, exitCode);
                        allKilled = false;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao tentar matar processo {}", pid, e);
                    allKilled = false;
                }
            }
            
            if (!allKilled) {
                logger.warn("Nem todos os processos foram terminados");
            }
            
            // Aguardar a porta ser liberada (tentar múltiplas vezes)
            logger.info("Aguardando porta {} ser liberada...", port);
            for (int i = 0; i < 10; i++) {
                Thread.sleep(500);
                if (!isPortInUse(port)) {
                    logger.info("✓ Porta {} liberada com sucesso após {}ms", port, (i + 1) * 500);
                    return true;
                }
                logger.debug("Tentativa {}/10: Porta {} ainda em uso", i + 1, port);
            }
            
            logger.error("✗ Porta {} ainda está em uso após 5 segundos", port);
            return false;
            
        } catch (Exception e) {
            logger.error("Erro ao liberar porta no Windows", e);
            return false;
        }
    }
    
    /**
     * Libera porta no Unix/Linux/Mac
     */
    private static boolean freePortUnix(int port) {
        try {
            // Encontrar PID do processo usando a porta
            String findCommand = "lsof -ti:" + port;
            Process findProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", findCommand});
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            String pidStr = reader.readLine();
            findProcess.waitFor();
            reader.close();
            
            if (pidStr != null && !pidStr.trim().isEmpty()) {
                Integer pid = Integer.parseInt(pidStr.trim());
                logger.info("Processo encontrado usando porta {}: PID {}", port, pid);
                
                // Matar o processo
                String killCommand = "kill -9 " + pid;
                Process killProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", killCommand});
                int exitCode = killProcess.waitFor();
                
                if (exitCode == 0) {
                    logger.info("Processo {} terminado com sucesso", pid);
                    
                    // Aguardar um pouco para a porta ser liberada
                    Thread.sleep(2000);
                    
                    // Verificar se a porta foi liberada
                    if (!isPortInUse(port)) {
                        logger.info("Porta {} liberada com sucesso", port);
                        return true;
                    } else {
                        logger.warn("Porta {} ainda está em uso após terminar o processo", port);
                        return false;
                    }
                } else {
                    logger.error("Falha ao terminar processo {}", pid);
                    return false;
                }
            } else {
                logger.warn("Não foi possível encontrar o processo usando a porta {}", port);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao liberar porta no Unix/Linux/Mac", e);
            return false;
        }
    }
    
    /**
     * Garante que uma porta esteja disponível, liberando-a se necessário
     * Tenta múltiplas vezes se necessário
     * 
     * @param port porta desejada
     * @return true se a porta está disponível
     */
    public static boolean ensurePortAvailable(int port) {
        // Verificar se já está disponível
        if (!isPortInUse(port)) {
            logger.info("✓ Porta {} está disponível", port);
            return true;
        }
        
        logger.warn("⚠ Porta {} está em uso, tentando liberar...", port);
        
        // Tentar liberar até 3 vezes
        for (int attempt = 1; attempt <= 3; attempt++) {
            logger.info("Tentativa {}/3 de liberar porta {}", attempt, port);
            
            boolean freed = freePort(port);
            
            if (freed) {
                logger.info("✓ Porta {} liberada com sucesso na tentativa {}", port, attempt);
                return true;
            }
            
            if (attempt < 3) {
                logger.warn("Tentativa {} falhou, aguardando antes de tentar novamente...", attempt);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrompido durante espera", e);
                    return false;
                }
            }
        }
        
        logger.error("✗ Não foi possível liberar porta {} após 3 tentativas", port);
        return false;
    }
    
    /**
     * Obtém informações sobre o processo usando uma porta
     * 
     * @param port porta a verificar
     * @return informações do processo ou null se não encontrado
     */
    public static String getProcessInfo(int port) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                String findCommand = "netstat -ano | findstr :" + port;
                Process findProcess = Runtime.getRuntime().exec(new String[]{"cmd", "/c", findCommand});
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
                StringBuilder info = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    if (line.contains("LISTENING")) {
                        info.append(line).append("\n");
                    }
                }
                
                findProcess.waitFor();
                reader.close();
                
                return info.length() > 0 ? info.toString() : null;
            }
            
        } catch (Exception e) {
            logger.error("Erro ao obter informações do processo", e);
        }
        
        return null;
    }
}

