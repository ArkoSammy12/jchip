package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.memory.Chip8Memory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;

public class MegaChipSoundSystem implements SoundSystem {

    private final Chip8Memory memory;
    private SourceDataLine audioLine;

    private int trackStart;
    private int trackSize;
    private boolean loop;

    private double step;
    private double samplePos;
    private boolean isPlaying;

    public MegaChipSoundSystem(Chip8Memory memory) {
        this.memory = memory;
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, true);
            audioLine = AudioSystem.getSourceDataLine(format);
            audioLine.open(format);
            audioLine.start();
        } catch (Exception e) {
            System.err.println("Error starting audio line: " + e);
        }
    }

    public void playTrack(int trackSampleRate, int trackSize, boolean loop, int trackStart) {
        this.step = (double) trackSampleRate / SAMPLE_RATE;
        this.trackSize = trackSize;
        this.trackStart = trackStart;
        this.loop = loop;
        this.samplePos = 0;
        this.isPlaying = true;
    }

    public void stopTrack() {
        this.trackStart = 0;
        this.trackSize = 0;
        this.loop = false;
        this.step = 0;
        this.samplePos = 0;
        this.isPlaying = false;
    }

    @Override
    public void pushSamples(int soundTimer) {
        if (this.audioLine == null || !this.audioLine.isOpen()) {
            return;
        }
        byte[] data = new byte[SAMPLES_PER_FRAME];
        if (!this.isPlaying) {
            Arrays.fill(data, (byte) 128);
            audioLine.write(data, 0, data.length);
            return;
        }
        for (int i = 0; i < data.length; i++) {
            if (loop && this.samplePos >= this.trackSize) {
                this.samplePos %= this.trackSize;
            }
            if (this.samplePos < this.trackSize) {
                data[i] = (byte) memory.readByte((int) (this.trackStart + this.samplePos));
                this.samplePos += this.step;
            } else {
                data[i] = (byte) 128;
            }
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
