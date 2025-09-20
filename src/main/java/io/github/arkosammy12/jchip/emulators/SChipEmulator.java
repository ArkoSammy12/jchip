package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.Chip8Processor;
import io.github.arkosammy12.jchip.hardware.SChipProcessor;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.Display;
import io.github.arkosammy12.jchip.video.SChipDisplay;

import java.awt.event.KeyAdapter;

public class SChipEmulator extends Chip8Emulator {

    protected boolean isModern;

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
    public SChipDisplay getDisplay() {
        return ((SChipDisplay) this.display);
    }

    @Override
    public Display createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return new SChipDisplay(emulatorConfig, keyAdapter);
    }

    @Override
    protected boolean waitForVBlank(int flags) {
        return this.config.doDisplayWait() && ((flags & Chip8Processor.DRAW_EXECUTED) != 0) && !this.isModern && !this.getDisplay().isExtendedMode();
    }

}
