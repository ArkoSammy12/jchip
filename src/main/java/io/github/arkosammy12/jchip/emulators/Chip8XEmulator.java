package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.memory.Chip8XMemory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Chip8XEmulator<M extends Chip8XMemory, D extends Chip8XDisplay, S extends Chip8SoundSystem> extends Chip8Emulator<M, D, S> {

    public Chip8XEmulator(EmulatorSettings emulatorSettings, Function<int[], M> memoryFactory, BiFunction<EmulatorSettings, List<KeyAdapter>, D> displayFactory, Function<EmulatorSettings, S> soundSystemFactory) {
        super(emulatorSettings, memoryFactory, displayFactory, soundSystemFactory);
    }

    @Override
    protected Chip8Processor<?, ?, ?, ?> createProcessor() {
        return new Chip8XProcessor<>(this);
    }

}
