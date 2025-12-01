package io.github.arkosammy12.jchip.sound;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.vip.IODevice;

import static io.github.arkosammy12.jchip.sound.Chip8SoundSystem.SQUARE_WAVE_AMPLITUDE;

public class VP595 implements SoundSystem, IODevice {

    private final Jchip jchip;

    private double frequencyLatch = 27535.0 / (0x80 + 1);
    private double phase = 0.0;

    public VP595(Emulator emulator) {
        this.jchip = emulator.getEmulatorSettings().getJchip();
    }

    @Override
    public boolean isOutputPort(int port) {
        return port == 3;
    }

    @Override
    public void onOutput(int port, int value) {
        int actualValue = value != 0 ? value : 0x80;
        this.frequencyLatch = 27535.0 / (actualValue + 1);
    }

    @Override
    public void pushSamples(int soundTimer) {
        double frequency = frequencyLatch;
        if (soundTimer <= 0) {
            phase = 0;
            return;
        }
        byte[] data = new byte[SAMPLES_PER_FRAME];
        double step = frequency / SAMPLE_RATE;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((phase < 0.5) ? SQUARE_WAVE_AMPLITUDE : -SQUARE_WAVE_AMPLITUDE);
            phase = (phase + step) % 1;
        }
        this.jchip.getSoundWriter().pushSamples(data);
    }

}
