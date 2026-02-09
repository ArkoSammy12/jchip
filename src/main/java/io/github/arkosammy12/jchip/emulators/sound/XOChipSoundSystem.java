package io.github.arkosammy12.jchip.emulators.sound;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.main.AudioRenderer;

public class XOChipSoundSystem extends Chip8SoundSystem {

    private final int[] patternBuffer = new int[16];

    public XOChipSoundSystem(XOChipEmulator emulator) {
        super(emulator);
        this.setPitch(64);
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
        AudioRenderer audioRenderer = this.jchip.getAudioRenderer();
        byte[] data = new byte[audioRenderer.getSamplesPerFrame()];
        for (int i = 0; i < data.length; i++) {
            int bitStep = (int) (this.phase * 128);
            data[i] = (byte) (((this.patternBuffer[bitStep >> 3]) & (1 << (7 ^ (bitStep & 7)))) != 0 ? SQUARE_WAVE_AMPLITUDE : -SQUARE_WAVE_AMPLITUDE);
            this.phase = (this.phase + step) % 1.0;
        }
        audioRenderer.pushSamples8(data);
    }

}
