# Script PowerShell para verificar salas usando JDBC diretamente
# Não depende de psql ou maven no PATH

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAÇÃO DE SALAS - JDBC" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configurações
$dbHost = "localhost"
$dbPort = "5432"
$dbName = "sispatrimonio"
$dbUser = "inventario"
$dbPass = "inventario"

# Procurar driver JDBC do PostgreSQL
$possibleJars = @(
    "lib\postgresql-*.jar",
    "target\classes",
    "$env:USERPROFILE\.m2\repository\org\postgresql\postgresql\*\postgresql-*.jar"
)

$jdbcJar = $null
foreach ($pattern in $possibleJars) {
    $found = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) {
        $jdbcJar = $found.FullName
        Write-Host "Driver JDBC encontrado: $jdbcJar" -ForegroundColor Green
        break
    }
}

if (-not $jdbcJar) {
    Write-Host "❌ Driver JDBC do PostgreSQL não encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Execute primeiro:" -ForegroundColor Yellow
    Write-Host "  mvn dependency:copy-dependencies -DoutputDirectory=lib" -ForegroundColor Gray
    exit 1
}

# Criar código Java temporário
$javaCode = @"
import java.sql.*;

public class VerificarSalas {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://$dbHost`:$dbPort/$dbName";
        String user = "$dbUser";
        String pass = "$dbPass";
        
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✓ Conectado ao PostgreSQL");
            System.out.println();
            
            // Teste 1: Total absoluto
            System.out.println("TESTE 1: Total de registros");
            System.out.println("----------------------------");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TABELA_SALA")) {
                if (rs.next()) {
                    System.out.println("Total de registros: " + rs.getInt(1));
                }
            }
            System.out.println();
            
            // Teste 2: Por status ATIVO
            System.out.println("TESTE 2: Distribuição por ATIVO");
            System.out.println("--------------------------------");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT ATIVO, COUNT(*) FROM TABELA_SALA GROUP BY ATIVO ORDER BY ATIVO DESC")) {
                while (rs.next()) {
                    System.out.println("  ATIVO = " + rs.getBoolean(1) + ": " + rs.getInt(2) + " sala(s)");
                }
            }
            System.out.println();
            
            // Teste 3: Com LEFT JOIN
            System.out.println("TESTE 3: Query com LEFT JOIN (listarTodasSalas)");
            System.out.println("------------------------------------------------");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM TABELA_SALA s LEFT JOIN TABELA_SETOR st ON s.ID_SETOR = st.ID")) {
                if (rs.next()) {
                    System.out.println("Total com LEFT JOIN: " + rs.getInt(1));
                }
            }
            System.out.println();
            
            // Teste 4: Primeiras 10 salas
            System.out.println("TESTE 4: Primeiras 10 salas");
            System.out.println("---------------------------");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT ID_SALA, NUMERO_SALA, DESCRICAO, ATIVO FROM TABELA_SALA ORDER BY ID_SALA LIMIT 10")) {
                int i = 1;
                while (rs.next()) {
                    System.out.println("  " + i + ". ID=" + rs.getInt(1) + 
                                     ", Número=" + rs.getString(2) + 
                                     ", Ativo=" + rs.getBoolean(4));
                    i++;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
"@

# Salvar código Java
$javaFile = "VerificarSalas.java"
$javaCode | Out-File -FilePath $javaFile -Encoding UTF8

Write-Host "Compilando código Java..." -ForegroundColor Yellow
javac -cp $jdbcJar $javaFile 2>&1 | Out-Null

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilação OK" -ForegroundColor Green
    Write-Host ""
    Write-Host "Executando verificação..." -ForegroundColor Yellow
    Write-Host ""
    
    # Executar
    java -cp ".;$jdbcJar" VerificarSalas
    
    # Limpar arquivos temporários
    Remove-Item $javaFile -ErrorAction SilentlyContinue
    Remove-Item "VerificarSalas.class" -ErrorAction SilentlyContinue
} else {
    Write-Host "❌ Erro na compilação" -ForegroundColor Red
    Remove-Item $javaFile -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  VERIFICAÇÃO CONCLUÍDA" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
