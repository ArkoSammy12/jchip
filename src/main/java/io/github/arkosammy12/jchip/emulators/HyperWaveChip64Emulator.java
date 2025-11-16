package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.HyperWaveChip64Processor;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

public class HyperWaveChip64Emulator extends XOChipEmulator {

    @Nullable
    private HyperWaveChip64Processor<?> processor;

    @Nullable
    private HyperWaveChip64Display<?> display;

    public HyperWaveChip64Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public HyperWaveChip64Processor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public HyperWaveChip64Display<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @Nullable
    protected HyperWaveChip64Processor<?> createProcessor() {
        this.processor = new HyperWaveChip64Processor<>(this);
        return null;
    }

    @Override
    @Nullable
    protected HyperWaveChip64Display<?> createDisplay() {
        this.display = new HyperWaveChip64Display<>(this);
        return null;
    }

}
