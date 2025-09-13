package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.XOChipProcessor;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.EmulatorConfig;

import java.io.IOException;

public class XOChipEmulator extends Chip8Emulator {

    public XOChipEmulator(EmulatorConfig emulatorConfig) throws IOException {
        super(emulatorConfig);
    }

    @Override
    protected Processor createProcessor() {
        return new XOChipProcessor(this);
    }

}
