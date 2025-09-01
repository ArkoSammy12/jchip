package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.AudioSystem;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;

public class DefaultAudioSystem implements AudioSystem {
    private final int[] patternBuffer = new int[16];
    private int pitch = 4000;

    private SourceDataLine line;
    private double phase = 0.0;

    private final int sampleRate = 44100;
    private final int samplesPerFrame = sampleRate / 60;

    private static final int[] DEFAULT_PATTERN_1 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] DEFAULT_PATTERN_2 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0
    };

    public DefaultAudioSystem(ConsoleVariant consoleVariant) {
        Arrays.fill(this.patternBuffer, 0);
        if (consoleVariant != ConsoleVariant.XO_CHIP) {
            System.arraycopy(DEFAULT_PATTERN_1, 0, this.patternBuffer, 0, DEFAULT_PATTERN_1.length);
            this.pitch = 400;
        }
        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            line = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadPatternByte(int index, int value) {
        this.patternBuffer[index] = value & 0xFF;
    }

    @Override
    public void setPitch(int vX) {
        this.pitch = (int) (4000 * Math.pow(2.0, (vX - 64) / 48.0));
    }

    public void pushFrame(int soundTimer) {
        byte[] buffer = new byte[samplesPerFrame * 2];
        if (soundTimer == 0) {
            Arrays.fill(buffer, (byte) 0);
        } else {
            double patternLength = 128.0;
            int idx = 0;
            for (int s = 0; s < samplesPerFrame; s++) {
                phase += patternLength * pitch / sampleRate;
                if (phase >= patternLength) {
                    phase -= patternLength;
                }

                int bitIndex = ((int) phase) % 128;
                int byteIndex = bitIndex / 8;
                int bitOffset = 7 - (bitIndex % 8);
                int bit = (patternBuffer[byteIndex] >> bitOffset) & 1;

                short sample = (bit == 1 ? (short) 750 : (short) -750);

                buffer[idx++] = (byte) (sample & 0xFF);
                buffer[idx++] = (byte) ((sample >> 8) & 0xFF);

            }
        }

        line.write(buffer, 0, buffer.length);
    }

    @Override
    public void close() {
        this.line.close();
    }

}
