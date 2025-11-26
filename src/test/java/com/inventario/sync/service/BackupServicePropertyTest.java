package com.inventario.sync.service;

import com.inventario.sync.exception.SyncException;
import com.inventario.sync.model.BackupInfo;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Property-Based Tests para BackupService
 * Feature: correcao-sincronizacao-sqlite-postgresql
 */
@RunWith(JUnitQuickcheck.class)
public class BackupServicePropertyTest {
    
    private Path testDbPath;
    private Path testBackupDir;
    private BackupService backupService;
    
    @Before
    public void setUp() throws IOException {
        // Criar diretório temporário para testes
        testBackupDir = Files.createTempDirectory("backup_test_");
        testDbPath = Files.createTempFile("test_db_", ".db");
        
        // Escrever alguns dados no arquivo de teste
        Files.write(testDbPath, "test data".getBytes());
        
        backupService = new BackupService(testBackupDir, testDbPath);
    }
    
    @After
    public void tearDown() throws IOException {
        // Limpar arquivos de teste
        if (Files.exists(testDbPath)) {
            Files.delete(testDbPath);
        }
        
        // Limpar diretório de backup
        if (Files.exists(testBackupDir)) {
            Files.walk(testBackupDir)
                .sorted((a, b) -> b.compareTo(a)) // Deletar arquivos antes de diretórios
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignorar erros de limpeza
                    }
                });
        }
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 14: Backup Antes de Modificação
     * Validates: Requirements 7.1
     * 
     * Para qualquer sincronização iniciada, um backup do SQLite deve existir 
     * antes de qualquer modificação ser feita.
     */
    @Property(trials = 100)
    public void backupDeveExistirAntesDeQualquerModificacao() throws SyncException, IOException {
        // Arrange - Limpar backups existentes
        backupService.cleanOldBackups();
        List<BackupInfo> backupsBefore = backupService.listBackups();
        
        // Act - Criar backup
        Path backupPath = backupService.createBackup();
        
        // Assert - Backup deve existir
        assertTrue("Backup deve existir no sistema de arquivos", Files.exists(backupPath));
        
        // Deve haver pelo menos 1 backup
        List<BackupInfo> backupsAfter = backupService.listBackups();
        assertTrue("Deve haver pelo menos 1 backup após criação", 
            backupsAfter.size() >= backupsBefore.size() + 1);
        
        // Backup deve ter conteúdo
        assertTrue("Backup deve ter tamanho maior que zero", 
            Files.size(backupPath) > 0);
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 15: Nomenclatura de Backup
     * Validates: Requirements 7.2
     * 
     * Para qualquer backup criado, o nome deve seguir o formato 
     * inventario_backup_yyyyMMdd_HHmmss.db.
     */
    @Property(trials = 100)
    public void backupDeveSeguirFormatoDeNomenclatura() throws SyncException {
        // Act
        Path backupPath = backupService.createBackup();
        
        // Assert
        String filename = backupPath.getFileName().toString();
        
        // Verificar formato do nome
        assertTrue("Nome deve começar com 'inventario_backup_'", 
            filename.startsWith("inventario_backup_"));
        assertTrue("Nome deve terminar com '.db'", 
            filename.endsWith(".db"));
        
        // Verificar formato do timestamp (yyyyMMdd_HHmmss)
        String timestampPart = filename
            .replace("inventario_backup_", "")
            .replace(".db", "");
        
        // Deve ter 15 caracteres (8 para data + 1 underscore + 6 para hora)
        assertEquals("Timestamp deve ter formato correto", 15, timestampPart.length());
        assertTrue("Timestamp deve conter underscore", timestampPart.contains("_"));
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 16: Limite de Backups
     * Validates: Requirements 7.4
     * 
     * Para qualquer momento no tempo, não deve haver mais de 10 backups armazenados.
     */
    @Property(trials = 50) // Menos trials pois cria muitos arquivos
    public void nuncaDeveHaverMaisDe10Backups() throws SyncException, InterruptedException {
        // Arrange - Criar 15 backups
        for (int i = 0; i < 15; i++) {
            backupService.createBackup();
            // Pequeno delay para garantir timestamps diferentes
            Thread.sleep(10);
        }
        
        // Act - Limpar backups antigos (já é chamado automaticamente no createBackup)
        backupService.cleanOldBackups();
        
        // Assert - Deve haver no máximo 10 backups
        List<BackupInfo> backups = backupService.listBackups();
        assertTrue("Deve haver no máximo 10 backups", backups.size() <= 10);
    }
    
    /**
     * Feature: correcao-sincronizacao-sqlite-postgresql, Property 17: Restauração de Backup
     * Validates: Requirements 7.6
     * 
     * Para qualquer backup criado, restaurá-lo deve resultar no SQLite 
     * idêntico ao momento do backup (round trip).
     */
    @Property(trials = 100)
    public void restauracaoDeveRecuperarEstadoOriginal() throws SyncException, IOException {
        // Arrange - Calcular checksum do estado original
        String originalChecksum = backupService.calculateChecksum(testDbPath);
        
        // Criar backup
        Path backupPath = backupService.createBackup();
        
        // Modificar arquivo original
        Files.write(testDbPath, "modified data".getBytes());
        String modifiedChecksum = backupService.calculateChecksum(testDbPath);
        
        // Verificar que foi modificado
        assertNotEquals("Arquivo deve ter sido modificado", 
            originalChecksum, modifiedChecksum);
        
        // Act - Restaurar backup
        boolean restored = backupService.restoreBackup(backupPath);
        
        // Assert - Checksum deve ser igual ao original
        assertTrue("Restauração deve ser bem-sucedida", restored);
        
        String restoredChecksum = backupService.calculateChecksum(testDbPath);
        assertEquals("Checksum deve ser igual ao original após restauração", 
            originalChecksum, restoredChecksum);
    }
}
