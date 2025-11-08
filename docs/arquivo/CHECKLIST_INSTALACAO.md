# ✅ Checklist de Instalação - SIHCP Mobile

## Antes de Instalar

- [ ] Smartphone Android disponível (mínimo Android 5.0 / API 21)
- [ ] Cabo USB para conectar ao computador (se usar instalação automática)
- [ ] Espaço livre no smartphone (mínimo 50 MB)
- [ ] Backup dos dados do app antigo (se necessário)

## Instalação

### Opção A: Instalação Automática (USB)

- [ ] Conectar smartphone ao computador via USB
- [ ] Ativar Depuração USB no smartphone
- [ ] Autorizar computador no smartphone (popup)
- [ ] Executar `instalar-app-debug.bat` ou `instalar-app-debug.ps1`
- [ ] Aguardar mensagem de sucesso

### Opção B: Instalação Manual

- [ ] Copiar `SIHCP-Mobile-Debug.apk` para o smartphone
- [ ] Abrir o arquivo APK no smartphone
- [ ] Permitir instalação de fontes desconhecidas (se solicitado)
- [ ] Tocar em "Instalar"
- [ ] Aguardar conclusão

## Após Instalação

### Primeiro Acesso

- [ ] Abrir aplicativo SIHCP Mobile
- [ ] Fazer login com usuário e senha
- [ ] Aguardar carregamento do dashboard
- [ ] Verificar se os dados aparecem corretamente

### Testar Scanner QR Code

- [ ] No dashboard, tocar em "Coleta Rápida (QR Code)"
- [ ] Selecionar uma sala da lista
- [ ] **CONCEDER PERMISSÃO DE CÂMERA** quando solicitado
- [ ] Verificar se a câmera abre
- [ ] Posicionar um QR Code de patrimônio
- [ ] Verificar se o QR Code é lido
- [ ] Verificar se as informações do patrimônio aparecem
- [ ] Testar botão "Coletar" (se item não coletado)

### Verificar Permissões

- [ ] Ir em Configurações do Android
- [ ] Aplicativos > SIHCP Mobile > Permissões
- [ ] Verificar se **Câmera** está **Permitida**
- [ ] Verificar se **Armazenamento** está **Permitido** (se necessário)

## Testes Funcionais

### Dashboard

- [ ] Contador de patrimônios aparece
- [ ] Contador de coletas pendentes aparece
- [ ] Contador de sincronizações pendentes aparece
- [ ] Nome do usuário aparece
- [ ] Perfil do usuário aparece

### Coleta Rápida (QR Code)

- [ ] Botão "Coleta Rápida" funciona
- [ ] Lista de salas carrega
- [ ] Seleção de sala funciona
- [ ] Scanner abre após selecionar sala
- [ ] Câmera funciona
- [ ] QR Code é lido corretamente
- [ ] Informações do patrimônio aparecem
- [ ] Status de coleta é exibido
- [ ] Botão "Coletar" funciona
- [ ] Contador de coletas atualiza

### Coleta Manual

- [ ] Botão "Coleta Manual" funciona
- [ ] Lista de salas carrega
- [ ] Formulário de coleta manual abre
- [ ] Campos podem ser preenchidos
- [ ] Busca de patrimônio funciona
- [ ] Salvar coleta funciona

### Visualizar Coletas

- [ ] Botão "Visualizar Coletas" funciona
- [ ] Lista de coletas carrega
- [ ] Detalhes da coleta aparecem ao tocar
- [ ] Informações estão corretas

## Troubleshooting

### Se algo não funcionar:

#### Scanner não abre
- [ ] Verificar permissão de câmera
- [ ] Reiniciar aplicativo
- [ ] Limpar cache do app
- [ ] Reinstalar aplicativo

#### Câmera não funciona
- [ ] Verificar se outra app usa a câmera
- [ ] Fechar outros apps
- [ ] Reiniciar smartphone
- [ ] Verificar se câmera funciona em outros apps

#### QR Code não é lido
- [ ] Verificar se QR Code está legível
- [ ] Melhorar iluminação
- [ ] Aproximar/afastar câmera
- [ ] Limpar lente da câmera

#### Patrimônio não encontrado
- [ ] Verificar se QR Code é válido
- [ ] Sincronizar dados
- [ ] Verificar conexão com servidor
- [ ] Verificar se patrimônio existe no sistema

#### App trava
- [ ] Fechar e reabrir app
- [ ] Limpar cache
- [ ] Desinstalar e reinstalar
- [ ] Verificar logs (desenvolvedores)

## Validação Final

### Testes Completos

- [ ] Login funciona
- [ ] Dashboard carrega
- [ ] Coleta rápida (QR Code) funciona
- [ ] Coleta manual funciona
- [ ] Visualizar coletas funciona
- [ ] Sincronização funciona
- [ ] Logout funciona

### Performance

- [ ] App abre rapidamente (< 3 segundos)
- [ ] Navegação é fluida
- [ ] Scanner responde rapidamente
- [ ] Sem travamentos
- [ ] Sem fechamentos inesperados

### Dados

- [ ] Patrimônios aparecem corretamente
- [ ] Salas aparecem corretamente
- [ ] Coletas são salvas
- [ ] Sincronização funciona
- [ ] Dados offline funcionam (se aplicável)

## Informações do Build

- **APK:** SIHCP-Mobile-Debug.apk
- **Tamanho:** 9.8 MB
- **Versão:** 1.1 (versionCode 2)
- **Package:** com.inventario.mobile.debug
- **Build Type:** Debug
- **Data:** 27/10/2025

## Documentação Relacionada

- 📄 `INSTRUCOES_INSTALACAO_APP.md` - Instruções detalhadas
- 📄 `SOLUCAO_ERRO_SCANNER_QRCODE.md` - Detalhes técnicos do problema
- 🔧 `instalar-app-debug.bat` - Script de instalação (CMD)
- 🔧 `instalar-app-debug.ps1` - Script de instalação (PowerShell)

## Status da Instalação

Marque conforme avança:

- [ ] ✅ Instalação concluída
- [ ] ✅ Primeiro acesso realizado
- [ ] ✅ Permissões concedidas
- [ ] ✅ Scanner testado e funcionando
- [ ] ✅ Coleta realizada com sucesso
- [ ] ✅ Todos os testes passaram

---

**Última atualização:** 27/10/2025
**Versão do checklist:** 1.0
