# Script para diagnosticar coletas no SQLite
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DIAGNÓSTICO DE COLETAS NO SQLITE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$dbPath = "data/inventario.db"

if (-not (Test-Path $dbPath)) {
    Write-Host "❌ Banco SQLite não encontrado: $dbPath" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Banco: $dbPath" -ForegroundColor Gray
Write-Host ""

# Verificar se sqlite3 está disponível
$sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue
if (-not $sqlite3) {
    Write-Host "⚠️ sqlite3 não encontrado no PATH" -ForegroundColor Yellow
    Write-Host "Usando Java para consultar..." -ForegroundColor Yellow
    
    # Usar Java para consultar
    $javaCode = @"
import java.sql.*;
public class DiagColetas {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/inventario.db")) {
            // Total de coletas
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_coleta")) {
                if (rs.next()) System.out.println("Total coletas no SQLite: " + rs.getInt(1));
            }
            
            // Coletas pendentes (sync_status)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM local_coleta WHERE sync_status = 'PENDING' OR sync_status IS NULL")) {
                if (rs.next()) System.out.println("Coletas pendentes (sync_status): " + rs.getInt(1));
            }
            
            // Operações pendentes na sync_control
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sync_control WHERE synced = 0")) {
                if (rs.next()) System.out.println("Operações pendentes (sync_control): " + rs.getInt(1));
            }
            
            // Operações de coleta pendentes
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sync_control WHERE synced = 0 AND table_name = 'local_coleta'")) {
                if (rs.next()) System.out.println("Coletas pendentes (sync_control): " + rs.getInt(1));
            }
        }
    }
}
"@
    
    Write-Host ""
    Write-Host "Execute a aplicação e verifique o console para ver o contador atualizado." -ForegroundColor Cyan
    exit 0
}

Write-Host "Consultando SQLite..." -ForegroundColor Yellow
Write-Host ""

# Total de coletas
$total = & sqlite3 $dbPath "SELECT COUNT(*) FROM local_coleta;"
Write-Host "Total de coletas no SQLite: $total" -ForegroundColor White

# Coletas pendentes por sync_status
$pendentes = & sqlite3 $dbPath "SELECT COUNT(*) FROM local_coleta WHERE sync_status = 'PENDING' OR sync_status IS NULL;"
Write-Host "Coletas pendentes (sync_status): $pendentes" -ForegroundColor $(if ($pendentes -gt 0) { "Yellow" } else { "Green" })

# Operações pendentes na sync_control
$opsPendentes = & sqlite3 $dbPath "SELECT COUNT(*) FROM sync_control WHERE synced = 0;"
Write-Host "Operações pendentes (sync_control): $opsPendentes" -ForegroundColor $(if ($opsPendentes -gt 0) { "Yellow" } else { "Green" })

# Coletas na sync_control
$coletasPendentes = & sqlite3 $dbPath "SELECT COUNT(*) FROM sync_control WHERE synced = 0 AND table_name = 'local_coleta';"
Write-Host "Coletas pendentes (sync_control): $coletasPendentes" -ForegroundColor $(if ($coletasPendentes -gt 0) { "Yellow" } else { "Green" })

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
