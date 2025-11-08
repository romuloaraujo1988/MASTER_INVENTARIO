package com.inventario.test;

import com.inventario.dao.ParticipanteInventarioDAO;
import com.inventario.dao.InventarioDAO;
import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.Inventario;
import com.inventario.model.Usuario;

/**
 * Teste completo simulando o processo de coleta
 */
public class TesteColetaCompleto {
    
    public static void main(String[] args) {
        System.out.println("=== Teste Completo do Processo de Coleta ===");
        System.out.println();
        
        // Simular o processo exato que acontece no ColetaFrame_v2
        testarProcessoColetaCompleto();
    }
    
    private static void testarProcessoColetaCompleto() {
        try {
            // 1. Buscar usuário logado (simulando daniel.rezende)
            UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
            Usuario usuarioLogado = usuarioDAO.buscarUsuarioPorLogin("daniel.rezende");
            
            if (usuarioLogado == null) {
                System.out.println("❌ ERRO: Usuário 'daniel.rezende' não encontrado!");
                return;
            }
            
            System.out.println("✅ Usuário logado encontrado:");
            System.out.println("   - ID: " + usuarioLogado.getId());
            System.out.println("   - Login: " + usuarioLogado.getLogin());
            System.out.println("   - Nome: " + usuarioLogado.getNomeCompleto());
            System.out.println("   - Perfil: " + usuarioLogado.getPerfil());
            System.out.println("   - Ativo: " + usuarioLogado.getAtivo());
            System.out.println();
            
            // 2. Buscar inventário ativo (exatamente como no código)
            InventarioDAO inventarioDAO = new InventarioDAO();
            Inventario inventarioAtivo = inventarioDAO.buscarInventarioPorStatus("EM_ANDAMENTO");
            
            if (inventarioAtivo == null) {
                System.out.println("❌ ERRO: Nenhum inventário ativo encontrado!");
                return;
            }
            
            System.out.println("✅ Inventário ativo encontrado:");
            System.out.println("   - ID: " + inventarioAtivo.getId());
            System.out.println("   - Nome: " + inventarioAtivo.getNome());
            System.out.println("   - Status: " + inventarioAtivo.getStatusInventario());
            System.out.println();
            
            // 3. Verificar se o usuário é participante ativo (exatamente como no código)
            ParticipanteInventarioDAO participanteDAO = new ParticipanteInventarioDAO();
            Integer idParticipante = participanteDAO.buscarIdParticipantePorUsuario(
                    inventarioAtivo.getId(), usuarioLogado.getId());
            
            System.out.println("🔍 Verificação de autorização:");
            System.out.println("   - ID Inventário: " + inventarioAtivo.getId());
            System.out.println("   - ID Usuário: " + usuarioLogado.getId());
            System.out.println("   - ID Participante retornado: " + idParticipante);
            System.out.println();
            
            // 4. Resultado final
            if (idParticipante == null) {
                System.out.println("❌ ACESSO NEGADO!");
                System.out.println("   Mensagem que seria exibida:");
                System.out.println("   \"Você não está habilitado para realizar coletas neste inventário.\"");
                System.out.println("   \"Entre em contato com o coordenador do inventário para obter as permissões necessárias.\"");
            } else {
                System.out.println("✅ ACESSO AUTORIZADO!");
                System.out.println("   O usuário PODE realizar coletas neste inventário.");
                System.out.println("   ID do Participante: " + idParticipante);
            }
            
        } catch (Exception e) {
            System.out.println("❌ ERRO durante o teste:");
            System.out.println("   Mensagem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}