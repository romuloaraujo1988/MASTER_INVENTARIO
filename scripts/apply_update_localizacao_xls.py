"""
apply_update_localizacao_xls.py
Lê a coluna SALA do arquivo XLS e atualiza localizacao_atual no banco
para os registros com NULL, usando o número de patrimônio como chave.
"""
import sys, xlrd, psycopg2

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

XLS = r"c:\Users\Romulo\Documents\PROJETOS\MASTER_INVENTARIO\PATRIMONIO IFMT 25.05.2025.xls"
SEP = "-" * 70

CONN = dict(host='localhost', database='sispatrimonio',
            user='postgres', password='Romulo@2020', port=5432)

# --- Ler XLS --- coluna 1=NUMERO, coluna 16=SALA
wb = xlrd.open_workbook(XLS)
ws = wb.sheet_by_index(0)
headers = [str(ws.cell_value(0, j)).strip() for j in range(ws.ncols)]
NUM_COL  = headers.index('NUMERO')
SALA_COL = headers.index('SALA')
print(f"Colunas usadas: NUMERO=[{NUM_COL}]  SALA=[{SALA_COL}]")

xls_map = {}
for i in range(1, ws.nrows):
    num_raw  = ws.cell_value(i, NUM_COL)
    sala_raw = str(ws.cell_value(i, SALA_COL)).strip()
    try:
        num = str(int(float(num_raw)))
    except:
        num = str(num_raw).strip()
    if num and sala_raw and sala_raw != '-':
        xls_map[num] = sala_raw

print(f"Registros lidos do XLS: {len(xls_map)}")
print()

# --- Buscar 709 registros NULL do banco ---
conn = psycopg2.connect(**CONN)
cur  = conn.cursor()
cur.execute("""
    SELECT tc.id AS id_coleta, tp.numero
    FROM tabela_coleta tc
    JOIN tabela_patrimonio tp ON tp.id = tc.id_patrimonio
    WHERE tc.localizacao_atual IS NULL;
""")
banco = cur.fetchall()
print(f"Registros NULL no banco: {len(banco)}")

# --- Cruzar ---
updates, sem_sala, nao_encontrados = [], [], []
for id_coleta, num in banco:
    num_str = str(int(float(num))) if num else None
    sala = xls_map.get(num_str)
    if sala:
        updates.append((sala, id_coleta, num_str))
    elif num_str and num_str in xls_map:
        sem_sala.append((id_coleta, num_str))
    else:
        nao_encontrados.append((id_coleta, num_str))

print(f"Com sala no XLS     : {len(updates)}")
print(f"No XLS sem sala ('-'): {len(sem_sala)}")
print(f"Não encontrados     : {len(nao_encontrados)}")
print()

# Mostrar amostra
print("Amostra (primeiros 20 updates):")
for sala, id_col, num in updates[:20]:
    print(f"  id_coleta:{id_col:<6}  num:{num:<12}  sala:'{sala}'")

# Distribuição de salas
from collections import Counter
dist = Counter(sala for sala, _, _ in updates)
print()
print(f"Distribuição por sala ({len(dist)} salas):")
for sala, cnt in dist.most_common():
    print(f"  {cnt:4d}x  '{sala}'")

print()
print(SEP)
print("Aplicando UPDATE no banco...")
print(SEP)

updated = 0
for sala, id_coleta, num in updates:
    cur.execute(
        "UPDATE tabela_coleta SET localizacao_atual = %s WHERE id = %s AND localizacao_atual IS NULL;",
        (sala, id_coleta)
    )
    updated += cur.rowcount

conn.commit()
print(f"UPDATE concluído. Registros atualizados: {updated}")

# Verificar
cur.execute("SELECT COUNT(*) FROM tabela_coleta WHERE localizacao_atual IS NULL;")
restante = cur.fetchone()[0]
print(f"Ainda com NULL: {restante}")

cur.close()
conn.close()
