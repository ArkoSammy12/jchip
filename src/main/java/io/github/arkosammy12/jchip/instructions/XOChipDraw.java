package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;

public class XOChipDraw extends Draw {

    public XOChipDraw(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = this.executionContext.getProcessor();
        Display display = this.executionContext.getDisplay();
        Memory memory = this.executionContext.getMemory();
        int selectedBitPlanes = processor.getSelectedBitPlanes();
        boolean collided = false;
        processor.setCarry(false);
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
                    int firstByte = memory.readByte(currentIndexRegister + (planeIterator * 2));
                    int secondByte = memory.readByte(currentIndexRegister + (planeIterator * 2) + 1);
                    slice = (firstByte << 8) | secondByte;
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
        processor.setCarry(collided);
    }

}
