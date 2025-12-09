package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.memory.Chip8Bus;
import io.github.arkosammy12.jchip.emulators.SChip11Emulator;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.SChip11Display;

public class SChip11Processor<E extends SChip11Emulator> extends SChip10Processor<E> {

    public SChip11Processor(E emulator) {
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
                default -> {
                    if (getY(firstByte, NN) == 0xC) { // 00CN: scroll-down N
                        int N = getN(firstByte, NN);
                        if (N == 0x0) {
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

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        SChip11Display<?> display = this.emulator.getDisplay();
        Chip8Bus bus = this.emulator.getBus();
        boolean hiresMode = display.isHiresMode();
        int currentIndexRegister = this.getIndexRegister();
        boolean doClipping = this.emulator.getEmulatorSettings().doClipping();

        int N = getN(firstByte, NN);
        int spriteHeight = N < 1 ? 16 : N;
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getX(firstByte, NN)) % displayWidth;
        int spriteY = this.getRegister(getY(firstByte, NN)) % displayHeight;

        boolean draw16WideSprite = hiresMode&& spriteHeight >= 16;

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
                    if (hiresMode) {
                        collisionCounter++;
                    }
                    continue;
                } else {
                    sliceY %= displayHeight;
                }
            }
            int slice = draw16WideSprite
                    ? (bus.readByte(currentIndexRegister + i * 2) << 8) | bus.readByte(currentIndexRegister + (i * 2) + 1)
                    : bus.readByte(currentIndexRegister + i);
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
                if (hiresMode) {
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
            if (!hiresMode) {
                int x1 = (spriteX * 2) & 0x70;
                int x2 = Math.min(x1 + 32, displayWidth * 2);
                int scaledSliceY = sliceY * 2;
                for (int j = x1; j < x2; j++) {
                    display.setPixel(j, scaledSliceY + 1, display.getPixel(j, scaledSliceY));
                }
                if (rowCollided) {
                    collisionCounter = 1;
                }
            } else if (rowCollided) {
                collisionCounter++;
            }
        }
        this.setRegister(0xF, collisionCounter);
        return HANDLED | DRAW_EXECUTED;
    }

}
