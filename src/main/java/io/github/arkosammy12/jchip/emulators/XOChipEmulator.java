package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class XOChipEmulator<M extends XOChipMemory, D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipEmulator<M, D, S> {

    public XOChipEmulator(EmulatorSettings emulatorSettings, Function<int[], M> memoryFactory, BiFunction<EmulatorSettings, List<KeyAdapter>, D> displayFactory, Function<EmulatorSettings, S> soundSystemFactory) {
        super(emulatorSettings, memoryFactory, displayFactory, soundSystemFactory, true);
    }

    @Override
    protected Chip8Processor<?, ?, ?, ?> createProcessor() {
        return new XOChipProcessor<>(this);
    }

}
