# SIHCP Mobile - Versão 2.7.0

## 📱 Informações do APK

- **Nome:** SIHCP-Mobile-2.7.0.apk
- **Versão:** 2.7.0 (Build 44)
- **Tamanho:** 15.26 MB
- **Compatibilidade:** Android 6.0+ (API 23+)
- **Assinatura:** Assinado com certificado de produção
- **Data de Compilação:** 12/12/2025

## ✅ Funcionalidades Implementadas

### Arquitetura
- ✅ Clean Architecture + MVVM + Hilt
- ✅ Injeção de dependência automática
- ✅ Separação clara de camadas (Data, Domain, Presentation)

### Sincronização
- ✅ Batch sync (múltiplas coletas em uma requisição)
- ✅ Sincronização em background com WorkManager
- ✅ Retry automático com backoff exponencial
- ✅ Constraints de rede e bateria
- ✅ Fallback automático para sync individual

### Validação
- ✅ Validação de patrimônios antes de coletar
- ✅ Detecção de coletas duplicadas
- ✅ Verificação de patrimônios já coletados
- ✅ Feedback detalhado ao usuário

### Coleta
- ✅ Coleta com QR Code
- ✅ Coleta manual por número
- ✅ Coleta de itens sem etiqueta
- ✅ Captura de foto obrigatória
- ✅ Vibração ao coletar (configurável)

### Relatórios
- ✅ Exportação em PDF
- ✅ Exportação em Excel (TSV)
- ✅ Exportação em CSV
- ✅ Filtros por status (todos, coletados, não coletados)
- ✅ Estatísticas detalhadas

### Interface
- ✅ Dark Mode (automático, claro, escuro)
- ✅ Busca rápida com servidor
- ✅ Rolagem infinita em listas
- ✅ Paginação com Paging 3
- ✅ Dashboard com gráficos

### Segurança
- ✅ Autenticação com JWT
- ✅ Refresh token automático
- ✅ Biometria/PIN local
- ✅ Controle de acesso por roles (RBAC)

### Offline
- ✅ Funciona completamente offline
- ✅ Sincronização automática quando online
- ✅ Banco de dados local com Room
- ✅ Estratégia offline-first

## 🚀 Como Instalar

### Via ADB (Desenvolvimento)
```bash
adb install SIHCP-Mobile-2.7.0.apk
```

### Via Google Play (Produção)
- Fazer upload para Google Play Console
- Configurar lançamento gradual
- Monitorar métricas de crash

### Via Distribuição Interna
- Enviar link para download
- Usuários clicam no link
- Android instala automaticamente

## 📋 Requisitos do Sistema

- **Android:** 6.0 ou superior (API 23+)
- **RAM:** Mínimo 2GB (recomendado 4GB+)
- **Armazenamento:** 50MB livres
- **Conexão:** WiFi ou dados móveis (para sincronização)

## 🔐 Segurança

- ✅ Assinado com certificado de produção
- ✅ Certificado válido até 2053
- ✅ Algoritmo SHA256withRSA
- ✅ Zipalign otimizado
- ✅ ProGuard configurado

## 📊 Compatibilidade

| Versão Android | Status |
|---|---|
| 6.0 (API 23) | ✅ Suportado |
| 7.0 (API 24) | ✅ Suportado |
| 8.0 (API 26) | ✅ Suportado |
| 9.0 (API 28) | ✅ Suportado |
| 10.0 (API 29) | ✅ Suportado |
| 11.0 (API 30) | ✅ Suportado |
| 12.0 (API 31) | ✅ Suportado |
| 13.0 (API 33) | ✅ Suportado |
| 14.0 (API 34) | ✅ Suportado |

## 🐛 Problemas Conhecidos

Nenhum problema crítico identificado.

## 📞 Suporte

Para reportar bugs ou solicitar features:
- Email: suporte@inventario.com
- Documentação: https://docs.inventario.com

## 📝 Notas de Versão

### v2.7.0 (12/12/2025)
- ✅ Compilação release assinada
- ✅ Otimização com zipalign
- ✅ Pronto para produção

---

**Desenvolvido por:** Romulo Araujo  
**Instituição:** IFMT - Instituto Federal de Mato Grosso  
**Licença:** Todos os direitos reservados © 2025
