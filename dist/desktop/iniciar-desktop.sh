#!/bin/bash
# ============================================================================
# Sistema de Inventario - Aplicacao Desktop
# IFMT - Instituto Federal de Mato Grosso
# ============================================================================

echo "Iniciando Sistema de Inventario Desktop..."
echo

# Verificar se Java esta instalado
if ! command -v java &> /dev/null; then
    echo "[ERRO] Java nao encontrado!"
    echo "Por favor, instale o Java 21 ou superior."
    exit 1
fi

# Executar aplicacao
java -jar sistema-inventario-desktop.jar

if [ $? -ne 0 ]; then
    echo
    echo "[ERRO] Falha ao iniciar a aplicacao!"
    exit 1
fi
