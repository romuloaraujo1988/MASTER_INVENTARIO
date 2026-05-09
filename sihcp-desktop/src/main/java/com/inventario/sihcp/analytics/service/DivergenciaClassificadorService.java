package com.inventario.sihcp.analytics.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.sihcp.analytics.model.GravidadeDivergencia;
import com.inventario.sihcp.analytics.model.TipoDivergencia;
import com.inventario.sihcp.model.Coleta;
import com.inventario.sihcp.model.Patrimonio;

/**
 * Serviço responsável pela classificação de gravidade das divergências.
 * 
 * Implementa as regras de negócio para determinar o tipo e gravidade
 * de divergências detectadas durante a coleta de patrimônios.
 */
public class DivergenciaClassificadorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DivergenciaClassificadorService.class);
    
    /**
     * Mapeamento de estados de conservação para níveis numéricos.
     * Quanto maior o número, melhor o estado.
     */
    private static final Map<String, Integer> NIVEIS_ESTADO = new HashMap<>();
    
    static {
        NIVEIS_ESTADO.put("EXCELENTE", 5);
        NIVEIS_ESTADO.put("OTIMO", 5);
        NIVEIS_ESTADO.put("BOM", 4);
        NIVEIS_ESTADO.put("REGULAR", 3);
        NIVEIS_ESTADO.put("RUIM", 2);
        NIVEIS_ESTADO.put("PESSIMO", 1);
        NIVEIS_ESTADO.put("INSERVIVEL", 0);
        NIVEIS_ESTADO.put("IRRECUPERAVEL", 0);
    }
    
    /**
     * Classifica a gravidade de uma divergência de localização.
     * 
     * Regras:
     * - ALTA: Patrimônio mudou de setor
     * - MÉDIA: Patrimônio mudou de sala no mesmo setor
     * - BAIXA: Apenas a descrição da localização difere
     * 
     * @param localCadastrado Localização cadastrada no sistema
     * @param localEncontrado Localização onde o patrimônio foi encontrado
     * @param setorCadastrado ID do setor cadastrado (pode ser null)
     * @param setorEncontrado ID do setor onde foi encontrado (pode ser null)
     * @return Gravidade da divergência
     */
    public GravidadeDivergencia classificarDivergenciaLocalizacao(
            String localCadastrado, 
            String localEncontrado,
            Integer setorCadastrado,
            Integer setorEncontrado) {
        
        logger.debug("Classificando divergência de localização: cadastrado='{}' (setor={}), encontrado='{}' (setor={})",
                localCadastrado, setorCadastrado, localEncontrado, setorEncontrado);
        
        // Se não há diferença, não é divergência
        if (Objects.equals(localCadastrado, localEncontrado) && 
            Objects.equals(setorCadastrado, setorEncontrado)) {
            return GravidadeDivergencia.BAIXA;
        }
        
        // Verifica se mudou de setor
        if (setorCadastrado != null && setorEncontrado != null && 
            !setorCadastrado.equals(setorEncontrado)) {
            logger.debug("Divergência ALTA: mudou de setor {} para {}", setorCadastrado, setorEncontrado);
            return GravidadeDivergencia.ALTA;
        }
        
        // Se os setores são iguais mas as localizações diferem, mudou de sala
        if (Objects.equals(setorCadastrado, setorEncontrado) && 
            !Objects.equals(localCadastrado, localEncontrado)) {
            
            // Verifica se é apenas diferença de descrição (ex: "Sala 101" vs "Sala 101 - Bloco A")
            if (localCadastrado != null && localEncontrado != null) {
                String cadastradoNorm = normalizar(localCadastrado);
                String encontradoNorm = normalizar(localEncontrado);
                
                if (cadastradoNorm.contains(encontradoNorm) || encontradoNorm.contains(cadastradoNorm)) {
                    logger.debug("Divergência BAIXA: apenas descrição difere");
                    return GravidadeDivergencia.BAIXA;
                }
            }
            
            logger.debug("Divergência MÉDIA: mudou de sala no mesmo setor");
            return GravidadeDivergencia.MEDIA;
        }
        
        // Caso padrão: apenas descrição difere
        return GravidadeDivergencia.BAIXA;
    }
    
    /**
     * Classifica a gravidade de uma divergência de estado de conservação.
     * 
     * Regras:
     * - CRÍTICA: Estado piorou 2 ou mais níveis
     * - ALTA: Estado piorou 1 nível
     * - BAIXA: Estado melhorou ou permaneceu igual
     * 
     * @param estadoCadastrado Estado cadastrado no sistema
     * @param estadoEncontrado Estado encontrado durante a coleta
     * @return Gravidade da divergência
     */
    public GravidadeDivergencia classificarDivergenciaEstado(
            String estadoCadastrado, 
            String estadoEncontrado) {
        
        logger.debug("Classificando divergência de estado: cadastrado='{}', encontrado='{}'",
                estadoCadastrado, estadoEncontrado);
        
        // Se não há diferença, não é divergência
        if (Objects.equals(estadoCadastrado, estadoEncontrado)) {
            return GravidadeDivergencia.BAIXA;
        }
        
        // Obtém níveis numéricos
        int nivelCadastrado = obterNivelEstado(estadoCadastrado);
        int nivelEncontrado = obterNivelEstado(estadoEncontrado);
        
        // Calcula diferença (positivo = piorou)
        int diferenca = nivelCadastrado - nivelEncontrado;
        
        logger.debug("Diferença de níveis: {} (cadastrado={}, encontrado={})", 
                diferenca, nivelCadastrado, nivelEncontrado);
        
        if (diferenca >= 2) {
            logger.debug("Divergência CRÍTICA: estado piorou {} níveis", diferenca);
            return GravidadeDivergencia.CRITICA;
        } else if (diferenca == 1) {
            logger.debug("Divergência ALTA: estado piorou 1 nível");
            return GravidadeDivergencia.ALTA;
        } else {
            // diferenca <= 0 significa que melhorou ou ficou igual
            logger.debug("Divergência BAIXA: estado melhorou ou igual");
            return GravidadeDivergencia.BAIXA;
        }
    }
    
    /**
     * Detecta automaticamente o tipo de divergência comparando coleta com patrimônio.
     * 
     * @param coleta Dados da coleta realizada
     * @param patrimonio Dados cadastrados do patrimônio
     * @return Tipo de divergência detectado
     */
    public TipoDivergencia detectarTipo(Coleta coleta, Patrimonio patrimonio) {
        if (coleta == null || patrimonio == null) {
            return TipoDivergencia.OUTRO;
        }
        
        // Verifica se foi marcada como divergência manual
        if (coleta.isDivergencia()) {
            String motivo = coleta.getMotivoDivergencia();
            if (motivo != null && !motivo.isEmpty()) {
                return TipoDivergencia.MANUAL;
            }
        }
        
        // Verifica divergência de localização
        // Patrimonio usa nomeSala (campo transiente) ao invés de objeto Sala
        String localCadastrado = patrimonio.getNomeSala();
        String localEncontrado = coleta.getLocalizacaoEncontrada();
        
        if (!Objects.equals(localCadastrado, localEncontrado)) {
            return TipoDivergencia.LOCALIZACAO;
        }
        
        // Verifica divergência de estado
        String estadoCadastrado = patrimonio.getEstadoConservacao();
        String estadoEncontrado = coleta.getEstadoEncontrado();
        
        if (!Objects.equals(estadoCadastrado, estadoEncontrado)) {
            return TipoDivergencia.ESTADO;
        }
        
        // Se chegou aqui mas foi marcada como divergência, é outro tipo
        if (coleta.isDivergencia()) {
            return TipoDivergencia.OUTRO;
        }
        
        return TipoDivergencia.OUTRO;
    }
    
    /**
     * Classifica a gravidade geral de uma divergência baseada no tipo detectado.
     * 
     * @param coleta Dados da coleta
     * @param patrimonio Dados do patrimônio
     * @return Gravidade da divergência
     */
    public GravidadeDivergencia classificarGravidade(Coleta coleta, Patrimonio patrimonio) {
        TipoDivergencia tipo = detectarTipo(coleta, patrimonio);
        
        return switch (tipo) {
            case LOCALIZACAO -> classificarDivergenciaLocalizacao(
                    patrimonio.getNomeSala(),
                    coleta.getLocalizacaoEncontrada(),
                    null, // setorCadastrado - não disponível diretamente
                    null  // setorEncontrado - não disponível diretamente
            );
            case ESTADO -> classificarDivergenciaEstado(
                    patrimonio.getEstadoConservacao(),
                    coleta.getEstadoEncontrado()
            );
            case MANUAL -> GravidadeDivergencia.MEDIA;
            default -> GravidadeDivergencia.BAIXA;
        }; // Patrimonio usa idSala, não temos acesso direto ao setor
        // Para uma classificação mais precisa, seria necessário buscar o setor via DAO
        // Divergências manuais são consideradas de gravidade média por padrão
    }
    
    /**
     * Obtém o nível numérico de um estado de conservação.
     * 
     * @param estado Nome do estado
     * @return Nível numérico (0-5, onde 5 é melhor)
     */
    private int obterNivelEstado(String estado) {
        if (estado == null || estado.isEmpty()) {
            return 3; // Assume REGULAR como padrão
        }
        
        String estadoNorm = normalizar(estado);
        return NIVEIS_ESTADO.getOrDefault(estadoNorm, 3);
    }
    
    /**
     * Normaliza uma string para comparação (uppercase, sem acentos, sem espaços extras).
     */
    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.toUpperCase()
                .trim()
                .replaceAll("[ÁÀÂÃ]", "A")
                .replaceAll("[ÉÈÊ]", "E")
                .replaceAll("[ÍÌÎ]", "I")
                .replaceAll("[ÓÒÔÕ]", "O")
                .replaceAll("[ÚÙÛ]", "U")
                .replaceAll("[Ç]", "C")
                .replaceAll("\\s+", " ");
    }
}
