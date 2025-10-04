package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

public class MegaChipProcessor<E extends MegaChipEmulator<D, S>, D extends MegaChipDisplay, S extends SoundSystem> extends SChipProcessor<E, D, S> {

    private int cachedCharacterFontIndex;

    public MegaChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        MegaChipDisplay display = this.emulator.getDisplay();
        int flags = 0;
        if (secondNibble == 0x0 && thirdNibble == 0x1) {
            switch (fourthNibble) {
                case 0x0 -> { // 0010: Disable megachip mode
                    display.setMegaChipMode(false);
                    // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
                    flags = set(flags, HANDLED);
                }
                case 0x1 -> { // 0011: Enable megachip mode
                    display.setMegaChipMode(true);
                    // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
                    flags = set(flags, HANDLED);
                }
            }
        }
        if (isSet(flags, HANDLED)) {
            return flags;
        }
        if (!display.isMegaChipModeEnabled()) {
            return super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        }
        flags = HANDLED;
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
                                flags = set(flags, CLS_EXECUTED);
                            }
                            case 0xE -> this.setProgramCounter(this.pop()); // 00EE: Return from subroutine
                            default -> flags = clear(flags, HANDLED);
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
                            default -> flags = clear(flags, HANDLED);
                        }
                    }
                    default -> flags = clear(flags, HANDLED);
                }
            }
            case 0x1 -> { // 01NN NNNN: Set index register to 24-bit address
                Chip8Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                this.setIndexRegister(
                        (secondByte << 16) |
                        (memory.readByte(currentProgramCounter) << 8) |
                        memory.readByte(currentProgramCounter + 1)
                );
                this.incrementProgramCounter();
            }
            case 0x2 -> { // 02NN: Load NN colors from I into the palette, colors are in ARGB
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < secondByte; i++) {
                    display.loadPaletteEntry(i + 1,
                            (memory.readByte(currentIndexRegister + (i * 4)) << 24) |
                                    (memory.readByte(currentIndexRegister + (i * 4) + 1) << 16) |
                                    (memory.readByte(currentIndexRegister + (i * 4) + 2) << 8) |
                                    memory.readByte(currentIndexRegister + (i * 4) + 3)
                    );
                }
            }
            case 0x3 -> display.setSpriteWidth(secondByte); // 03NN: Set sprite width to NN
            case 0x4 -> display.setSpriteHeight(secondByte); // 04NN: Set sprite height to NN
            case 0x5 -> display.setScreenAlpha(secondByte); // 05NN: Set screen alpha to NN
            case 0x6 -> { // 06NU: Play digitized sound at I. On the Mega8, play audio once if N = 1, and loop if N = 0. On MEGA-CHIP8, ignore N.
                if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentIndexRegister = this.getIndexRegister();
                    int size = ((memory.readByte(currentIndexRegister + 2) & 0xFF) << 16) | ((memory.readByte(currentIndexRegister + 3) & 0xFF) << 8) | (memory.readByte(currentIndexRegister + 4) & 0xFF);
                    if (size == 0) {
                        return 0;
                    }
                    megaChipSoundSystem.playTrack(((memory.readByte(currentIndexRegister) & 0xFF) << 8) | memory.readByte(currentIndexRegister + 1) & 0xFF, size, fourthNibble == 0, currentIndexRegister + 6);
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
            case 0x9 -> display.setCollisionIndex(secondByte); // 09NN: Set collision color to color palette entry NN
            default -> flags = clear(flags, HANDLED);
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

        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(secondNibble) % displayWidth;
        int spriteY = this.getRegister(thirdNibble) % displayHeight;

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
                if (sliceY >= displayHeight) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceY %= displayHeight;
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
                    display.drawFontPixel(sliceX, sliceY);
                }
            }
        } else {
            int currentCollisionIndex = display.getCollisionIndex();
            int spriteWidth = display.getSpriteWidth();
            int spriteHeight = display.getSpriteHeight();

            for (int i = 0; i < spriteHeight; i++) {
                int pixelY = spriteY + i;
                if (pixelY >= displayHeight) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        pixelY %= displayHeight;
                    }
                }
                for (int j = 0; j < spriteWidth; j++) {
                    int pixelX = spriteX + j;
                    if (pixelX >= displayWidth) {
                        if (config.doClipping()) {
                            break;
                        } else {
                            pixelX %= displayWidth;
                        }
                    }
                    int colorIndex = memory.readByte(currentIndexRegister + (i * spriteWidth) + j) & 0xFF;
                    if (colorIndex == 0) {
                        continue;
                    }
                    if (display.getColorIndexAt(pixelX, pixelY) == currentCollisionIndex && display.getColorForIndex(colorIndex) != 0) {
                        this.setVF(true);
                    }
                    display.setPixel(pixelX, pixelY, colorIndex);
                }
            }
        }
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfKey(secondNibble, secondByte));
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if (this.emulator.getDisplay().isMegaChipModeEnabled()) {
            if (isSet(flagsSuper, GET_KEY_EXECUTED)) {
                this.emulator.getDisplay().flushBackBuffer();
            }
            if (isSet(flagsSuper, FONT_SPRITE_POINTER)) {
                this.cachedCharacterFontIndex = this.getIndexRegister();
            }
        }
        return flagsSuper;
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if (isSet(flags, HANDLED) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWas01()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWas01() {
        return this.emulator.getMemory().readByte(this.getProgramCounter() - 2) == 0x01;
    }

}
