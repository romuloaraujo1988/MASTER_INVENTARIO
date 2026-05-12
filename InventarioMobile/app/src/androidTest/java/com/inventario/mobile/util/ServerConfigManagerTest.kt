package com.inventario.mobile.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.inventario.mobile.utils.ServerConfigManager
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Testes instrumentados para ServerConfigManager
 * Executa no dispositivo/emulador Android
 */
@RunWith(AndroidJUnit4::class)
class ServerConfigManagerTest {

    @Test
    fun testValidIps() {
        // IPs válidos
        assertTrue("127.0.0.1 deve ser válido", ServerConfigManager.isValidIp("127.0.0.1"))
        assertTrue("192.168.1.100 deve ser válido", ServerConfigManager.isValidIp("192.168.1.100"))
        assertTrue("10.0.0.1 deve ser válido", ServerConfigManager.isValidIp("10.0.0.1"))
        assertTrue("172.16.0.1 deve ser válido", ServerConfigManager.isValidIp("172.16.0.1"))
        assertTrue("0.0.0.0 deve ser válido", ServerConfigManager.isValidIp("0.0.0.0"))
        assertTrue("255.255.255.255 deve ser válido", ServerConfigManager.isValidIp("255.255.255.255"))
    }

    @Test
    fun testInvalidIps() {
        // IPs inválidos
        assertFalse("IP vazio deve ser inválido", ServerConfigManager.isValidIp(""))
        assertFalse("IP com octeto > 255", ServerConfigManager.isValidIp("256.1.1.1"))
        assertFalse("IP com formato incorreto", ServerConfigManager.isValidIp("192.168.1"))
        assertFalse("IP com letras", ServerConfigManager.isValidIp("192.168.1.abc"))
        assertFalse("IP com caracteres especiais", ServerConfigManager.isValidIp("192.168.1.1@"))
        assertFalse("Texto aleatório", ServerConfigManager.isValidIp("servidor.local"))
        assertFalse("IP com octeto negativo", ServerConfigManager.isValidIp("-1.168.1.1"))
        assertFalse("IP com muitos octetos", ServerConfigManager.isValidIp("192.168.1.1.1"))
        assertFalse("IP com poucos octetos", ServerConfigManager.isValidIp("192.168"))
    }

    @Test
    fun testServerConfigManagerInstance() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val serverConfigManager = ServerConfigManager.getInstance(context)
        
        // Verifica se a instância foi criada corretamente
        assertNotNull("ServerConfigManager deve ser criado", serverConfigManager)
    }
}