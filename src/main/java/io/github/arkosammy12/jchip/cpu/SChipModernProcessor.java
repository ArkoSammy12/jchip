package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.SChipModernEmulator;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Bus;
import io.github.arkosammy12.jchip.video.SChip1Display;
import io.github.arkosammy12.jchip.video.SChipModernDisplay;

public class SChipModernProcessor<E extends SChipModernEmulator> extends SChipProcessor<E> {

    public SChipModernProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xFE -> { // 00FE: lores
                    SChipModernDisplay<?> display = this.emulator.getDisplay();
                    display.setHiresMode(false);
                    display.clear();
                    yield HANDLED;
                }
                case 0xFF -> { // 00FF: hires
                    SChipModernDisplay<?> display = this.emulator.getDisplay();
                    display.setHiresMode(true);
                    display.clear();
                    yield HANDLED;
                }
                default -> {
                    if (getY(firstByte, NN) == 0xC) { // 00CN: scroll-down N
                        this.emulator.getDisplay().scrollDown(getN(firstByte, NN));
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
        SChip1Display<?> display = this.emulator.getDisplay();
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

        boolean collided = false;
        this.setVF(false);

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
                    ? (bus.readByte(currentIndexRegister + i * 2) << 8) | bus.readByte(currentIndexRegister + (i * 2) + 1)
                    : bus.readByte(currentIndexRegister + i);
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
                    collided |= display.flipPixel(sliceX, sliceY);
                } else {
                    int scaledSliceX = sliceX * 2;
                    int scaledSliceY = sliceY * 2;
                    collided |= display.flipPixel(scaledSliceX, scaledSliceY);
                    collided |= display.flipPixel(scaledSliceX + 1, scaledSliceY);
                    display.flipPixel(scaledSliceX, scaledSliceY + 1);
                    display.flipPixel(scaledSliceX + 1, scaledSliceY + 1);
                }
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

}
