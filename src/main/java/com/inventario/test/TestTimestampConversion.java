package com.inventario.test;

import com.inventario.offline.ColetaOfflineService;
import com.inventario.model.Coleta;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TestTimestampConversion {
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando teste de conversão de timestamp...");

            // Acessar o método privado mapToColeta via reflexão
            Method mapToColetaMethod = ColetaOfflineService.class.getDeclaredMethod("mapToColeta", Map.class);
            mapToColetaMethod.setAccessible(true);

            ColetaOfflineService service = ColetaOfflineService.getInstance();

            // Teste 1: Timestamp direto (comportamento padrão)
            System.out.println("\nTeste 1: Timestamp direto");
            Map<String, Object> map1 = new HashMap<>();
            Timestamp ts1 = new Timestamp(System.currentTimeMillis());
            map1.put("data_coleta", ts1);
            Coleta c1 = (Coleta) mapToColetaMethod.invoke(service, map1);
            System.out.println("Resultado 1: " + (c1.getDataColeta().equals(ts1) ? "SUCESSO" : "FALHA"));

            // Teste 2: Long (comum no SQLite)
            System.out.println("\nTeste 2: Long (millis)");
            Map<String, Object> map2 = new HashMap<>();
            long millis = System.currentTimeMillis();
            map2.put("data_coleta", millis);
            Coleta c2 = (Coleta) mapToColetaMethod.invoke(service, map2);
            System.out.println("Resultado 2: " + (c2.getDataColeta().getTime() == millis ? "SUCESSO" : "FALHA"));

            // Teste 3: String numérica (comum em JSON/SQLite)
            System.out.println("\nTeste 3: String numérica");
            Map<String, Object> map3 = new HashMap<>();
            map3.put("data_coleta", String.valueOf(millis));
            Coleta c3 = (Coleta) mapToColetaMethod.invoke(service, map3);
            System.out.println("Resultado 3: " + (c3.getDataColeta().getTime() == millis ? "SUCESSO" : "FALHA"));

            // Teste 4: String formato SQL
            System.out.println("\nTeste 4: String formato SQL");
            Map<String, Object> map4 = new HashMap<>();
            String sqlDate = "2023-11-21 10:30:00";
            map4.put("data_coleta", sqlDate);
            Coleta c4 = (Coleta) mapToColetaMethod.invoke(service, map4);
            System.out.println(
                    "Resultado 4: " + (c4.getDataColeta() != null ? "SUCESSO (" + c4.getDataColeta() + ")" : "FALHA"));

            System.out.println("\nTeste concluído.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
