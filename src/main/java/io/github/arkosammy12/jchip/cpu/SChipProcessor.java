package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.emulators.SChipEmulator;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.SChipDisplay;

public class SChipProcessor<E extends SChipEmulator> extends Chip8Processor<E> {

    public static final int BASE_SLICE_MASK_16 = 1 << 15;

    public SChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xFB -> { // 00FB: scroll-right
                    this.emulator.getDisplay().scrollRight();
                    yield HANDLED;
                }
                case 0xFC -> { // 00FC: scroll-left
                    this.emulator.getDisplay().scrollLeft();
                    yield HANDLED;
                }
                case 0xFD -> { // 00FD: exit
                    this.shouldTerminate = true;
                    yield HANDLED;
                }
                case 0xFE -> { // 00FE: lores
                    SChipDisplay<?> display = this.emulator.getDisplay();
                    display.setExtendedMode(false);
                    if (this.emulator.isModern()) {
                        display.clear();
                    }
                    yield HANDLED;
                }
                case 0xFF -> { // 00FF: hires
                    SChipDisplay<?> display = this.emulator.getDisplay();
                    display.setExtendedMode(true);
                    if (this.emulator.isModern()) {
                        display.clear();
                    }
                    yield HANDLED;
                }
                default -> {
                    if (getY(firstByte, NN) == 0xC) { // 00CN: scroll-down N
                        int N = getN(firstByte, NN);
                        if (N == 0x0 && !this.emulator.isModern()) {
                            // 00C0 is invalid on legacy SUPER-CHIP
                            yield super.execute0Opcode(firstByte, NN);
                        }
                        this.emulator.getDisplay().scrollDown(N);
                        yield HANDLED;
                    } else {
                        yield super.execute0Opcode(firstByte, NN);
                    }
                }
            };
        } else {
            return super.execute0Opcode(firstByte, NN);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        SChipDisplay<?> display = this.emulator.getDisplay();
        Chip8Memory memory = this.emulator.getMemory();
        EmulatorSettings config = this.emulator.getEmulatorSettings();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();
        boolean doClipping = config.doClipping();

        int N = getN(firstByte, NN);
        int spriteHeight = N < 1 ? 16 : N;
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getX(firstByte, NN)) % displayWidth;
        int spriteY = this.getRegister(getY(firstByte, NN)) % displayHeight;

        boolean isModern = this.emulator.isModern();
        boolean draw16WideSprite = (isModern || extendedMode) && spriteHeight >= 16;
        boolean addBottomClippedRows = !isModern && extendedMode;

        int sliceLength;
        int baseMask;
        if (draw16WideSprite) {
            sliceLength = 16;
            baseMask = BASE_SLICE_MASK_16;
        } else {
            sliceLength = 8;
            baseMask = BASE_SLICE_MASK_8;
        }

        int collisionCounter = 0;
        this.setVF(false);

        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= displayHeight) {
                if (doClipping) {
                    if (addBottomClippedRows) {
                        collisionCounter++;
                        continue;
                    }
                    break;
                } else {
                    sliceY %= displayHeight;
                }
            }
            int slice = draw16WideSprite
                    ? (memory.readByte(currentIndexRegister + i * 2) << 8) | memory.readByte(currentIndexRegister + (i * 2) + 1)
                    : memory.readByte(currentIndexRegister + i);
            boolean rowCollided = false;
            for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= displayWidth) {
                    if (doClipping) {
                        break;
                    } else {
                        sliceX %= displayWidth;
                    }
                }
                if ((slice & sliceMask) == 0) {
                    continue;
                }
                if (extendedMode) {
                    rowCollided |= display.flipPixel(sliceX, sliceY);
                } else {
                    int scaledSliceX = sliceX * 2;
                    int scaledSliceY = sliceY * 2;
                    rowCollided |= display.flipPixel(scaledSliceX, scaledSliceY);
                    rowCollided |= display.flipPixel(scaledSliceX + 1, scaledSliceY);
                    display.flipPixel(scaledSliceX, scaledSliceY + 1);
                    display.flipPixel(scaledSliceX + 1, scaledSliceY + 1);
                }
            }
            if (!isModern) {
                if (!extendedMode) {
                    int x1 = (spriteX * 2) & 0x70;
                    int x2 = Math.min(x1 + 32, displayWidth * 2);
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
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x30 -> { // FX30: i := bighex vX
                this.setIndexRegister(this.emulator.getChip8Variant().getSpriteFont().getBigFontSpriteOffset(this.getRegister(getX(firstByte, NN)) & 0xF));
                yield HANDLED | FONT_SPRITE_POINTER;
            }
            case 0x75 -> { // FX75: saveflags vX
                JChip jchip = this.emulator.getEmulatorSettings().getJChip();
                int X = getX(firstByte, NN);
                for (int i = 0; i <= X; i++) {
                    jchip.setFlagRegister(i, this.getRegister(i));
                }
                yield HANDLED;
            }
            case 0x85 -> { // FX85: loadflags vX
                JChip jchip = this.emulator.getEmulatorSettings().getJChip();
                int X = getX(firstByte, NN);
                for (int i = 0; i <= X; i++) {
                    this.setRegister(i, jchip.getFlagRegister(i));
                }
                yield HANDLED;
            }
            default -> super.executeFOpcode(firstByte, NN);
        };
    }

}
