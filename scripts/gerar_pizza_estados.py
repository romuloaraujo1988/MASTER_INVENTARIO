import psycopg2
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

def main():
    conn = psycopg2.connect(
        host='localhost',
        database='sispatrimonio',
        user='postgres',
        password='Romulo@2020',
        port=5432
    )
    cur = conn.cursor()

    # Estados já corrigidos no banco
    cur.execute("""
        SELECT 
            CASE 
                WHEN UPPER(TRIM(estado_encontrado)) IN ('IRRECUPERAVEL','IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
                WHEN UPPER(TRIM(estado_encontrado)) IN ('RECUPERAVEL','RECUPERÁVEL') THEN 'RECUPERÁVEL'
                WHEN estado_encontrado IS NULL OR TRIM(estado_encontrado) = '' THEN 'N/A'
                ELSE UPPER(TRIM(estado_encontrado))
            END AS estado,
            COUNT(*) AS qtd
        FROM tabela_coleta
        GROUP BY 1
        ORDER BY 2 DESC;
    """)
    rows_coleta = cur.fetchall()

    cur.execute("SELECT COUNT(*) FROM tabela_coleta_componente;")
    total_comp = cur.fetchone()[0]
    cur.close()
    conn.close()

    # Somar componentes como BOM (coletas de itens compostos bem-sucedidas)
    estados = [r[0] for r in rows_coleta]
    qtds = [r[1] for r in rows_coleta]

    qtds_final = []
    for e, q in zip(estados, qtds):
        if e == 'BOM':
            qtds_final.append(q + total_comp)
        else:
            qtds_final.append(q)

    total_geral = sum(qtds_final)

    # Paleta de cores
    palette = {
        'BOM':           '#2ecc71',
        'IRRECUPERÁVEL': '#e74c3c',
        'OCIOSO':        '#f39c12',
        'N/A':           '#95a5a6',
        'RECUPERÁVEL':   '#9b59b6',
        'COLETADO':      '#1abc9c',
    }
    cores = [palette.get(e, '#bdc3c7') for e in estados]

    fig, ax = plt.subplots(figsize=(11, 7))

    wedges, _ = ax.pie(
        qtds_final,
        colors=cores,
        startangle=90,
        wedgeprops=dict(width=0.55, edgecolor='white', linewidth=1.5),
        counterclock=False,
    )

    # Texto central
    ax.text(0, 0.05, f'{total_geral:,}', ha='center', va='center',
            fontsize=18, fontweight='bold', color='#2c3e50')
    ax.text(0, -0.18, 'coletas totais', ha='center', va='center',
            fontsize=10, color='#7f8c8d')

    # Legenda lateral
    legend_labels = [
        f'{e}  —  {q:,}  ({q/total_geral*100:.1f}%)'
        for e, q in zip(estados, qtds_final)
    ]
    patches = [mpatches.Patch(color=c, label=l) for c, l in zip(cores, legend_labels)]
    ax.legend(
        handles=patches,
        loc='center left',
        bbox_to_anchor=(1.02, 0.5),
        fontsize=11,
        frameon=True,
        framealpha=0.95,
        edgecolor='#bdc3c7',
        title='Estado de Conservação',
        title_fontsize=12,
    )

    ax.set_title(
        'Distribuição do Estado de Conservação\nInventário Patrimonial 2025 — IFMT Campus Primavera do Leste',
        fontsize=13, fontweight='bold', pad=20
    )

    plt.tight_layout()
    out = 'data/charts/estado_conservacao_pie.png'
    plt.savefig(out, dpi=150, bbox_inches='tight')
    plt.close()
    print(f'Salvo em: {out}')
    print(f'Total: {total_geral:,} | BOM: {qtds_final[0]:,} ({qtds_final[0]/total_geral*100:.1f}%)')

if __name__ == '__main__':
    main()
