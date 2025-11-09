package com.inventario.mobile.server.config;

import com.inventario.mobile.server.security.MobileJwtAuthenticationFilter;
import com.inventario.mobile.server.service.MobileAuthService;
import com.inventario.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração de segurança específica para endpoints mobile
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
// @Profile("mobile") // TEMPORARIAMENTE DESABILITADO PARA TESTE - Ativo sempre
@Order(1) // Prioridade alta para interceptar requests mobile primeiro
public class MobileSecurityConfig {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MobileSecurityConfig.class);

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private MobileAuthService mobileAuthService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private UserDetailsService userDetailsService;

    public MobileSecurityConfig() {
        logger.info("╔════════════════════════════════════════════════════════════════╗");
        logger.info("║  MOBILE SECURITY CONFIG CARREGADA - PROFILE MOBILE ATIVO      ║");
        logger.info("╚════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Configuração de segurança para endpoints mobile
     */
    @Bean
    public SecurityFilterChain mobileSecurityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando SecurityFilterChain para API Mobile...");
        
        http
                .securityMatcher("/api/mobile/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                    logger.info("Configurando regras de autorização...");
                    
                    authz
                        // Endpoints públicos - Health e Test (PRIMEIRO!)
                        .requestMatchers("/api/mobile/health").permitAll()
                        .requestMatchers("/api/mobile/test").permitAll()
                        .requestMatchers("/api/mobile/test/**").permitAll()
                        
                        // Endpoints públicos - Autenticação
                        .requestMatchers("/api/mobile/auth/login").permitAll()
                        .requestMatchers("/api/mobile/auth/validate").permitAll()

                        // TEMPORÁRIO: Liberar dashboard e coletas sem autenticação
                        .requestMatchers("/api/mobile/dashboard/**").permitAll()
                        .requestMatchers("/api/mobile/coletas/**").permitAll()
                        .requestMatchers("/api/mobile/patrimonios/**").permitAll()
                        .requestMatchers("/api/mobile/salas/**").permitAll()
                        .requestMatchers("/api/mobile/setores/**").permitAll()
                        .requestMatchers("/api/mobile/descricoes/**").permitAll()
                        .requestMatchers("/api/mobile/v1/connection/**").permitAll() // Monitor de conexões

                        // Endpoints protegidos
                        .requestMatchers("/api/mobile/sync/**").authenticated()
                        .requestMatchers("/api/mobile/auth/logout").authenticated()
                        .requestMatchers("/api/mobile/auth/refresh").authenticated()

                        // Qualquer outro endpoint mobile requer autenticação
                        .anyRequest().authenticated();
                    
                    logger.info("Regras de autorização configuradas!");
                });

        // IMPORTANTE: Criar e adicionar filtro JWT com dependências injetadas
        logger.info("Criando MobileJwtAuthenticationFilter...");
        MobileJwtAuthenticationFilter jwtFilter = new MobileJwtAuthenticationFilter(mobileAuthService,
                userDetailsService);
        logger.info("Adicionando MobileJwtAuthenticationFilter à cadeia de segurança");
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        logger.info("MobileJwtAuthenticationFilter adicionado com sucesso!");
        
        logger.info("SecurityFilterChain para API Mobile configurado!");

        return http.build();
    }

    /**
     * Configuração CORS para mobile
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir origens específicas para mobile (incluindo o IP configurado)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://192.168.10.107:8081",
            "https://192.168.10.107:8081",
            "http://localhost:8081",
            "https://localhost:8081",
            "http://10.0.2.2:8081",
            "https://10.0.2.2:8081"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Desabilitar credentials para permitir allowedOriginPatterns("*")
        configuration.setAllowCredentials(false);

        // Expor headers de resposta
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Tempo de cache para preflight
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/mobile/**", configuration);

        return source;
    }

    /**
     * Authentication Manager para mobile
     * Usa @Lazy para evitar referência circular
     */
    @Bean(name = "mobileAuthenticationManager")
    @org.springframework.context.annotation.Lazy
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password Encoder para mobile
     */
    @Bean(name = "passwordEncoder")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}