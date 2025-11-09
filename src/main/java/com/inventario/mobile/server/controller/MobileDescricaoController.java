package com.inventario.mobile.server.controller;

import com.inventario.mobile.server.dto.ApiResponse;
import com.inventario.dao.PatrimonioDAO;
import com.inventario.model.Patrimonio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para descrições de patrimônios (itens sem patrimônio)
 */
@RestController
@RequestMapping("/api/mobile/descricoes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MobileDescricaoController {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileDescricaoController.class);
    
    @Autowired
    private PatrimonioDAO patrimonioDAO;
    
    /**
     * Listar descrições únicas de patrimônios
     * Retorna lista de descrições agrupadas com contagem
     * 
     * @return lista de descrições
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarDescricoes() {
        try {
            logger.info("Buscando descrições únicas de patrimônios");
            
            // Buscar todos os patrimônios
            List<Patrimonio> patrimonios = patrimonioDAO.findAll();
            
            // Agrupar por descrição e contar
            Map<String, Long> descricoesAgrupadas = patrimonios.stream()
                    .filter(p -> p.getDescricao() != null && !p.getDescricao().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            Patrimonio::getDescricao,
                            Collectors.counting()
                    ));
            
            // Converter para lista de mapas
            List<Map<String, Object>> descricoes = descricoesAgrupadas.entrySet().stream()
                    .map(entry -> Map.of(
                            "descricao", (Object) entry.getKey(),
                            "quantidade", (Object) entry.getValue()
                    ))
                    .sorted((a, b) -> ((String) a.get("descricao")).compareTo((String) b.get("descricao")))
                    .collect(Collectors.toList());
            
            logger.info("{} descrições únicas encontradas", descricoes.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoes, 
                            String.format("%d descrição(ões) encontrada(s)", descricoes.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições", "FETCH_ERROR"));
        }
    }
    
    /**
     * Buscar descrições por termo
     * 
     * @param termo termo de busca
     * @return lista de descrições filtradas
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> buscarDescricoes(
            @RequestParam String termo) {
        try {
            logger.info("Buscando descrições com termo: {}", termo);
            
            // Buscar patrimônios por descrição
            List<Patrimonio> patrimonios = patrimonioDAO.buscarPorDescricaoAbrangente(termo);
            
            // Agrupar por descrição e contar
            Map<String, Long> descricoesAgrupadas = patrimonios.stream()
                    .filter(p -> p.getDescricao() != null && !p.getDescricao().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            Patrimonio::getDescricao,
                            Collectors.counting()
                    ));
            
            // Converter para lista de mapas
            List<Map<String, Object>> descricoes = descricoesAgrupadas.entrySet().stream()
                    .map(entry -> Map.of(
                            "descricao", (Object) entry.getKey(),
                            "quantidade", (Object) entry.getValue()
                    ))
                    .sorted((a, b) -> ((String) a.get("descricao")).compareTo((String) b.get("descricao")))
                    .collect(Collectors.toList());
            
            logger.info("{} descrições encontradas para o termo '{}'", descricoes.size(), termo);
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoes, 
                            String.format("%d descrição(ões) encontrada(s)", descricoes.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições por termo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições", "FETCH_ERROR"));
        }
    }
    
    /**
     * Listar descrições únicas de patrimônios NÃO COLETADOS
     * Facilita a coleta sem etiqueta mostrando apenas itens pendentes
     * 
     * @param idInventario ID do inventário ativo
     * @return lista de descrições não coletadas
     */
    @GetMapping("/nao-coletadas")
    public ResponseEntity<ApiResponse<List<String>>> listarDescricoesNaoColetadas(
            @RequestParam(required = false) Integer idInventario) {
        try {
            logger.info("Buscando descrições de patrimônios não coletados");
            
            // Buscar todos os patrimônios
            List<Patrimonio> patrimonios = patrimonioDAO.findAll();
            
            // Buscar patrimônios já coletados no inventário atual
            List<Integer> idsColetados = patrimonioDAO.buscarPatrimoniosColetados(idInventario);
            
            // Filtrar apenas não coletados e extrair descrições únicas
            List<String> descricoesNaoColetadas = patrimonios.stream()
                    .filter(p -> !idsColetados.contains(p.getId()))
                    .filter(p -> p.getDescricao() != null && !p.getDescricao().trim().isEmpty())
                    .map(Patrimonio::getDescricao)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            logger.info("{} descrições não coletadas encontradas", descricoesNaoColetadas.size());
            
            return ResponseEntity.ok(
                    ApiResponse.success(descricoesNaoColetadas, 
                            String.format("%d descrição(ões) não coletada(s)", descricoesNaoColetadas.size())));
            
        } catch (Exception e) {
            logger.error("Erro ao buscar descrições não coletadas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro ao buscar descrições não coletadas", "FETCH_ERROR"));
        }
    }
}
