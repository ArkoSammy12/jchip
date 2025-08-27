package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.ConsoleVariant;
import io.github.arkosammy12.jchip.io.EmulatorScreen;

public class DisplayInstruction extends Instruction {

    public DisplayInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }


    @Override
    public void execute(Emulator emulator) {

        /*
         int firstRegister = this.getSecondNibble();
         int secondRegister = this.getThirdNibble();
         int screenWidth = emulator.getEmulatorScreen().getScreenWidth();
         int screenHeight = emulator.getEmulatorScreen().getScreenHeight();
         int column = emulator.getProcessor().getRegisterValue(firstRegister) % screenWidth;
         int row = emulator.getProcessor().getRegisterValue(secondRegister) % screenHeight;
         int type = this.getFourthNibble();
         int currentIndexRegister = emulator.getProcessor().getIndexRegister();
         ConsoleVariant consoleVariant = emulator.getConsoleVariant();
         boolean toggledOffOcurred = false;
         int collisionCounter = 0;
         */

        if (emulator.getConsoleVariant() == ConsoleVariant.CHIP_8) {
            this.executeWithExtendedMode(emulator);
            return;
        }

         if (emulator.getEmulatorScreen().isExtendedMode()) {
             this.executeWithExtendedMode(emulator);
         } else {
             this.executeWithoutExtendedMode(emulator);
         }

    }

    private void executeWithExtendedMode(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int screenWidth = emulator.getEmulatorScreen().getScreenWidth();
        int screenHeight = emulator.getEmulatorScreen().getScreenHeight();
        int column = emulator.getProcessor().getRegisterValue(firstRegister) % screenWidth;
        int row = emulator.getProcessor().getRegisterValue(secondRegister) % screenHeight;
        int type = this.getFourthNibble();
        int currentIndexRegister = emulator.getProcessor().getIndexRegister();
        ConsoleVariant consoleVariant = emulator.getConsoleVariant();
        int collisionCounter = 0;
        switch (type) {
            case 0x0 -> { // Draw 16x16 sprite
                if (!emulator.getProgramArgs().getConsoleVariant().isSchipOrXoChip()) {
                    break;
                }
                emulator.getProcessor().setCarry(false);

                for (int i = 0; i < 16; i++) {
                    int sliceY = row + i;
                    if (consoleVariant != ConsoleVariant.XO_CHIP && sliceY >= screenHeight) {
                        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                            collisionCounter++;
                        }
                        break;
                    } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                        sliceY %= screenHeight;
                    }
                    int firstByte = emulator.getMemory().read((currentIndexRegister + (row * 2)) & 0xFFF);
                    int secondByte = emulator.getMemory().read((currentIndexRegister + (row * 2) + 1) & 0xFFF);
                    int slice = (firstByte << 8) | secondByte;
                    boolean rowHasCollision = false;
                    for (int j = 0; j < 16; j++) {
                        int sliceX = column + j;
                        if (consoleVariant != ConsoleVariant.XO_CHIP && sliceX >= screenWidth) {
                            break;
                        } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                            sliceX %= screenWidth;
                        }
                        int mask = (int) Math.pow(2, 15 - i);
                        if ((slice & mask) <= 0) {
                            continue;
                        }
                        boolean toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX, sliceY);
                        if ((collisionCounter < 1) && toggledOff && consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            collisionCounter = 1;
                        } else if (toggledOff && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                            rowHasCollision = true;
                        }
                    }
                    if (rowHasCollision) {
                        collisionCounter++;
                    }
                }

            }
            default -> { // Draw 8xN sprite
                int height = type;
                emulator.getProcessor().setCarry(false);
                for (int i = 0; i < height; i++) {
                    int sliceY = row + i;
                    if (consoleVariant != ConsoleVariant.XO_CHIP && sliceY >= screenHeight) {
                        if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                            collisionCounter++;
                        }
                        break;
                    } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                        sliceY %= screenHeight;
                    }
                    // Reading from memory beyond 0xFFF is undefined behavior. Chosen action is to overflow back to 0
                    int slice = emulator.getMemory().read((currentIndexRegister + i) & 0xFFF);
                    boolean rowHasCollision = false;
                    for (int j = 0; j < 8; j++) {
                        int sliceX = column + j;
                        if (consoleVariant != ConsoleVariant.XO_CHIP && sliceX >= screenWidth) {
                            break;
                        } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                            sliceX %= screenWidth;
                        }
                        int mask = (int) Math.pow(2, 7 - j);
                        if ((slice & mask) <= 0) {
                            continue;
                        }
                        boolean toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX, sliceY);
                        if ((collisionCounter < 1) && toggledOff && consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            collisionCounter = 1;
                        } else if (toggledOff && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                            rowHasCollision = true;
                        }
                    }
                    if (rowHasCollision) {
                        collisionCounter++;
                    }

                }
            }
        }
        emulator.getProcessor().setRegisterValue(0xF, collisionCounter);
    }

    private void executeWithoutExtendedMode(Emulator emulator) {
        int firstRegister = this.getSecondNibble();
        int secondRegister = this.getThirdNibble();
        int screenWidth = emulator.getEmulatorScreen().getScreenWidth();
        int screenHeight = emulator.getEmulatorScreen().getScreenHeight();
        int column = emulator.getProcessor().getRegisterValue(firstRegister) % screenWidth;
        int row = emulator.getProcessor().getRegisterValue(secondRegister) % screenHeight;
        int height = this.getFourthNibble();
        if (height < 1) {
            height = 16;
        }
        int currentIndexRegister = emulator.getProcessor().getIndexRegister();
        ConsoleVariant consoleVariant = emulator.getConsoleVariant();
        int collisionCounter = 0;
        emulator.getProcessor().setCarry(false);
        for (int i = 0; i < height; i++) {
            int sliceY = row + i;
            if (consoleVariant != ConsoleVariant.XO_CHIP && sliceY >= screenHeight) {
                if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                    collisionCounter++;
                }
                break;
            } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                sliceY %= screenHeight;
            }
            // Reading from memory beyond 0xFFF is undefined behavior. Chosen action is to overflow back to 0
            int slice = emulator.getMemory().read((currentIndexRegister + i) & 0xFFF);
            boolean rowHasCollision = false;
            for (int j = 0; j < 8; j++) {
                int sliceX = column + j;
                if (consoleVariant != ConsoleVariant.XO_CHIP && sliceX >= screenWidth) {
                    break;
                } else if (consoleVariant == ConsoleVariant.XO_CHIP) {
                    sliceX %= screenWidth;
                }
                int mask = (int) Math.pow(2, 7 - j);
                if ((slice & mask) <= 0) {
                    continue;
                }
                boolean toggledOff = emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, sliceY * 2);
                emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, sliceY * 2);
                emulator.getEmulatorScreen().togglePixelAt(sliceX * 2, (sliceY * 2) + 1);
                emulator.getEmulatorScreen().togglePixelAt((sliceX * 2) + 1, (sliceY * 2) + 1);

                if ((collisionCounter < 1) && toggledOff && consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                    collisionCounter = 1;
                } else if (toggledOff && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                    rowHasCollision = true;
                }
            }
            if (rowHasCollision) {
                collisionCounter++;
            }
        }
        emulator.getProcessor().setRegisterValue(0xF, collisionCounter);
    }

}
