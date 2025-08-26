package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.EmulatorScreen;

public class DisplayInstruction extends Instruction {

    public DisplayInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }


    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int height = this.getFourthNibble();
        int column = emulator.getProcessor().getRegisterValue(firstRegister) % EmulatorScreen.SCREEN_WIDTH;
        int row = emulator.getProcessor().getRegisterValue(secondRegister) % EmulatorScreen.SCREEN_HEIGHT;
        emulator.getProcessor().setCarry(false);
        boolean toggledOffOcurred = false;
        for (int i = 0; i < height; i++) {
            int spriteY = row + i;
            // Clip sprites vertically. COSMAC CHIP-8 quirk
            if (spriteY >= EmulatorScreen.SCREEN_HEIGHT) {
                break;
            }
            int indexRegisterValue = emulator.getProcessor().getIndexRegister();
            int sprite = emulator.getMemory().read(indexRegisterValue + i);
            for (int j = 0; j < 8; j++) {
                int spriteX = column + j;
                // Clip sprites horizontally. COSMAC CHIP-8 quirk
                if (spriteX >= EmulatorScreen.SCREEN_WIDTH) {
                    break;
                }
                int mask = (int) Math.pow(2, 7 - j);
                if ((sprite & mask) <= 0) {
                    continue;
                }
                boolean toggledOff = emulator.getEmulatorScreen().togglePixelAt(spriteX, spriteY);
                if (!toggledOffOcurred && toggledOff) {
                    toggledOffOcurred = true;
                }
                emulator.getProcessor().setCarry(toggledOffOcurred);
            }

        }

    }



}
