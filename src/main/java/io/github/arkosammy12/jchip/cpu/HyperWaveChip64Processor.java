package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.HyperWaveChip64Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;

public class HyperWaveChip64Processor<E extends HyperWaveChip64Emulator<D, S>, D extends HyperWaveChip64Display, S extends Chip8SoundSystem> extends XOChipProcessor<E, D, S> {

    public HyperWaveChip64Processor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        int flagsSuper = super.execute0Opcode(firstByte, NN);
        if (isHandled(flagsSuper)) {
            return flagsSuper;
        }
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xE1 -> { // OOE1: Invert selected bitplanes
                    this.emulator.getDisplay().invert();
                    yield HANDLED;
                }
                case 0xF1 -> { // 00F1: Set draw mode to OR
                    this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.OR);
                    yield HANDLED;
                }
                case 0xF2 -> { // 00F2: Set draw mode to SUBTRACT
                    this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.SUBTRACT);
                    yield HANDLED;
                }
                case 0xF3 -> { // 00F3: Set draw mode to XOR
                    this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.XOR);
                    yield HANDLED;
                }
                default -> 0;
            };
        } else {
            return 0;
        }
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        int flagsSuper = super.execute5Opcode(firstByte, NN);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        if (getNFromNN(NN) == 0x1) { // 5XY1: Skip if register greater than
            int flags = HANDLED;
            if (this.getRegister(getXFromFirstByte(firstByte)) > this.getRegister(getYFromNN(NN))) {
                this.incrementProgramCounter();
                if (this.previousOpcodeWasFX00()) {
                    this.incrementProgramCounter();
                }
                flags = set(flags, SKIP_TAKEN);
            }
            return flags;
        } else {
            return 0;
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int execute8Opcode(int firstByte, int NN) {
        int flagsSuper = super.execute8Opcode(firstByte, NN);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        return switch (getNFromNN(NN)) {
            case 0xC -> { // 8XYC: Multiply registers
                int X = getXFromFirstByte(firstByte);
                int product = this.getRegister(X) * this.getRegister(getYFromNN(NN));
                this.setRegister(X, product);
                this.setRegister(0xF, (product & 0xFF00) >>> 8);
                yield HANDLED;
            }
            case 0xD -> { // 8XYD: Divide registers
                int X = getXFromFirstByte(firstByte);
                int vY = this.getRegister(getYFromNN(NN));
                if (vY <= 0) {
                    this.setRegister(X, 0);
                    this.setVF(false);
                } else {
                    int vX = this.getRegister(X);
                    this.setRegister(X, vX / vY);
                    this.setRegister(0xF, vX % vY);
                }
                yield HANDLED;
            }
            case 0xF -> { // 8XYF: Divide registers inverse
                int X = getXFromFirstByte(firstByte);
                int vX = this.getRegister(X);
                if (vX <= 0) {
                    this.setRegister(X, 0);
                    this.setVF(false);
                } else {
                    int vY = this.getRegister(getYFromNN(NN));
                    this.setRegister(X, vY / vX);
                    this.setRegister(0xF, vY % vX);
                }
                yield HANDLED;
            }
            default -> 0;
        };
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        int flagsSuper = super.executeFOpcode(firstByte, NN);
        if (isHandled(flagsSuper)) {
            return flagsSuper;
        }
        return switch (NN) {
            case 0x00 -> switch (firstByte) {
                case 0xF1 -> { // F100 NNNN: Long jump
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentProgramCounter = this.getProgramCounter();
                    this.setProgramCounter((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                    yield HANDLED;
                }
                case 0xF2 -> { // F200 NNNN: Long call to subroutine
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentProgramCounter = this.getProgramCounter();
                    this.push(currentProgramCounter + 2);
                    this.setProgramCounter((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                    yield HANDLED;
                }
                case 0xF3 -> { // F300 NNNN: Long jump with offset
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentProgramCounter = this.getProgramCounter();
                    this.setProgramCounter(((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1)) + this.getRegister(0x0));
                    yield HANDLED;
                }
                default -> 0;
            };
            case 0x03 -> { // FX03: Set color of palette X to three byte (24-bit) color from memory at I, I+1, I+2
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                this.emulator.getDisplay().setPaletteEntry(getXFromFirstByte(firstByte) & 0xF, (memory.readByte(currentIndexRegister) << 16) | (memory.readByte(currentIndexRegister + 1) << 8) | memory.readByte(currentIndexRegister + 2));
                yield HANDLED;
            }
            case 0x1F -> { // FX1F: Subtract register from index
                this.setIndexRegister(this.getIndexRegister() - this.getRegister(getXFromFirstByte(firstByte)));
                yield HANDLED;
            }
            default -> 0;
        };
    }

    @Override
    protected boolean previousOpcodeWasFX00() {
        Chip8Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        int firstByte = memory.readByte(currentProgramCounter - 2);
        return (firstByte == 0xF0 || firstByte == 0xF1 || firstByte == 0xF2 || firstByte == 0xF3) && (memory.readByte(currentProgramCounter - 1) == 0x00);
    }
}
