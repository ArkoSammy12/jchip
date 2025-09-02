package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.Main;
import io.github.arkosammy12.jchip.base.AudioSystem;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;

public class DefaultAudioSystem implements AudioSystem {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES_PER_FRAME = SAMPLE_RATE / Main.FRAMES_PER_SECOND;
    private final int[] patternBuffer = new int[16];
    private double playbackRate = 4000;

    private SourceDataLine line;
    private double phase = 0.0;

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
            this.playbackRate = 400;
        }
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
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
    public void setPlaybackRate(int pitch) {
        this.playbackRate = 4000 * Math.pow(2.0, (pitch - 64) / 48.0);
    }

    public void pushFrame(int soundTimer) {
        byte[] data = new byte[SAMPLES_PER_FRAME];
        if (soundTimer <= 0) {
            this.phase = 0;
            Arrays.fill(data, (byte) 0);
            line.write(data, 0, data.length);
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
        line.write(data, 0, data.length);
    }

    @Override
    public void close() {
        this.line.close();
    }

}
