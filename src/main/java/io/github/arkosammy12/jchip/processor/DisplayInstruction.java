package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.ConsoleVariant;

public class DisplayInstruction extends Instruction {

    public DisplayInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int spriteHeight = this.getFourthNibble();
        int currentIndexRegister = emulator.getProcessor().getIndexRegister();
        int screenWidth = emulator.getEmulatorScreen().getScreenWidth();
        int screenHeight = emulator.getEmulatorScreen().getScreenHeight();
        boolean extendedMode = emulator.getEmulatorScreen().isExtendedMode();
        ConsoleVariant consoleVariant = emulator.getConsoleVariant();
        // Halve screen height and width to account for doubled sprite size in lores mode to fix clipping
        if (!extendedMode && consoleVariant != ConsoleVariant.CHIP_8) {
            screenHeight /= 2;
            screenWidth /= 2;
        }
        int spriteX = emulator.getProcessor().getRegisterValue(firstRegister) % screenWidth;
        int spriteY = emulator.getProcessor().getRegisterValue(secondRegister) % screenHeight;
        int collisionCounter = 0;
        if (spriteHeight < 1) {
            if (!consoleVariant.isSchipOrXoChip()) {
                return;
            }
            spriteHeight = 16;
        }
        emulator.getProcessor().setCarry(false);
        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= screenHeight) {
                if (consoleVariant == ConsoleVariant.XO_CHIP) {
                    sliceY %= screenHeight;
                } else {
                    if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && extendedMode) {
                        collisionCounter++;
                        continue;
                    }
                    break;
                }
            }
            int slice;
            int sliceLength;
            if (consoleVariant.isSchipOrXoChip() && spriteHeight >= 16) {
                int firstByte = emulator.getMemory().read((currentIndexRegister + i * 2) & 0xFFF);
                int secondByte = emulator.getMemory().read((currentIndexRegister + (i * 2) + 1) & 0xFFF);
                slice = (firstByte << 8) | secondByte;
                sliceLength = 16;
            } else {
                slice = emulator.getMemory().read((currentIndexRegister + i) & 0xFFF);
                sliceLength = 8;
            }
            boolean rowHasCollision = false;
            for (int j = 0; j < sliceLength; j++) {
                int sliceX = spriteX + j;
                if (sliceX >= screenWidth) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP) {
                        sliceX %= screenWidth;
                    } else {
                        break;
                    }
                }
                int mask = (int) Math.pow(2, (sliceLength - 1) - j);
                if ((slice & mask) <= 0) {
                    continue;
                }
                boolean toggledOff;
                if (extendedMode || consoleVariant == ConsoleVariant.CHIP_8) {
                    toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX, sliceY);
                } else {
                    // TODO: Copy top row of pixels into bottom one instead of xoring bottom row for schip-legacy
                    toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, sliceY * 2);
                    toggledOff |= emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, sliceY * 2);
                    emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, (sliceY * 2) + 1);
                    emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, (sliceY * 2) + 1);
                }
                if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && toggledOff && extendedMode) {
                    rowHasCollision = true;
                } else if (toggledOff) {
                    collisionCounter = 1;
                }
            }
            if (rowHasCollision && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                collisionCounter++;
            }
        }
        emulator.getProcessor().setRegisterValue(0xF, collisionCounter & 0xFF);
    }

}
