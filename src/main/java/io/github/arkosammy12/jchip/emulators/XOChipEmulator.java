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

    @Override
    protected void runInstructionLoop() throws InvalidInstructionException {
        for (int i = 0; i < this.targetInstructionsPerFrame; i++) {
            boolean shouldWaitForNextFrame = this.processor.cycle(i < 1);
            if (this.config.doDisplayWait() && shouldWaitForNextFrame && !this.getDisplay().isExtendedMode()) {
                break;
            }
            if (this.processor.shouldTerminate()) {
                this.terminate();
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminate();
            }
        }
    }

}
