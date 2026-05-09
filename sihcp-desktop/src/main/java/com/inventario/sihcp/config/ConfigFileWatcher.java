package com.inventario.sihcp.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;

/**
 * Observador de mudanças no arquivo de configuração do banco de dados.
 *
 * <p>Monitora o arquivo {@code configuracao_banco.json} no diretório {@code config/}
 * local usando a API {@link WatchService} do Java NIO. Quando o arquivo é modificado
 * (evento {@link StandardWatchEventKinds#ENTRY_MODIFY}), chama
 * {@link MobileServerProcessManager#restart()} para que a API Mobile recarregue
 * as novas configurações.</p>
 *
 * <p>O watcher só é ativado quando o modo {@code APP_DIR} estiver ativo
 * (verificado via {@link ConfigurationPaths#getCurrentMode()}).</p>
 *
 * <p>O watcher roda em uma thread daemon separada para não bloquear a inicialização
 * do App Desktop.</p>
 *
 * @author Sistema de Inventário IFMT
 * @version 1.0.0
 */
@Component
public class ConfigFileWatcher {

    private static final Logger log = LoggerFactory.getLogger(ConfigFileWatcher.class);

    /** Nome do arquivo monitorado. */
    private static final String CONFIG_FILE_NAME = "configuracao_banco.json";

    /** Diretório monitorado (relativo ao diretório de trabalho). */
    private static final String CONFIG_DIR = "config";

    @Autowired
    private MobileServerProcessManager processManager;

    private WatchService watchService;
    private Thread watcherThread;
    private volatile boolean running = false;

    /**
     * Registra o watcher na inicialização do componente.
     *
     * <p>Só ativa o monitoramento quando o modo {@code APP_DIR} estiver ativo.
     * Em outros modos (ex.: {@code USER_HOME}), o watcher não é iniciado.</p>
     */
    @PostConstruct
    public void init() {
        if (ConfigurationPaths.getCurrentMode() != ConfigurationPaths.ConfigMode.APP_DIR) {
            log.info("ConfigFileWatcher: modo atual é '{}'. "
                    + "Monitoramento de configuração desativado (requer modo APP_DIR).",
                    ConfigurationPaths.getCurrentMode());
            return;
        }

        Path configDir = Paths.get(CONFIG_DIR);

        if (!Files.isDirectory(configDir)) {
            log.warn("ConfigFileWatcher: diretório '{}' não encontrado. "
                    + "Monitoramento de configuração não será iniciado.", configDir.toAbsolutePath());
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            running = true;
            watcherThread = new Thread(this::watchLoop, "config-file-watcher");
            watcherThread.setDaemon(true);
            watcherThread.start();

            log.info("ConfigFileWatcher: monitorando '{}' em '{}'.",
                    CONFIG_FILE_NAME, configDir.toAbsolutePath());

        } catch (IOException e) {
            log.error("ConfigFileWatcher: falha ao iniciar WatchService — monitoramento desativado.", e);
        }
    }

    /**
     * Para o watcher no shutdown do contexto Spring.
     */
    @PreDestroy
    public void destroy() {
        running = false;

        if (watchService != null) {
            try {
                watchService.close();
                log.info("ConfigFileWatcher: WatchService encerrado.");
            } catch (IOException e) {
                log.warn("ConfigFileWatcher: erro ao fechar WatchService.", e);
            }
        }

        if (watcherThread != null) {
            watcherThread.interrupt();
            log.info("ConfigFileWatcher: thread de monitoramento interrompida.");
        }
    }

    // -------------------------------------------------------------------------
    // Loop de monitoramento
    // -------------------------------------------------------------------------

    /**
     * Loop principal da thread daemon que processa eventos do {@link WatchService}.
     */
    private void watchLoop() {
        log.debug("ConfigFileWatcher: thread de monitoramento iniciada.");

        while (running) {
            WatchKey key;
            try {
                // Bloqueia até que um evento seja disponibilizado ou o serviço seja fechado
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("ConfigFileWatcher: thread interrompida.");
                break;
            } catch (ClosedWatchServiceException e) {
                log.debug("ConfigFileWatcher: WatchService fechado — encerrando loop.");
                break;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Ignorar eventos de overflow
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                Path changedFile = pathEvent.context();

                if (CONFIG_FILE_NAME.equals(changedFile.getFileName().toString())) {
                    log.info("ConfigFileWatcher: mudança detectada em '{}'. "
                            + "Reiniciando API Mobile...", CONFIG_FILE_NAME);
                    handleConfigChange();
                }
            }

            // Reregistrar a chave para continuar recebendo eventos
            boolean valid = key.reset();
            if (!valid) {
                log.warn("ConfigFileWatcher: chave de monitoramento inválida — "
                        + "diretório pode ter sido removido. Encerrando monitoramento.");
                break;
            }
        }

        log.debug("ConfigFileWatcher: thread de monitoramento encerrada.");
    }

    /**
     * Trata a mudança no arquivo de configuração reiniciando a API Mobile.
     */
    private void handleConfigChange() {
        try {
            processManager.restart();
            log.info("ConfigFileWatcher: API Mobile reiniciada com sucesso após mudança em '{}'.",
                    CONFIG_FILE_NAME);
        } catch (IOException e) {
            log.error("ConfigFileWatcher: falha ao reiniciar API Mobile após mudança em '{}'.",
                    CONFIG_FILE_NAME, e);
        }
    }
}
