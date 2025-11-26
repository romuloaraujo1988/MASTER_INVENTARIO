package com.inventario.sync.model;

import java.util.ArrayList;
import java.util.List;

public class IntegrityCheckResult {
    private boolean passed;
    private int sqliteCount;
    private int postgresCount;
    private int pendingCount;
    private String checksum;
    private List<String> issues;
    
    public IntegrityCheckResult() {
        this.issues = new ArrayList<>();
    }
    
    // Getters and Setters
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public int getSqliteCount() { return sqliteCount; }
    public void setSqliteCount(int sqliteCount) { this.sqliteCount = sqliteCount; }
    public int getPostgresCount() { return postgresCount; }
    public void setPostgresCount(int postgresCount) { this.postgresCount = postgresCount; }
    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public List<String> getIssues() { return issues; }
    public void setIssues(List<String> issues) { this.issues = issues; }
    
    public void addIssue(String issue) {
        this.issues.add(issue);
        this.passed = false;
    }
}
