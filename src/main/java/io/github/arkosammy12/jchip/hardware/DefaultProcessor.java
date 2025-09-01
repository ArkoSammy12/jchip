package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.instructions.*;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.DefaultExecutionContext;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

import java.util.Random;
import java.util.Stack;

public class DefaultProcessor implements Processor {

    private final Emulator emulator;
    private int programCounter = 512;
    private int indexRegister;
    private final Stack<Integer> programStack = new Stack<>();
    private int delayTimer;
    private int soundTimer;
    private int selectedBitPlanes = 1;
    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private Random random;

    public DefaultProcessor(Emulator emulator) {
        this.emulator = emulator;
    }

    @Override
    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    @Override
    public int getProgramCounter() {
        return this.programCounter;
    }

    @Override
    public void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister;
    }

    @Override
    public int getIndexRegister() {
        return this.indexRegister;
    }

    @Override
    public void push(int value) {
        this.programStack.push(value);
    }

    @Override
    public int pop() {
        return this.programStack.pop();
    }

    @Override
    public void setDelayTimer(int timer) {
        this.delayTimer = timer;
    }

    @Override
    public int getDelayTimer() {
        return this.delayTimer;
    }

    @Override
    public void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

    @Override
    public int getSoundTimer() {
        return this.soundTimer;
    }

    @Override
    public void setRegister(int register, int value) {
        this.registers[register] = value;
    }

    @Override
    public void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    @Override
    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
    }

    @Override
    public int getRegister(int register) {
        return this.registers[register];
    }

    @Override
    public Random getRandom() {
        Random random = this.random;
        if (random == null) {
            this.random = new Random();
        }
        return this.random;
    }

    @Override
    public void loadFlags(int length) {
        System.arraycopy(this.flagsStorage, 0, this.registers, 0, length);
    }

    @Override
    public void saveFlags(int length) {
        System.arraycopy(this.registers, 0, this.flagsStorage, 0, length);
    }

    @Override
    public void setCarry(boolean carry) {
        this.registers[0xF] = carry ? 1 : 0;
    }

    @Override
    public void incrementProgramCounter() {
        this.programCounter += 2;
    }

    @Override
    public void decrementProgramCounter() {
        this.programCounter -= 2;
    }

    @Override
    public Instruction cycle(boolean sixtiethOfASecond) throws InvalidInstructionException {
        int[] newBytes = this.fetch();
        this.incrementProgramCounter();
        Instruction instruction = this.decode(newBytes[0], newBytes[1]);
        instruction.execute();
        if (sixtiethOfASecond) {
            this.decrementTimers();
        }
        return instruction;
    }

    private int[] fetch() {
        Memory memory = this.emulator.getMemory();
        int programCounter = this.emulator.getProcessor().getProgramCounter();
        return new int[] {
                memory.readByte(programCounter),
                memory.readByte(programCounter + 1)
        };
    }

    private void decrementTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

    private Instruction decode(int firstByte, int secondByte) {
        int firstNibble = (firstByte  & 0xF0) >> 4;
        ExecutionContext executionContext = new DefaultExecutionContext(this.emulator.getProcessor(), this.emulator.getMemory(), this.emulator.getDisplay(), this.emulator.getConsoleVariant(), this.emulator.getKeyState(), this.emulator.getAudioSystem());
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        return switch (firstNibble) {
            case 0x0 -> new ZeroOpcodeInstruction(firstByte, secondByte, executionContext);
            case 0x1 -> new Jump(firstByte, secondByte, executionContext);
            case 0x2 -> new CallInstruction(firstByte, secondByte, executionContext);
            case 0x3 -> new SkipIfEqualsImmediate(firstByte, secondByte, executionContext);
            case 0x4 -> new SkipIfNotEqualsImmediate(firstByte, secondByte, executionContext);
            case 0x5 -> new FiveOpcodeInstruction(firstByte, secondByte, executionContext);
            case 0x6 -> new SetRegisterImmediate(firstByte, secondByte, executionContext);
            case 0x7 -> new AddRegisterImmediate(firstByte, secondByte, executionContext);
            case 0x8 -> new ALUInstruction(firstByte, secondByte, executionContext);
            case 0x9 -> new SkipIfRegistersNotEqual(firstByte, secondByte, executionContext);
            case 0xA -> new SetIndexRegister(firstByte, secondByte, executionContext);
            case 0xB -> new JumpWithOffset(firstByte, secondByte, executionContext);
            case 0xC -> new GetRandomNumber(firstByte, secondByte, executionContext);
            case 0xD -> switch (consoleVariant) {
                case XO_CHIP -> new XOChipDraw(firstByte, secondByte, executionContext);
                default -> new Draw(firstByte, secondByte, executionContext);
            };
            case 0xE -> new SkipIfKey(firstByte, secondByte, executionContext);
            case 0xF -> new FXOpcodeInstruction(firstByte, secondByte, executionContext);
            default -> throw new IllegalArgumentException("Invalid instruction opcode: " + firstNibble + "!");
        };
    }

}
