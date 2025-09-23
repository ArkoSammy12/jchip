package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.util.Chip8Variant;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;

public class Chip8SoundSystem implements SoundSystem {

    private SourceDataLine audioLine;

    private final int[] patternBuffer = new int[16];
    private double playbackRate = 4000;
    private double phase = 0.0;

    private static final int[] DEFAULT_PATTERN_1 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0, 0, 0, 0
    };

    private static final int[] DEFAULT_PATTERN_2 = {
            0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0, 0, 0, 0
    };

    public Chip8SoundSystem(Chip8Variant chip8Variant) {
        Arrays.fill(this.patternBuffer, 0);
        if (chip8Variant != Chip8Variant.XO_CHIP) {
            System.arraycopy(DEFAULT_PATTERN_2, 0, this.patternBuffer, 0, DEFAULT_PATTERN_2.length);
            this.setPlaybackRate(175);
        }
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format);
            audioLine.start();
        } catch (Exception e) {
            System.err.println("Error starting audio line: " + e);
        }
    }

    public void loadPatternByte(int index, int value) {
        this.patternBuffer[index] = value & 0xFF;
    }

    public void setPlaybackRate(int pitch) {
        this.playbackRate = 4000 * Math.pow(2.0, (pitch - 64) / 48.0);
    }

    public void pushSamples(int soundTimer) {
        if (this.audioLine == null || !this.audioLine.isOpen()) {
            return;
        }
        byte[] data = new byte[SAMPLES_PER_FRAME];
        if (soundTimer <= 0) {
            this.phase = 0;
            Arrays.fill(data, (byte) 0);
            audioLine.write(data, 0, data.length);
            return;
        }
        double step = this.playbackRate / 128.0 / SAMPLE_RATE;
        for (int i = 0; i < data.length; i++) {
            int bitStep = (int) (this.phase * 128);
            int bitMask = 1 << (7 ^ (bitStep & 7));
            int index = bitStep >> 3;
            int rawData = this.patternBuffer[index];
            byte sample = (byte) ((rawData & bitMask) != 0 ? 4 : -4);
            data[i] = sample;
            this.phase += step;
            this.phase %= 1.0f;
        }
        audioLine.write(data, 0, data.length);
    }

    @Override
    public void close() {
        if (this.audioLine != null) {
            this.audioLine.close();
        }
    }

}
