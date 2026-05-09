# Configurar o App Android — SIHCP

Este guia explica como instalar e configurar o app SIHCP nos dispositivos Android dos coletores de campo.

---

## Habilitar Instalação de Fontes Desconhecidas

O app SIHCP é distribuído como APK direto (fora da Play Store), então é necessário permitir a instalação de fontes desconhecidas no dispositivo.

### Android 8.0 ou superior (Oreo+)

A permissão é concedida por aplicativo, não globalmente:

1. Abra **Configurações** → **Aplicativos** (ou **Gerenciar aplicativos**)
2. Toque no gerenciador de arquivos que você usará para instalar o APK (ex.: **Meus Arquivos**, **Files by Google**)
3. Toque em **Instalar aplicativos desconhecidos**
4. Ative a opção **Permitir desta fonte**

> **Alternativa:** Ao tentar instalar o APK, o Android exibirá automaticamente uma tela pedindo permissão. Toque em **Configurações** e ative a opção.

### Android 7.x ou inferior

1. Abra **Configurações** → **Segurança**
2. Ative a opção **Fontes desconhecidas**
3. Confirme o aviso de segurança

---

## Instalar o APK

O arquivo APK está em `bin/sihcp-mobile.apk` no pacote de implantação.

### Opção 1 — Via cabo USB

1. Conecte o dispositivo ao computador via USB
2. Copie o arquivo `bin/sihcp-mobile.apk` para o armazenamento interno do dispositivo
3. No dispositivo, abra o gerenciador de arquivos
4. Navegue até o arquivo copiado e toque nele
5. Toque em **Instalar**

### Opção 2 — Via rede Wi-Fi

1. Coloque o arquivo `bin/sihcp-mobile.apk` em um servidor web acessível na rede do campus
2. No dispositivo, abra o navegador e acesse o endereço do arquivo
3. Faça o download e abra o arquivo baixado
4. Toque em **Instalar**

### Opção 3 — Via e-mail ou mensagem

1. Envie o arquivo `bin/sihcp-mobile.apk` por e-mail ou aplicativo de mensagens
2. No dispositivo, abra o anexo
3. Toque em **Instalar**

---

## Configurar o Endereço do Servidor no App

Após instalar o app, é necessário informar o endereço IP do servidor onde a API Mobile está rodando.

1. Abra o app **SIHCP** no dispositivo
2. Na tela de login, toque em **Configurações** (ícone de engrenagem) ou acesse o menu
3. No campo **Endereço do servidor**, informe:
   ```
   http://<IP_DO_SERVIDOR>:<PORTA>
   ```
   Exemplo: `http://192.168.1.100:8080`
4. Toque em **Salvar** ou **Conectar**
5. O app exibirá uma confirmação de conexão bem-sucedida

> **Onde encontrar o IP do servidor?** Execute `ipconfig` (Windows) ou `ip addr` (Linux) no servidor. Use o endereço da interface de rede local (ex.: `192.168.x.x`).

---

## Usar o QR Code para Configuração Rápida

O setup automatizado gera um QR Code com o endereço da API em `qrcode/api-qrcode.png`. Isso facilita a configuração em múltiplos dispositivos.

1. Imprima ou exiba o arquivo `qrcode/api-qrcode.png` em uma tela
2. No app SIHCP, na tela de configurações, toque em **Escanear QR Code**
3. Aponte a câmera para o QR Code
4. O endereço do servidor será preenchido automaticamente
5. Toque em **Salvar**

> **Dica:** Imprima o QR Code e cole em um local visível no campus para facilitar a configuração de novos dispositivos.

---

## Campus em Rede Interna

Se o campus opera em rede interna sem acesso à internet, use o endereço IP local do servidor.

### Encontrar o IP local do servidor

**Windows:**
```powershell
ipconfig
```
Procure por **Endereço IPv4** na interface de rede (ex.: `192.168.1.100`).

**Linux:**
```bash
ip addr show
# ou
hostname -I
```

### Configurar o app com IP local

Use o endereço no formato:
```
http://192.168.x.x:<porta>
```

Exemplo: `http://192.168.1.100:8080`

### Garantir que os dispositivos estão na mesma rede

- Todos os dispositivos Android devem estar conectados ao **mesmo Wi-Fi** do campus
- O servidor deve estar acessível nessa rede (sem firewall bloqueando a porta da API)
- Verifique a conectividade: no dispositivo, abra o navegador e acesse `http://192.168.x.x:<porta>/api/mobile/health` — deve retornar `{"status":"UP"}`

---

## Solução de Problemas Comuns do App

### App não conecta ao servidor

- Verifique se o endereço IP e a porta estão corretos
- Verifique se o servidor está em execução (`scripts/verificar-saude.ps1` ou `.sh`)
- Verifique se o dispositivo está na mesma rede Wi-Fi do servidor
- Verifique se o firewall do servidor não está bloqueando a porta da API
- Tente acessar `http://<IP>:<porta>/api/mobile/health` no navegador do dispositivo

### APK não instala

- Verifique se a opção de fontes desconhecidas está habilitada (veja a seção acima)
- Verifique se há espaço suficiente no armazenamento do dispositivo
- Verifique se a versão do Android é compatível (mínimo: Android 6.0 / API 23)
- Tente baixar o APK novamente — o arquivo pode estar corrompido

### Login não funciona

- Verifique se o usuário `admin` foi criado durante o setup
- Verifique se a senha está correta (a senha é definida durante o setup automatizado)
- Verifique se o servidor está respondendo (`GET /api/mobile/health`)
- Consulte [SOLUCAO_PROBLEMAS.md#autenticacao](SOLUCAO_PROBLEMAS.md#autenticacao)

### QR Code não é lido

- Certifique-se de que a câmera tem permissão de acesso no app
- Tente em um ambiente com boa iluminação
- Aumente o brilho da tela onde o QR Code está sendo exibido
- Gere um novo QR Code com tamanho maior (edite o script de setup)

### App trava ou fecha inesperadamente

- Verifique se o dispositivo tem pelo menos 2 GB de RAM
- Limpe o cache do app: **Configurações** → **Aplicativos** → **SIHCP** → **Limpar cache**
- Desinstale e reinstale o app
- Verifique os logs do servidor para erros relacionados

---

## Informações Adicionais

- **Versão mínima do Android:** 6.0 (API 23)
- **Permissões necessárias:** Câmera (para QR Code), Armazenamento (para exportação)
- **Modo offline:** O app funciona sem conexão e sincroniza quando a rede estiver disponível
- **Suporte:** suporte@sihcp.ifmt.edu.br
