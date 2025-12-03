package com.inventario.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Gerenciador do Servidor Mobile como Processo Externo
 * 
 * Esta classe gerencia o servidor mobile em um processo JVM separado,
 * garantindo isolamento de memória e melhor controle de recursos.
 * 
 * VANTAGENS DO PROCESSO SEPARADO:
 * - Memória isolada (GC independente)
 * - Pode ser reiniciado sem afetar o desktop
 * - Melhor estabilidade
 * - Logs separados
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
public class MobileServerProcessManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileServerProcessManager.class);
    
    // Configurações padrão
    private static final int DEFAULT_PORT = 8081;
    private static final String CONTEXT_PATH = "/inventario";
    private static final int STARTUP_TIMEOUT_SECONDS = 60;
    private static final int HEALTH_CHECK_INTERVAL_MS = 1000;
    
    // Configurações de memória para o servidor standalone
    private static final String[] JVM_MEMORY_OPTS = {
        "-Xms256m",                          // Memória inicial
        "-Xmx1g",                            // Memória máxima
        "-XX:MaxMetaspaceSize=192m",         // Metaspace
        "-Xss256k",                          // Stack de threads
        "-XX:+UseG1GC",                      // Garbage Collector
        "-XX:MaxGCPauseMillis=100",          // Pausa máxima do GC
        "-XX:InitiatingHeapOccupancyPercent=45",
        "-XX:G1ReservePercent=15",
        "-XX:+UseStringDeduplication",       // Deduplicação de strings
        "-XX:+UseCompressedOops",            // Ponteiros comprimidos
        "-XX:+HeapDumpOnOutOfMemoryError",   // Dump em OOM
        "-XX:HeapDumpPath=logs/",
        "-XX:+ExitOnOutOfMemoryError"        // Sair em OOM
    };
    
    // Estado do processo
    private Process serverProcess;
    private Thread outputReaderThread;
    private Thread errorReaderThread;
    private volatile boolean isRunning = false;
    private int serverPort = DEFAULT_PORT;
    
    // Callbacks
    private Consumer<String> outputCallback;
    private Consumer<String> errorCallback;
    private Runnable onStartedCallback;
    private Runnable onStoppedCallback;
    
    // Singleton
    private static MobileServerProcessManager instance;
    
    private MobileServerProcessManager() {}
    
    public static synchronized MobileServerProcessManager getInstance() {
        if (instance == null) {
            instance = new MobileServerProcessManager();
        }
        return instance;
    }
    
    /**
     * Inicia o servidor mobile em processo separado
     * 
     * @return true se iniciou com sucesso
     */
    public boolean startServer() {
        return startServer(DEFAULT_PORT);
    }
    
    /**
     * Inicia o servidor mobile em processo separado na porta especificada
     * 
     * @param port Porta do servidor
     * @return true se iniciou com sucesso
     */
    public boolean startServer(int port) {
        if (isRunning) {
            logger.warn("Servidor mobile já está em execução");
            return false;
        }
        
        this.serverPort = port;
        
        try {
            // Verificar se a porta está disponível
            if (!PortManager.ensurePortAvailable(port)) {
                logger.error("Porta {} não está disponível", port);
                return false;
            }
            
            // Determinar modo de execução
            Path standaloneJar = findStandaloneJar();
            
            if (standaloneJar != null && Files.exists(standaloneJar)) {
                logger.info("Iniciando servidor mobile via JAR standalone: {}", standaloneJar);
                return startFromStandaloneJar(standaloneJar, port);
            } else {
                logger.info("Iniciando servidor mobile via classpath");
                return startFromClasspath(port);
            }
            
        } catch (Exception e) {
            logger.error("Erro ao iniciar servidor mobile", e);
            return false;
        }
    }
    
    /**
     * Inicia o servidor a partir do JAR standalone
     */
    private boolean startFromStandaloneJar(Path jarPath, int port) throws IOException {
        String javaPath = getJavaExecutable();
        
        List<String> command = new ArrayList<>();
        command.add(javaPath);
        
        // Adicionar opções de memória
        for (String opt : JVM_MEMORY_OPTS) {
            command.add(opt);
        }
        
        // JAR executável
        command.add("-jar");
        command.add(jarPath.toString());
        
        // Argumentos do Spring Boot
        command.add("--spring.profiles.active=mobile");
        command.add("--server.port=" + port);
        command.add("--server.address=0.0.0.0");
        command.add("--server.servlet.context-path=" + CONTEXT_PATH);
        
        return executeProcess(command, jarPath.getParent().toFile());
    }
    
    /**
     * Inicia o servidor a partir do classpath atual
     */
    private boolean startFromClasspath(int port) throws IOException {
        String javaPath = getJavaExecutable();
        String classpath = System.getProperty("java.class.path");
        
        List<String> command = new ArrayList<>();
        command.add(javaPath);
        
        // Adicionar opções de memória
        for (String opt : JVM_MEMORY_OPTS) {
            command.add(opt);
        }
        
        // Propriedades do sistema
        command.add("-Dspring.profiles.active=mobile");
        command.add("-Dspring.config.name=application");
        command.add("-Dspring.config.location=classpath:/");
        command.add("-Dserver.port=" + port);
        command.add("-Dserver.address=0.0.0.0");
        command.add("-Dserver.servlet.context-path=" + CONTEXT_PATH);
        command.add("-Dspring.jmx.enabled=false");
        command.add("-Dspring.main.lazy-initialization=true");
        command.add("-Dspring.data.jpa.repositories.bootstrap-mode=lazy");
        
        // Classpath e classe principal
        command.add("-cp");
        command.add(classpath);
        command.add("com.inventario.MobileApiApplication");
        
        return executeProcess(command, new File("."));
    }
    
    /**
     * Executa o processo do servidor
     */
    private boolean executeProcess(List<String> command, File workingDir) throws IOException {
        logger.info("Comando: {}", String.join(" ", command.subList(0, Math.min(5, command.size()))) + "...");
        
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir);
        processBuilder.redirectErrorStream(false);
        
        serverProcess = processBuilder.start();
        
        // Iniciar leitores de output
        startOutputReaders();
        
        // Aguardar inicialização
        return waitForStartup();
    }
    
    /**
     * Inicia threads para ler output do processo
     */
    private void startOutputReaders() {
        // Leitor de stdout
        outputReaderThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(serverProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.info("[MOBILE] {}", line);
                    if (outputCallback != null) {
                        outputCallback.accept(line);
                    }
                    
                    // Detectar inicialização bem-sucedida
                    if (line.contains("Started MobileApiApplication") ||
                        line.contains("Tomcat started on port")) {
                        isRunning = true;
                        if (onStartedCallback != null) {
                            onStartedCallback.run();
                        }
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    logger.error("Erro ao ler output do servidor", e);
                }
            }
        }, "MobileServer-Output");
        outputReaderThread.setDaemon(true);
        outputReaderThread.start();
        
        // Leitor de stderr
        errorReaderThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(serverProcess.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.warn("[MOBILE-ERR] {}", line);
                    if (errorCallback != null) {
                        errorCallback.accept(line);
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    logger.error("Erro ao ler stderr do servidor", e);
                }
            }
        }, "MobileServer-Error");
        errorReaderThread.setDaemon(true);
        errorReaderThread.start();
    }
    
    /**
     * Aguarda o servidor inicializar
     */
    private boolean waitForStartup() {
        logger.info("Aguardando inicialização do servidor (timeout: {}s)...", STARTUP_TIMEOUT_SECONDS);
        
        long startTime = System.currentTimeMillis();
        long timeout = STARTUP_TIMEOUT_SECONDS * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeout) {
            // Verificar se o processo ainda está rodando
            if (!serverProcess.isAlive()) {
                logger.error("Processo do servidor terminou inesperadamente");
                return false;
            }
            
            // Verificar health check
            if (isServerHealthy()) {
                logger.info("Servidor mobile iniciado com sucesso na porta {}", serverPort);
                isRunning = true;
                return true;
            }
            
            try {
                Thread.sleep(HEALTH_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        logger.error("Timeout aguardando inicialização do servidor");
        stopServer();
        return false;
    }
    
    /**
     * Verifica se o servidor está saudável
     */
    public boolean isServerHealthy() {
        try {
            java.net.URI uri = java.net.URI.create("http://localhost:" + serverPort + CONTEXT_PATH + "/actuator/health");
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Para o servidor mobile
     */
    public void stopServer() {
        if (serverProcess == null) {
            return;
        }
        
        logger.info("Parando servidor mobile...");
        isRunning = false;
        
        try {
            // Tentar shutdown graceful via endpoint
            try {
                java.net.URI uri = java.net.URI.create("http://localhost:" + serverPort + CONTEXT_PATH + "/actuator/shutdown");
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(5000);
                conn.getResponseCode();
                conn.disconnect();
                
                // Aguardar processo terminar
                if (serverProcess.waitFor(10, TimeUnit.SECONDS)) {
                    logger.info("Servidor parado graciosamente");
                    if (onStoppedCallback != null) {
                        onStoppedCallback.run();
                    }
                    return;
                }
            } catch (Exception e) {
                logger.debug("Shutdown graceful não disponível, forçando parada");
            }
            
            // Forçar parada
            serverProcess.destroy();
            if (!serverProcess.waitFor(5, TimeUnit.SECONDS)) {
                serverProcess.destroyForcibly();
            }
            
            logger.info("Servidor mobile parado");
            
        } catch (Exception e) {
            logger.error("Erro ao parar servidor", e);
            if (serverProcess != null) {
                serverProcess.destroyForcibly();
            }
        } finally {
            serverProcess = null;
            if (onStoppedCallback != null) {
                onStoppedCallback.run();
            }
        }
    }
    
    /**
     * Reinicia o servidor
     */
    public boolean restartServer() {
        stopServer();
        try {
            Thread.sleep(2000); // Aguardar liberação de recursos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return startServer(serverPort);
    }
    
    /**
     * Encontra o JAR standalone do servidor mobile
     */
    private Path findStandaloneJar() {
        // Locais possíveis
        String[] possiblePaths = {
            "dist/mobile-server/mobile-server.jar",
            "mobile-server/mobile-server.jar",
            "target/mobile-server/sistema-inventario-2.0.0.jar",
            "../mobile-server/mobile-server.jar"
        };
        
        for (String path : possiblePaths) {
            Path jarPath = Paths.get(path);
            if (Files.exists(jarPath)) {
                return jarPath;
            }
        }
        
        return null;
    }
    
    /**
     * Obtém o caminho do executável Java
     */
    private String getJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        
        // Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }
        
        return javaBin;
    }
    
    // Getters e Setters
    
    public boolean isRunning() {
        return isRunning && serverProcess != null && serverProcess.isAlive();
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public long getProcessId() {
        return serverProcess != null ? serverProcess.pid() : -1;
    }
    
    public String getServerUrl() {
        return "http://localhost:" + serverPort + CONTEXT_PATH;
    }
    
    public void setOutputCallback(Consumer<String> callback) {
        this.outputCallback = callback;
    }
    
    public void setErrorCallback(Consumer<String> callback) {
        this.errorCallback = callback;
    }
    
    public void setOnStartedCallback(Runnable callback) {
        this.onStartedCallback = callback;
    }
    
    public void setOnStoppedCallback(Runnable callback) {
        this.onStoppedCallback = callback;
    }
}
