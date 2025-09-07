package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.SChipProcessor;
import io.github.arkosammy12.jchip.hardware.XOChipProcessor;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.ProgramArgs;

import java.io.IOException;

public class XOChipEmulator extends Chip8Emulator {

    public XOChipEmulator(ProgramArgs programArgs) throws IOException {
        super(programArgs);
    }

    @Override
    protected Processor createProcessor() {
        return new XOChipProcessor(this);
    }

    @Override
    protected void runInstructionLoop() throws InvalidInstructionException {
        for (int i = 0; i < this.targetInstructionsPerFrame; i++) {
            boolean shouldWaitForNextFrame = this.processor.cycle(i < 1);
            if (this.displayWaitEnabled && shouldWaitForNextFrame && !this.getDisplay().isExtendedMode()) {
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
