package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.ConsoleVariant;
import io.github.arkosammy12.jchip.io.EmulatorScreen;

public class DisplayInstruction extends Instruction {

    public DisplayInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }


    @Override
    public void execute(Emulator emulator) {
         int firstRegister = this.getSecondNibble();
         int secondRegister = this.getThirdNibble();
         int screenWidth = emulator.getEmulatorScreen().getScreenWidth();
         int screenHeight = emulator.getEmulatorScreen().getScreenHeight();
         int column = emulator.getProcessor().getRegisterValue(firstRegister) % screenWidth;
         int row = emulator.getProcessor().getRegisterValue(secondRegister) % screenHeight;
         int currentIndexRegister = emulator.getProcessor().getIndexRegister();
         ConsoleVariant consoleVariant = emulator.getConsoleVariant();
         int collisionCounter = 0;
         boolean extendedMode = emulator.getEmulatorScreen().isExtendedMode();
         int height = this.getFourthNibble();
         if (height < 1) {
             if (!consoleVariant.isSchipOrXoChip()) {
                 return;
             }
             height = 16;
         }
         emulator.getProcessor().setCarry(false);
         for (int i = 0; i < height; i++) {
             int sliceY = row + i;
             if (sliceY >= screenHeight) {
                 if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                     collisionCounter++;
                     continue;
                 } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                     sliceY %= screenHeight;
                 }
             }
             int slice;
             int sliceLength;
             if (consoleVariant.isSchipOrXoChip() && height >= 16) {
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
                 int sliceX = column + j;
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
                     toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, sliceY * 2);
                     emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, sliceY * 2);
                     emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, (sliceY * 2) + 1);
                     emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, (sliceY * 2) + 1);
                 }
                 if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && toggledOff) {
                     rowHasCollision = true;
                 } else if (toggledOff) {
                     collisionCounter = 1;
                 }
             }
             if (rowHasCollision && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                 collisionCounter++;
             }
         }
        emulator.getProcessor().setRegisterValue(0xF, collisionCounter);
    }

}
