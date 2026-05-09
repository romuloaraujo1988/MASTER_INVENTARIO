import psycopg2
import sys

def main():
    try:
        conn = psycopg2.connect(
            host='localhost',
            database='sispatrimonio',
            user='postgres',
            password='Romulo@2020',
            port=5432
        )
        cur = conn.cursor()
        
        cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public';")
        tables = [t[0] for t in cur.fetchall()]
        
        with open('coletas_info.txt', 'w', encoding='utf-8') as f:
            f.write("Tables in 'sispatrimonio' public schema:\n")
            f.write(", ".join(tables) + "\n\n")
            
            # also search for tables containing 'colet'
            colet_tables = [t for t in tables if 'coleta' in t.lower() or 'colet' in t.lower()]
            if colet_tables:
                for t in colet_tables:
                    cur.execute(f"SELECT count(*) FROM {t};")
                    count = cur.fetchone()[0]
                    f.write(f"Total rows in '{t}': {count}\n")
                    
                    # Check columns of the table
                    cur.execute(f"SELECT column_name, data_type FROM information_schema.columns WHERE table_name='{t}'")
                    cols = cur.fetchall()
                    f.write(f"Columns in {t}: {[c[0] for c in cols]}\n")

        cur.close()
        conn.close()
    except Exception as e:
        print(f"Error connecting to DB: {e}", file=sys.stderr)

if __name__ == '__main__':
    main()
