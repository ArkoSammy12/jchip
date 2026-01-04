package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.SChip11Processor;
import io.github.arkosammy12.jchip.disassembler.AbstractDisassembler;
import io.github.arkosammy12.jchip.disassembler.SChip11Disassembler;
import io.github.arkosammy12.jchip.video.SChip11Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SChip11Emulator extends SChip10Emulator {

    @Nullable
    private SChip11Processor<?> processor;

    @Nullable
    private SChip11Display<?> display;

    public SChip11Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChip11Processor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChip11Display<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    protected SChip11Processor<?> createProcessor() {
        this.processor = new SChip11Processor<>(this);
        return this.processor;
    }

    @Override
    @NotNull
    protected SChip11Display<?> createDisplay() {
        this.display = new SChip11Display<>(this);
        return this.display;
    }

    protected AbstractDisassembler<?> createDisassembler() {
        return new SChip11Disassembler<>(this);
    }

}
