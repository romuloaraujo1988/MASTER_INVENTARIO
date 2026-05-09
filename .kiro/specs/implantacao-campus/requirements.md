# Documento de Requisitos

## Introdução

Esta feature define a estratégia de implantação do SIHCP (Sistema de Histórico e Coleta Patrimonial) para novos campus do IFMT (Instituto Federal de Mato Grosso) que desejam aderir ao sistema. O objetivo é tornar o processo de implantação simples, padronizado e acessível a equipes com diferentes níveis de experiência técnica, oferecendo múltiplas modalidades de instalação para atender diferentes cenários de infraestrutura.

O sistema é composto por três componentes principais: aplicação desktop Java/Swing, API mobile Spring Boot (porta 8080/8081) e app Android Kotlin, todos integrados a um banco de dados PostgreSQL.

## Glossário

- **Campus**: Unidade do IFMT que deseja implantar o SIHCP.
- **Administrador_Campus**: Responsável técnico pela implantação e configuração do SIHCP no campus.
- **Pacote_Implantacao**: Conjunto de artefatos necessários para instalar e configurar o SIHCP em um campus.
- **Script_Configuracao**: Arquivo executável (`.ps1`, `.sh` ou `.bat`) que automatiza etapas da implantação.
- **Instalador**: Componente do Pacote_Implantacao responsável por guiar o Administrador_Campus durante a instalação.
- **Perfil_Implantacao**: Conjunto de configurações específicas de um campus (nome, banco de dados, porta, credenciais iniciais).
- **Modo_Manual**: Modalidade de implantação com instalação direta dos componentes no sistema operacional.
- **Modo_Automatizado**: Modalidade de implantação via script único que executa todas as etapas automaticamente.
- **Verificador_Saude**: Componente que valida se todos os serviços do SIHCP estão operacionais após a implantação.
- **Banco_Dados**: Instância PostgreSQL 12+ utilizada pelo SIHCP.
- **API_Mobile**: Servidor Spring Boot que expõe os endpoints REST para o app Android.
- **App_Desktop**: Aplicação Java/Swing para gestão patrimonial.
- **APK**: Arquivo de instalação do app Android para distribuição aos coletores de campo.

---

## Requisitos

### Requisito 1: Pacote de Implantação Unificado

**User Story:** Como Administrador_Campus, quero receber um único pacote contendo tudo que preciso para implantar o SIHCP, para que eu não precise buscar artefatos em múltiplos lugares.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter o JAR executável da API_Mobile, o JAR executável do App_Desktop, o APK do app Android, os scripts SQL de criação do Banco_Dados e o Instalador.
2. THE Pacote_Implantacao SHALL conter um arquivo `README.md` com instruções de implantação em português.
3. THE Pacote_Implantacao SHALL conter um arquivo `CHECKLIST_IMPLANTACAO.md` com todas as etapas de verificação pós-instalação.
4. WHEN o Administrador_Campus descompactar o Pacote_Implantacao, THE Instalador SHALL estar pronto para execução sem etapas adicionais de configuração prévia.
5. THE Pacote_Implantacao SHALL ter tamanho máximo de 200 MB para viabilizar distribuição por e-mail ou link de download.

---

### Requisito 2: Modalidade de Implantação Manual

**User Story:** Como Administrador_Campus sem Docker disponível, quero instalar o SIHCP diretamente no servidor, para que eu possa implantar o sistema mesmo em ambientes restritos.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter um guia passo a passo `INSTALACAO_MANUAL.md` cobrindo instalação do JDK 21, PostgreSQL 12+, criação do banco de dados e execução dos JARs.
2. THE Pacote_Implantacao SHALL conter scripts de inicialização para Windows (`iniciar-servidor.bat`) e Linux/macOS (`iniciar-servidor.sh`) que executem a API_Mobile com as configurações do Perfil_Implantacao.
3. WHEN o Administrador_Campus executar o script de inicialização, THE Script_Configuracao SHALL verificar se o JDK 21 está instalado e exibir mensagem de erro descritiva caso não esteja.
4. WHEN o Administrador_Campus executar o script de inicialização, THE Script_Configuracao SHALL verificar se o Banco_Dados está acessível antes de iniciar a API_Mobile.
5. THE Pacote_Implantacao SHALL conter todos os scripts SQL necessários para criar o esquema completo do Banco_Dados em um único arquivo `setup_banco_completo.sql`.
6. IF o Banco_Dados não estiver acessível no momento da inicialização, THEN THE Script_Configuracao SHALL exibir a mensagem "Banco de dados inacessível. Verifique as configurações em configuracao_banco.json" e encerrar com código de saída 1.

---

### Requisito 4: Modalidade de Implantação Automatizada

**User Story:** Como Administrador_Campus com pouca experiência técnica, quero executar um único script que configure tudo automaticamente, para que eu possa implantar o SIHCP sem conhecimento aprofundado de servidores.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter um script `setup.ps1` (Windows) e `setup.sh` (Linux/macOS) que execute todas as etapas de implantação de forma interativa.
2. WHEN o Administrador_Campus executar o Script_Configuracao de implantação automatizada, THE Script_Configuracao SHALL solicitar interativamente: nome do campus, senha do administrador do sistema, porta da API e credenciais do banco de dados.
3. WHEN o Administrador_Campus fornecer todas as informações solicitadas, THE Script_Configuracao SHALL gerar automaticamente o arquivo `configuracao_banco.json` e o `application.properties` com os valores informados.
4. THE Script_Configuracao SHALL executar os scripts SQL de criação do Banco_Dados automaticamente após configurar as credenciais.
5. WHEN a implantação automatizada for concluída com sucesso, THE Script_Configuracao SHALL exibir um resumo com: URL de acesso à API, credenciais do administrador inicial e caminho do APK para distribuição.
6. IF qualquer etapa da implantação automatizada falhar, THEN THE Script_Configuracao SHALL exibir a etapa que falhou, a causa do erro e as instruções de correção, sem apagar configurações já realizadas com sucesso.
7. THE Script_Configuracao SHALL criar um usuário administrador inicial no Banco_Dados com login `admin` e senha definida pelo Administrador_Campus durante a configuração.

---

### Requisito 5: Verificação de Saúde Pós-Implantação

**User Story:** Como Administrador_Campus, quero verificar se o sistema foi implantado corretamente, para que eu tenha confiança de que o SIHCP está operacional antes de distribuir o APK aos usuários.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter um script `verificar-saude.ps1` (Windows) e `verificar-saude.sh` (Linux/macOS) que valide todos os componentes do sistema.
2. WHEN o Administrador_Campus executar o Verificador_Saude, THE Verificador_Saude SHALL testar: conectividade com o Banco_Dados, resposta do endpoint `GET /api/mobile/health`, autenticação com credenciais do administrador inicial e listagem de patrimônios via `GET /api/mobile/patrimonio`.
3. WHEN todos os testes do Verificador_Saude passarem, THE Verificador_Saude SHALL exibir "✅ Sistema SIHCP operacional e pronto para uso" e o endereço de acesso.
4. IF qualquer teste do Verificador_Saude falhar, THEN THE Verificador_Saude SHALL exibir qual componente falhou, o erro obtido e o link para a seção correspondente do guia de solução de problemas.
5. THE Verificador_Saude SHALL gerar um arquivo `relatorio-saude-{data}.txt` com o resultado de cada teste para fins de registro e suporte.

---

### Requisito 6: Configuração do Perfil do Campus

**User Story:** Como Administrador_Campus, quero personalizar o sistema com as informações do meu campus, para que o SIHCP reflita a identidade e estrutura organizacional da minha unidade.

#### Critérios de Aceitação

1. THE Instalador SHALL permitir que o Administrador_Campus configure: nome do campus, sigla, cidade, endereço e contato do responsável técnico.
2. WHEN o Administrador_Campus salvar o Perfil_Implantacao, THE Instalador SHALL persistir as configurações no arquivo `configuracao_banco.json` no diretório home do usuário do sistema operacional.
3. THE Instalador SHALL validar que o nome do campus não está vazio e contém no máximo 100 caracteres antes de salvar o Perfil_Implantacao.
4. THE Instalador SHALL validar que a porta informada para a API_Mobile está no intervalo de 1024 a 65535 e não está em uso por outro processo antes de iniciar o servidor.
5. WHEN o Administrador_Campus alterar o Perfil_Implantacao após a implantação inicial, THE Instalador SHALL reiniciar automaticamente a API_Mobile para aplicar as novas configurações.

---

### Requisito 7: Distribuição do APK Android

**User Story:** Como Administrador_Campus, quero distribuir o app Android aos coletores de campo de forma simples, para que eles possam começar a usar o sistema sem depender de lojas de aplicativos.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter o APK pré-compilado do app Android pronto para instalação direta nos dispositivos dos coletores.
2. THE Pacote_Implantacao SHALL conter um guia `CONFIGURAR_APP_ANDROID.md` com instruções para: habilitar instalação de fontes desconhecidas, instalar o APK e configurar o endereço IP do servidor da API_Mobile no app.
3. WHEN o Administrador_Campus executar o Script_Configuracao de implantação automatizada com sucesso, THE Script_Configuracao SHALL exibir o endereço IP e porta que os coletores devem configurar no app Android.
4. THE Pacote_Implantacao SHALL conter um QR Code gerado com o endereço da API_Mobile para facilitar a configuração do app Android pelos coletores.
5. IF o campus operar em rede interna sem acesso externo, THEN THE Pacote_Implantacao SHALL conter instruções para configurar o app Android com endereço IP local da rede do campus.

---

### Requisito 8: Migração e Atualização de Versão

**User Story:** Como Administrador_Campus com o SIHCP já implantado, quero atualizar o sistema para uma nova versão sem perder os dados, para que o campus se beneficie de melhorias sem risco de perda de patrimônios cadastrados.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter um script `atualizar.ps1` (Windows) e `atualizar.sh` (Linux/macOS) para atualização de versões anteriores.
2. WHEN o Administrador_Campus executar o script de atualização, THE Script_Configuracao SHALL criar automaticamente um backup do Banco_Dados antes de aplicar qualquer alteração.
3. THE Script_Configuracao de atualização SHALL aplicar apenas os scripts SQL de migração necessários para a versão alvo, sem recriar tabelas existentes.
4. IF o script de atualização falhar após o backup, THEN THE Script_Configuracao SHALL restaurar automaticamente o backup criado e exibir o erro ocorrido.
5. WHEN a atualização for concluída com sucesso, THE Script_Configuracao SHALL exibir a versão anterior, a versão atual e o número de scripts de migração aplicados.

---

### Requisito 9: Documentação de Solução de Problemas

**User Story:** Como Administrador_Campus, quero ter acesso a um guia de solução de problemas comuns, para que eu possa resolver erros de implantação sem precisar acionar suporte especializado.

#### Critérios de Aceitação

1. THE Pacote_Implantacao SHALL conter um arquivo `SOLUCAO_PROBLEMAS.md` cobrindo os erros mais comuns: falha de conexão com banco de dados, porta em uso, JDK não encontrado e APK não instalando.
2. THE Pacote_Implantacao SHALL conter um script `coletar-diagnostico.ps1` (Windows) e `coletar-diagnostico.sh` (Linux/macOS) que colete logs, versões de software e configurações para envio ao suporte.
3. WHEN o Administrador_Campus executar o script de diagnóstico, THE Script_Configuracao SHALL gerar um arquivo `diagnostico-{campus}-{data}.zip` contendo logs da API_Mobile, versão do JDK, versão do PostgreSQL e configurações anonimizadas (sem senhas).
4. THE Pacote_Implantacao SHALL conter instruções de contato com a equipe de suporte do SIHCP, incluindo e-mail e repositório do projeto.
