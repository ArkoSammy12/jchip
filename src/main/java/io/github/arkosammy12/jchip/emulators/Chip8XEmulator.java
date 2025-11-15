package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.memory.Chip8XMemory;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

public class Chip8XEmulator extends Chip8Emulator {

    private Chip8XProcessor<?> processor;
    private Chip8XMemory memory;
    private Chip8XDisplay<?> display;

    public Chip8XEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    public Chip8XProcessor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public Chip8XMemory getMemory() {
        return this.memory;
    }

    @Override
    public Chip8XDisplay<?> getDisplay() {
        return this.display;
    }

    @Override
    protected void initializeDisplay() {
        this.display = new Chip8XDisplay<>(this);
    }

    @Override
    protected void initializeMemory() {
        this.memory = new Chip8XMemory(this);
    }

    @Override
    protected void initializeProcessor() {
        this.processor = new Chip8XProcessor<>(this);
    }

}
