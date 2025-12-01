package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.Variant;

import java.util.Arrays;

public class Chip8SoundSystem implements SoundSystem {

    public static final int SQUARE_WAVE_AMPLITUDE = 4;

    private final Jchip jchip;
    private final int[] patternBuffer = new int[16];
    private double step = 0;
    private double phase = 0.0;

    private static final int[] DEFAULT_PATTERN_1 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] DEFAULT_PATTERN_2 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0
    };

    public Chip8SoundSystem(Emulator emulator) {
        this.jchip = emulator.getEmulatorSettings().getJchip();
        Variant variant = emulator.getEmulatorSettings().getVariant();
        if (variant != Variant.XO_CHIP && variant != Variant.HYPERWAVE_CHIP_64) {
            System.arraycopy(DEFAULT_PATTERN_2, 0, this.patternBuffer, 0, DEFAULT_PATTERN_2.length);
            this.setPitch(175);
        } else {
            Arrays.fill(this.patternBuffer, 0);
            this.setPitch(64);
        }
    }

    public void loadPatternByte(int index, int value) {
        this.patternBuffer[index] = value & 0xFF;
    }

    public void setPitch(int pitch) {
        this.step = (4000 * Math.pow(2.0, (pitch - 64) / 48.0)) / 128.0 / SAMPLE_RATE;
    }

    public void pushSamples(int soundTimer) {
        if (soundTimer <= 0) {
            this.phase = 0;
            return;
        }
        byte[] data = new byte[SAMPLES_PER_FRAME];
        for (int i = 0; i < data.length; i++) {
            int bitStep = (int) (this.phase * 128);
            data[i] = (byte) (((this.patternBuffer[bitStep >> 3]) & (1 << (7 ^ (bitStep & 7)))) != 0 ? SQUARE_WAVE_AMPLITUDE : -SQUARE_WAVE_AMPLITUDE);
            this.phase = (this.phase + step) % 1.0;
        }
        this.jchip.getSoundWriter().pushSamples(data);
    }

}