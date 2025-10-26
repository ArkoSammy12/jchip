package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.memory.Chip8Memory;

public class MegaChipSoundSystem implements SoundSystem {

    private final JChip jchip;
    private final Chip8Memory memory;

    private int trackStart;
    private int trackSize;
    private boolean loop;

    private double step;
    private double samplePos;
    private boolean isPlaying;

    public MegaChipSoundSystem(EmulatorInitializer emulatorInitializer, Chip8Memory memory) {
        this.jchip = emulatorInitializer.getJChip();
        this.memory = memory;
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
        if (!this.isPlaying) {
            this.jchip.getSoundWriter().silence();
            return;
        }
        byte[] data = new byte[SAMPLES_PER_FRAME];
        for (int i = 0; i < data.length; i++) {
            if (loop && this.samplePos >= this.trackSize) {
                this.samplePos %= this.trackSize;
            }
            if (this.samplePos < this.trackSize) {
                data[i] = (byte) (memory.readByte((int) (this.trackStart + this.samplePos)) - 128);
                this.samplePos += this.step;
            } else {
                data[i] = 0;
            }
        }
        this.jchip.getSoundWriter().writeSamples(data);
    }

    @Override
    public void close() {
        this.jchip.getSoundWriter().stop();
    }
}