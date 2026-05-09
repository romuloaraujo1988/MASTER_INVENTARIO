import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class InsertAdmin {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/sispatrimonio";
        String user = "postgres";
        String[] passwords = {"Romulo@2020", "ifmtpdl@2016", "postgres", "admin", "admin123", "root", ""};

        boolean connected = false;

        for (String pass : passwords) {
            System.out.println("Tentando conectar com a senha: " + (pass.isEmpty() ? "<vazia>" : pass));
            try {
                Class.forName("org.postgresql.Driver");
                try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                    connected = true;
                    System.out.println("Conexão bem-sucedida!");
                    
                    String sql = "INSERT INTO TABELA_USUARIO (" +
                                 "    LOGIN, SENHA_HASH, NOME_COMPLETO, EMAIL, PERFIL, ID_SETOR, ATIVO, PRIMEIRO_ACESSO" +
                                 ") SELECT " +
                                 "    'admin', " +
                                 "    '$2a$10$N9qo8uLOickgx2ZMRZoMye1VdLIqCRFpb6AQBaOLLqI.xjVgLOjHi', " +
                                 "    'Administrador do Sistema', " +
                                 "    'admin@sistema.com', " +
                                 "    'ADMIN', " +
                                 "    (SELECT ID FROM TABELA_SETOR WHERE NOME = 'Administração' LIMIT 1), " +
                                 "    'S', " +
                                 "    'S' " +
                                 "WHERE NOT EXISTS (" +
                                 "    SELECT 1 FROM TABELA_USUARIO WHERE LOGIN = 'admin'" +
                                 ")";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        int rows = stmt.executeUpdate();
                        if (rows > 0) {
                            System.out.println("Usuário 'admin' inserido com sucesso no banco de dados local.");
                        } else {
                            System.out.println("O usuário 'admin' já existia no banco de dados local.");
                        }
                    }
                    break;
                }
            } catch (Exception e) {
                // Ignore auth failures and try next
            }
        }

        if (!connected) {
            System.out.println("Falha ao conectar com todas as senhas conhecidas.");
        }
    }
}
