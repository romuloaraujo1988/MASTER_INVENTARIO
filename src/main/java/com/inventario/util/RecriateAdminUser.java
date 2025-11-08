package com.inventario.util;

import com.inventario.dao.UsuarioDAORefactored;
import com.inventario.model.Usuario;
import com.inventario.model.PerfilUsuario;
import com.inventario.service.AutenticacaoServiceDB;
import java.time.LocalDateTime;

/**
 * Utilitário para recriar o usuário administrador no banco de dados
 * @author Sistema de Inventário
 */
public class RecriateAdminUser {
    
    /**
     * Busca usuário por login independente do status ativo
     */
    private static Usuario buscarUsuarioPorLoginCompleto(UsuarioDAORefactored usuarioDAO, String login) {
        String sql = "SELECT u.*, s.NOME as NOME_SETOR FROM TABELA_USUARIO u " +
                    "LEFT JOIN TABELA_SETOR s ON u.ID_SETOR = s.ID " +
                    "WHERE u.LOGIN = ?";
        
        try (java.sql.Connection conn = com.inventario.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("ID"));
                    usuario.setLogin(rs.getString("LOGIN"));
                    usuario.setSenhaHash(rs.getString("SENHA_HASH"));
                    usuario.setNomeCompleto(rs.getString("NOME_COMPLETO"));
                    usuario.setEmail(rs.getString("EMAIL"));
                    usuario.setMatricula(rs.getString("CPF"));
                    
                    String perfilStr = rs.getString("PERFIL");
                    if (perfilStr != null) {
                        try {
                            usuario.setPerfil(PerfilUsuario.valueOf(perfilStr));
                        } catch (IllegalArgumentException e) {
                            usuario.setPerfil(PerfilUsuario.ADMIN);
                        }
                    }
                    
                    usuario.setIdSetor(rs.getObject("ID_SETOR", Integer.class));
                    usuario.setAtivo(rs.getBoolean("ATIVO"));
                    usuario.setBloqueado(rs.getBoolean("BLOQUEADO"));
                    usuario.setPrimeiroAcesso(rs.getBoolean("PRIMEIRO_ACESSO"));
                    usuario.setTentativasLogin(rs.getInt("TENTATIVAS_LOGIN"));
                    
                    java.sql.Timestamp dataCriacao = rs.getTimestamp("DATA_CRIACAO");
                    if (dataCriacao != null) {
                        usuario.setDataCriacao(dataCriacao.toLocalDateTime());
                    }
                    
                    return usuario;
                }
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("Erro ao buscar usuário por login completo: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== RECRIANDO USUÁRIO ADMINISTRADOR ===");
            
            UsuarioDAORefactored usuarioDAO = new UsuarioDAORefactored();
            AutenticacaoServiceDB authService = new AutenticacaoServiceDB();
            
            // 1. Verificar se usuário admin existe (buscar independente do status)
            Usuario adminExistente = buscarUsuarioPorLoginCompleto(usuarioDAO, "admin");
            boolean sucesso = false;
            
            if (adminExistente != null) {
                System.out.println("Atualizando usuário admin existente...");
                
                // Atualizar usuário existente
                adminExistente.setSenhaHash("admin123"); // Senha em texto plano
                adminExistente.setNomeCompleto("Administrador do Sistema");
                adminExistente.setEmail("admin@ifmt.edu.br");
                adminExistente.setMatricula("00000000000");
                adminExistente.setPerfil(PerfilUsuario.ADMIN);
                adminExistente.setAtivo(true);
                adminExistente.setBloqueado(false);
                adminExistente.setPrimeiroAcesso(true);
                adminExistente.setTentativasLogin(0);
                
                sucesso = usuarioDAO.atualizarUsuario(adminExistente);
                
            } else {
                System.out.println("Criando novo usuário administrador...");
                
                // Criar novo usuário admin
                Usuario novoAdmin = new Usuario();
                novoAdmin.setLogin("admin");
                novoAdmin.setSenhaHash("admin123"); // Senha em texto plano
                novoAdmin.setNomeCompleto("Administrador do Sistema");
                novoAdmin.setEmail("admin@ifmt.edu.br");
                novoAdmin.setMatricula("00000000000");
                novoAdmin.setPerfil(PerfilUsuario.ADMIN);
                novoAdmin.setAtivo(true);
                novoAdmin.setBloqueado(false);
                novoAdmin.setPrimeiroAcesso(true);
                novoAdmin.setTentativasLogin(0);
                novoAdmin.setDataCriacao(LocalDateTime.now());
                
                sucesso = usuarioDAO.inserirUsuario(novoAdmin);
            }
            
            if (sucesso) {
                System.out.println("✅ USUÁRIO ADMIN CRIADO COM SUCESSO!");
                System.out.println("Login: admin");
                System.out.println("Senha: admin123");
                
                // 4. Verificar se foi criado corretamente
                Usuario verificacao = usuarioDAO.buscarUsuarioPorLogin("admin");
                if (verificacao != null) {
                    System.out.println("\n=== VERIFICAÇÃO ===");
                    System.out.println("ID: " + verificacao.getId());
                    System.out.println("Login: " + verificacao.getLogin());
                    System.out.println("Nome: " + verificacao.getNomeCompleto());
                    System.out.println("Email: " + verificacao.getEmail());
                    System.out.println("Perfil: " + verificacao.getPerfil());
                    System.out.println("Ativo: " + verificacao.getAtivo());
                    System.out.println("Bloqueado: " + verificacao.getBloqueado());
                    System.out.println("Senha Hash: " + verificacao.getSenhaHash());
                    
                    // 5. Testar autenticação
                    System.out.println("\n=== TESTE DE AUTENTICAÇÃO ===");
                    Usuario usuarioAutenticado = authService.autenticar("admin", "admin123");
                    if (usuarioAutenticado != null) {
                        System.out.println("✅ AUTENTICAÇÃO FUNCIONANDO!");
                        System.out.println("Usuário autenticado: " + usuarioAutenticado.getNomeCompleto());
                    } else {
                        System.out.println("❌ ERRO NA AUTENTICAÇÃO!");
                    }
                } else {
                    System.out.println("❌ ERRO: Usuário não foi encontrado após criação!");
                }
            } else {
                System.out.println("❌ ERRO AO CRIAR USUÁRIO ADMIN!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERRO DURANTE EXECUÇÃO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}