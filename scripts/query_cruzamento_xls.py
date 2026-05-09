"""
Lê o arquivo PATRIMONIO IFMT 25.05.2025.xls, identifica a estrutura,
e cruza os números de patrimônio dos 747 itens com NULL localizacao_atual
para descobrir a sala correta de cada um.
"""
import sys, psycopg2, openpyxl
try:
    import xlrd
except ImportError:
    pass

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

XLS = r"c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\PATRIMONIO IFMT 25.05.2025.xls"
SEP = "-" * 70

# --- Tentar ler com xlrd (formato .xls antigo) ---
try:
    import xlrd
    wb = xlrd.open_workbook(XLS)
    print(f"Aberto com xlrd. Sheets: {wb.sheet_names()}")
    ws = wb.sheet_by_index(0)
    print(f"Linhas: {ws.nrows}  Colunas: {ws.ncols}")
    print()
    print("Primeiras 3 linhas:")
    for i in range(min(3, ws.nrows)):
        row = [str(ws.cell_value(i, j))[:25] for j in range(ws.ncols)]
        print(f"  [{i}] {row}")

    print()
    print("Cabeçalho (linha 0):")
    headers = [str(ws.cell_value(0, j)).strip() for j in range(ws.ncols)]
    for i, h in enumerate(headers):
        print(f"  [{i}] {h}")

    # Detectar colunas de número e sala/localização
    num_col = next((i for i, h in enumerate(headers)
                    if any(k in h.upper() for k in ['NUMERO','NÚMERO','NUM.','TOMBO','PATRIMONIO','PATRIMÔNIO'])), None)
    sala_col = next((i for i, h in enumerate(headers)
                     if any(k in h.upper() for k in ['SALA','LOCAL','SETOR','DEPENDÊNCIA','DEPENDEN','UNIDADE'])), None)

    print()
    print(f"Coluna número patrimônio: [{num_col}] = '{headers[num_col] if num_col is not None else '?'}'")
    print(f"Coluna sala/localização : [{sala_col}] = '{headers[sala_col] if sala_col is not None else '?'}'")

    if num_col is not None and sala_col is not None:
        # Ler um mapa numero→sala
        xls_map = {}
        for i in range(1, ws.nrows):
            num_raw = ws.cell_value(i, num_col)
            sala_raw = ws.cell_value(i, sala_col)
            # Normalizar número
            try:
                num = str(int(float(num_raw))) if num_raw else None
            except:
                num = str(num_raw).strip()
            if num:
                xls_map[num] = str(sala_raw).strip()

        print(f"\nTotal de registros lidos do XLS: {len(xls_map)}")
        print("Amostra do mapa (número → sala):")
        for num, sala in list(xls_map.items())[:10]:
            print(f"  {num:<15} → {sala}")

        # Cruzar com os 747 do banco
        conn = psycopg2.connect(host='localhost', database='sispatrimonio',
                                user='postgres', password='Romulo@2020', port=5432)
        cur = conn.cursor()
        cur.execute("""
            SELECT tc.id, tp.numero
            FROM tabela_coleta tc
            JOIN tabela_patrimonio tp ON tp.id = tc.id_patrimonio
            WHERE tc.localizacao_atual IS NULL;
        """)
        banco = cur.fetchall()
        print(f"\nTotal de registros NULL no banco: {len(banco)}")

        encontrados, nao_encontrados = [], []
        for tc_id, num in banco:
            num_str = str(int(float(num))) if num else None
            sala = xls_map.get(num_str)
            if sala:
                encontrados.append((tc_id, num_str, sala))
            else:
                nao_encontrados.append((tc_id, num_str))

        print(f"Encontrados no XLS: {len(encontrados)}")
        print(f"Não encontrados   : {len(nao_encontrados)}")
        print()
        print("Amostra (encontrados):")
        for r in encontrados[:15]:
            print(f"  id_coleta:{r[0]}  num:{r[1]:<12} → sala:'{r[2]}'")
        if nao_encontrados:
            print(f"\nNão encontrados (primeiros 10):")
            for r in nao_encontrados[:10]:
                print(f"  id_coleta:{r[0]}  num:{r[1]}")

        # Distribuição de salas dos encontrados
        from collections import Counter
        dist = Counter(sala for _, _, sala in encontrados)
        print()
        print(f"Distribuição por sala ({len(dist)} salas únicas):")
        for sala, cnt in dist.most_common(20):
            print(f"  {cnt:4d}x  '{sala}'")

        cur.close()
        conn.close()

except Exception as e:
    print(f"Erro: {e}")

    # Tentar com pandas como fallback
    try:
        import pandas as pd
        df = pd.read_excel(XLS, engine='xlrd')
        print("Lido com pandas. Shape:", df.shape)
        print("Colunas:", list(df.columns))
        print(df.head(3))
    except Exception as e2:
        print(f"Pandas também falhou: {e2}")
