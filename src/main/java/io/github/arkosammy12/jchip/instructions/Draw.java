package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;

public class Draw extends AbstractInstruction {

    protected final int spriteX;
    protected final int spriteY;
    protected final int spriteHeight;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int currentIndexRegister;
    protected final boolean extendedMode;
    protected final ConsoleVariant consoleVariant;

    public Draw(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        Display display = executionContext.getDisplay();
        this.consoleVariant = executionContext.getConsoleVariant();
        this.extendedMode = display.isExtendedMode();
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int spriteHeight = this.getFourthNibble();
        if (spriteHeight < 1 && consoleVariant.isSchipOrXoChip()) {
            spriteHeight = 16;
        }
        this.spriteHeight = spriteHeight;
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        if (!this.extendedMode && consoleVariant != ConsoleVariant.CHIP_8) {
            screenWidth /= 2;
            screenHeight /= 2;
        }
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.spriteX = processor.getRegister(firstRegister) % screenWidth;
        this.spriteY = processor.getRegister(secondRegister) % screenHeight;
        this.currentIndexRegister = processor.getIndexRegister();
    }

    @Override
    public void execute() {
        Processor processor = this.executionContext.getProcessor();
        Display display = this.executionContext.getDisplay();
        Memory memory = this.executionContext.getMemory();
        int collisionCounter = 0;
        processor.setCarry(false);
        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= screenHeight) {
                if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && extendedMode) {
                    collisionCounter++;
                    continue;
                }
                break;
            }
            int slice;
            int sliceLength;
            if (consoleVariant.isSchipOrXoChip() && spriteHeight >= 16) {
                int firstByte = memory.readByte(currentIndexRegister + i * 2);
                int secondByte = memory.readByte(currentIndexRegister + (i * 2) + 1);
                slice = (firstByte << 8) | secondByte;
                sliceLength = 16;
            } else {
                slice = memory.readByte(currentIndexRegister + i);
                sliceLength = 8;
            }
            boolean rowCollided = false;
            for (int j = 0; j < sliceLength; j++) {
                int sliceX = spriteX + j;
                if (sliceX >= screenWidth) {
                    break;
                }
                int mask = 1 << ((sliceLength - 1) - j);
                if ((slice & mask) <= 0) {
                    continue;
                }
                if (extendedMode || consoleVariant == ConsoleVariant.CHIP_8) {
                    rowCollided |= display.togglePixel(0, sliceX, sliceY);
                } else {
                    rowCollided |= display.togglePixel(0, sliceX * 2, sliceY * 2);
                    rowCollided |= display.togglePixel(0, (sliceX * 2) + 1, sliceY * 2);
                    boolean topLeftPixel = display.getPixel(0, sliceX * 2, sliceY * 2);
                    boolean topRightPixel = display.getPixel(0, (sliceX * 2) + 1, sliceY * 2);
                    display.setPixel(0, sliceX * 2, (sliceY * 2) + 1, topLeftPixel);
                    display.setPixel(0, (sliceX * 2) + 1, (sliceY * 2) + 1, topRightPixel);
                }
            }
            if (!rowCollided) {
                continue;
            }
            if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && extendedMode) {
                collisionCounter++;
            } else {
                collisionCounter = 1;
            }
        }
        processor.setRegister(0xF, collisionCounter & 0xFF);
    }

}
