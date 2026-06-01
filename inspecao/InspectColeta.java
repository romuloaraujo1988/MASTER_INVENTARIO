import java.sql.*;

public class InspectColeta {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost:5432/sispatrimonio";
        String dbUser = System.getenv().getOrDefault("DATASOURCE_USERNAME", "postgres");
        String dbPassword = System.getenv("DATASOURCE_PASSWORD");
        if (dbPassword == null || dbPassword.isEmpty()) {
            System.err.println("Defina a variável de ambiente DATASOURCE_PASSWORD antes de executar.");
            return;
        }
        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword)) {

            System.out.println("=== Colunas da tabela_coleta ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT column_name, data_type, is_nullable " +
                    "FROM information_schema.columns " +
                    "WHERE table_name='tabela_coleta' " +
                    "ORDER BY ordinal_position")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("  %-40s %-20s nullable=%s%n",
                                rs.getString(1), rs.getString(2), rs.getString(3));
                    }
                }
            }

            System.out.println();
            System.out.println("=== Última coleta + valores ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, id_inventario, id_patrimonio, " +
                    "       localizacao_atual, localizacao_encontrada, " +
                    "       sem_etiqueta, data_coleta " +
                    "FROM tabela_coleta " +
                    "ORDER BY id DESC LIMIT 5")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf(
                                "id=%d inv=%d patr=%d loc_atual='%s' loc_encontrada='%s' sem_etiqueta=%s data=%s%n",
                                rs.getInt(1), rs.getInt(2), rs.getInt(3),
                                rs.getString(4), rs.getString(5),
                                rs.getBoolean(6), rs.getTimestamp(7));
                    }
                }
            }

            System.out.println();
            System.out.println("=== Comparação: localizacao_encontrada vs nome da sala da origem (top 10) ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT c.id, c.localizacao_encontrada, " +
                    "       p.id_sala AS sala_origem_id, " +
                    "       so.numero_sala AS sala_origem_num, " +
                    "       so.descricao AS sala_origem_desc " +
                    "FROM tabela_coleta c " +
                    "LEFT JOIN tabela_patrimonio p ON c.id_patrimonio = p.id " +
                    "LEFT JOIN tabela_sala so ON p.id_sala = so.id_sala " +
                    "ORDER BY c.id DESC LIMIT 10")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf(
                                "id=%d enc='%s' origem=%s/%s/%s%n",
                                rs.getInt("id"),
                                rs.getString("localizacao_encontrada"),
                                rs.getString("sala_origem_id"),
                                rs.getString("sala_origem_num"),
                                rs.getString("sala_origem_desc"));
                    }
                }
            }

            System.out.println();
            System.out.println("=== Colunas da tabela_sala (para entender o que casar com localizacao_encontrada) ===");
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT column_name, data_type " +
                    "FROM information_schema.columns " +
                    "WHERE table_name='tabela_sala' " +
                    "ORDER BY ordinal_position")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.printf("  %-30s %s%n", rs.getString(1), rs.getString(2));
                    }
                }
            }
        }
    }
}
