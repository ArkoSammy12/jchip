package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.emulators.bus.Bus;
import io.github.arkosammy12.jchip.emulators.bus.BusView;

import static io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor.getX;
import static io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor.getY;

public class MegaChipDisassembler<E extends MegaChipEmulator> extends SChip11Disassembler<E> {

    public MegaChipDisassembler(E emulator) {
        super(emulator);
    }

    @Override
    protected int getLengthForInstructionAt(int address) {
        BusView bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        return (firstByte == 0x01) ? 4 : 2;
    }

    @Override
    protected int getBytecodeForInstructionAt(int address) {
        BusView bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        if (this.getLengthForInstructionAt(address) == 4) {
            return ((firstByte) << 24) | ((secondByte) << 16) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3));
        } else {
            return (firstByte << 8) | secondByte;
        }
    }

    @Override
    protected String getTextForInstructionAt(int address) {
        BusView bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        if ((firstByte >>> 4) == 0x0) {
            return switch (getX(firstByte, secondByte)) {
                case 0x0 -> switch (secondByte) {
                    case 0x10 -> "megaoff";
                    case 0x11 -> "megaon";
                    default -> {
                        if (getY(firstByte, secondByte) == 0xB) {
                            yield "scroll_up " + getNFormatted(firstByte, secondByte);
                        } else {
                            yield super.getTextForInstructionAt(address);
                        }
                    }
                };
                case 0x1 -> "ldhi " + String.format("0x%06X", (secondByte << 16) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
                case 0x2 -> "ldpal " + getNNFormatted(firstByte, secondByte);
                case 0x3 -> "sprw " + getNNFormatted(firstByte, secondByte);
                case 0x4 -> "sprh " + getNNFormatted(firstByte, secondByte);
                case 0x5 -> "alpha " + getNNFormatted(firstByte, secondByte);
                case 0x6 -> {
                    if (getY(firstByte, secondByte) == 0x0) {
                        yield "digisnd " + getNFormatted(firstByte, secondByte);
                    } else {
                        yield super.getTextForInstructionAt(address);
                    }
                }
                case 0x7 -> {
                    if (secondByte == 0x00) {
                        yield "stopsnd";
                    } else {
                        yield super.getTextForInstructionAt(address);
                    }
                }
                case 0x8 -> "bmode " + getNFormatted(firstByte, secondByte);
                case 0x9 -> "ccol " + getNNFormatted(firstByte, secondByte);
                default -> super.getTextForInstructionAt(address);
            };
        } else {
            return super.getTextForInstructionAt(address);
        }
    }

}
