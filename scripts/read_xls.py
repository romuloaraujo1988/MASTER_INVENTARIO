import pandas as pd
import json

df = pd.read_excel('docs/Relatorio-08.03.xls')

with open('cols.txt', 'w', encoding='utf-8') as f:
    f.write("Columns: " + str(df.columns.tolist()) + "\n\n")
    
    loc_cols = [c for c in df.columns if isinstance(c, str) and ('local' in c.lower() or 'setor' in c.lower() or 'sala' in c.lower() or 'desc' in c.lower())]
    
    for col in loc_cols:
        f.write(f"--- {col} ---\n")
        f.write(df[col].value_counts().head(20).to_string() + "\n\n")
        
    f.write("--- First 5 rows ---\n")
    f.write(df.head(5).to_string() + "\n")
