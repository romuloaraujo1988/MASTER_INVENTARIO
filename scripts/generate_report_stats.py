import psycopg2
import sys
import json
from decimal import Decimal

# Custom JSON encoder for Decimal
class DecimalEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        return super(DecimalEncoder, self).default(obj)

def get_db_stats():
    try:
        conn = psycopg2.connect(
            host='localhost',
            database='sispatrimonio',
            user='postgres',
            password='Romulo@2020',
            port=5432
        )
        cur = conn.cursor()
        stats = {}
        
        cur.execute("SELECT count(*) FROM tabela_coleta;")
        stats['total_coletas'] = cur.fetchone()[0]
        
        cur.execute("SELECT count(*) FROM tabela_coleta_componente;")
        stats['total_componentes'] = cur.fetchone()[0]
        
        cur.execute("SELECT count(DISTINCT id_patrimonio) FROM tabela_coleta;")
        stats['patrimonios_unicos'] = cur.fetchone()[0]

        cur.execute("SELECT count(*) FROM tabela_patrimonio;")
        total_cadastrados = cur.fetchone()[0]
        stats['total_cadastrados'] = total_cadastrados
        
        cur.execute("SELECT count(*) FROM tabela_coleta WHERE sem_etiqueta = true;")
        sem_etiqueta_count = cur.fetchone()
        stats['sem_etiqueta'] = sem_etiqueta_count[0] if sem_etiqueta_count else 0
        
        try:
            cur.execute("SELECT avg(tempo_coleta_segundos) FROM tabela_coleta WHERE tempo_coleta_segundos IS NOT NULL;")
            avg_time = cur.fetchone()
            stats['tempo_medio'] = round(avg_time[0], 2) if avg_time and avg_time[0] else 0
        except psycopg2.Error:
            conn.rollback()
            stats['tempo_medio'] = 0

        # --- ESTADO DE CONSERVAÇÃO ---
        cur.execute("""
            SELECT 
                CASE 
                    WHEN estado_encontrado IN ('IRRECUPERAVEL', 'IRRECUPERÁVEL') THEN 'IRRECUPERÁVEL'
                    WHEN estado_encontrado IN ('RECUPERAVEL', 'RECUPERÁVEL') THEN 'RECUPERÁVEL'
                    WHEN estado_encontrado IS NULL OR trim(estado_encontrado) = '' THEN 'N/A'
                    ELSE upper(estado_encontrado)
                END as estado,
                count(*) as qtd
            FROM tabela_coleta
            GROUP BY 1
            ORDER BY 2 DESC;
        """)
        stats['estado_conservacao'] = cur.fetchall()

        # --- ANÁLISE ESPACIAL ---
        cur.execute("""
            SELECT 
                localizacao_encontrada,
                count(id) as coletas,
                sum(case when estado_encontrado = 'BOM' then 1 else 0 end) as qtd_bom,
                sum(case when estado_encontrado in ('IRRECUPERAVEL', 'IRRECUPERÁVEL') then 1 else 0 end) as qtd_irrecuperavel,
                sum(case when divergencia = true then 1 else 0 end) as qtd_divergencias,
                avg(tempo_coleta_segundos) as tempo_medio
            FROM tabela_coleta
            WHERE localizacao_encontrada IS NOT NULL
            GROUP BY localizacao_encontrada
            ORDER BY count(id) DESC
            LIMIT 5;
        """)
        stats['top_salas'] = cur.fetchall()

        # --- DIVERGÊNCIAS ---
        cur.execute("SELECT count(*) FROM tabela_coleta WHERE divergencia = true;")
        stats['total_divergencias'] = cur.fetchone()[0]

        cur.execute("""
            SELECT 
                localizacao_encontrada,
                count(id) as total_coletas,
                sum(case when divergencia = true then 1 else 0 end) as qtd_divergencias
            FROM tabela_coleta
            WHERE localizacao_encontrada IS NOT NULL
            GROUP BY localizacao_encontrada
            HAVING count(id) > 10
            ORDER BY sum(case when divergencia = true then 1 else 0 end) * 1.0 / count(id) DESC
            LIMIT 5;
        """)
        stats['salas_divergencia'] = cur.fetchall()

        cur.close()
        conn.close()
        
        with open('estatisticas.json', 'w', encoding='utf-8') as f:
            json.dump(stats, f, cls=DecimalEncoder, indent=4, ensure_ascii=False)
            
        print("Estatísticas extraídas e salvas em estatisticas.json")
    except Exception as e:
        print(f"Error querying DB: {e}", file=sys.stderr)

if __name__ == '__main__':
    get_db_stats()
