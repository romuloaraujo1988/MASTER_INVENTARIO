package com.inventario.util.notification;

import java.time.LocalDateTime;

/**
 * Classe para representar um relatório de erro
 */
public class ErrorReport {
    private String message;
    private String context;
    private String stackTrace;
    private LocalDateTime timestamp;
    private String username;
    private String javaVersion;
    private String osName;
    private String osVersion;
    
    public ErrorReport() {}
    
    public ErrorReport(String message, String context, Throwable exception, 
                      LocalDateTime timestamp, String username, String javaVersion) {
        this.message = message;
        this.context = context;
        this.stackTrace = exception != null ? getStackTraceString(exception) : null;
        this.timestamp = timestamp;
        this.username = username;
        this.javaVersion = javaVersion;
        this.osName = System.getProperty("os.name");
        this.osVersion = System.getProperty("os.version");
    }
    
    private String getStackTraceString(Throwable exception) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
    
    // Getters e Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getJavaVersion() { return javaVersion; }
    public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }
    
    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
}
