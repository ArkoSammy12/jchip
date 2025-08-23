package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

import java.util.Random;

public class RandomNumberInstruction extends Instruction {

    public RandomNumberInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int operand = this.getSecondByte();
        int random = (new Random()).nextInt();
        int value = (random & operand) & 0xFF;
        emulator.getProcessor().setByteInRegister(register, value);
    }

}
