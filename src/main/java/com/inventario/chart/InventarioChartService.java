package com.inventario.chart;

import com.inventario.dao.ChartDataDAO;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para geração de gráficos específicos do sistema de inventário
 */
public class InventarioChartService {

    private final ChartDataDAO chartDataDAO;

    public InventarioChartService() {
        this.chartDataDAO = new ChartDataDAO();
    }

    /**
     * Gráfico de pizza: Status dos patrimônios
     */
    public JFreeChart createPatrimonioStatusChart() {
        Map<String, Number> data = new LinkedHashMap<>();
        
        try {
            // Buscar contagens por status
            int ativos = chartDataDAO.contarPorStatus("ATIVO");
            int inativos = chartDataDAO.contarPorStatus("INATIVO");
            int manutencao = chartDataDAO.contarPorStatus("MANUTENÇÃO");
            int baixados = chartDataDAO.contarPorStatus("BAIXADO");

            data.put("Ativos", ativos);
            data.put("Inativos", inativos);
            data.put("Em Manutenção", manutencao);
            data.put("Baixados", baixados);

        } catch (Exception e) {
            System.err.println("Erro ao gerar gráfico de status: " + e.getMessage());
            // Dados de exemplo em caso de erro
            data.put("Ativos", 150);
            data.put("Inativos", 30);
            data.put("Em Manutenção", 10);
            data.put("Baixados", 5);
        }

        return InventarioChartFactory.createPieChart("Status dos Patrimônios", data);
    }

    /**
     * Gráfico de barras: Patrimônios por setor
     */
    public JFreeChart createPatrimoniosPorSetorChart() {
        Map<String, Number> data = new LinkedHashMap<>();

        try {
            List<Map<String, Object>> resultado = chartDataDAO.contarPatrimoniosPorSetor();
            
            for (Map<String, Object> row : resultado) {
                String setor = (String) row.get("setor");
                Number quantidade = (Number) row.get("quantidade");
                data.put(setor, quantidade);
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar gráfico por setor: " + e.getMessage());
            // Dados de exemplo
            data.put("TI", 45);
            data.put("Administrativo", 32);
            data.put("RH", 18);
            data.put("Financeiro", 25);
        }

        DefaultCategoryDataset dataset = InventarioChartFactory.createCategoryDataset(data, "Patrimônios");
        return InventarioChartFactory.createBarChart(
            "Patrimônios por Setor",
            "Setor",
            "Quantidade",
            dataset
        );
    }

    /**
     * Gráfico de barras: Progresso da coleta por inventário
     */
    public JFreeChart createProgressoColetaChart(Integer idInventario) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            int totalPatrimonios = chartDataDAO.contarTodosPatrimonios();
            int coletados = chartDataDAO.contarColetadosPorInventario(idInventario);
            int pendentes = totalPatrimonios - coletados;

            dataset.addValue(coletados, "Coletados", "Progresso");
            dataset.addValue(pendentes, "Pendentes", "Progresso");

        } catch (Exception e) {
            System.err.println("Erro ao gerar gráfico de progresso: " + e.getMessage());
            // Dados de exemplo
            dataset.addValue(120, "Coletados", "Progresso");
            dataset.addValue(75, "Pendentes", "Progresso");
        }

        return InventarioChartFactory.createBarChart(
            "Progresso da Coleta",
            "Status",
            "Quantidade",
            dataset
        );
    }

    /**
     * Gráfico de linhas: Evolução das coletas por dia
     */
    public JFreeChart createEvolucaoColetasChart(Integer idInventario) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            List<Map<String, Object>> evolucao = chartDataDAO.buscarEvolucaoDiaria(idInventario);
            
            for (Map<String, Object> row : evolucao) {
                String data = (String) row.get("data");
                Number quantidade = (Number) row.get("quantidade");
                dataset.addValue(quantidade, "Coletas", data);
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar gráfico de evolução: " + e.getMessage());
            // Dados de exemplo
            dataset.addValue(10, "Coletas", "01/01");
            dataset.addValue(25, "Coletas", "02/01");
            dataset.addValue(35, "Coletas", "03/01");
            dataset.addValue(50, "Coletas", "04/01");
            dataset.addValue(65, "Coletas", "05/01");
        }

        return InventarioChartFactory.createLineChart(
            "Evolução das Coletas",
            "Data",
            "Quantidade Acumulada",
            dataset
        );
    }

    /**
     * Gráfico de barras horizontais: Top 10 descrições mais coletadas
     */
    public JFreeChart createTop10DescricoesChart(Integer idInventario) {
        Map<String, Number> data = new LinkedHashMap<>();

        try {
            List<Map<String, Object>> top10 = chartDataDAO.buscarTop10Descricoes(idInventario);
            
            for (Map<String, Object> row : top10) {
                String descricao = (String) row.get("descricao");
                Number quantidade = (Number) row.get("quantidade");
                
                // Truncar descrição se muito longa
                if (descricao.length() > 30) {
                    descricao = descricao.substring(0, 27) + "...";
                }
                
                data.put(descricao, quantidade);
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar gráfico top 10: " + e.getMessage());
            // Dados de exemplo
            data.put("CADEIRA GIRATÓRIA", 25);
            data.put("MESA ESCRITÓRIO", 18);
            data.put("COMPUTADOR DESKTOP", 15);
            data.put("MONITOR LCD", 12);
            data.put("ARMÁRIO ARQUIVO", 10);
        }

        DefaultCategoryDataset dataset = InventarioChartFactory.createCategoryDataset(data, "Quantidade");
        return InventarioChartFactory.createHorizontalBarChart(
            "Top 10 Itens Mais Coletados",
            "Quantidade",
            "Descrição",
            dataset
        );
    }
}
