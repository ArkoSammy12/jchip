package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.Chip8Processor;
import io.github.arkosammy12.jchip.hardware.XOChipProcessor;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

import java.awt.event.KeyAdapter;

public class XOChipEmulator extends SChipEmulator {

    public XOChipEmulator(EmulatorConfig emulatorConfig) throws Exception {
        super(emulatorConfig);
        this.isModern = true;
    }

    @Override
    protected Processor createProcessor() {
        return new XOChipProcessor(this);
    }

    @Override
    public XOChipDisplay getDisplay() {
        return ((XOChipDisplay) this.display);
    }

    @Override
    public Display createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return new XOChipDisplay(emulatorConfig, keyAdapter);
    }

    @Override
    protected boolean waitForVBlank(int flags) {
        return this.config.doDisplayWait() && ((flags & Chip8Processor.DRAW_EXECUTED) != 0) && !this.getDisplay().isExtendedMode();
    }

}
