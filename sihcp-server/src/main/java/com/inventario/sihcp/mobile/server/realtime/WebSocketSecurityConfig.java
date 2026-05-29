package com.inventario.sihcp.mobile.server.realtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Filter chain dedicado a liberar o handshake do WebSocket nativo
 * ({@code /ws/**}) — sem este chain, as configurações principais
 * ({@link com.inventario.sihcp.mobile.server.config.MobileSecurityConfig}
 * com {@code securityMatcher("/api/mobile/**")} e
 * {@link com.inventario.sihcp.config.SecurityConfig} com {@code @Profile("!mobile")})
 * deixam o caminho desprotegido pelo Spring Security padrão, que por
 * default bloqueia tudo, devolvendo 403 no upgrade do WebSocket.
 *
 * <p><b>Ordem:</b> {@link Ordered#HIGHEST_PRECEDENCE} para garantir que este
 * chain seja avaliado <b>antes</b> do {@code MobileSecurityConfig} (que está
 * em {@code @Order(1)}).</p>
 *
 * <p><b>Autenticação:</b> aberta intencionalmente nesta versão. O cliente
 * Android envia o token JWT como header {@code Authorization} no handshake,
 * mas a validação ainda não está implementada — está marcado como TODO no
 * handler. Como o broadcast é apenas leitura e não expõe dados sensíveis
 * fora do que já vai por HTTP, é aceitável temporariamente.</p>
 */
@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WebSocketSecurityConfig {

    @Bean
    public SecurityFilterChain webSocketFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/ws/**", "/ws-coletas-live", "/ws-coletas-live/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }
}
