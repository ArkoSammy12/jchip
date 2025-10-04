package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.SpriteFont;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

import java.awt.event.KeyAdapter;

public class Chip8XEmulator<D extends Chip8XDisplay, S extends Chip8SoundSystem> extends Chip8Emulator<D, S> {

    public Chip8XEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new Chip8XProcessor<>(this);
    }

    @Override
    protected Chip8Memory createMemory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        return new Chip8Memory(program, chip8Variant, spriteFont, 0x300, 0xFFF + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new Chip8XDisplay(emulatorConfig, keyAdapter);
    }

}
