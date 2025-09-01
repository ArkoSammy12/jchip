package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Instruction;
import io.github.arkosammy12.jchip.instructions.Draw;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.ProgramArgs;
import io.github.arkosammy12.jchip.instructions.ZeroOpcodeInstruction;

import java.io.IOException;

public class SchipEmulator extends Chip8Emulator {

    private final boolean isModern;

    public SchipEmulator(ProgramArgs programArgs) throws IOException {
        super(programArgs);
        this.isModern = this.getConsoleVariant() == ConsoleVariant.SUPER_CHIP_MODERN || this.getConsoleVariant() ==ConsoleVariant.XO_CHIP;
    }

    @Override
    public void tick() throws IOException, InvalidInstructionException {
        for (int i = 0; i < this.instructionsPerFrame; i++) {
            Instruction executedInstruction = this.processor.cycle(i < 1);
            if (this.displayWaitEnabled && executedInstruction instanceof Draw && !this.isModern && !this.getDisplay().isExtendedMode()) {
                break;
            }
            if (executedInstruction instanceof ZeroOpcodeInstruction zeroOpcodeInstruction && zeroOpcodeInstruction.shouldTerminateEmulator()) {
                this.terminate();
            }
            if (this.getKeyState().shouldTerminate()) {
                this.terminate();
            }
        }
        this.getDisplay().flush();
        this.getAudioSystem().pushFrame(this.getProcessor().getSoundTimer());
    }

}
