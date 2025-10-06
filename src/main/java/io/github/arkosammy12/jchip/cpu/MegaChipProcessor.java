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
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        MegaChipDisplay display = this.emulator.getDisplay();
        int X = getXFromFirstByte(firstByte);
        int Y = getYFromNN(NN);
        int N = getNFromNN(NN);
        boolean handled = false;
        if (X == 0x0 && Y == 0x1) {
            // Do not clear the frame buffer of the display we are switching into as per original Mega8 behavior
            handled = switch (N) {
                case 0x0 -> { // 0010: Disable megachip mode
                    display.setMegaChipMode(false);
                    yield true;
                }
                case 0x1 -> { // 0011: Enable megachip mode
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
        return switch (X) {
            case 0x0 -> switch (Y) {
                case 0xB -> { // 00BN: Scroll screen up
                    display.scrollUp(N);
                    display.setDisplayUpdateScrollTriggered();
                    yield HANDLED;
                }
                case 0xC -> { // 00CN: Scroll screen down
                    if (N > 0) {
                        display.scrollDown(N);
                        display.setDisplayUpdateScrollTriggered();
                        yield HANDLED;
                    } else {
                        yield 0;
                    }
                }
                case 0xE -> switch (N) {
                    case 0x0 -> { // 00E0: Clear the screen
                        display.flushBackBuffer();
                        display.clear();
                        yield HANDLED | CLS_EXECUTED;
                    }
                    case 0xE -> { // 00EE: Return from subroutine
                        this.setProgramCounter(this.pop());
                        yield HANDLED;
                    }
                    default -> 0;
                };
                case 0xF -> switch (N) {
                    case 0xB -> { // 00FB: Scroll screen right
                        display.scrollRight();
                        display.setDisplayUpdateScrollTriggered();
                        yield HANDLED;
                    }
                    case 0xC -> { // 00FC: Scroll screen left
                        display.scrollLeft();
                        display.setDisplayUpdateScrollTriggered();
                        yield HANDLED;
                    }
                    case 0xD -> { // 00FD: Exit interpreter
                        this.shouldTerminate = true;
                        yield HANDLED;
                    }
                    case 0xE -> { // 00FE: Switch to lores mode
                        // Doesn't work in MegaChip mode
                        yield HANDLED;
                    }
                    case 0xF -> { // 00FF: Switch to hires mode
                        // Doesn't work in MegaChip mode
                        yield HANDLED;
                    }
                    default -> 0;
                };
                default -> 0;
            };
            case 0x1 -> { // 01NN NNNN: Set index to immediate 24-bit address
                Chip8Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                this.setIndexRegister(
                        (NN << 16) |
                        (memory.readByte(currentProgramCounter) << 8) |
                        memory.readByte(currentProgramCounter + 1)
                );
                this.incrementProgramCounter();
                yield HANDLED;
            }
            case 0x2 -> { // 02NN: Load color palette entries
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < NN; i++) {
                    display.loadPaletteEntry(i + 1,
                            (memory.readByte(currentIndexRegister + (i * 4)) << 24) |
                                    (memory.readByte(currentIndexRegister + (i * 4) + 1) << 16) |
                                    (memory.readByte(currentIndexRegister + (i * 4) + 2) << 8) |
                                    memory.readByte(currentIndexRegister + (i * 4) + 3)
                    );
                }
                yield HANDLED;
            }
            case 0x3 -> { // 03NN: Set sprite width
                display.setSpriteWidth(NN);
                yield HANDLED;
            }
            case 0x4 -> { // 04NN: Set sprite height
                display.setSpriteHeight(NN);
                yield HANDLED;
            }
            case 0x5 -> { // 05NN: Set screen alpha
                display.setScreenAlpha(NN);
                yield HANDLED;
            }
            case 0x6 -> { // 06NU: Play digitized sound
                if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentIndexRegister = this.getIndexRegister();
                    int size = ((memory.readByte(currentIndexRegister + 2) & 0xFF) << 16) | ((memory.readByte(currentIndexRegister + 3) & 0xFF) << 8) | (memory.readByte(currentIndexRegister + 4) & 0xFF);
                    if (size == 0) {
                        yield 0;
                    }
                    megaChipSoundSystem.playTrack(((memory.readByte(currentIndexRegister) & 0xFF) << 8) | memory.readByte(currentIndexRegister + 1) & 0xFF, size, N == 0, currentIndexRegister + 6);
                }
                yield HANDLED;
            }
            case 0x7 -> { // 0700: Stop digitized sound
                if (NN == 0x00) {
                    if (this.emulator.getSoundSystem() instanceof MegaChipSoundSystem megaChipSoundSystem) {
                        megaChipSoundSystem.stopTrack();
                    }
                    yield HANDLED;
                } else {
                    yield 0;
                }
            }
            case 0x8 -> {
                if (Y == 0) { // 080N: Set sprite blend mode
                    MegaChipDisplay.BlendMode blendMode = switch (N) {
                        case 0 -> MegaChipDisplay.BlendMode.BLEND_NORMAL;
                        case 1 -> MegaChipDisplay.BlendMode.BLEND_25;
                        case 2 -> MegaChipDisplay.BlendMode.BLEND_50;
                        case 3 -> MegaChipDisplay.BlendMode.BLEND_75;
                        case 4 -> MegaChipDisplay.BlendMode.BLEND_ADD;
                        case 5 -> MegaChipDisplay.BlendMode.BLEND_MULTIPLY;
                        default -> null;
                    };
                    if (blendMode == null) {
                        yield 0;
                    }
                    display.setBlendMode(blendMode);
                    yield HANDLED;
                } else {
                    yield 0;
                }
            }
            case 0x9 -> { // 09NN: Set collision color index
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
            return super.executeDOpcode(firstByte, NN);
        }

        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();

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
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceY %= displayHeight;
                    }
                }
                int slice;
                if (draw16WideSprite) {
                    slice = (memory.readByte(currentIndexRegister + i * 2) << 8) | memory.readByte(currentIndexRegister + (i * 2) + 1);
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
    protected int executeEOpcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.executeEOpcode(firstByte, NN));
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        int flagsSuper = super.executeFOpcode(firstByte, NN);
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
        if (isHandled(flags) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWas01()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWas01() {
        return this.emulator.getMemory().readByte(this.getProgramCounter() - 2) == 0x01;
    }

}
