import psycopg2
import pandas as pd

try:
    # 1. Obter os itens que estão na Secretaria
    conn = psycopg2.connect(
        host="localhost",
        database="sispatrimonio",
        user="postgres",
        password="Romulo@2020",
        port=5432
    )
    cur = conn.cursor()
    cur.execute("SELECT id, numero, encode(descricao::bytea, 'hex') FROM tabela_patrimonio WHERE id_sala = 14")
    itens_secretaria = cur.fetchall()
    conn.close()
    
    # Criar um dicionário para busca rápida: numero -> (id, descricao)
    dict_secretaria = {}
    for item in itens_secretaria:
        pat_id = item[0]
        numero = str(item[1]).strip()
        desc_hex = item[2]
        desc = bytes.fromhex(desc_hex).decode('windows-1252', errors='replace').strip() if desc_hex else ""
        dict_secretaria[numero] = {'id': pat_id, 'desc': desc}
        
    print(f"Buscando {len(dict_secretaria)} itens da Secretaria no arquivo Excel...")
    
    # 2. Ler o arquivo Excel (o único disponível)
    df = pd.read_excel('docs/Relatorio-08.03.xls')
    
    # Converter a coluna NUMERO para string visando cross-check
    df['NUMERO'] = df['NUMERO'].astype(str).str.strip().str.replace(r'\.0$', '', regex=True)
    
    encontrados = 0
    nao_encontrados = 0
    
    print("\n--- ITENS DA SECRETARIA ENCONTRADOS NO EXCEL ---")
    for num, dados in dict_secretaria.items():
        # Procurar o numero no Excel
        match = df[df['NUMERO'] == num]
        if not match.empty:
            encontrados += 1
            sala_excel = match.iloc[0]['SALA']
            print(f"Patrimônio: {num}")
            print(f"  Desc BD: {dados['desc'][:60]}...")
            print(f"  Sala EXCEL: '{sala_excel}'")
            print("-" * 40)
        else:
            nao_encontrados += 1
            
    print(f"\nResumo:")
    print(f"Total pesquisado: {len(dict_secretaria)}")
    print(f"Encontrados no Excel: {encontrados}")
    print(f"NÃO encontrados no Excel: {nao_encontrados}")
    
except Exception as e:
    print(f"Error: {e}")
