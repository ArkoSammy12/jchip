package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class XOChipProcessor extends SChipProcessor {

    public XOChipProcessor(Emulator emulator) {
        super(emulator);
    }

    @Override
    protected boolean executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        if (super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte)) {
            return true;
        }
        Display display = this.emulator.getDisplay();
        boolean opcodeHandled = true;
        switch (thirdNibble) {
            case 0xD -> { // OODN: Scroll screen up
                display.scrollUp(fourthNibble);
            }
            default -> opcodeHandled = false;
        }
        return opcodeHandled;
    }

    @Override
    protected boolean executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        if (super.executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte)) {
            return true;
        }
        Memory memory = this.emulator.getMemory();
        int currentIndexRegister = this.getIndexRegister();
        boolean iterateInReverse = secondNibble > thirdNibble;
        boolean opcodeHandled = true;
        switch (fourthNibble) {
            case 0x2 -> { // 5XY2: Write vX to vY to memory
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--) {
                        int registerValue = this.getRegister(i);
                        memory.writeByte(currentIndexRegister + j, registerValue);
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        int registerValue = this.getRegister(i);
                        memory.writeByte(currentIndexRegister + j, registerValue);
                        j++;
                    }
                }
            }
            case 0x3 -> { // 5XY3: Read values vX to vY from memory
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--) {
                        int memoryValue = memory.readByte(currentIndexRegister + j);
                        this.setRegister(i, memoryValue);
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        int memoryValue = memory.readByte(currentIndexRegister + j);
                        this.setRegister(i, memoryValue);
                        j++;
                    }
                }
            }
            default -> opcodeHandled = false;
        }
        return opcodeHandled;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected boolean executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        Display display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();
        int selectedBitPlanes = display.getSelectedBitPlanes();

        int spriteHeight = fourthNibble;
        if (spriteHeight < 1) {
            spriteHeight = 16;
        }

        int logicalScreenWidth = display.getFrameBufferWidth();
        int logicalScreenHeight = display.getFrameBufferHeight();
        if (!extendedMode) {
            logicalScreenWidth /= 2;
            logicalScreenHeight /= 2;
        }

        int spriteX = this.getRegister(secondNibble) % logicalScreenWidth;
        int spriteY = this.getRegister(thirdNibble) % logicalScreenHeight;

        boolean draw16WideSprite = spriteHeight >= 16;

        int sliceLength = 8;
        if (draw16WideSprite) {
            sliceLength = 16;
        }

        boolean collided = false;
        int planeIterator = 0;
        this.setVF(false);

        for (int bitPlane = 0; bitPlane < 4; bitPlane++) {
            int bitPlaneMask = 1 << bitPlane;
            if ((bitPlaneMask & selectedBitPlanes) <= 0) {
                continue;
            }
            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= logicalScreenHeight) {
                    if (config.doClipping()) {
                        planeIterator++;
                        continue;
                    } else {
                        sliceY %= logicalScreenHeight;
                    }
                }
                int slice;
                if (draw16WideSprite) {
                    int firstSliceByte = memory.readByte(currentIndexRegister + (planeIterator * 2));
                    int secondSliceByte = memory.readByte(currentIndexRegister + (planeIterator * 2) + 1);
                    slice = (firstSliceByte << 8) | secondSliceByte;
                } else {
                    slice = memory.readByte(currentIndexRegister + planeIterator);
                }
                for (int j = 0; j < sliceLength; j++) {
                    int sliceX = spriteX + j;
                    if (sliceX >= logicalScreenWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= logicalScreenWidth;
                        }
                    }
                    int mask = 1 << ((sliceLength - 1) - j);
                    if ((slice & mask) <= 0) {
                        continue;
                    }
                    if (extendedMode) {
                        collided |= display.togglePixel(bitPlane, sliceX, sliceY);
                    } else {
                        int scaledSliceX = sliceX * 2;
                        int scaledSliceY = sliceY * 2;
                        collided |= display.togglePixel(bitPlane, scaledSliceX, scaledSliceY);
                        collided |= display.togglePixel(bitPlane, scaledSliceX + 1, scaledSliceY);
                        display.togglePixel(bitPlane, scaledSliceX, scaledSliceY + 1);
                        display.togglePixel(bitPlane, scaledSliceX + 1, scaledSliceY + 1);
                    }
                }
                planeIterator++;
            }
        }
        this.setVF(collided);
        return true;
    }

    @Override
    protected boolean executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        if (super.executeFXOpcode(firstNibble, secondNibble, secondByte)) {
            return true;
        }
        int vX = this.getRegister(secondNibble);
        boolean opcodeHanded = true;
        switch (secondByte) {
            case 0x00 -> { // F000 NNNN: Set index register to 16-bit address
                if (secondNibble != 0) {
                    return false;
                }
                Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                int firstAddressByte = memory.readByte(currentProgramCounter);
                int secondAddressByte = memory.readByte(currentProgramCounter + 1);
                int address = (firstAddressByte << 8) | secondAddressByte;
                this.setIndexRegister(address);
                this.incrementProgramCounter();
            }
            case 0x01 -> { // FX01: Set selected bit planes
                this.emulator.getDisplay().setSelectedBitPlanes(secondNibble);
            }
            case 0x02 -> { // F002: Load audio pattern
                if (secondNibble != 0) {
                    return false;
                }
                Memory memory = this.emulator.getMemory();
                SoundSystem soundSystem = this.emulator.getSoundSystem();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    int audioByte = memory.readByte(currentIndexRegister + i);
                    soundSystem.loadPatternByte(i, audioByte);
                }
            }
            case 0x3A -> { // FX33: Set audio pattern pitch
                this.emulator.getSoundSystem().setPlaybackRate(vX);
            }
            default -> opcodeHanded = false;
        }
        return opcodeHanded;
    }

}
