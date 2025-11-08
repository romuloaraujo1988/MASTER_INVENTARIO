package com.inventario.test;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.model.Usuario;
import com.inventario.model.Inventario;
import com.inventario.util.DatabaseConnection;

import java.sql.Connection;

public class TesteAdminColeta {
    
    public static void main(String[] args) {
        System.out.println("=== TESTE DE AUTORIZAÇÃO DO ADMIN PARA COLETAS ===\n");
        
        try (Connection connection = DatabaseConnection.getConnection()) {
            // Inicializar DAOs
            UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
            InventarioDAO inventarioDAO = new InventarioDAO();
            ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
            
            // 1. Verificar se o usuário admin existe
            System.out.println("1. Verificando usuário admin...");
            Usuario admin = usuarioDAO.buscarUsuarioPorLogin("admin");
            
            if (admin == null) {
                System.out.println("❌ ERRO: Usuário 'admin' não encontrado!");
                return;
            }
            
            System.out.println("✅ Usuário admin encontrado:");
            System.out.println("   - ID: " + admin.getId());
            System.out.println("   - Login: " + admin.getLogin());
            System.out.println("   - Nome: " + admin.getNomeCompleto());
            System.out.println("   - Ativo: " + admin.getAtivo());
            System.out.println("   - Perfil: " + admin.getPerfil());
            
            if (!admin.getAtivo()) {
                System.out.println("❌ ERRO: Usuário admin está INATIVO!");
                return;
            }
            
            // 2. Buscar inventário ativo
            System.out.println("\n2. Verificando inventário ativo...");
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
            
            if (inventarioAtivo == null) {
                System.out.println("❌ ERRO: Nenhum inventário ativo encontrado!");
                return;
            }
            
            System.out.println("✅ Inventário ativo encontrado:");
            System.out.println("   - ID: " + inventarioAtivo.getId());
            System.out.println("   - Nome: " + inventarioAtivo.getNome());
            System.out.println("   - Status: " + inventarioAtivo.getStatusInventario());
            
            // 3. Verificar se admin é participante do inventário
            System.out.println("\n3. Verificando participação do admin no inventário...");
            Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
                inventarioAtivo.getId(),
                admin.getId()
            );
            
            if (idParticipante == null) {
                System.out.println("❌ ERRO: Admin NÃO é participante do inventário ativo!");
                System.out.println("   O admin precisa ser adicionado como participante para realizar coletas.");
                return;
            }
            
            System.out.println("✅ Admin é participante do inventário:");
            System.out.println("   - ID Participante: " + idParticipante);
            
            // 4. Resultado final
            System.out.println("\n=== RESULTADO FINAL ===");
            System.out.println("✅ ADMIN AUTORIZADO para realizar coletas!");
            System.out.println("   - Usuário: " + admin.getLogin() + " (ID: " + admin.getId() + ")");
            System.out.println("   - Inventário: " + inventarioAtivo.getNome() + " (ID: " + inventarioAtivo.getId() + ")");
            System.out.println("   - Participante ID: " + idParticipante);
            System.out.println("   - Status: PODE REALIZAR COLETAS ✅");
            
        } catch (Exception e) {
            System.err.println("❌ ERRO durante o teste: " + e.getMessage());
            e.printStackTrace();
        }
    }
}