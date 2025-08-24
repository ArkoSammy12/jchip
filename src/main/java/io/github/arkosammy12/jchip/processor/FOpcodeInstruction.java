package io.github.arkosammy12.jchip.processor;

import com.googlecode.lanterna.input.KeyStroke;
import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.Utils;
import io.github.arkosammy12.jchip.display.CharacterSprites;

import java.io.IOException;

public class FOpcodeInstruction extends Instruction {

    public FOpcodeInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) throws IOException {
        int type = this.getSecondByte();
        int register = this.getSecondNibble();
        int vx = emulator.getProcessor().getByteInRegister(register);
        switch (type) {
            case 0x07 -> { // Set VX to current delay timer
                int value = emulator.getProcessor().getDelayTimer();
                emulator.getProcessor().setByteInRegister(register, value);
            }
            case 0x0A -> { // Get key
                KeyStroke keyStroke = emulator.getEmulatorScreen().pollInput();
                int currentProgramCounter = emulator.getProcessor().getProgramCounter();
                if (keyStroke == null) {
                    emulator.getProcessor().setProgramCounter(currentProgramCounter - 2);
                    break;
                }
                try {
                    int characterAsIntValue = Utils.getValueForCharacter(keyStroke.getCharacter());
                    emulator.getProcessor().setByteInRegister(register, characterAsIntValue);
                } catch (IllegalArgumentException ignored) {
                    emulator.getProcessor().setProgramCounter(currentProgramCounter - 2);
                }
            }
            case 0x15 -> { // Set delay timer to VX
                emulator.getProcessor().setDelayTimer(vx);
            }
            case 0x18 -> { // Set sound timer to VX
                emulator.getProcessor().setSoundTimer(vx);
            }
            case 0x1E -> { // Add to index
                int currentIndexRegister = emulator.getProcessor().getIndexRegister();
                int value = vx + currentIndexRegister;
                if (value > 0xFFF) {
                    emulator.getProcessor().setByteInRegister(0xF, 1);
                }
                int maskedValue = value & 0xFFF;
                emulator.getProcessor().setIndexRegister(maskedValue);
            }
            case 0x29 -> { // Point to font character
                int character = vx & 0xF;
                int memoryOffset = CharacterSprites.getMemoryOffsetForCharacter(character);
                emulator.getProcessor().setIndexRegister(memoryOffset);
            }
            case 0x33 -> { // Convert to binary-coded decimal
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                int[] digits = new int[3];
                int temp = vx;
                for (int i = 0; i < 3; i++) {
                    int power = (int) Math.pow(10, 2 - i);
                    int digit = (int) Math.floor((double) temp / power);
                    digits[i] = digit;
                    temp -= digit * power;
                }
                for (int i = 0; i < 3; i++) {
                    emulator.getMemory().store(currentIndexPointer + i, digits[i]);
                }
            }
            case 0x55 -> { // Store in memory
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                for (int i = 0; i < register + 1; i++) {
                    int registerValue = emulator.getProcessor().getByteInRegister(i);
                    emulator.getMemory().store(currentIndexPointer + i, registerValue);
                }
            }
            case 0x65 -> { // Load from memory
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                for (int i = 0; i < register + 1; i++) {
                    int memoryValue = emulator.getMemory().read(currentIndexPointer + i);
                    emulator.getProcessor().setByteInRegister(i, memoryValue);
                }
            }
        }
    }

}
