package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

import java.awt.event.KeyAdapter;

public class Chip8XEmulator<D extends Chip8XDisplay, S extends Chip8SoundSystem> extends Chip8Emulator<D, S> {

    public Chip8XEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
    }

    @Override
    protected Processor createProcessor() {
        return new Chip8XProcessor<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new Chip8XDisplay(emulatorConfig, keyAdapter);
    }

}
