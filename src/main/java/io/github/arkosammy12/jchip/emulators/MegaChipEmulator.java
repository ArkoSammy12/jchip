package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.MegaChipProcessor;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.Display;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

import java.awt.event.KeyAdapter;

public class MegaChipEmulator extends SChipEmulator {

    public MegaChipEmulator(EmulatorConfig emulatorConfig) throws Exception {
        super(emulatorConfig);
    }

    @Override
    protected Processor createProcessor() {
        return new MegaChipProcessor(this);
    }

    @Override
    public MegaChipDisplay getDisplay() {
        return ((MegaChipDisplay) this.display);
    }

    @Override
    public Display createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return new MegaChipDisplay(emulatorConfig, keyAdapter);
    }

}
