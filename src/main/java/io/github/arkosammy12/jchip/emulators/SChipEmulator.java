package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.hardware.SChipProcessor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.ProgramArgs;

import java.io.IOException;

public class SChipEmulator extends Chip8Emulator {

    private final boolean isModern;

    public SChipEmulator(ProgramArgs programArgs) throws IOException {
        super(programArgs);
        this.isModern = this.getConsoleVariant() == ConsoleVariant.SUPER_CHIP_MODERN;
    }

    @Override
    protected Processor createProcessor() {
        return new SChipProcessor(this);
    }

    @Override
    protected void runInstructionLoop() throws InvalidInstructionException {
        for (int i = 0; i < this.targetInstructionsPerFrame; i++) {
            boolean shouldWaitForNextFrame = this.processor.cycle(i < 1);
            if (this.displayWaitEnabled && shouldWaitForNextFrame && !this.isModern && !this.getDisplay().isExtendedMode()) {
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
