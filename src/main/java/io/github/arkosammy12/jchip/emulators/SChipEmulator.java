package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.video.SChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipEmulator extends Chip8Emulator {

    @UnknownNullability
    private SChipProcessor<?> processor;

    @UnknownNullability
    private SChipDisplay<?> display;

    private final boolean isModern;

    public SChipEmulator(Chip8EmulatorSettings emulatorSettings, boolean isModern) {
        this.isModern = isModern;
        super(emulatorSettings);
    }

    @Override
    public @NotNull SChipProcessor<?> getProcessor() {
        return this.processor;
    }

    @Override
    public @NotNull SChipDisplay<?> getDisplay() {
        return this.display;
    }

    @Override
    protected void initializeDisplay() {
        this.display = new SChipDisplay<>(this, this.isModern);
    }

    @Override
    protected void initializeProcessor() {
        this.processor = new SChipProcessor<>(this);
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
