# SIHCP Mobile - Sistema de Inventário Mobile

Aplicativo Android para coleta de dados de inventário patrimonial do IFMT.

## 📱 Sobre o Aplicativo

O SIHCP Mobile é um aplicativo Android nativo desenvolvido em Kotlin que permite:

- **Coleta de Patrimônio**: Escaneamento de QR Codes e coleta manual
- **Sincronização**: Sincronização automática com o servidor
- **Modo Offline**: Funcionamento sem conexão com sincronização posterior
- **Relatórios**: Visualização de coletas realizadas

## 🏗️ Arquitetura

- **Linguagem**: Kotlin
- **Arquitetura**: MVVM (Model-View-ViewModel)
- **Banco Local**: Room Database
- **Rede**: Retrofit + OkHttp
- **UI**: View Binding + Material Design
- **Async**: Coroutines + Flow

## 📦 Estrutura do Projeto

```
app/src/main/java/com/inventario/mobile/
├── data/                    # Camada de dados
│   ├── local/              # Room Database
│   ├── remote/             # API REST
│   └── repository/         # Repositórios
├── domain/                 # Regras de negócio
│   ├── model/              # Modelos de domínio
│   └── repository/         # Interfaces
├── presentation/           # Camada de apresentação
│   ├── login/              # Tela de login
│   ├── main/               # Tela principal
│   ├── scanner/            # Scanner QR Code
│   └── coleta/             # Coletas
├── utils/                  # Utilitários
└── InventarioMobileApplication.kt
```

## 🚀 Como Executar

### Pré-requisitos

- Android Studio Arctic Fox ou superior
- JDK 11 ou superior
- Android SDK 21+ (Android 5.0+)
- Servidor backend rodando

### Configuração

1. **Clone o repositório**
2. **Abra no Android Studio**
3. **Configure o IP do servidor**:
   - Edite `app/src/main/res/values/server_config.xml`
   - Altere `default_server_ip` para o IP do seu servidor
4. **Compile e execute**

### Build APK

```bash
# APK Debug
./gradlew assembleDebug

# APK Release
./gradlew assembleRelease
```

## ⚙️ Configuração do Servidor

O app se conecta ao servidor Java via REST API:

- **Porta padrão**: 8081
- **Context path**: `/inventario`
- **API path**: `/api/mobile`
- **URL completa**: `http://[IP]:8081/inventario/api/mobile`

### IPs Sugeridos

- `192.168.11.136` (padrão atual)
- `10.14.250.238` (servidor IFMT)
- `localhost` (desenvolvimento)

## 📋 Funcionalidades

### ✅ Implementadas

- [x] Login com autenticação JWT
- [x] Scanner QR Code
- [x] Coleta manual de patrimônio
- [x] Seleção de salas
- [x] Banco de dados local (Room)
- [x] Sincronização básica
- [x] Configuração dinâmica de servidor
- [x] **[NOVO v1.3]** Busca em tempo real de patrimônios
- [x] **[NOVO v1.3]** Sistema de ordenação (6 opções)
- [x] **[NOVO v1.3]** Visualização detalhada de patrimônios
- [x] **[NOVO v1.3]** Menu de ações rápidas
- [x] **[NOVO v1.3]** Layout aprimorado com marca e modelo

### 🔄 Em Desenvolvimento

- [ ] Exportação de dados (Excel/PDF)
- [ ] Filtros avançados
- [ ] Sincronização completa
- [ ] Relatórios detalhados
- [ ] Modo offline avançado
- [ ] Notificações push

## 🛠️ Tecnologias Utilizadas

### Core
- **Kotlin 1.9.10**
- **Android SDK 34**
- **Material Design 3**

### Arquitetura
- **Room 2.6.1** - Banco local
- **Retrofit 2.9.0** - Cliente HTTP
- **Coroutines 1.7.3** - Programação assíncrona

### UI/UX
- **View Binding** - Binding de views
- **Navigation Component** - Navegação
- **SwipeRefreshLayout** - Pull to refresh

### Utilitários
- **ZXing 4.3.0** - Scanner QR Code
- **Glide 4.16.0** - Carregamento de imagens
- **ThreeTenABP** - Manipulação de datas

## 📱 Compatibilidade

- **Mínimo**: Android 5.0 (API 21)
- **Target**: Android 14 (API 34)
- **Arquiteturas**: ARM64, ARM32, x86, x86_64

## 🔧 Desenvolvimento

### Comandos Úteis

```bash
# Limpar projeto
./gradlew clean

# Compilar
./gradlew build

# Executar testes
./gradlew test

# Instalar no dispositivo
./gradlew installDebug
```

### Estrutura de Branches

- `main` - Versão estável
- `develop` - Desenvolvimento ativo
- `feature/*` - Novas funcionalidades

## 📄 Documentação

### Geral
- `README.md` - Este arquivo
- `DOCUMENTACAO_PROJETO.md` - Documentação técnica detalhada
- `TECNOLOGIAS_DEPENDENCIAS.md` - Lista de dependências

### Build e Deploy
- `COMANDOS_UTEIS.md` - Comandos de build e desenvolvimento
- `GUIA_GERACAO_APK.md` - Guia para gerar APKs
- `INSTRUCOES_BUILD_V1.3.md` - **[NOVO]** Instruções completas de build

### Configuração
- `CONFIGURACAO_IP_SERVIDOR.md` - Configuração de rede
- `CORRECAO_IPS_ESTATICOS.md` - Correção de IPs

### Versão 1.3.0 (Tela de Inventário)
- `MELHORIAS_TELA_INVENTARIO.md` - **[NOVO]** Documentação técnica das melhorias
- `GUIA_VISUAL_INVENTARIO.md` - **[NOVO]** Guia visual para usuários
- `CHANGELOG_INVENTARIO.md` - **[NOVO]** Histórico de mudanças
- `GUIA_TESTE_RAPIDO_V1.3.md` - **[NOVO]** Roteiro de testes
- `INDICE_DOCUMENTACAO_V1.3.md` - **[NOVO]** Índice completo da documentação

### Outros
- `SOLUCAO_CONECTIVIDADE.md` - Solução de problemas de rede
- `GUIA_TESTE_RAPIDO.md` - Testes gerais

## 🐛 Troubleshooting

### Problemas Comuns

1. **Erro de conexão**: Verifique se o servidor está rodando e o IP está correto
2. **Erro de build**: Execute `./gradlew clean` e tente novamente
3. **Scanner não funciona**: Verifique permissões de câmera

### Logs

Os logs do app ficam em:
- Android Studio: Logcat
- Dispositivo: `/Android/data/com.inventario.mobile/files/logs/`

## 📞 Suporte

Para dúvidas ou problemas:

1. Verifique a documentação
2. Consulte os logs de erro
3. Abra uma issue no repositório

## 📝 Licença

Este projeto é propriedade do IFMT (Instituto Federal de Mato Grosso).

---

## 🎉 Novidades da Versão 1.3.0

### Tela de Inventário Aprimorada

A versão 1.3.0 traz melhorias significativas na tela de inventário:

#### 🔍 Busca em Tempo Real
- Busca instantânea enquanto digita
- Pesquisa em múltiplos campos (número, descrição, marca, modelo, setor, sala)
- Contador contextual mostrando resultados

#### 📊 Sistema de Ordenação
- 6 opções de ordenação disponíveis
- Ordenação por número, descrição ou setor
- Ordem crescente ou decrescente

#### 📋 Visualização Detalhada
- Diálogo completo com todas as informações do patrimônio
- Acesso rápido ao scanner QR
- Layout profissional e organizado

#### ⚡ Menu de Ações Rápidas
- FAB com ações frequentes
- Escanear QR Code
- Atualizar lista
- Exportar dados (em breve)

#### 🎨 Melhorias Visuais
- Exibição de marca e modelo nos cards
- Ícones melhorados
- Status de coleta com cores distintas
- Layout otimizado

**Para mais detalhes**, consulte:
- `MELHORIAS_TELA_INVENTARIO.md` - Documentação técnica
- `GUIA_VISUAL_INVENTARIO.md` - Guia do usuário
- `CHANGELOG_INVENTARIO.md` - Histórico completo

---

**Versão**: 1.3.0  
**Última atualização**: 03 de Novembro de 2025