package com.inventario.sihcp.util.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço para coleta e armazenamento de relatórios de erro
 */
public class ErrorReportingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorReportingService.class);
    private static final String ERROR_REPORTS_DIR = "error_reports";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Salva relatório de erro localmente
     */
    public static void saveErrorReport(ErrorReport report) {
        try {
            // Criar diretório se não existir
            Path reportsDir = Paths.get(ERROR_REPORTS_DIR);
            if (!Files.exists(reportsDir)) {
                Files.createDirectories(reportsDir);
            }
            
            // Nome do arquivo com timestamp
            String fileName = String.format("error_%s.json", 
                report.getTimestamp().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path reportFile = reportsDir.resolve(fileName);
            
            // Salvar como JSON
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(reportFile.toFile(), report);
            
            logger.info("Relatório de erro salvo: {}", reportFile);
            
        } catch (IOException e) {
            logger.error("Erro ao salvar relatório de erro", e);
        }
    }
    
    /**
     * Lista todos os relatórios de erro
     */
    public static List<ErrorReport> listErrorReports() {
        List<ErrorReport> reports = new ArrayList<>();
        
        try {
            Path reportsDir = Paths.get(ERROR_REPORTS_DIR);
            if (!Files.exists(reportsDir)) {
                return reports;
            }
            
            Files.list(reportsDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        ErrorReport report = objectMapper.readValue(path.toFile(), ErrorReport.class);
                        reports.add(report);
                    } catch (IOException e) {
                        logger.warn("Erro ao ler relatório: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            logger.error("Erro ao listar relatórios de erro", e);
        }
        
        return reports;
    }
    
    /**
     * Limpa relatórios antigos (mais de 30 dias)
     */
    public static void cleanOldReports() {
        try {
            Path reportsDir = Paths.get(ERROR_REPORTS_DIR);
            if (!Files.exists(reportsDir)) {
                return;
            }
            
            LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
            
            Files.list(reportsDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        ErrorReport report = objectMapper.readValue(path.toFile(), ErrorReport.class);
                        if (report.getTimestamp().isBefore(cutoff)) {
                            Files.delete(path);
                            logger.info("Relatório antigo removido: {}", path);
                        }
                    } catch (IOException e) {
                        logger.warn("Erro ao processar relatório: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            logger.error("Erro ao limpar relatórios antigos", e);
        }
    }
}
