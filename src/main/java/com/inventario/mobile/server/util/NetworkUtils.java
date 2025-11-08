package com.inventario.mobile.server.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Utilitários de rede para o servidor mobile
 */
public class NetworkUtils {
    
    /**
     * Obtém o endereço IP local da rede
     * Prioriza IPs de redes privadas (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
     * 
     * @return IP local ou "localhost" se não encontrar
     */
    public static String obterIPLocal() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                
                // Ignorar interfaces desativadas e loopback
                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    
                    // Pegar apenas IPv4 e ignorar loopback
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        
                        // Preferir IPs da rede local (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || 
                            (ip.startsWith("172.") && 
                             Integer.parseInt(ip.split("\\.")[1]) >= 16 && 
                             Integer.parseInt(ip.split("\\.")[1]) <= 31)) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao obter IP local: " + e.getMessage());
        }
        return "localhost";
    }
    
    /**
     * Constrói URL base do servidor usando IP dinâmico
     * 
     * @param porta Porta do servidor
     * @param contextPath Context path da aplicação
     * @return URL base completa
     */
    public static String construirURLBase(int porta, String contextPath) {
        String ip = obterIPLocal();
        return String.format("http://%s:%d%s", ip, porta, contextPath);
    }
    
    /**
     * Constrói URL base do servidor usando configurações padrão
     * 
     * @return URL base completa
     */
    public static String construirURLBase() {
        return construirURLBase(8081, "/inventario");
    }
}