import psycopg2
import csv
import sys
import os

def export_to_csv():
    try:
        conn = psycopg2.connect(
            host='localhost',
            database='sispatrimonio',
            user='postgres',
            password='Romulo@2020',
            port=5432
        )
        cur = conn.cursor()
        
        # Ensure data directory exists
        os.makedirs('../data', exist_ok=True)
        
        # Exporting Coletas
        print("Exporting tabela_coleta...")
        cur.execute("SELECT * FROM tabela_coleta;")
        rows = cur.fetchall()
        colnames = [desc[0] for desc in cur.description]
        with open('../data/dataset_coletas.csv', 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(colnames)
            writer.writerows(rows)
            
        # Exporting Componentes
        print("Exporting tabela_coleta_componente...")
        cur.execute("SELECT * FROM tabela_coleta_componente;")
        rows = cur.fetchall()
        colnames = [desc[0] for desc in cur.description]
        with open('../data/dataset_componentes.csv', 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(colnames)
            writer.writerows(rows)

        # Exporting Patrimonios (to cross reference later)
        print("Exporting tabela_patrimonio...")
        cur.execute("SELECT * FROM tabela_patrimonio;")
        rows = cur.fetchall()
        colnames = [desc[0] for desc in cur.description]
        with open('../data/dataset_patrimonio.csv', 'w', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow(colnames)
            writer.writerows(rows)
            
        cur.close()
        conn.close()
        print("Export Completed Successfully. Datasets available in the 'data' folder.")
    except Exception as e:
        print(f"Error exporting data: {e}", file=sys.stderr)

if __name__ == '__main__':
    export_to_csv()
