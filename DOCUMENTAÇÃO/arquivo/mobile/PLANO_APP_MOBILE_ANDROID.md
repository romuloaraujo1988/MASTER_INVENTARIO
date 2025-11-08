# Plano de Desenvolvimento - App Mobile Android para Sistema de Inventário

## 1. Visão Geral do Projeto

### 1.1 Objetivo
Desenvolver um aplicativo mobile Android para coleta de dados de inventário com capacidade de trabalhar offline e sincronizar dados quando conectado.

### 1.2 Funcionalidades Principais
- Leitura de códigos QR Code
- Autenticação de usuário
- Verificação de itens na base de dados
- Registro de coleta de patrimônio
- Modo offline com sincronização
- Interface intuitiva para operadores

## 2. Arquitetura do Sistema

### 2.1 Tecnologias Recomendadas
- **Linguagem**: Kotlin (nativo Android)
- **Banco Local**: SQLite com Room Database
- **Comunicação**: Retrofit para APIs REST
- **QR Code**: ZXing Android Embedded
- **Autenticação**: JWT Token
- **Sincronização**: WorkManager para tarefas em background

### 2.2 Estrutura do Projeto
```
app/
├── src/main/java/com/inventario/mobile/
│   ├── data/
│   │   ├── local/          # Room Database, DAOs
│   │   ├── remote/         # API Services, DTOs
│   │   └── repository/     # Repository Pattern
│   ├── domain/
│   │   ├── model/          # Entidades de domínio
│   │   ├── usecase/        # Casos de uso
│   │   └── repository/     # Interfaces de repositório
│   ├── presentation/
│   │   ├── ui/             # Activities, Fragments
│   │   ├── viewmodel/      # ViewModels
│   │   └── adapter/        # RecyclerView Adapters
│   └── util/
│       ├── network/        # Utilitários de rede
│       ├── security/       # Criptografia, tokens
│       └── qr/             # Utilitários QR Code
```

## 3. Funcionalidades Detalhadas

### 3.1 Autenticação
- **Tela de Login**: Usuário e senha
- **Validação**: Contra base de dados do sistema principal
- **Token JWT**: Armazenamento seguro local
- **Timeout**: Renovação automática de token

### 3.2 Leitura de QR Code
- **Scanner**: Integração com câmera do dispositivo
- **Decodificação**: Extração do código do patrimônio
- **Validação**: Verificação de formato válido
- **Feedback**: Visual e sonoro para leitura bem-sucedida

### 3.3 Verificação de Patrimônio
- **Busca Local**: Primeiro no banco SQLite local
- **Busca Remota**: Se não encontrado e online
- **Cache**: Armazenamento local de dados consultados
- **Status**: Verificação se já foi coletado

### 3.4 Registro de Coleta
- **Formulário**: Dados do patrimônio e observações
- **Validação**: Campos obrigatórios
- **Timestamp**: Data/hora da coleta
- **Localização**: GPS opcional
- **Fotos**: Captura opcional de imagens

### 3.5 Modo Offline
- **Sincronização Prévia**: Download de dados necessários
- **Armazenamento Local**: SQLite para dados offline
- **Queue de Sincronização**: Fila de operações pendentes
- **Indicadores**: Status de conectividade

## 4. Banco de Dados Local (SQLite)

### 4.1 Tabelas Principais
```sql
-- Usuários
CREATE TABLE usuario_local (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    nome TEXT NOT NULL,
    token TEXT,
    ultimo_sync TIMESTAMP
);

-- Patrimônios (cache)
CREATE TABLE patrimonio_cache (
    id INTEGER PRIMARY KEY,
    codigo TEXT UNIQUE NOT NULL,
    descricao TEXT,
    setor_id INTEGER,
    sala_id INTEGER,
    status TEXT,
    ultimo_sync TIMESTAMP
);

-- Coletas (offline)
CREATE TABLE coleta_offline (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patrimonio_codigo TEXT NOT NULL,
    usuario_id INTEGER NOT NULL,
    data_coleta TIMESTAMP NOT NULL,
    observacoes TEXT,
    latitude REAL,
    longitude REAL,
    foto_path TEXT,
    sincronizado BOOLEAN DEFAULT FALSE,
    tentativas_sync INTEGER DEFAULT 0
);

-- Setores (cache)
CREATE TABLE setor_cache (
    id INTEGER PRIMARY KEY,
    nome TEXT NOT NULL,
    campus_id INTEGER,
    ultimo_sync TIMESTAMP
);

-- Salas (cache)
CREATE TABLE sala_cache (
    id INTEGER PRIMARY KEY,
    nome TEXT NOT NULL,
    setor_id INTEGER,
    ultimo_sync TIMESTAMP
);
```

## 5. API REST - Endpoints Necessários

### 5.1 Autenticação
```
POST /api/mobile/auth/login
POST /api/mobile/auth/refresh
POST /api/mobile/auth/logout
```

### 5.2 Sincronização
```
GET /api/mobile/sync/patrimonio/{setor_id}
GET /api/mobile/sync/setores
GET /api/mobile/sync/salas/{setor_id}
POST /api/mobile/sync/coletas
```

### 5.3 Operações
```
GET /api/mobile/patrimonio/{codigo}
POST /api/mobile/coleta
GET /api/mobile/coleta/status/{patrimonio_codigo}
```

## 6. Fluxo de Trabalho

### 6.1 Inicialização
1. Login do usuário
2. Sincronização inicial (setores, salas, patrimônios)
3. Configuração do modo de trabalho

### 6.2 Coleta
1. Scan do QR Code
2. Busca do patrimônio (local/remoto)
3. Exibição dos dados
4. Confirmação/registro da coleta
5. Armazenamento local se offline

### 6.3 Sincronização
1. Verificação de conectividade
2. Upload de coletas pendentes
3. Download de atualizações
4. Limpeza de cache antigo

## 7. Interface do Usuário

### 7.1 Telas Principais
- **Login**: Autenticação
- **Dashboard**: Resumo e estatísticas
- **Scanner**: Leitura de QR Code
- **Detalhes**: Informações do patrimônio
- **Coleta**: Formulário de registro
- **Sincronização**: Status e controles
- **Configurações**: Preferências do app

### 7.2 Componentes UI
- **Material Design**: Seguir guidelines do Android
- **Navegação**: Bottom Navigation ou Navigation Drawer
- **Feedback**: SnackBars, ProgressBars, Dialogs
- **Acessibilidade**: Suporte a TalkBack

## 8. Segurança

### 8.1 Medidas de Proteção
- **Criptografia**: Dados sensíveis no SQLite
- **HTTPS**: Todas as comunicações
- **Token JWT**: Autenticação segura
- **Obfuscação**: Código do aplicativo
- **Certificado**: Assinatura digital

### 8.2 Validações
- **Input**: Sanitização de dados
- **Autorização**: Verificação de permissões
- **Timeout**: Sessões expiradas
- **Logs**: Auditoria de operações

## 9. Implementação por Fases

### 9.1 Fase 1 - MVP (4-6 semanas)
- Autenticação básica
- Leitura de QR Code
- Busca de patrimônio online
- Registro de coleta simples

### 9.2 Fase 2 - Offline (3-4 semanas)
- Banco SQLite local
- Sincronização básica
- Cache de dados
- Queue de operações

### 9.3 Fase 3 - Melhorias (2-3 semanas)
- Interface aprimorada
- Relatórios locais
- Configurações avançadas
- Otimizações de performance

### 9.4 Fase 4 - Recursos Extras (2-3 semanas)
- Captura de fotos
- Localização GPS
- Exportação de dados
- Notificações push

## 10. Requisitos Técnicos

### 10.1 Dispositivo
- **Android**: Versão 7.0+ (API 24+)
- **RAM**: Mínimo 2GB
- **Armazenamento**: 100MB livres
- **Câmera**: Para QR Code
- **Rede**: WiFi ou dados móveis

### 10.2 Permissões
- CAMERA (QR Code)
- INTERNET (Sincronização)
- ACCESS_NETWORK_STATE (Status de rede)
- WRITE_EXTERNAL_STORAGE (Fotos)
- ACCESS_FINE_LOCATION (GPS opcional)

## 11. Testes

### 11.1 Tipos de Teste
- **Unitários**: Lógica de negócio
- **Instrumentados**: Interface e banco
- **Integração**: APIs e sincronização
- **Usabilidade**: Experiência do usuário

### 11.2 Cenários Críticos
- Perda de conectividade durante operação
- Sincronização com conflitos
- QR Codes inválidos ou danificados
- Timeout de sessão
- Armazenamento insuficiente

## 12. Deploy e Distribuição

### 12.1 Opções de Distribuição
- **Google Play Store**: Distribuição pública
- **Play Console**: Distribuição interna
- **APK Direto**: Instalação manual
- **MDM**: Gerenciamento corporativo

### 12.2 Versionamento
- **Semantic Versioning**: MAJOR.MINOR.PATCH
- **Build Number**: Incremento automático
- **Release Notes**: Documentação de mudanças

## 13. Manutenção e Suporte

### 13.1 Monitoramento
- **Crashlytics**: Relatórios de erro
- **Analytics**: Uso do aplicativo
- **Performance**: Métricas de desempenho

### 13.2 Atualizações
- **Automáticas**: Via Play Store
- **Forçadas**: Para versões críticas
- **Rollback**: Reversão se necessário

## 14. Estimativa de Recursos

### 14.1 Equipe Recomendada
- **1 Desenvolvedor Android Senior**: 3-4 meses
- **1 Desenvolvedor Backend**: 1-2 meses (APIs)
- **1 Designer UI/UX**: 2-3 semanas
- **1 Testador QA**: 2-3 semanas

### 14.2 Cronograma Total
- **Desenvolvimento**: 12-16 semanas
- **Testes**: 3-4 semanas
- **Deploy**: 1-2 semanas
- **Total**: 16-22 semanas

## 15. Considerações Finais

### 15.1 Riscos
- Complexidade da sincronização offline
- Performance em dispositivos antigos
- Variações de câmera para QR Code
- Conectividade instável em campo

### 15.2 Mitigações
- Testes extensivos em diferentes dispositivos
- Implementação robusta de retry/fallback
- Interface adaptativa para diferentes telas
- Modo degradado para situações críticas

### 15.3 Próximos Passos
1. Aprovação do plano
2. Setup do ambiente de desenvolvimento
3. Criação do projeto Android
4. Implementação das APIs no backend
5. Desenvolvimento iterativo por fases

---

**Documento criado em**: $(date)
**Versão**: 1.0
**Responsável**: Equipe de Desenvolvimento