package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.ConsoleVariant;

public class FiveOpcodeInstruction extends Instruction {

    public FiveOpcodeInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int vX = emulator.getProcessor().getRegisterValue(firstRegister);
        int vY = emulator.getProcessor().getRegisterValue(secondRegister);
        int type = this.getFourthNibble();
        switch (type) {
            case 0x0 -> { // Skip if registers equal
                if (vX == vY) {
                    emulator.getProcessor().incrementProgramCounter();
                }
            }
            case 0x2 -> { // Copy values vX to vY to memory
                if (emulator.getProgramArgs().getConsoleVariant() != ConsoleVariant.XO_CHIP) {
                    break;
                }
                // TODO: Implement
            }
            case 0x3 -> { // Load values vY to vY from memory
                if (emulator.getProgramArgs().getConsoleVariant() != ConsoleVariant.XO_CHIP) {
                    break;
                }
                // TODO: Implement
            }
        }
    }

}
