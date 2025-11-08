# Especificação Técnica - API Mobile para Sistema de Inventário

## 1. Visão Geral

Este documento detalha as especificações técnicas para implementação das APIs REST que suportarão o aplicativo mobile Android do sistema de inventário.

## 2. Arquitetura da API

### 2.1 Estrutura de Pacotes
```
src/main/java/com/inventario/
├── mobile/
│   ├── controller/     # Controllers REST para mobile
│   ├── dto/           # Data Transfer Objects
│   ├── service/       # Serviços específicos mobile
│   └── config/        # Configurações mobile
├── security/
│   ├── jwt/           # Implementação JWT
│   └── mobile/        # Segurança específica mobile
└── util/
    └── mobile/        # Utilitários mobile
```

## 3. Endpoints da API

### 3.1 Autenticação Mobile

#### POST /api/mobile/auth/login
**Descrição**: Autenticação de usuário mobile

**Request Body**:
```json
{
  "username": "string",
  "password": "string",
  "deviceId": "string",
  "appVersion": "string"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_here",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "operador01",
      "nome": "João Silva",
      "setorId": 5,
      "setorNome": "TI",
      "campusId": 1,
      "campusNome": "Campus Principal"
    }
  }
}
```

#### POST /api/mobile/auth/refresh
**Descrição**: Renovação de token JWT

**Request Body**:
```json
{
  "refreshToken": "string"
}
```

#### POST /api/mobile/auth/logout
**Descrição**: Logout e invalidação de token

### 3.2 Sincronização de Dados

#### GET /api/mobile/sync/patrimonio
**Descrição**: Sincronização de patrimônios por setor

**Query Parameters**:
- `setorId` (optional): ID do setor
- `lastSync` (optional): Timestamp da última sincronização
- `page` (optional): Página (default: 0)
- `size` (optional): Tamanho da página (default: 100)

**Response**:
```json
{
  "success": true,
  "data": {
    "patrimonios": [
      {
        "id": 1,
        "codigo": "123456",
        "descricao": "Notebook Dell",
        "setorId": 5,
        "setorNome": "TI",
        "salaId": 10,
        "salaNome": "Sala 101",
        "status": "ATIVO",
        "jaColetado": false,
        "ultimaColeta": null,
        "qrCode": "QR123456"
      }
    ],
    "totalElements": 150,
    "totalPages": 2,
    "currentPage": 0,
    "lastSync": "2024-01-15T10:30:00Z"
  }
}
```

#### GET /api/mobile/sync/setores
**Descrição**: Lista de setores disponíveis

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 5,
      "nome": "Tecnologia da Informação",
      "campusId": 1,
      "campusNome": "Campus Principal",
      "ativo": true
    }
  ]
}
```

#### GET /api/mobile/sync/salas/{setorId}
**Descrição**: Lista de salas por setor

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "nome": "Sala 101",
      "setorId": 5,
      "andar": "1º Andar",
      "capacidade": 20
    }
  ]
}
```

### 3.3 Operações de Patrimônio

#### GET /api/mobile/patrimonio/{codigo}
**Descrição**: Busca patrimônio por código

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "codigo": "123456",
    "descricao": "Notebook Dell Inspiron 15",
    "marca": "Dell",
    "modelo": "Inspiron 15 3000",
    "numeroSerie": "ABC123XYZ",
    "setorId": 5,
    "setorNome": "TI",
    "salaId": 10,
    "salaNome": "Sala 101",
    "responsavel": "João Silva",
    "status": "ATIVO",
    "jaColetado": false,
    "ultimaColeta": null,
    "observacoes": "Em bom estado"
  }
}
```

#### GET /api/mobile/patrimonio/qr/{qrCode}
**Descrição**: Busca patrimônio por QR Code

### 3.4 Coleta de Dados

#### POST /api/mobile/coleta
**Descrição**: Registra coleta de patrimônio

**Request Body**:
```json
{
  "patrimonioId": 1,
  "patrimonioCodigo": "123456",
  "usuarioId": 10,
  "dataColeta": "2024-01-15T14:30:00Z",
  "observacoes": "Item encontrado em bom estado",
  "latitude": -15.123456,
  "longitude": -56.789012,
  "fotoBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "situacaoEncontrada": "ENCONTRADO",
  "estadoConservacao": "BOM"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "coletaId": 100,
    "patrimonioCodigo": "123456",
    "dataColeta": "2024-01-15T14:30:00Z",
    "status": "COLETADO"
  }
}
```

#### POST /api/mobile/coleta/batch
**Descrição**: Registra múltiplas coletas (sincronização offline)

**Request Body**:
```json
{
  "coletas": [
    {
      "patrimonioId": 1,
      "patrimonioCodigo": "123456",
      "usuarioId": 10,
      "dataColeta": "2024-01-15T14:30:00Z",
      "observacoes": "Item encontrado",
      "situacaoEncontrada": "ENCONTRADO"
    }
  ]
}
```

#### GET /api/mobile/coleta/status/{patrimonioCodigo}
**Descrição**: Verifica status de coleta de um patrimônio

## 4. DTOs (Data Transfer Objects)

### 4.1 MobileLoginRequest
```java
public class MobileLoginRequest {
    private String username;
    private String password;
    private String deviceId;
    private String appVersion;
    // getters e setters
}
```

### 4.2 MobileLoginResponse
```java
public class MobileLoginResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private MobileUserInfo user;
    // getters e setters
}
```

### 4.3 MobilePatrimonioDTO
```java
public class MobilePatrimonioDTO {
    private Long id;
    private String codigo;
    private String descricao;
    private String marca;
    private String modelo;
    private String numeroSerie;
    private Long setorId;
    private String setorNome;
    private Long salaId;
    private String salaNome;
    private String responsavel;
    private String status;
    private Boolean jaColetado;
    private LocalDateTime ultimaColeta;
    private String qrCode;
    private String observacoes;
    // getters e setters
}
```

### 4.4 MobileColetaRequest
```java
public class MobileColetaRequest {
    private Long patrimonioId;
    private String patrimonioCodigo;
    private Long usuarioId;
    private LocalDateTime dataColeta;
    private String observacoes;
    private Double latitude;
    private Double longitude;
    private String fotoBase64;
    private String situacaoEncontrada;
    private String estadoConservacao;
    // getters e setters
}
```

## 5. Controllers

### 5.1 MobileAuthController
```java
@RestController
@RequestMapping("/api/mobile/auth")
@CrossOrigin(origins = "*")
public class MobileAuthController {
    
    @Autowired
    private MobileAuthService mobileAuthService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MobileLoginResponse>> login(
            @RequestBody @Valid MobileLoginRequest request) {
        try {
            MobileLoginResponse response = mobileAuthService.authenticate(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Erro na autenticação: " + e.getMessage()));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<MobileLoginResponse>> refresh(
            @RequestBody RefreshTokenRequest request) {
        // implementação
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            HttpServletRequest request) {
        // implementação
    }
}
```

### 5.2 MobileSyncController
```java
@RestController
@RequestMapping("/api/mobile/sync")
@CrossOrigin(origins = "*")
public class MobileSyncController {
    
    @Autowired
    private MobileSyncService mobileSyncService;
    
    @GetMapping("/patrimonio")
    public ResponseEntity<ApiResponse<PagedResponse<MobilePatrimonioDTO>>> 
            syncPatrimonio(
                @RequestParam(required = false) Long setorId,
                @RequestParam(required = false) String lastSync,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "100") int size) {
        // implementação
    }
    
    @GetMapping("/setores")
    public ResponseEntity<ApiResponse<List<MobileSetorDTO>>> syncSetores() {
        // implementação
    }
    
    @GetMapping("/salas/{setorId}")
    public ResponseEntity<ApiResponse<List<MobileSalaDTO>>> 
            syncSalas(@PathVariable Long setorId) {
        // implementação
    }
}
```

### 5.3 MobilePatrimonioController
```java
@RestController
@RequestMapping("/api/mobile/patrimonio")
@CrossOrigin(origins = "*")
public class MobilePatrimonioController {
    
    @Autowired
    private MobilePatrimonioService mobilePatrimonioService;
    
    @GetMapping("/{codigo}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> 
            buscarPorCodigo(@PathVariable String codigo) {
        // implementação
    }
    
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<ApiResponse<MobilePatrimonioDTO>> 
            buscarPorQrCode(@PathVariable String qrCode) {
        // implementação
    }
}
```

### 5.4 MobileColetaController
```java
@RestController
@RequestMapping("/api/mobile/coleta")
@CrossOrigin(origins = "*")
public class MobileColetaController {
    
    @Autowired
    private MobileColetaService mobileColetaService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<MobileColetaResponse>> 
            registrarColeta(@RequestBody @Valid MobileColetaRequest request) {
        // implementação
    }
    
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<BatchColetaResponse>> 
            registrarColetasBatch(@RequestBody @Valid BatchColetaRequest request) {
        // implementação
    }
    
    @GetMapping("/status/{patrimonioCodigo}")
    public ResponseEntity<ApiResponse<ColetaStatusDTO>> 
            verificarStatus(@PathVariable String patrimonioCodigo) {
        // implementação
    }
}
```

## 6. Services

### 6.1 MobileAuthService
```java
@Service
public class MobileAuthService {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    public MobileLoginResponse authenticate(MobileLoginRequest request) {
        // Validar credenciais
        Usuario usuario = usuarioService.autenticar(
            request.getUsername(), request.getPassword());
        
        if (usuario == null) {
            throw new AuthenticationException("Credenciais inválidas");
        }
        
        // Gerar tokens
        String token = jwtTokenProvider.generateToken(usuario);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);
        
        // Registrar login mobile
        registrarLoginMobile(usuario, request.getDeviceId(), request.getAppVersion());
        
        return new MobileLoginResponse(token, refreshToken, 3600L, 
            new MobileUserInfo(usuario));
    }
    
    private void registrarLoginMobile(Usuario usuario, String deviceId, String appVersion) {
        // Implementar log de acesso mobile
    }
}
```

### 6.2 MobileSyncService
```java
@Service
public class MobileSyncService {
    
    @Autowired
    private PatrimonioService patrimonioService;
    
    @Autowired
    private SetorService setorService;
    
    @Autowired
    private SalaService salaService;
    
    public PagedResponse<MobilePatrimonioDTO> syncPatrimonio(
            Long setorId, String lastSync, int page, int size) {
        
        LocalDateTime ultimaSync = null;
        if (lastSync != null) {
            ultimaSync = LocalDateTime.parse(lastSync);
        }
        
        Page<Patrimonio> patrimonios = patrimonioService
            .buscarParaSincronizacao(setorId, ultimaSync, page, size);
        
        List<MobilePatrimonioDTO> dtos = patrimonios.getContent().stream()
            .map(this::convertToMobileDTO)
            .collect(Collectors.toList());
        
        return new PagedResponse<>(dtos, patrimonios.getTotalElements(),
            patrimonios.getTotalPages(), page, LocalDateTime.now());
    }
    
    private MobilePatrimonioDTO convertToMobileDTO(Patrimonio patrimonio) {
        // Converter entidade para DTO mobile
    }
}
```

## 7. Segurança JWT

### 7.1 JwtTokenProvider
```java
@Component
public class JwtTokenProvider {
    
    private String jwtSecret = "mobileInventarioSecret";
    private int jwtExpirationInMs = 3600000; // 1 hora
    
    public String generateToken(Usuario usuario) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMs);
        
        return Jwts.builder()
            .setSubject(usuario.getUsername())
            .claim("userId", usuario.getId())
            .claim("setorId", usuario.getSetorId())
            .claim("type", "mobile")
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String generateRefreshToken(Usuario usuario) {
        Date expiryDate = new Date(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)); // 7 dias
        
        return Jwts.builder()
            .setSubject(usuario.getUsername())
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

### 7.2 JwtAuthenticationFilter
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            Usuario usuario = usuarioService.buscarPorUsername(username);
            
            if (usuario != null) {
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

## 8. Configuração de Segurança

### 8.1 MobileSecurityConfig
```java
@Configuration
@EnableWebSecurity
public class MobileSecurityConfig {
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
    
    @Bean
    public SecurityFilterChain mobileFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/mobile/**")
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/mobile/auth/login").permitAll()
                .requestMatchers("/api/mobile/auth/refresh").permitAll()
                .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 9. Tratamento de Erros

### 9.1 ApiResponse
```java
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        response.timestamp = LocalDateTime.now();
        return response;
    }
    
    // getters e setters
}
```

### 9.2 GlobalExceptionHandler
```java
@ControllerAdvice
public class MobileExceptionHandler {
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(
            AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Erro de autenticação: " + ex.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(
            ValidationException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Erro de validação: " + ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(
            Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Erro interno do servidor"));
    }
}
```

## 10. Configurações Adicionais

### 10.1 application.yml (Mobile)
```yaml
mobile:
  jwt:
    secret: ${MOBILE_JWT_SECRET:mobileInventarioSecret}
    expiration: 3600000 # 1 hora
    refresh-expiration: 604800000 # 7 dias
  sync:
    batch-size: 100
    max-photo-size: 5242880 # 5MB
  security:
    allowed-origins: "*"
    rate-limit:
      requests-per-minute: 60
```

### 10.2 Dependências Maven
```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

**Documento criado em**: $(date)
**Versão**: 1.0
**Responsável**: Equipe Backend