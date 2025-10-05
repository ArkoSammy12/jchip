package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

public class XOChipProcessor<E extends XOChipEmulator<D, S>, D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipProcessor<E, D, S> {

    public XOChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        if (secondNibble == 0x0 && thirdNibble == 0xD) {// OODN: Scroll screen N rows up
            this.emulator.getDisplay().scrollUp(fourthNibble);
        } else {
            flags = clear(flags, HANDLED);
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
        if (isSet(flagsSuper, HANDLED)) {
            if (isSet(flagsSuper, SKIP_TAKEN) && this.previousOpcodeWasFX00()) {
                this.incrementProgramCounter();
            }
            return flagsSuper;
        }
        int flags = HANDLED;
        Chip8Memory memory = this.emulator.getMemory();
        int currentIndexRegister = this.getIndexRegister();
        boolean iterateInReverse = secondNibble > thirdNibble;
        switch (fourthNibble) {
            case 0x2 -> { // 5XY2: Write vX to vY to memory
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--, j++) {
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++, j++) {
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                }
            }
            case 0x3 -> { // 5XY3: Read values into vX to vY from memory
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--, j++) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++, j++) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                    }
                }
            }
            default -> flags = clear(flags, HANDLED);
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
        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();
        int selectedBitPlanes = display.getSelectedBitPlanes();

        int spriteHeight = fourthNibble;
        if (spriteHeight < 1) {
            spriteHeight = 16;
        }

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(secondNibble) % displayWidth;
        int spriteY = this.getRegister(thirdNibble) % displayHeight;

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
                if (sliceY >= displayHeight) {
                    if (config.doClipping()) {
                        planeIterator++;
                        continue;
                    } else {
                        sliceY %= displayHeight;
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
                    if (sliceX >= displayWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= displayWidth;
                        }
                    }
                    if ((slice & sliceMask) <= 0) {
                        continue;
                    }
                    if (extendedMode) {
                        collided |= display.togglePixelAtBitPlanes(sliceX, sliceY, bitPlaneMask);
                    } else {
                        int scaledSliceX = sliceX * 2;
                        int scaledSliceY = sliceY * 2;
                        collided |= display.togglePixelAtBitPlanes(scaledSliceX, scaledSliceY, bitPlaneMask);
                        collided |= display.togglePixelAtBitPlanes(scaledSliceX + 1, scaledSliceY, bitPlaneMask);
                        display.togglePixelAtBitPlanes(scaledSliceX, scaledSliceY + 1, bitPlaneMask);
                        display.togglePixelAtBitPlanes(scaledSliceX + 1, scaledSliceY + 1, bitPlaneMask);
                    }
                }
                planeIterator++;
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfKey(secondNibble, secondByte));
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        switch (secondByte) {
            case 0x00 -> { // F000 NNNN: Set index register to 16-bit address
                if (secondNibble != 0) {
                    return 0;
                }
                Chip8Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                this.setIndexRegister((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                this.incrementProgramCounter();
            }
            case 0x01 -> this.emulator.getDisplay().setSelectedBitPlanes(secondNibble); // FX01: Set selected bit planes
            case 0x02 -> { // F002: Load audio pattern
                if (secondNibble != 0x0) {
                    return 0;
                }
                Chip8Memory memory = this.emulator.getMemory();
                Chip8SoundSystem soundSystem = this.emulator.getSoundSystem();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    soundSystem.loadPatternByte(i, memory.readByte(currentIndexRegister + i));
                }
            }
            case 0x3A -> this.emulator.getSoundSystem().setPlaybackRate(this.getRegister(secondNibble)); // FX3A: Set audio pattern pitch
            default -> flags = clear(flags, HANDLED);
        }
        return flags;
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if (isSet(flags, HANDLED) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWasFX00()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected boolean previousOpcodeWasFX00() {
        Chip8Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        return (memory.readByte(currentProgramCounter - 2) == 0xF0) && (memory.readByte(currentProgramCounter - 1) == 0x00);
    }

}
