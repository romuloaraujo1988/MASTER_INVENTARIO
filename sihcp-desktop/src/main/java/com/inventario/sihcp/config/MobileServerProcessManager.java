package com.inventario.sihcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Gerenciador do processo da API Mobile.
 *
 * <p>Responsável por iniciar, parar e reiniciar o processo da API Mobile
 * ({@code bin/mobile-server.jar}) via {@link ProcessBuilder}. Deve ser usado
 * pelo App Desktop quando o modo {@code APP_DIR} estiver ativo, permitindo
 * que mudanças no arquivo de configuração reflitam imediatamente na API.</p>
 *
 * <p>Parâmetros de inicialização do JAR:</p>
 * <pre>
 *   java -Xms256m -Xmx1g
 *        -Dinventario.config.mode=APP_DIR
 *        -jar bin/mobile-server.jar
 *        --spring.profiles.active=mobile,prod
 *        --spring.config.additional-location=config/
 * </pre>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@Component
public class MobileServerProcessManager {

    private static final Logger log = LoggerFactory.getLogger(MobileServerProcessManager.class);

    /** Timeout em segundos para aguardar o encerramento gracioso do processo. */
    private static final int STOP_TIMEOUT_SECONDS = 10;

    /** Processo gerenciado atualmente (pode ser {@code null} se não iniciado). */
    private Process managedProcess;

    /**
     * Inicia o processo da API Mobile.
     *
     * <p>Se já houver um processo em execução, ele será encerrado antes de
     * iniciar um novo.</p>
     *
     * @throws IOException se ocorrer erro ao iniciar o processo
     */
    public synchronized void start() throws IOException {
        if (managedProcess != null && managedProcess.isAlive()) {
            log.warn("MobileServerProcessManager.start: processo já está em execução (PID {}). "
                    + "Encerrando antes de reiniciar.", processId(managedProcess));
            stopInternal();
        }

        List<String> command = Arrays.asList(
                "java",
                "-Xms256m",
                "-Xmx1g",
                "-Dinventario.config.mode=APP_DIR",
                "-jar",
                "bin/mobile-server.jar",
                "--spring.profiles.active=mobile,prod",
                "--spring.config.additional-location=config/"
        );

        log.info("MobileServerProcessManager.start: iniciando API Mobile com comando: {}", command);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO(); // redireciona stdout/stderr para o processo pai
        managedProcess = pb.start();

        log.info("MobileServerProcessManager.start: API Mobile iniciada (PID {}).",
                processId(managedProcess));
    }

    /**
     * Para o processo da API Mobile.
     *
     * <p>Tenta encerrar o processo de forma graciosa via {@link Process#destroy()}.
     * Se o processo não encerrar dentro de {@value #STOP_TIMEOUT_SECONDS} segundos,
     * força o encerramento via {@link Process#destroyForcibly()}.</p>
     */
    public synchronized void stop() {
        if (managedProcess == null) {
            log.debug("MobileServerProcessManager.stop: nenhum processo gerenciado.");
            return;
        }

        stopInternal();
    }

    /**
     * Para e reinicia o processo da API Mobile.
     *
     * @throws IOException se ocorrer erro ao iniciar o novo processo
     */
    public synchronized void restart() throws IOException {
        log.info("MobileServerProcessManager.restart: reiniciando API Mobile...");
        stop();
        start();
        log.info("MobileServerProcessManager.restart: API Mobile reiniciada com sucesso.");
    }

    // -------------------------------------------------------------------------
    // Métodos internos
    // -------------------------------------------------------------------------

    /**
     * Encerra o processo gerenciado sem verificar se ele existe.
     * Deve ser chamado apenas quando {@code managedProcess != null}.
     */
    private void stopInternal() {
        long pid = processId(managedProcess);
        log.info("MobileServerProcessManager.stop: encerrando processo (PID {})...", pid);

        managedProcess.destroy();

        try {
            boolean terminated = managedProcess.waitFor(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("MobileServerProcessManager.stop: processo (PID {}) não encerrou em {}s. "
                        + "Forçando encerramento.", pid, STOP_TIMEOUT_SECONDS);
                managedProcess.destroyForcibly();
                managedProcess.waitFor(5, TimeUnit.SECONDS);
            } else {
                log.info("MobileServerProcessManager.stop: processo (PID {}) encerrado com código {}.",
                        pid, managedProcess.exitValue());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("MobileServerProcessManager.stop: interrompido aguardando encerramento do processo (PID {}).",
                    pid);
            managedProcess.destroyForcibly();
        } finally {
            managedProcess = null;
        }
    }

    /**
     * Retorna o PID do processo ou {@code -1} se não disponível.
     */
    private static long processId(Process process) {
        try {
            return process.pid();
        } catch (UnsupportedOperationException e) {
            return -1L;
        }
    }

    /**
     * Verifica se o processo gerenciado está em execução.
     *
     * @return {@code true} se o processo está ativo
     */
    public boolean isRunning() {
        return managedProcess != null && managedProcess.isAlive();
    }
}
