package com.inventario;

import com.inventario.util.PortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Classe principal para a API Mobile do Sistema de Inventário
 * Esta classe inicializa o servidor Spring Boot para os endpoints mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.inventario.mobile.server",
    "com.inventario.service",
    "com.inventario.dao",
    "com.inventario.config",
    "com.inventario.util",
    "com.inventario.security"
})
@EntityScan(basePackages = "com.inventario.model")
public class MobileApiApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileApiApplication.class);
    private static final int DEFAULT_PORT = 8081;
    
    public static void main(String[] args) {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║  Sistema de Inventário - Servidor Mobile API                  ║");
            System.out.println("║  Versão 1.2.0                                                  ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
            
            // Verificar e liberar porta se necessário
            logger.info("╔════════════════════════════════════════════════════════════════");
            logger.info("║ VERIFICANDO PORTA {}", DEFAULT_PORT);
            logger.info("╚════════════════════════════════════════════════════════════════");
            
            boolean portAvailable = PortManager.ensurePortAvailable(DEFAULT_PORT);
            
            if (!portAvailable) {
                logger.error("╔════════════════════════════════════════════════════════════════");
                logger.error("║ ERRO: Porta {} não pôde ser liberada", DEFAULT_PORT);
                logger.error("╚════════════════════════════════════════════════════════════════");
                
                System.err.println("\n╔════════════════════════════════════════════════════════════════╗");
                System.err.println("║  ERRO: Porta " + DEFAULT_PORT + " está em uso e não pôde ser liberada      ║");
                System.err.println("║                                                                ║");
                System.err.println("║  A porta pode estar sendo usada por:                           ║");
                System.err.println("║  - Outra instância deste servidor                             ║");
                System.err.println("║  - Outro aplicativo na porta " + DEFAULT_PORT + "                            ║");
                System.err.println("║  - Processo sem permissão para ser terminado                   ║");
                System.err.println("║                                                                ║");
                System.err.println("║  Soluções:                                                     ║");
                System.err.println("║  1. Feche manualmente o processo usando a porta " + DEFAULT_PORT + "         ║");
                System.err.println("║     Windows: netstat -ano | findstr :" + DEFAULT_PORT + "                    ║");
                System.err.println("║              taskkill /F /PID <PID>                            ║");
                System.err.println("║  2. Execute como Administrador                                 ║");
                System.err.println("║  3. Use porta diferente: --server.port=XXXX                    ║");
                System.err.println("╚════════════════════════════════════════════════════════════════╝\n");
                
                System.exit(1);
            }
            
            logger.info("╔════════════════════════════════════════════════════════════════");
            logger.info("║ Porta {} disponível - Iniciando servidor...", DEFAULT_PORT);
            logger.info("╚════════════════════════════════════════════════════════════════");
            
            // Configurar propriedades do sistema para a API mobile
            System.setProperty("spring.application.name", "inventario-mobile-api");
            
            // Adicionar shutdown hook para liberar recursos
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("╔════════════════════════════════════════════════════════════════");
                logger.info("║ Encerrando servidor mobile...");
                logger.info("╚════════════════════════════════════════════════════════════════");
            }));
            
            // Desabilitar sons do desktop quando servidor mobile está rodando
            // Os sons devem tocar apenas no dispositivo que faz a coleta (Android)
            try {
                Class<?> soundClass = Class.forName("com.inventario.util.SoundNotification");
                java.lang.reflect.Method method = soundClass.getMethod("setSoundsEnabled", boolean.class);
                method.invoke(null, false);
                logger.info("Sons de notificação do desktop desabilitados (servidor mobile ativo)");
            } catch (Exception e) {
                logger.warn("Não foi possível desabilitar sons: " + e.getMessage());
            }
            
            // Inicializar Spring Boot
            SpringApplication.run(MobileApiApplication.class, args);
            
        } catch (Exception e) {
            logger.error("╔════════════════════════════════════════════════════════════════");
            logger.error("║ ERRO FATAL ao iniciar servidor mobile");
            logger.error("╚════════════════════════════════════════════════════════════════", e);
            
            System.err.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.err.println("║  ERRO FATAL ao iniciar servidor mobile                         ║");
            System.err.println("║                                                                ║");
            System.err.println("║  Erro: " + (e.getMessage() != null ? e.getMessage() : "Desconhecido") + "                                                   ║");
            System.err.println("║                                                                ║");
            System.err.println("║  Verifique:                                                    ║");
            System.err.println("║  - Banco de dados está acessível                               ║");
            System.err.println("║  - Configurações em application-mobile.properties              ║");
            System.err.println("║  - Logs acima para mais detalhes                               ║");
            System.err.println("╚════════════════════════════════════════════════════════════════╝\n");
            
            e.printStackTrace();
            System.exit(1);
        }
    }
}