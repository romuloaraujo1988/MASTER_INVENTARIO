package com.inventario.sihcp.service;

import com.inventario.sihcp.util.DatabaseConnection;
import com.inventario.sihcp.util.PortManager;
import com.inventario.sihcp.config.DatabaseConfig;
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
    // CORREÇÃO: Usar WeakReference para evitar vazamento de memória com listeners
    private static final List<java.lang.ref.WeakReference<ServerStatusListener>> listeners = new java.util.concurrent.CopyOnWriteArrayList<>();

    // Cache para status do servidor (evitar chamadas excessivas ao netstat)
    private static long lastStatusCheck = 0;
    private static boolean cachedStatus = false;
    private static final long STATUS_CACHE_MS = 3000; // Cache por 3 segundos

    // ===== CONFIGURAÇÕES JVM OTIMIZADAS v4.0 - BAIXO CONSUMO =====
    // CRÍTICO: Heap MUITO REDUZIDO para evitar 1GB por usuário
    // Servidor mobile é LEVE - não precisa de muita memória
    // Alvo: 150-300MB total para 10 usuários simultâneos
    private static final String JVM_OPTS = 
            // Memória: 128MB inicial, 384MB máximo (DRASTICAMENTE REDUZIDO)
            "-Xms128m -Xmx384m " +
            "-XX:MaxMetaspaceSize=64m " +  // Reduzido de 128m
            "-Xss192k " +  // Stack mínimo por thread
            // G1GC com coleta MUITO AGRESSIVA
            "-XX:+UseG1GC " +
            "-XX:MaxGCPauseMillis=50 " +  // Pausas curtas
            "-XX:InitiatingHeapOccupancyPercent=20 " +  // GC inicia com 20% do heap
            "-XX:G1ReservePercent=25 " +  // Mais reserva para evitar OOM
            "-XX:G1HeapWastePercent=3 " +  // Menos desperdício
            "-XX:G1MixedGCCountTarget=4 " +  // GC misto mais frequente
            "-XX:G1HeapRegionSize=1m " +  // Regiões menores
            // Otimizações de memória AGRESSIVAS
            "-XX:+UseStringDeduplication " +
            "-XX:+UseCompressedOops " +
            "-XX:+UseCompressedClassPointers " +
            "-XX:+OptimizeStringConcat " +
            "-XX:-UseBiasedLocking " +
            "-XX:+AlwaysPreTouch " +  // Alocar memória no início
            "-XX:SoftRefLRUPolicyMSPerMB=50 " +  // Limpar soft refs mais rápido
            // Spring Boot otimizações MÁXIMAS
            "-Dspring.main.lazy-initialization=true " +
            "-Dspring.jmx.enabled=false " +
            "-Dspring.data.jpa.repositories.bootstrap-mode=lazy " +
            "-Dspring.main.banner-mode=off " +
            "-Dspring.output.ansi.enabled=never " +
            // HikariCP MÍNIMO (5 conexões máximo)
            "-Dspring.datasource.hikari.maximum-pool-size=5 " +
            "-Dspring.datasource.hikari.minimum-idle=1 " +
            "-Dspring.datasource.hikari.idle-timeout=120000 " +
            "-Dspring.datasource.hikari.max-lifetime=300000 " +
            // Tomcat MÍNIMO
            "-Dserver.tomcat.threads.max=10 " +
            "-Dserver.tomcat.threads.min-spare=2 " +
            "-Dserver.tomcat.max-connections=20 " +
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
     * CORREÇÃO: Usa WeakReference para evitar vazamento de memória
     */
    public static void addStatusListener(ServerStatusListener listener) {
        // Limpar referências mortas antes de adicionar
        cleanupDeadListeners();
        
        // Verificar se já existe
        for (java.lang.ref.WeakReference<ServerStatusListener> ref : listeners) {
            if (ref.get() == listener) {
                return;
            }
        }
        listeners.add(new java.lang.ref.WeakReference<>(listener));
    }

    /**
     * Remove um listener
     */
    public static void removeStatusListener(ServerStatusListener listener) {
        listeners.removeIf(ref -> ref.get() == null || ref.get() == listener);
    }
    
    /**
     * Limpa referências mortas de listeners
     */
    private static void cleanupDeadListeners() {
        listeners.removeIf(ref -> ref.get() == null);
    }

    /**
     * Notifica todos os listeners sobre mudança de status
     * CORREÇÃO: Limpa referências mortas durante notificação
     */
    private static void notifyStatusChanged(ServerStatus status, String message) {
        SwingUtilities.invokeLater(() -> {
            java.util.Iterator<java.lang.ref.WeakReference<ServerStatusListener>> it = listeners.iterator();
            while (it.hasNext()) {
                java.lang.ref.WeakReference<ServerStatusListener> ref = it.next();
                ServerStatusListener listener = ref.get();
                if (listener != null) {
                    listener.onStatusChanged(status, message);
                }
                // Referências mortas serão limpas na próxima chamada de cleanupDeadListeners
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
                command.add("-Dspring-boot.run.main-class=com.inventario.sihcp.MobileApiApplication");
                command.add("-Dspring-boot.run.jvmArguments=" + JVM_OPTS);
                
                // UNIFICAÇÃO: Buscar configuração ativa do banco
                DatabaseConfig dbConfig = DatabaseConnection.getCurrentConfig();
                if (dbConfig != null && dbConfig.isValid()) {
                    String url = String.format("jdbc:postgresql://%s:%d/%s", 
                            dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase());
                    command.add("-Dspring.datasource.url=" + url);
                    command.add("-Dspring.datasource.username=" + dbConfig.getUsername());
                    command.add("-Dspring.datasource.password=" + dbConfig.getPassword());
                    System.out.println("[MobileServer] Usando configuração unificada: " + hostPort(dbConfig));
                } else {
                    // Fallback para localhost se não houver config
                    command.add("-Dspring.datasource.url=jdbc:postgresql://localhost:5432/sispatrimonio");
                    command.add("-Dspring.datasource.username=postgres");
                    command.add("-Dspring.datasource.password=Romulo@1919");
                    System.out.println("[MobileServer] Usando configuração de fallback (localhost)");
                }

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
    /**
     * Auxiliar para log seguro
     */
    private static String hostPort(DatabaseConfig config) {
        return config.getHost() + ":" + config.getPort() + "/" + config.getDatabase();
    }
}
