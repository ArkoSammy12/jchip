package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.memory.MegaChipBus;

public class MegaChipSoundSystem extends Chip8SoundSystem {

    private final MegaChipEmulator emulator;

    private int trackStart;
    private int trackSize;
    private boolean loop;
    private boolean isPlaying;

    public MegaChipSoundSystem(MegaChipEmulator emulator) {
        super(emulator);
        this.emulator = emulator;
        this.step = 0;
    }

    public void playTrack(int trackSampleRate, int trackSize, boolean loop, int trackStart) {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            return;
        }
        this.step = (double) trackSampleRate / SAMPLE_RATE;
        this.trackSize = trackSize;
        this.trackStart = trackStart;
        this.loop = loop;
        this.phase = 0;
        this.isPlaying = true;
    }

    public void stopTrack() {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            return;
        }
        this.trackStart = 0;
        this.trackSize = 0;
        this.loop = false;
        this.step = 0;
        this.phase = 0;
        this.isPlaying = false;
    }

    @Override
    public void pushSamples(int soundTimer) {
        if (!this.emulator.getProcessor().isMegaModeOn()) {
            super.pushSamples(soundTimer);
            return;
        }
        if (!this.isPlaying) {
            return;
        }
        MegaChipBus bus = this.emulator.getBus();
        byte[] data = new byte[SAMPLES_PER_FRAME];
        for (int i = 0; i < data.length; i++) {
            if (loop && this.phase >= this.trackSize) {
                this.phase %= this.trackSize;
            }
            if (this.phase < this.trackSize) {
                data[i] = (byte) (bus.readByte((int) (this.trackStart + this.phase)) - 128);
                this.phase += this.step;
            } else {
                data[i] = 0;
            }
        }
        this.jchip.getAudioRenderer().pushSamples8(data);
    }

}