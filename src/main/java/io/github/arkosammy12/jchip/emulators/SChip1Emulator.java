package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChip1Processor;
import io.github.arkosammy12.jchip.video.SChip1Display;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChip1Emulator extends Chip8Emulator {

    @Nullable
    private SChip1Processor<?> processor;

    @Nullable
    private SChip1Display<?> display;

    public SChip1Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChip1Processor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChip1Display<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    protected SChip1Processor<?> createProcessor() {
        this.processor = new SChip1Processor<>(this);
        return this.processor;
    }

    @Override
    protected SChip1Display<?> createDisplay() {
        this.display = new SChip1Display<>(this);
        return this.display;
    }

    @Override
    protected boolean waitVBlank(int flags) {
        return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isHiresMode();
    }

}
