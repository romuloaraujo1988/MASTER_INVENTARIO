package com.inventario.sihcp.util;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe utilitária para reproduzir sons de notificação no sistema
 */
public class SoundNotification {
    
    private static final Logger LOGGER = Logger.getLogger(SoundNotification.class.getName());
    
    // Sons pré-definidos usando frequências
    public enum SoundType {
        SUCCESS(800, 200),      // Som de sucesso - frequência 800Hz por 200ms
        ERROR(400, 300),        // Som de erro - frequência 400Hz por 300ms
        WARNING(600, 250),      // Som de aviso - frequência 600Hz por 250ms
        INFO(1000, 150);        // Som de informação - frequência 1000Hz por 150ms
        
        private final int frequency;
        private final int duration;
        
        SoundType(int frequency, int duration) {
            this.frequency = frequency;
            this.duration = duration;
        }
        
        public int getFrequency() { return frequency; }
        public int getDuration() { return duration; }
    }
    
    // Flag para controlar se sons estão habilitados
    private static boolean soundsEnabled = true;
    
    /**
     * Habilita ou desabilita a reprodução de sons
     * @param enabled true para habilitar, false para desabilitar
     */
    public static void setSoundsEnabled(boolean enabled) {
        soundsEnabled = enabled;
        LOGGER.info("Sons de notificação " + (enabled ? "habilitados" : "desabilitados"));
    }
    
    /**
     * Verifica se os sons estão habilitados
     * @return true se habilitados, false caso contrário
     */
    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }
    
    /**
     * Reproduz um som de notificação
     * @param soundType Tipo de som a ser reproduzido
     */
    public static void playSound(SoundType soundType) {
        // Verificar se sons estão habilitados
        if (!soundsEnabled) {
            LOGGER.fine("Som desabilitado - não será reproduzido: " + soundType);
            return;
        }
        
        try {
            // Definir formato de áudio
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            
            // Verificar se o sistema suporta áudio
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.warning("Sistema não suporta reprodução de áudio");
                return;
            }
            
            // Executar em thread separada para não bloquear a UI
            Thread soundThread = new Thread(() -> {
                try {
                    generateAndPlayTone(soundType.getFrequency(), soundType.getDuration());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao reproduzir som: " + e.getMessage(), e);
                }
            });
            soundThread.setDaemon(true);
            soundThread.start();
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao inicializar reprodução de som: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reproduz som de sucesso para coletas salvas
     */
    public static void playColetaSalvaSound() {
        playSound(SoundType.SUCCESS);
    }
    
    /**
     * Reproduz som de erro
     */
    public static void playErrorSound() {
        playSound(SoundType.ERROR);
    }
    
    /**
     * Reproduz som de aviso
     */
    public static void playWarningSound() {
        playSound(SoundType.WARNING);
    }
    
    /**
     * Reproduz som de informação
     */
    public static void playInfoSound() {
        playSound(SoundType.INFO);
    }
    
    /**
     * Gera e reproduz um tom com frequência e duração específicas
     */
    private static void generateAndPlayTone(int frequency, int duration) throws LineUnavailableException, IOException {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat);
            line.start();
            
            // Calcular número de amostras
            int samples = (int) (sampleRate * duration / 1000);
            byte[] buffer = new byte[samples * 2]; // 2 bytes por amostra (16-bit)
            
            // Gerar onda senoidal
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                short sample = (short) (Math.sin(angle) * 32767 * 0.3); // Volume reduzido (30%)
                
                // Converter para bytes (little endian)
                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            // Aplicar fade out para evitar clique no final
            int fadeOutSamples = Math.min(samples / 10, 1000); // 10% do som ou 1000 amostras
            for (int i = samples - fadeOutSamples; i < samples; i++) {
                double fadeMultiplier = (double) (samples - i) / fadeOutSamples;
                int sampleIndex = i * 2;
                
                short sample = (short) ((buffer[sampleIndex] | (buffer[sampleIndex + 1] << 8)) * fadeMultiplier);
                buffer[sampleIndex] = (byte) (sample & 0xFF);
                buffer[sampleIndex + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            // Reproduzir o som
            line.write(buffer, 0, buffer.length);
            line.drain();
        }
    }
    
    /**
     * Reproduz um som personalizado com múltiplas frequências (acorde)
     */
    public static void playChord(int[] frequencies, int duration) {
        try {
            Thread soundThread = new Thread(() -> {
                try {
                    generateAndPlayChord(frequencies, duration);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Erro ao reproduzir acorde: " + e.getMessage(), e);
                }
            });
            soundThread.setDaemon(true);
            soundThread.start();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao inicializar reprodução de acorde: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera e reproduz um acorde com múltiplas frequências
     */
    private static void generateAndPlayChord(int[] frequencies, int duration) throws LineUnavailableException {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        
        AudioFormat audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        
        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(audioFormat);
            line.start();
            
            int samples = (int) (sampleRate * duration / 1000);
            byte[] buffer = new byte[samples * 2];
            
            // Gerar soma de ondas senoidais
            for (int i = 0; i < samples; i++) {
                double sampleValue = 0;
                
                for (int frequency : frequencies) {
                    double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                    sampleValue += Math.sin(angle);
                }
                
                // Normalizar e aplicar volume
                short sample = (short) (sampleValue / frequencies.length * 32767 * 0.2);
                
                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            line.write(buffer, 0, buffer.length);
            line.drain();
        }
    }
    
    /**
     * Som especial para coleta salva com sucesso (acorde agradável)
     */
    public static void playColetaSalvaChord() {
        // Acorde C maior (Dó, Mi, Sol) em oitava alta
        int[] chord = {523, 659, 784}; // C5, E5, G5
        playChord(chord, 300);
    }
    
    /**
     * Verifica se o sistema suporta reprodução de áudio
     */
    public static boolean isAudioSupported() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }
}