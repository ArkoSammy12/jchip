package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.main.Jchip;
import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.memory.MegaChipBus;

public class MegaChipSoundSystem implements SoundSystem {

    private final Jchip jchip;
    private final MegaChipEmulator emulator;
    private final Chip8SoundSystem megaOffSoundSystem;

    private double step;
    private double phase;
    private int trackStart;
    private int trackSize;
    private boolean loop;
    private boolean isPlaying;

    public MegaChipSoundSystem(MegaChipEmulator emulator) {
        this.jchip = emulator.getEmulatorSettings().getJchip();
        this.emulator = emulator;
        this.megaOffSoundSystem = new Chip8SoundSystem(emulator);
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
            this.megaOffSoundSystem.pushSamples(soundTimer);
            return;
        }
        if (!this.isPlaying) {
            return;
        }
        MegaChipBus bus = this.emulator.getBus();
        AudioRenderer audioRenderer = this.jchip.getAudioRenderer();
        byte[] data = new byte[audioRenderer.getSamplesPerFrame()];
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
        audioRenderer.pushSamples8(data);
    }

}