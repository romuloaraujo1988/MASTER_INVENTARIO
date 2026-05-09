# SIHCP - Sistema de Histórico e Coleta Patrimonial

Bem-vindo ao pacote de implantação do SIHCP para campus do IFMT. Este guia ajuda você a colocar o sistema em funcionamento rapidamente.

---

## Pré-requisitos

Antes de começar, certifique-se de que o servidor possui:

| Requisito | Versão mínima | Download |
|-----------|---------------|----------|
| **JDK** | 21 (LTS) | [adoptium.net](https://adoptium.net) |
| **PostgreSQL** | 12 ou superior | [postgresql.org](https://www.postgresql.org/download/) |

> **Dica:** O JDK 21 deve estar no PATH do sistema. Verifique com `java -version`.

---

## Escolha da Modalidade

O SIHCP oferece duas formas de instalação:

### 🤖 Modalidade Automatizada (Recomendada)

Um único script interativo conduz todo o processo: coleta os dados do campus, cria o banco de dados, gera as configurações e exibe um resumo ao final.

**Ideal para:** administradores com pouca experiência em servidores.

### 🔧 Modalidade Manual

Você instala os pré-requisitos e executa cada etapa individualmente, com controle total sobre o processo.

**Ideal para:** ambientes com restrições de segurança ou administradores experientes.

---

## Modalidade Automatizada

### Windows

Abra o PowerShell como Administrador e execute:

```powershell
scripts\setup.ps1
```

### Linux / macOS

Abra o terminal e execute:

```bash
bash scripts/setup.sh
```

O script irá:
1. Verificar os pré-requisitos (JDK 21 e PostgreSQL)
2. Solicitar os dados do campus (nome, sigla, cidade, porta da API, credenciais do banco)
3. Gerar os arquivos de configuração automaticamente
4. Criar o banco de dados e executar o SQL de setup
5. Criar o usuário administrador inicial
6. Gerar o QR Code da API e exibir o resumo final

---

## Modalidade Manual

Siga o guia passo a passo detalhado em:

📄 **[INSTALACAO_MANUAL.md](INSTALACAO_MANUAL.md)**

---

## Próximos Passos

Após a instalação (automatizada ou manual):

### 1. Iniciar o servidor

**Windows:**
```bat
scripts\iniciar-servidor.bat
```

**Linux / macOS:**
```bash
bash scripts/iniciar-servidor.sh
```

### 2. Verificar a saúde do sistema

**Windows:**
```powershell
scripts\verificar-saude.ps1
```

**Linux / macOS:**
```bash
bash scripts/verificar-saude.sh
```

O verificador testa a conectividade com o banco, a resposta da API, a autenticação e a listagem de patrimônios. Se tudo estiver correto, você verá:

```
✅ Sistema SIHCP operacional e pronto para uso
   URL da API: http://<ip>:<porta>
```

### 3. Distribuir o APK aos coletores

O APK do app Android está em `bin/sihcp-mobile.apk`. Consulte o guia de distribuição:

📄 **[CONFIGURAR_APP_ANDROID.md](CONFIGURAR_APP_ANDROID.md)**

---

## Documentação Adicional

| Arquivo | Descrição |
|---------|-----------|
| [INSTALACAO_MANUAL.md](INSTALACAO_MANUAL.md) | Guia passo a passo para instalação manual |
| [CHECKLIST_IMPLANTACAO.md](CHECKLIST_IMPLANTACAO.md) | Checklist de verificação pós-instalação |
| [CONFIGURAR_APP_ANDROID.md](CONFIGURAR_APP_ANDROID.md) | Configuração do app Android nos dispositivos |
| [SOLUCAO_PROBLEMAS.md](SOLUCAO_PROBLEMAS.md) | Guia de troubleshooting para erros comuns |

---

## Suporte

Em caso de dúvidas ou problemas não cobertos pela documentação:

- **E-mail:** suporte@sihcp.ifmt.edu.br
- **Repositório:** https://github.com/ifmt/sihcp
