package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.emulators.SChipEmulator;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.SChipDisplay;

public class SChipProcessor<E extends SChipEmulator<D, S>, D extends SChipDisplay, S extends SoundSystem> extends Chip8Processor<E, D, S> {

    public static final int BASE_SLICE_MASK_16 = 1 << 15;

    protected boolean isModern;

    public SChipProcessor(E emulator) {
        super(emulator);
        if (emulator instanceof SChipEmulator<?, ?> sChipEmulator && sChipEmulator.isModern()) {
            this.isModern = true;
        }
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        SChipDisplay display = this.emulator.getDisplay();
        if (secondNibble == 0x0) {
            switch (thirdNibble) {
                case 0xC -> { // 00CN: Scroll screen N rows down
                    if (fourthNibble <= 0 && !isModern) {
                        return 0;
                    }
                    display.scrollDown(fourthNibble);
                }
                case 0xF -> {
                    switch (fourthNibble) {
                        case 0xB -> { // 00FB: Scroll screen 4 columns right
                            display.scrollRight();
                        }
                        case 0xC -> { // 00FC: Scroll screen 4 columns left
                            display.scrollLeft();
                        }
                        case 0xD -> { // 00FD: Exit interpreter
                            this.shouldTerminate = true;
                        }
                        case 0xE -> { // 00FE: Set lores mode
                            display.setExtendedMode(false);
                            if (isModern) {
                                display.clear();
                            }
                        }
                        case 0xF -> { // 00FF: Set hires mode
                            display.setExtendedMode(true);
                            if (isModern) {
                                display.clear();
                            }
                        }
                        default -> flags &= ~Chip8Processor.HANDLED;
                    }
                }
                default -> flags &= ~Chip8Processor.HANDLED;
            }
        } else {
            flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    // BXNN
    @Override
    protected int executeJumpWithOffset(int secondNibble, int thirdNibble, int fourthNibble, int memoryAddress) {
        if (!this.emulator.getEmulatorConfig().doJumpWithVX()) {
            return super.executeJumpWithOffset(secondNibble, thirdNibble, fourthNibble, memoryAddress);
        }
        int offset = this.getRegister(secondNibble);
        int jumpAddress = memoryAddress + offset;
        this.setProgramCounter(jumpAddress);
        return Chip8Processor.HANDLED;
    }

    // DXYN
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        SChipDisplay display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();

        int spriteHeight = fourthNibble;
        if (spriteHeight < 1) {
            spriteHeight = 16;
        }

        int logicalScreenWidth = display.getWidth();
        int logicalScreenHeight = display.getHeight();
        if (!extendedMode) {
            logicalScreenWidth /= 2;
            logicalScreenHeight /= 2;
        }

        int spriteX = this.getRegister(secondNibble) % logicalScreenWidth;
        int spriteY = this.getRegister(thirdNibble) % logicalScreenHeight;

        boolean draw16WideSprite = (isModern || extendedMode) && spriteHeight >= 16;
        boolean addBottomClippedRows = !isModern && extendedMode;

        int sliceLength = 8;
        int baseMask = Chip8Processor.BASE_SLICE_MASK_8;
        if (draw16WideSprite) {
            sliceLength = 16;
            baseMask = BASE_SLICE_MASK_16;
        }

        int collisionCounter = 0;
        this.setVF(false);

        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= logicalScreenHeight) {
                if (config.doClipping()) {
                    if (addBottomClippedRows) {
                        collisionCounter++;
                        continue;
                    }
                    break;
                } else {
                    sliceY %= logicalScreenHeight;
                }
            }
            int slice;
            if (draw16WideSprite) {
                int firstSliceByte = memory.readByte(currentIndexRegister + i * 2);
                int secondSliceByte = memory.readByte(currentIndexRegister + (i * 2) + 1);
                slice = (firstSliceByte << 8) | secondSliceByte;
            } else {
                slice = memory.readByte(currentIndexRegister + i);
            }
            boolean rowCollided = false;
            for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= logicalScreenWidth) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceX %= logicalScreenWidth;
                    }
                }
                if ((slice & sliceMask) <= 0) {
                    continue;
                }
                if (extendedMode) {
                    rowCollided |= display.togglePixel(sliceX, sliceY);
                } else {
                    int scaledSliceX = sliceX * 2;
                    int scaledSliceY = sliceY * 2;
                    rowCollided |= display.togglePixel(scaledSliceX, scaledSliceY);
                    rowCollided |= display.togglePixel(scaledSliceX + 1, scaledSliceY);
                    display.togglePixel(scaledSliceX, scaledSliceY + 1);
                    display.togglePixel(scaledSliceX + 1, scaledSliceY + 1);
                }
            }
            if (!isModern) {
                if (!extendedMode) {
                    int x1 = (spriteX * 2) & 0x70;
                    int x2 = Math.min(x1 + 32, logicalScreenWidth * 2);
                    int scaledSliceY = sliceY * 2;
                    for (int j = x1; j < x2; j++) {
                        int pixel = display.getPixel(j, scaledSliceY);
                        display.setPixel(j, scaledSliceY + 1, pixel);
                    }
                    if (rowCollided) {
                        collisionCounter = 1;
                    }
                } else if (rowCollided) {
                    collisionCounter++;
                }
            } else if (rowCollided) {
                collisionCounter = 1;
            }
        }
        this.setRegister(0xF, collisionCounter);
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        switch (secondByte) {
            case 0x30 -> { // FX30: Set index register to big font sprite offset
                int character = vX & 0xF;
                int spriteOffset = this.emulator.getDisplay().getCharacterSpriteFont().getBigFontSpriteOffset(character);
                this.setIndexRegister(spriteOffset);
                flags |= Chip8Processor.FONT_SPRITE_POINTER;
            }
            case 0x75 -> { // FX75: Store registers to flags storage
                this.saveRegistersToFlags(secondNibble);
            }
            case 0x85 -> { // FX85: Load registers from flags storage
                this.loadFlagsToRegisters(secondNibble);
            }
            default -> flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }


}
