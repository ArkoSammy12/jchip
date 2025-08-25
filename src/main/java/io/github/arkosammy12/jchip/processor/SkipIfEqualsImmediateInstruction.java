package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SkipIfEqualsImmediateInstruction extends Instruction {

    public SkipIfEqualsImmediateInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int operand = this.getSecondByte();
        int registerValue = emulator.getProcessor().getRegisterValue(register);
        if (operand == registerValue) {
            emulator.getProcessor().incrementProgramCounter();
        }
    }

}
