package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class AddToRegister extends Instruction {

    public AddToRegister(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int operand = this.getSecondByte();
        int vx = emulator.getProcessor().getByteInRegister(register);
        int value = (vx + operand) & 0xFF;
        emulator.getProcessor().setByteInRegister(register, value);
    }

}
