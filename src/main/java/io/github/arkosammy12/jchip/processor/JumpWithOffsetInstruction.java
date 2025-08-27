package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class JumpWithOffsetInstruction extends Instruction {

    public JumpWithOffsetInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int register = this.getSecondNibble();
        int memoryAddress = this.getMemoryAddress();
        int offsetRegister = emulator.getConsoleVariant().isSchip() ? register : 0x0;
        int offset = emulator.getProcessor().getRegisterValue(offsetRegister);
        int jumpAddress = memoryAddress + offset;
        emulator.getProcessor().setProgramCounter(jumpAddress);
    }

}
