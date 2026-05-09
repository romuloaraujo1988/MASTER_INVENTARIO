import psycopg2
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import numpy as np
from datetime import date
import os

def main():
    conn = psycopg2.connect(
        host='localhost',
        database='sispatrimonio',
        user='postgres',
        password='Romulo@2020',
        port=5432
    )
    cur = conn.cursor()

    # Coletas por dia + acumulado
    cur.execute("""
        SELECT 
            DATE(data_coleta AT TIME ZONE 'UTC' AT TIME ZONE 'America/Cuiaba') AS dia,
            COUNT(*) AS total_coletas
        FROM tabela_coleta
        GROUP BY 1
        ORDER BY 1;
    """)
    rows = cur.fetchall()
    cur.close()
    conn.close()

    datas = [r[0] for r in rows]
    coletas_dia = [r[1] for r in rows]
    acumulado = []
    soma = 0
    for c in coletas_dia:
        soma += c
        acumulado.append(soma)

    # Converter para tipo matplotlib
    import matplotlib.dates as mdates
    from datetime import datetime
    datas_dt = [datetime(d.year, d.month, d.day) for d in datas]

    fig, ax1 = plt.subplots(figsize=(16, 7))

    # Barras - coletas por dia
    cores = ['#e74c3c' if d.weekday() >= 5 else '#3498db' for d in datas]
    bars = ax1.bar(datas_dt, coletas_dia, color=cores, alpha=0.75, width=1.5, label='Coletas por Dia')
    ax1.set_ylabel('Coletas por Dia', fontsize=12, color='#3498db')
    ax1.tick_params(axis='y', labelcolor='#3498db')
    ax1.set_ylim(0, max(coletas_dia) * 1.25)

    # Eixo secundário - acumulado
    ax2 = ax1.twinx()
    ax2.plot(datas_dt, acumulado, color='#2ecc71', linewidth=2.5, marker='o', markersize=4, label='Acumulado')
    ax2.set_ylabel('Total Acumulado de Coletas', fontsize=12, color='#2ecc71')
    ax2.tick_params(axis='y', labelcolor='#2ecc71')
    ax2.set_ylim(0, max(acumulado) * 1.1)

    # Anotações nos picos
    picos_idx = sorted(range(len(coletas_dia)), key=lambda i: coletas_dia[i], reverse=True)[:3]
    for idx in picos_idx:
        ax1.annotate(
            f'{coletas_dia[idx]:,}',
            xy=(datas_dt[idx], coletas_dia[idx]),
            xytext=(0, 8),
            textcoords='offset points',
            ha='center', fontsize=9, color='#2c3e50', fontweight='bold'
        )

    # Formatação de eixo X
    ax1.xaxis.set_major_formatter(mdates.DateFormatter('%d/%m'))
    ax1.xaxis.set_major_locator(mdates.WeekdayLocator(interval=1))
    plt.setp(ax1.xaxis.get_majorticklabels(), rotation=45, ha='right', fontsize=9)

    # Legenda unificada
    from matplotlib.patches import Patch
    from matplotlib.lines import Line2D
    legend_elements = [
        Patch(facecolor='#3498db', alpha=0.75, label='Coletas (dia útil)'),
        Patch(facecolor='#e74c3c', alpha=0.75, label='Coletas (fim de semana)'),
        Line2D([0], [0], color='#2ecc71', linewidth=2.5, marker='o', markersize=5, label='Acumulado')
    ]
    ax1.legend(handles=legend_elements, loc='upper left', fontsize=10)

    # Título e grade
    plt.title(
        'Evolução Cronológica das Coletas - Inventário Patrimonial 2025\nIFMT Campus Primavera do Leste (18/11/2025 a 19/02/2026)',
        fontsize=14, fontweight='bold', pad=15
    )
    ax1.grid(axis='y', alpha=0.3)
    ax1.set_xlim(min(datas_dt), max(datas_dt))

    # Texto de total final
    fig.text(0.99, 0.02, f'Total: {soma:,} coletas | {len(rows)} dias com coleta (31 úteis + 3 fins de semana)',
             ha='right', fontsize=9, color='#7f8c8d')

    plt.tight_layout()
    out_path = os.path.join('images', 'grafico_evolucao_cronologica.png')
    plt.savefig(out_path, dpi=150, bbox_inches='tight')
    plt.close()
    print(f'Grafico salvo em: {out_path}')

if __name__ == '__main__':
    main()
