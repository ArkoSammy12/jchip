package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class CallInstruction extends Instruction {

    public CallInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int subroutineAddress = this.getMemoryAddress();
        int currentProgramCounter = emulator.getProcessor().getProgramCounter();
        emulator.getProcessor().push(currentProgramCounter);
        emulator.getProcessor().setProgramCounter(subroutineAddress);
    }

}
