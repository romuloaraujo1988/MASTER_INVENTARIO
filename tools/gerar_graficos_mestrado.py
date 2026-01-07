#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gerador de Gráficos para Dissertação de Mestrado
Sistema SIHCP - Inventário Patrimonial IFMT
Autor: Gerado automaticamente
Data: 03/01/2026
"""

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np
from datetime import datetime
import os

# Configurações globais
plt.rcParams['font.family'] = 'DejaVu Sans'
plt.rcParams['font.size'] = 10
plt.rcParams['axes.titlesize'] = 12
plt.rcParams['axes.labelsize'] = 10
plt.rcParams['figure.dpi'] = 150

# Cores institucionais IFMT
CORES = {
    'verde_ifmt': '#006633',
    'verde_claro': '#00994C',
    'azul': '#0066CC',
    'laranja': '#FF6600',
    'vermelho': '#CC0000',
    'cinza': '#666666',
    'cinza_claro': '#CCCCCC'
}

OUTPUT_DIR = 'images'
os.makedirs(OUTPUT_DIR, exist_ok=True)


def grafico_evolucao_diaria():
    """Gráfico 1: Evolução diária das coletas"""
    # Dados reais do banco PostgreSQL
    dados = [
        ('28/11', 1709),
        ('27/11', 1099),
        ('02/12', 1021),
        ('26/11', 988),
        ('25/11', 823),
        ('23/12', 754),
        ('04/12', 298),
        ('03/12', 294),
        ('15/12', 247),
        ('09/12', 215),
        ('01/12', 198),
        ('14/12', 168),
        ('08/12', 125),
        ('29/11', 117),
        ('06/12', 77)
    ]
    
    dias = [d[0] for d in dados]
    coletas = [d[1] for d in dados]
    
    fig, ax = plt.subplots(figsize=(12, 6))
    
    bars = ax.bar(dias, coletas, color=CORES['verde_ifmt'], edgecolor='white', linewidth=0.5)
    
    # Destacar o dia de pico
    bars[0].set_color(CORES['laranja'])
    
    # Adicionar valores nas barras
    for bar, val in zip(bars, coletas):
        height = bar.get_height()
        ax.annotate(f'{val:,}'.replace(',', '.'),
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 3), textcoords="offset points",
                    ha='center', va='bottom', fontsize=8, fontweight='bold')
    
    ax.set_xlabel('Data', fontweight='bold')
    ax.set_ylabel('Quantidade de Coletas', fontweight='bold')
    ax.set_title('Evolução Diária das Coletas - Inventário 2025\n(Top 15 dias mais produtivos)', 
                 fontweight='bold', fontsize=14)
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.set_ylim(0, max(coletas) * 1.15)
    
    # Linha de média
    media = sum(coletas) / len(coletas)
    ax.axhline(y=media, color=CORES['vermelho'], linestyle='--', linewidth=1.5, label=f'Média: {media:.0f}')
    ax.legend(loc='upper right')
    
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_evolucao_diaria.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 1: Evolução diária salvo")


def grafico_estados_conservacao():
    """Gráfico 2: Pizza - Estados de conservação"""
    # Dados consolidados (IRRECUPERAVEL + IRRECUPERÁVEL, RECUPERAVEL + RECUPERÁVEL)
    # Atualizado em 03/01/2026 - Estado 'COLETADO' corrigido
    estados = ['BOM', 'IRRECUPERÁVEL', 'PENDENTE', 'N/A', 'OCIOSO', 'RECUPERÁVEL']
    quantidades = [7061, 1130, 81, 59, 30, 23]  # Total: 8.384
    
    cores = [CORES['verde_ifmt'], CORES['vermelho'], CORES['laranja'], 
             CORES['cinza'], '#9966CC', '#FFCC00']
    
    fig, ax = plt.subplots(figsize=(10, 8))
    
    # Explodir a fatia "BOM" para destaque
    explode = (0.05, 0.02, 0, 0, 0, 0)
    
    wedges, texts, autotexts = ax.pie(quantidades, labels=estados, autopct='%1.1f%%',
                                       colors=cores, explode=explode, startangle=90,
                                       pctdistance=0.75, labeldistance=1.1)
    
    # Estilizar textos
    for autotext in autotexts:
        autotext.set_fontsize(9)
        autotext.set_fontweight('bold')
    
    ax.set_title('Distribuição por Estado de Conservação\nInventário 2025 - 8.384 coletas', 
                 fontweight='bold', fontsize=14)
    
    # Legenda com quantidades
    legend_labels = [f'{e}: {q:,}'.replace(',', '.') for e, q in zip(estados, quantidades)]
    ax.legend(wedges, legend_labels, title="Estados", loc="center left", 
              bbox_to_anchor=(1, 0, 0.5, 1))
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_estados_conservacao.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 2: Estados de conservação salvo")


def grafico_dia_semana():
    """Gráfico 3: Coletas por dia da semana"""
    dias = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']
    coletas = [168, 616, 2882, 1286, 1474, 1764, 194]
    
    fig, ax = plt.subplots(figsize=(10, 6))
    
    cores_barras = [CORES['cinza_claro']] * 7
    cores_barras[2] = CORES['verde_ifmt']  # Terça - destaque
    cores_barras[5] = CORES['verde_claro']  # Sexta - segundo lugar
    
    bars = ax.bar(dias, coletas, color=cores_barras, edgecolor='white', linewidth=1)
    
    for bar, val in zip(bars, coletas):
        height = bar.get_height()
        ax.annotate(f'{val:,}'.replace(',', '.'),
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 3), textcoords="offset points",
                    ha='center', va='bottom', fontsize=10, fontweight='bold')
    
    ax.set_xlabel('Dia da Semana', fontweight='bold')
    ax.set_ylabel('Quantidade de Coletas', fontweight='bold')
    ax.set_title('Distribuição de Coletas por Dia da Semana\nInventário 2025', 
                 fontweight='bold', fontsize=14)
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    ax.set_ylim(0, max(coletas) * 1.15)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_dia_semana.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 3: Dia da semana salvo")


def grafico_hora_dia():
    """Gráfico 4: Coletas por hora do dia"""
    horas = list(range(6, 19))  # 6h às 18h (horário comercial ajustado)
    # Dados ajustados para horário local (UTC-4)
    coletas_utc = {0: 17, 2: 45, 3: 711, 6: 77, 7: 269, 8: 1300, 9: 1551, 
                   10: 1293, 11: 1017, 12: 382, 13: 170, 14: 307, 15: 587, 
                   16: 479, 17: 78, 18: 24}
    
    # Ajustar para horário local (UTC-4 -> horário local)
    coletas = []
    for h in horas:
        utc_h = (h + 4) % 24  # Converter para UTC
        coletas.append(coletas_utc.get(utc_h, 0))
    
    fig, ax = plt.subplots(figsize=(12, 6))
    
    # Gradiente de cores baseado no valor
    max_val = max(coletas)
    cores = [plt.cm.Greens(0.3 + 0.7 * (v / max_val)) for v in coletas]
    
    bars = ax.bar([f'{h}h' for h in horas], coletas, color=cores, edgecolor='white')
    
    for bar, val in zip(bars, coletas):
        if val > 100:
            height = bar.get_height()
            ax.annotate(f'{val}',
                        xy=(bar.get_x() + bar.get_width() / 2, height),
                        xytext=(0, 3), textcoords="offset points",
                        ha='center', va='bottom', fontsize=8)
    
    ax.set_xlabel('Hora do Dia (Horário Local)', fontweight='bold')
    ax.set_ylabel('Quantidade de Coletas', fontweight='bold')
    ax.set_title('Distribuição de Coletas por Hora do Dia\nInventário 2025 - Pico às 9h', 
                 fontweight='bold', fontsize=14)
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Destacar período de pico (8h-12h)
    ax.axvspan(1.5, 5.5, alpha=0.2, color=CORES['verde_ifmt'], label='Período de pico')
    ax.legend(loc='upper right')
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_hora_dia.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 4: Hora do dia salvo")


def grafico_top_locais():
    """Gráfico 5: Top 10 locais com mais coletas"""
    locais = ['BIBLIOTECA', 'Sala Desfazimento', 'Desf. (Antigo)', 'AUDITÓRIO', 
              'SALA DE PROF.', 'HANGAR', 'ALMOXARIFADO 1', 'LAB. INF. A9', 
              'LAB. INF. A10', 'DAP']
    coletas = [4165, 709, 433, 373, 193, 171, 140, 134, 128, 119]
    divergencias = [60, 0, 429, 318, 0, 22, 0, 0, 65, 0]
    
    fig, ax = plt.subplots(figsize=(12, 7))
    
    y_pos = np.arange(len(locais))
    
    # Barras de coletas
    bars1 = ax.barh(y_pos, coletas, color=CORES['verde_ifmt'], label='Coletas', height=0.6)
    
    # Barras de divergências (sobrepostas)
    bars2 = ax.barh(y_pos, divergencias, color=CORES['vermelho'], label='Divergências', 
                    height=0.6, alpha=0.7)
    
    ax.set_yticks(y_pos)
    ax.set_yticklabels(locais)
    ax.invert_yaxis()
    
    ax.set_xlabel('Quantidade', fontweight='bold')
    ax.set_title('Top 10 Locais com Mais Coletas\nInventário 2025 - Divergências em destaque', 
                 fontweight='bold', fontsize=14)
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Adicionar valores
    for i, (c, d) in enumerate(zip(coletas, divergencias)):
        ax.annotate(f'{c:,}'.replace(',', '.'), xy=(c + 50, i), va='center', fontsize=9)
        if d > 0:
            pct = d / c * 100
            ax.annotate(f'({pct:.0f}% div)', xy=(c + 350, i), va='center', 
                       fontsize=8, color=CORES['vermelho'])
    
    ax.legend(loc='lower right')
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_top_locais.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 5: Top locais salvo")


def grafico_progresso_inventario():
    """Gráfico 6: Progresso do inventário (gauge/donut)"""
    coletados = 8287
    pendentes = 2783
    total = coletados + pendentes
    percentual = coletados / total * 100
    
    fig, ax = plt.subplots(figsize=(8, 8))
    
    # Donut chart
    sizes = [coletados, pendentes]
    colors = [CORES['verde_ifmt'], CORES['cinza_claro']]
    
    wedges, texts = ax.pie(sizes, colors=colors, startangle=90,
                           wedgeprops=dict(width=0.4, edgecolor='white'))
    
    # Texto central
    ax.text(0, 0.1, f'{percentual:.1f}%', ha='center', va='center', 
            fontsize=36, fontweight='bold', color=CORES['verde_ifmt'])
    ax.text(0, -0.15, 'Concluído', ha='center', va='center', 
            fontsize=14, color=CORES['cinza'])
    
    ax.set_title('Progresso do Inventário 2025\nIFMT Campus Primavera do Leste', 
                 fontweight='bold', fontsize=14, pad=20)
    
    # Legenda
    legend_labels = [f'Coletados: {coletados:,}'.replace(',', '.'),
                     f'Pendentes: {pendentes:,}'.replace(',', '.')]
    ax.legend(wedges, legend_labels, loc='lower center', bbox_to_anchor=(0.5, -0.05))
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_progresso_inventario.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 6: Progresso do inventário salvo")


def grafico_comparativo_as_is_to_be():
    """Gráfico 7: Comparativo AS-IS vs TO-BE"""
    metricas = ['Tempo/patrimônio\n(minutos)', 'Taxa de erro\n(%)', 
                'Handoffs', 'Atividades\nmanuais', 'Pontos de\nretrabalho']
    as_is = [6.5, 15, 3, 8, 2]
    to_be = [0.11, 1, 1, 3, 0]  # 6.7 seg = 0.11 min
    
    x = np.arange(len(metricas))
    width = 0.35
    
    fig, ax = plt.subplots(figsize=(12, 6))
    
    bars1 = ax.bar(x - width/2, as_is, width, label='AS-IS (Manual)', 
                   color=CORES['vermelho'], alpha=0.8)
    bars2 = ax.bar(x + width/2, to_be, width, label='TO-BE (SIHCP)', 
                   color=CORES['verde_ifmt'])
    
    ax.set_ylabel('Valor', fontweight='bold')
    ax.set_title('Comparativo: Processo Manual vs Sistema SIHCP\nMelhoria de Processo - Inventário Patrimonial', 
                 fontweight='bold', fontsize=14)
    ax.set_xticks(x)
    ax.set_xticklabels(metricas)
    ax.legend()
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    # Adicionar valores e percentual de melhoria
    for i, (a, t) in enumerate(zip(as_is, to_be)):
        ax.annotate(f'{a}', xy=(i - width/2, a), xytext=(0, 3), 
                   textcoords="offset points", ha='center', fontsize=9)
        ax.annotate(f'{t}', xy=(i + width/2, t), xytext=(0, 3), 
                   textcoords="offset points", ha='center', fontsize=9)
        if a > 0:
            melhoria = ((a - t) / a) * 100
            ax.annotate(f'-{melhoria:.0f}%', xy=(i, max(a, t) + 0.5), 
                       ha='center', fontsize=8, color=CORES['verde_ifmt'], fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_comparativo_as_is_to_be.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 7: Comparativo AS-IS vs TO-BE salvo")


def diagrama_fluxo_bpm():
    """Diagrama 1: Fluxo BPM simplificado"""
    fig, ax = plt.subplots(figsize=(14, 8))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 8)
    ax.axis('off')
    
    # Título
    ax.text(7, 7.5, 'Fluxo do Processo de Coleta - BPMN Simplificado', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Cores
    cor_inicio = '#00CC66'
    cor_atividade = CORES['verde_ifmt']
    cor_gateway = '#FFD700'
    cor_fim = '#CC0000'
    
    # Elementos do fluxo
    elementos = [
        {'tipo': 'inicio', 'x': 1, 'y': 4, 'texto': 'Início'},
        {'tipo': 'atividade', 'x': 3, 'y': 4, 'texto': 'Escanear\nCódigo'},
        {'tipo': 'gateway', 'x': 5, 'y': 4, 'texto': '?'},
        {'tipo': 'atividade', 'x': 7, 'y': 5.5, 'texto': 'Validar\nDados'},
        {'tipo': 'atividade', 'x': 7, 'y': 2.5, 'texto': 'Registrar\nDivergência'},
        {'tipo': 'atividade', 'x': 9.5, 'y': 4, 'texto': 'Confirmar\nColeta'},
        {'tipo': 'atividade', 'x': 11.5, 'y': 4, 'texto': 'Sincronizar'},
        {'tipo': 'fim', 'x': 13, 'y': 4, 'texto': 'Fim'},
    ]
    
    for elem in elementos:
        x, y = elem['x'], elem['y']
        if elem['tipo'] == 'inicio':
            circle = plt.Circle((x, y), 0.3, color=cor_inicio, ec='black', lw=2)
            ax.add_patch(circle)
        elif elem['tipo'] == 'fim':
            circle = plt.Circle((x, y), 0.3, color=cor_fim, ec='black', lw=2)
            ax.add_patch(circle)
        elif elem['tipo'] == 'gateway':
            diamond = mpatches.RegularPolygon((x, y), numVertices=4, radius=0.4, 
                                               orientation=np.pi/4, color=cor_gateway, ec='black', lw=2)
            ax.add_patch(diamond)
            ax.text(x, y, elem['texto'], ha='center', va='center', fontsize=12, fontweight='bold')
        else:
            rect = mpatches.FancyBboxPatch((x-0.7, y-0.5), 1.4, 1, 
                                            boxstyle="round,pad=0.05", 
                                            facecolor=cor_atividade, edgecolor='black', lw=2)
            ax.add_patch(rect)
            ax.text(x, y, elem['texto'], ha='center', va='center', 
                   fontsize=9, color='white', fontweight='bold')
    
    # Setas
    setas = [
        (1.3, 4, 1.6, 4),      # Início -> Escanear
        (3.7, 4, 4.6, 4),      # Escanear -> Gateway
        (5.4, 4.3, 6.3, 5.2),  # Gateway -> Validar (sim)
        (5.4, 3.7, 6.3, 2.8),  # Gateway -> Divergência (não)
        (7.7, 5.2, 8.8, 4.3),  # Validar -> Confirmar
        (7.7, 2.8, 8.8, 3.7),  # Divergência -> Confirmar
        (10.2, 4, 10.8, 4),    # Confirmar -> Sincronizar
        (12.2, 4, 12.7, 4),    # Sincronizar -> Fim
    ]
    
    for x1, y1, x2, y2 in setas:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                   arrowprops=dict(arrowstyle='->', color='black', lw=1.5))
    
    # Labels nas setas do gateway
    ax.text(5.8, 5, 'Encontrado', fontsize=8, color=CORES['verde_ifmt'])
    ax.text(5.8, 3, 'Não encontrado', fontsize=8, color=CORES['vermelho'])
    
    # Legenda
    ax.text(1, 1, '● Início/Fim    ◇ Gateway (Decisão)    ▢ Atividade', 
            fontsize=10, color=CORES['cinza'])
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_fluxo_bpm.png', dpi=150, bbox_inches='tight', 
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 1: Fluxo BPM salvo")


def diagrama_arquitetura_sistema():
    """Diagrama 2: Arquitetura do Sistema SIHCP"""
    fig, ax = plt.subplots(figsize=(14, 10))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 10)
    ax.axis('off')
    
    # Título
    ax.text(7, 9.5, 'Arquitetura do Sistema SIHCP', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Cores por camada
    cor_mobile = '#4CAF50'
    cor_api = '#2196F3'
    cor_desktop = '#FF9800'
    cor_db = '#9C27B0'
    cor_externo = '#607D8B'
    
    # Camada Mobile (topo)
    rect = mpatches.FancyBboxPatch((1, 7), 5, 1.8, boxstyle="round,pad=0.1",
                                    facecolor=cor_mobile, alpha=0.3, edgecolor=cor_mobile, lw=2)
    ax.add_patch(rect)
    ax.text(3.5, 8.5, '[MOBILE] App Android', ha='center', fontsize=11, fontweight='bold')
    ax.text(3.5, 7.8, 'Kotlin - Room - Retrofit', ha='center', fontsize=9, color=CORES['cinza'])
    ax.text(3.5, 7.3, 'Coleta em Campo - Offline-First', ha='center', fontsize=9)
    
    # Camada API (meio)
    rect = mpatches.FancyBboxPatch((4, 4.5), 6, 1.8, boxstyle="round,pad=0.1",
                                    facecolor=cor_api, alpha=0.3, edgecolor=cor_api, lw=2)
    ax.add_patch(rect)
    ax.text(7, 6, '[API] REST Spring Boot', ha='center', fontsize=11, fontweight='bold')
    ax.text(7, 5.3, 'Java 21 - JWT - WebSocket', ha='center', fontsize=9, color=CORES['cinza'])
    ax.text(7, 4.8, 'Autenticacao - Sincronizacao - Validacao', ha='center', fontsize=9)
    
    # Camada Desktop (direita)
    rect = mpatches.FancyBboxPatch((8, 7), 5, 1.8, boxstyle="round,pad=0.1",
                                    facecolor=cor_desktop, alpha=0.3, edgecolor=cor_desktop, lw=2)
    ax.add_patch(rect)
    ax.text(10.5, 8.5, '[DESKTOP] Java Swing', ha='center', fontsize=11, fontweight='bold')
    ax.text(10.5, 7.8, 'Java 21 - Spring Boot - JPA', ha='center', fontsize=9, color=CORES['cinza'])
    ax.text(10.5, 7.3, 'Gestao - Relatorios - Exportacao', ha='center', fontsize=9)
    
    # Banco de Dados (centro-baixo)
    rect = mpatches.FancyBboxPatch((4, 2), 6, 1.5, boxstyle="round,pad=0.1",
                                    facecolor=cor_db, alpha=0.3, edgecolor=cor_db, lw=2)
    ax.add_patch(rect)
    ax.text(7, 3.2, '[DB] PostgreSQL', ha='center', fontsize=11, fontweight='bold')
    ax.text(7, 2.5, 'Patrimonios - Coletas - Usuarios', ha='center', fontsize=9)
    
    # Sistemas Externos (esquerda-baixo)
    rect = mpatches.FancyBboxPatch((0.5, 2), 2.5, 1.5, boxstyle="round,pad=0.1",
                                    facecolor=cor_externo, alpha=0.3, edgecolor=cor_externo, lw=2)
    ax.add_patch(rect)
    ax.text(1.75, 3.2, 'SUAP', ha='center', fontsize=10, fontweight='bold')
    ax.text(1.75, 2.5, '(Importacao)', ha='center', fontsize=9)
    
    rect = mpatches.FancyBboxPatch((11, 2), 2.5, 1.5, boxstyle="round,pad=0.1",
                                    facecolor=cor_externo, alpha=0.3, edgecolor=cor_externo, lw=2)
    ax.add_patch(rect)
    ax.text(12.25, 3.2, 'SIADS', ha='center', fontsize=10, fontweight='bold')
    ax.text(12.25, 2.5, '(Exportacao)', ha='center', fontsize=9)
    
    # Setas de conexão
    # Mobile -> API
    ax.annotate('', xy=(5.5, 6.3), xytext=(3.5, 7),
               arrowprops=dict(arrowstyle='<->', color=cor_mobile, lw=2))
    ax.text(4.2, 6.8, 'REST/JSON', fontsize=8, rotation=45)
    
    # Desktop -> API
    ax.annotate('', xy=(8.5, 6.3), xytext=(10.5, 7),
               arrowprops=dict(arrowstyle='<->', color=cor_desktop, lw=2))
    
    # API -> DB
    ax.annotate('', xy=(7, 3.5), xytext=(7, 4.5),
               arrowprops=dict(arrowstyle='<->', color=cor_db, lw=2))
    ax.text(7.2, 4, 'JPA/JDBC', fontsize=8)
    
    # SUAP -> DB
    ax.annotate('', xy=(4, 2.75), xytext=(3, 2.75),
               arrowprops=dict(arrowstyle='->', color=cor_externo, lw=1.5))
    
    # DB -> SIADS
    ax.annotate('', xy=(11, 2.75), xytext=(10, 2.75),
               arrowprops=dict(arrowstyle='->', color=cor_externo, lw=1.5))
    
    # Legenda
    ax.text(0.5, 0.5, 'Fluxo: SUAP (dados cadastrais) → SIHCP (coleta em campo) → SIADS (registro oficial)', 
            fontsize=10, style='italic', color=CORES['cinza'])
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_arquitetura_sistema.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 2: Arquitetura do sistema salvo")


def diagrama_hierarquia_processos():
    """Diagrama 3: Hierarquia de Processos BPM"""
    fig, ax = plt.subplots(figsize=(12, 8))
    ax.set_xlim(0, 12)
    ax.set_ylim(0, 8)
    ax.axis('off')
    
    # Título
    ax.text(6, 7.5, 'Hierarquia de Processos - Abordagem BPM', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Nível 1 - Macroprocesso
    rect = mpatches.FancyBboxPatch((2, 6), 8, 1, boxstyle="round,pad=0.1",
                                    facecolor='#E3F2FD', edgecolor='#1976D2', lw=2)
    ax.add_patch(rect)
    ax.text(6, 6.5, '1. MACROPROCESSO: Gestão Patrimonial do IFMT', 
            ha='center', fontsize=11, fontweight='bold', color='#1976D2')
    
    # Nível 2 - Processo
    rect = mpatches.FancyBboxPatch((2.5, 4.2), 7, 1, boxstyle="round,pad=0.1",
                                    facecolor='#E8F5E9', edgecolor='#388E3C', lw=2)
    ax.add_patch(rect)
    ax.text(6, 4.7, '2. PROCESSO: Inventário Físico Anual', 
            ha='center', fontsize=11, fontweight='bold', color='#388E3C')
    ax.text(6, 4.3, '(Foco do Estudo)', ha='center', fontsize=9, style='italic')
    
    # Nível 3 - Subprocesso
    rect = mpatches.FancyBboxPatch((3, 2.4), 6, 1, boxstyle="round,pad=0.1",
                                    facecolor='#FFF3E0', edgecolor='#F57C00', lw=3)
    ax.add_patch(rect)
    ax.text(6, 2.9, '3. SUBPROCESSO: Coleta em Campo', 
            ha='center', fontsize=11, fontweight='bold', color='#F57C00')
    ax.text(6, 2.5, '(Escopo do SIHCP)', ha='center', fontsize=9, style='italic')
    
    # Setas
    ax.annotate('', xy=(6, 5.2), xytext=(6, 6),
               arrowprops=dict(arrowstyle='->', color='#666', lw=2))
    ax.annotate('', xy=(6, 3.4), xytext=(6, 4.2),
               arrowprops=dict(arrowstyle='->', color='#666', lw=2))
    
    # Processos relacionados (nível 2)
    processos = ['Aquisição', 'Recebimento', 'Movimentação', 'INVENTÁRIO', 'Baixa']
    for i, proc in enumerate(processos):
        x = 1.5 + i * 2.2
        cor = '#388E3C' if proc == 'INVENTÁRIO' else '#90A4AE'
        peso = 'bold' if proc == 'INVENTÁRIO' else 'normal'
        ax.text(x, 5.5, proc, ha='center', fontsize=9, fontweight=peso, color=cor)
        if i < len(processos) - 1:
            ax.annotate('', xy=(x + 1.5, 5.5), xytext=(x + 0.7, 5.5),
                       arrowprops=dict(arrowstyle='->', color='#CCC', lw=1))
    
    # Subprocessos do inventário
    subprocs = ['Coleta', 'Consolidação', 'Exportação']
    for i, sub in enumerate(subprocs):
        x = 3 + i * 2
        cor = '#F57C00' if sub == 'Coleta' else '#90A4AE'
        peso = 'bold' if sub == 'Coleta' else 'normal'
        ax.text(x, 3.7, sub, ha='center', fontsize=9, fontweight=peso, color=cor)
        if i < len(subprocs) - 1:
            ax.annotate('', xy=(x + 1.3, 3.7), xytext=(x + 0.5, 3.7),
                       arrowprops=dict(arrowstyle='->', color='#CCC', lw=1))
    
    # Nota explicativa
    ax.text(6, 1.5, '▶ O SIHCP atua especificamente no subprocesso de Coleta em Campo,', 
            ha='center', fontsize=10)
    ax.text(6, 1.1, 'otimizando a captura de dados antes do registro oficial no SIADS.', 
            ha='center', fontsize=10)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_hierarquia_processos.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 3: Hierarquia de processos salvo")


def diagrama_triangulacao():
    """Diagrama 4: Triangulação de Dados (metodologia)"""
    fig, ax = plt.subplots(figsize=(10, 8))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 8)
    ax.axis('off')
    
    # Título
    ax.text(5, 7.5, 'Triangulacao de Dados - Validacao Cientifica', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Triângulo central
    triangle = plt.Polygon([(5, 5.5), (2.5, 2), (7.5, 2)], 
                           fill=False, edgecolor=CORES['verde_ifmt'], lw=3)
    ax.add_patch(triangle)
    
    # Vértice 1 - Métricas do Sistema (topo)
    circle = plt.Circle((5, 5.5), 0.8, color='#E3F2FD', ec='#1976D2', lw=2)
    ax.add_patch(circle)
    ax.text(5, 5.5, 'M', ha='center', va='center', fontsize=20, fontweight='bold', color='#1976D2')
    ax.text(5, 6.6, 'Metricas do\nSistema', ha='center', fontsize=10, fontweight='bold')
    ax.text(5, 4.3, '- Tempo de coleta\n- Taxa de erro\n- Produtividade', 
            ha='center', fontsize=8, color=CORES['cinza'])
    
    # Vértice 2 - Questionários (esquerda)
    circle = plt.Circle((2.5, 2), 0.8, color='#E8F5E9', ec='#388E3C', lw=2)
    ax.add_patch(circle)
    ax.text(2.5, 2, 'Q', ha='center', va='center', fontsize=20, fontweight='bold', color='#388E3C')
    ax.text(2.5, 0.8, 'Questionarios', ha='center', fontsize=10, fontweight='bold')
    ax.text(2.5, 3.2, '- Satisfacao\n- Facilidade\n- Comparacao', 
            ha='center', fontsize=8, color=CORES['cinza'])
    
    # Vértice 3 - Observação (direita)
    circle = plt.Circle((7.5, 2), 0.8, color='#FFF3E0', ec='#F57C00', lw=2)
    ax.add_patch(circle)
    ax.text(7.5, 2, 'O', ha='center', va='center', fontsize=20, fontweight='bold', color='#F57C00')
    ax.text(7.5, 0.8, 'Observacao\nde Campo', ha='center', fontsize=10, fontweight='bold')
    ax.text(7.5, 3.2, '- Comportamento\n- Dificuldades\n- Adocao', 
            ha='center', fontsize=8, color=CORES['cinza'])
    
    # Centro - Validação
    circle = plt.Circle((5, 3.5), 0.6, color=CORES['verde_ifmt'], ec='white', lw=2)
    ax.add_patch(circle)
    ax.text(5, 3.5, 'V', ha='center', va='center', fontsize=20, color='white', fontweight='bold')
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_triangulacao.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("  Diagrama 4: Triangulacao salvo")


def grafico_economia_tempo():
    """Gráfico 8: Economia de tempo (antes vs depois)"""
    fig, ax = plt.subplots(figsize=(10, 6))
    
    categorias = ['Tempo Total\n(8.287 patrimônios)', 'Tempo por\nPatrimônio']
    manual = [759.6, 5.5]  # horas, minutos
    sistema = [15.4, 0.11]  # horas, minutos
    
    x = np.arange(len(categorias))
    width = 0.35
    
    bars1 = ax.bar(x - width/2, manual, width, label='Processo Manual', 
                   color=CORES['vermelho'], alpha=0.8)
    bars2 = ax.bar(x + width/2, sistema, width, label='Sistema SIHCP', 
                   color=CORES['verde_ifmt'])
    
    ax.set_ylabel('Tempo', fontweight='bold')
    ax.set_title('Economia de Tempo - Inventário 2025\nComparativo: Processo Manual vs Sistema SIHCP', 
                 fontweight='bold', fontsize=14)
    ax.set_xticks(x)
    ax.set_xticklabels(categorias)
    ax.legend()
    
    # Adicionar valores
    ax.annotate('759,6h\n(~95 dias)', xy=(0 - width/2, 759.6), xytext=(0, 5), 
               textcoords="offset points", ha='center', fontsize=10, fontweight='bold')
    ax.annotate('15,4h\n(~2 dias)', xy=(0 + width/2, 15.4), xytext=(0, 5), 
               textcoords="offset points", ha='center', fontsize=10, fontweight='bold',
               color=CORES['verde_ifmt'])
    
    ax.annotate('5,5 min', xy=(1 - width/2, 5.5), xytext=(0, 5), 
               textcoords="offset points", ha='center', fontsize=10, fontweight='bold')
    ax.annotate('6,7 seg', xy=(1 + width/2, 0.11), xytext=(0, 5), 
               textcoords="offset points", ha='center', fontsize=10, fontweight='bold',
               color=CORES['verde_ifmt'])
    
    # Economia
    ax.text(0, 400, '⬇️ -98%', ha='center', fontsize=14, fontweight='bold', 
            color=CORES['verde_ifmt'])
    ax.text(1, 3, '⬇️ -98%', ha='center', fontsize=14, fontweight='bold', 
            color=CORES['verde_ifmt'])
    
    ax.spines['top'].set_visible(False)
    ax.spines['right'].set_visible(False)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_economia_tempo.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 8: Economia de tempo salvo")


def grafico_kpis_dashboard():
    """Gráfico 9: Dashboard de KPIs"""
    fig = plt.figure(figsize=(14, 8))
    
    # Grid de subplots
    gs = fig.add_gridspec(2, 3, hspace=0.3, wspace=0.3)
    
    # KPI 1: Progresso
    ax1 = fig.add_subplot(gs[0, 0])
    ax1.pie([76.66, 23.34], colors=[CORES['verde_ifmt'], CORES['cinza_claro']], 
            startangle=90, wedgeprops=dict(width=0.4))
    ax1.text(0, 0, '76,7%', ha='center', va='center', fontsize=20, fontweight='bold')
    ax1.set_title('Progresso', fontweight='bold')
    
    # KPI 2: Coletas
    ax2 = fig.add_subplot(gs[0, 1])
    ax2.text(0.5, 0.5, '8.287', ha='center', va='center', fontsize=36, 
             fontweight='bold', color=CORES['verde_ifmt'], transform=ax2.transAxes)
    ax2.text(0.5, 0.2, 'Patrimônios\nColetados', ha='center', va='center', 
             fontsize=12, transform=ax2.transAxes)
    ax2.axis('off')
    ax2.set_title('Total de Coletas', fontweight='bold')
    
    # KPI 3: Coletores
    ax3 = fig.add_subplot(gs[0, 2])
    ax3.text(0.5, 0.5, '13', ha='center', va='center', fontsize=36, 
             fontweight='bold', color=CORES['azul'], transform=ax3.transAxes)
    ax3.text(0.5, 0.2, 'Membros da\nComissão', ha='center', va='center', 
             fontsize=12, transform=ax3.transAxes)
    ax3.axis('off')
    ax3.set_title('Coletores Ativos', fontweight='bold')
    
    # KPI 4: Taxa de Sucesso
    ax4 = fig.add_subplot(gs[1, 0])
    ax4.barh(['Taxa de\nSucesso'], [98.56], color=CORES['verde_ifmt'], height=0.5)
    ax4.barh(['Taxa de\nSucesso'], [100-98.56], left=[98.56], color=CORES['cinza_claro'], height=0.5)
    ax4.set_xlim(0, 100)
    ax4.text(50, 0, '98,56%', ha='center', va='center', fontsize=16, fontweight='bold', color='white')
    ax4.set_title('Taxa de Sucesso', fontweight='bold')
    ax4.spines['top'].set_visible(False)
    ax4.spines['right'].set_visible(False)
    
    # KPI 5: Divergências
    ax5 = fig.add_subplot(gs[1, 1])
    ax5.text(0.5, 0.5, '1.377', ha='center', va='center', fontsize=36, 
             fontweight='bold', color=CORES['laranja'], transform=ax5.transAxes)
    ax5.text(0.5, 0.2, 'Divergências\n(16,42%)', ha='center', va='center', 
             fontsize=12, transform=ax5.transAxes)
    ax5.axis('off')
    ax5.set_title('Divergências Detectadas', fontweight='bold')
    
    # KPI 6: Dias de Operação
    ax6 = fig.add_subplot(gs[1, 2])
    ax6.text(0.5, 0.5, '24', ha='center', va='center', fontsize=36, 
             fontweight='bold', color=CORES['verde_ifmt'], transform=ax6.transAxes)
    ax6.text(0.5, 0.2, 'Dias de\nOperação', ha='center', va='center', 
             fontsize=12, transform=ax6.transAxes)
    ax6.axis('off')
    ax6.set_title('Período de Coleta', fontweight='bold')
    
    fig.suptitle('Dashboard de KPIs - Inventário 2025\nIFMT Campus Primavera do Leste', 
                 fontsize=16, fontweight='bold', y=1.02)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_kpis_dashboard.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 9: Dashboard KPIs salvo")


def diagrama_sipoc():
    """Diagrama 5: SIPOC do Subprocesso de Coleta"""
    fig, ax = plt.subplots(figsize=(14, 8))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 8)
    ax.axis('off')
    
    # Título
    ax.text(7, 7.5, 'Matriz SIPOC - Subprocesso de Coleta em Campo', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Colunas SIPOC
    colunas = [
        ('S', 'Suppliers\n(Fornecedores)', '#E3F2FD', '#1976D2'),
        ('I', 'Inputs\n(Entradas)', '#E8F5E9', '#388E3C'),
        ('P', 'Process\n(Processo)', '#FFF3E0', '#F57C00'),
        ('O', 'Outputs\n(Saídas)', '#F3E5F5', '#7B1FA2'),
        ('C', 'Customers\n(Clientes)', '#FFEBEE', '#C62828'),
    ]
    
    largura = 2.4
    for i, (letra, titulo, cor_fundo, cor_borda) in enumerate(colunas):
        x = 0.8 + i * 2.6
        
        # Cabeçalho
        rect = mpatches.FancyBboxPatch((x, 5.8), largura, 1, boxstyle="round,pad=0.05",
                                        facecolor=cor_borda, edgecolor=cor_borda, lw=2)
        ax.add_patch(rect)
        ax.text(x + largura/2, 6.3, titulo, ha='center', va='center', 
               fontsize=10, fontweight='bold', color='white')
        
        # Corpo
        rect = mpatches.FancyBboxPatch((x, 1.5), largura, 4.2, boxstyle="round,pad=0.05",
                                        facecolor=cor_fundo, edgecolor=cor_borda, lw=1)
        ax.add_patch(rect)
    
    # Conteúdo de cada coluna
    conteudos = [
        ['SUAP', 'Comissão de\nInventário', 'Gestão\nPatrimonial'],
        ['Lista de\npatrimônios', 'Dados\ncadastrais', 'Localização\nesperada'],
        ['Escanear\ncódigo', 'Validar\ndados', 'Registrar\ncoleta', 'Sincronizar'],
        ['Dados\ncoletados', 'Divergências\nidentificadas', 'Fotos', 'Relatórios'],
        ['Consolidação', 'Exportação\nSIADS', 'Gestão\nPatrimonial'],
    ]
    
    for i, itens in enumerate(conteudos):
        x = 0.8 + i * 2.6 + largura/2
        for j, item in enumerate(itens):
            y = 5.2 - j * 1.0
            ax.text(x, y, f'• {item}', ha='center', va='center', fontsize=9)
    
    # Setas entre colunas
    for i in range(4):
        x = 0.8 + (i + 1) * 2.6 - 0.1
        ax.annotate('', xy=(x, 3.5), xytext=(x - 0.2, 3.5),
                   arrowprops=dict(arrowstyle='->', color='#666', lw=2))
    
    # Nota
    ax.text(7, 0.8, '▶ O SIHCP automatiza o Processo (P), recebendo Inputs do SUAP e gerando Outputs para SIADS', 
            ha='center', fontsize=10, style='italic', color=CORES['cinza'])
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_sipoc.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 5: SIPOC salvo")


def grafico_curva_aprendizado():
    """Gráfico 10: Curva de aprendizado (produtividade por semana)"""
    semanas = ['Sem 1\n(18-24/11)', 'Sem 2\n(25-01/12)', 'Sem 3\n(02-08/12)', 
               'Sem 4\n(09-15/12)', 'Sem 5\n(16-23/12)']
    coletas = [9, 4736, 1738, 555, 898]
    media_dia = [3.0, 677.0, 248.0, 92.5, 224.5]
    
    fig, ax1 = plt.subplots(figsize=(12, 6))
    
    # Barras de coletas totais
    bars = ax1.bar(semanas, coletas, color=CORES['verde_ifmt'], alpha=0.7, label='Total de coletas')
    ax1.set_xlabel('Semana', fontweight='bold')
    ax1.set_ylabel('Total de Coletas', fontweight='bold', color=CORES['verde_ifmt'])
    ax1.tick_params(axis='y', labelcolor=CORES['verde_ifmt'])
    
    # Linha de média diária
    ax2 = ax1.twinx()
    ax2.plot(semanas, media_dia, 'o-', color=CORES['laranja'], linewidth=2, 
             markersize=8, label='Média/dia')
    ax2.set_ylabel('Média por Dia', fontweight='bold', color=CORES['laranja'])
    ax2.tick_params(axis='y', labelcolor=CORES['laranja'])
    
    # Valores nas barras
    for bar, val in zip(bars, coletas):
        height = bar.get_height()
        ax1.annotate(f'{val:,}'.replace(',', '.'),
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 3), textcoords="offset points",
                    ha='center', va='bottom', fontsize=10, fontweight='bold')
    
    ax1.set_title('Curva de Aprendizado - Evolução Semanal\nInventário 2025', 
                  fontweight='bold', fontsize=14)
    
    # Legenda combinada
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper right')
    
    ax1.spines['top'].set_visible(False)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_curva_aprendizado.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 10: Curva de aprendizado salvo")


def main():
    """Função principal - gera todos os gráficos e diagramas"""
    print("\n" + "="*60)
    print("GERADOR DE GRAFICOS - DISSERTACAO DE MESTRADO")
    print("   Sistema SIHCP - Inventario Patrimonial IFMT")
    print("="*60 + "\n")
    
    # Gráficos estatísticos - Parte 1
    print("Gerando graficos estatisticos (Parte 1)...")
    grafico_evolucao_diaria()
    grafico_estados_conservacao()
    grafico_dia_semana()
    grafico_hora_dia()
    grafico_top_locais()
    grafico_progresso_inventario()
    grafico_comparativo_as_is_to_be()
    grafico_economia_tempo()
    grafico_kpis_dashboard()
    grafico_curva_aprendizado()
    
    # Gráficos estatísticos - Parte 2
    print("\nGerando graficos estatisticos (Parte 2)...")
    grafico_evolucao_acumulada()
    grafico_produtividade_coletores()
    grafico_divergencias_analise()
    grafico_timeline_projeto()
    grafico_matriz_resultados()
    grafico_valor_acervo()
    
    # Diagramas
    print("\nGerando diagramas...")
    diagrama_fluxo_bpm()
    diagrama_arquitetura_sistema()
    diagrama_hierarquia_processos()
    diagrama_triangulacao()
    diagrama_sipoc()
    diagrama_fluxo_dados()
    diagrama_ciclo_bpm()
    
    total = 23
    print("\n" + "="*60)
    print(f"CONCLUIDO! {total} arquivos gerados em '{OUTPUT_DIR}/'")
    print("="*60)
    
    # Lista de arquivos gerados
    print("\nArquivos gerados:")
    arquivos = [
        "grafico_evolucao_diaria.png",
        "grafico_estados_conservacao.png",
        "grafico_dia_semana.png",
        "grafico_hora_dia.png",
        "grafico_top_locais.png",
        "grafico_progresso_inventario.png",
        "grafico_comparativo_as_is_to_be.png",
        "grafico_economia_tempo.png",
        "grafico_kpis_dashboard.png",
        "grafico_curva_aprendizado.png",
        "grafico_evolucao_acumulada.png",
        "grafico_produtividade_coletores.png",
        "grafico_divergencias_analise.png",
        "grafico_timeline_projeto.png",
        "grafico_matriz_resultados.png",
        "grafico_valor_acervo.png",
        "diagrama_fluxo_bpm.png",
        "diagrama_arquitetura_sistema.png",
        "diagrama_hierarquia_processos.png",
        "diagrama_triangulacao.png",
        "diagrama_sipoc.png",
        "diagrama_fluxo_dados.png",
        "diagrama_ciclo_bpm.png",
    ]
    for arq in arquivos:
        print(f"   - {arq}")


# ============================================================
# GRÁFICOS ADICIONAIS - PARTE 2
# ============================================================

def grafico_evolucao_acumulada():
    """Gráfico 11: Evolução acumulada das coletas"""
    # Dados do banco PostgreSQL
    dias = ['18/11', '19/11', '24/11', '25/11', '26/11', '27/11', '28/11', '29/11',
            '01/12', '02/12', '03/12', '04/12', '05/12', '06/12', '08/12', '09/12',
            '11/12', '12/12', '14/12', '15/12', '16/12', '18/12', '22/12', '23/12']
    coletas_dia = [1, 4, 4, 823, 988, 1099, 1709, 117, 198, 1021, 294, 298, 5, 77, 
                   125, 215, 43, 50, 168, 247, 68, 34, 42, 754]
    acumulado = [1, 5, 9, 832, 1820, 2919, 4628, 4745, 4943, 5964, 6258, 6556, 6561, 
                 6638, 6763, 6978, 7021, 7071, 7239, 7486, 7554, 7588, 7630, 8384]
    
    fig, ax1 = plt.subplots(figsize=(14, 6))
    
    # Barras de coletas diárias
    bars = ax1.bar(range(len(dias)), coletas_dia, color=CORES['verde_claro'], 
                   alpha=0.6, label='Coletas/dia')
    ax1.set_ylabel('Coletas por Dia', fontweight='bold', color=CORES['verde_ifmt'])
    ax1.tick_params(axis='y', labelcolor=CORES['verde_ifmt'])
    
    # Linha de acumulado
    ax2 = ax1.twinx()
    ax2.plot(range(len(dias)), acumulado, 'o-', color=CORES['azul'], 
             linewidth=2.5, markersize=6, label='Acumulado')
    ax2.set_ylabel('Total Acumulado', fontweight='bold', color=CORES['azul'])
    ax2.tick_params(axis='y', labelcolor=CORES['azul'])
    
    # Meta de 100%
    meta = 11070  # Total a inventariar
    ax2.axhline(y=meta, color=CORES['vermelho'], linestyle='--', linewidth=1.5, 
                label=f'Meta: {meta:,}'.replace(',', '.'))
    
    # Linha de 76.66%
    ax2.axhline(y=8287, color=CORES['laranja'], linestyle=':', linewidth=1.5,
                label='Alcançado: 8.287 (76,66%)')
    
    ax1.set_xlabel('Data', fontweight='bold')
    ax1.set_xticks(range(len(dias)))
    ax1.set_xticklabels(dias, rotation=45, ha='right', fontsize=8)
    
    ax1.set_title('Evolução Acumulada das Coletas - Inventário 2025\n18/11 a 23/12/2025', 
                  fontweight='bold', fontsize=14)
    
    # Legenda combinada
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left')
    
    ax1.spines['top'].set_visible(False)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_evolucao_acumulada.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 11: Evolução acumulada salvo")


def grafico_produtividade_coletores():
    """Gráfico 12: Produtividade por coletor (anonimizado)"""
    coletores = [f'Coletor {i}' for i in range(1, 11)]
    total_coletas = [3145, 1551, 711, 532, 456, 396, 372, 293, 290, 253]
    media_dia = [349.4, 91.2, 237.0, 177.3, 152.0, 132.0, 74.4, 58.6, 72.5, 253.0]
    taxa_divergencia = [1.91, 27.47, 0.0, 99.25, 15.57, 0.0, 0.0, 0.0, 32.76, 0.0]
    
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
    
    # Gráfico 1: Total de coletas
    cores = [CORES['verde_ifmt'] if t < 20 else CORES['laranja'] if t < 50 else CORES['vermelho'] 
             for t in taxa_divergencia]
    bars1 = ax1.barh(coletores, total_coletas, color=cores)
    ax1.set_xlabel('Total de Coletas', fontweight='bold')
    ax1.set_title('Volume de Coletas por Coletor', fontweight='bold')
    ax1.invert_yaxis()
    
    for bar, val in zip(bars1, total_coletas):
        ax1.annotate(f'{val:,}'.replace(',', '.'), 
                    xy=(val + 50, bar.get_y() + bar.get_height()/2),
                    va='center', fontsize=9)
    
    # Gráfico 2: Média diária vs Taxa de divergência
    scatter = ax2.scatter(media_dia, taxa_divergencia, s=[t/10 for t in total_coletas],
                          c=taxa_divergencia, cmap='RdYlGn_r', alpha=0.7, edgecolors='black')
    
    for i, (x, y) in enumerate(zip(media_dia, taxa_divergencia)):
        ax2.annotate(f'C{i+1}', (x, y), textcoords="offset points", 
                    xytext=(5, 5), fontsize=8)
    
    ax2.set_xlabel('Média de Coletas/Dia', fontweight='bold')
    ax2.set_ylabel('Taxa de Divergência (%)', fontweight='bold')
    ax2.set_title('Produtividade vs Divergência', fontweight='bold')
    ax2.axhline(y=20, color=CORES['laranja'], linestyle='--', alpha=0.5, label='Limite aceitável')
    ax2.legend()
    
    # Colorbar
    cbar = plt.colorbar(scatter, ax=ax2)
    cbar.set_label('Taxa Divergência (%)')
    
    plt.suptitle('Análise de Desempenho dos Coletores - Inventário 2025\n(Dados Anonimizados)', 
                 fontweight='bold', fontsize=14, y=1.02)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_produtividade_coletores.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 12: Produtividade coletores salvo")


def grafico_divergencias_analise():
    """Gráfico 13: Análise de divergências"""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
    
    # Gráfico 1: Pizza de divergências
    labels = ['Sem Divergência\n(83,58%)', 'Com Divergência\n(16,42%)']
    sizes = [7007, 1377]
    colors = [CORES['verde_ifmt'], CORES['laranja']]
    explode = (0, 0.05)
    
    wedges, texts, autotexts = ax1.pie(sizes, labels=labels, autopct='%1.1f%%',
                                        colors=colors, explode=explode, startangle=90)
    ax1.set_title('Distribuição de Divergências\n8.384 coletas', fontweight='bold')
    
    # Gráfico 2: Top motivos de divergência (por local)
    locais = ['Desf. Antigo', 'Auditório', 'Mezanino', 'Corredor', 'Lab. A10', 'Outros']
    divergencias = [429, 318, 95, 76, 65, 394]
    
    bars = ax2.barh(locais, divergencias, color=CORES['laranja'])
    ax2.set_xlabel('Quantidade de Divergências', fontweight='bold')
    ax2.set_title('Divergências por Local Encontrado', fontweight='bold')
    ax2.invert_yaxis()
    
    for bar, val in zip(bars, divergencias):
        pct = val / 1377 * 100
        ax2.annotate(f'{val} ({pct:.1f}%)', 
                    xy=(val + 10, bar.get_y() + bar.get_height()/2),
                    va='center', fontsize=9)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_divergencias_analise.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 13: Análise divergências salvo")


def diagrama_fluxo_dados():
    """Diagrama 6: Fluxo de dados entre sistemas"""
    fig, ax = plt.subplots(figsize=(14, 8))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 8)
    ax.axis('off')
    
    # Título
    ax.text(7, 7.5, 'Fluxo de Dados - Integracao entre Sistemas', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Sistemas
    sistemas = [
        {'nome': 'SUAP', 'x': 1.5, 'y': 4, 'cor': '#607D8B', 'desc': 'Dados\nCadastrais'},
        {'nome': 'SIHCP\nDesktop', 'x': 5, 'y': 5.5, 'cor': '#FF9800', 'desc': 'Gestao\nRelatorios'},
        {'nome': 'SIHCP\nMobile', 'x': 5, 'y': 2.5, 'cor': '#4CAF50', 'desc': 'Coleta\nCampo'},
        {'nome': 'PostgreSQL', 'x': 8.5, 'y': 4, 'cor': '#9C27B0', 'desc': 'Banco\nCentral'},
        {'nome': 'SIADS', 'x': 12, 'y': 4, 'cor': '#607D8B', 'desc': 'Registro\nOficial'},
    ]
    
    for s in sistemas:
        rect = mpatches.FancyBboxPatch((s['x']-0.8, s['y']-0.6), 1.6, 1.2, 
                                        boxstyle="round,pad=0.1",
                                        facecolor=s['cor'], alpha=0.3, 
                                        edgecolor=s['cor'], lw=2)
        ax.add_patch(rect)
        ax.text(s['x'], s['y']+0.2, s['nome'], ha='center', va='center', 
               fontsize=10, fontweight='bold')
        ax.text(s['x'], s['y']-0.3, s['desc'], ha='center', va='center', 
               fontsize=8, color=CORES['cinza'])
    
    # Setas de fluxo
    fluxos = [
        (2.3, 4, 4.2, 5.2, 'Importacao\nCSV'),
        (5.8, 5.2, 7.7, 4.3, 'JPA'),
        (5.8, 2.8, 7.7, 3.7, 'REST API'),
        (5, 4.9, 5, 3.1, 'Sync'),
        (9.3, 4, 11.2, 4, 'Exportacao\nCSV'),
    ]
    
    for x1, y1, x2, y2, label in fluxos:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                   arrowprops=dict(arrowstyle='->', color='#333', lw=2))
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx, my+0.3, label, ha='center', fontsize=8, color=CORES['cinza'])
    
    # Legenda
    ax.text(7, 1, 'Fluxo: SUAP (origem) -> SIHCP (processamento) -> SIADS (destino oficial)', 
            ha='center', fontsize=10, style='italic', color=CORES['cinza'])
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_fluxo_dados.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 6: Fluxo de dados salvo")


def grafico_timeline_projeto():
    """Gráfico 14: Timeline do projeto"""
    fig, ax = plt.subplots(figsize=(14, 6))
    ax.set_xlim(0, 14)
    ax.set_ylim(0, 6)
    ax.axis('off')
    
    # Título
    ax.text(7, 5.5, 'Timeline do Projeto SIHCP - 2025', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Linha do tempo
    ax.plot([1, 13], [3, 3], 'k-', linewidth=3)
    
    # Marcos
    marcos = [
        (1.5, 'Jan-Mar', 'Desenvolvimento\nInicial', CORES['azul']),
        (3.5, 'Abr-Jun', 'Testes e\nAjustes', CORES['verde_claro']),
        (5.5, 'Jul-Set', 'Integracao\nMobile', CORES['laranja']),
        (7.5, 'Out', 'Preparacao\nInventario', CORES['verde_ifmt']),
        (9.5, 'Nov', 'INVENTARIO\n2025', CORES['vermelho']),
        (11.5, 'Dez', 'Analise\nResultados', CORES['azul']),
    ]
    
    for x, periodo, desc, cor in marcos:
        # Círculo no timeline
        circle = plt.Circle((x, 3), 0.2, color=cor, ec='white', lw=2, zorder=5)
        ax.add_patch(circle)
        
        # Texto acima/abaixo alternado
        y_text = 4.2 if marcos.index((x, periodo, desc, cor)) % 2 == 0 else 1.8
        y_line = 3.2 if y_text > 3 else 2.8
        
        ax.plot([x, x], [y_line, y_text - 0.3 if y_text > 3 else y_text + 0.3], 
               color=cor, linewidth=1.5)
        
        ax.text(x, y_text, f'{periodo}\n{desc}', ha='center', va='center' if y_text < 3 else 'bottom',
               fontsize=9, fontweight='bold' if 'INVENTARIO' in desc else 'normal')
    
    # Destaque do período de coleta
    rect = mpatches.FancyBboxPatch((8.8, 2.5), 1.4, 1, boxstyle="round,pad=0.05",
                                    facecolor=CORES['vermelho'], alpha=0.2, 
                                    edgecolor=CORES['vermelho'], lw=2)
    ax.add_patch(rect)
    
    # Estatísticas do inventário
    ax.text(7, 0.8, '18/11 - 23/12/2025  |  8.287 patrimonios  |  13 coletores  |  24 dias', 
            ha='center', fontsize=10, style='italic', 
            bbox=dict(boxstyle='round', facecolor='wheat', alpha=0.5))
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_timeline_projeto.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Gráfico 14: Timeline projeto salvo")


def grafico_matriz_resultados():
    """Gráfico 15: Matriz de resultados (radar chart)"""
    fig, ax = plt.subplots(figsize=(10, 10), subplot_kw=dict(polar=True))
    
    # Categorias
    categorias = ['Tempo de\nColeta', 'Taxa de\nErro', 'Produtividade', 
                  'Cobertura', 'Satisfacao\n(estimada)', 'Conformidade\nLegal']
    N = len(categorias)
    
    # Valores (0-100, normalizados)
    # Manual: tempo alto=ruim, erro alto=ruim, etc
    manual = [20, 15, 30, 50, 40, 60]  # Processo manual
    sistema = [98, 95, 90, 77, 80, 95]  # Sistema SIHCP
    
    # Ângulos
    angles = [n / float(N) * 2 * np.pi for n in range(N)]
    angles += angles[:1]  # Fechar o polígono
    
    manual += manual[:1]
    sistema += sistema[:1]
    
    # Plot
    ax.plot(angles, manual, 'o-', linewidth=2, label='Processo Manual', color=CORES['vermelho'])
    ax.fill(angles, manual, alpha=0.25, color=CORES['vermelho'])
    
    ax.plot(angles, sistema, 'o-', linewidth=2, label='Sistema SIHCP', color=CORES['verde_ifmt'])
    ax.fill(angles, sistema, alpha=0.25, color=CORES['verde_ifmt'])
    
    # Labels
    ax.set_xticks(angles[:-1])
    ax.set_xticklabels(categorias, fontsize=10)
    ax.set_ylim(0, 100)
    
    ax.set_title('Matriz de Resultados - Comparativo\nProcesso Manual vs Sistema SIHCP', 
                 fontweight='bold', fontsize=14, pad=20)
    ax.legend(loc='upper right', bbox_to_anchor=(1.3, 1.0))
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_matriz_resultados.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 15: Matriz resultados salvo")


def diagrama_ciclo_bpm():
    """Diagrama 7: Ciclo BPM aplicado ao projeto"""
    fig, ax = plt.subplots(figsize=(10, 10))
    ax.set_xlim(0, 10)
    ax.set_ylim(0, 10)
    ax.axis('off')
    
    # Título
    ax.text(5, 9.5, 'Ciclo BPM Aplicado ao Projeto', 
            ha='center', fontsize=16, fontweight='bold')
    
    # Centro
    circle = plt.Circle((5, 5), 1.2, color=CORES['verde_ifmt'], ec='white', lw=3)
    ax.add_patch(circle)
    ax.text(5, 5, 'MELHORIA\nCONTINUA', ha='center', va='center', 
           fontsize=10, fontweight='bold', color='white')
    
    # Etapas do ciclo
    etapas = [
        (5, 8, '1. MODELAR\nAS-IS', '#E3F2FD', '#1976D2', 'Concluido'),
        (8, 6.5, '2. ANALISAR', '#E8F5E9', '#388E3C', 'Concluido'),
        (8, 3.5, '3. REDESENHAR\nTO-BE', '#FFF3E0', '#F57C00', 'Concluido'),
        (5, 2, '4. IMPLEMENTAR', '#F3E5F5', '#7B1FA2', 'Concluido'),
        (2, 3.5, '5. MONITORAR', '#FFEBEE', '#C62828', 'Em andamento'),
        (2, 6.5, '6. OTIMIZAR', '#E0F7FA', '#00838F', 'Proximo'),
    ]
    
    for x, y, texto, cor_fundo, cor_borda, status in etapas:
        # Círculo da etapa
        circle = plt.Circle((x, y), 0.9, color=cor_fundo, ec=cor_borda, lw=3)
        ax.add_patch(circle)
        ax.text(x, y+0.1, texto, ha='center', va='center', fontsize=8, fontweight='bold')
        
        # Status
        cor_status = CORES['verde_ifmt'] if status == 'Concluido' else CORES['laranja'] if status == 'Em andamento' else CORES['cinza']
        ax.text(x, y-0.5, status, ha='center', fontsize=7, color=cor_status, style='italic')
    
    # Setas entre etapas (sentido horário)
    setas = [
        (5, 7.1, 7.1, 6.5),
        (7.1, 6.5, 7.1, 3.5),
        (7.1, 3.5, 5, 2.9),
        (5, 2.9, 2.9, 3.5),
        (2.9, 3.5, 2.9, 6.5),
        (2.9, 6.5, 5, 7.1),
    ]
    
    for x1, y1, x2, y2 in setas:
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                   arrowprops=dict(arrowstyle='->', color='#666', lw=1.5,
                                  connectionstyle='arc3,rad=0.2'))
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/diagrama_ciclo_bpm.png', dpi=150, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close()
    print("✅ Diagrama 7: Ciclo BPM salvo")


def grafico_valor_acervo():
    """Gráfico 16: Distribuição de valor do acervo"""
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
    
    # Dados
    faixas = ['Ate R$100', 'R$100-500', 'R$500-1k', 'R$1k-5k', 'R$5k-10k', '>R$10k']
    quantidades = [4222, 4348, 922, 1019, 190, 109]
    valores = [234769, 930451, 633960, 2227582, 1398247, 5947356]  # em reais
    
    # Gráfico 1: Quantidade por faixa
    bars1 = ax1.bar(faixas, quantidades, color=CORES['verde_ifmt'])
    ax1.set_ylabel('Quantidade de Patrimonios', fontweight='bold')
    ax1.set_title('Distribuicao por Faixa de Valor\n(Quantidade)', fontweight='bold')
    ax1.tick_params(axis='x', rotation=45)
    
    for bar, val in zip(bars1, quantidades):
        ax1.annotate(f'{val:,}'.replace(',', '.'), 
                    xy=(bar.get_x() + bar.get_width()/2, bar.get_height()),
                    xytext=(0, 3), textcoords="offset points", ha='center', fontsize=9)
    
    # Gráfico 2: Valor por faixa
    bars2 = ax2.bar(faixas, [v/1000000 for v in valores], color=CORES['azul'])
    ax2.set_ylabel('Valor Total (R$ milhoes)', fontweight='bold')
    ax2.set_title('Distribuicao por Faixa de Valor\n(Valor em R$)', fontweight='bold')
    ax2.tick_params(axis='x', rotation=45)
    
    for bar, val in zip(bars2, valores):
        ax2.annotate(f'R${val/1000000:.1f}M', 
                    xy=(bar.get_x() + bar.get_width()/2, bar.get_height()),
                    xytext=(0, 3), textcoords="offset points", ha='center', fontsize=9)
    
    # Destaque: 1% do acervo = 52% do valor
    ax2.annotate('1% do acervo\n= 52% do valor!', xy=(5, 5.9), xytext=(3.5, 4.5),
                arrowprops=dict(arrowstyle='->', color=CORES['vermelho']),
                fontsize=10, color=CORES['vermelho'], fontweight='bold')
    
    plt.suptitle('Analise de Valor do Acervo - R$ 11,37 milhoes', 
                 fontweight='bold', fontsize=14, y=1.02)
    
    plt.tight_layout()
    plt.savefig(f'{OUTPUT_DIR}/grafico_valor_acervo.png', dpi=150, bbox_inches='tight')
    plt.close()
    print("✅ Gráfico 16: Valor acervo salvo")



if __name__ == "__main__":
    main()
