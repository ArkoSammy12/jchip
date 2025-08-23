package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

import java.io.IOException;
import java.util.Stack;

public class Processor {

    private int programCounter = 512;
    private int indexRegister;
    private final Stack<Integer> programStack = new Stack<>();
    private int delayTimer;
    private int soundTimer;
    private final int[] registers = new int[16];

    void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    int getProgramCounter() {
        return this.programCounter;
    }

    void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister;
    }

    int getIndexRegister() {
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

    int getDelayTimer() {
        return this.delayTimer;
    }

    void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

    public int getSoundTimer() {
        return this.soundTimer;
    }

    void setByteInRegister(int register, int value) {
        this.registers[register] = value;
    }

    int getByteInRegister(int register) {
        return this.registers[register];
    }

    public void cycle(Emulator emulator, boolean decrementTimers) throws IOException {
        int[] newBytes = emulator.fetch(programCounter);
        this.programCounter += 2;
        Instruction instruction = Instructions.decodeBytes(newBytes[0], newBytes[1]);
        instruction.execute(emulator);
        if (decrementTimers) {
            this.decrementTimers();
        }
        emulator.getEmulatorScreen().flush();
    }

    private void decrementTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

}
