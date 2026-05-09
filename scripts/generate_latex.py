import pandas as pd
import os

def generate_latex():
    # Setup directories
    os.makedirs('../data/latex', exist_ok=True)
    
    try:
        df = pd.read_csv('../data/dataset_coletas.csv')
    except Exception as e:
        print(f"Error loading CSV: {e}")
        return
        
    df['estado_encontrado'] = df['estado_encontrado'].str.upper().replace({
        'IRRECUPERAVEL': 'IRRECUPERÁVEL',
        'RECUPERAVEL': 'RECUPERÁVEL'
    }).fillna('N/A')

    # 1. Tabela LaTeX: Distribuição de Estado de Conservação
    estado_counts = df['estado_encontrado'].value_counts().reset_index()
    estado_counts.columns = ['Estado de Conservação', 'Quantidade']
    estado_counts['Percentual (%)'] = (estado_counts['Quantidade'] / len(df) * 100).round(2)
    
    latex_estado = estado_counts.to_latex(index=False, float_format="%.2f", caption="Distribuição do Estado de Conservação dos Bens Analisados", label="tab:estado_conservacao", column_format="lrr")
    
    with open('../data/latex/tabela_estado_conservacao.tex', 'w', encoding='utf-8') as f:
        f.write("% Tabela para Artigo Científico - Requer pacote booktabs (opcional mas recomendado)\n")
        f.write(latex_estado)
        
    # 2. Tabela LaTeX: Divergências Top 5 Salas
    sala_data = df.groupby('localizacao_encontrada').agg(
        total_coletas=('id', 'count'),
        divergencias=('divergencia', 'sum')
    ).reset_index()
    
    top_div = sala_data[sala_data['divergencias'] > 0].copy()
    top_div['Taxa (%)'] = (top_div['divergencias'] / top_div['total_coletas'] * 100).round(2)
    # Get top 5 by percentage for rooms with > 10 items to prevent statistical noise
    top_div_filtered = top_div[top_div['total_coletas'] > 10].nlargest(5, 'Taxa (%)')
    top_div_filtered.columns = ['Sala/Local', 'Total de Itens', 'Apresentam Divergência', 'Taxa de Divergência (%)']
    
    latex_div = top_div_filtered.to_latex(index=False, float_format="%.2f", caption="As 5 Localizações com Maior Índice de Divergência Inventarial", label="tab:divergencias_loc", column_format="lrrr")
    
    with open('../data/latex/tabela_top_divergencias.tex', 'w', encoding='utf-8') as f:
        f.write("% Tabela para Artigo Científico\n")
        f.write(latex_div)

    print("Tabelas LaTeX geradas com sucesso na pasta 'data/latex/'.")

if __name__ == '__main__':
    generate_latex()
