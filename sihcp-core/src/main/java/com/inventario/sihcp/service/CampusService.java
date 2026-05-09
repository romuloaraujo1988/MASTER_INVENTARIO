package com.inventario.sihcp.service;

import java.util.List;
import java.util.regex.Pattern;

import com.inventario.sihcp.dao.CampusDAO;
import com.inventario.sihcp.model.Campus;

/**
 * Serviço para gerenciamento de Campus
 * Implementa regras de negócio e validações para operações com Campus
 * 
 * @author Sistema de Inventário
 * @version 1.0.0
 */
public class CampusService {
    
    private CampusDAO campusDAO;
    
    // Padrão para validação de CNPJ - aceita com ou sem formatação
    // Formatos aceitos: XX.XXX.XXX/XXXX-XX ou XXXXXXXXXXXXXX (apenas números)
    private static final Pattern CNPJ_PATTERN = Pattern.compile(
        "^(\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|\\d{14})$"
    );
    
    // Padrão para validação de email - mais flexível
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // Padrão para validação de telefone - aceita vários formatos
    // Formatos aceitos: (XX) XXXXX-XXXX, (XX) XXXX-XXXX, XX XXXXX-XXXX, XXXXXXXXXXX
    private static final Pattern TELEFONE_PATTERN = Pattern.compile(
        "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{2}\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$"
    );
    
    public CampusService() {
        this.campusDAO = new CampusDAO();
    }
    
    /**
     * Insere um novo campus no sistema
     * @param campus Campus a ser inserido
     * @return ID do campus inserido
     * @throws IllegalArgumentException se a validação falhar
     */
    public Integer inserirCampus(Campus campus) {
        validarCampusComExcecao(campus);
        
        // Verificar se já existe campus com o mesmo nome
        if (campusDAO.campusExistePorNome(campus.getNome())) {
            throw new IllegalArgumentException("Já existe um campus com o nome: " + campus.getNome());
        }
        
        // Verificar se já existe campus com o mesmo CNPJ
        if (campus.getCnpj() != null && !campus.getCnpj().trim().isEmpty()) {
            if (campusDAO.campusExistePorCnpj(campus.getCnpj())) {
                throw new IllegalArgumentException("Já existe um campus com o CNPJ: " + campus.getCnpj());
            }
        }
        
        Integer id = campusDAO.inserirCampus(campus);
        if (id == null) {
            throw new RuntimeException("Erro ao inserir campus no banco de dados");
        }
        return id;
    }
    
    /**
     * Atualiza um campus existente
     * @param campus Campus a ser atualizado
     * @return true se atualizado com sucesso
     * @throws IllegalArgumentException se a validação falhar
     */
    public boolean atualizarCampus(Campus campus) {
        if (campus.getId() <= 0) {
            throw new IllegalArgumentException("ID do campus é obrigatório para atualização");
        }
        
        validarCampusComExcecao(campus);
        
        // Verificar se existe outro campus com o mesmo nome (exceto o atual)
        Campus campusExistente = campusDAO.buscarCampusPorNome(campus.getNome());
        if (campusExistente != null && campusExistente.getId() != campus.getId()) {
            throw new IllegalArgumentException("Já existe outro campus com o nome: " + campus.getNome());
        }
        
        // Verificar se existe outro campus com o mesmo CNPJ (exceto o atual)
        if (campus.getCnpj() != null && !campus.getCnpj().trim().isEmpty()) {
            Campus campusCnpj = campusDAO.buscarCampusPorCnpj(campus.getCnpj());
            if (campusCnpj != null && campusCnpj.getId() != campus.getId()) {
                throw new IllegalArgumentException("Já existe outro campus com o CNPJ: " + campus.getCnpj());
            }
        }
        
        return campusDAO.atualizarCampus(campus);
    }
    
    /**
     * Exclui (desativa) um campus
     * @param campusId ID do campus a ser excluído
     * @return true se excluído com sucesso
     */
    public boolean excluirCampus(int campusId) {
        // Verificar se o campus existe
        Campus campus = campusDAO.buscarCampusPorId(campusId);
        if (campus == null) {
            System.out.println("Campus não encontrado com ID: " + campusId);
            return false;
        }
        
        // Verificar se existem setores vinculados ao campus
        int quantidadeSetores = campusDAO.contarSetoresDoCampus(campusId);
        if (quantidadeSetores > 0) {
            System.out.println("Não é possível excluir o campus. Existem " + quantidadeSetores + " setores vinculados.");
            return false;
        }
        
        return campusDAO.excluirCampus(campusId);
    }
    
    /**
     * Busca um campus por ID
     * @param campusId ID do campus
     * @return Campus encontrado ou null
     */
    public Campus buscarCampusPorId(int campusId) {
        return campusDAO.buscarCampusPorId(campusId);
    }
    
    /**
     * Lista todos os campus ativos
     * @return Lista de campus ativos
     */
    public List<Campus> listarCampusAtivos() {
        return campusDAO.listarCampus();
    }
    
    /**
     * Busca campus por filtros
     * @param nome Nome do campus (busca parcial)
     * @param local Local do campus (busca parcial)
     * @param diretor Nome do diretor (busca parcial)
     * @return Lista de campus que atendem aos filtros
     */
    public List<Campus> buscarCampusPorFiltro(String nome, String local, String diretor) {
        return campusDAO.buscarCampusPorFiltro(nome, local, diretor, null);
    }
    
    /**
     * Busca campus por nome
     * @param nome Nome do campus
     * @return Campus encontrado ou null
     */
    public Campus buscarCampusPorNome(String nome) {
        return campusDAO.buscarCampusPorNome(nome);
    }
    
    /**
     * Busca campus por CNPJ
     * @param cnpj CNPJ do campus
     * @return Campus encontrado ou null
     */
    public Campus buscarCampusPorCnpj(String cnpj) {
        return campusDAO.buscarCampusPorCnpj(cnpj);
    }
    
    /**
     * Valida os dados de um campus (lança exceção com mensagem clara)
     * @param campus Campus a ser validado
     * @throws IllegalArgumentException se a validação falhar
     */
    private void validarCampusComExcecao(Campus campus) {
        if (campus == null) {
            throw new IllegalArgumentException("Campus não pode ser nulo");
        }
        
        // Validar nome (obrigatório)
        if (campus.getNome() == null || campus.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do campus é obrigatório");
        }
        
        if (campus.getNome().trim().length() > 100) {
            throw new IllegalArgumentException("Nome do campus não pode ter mais de 100 caracteres");
        }
        
        // Validar local (obrigatório)
        if (campus.getLocal() == null || campus.getLocal().trim().isEmpty()) {
            throw new IllegalArgumentException("Local do campus é obrigatório");
        }
        
        if (campus.getLocal().trim().length() > 200) {
            throw new IllegalArgumentException("Local do campus não pode ter mais de 200 caracteres");
        }
        
        // Validar CNPJ (obrigatório e deve ser válido)
        if (campus.getCnpj() == null || campus.getCnpj().trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ do campus é obrigatório");
        }
        
        if (!validarCnpj(campus.getCnpj().trim())) {
            throw new IllegalArgumentException("CNPJ inválido. Use o formato: XX.XXX.XXX/XXXX-XX ou apenas números (14 dígitos)");
        }
        
        // Validar Código UOrg (obrigatório para SIADS)
        if (campus.getCodigoUorg() == null || campus.getCodigoUorg().trim().isEmpty()) {
            throw new IllegalArgumentException("Código UOrg é obrigatório para integração com SIADS");
        }
        
        // Validar diretor (opcional)
        if (campus.getDiretor() != null && campus.getDiretor().trim().length() > 100) {
            throw new IllegalArgumentException("Nome do diretor não pode ter mais de 100 caracteres");
        }
        
        // Validar cursos (opcional)
        if (campus.getCursos() != null && campus.getCursos().trim().length() > 500) {
            throw new IllegalArgumentException("Lista de cursos não pode ter mais de 500 caracteres");
        }
        
        // Validar telefone (opcional, mas se informado deve ser válido)
        if (campus.getTelefone() != null && !campus.getTelefone().trim().isEmpty()) {
            if (!validarTelefone(campus.getTelefone().trim())) {
                throw new IllegalArgumentException("Telefone inválido. Use formatos como: (65) 99999-9999 ou 6599999999");
            }
        }
        
        // Validar email (opcional, mas se informado deve ser válido)
        if (campus.getEmail() != null && !campus.getEmail().trim().isEmpty()) {
            if (!validarEmail(campus.getEmail().trim())) {
                throw new IllegalArgumentException("Email inválido. Use o formato: exemplo@dominio.com");
            }
        }
        
        // Validar observações (opcional)
        if (campus.getObservacoes() != null && campus.getObservacoes().trim().length() > 1000) {
            throw new IllegalArgumentException("Observações não podem ter mais de 1000 caracteres");
        }
    }
    
    /**
     * Valida os dados de um campus (retorna boolean - mantido para compatibilidade)
     * @param campus Campus a ser validado
     * @return true se válido
     */
    private boolean validarCampus(Campus campus) {
        try {
            validarCampusComExcecao(campus);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida formato do CNPJ
     * @param cnpj CNPJ a ser validado
     * @return true se válido
     */
    private boolean validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        
        return CNPJ_PATTERN.matcher(cnpj.trim()).matches();
    }
    
    /**
     * Valida formato do email
     * @param email Email a ser validado
     * @return true se válido
     */
    private boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Valida formato do telefone
     * @param telefone Telefone a ser validado
     * @return true se válido
     */
    private boolean validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            return false;
        }
        
        return TELEFONE_PATTERN.matcher(telefone.trim()).matches();
    }
    
    /**
     * Verifica se um campus pode ser excluído
     * @param campusId ID do campus
     * @return true se pode ser excluído
     */
    public boolean podeExcluirCampus(int campusId) {
        return campusDAO.contarSetoresDoCampus(campusId) == 0;
    }
    
    /**
     * Conta quantos setores estão vinculados a um campus
     * @param campusId ID do campus
     * @return Quantidade de setores vinculados
     */
    public int contarSetoresDoCampus(int campusId) {
        return campusDAO.contarSetoresDoCampus(campusId);
    }
}