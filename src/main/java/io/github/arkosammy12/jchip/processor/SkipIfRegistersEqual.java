package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class SkipIfRegistersEqual extends Instruction {

    public SkipIfRegistersEqual(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int vx = emulator.getProcessor().getByteInRegister(firstRegister);
        int vy = emulator.getProcessor().getByteInRegister(secondRegister);
        if (vx == vy) {
            int currentProgramCounter = emulator.getProcessor().getProgramCounter();
            emulator.getProcessor().setProgramCounter(currentProgramCounter + 2);
        }
    }

}
