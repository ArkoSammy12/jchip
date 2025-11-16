package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.HyperWaveChip64Processor;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class HyperWaveChip64Emulator extends XOChipEmulator {

    @UnknownNullability
    private HyperWaveChip64Processor<?> processor;

    @UnknownNullability
    private HyperWaveChip64Display<?> display;

    public HyperWaveChip64Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    public @NotNull HyperWaveChip64Processor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public @NotNull HyperWaveChip64Display<?> getDisplay() {
        return this.display;
    }

    @Override
    protected void initializeDisplay() {
        this.display = new HyperWaveChip64Display<>(this);
    }

    @Override
    protected void initializeProcessor() {
        this.processor = new HyperWaveChip64Processor<>(this);
    }


}
