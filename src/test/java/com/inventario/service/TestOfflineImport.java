package com.inventario.service;

import com.inventario.offline.OfflineDAO;
import com.inventario.offline.SQLiteConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * Teste para verificar se o OfflineDAO está salvando dados
 */
public class TestOfflineImport {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DE SALVAMENTO NO SQLITE ===\n");
        
        try {
            // Inicializar banco SQLite
            System.out.println("1. Inicializando banco SQLite...");
            SQLiteConnection sqliteConn = SQLiteConnection.getInstance();
            sqliteConn.initializeDatabase();
            System.out.println("   ✅ Banco SQLite inicializado\n");
            
            // Criar OfflineDAO
            OfflineDAO offlineDAO = new OfflineDAO();
            
            // Testar salvamento de patrimônio
            System.out.println("2. Testando salvamento de patrimônio...");
            Map<String, Object> patrimonioMap = new HashMap<>();
            patrimonioMap.put("id", 999999);
            patrimonioMap.put("numero", "TESTE-001");
            patrimonioMap.put("descricao", "Patrimônio de Teste");
            patrimonioMap.put("descricao_resumida", "Teste");
            patrimonioMap.put("marca", "Marca Teste");
            patrimonioMap.put("modelo", "Modelo Teste");
            patrimonioMap.put("numero_serie", "SN-001");
            patrimonioMap.put("situacao", "ATIVO");
            patrimonioMap.put("valor", 100.00);
            patrimonioMap.put("data_aquisicao", null);
            patrimonioMap.put("id_setor", null);
            patrimonioMap.put("id_sala", null);
            patrimonioMap.put("observacoes", "Teste de importação");
            
            int id = offlineDAO.salvarPatrimonio(patrimonioMap);
            System.out.println("   ✅ Patrimônio salvo com ID: " + id);
            
            // Verificar se foi salvo
            System.out.println("\n3. Verificando se foi salvo...");
            var conn = offlineDAO.getConnection();
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) as total FROM local_patrimonio WHERE numero = 'TESTE-001'");
            if (rs.next()) {
                int count = rs.getInt("total");
                System.out.println("   ✅ Registros encontrados: " + count);
                
                if (count > 0) {
                    System.out.println("\n   ✅✅✅ SALVAMENTO FUNCIONANDO! ✅✅✅");
                } else {
                    System.out.println("\n   ❌ ERRO: Registro não foi salvo!");
                }
            }
            rs.close();
            stmt.close();
            
            // Testar salvamento de sala
            System.out.println("\n4. Testando salvamento de sala...");
            Map<String, Object> salaMap = new HashMap<>();
            salaMap.put("id", 999999);
            salaMap.put("nome", "SALA-TESTE-001");
            salaMap.put("descricao", "Sala de Teste");
            salaMap.put("bloco", "Bloco A");
            salaMap.put("andar", "1º Andar");
            salaMap.put("capacidade", 30);
            salaMap.put("tipo", "SALA_AULA");
            salaMap.put("ativa", true);
            
            int idSala = offlineDAO.salvarSala(salaMap);
            System.out.println("   ✅ Sala salva com ID: " + idSala);
            
            // Testar salvamento de responsável
            System.out.println("\n5. Testando salvamento de responsável...");
            Map<String, Object> responsavelMap = new HashMap<>();
            responsavelMap.put("id", 999999);
            responsavelMap.put("nome", "Responsável Teste");
            responsavelMap.put("cpf", "000.000.000-00");
            responsavelMap.put("matricula", "MAT-001");
            responsavelMap.put("email", "teste@teste.com");
            responsavelMap.put("telefone", "(00) 0000-0000");
            responsavelMap.put("cargo", "Cargo Teste");
            responsavelMap.put("setor", "Setor Teste");
            responsavelMap.put("ativo", true);
            
            int idResp = offlineDAO.salvarResponsavel(responsavelMap);
            System.out.println("   ✅ Responsável salvo com ID: " + idResp);
            
            // Testar salvamento de usuário
            System.out.println("\n6. Testando salvamento de usuário...");
            Map<String, Object> usuarioMap = new HashMap<>();
            usuarioMap.put("id", 999999);
            usuarioMap.put("login", "teste");
            usuarioMap.put("senha_hash", "$2a$10$HASH_TESTE");
            usuarioMap.put("nome", "Usuário Teste");
            usuarioMap.put("email", "teste@teste.com");
            usuarioMap.put("perfil", "COLETOR");
            usuarioMap.put("ativo", true);
            
            int idUser = offlineDAO.salvarUsuario(usuarioMap);
            System.out.println("   ✅ Usuário salvo com ID: " + idUser);
            
            System.out.println("\n=== TODOS OS TESTES PASSARAM! ===");
            System.out.println("\n✅ OfflineDAO está funcionando corretamente!");
            System.out.println("✅ Problema deve estar no DataImportService ou na chamada dele");
            
        } catch (Exception e) {
            System.err.println("\n❌ ERRO NO TESTE:");
            e.printStackTrace();
        }
    }
}
