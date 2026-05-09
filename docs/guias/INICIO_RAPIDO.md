# 🚀 Início Rápido - Servidor Mobile

## 1️⃣ Iniciar o Servidor

```powershell
.\iniciar-servidor-mobile.ps1
```

Aguarde até ver: `Started InventarioApplication in X.XXX seconds`

## 2️⃣ Descobrir o IP para o Smartphone

```powershell
.\configurar-smartphone.ps1
```

Este script mostrará a URL completa para usar no app.

## 3️⃣ Configurar o App

No smartphone:
1. Abra o app **Inventário Mobile**
2. Vá em **Configurações**
3. Digite a URL mostrada no passo 2
4. Toque em **Testar Conexão**
5. Toque em **Salvar**

## 4️⃣ Fazer Login

Use as credenciais do sistema:
- **Usuário**: admin (ou seu usuário)
- **Senha**: admin (ou sua senha)

---

## ⚠️ Problemas?

### Porta 8081 em uso
```powershell
.\gerenciar-servidor.ps1 stop
.\gerenciar-servidor.ps1 start
```

### Não consegue conectar do smartphone
1. Verifique se ambos estão na mesma rede Wi-Fi
2. Desative dados móveis no smartphone
3. Verifique o firewall do Windows

### Erro de banco de dados
Verifique se o PostgreSQL está rodando:
```powershell
Get-Service -Name "*postgres*"
```

---

## 📚 Documentação Completa

- **Guia detalhado**: `GUIA_CONEXAO_SMARTPHONE.md`
- **Scripts disponíveis**:
  - `iniciar-servidor-mobile.ps1` - Inicia o servidor
  - `gerenciar-servidor.ps1` - Gerencia o servidor (start/stop/status)
  - `configurar-smartphone.ps1` - Mostra IP e instruções

---

## 🔧 Comandos Úteis

```powershell
# Ver status do servidor
.\gerenciar-servidor.ps1 status

# Parar o servidor
.\gerenciar-servidor.ps1 stop

# Reiniciar o servidor
.\gerenciar-servidor.ps1 restart

# Ver IP do computador
ipconfig | findstr "IPv4"

# Testar API no navegador
# http://localhost:8081/inventario/api/mobile/health
```

---

## ✅ Checklist

Antes de conectar o smartphone:

- [ ] PostgreSQL rodando
- [ ] Servidor iniciado (porta 8081)
- [ ] Firewall configurado
- [ ] Smartphone na mesma rede Wi-Fi
- [ ] IP do servidor anotado
- [ ] App instalado no smartphone

---

## 📱 APKs Gerados

Os APKs estão em:
```
InventarioMobile/app/build/outputs/apk/
├── debug/
│   └── app-debug.apk          (para testes)
└── release/
    └── app-release.apk        (para produção)
```

Transfira o APK para o smartphone e instale.

---

## 🆘 Suporte

Se precisar de ajuda, consulte:
1. `GUIA_CONEXAO_SMARTPHONE.md` - Guia completo
2. `logs/sistema-inventario.log` - Logs do servidor
3. Logs do app (Menu > Configurações > Logs)
