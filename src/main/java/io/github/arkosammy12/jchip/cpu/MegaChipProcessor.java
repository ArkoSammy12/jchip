package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

public class MegaChipProcessor<E extends MegaChipEmulator<D, S>, D extends MegaChipDisplay, S extends MegaChipSoundSystem> extends SChipProcessor<E, D, S> {

    private int cachedCharacterFontIndex;

    public MegaChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) throws InvalidInstructionException {
        MegaChipDisplay display = this.emulator.getDisplay();
        int flags = 0;
        if (secondNibble == 0 && thirdNibble == 1) {
            switch (fourthNibble) {
                case 0x0 -> { // 0010: Disable megachip mode
                    display.setMegaChipMode(false);
                    // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
                    flags |= HANDLED;
                }
                case 0x1 -> { // 0011: Enable megachip mode
                    display.setMegaChipMode(true);
                    // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
                    flags |= HANDLED;
                }
            }
        }

        if ((flags & HANDLED) != 0) {
            return flags;
        }
        if (!display.isMegaChipModeEnabled()) {
            return super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
        }

        flags = Chip8Processor.HANDLED;
        switch (secondNibble) {
            case 0x0 -> {
                switch (thirdNibble) {
                    case 0xB -> { // 00BN: Scroll screen N rows up
                        display.scrollUp(fourthNibble);
                        display.setUpdateScrollTriggered();
                    }
                    case 0xC -> { // 00CN: Scroll screen N rows down
                        if (fourthNibble <= 0) {
                            return 0;
                        }
                        display.scrollDown(fourthNibble);
                        display.setUpdateScrollTriggered();
                    }
                    case 0xE -> {
                        switch (fourthNibble) {
                            case 0x0 -> { // 00E0: Clear the screen
                                display.flushBackBuffer();
                                display.clear();
                                flags |= Chip8Processor.CLS_EXECUTED;
                            }
                            case 0xE -> { // 00EE: Return from subroutine
                                this.setProgramCounter(this.pop());
                            }
                            default -> flags &= ~Chip8Processor.HANDLED;
                        }
                    }
                    case 0xF -> {
                        switch (fourthNibble) {
                            case 0xB -> { // 00FB: Scroll screen 4 columns right
                                display.scrollRight();
                                display.setUpdateScrollTriggered();
                            }
                            case 0xC -> { // 00FC: Scroll screen 4 columns left
                                display.scrollLeft();
                                display.setUpdateScrollTriggered();
                            }
                            case 0xD -> { // 00FD: Exit interpreter
                                this.shouldTerminate = true;
                            }
                            case 0xE -> { // 00FE: Switch to lores mode
                                // Doesn't work in MegaChip mode as per the NinjaWeedle
                            }
                            case 0xF -> { // 00FF: Switch to hires mode
                                // Doesn't work in MegaChip mode as per the NinjaWeedle
                            }
                            default -> flags &= ~Chip8Processor.HANDLED;
                        }
                    }
                    default -> flags &= ~Chip8Processor.HANDLED;
                }
            }
            case 0x1 -> { // 01NN NNNN: Set index register to 24-bit address
                Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                int secondAddressByte = memory.readByte(currentProgramCounter);
                int thirdAddressByte = memory.readByte(currentProgramCounter + 1);
                int address = (secondByte << 16) | (secondAddressByte << 8) | thirdAddressByte;
                this.setIndexRegister(address);
                this.incrementProgramCounter();
            }
            case 0x2 -> { // 02NN: Load NN colors from I into the palette, colors are in ARGB
                Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < secondByte; i++) {
                    int a = memory.readByte(currentIndexRegister + (i * 4));
                    int r = memory.readByte(currentIndexRegister + (i * 4) + 1);
                    int g = memory.readByte(currentIndexRegister + (i * 4) + 2);
                    int b = memory.readByte(currentIndexRegister + (i * 4) + 3);
                    display.loadPaletteEntry(i + 1, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            case 0x3 -> { // 03NN: Set sprite width to NN
                display.setSpriteWidth(secondByte);
            }
            case 0x4 -> { // 04NN: Set sprite height to NN
                display.setSpriteHeight(secondByte);
            }
            case 0x5 -> { // 05NN: Set screen alpha to NN
                display.setScreenAlpha(secondByte);
            }
            case 0x6 -> { // 06NU: Play digitized sound at I. On the Mega8, play audio once if N = 1, and loop if N = 0. On MEGA-CHIP8, ignore N.
                if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                    Memory memory = this.emulator.getMemory();
                    int currentIndexRegister = this.getIndexRegister();
                    int sampleRate = ((memory.readByte(currentIndexRegister) & 0xFF) << 8) | memory.readByte(currentIndexRegister + 1) & 0xFF;
                    int size = ((memory.readByte(currentIndexRegister + 2) & 0xFF) << 16) | ((memory.readByte(currentIndexRegister + 3) & 0xFF) << 8) | (memory.readByte(currentIndexRegister + 4) & 0xFF);
                    if (size == 0) {
                        return 0;
                    }
                    megaChipSoundSystem.playTrack(sampleRate, size, fourthNibble == 0, currentIndexRegister + 6);
                }
            }
            case 0x7 -> { // 0700: Stop digitized sound
                if (secondByte != 0x00) {
                    return 0;
                }
                if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                    megaChipSoundSystem.stopTrack();
                }
            }
            case 0x8 -> { // 080N: Set sprite blend mode (0 = normal, 1 = 25%, 2 = 50%, 3 = 75%, 4 = additive, 5 = multiply)
                if (thirdNibble != 0) {
                    return 0;
                }
                MegaChipDisplay.BlendMode blendMode = switch (fourthNibble) {
                    case 0 -> MegaChipDisplay.BlendMode.BLEND_NORMAL;
                    case 1 -> MegaChipDisplay.BlendMode.BLEND_25;
                    case 2 -> MegaChipDisplay.BlendMode.BLEND_50;
                    case 3 -> MegaChipDisplay.BlendMode.BLEND_75;
                    case 4 -> MegaChipDisplay.BlendMode.BLEND_ADD;
                    case 5 -> MegaChipDisplay.BlendMode.BLEND_MULTIPLY;
                    default -> null;
                };
                if (blendMode == null) {
                    return 0;
                }
                display.setBlendMode(blendMode);
            }
            case 0x9 -> { // 09NN: Set collision color to color palette entry NN
                display.setCollisionIndex(secondByte);
            }
            default -> flags &= ~Chip8Processor.HANDLED;
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
        return handleDoubleSkipIfNecessary(super.executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte));
    }

    @Override
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble, int fourthNibble) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfRegistersNotEqual(secondNibble, thirdNibble, fourthNibble));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        MegaChipDisplay display = this.emulator.getDisplay();

        if (!display.isMegaChipModeEnabled()) {
            return super.executeDraw(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
        }

        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();

        int spriteX = this.getRegister(secondNibble) % 256;
        int spriteY = this.getRegister(thirdNibble) % 256;

        this.setVF(false);

        if (currentIndexRegister == cachedCharacterFontIndex) {
            // Normal DXYN behavior for font sprites
            int spriteHeight = fourthNibble;
            if (spriteHeight < 1) {
                spriteHeight = 16;
            }
            boolean draw16WideSprite = spriteHeight >= 16;
            int sliceLength = 8;
            int baseMask = Chip8Processor.BASE_SLICE_MASK_8;
            if (draw16WideSprite) {
                sliceLength = 16;
                baseMask = BASE_SLICE_MASK_16;
            }

            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= 256) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceY %= 256;
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
                for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                    int sliceX = spriteX + j;
                    if (sliceX >= 256) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            sliceX %= 256;
                        }
                    }
                    if ((slice & sliceMask) <= 0) {
                        continue;
                    }
                    display.drawFontPixel(sliceX, sliceY);
                }
            }
        } else {
            int currentCollisionIndex = display.getCollisionIndex();
            int spriteWidth = display.getSpriteWidth();
            int spriteHeight = display.getSpriteHeight();

            for (int i = 0; i < spriteHeight; i++) {
                int pixelY = spriteY + i;
                if (pixelY >= 256) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        pixelY %= 256;
                    }
                }
                for (int j = 0; j < spriteWidth; j++) {
                    int pixelX = spriteX + j;
                    if (pixelX >= 256) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            pixelX %= 256;
                        }
                    }
                    int pixel = memory.readByte(currentIndexRegister + (i * spriteWidth) + j) & 0xFF;
                    if (pixel == 0) {
                        continue;
                    }
                    int collisionIndexAtPos = display.getColorIndexAt(pixelX, pixelY);
                    if (collisionIndexAtPos == currentCollisionIndex && display.getColorForIndex(pixel) != 0) {
                        this.setVF(true);
                    }
                    display.setPixel(pixelX, pixelY, pixel);
                }
            }
        }
        return Chip8Processor.HANDLED | Chip8Processor.DRAW_EXECUTED;
    }

    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfKey(secondNibble, secondByte));
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if (!this.emulator.getDisplay().isMegaChipModeEnabled()) {
            return flagsSuper;
        }
        if ((flagsSuper & Chip8Processor.GET_KEY_EXECUTED) != 0) {
            MegaChipDisplay display = this.emulator.getDisplay();
            display.flushBackBuffer();
        } else if ((flagsSuper & Chip8Processor.FONT_SPRITE_POINTER) != 0) {
            this.cachedCharacterFontIndex = this.getIndexRegister();
        }
        return flagsSuper;
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if ((flags & Chip8Processor.HANDLED) != 0 && (flags & Chip8Processor.SKIP_TAKEN) != 0 && this.previousOpcodeWas01()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWas01() {
        Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        int opcode = memory.readByte(currentProgramCounter - 2);
        return opcode == 0x01;
    }

}
