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
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        if (secondNibble == 0x0) {
            switch (thirdNibble) {
                case 0xE -> { // OOE1: Invert selected bitplanes
                    if (fourthNibble != 0x1) {
                        return 0;
                    }
                    this.emulator.getDisplay().invertSelectedBitplanes();
                }
                case 0xF -> {
                    switch (fourthNibble) {
                        case 0x1 -> this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.OR); // 00F1: Set draw mode to OR
                        case 0x2 -> this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.SUBTRACT); // 00F2: Set draw mode to SUBTRACT
                        case 0x3 -> this.emulator.getDisplay().setDrawingMode(HyperWaveChip64Display.DrawingMode.XOR); // 00F3: Set draw mode to XOR
                        default -> flags = clear(flags, HANDLED);
                    }
                }
                default -> flags = clear(flags, HANDLED);
            }
        } else {
            flags = clear(flags, HANDLED);
        }
        return flags;
    }

    @Override
    protected int executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        if (fourthNibble == 0x1) { // 5XY1: Skip if vX > vY
            if (this.getRegister(secondNibble) > this.getRegister(thirdNibble)) {
                this.incrementProgramCounter();
                if (this.previousOpcodeWasFX00()) {
                    this.incrementProgramCounter();
                }
            }
        } else {
            flags = clear(flags, HANDLED);
        }
        return flags;
    }

    @Override
    protected int executeALUInstruction(int secondNibble, int thirdNibble, int fourthNibble) {
        int flagsSuper = super.executeALUInstruction(secondNibble, thirdNibble, fourthNibble);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        switch (fourthNibble) {
            case 0xC -> { // 8XYC: Multiply vX * vY
                int product = this.getRegister(secondNibble) * this.getRegister(thirdNibble);
                this.setRegister(secondNibble, product);
                this.setRegister(0xF, (product & 0xFF00) >>> 8);
            }
            case 0xD -> { // 8XYD: Divide vX / vY
                int vY = this.getRegister(thirdNibble);
                if (vY <= 0) {
                    this.setRegister(secondNibble, 0);
                    this.setVF(false);
                } else {
                    int vX = this.getRegister(secondNibble);
                    this.setRegister(secondNibble, vX / vY);
                    this.setRegister(0xF, vX % vY);
                }
            }
            case 0xF -> { // 8XYF: Divide vY / vX
                int vX = this.getRegister(secondNibble);
                if (vX <= 0) {
                    this.setRegister(secondNibble, 0);
                    this.setVF(false);
                } else {
                    int vY = this.getRegister(thirdNibble);
                    this.setRegister(secondNibble, vY / vX);
                    this.setRegister(0xF, vY % vX);
                }
            }
            default -> flags = clear(flags, HANDLED);
        }
        return flags;
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if (isSet(flagsSuper, HANDLED)) {
            return flagsSuper;
        }
        int flags = HANDLED;
        Chip8Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        switch (secondByte) {
            case 0x00 -> {
                switch (secondNibble) {
                    case 0x1 -> this.setProgramCounter((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1)); // F100 NNNN: Long jump
                    case 0x2 -> { // F200 NNNN: Long call to subroutine
                        this.push(currentProgramCounter + 2);
                        this.setProgramCounter((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                    }
                    case 0x3 -> this.setProgramCounter(((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1)) + this.getRegister(0x0)); // F300 NNNN: Long jump with offset
                    default -> flags = clear(flags, HANDLED);
                }
            }
            case 0x03 -> { // FX03: Set color of palette X to three byte (24-bit) color from memory at I, I+1, I+2
                int currentIndexRegister = this.getIndexRegister();
                this.emulator.getDisplay().setPaletteEntry(secondNibble & 0xF, (memory.readByte(currentIndexRegister) << 16) | (memory.readByte(currentIndexRegister + 1) << 8) | memory.readByte(currentIndexRegister + 2));
            }
            case 0x1F -> this.setIndexRegister(this.getIndexRegister() - this.getRegister(secondNibble)); // FX1F: Subtract vX from I
            default -> flags = clear(flags, HANDLED);
        }
        return flags;
    }

    @Override
    protected boolean previousOpcodeWasFX00() {
        Chip8Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        int firstByte = memory.readByte(currentProgramCounter - 2);
        return (firstByte == 0xF0 || firstByte == 0xF1 || firstByte == 0xF2 || firstByte == 0xF3) && (memory.readByte(currentProgramCounter - 1) == 0x00);
    }
}
