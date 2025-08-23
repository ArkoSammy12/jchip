package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class JumpInstruction extends Instruction {

    public JumpInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int jumpAddress = this.getMemoryAddress();
        emulator.getProcessor().setProgramCounter(jumpAddress);
    }

}
