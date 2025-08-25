package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SkipIfRegistersNotEqualInstruction extends Instruction {

    public SkipIfRegistersNotEqualInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int vX = emulator.getProcessor().getRegisterValue(firstRegister);
        int vY = emulator.getProcessor().getRegisterValue(secondRegister);
        if (vX != vY) {
            emulator.getProcessor().incrementProgramCounter();
        }
    }

}
