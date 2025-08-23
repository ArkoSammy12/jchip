package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class ZeroOpcodeInstruction extends Instruction {

    public ZeroOpcodeInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        switch (this.getSecondByte()) {
            case 0xE0 -> { // Clear screen
                emulator.getEmulatorScreen().clear();
            }
            case 0xEE -> { // Return from subroutine
                int returnAddress = emulator.getProcessor().pop();
                emulator.getProcessor().setProgramCounter(returnAddress);
            }
        }
    }

}
