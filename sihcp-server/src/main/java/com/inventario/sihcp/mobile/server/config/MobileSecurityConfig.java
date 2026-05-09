package com.inventario.sihcp.mobile.server.config;

import com.inventario.sihcp.mobile.server.security.MobileJwtAuthenticationFilter;
import com.inventario.sihcp.mobile.server.service.MobileAuthService;
import com.inventario.sihcp.security.JwtAuthenticationEntryPoint;
import com.inventario.sihcp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
 * Hierarquia de Perfis:
 * - ADMIN: Acesso total (gerenciamento de usuários, configurações, etc.)
 * - SUPERVISOR: Supervisiona coletas, relatórios, pode ver todos os dados
 * - COLETOR: Pode realizar coletas e ver seus próprios dados
 * - CONSULTA: Apenas visualização de dados
 * 
 * @author Sistema de Inventário
 * @version 2.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public MobileSecurityConfig() {
        logger.info("╔════════════════════════════════════════════════════════════════╗");
        logger.info("║  MOBILE SECURITY CONFIG v2.0 - SEGURANÇA POR ROLES ATIVA      ║");
        logger.info("╚════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Configuração de segurança para endpoints mobile
     * 
     * Endpoints públicos (sem autenticação):
     * - /api/mobile/health - Health check
     * - /api/mobile/test/** - Testes
     * - /api/mobile/auth/login - Login
     * - /api/mobile/auth/validate - Validação de token
     * 
     * Endpoints protegidos (requerem autenticação):
     * - Todos os outros endpoints /api/mobile/**
     * 
     * Controle de acesso por role é feito via @PreAuthorize nos controllers
     */
    @Bean
    public SecurityFilterChain mobileSecurityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando SecurityFilterChain para API Mobile com segurança por roles...");
        
        http
                .securityMatcher("/api/mobile/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> {
                    logger.info("Configurando regras de autorização...");
                    
                    authz
                        // ═══════════════════════════════════════════════════════════════
                        // ENDPOINTS PÚBLICOS (sem autenticação)
                        // ═══════════════════════════════════════════════════════════════
                        
                        // Health check e testes
                        .requestMatchers("/api/mobile/health").permitAll()
                        .requestMatchers("/api/mobile/test").permitAll()
                        .requestMatchers("/api/mobile/test/**").permitAll()
                        
                        // Autenticação
                        .requestMatchers("/api/mobile/auth/login").permitAll()
                        .requestMatchers("/api/mobile/auth/validate").permitAll()
                        
                        // Monitor de conexões (para debug)
                        .requestMatchers("/api/mobile/v1/connection/**").permitAll()
                        
                        // Informações do campus (público — app precisa identificar o campus antes do login)
                        .requestMatchers("/api/mobile/campus/info").permitAll()
                        
                        // ═══════════════════════════════════════════════════════════════
                        // ENDPOINTS PROTEGIDOS (requerem autenticação)
                        // O controle fino de acesso é feito via @PreAuthorize nos controllers
                        // ═══════════════════════════════════════════════════════════════
                        
                        // Refresh token requer autenticação
                        .requestMatchers("/api/mobile/auth/refresh").authenticated()
                        .requestMatchers("/api/mobile/auth/logout").authenticated()
                        
                        // Dashboard - qualquer usuário autenticado
                        .requestMatchers("/api/mobile/dashboard/**").authenticated()
                        
                        // Coletas - requer role COLETOR ou superior
                        .requestMatchers("/api/mobile/coletas/**").authenticated()
                        
                        // Patrimônios - qualquer usuário autenticado pode consultar
                        .requestMatchers("/api/mobile/patrimonios/**").authenticated()
                        .requestMatchers("/api/mobile/patrimonio/**").authenticated()
                        
                        // Salas e Setores - qualquer usuário autenticado
                        .requestMatchers("/api/mobile/salas/**").authenticated()
                        .requestMatchers("/api/mobile/setores/**").authenticated()
                        
                        // Descrições - qualquer usuário autenticado
                        .requestMatchers("/api/mobile/descricoes/**").authenticated()
                        
                        // Responsáveis - qualquer usuário autenticado
                        .requestMatchers("/api/mobile/responsaveis/**").authenticated()
                        
                        // Inventários - qualquer usuário autenticado
                        .requestMatchers("/api/mobile/inventario/**").authenticated()
                        
                        // Sincronização - requer autenticação
                        .requestMatchers("/api/mobile/sync/**").authenticated()
                        
                        // Usuários - controle fino via @PreAuthorize
                        .requestMatchers("/api/mobile/usuarios/**").authenticated()
                        
                        // Dispositivos - controle fino via @PreAuthorize
                        .requestMatchers("/api/mobile/dispositivos/**").authenticated()
                        
                        // Qualquer outro endpoint mobile requer autenticação
                        .anyRequest().authenticated();
                    
                    logger.info("Regras de autorização configuradas com sucesso!");
                });

        // Criar e adicionar filtro JWT
        logger.info("Criando MobileJwtAuthenticationFilter...");
        MobileJwtAuthenticationFilter jwtFilter = new MobileJwtAuthenticationFilter(mobileAuthService, userDetailsService, jwtTokenProvider);
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

        // Permitir origens específicas para mobile
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
