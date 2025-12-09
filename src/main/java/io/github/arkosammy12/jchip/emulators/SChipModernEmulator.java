package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChip1Processor;
import io.github.arkosammy12.jchip.cpu.SChipModernProcessor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.video.SChip1Display;
import io.github.arkosammy12.jchip.video.SChipDisplay;
import io.github.arkosammy12.jchip.video.SChipModernDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipModernEmulator extends SChipEmulator {

    @Nullable
    private SChipModernProcessor<?> processor;

    @Nullable
    private SChipModernDisplay<?> display;

    public SChipModernEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChipModernProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChipModernDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    protected SChipModernProcessor<?> createProcessor() {
        this.processor = new SChipModernProcessor<>(this);
        return this.processor;
    }

    @Override
    protected SChipModernDisplay<?> createDisplay() {
        this.display = new SChipModernDisplay<>(this);
        return this.display;
    }

    @Override
    protected boolean waitVBlank(int flags) {
        return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED);
    }

}
