package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

import java.io.IOException;

public class SkipIfKeyInstruction extends Instruction {

    public SkipIfKeyInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) throws IOException {
        int register = this.getSecondNibble();
        int vX = emulator.getProcessor().getRegisterValue(register);
        int keyCode = vX & 0xF;
        int type = this.getSecondByte();
        switch (type) {
            case 0x9E -> { // Skip if pressed
                if (emulator.getKeyState().isKeyPressed(keyCode)) {
                    emulator.getProcessor().incrementProgramCounter();
                }
            }
            case 0xA1 -> { // Skip if not pressed
                if (!emulator.getKeyState().isKeyPressed(keyCode)) {
                    emulator.getProcessor().incrementProgramCounter();
                }
            }
        }
    }

}
