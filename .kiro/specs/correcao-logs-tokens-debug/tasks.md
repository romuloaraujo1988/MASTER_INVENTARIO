# Implementation Tasks - Correção Logs Tokens Debug

## 1. Análise e Identificação

- [x] 1.1 Buscar todos os logs que podem conter tokens
  - Executar: `grep -r "Log\\.d.*token" InventarioMobile/app/src/`
  - Executar: `grep -r "Log\\.i.*token" InventarioMobile/app/src/`
  - Executar: `grep -r "getTokenDebugInfo" InventarioMobile/app/src/`
  - Documentar todos os locais encontrados
  - _Requirements: Security Checklist_

- [x] 1.2 Verificar LogSanitizer existente
  - Localizar arquivo `LogSanitizer.kt`
  - Verificar método `sanitize(message: String)`
  - Verificar regex para detectar JWT
  - Testar sanitização com token de exemplo
  - _Requirements: 2.8_

- [x] 1.3 Analisar SplashActivity
  - Abrir `ui/splash/SplashActivity.kt`
  - Localizar linha 138 com `Log.d(TAG, tokenManager.getTokenDebugInfo())`
  - Verificar contexto do log (quando é chamado)
  - Documentar comportamento atual
  - _Requirements: 1.1_

- [x] 1.4 Analisar TokenManager
  - Localizar arquivo `TokenManager.kt`
  - Encontrar método `getTokenDebugInfo()`
  - Verificar o que o método retorna
  - Documentar estrutura do retorno
  - _Requirements: 1.4_

## 2. Implementação da Correção

- [x] 2.1 Corrigir SplashActivity (Opção 1: LogSanitizer)
  - Abrir `ui/splash/SplashActivity.kt` linha 138
  - Substituir `Log.d(TAG, tokenManager.getTokenDebugInfo())`
  - Por: `LogSanitizer.log(TAG, tokenManager.getTokenDebugInfo())`
  - Adicionar import: `import com.inventario.mobile.util.LogSanitizer`
  - _Requirements: 2.1, 2.5, 2.9_


- [ ] 2.2 Corrigir SplashActivity (Opção 2: Remover em Release)
  - Envolver log em condicional: `if (BuildConfig.DEBUG) { ... }`
  - Garantir que log não aparece em builds de release
  - _Requirements: 2.7_

- [ ] 2.3 Modificar TokenManager.getTokenDebugInfo()
  - Abrir `TokenManager.kt`
  - Modificar método para retornar apenas informações não sensíveis:
    ```kotlin
    fun getTokenDebugInfo(): String {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        return """
            Access Token Valid: ${accessToken != null && accessToken.isNotEmpty()}
            Refresh Token Valid: ${refreshToken != null && refreshToken.isNotEmpty()}
            Token Expires At: ${getTokenExpiration()}
            Token Expired: ${isTokenExpired()}
        """.trimIndent()
    }
    ```
  - _Requirements: 2.4_

- [x] 2.4 Corrigir outros logs de tokens encontrados
  - Para cada local identificado em 1.1
  - Aplicar sanitização com `LogSanitizer.log()`
  - OU modificar para logar apenas informações não sensíveis
  - Documentar mudanças realizadas
  - _Requirements: 2.5, 2.9_

## 3. Testes de Validação

- [ ] 3.1 Testar sanitização do LogSanitizer
  - Criar teste unitário `LogSanitizerTest.kt`
  - Testar com JWT completo: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
  - Verificar que retorna token mascarado: `eyJ***...***[MASKED]`
  - Testar com múltiplos tokens na mesma mensagem
  - _Requirements: 2.8_

- [ ] 3.2 Testar logs no Logcat (Debug)
  - Compilar APK debug
  - Instalar e executar app
  - Fazer login para gerar tokens
  - Executar: `adb logcat | grep SplashActivity`
  - Verificar que tokens aparecem mascarados OU apenas informações não sensíveis
  - _Requirements: 2.2_

- [ ] 3.3 Testar que tokens completos não aparecem
  - Executar: `adb logcat | grep -E "eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+"`
  - Verificar que nenhum token completo é encontrado
  - Se encontrar, identificar origem e corrigir
  - _Requirements: 2.6_

- [ ] 3.4 Testar getTokenDebugInfo() modificado
  - Chamar método diretamente em teste
  - Verificar que retorno não contém tokens completos
  - Verificar que retorno contém informações úteis (validade, expiração)
  - Verificar que regex JWT não detecta tokens no retorno
  - _Requirements: 2.4_

## 4. Configuração ProGuard/R8

- [ ] 4.1 Atualizar proguard-rules.pro
  - Abrir `InventarioMobile/app/proguard-rules.pro`
  - Adicionar regras para remover logs de debug em release:
    ```proguard
    # Remove logs de debug em release
    -assumenosideeffects class android.util.Log {
        public static int d(...);
        public static int v(...);
    }
    ```
  - _Requirements: 2.7_

- [ ] 4.2 Testar build de release
  - Executar: `./gradlew assembleRelease`
  - Descompilar APK: `jadx app-release.apk`
  - Buscar por `Log.d` no código descompilado
  - Verificar que logs de debug foram removidos
  - _Requirements: 2.7_

- [ ] 4.3 Verificar que app funciona em release
  - Instalar APK de release em dispositivo
  - Fazer login e usar app normalmente
  - Verificar que não há crashes
  - Verificar que funcionalidades funcionam
  - _Requirements: 3.2_

## 5. Prevenção (Lint Rules)

- [ ] 5.1 Criar lint rule customizada (opcional)
  - Criar `NoTokenLoggingDetector.kt`
  - Implementar detector que identifica logs suspeitos
  - Configurar para alertar sobre `Log.d()` com "token" na mensagem
  - _Requirements: Prevention_

- [ ] 5.2 Executar lint e corrigir warnings
  - Executar: `./gradlew lint`
  - Revisar relatório de lint
  - Corrigir warnings relacionados a logs
  - _Requirements: Prevention_

## 6. Testes de Preservação

- [ ] 6.1 Testar autenticação funciona normalmente
  - Fazer login com usuário válido
  - Verificar que tokens são salvos corretamente
  - Verificar que requisições autenticadas funcionam
  - Verificar que refresh token funciona
  - _Requirements: 3.2_

- [ ] 6.2 Testar logs não sensíveis continuam funcionando
  - Verificar logs de navegação entre telas
  - Verificar logs de erros de rede
  - Verificar logs de validação de formulários
  - Garantir que apenas logs de tokens foram afetados
  - _Requirements: 3.1, 3.5_

- [ ] 6.3 Testar SharedPreferences criptografadas
  - Verificar que tokens são salvos criptografados
  - Verificar que não há logs ao salvar/carregar tokens
  - Verificar que criptografia funciona normalmente
  - _Requirements: 3.3_

- [ ] 6.4 Testar requisições HTTPS
  - Verificar que tokens são enviados em headers seguros
  - Verificar que HTTPS está sendo usado
  - Verificar que não há logs de headers com tokens
  - _Requirements: 3.4_

## 7. Testes de Crash Reports

- [ ] 7.1 Simular crash após login
  - Fazer login no app
  - Forçar crash (ex: throw RuntimeException)
  - Capturar crash report gerado
  - _Requirements: 2.3_

- [ ] 7.2 Analisar crash report
  - Verificar seção de Logcat no crash report
  - Buscar por tokens JWT no Logcat
  - Verificar que apenas tokens mascarados aparecem
  - Verificar que informações úteis para debug estão presentes
  - _Requirements: 2.3_

## 8. Documentação

- [ ] 8.1 Documentar uso do LogSanitizer
  - Criar `LOGGING_GUIDELINES.md`
  - Explicar quando usar `LogSanitizer.log()`
  - Fornecer exemplos de uso correto
  - Listar dados sensíveis que devem ser sanitizados
  - _Requirements: Documentation_

- [ ] 8.2 Atualizar guia de desenvolvimento
  - Adicionar seção sobre logging seguro
  - Explicar configuração do ProGuard
  - Documentar lint rules customizadas
  - _Requirements: Documentation_

- [ ] 8.3 Criar checklist de code review
  - Adicionar item: "Logs não expõem dados sensíveis"
  - Adicionar item: "LogSanitizer usado para logs de autenticação"
  - Adicionar item: "ProGuard configurado para remover logs em release"
  - _Requirements: Documentation_

## 9. Auditoria de Segurança

- [ ] 9.1 Revisar todo o código Android
  - Buscar por `Log.d`, `Log.i`, `Log.w`, `Log.e`
  - Verificar cada ocorrência para dados sensíveis
  - Aplicar sanitização onde necessário
  - Documentar locais revisados
  - _Requirements: Security Checklist_

- [ ] 9.2 Verificar conformidade LGPD/GDPR
  - Confirmar que tokens não são logados
  - Confirmar que dados pessoais não são logados
  - Confirmar que logs não são enviados para servidores externos
  - Preparar documentação de conformidade
  - _Requirements: 2.6_

- [ ] 9.3 Preparar relatório de correção
  - Documentar bug encontrado
  - Documentar correção implementada
  - Documentar testes realizados
  - Documentar medidas preventivas
  - _Requirements: Security Checklist_

## 10. Validação Final

- [ ] 10.1 Executar todos os testes
  - Testes unitários do LogSanitizer
  - Testes de integração de autenticação
  - Testes manuais no dispositivo
  - Verificar que todos passam
  - _Requirements: All_

- [ ] 10.2 Verificar Logcat limpo
  - Executar app completo (login, navegação, logout)
  - Capturar Logcat completo
  - Buscar por tokens JWT
  - Confirmar que nenhum token completo aparece
  - _Requirements: 2.2, 2.6_

- [ ] 10.3 Verificar build de release
  - Gerar APK de release
  - Descompilar e verificar ausência de logs
  - Instalar e testar em dispositivo
  - Confirmar que app funciona normalmente
  - _Requirements: 2.7_

- [ ] 10.4 Code review final
  - Revisar todas as mudanças no código
  - Verificar que apenas logs de tokens foram afetados
  - Verificar que comportamento do app não mudou
  - Aprovar mudanças
  - _Requirements: Preservation_

## 11. Deploy e Monitoramento

- [ ] 11.1 Atualizar CHANGELOG
  - Adicionar entrada sobre correção de segurança
  - Explicar que logs de tokens foram sanitizados
  - Referenciar documentação de logging seguro
  - _Requirements: Documentation_

- [ ] 11.2 Treinar equipe
  - Realizar sessão sobre logging seguro
  - Demonstrar uso do LogSanitizer
  - Explicar importância da sanitização
  - Responder dúvidas
  - _Requirements: Security Checklist_

- [ ] 11.3 Configurar monitoramento
  - Configurar alertas para logs suspeitos em produção
  - Monitorar crash reports para tokens expostos
  - Configurar revisão periódica de logs
  - _Requirements: Monitoring_

## Notas Importantes

### ⚠️ Segurança Crítica

- **NUNCA** logue tokens JWT completos
- **SEMPRE** use `LogSanitizer.log()` para logs de autenticação
- **SEMPRE** configure ProGuard para remover logs em release
- **SEMPRE** revise crash reports para dados sensíveis

### 📋 Checklist de Segurança

Antes de considerar a correção completa:
- [ ] Tokens não aparecem em Logcat
- [ ] LogSanitizer funciona corretamente
- [ ] ProGuard remove logs em release
- [ ] Crash reports não expõem tokens
- [ ] Documentação completa
- [ ] Equipe treinada
- [ ] Lint rules configuradas

### 🔍 Dados Sensíveis a Sanitizar

Além de tokens JWT, sanitizar:
- Senhas
- Tokens de API
- Chaves de criptografia
- Dados pessoais (CPF, email, telefone)
- Informações de cartão de crédito
- Cookies de sessão

### 📚 Referências

- [OWASP Mobile Top 10 - M2: Insecure Data Storage](https://owasp.org/www-project-mobile-top-10/)
- [Android Developers - Log](https://developer.android.com/reference/android/util/Log)
- [LGPD - Lei Geral de Proteção de Dados](http://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm)
