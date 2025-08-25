package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SetRegisterInstruction extends Instruction {

    public SetRegisterInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int value = this.getSecondByte();
        emulator.getProcessor().setRegisterValue(register, value);
    }

}
