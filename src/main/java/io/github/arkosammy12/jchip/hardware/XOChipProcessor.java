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
        this.isModern = true;
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = Chip8Processor.HANDLED;
        Display display = this.emulator.getDisplay();
        if (thirdNibble == 0xD) {// OODN: Scroll screen up
            display.scrollUp(fourthNibble);
        } else {
            flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeSkipIfEqualsImmediate(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfEqualsImmediate(secondNibble, secondByte));
    }

    @Override
    protected int executeSkipIfNotEqualsImmediate(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfNotEqualsImmediate(secondNibble, secondByte));
    }

    @Override
    protected int executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            if ((flagsSuper & Chip8Processor.SKIP_TAKEN) != 0 && this.previousOpcodeWasF000()) {
                this.incrementProgramCounter();
            }
            return flagsSuper;
        }
        int flags = HANDLED;
        Memory memory = this.emulator.getMemory();
        int currentIndexRegister = this.getIndexRegister();
        boolean iterateInReverse = secondNibble > thirdNibble;
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
            default -> flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfRegistersNotEqual(secondNibble, thirdNibble));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
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
        int baseMask = Chip8Processor.BASE_SLICE_MASK_8;
        if (draw16WideSprite) {
            sliceLength = 16;
            baseMask = SChipProcessor.BASE_SLICE_MASK_16;
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
                for (int j = 0, mask = baseMask; j < sliceLength; j++, mask >>>= 1) {
                    int sliceX = spriteX + j;
                    if (sliceX >= logicalScreenWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= logicalScreenWidth;
                        }
                    }
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
        return Chip8Processor.HANDLED | Chip8Processor.DRAW_EXECUTED;
    }

    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfKey(secondNibble, secondByte));
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = Chip8Processor.HANDLED;
        int vX = this.getRegister(secondNibble);
        switch (secondByte) {
            case 0x00 -> { // F000 NNNN: Set index register to 16-bit address
                if (secondNibble != 0) {
                    return 0;
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
                    return 0;
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
            default -> flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if ((flags & Chip8Processor.HANDLED) != 0 && (flags & Chip8Processor.SKIP_TAKEN) != 0 && this.previousOpcodeWasF000()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWasF000() {
        Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        int firstByte = memory.readByte(currentProgramCounter - 2);
        int secondByte = memory.readByte(currentProgramCounter - 1);
        int opcode = (firstByte << 8) | secondByte;
        return opcode == 0xF000;
    }

}
