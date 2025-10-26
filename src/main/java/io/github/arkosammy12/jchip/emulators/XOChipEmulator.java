package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class XOChipEmulator<D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipEmulator<D, S> {

    public XOChipEmulator(EmulatorInitializer emulatorInitializer, BiFunction<EmulatorInitializer, List<KeyAdapter>, D> displayFactory, Function<EmulatorInitializer, S> soundSystemFactory) {
        super(emulatorInitializer, displayFactory, soundSystemFactory, true);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new XOChipProcessor<>(this);
    }

    @Override
    protected Chip8Memory createMemory(int[] rom, Chip8Variant chip8Variant) {
        return new Chip8Memory(rom, chip8Variant, 0x200, 0xFFFF + 1);
    }

}
