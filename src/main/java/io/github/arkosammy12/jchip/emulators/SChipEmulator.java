package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.video.SChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipEmulator extends Chip8Emulator {

    @Nullable
    private SChipProcessor<?> processor;

    @Nullable
    private SChipDisplay<?> display;

    private final boolean isModern;

    public SChipEmulator(Chip8EmulatorSettings emulatorSettings, boolean isModern) {
        this.isModern = isModern;
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChipProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChipDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @Nullable
    protected SChipProcessor<?> createProcessor() {
        this.processor = new SChipProcessor<>(this);
        return null;
    }

    @Override
    @Nullable
    protected SChipDisplay<?> createDisplay() {
        this.display = new SChipDisplay<>(this, this.isModern);
        return null;
    }

    public boolean isModern() {
        return this.isModern;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (this.isModern) {
            return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED);
        } else {
            return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isExtendedMode();
        }
    }

}
