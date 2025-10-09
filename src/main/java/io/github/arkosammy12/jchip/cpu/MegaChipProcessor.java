package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;
import io.github.arkosammy12.jchip.video.SChipDisplay;

public class MegaChipProcessor<E extends MegaChipEmulator<D, S>, D extends MegaChipDisplay, S extends SoundSystem> extends SChipProcessor<E, D, S> {

    private int cachedCharacterFontIndex;

    public MegaChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    public void reset() {
        super.reset();
        this.cachedCharacterFontIndex = 0;
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        MegaChipDisplay display = this.emulator.getDisplay();
        boolean handled = false;
        if (firstByte == 0x00) {
            // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
            handled = switch (NN) {
                case 0x10 -> { // 0010: Disable megachip mode
                    display.setMegaChipMode(false);
                    yield true;
                }
                case 0x11 -> { // 0011: Enable megachip mode
                    display.setMegaChipMode(true);
                    yield true;
                }
                default -> false;
            };
        }
        if (handled) {
            return HANDLED;
        }
        if (!display.isMegaChipModeEnabled()) {
            return super.execute0Opcode(firstByte, NN);
        }
        return switch (firstByte) {
            case 0x00 -> switch (NN) {
                case 0xE0 -> { // 00E0: Clear the screen
                    display.flushBackBuffer();
                    display.clear();
                    yield HANDLED | CLS_EXECUTED;
                }
                case 0xEE -> { // 00EE: Return from subroutine
                    this.setProgramCounter(this.pop());
                    yield HANDLED;
                }
                case 0xFB -> { // 00FB: Scroll screen right
                    display.scrollRight();
                    display.setDisplayUpdateScrollTriggered();
                    yield HANDLED;
                }
                case 0xFC -> { // 00FC: Scroll screen left
                    display.scrollLeft();
                    display.setDisplayUpdateScrollTriggered();
                    yield HANDLED;
                }
                case 0xFD -> { // 00FD: Exit interpreter
                    this.shouldTerminate = true;
                    yield HANDLED;
                }
                case 0xFE -> { // 00FE: Switch to lores mode
                    // Doesn't work in MegaChip mode
                    yield HANDLED;
                }
                case 0xFF -> { // 00FF: Switch to hires mode
                    // Doesn't work in MegaChip mode
                    yield HANDLED;
                }
                default -> switch (getYFromNN(NN)) {
                    case 0xB -> { // 00BN: Scroll screen up
                        display.scrollUp(getNFromNN(NN));
                        display.setDisplayUpdateScrollTriggered();
                        yield HANDLED;
                    }
                    case 0xC -> { // 00CN: Scroll screen down
                        int N = getNFromNN(NN);
                        if (N > 0) {
                            display.scrollDown(N);
                            display.setDisplayUpdateScrollTriggered();
                            yield HANDLED;
                        } else {
                            yield 0;
                        }
                    }
                    default -> 0;
                };
            };
            case 0x01 -> { // 01NN NNNN: Set index to immediate 24-bit address
                Chip8Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                this.setIndexRegister((NN << 16) | (memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                this.incrementProgramCounter();
                yield HANDLED;
            }
            case 0x02 -> { // 02NN: Load color palette entries
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < NN; i++) {
                    display.loadPaletteEntry(i + 1, (memory.readByte(currentIndexRegister + (i * 4)) << 24) | (memory.readByte(currentIndexRegister + (i * 4) + 1) << 16) | (memory.readByte(currentIndexRegister + (i * 4) + 2) << 8) | memory.readByte(currentIndexRegister + (i * 4) + 3));
                }
                yield HANDLED;
            }
            case 0x03 -> { // 03NN: Set sprite width
                display.setSpriteWidth(NN);
                yield HANDLED;
            }
            case 0x04 -> { // 04NN: Set sprite height
                display.setSpriteHeight(NN);
                yield HANDLED;
            }
            case 0x05 -> { // 05NN: Set screen alpha
                display.setScreenAlpha(NN);
                yield HANDLED;
            }
            case 0x06 -> {
                if (getYFromNN(NN) == 0x0) { // 060N: Play digitized sound
                    if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                        Chip8Memory memory = this.emulator.getMemory();
                        int currentIndexRegister = this.getIndexRegister();
                        int size = ((memory.readByte(currentIndexRegister + 2) & 0xFF) << 16) | ((memory.readByte(currentIndexRegister + 3) & 0xFF) << 8) | (memory.readByte(currentIndexRegister + 4) & 0xFF);
                        if (size == 0) {
                            yield 0;
                        }
                        megaChipSoundSystem.playTrack(((memory.readByte(currentIndexRegister) & 0xFF) << 8) | memory.readByte(currentIndexRegister + 1) & 0xFF, size, getNFromNN(NN) == 0, currentIndexRegister + 6);
                    }
                    yield HANDLED;
                } else {
                    yield 0;
                }
            }
            case 0x07 -> {
                if (NN == 0x00) { // 0700: Stop digitized sound
                    if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                        megaChipSoundSystem.stopTrack();
                    }
                    yield HANDLED;
                } else {
                    yield 0;
                }
            }
            case 0x08 -> switch (NN) { // 080N: Set draw blend mode
                case 0x00 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_NORMAL);
                    yield HANDLED;
                }
                case 0x01 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_25);
                    yield HANDLED;
                }
                case 0x02 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_50);
                    yield HANDLED;
                }
                case 0x03 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_75);
                    yield HANDLED;
                }
                case 0x04 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_ADD);
                    yield HANDLED;
                }
                case 0x05 -> {
                    display.setBlendMode(MegaChipDisplay.BlendMode.BLEND_MULTIPLY);
                    yield HANDLED;
                }
                default -> 0;
            };
            case 0x09 -> { // 09NN: Set collision color index
                display.setCollisionIndex(NN);
                yield HANDLED;
            }
            default -> 0;
        };
    }

    @Override
    protected int execute3Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute3Opcode(firstByte, NN));
    }

    @Override
    protected int execute4Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute4Opcode(firstByte, NN));
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        return handleDoubleSkipIfNecessary(super.execute5Opcode(firstByte, NN));
    }

    @Override
    protected int execute9Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute9Opcode(firstByte, NN));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        MegaChipDisplay display = this.emulator.getDisplay();
        if (!display.isMegaChipModeEnabled()) {
            return this.handleMegaOffDraw(firstByte, NN);
        }

        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();
        boolean doClipping = config.doClipping();

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getXFromFirstByte(firstByte)) % displayWidth;
        int spriteY = this.getRegister(getYFromNN(NN)) % displayHeight;

        this.setVF(false);

        if (currentIndexRegister == cachedCharacterFontIndex) {
            // Normal DXYN behavior for font sprites
            int N = getNFromNN(NN);
            int spriteHeight = N < 1 ? 16 : N;
            boolean draw16WideSprite = spriteHeight >= 16;
            int sliceLength;
            int baseMask;
            if (draw16WideSprite) {
                sliceLength = 16;
                baseMask = BASE_SLICE_MASK_16;
            } else {
                sliceLength = 8;
                baseMask = BASE_SLICE_MASK_8;
            }
            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= displayHeight) {
                    if (doClipping) {
                        break;
                    } else {
                        sliceY %= displayHeight;
                    }
                }
                int slice = draw16WideSprite
                        ? (memory.readByte(currentIndexRegister + i * 2) << 8) | memory.readByte(currentIndexRegister + (i * 2) + 1)
                        : memory.readByte(currentIndexRegister + i);
                for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                    int sliceX = spriteX + j;
                    if (sliceX >= displayWidth) {
                        if (doClipping) {
                            break;
                        } else {
                            sliceX %= displayWidth;
                        }
                    }
                    if ((slice & sliceMask) != 0) {
                        display.drawFontPixel(sliceX, sliceY);
                    }
                }
            }
        } else {
            int currentCollisionIndex = display.getCollisionIndex();
            int spriteWidth = display.getSpriteWidth();
            int spriteHeight = display.getSpriteHeight();
            boolean collided = false;
            for (int i = 0; i < spriteHeight; i++) {
                int pixelY = spriteY + i;
                if (pixelY >= displayHeight) {
                    if (doClipping) {
                        break;
                    } else {
                        pixelY %= displayHeight;
                    }
                }
                int base = i * spriteWidth;
                for (int j = 0; j < spriteWidth; j++) {
                    int pixelX = spriteX + j;
                    if (pixelX >= displayWidth) {
                        if (doClipping) {
                            break;
                        } else {
                            pixelX %= displayWidth;
                        }
                    }
                    int pixelColorIndex = memory.readByte(currentIndexRegister + base + j);
                    if (pixelColorIndex == 0) {
                        continue;
                    }
                    if (display.getColorIndexAt(pixelX, pixelY) == currentCollisionIndex && display.getColorForIndex(pixelColorIndex) != 0) {
                        collided = true;
                    }
                    display.setPixel(pixelX, pixelY, pixelColorIndex);
                }
            }
            this.setVF(collided);
        }
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeEOpcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.executeEOpcode(firstByte, NN));
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        int result = super.executeFOpcode(firstByte, NN);
        if (this.emulator.getDisplay().isMegaChipModeEnabled()) {
            if (isSet(result, GET_KEY_EXECUTED)) {
                this.emulator.getDisplay().flushBackBuffer();
            }
            if (isSet(result, FONT_SPRITE_POINTER)) {
                this.cachedCharacterFontIndex = this.getIndexRegister();
            }
        }
        return result;
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if (isHandled(flags) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWas01()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWas01() {
        return this.emulator.getMemory().readByte(this.getProgramCounter() - 2) == 0x01;
    }

    @SuppressWarnings("DuplicatedCode")
    private int handleMegaOffDraw(int firstByte, int NN) {
        SChipDisplay display = this.emulator.getDisplay();
        Chip8Memory memory = this.emulator.getMemory();

        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();

        int N = getNFromNN(NN);
        int spriteHeight = N < 1 ? 16 : N;
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getXFromFirstByte(firstByte)) % displayWidth;
        int spriteY = this.getRegister(getYFromNN(NN)) % displayHeight;

        boolean draw16WideSprite = extendedMode && spriteHeight >= 16;

        int sliceLength;
        int baseMask;
        if (draw16WideSprite) {
            sliceLength = 16;
            baseMask = BASE_SLICE_MASK_16;
        } else {
            sliceLength = 8;
            baseMask = BASE_SLICE_MASK_8;
        }


        boolean collided = false;
        this.setVF(false);

        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= displayHeight) {
                break;
            }
            int slice = draw16WideSprite
                    ? (memory.readByte(currentIndexRegister + i * 2) << 8) | memory.readByte(currentIndexRegister + (i * 2) + 1)
                    : memory.readByte(currentIndexRegister + i);
            for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= displayWidth) {
                    break;
                }
                if ((slice & sliceMask) != 0) {
                    if (extendedMode) {
                        collided |= display.togglePixel(sliceX, sliceY);
                    } else {
                        int scaledSliceX = sliceX * 2;
                        int scaledSliceY = sliceY * 2;
                        collided |= display.togglePixel(scaledSliceX, scaledSliceY);
                        collided |= display.togglePixel(scaledSliceX + 1, scaledSliceY);
                        display.togglePixel(scaledSliceX, scaledSliceY + 1);
                        display.togglePixel(scaledSliceX + 1, scaledSliceY + 1);
                    }
                }
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

}
