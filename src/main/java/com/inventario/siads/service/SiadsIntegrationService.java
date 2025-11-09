package com.inventario.siads.service;

import com.inventario.siads.dao.SiadsPatrimonioDAO;
import com.inventario.siads.config.SiadsConfig;
import com.inventario.model.Patrimonio;
import com.inventario.siads.model.SiadsRegistro;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Serviço principal de integração com SIADS
 * Orquestra a conversão e exportação de dados
 */
public class SiadsIntegrationService {
    
    private final SiadsPatrimonioDAO patrimonioDAO;
    private final SiadsConverterService converterService;
    private final SiadsExportService exportService;
    private final SiadsConfig config;
    
    public SiadsIntegrationService() {
        this.patrimonioDAO = new SiadsPatrimonioDAO();
        this.converterService = new SiadsConverterService();
        this.exportService = new SiadsExportService();
        this.config = new SiadsConfig();
        
        // Configura códigos institucionais
        exportService.configurarInstituicao(
            config.getCodigoOrgao(),
            config.getCodigoUG(),
            config.getCpfResponsavel()
        );
    }
    
    /**
     * Retorna a configuração SIADS
     */
    public SiadsConfig getConfig() {
        return config;
    }
    
    /**
     * Gera arquivo SIADS com todos os patrimônios ativos
     * 
     * @param diretorioDestino Diretório onde o arquivo será salvo
     * @return Arquivo gerado
     * @throws Exception Se houver erro no processo
     */
    public File gerarArquivoSiads(File diretorioDestino) throws Exception {
        try {
            // 1. Buscar patrimônios do banco com relacionamentos
            List<Patrimonio> patrimonios = patrimonioDAO.listarTodosParaSiads();
            
            if (patrimonios.isEmpty()) {
                throw new IllegalStateException("Nenhum patrimônio encontrado para exportação");
            }
            
            // 2. Converter para formato SIADS
            List<SiadsRegistro> registros = converterService.converterPatrimonios(patrimonios);
            
            // 3. Exportar arquivo
            File arquivo = exportService.exportarArquivo(registros, diretorioDestino);
            
            return arquivo;
            
        } catch (SQLException e) {
            throw new Exception("Erro ao buscar patrimônios do banco de dados: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera arquivo SIADS com filtro de inventário
     * 
     * @param diretorioDestino Diretório de destino
     * @param idInventario ID do inventário específico
     * @return Arquivo gerado
     * @throws Exception Se houver erro
     */
    public File gerarArquivoSiadsPorInventario(File diretorioDestino, Long idInventario) 
            throws Exception {
        
        try {
            // Buscar patrimônios do inventário específico
            List<Patrimonio> patrimonios = patrimonioDAO.listarPorInventario(idInventario);
            
            if (patrimonios.isEmpty()) {
                throw new IllegalStateException(
                    "Nenhum patrimônio encontrado para o inventário especificado");
            }
            
            // Converter e exportar
            List<SiadsRegistro> registros = converterService.converterPatrimonios(patrimonios);
            File arquivo = exportService.exportarArquivo(registros, diretorioDestino);
            
            return arquivo;
            
        } catch (SQLException e) {
            throw new Exception("Erro ao buscar patrimônios do inventário: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera arquivo SIADS por setor
     * 
     * @param diretorioDestino Diretório de destino
     * @param idSetor ID do setor
     * @return Arquivo gerado
     * @throws Exception Se houver erro
     */
    public File gerarArquivoSiadsPorSetor(File diretorioDestino, int idSetor) 
            throws Exception {
        
        try {
            List<Patrimonio> patrimonios = patrimonioDAO.listarPorSetor(idSetor);
            
            if (patrimonios.isEmpty()) {
                throw new IllegalStateException(
                    "Nenhum patrimônio encontrado para o setor especificado");
            }
            
            List<SiadsRegistro> registros = converterService.converterPatrimonios(patrimonios);
            File arquivo = exportService.exportarArquivo(registros, diretorioDestino);
            
            return arquivo;
            
        } catch (SQLException e) {
            throw new Exception("Erro ao buscar patrimônios do setor: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valida se os dados estão prontos para exportação SIADS
     * 
     * @return Lista de mensagens de validação (vazia se tudo OK)
     */
    public List<String> validarDadosParaExportacao() {
        List<String> erros = new java.util.ArrayList<>();
        
        try {
            List<Patrimonio> patrimonios = patrimonioDAO.listarTodosParaSiads();
            
            for (Patrimonio p : patrimonios) {
                // Validar campos obrigatórios
                if (p.getNumero() == null || p.getNumero().trim().isEmpty()) {
                    erros.add("Patrimônio sem número: ID " + p.getId());
                }
                
                if (p.getDescricao() == null || p.getDescricao().trim().isEmpty()) {
                    erros.add("Patrimônio sem descrição: " + p.getNumero());
                }
                
                if (p.getValorAquisicao() == null) {
                    erros.add("Patrimônio sem valor: " + p.getNumero());
                }
                
                if (p.getIdResponsavel() <= 0) {
                    erros.add("Patrimônio sem responsável: " + p.getNumero());
                }
                
                if (p.getNomeResponsavel() == null || p.getNomeResponsavel().trim().isEmpty()) {
                    erros.add("Responsável sem nome cadastrado: " + p.getNumero());
                }
            }
            
        } catch (Exception e) {
            erros.add("Erro ao validar dados: " + e.getMessage());
        }
        
        return erros;
    }
    
    /**
     * Exibe estatísticas sobre a exportação
     */
    public String gerarEstatisticas() {
        try {
            long total = patrimonioDAO.contarPatrimoniosAtivosParaSiads();
            long comResponsavel = patrimonioDAO.contarPatrimoniosComResponsavel();
            long comLocalizacao = patrimonioDAO.contarPatrimoniosComLocalizacao();
            
            if (total == 0) {
                return "Nenhum patrimônio ativo encontrado no sistema.";
            }
            
            return String.format(
                "Total de patrimônios ativos: %d\n" +
                "Com responsável: %d (%.1f%%)\n" +
                "Com localização: %d (%.1f%%)",
                total,
                comResponsavel, (comResponsavel * 100.0 / total),
                comLocalizacao, (comLocalizacao * 100.0 / total)
            );
            
        } catch (SQLException e) {
            return "Erro ao gerar estatísticas: " + e.getMessage();
        }
    }
}
