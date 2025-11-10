package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.HyperWaveChip64Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.HyperWaveChip64Display;

public class HyperWaveChip64Processor<E extends HyperWaveChip64Emulator<M, D, S>, M extends XOChipMemory, D extends HyperWaveChip64Display, S extends Chip8SoundSystem> extends XOChipProcessor<E, M, D, S> {

    public HyperWaveChip64Processor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xE1 -> { // 00E1: Invert selected bitplanes
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
                default -> super.execute0Opcode(firstByte, NN);
            };
        } else {
            return super.execute0Opcode(firstByte, NN);
        }
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (getN(firstByte, NN) == 0x1) { // 5XY1: Skip if register greater than
            int flags = HANDLED;
            if (this.getRegister(getX(firstByte, NN)) > this.getRegister(getY(firstByte, NN))) {
                this.incrementProgramCounter();
                if (this.previousOpcodeWasFX00()) {
                    this.incrementProgramCounter();
                }
                flags = set(flags, SKIP_TAKEN);
            }
            return flags;
        } else {
            return super.execute5Opcode(firstByte, NN);
        }
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int execute8Opcode(int firstByte, int NN) {
        return switch (getN(firstByte, NN)) {
            case 0xC -> { // 8XYC: Multiply registers
                int X = getX(firstByte, NN);
                int product = this.getRegister(X) * this.getRegister(getY(firstByte, NN));
                this.setRegister(X, product);
                this.setRegister(0xF, (product & 0xFF00) >>> 8);
                yield HANDLED;
            }
            case 0xD -> { // 8XYD: Divide registers
                int X = getX(firstByte, NN);
                int vY = this.getRegister(getY(firstByte, NN));
                if (vY == 0) {
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
                int X = getX(firstByte, NN);
                int vX = this.getRegister(X);
                if (vX == 0) {
                    this.setRegister(X, 0);
                    this.setVF(false);
                } else {
                    int vY = this.getRegister(getY(firstByte, NN));
                    this.setRegister(X, vY / vX);
                    this.setRegister(0xF, vY % vX);
                }
                yield HANDLED;
            }
            default -> super.execute8Opcode(firstByte, NN);
        };
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
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
                default -> super.executeFOpcode(firstByte, NN);
            };
            case 0x03 -> { // FX03: Set color of palette X to three byte (24-bit) color from memory at I, I+1, I+2
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                this.emulator.getDisplay().setPaletteEntry(getX(firstByte, NN) & 0xF, (memory.readByte(currentIndexRegister) << 16) | (memory.readByte(currentIndexRegister + 1) << 8) | memory.readByte(currentIndexRegister + 2));
                yield HANDLED;
            }
            case 0x1F -> { // FX1F: Subtract register from index
                this.setIndexRegister(this.getIndexRegister() - this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            default -> super.executeFOpcode(firstByte, NN);
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
