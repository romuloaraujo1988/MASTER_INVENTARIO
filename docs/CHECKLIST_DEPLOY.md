# ✅ Checklist de Deploy - Sistema de Inventário Mobile

## 📋 PRÉ-DEPLOY

### Preparação
- [ ] Ler plano de implantação completo
- [ ] Definir data e horário do deploy
- [ ] Notificar equipe técnica
- [ ] Notificar administradores
- [ ] Notificar usuários finais
- [ ] Preparar ambiente de homologação
- [ ] Testar em homologação

### Backups
- [ ] Backup do banco de dados PostgreSQL
- [ ] Backup do código (Git tag)
- [ ] Backup do JAR atual
- [ ] Backup do APK atual
- [ ] Verificar integridade dos backups
- [ ] Documentar localização dos backups

### Documentação
- [ ] Atualizar documentação de API
- [ ] Preparar guia de rollback
- [ ] Preparar comunicados
- [ ] Atualizar changelog
- [ ] Revisar plano de contingência

---

## 🔧 DEPLOY BACKEND

### Preparação
- [ ] Verificar horário (madrugada/fim de semana)
- [ ] Equipe técnica disponível
- [ ] Acesso ao servidor confirmado
- [ ] Ferramentas necessárias instaladas

### Execução
- [ ] Identificar processo Java rodando
- [ ] Parar servidor gracefully (kill -15)
- [ ] Aguardar 10 segundos
- [ ] Verificar que processo parou
- [ ] Navegar para diretório do projeto
- [ ] Fazer backup do JAR atual
- [ ] Atualizar código (git pull)
- [ ] Compilar projeto (mvnw clean package)
- [ ] Verificar compilação bem-sucedida
- [ ] Verificar tamanho do JAR gerado

### Inicialização
- [ ] Iniciar servidor com profile mobile
- [ ] Verificar PID do processo
- [ ] Aguardar mensagem "Started MobileApiApplication"
- [ ] Verificar logs de erro
- [ ] Verificar uso de CPU/memória

### Validação
- [ ] Testar endpoint /api/mobile/dashboard/stats
- [ ] Verificar campo patrimoniosColetados presente
- [ ] Verificar campo patrimoniosPendentes presente
- [ ] Verificar valores corretos
- [ ] Testar outros endpoints críticos
- [ ] Verificar tempo de resposta < 500ms
- [ ] Verificar logs sem erros

### Monitoramento (Primeiras 2 horas)
- [ ] Verificar logs a cada 15 minutos
- [ ] Monitorar uso de recursos
- [ ] Verificar requisições bem-sucedidas
- [ ] Verificar taxa de erro < 1%
- [ ] Responder a alertas imediatamente

---

## 📱 DEPLOY ANDROID - GRUPO PILOTO

### Preparação
- [ ] Backend estável por 24 horas
- [ ] Grupo piloto definido (5-8 pessoas)
- [ ] Contatos do grupo piloto atualizados
- [ ] Método de distribuição definido

### Compilação
- [ ] Navegar para InventarioMobile
- [ ] Limpar build anterior (gradlew clean)
- [ ] Compilar release (gradlew assembleRelease)
- [ ] Verificar APK gerado
- [ ] Verificar tamanho do APK (~10-20 MB)

### Assinatura
- [ ] Localizar keystore
- [ ] Assinar APK com jarsigner
- [ ] Otimizar com zipalign
- [ ] Verificar assinatura (jarsigner -verify)
- [ ] Renomear APK (app-release-v2.0.1.apk)

### Distribuição
- [ ] Upload APK para servidor interno
- [ ] Gerar link de download
- [ ] Preparar mensagem para piloto
- [ ] Enviar link via WhatsApp/Email
- [ ] Confirmar recebimento por todos
- [ ] Fornecer instruções de instalação

### Monitoramento (48 horas)
- [ ] Coletar feedback diário
- [ ] Verificar crashes reportados
- [ ] Verificar problemas de sincronização
- [ ] Verificar problemas de UI
- [ ] Documentar todos os problemas
- [ ] Resolver problemas críticos

### Critérios de Aprovação
- [ ] Zero crashes críticos
- [ ] Feedback positivo de 80%+ dos pilotos
- [ ] Todas as funcionalidades testadas
- [ ] Dashboard carrega corretamente
- [ ] Gráficos funcionam
- [ ] Coletas funcionam normalmente

---

## 📱 DEPLOY ANDROID - TODOS OS USUÁRIOS

### Pré-requisitos
- [ ] Grupo piloto aprovou (48 horas)
- [ ] Zero crashes críticos
- [ ] Backend estável por 72 horas
- [ ] Feedback positivo coletado
- [ ] Problemas conhecidos documentados

### Preparação
- [ ] Preparar comunicado oficial
- [ ] Preparar guia de instalação
- [ ] Preparar FAQ
- [ ] Definir canais de suporte
- [ ] Equipe de suporte preparada

### Distribuição
- [ ] Upload APK para servidor
- [ ] Gerar link curto
- [ ] Testar link de download
- [ ] Enviar comunicado para todos
- [ ] Postar em grupos/canais oficiais
- [ ] Disponibilizar suporte

### Monitoramento (Primeira Semana)
- [ ] Verificar taxa de adoção diária
- [ ] Coletar feedback de usuários
- [ ] Responder dúvidas rapidamente
- [ ] Documentar problemas reportados
- [ ] Criar FAQ com dúvidas comuns
- [ ] Atualizar documentação conforme necessário

---

## 📊 PÓS-DEPLOY

### Primeiras 24 Horas
- [ ] Verificar logs a cada 2 horas
- [ ] Monitorar uso de recursos
- [ ] Verificar taxa de erro < 1%
- [ ] Verificar tempo de resposta < 500ms
- [ ] Responder a problemas imediatamente
- [ ] Documentar incidentes

### Primeira Semana
- [ ] Verificar logs diariamente
- [ ] Coletar feedback de usuários
- [ ] Analisar métricas de uso
- [ ] Verificar performance do servidor
- [ ] Atualizar documentação
- [ ] Planejar próximas melhorias

### Métricas de Sucesso
- [ ] Zero downtime não planejado
- [ ] Taxa de erro < 1%
- [ ] Tempo de resposta < 500ms
- [ ] 95%+ usuários atualizaram
- [ ] Feedback positivo > 80%
- [ ] Zero rollbacks necessários

---

## 🚨 CONTINGÊNCIA

### Preparação
- [ ] Plano de rollback documentado
- [ ] Backups verificados e acessíveis
- [ ] Contatos de emergência atualizados
- [ ] Procedimentos de rollback testados

### Se Necessário Rollback Backend
- [ ] Identificar problema rapidamente
- [ ] Decidir por rollback (< 15 minutos)
- [ ] Parar servidor atual
- [ ] Restaurar JAR do backup
- [ ] Reiniciar servidor
- [ ] Verificar funcionamento
- [ ] Notificar equipe e usuários
- [ ] Documentar causa do rollback

### Se Necessário Rollback Android
- [ ] Identificar problema rapidamente
- [ ] Remover link de download
- [ ] Disponibilizar versão anterior
- [ ] Notificar usuários afetados
- [ ] Fornecer instruções de downgrade
- [ ] Documentar causa do rollback

---

## 📝 COMUNICAÇÃO

### Antes do Deploy
- [ ] Notificar administradores (3 dias antes)
- [ ] Notificar usuários (1 dia antes)
- [ ] Confirmar horário de manutenção
- [ ] Preparar mensagens de status

### Durante o Deploy
- [ ] Enviar status: "Iniciando atualização"
- [ ] Enviar status: "Backend atualizado"
- [ ] Enviar status: "Testes em andamento"
- [ ] Enviar status: "Sistema disponível"

### Após o Deploy
- [ ] Confirmar conclusão bem-sucedida
- [ ] Disponibilizar nova versão do app
- [ ] Fornecer instruções de atualização
- [ ] Agradecer pela paciência

---

## 📈 VALIDAÇÃO FINAL

### Funcionalidades Críticas
- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Estatísticas aparecem corretamente
- [ ] Gráficos carregam
- [ ] QR Code scanner funciona
- [ ] Coleta manual funciona
- [ ] Item sem etiqueta funciona
- [ ] Sincronização funciona
- [ ] Offline mode funciona

### Performance
- [ ] Tempo de resposta < 500ms
- [ ] Taxa de erro < 1%
- [ ] CPU < 70%
- [ ] Memória < 80%
- [ ] Disco < 80%

### Qualidade
- [ ] Zero crashes críticos
- [ ] Zero bugs bloqueantes
- [ ] Feedback positivo > 80%
- [ ] Documentação atualizada
- [ ] Código versionado (Git tag)

---

## ✅ CONCLUSÃO

### Documentação
- [ ] Atualizar changelog
- [ ] Atualizar documentação técnica
- [ ] Documentar lições aprendidas
- [ ] Atualizar FAQ
- [ ] Arquivar logs de deploy

### Git
- [ ] Criar tag de versão (v2.0.1)
- [ ] Push de tags
- [ ] Atualizar README se necessário
- [ ] Fechar issues relacionadas

### Equipe
- [ ] Agradecer equipe técnica
- [ ] Compartilhar resultados
- [ ] Celebrar sucesso 🎉
- [ ] Planejar próximas melhorias

---

**Data do Deploy:** ___/___/_____  
**Responsável:** _________________  
**Status Final:** [ ] Sucesso  [ ] Parcial  [ ] Rollback  

**Observações:**
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
