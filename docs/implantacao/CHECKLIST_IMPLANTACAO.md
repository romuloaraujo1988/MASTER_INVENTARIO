# Checklist de Implantação — SIHCP

Use este checklist para verificar que todos os componentes foram instalados e configurados corretamente antes de liberar o sistema para uso.

---

## Pré-requisitos

- [ ] JDK 21 instalado e no PATH
  - Verificar: `java -version` deve exibir `openjdk version "21.x.x"`
- [ ] PostgreSQL 12+ instalado e em execução
  - Verificar: `psql --version` deve exibir `psql (PostgreSQL) 12.x` ou superior
  - Verificar: serviço PostgreSQL está ativo (`pg_isready` ou `systemctl status postgresql`)

---

## Banco de Dados

- [ ] Banco de dados criado
  - Verificar: `psql -U postgres -l` lista o banco `sispatrimonio`
- [ ] SQL de setup executado sem erros
  - Verificar: `psql -d sispatrimonio -c "\dt"` lista as tabelas do sistema
  - Verificar: tabela `schema_version` existe e contém o registro da versão inicial

---

## Configuração

- [ ] Arquivo `config/configuracao_banco.json` criado
  - Verificar: arquivo existe no diretório `config/`
  - Verificar: campos `campus.nome`, `postgresql.host`, `postgresql.user`, `postgresql.password` e `api.porta` estão preenchidos
  - Verificar: `campus.nome` tem no máximo 100 caracteres
  - Verificar: `api.porta` está no intervalo 1024–65535

---

## Servidor

- [ ] Servidor iniciado sem erros
  - Verificar: script `scripts/iniciar-servidor.bat` (Windows) ou `bash scripts/iniciar-servidor.sh` (Linux) executa sem mensagens de erro
  - Verificar: log exibe `Started MobileApiApplication`
- [ ] Endpoint `/api/mobile/health` retorna HTTP 200
  - Verificar: `curl http://localhost:<porta>/api/mobile/health` retorna `{"status":"UP"}`

---

## Autenticação e Acesso

- [ ] Login com usuário `admin` funciona
  - Verificar: `POST /api/mobile/auth/login` com `{"login":"admin","senha":"<senha_definida>"}` retorna token JWT
  - Verificar: token pode ser usado para acessar `GET /api/mobile/patrimonio`

---

## App Android

- [ ] APK distribuído aos coletores
  - Verificar: arquivo `bin/sihcp-mobile.apk` foi copiado ou enviado aos dispositivos
  - Verificar: APK instalado com sucesso em pelo menos um dispositivo de teste
- [ ] QR Code gerado e disponível
  - Verificar: arquivo `qrcode/api-qrcode.png` existe
  - Verificar: QR Code decodificado aponta para `http://<ip>:<porta>/api/mobile`
- [ ] App Android conecta ao servidor
  - Verificar: login no app com usuário `admin` funciona
  - Verificar: lista de patrimônios carrega no app

---

## Verificação Final

Execute o script de verificação de saúde para confirmar que todos os componentes estão operacionais:

**Windows:**
```powershell
scripts\verificar-saude.ps1
```

**Linux / macOS:**
```bash
bash scripts/verificar-saude.sh
```

Resultado esperado:
```
✅ Sistema SIHCP operacional e pronto para uso
   URL da API: http://<ip>:<porta>
```

O relatório gerado (`relatorio-saude-<data>.txt`) deve ser arquivado como registro da implantação.

---

## Observações

- Registre a data da implantação, a versão instalada e o responsável técnico
- Guarde uma cópia do `relatorio-saude-<data>.txt` para fins de auditoria
- Em caso de problemas, consulte [SOLUCAO_PROBLEMAS.md](SOLUCAO_PROBLEMAS.md)
- Para suporte: suporte@sihcp.ifmt.edu.br
