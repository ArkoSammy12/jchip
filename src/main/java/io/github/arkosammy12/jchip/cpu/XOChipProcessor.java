package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.memory.Chip8Bus;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.sound.XOChipSoundSystem;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

public class XOChipProcessor<E extends XOChipEmulator> extends SChipProcessor<E> {

    public XOChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00 && getY(firstByte, NN) == 0xD) { // OODN: scroll-up N
            this.emulator.getDisplay().scrollUp(getN(firstByte, NN));
            return HANDLED;
        } else {
            return super.execute0Opcode(firstByte, NN);
        }
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
        int result = super.execute5Opcode(firstByte, NN);
        if (isHandled(result)) {
            if (isSet(result, SKIP_TAKEN) && this.previousOpcodeWasFX00()) {
                this.incrementProgramCounter();
            }
            return result;
        }
        return switch (getN(firstByte, NN)) {
            case 0x2 -> { // 5XY2: save vX - vY
                Chip8Bus bus = this.emulator.getBus();
                int currentIndexRegister = this.getIndexRegister();
                int X = getX(firstByte, NN);
                int Y = getY(firstByte, NN);
                boolean iterateInReverse = X > Y;
                if (iterateInReverse) {
                    for (int i = X, j = 0; i >= Y; i--, j++) {
                        bus.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                } else {
                    for (int i = X, j = 0; i <= Y; i++, j++) {
                        bus.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                }
                yield HANDLED;
            }
            case 0x3 -> { // 5XY3: load vX - vY
                Chip8Bus bus = this.emulator.getBus();
                int currentIndexRegister = this.getIndexRegister();
                int X = getX(firstByte, NN);
                int Y = getY(firstByte, NN);
                boolean iterateInReverse = X > Y;
                if (iterateInReverse) {
                    for (int i = X, j = 0; i >= Y; i--, j++) {
                        this.setRegister(i, bus.readByte(currentIndexRegister + j));
                    }
                } else {
                    for (int i = X, j = 0; i <= Y; i++, j++) {
                        this.setRegister(i, bus.readByte(currentIndexRegister + j));
                    }
                }
                yield HANDLED;
            }
            default -> 0;
        };
    }

    @Override
    protected int execute9Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute9Opcode(firstByte, NN));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        XOChipDisplay<?> display = this.emulator.getDisplay();
        Chip8Bus bus = this.emulator.getBus();
        Chip8EmulatorSettings settings = this.emulator.getEmulatorSettings();
        boolean extendedMode = display.isHiresMode();
        int currentIndexRegister = this.getIndexRegister();
        int selectedBitPlanes = display.getSelectedBitPlanes();
        boolean doClipping = settings.doClipping();

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
        int planeIterator = 0;
        this.setVF(false);
        for (int bitPlaneMask = 1; bitPlaneMask <= XOChipDisplay.BITPLANE_BASE_MASK; bitPlaneMask <<= 1) {
            if ((bitPlaneMask & selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < spriteHeight; i++, planeIterator++) {
                int sliceY = spriteY + i;
                if (sliceY >= displayHeight) {
                    if (doClipping) {
                        continue;
                    } else {
                        sliceY %= displayHeight;
                    }
                }
                int slice = draw16WideSprite
                        ? (bus.readByte(currentIndexRegister + (planeIterator * 2)) << 8) | bus.readByte(currentIndexRegister + (planeIterator * 2) + 1)
                        : bus.readByte(currentIndexRegister + planeIterator);
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
                        collided |= display.flipPixelAtBitPlanes(sliceX, sliceY, bitPlaneMask);
                    } else {
                        int scaledSliceX = sliceX * 2;
                        int scaledSliceY = sliceY * 2;
                        collided |= display.flipPixelAtBitPlanes(scaledSliceX, scaledSliceY, bitPlaneMask);
                        collided |= display.flipPixelAtBitPlanes(scaledSliceX + 1, scaledSliceY, bitPlaneMask);
                        display.flipPixelAtBitPlanes(scaledSliceX, scaledSliceY + 1, bitPlaneMask);
                        display.flipPixelAtBitPlanes(scaledSliceX + 1, scaledSliceY + 1, bitPlaneMask);
                    }
                }
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeEOpcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.executeEOpcode(firstByte, NN));
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x00 -> {
                if (firstByte == 0xF0) { // F000 NNNN: i := long NNNN
                    Chip8Bus memory = this.emulator.getBus();
                    int currentProgramCounter = this.getProgramCounter();
                    this.setIndexRegister((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                    this.incrementProgramCounter();
                    yield HANDLED;
                } else {
                    yield super.executeFOpcode(firstByte, NN);
                }
            }
            case 0x01 -> { // FX01: plane X
                this.emulator.getDisplay().setSelectedBitPlanes(getX(firstByte, NN));
                yield HANDLED;
            }
            case 0x02 -> {
                if (firstByte == 0xF0) { // F002: audio
                    Chip8Bus bus = this.emulator.getBus();
                    XOChipSoundSystem soundSystem = this.emulator.getSoundSystem();
                    int currentIndexRegister = this.getIndexRegister();
                    for (int i = 0; i < 16; i++) {
                        soundSystem.loadPatternByte(i, bus.readByte(currentIndexRegister + i));
                    }
                    yield HANDLED;
                } else {
                    yield super.executeFOpcode(firstByte, NN);
                }
            }
            case 0x3A -> { // FX3A: pitch := vX
                this.emulator.getSoundSystem().setPitch(this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            default -> super.executeFOpcode(firstByte, NN);
        };
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if (isHandled(flags) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWasFX00()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected boolean previousOpcodeWasFX00() {
        Chip8Bus bus = this.emulator.getBus();
        int currentProgramCounter = this.getProgramCounter();
        return (bus.readByte(currentProgramCounter - 2) == 0xF0) && (bus.readByte(currentProgramCounter - 1) == 0x00);
    }

}
