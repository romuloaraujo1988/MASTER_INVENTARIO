package com.inventario.sihcp.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitária para obter informações de versão do sistema
 * Utiliza Git para capturar informações de commit, branch e versão
 */
public class VersionInfo {
    
    private static final String VERSION = "2.0.0";
    private static final String APP_NAME = "SIHCP - Sistema de Histórico e Coleta Patrimonial";
    private static String gitCommit = null;
    private static String gitBranch = null;
    private static String gitCommitDate = null;
    private static String buildDate = null;
    private static boolean gitInfoLoaded = false;
    
    /**
     * Obtém a versão do sistema
     */
    public static String getVersion() {
        return VERSION;
    }
    
    /**
     * Obtém o nome da aplicação
     */
    public static String getAppName() {
        return APP_NAME;
    }
    
    /**
     * Obtém o hash do commit Git atual
     */
    public static String getGitCommit() {
        loadGitInfo();
        return gitCommit != null ? gitCommit : "N/A";
    }
    
    /**
     * Obtém o hash curto do commit Git (7 caracteres)
     */
    public static String getGitCommitShort() {
        String commit = getGitCommit();
        if (commit != null && !commit.equals("N/A") && commit.length() >= 7) {
            return commit.substring(0, 7);
        }
        return commit;
    }
    
    /**
     * Obtém o branch Git atual
     */
    public static String getGitBranch() {
        loadGitInfo();
        return gitBranch != null ? gitBranch : "N/A";
    }
    
    /**
     * Obtém a data do último commit
     */
    public static String getGitCommitDate() {
        loadGitInfo();
        return gitCommitDate != null ? gitCommitDate : "N/A";
    }
    
    /**
     * Obtém a data de build da aplicação
     */
    public static String getBuildDate() {
        if (buildDate == null) {
            buildDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return buildDate;
    }
    
    /**
     * Obtém a versão completa com informações do Git
     */
    public static String getFullVersion() {
        return String.format("%s (commit: %s, branch: %s)", 
            VERSION, getGitCommitShort(), getGitBranch());
    }
    
    /**
     * Obtém todas as informações de versão formatadas
     */
    public static String getVersionInfo() {
        StringBuilder info = new StringBuilder();
        info.append(APP_NAME).append("\n");
        info.append("Versão: ").append(VERSION).append("\n");
        info.append("Git Commit: ").append(getGitCommit()).append("\n");
        info.append("Git Branch: ").append(getGitBranch()).append("\n");
        info.append("Data do Commit: ").append(getGitCommitDate()).append("\n");
        info.append("Data de Build: ").append(getBuildDate()).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append(" ")
            .append(System.getProperty("os.version"));
        return info.toString();
    }
    
    /**
     * Carrega informações do Git executando comandos
     */
    private static synchronized void loadGitInfo() {
        if (gitInfoLoaded) {
            return;
        }
        
        try {
            // Obter hash do commit
            gitCommit = executeGitCommand("git rev-parse HEAD");
            
            // Obter branch atual
            gitBranch = executeGitCommand("git rev-parse --abbrev-ref HEAD");
            
            // Obter data do último commit
            gitCommitDate = executeGitCommand("git log -1 --format=%cd --date=format:%d/%m/%Y %H:%M");
            
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível obter informações do Git: " + e.getMessage());
        } finally {
            gitInfoLoaded = true;
        }
    }
    
    /**
     * Executa um comando Git e retorna o resultado
     */
    @SuppressWarnings("deprecation")
    private static String executeGitCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            
            String line = reader.readLine();
            reader.close();
            
            int exitCode = process.waitFor();
            if (exitCode == 0 && line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
        } catch (Exception e) {
            // Silenciosamente falhar se Git não estiver disponível
        }
        return null;
    }
    
    /**
     * Verifica se o repositório Git tem mudanças não commitadas
     */
    public static boolean hasUncommittedChanges() {
        try {
            String status = executeGitCommand("git status --porcelain");
            return status != null && !status.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtém o número de commits à frente do remote
     */
    public static int getCommitsAhead() {
        try {
            String result = executeGitCommand("git rev-list --count @{u}..");
            if (result != null && !result.isEmpty()) {
                return Integer.parseInt(result);
            }
        } catch (Exception e) {
            // Ignorar erro
        }
        return 0;
    }
    
    /**
     * Obtém informações de versão em formato HTML para exibição
     */
    public static String getVersionInfoHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif;'>");
        html.append("<h2>").append(APP_NAME).append("</h2>");
        html.append("<table border='0' cellpadding='5'>");
        html.append("<tr><td><b>Versão:</b></td><td>").append(VERSION).append("</td></tr>");
        html.append("<tr><td><b>Git Commit:</b></td><td>").append(getGitCommitShort()).append("</td></tr>");
        html.append("<tr><td><b>Git Branch:</b></td><td>").append(getGitBranch()).append("</td></tr>");
        html.append("<tr><td><b>Data do Commit:</b></td><td>").append(getGitCommitDate()).append("</td></tr>");
        
        if (hasUncommittedChanges()) {
            html.append("<tr><td colspan='2' style='color: orange;'><i>⚠ Há mudanças não commitadas</i></td></tr>");
        }
        
        int ahead = getCommitsAhead();
        if (ahead > 0) {
            html.append("<tr><td colspan='2' style='color: blue;'><i>↑ ").append(ahead)
                .append(" commit(s) à frente do remote</i></td></tr>");
        }
        
        html.append("<tr><td><b>Java:</b></td><td>").append(System.getProperty("java.version")).append("</td></tr>");
        html.append("<tr><td><b>Sistema:</b></td><td>").append(System.getProperty("os.name"))
            .append(" ").append(System.getProperty("os.version")).append("</td></tr>");
        html.append("</table>");
        html.append("<br><p style='font-size: 10px; color: gray;'>")
            .append("Desenvolvido para Instituto Federal de Mato Grosso</p>");
        html.append("</body></html>");
        return html.toString();
    }
    
    /**
     * Método main para teste
     */
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println(getVersionInfo());
        System.out.println("=".repeat(60));
        System.out.println("\nVersão Completa: " + getFullVersion());
        System.out.println("Mudanças não commitadas: " + hasUncommittedChanges());
        System.out.println("Commits à frente: " + getCommitsAhead());
    }
}
