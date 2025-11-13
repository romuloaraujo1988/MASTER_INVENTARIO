#!/bin/bash
# Script de Desinstalação do SIHCP - Sistema de Inventário

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Variáveis
INSTALL_DIR="/opt/sihcp-inventario"
BIN_DIR="/usr/local/bin"
DESKTOP_DIR="/usr/share/applications"
ICON_DIR="/usr/share/icons/hicolor/256x256/apps"
CONFIG_DIR="$HOME/.config/sihcp-inventario"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  SIHCP - Desinstalação                                         ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verificar se está rodando como root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}✗ Este script precisa ser executado como root${NC}"
    echo -e "${YELLOW}  Execute: sudo $0${NC}"
    exit 1
fi

# Confirmar desinstalação
echo -e "${YELLOW}Tem certeza que deseja desinstalar o SIHCP? (s/n)${NC}"
read -r response

if [[ ! "$response" =~ ^[Ss]$ ]]; then
    echo -e "${BLUE}Desinstalação cancelada${NC}"
    exit 0
fi

# Perguntar sobre configurações
echo ""
echo -e "${YELLOW}Deseja remover também as configurações do banco de dados? (s/n)${NC}"
echo -e "${YELLOW}(Se você pretende reinstalar, mantenha as configurações)${NC}"
read -r remove_config

# Remover arquivos
echo ""
echo -e "${BLUE}→ Removendo arquivos...${NC}"

# Aplicação
if [ -d "$INSTALL_DIR" ]; then
    rm -rf "$INSTALL_DIR"
    echo -e "${GREEN}✓ Aplicação removida${NC}"
fi

# Launcher
if [ -f "$BIN_DIR/sihcp-inventario" ]; then
    rm -f "$BIN_DIR/sihcp-inventario"
    echo -e "${GREEN}✓ Launcher removido${NC}"
fi

# Desktop entry
if [ -f "$DESKTOP_DIR/sihcp-inventario.desktop" ]; then
    rm -f "$DESKTOP_DIR/sihcp-inventario.desktop"
    echo -e "${GREEN}✓ Entrada no menu removida${NC}"
fi

# Ícone
if [ -f "$ICON_DIR/sihcp-inventario.png" ]; then
    rm -f "$ICON_DIR/sihcp-inventario.png"
    echo -e "${GREEN}✓ Ícone removido${NC}"
fi

# Configurações (se solicitado)
if [[ "$remove_config" =~ ^[Ss]$ ]]; then
    if [ -d "$CONFIG_DIR" ]; then
        rm -rf "$CONFIG_DIR"
        echo -e "${GREEN}✓ Configurações removidas${NC}"
    fi
else
    echo -e "${YELLOW}⚠ Configurações mantidas em: $CONFIG_DIR${NC}"
fi

# Atualizar cache de ícones
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor 2>/dev/null || true
fi

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Desinstalação concluída!                                   ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
