# Guia de Build para Produção - Sistema de Inventário IFMT

## Visão Geral

O sistema foi projetado para ser **modular e não-monolítico**, permitindo que cada componente seja implantado separadamente:

1. **Aplicação Desktop** - Interface Swing para operadores
2. **API Mobile** - Backend REST para app Android
3. **App Android** - Aplicativo móvel para coleta

## Arquitetura de Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT SEPARADO                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │   DESKTOP    │    │  API MOBILE  │    │  APP ANDROID │  │
│  │              │    │              │    │              │  │
│  │  Servidor A  │    │  Servidor B  │    │  Dispositivo │  │
│  │  Porta: N/A  │    │  Porta: 8081 │    │  Cliente     │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         │                    │                    │          │
│         └────────────────────┴────────────────────┘          │
│                              │                                │
│                    ┌─────────▼─────────┐                     │
│                    │   PostgreSQL      │                     │
│                    │   Servidor C      │                     │
│                    └───────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

## Scripts de Build Disponíveis

### 1. Build Desktop Apenas
```batch
build-desktop-producao.bat
```

**Gera:**
- `dist/desktop/sistema-inventario-desktop.jar` (JAR leve)
- `dist/desktop/lib/` (Dependências separadas)
- Scripts de inicialização
- Documentação

**Características:**
- JAR pequeno (~2-5 MB)
- Dependências externas na pasta `lib/`
- Não inclui componentes da API mobile
- Ideal para instalação em múltiplas estações

### 2. Build API Mobile Apenas
```batch
build-api-mobile-producao.bat
```

**Gera:**
- `dist/api-mobile/sistema-inventario-api-mobile.jar` (JAR standalone)
- Scripts de inicialização
- Arquivo de configuração exemplo
- Documentação

**Características:**
- JAR completo (~50-80 MB)
- Todas as dependências incluídas
- Não inclui interface desktop
- Ideal para servidor dedicado

### 3. Build Completo
```batch
build-producao-completo.bat
```

**Gera:**
- Ambas as aplicações separadamente
- Documentação completa
- Guia de deployment

## Uso dos Scripts

### Build Desktop

```batch
# Executar script
build-desktop-producao.bat

# Aguardar conclusão
# Resultado em: dist/desktop/
```

**Saída:**
```
dist/desktop/
├── sistema-inventario-desktop.jar
├── lib/
│   ├── spring-boot-*.jar
│   ├── postgresql-*.jar
│   └── ... (outras dependências)
├── iniciar-desktop.bat
├── iniciar-desktop.sh
├── README.md
└── configuracao_banco.json.exemplo
```

### Build API Mobile

```batch
# Executar script
build-api-mobile-producao.bat

# Aguardar conclusão
# Resultado em: dist/api-mobile/
```

**Saída:**
```
dist/api-mobile/
├── sistema-inventario-api-mobile.jar
├── iniciar-api-mobile.bat
├── iniciar-api-mobile.sh
├── README.md
└── application-mobile.properties.exemplo
```

## Configuração para Produção

### Desktop

**Arquivo:** `configuracao_banco.json`  
**Localização:** Diretório home do usuário

```json
{
  "host": "servidor-bd.ifmt.edu.br",
  "porta": "5432",
  "database": "sispatrimonio",
  "usuario": "inventario",
  "senha": "senha_segura_aqui"
}
```

**Localizações por SO:**
- Windows: `C:\Users\[usuario]\configuracao_banco.json`
- Linux: `~/configuracao_banco.json`
- Mac: `~/configuracao_banco.json`

### API Mobile

**Arquivo:** `application-mobile.properties`  
**Localização:** Mesma pasta do JAR

```properties
# Servidor
server.port=8081

# Banco de dados
spring.datasource.url=jdbc:postgresql://servidor-bd.ifmt.edu.br:5432/sispatrimonio
spring.datasource.username=inventario
spring.datasource.password=senha_segura_aqui

# JWT
jwt.secret=chave_secreta_muito_longa_e_segura_para_producao
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.inventario=DEBUG
logging.file.name=logs/sistema-inventario.log
```

## Deployment

### Cenário 1: Tudo no Mesmo Servidor

```
Servidor Único
├── Desktop: Executar localmente em cada estação
├── API Mobile: Porta 8081
└── PostgreSQL: Porta 5432
```

**Passos:**
1. Instalar PostgreSQL
2. Copiar `dist/api-mobile/` para `/opt/inventario-api/`
3. Configurar `application-mobile.properties`
4. Iniciar API: `./iniciar-api-mobile.sh`
5. Distribuir `dist/desktop/` para estações
6. Configurar `configuracao_banco.json` em cada estação

### Cenário 2: Servidores Separados (Recomendado)

```
Servidor 1 (Banco de Dados)
└── PostgreSQL: Porta 5432

Servidor 2 (API Mobile)
└── API Mobile: Porta 8081
    └── Conecta ao Servidor 1

Estações de Trabalho
└── Desktop
    └── Conecta ao Servidor 1
```

**Vantagens:**
- Melhor performance
- Maior segurança
- Escalabilidade
- Manutenção independente

### Cenário 3: Cloud/Híbrido

```
Cloud (AWS/Azure/GCP)
└── API Mobile: Porta 8081
    └── RDS PostgreSQL

On-Premise
└── Desktop em estações locais
    └── Conecta ao RDS via VPN
```

## Execução

### Desktop

**Windows:**
```batch
cd dist\desktop
iniciar-desktop.bat
```

**Linux/Mac:**
```bash
cd dist/desktop
chmod +x iniciar-desktop.sh
./iniciar-desktop.sh
```

### API Mobile

**Windows:**
```batch
cd dist\api-mobile
iniciar-api-mobile.bat
```

**Linux/Mac:**
```bash
cd dist/api-mobile
chmod +x iniciar-api-mobile.sh
./iniciar-api-mobile.sh
```

**Como Serviço (Linux - systemd):**

Criar arquivo `/etc/systemd/system/inventario-api.service`:

```ini
[Unit]
Description=Sistema Inventario - API Mobile
After=network.target postgresql.service

[Service]
Type=simple
User=inventario
WorkingDirectory=/opt/inventario-api
ExecStart=/usr/bin/java -jar sistema-inventario-api-mobile.jar --spring.profiles.active=mobile
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Ativar:
```bash
sudo systemctl daemon-reload
sudo systemctl enable inventario-api
sudo systemctl start inventario-api
sudo systemctl status inventario-api
```

## Verificação

### Desktop
1. Abrir aplicação
2. Fazer login
3. Verificar conexão com banco
4. Testar funcionalidades principais

### API Mobile
1. Verificar logs: `tail -f logs/sistema-inventario.log`
2. Testar endpoint de saúde: `curl http://localhost:8081/actuator/health`
3. Acessar Swagger: `http://localhost:8081/swagger-ui.html`
4. Testar login via API

### App Android
1. Configurar IP do servidor no app
2. Fazer login
3. Testar coleta de patrimônio
4. Verificar sincronização

## Troubleshooting

### Desktop não inicia

**Problema:** Erro ao conectar ao banco
**Solução:** 
- Verificar `configuracao_banco.json`
- Testar conexão: `psql -h host -U usuario -d database`
- Verificar firewall

### API Mobile não inicia

**Problema:** Porta 8081 já em uso
**Solução:**
- Alterar porta em `application-mobile.properties`
- Ou parar processo: `netstat -ano | findstr :8081`

**Problema:** Erro de conexão com banco
**Solução:**
- Verificar `application-mobile.properties`
- Verificar se PostgreSQL está rodando
- Verificar firewall

### App Android não conecta

**Problema:** Timeout ao conectar
**Solução:**
- Verificar se API está rodando
- Verificar IP configurado no app
- Verificar firewall do servidor
- Testar: `curl http://ip-servidor:8081/api/mobile/auth/login`

## Manutenção

### Atualização Desktop
1. Parar aplicação em todas as estações
2. Fazer backup da pasta atual
3. Substituir JAR e pasta `lib/`
4. Reiniciar aplicação

### Atualização API Mobile
1. Parar serviço: `systemctl stop inventario-api`
2. Fazer backup do JAR atual
3. Substituir JAR
4. Reiniciar: `systemctl start inventario-api`
5. Verificar logs

## Backup

### Banco de Dados
```bash
# Backup
pg_dump -h localhost -U inventario sispatrimonio > backup_$(date +%Y%m%d).sql

# Restore
psql -h localhost -U inventario -d sispatrimonio < backup_20251105.sql
```

### Configurações
- Desktop: Backup de `configuracao_banco.json`
- API: Backup de `application-mobile.properties`

## Monitoramento

### Logs Desktop
- Localização: `logs/sistema-inventario.log`
- Rotação: Automática (10 MB por arquivo)

### Logs API Mobile
- Localização: `logs/sistema-inventario.log`
- Monitorar: `tail -f logs/sistema-inventario.log`

### Métricas
- API: Actuator endpoints em `/actuator/*`
- Health: `/actuator/health`
- Metrics: `/actuator/metrics`

## Segurança

### Checklist Produção

- [ ] Alterar senha padrão do banco
- [ ] Configurar JWT secret forte
- [ ] Habilitar HTTPS na API
- [ ] Configurar firewall
- [ ] Restringir acesso ao banco
- [ ] Backup automático configurado
- [ ] Logs de auditoria habilitados
- [ ] Atualizar dependências regularmente

## Suporte

Para dúvidas ou problemas:
- Documentação: `dist/*/README.md`
- Logs: `logs/sistema-inventario.log`
- Equipe TI IFMT

## Resumo

✅ **Desktop e API são INDEPENDENTES**  
✅ **Podem rodar em servidores diferentes**  
✅ **Desktop NÃO precisa da API**  
✅ **API NÃO precisa do Desktop**  
✅ **App Android precisa APENAS da API**  
✅ **Builds separados = Deployment flexível**
