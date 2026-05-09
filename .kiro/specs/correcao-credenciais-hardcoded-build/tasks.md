# Implementation Tasks - Correção Credenciais Hardcoded Build

## 1. Preparação e Backup

- [x] 1.1 Fazer backup da keystore atual
  - Localizar arquivo `inventario-release.keystore`
  - Criar cópia de segurança em local seguro
  - Documentar localização do backup
  - _Requirements: Security Checklist_

- [x] 1.2 Documentar credenciais atuais
  - Registrar senhas em gerenciador de senhas seguro (ex: 1Password, LastPass)
  - Documentar alias da keystore
  - Documentar caminho do arquivo keystore
  - Notificar equipe sobre mudança de processo
  - _Requirements: Security Checklist_

## 2. Implementação da Correção

- [x] 2.1 Criar arquivo keystore.properties
  - Na raiz do projeto `InventarioMobile/`, criar `keystore.properties`
  - Adicionar conteúdo:
    ```properties
    storePassword=inventario2025
    keyPassword=inventario2025
    keyAlias=inventario
    storeFile=../inventario-release.keystore
    ```
  - Verificar que arquivo foi criado corretamente
  - _Requirements: 2.1, 2.7_

- [x] 2.2 Atualizar .gitignore
  - Abrir arquivo `.gitignore` na raiz do projeto
  - Adicionar linhas:
    ```
    # Keystore credentials (NUNCA versionar)
    keystore.properties
    *.keystore
    *.jks
    ```
  - Verificar que `git status` não mostra `keystore.properties`
  - _Requirements: 2.8_

- [x] 2.3 Atualizar build.gradle
  - Abrir `InventarioMobile/app/build.gradle`
  - ANTES da seção `android {`, adicionar código para carregar propriedades
  - SUBSTITUIR seção `signingConfigs` por versão que lê do arquivo
  - Adicionar tratamento de erro se arquivo não existir
  - _Requirements: 2.1, 2.5, 2.7_


- [x] 2.4 Criar arquivo de exemplo (template)
  - Criar `InventarioMobile/keystore.properties.example`
  - Adicionar conteúdo template com placeholders
  - Versionar este arquivo no Git (é apenas exemplo)
  - _Requirements: 2.9_

## 3. Testes de Validação

- [x] 3.1 Testar build de release com arquivo configurado
  - Garantir que `keystore.properties` existe
  - Executar: `cd InventarioMobile && ./gradlew assembleRelease`
  - Verificar que build completa com sucesso
  - Verificar que APK é gerado em `app/build/outputs/apk/release/`
  - _Requirements: 2.2, 3.2_

- [x] 3.2 Verificar assinatura do APK
  - Executar: `jarsigner -verify -verbose app-release.apk`
  - Verificar que assinatura é válida
  - Verificar que alias está correto
  - _Requirements: 3.2_

- [x] 3.3 Testar build sem arquivo (falha graceful)
  - Renomear `keystore.properties` temporariamente
  - Executar: `./gradlew assembleRelease`
  - Verificar que build FALHA com mensagem clara
  - Verificar que mensagem indica necessidade de criar arquivo
  - Restaurar `keystore.properties`
  - _Requirements: 2.5_

- [x] 3.4 Testar build de debug (não afetado)
  - Executar: `./gradlew assembleDebug`
  - Verificar que build completa normalmente
  - Verificar que não requer `keystore.properties`
  - _Requirements: 3.1_

## 4. Documentação

- [x] 4.1 Criar documentação de configuração
  - Criar arquivo `InventarioMobile/CONFIGURACAO_KEYSTORE.md`
  - Documentar passo a passo para configurar `keystore.properties`
  - Incluir avisos de segurança
  - Incluir troubleshooting comum
  - _Requirements: 2.9_

- [ ] 4.2 Atualizar README do projeto
  - Adicionar seção sobre configuração da keystore
  - Referenciar `CONFIGURACAO_KEYSTORE.md`
  - Adicionar avisos sobre não versionar credenciais
  - _Requirements: 2.9_

- [ ] 4.3 Criar checklist de segurança
  - Documentar processo de rotação de credenciais
  - Documentar o que fazer em caso de vazamento
  - Documentar boas práticas de segurança
  - _Requirements: Security Checklist_

## 5. Limpeza do Histórico do Git

- [ ] 5.1 Verificar exposição no histórico
  - Executar: `git log --all -p | grep "inventario2025"`
  - Documentar quantos commits contêm as senhas
  - Decidir estratégia de limpeza (filter-repo, BFG, ou novo repo)
  - _Requirements: 2.3_

- [ ] 5.2 Preparar limpeza do histórico
  - Notificar toda a equipe sobre reescrita do histórico
  - Pedir que todos façam commit/push de trabalho pendente
  - Agendar janela de manutenção
  - Fazer backup completo do repositório
  - _Requirements: 2.3_

- [ ] 5.3 Executar limpeza com git-filter-repo
  - Instalar: `pip install git-filter-repo`
  - Criar arquivo `passwords.txt` com senhas a remover
  - Executar: `git filter-repo --replace-text passwords.txt`
  - Verificar que senhas foram removidas do histórico
  - _Requirements: 2.3_

- [ ] 5.4 Force push e notificar equipe
  - Executar: `git push --force --all`
  - Notificar equipe para re-clonar repositório
  - Fornecer instruções de migração
  - Verificar que todos migraram com sucesso
  - _Requirements: 2.3_

## 6. Rotação de Credenciais

- [ ] 6.1 Gerar nova keystore (opcional mas recomendado)
  - Executar: `keytool -genkey -v -keystore inventario-release-new.keystore ...`
  - Documentar novas credenciais em gerenciador de senhas
  - Atualizar `keystore.properties` com novas credenciais
  - Testar build com nova keystore
  - _Requirements: Security Best Practices_

- [ ] 6.2 Atualizar credenciais em todos os ambientes
  - Atualizar `keystore.properties` local de cada desenvolvedor
  - Atualizar variáveis de ambiente no CI/CD
  - Atualizar documentação com novas instruções
  - Verificar que todos os ambientes funcionam
  - _Requirements: Security Best Practices_

## 7. Configuração CI/CD

- [ ] 7.1 Configurar variáveis de ambiente no CI/CD
  - Adicionar `KEYSTORE_PASSWORD` como variável secreta
  - Adicionar `KEY_PASSWORD` como variável secreta
  - Adicionar `KEY_ALIAS` como variável
  - Adicionar `KEYSTORE_FILE` (base64 encoded) como variável secreta
  - _Requirements: 2.6_

- [ ] 7.2 Atualizar script de build do CI/CD
  - Modificar pipeline para criar `keystore.properties` dinamicamente
  - Usar variáveis de ambiente para preencher credenciais
  - Garantir que arquivo é criado antes do build
  - Garantir que arquivo é deletado após o build
  - _Requirements: 2.6_

- [ ] 7.3 Testar build no CI/CD
  - Executar pipeline de build
  - Verificar que `keystore.properties` é criado corretamente
  - Verificar que build de release completa
  - Verificar que APK é assinado corretamente
  - Verificar que logs não expõem credenciais
  - _Requirements: 2.6_

## 8. Validação Final

- [ ] 8.1 Verificar que credenciais não estão no Git
  - Executar: `git log --all -p | grep -i "password"`
  - Executar: `git log --all -p | grep "inventario2025"`
  - Verificar que nenhuma senha aparece
  - _Requirements: 2.3_

- [ ] 8.2 Verificar .gitignore
  - Executar: `git status`
  - Verificar que `keystore.properties` não aparece
  - Verificar que `*.keystore` não aparece
  - _Requirements: 2.8_

- [ ] 8.3 Auditoria de segurança
  - Revisar todas as mudanças no código
  - Verificar que nenhuma credencial está hardcoded
  - Verificar que documentação está completa
  - Verificar que equipe foi treinada
  - _Requirements: 2.6_

- [ ] 8.4 Teste end-to-end
  - Clonar repositório em máquina limpa
  - Seguir documentação para configurar `keystore.properties`
  - Executar build de release
  - Verificar que APK é gerado e assinado
  - _Requirements: 2.9_

## 9. Comunicação e Treinamento

- [ ] 9.1 Documentar mudanças no CHANGELOG
  - Adicionar entrada sobre correção de segurança
  - Explicar novo processo de configuração
  - Referenciar documentação detalhada
  - _Requirements: Documentation_

- [ ] 9.2 Treinar equipe
  - Realizar sessão de treinamento sobre novo processo
  - Explicar importância da segurança de credenciais
  - Demonstrar como configurar `keystore.properties`
  - Responder dúvidas da equipe
  - _Requirements: Security Checklist_

- [ ] 9.3 Criar runbook de incidentes
  - Documentar o que fazer se credenciais vazarem
  - Documentar processo de rotação emergencial
  - Documentar contatos de segurança
  - _Requirements: Security Best Practices_

## Notas Importantes

### ⚠️ Segurança Crítica

- **NUNCA** commite `keystore.properties` no Git
- **NUNCA** compartilhe credenciais por email/chat
- **SEMPRE** use gerenciador de senhas para armazenar credenciais
- **SEMPRE** rotacione credenciais após exposição

### 📋 Checklist de Segurança

Antes de considerar a correção completa:
- [ ] Credenciais não estão no Git (histórico limpo)
- [ ] `.gitignore` configurado corretamente
- [ ] Documentação completa e clara
- [ ] Equipe treinada no novo processo
- [ ] CI/CD configurado com variáveis de ambiente
- [ ] Auditoria de segurança realizada
- [ ] Runbook de incidentes criado

### 🔄 Rotação de Credenciais

Após implementar a correção, considere:
1. Gerar nova keystore com credenciais diferentes
2. Atualizar todos os ambientes
3. Invalidar credenciais antigas
4. Documentar novas credenciais em local seguro

### 📚 Referências

- [Android Developers - Sign your app](https://developer.android.com/studio/publish/app-signing)
- [OWASP - Hardcoded Credentials](https://owasp.org/www-community/vulnerabilities/Use_of_hard-coded_password)
- [Git - Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
