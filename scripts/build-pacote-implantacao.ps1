# =============================================================================
# Build do Pacote de Implantação SIHCP
# Gera o arquivo SIHCP-Campus-v{versao}.zip pronto para distribuição
# =============================================================================

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "  SIHCP - Build do Pacote de Implantação para Campus             " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host ""

# ---------------------------------------------------------------------------
# Funções auxiliares
# ---------------------------------------------------------------------------

function Write-Step {
    param([string]$Mensagem)
    Write-Host ""
    Write-Host $Mensagem -ForegroundColor Cyan
}

function Write-OK {
    param([string]$Mensagem)
    Write-Host "  [OK] $Mensagem" -ForegroundColor Green
}

function Write-Aviso {
    param([string]$Mensagem)
    Write-Host "  [AVISO] $Mensagem" -ForegroundColor Yellow
}

function Write-Erro {
    param([string]$Mensagem)
    Write-Host "  [ERRO] $Mensagem" -ForegroundColor Red
}

# ---------------------------------------------------------------------------
# Ler versão do pom.xml
# ---------------------------------------------------------------------------

$versao = "0.0.0"
if (Test-Path "pom.xml") {
    $pomContent = Get-Content "pom.xml" -Raw
    if ($pomContent -match '<version>([^<]+)</version>') {
        # Pega a primeira ocorrência (versão do projeto, não do parent)
        $matches2 = [regex]::Matches($pomContent, '<version>([^<]+)</version>')
        foreach ($m in $matches2) {
            $v = $m.Groups[1].Value.Trim()
            # Ignora versões do Spring Boot parent (contém letras como "3.2.0")
            # e pega a versão do projeto (ex: "2.7.0")
            if ($v -notmatch 'SNAPSHOT' -and $v -match '^\d+\.\d+\.\d+$') {
                $versao = $v
                break
            }
        }
    }
    Write-Host "Versão do projeto: $versao" -ForegroundColor White
} else {
    Write-Aviso "pom.xml não encontrado. Usando versão padrão: $versao"
}

# ---------------------------------------------------------------------------
# Verificar Maven
# ---------------------------------------------------------------------------

$mvnCmd = "mvn"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    if (Test-Path ".\mvnw.cmd") {
        $mvnCmd = ".\mvnw.cmd"
        Write-Aviso "Maven não encontrado no PATH. Usando Maven Wrapper (mvnw.cmd)."
    } else {
        Write-Erro "Maven não encontrado! Instale o Maven e adicione ao PATH."
        exit 1
    }
}

# ---------------------------------------------------------------------------
# Preparar diretório de saída
# ---------------------------------------------------------------------------

$pacoteDir = "dist\pacote-campus"
$distDir   = "dist"

if (-not (Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir -Force | Out-Null
}

if (Test-Path $pacoteDir) {
    Write-Host "Removendo pacote anterior em $pacoteDir..." -ForegroundColor Yellow
    Remove-Item -Path $pacoteDir -Recurse -Force
}

New-Item -ItemType Directory -Path "$pacoteDir\bin"      -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\sql"      -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\config"   -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\scripts"  -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\backups"  -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\logs"     -Force | Out-Null
New-Item -ItemType Directory -Path "$pacoteDir\qrcode"   -Force | Out-Null

Write-OK "Estrutura de diretórios criada em $pacoteDir"

# =============================================================================
# [1/6] Build do JAR da API Mobile
# =============================================================================

Write-Step "[1/6] Build do JAR da API Mobile..."

# Tenta com o profile mobile-server primeiro; se falhar, tenta sem profile
Write-Host "  Executando: mvn clean package -DskipTests -P mobile-server" -ForegroundColor White
& $mvnCmd clean package -DskipTests -P mobile-server -q 2>&1 | Out-Null

if ($LASTEXITCODE -ne 0) {
    Write-Aviso "Profile 'mobile-server' não encontrado. Tentando sem profile..."
    & $mvnCmd clean package -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Erro "Falha no build do JAR da API Mobile!"
        exit 1
    }
}

# Localizar o JAR gerado
$mobileJar = $null

# Procura em target/mobile-server/ primeiro (profile thin-jar)
$candidatos = @(
    (Get-ChildItem -Path "target\mobile-server" -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } | Select-Object -First 1),
    (Get-ChildItem -Path "target" -Filter "sistema-inventario*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } | Select-Object -First 1),
    (Get-ChildItem -Path "target" -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } | Select-Object -First 1)
)

foreach ($c in $candidatos) {
    if ($c -and (Test-Path $c.FullName)) {
        $mobileJar = $c
        break
    }
}

if ($mobileJar) {
    Copy-Item $mobileJar.FullName "$pacoteDir\bin\mobile-server.jar" -Force
    $tamanhoMB = [math]::Round($mobileJar.Length / 1MB, 2)
    Write-OK "mobile-server.jar copiado ($tamanhoMB MB)"
} else {
    Write-Erro "JAR da API Mobile não encontrado em target/!"
    exit 1
}

# =============================================================================
# [2/6] Build do JAR do App Desktop
# =============================================================================

Write-Step "[2/6] Build do JAR do App Desktop..."

Write-Host "  Executando: mvn package -DskipTests" -ForegroundColor White
& $mvnCmd package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Erro "Falha no build do JAR do App Desktop!"
    exit 1
}

# Localizar o JAR do desktop (mesmo JAR base, classe principal diferente)
$desktopJar = $null
$candidatosDesktop = @(
    (Get-ChildItem -Path "target" -Filter "sistema-inventario*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } | Select-Object -First 1),
    (Get-ChildItem -Path "target" -Filter "*.jar" -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } | Select-Object -First 1)
)

foreach ($c in $candidatosDesktop) {
    if ($c -and (Test-Path $c.FullName)) {
        $desktopJar = $c
        break
    }
}

if ($desktopJar) {
    Copy-Item $desktopJar.FullName "$pacoteDir\bin\sihcp-desktop.jar" -Force
    $tamanhoMB = [math]::Round($desktopJar.Length / 1MB, 2)
    Write-OK "sihcp-desktop.jar copiado ($tamanhoMB MB)"
} else {
    Write-Aviso "JAR do App Desktop não encontrado. O pacote ficará sem sihcp-desktop.jar."
}

# =============================================================================
# [3/6] Build do APK Android
# =============================================================================

Write-Step "[3/6] Build do APK Android..."

$apkGerado = $false

if (Test-Path "InventarioMobile") {
    Write-Host "  Executando: gradlew.bat assembleRelease" -ForegroundColor White

    $gradleCmd = ".\gradlew.bat"
    if (-not (Test-Path "InventarioMobile\gradlew.bat")) {
        Write-Aviso "gradlew.bat não encontrado em InventarioMobile\. Pulando build do APK."
    } else {
        Push-Location "InventarioMobile"
        & $gradleCmd assembleRelease 2>&1 | Out-Null
        $gradleExitCode = $LASTEXITCODE
        Pop-Location

        if ($gradleExitCode -ne 0) {
            Write-Aviso "Build do APK falhou (código $gradleExitCode). O pacote ficará sem o APK Android."
        } else {
            # Localizar o APK gerado
            $apkPath = Get-ChildItem -Path "InventarioMobile\app\build\outputs\apk\release" -Filter "*.apk" -ErrorAction SilentlyContinue | Select-Object -First 1
            if (-not $apkPath) {
                $apkPath = Get-ChildItem -Path "InventarioMobile\app\build\outputs\apk" -Filter "*.apk" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            }

            if ($apkPath) {
                Copy-Item $apkPath.FullName "$pacoteDir\bin\sihcp-mobile.apk" -Force
                $tamanhoMB = [math]::Round($apkPath.Length / 1MB, 2)
                Write-OK "sihcp-mobile.apk copiado ($tamanhoMB MB)"
                $apkGerado = $true
            } else {
                Write-Aviso "APK não encontrado após o build. Verifique o diretório InventarioMobile\app\build\outputs\apk\"
            }
        }
    }
} else {
    Write-Aviso "Diretório InventarioMobile não encontrado. Pulando build do APK Android."
    Write-Aviso "Para incluir o APK, copie manualmente para $pacoteDir\bin\sihcp-mobile.apk"
}

if (-not $apkGerado) {
    # Verificar se há APK pré-compilado em dist/
    $apkPrecompilado = Get-ChildItem -Path "dist" -Filter "*.apk" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($apkPrecompilado) {
        Copy-Item $apkPrecompilado.FullName "$pacoteDir\bin\sihcp-mobile.apk" -Force
        Write-OK "APK pré-compilado copiado de $($apkPrecompilado.FullName)"
        $apkGerado = $true
    }
}

# =============================================================================
# [4/6] Montar estrutura do pacote
# =============================================================================

Write-Step "[4/6] Montando estrutura do pacote em $pacoteDir..."

# --- SQL ---
if (Test-Path "sql\setup_banco_completo.sql") {
    Copy-Item "sql\setup_banco_completo.sql" "$pacoteDir\sql\" -Force
    Write-OK "sql/setup_banco_completo.sql copiado"
} else {
    Write-Aviso "sql/setup_banco_completo.sql não encontrado. Execute a tarefa 5 do spec primeiro."
}

# Copiar demais scripts SQL (migrações)
$sqlScripts = Get-ChildItem -Path "sql" -Filter "*.sql" -ErrorAction SilentlyContinue | Where-Object { $_.Name -ne "setup_banco_completo.sql" }
if ($sqlScripts) {
    foreach ($sql in $sqlScripts) {
        Copy-Item $sql.FullName "$pacoteDir\sql\" -Force
    }
    Write-OK "$($sqlScripts.Count) script(s) SQL adicional(is) copiado(s)"
}

# --- Config template ---
if (Test-Path "config\application.properties.template") {
    Copy-Item "config\application.properties.template" "$pacoteDir\config\" -Force
    Write-OK "config/application.properties.template copiado"
} else {
    Write-Aviso "config/application.properties.template não encontrado."
}

# --- Scripts ---
$scriptsCopiados = 0
$extensoesScript = @("*.ps1", "*.bat", "*.sh")
foreach ($ext in $extensoesScript) {
    $arquivos = Get-ChildItem -Path "scripts" -Filter $ext -ErrorAction SilentlyContinue
    foreach ($arq in $arquivos) {
        Copy-Item $arq.FullName "$pacoteDir\scripts\" -Force
        $scriptsCopiados++
    }
}
if ($scriptsCopiados -gt 0) {
    Write-OK "$scriptsCopiados script(s) copiado(s) para scripts/"
} else {
    Write-Aviso "Nenhum script encontrado em scripts/"
}

# --- Documentação ---
$docsCopiados = 0
if (Test-Path "docs\implantacao") {
    $docFiles = Get-ChildItem -Path "docs\implantacao" -Filter "*.md" -ErrorAction SilentlyContinue
    foreach ($doc in $docFiles) {
        Copy-Item $doc.FullName "$pacoteDir\" -Force
        $docsCopiados++
    }
    Write-OK "$docsCopiados arquivo(s) de documentação copiado(s) para a raiz do pacote"
} else {
    Write-Aviso "Pasta docs/implantacao não encontrada. Execute a tarefa 13 do spec primeiro."
}

# Diretórios vazios já foram criados acima (backups/, logs/, qrcode/)
Write-OK "Diretórios backups/, logs/ e qrcode/ criados (vazios)"

# =============================================================================
# [5/6] Compactar em ZIP
# =============================================================================

Write-Step "[5/6] Compactando pacote..."

$zipNome   = "SIHCP-Campus-v${versao}.zip"
$zipCaminho = "dist\$zipNome"

if (Test-Path $zipCaminho) {
    Remove-Item $zipCaminho -Force
}

Write-Host "  Gerando $zipCaminho..." -ForegroundColor White

try {
    Compress-Archive -Path "$pacoteDir\*" -DestinationPath $zipCaminho -CompressionLevel Optimal
    Write-OK "ZIP gerado: $zipCaminho"
} catch {
    Write-Erro "Falha ao compactar o pacote: $_"
    exit 1
}

# =============================================================================
# [6/6] Verificar tamanho
# =============================================================================

Write-Step "[6/6] Verificando tamanho do pacote..."

$zipItem   = Get-Item $zipCaminho
$tamanhoMB = [math]::Round($zipItem.Length / 1MB, 1)

if ($tamanhoMB -gt 200) {
    Write-Aviso "Pacote excede 200 MB ($tamanhoMB MB). Pode ser difícil distribuir por e-mail."
    Write-Aviso "Considere remover dependências desnecessárias ou usar um link de download."
} else {
    Write-OK "Tamanho do pacote: $tamanhoMB MB (dentro do limite de 200 MB)"
}

# =============================================================================
# Resumo final
# =============================================================================

Write-Host ""
Write-Host "=================================================================" -ForegroundColor Green
Write-Host "  BUILD CONCLUÍDO!                                               " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Pacote gerado: $zipCaminho" -ForegroundColor Cyan
Write-Host "  Tamanho:       $tamanhoMB MB" -ForegroundColor White
Write-Host "  Versão:        $versao" -ForegroundColor White
Write-Host ""

# Listar conteúdo do bin/
Write-Host "  Artefatos em bin/:" -ForegroundColor White
$binFiles = Get-ChildItem -Path "$pacoteDir\bin" -ErrorAction SilentlyContinue
if ($binFiles) {
    foreach ($f in $binFiles) {
        $fMB = [math]::Round($f.Length / 1MB, 2)
        Write-Host "    - $($f.Name) ($fMB MB)" -ForegroundColor Gray
    }
} else {
    Write-Host "    (vazio)" -ForegroundColor Gray
}

if (-not $apkGerado) {
    Write-Host ""
    Write-Aviso "O APK Android NÃO foi incluído no pacote."
    Write-Aviso "Para incluí-lo, compile o projeto Android e copie o APK para:"
    Write-Aviso "  $pacoteDir\bin\sihcp-mobile.apk"
    Write-Aviso "Depois execute novamente a etapa 5/6 (compactação)."
}

Write-Host ""
Write-Host "Para distribuir: envie o arquivo $zipNome por e-mail ou link de download." -ForegroundColor Cyan
Write-Host ""
