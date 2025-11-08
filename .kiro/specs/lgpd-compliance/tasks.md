# Implementation Plan

- [ ] 1. Criar estrutura de banco de dados para LGPD
  - Criar script SQL com as 3 tabelas necessárias
  - Adicionar índices para performance
  - Criar script de rollback
  - _Requirements: 1.1, 2.2, 3.2_

- [ ] 2. Implementar models de domínio
  - [ ] 2.1 Criar classe Consentimento.java com todos os atributos
    - Incluir enum TipoConsentimento
    - Adicionar validações básicas
    - Implementar equals/hashCode
    - _Requirements: 2.1, 2.5_
  
  - [ ] 2.2 Criar classe PoliticaPrivacidade.java
    - Incluir versionamento
    - Adicionar hash de integridade
    - Implementar métodos utilitários
    - _Requirements: 1.3, 6.2_
  
  - [ ] 2.3 Criar exceções customizadas
    - ConsentimentoException
    - PoliticaPrivacidadeException
    - _Requirements: 2.7, 8.3_

- [ ] 3. Implementar camada DAO
  - [ ] 3.1 Criar ConsentimentoDAO.java
    - Método inserirConsentimento
    - Método buscarConsentimentoAtivo
    - Método temConsentimentoValido
    - Método revogarConsentimento
    - Método listarConsentimentosUsuario
    - Método buscarUsuariosSemConsentimentoAtual
    - Método gerarRelatorioAuditoria
    - _Requirements: 2.5, 3.1, 4.5, 9.1_
  
  - [ ] 3.2 Criar PoliticaPrivacidadeDAO.java
    - Método buscarPoliticaAtiva
    - Método buscarPorVersao
    - Método inserirPolitica
    - Método atualizarPolitica
    - Método desativarPoliticasAnteriores
    - Método listarHistorico
    - _Requirements: 1.2, 6.1, 6.6, 7.2_
  
  - [ ] 3.3 Criar LogConsentimentoDAO.java
    - Método registrarLog
    - Método buscarLogsPorUsuario
    - Método buscarLogsPorConsentimento
    - _Requirements: 3.1, 5.1, 8.3_

- [ ] 4. Implementar camada de serviço
  - [ ] 4.1 Criar ConsentimentoService.java
    - Método precisaConsentimento
    - Método registrarConsentimento com validações
    - Método revogarConsentimento com notificação
    - Método validarConsentimento
    - Método notificarDPO (email)
    - _Requirements: 2.1, 4.1, 5.1, 5.2, 8.1_
  
  - [ ] 4.2 Criar PoliticaPrivacidadeService.java
    - Método buscarPoliticaAtiva com cache
    - Método atualizarPolitica com versionamento
    - Método invalidarCache
    - Método gerarHashConteudo
    - _Requirements: 1.1, 6.1, 6.2, 6.3_
  
  - [ ] 4.3 Criar AuditoriaService.java
    - Método gerarRelatorioConsentimentos
    - Método exportarRelatorioPDF
    - Método registrarAcesso
    - _Requirements: 3.3, 3.4, 9.1, 9.2_

- [ ] 5. Criar documento inicial da Política de Privacidade
  - Escrever conteúdo em HTML formatado
  - Incluir todas as seções obrigatórias (dados coletados, finalidade, base legal, etc.)
  - Definir versão inicial como 1.0.0
  - Inserir no banco de dados
  - _Requirements: 1.3, 1.4_

- [ ] 6. Implementar componentes de UI - Dialogs
  - [ ] 6.1 Criar ConsentimentoDialog.java
    - Layout com resumo da política
    - Botão "Ver Política Completa"
    - Checkboxes para tipos de consentimento
    - Destacar consentimentos obrigatórios vs opcionais
    - Botões Aceitar/Recusar
    - Validação de checkboxes obrigatórios
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 10.1, 10.5_
  
  - [ ] 6.2 Criar PoliticaPrivacidadeViewer.java
    - JEditorPane para exibir HTML
    - Scroll para documento completo
    - Exibir versão e data
    - Botão para baixar PDF
    - _Requirements: 1.2, 1.4, 1.5, 7.2, 7.4_
  
  - [ ] 6.3 Criar ConfirmacaoRevogacaoDialog.java
    - Explicar consequências da revogação
    - Campo opcional para motivo
    - Botões Confirmar/Cancelar
    - _Requirements: 4.3, 4.4_

- [ ] 7. Modificar JLogin para verificar consentimento
  - Adicionar link "Política de Privacidade" na tela de login
  - Após login bem-sucedido, verificar se usuário tem consentimento válido
  - Se não tiver, exibir ConsentimentoDialog antes de abrir MainFrame
  - Se recusar consentimento, fazer logout e exibir mensagem
  - _Requirements: 1.1, 2.1, 2.6, 2.7, 8.1_

- [ ] 8. Implementar seção de Privacidade no perfil do usuário
  - [ ] 8.1 Criar PrivacidadePanel.java
    - Exibir status do consentimento (ativo/revogado)
    - Exibir data de concessão
    - Botão "Ver Política Aceita"
    - Botão "Revogar Consentimento"
    - _Requirements: 4.1, 4.2, 7.1_
  
  - [ ] 8.2 Integrar PrivacidadePanel no PerfilUsuarioFrame
    - Adicionar nova aba "Privacidade"
    - Conectar eventos dos botões
    - _Requirements: 4.1, 7.1_

- [ ] 9. Implementar tela administrativa de gestão de consentimentos
  - [ ] 9.1 Criar GestaoConsentimentosFrame.java
    - Tabela com lista de consentimentos
    - Filtros por usuário, data, status
    - Botão "Exportar Relatório"
    - Botão "Atualizar Política"
    - _Requirements: 3.1, 3.3, 3.4, 6.1_
  
  - [ ] 9.2 Criar EditorPoliticaDialog.java
    - Campo para título
    - Editor de texto rico para conteúdo
    - Geração automática de versão
    - Preview antes de publicar
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ] 9.3 Adicionar menu "LGPD" no MainFrame (apenas para ADMIN)
    - Item "Gestão de Consentimentos"
    - Item "Política de Privacidade"
    - Item "Relatórios de Auditoria"
    - _Requirements: 3.1, 6.1, 9.1_

- [ ] 10. Implementar sistema de notificação ao DPO
  - [ ] 10.1 Criar EmailService.java
    - Método enviarEmailDPO
    - Template de email para revogação
    - Configuração de SMTP
    - _Requirements: 5.2, 5.3_
  
  - [ ] 10.2 Criar TarefaDPOService.java
    - Registrar tarefa pendente quando houver revogação
    - Listar tarefas pendentes
    - Marcar tarefa como concluída
    - _Requirements: 5.4_

- [ ] 11. Implementar validação de consentimento em operações críticas
  - Criar interceptor/filter para validar consentimento
  - Adicionar validação antes de acessar dados pessoais
  - Registrar tentativas de acesso sem consentimento
  - _Requirements: 8.1, 8.2, 8.3, 8.4_

- [ ] 12. Implementar funcionalidade de exportação de dados
  - [ ] 12.1 Criar ExportacaoDadosService.java
    - Método exportarDadosUsuario (JSON)
    - Método gerarPDFDadosUsuario
    - Incluir todos os dados pessoais do usuário
    - _Requirements: 7.3, 7.4_
  
  - [ ] 12.2 Adicionar botão "Exportar Meus Dados" no perfil
    - Permitir escolher formato (JSON/PDF)
    - Download automático do arquivo
    - _Requirements: 7.3_

- [ ] 13. Criar scripts de migração para usuários existentes
  - Script para marcar usuários existentes como "requer consentimento"
  - Script para popular política inicial
  - Script de rollback
  - _Requirements: 6.4, 6.5_

- [ ] 14. Implementar relatórios de auditoria
  - [ ] 14.1 Criar RelatorioAuditoriaService.java
    - Gerar estatísticas de consentimentos
    - Calcular taxa de aceitação/revogação
    - Listar usuários sem consentimento
    - _Requirements: 9.1_
  
  - [ ] 14.2 Criar RelatorioAuditoriaViewer.java
    - Exibir gráficos de estatísticas
    - Permitir filtrar por período
    - Exportar em PDF com assinatura digital
    - _Requirements: 9.1, 9.2, 9.3_

- [ ] 15. Implementar cache de política de privacidade
  - Cache em memória com expiração de 1 hora
  - Invalidação automática ao atualizar política
  - Fallback para banco em caso de falha
  - _Requirements: 1.2_

- [ ] 16. Adicionar logs de auditoria para todas as operações LGPD
  - Log de concessão de consentimento
  - Log de revogação de consentimento
  - Log de visualização de política
  - Log de exportação de dados
  - Log de atualização de política
  - _Requirements: 3.1, 5.1, 8.3, 9.3_

- [ ] 17. Criar documentação técnica
  - Documentar APIs dos services
  - Documentar estrutura do banco
  - Criar guia de uso para administradores
  - Criar FAQ para usuários
  - _Requirements: Todos_

- [ ] 18. Implementar testes unitários
  - [ ]* 18.1 Testes para ConsentimentoDAO
    - Testar inserção, busca, revogação
    - Testar validação de consentimento
    - _Requirements: 2.5, 4.5_
  
  - [ ]* 18.2 Testes para ConsentimentoService
    - Testar lógica de validação
    - Testar notificação ao DPO
    - Testar registro com múltiplos tipos
    - _Requirements: 2.1, 5.2_
  
  - [ ]* 18.3 Testes para PoliticaPrivacidadeDAO
    - Testar busca de política ativa
    - Testar versionamento
    - Testar histórico
    - _Requirements: 1.2, 6.2, 6.6_

- [ ] 19. Implementar testes de integração
  - [ ]* 19.1 Testar fluxo completo de consentimento
    - Login → Verificação → Dialog → Registro → Acesso
    - _Requirements: 2.1, 2.5, 2.6_
  
  - [ ]* 19.2 Testar fluxo de revogação
    - Revogar → Notificar → Logout
    - _Requirements: 4.4, 4.5, 5.2_
  
  - [ ]* 19.3 Testar atualização de política
    - Atualizar → Invalidar → Solicitar novo consentimento
    - _Requirements: 6.2, 6.4, 6.5_

- [ ] 20. Realizar testes de UI e usabilidade
  - [ ]* 20.1 Testar ConsentimentoDialog
    - Verificar exibição correta
    - Testar validação de checkboxes
    - Testar navegação
    - _Requirements: 2.1, 2.3, 2.4_
  
  - [ ]* 20.2 Testar PoliticaPrivacidadeViewer
    - Verificar carregamento de conteúdo
    - Testar scroll
    - Testar exportação PDF
    - _Requirements: 1.2, 1.4, 7.4_
  
  - [ ]* 20.3 Testar GestaoConsentimentosFrame
    - Verificar listagem
    - Testar filtros
    - Testar exportação de relatório
    - _Requirements: 3.1, 3.3, 3.4_

- [ ] 21. Preparar ambiente de produção
  - Executar scripts de criação de tabelas
  - Inserir política de privacidade inicial
  - Configurar SMTP para emails
  - Configurar DPO no sistema
  - _Requirements: Todos_

- [ ] 22. Realizar deploy e migração
  - Deploy do backend (DAOs, Services)
  - Deploy do frontend (Views)
  - Executar script de migração de usuários
  - Monitorar logs durante primeiras 24h
  - _Requirements: Todos_

- [ ] 23. Criar material de treinamento
  - Vídeo tutorial para usuários
  - Manual para administradores
  - FAQ sobre LGPD no sistema
  - Slides de apresentação
  - _Requirements: Todos_

- [ ] 24. Realizar auditoria final de conformidade
  - Verificar todos os requisitos da LGPD
  - Testar todos os fluxos
  - Validar documentação
  - Gerar relatório de conformidade
  - _Requirements: Todos_
