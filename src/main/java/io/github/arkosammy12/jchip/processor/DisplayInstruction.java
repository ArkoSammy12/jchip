package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;

public class DisplayInstruction extends Instruction {

    public DisplayInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int height = this.getFourthNibble();

        int column = emulator.getProcessor().getByteInRegister(firstRegister) % 64;
        int row = emulator.getProcessor().getByteInRegister(secondRegister) % 32;

        emulator.getProcessor().setByteInRegister(0xF, 0);

        for (int i = 0; i < height; i++) {
            int sprite = emulator.getMemory().read(emulator.getProcessor().getIndexRegister() + i);
            for (int j = 7; j >= 0; j--) {
                int mask = (int) Math.pow(2, j);
                if ((sprite & mask) > 0) {
                    boolean toggledOff = emulator.getEmulatorScreen().togglePixelAt(column + (7 - j), row + i);
                    if (toggledOff) {
                        emulator.getProcessor().setByteInRegister(0xF, 1);
                    }
                }
            }
        }
    }

}
