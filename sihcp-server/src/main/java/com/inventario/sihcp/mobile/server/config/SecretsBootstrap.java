package com.inventario.sihcp.mobile.server.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap de segredos (fail-fast) da API Mobile.
 *
 * <p>Esta classe é executada pelo Spring via {@code @PostConstruct} logo após
 * o contexto ser criado e antes de qualquer endpoint ficar disponível. Sua
 * responsabilidade é validar que os segredos necessários para o funcionamento
 * seguro da aplicação estão presentes e satisfazem os requisitos mínimos:
 *
 * <ul>
 *     <li>{@code DATASOURCE_PASSWORD} — senha do banco de dados PostgreSQL
 *         (obrigatória, não vazia);</li>
 *     <li>{@code JWT_SECRET} — segredo usado para assinar/validar tokens JWT
 *         (obrigatório, não vazio, com no mínimo 32 caracteres para garantir
 *         entropia compatível com HMAC-SHA256).</li>
 * </ul>
 *
 * <p>Os valores são lidos dos placeholders
 * {@code ${spring.datasource.password:}} e {@code ${jwt.secret:}} definidos em
 * {@code application-mobile.properties}. O sufixo {@code :} com string vazia
 * garante que o Spring injete {@code ""} (em vez de lançar um erro críptico)
 * quando a variável de ambiente correspondente não estiver definida — isso
 * permite que esta classe capture a falha e emita uma mensagem clara.
 *
 * <p>Em caso de falha de validação, o servidor é abortado imediatamente
 * ({@link SpringApplication#exit(org.springframework.context.ConfigurableApplicationContext, org.springframework.boot.ExitCodeGenerator...)}
 * seguido de {@link System#exit(int)} com código 1), impedindo que a aplicação
 * inicie em um estado inseguro.
 *
 * <p>Referência: Documento de Design, seção "R1 — Credenciais do Servidor
 * via Variáveis de Ambiente" (spec correcoes-seguranca).
 *
 * <p>Requisitos validados: 1.1, 1.2, 1.3, 1.4, 1.6, 6.1, 6.2, 6.4.
 */
@Configuration
public class SecretsBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SecretsBootstrap.class);

    /**
     * Comprimento mínimo exigido para o {@code JWT_SECRET} (em caracteres).
     * Garante entropia compatível com HMAC-SHA256 (256 bits).
     */
    private static final int JWT_SECRET_MIN_LENGTH = 32;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    private final ApplicationContext ctx;

    public SecretsBootstrap(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Valida a presença e o comprimento dos segredos obrigatórios.
     *
     * <p>Se algum segredo estiver ausente, vazio ou abaixo do comprimento
     * mínimo, registra todas as falhas em log (banner visível) e aborta a
     * aplicação com código de saída {@code 1}. Caso contrário, registra uma
     * linha de confirmação informando que os segredos foram carregados com
     * sucesso a partir do ambiente.
     */
    @PostConstruct
    public void validate() {
        List<String> errors = new ArrayList<>();

        if (datasourcePassword == null || datasourcePassword.isBlank()) {
            errors.add("Variável de ambiente DATASOURCE_PASSWORD não definida");
        }

        if (jwtSecret == null || jwtSecret.isBlank()) {
            errors.add("Variável de ambiente JWT_SECRET não definida");
        } else if (jwtSecret.length() < JWT_SECRET_MIN_LENGTH) {
            errors.add("JWT_SECRET muito curto: " + jwtSecret.length()
                    + " chars (mínimo: " + JWT_SECRET_MIN_LENGTH + ")");
        }

        if (!errors.isEmpty()) {
            log.error("============================================================");
            log.error("FALHA DE BOOTSTRAP — CONFIGURAÇÃO DE SEGREDOS INCOMPLETA");
            log.error("============================================================");
            for (String e : errors) {
                log.error("  ✗ {}", e);
            }
            log.error("============================================================");
            log.error("Defina as variáveis de ambiente DATASOURCE_PASSWORD e JWT_SECRET");
            log.error("ou configure application-mobile-local.properties antes de iniciar.");
            log.error("============================================================");

            SpringApplication.exit(ctx, () -> 1);
            System.exit(1);
            return;
        }

        log.info("SecretsBootstrap OK — DATASOURCE_PASSWORD e JWT_SECRET carregados do ambiente");
    }
}
