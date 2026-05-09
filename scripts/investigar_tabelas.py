import sys
import psycopg2

sys.stdout.reconfigure(encoding='utf-8')

conn_params = {
    "host": "10.14.150.14",
    "dbname": "sispatrimonio",
    "user": "postgres",
    "password": "ifmtpdl@2016",
    "port": 5432
}

try:
    conn = psycopg2.connect(**conn_params)
    cur = conn.cursor()
    print("=== INVESTIGACAO: ESTRUTURA DAS TABELAS ENVOLVIDAS ===\n")

    # Colunas da TABELA_PARTICIPANTE_INVENTARIO
    cur.execute("""
        SELECT column_name, data_type, is_nullable, column_default
        FROM information_schema.columns
        WHERE LOWER(table_name) = 'tabela_participante_inventario'
          AND table_schema = 'public'
        ORDER BY ordinal_position;
    """)
    cols = cur.fetchall()
    print("--- TABELA_PARTICIPANTE_INVENTARIO (colunas) ---")
    for c in cols:
        print("  {:<35} {:<20} nullable={} default={}".format(c[0], c[1], c[2], c[3]))

    print()

    # Colunas da TABELA_INVENTARIO_SETOR
    cur.execute("""
        SELECT column_name, data_type, is_nullable, column_default
        FROM information_schema.columns
        WHERE LOWER(table_name) = 'tabela_inventario_setor'
          AND table_schema = 'public'
        ORDER BY ordinal_position;
    """)
    cols2 = cur.fetchall()
    print("--- TABELA_INVENTARIO_SETOR (colunas) ---")
    for c in cols2:
        print("  {:<35} {:<20} nullable={} default={}".format(c[0], c[1], c[2], c[3]))

    print()

    # Colunas da TABELA_USUARIO (verificar campo 'ativo')
    cur.execute("""
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE LOWER(table_name) = 'tabela_usuario'
          AND table_schema = 'public'
        ORDER BY ordinal_position;
    """)
    cols3 = cur.fetchall()
    print("--- TABELA_USUARIO (colunas) ---")
    for c in cols3:
        print("  {:<35} {:<20} nullable={}".format(c[0], c[1], c[2]))

    print()

    # Verificar dados reais: participantes e inventários
    cur.execute("SELECT id, nome, status_inventario FROM tabela_inventario ORDER BY id DESC LIMIT 5;")
    invs = cur.fetchall()
    print("--- Ultimos 5 inventarios ---")
    for r in invs:
        print("  ID={} | Nome={} | Status={}".format(r[0], r[1], r[2]))

    print()

    # Ver participantes dos inventarios recentes
    cur.execute("""
        SELECT pi.id_inventario, pi.id_usuario, pi.papel, pi.ativo,
               u.nome_completo, u.ativo as usuario_ativo
        FROM tabela_participante_inventario pi
        LEFT JOIN tabela_usuario u ON pi.id_usuario = u.id
        ORDER BY pi.id_inventario DESC
        LIMIT 20;
    """)
    parts = cur.fetchall()
    print("--- Ultimos participantes registrados ---")
    if parts:
        for r in parts:
            print("  inv={} usr={} papel={} part_ativo={} nome={} usr_ativo={}".format(
                r[0], r[1], r[2], r[3], r[4], r[5]))
    else:
        print("  Nenhum participante encontrado!")

    print()

    # Ver configuracoes de setor dos inventarios recentes
    cur.execute("""
        SELECT id_inventario, id_setor, incluir_todos_setores, ativo
        FROM tabela_inventario_setor
        ORDER BY id_inventario DESC
        LIMIT 20;
    """)
    setores = cur.fetchall()
    print("--- Ultimas configuracoes de setor ---")
    if setores:
        for r in setores:
            print("  inv={} setor={} incluir_todos={} ativo={}".format(r[0], r[1], r[2], r[3]))
    else:
        print("  Nenhuma configuracao de setor encontrada!")

    print()

    # Verificar constraints e chaves estrangeiras da tabela_participante_inventario
    cur.execute("""
        SELECT tc.constraint_name, tc.constraint_type, kcu.column_name,
               ccu.table_name AS foreign_table, ccu.column_name AS foreign_column
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
        LEFT JOIN information_schema.referential_constraints rc
          ON tc.constraint_name = rc.constraint_name
        LEFT JOIN information_schema.constraint_column_usage ccu
          ON rc.unique_constraint_name = ccu.constraint_name
        WHERE LOWER(tc.table_name) = 'tabela_participante_inventario'
        ORDER BY tc.constraint_type, tc.constraint_name;
    """)
    constraints = cur.fetchall()
    print("--- Constraints TABELA_PARTICIPANTE_INVENTARIO ---")
    for c in constraints:
        print("  {} | {} | col={} | fk->{}({})".format(c[0], c[1], c[2], c[3], c[4]))

    cur.close()
    conn.close()

except Exception as e:
    print("ERRO: {}".format(repr(e)))
