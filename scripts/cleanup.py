#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Limpeza final de duplicações"""

with open('MELHORIA_PROCESSO_COLETA_PATRIMONIAL.md', 'r', encoding='utf-8') as f:
    content = f.read()

# Remover duplicações específicas
content = content.replace(
    '> [!NOTE]\r\n> > [!NOTE]\r\n\r\n> **Resultado:**',
    '> [!NOTE]\r\n> **Resultado:'
)

with open('MELHORIA_PROCESSO_COLETA_PATRIMONIAL.md', 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Limpeza final concluída")
