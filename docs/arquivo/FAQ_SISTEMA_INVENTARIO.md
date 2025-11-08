# 🤔 FAQ - Perguntas Frequentes
## Sistema de Inventário IFMT

**Versão:** 1.0.0  
**Data:** Janeiro 2025  
**Instituto Federal de Mato Grosso**

---

## 📋 Índice

1. [🚀 Primeiros Passos](#-primeiros-passos)
2. [👤 Gestão de Usuários](#-gestão-de-usuários)
3. [📦 Gestão de Patrimônio](#-gestão-de-patrimônio)
4. [📱 Coleta de Inventário](#-coleta-de-inventário)
5. [📊 Relatórios](#-relatórios)
6. [🔗 Integração SUAP](#-integração-suap)
7. [📈 Dashboard](#-dashboard)
8. [🔧 Problemas Técnicos](#-problemas-técnicos)
9. [📱 Aplicativo Mobile](#-aplicativo-mobile)
10. [🔒 Segurança](#-segurança)

---

## 🚀 Primeiros Passos

### ❓ Como faço o primeiro acesso ao sistema?
**R:** Use as credenciais padrão:
- **Usuário:** `admin`
- **Senha:** `admin123`

⚠️ **IMPORTANTE:** Altere a senha imediatamente após o primeiro login!

### ❓ O sistema não está iniciando. O que fazer?
**R:** Verifique:
1. ✅ Java 21 está instalado
2. ✅ PostgreSQL está rodando
3. ✅ Arquivo de configuração está correto
4. ✅ Porta 5432 está disponível

```bash
# Verificar Java
java --version

# Verificar PostgreSQL (Windows)
sc query postgresql
```

### ❓ Como configurar a conexão com o banco de dados?
**R:** Edite o arquivo `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sispatrimonio
    username: inventario
    password: sua_senha_aqui
```

### ❓ Esqueci minha senha. Como recuperar?
**R:** Contate o administrador do sistema ou use o script de reset:
```sql
UPDATE usuario SET senha = MD5('nova_senha') WHERE login = 'seu_usuario';
```

---

## 👤 Gestão de Usuários

### ❓ Como criar um novo usuário?
**R:** 
1. 📋 Acesse **Menu → Usuários → Cadastrar**
2. ✏️ Preencha os campos obrigatórios:
   - Nome completo
   - Login (único)
   - Senha
   - Email
   - Perfil de acesso
3. 💾 Clique em **Salvar**

### ❓ Quais são os perfis de usuário disponíveis?
**R:** 
- **👑 Administrador**: Acesso total ao sistema
- **👨‍💼 Gestor**: Gerencia inventários e relatórios
- **👨‍🔧 Operador**: Realiza coletas e consultas
- **👁️ Consulta**: Apenas visualização

### ❓ Como alterar o perfil de um usuário?
**R:** 
1. 🔍 Localize o usuário na lista
2. ✏️ Clique em **Editar**
3. 🔄 Altere o campo **Perfil**
4. 💾 Salve as alterações

### ❓ Posso desativar um usuário sem excluí-lo?
**R:** Sim! Marque a opção **"Inativo"** no cadastro do usuário. Ele não conseguirá mais fazer login, mas seus dados serão preservados.

---

## 📦 Gestão de Patrimônio

### ❓ Como cadastrar um novo patrimônio?
**R:** 
1. 📋 Acesse **Menu → Patrimônio → Cadastrar**
2. ✏️ Preencha os campos:
   - **Número do Patrimônio** (obrigatório)
   - **Descrição** (obrigatório)
   - **Valor de Aquisição**
   - **Data de Aquisição**
   - **Localização** (Campus/Setor/Sala)
   - **Responsável**
3. 💾 Clique em **Salvar**

### ❓ O que fazer se o número do patrimônio já existe?
**R:** 
- ⚠️ O sistema não permite números duplicados
- 🔍 Verifique se o bem já está cadastrado
- 📞 Se necessário, contate o setor de patrimônio

### ❓ Como alterar a localização de um patrimônio?
**R:** 
1. 🔍 Localize o patrimônio
2. ✏️ Clique em **Editar**
3. 🏢 Altere **Campus**, **Setor** e **Sala**
4. 👤 Atualize o **Responsável** se necessário
5. 💾 Salve as alterações

### ❓ Posso importar patrimônios em lote?
**R:** Sim! Use a funcionalidade de **Importação CSV**:
1. 📁 Prepare arquivo CSV com as colunas corretas
2. 📋 Acesse **Menu → Patrimônio → Importar**
3. 📤 Selecione o arquivo
4. ✅ Valide os dados
5. 💾 Confirme a importação

### ❓ Como gerar QR Code para os patrimônios?
**R:** 
1. 🔍 Selecione os patrimônios desejados
2. 📋 Clique em **Ações → Gerar QR Code**
3. 🖨️ Escolha o formato (PDF, PNG)
4. 📥 Baixe e imprima as etiquetas

---

## 📱 Coleta de Inventário

### ❓ Como criar um novo inventário?
**R:** 
1. 📋 Acesse **Menu → Inventário → Novo**
2. ✏️ Defina:
   - **Nome do Inventário**
   - **Período** (data início/fim)
   - **Campus/Setores** incluídos
   - **Responsáveis**
3. 💾 Salve e **Inicie** o inventário

### ❓ Como fazer a coleta usando QR Code?
**R:** 
1. 📱 Abra o app mobile ou use a câmera web
2. 📷 Escaneie o QR Code do patrimônio
3. ✅ Confirme os dados exibidos
4. 📝 Adicione observações se necessário
5. 💾 Salve a coleta

### ❓ E se o patrimônio não tiver QR Code?
**R:** 
- 🔢 Digite o número do patrimônio manualmente
- 🔍 Use a busca por descrição
- 📋 Marque como "Sem Etiqueta" se necessário

### ❓ Como trabalhar offline?
**R:** 
1. 📥 Sincronize os dados antes de sair
2. 📱 Use o modo offline do app
3. 📝 Realize as coletas normalmente
4. 🔄 Sincronize quando retornar à rede

### ❓ O que fazer com patrimônios não localizados?
**R:** 
- 🔍 Marque como **"Não Localizado"**
- 📝 Adicione observações detalhadas
- 📞 Comunique ao responsável do setor
- 📋 Gere relatório de pendências

---

## 📊 Relatórios

### ❓ Quais relatórios estão disponíveis?
**R:** 
- 📈 **Gerenciais**: Resumo executivo, indicadores
- 📋 **Operacionais**: Coletas, pendências, divergências
- 🏢 **Por Localização**: Campus, setor, sala
- 👤 **Por Responsável**: Bens sob responsabilidade
- 📅 **Históricos**: Movimentações, alterações

### ❓ Como gerar um relatório?
**R:** 
1. 📋 Acesse **Menu → Relatórios**
2. 🎯 Escolha o tipo de relatório
3. 🔧 Configure os filtros:
   - Período
   - Campus/Setor
   - Status
   - Responsável
4. 📊 Clique em **Gerar**
5. 📥 Exporte em PDF, Excel ou CSV

### ❓ Posso agendar relatórios automáticos?
**R:** Sim! Configure relatórios recorrentes:
1. ⚙️ Acesse **Configurações → Relatórios**
2. 📅 Defina frequência (diário, semanal, mensal)
3. 📧 Configure destinatários por email
4. ✅ Ative o agendamento

### ❓ Como interpretar o relatório de divergências?
**R:** 
- 🔴 **Não Localizado**: Bem não foi encontrado
- 🟡 **Localização Divergente**: Bem em local diferente
- 🟢 **Conferido**: Bem localizado corretamente
- ⚪ **Não Coletado**: Ainda não foi verificado

---

## 🔗 Integração SUAP

### ❓ Como configurar a integração com SUAP?
**R:** 
1. ⚙️ Acesse **Menu → Configurações → SUAP**
2. 🔧 Configure:
   - URL do servidor SUAP
   - Token de autenticação
   - Frequência de sincronização
3. 🧪 Teste a conexão
4. ✅ Ative a sincronização

### ❓ Quais dados são sincronizados?
**R:** 
- 👥 **Servidores**: Nome, matrícula, setor
- 🏢 **Estrutura Organizacional**: Campus, setores
- 📦 **Patrimônios**: Novos bens cadastrados
- 📍 **Localizações**: Salas e ambientes

### ❓ Com que frequência ocorre a sincronização?
**R:** 
- 🔄 **Automática**: A cada 6 horas (configurável)
- 🔧 **Manual**: Botão "Sincronizar Agora"
- 🌙 **Noturna**: Sincronização completa às 02:00

### ❓ E se houver conflito de dados?
**R:** 
1. 📋 O sistema gera log de conflitos
2. 👨‍💼 Administrador recebe notificação
3. 🔧 Resolução manual necessária
4. 📊 Relatório de inconsistências disponível

---

## 📈 Dashboard

### ❓ Quais indicadores são exibidos?
**R:** 
- 📊 **Total de Patrimônios**: Por campus/setor
- 📈 **Progresso do Inventário**: % concluído
- 🎯 **Taxa de Localização**: Bens encontrados
- ⏱️ **Tempo Médio**: Por coleta
- 🔍 **Pendências**: Itens não coletados

### ❓ Como personalizar o dashboard?
**R:** 
1. ⚙️ Clique no ícone de configuração
2. 🎛️ Escolha os widgets desejados
3. 📐 Redimensione e reposicione
4. 🎨 Selecione cores e temas
5. 💾 Salve o layout personalizado

### ❓ Posso exportar os gráficos?
**R:** Sim!
- 🖼️ **PNG**: Para apresentações
- 📊 **PDF**: Para relatórios
- 📈 **Excel**: Para análises
- 📋 **CSV**: Para dados brutos

### ❓ Como configurar alertas?
**R:** 
1. 🔔 Acesse **Dashboard → Alertas**
2. 🎯 Defina condições:
   - Meta não atingida
   - Prazo vencendo
   - Divergências críticas
3. 📧 Configure notificações
4. ✅ Ative os alertas

---

## 🔧 Problemas Técnicos

### ❓ O sistema está lento. O que fazer?
**R:** 
1. 🧠 Verifique memória RAM disponível
2. 💾 Libere espaço em disco
3. 🌐 Teste velocidade da internet
4. 🔄 Reinicie a aplicação
5. 📞 Se persistir, contate o suporte

### ❓ Erro "Conexão com banco perdida"?
**R:** 
1. 🔍 Verifique se PostgreSQL está rodando
2. 🌐 Teste conectividade de rede
3. 🔑 Confirme credenciais de acesso
4. 🔄 Reinicie o serviço do banco
5. 📋 Verifique logs de erro

### ❓ QR Code não está sendo reconhecido?
**R:** 
- 🖨️ Verifique qualidade da impressão
- 💡 Melhore iluminação do ambiente
- 🧽 Limpe lente da câmera
- 📱 Atualize o app mobile
- 🔧 Recalibre o leitor

### ❓ Como fazer backup dos dados?
**R:** 
```bash
# Backup automático (recomendado)
pg_dump -h localhost -U inventario sispatrimonio > backup_$(date +%Y%m%d).sql

# Backup manual via interface
Menu → Administração → Backup → Gerar Backup
```

### ❓ Sistema travou durante importação?
**R:** 
1. ⏹️ Não force o fechamento
2. ⏳ Aguarde conclusão do processo
3. 📋 Verifique logs de importação
4. 🔄 Se necessário, reinicie e tente novamente
5. 📞 Contate suporte se persistir

---

## 📱 Aplicativo Mobile

### ❓ Como instalar o app mobile?
**R:** 
- **📱 Android**: Baixe o APK do portal institucional
- **🍎 iOS**: Disponível na App Store
- **🔍 Busque por**: "IFMT Inventário"

### ❓ Como sincronizar dados offline?
**R:** 
1. 📶 Conecte-se à rede Wi-Fi
2. 📱 Abra o aplicativo
3. 🔄 Toque em "Sincronizar"
4. ⏳ Aguarde conclusão
5. ✅ Confirme sincronização

### ❓ App não está conectando ao servidor?
**R:** 
1. 📶 Verifique conexão com internet
2. 🔧 Confirme URL do servidor
3. 🔑 Valide credenciais de login
4. 🛡️ Verifique configurações de firewall
5. 📱 Atualize versão do app

### ❓ Como usar o modo offline?
**R:** 
1. 📥 Sincronize dados antes de sair
2. ✈️ Ative modo offline no app
3. 📝 Realize coletas normalmente
4. 💾 Dados ficam armazenados localmente
5. 🔄 Sincronize ao retornar à rede

---

## 🔒 Segurança

### ❓ Como alterar minha senha?
**R:** 
1. 👤 Clique no seu nome (canto superior)
2. ⚙️ Selecione **"Meu Perfil"**
3. 🔑 Clique em **"Alterar Senha"**
4. ✏️ Digite senha atual e nova senha
5. 💾 Confirme a alteração

### ❓ Quais são os requisitos de senha?
**R:** 
- 📏 **Mínimo**: 8 caracteres
- 🔤 **Obrigatório**: Letras maiúsculas e minúsculas
- 🔢 **Obrigatório**: Pelo menos 1 número
- 🔣 **Recomendado**: Caracteres especiais
- 🚫 **Proibido**: Senhas óbvias (123456, admin, etc.)

### ❓ Como funciona o controle de acesso?
**R:** 
- 🔐 **Autenticação**: Login e senha
- 👥 **Autorização**: Perfis de usuário
- 📋 **Auditoria**: Log de todas as ações
- ⏰ **Sessão**: Timeout automático
- 🔒 **Criptografia**: Dados sensíveis protegidos

### ❓ Posso acessar de qualquer lugar?
**R:** 
- 🏢 **Rede Interna**: Acesso completo
- 🌐 **Internet**: Apenas com VPN institucional
- 📱 **Mobile**: Através do app oficial
- 🔒 **Segurança**: Sempre use conexões seguras

---

## 📞 Suporte e Contato

### 🆘 Preciso de Ajuda Urgente
- **📞 Telefone**: (65) 3616-4100 ramal 4500
- **📧 Email**: suporte.inventario@ifmt.edu.br
- **💬 Chat**: Disponível no sistema
- **⏰ Horário**: Segunda a Sexta, 8h às 17h

### 📋 Informações para Suporte
Antes de entrar em contato, tenha em mãos:
- 🖥️ Sistema operacional e versão
- ☕ Versão do Java instalada
- 🗄️ Versão do PostgreSQL
- 📱 Versão do sistema
- 👤 Usuário logado
- 📅 Data/hora do problema
- 📝 Descrição detalhada do erro

### 🔄 Atualizações do Sistema
- 📢 **Notificações**: Via email e sistema
- 📅 **Frequência**: Mensais (correções) e trimestrais (novas funcionalidades)
- 🧪 **Testes**: Sempre em ambiente de homologação
- 📚 **Documentação**: Atualizada a cada versão

---

## 💡 Dicas e Boas Práticas

### ✅ Recomendações Gerais
- 🔄 **Faça backup** regularmente
- 🔑 **Altere senhas** periodicamente
- 📱 **Mantenha apps** atualizados
- 📊 **Monitore relatórios** de divergências
- 🎓 **Treine usuários** regularmente

### 🚀 Otimização de Performance
- 💾 **Libere espaço** em disco regularmente
- 🧠 **Monitore uso** de memória
- 🌐 **Use conexão** estável
- 📊 **Gere relatórios** fora do horário de pico
- 🔄 **Reinicie sistema** semanalmente

### 📈 Melhores Práticas de Inventário
- 📅 **Planeje bem** os períodos
- 👥 **Treine equipes** antes do início
- 🎯 **Defina metas** claras
- 📊 **Monitore progresso** diariamente
- 📋 **Documente problemas** encontrados

---

**📚 Este FAQ é atualizado regularmente. Para sugestões de novas perguntas ou melhorias, entre em contato com a equipe de desenvolvimento.**

**© 2025 Instituto Federal de Mato Grosso - Todos os direitos reservados**