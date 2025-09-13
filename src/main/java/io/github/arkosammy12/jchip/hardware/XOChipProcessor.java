package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.AudioSystem;
import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
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
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        Display display = this.emulator.getDisplay();
        boolean opcodeHandled = true;
        switch (thirdNibble) {
            case 0xD -> { // OODN: Scroll screen up
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
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
        boolean opcodeHandled = true;
        switch (fourthNibble) {
            case 0x2 -> { // 5XY2: Write vX to vY to memory
                int currentIndexRegister = this.getIndexRegister();
                boolean iterateInReverse = secondNibble > thirdNibble;
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
                int currentIndexRegister = this.getIndexRegister();
                boolean iterateInReverse = secondNibble > thirdNibble;
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
    protected boolean executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        Display display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int selectedBitPlanes = display.getSelectedBitPlanes();
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

        boolean draw16WideSprite = spriteHeight >= 16;
        int sliceLength = 8;
        if (draw16WideSprite) {
            sliceLength = 16;
        }

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
                    if (config.doClipping()) {
                        planeIterator++;
                        continue;
                    } else {
                        sliceY %= screenHeight;
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
                    if (sliceX >= screenWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= screenWidth;
                        }
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
                AudioSystem audioSystem = this.emulator.getAudioSystem();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    int audioByte = memory.readByte(currentIndexRegister + i);
                    audioSystem.loadPatternByte(i, audioByte);
                }
            }
            case 0x3A -> { // FX33: Set audio pattern pitch
                this.emulator.getAudioSystem().setPlaybackRate(vX);
            }
            default -> opcodeHanded = false;
        }
        return opcodeHanded;
    }

}
