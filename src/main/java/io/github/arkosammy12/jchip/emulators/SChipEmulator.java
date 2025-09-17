package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.Chip8Processor;
import io.github.arkosammy12.jchip.hardware.SChipProcessor;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

public class SChipEmulator extends Chip8Emulator {

    private final boolean isModern;

    public SChipEmulator(EmulatorConfig emulatorConfig) throws Exception {
        super(emulatorConfig);
        this.isModern = this.getChip8Variant() == Chip8Variant.SUPER_CHIP_MODERN;
    }

    @Override
    protected Processor createProcessor() {
        return new SChipProcessor(this);
    }

    public boolean isModern() {
        return this.isModern;
    }

    @Override
    protected boolean waitForVBlank(int flags) {
        return this.config.doDisplayWait() && ((flags & Chip8Processor.DRAW_EXECUTED) != 0) && !this.isModern && !this.getDisplay().isExtendedMode();
    }

}
