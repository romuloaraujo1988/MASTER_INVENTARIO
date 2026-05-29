# 🔧 Configuração Inicial de IP do Servidor

## 📋 Problema Resolvido

Antes desta versão, todo APK gerado vinha com o IP `10.14.250.214` fixado como padrão. Isso causava transtorno em três situações:

1. **Servidor mudou de IP**: usuários precisavam editar manualmente o IP no campo da tela de login a cada nova instalação
2. **Múltiplos servidores (homologação/produção)**: difícil distinguir e configurar
3. **Onboarding**: o usuário tinha que adivinhar o IP correto, sem saber qual era

## ✅ Solução Implementada (v2.29.5)

Nova tela de **Configuração Inicial** (`InitialSetupActivity`) que aparece apenas na **primeira utilização** do app.

### Fluxo de Primeira Instalação

```
┌─────────────────────────────────────────┐
│   App é aberto pela primeira vez        │
└─────────────────┬───────────────────────┘
                  │
                  ▼
        ┌─────────────────────┐
        │  SplashActivity     │
        │  (5 segundos)       │
        └─────────┬───────────┘
                  │
                  ▼
        ┌─────────────────────────────┐
        │  isServerConfigured()?      │
        └──┬──────────────────────┬───┘
          NÃO                    SIM
           │                      │
           ▼                      ▼
  ┌─────────────────────┐ ┌─────────────────┐
  │ InitialSetupActivity │ │ LoginActivity   │
  │                      │ │ (fluxo normal)  │
  │ - Campo IP           │ └─────────────────┘
  │ - Campo Porta        │
  │ - Testar Conexão     │
  │ - Continuar          │
  └─────────┬────────────┘
            │
            │ (após configurar)
            ▼
  ┌─────────────────────┐
  │ LoginActivity       │
  └─────────────────────┘
```

### Tela de Configuração Inicial

A tela contém:

- **Ícone de servidor** (visual amigável)
- **Título e subtítulo** explicativos
- **Card de informação** orientando a buscar o IP com o administrador
- **Campo IP do Servidor** (com validação de formato IPv4)
- **Campo Porta** (padrão: 8081)
- **Botão "Testar Conexão"** — valida se o servidor responde
- **Indicador visual de status** (testando / sucesso / erro)
- **Botão "Continuar"** — só fica habilitado após teste bem-sucedido

### Validações

| Campo | Validação |
|-------|-----------|
| IP | Formato IPv4 (192.168.1.100) ou "localhost" |
| Porta | Número entre 1 e 65535 |
| Conexão | Resposta HTTP 200 do endpoint `/api/mobile/health` |

### Comportamentos Especiais

- **Botão Voltar bloqueado**: usuário não pode pular a configuração
- **Reset automático**: se editar IP/porta após teste, precisa testar novamente
- **Persistência**: configuração salva em `SharedPreferences` criptografado
- **Segunda instalação**: lembra do IP configurado anteriormente, mas tela ainda aparece (já vem pré-preenchida)

## 🔧 Alterar IP Posteriormente

Mesmo após a configuração inicial, o usuário pode alterar o IP a qualquer momento:

1. Na **LoginActivity**, há um campo "IP do Servidor" sempre visível
2. Ao alterar e fazer login, o novo IP é automaticamente salvo
3. A flag `isServerConfigured()` continua marcada como `true`

## 📦 Arquivos Modificados

### Criados

1. **`InitialSetupActivity.kt`** — Activity da tela de configuração
2. **`activity_initial_setup.xml`** — Layout da tela
3. **`ic_server_setup.xml`** — Ícone do servidor

### Modificados

1. **`PreferencesManager.kt`**
   - `isServerConfigured(): Boolean`
   - `setServerConfigured(configured: Boolean = true)`

2. **`SplashActivity.kt`**
   - Verifica `isServerConfigured()` antes de navegar
   - Se `false`, redireciona para `InitialSetupActivity`

3. **`LoginActivity.kt`**
   - Quando usuário altera IP no login, marca como configurado

4. **`AndroidManifest.xml`**
   - Registro da `InitialSetupActivity`

5. **`strings.xml`**
   - 16 novas strings para a tela de configuração

## 🧪 Cenários de Teste

### ✅ Cenário 1: Primeira instalação
1. Desinstalar app (se já instalado)
2. Instalar APK release v2.29.5
3. Abrir app
4. Splash aparece por 5 segundos
5. **InitialSetupActivity é exibida** ✅
6. Tentar continuar sem testar → erro
7. Inserir IP inválido (ex: "abc") → erro de validação
8. Inserir IP válido + servidor offline → erro "Servidor não encontrado"
9. Inserir IP válido + servidor online → sucesso
10. Botão "Continuar" habilita
11. Clicar Continuar → vai para LoginActivity

### ✅ Cenário 2: Tentativa de pular configuração
1. Na InitialSetupActivity, pressionar botão Voltar
2. **Snackbar exibe**: "Configure o servidor para continuar"
3. Tela não fecha

### ✅ Cenário 3: Reinstalação preservando dados
1. Após Cenário 1, fechar app
2. Reabrir app
3. Splash → vai direto para LoginActivity (não passa pela setup)

### ✅ Cenário 4: Mudança de IP no LoginActivity
1. Após primeira configuração
2. Na LoginActivity, alterar IP no campo
3. Fazer login com sucesso
4. Fechar app e reabrir
5. Splash → vai direto para LoginActivity (configuração mantida)

### ✅ Cenário 5: Limpar dados do app
1. Após uso normal
2. Configurações Android → Apps → SiHCP → Armazenamento → Limpar dados
3. Reabrir app
4. **InitialSetupActivity é exibida novamente** (configuração foi resetada)

## 🎯 Benefícios

- ✅ **Zero IPs hardcoded** entre instalações
- ✅ **Onboarding guiado** com instruções claras
- ✅ **Validação prévia** evita problemas após login
- ✅ **Compatível** com mudanças de servidor (reconfigurável)
- ✅ **Persistente** — configuração sobrevive reinicializações
- ✅ **Acessível** — material design + textos descritivos

## ⚠️ Limitações Aceitas

1. **Necessário ter servidor acessível** durante a configuração inicial (não é possível configurar offline)
2. **Sem auto-discovery** de servidores na rede (requer IP digitado manualmente)
3. **Sem QR Code** para configuração rápida (futura melhoria)

## 📈 Melhorias Futuras

### Curto Prazo
- [ ] Opção de "Pular" para instalações sem servidor disponível (apenas para offline-first)
- [ ] Histórico de últimos IPs usados (lista para selecionar)
- [ ] Sugestão automática baseada na rede local (ex: gateway + .100)

### Médio Prazo
- [ ] QR Code para configuração rápida (administrador gera QR no desktop)
- [ ] Auto-discovery via mDNS / Bonjour
- [ ] Múltiplos perfis de servidor (Produção, Homologação, Desenvolvimento)

### Longo Prazo
- [ ] Sincronização de configuração via conta corporativa
- [ ] Configuração via Mobile Device Management (MDM)

---

**Implementado em:** 22/05/2026  
**Versão:** 2.29.5 (Build 81)  
**Status:** ✅ PRODUÇÃO READY
