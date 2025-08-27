package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

import java.io.IOException;
import java.util.Random;
import java.util.Stack;

public class Processor {

    private int programCounter = 512;
    private int indexRegister;
    private final Stack<Integer> programStack = new Stack<>();
    private int delayTimer;
    private int soundTimer;
    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private Random random;

    void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public int getProgramCounter() {
        return this.programCounter;
    }

    void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister;
    }

    public int getIndexRegister() {
        return this.indexRegister;
    }

    void push(int b) {
        this.programStack.push(b);
    }

    int pop() {
        return this.programStack.pop();
    }

    void setDelayTimer(int timer) {
        this.delayTimer = timer;
    }

    public int getDelayTimer() {
        return this.delayTimer;
    }

    void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

    public int getSoundTimer() {
        return this.soundTimer;
    }

    void setRegisterValue(int register, int value) {
        this.registers[register] = value;
    }

    public int getRegisterValue(int register) {
        return this.registers[register];
    }

    void setCarry(boolean carry) {
        this.registers[0xF] = carry ? 1 : 0;
    }

    void incrementProgramCounter() {
        this.programCounter += 2;
    }

    void decrementProgramCounter() {
        this.programCounter -= 2;
    }

    Random getRandom() {
        if (this.random == null) {
            this.random = new Random();
        }
        return this.random;
    }

    private void decrementTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

    void loadFlags(int length) {
        // TODO: Implementation may be wrong
        System.arraycopy(this.flagsStorage, 0, this.registers, 0, length);
    }

    void saveFlags(int length) {
        System.arraycopy(this.registers, 0, this.flagsStorage, 0, length);
    }

    public void execute(Emulator emulator, Instruction instruction, boolean decrementTimers) throws IOException {
        this.incrementProgramCounter();
        instruction.execute(emulator);
        if (decrementTimers) {
            this.decrementTimers();
        }
    }

}
