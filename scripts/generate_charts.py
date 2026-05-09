import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

def generate_charts():
    # Setup directories
    os.makedirs('../data/charts', exist_ok=True)
    sns.set_theme(style="whitegrid")
    
    # Load Data
    try:
        df = pd.read_csv('../data/dataset_coletas.csv')
    except Exception as e:
        print(f"Error loading dataset: {e}")
        return
        
    # Clean data
    df['estado_encontrado'] = df['estado_encontrado'].str.upper().replace({
        'IRRECUPERAVEL': 'IRRECUPERÁVEL',
        'RECUPERAVEL': 'RECUPERÁVEL'
    }).fillna('N/A')
    
    # 1. Gráfico de Pizza (Distribuição de Estado de Conservação)
    print("Generating Pie Chart...")
    estado_counts = df['estado_encontrado'].value_counts()
    
    # Filter very small values for the pie chart to look cleaner
    main_estados = estado_counts[estado_counts > 50]
    outros = estado_counts[estado_counts <= 50].sum()
    if outros > 0:
        main_estados['OUTROS'] = outros
        
    plt.figure(figsize=(10, 8))
    colors = sns.color_palette("pastel")[0:len(main_estados)]
    plt.pie(main_estados.values, labels=main_estados.index, colors=colors, autopct='%.1f%%', startangle=140)
    plt.title('Distribuição do Estado de Conservação', fontsize=16)
    plt.axis('equal')
    plt.savefig('../data/charts/estado_conservacao_pie.png', dpi=300, bbox_inches='tight')
    plt.close()

    # 2. Gráfico de Barras (Top Salas com mais Coletas e Divergências)
    print("Generating Bar Chart...")
    sala_data = df.groupby('localizacao_encontrada').agg(
        total_coletas=('id', 'count'),
        divergencias=('divergencia', 'sum')
    ).reset_index()
    
    # Top 10 salas por volume
    top_salas = sala_data.nlargest(10, 'total_coletas')
    
    plt.figure(figsize=(12, 6))
    
    # Plotting using matplotlib directly for side-by-side bars
    import numpy as np
    x = np.arange(len(top_salas['localizacao_encontrada']))
    width = 0.35
    
    fig, ax = plt.subplots(figsize=(14, 7))
    rects1 = ax.bar(x - width/2, top_salas['total_coletas'], width, label='Total Coletas', color='skyblue')
    rects2 = ax.bar(x + width/2, top_salas['divergencias'], width, label='Divergências', color='salmon')
    
    ax.set_ylabel('Quantidade')
    ax.set_title('Coletas e Divergências nas Top 10 Salas')
    ax.set_xticks(x)
    ax.set_xticklabels(top_salas['localizacao_encontrada'], rotation=45, ha='right')
    ax.legend()
    
    plt.tight_layout()
    plt.savefig('../data/charts/top_salas_divergencias.png', dpi=300)
    plt.close()
    
    # 3. Boxplot (Dispersão Temporal de Coleta por Sala)
    # Consider only top 5 rooms to avoid clutter
    print("Generating Boxplot...")
    top5_names = sala_data.nlargest(5, 'total_coletas')['localizacao_encontrada']
    df_top5 = df[df['localizacao_encontrada'].isin(top5_names)]
    df_top5.loc[:, 'tempo_coleta_segundos'] = pd.to_numeric(df_top5['tempo_coleta_segundos'], errors='coerce')
    
    # Filter outliers > 60 seconds for visual clarity
    df_top5_filtered = df_top5[df_top5['tempo_coleta_segundos'] <= 60]
    
    plt.figure(figsize=(12, 6))
    sns.boxplot(x='localizacao_encontrada', y='tempo_coleta_segundos', data=df_top5_filtered, palette='Set2')
    plt.title('Dispersão do Tempo de Coleta nas Top 5 Salas (Até 60s)')
    plt.xlabel('Sala')
    plt.ylabel('Tempo de Coleta (segundos)')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig('../data/charts/tempo_coleta_boxplot.png', dpi=300)
    plt.close()

    # 4. Heatmap (Divergências vs Estado de Conservação)
    print("Generating Heatmap...")
    heatmap_data = pd.crosstab(df['localizacao_encontrada'], df['estado_encontrado'])
    # limit to top 15 rows with most irrecoverables for the heatmap
    top_irr = heatmap_data.sort_values(by='IRRECUPERÁVEL', ascending=False).head(15)
    
    plt.figure(figsize=(12, 8))
    sns.heatmap(top_irr, annot=True, fmt='d', cmap='YlOrRd')
    plt.title('Mapa de Calor: Estado de Conservação vs Localização (Top 15 IRRECUPERÁVEIS)')
    plt.xlabel('Estado de Conservação')
    plt.ylabel('Sala')
    plt.tight_layout()
    plt.savefig('../data/charts/estado_heatmap.png', dpi=300)
    plt.close()

    print("Charts generated successfully in 'data/charts/'.")

if __name__ == '__main__':
    generate_charts()
