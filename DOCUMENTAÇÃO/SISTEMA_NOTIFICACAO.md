# 🔔 Sistema de Notificação - Documentação Completa

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Status e Componentes](#status-e-componentes)
3. [Arquitetura](#arquitetura)
4. [Guia de Uso](#guia-de-uso)
5. [Configuração](#configuração)
6. [Exemplos Práticos](#exemplos-práticos)
7. [Relatórios de Erro](#relatórios-de-erro)
8. [Migração](#migração)
9. [Troubleshooting](#troubleshooting)
10. [Referências](#referências)

---

## 📋 Visão Geral

Sistema de notificação não intrusivo que melhora a experiência do usuário com toasts visuais, logs estruturados e relatórios automáticos de erro.

**Status**: ✅ Implementado e Pronto para Uso  
**Versão**: 1.0.0  
**Data**: Novembro 2025  
**Autor**: Sistema de Inventário - IFMT

### Características Principais

- ✅ **Notificações Toast**: Não bloqueiam a interface
- ✅ **Compatibilidade Total**: Funciona com código existente (JOptionPane)
- ✅ **Relatórios de Erro**: Salvos automaticamente em JSON
- ✅ **Logs Estruturados**: Integrado com SLF4J
- ✅ **Configurável**: Ativar/desativar via properties
- ✅ **Zero Impacto**: Não quebra código atual
- ✅ **Visual Moderno**: Toasts com gradientes e animações

---

## 📦 Status e Componentes

### Componentes Implementados

#### Classes Java
- ✅ `ToastNotification.java` - Notificações visuais não intrusivas
- ✅ `NotificationManager.java` - Gerenciador central
- ✅ `NotificationUtils.java` - Utilitários de migração
- ✅ `ErrorReport.java` - Modelo de relatório de erro
- ✅ `ErrorReportingService.java` - Serviço de relatórios
- ✅ `NotificationType.java` - Enum de tipos
- ✅ `NotificationConfig.java` - Configuração Spring

#### Configuração
- ✅ `application.properties` - Configurações adicionadas

#### Documentação
- ✅ Documentação unificada completa
- ✅ Exemplos práticos de uso

### Estrutura de Arquivos

```
src/main/java/com/inventario/
├── util/notification/
│   ├── ToastNotification.java          # Componente visual
│   ├── NotificationManager.java        # Gerenciador central
│   ├── NotificationUtils.java          # Wrapper compatível
│   ├── ErrorReport.java                # Modelo de dados
│   ├── ErrorReportingService.java      # Serviço de relatórios
│   └── NotificationType.java           # Enum de tipos
└── config/
    └── NotificationConfig.java         # Configuração Spring

error_reports/                          # Relatórios (criado automaticamente)
└── error_YYYYMMDD_HHmmss.json

docs/
└── EXEMPLO_USO_NOTIFICACAO.java        # Exemplos práticos
```

---

## 🏗️ Arquitetura

### Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────┐
│                    Aplicação Desktop                     │
│  ┌────────────────┐  ┌────────────────┐                │
│  │ PatrimonioFrame│  │  UsuarioFrame  │  ...           │
│  └────────┬───────┘  └────────┬───────┘                │
│           │                    │                         │
│           └────────┬───────────┘                         │
│                    ▼                                     │
│         ┌──────────────────────┐                        │
│         │ NotificationUtils    │  ◄── Wrapper           │
│         └──────────┬───────────┘                        │
│                    ▼                                     │
│         ┌──────────────────────┐                        │
│         │ NotificationManager  │  ◄── Gerenciador       │
│         └──────────┬───────────┘                        │
│                    │                                     │
│         ┌──────────┼───────────┐                        │
│         ▼          ▼           ▼                         │
│  ┌──────────┐ ┌────────┐ ┌──────────────┐             │
│  │  Toast   │ │  Log   │ │ErrorReporting│             │
│  │Notification│ │ SLF4J │ │   Service    │             │
│  └──────────┘ └────────┘ └──────────────┘             │
└─────────────────────────────────────────────────────────┘
```

### Fluxo de Notificação

```
Evento → NotificationUtils → NotificationManager
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
              ToastNotification  Logger    ErrorReportingService
                    │               │               │
                    ▼               ▼               ▼
              Tela do Usuário   Log File      error_reports/
```

---

## 🚀 Guia de Uso

### 1. Notificações de Sucesso

```java
import com.inventario.util.notification.NotificationUtils;

// Apenas toast (não bloqueia a interface)
NotificationUtils.showSuccessMessage("Patrimônio salvo com sucesso!");

// Toast + Dialog opcional
NotificationUtils.showSuccessMessage(this, "Operação concluída!", true);
```


### 2. Notificações de Erro

```java
// Método compatível com JOptionPane
NotificationUtils.showErrorMessage(this, "Erro ao salvar patrimônio");

// Com exceção (gera relatório automático)
try {
    patrimonioService.salvar(patrimonio);
} catch (Exception e) {
    NotificationUtils.showErrorMessage(this, "Erro ao salvar", e);
}
```

### 3. Notificações de Aviso

```java
NotificationUtils.showWarningMessage(this, "Campos obrigatórios não preenchidos");
```

### 4. Notificações de Informação

```java
NotificationUtils.showInfoMessage(this, "Sincronização iniciada");
```

### 5. Diálogos de Confirmação

```java
boolean confirmado = NotificationUtils.showConfirmDialog(
    this, 
    "Deseja realmente excluir este patrimônio?", 
    "Confirmar Exclusão"
);

if (confirmado) {
    // Executar exclusão
}
```

### 6. Notificações Diretas (Toast apenas)

```java
import com.inventario.util.notification.NotificationManager;

// Sucesso
NotificationManager.notifySuccess("Operação realizada com sucesso!");

// Erro
NotificationManager.notifyError("Erro ao processar requisição");

// Aviso
NotificationManager.notifyWarning("Atenção: dados incompletos");

// Informação
NotificationManager.notifyInfo("Processamento em andamento...");
```

---

## 🎨 Tipos de Notificação

| Tipo | Ícone | Cor | Uso | Método |
|------|-------|-----|-----|--------|
| SUCCESS | ✅ | Verde | Operações bem-sucedidas | `notifySuccess()` |
| ERROR | ❌ | Vermelho | Erros críticos | `notifyError()` |
| WARNING | ⚠️ | Amarelo | Avisos e validações | `notifyWarning()` |
| INFO | ℹ️ | Azul | Informações gerais | `notifyInfo()` |

### Características Visuais

- **Posição**: Canto superior direito da tela
- **Duração**: 4 segundos (configurável)
- **Animação**: Fade out suave
- **Interação**: Botão de fechar manual
- **Estilo**: Gradiente com bordas arredondadas
- **Comportamento**: Always on top (não fica atrás de outras janelas)

---

## ⚙️ Configuração

### application.properties

```properties
# Sistema de Notificação (opcional)
app.notification.toast.enabled=true
app.notification.error-reporting.enabled=true
app.notification.toast.duration=4000
app.notification.error-reports.retention.days=30
```

### Configuração Programática

```java
// Desativar toasts temporariamente
NotificationManager.setToastEnabled(false);

// Desativar relatórios de erro
NotificationManager.setErrorReportingEnabled(false);

// Verificar status
boolean toastAtivo = NotificationManager.isToastEnabled();
boolean reportAtivo = NotificationManager.isErrorReportingEnabled();
```

### Configuração Spring Boot

A classe `NotificationConfig` carrega automaticamente as configurações:

```java
@Configuration
public class NotificationConfig {
    @Value("${app.notification.toast.enabled:true}")
    private boolean toastEnabled;
    
    @Value("${app.notification.error-reporting.enabled:true}")
    private boolean errorReportingEnabled;
    
    @PostConstruct
    public void init() {
        NotificationManager.setToastEnabled(toastEnabled);
        NotificationManager.setErrorReportingEnabled(errorReportingEnabled);
    }
}
```

---

## 📝 Exemplos Práticos

### Exemplo 1: Salvar Patrimônio

```java
private void salvarPatrimonio() {
    try {
        // Validar dados
        if (!validarCampos()) {
            NotificationUtils.showWarningMessage(this, "Preencha todos os campos obrigatórios");
            return;
        }
        
        // Salvar
        patrimonioService.salvar(patrimonio);
        
        // Sucesso (apenas toast, não bloqueia)
        NotificationUtils.showSuccessMessage("Patrimônio salvo com sucesso!");
        
        // Fechar janela
        dispose();
        
    } catch (Exception e) {
        // Erro (dialog + toast + relatório)
        NotificationUtils.showErrorMessage(this, "Erro ao salvar patrimônio", e);
    }
}
```

### Exemplo 2: Sincronização em Background

```java
private void sincronizarDados() {
    // Informar início
    NotificationManager.notifyInfo("Sincronização iniciada...");
    
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
        @Override
        protected Void doInBackground() throws Exception {
            sincronizacaoService.sincronizar();
            return null;
        }
        
        @Override
        protected void done() {
            try {
                get(); // Verificar se houve erro
                NotificationManager.notifySuccess("Sincronização concluída!");
                
            } catch (Exception e) {
                NotificationManager.notifyError("Falha na sincronização");
                NotificationManager.reportError("Sincronização falhou", "SyncService", e);
            }
        }
    };
    
    worker.execute();
}
```

### Exemplo 3: Exclusão com Confirmação

```java
private void excluirPatrimonio() {
    boolean confirmado = NotificationUtils.showConfirmDialog(
        this,
        "Deseja realmente excluir este patrimônio?\nEsta ação não pode ser desfeita.",
        "Confirmar Exclusão"
    );
    
    if (!confirmado) {
        return;
    }
    
    try {
        patrimonioService.excluir(patrimonio.getId());
        NotificationUtils.showSuccessMessage("Patrimônio excluído com sucesso!");
        atualizarLista();
        
    } catch (Exception e) {
        NotificationUtils.showErrorMessage(this, "Erro ao excluir patrimônio", e);
    }
}
```

### Exemplo 4: Validação de Formulário

```java
private void validarESubmeter() {
    // Validar campos
    if (campoNumeroPatrimonio.getText().isEmpty()) {
        NotificationUtils.showWarningMessage(this, "O número do patrimônio é obrigatório");
        campoNumeroPatrimonio.requestFocus();
        return;
    }
    
    if (campoDescricao.getText().isEmpty()) {
        NotificationUtils.showWarningMessage(this, "A descrição é obrigatória");
        campoDescricao.requestFocus();
        return;
    }
    
    // Tudo OK, salvar
    salvarPatrimonio();
}
```

### Exemplo 5: Operação com Múltiplas Etapas

```java
private void processarEmLote() {
    try {
        // Etapa 1
        NotificationManager.notifyInfo("Validando dados...");
        validarDados();
        
        // Etapa 2
        NotificationManager.notifyInfo("Salvando no banco...");
        salvarNoBanco();
        
        // Etapa 3
        NotificationManager.notifyInfo("Gerando relatório...");
        gerarRelatorio();
        
        // Sucesso final
        NotificationManager.notifySuccess("Todas as etapas concluídas!");
        
    } catch (Exception e) {
        NotificationUtils.showErrorMessage(this, "Erro durante o processamento", e);
    }
}
```

### Exemplo 6: Configuração Dinâmica

```java
private void processarSemNotificacoes() {
    // Desativar toasts temporariamente
    NotificationManager.setToastEnabled(false);
    
    // Executar operações em lote
    for (Patrimonio p : patrimonios) {
        processar(p);
    }
    
    // Reativar toasts
    NotificationManager.setToastEnabled(true);
    
    // Notificar conclusão
    NotificationManager.notifySuccess("Processamento em lote concluído!");
}
```

---

## 📊 Relatórios de Erro

### Localização

Os relatórios são salvos em: `error_reports/error_YYYYMMDD_HHmmss.json`

### Estrutura do Relatório

```json
{
  "message": "Erro ao conectar ao banco de dados",
  "context": "PatrimonioFrame - Salvar",
  "stackTrace": "java.sql.SQLException: Connection refused...",
  "timestamp": "2025-11-07T14:30:45",
  "username": "admin",
  "javaVersion": "21.0.1",
  "osName": "Windows 11",
  "osVersion": "10.0"
}
```

### Gerenciamento de Relatórios

```java
import com.inventario.util.notification.ErrorReportingService;
import com.inventario.util.notification.ErrorReport;
import java.util.List;

// Listar todos os relatórios
List<ErrorReport> reports = ErrorReportingService.listErrorReports();

// Processar relatórios
for (ErrorReport report : reports) {
    System.out.println("Erro: " + report.getMessage());
    System.out.println("Data: " + report.getTimestamp());
    System.out.println("Usuário: " + report.getUsername());
}

// Limpar relatórios antigos (>30 dias)
ErrorReportingService.cleanOldReports();
```

### Quando Relatórios São Gerados

Relatórios são gerados automaticamente quando:
- `NotificationUtils.showErrorMessage()` é chamado com exceção
- `NotificationManager.reportError()` é chamado explicitamente
- Configuração `app.notification.error-reporting.enabled=true`

---

## 🔄 Migração

### Estratégia de Migração Gradual

#### Fase 1: Uso Imediato ✅ CONCLUÍDO
- [x] Implementar componentes base
- [x] Adicionar configurações
- [x] Criar documentação

#### Fase 2: Integração Opcional (Quando Necessário)
- [ ] Usar em novos desenvolvimentos
- [ ] Migrar código existente gradualmente
- [ ] Testar em produção

#### Fase 3: Adoção Completa (Futuro)
- [ ] Substituir JOptionPane em código crítico
- [ ] Adicionar notificações de sucesso em todas operações
- [ ] Implementar dashboard de relatórios de erro

### Código Antigo vs Código Novo

#### ANTES (continua funcionando)

```java
JOptionPane.showMessageDialog(this, "Erro ao salvar", "Erro", JOptionPane.ERROR_MESSAGE);
```

#### DEPOIS (recomendado)

```java
NotificationUtils.showErrorMessage(this, "Erro ao salvar");
```

### Benefícios da Migração

- ✅ Toast não intrusivo
- ✅ Log estruturado automático
- ✅ Relatório de erro automático
- ✅ Melhor experiência do usuário
- ✅ Rastreabilidade completa
- ✅ Suporte técnico facilitado

### Compatibilidade

O sistema é **100% compatível** com código existente:
- Não requer alterações em código atual
- Funciona como wrapper sobre JOptionPane
- Pode ser ativado/desativado via configuração
- Rollback fácil se necessário

---

## 🎯 Boas Práticas

### ✅ Faça

- Use `notifySuccess()` para operações bem-sucedidas
- Use `showErrorMessage()` com exceção para erros críticos
- Use toasts para notificações não críticas
- Use dialogs para ações que requerem atenção do usuário
- Forneça mensagens claras e objetivas
- Inclua contexto nos relatórios de erro

### ❌ Evite

- Não use toast para mensagens críticas que requerem ação
- Não abuse de notificações (spam)
- Não use dialogs para operações em background
- Não ignore exceções sem reportar
- Não use mensagens genéricas ("Erro", "Falha")
- Não bloqueie a interface com dialogs desnecessários

---

## 🔧 Troubleshooting

### Toasts não aparecem

**Problema**: Notificações toast não são exibidas

**Soluções**:
```java
// 1. Verificar se está ativado
if (!NotificationManager.isToastEnabled()) {
    NotificationManager.setToastEnabled(true);
}

// 2. Verificar configuração
// application.properties
app.notification.toast.enabled=true

// 3. Verificar logs
// Procurar por: "Toast notifications enabled/disabled"
```

### Relatórios não são salvos

**Problema**: Relatórios de erro não são gerados

**Soluções**:
```java
// 1. Verificar se está ativado
if (!NotificationManager.isErrorReportingEnabled()) {
    NotificationManager.setErrorReportingEnabled(true);
}

// 2. Verificar configuração
// application.properties
app.notification.error-reporting.enabled=true

// 3. Verificar permissões da pasta error_reports/
// Criar manualmente se necessário

// 4. Verificar logs
// Procurar por: "Relatório de erro salvo" ou "Erro ao salvar relatório"
```

### Toast aparece atrás de outras janelas

**Problema**: Toast não fica sempre visível

**Causa**: Problema de permissões do sistema operacional

**Solução**:
```java
// Toast usa setAlwaysOnTop(true) por padrão
// Se não funcionar, pode ser limitação do SO
// Considere usar dialogs para mensagens críticas
```

### Exceção ao inicializar NotificationConfig

**Problema**: Erro ao carregar configurações Spring

**Solução**:
```properties
# Adicionar valores padrão no application.properties
app.notification.toast.enabled=true
app.notification.error-reporting.enabled=true
```

---

## 📚 Referências

### Classes Principais

#### NotificationManager
Gerenciador central do sistema de notificações.

**Métodos principais**:
- `notifySuccess(String message)` - Toast de sucesso
- `notifyError(String message)` - Toast de erro
- `notifyWarning(String message)` - Toast de aviso
- `notifyInfo(String message)` - Toast de informação
- `reportError(String message, String context, Throwable exception)` - Gerar relatório
- `setToastEnabled(boolean enabled)` - Ativar/desativar toasts
- `setErrorReportingEnabled(boolean enabled)` - Ativar/desativar relatórios

#### NotificationUtils
Utilitários para migração gradual.

**Métodos principais**:
- `showSuccessMessage(String message)` - Notificação de sucesso
- `showErrorMessage(Component parent, String message)` - Notificação de erro
- `showErrorMessage(Component parent, String message, Throwable exception)` - Erro com exceção
- `showWarningMessage(Component parent, String message)` - Notificação de aviso
- `showInfoMessage(Component parent, String message)` - Notificação de informação
- `showConfirmDialog(Component parent, String message, String title)` - Dialog de confirmação

#### ToastNotification
Componente visual de notificação.

**Tipos**:
- `Type.SUCCESS` - Verde
- `Type.ERROR` - Vermelho
- `Type.WARNING` - Amarelo
- `Type.INFO` - Azul

**Métodos estáticos**:
- `showSuccess(String message)`
- `showError(String message)`
- `showWarning(String message)`
- `showInfo(String message)`

#### ErrorReportingService
Serviço de gerenciamento de relatórios.

**Métodos principais**:
- `saveErrorReport(ErrorReport report)` - Salvar relatório
- `listErrorReports()` - Listar todos os relatórios
- `cleanOldReports()` - Limpar relatórios antigos

### Arquivos de Configuração

- **application.properties**: Configurações do sistema
- **NotificationConfig.java**: Configuração Spring Boot

### Diretórios

- **error_reports/**: Relatórios de erro em JSON
- **logs/**: Logs do sistema (sistema-inventario.log)

---

## 🎉 Conclusão

O sistema de notificação está **100% implementado** e pronto para uso. 

### Principais Vantagens

1. ✅ **Zero Impacto**: Não quebra código existente
2. ✅ **Fácil Adoção**: Uso imediato em novos códigos
3. ✅ **Migração Gradual**: Substitua código antigo conforme necessidade
4. ✅ **Configurável**: Ative/desative conforme necessário
5. ✅ **Rastreável**: Relatórios automáticos de erro
6. ✅ **Profissional**: Interface moderna e não intrusiva

### Recomendações

- Comece usando em novos desenvolvimentos
- Migre código existente gradualmente
- Monitore relatórios de erro periodicamente
- Ajuste configurações conforme necessidade

### Próximos Passos

1. Testar em ambiente real
2. Validar com usuários
3. Coletar feedback
4. Ajustar conforme necessário

---

**Última atualização**: Novembro 2025  
**Versão**: 1.0.0  
**Status**: ✅ Pronto para Produção  
**Autor**: Sistema de Inventário - IFMT
