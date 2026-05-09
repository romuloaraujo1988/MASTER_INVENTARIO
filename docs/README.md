# 📚 Documentação do Sistema de Inventário

## 🎯 Visão Geral

Documentação completa e consolidada do Sistema de Inventário IFMT (SIHCP).

**Versão**: 1.2.0  
**Última atualização**: 07/11/2025  
**Status**: ✅ Documentação Unificada

---

## 🎯 Manuais por Público-Alvo (novos — maio/2026)

Para usuários finais, a documentação mais atual está em **[manuais/](manuais/)**:

- [**MANUAL_COLETOR.md**](manuais/MANUAL_COLETOR.md) — Coletor em campo com o app Android
- [**MANUAL_ADMINISTRADOR.md**](manuais/MANUAL_ADMINISTRADOR.md) — Administrador do servidor API e infraestrutura
- [**MANUAL_OPERADOR.md**](manuais/MANUAL_OPERADOR.md) — Operador do desktop Swing para gestão patrimonial

Os manuais abaixo (sistema antigo) permanecem para referência histórica.

---

## 📋 Documentos Principais

### 🔔 Sistema de Notificação
**[SISTEMA_NOTIFICACAO.md](SISTEMA_NOTIFICACAO.md)**
- Sistema de notificações não intrusivas
- Toasts visuais e relatórios de erro
- Configuração e exemplos de uso
- Troubleshooting completo

### 🔧 Histórico de Correções
**[HISTORICO_CORRECOES.md](HISTORICO_CORRECOES.md)**
- 9 correções documentadas (Nov/2025)
- Organizadas por data e categoria
- Estatísticas e métricas
- Lições aprendidas

### 📱 Guia Mobile
**[GUIA_MOBILE.md](GUIA_MOBILE.md)**
- Backend API REST (Spring Boot)
- Android App (Kotlin + MVVM)
- Modo Offline (Room Database)
- Implementação e Deploy

### 🎯 Guia de Funcionalidades
**[GUIA_FUNCIONALIDADES.md](GUIA_FUNCIONALIDADES.md)**
- Descrição Resumida (reconhecimento de marcas)
- Salas Finalizadas (controle por sala)
- Melhorias no Módulo de Coleta
- Sistema de Retry
- Gerenciamento de Inventários

---

## 📖 Manual do Usuário

**[MANUAL_DO_USUARIO.md](MANUAL_DO_USUARIO.md)**
- Guia completo para usuários finais
- Instruções de uso do sistema
- Funcionalidades principais

---

## 📁 Documentação Adicional

Para documentação técnica detalhada, consulte a pasta **[arquivo/](arquivo/)**:

### 🏗️ Build e Deploy
- `arquivo/build/` - Guias de build, execução e produção

### 📊 Requisitos e Especificações
- `arquivo/requisitos/` - Requisitos do sistema e banco de dados

### 📱 Mobile
- `arquivo/mobile/` - Documentação mobile detalhada

### 🔧 Funcionalidades
- `arquivo/funcionalidades/` - Documentação de features

### 📝 Planos e Instruções
- `arquivo/planos/` - Planos de implementação
- `arquivo/instrucoes/` - Instruções técnicas

### 🐛 Correções
- `arquivo/correcoes/` - Histórico detalhado de correções

---

## 📁 Estrutura da Documentação

```
DOCUMENTAÇÃO/
├── README.md                          # 📖 Este arquivo (índice geral)
│
├── Guias Unificados (4 documentos principais)
│   ├── SISTEMA_NOTIFICACAO.md        # 🔔 Sistema de notificações
│   ├── HISTORICO_CORRECOES.md        # 🔧 Histórico de correções
│   ├── GUIA_MOBILE.md                # 📱 Desenvolvimento mobile
│   └── GUIA_FUNCIONALIDADES.md       # 🎯 Funcionalidades do sistema
│
├── MANUAL_DO_USUARIO.md              # 👤 Manual do usuário
│
└── arquivo/                           # 📁 Documentação detalhada (40+ docs)
    ├── README.md                      # Índice do arquivo
    ├── build/                         # Build, deploy e execução
    ├── requisitos/                    # Requisitos e especificações
    ├── mobile/                        # Documentação mobile detalhada
    ├── funcionalidades/               # Documentação de features
    ├── correcoes/                     # Histórico detalhado de correções
    ├── planos/                        # Planos de implementação
    └── instrucoes/                    # Instruções técnicas
```

---

## 🚀 Início Rápido

### Para Desenvolvedores

1. **Começar pelo básico**:
   - Leia [DOCUMENTACAO_REQUISITOS_SISTEMA.md](DOCUMENTACAO_REQUISITOS_SISTEMA.md)
   - Veja [EXECUCAO_APLICACAO.md](EXECUCAO_APLICACAO.md)

2. **Desenvolvimento Mobile**:
   - Consulte [GUIA_MOBILE.md](GUIA_MOBILE.md)
   - Backend API + Android App completo

3. **Funcionalidades**:
   - Veja [GUIA_FUNCIONALIDADES.md](GUIA_FUNCIONALIDADES.md)
   - Todas as features documentadas

4. **Correções e Bugs**:
   - Consulte [HISTORICO_CORRECOES.md](HISTORICO_CORRECOES.md)
   - Lições aprendidas e soluções

### Para Usuários

1. **Manual do Usuário**:
   - Leia [MANUAL_DO_USUARIO.md](MANUAL_DO_USUARIO.md)
   - Guia completo de uso

2. **Sistema de Notificações**:
   - Veja [SISTEMA_NOTIFICACAO.md](SISTEMA_NOTIFICACAO.md)
   - Como usar as notificações

### Para Administradores

1. **Build e Deploy**:
   - Consulte [GUIA_BUILD_PRODUCAO.md](GUIA_BUILD_PRODUCAO.md)
   - Processo completo de deploy

2. **Modo Offline**:
   - Veja [GUIA_HABILITACAO_MODO_OFFLINE.md](GUIA_HABILITACAO_MODO_OFFLINE.md)
   - Configuração e uso

---

## 📊 Estatísticas da Documentação

### Documentos Consolidados
- **Guias Unificados**: 4 documentos principais
- **Manual do Usuário**: 1 documento
- **Documentos Arquivados**: 40+ documentos organizados
- **Redução**: ~86% nos arquivos ativos (6 vs 42)
- **Benefício**: Informação centralizada e organizada

### Cobertura
- ✅ Sistema de Notificações (completo)
- ✅ Histórico de Correções (9 correções)
- ✅ Desenvolvimento Mobile (Backend + Android)
- ✅ Funcionalidades (6 principais)
- ✅ Requisitos e Especificações
- ✅ Guias de Build e Deploy

---

## 🔍 Como Encontrar Informação

### Por Tópico

**Notificações e Alertas** → [SISTEMA_NOTIFICACAO.md](SISTEMA_NOTIFICACAO.md)  
**Bugs e Correções** → [HISTORICO_CORRECOES.md](HISTORICO_CORRECOES.md)  
**Desenvolvimento Mobile** → [GUIA_MOBILE.md](GUIA_MOBILE.md)  
**Funcionalidades** → [GUIA_FUNCIONALIDADES.md](GUIA_FUNCIONALIDADES.md)  
**Uso do Sistema** → [MANUAL_DO_USUARIO.md](MANUAL_DO_USUARIO.md)  
**Build e Deploy** → [GUIA_BUILD_PRODUCAO.md](GUIA_BUILD_PRODUCAO.md)

### Por Tipo de Usuário

**Desenvolvedor Backend** → GUIA_MOBILE.md (seção Backend)  
**Desenvolvedor Android** → GUIA_MOBILE.md (seção Android)  
**Usuário Final** → MANUAL_DO_USUARIO.md  
**Administrador** → GUIA_BUILD_PRODUCAO.md  
**Suporte Técnico** → HISTORICO_CORRECOES.md

---

## 📝 Convenções

### Nomenclatura
- **GUIA_*.md**: Documentos unificados principais
- **HISTORICO_*.md**: Históricos e logs
- **MANUAL_*.md**: Manuais de usuário
- **DOCUMENTACAO_*.md**: Especificações técnicas

### Status
- ✅ Implementado e documentado
- 🔄 Em desenvolvimento
- ⏳ Planejado
- ❌ Descontinuado

### Prioridade
- 🔴 Alta
- 🟡 Média
- 🟢 Baixa

---

## 🔄 Manutenção da Documentação

### Atualização de Documentos

1. **Documentos Unificados**: Atualizar diretamente
2. **Novos Recursos**: Adicionar ao guia apropriado
3. **Correções**: Documentar em HISTORICO_CORRECOES.md
4. **Versões**: Atualizar BUILD_VERSAO_*.md

### Processo de Consolidação

Quando houver muitos documentos fragmentados:
1. Identificar documentos relacionados
2. Criar guia unificado
3. Mover originais para `arquivo/`
4. Atualizar este README

---

## 📞 Suporte

### Documentação
- Consulte os guias apropriados acima
- Verifique o histórico de correções
- Veja exemplos de código nos guias

### Problemas Conhecidos
- Consulte [HISTORICO_CORRECOES.md](HISTORICO_CORRECOES.md)
- Seção de Troubleshooting em cada guia

### Contribuindo
- Mantenha documentação atualizada
- Siga convenções de nomenclatura
- Documente todas as mudanças importantes

---

## 🎯 Próximos Passos

### Documentação Planejada
- [ ] GUIA_BANCO_DADOS.md (consolidar docs de BD)
- [ ] GUIA_DESENVOLVIMENTO.md (setup e padrões)
- [ ] CHANGELOG.md (histórico de versões)

### Melhorias
- [ ] Adicionar diagramas UML
- [ ] Criar tutoriais em vídeo
- [ ] Documentação de API (Swagger)
- [ ] Guia de contribuição

---

**Mantido por**: Equipe de Desenvolvimento SIHCP  
**Última revisão**: 07/11/2025  
**Versão da documentação**: 2.0.0
