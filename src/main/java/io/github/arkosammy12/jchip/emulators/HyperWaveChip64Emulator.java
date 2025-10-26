package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.HyperWaveChip64Processor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class HyperWaveChip64Emulator<D extends HyperWaveChip64Display, S extends Chip8SoundSystem> extends XOChipEmulator<D, S> {

    public HyperWaveChip64Emulator(EmulatorInitializer emulatorInitializer, BiFunction<EmulatorInitializer, List<KeyAdapter>, D> displayFactory, Function<EmulatorInitializer, S> soundSystemFactory) {
        super(emulatorInitializer, displayFactory, soundSystemFactory);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new HyperWaveChip64Processor<>(this);
    }

}
