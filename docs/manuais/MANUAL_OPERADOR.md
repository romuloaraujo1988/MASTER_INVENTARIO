# Manual do Operador — SIHCP Desktop

**Aplicativo Desktop Swing para gestão patrimonial e operação de inventário**

Versão: 2.7.0 (maio/2026)
Stack: Java 21 + Swing + Spring Boot + PostgreSQL
Instituição: Instituto Federal de Mato Grosso

---

## Sumário

1. [O que é este aplicativo](#1-o-que-é-este-aplicativo)
2. [Requisitos e instalação](#2-requisitos-e-instalação)
3. [Configuração do banco de dados](#3-configuração-do-banco-de-dados)
4. [Primeiro acesso](#4-primeiro-acesso)
5. [Visão geral da tela principal](#5-visão-geral-da-tela-principal)
6. [Gestão de patrimônios](#6-gestão-de-patrimônios)
7. [Gestão de salas, setores e responsáveis](#7-gestão-de-salas-setores-e-responsáveis)
8. [Gestão de usuários e perfis](#8-gestão-de-usuários-e-perfis)
9. [Inventários e coletas](#9-inventários-e-coletas)
10. [Dashboard e análises](#10-dashboard-e-análises)
11. [Relatórios](#11-relatórios)
12. [Geração de QR Codes e plaquetas](#12-geração-de-qr-codes-e-plaquetas)
13. [Importação do SUAP](#13-importação-do-suap)
14. [Modo offline do desktop](#14-modo-offline-do-desktop)
15. [Problemas comuns](#15-problemas-comuns)
16. [Atalhos e produtividade](#16-atalhos-e-produtividade)
17. [Glossário](#17-glossário)

---

## 1. O que é este aplicativo

O SIHCP Desktop é a ferramenta de administração e operação cotidiana do sistema patrimonial. É onde você cadastra patrimônios, gere o inventário anual, consulta coletas recebidas do app mobile, emite relatórios e imprime plaquetas com QR Code.

### Seu papel como operador

A depender do seu perfil no sistema (ver [seção 8](#8-gestão-de-usuários-e-perfis)), você pode:

- **Cadastrar e manter** patrimônios, salas, setores, responsáveis
- **Configurar inventários** anuais e definir os coletores participantes
- **Consultar coletas** que vieram do app mobile em tempo real
- **Gerar relatórios** em PDF, Excel e visualizações gráficas
- **Emitir plaquetas** com QR Code para identificação de patrimônios
- **Importar dados** do SUAP (sistema institucional federal)
- **Administrar usuários** (se for ADMIN)

### Diferença entre desktop e mobile

| Aspecto | Desktop (este manual) | Mobile (app Android) |
|---|---|---|
| Foco | Gestão e análise | Coleta de campo |
| Conexão | Rede local com PostgreSQL | Internet com servidor REST |
| Acesso | Operadores em escritório | Coletores em campo |
| Cria patrimônios? | Sim | Não |
| Registra coletas? | Não (visualiza) | Sim |
| Finaliza inventário? | Sim | Não |

---

## 2. Requisitos e instalação

### Requisitos mínimos

- Windows 10 / 11 ou Linux (Ubuntu 20.04+)
- Java 21 (JRE ou JDK) instalado
- 4 GB RAM (8 GB recomendado)
- 2 GB de disco livre
- Resolução 1366x768 ou superior
- Rede local com acesso ao servidor PostgreSQL

### Instalação

A TI do IFMT fornece um pacote com:

```
SIHCP_DESKTOP_v2.7.0/
├── sihcp-desktop.jar              (ou lib/ com JARs)
├── config/
│   ├── configuracao_banco.json   (conexão ao banco)
│   ├── log4j2.xml
│   └── ...
├── executar.bat                   (Windows)
└── executar.sh                    (Linux)
```

1. Descompacte em `C:\SIHCP\` (Windows) ou `/opt/sihcp/` (Linux).
2. Confirme que o Java 21 está no PATH: `java -version` deve mostrar `21.x`.
3. Execute `executar.bat` (duplo clique) ou `./executar.sh`.

Na primeira execução, o aplicativo tentará conectar ao banco com as configurações em `config/configuracao_banco.json`.

---

## 3. Configuração do banco de dados

O arquivo `config/configuracao_banco.json` define como o desktop se conecta ao PostgreSQL:

```json
{
  "host": "localhost",
  "porta": 5432,
  "nome_banco": "sispatrimonio",
  "usuario": "inventario",
  "senha": "senha_do_postgres"
}
```

> **Segurança**: este arquivo contém a senha do banco. Mantenha-o fora de pastas compartilhadas e garanta permissões de leitura apenas ao usuário que executa o app.

Se precisar mudar a configuração após a primeira execução, o menu **Arquivo → Configurar Banco** abre a interface de edição.

---

## 4. Primeiro acesso

### Tela de login

Ao abrir o aplicativo:

1. Informe seu **login** (mesmo do SUAP se integrado, ou o que a TI atribuiu)
2. Informe sua **senha**
3. Clique em **Entrar**

O sistema valida as credenciais contra `tabela_usuario` no PostgreSQL, que usa hash BCrypt — sua senha nunca é armazenada em texto plano.

### Se é o primeiríssimo acesso do sistema

Não há usuários ainda. A TI cria o primeiro administrador via script SQL (ver [MANUAL_ADMINISTRADOR.md](MANUAL_ADMINISTRADOR.md) seção 9). Depois de entrar pela primeira vez como admin, você pode criar outros usuários pela interface.

### Recuperação de senha

Se esqueceu a senha, peça à TI para resetar. Não há recuperação por e-mail neste desktop.

---

## 5. Visão geral da tela principal

Após o login aparece a janela principal com:

### Barra de menu

- **Arquivo** — Configurar banco, importar SUAP, exportar dados, sair
- **Cadastros** — Patrimônios, Salas, Setores, Responsáveis, Usuários
- **Inventário** — Abrir inventário, acompanhar coletas, finalizar
- **Dashboard** — KPIs, gráficos, análises
- **Relatórios** — Vários formatos e escopos
- **Utilitários** — Geração de QR Codes, impressão de plaquetas, backup
- **Ajuda** — Sobre, manual, versão

### Painel central (Dashboard inicial)

Mostra os principais indicadores:

- Total de patrimônios cadastrados
- Inventário ativo (se houver)
- Coletas registradas hoje / na semana
- Percentual de conclusão do inventário corrente
- Usuários ativos

### Barra de status (inferior)

- Usuário logado e perfil
- Status de conexão ao banco
- Versão do aplicativo
- Modo online/offline

---

## 6. Gestão de patrimônios

**Menu → Cadastros → Patrimônios**

Aqui você cadastra, consulta e altera os bens patrimoniais.

### Listagem

A tabela mostra: número, descrição, sala atual, responsável, estado, valor, data de aquisição. Colunas são ordenáveis e filtráveis.

**Filtros rápidos na parte superior:**
- Busca por número ou descrição
- Filtro por sala
- Filtro por setor
- Filtro por responsável
- Filtro por estado
- Marcador "Só ativos"

### Novo patrimônio

1. Clique em **Novo** (botão ou `Ctrl+N`)
2. Preencha:
   - **Número do patrimônio** (obrigatório, único)
   - **Descrição** (obrigatória)
   - **Marca / Modelo** (opcional)
   - **Valor de aquisição** (opcional)
   - **Data de aquisição** (opcional)
   - **Sala atual** (obrigatória — selecione da lista)
   - **Setor** (geralmente preenchido pela sala)
   - **Responsável** (obrigatório — selecione da lista)
   - **Estado de conservação** (Ótimo, Bom, Regular, Ruim, Inservível)
   - **Observações** (opcional)
3. Clique em **Salvar** (`Ctrl+S`)

### Alteração

Selecione o patrimônio na lista e clique em **Editar** (ou duplo clique). Faça mudanças e salve.

> **Importante**: alterar sala ou responsável gera registro em `tabela_historico`. Não é apagada informação, é versionada.

### Exclusão (baixa)

Patrimônios não são fisicamente apagados. Use **Editar → Estado → Baixado** para tirá-los das listas ativas. Apenas perfil ADMIN pode realmente remover registros via SQL (em casos excepcionais).

### Importação em lote

**Menu → Arquivo → Importar Patrimônios** aceita:
- CSV com colunas: numero, descricao, sala_id, responsavel_id, estado, valor
- XLSX com o mesmo esquema
- Planilha gerada pelo SUAP (ver [seção 13](#13-importação-do-suap))

---

## 7. Gestão de salas, setores e responsáveis

### Salas

**Menu → Cadastros → Salas**

Cada sala representa um espaço físico: "Sala 101", "Laboratório de Química", etc.

Campos:
- **Código** (obrigatório, ex.: "SL101")
- **Nome** (obrigatório)
- **Setor** (obrigatório — a sala pertence a um setor)
- **Descrição** (opcional)

### Setores

**Menu → Cadastros → Setores**

Um setor agrupa salas. Exemplos: "Coordenação de Tecnologia da Informação", "Biblioteca Central".

Campos:
- **Nome** (obrigatório)
- **Sigla** (opcional)
- **Responsável** (obrigatório — pessoa responsável pelo setor)

### Responsáveis

**Menu → Cadastros → Responsáveis**

Responsáveis são pessoas físicas (servidores, professores) a quem patrimônios ou setores são atribuídos.

Campos:
- **Nome completo** (obrigatório)
- **SIAPE / Matrícula** (único)
- **E-mail**
- **Cargo / Função**
- **Setor principal**

---

## 8. Gestão de usuários e perfis

Apenas perfil **ADMIN** vê este menu.

**Menu → Cadastros → Usuários**

### Perfis disponíveis

| Perfil | O que faz |
|---|---|
| **ADMIN** | Acesso total: cria usuários, exclui coletas, altera configurações |
| **SUPERVISOR** | Supervisiona coletas de todos os coletores, acessa relatórios gerenciais |
| **COLETOR** | Usa o app mobile para registrar coletas; no desktop só visualiza as suas |
| **CONSULTA** | Apenas visualização — não altera nada |

### Criar novo usuário

1. **Usuários → Novo** (`Ctrl+N`)
2. Preencha:
   - **Login** (obrigatório, único — sugestão: `nome.sobrenome`)
   - **Nome completo** (obrigatório)
   - **E-mail institucional** (obrigatório)
   - **Senha inicial** (obrigatória — o usuário troca no primeiro acesso)
   - **Perfil** (obrigatório)
   - **Ativo** (marcado por padrão)
3. Salvar (`Ctrl+S`)

A senha é imediatamente convertida para hash BCrypt antes de ir para o banco.

### Trocar senha

Você pode trocar sua própria senha em **Arquivo → Minha Conta → Trocar Senha**.

Administrador pode **resetar** a senha de outro usuário em **Usuários → Editar → Resetar Senha**. Nunca verá a senha original — será gerada uma nova e comunicada ao usuário por canal seguro.

### Desativar usuário

Edite o usuário e desmarque **Ativo**. Ele não conseguirá mais logar, mas o histórico de ações permanece.

---

## 9. Inventários e coletas

O inventário é a campanha anual (ou periódica) de verificação patrimonial. Durante um inventário, coletores no campo usam o app mobile para registrar quais patrimônios foram encontrados em cada sala.

### Abrir um novo inventário

**Menu → Inventário → Novo Inventário**

1. **Nome** — "Inventário 2026" por exemplo
2. **Data de início**
3. **Data prevista de fim**
4. **Status** — começa como `EM_ANDAMENTO`
5. **Participantes** — adicione os coletores autorizados e seus setores/salas de responsabilidade

Um inventário ativo é o que o app mobile automaticamente detecta ao logar. Só pode haver **um** inventário `EM_ANDAMENTO` por vez.

### Acompanhamento em tempo real

**Menu → Inventário → Acompanhamento**

Tela dividida em três áreas:

- **Esquerda** — árvore de setores/salas com status (verde = 100% coletado, amarelo = parcial, vermelho = não iniciado)
- **Centro** — lista de coletas recebidas nas últimas horas, com coletor, patrimônio, sala, estado
- **Direita** — progresso geral com gráficos: coletas por dia, coletas por coletor, itens não encontrados

A tela atualiza automaticamente a cada 30 segundos. Clique em **Atualizar** (`F5`) para forçar.

### Consulta de coletas

**Menu → Inventário → Coletas Registradas**

Tabela com todas as coletas do inventário ativo. Filtros:
- Por coletor
- Por sala
- Por data
- Por estado
- Por status de sincronização (algumas podem estar com erro)

Ações por coleta:
- **Ver detalhes** — modal com todas as informações e histórico
- **Aprovar / Rejeitar** (se perfil SUPERVISOR ou ADMIN) — exigido em alguns fluxos
- **Excluir** (apenas ADMIN) — casos excepcionais de coleta duplicada indesejada

### Finalizar inventário

Quando todas as salas estão coletadas (ou você decidiu encerrar mesmo com itens faltantes):

1. **Inventário → Finalizar**
2. Revise o resumo: total coletado, total não encontrado, divergências
3. Adicione observações gerais
4. Confirme

O status muda para `CONCLUIDO` e o inventário fica fechado para novas coletas. O app mobile detecta no próximo login e pede para abrir o novo inventário ativo (quando você criar).

### Divergências

Uma **divergência** ocorre quando:
- Patrimônio encontrado em sala diferente da cadastrada → divergência de localização
- Estado muito pior que o anterior → divergência de estado
- Coleta duplicada → divergência operacional
- Patrimônio no cadastro mas não encontrado na campanha → item não localizado

A tela **Inventário → Divergências** lista todas e permite:
- Atualizar cadastro (transfere patrimônio para sala onde foi encontrado)
- Marcar como investigar
- Registrar observação

---

## 10. Dashboard e análises

**Menu → Dashboard**

Métricas em tempo real:

### Indicadores numéricos

- Total de patrimônios (ativos / baixados)
- Patrimônios por setor (gráfico de barras)
- Valor total do patrimônio ativo
- Coletas no inventário corrente
- Percentual de conclusão do inventário

### Gráficos

- **Coletas por dia** — últimos 30 dias
- **Coletas por coletor** — ranking
- **Distribuição por estado de conservação** — pizza
- **Itens mais antigos ainda ativos** — top 10

### Exportação

Cada gráfico tem botão **Exportar** → PNG para relatórios.

---

## 11. Relatórios

**Menu → Relatórios**

### Relatórios disponíveis

| Relatório | Conteúdo |
|---|---|
| **Patrimônio por sala** | Todos os patrimônios agrupados por sala |
| **Patrimônio por responsável** | Agrupado por responsável |
| **Patrimônio por setor** | Agrupado por setor |
| **Coletas do inventário** | Todas as coletas do inventário ativo |
| **Divergências do inventário** | Só as divergências |
| **Não localizados** | Patrimônios do cadastro não encontrados |
| **Termo de responsabilidade** | Formulário de assinatura por responsável |
| **Auditoria** | Histórico de alterações em tabela_historico |

### Como gerar

1. Selecione o relatório
2. Defina os filtros (período, setor, responsável, etc.)
3. Escolha o formato: **PDF**, **Excel** ou **Visualização na tela**
4. Clique em **Gerar**

Relatórios pesados (milhares de linhas) podem demorar alguns segundos — uma barra de progresso aparece.

### Relatório personalizado

**Relatórios → Personalizado** abre um construtor que permite:
- Escolher colunas
- Adicionar filtros complexos (AND, OR, NOT)
- Salvar como template para reutilizar

---

## 12. Geração de QR Codes e plaquetas

**Menu → Utilitários → Gerar QR Codes**

Para cada patrimônio, o sistema gera um QR Code contendo o número. O coletor escaneia no app mobile para identificação automática.

### Modo individual

- Selecione um patrimônio
- Clique em **Gerar QR** — mostra o código na tela
- **Imprimir** envia diretamente à impressora
- **Salvar como PNG** exporta para colar em documentos

### Modo em lote

- Selecione múltiplos patrimônios (Ctrl+clique ou filtro por sala/setor)
- **Gerar QRs em Lote** produz um PDF com layout de plaquetas (grade de 8 ou 12 por página, configurável)
- Imprima em papel adesivo resistente e cole nos patrimônios

### Configuração do layout

**Utilitários → Configurar Plaquetas** permite definir:
- Tamanho da plaqueta (mm)
- Número de colunas/linhas por página
- Margens
- Texto adicional (número + descrição curta)

---

## 13. Importação do SUAP

O SUAP (Sistema Unificado de Administração Pública) é a fonte oficial de dados patrimoniais da instituição. Periodicamente, o setor responsável exporta uma planilha do SUAP e importa no SIHCP para sincronizar bens novos, transferências e baixas.

**Menu → Arquivo → Importar SUAP**

### Formato esperado

Planilha XLSX com as colunas exportadas pelo SUAP (o sistema reconhece o formato padrão automaticamente):

- Número do patrimônio
- Descrição
- Valor
- Data de aquisição
- Localização (código)
- Responsável (SIAPE)
- Estado
- Situação (ativo/baixado)

### Processo

1. Abra a tela de importação
2. Selecione o arquivo (`Arquivo → Importar SUAP → Escolher arquivo`)
3. O sistema mostra um **preview** das primeiras 50 linhas
4. Configure o mapeamento de colunas se necessário (normalmente auto-detectado)
5. Clique em **Analisar Diferenças** — sistema compara com o cadastro atual e mostra:
   - Novos patrimônios (serão inseridos)
   - Patrimônios alterados (sala, responsável ou estado mudou)
   - Patrimônios baixados no SUAP (serão marcados como baixados aqui)
   - Patrimônios que existem aqui mas não no SUAP (decisão manual)
6. Revise a lista e clique em **Aplicar**

A importação é **transacional** — se algo falhar, nada é alterado.

Registro completo em `tabela_historico` de todas as mudanças aplicadas.

### Recomendação

Faça backup do banco **antes** de qualquer importação SUAP grande. Ver [MANUAL_ADMINISTRADOR.md](MANUAL_ADMINISTRADOR.md) seção 12.

---

## 14. Modo offline do desktop

Se o servidor PostgreSQL ficar indisponível (rede caiu, servidor em manutenção), o desktop entra em **modo offline degradado**:

- Você consegue visualizar o último estado cacheado (`data/inventario.db` SQLite local)
- Não é possível salvar novas informações
- Relatórios permanecem disponíveis (com dados do cache)
- Ao voltar a conexão, o sistema ressincroniza automaticamente

O modo offline do desktop é limitado e serve para **consulta em emergência**, não para operação prolongada. Se a rede está fora, o ideal é usar o app mobile (que tem modo offline completo e sincroniza depois).

---

## 15. Problemas comuns

### Não consigo logar

- Confira login e senha (sem espaços)
- Confirme com a TI se sua conta está ativa
- Se houver bloqueio por tentativas, aguarde alguns minutos
- Teste a conexão ao banco: **Arquivo → Testar Conexão**

### "Erro ao conectar ao banco"

- O servidor PostgreSQL está rodando?
- Sua rede alcança o servidor? (`ping servidor`)
- As credenciais em `config/configuracao_banco.json` estão corretas?
- Peça à TI para verificar logs do PostgreSQL

### Aplicativo está lento

- Reinicie para limpar memória
- Verifique RAM do computador (≥ 4 GB ativos)
- Feche relatórios pesados que ficaram abertos
- Em telas com milhares de linhas, use filtros para reduzir o volume

### Relatório PDF não abre

- Verifique se há um leitor PDF instalado (Adobe Reader, SumatraPDF)
- Tente **Exportar** em vez de **Abrir**
- Arquivos grandes podem demorar para renderizar

### QR Codes ilegíveis ao imprimir

- Aumente o tamanho da plaqueta em **Utilitários → Configurar Plaquetas**
- Use impressora laser, não jato de tinta
- Use papel adesivo de boa qualidade
- Resolução de impressão: 600 dpi mínimo

### Dados desatualizados

- Clique em **Atualizar** (F5) na tela relevante
- Alguns dados são cacheados por 5 minutos para performance
- Em última instância, reinicie o aplicativo

---

## 16. Atalhos e produtividade

### Atalhos globais

| Tecla | Ação |
|---|---|
| `Ctrl+N` | Novo registro (na tela atual) |
| `Ctrl+S` | Salvar |
| `Ctrl+F` | Buscar |
| `F5` | Atualizar lista |
| `Ctrl+P` | Imprimir |
| `Ctrl+E` | Exportar para Excel |
| `Esc` | Cancelar edição / fechar modal |
| `F1` | Ajuda da tela atual |
| `Ctrl+L` | Logout |
| `Ctrl+Q` | Sair |

### Dicas de produtividade

- **Filtros combinados**: combine busca + sala + setor para reduzir listagens grandes
- **Colunas ordenáveis**: clique no cabeçalho para ordenar (duplo clique = ordem inversa)
- **Seleção múltipla**: Ctrl+clique ou Shift+clique em listas permite ações em lote
- **Duplicar cadastro**: ao cadastrar vários patrimônios similares, use **Editar → Duplicar** e ajuste só o que mudou
- **Templates de relatório**: salve configurações de relatórios usados com frequência como templates
- **Gerar QR em lote**: sempre que cadastrar novos patrimônios, emita QR Codes em lote no final do expediente

---

## 17. Glossário

- **Baixa patrimonial** — processo de retirar um patrimônio do cadastro ativo (descarte, extravio, transferência externa). No SIHCP, muda o estado para "Baixado", não apaga o registro.
- **BCrypt** — algoritmo de hash usado para armazenar senhas com salt, resistente a ataques de força bruta.
- **Coleta** — registro individual de que um patrimônio foi visto em uma sala durante o inventário.
- **Divergência** — diferença entre o que o cadastro diz (localização, estado, responsável) e o que foi encontrado no inventário.
- **Hash** — representação unidirecional de uma informação (como senha) que não pode ser revertida.
- **Inventário** — campanha de verificação patrimonial (tipicamente anual).
- **JWT** — formato de token usado pelo app mobile para autenticação. Não se aplica ao desktop (que usa sessão local).
- **Patrimônio** — bem duradouro cadastrado no sistema com número único.
- **Responsável** — pessoa física atribuída como guardiã de um patrimônio ou setor.
- **Setor** — agrupamento administrativo de salas (coordenação, departamento, etc.).
- **SUAP** — Sistema Unificado de Administração Pública, fonte institucional dos dados patrimoniais.
- **tabela_historico** — tabela que registra toda alteração feita em patrimônios, para auditoria.
- **Termo de responsabilidade** — documento assinado pelo responsável atestando que tem sob guarda os patrimônios listados.

---

## Referências

- Padrões de projeto do desktop: [docs/PADROES_DESKTOP.md](../PADROES_DESKTOP.md)
- Configuração de banco: [MANUAL_ADMINISTRADOR.md](MANUAL_ADMINISTRADOR.md) seção 6
- Gestão de usuários (SQL direto): [MANUAL_ADMINISTRADOR.md](MANUAL_ADMINISTRADOR.md) seção 9
- Para uso do app mobile: [MANUAL_COLETOR.md](MANUAL_COLETOR.md)

---

**Suporte**

Problemas técnicos: equipe de TI do campus.
Dúvidas sobre fluxo patrimonial: coordenação de patrimônio institucional.
