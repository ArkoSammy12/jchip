package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.HyperWaveChip64Processor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;

import java.awt.event.KeyAdapter;
import java.util.List;

public class HyperWaveChip64Emulator<D extends HyperWaveChip64Display, S extends Chip8SoundSystem> extends XOChipEmulator<D, S> {

    public HyperWaveChip64Emulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new HyperWaveChip64Processor<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, List<KeyAdapter> keyAdapters) {
        return (D) new HyperWaveChip64Display(emulatorConfig, keyAdapters);
    }
}
