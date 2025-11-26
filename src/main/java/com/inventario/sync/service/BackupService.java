package com.inventario.sync.service;

import com.inventario.sync.exception.SyncErrorType;
import com.inventario.sync.exception.SyncException;
import com.inventario.sync.model.BackupInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serviço de backup automático do SQLite
 * Cria e gerencia backups antes de sincronizações
 */
public class BackupService {
    
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final int MAX_BACKUPS = 10;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    private final Path backupDirectory;
    private final Path sqliteDbPath;
    
    /**
     * Construtor com diretório de backup configurável
     * @param backupDirectory Diretório onde backups serão armazenados
     * @param sqliteDbPath Caminho do banco SQLite a fazer backup
     */
    public BackupService(Path backupDirectory, Path sqliteDbPath) {
        this.backupDirectory = backupDirectory;
        this.sqliteDbPath = sqliteDbPath;
        
        // Criar diretório de backup se não existir
        try {
            if (!Files.exists(backupDirectory)) {
                Files.createDirectories(backupDirectory);
                logger.info("Diretório de backup criado: {}", backupDirectory);
            }
        } catch (IOException e) {
            logger.error("Erro ao criar diretório de backup: {}", e.getMessage());
        }
    }
    
    /**
     * Construtor padrão usando diretório data/backups
     */
    public BackupService(Path sqliteDbPath) {
        this(Paths.get("data", "backups"), sqliteDbPath);
    }
    
    /**
     * Cria backup do SQLite antes de sincronização
     * @return Path do arquivo de backup criado
     * @throws SyncException se falhar ao criar backup
     */
    public Path createBackup() throws SyncException {
        logger.info("Iniciando criação de backup do SQLite");
        
        try {
            // Verificar se arquivo SQLite existe
            if (!Files.exists(sqliteDbPath)) {
                throw new SyncException(
                    "Arquivo SQLite não encontrado: " + sqliteDbPath,
                    SyncErrorType.BACKUP_ERROR,
                    "createBackup"
                );
            }
            
            // Gerar nome do backup com timestamp
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String backupFileName = String.format("inventario_backup_%s.db", timestamp);
            Path backupPath = backupDirectory.resolve(backupFileName);
            
            // Copiar arquivo
            Files.copy(sqliteDbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            long sizeBytes = Files.size(backupPath);
            logger.info("Backup criado com sucesso: {} ({} bytes)", backupPath, sizeBytes);
            
            // Limpar backups antigos
            cleanOldBackups();
            
            return backupPath;
            
        } catch (IOException e) {
            logger.error("Erro ao criar backup: {}", e.getMessage());
            throw new SyncException(
                "Falha ao criar backup: " + e.getMessage(),
                SyncErrorType.BACKUP_ERROR,
                "createBackup",
                e
            );
        }
    }
    
    /**
     * Restaura backup específico
     * @param backupPath Path do backup a restaurar
     * @return true se restauração foi bem-sucedida
     * @throws SyncException se falhar ao restaurar
     */
    public boolean restoreBackup(Path backupPath) throws SyncException {
        logger.info("Iniciando restauração de backup: {}", backupPath);
        
        try {
            // Verificar se backup existe
            if (!Files.exists(backupPath)) {
                throw new SyncException(
                    "Arquivo de backup não encontrado: " + backupPath,
                    SyncErrorType.BACKUP_ERROR,
                    "restoreBackup"
                );
            }
            
            // Criar backup do estado atual antes de restaurar
            Path currentBackup = backupDirectory.resolve("pre_restore_" + 
                LocalDateTime.now().format(TIMESTAMP_FORMATTER) + ".db");
            
            if (Files.exists(sqliteDbPath)) {
                Files.copy(sqliteDbPath, currentBackup, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Backup do estado atual criado: {}", currentBackup);
            }
            
            // Restaurar backup
            Files.copy(backupPath, sqliteDbPath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Backup restaurado com sucesso: {}", backupPath);
            return true;
            
        } catch (IOException e) {
            logger.error("Erro ao restaurar backup: {}", e.getMessage());
            throw new SyncException(
                "Falha ao restaurar backup: " + e.getMessage(),
                SyncErrorType.BACKUP_ERROR,
                "restoreBackup",
                e
            );
        }
    }
    
    /**
     * Lista backups disponíveis ordenados por data (mais recente primeiro)
     * @return Lista de backups
     */
    public List<BackupInfo> listBackups() {
        logger.debug("Listando backups disponíveis");
        
        List<BackupInfo> backups = new ArrayList<>();
        
        try (Stream<Path> paths = Files.list(backupDirectory)) {
            backups = paths
                .filter(path -> path.toString().endsWith(".db"))
                .filter(path -> path.getFileName().toString().startsWith("inventario_backup_"))
                .map(this::createBackupInfo)
                .sorted(Comparator.comparing(BackupInfo::getCreatedAt).reversed())
                .collect(Collectors.toList());
            
            logger.debug("Encontrados {} backups", backups.size());
            
        } catch (IOException e) {
            logger.error("Erro ao listar backups: {}", e.getMessage());
        }
        
        return backups;
    }
    
    /**
     * Remove backups antigos mantendo apenas os últimos MAX_BACKUPS
     */
    public void cleanOldBackups() {
        logger.debug("Limpando backups antigos (mantendo últimos {})", MAX_BACKUPS);
        
        List<BackupInfo> backups = listBackups();
        
        if (backups.size() <= MAX_BACKUPS) {
            logger.debug("Número de backups ({}) dentro do limite", backups.size());
            return;
        }
        
        // Remover backups excedentes (os mais antigos)
        List<BackupInfo> toRemove = backups.subList(MAX_BACKUPS, backups.size());
        
        for (BackupInfo backup : toRemove) {
            try {
                Files.delete(backup.getFilePath());
                logger.info("Backup antigo removido: {}", backup.getFilePath());
            } catch (IOException e) {
                logger.warn("Erro ao remover backup antigo: {}", e.getMessage());
            }
        }
        
        logger.info("Limpeza concluída: {} backups removidos", toRemove.size());
    }
    
    /**
     * Calcula checksum MD5 de um arquivo
     * @param filePath Path do arquivo
     * @return Checksum MD5 em hexadecimal
     */
    public String calculateChecksum(Path filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] digest = md.digest(fileBytes);
            
            // Converter para hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Erro ao calcular checksum: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Cria BackupInfo a partir de um Path
     */
    private BackupInfo createBackupInfo(Path path) {
        try {
            long sizeBytes = Files.size(path);
            LocalDateTime createdAt = extractTimestampFromFilename(path.getFileName().toString());
            
            BackupInfo info = new BackupInfo(path, createdAt, sizeBytes);
            
            // Calcular checksum (pode ser lento para arquivos grandes)
            // info.setChecksum(calculateChecksum(path));
            
            return info;
            
        } catch (IOException e) {
            logger.warn("Erro ao criar BackupInfo para {}: {}", path, e.getMessage());
            return new BackupInfo(path, LocalDateTime.now(), 0);
        }
    }
    
    /**
     * Extrai timestamp do nome do arquivo de backup
     * Formato esperado: inventario_backup_yyyyMMdd_HHmmss.db
     */
    private LocalDateTime extractTimestampFromFilename(String filename) {
        try {
            // Remover prefixo e sufixo
            String timestampStr = filename
                .replace("inventario_backup_", "")
                .replace(".db", "");
            
            return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
            
        } catch (Exception e) {
            logger.warn("Erro ao extrair timestamp de {}: {}", filename, e.getMessage());
            return LocalDateTime.now();
        }
    }
    
    /**
     * Verifica se há espaço suficiente em disco para criar backup
     * @return true se há espaço suficiente
     */
    public boolean hasEnoughDiskSpace() {
        try {
            long dbSize = Files.size(sqliteDbPath);
            long freeSpace = Files.getFileStore(backupDirectory).getUsableSpace();
            
            // Verificar se há pelo menos 2x o tamanho do DB disponível
            boolean hasSpace = freeSpace >= (dbSize * 2);
            
            if (!hasSpace) {
                logger.warn("Espaço em disco insuficiente. Necessário: {} bytes, Disponível: {} bytes",
                    dbSize * 2, freeSpace);
            }
            
            return hasSpace;
            
        } catch (IOException e) {
            logger.error("Erro ao verificar espaço em disco: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtém informações sobre o backup mais recente
     * @return BackupInfo do backup mais recente ou null se não houver backups
     */
    public BackupInfo getLatestBackup() {
        List<BackupInfo> backups = listBackups();
        return backups.isEmpty() ? null : backups.get(0);
    }
    
    /**
     * Verifica integridade de um backup comparando com checksum original
     * @param backupPath Path do backup a verificar
     * @param originalChecksum Checksum original para comparação
     * @return true se backup está íntegro
     */
    public boolean verifyBackupIntegrity(Path backupPath, String originalChecksum) {
        if (originalChecksum == null) {
            logger.warn("Checksum original não fornecido, pulando verificação");
            return true;
        }
        
        String backupChecksum = calculateChecksum(backupPath);
        boolean isValid = originalChecksum.equals(backupChecksum);
        
        if (!isValid) {
            logger.error("Backup corrompido! Checksum não coincide. Original: {}, Backup: {}",
                originalChecksum, backupChecksum);
        }
        
        return isValid;
    }
}
