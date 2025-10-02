package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

public class XOChipProcessor<E extends XOChipEmulator<D, S>, D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipProcessor<E, D, S> {

    public XOChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = Chip8Processor.HANDLED;
        if (secondNibble == 0x0 && thirdNibble == 0xD) {// OODN: Scroll screen N rows up
            this.emulator.getDisplay().scrollUp(fourthNibble);
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
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                        j++;
                    }
                }
            }
            case 0x3 -> { // 5XY3: Read values into vX to vY from memory
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                        j++;
                    }
                }
            }
            default -> flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble, int fourthNibble) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfRegistersNotEqual(secondNibble, thirdNibble, fourthNibble));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        XOChipDisplay display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();
        int selectedBitPlanes = display.getSelectedBitPlanes();

        int spriteHeight = fourthNibble;
        if (spriteHeight < 1) {
            spriteHeight = 16;
        }

        int logicalDisplayWidth = display.getWidth();
        int logicalDisplayHeight = display.getHeight();
        if (!extendedMode) {
            logicalDisplayWidth /= 2;
            logicalDisplayHeight /= 2;
        }

        int spriteX = this.getRegister(secondNibble) % logicalDisplayWidth;
        int spriteY = this.getRegister(thirdNibble) % logicalDisplayHeight;

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
        for (int bitPlaneMask = 1; bitPlaneMask <= XOChipDisplay.BITPLANE_BASE_MASK; bitPlaneMask <<= 1) {
            if ((bitPlaneMask & selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= logicalDisplayHeight) {
                    if (config.doClipping()) {
                        planeIterator++;
                        continue;
                    } else {
                        sliceY %= logicalDisplayHeight;
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
                for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                    int sliceX = spriteX + j;
                    if (sliceX >= logicalDisplayWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= logicalDisplayWidth;
                        }
                    }
                    if ((slice & sliceMask) <= 0) {
                        continue;
                    }
                    if (extendedMode) {
                        collided |= display.togglePixelAtBitPlanes(bitPlaneMask, sliceX, sliceY);
                    } else {
                        int scaledSliceX = sliceX * 2;
                        int scaledSliceY = sliceY * 2;
                        collided |= display.togglePixelAtBitPlanes(bitPlaneMask, scaledSliceX, scaledSliceY);
                        collided |= display.togglePixelAtBitPlanes(bitPlaneMask, scaledSliceX + 1, scaledSliceY);
                        display.togglePixelAtBitPlanes(bitPlaneMask, scaledSliceX, scaledSliceY + 1);
                        display.togglePixelAtBitPlanes(bitPlaneMask, scaledSliceX + 1, scaledSliceY + 1);
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
                this.setIndexRegister((firstAddressByte << 8) | secondAddressByte);
                this.incrementProgramCounter();
            }
            case 0x01 -> { // FX01: Set selected bit planes
                this.emulator.getDisplay().setSelectedBitPlanes(secondNibble);
            }
            case 0x02 -> { // F002: Load audio pattern
                if (secondNibble != 0x0) {
                    return 0;
                }
                Memory memory = this.emulator.getMemory();
                Chip8SoundSystem soundSystem = this.emulator.getSoundSystem();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    soundSystem.loadPatternByte(i, memory.readByte(currentIndexRegister + i));
                }
            }
            case 0x3A -> { // FX3A: Set audio pattern pitch
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
        return ((memory.readByte(currentProgramCounter - 2) << 8) | memory.readByte(currentProgramCounter - 1)) == 0xF000;
    }

}
