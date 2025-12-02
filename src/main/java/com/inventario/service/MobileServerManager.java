package com.inventario.service;

import com.inventario.util.PortManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * Gerenciador do servidor mobile
 * Permite iniciar, parar e verificar status do servidor mobile
 */
public class MobileServerManager {

    private static final int PORTA_MOBILE = 8081;
    private static final String PROFILE_MOBILE = "mobile";
    private static Process serverProcess = null;
    private static boolean isStarting = false;
    private static List<ServerStatusListener> listeners = new ArrayList<>();

    // Cache para status do servidor (evitar chamadas excessivas ao netstat)
    private static long lastStatusCheck = 0;
    private static boolean cachedStatus = false;
    private static final long STATUS_CACHE_MS = 3000; // Cache por 3 segundos

    // ===== CONFIGURAÇÕES JVM OTIMIZADAS =====
    // v2.1: Permite até 1GB com monitoramento e recuperação automática
    // O MemoryMonitorService cuida de detectar problemas e recuperar
    private static final String JVM_OPTS = 
            // Memória: 256MB inicial, 1GB máximo
            "-Xms256m -Xmx1g " +
            "-XX:MaxMetaspaceSize=192m " +
            "-Xss256k " +  // Stack de threads reduzido
            // G1GC com coleta eficiente
            "-XX:+UseG1GC " +
            "-XX:MaxGCPauseMillis=100 " +
            "-XX:InitiatingHeapOccupancyPercent=45 " +
            "-XX:G1ReservePercent=15 " +
            // Otimizações de memória
            "-XX:+UseStringDeduplication " +
            "-XX:+UseCompressedOops " +
            "-XX:+UseCompressedClassPointers " +
            "-XX:+OptimizeStringConcat " +
            // Segurança: sair se OOM
            "-XX:-UseBiasedLocking " +
            "-XX:+DisableExplicitGC " +
            // Spring Boot otimizações
            "-Dspring.main.lazy-initialization=true " +
            "-Dspring.jmx.enabled=false " +
            "-Dspring.data.jpa.repositories.bootstrap-mode=lazy " +
            // Diagnóstico
            "-XX:+HeapDumpOnOutOfMemoryError " +
            "-XX:HeapDumpPath=logs/ " +
            "-XX:+ExitOnOutOfMemoryError " +
            "-Djava.awt.headless=true";

    /**
     * Interface para receber notificações de mudança de status
     */
    public interface ServerStatusListener {
        void onStatusChanged(ServerStatus status, String message);
    }

    /**
     * Status do servidor
     */
    public enum ServerStatus {
        STOPPED("Parado"),
        STARTING("Iniciando..."),
        RUNNING("Rodando"),
        STOPPING("Parando..."),
        ERROR("Erro");

        private final String descricao;

        ServerStatus(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    /**
     * Adiciona um listener para receber notificações
     */
    public static void addStatusListener(ServerStatusListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove um listener
     */
    public static void removeStatusListener(ServerStatusListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica todos os listeners sobre mudança de status
     */
    private static void notifyStatusChanged(ServerStatus status, String message) {
        SwingUtilities.invokeLater(() -> {
            for (ServerStatusListener listener : listeners) {
                listener.onStatusChanged(status, message);
            }
        });
    }

    /**
     * Verifica se o servidor está rodando (com cache para evitar chamadas
     * excessivas)
     */
    public static boolean isServerRunning() {
        long now = System.currentTimeMillis();

        // Usar cache se disponível (evita executar netstat repetidamente)
        if (now - lastStatusCheck < STATUS_CACHE_MS) {
            return cachedStatus;
        }

        // Atualizar cache
        cachedStatus = checkServerRunningInternal();
        lastStatusCheck = now;

        return cachedStatus;
    }

    /**
     * Verifica internamente se o servidor está rodando (SEM cache)
     * CORREÇÃO: Usa try-with-resources para garantir fechamento de streams
     */
    private static boolean checkServerRunningInternal() {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("netstat", "-ano");
            pb.redirectErrorStream(true);
            process = pb.start();

            // try-with-resources garante fechamento do BufferedReader
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":" + PORTA_MOBILE) && line.contains("LISTENING")) {
                        return true;
                    }
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Erro ao verificar status do servidor: " + e.getMessage());
            return false;
        } finally {
            // CRÍTICO: Destruir processo para evitar vazamento
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * Obtém o PID do processo que está usando a porta
     * CORREÇÃO: Usa try-with-resources para garantir fechamento de streams
     */
    private static Integer getServerPID() {
        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("netstat", "-ano");
            pb.redirectErrorStream(true);
            process = pb.start();

            // try-with-resources garante fechamento do BufferedReader
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(":" + PORTA_MOBILE) && line.contains("LISTENING")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length > 0) {
                            String pidStr = parts[parts.length - 1];
                            try {
                                return Integer.parseInt(pidStr);
                            } catch (NumberFormatException e) {
                                // Ignora
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao obter PID do servidor: " + e.getMessage());
        } finally {
            // CRÍTICO: Destruir processo para evitar vazamento
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }

        return null;
    }

    /**
     * Obtém o status atual do servidor
     */
    public static ServerStatus getServerStatus() {
        if (isStarting) {
            return ServerStatus.STARTING;
        }

        if (serverProcess != null && serverProcess.isAlive()) {
            return ServerStatus.RUNNING;
        }

        if (isServerRunning()) {
            return ServerStatus.RUNNING;
        }

        return ServerStatus.STOPPED;
    }

    /**
     * Inicia o servidor mobile
     */
    public static CompletableFuture<Boolean> startServer() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verificar se já está rodando
                if (isServerRunning()) {
                    notifyStatusChanged(ServerStatus.RUNNING, "Servidor já está rodando na porta " + PORTA_MOBILE);
                    return false;
                }

                isStarting = true;
                notifyStatusChanged(ServerStatus.STARTING,
                        "Verificando disponibilidade da porta " + PORTA_MOBILE + "...");

                // Verificar e liberar porta se necessário
                if (!PortManager.ensurePortAvailable(PORTA_MOBILE)) {
                    isStarting = false;
                    notifyStatusChanged(ServerStatus.ERROR,
                            "Porta " + PORTA_MOBILE
                                    + " está em uso e não pôde ser liberada. Feche o processo manualmente.");
                    return false;
                }

                notifyStatusChanged(ServerStatus.STARTING,
                        "Porta disponível. Iniciando servidor mobile (memória: 512MB-2GB, HikariCP ativo)...");

                System.out.println("[MobileServer] Iniciando com JVM otimizada: " + JVM_OPTS);

                // Construir comando Maven com configurações explícitas
                String os = System.getProperty("os.name").toLowerCase();
                List<String> command = new ArrayList<>();

                if (os.contains("win")) {
                    command.add("cmd");
                    command.add("/c");
                    command.add("mvnw.cmd");
                } else {
                    command.add("./mvnw");
                }

                command.add("spring-boot:run");
                command.add("-Dspring-boot.run.profiles=" + PROFILE_MOBILE);
                command.add("-Dspring-boot.run.main-class=com.inventario.MobileApiApplication");
                command.add("-Dspring-boot.run.jvmArguments=" + JVM_OPTS);
                command.add("-Dspring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio");
                command.add("-Dspring.datasource.username=postgres");

                // Iniciar processo
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);

                serverProcess = pb.start();

                // Aguardar servidor iniciar (até 60 segundos)
                int maxWait = 60;
                int waited = 0;

                while (waited < maxWait) {
                    Thread.sleep(2000);
                    waited += 2;

                    if (isServerRunning()) {
                        isStarting = false;
                        notifyStatusChanged(ServerStatus.RUNNING,
                                "Servidor iniciado com sucesso na porta " + PORTA_MOBILE);
                        return true;
                    }

                    // Verificar se o processo morreu
                    if (!serverProcess.isAlive()) {
                        isStarting = false;
                        notifyStatusChanged(ServerStatus.ERROR,
                                "Erro ao iniciar servidor. Verifique os logs.");
                        return false;
                    }
                }

                isStarting = false;
                notifyStatusChanged(ServerStatus.ERROR,
                        "Timeout ao iniciar servidor. Verifique os logs.");
                return false;

            } catch (Exception e) {
                isStarting = false;
                notifyStatusChanged(ServerStatus.ERROR,
                        "Erro ao iniciar servidor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Para o servidor mobile
     */
    public static CompletableFuture<Boolean> stopServer() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                notifyStatusChanged(ServerStatus.STOPPING, "Parando servidor mobile...");

                // Tentar parar o processo gerenciado
                if (serverProcess != null && serverProcess.isAlive()) {
                    serverProcess.destroy();
                    serverProcess.waitFor(10, TimeUnit.SECONDS);

                    if (serverProcess.isAlive()) {
                        serverProcess.destroyForcibly();
                    }

                    serverProcess = null;
                }

                // Verificar se ainda há processo na porta
                Integer pid = getServerPID();
                if (pid != null) {
                    // Matar processo pela porta
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder killPb;

                    if (os.contains("win")) {
                        killPb = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid));
                    } else {
                        killPb = new ProcessBuilder("kill", "-9", String.valueOf(pid));
                    }

                    killPb.redirectErrorStream(true);
                    Process killProcess = killPb.start();
                    killProcess.waitFor();
                }

                // Aguardar porta liberar
                Thread.sleep(2000);

                if (!isServerRunning()) {
                    notifyStatusChanged(ServerStatus.STOPPED, "Servidor parado com sucesso");
                    return true;
                } else {
                    notifyStatusChanged(ServerStatus.ERROR, "Erro ao parar servidor");
                    return false;
                }

            } catch (Exception e) {
                notifyStatusChanged(ServerStatus.ERROR,
                        "Erro ao parar servidor: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Reinicia o servidor mobile
     */
    public static CompletableFuture<Boolean> restartServer() {
        return stopServer().thenCompose(stopped -> {
            if (stopped) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return startServer();
            }
            return CompletableFuture.completedFuture(false);
        });
    }

    /**
     * Obtém informações do servidor
     */
    public static String getServerInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Status: ").append(getServerStatus().getDescricao()).append("\n");
        info.append("Porta: ").append(PORTA_MOBILE).append("\n");
        info.append("Perfil: ").append(PROFILE_MOBILE).append("\n");

        if (isServerRunning()) {
            Integer pid = getServerPID();
            if (pid != null) {
                info.append("PID: ").append(pid).append("\n");
            }
            info.append("URL: http://localhost:").append(PORTA_MOBILE).append("/inventario/api/mobile\n");
        }

        return info.toString();
    }
}
