# Requirements Document

## Introduction

Este documento especifica os requisitos para implementação das medidas de segurança críticas no aplicativo Android do Sistema de Inventário de Patrimônio do IFMT. O objetivo é elevar o nível de segurança do aplicativo de BAIXO (30%) para ALTO (85%), corrigindo vulnerabilidades críticas identificadas e garantindo conformidade com as melhores práticas de segurança mobile (OWASP Mobile Security).

A implementação será feita de forma incremental e cuidadosa para não quebrar funcionalidades existentes do aplicativo.

## Glossary

- **App_Android**: Aplicativo móvel Android do Sistema de Inventário (InventarioMobile)
- **SecureStorage**: Componente responsável pelo armazenamento criptografado de dados sensíveis
- **JWT**: JSON Web Token utilizado para autenticação
- **EncryptedSharedPreferences**: API do Android para armazenamento criptografado de preferências
- **SQLCipher**: Biblioteca para criptografia de banco de dados SQLite
- **Certificate_Pinning**: Técnica de segurança que vincula o app a certificados SSL específicos
- **ProGuard_R8**: Ferramenta de ofuscação e otimização de código Android
- **SessionManager**: Componente responsável pelo gerenciamento de sessão do usuário
- **InputValidator**: Componente responsável pela validação e sanitização de entradas
- **OWASP**: Open Web Application Security Project - padrão de segurança para aplicações

## Requirements

### Requirement 1

**User Story:** Como desenvolvedor de segurança, eu quero que tokens JWT e credenciais sejam armazenados de forma criptografada, para que dados sensíveis não possam ser extraídos do dispositivo.

#### Acceptance Criteria

1. WHEN o App_Android armazena um token JWT, THE App_Android SHALL utilizar EncryptedSharedPreferences com criptografia AES256-GCM
2. WHEN o App_Android armazena credenciais do usuário, THE App_Android SHALL utilizar EncryptedSharedPreferences com criptografia AES256-GCM
3. THE App_Android SHALL criar uma classe SecureStorage que encapsula todas as operações de armazenamento seguro
4. WHEN o usuário faz logout, THE SecureStorage SHALL limpar todos os dados sensíveis armazenados
5. THE App_Android SHALL migrar dados existentes do SharedPreferences comum para EncryptedSharedPreferences na primeira execução após atualização

### Requirement 2

**User Story:** Como desenvolvedor de segurança, eu quero que o banco de dados SQLite seja criptografado, para que dados de patrimônios e coletas não possam ser acessados por terceiros.

#### Acceptance Criteria

1. THE App_Android SHALL utilizar SQLCipher para criptografar o banco de dados Room
2. THE App_Android SHALL gerar uma chave de criptografia única por instalação e armazená-la no SecureStorage
3. WHEN o App_Android é atualizado de uma versão sem criptografia, THE App_Android SHALL migrar o banco existente para o formato criptografado
4. IF a migração do banco falhar, THEN THE App_Android SHALL manter backup do banco original e notificar o usuário
5. THE App_Android SHALL verificar a integridade do banco criptografado ao iniciar

### Requirement 3

**User Story:** Como desenvolvedor de segurança, eu quero que logs não contenham dados sensíveis, para que informações confidenciais não vazem através de logs do sistema.

#### Acceptance Criteria

1. THE App_Android SHALL remover todos os logs que contenham senhas, tokens ou dados pessoais
2. THE App_Android SHALL implementar uma classe LogSanitizer que ofusca dados sensíveis antes de logar
3. WHEN o App_Android loga informações de usuário, THE LogSanitizer SHALL substituir dados sensíveis por asteriscos
4. THE App_Android SHALL configurar android:allowBackup="false" no AndroidManifest.xml
5. THE App_Android SHALL desabilitar logs de debug em builds de release

### Requirement 4

**User Story:** Como desenvolvedor de segurança, eu quero que a comunicação com o servidor utilize Certificate Pinning, para que ataques Man-in-the-Middle sejam prevenidos.

#### Acceptance Criteria

1. THE App_Android SHALL criar um arquivo network_security_config.xml com configuração de Certificate Pinning
2. THE App_Android SHALL bloquear tráfego HTTP não criptografado (cleartextTrafficPermitted="false")
3. THE App_Android SHALL configurar pins SHA-256 para o certificado do servidor de produção
4. THE App_Android SHALL incluir um certificado de backup para evitar bloqueio em caso de renovação
5. IF a validação do certificado falhar, THEN THE App_Android SHALL exibir mensagem de erro de segurança e bloquear a conexão
6. THE App_Android SHALL forçar uso de TLS 1.2 ou superior

### Requirement 5

**User Story:** Como desenvolvedor de segurança, eu quero que o código do aplicativo seja ofuscado, para que engenharia reversa seja dificultada.

#### Acceptance Criteria

1. THE App_Android SHALL habilitar minifyEnabled e shrinkResources em builds de release
2. THE App_Android SHALL configurar regras ProGuard/R8 para manter classes necessárias (Retrofit, Room, Hilt)
3. THE App_Android SHALL remover logs de debug através de regras ProGuard
4. WHEN uma build de release é gerada, THE App_Android SHALL verificar que a ofuscação foi aplicada corretamente
5. THE App_Android SHALL manter um arquivo proguard-rules.pro documentado com todas as regras necessárias

### Requirement 6

**User Story:** Como desenvolvedor de segurança, eu quero que todas as entradas do usuário sejam validadas e sanitizadas, para que ataques de injeção sejam prevenidos.

#### Acceptance Criteria

1. THE App_Android SHALL criar uma classe InputValidator com métodos de validação para cada tipo de entrada
2. WHEN o usuário digita um número de patrimônio, THE InputValidator SHALL validar que contém apenas dígitos numéricos
3. WHEN o usuário digita texto livre, THE InputValidator SHALL sanitizar removendo caracteres especiais perigosos (<, >, ", ')
4. THE App_Android SHALL limitar o tamanho máximo de todas as entradas de texto
5. IF uma entrada inválida for detectada, THEN THE App_Android SHALL exibir mensagem de erro clara e bloquear o envio

### Requirement 7

**User Story:** Como desenvolvedor de segurança, eu quero que secrets não sejam hardcoded no código, para que informações sensíveis não vazem através do APK.

#### Acceptance Criteria

1. THE App_Android SHALL mover todas as URLs de API para BuildConfig
2. THE App_Android SHALL utilizar variáveis de ambiente ou local.properties para configurações sensíveis
3. THE App_Android SHALL adicionar local.properties ao .gitignore
4. WHEN uma build é gerada, THE App_Android SHALL injetar configurações através de BuildConfig fields
5. THE App_Android SHALL documentar todas as variáveis de configuração necessárias

### Requirement 8

**User Story:** Como usuário do aplicativo, eu quero que minha sessão expire após período de inatividade, para que minha conta esteja protegida se eu esquecer o celular desbloqueado.

#### Acceptance Criteria

1. THE App_Android SHALL criar uma classe SessionManager que monitora a atividade do usuário
2. WHEN o usuário não interage com o app por 15 minutos, THE SessionManager SHALL expirar a sessão automaticamente
3. WHEN a sessão expira, THE App_Android SHALL redirecionar para a tela de login
4. THE App_Android SHALL salvar o estado atual antes de expirar a sessão para permitir recuperação
5. THE App_Android SHALL exibir aviso 1 minuto antes da sessão expirar

### Requirement 9

**User Story:** Como usuário do aplicativo, eu quero que minha conta seja bloqueada após múltiplas tentativas de login falhadas, para que ataques de força bruta sejam prevenidos.

#### Acceptance Criteria

1. THE App_Android SHALL criar uma classe LoginAttemptManager que rastreia tentativas de login
2. WHEN o usuário falha 5 tentativas de login consecutivas, THE App_Android SHALL bloquear novas tentativas por 15 minutos
3. WHEN a conta está bloqueada, THE App_Android SHALL exibir o tempo restante para desbloqueio
4. WHEN o usuário faz login com sucesso, THE LoginAttemptManager SHALL resetar o contador de tentativas
5. THE App_Android SHALL persistir o estado de bloqueio para evitar bypass por reinicialização

### Requirement 10

**User Story:** Como desenvolvedor de segurança, eu quero que timeouts de rede sejam configurados adequadamente, para que o app não fique vulnerável a ataques de negação de serviço.

#### Acceptance Criteria

1. THE App_Android SHALL configurar timeout de conexão de 30 segundos
2. THE App_Android SHALL configurar timeout de leitura de 30 segundos
3. THE App_Android SHALL configurar timeout de escrita de 30 segundos
4. THE App_Android SHALL implementar retry com backoff exponencial limitado a 3 tentativas
5. WHEN uma requisição excede o timeout, THE App_Android SHALL cancelar a requisição e exibir mensagem de erro

