# Script para diagnosticar dados de inventário no SQLite
# Sistema de Inventário de Patrimônio

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Diagnóstico SQLite - Inventários" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$SQLITE_DB = "data\inventario.db"

# Verificar se o banco existe
if (!(Test-Path $SQLITE_DB)) {
    Write-Host "[ERRO] Banco SQLite não encontrado: $SQLITE_DB" -ForegroundColor Red
    Write-Host "[DICA] Execute primeiro: .\sincronizar-sqlite-offline.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Banco SQLite encontrado: $SQLITE_DB" -ForegroundColor Green
Write-Host ""

# Verificar se sqlite3 está disponível
$sqlite3 = Get-Command sqlite3 -ErrorAction SilentlyContinue
if (!$sqlite3) {
    Write-Host "[AVISO] sqlite3 CLI não encontrado. Tentando usar Java..." -ForegroundColor Yellow
    Write-Host ""
    
    # Usar Java para consultar
    $javaCode = @"
import java.sql.*;

public class DiagnosticoSQLite {
    public static void main(String[] args) {
        String dbPath = "data/inventario.db";
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            System.out.println("=== TABELA: TABELA_INVENTARIO ===");
            consultarTabela(conn, "TABELA_INVENTARIO", 
                "SELECT ID, NOME, STATUS_INVENTARIO, DATA_INICIO FROM TABELA_INVENTARIO");
            
            System.out.println("\n=== TABELA: local_inventario ===");
            consultarTabela(conn, "local_inventario", 
                "SELECT id, nome_inventario, status_inventario, data_inicio FROM local_inventario");
            
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void consultarTabela(Connection conn, String nomeTabela, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("  [" + count + "] ID: " + rs.getInt(1) + 
                    " | Nome: " + rs.getString(2) + 
                    " | Status: " + rs.getString(3) + 
                    " | Data: " + rs.getString(4));
            }
            
            if (count == 0) {
                System.out.println("  (vazio)");
            } else {
                System.out.println("  Total: " + count + " registro(s)");
            }
            
        } catch (SQLException e) {
            System.out.println("  ERRO: " + e.getMessage());
        }
    }
}
"@
    
    # Salvar código Java temporário
    $javaCode | Out-File -FilePath "DiagnosticoSQLite.java" -Encoding UTF8
    
    # Compilar e executar
    javac DiagnosticoSQLite.java
    java -cp ".;sqlite-jdbc-3.42.0.0.jar" DiagnosticoSQLite
    
    # Limpar arquivos temporários
    Remove-Item "DiagnosticoSQLite.java" -ErrorAction SilentlyContinue
    Remove-Item "DiagnosticoSQLite.class" -ErrorAction SilentlyContinue
    
} else {
    Write-Host "[OK] sqlite3 CLI encontrado" -ForegroundColor Green
    Write-Host ""
    
    # Consultar TABELA_INVENTARIO
    Write-Host "=== TABELA: TABELA_INVENTARIO ===" -ForegroundColor Cyan
    $result = sqlite3 $SQLITE_DB "SELECT ID, NOME, STATUS_INVENTARIO, DATA_INICIO FROM TABELA_INVENTARIO;" 2>&1
    
    if ($result) {
        $lines = $result -split "`n"
        $count = 0
        foreach ($line in $lines) {
            if ($line.Trim()) {
                $count++
                $fields = $line -split "\|"
                Write-Host "  [$count] ID: $($fields[0]) | Nome: $($fields[1]) | Status: $($fields[2]) | Data: $($fields[3])" -ForegroundColor White
            }
        }
        if ($count -eq 0) {
            Write-Host "  (vazio)" -ForegroundColor Yellow
        } else {
            Write-Host "  Total: $count registro(s)" -ForegroundColor Green
        }
    } else {
        Write-Host "  (vazio)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    
    # Consultar local_inventario
    Write-Host "=== TABELA: local_inventario ===" -ForegroundColor Cyan
    $result = sqlite3 $SQLITE_DB "SELECT id, nome, status, data_inicio FROM local_inventario;" 2>&1
    
    if ($result) {
        $lines = $result -split "`n"
        $count = 0
        foreach ($line in $lines) {
            if ($line.Trim()) {
                $count++
                $fields = $line -split "\|"
                Write-Host "  [$count] ID: $($fields[0]) | Nome: $($fields[1]) | Status: $($fields[2]) | Data: $($fields[3])" -ForegroundColor White
            }
        }
        if ($count -eq 0) {
            Write-Host "  (vazio)" -ForegroundColor Yellow
        } else {
            Write-Host "  Total: $count registro(s)" -ForegroundColor Green
        }
    } else {
        Write-Host "  (vazio)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Diagnóstico Concluído" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[DICA] Se ambas as tabelas estiverem vazias, execute:" -ForegroundColor Yellow
Write-Host "       .\sincronizar-sqlite-offline.ps1" -ForegroundColor Yellow
Write-Host ""
