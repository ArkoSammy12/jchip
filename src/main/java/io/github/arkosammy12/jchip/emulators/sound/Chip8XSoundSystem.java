package io.github.arkosammy12.jchip.emulators.sound;

import io.github.arkosammy12.jchip.emulators.Chip8XEmulator;
import io.github.arkosammy12.jchip.main.AudioRenderer;

public class Chip8XSoundSystem extends Chip8SoundSystem {

    public Chip8XSoundSystem(Chip8XEmulator emulator) {
        super(emulator);
    }

    public void setPitch(int value) {
        int actualValue = value != 0 ? value : 0x80;
        this.step = (27535.0 / (actualValue + 1)) / SAMPLE_RATE;
    }

    @Override
    public void pushSamples(int soundTimer) {
        if (soundTimer <= 0) {
            this.phase = 0;
            return;
        }
        AudioRenderer audioRenderer = this.jchip.getAudioRenderer();
        byte[] data = new byte[audioRenderer.getSamplesPerFrame()];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((phase < 0.5) ? SQUARE_WAVE_AMPLITUDE : -SQUARE_WAVE_AMPLITUDE);
            this.phase = (phase + step) % 1;
        }
        audioRenderer.pushSamples8(data);
    }

}
