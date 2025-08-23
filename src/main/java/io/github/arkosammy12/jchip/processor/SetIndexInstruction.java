package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SetIndexInstruction extends Instruction {

    public SetIndexInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int memoryAddress = this.getMemoryAddress();
        emulator.getProcessor().setIndexRegister(memoryAddress);
    }

}
