package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class JumpWithOffsetInstruction extends Instruction {

    public JumpWithOffsetInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int memoryAddress = this.getMemoryAddress();
        int v0 = emulator.getProcessor().getByteInRegister(0x0);
        int finalAddress = memoryAddress + v0;
        emulator.getProcessor().setProgramCounter(finalAddress);
    }

}
