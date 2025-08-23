package io.github.arkosammy12.jchip.processor;

import com.googlecode.lanterna.input.KeyStroke;
import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.Utils;


import java.io.IOException;

public class SkipIfKey extends Instruction {

    public SkipIfKey(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) throws IOException {
        int register = this.getSecondNibble();
        int vx = emulator.getProcessor().getByteInRegister(register);
        int type = this.getSecondByte();
        int currentProgramCounter = emulator.getProcessor().getProgramCounter();
        KeyStroke keyStroke = emulator.pollInput();
        switch (type) {
            case 0x9E -> { // Skip if pressed
                if (keyStroke == null) {
                    break;
                }
                char c = keyStroke.getCharacter();
                int charAsIntValue = Utils.getValueForCharacter(c);
                if (charAsIntValue == vx) {
                    emulator.getProcessor().setProgramCounter(currentProgramCounter + 2);
                }
            }
            case 0xA1 -> { // Skip if not pressed
                if (keyStroke == null) {
                    emulator.getProcessor().setProgramCounter(currentProgramCounter + 2);
                    break;
                }
                char c = keyStroke.getCharacter();
                int charAsIntValue = Utils.getValueForCharacter(c);
                if (charAsIntValue != vx) {
                    emulator.getProcessor().setProgramCounter(currentProgramCounter + 2);
                }
            }
        }
    }



}
