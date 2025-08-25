package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class AddToRegisterInstruction extends Instruction {

    public AddToRegisterInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int operand = this.getSecondByte();
        int vX = emulator.getProcessor().getRegisterValue(register);
        int value = (vX + operand) & 0xFF;
        emulator.getProcessor().setRegisterValue(register, value);
    }

}
