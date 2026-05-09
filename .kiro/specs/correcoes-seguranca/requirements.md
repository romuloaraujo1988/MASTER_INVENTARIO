# Documento de Requisitos

## Introdução

Esta feature cobre a correção de 12 vulnerabilidades de segurança identificadas no SIHCP (Sistema de Histórico e Coleta Patrimonial), distribuídas entre o servidor Spring Boot (`sihcp-server`) e o aplicativo Android (`InventarioMobile`). As vulnerabilidades foram classificadas em três níveis de severidade — crítica, alta e média — e abrangem exposição de credenciais no repositório Git, configurações inseguras de rede, ausência de proteção contra força bruta, logging inadequado de tokens e permissões desnecessárias no app Android.

O objetivo é eliminar cada vulnerabilidade sem quebrar funcionalidades existentes, seguindo as boas práticas de segurança para aplicações Spring Boot 3.2 e Android (minSdk 23, targetSdk 34).

---

## Glossário

- **Servidor**: Aplicação Spring Boot 3.2 localizada em `sihcp-server/`.
- **App**: Aplicativo Android Kotlin localizado em `InventarioMobile/`.
- **JWT_Access_Token**: Token de curta duração usado para autenticar requisições à API mobile.
- **JWT_Refresh_Token**: Token de longa duração usado para renovar o JWT_Access_Token.
- **JWT_Secret**: Chave HMAC-SHA256 usada para assinar e verificar tokens JWT.
- **Datasource_Password**: Senha de conexão com o banco de dados PostgreSQL.
- **Keystore**: Arquivo `.jks` ou `.keystore` contendo o certificado de assinatura do APK de release.
- **Keystore_Properties**: Arquivo `keystore.properties` com credenciais de acesso ao Keystore.
- **SQLCipher**: Biblioteca de criptografia do banco de dados SQLite local do App.
- **SQLCipher_Passphrase**: Chave de criptografia usada pelo SQLCipher para proteger o banco local.
- **Android_Keystore**: Sistema de armazenamento seguro de chaves criptográficas do Android OS.
- **EncryptedSharedPreferences**: API Android para armazenamento seguro de pares chave-valor.
- **ProGuard_R8**: Ferramenta de ofuscação e minificação de código para builds de release Android.
- **CORS**: Mecanismo de controle de acesso entre origens distintas (Cross-Origin Resource Sharing).
- **Rate_Limiter**: Componente que limita o número de requisições por unidade de tempo.
- **Cleartext_Traffic**: Tráfego HTTP sem criptografia (sem TLS/SSL).
- **ADB_Backup**: Mecanismo do Android Debug Bridge que permite extrair dados de apps via USB.
- **MITM**: Ataque de intermediário (Man-in-the-Middle) que intercepta comunicações de rede.
- **Variavel_de_Ambiente**: Variável configurada no sistema operacional ou em arquivo `.env` não versionado, usada para injetar segredos em tempo de execução.
- **Gitignore**: Arquivo `.gitignore` que lista padrões de arquivos a serem ignorados pelo Git.

---

## Requisitos

### Requisito 1: Remoção de Credenciais Hardcoded do Repositório

**User Story:** Como administrador do sistema, quero que credenciais sensíveis não estejam versionadas no repositório Git, para que um acesso não autorizado ao código-fonte não comprometa o banco de dados nem a segurança dos tokens JWT.

#### Critérios de Aceitação

1. THE Servidor SHALL carregar o valor de `spring.datasource.password` exclusivamente a partir de uma Variavel_de_Ambiente ou de um arquivo de configuração não versionado, nunca a partir de um valor literal no arquivo `application-mobile.properties` versionado.

2. THE Servidor SHALL carregar o valor de `jwt.secret` exclusivamente a partir de uma Variavel_de_Ambiente ou de um arquivo de configuração não versionado, nunca a partir de um valor literal no arquivo `application-mobile.properties` versionado.

3. WHEN o Servidor for iniciado sem a Variavel_de_Ambiente `DATASOURCE_PASSWORD` definida, THEN THE Servidor SHALL encerrar a inicialização com mensagem de erro indicando a variável ausente.

4. WHEN o Servidor for iniciado sem a Variavel_de_Ambiente `JWT_SECRET` definida, THEN THE Servidor SHALL encerrar a inicialização com mensagem de erro indicando a variável ausente.

5. THE Gitignore SHALL conter entradas que impeçam o versionamento de arquivos `.env`, `application-mobile-local.properties` e quaisquer arquivos de override de configuração que contenham segredos.

6. THE Servidor SHALL aceitar o valor de `jwt.secret` somente quando este possuir comprimento mínimo de 32 caracteres, rejeitando valores mais curtos na inicialização.

---

### Requisito 2: Proteção das Credenciais do Keystore de Release

**User Story:** Como desenvolvedor responsável pelo build de release, quero que as senhas do Keystore não estejam versionadas no repositório, para que o certificado de assinatura do APK não possa ser comprometido por acesso ao histórico do Git.

#### Critérios de Aceitação

1. THE Gitignore SHALL conter a entrada `keystore.properties` de forma que o arquivo nunca seja rastreado pelo Git.

2. THE App SHALL carregar as credenciais do Keystore (`storePassword`, `keyPassword`, `keyAlias`, `storeFile`) exclusivamente a partir do arquivo `keystore.properties` local, que não deve estar versionado.

3. WHEN o arquivo `keystore.properties` não existir durante um build de release, THEN THE App SHALL interromper o build com mensagem de erro clara orientando o desenvolvedor a criar o arquivo localmente.

4. THE Gitignore SHALL conter entradas para os padrões `*.keystore` e `*.jks` de forma que arquivos de Keystore nunca sejam rastreados pelo Git.

---

### Requisito 3: Proteção da SQLCipher_Passphrase no App Android

**User Story:** Como usuário do App, quero que a chave de criptografia do banco de dados local não esteja hardcoded no código-fonte, para que a extração do APK não revele a passphrase e comprometa os dados armazenados localmente.

#### Critérios de Aceitação

1. THE App SHALL armazenar a SQLCipher_Passphrase no Android_Keystore ou em EncryptedSharedPreferences, nunca como literal de string no código-fonte Kotlin.

2. WHEN o App for iniciado pela primeira vez em um dispositivo, THE App SHALL gerar uma SQLCipher_Passphrase aleatória com entropia mínima de 128 bits e armazená-la no Android_Keystore.

3. WHEN o App for iniciado em execuções subsequentes, THE App SHALL recuperar a SQLCipher_Passphrase do Android_Keystore para abrir o banco de dados, sem expô-la em logs ou variáveis de instância de longa duração.

4. IF a recuperação da SQLCipher_Passphrase do Android_Keystore falhar, THEN THE App SHALL exibir mensagem de erro ao usuário e impedir o acesso ao banco de dados local.

---

### Requisito 4: Habilitação do ProGuard/R8 no Build de Release

**User Story:** Como responsável pela segurança do App, quero que o código de release seja ofuscado e minificado, para que a engenharia reversa do APK seja significativamente mais difícil.

#### Critérios de Aceitação

1. THE App SHALL ter `minifyEnabled true` configurado no bloco `buildTypes.release` do `app/build.gradle`.

2. THE App SHALL ter `shrinkResources true` configurado no bloco `buildTypes.release` do `app/build.gradle`.

3. THE App SHALL manter regras ProGuard que preservem as classes necessárias para o funcionamento correto de Room, Retrofit, Hilt, SQLCipher e Gson, de forma que o App compile e execute corretamente com ofuscação ativa.

4. WHEN o build de release for executado com `minifyEnabled true`, THE App SHALL compilar sem erros e o APK gerado SHALL funcionar corretamente em dispositivo físico ou emulador.

---

### Requisito 5: Restrição do Tráfego HTTP Cleartext no App Android

**User Story:** Como usuário do App, quero que o tráfego de rede seja criptografado por padrão, para que dados sensíveis como tokens JWT e dados de coleta não sejam interceptados em redes não confiáveis.

#### Critérios de Aceitação

1. THE App SHALL ter `cleartextTrafficPermitted="false"` na `<base-config>` do arquivo `network_security_config.xml`, bloqueando Cleartext_Traffic para todos os domínios não explicitamente listados.

2. WHERE o ambiente for de desenvolvimento local, THE App SHALL permitir Cleartext_Traffic exclusivamente para os domínios e IPs explicitamente listados na `<domain-config>` do `network_security_config.xml` (endereços `localhost`, `10.0.2.2` e IPs da rede interna do IFMT).

3. THE App SHALL remover `<certificates src="user" />` da `<base-config>` e da `<domain-config>` de produção, aceitando apenas certificados do sistema (`<certificates src="system" />`).

4. WHERE o ambiente for de desenvolvimento, THE App SHALL manter `<certificates src="user" />` somente na `<domain-config>` de desenvolvimento, nunca na `<base-config>`.

5. THE App SHALL remover o atributo `android:usesCleartextTraffic="true"` do elemento `<application>` no `AndroidManifest.xml`, delegando o controle de tráfego inteiramente ao `network_security_config.xml`.

---

### Requisito 6: Correção da Configuração CORS no Servidor

**User Story:** Como administrador do servidor, quero que a política CORS não permita qualquer origem com credenciais habilitadas, para que requisições cross-origin maliciosas não possam autenticar-se na API mobile.

#### Critérios de Aceitação

1. THE Servidor SHALL configurar `api.mobile.cors.allowed-origins` com uma lista explícita de origens permitidas, nunca com o valor curinga `*`.

2. THE Servidor SHALL configurar `api.mobile.cors.allow-credentials=false` no arquivo de propriedades, mantendo consistência com a implementação em `MobileSecurityConfig.java` que já define `setAllowCredentials(false)`.

3. WHEN uma requisição CORS for recebida de uma origem não listada em `api.mobile.cors.allowed-origins`, THEN THE Servidor SHALL rejeitar a requisição com status HTTP 403.

4. THE Servidor SHALL incluir na lista de origens permitidas apenas os endereços IP e hostnames conhecidos do ambiente de desenvolvimento e produção do SIHCP.

---

### Requisito 7: Habilitação do Rate Limiting no Endpoint de Login

**User Story:** Como administrador do servidor, quero que o endpoint de login tenha limite de requisições por IP, para que ataques de força bruta contra credenciais de usuários sejam mitigados.

#### Critérios de Aceitação

1. THE Servidor SHALL ter `api.mobile.rate-limit.enabled=true` configurado no arquivo de propriedades de produção.

2. WHEN o Rate_Limiter estiver habilitado, THE Servidor SHALL limitar o endpoint `POST /api/mobile/auth/login` a no máximo 10 tentativas por IP em uma janela de 1 minuto.

3. WHEN um IP exceder o limite de tentativas de login, THEN THE Servidor SHALL retornar HTTP 429 (Too Many Requests) com cabeçalho `Retry-After` indicando o tempo de espera em segundos.

4. WHEN um IP exceder o limite de tentativas de login 3 vezes consecutivas em 5 minutos, THEN THE Servidor SHALL registrar um evento de alerta no log com nível WARN contendo o IP de origem.

5. THE Servidor SHALL aplicar Rate_Limiter exclusivamente ao endpoint de login, sem afetar outros endpoints autenticados.

---

### Requisito 8: Controle do Nível de Log em Produção

**User Story:** Como administrador do servidor, quero que tokens JWT não sejam registrados em logs em ambiente de produção, para que o acesso aos arquivos de log não permita a um atacante reutilizar tokens válidos.

#### Critérios de Aceitação

1. THE Servidor SHALL configurar o nível de log do pacote `com.inventario` como `INFO` no perfil de produção, nunca como `DEBUG`.

2. THE Servidor SHALL configurar o nível de log do pacote `com.inventario.sihcp.mobile.server.controller` como `INFO` no perfil de produção.

3. THE Servidor SHALL configurar o nível de log do pacote `com.inventario.sihcp.mobile.server.service` como `INFO` no perfil de produção.

4. IF um token JWT for recebido em uma requisição, THEN THE Servidor SHALL registrar no log apenas o identificador do usuário extraído do token, nunca o valor completo do token.

5. THE Servidor SHALL criar um perfil Spring separado (`prod`) com as configurações de log restritas, de forma que o perfil de desenvolvimento (`dev`) possa manter `DEBUG` sem afetar produção.

---

### Requisito 9: Redução da Validade do JWT_Access_Token

**User Story:** Como administrador do servidor, quero que o JWT_Access_Token tenha validade reduzida, para que tokens comprometidos tenham uma janela de exploração menor.

#### Critérios de Aceitação

1. THE Servidor SHALL configurar `jwt.expiration` com valor máximo de 3600 segundos (1 hora) no perfil de produção.

2. THE Servidor SHALL manter `jwt.refresh-expiration` com valor de até 604800 segundos (7 dias), permitindo que o App renove o JWT_Access_Token sem exigir novo login.

3. WHEN o JWT_Access_Token expirar, THE Servidor SHALL retornar HTTP 401 com corpo JSON contendo o campo `"error": "token_expired"` para que o App possa distinguir expiração de token inválido.

4. THE App SHALL renovar automaticamente o JWT_Access_Token usando o JWT_Refresh_Token antes de cada requisição quando o tempo restante de validade for inferior a 300 segundos (5 minutos).

---

### Requisito 10: Remoção de Certificados de Usuário da Configuração de Rede

**User Story:** Como usuário do App, quero que o App não aceite certificados instalados manualmente no dispositivo como âncoras de confiança em produção, para que ataques MITM via proxy não sejam possíveis em campo.

#### Critérios de Aceitação

1. THE App SHALL remover `<certificates src="user" />` da `<base-config>` do `network_security_config.xml`.

2. WHERE o ambiente for de produção, THE App SHALL aceitar exclusivamente certificados da âncora de confiança do sistema (`<certificates src="system" />`).

3. WHERE o ambiente for de desenvolvimento, THE App SHALL permitir `<certificates src="user" />` somente dentro da `<domain-config>` que lista os IPs de desenvolvimento, nunca na `<base-config>`.

---

### Requisito 11: Desabilitação do ADB Backup

**User Story:** Como responsável pela segurança do App, quero que o backup via ADB esteja desabilitado, para que dados sensíveis armazenados localmente não possam ser extraídos de dispositivos físicos via USB.

#### Critérios de Aceitação

1. THE App SHALL ter `android:allowBackup="false"` no elemento `<application>` do `AndroidManifest.xml`.

2. THE App SHALL ter regras de extração de dados (`data_extraction_rules.xml`) que excluam explicitamente o banco de dados SQLCipher e os arquivos de EncryptedSharedPreferences de qualquer backup em nuvem ou local.

3. WHEN um backup ADB for tentado em um dispositivo com o App instalado, THE App SHALL não incluir dados do banco de dados local nem tokens armazenados no backup gerado.

---

### Requisito 12: Remoção da Permissão SYSTEM_ALERT_WINDOW

**User Story:** Como responsável pela segurança do App, quero que permissões desnecessárias sejam removidas do manifesto, para que a superfície de ataque do App seja reduzida ao mínimo necessário para seu funcionamento.

#### Critérios de Aceitação

1. THE App SHALL remover a declaração `<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />` do `AndroidManifest.xml`.

2. WHEN o App for compilado sem a permissão `SYSTEM_ALERT_WINDOW`, THE App SHALL compilar sem erros e todas as funcionalidades existentes (scanner QR, coleta, sincronização, exportação) SHALL continuar funcionando corretamente.

3. THE App SHALL remover a declaração `<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />` do `AndroidManifest.xml`, caso não seja utilizada por nenhuma funcionalidade ativa.
