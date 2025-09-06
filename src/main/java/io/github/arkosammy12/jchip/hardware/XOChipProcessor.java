package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.base.Memory;

public class XOChipProcessor extends Chip8Processor {

    public XOChipProcessor(Emulator emulator) {
        super(emulator);
    }

    @Override
    protected void executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        Display display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        int selectedBitPlanes = this.getSelectedBitPlanes();
        boolean extendedMode = display.isExtendedMode();
        int spriteHeight = fourthNibble;
        if (spriteHeight < 1) {
            spriteHeight = 16;
        }
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        if (!extendedMode) {
            screenWidth /= 2;
            screenHeight /= 2;
        }
        int spriteX = this.getRegister(secondNibble) % screenWidth;
        int spriteY = this.getRegister(thirdNibble) % screenHeight;
        int currentIndexRegister = this.getIndexRegister();

        boolean collided = false;
        this.setCarry(false);
        int planeIterator = 0;
        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= screenHeight) {
                    sliceY %= screenHeight;
                }
                int slice;
                int sliceLength;
                if (spriteHeight >= 16) {
                    int firstSliceByte = memory.readByte(currentIndexRegister + (planeIterator * 2));
                    int secondSliceByte = memory.readByte(currentIndexRegister + (planeIterator * 2) + 1);
                    slice = (firstSliceByte << 8) | secondSliceByte;
                    sliceLength = 16;
                } else {
                    slice = memory.readByte(currentIndexRegister + planeIterator);
                    sliceLength = 8;
                }
                for (int j = 0; j < sliceLength; j++) {
                    int sliceX = spriteX + j;
                    if (sliceX >= screenWidth) {
                        sliceX %= screenWidth;
                    }
                    int mask = 1 << ((sliceLength - 1) - j);
                    if ((slice & mask) <= 0) {
                        continue;
                    }
                    if (extendedMode) {
                        collided |= display.togglePixel(bitPlane, sliceX, sliceY);
                    } else {
                        collided |= display.togglePixel(bitPlane, sliceX * 2, sliceY * 2);
                        collided |= display.togglePixel(bitPlane, (sliceX * 2) + 1, sliceY * 2);
                        display.togglePixel(bitPlane, sliceX * 2, (sliceY * 2) + 1);
                        display.togglePixel(bitPlane, (sliceX * 2) + 1, (sliceY * 2) + 1);
                    }
                }
                planeIterator++;
            }
        }
        this.setCarry(collided);
    }

}
