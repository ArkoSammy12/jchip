package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SetRegister extends Instruction {

    public SetRegister(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int value = this.getSecondByte();
        emulator.getProcessor().setByteInRegister(register, value);
    }

}
