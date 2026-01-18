package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChip10Processor;
import io.github.arkosammy12.jchip.disassembler.AbstractDisassembler;
import io.github.arkosammy12.jchip.disassembler.SChip10Disassembler;
import io.github.arkosammy12.jchip.video.SChip10Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChip10Emulator extends Chip48Emulator {

    public static final String FLAG_REGISTERS_ENTRY_KEY = "schip10.processor.flag_registers";

    @Nullable
    private SChip10Processor<?> processor;

    @Nullable
    private SChip10Display<?> display;

    public SChip10Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChip10Processor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChip10Display<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    protected SChip10Processor<?> createProcessor() {
        this.processor = new SChip10Processor<>(this);
        return this.processor;
    }

    @Override
    @NotNull
    protected SChip10Display<?> createDisplay() {
        this.display = new SChip10Display<>(this);
        return this.display;
    }

    @Override
    protected boolean waitVBlank(int flags) {
        return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isHiresMode();
    }

    protected AbstractDisassembler<?> createDisassembler() {
        return new SChip10Disassembler<>(this);
    }

}
