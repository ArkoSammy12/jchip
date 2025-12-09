package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.SChip10Emulator;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Bus;
import io.github.arkosammy12.jchip.video.SChip10Display;

public class SChip10Processor<E extends SChip10Emulator> extends Chip8Processor<E> {

    public static final int BASE_SLICE_MASK_16 = 1 << 15;

    public SChip10Processor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xFD -> { // 00FD: exit
                    this.shouldTerminate = true;
                    yield HANDLED;
                }
                case 0xFE -> { // 00FE: lores
                    this.emulator.getDisplay().setHiresMode(false);
                    yield HANDLED;
                }
                case 0xFF -> { // 00FF: hires
                    this.emulator.getDisplay().setHiresMode(true);
                    yield HANDLED;
                }
                default -> super.execute0Opcode(firstByte, NN);
            };
        } else {
            return super.execute0Opcode(firstByte, NN);
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        SChip10Display<?> display = this.emulator.getDisplay();
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

        boolean draw16WideSprite = hiresMode && spriteHeight >= 16;

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
            if (!hiresMode) {
                int x1 = (spriteX * 2) & 0x70;
                int x2 = Math.min(x1 + 32, displayWidth * 2);
                int scaledSliceY = sliceY * 2;
                for (int j = x1; j < x2; j++) {
                    display.setPixel(j, scaledSliceY + 1, display.getPixel(j, scaledSliceY));
                }
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x30 -> { // FX30: i := bighex vX
                this.setIndexRegister(this.emulator.getEmulatorSettings().getHexSpriteFont().getBigFontSpriteOffset(this.getRegister(getX(firstByte, NN)) & 0xF));
                yield HANDLED | FONT_SPRITE_POINTER;
            }
            case 0x75 -> { // FX75: saveflags vX
                Jchip jchip = this.emulator.getEmulatorSettings().getJchip();
                int X = getX(firstByte, NN);
                for (int i = 0; i <= X; i++) {
                    jchip.setFlagRegister(i, this.getRegister(i));
                }
                yield HANDLED;
            }
            case 0x85 -> { // FX85: loadflags vX
                Jchip jchip = this.emulator.getEmulatorSettings().getJchip();
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
