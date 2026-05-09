package com.inventario.sihcp.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para restringir acesso a coletores, supervisores ou administradores
 * Uso: @RequireColetor em métodos ou classes de controller
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR', 'COLETOR')")
public @interface RequireColetor {
}
