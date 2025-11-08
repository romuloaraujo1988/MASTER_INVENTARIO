package com.inventario.util;

import java.util.List;
import java.util.Collections;

/**
 * Classe genérica para representar uma página de resultados
 * Utilizada para implementar paginação em listagens
 * 
 * @param <T> Tipo dos elementos da página
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class Page<T> {
    
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    
    /**
     * Construtor da página
     * 
     * @param content Lista de elementos da página
     * @param pageNumber Número da página (0-based)
     * @param pageSize Tamanho da página
     * @param totalElements Total de elementos em todas as páginas
     */
    public Page(List<T> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content != null ? content : Collections.emptyList();
        this.pageNumber = Math.max(0, pageNumber);
        this.pageSize = Math.max(1, pageSize);
        this.totalElements = Math.max(0, totalElements);
        this.totalPages = (int) Math.ceil((double) this.totalElements / this.pageSize);
    }
    
    /**
     * Retorna o conteúdo da página
     */
    public List<T> getContent() {
        return content;
    }
    
    /**
     * Retorna o número da página atual (0-based)
     */
    public int getPageNumber() {
        return pageNumber;
    }
    
    /**
     * Retorna o tamanho da página
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Retorna o total de elementos em todas as páginas
     */
    public long getTotalElements() {
        return totalElements;
    }
    
    /**
     * Retorna o total de páginas
     */
    public int getTotalPages() {
        return totalPages;
    }
    
    /**
     * Retorna o número de elementos na página atual
     */
    public int getNumberOfElements() {
        return content.size();
    }
    
    /**
     * Verifica se existe uma próxima página
     */
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }
    
    /**
     * Verifica se existe uma página anterior
     */
    public boolean hasPrevious() {
        return pageNumber > 0;
    }
    
    /**
     * Verifica se é a primeira página
     */
    public boolean isFirst() {
        return pageNumber == 0;
    }
    
    /**
     * Verifica se é a última página
     */
    public boolean isLast() {
        return pageNumber >= totalPages - 1;
    }
    
    /**
     * Verifica se a página está vazia
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    /**
     * Retorna o número do primeiro elemento da página (1-based)
     */
    public long getFirstElementNumber() {
        return isEmpty() ? 0 : (pageNumber * pageSize) + 1;
    }
    
    /**
     * Retorna o número do último elemento da página (1-based)
     */
    public long getLastElementNumber() {
        return isEmpty() ? 0 : getFirstElementNumber() + getNumberOfElements() - 1;
    }
    
    @Override
    public String toString() {
        return String.format("Page %d of %d containing %d elements (total: %d)", 
            pageNumber + 1, totalPages, getNumberOfElements(), totalElements);
    }
}
