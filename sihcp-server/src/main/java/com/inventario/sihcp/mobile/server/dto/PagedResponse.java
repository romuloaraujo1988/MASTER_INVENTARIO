package com.inventario.sihcp.mobile.server.dto;

import java.util.List;

/**
 * DTO para resposta paginada
 * Inclui dados da página + metadados de paginação
 * 
 * @param <T> tipo dos elementos
 */
public class PagedResponse<T> {
    
    private List<T> content;       // Dados da página atual
    private int page;              // Número da página (0-based)
    private int size;              // Tamanho da página
    private long totalElements;    // Total de elementos
    private int totalPages;        // Total de páginas
    private boolean first;         // É primeira página?
    private boolean last;          // É última página?
    private boolean hasNext;       // Tem próxima página?
    private boolean hasPrevious;   // Tem página anterior?
    
    public PagedResponse() {}
    
    public PagedResponse(List<T> content, int page, int size, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        this.first = page == 0;
        this.last = page >= totalPages - 1;
        this.hasNext = !last;
        this.hasPrevious = !first;
    }
    
    /**
     * Factory method para criar resposta paginada
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        return new PagedResponse<>(content, page, size, totalElements);
    }
    
    // Getters e Setters
    
    public List<T> getContent() {
        return content;
    }
    
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public boolean isFirst() {
        return first;
    }
    
    public void setFirst(boolean first) {
        this.first = first;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
    
    public boolean isHasNext() {
        return hasNext;
    }
    
    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
