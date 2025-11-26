# Script para corrigir o arquivo de configuração do banco de dados
# Usa Java Properties para garantir formato correto

$configDir = "$env:LOCALAPPDATA\SIHCP-Inventario"
$configPath = "$configDir\database-config.properties"

# Criar diretório se não existir
if (-not (Test-Path $configDir)) {
    New-Item -ItemType Directory -Path $configDir -Force | Out-Null
    Write-Host "Diretório criado: $configDir"
}

# Criar conteúdo do arquivo de propriedades
# Nota: Java Properties usa = como separador e escapa caracteres especiais
$content = @"
#Database Configuration - Fixed
#$(Get-Date -Format "ddd MMM dd HH:mm:ss yyyy")
host=localhost
port=5432
database=sispatrimonio
username=postgres
password=Romulo@2020
"@

# Escrever arquivo usando .NET para garantir encoding correto
[System.IO.File]::WriteAllText($configPath, $content, [System.Text.Encoding]::GetEncoding("ISO-8859-1"))

Write-Host "Arquivo criado: $configPath"
Write-Host ""
Write-Host "Conteúdo:"
Get-Content $configPath
Write-Host ""
Write-Host "Verificando se password está presente..."
$fileContent = [System.IO.File]::ReadAllText($configPath)
if ($fileContent -match "password=") {
    Write-Host "OK: password encontrado no arquivo!"
} else {
    Write-Host "ERRO: password NAO encontrado!"
}
