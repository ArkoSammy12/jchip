package io.github.arkosammy12.hardware;

import java.io.IOException;
import java.util.Stack;

public class Processor {

    private int programCounter;
    private int indexRegister; // "I" Register
    private final Stack<Integer> programStack = new Stack<>();
    private int delayTimer;
    private int soundTimer;
    private int[] registers = new int[16];

    public void cycle(Emulation emulation) throws IOException {
        int[] newBytes = emulation.fetch(programCounter);
        this.programCounter += 2;
        int firstByte = newBytes[0];
        int secondByte = newBytes[1];                                 // NN

        int firstNibble  = (firstByte  & 0xF0) >> 4;                  // X
        int secondNibble = (firstByte  & 0x0F);                       // Y
        int fourthNibble = (secondByte & 0x0F);                       // N
        int memoryAddress = ((firstByte << 8) | secondByte) & 0x0FFF; // NNN
        int thirdNibble  = (secondByte & 0xF0) >> 4;

        switch (firstNibble) {
            case 0 -> { // Clear Screen
                if (thirdNibble == 0xE) {
                    emulation.getEmulatorScreen().clear();
                }
            }
            case 1 -> {
                this.programCounter =  memoryAddress;
            }
            case 6 -> {
                this.registers[secondNibble] = secondByte;
            }
            case 7 -> {
                this.registers[secondNibble] += (secondByte) % 256;
            }
            case 0xA -> {
                this.indexRegister = memoryAddress;
            }
            case 0xD -> {
                int column = this.registers[secondNibble] % 64;
                int row = this.registers[thirdNibble] % 32;
                int rows = fourthNibble;

                this.registers[0xF] = 0;

                for (int i = 0; i <= rows; i++) {
                    int sprite = emulation.getMemory().read(this.indexRegister + i);

                    for (int j = 7; j >= 0; j--) {
                        int mask = (int) Math.pow(2, j);
                        if ((sprite & mask) > 0) {
                            boolean onToOff = emulation.getEmulatorScreen().togglePixelAt(column + (7 - j), row + i);
                            if (onToOff) {
                                this.registers[0xF] = 1;
                            }
                        }

                    }

                }


            }
        }

        emulation.getEmulatorScreen().flush();

    }


}
