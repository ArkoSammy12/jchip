package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SkipIfNotEqualsImmediate extends Instruction {

    public SkipIfNotEqualsImmediate(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int operand = this.getSecondByte();
        int registerValue = emulator.getProcessor().getByteInRegister(register);
        if (operand != registerValue) {
            int currentProgramCounter = emulator.getProcessor().getProgramCounter();
            emulator.getProcessor().setProgramCounter(currentProgramCounter + 2);
        }
    }

}
