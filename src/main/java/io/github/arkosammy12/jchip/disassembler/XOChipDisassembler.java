package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.*;

public class XOChipDisassembler<E extends XOChipEmulator> extends SChip11Disassembler<E> {

    public XOChipDisassembler(E emulator) {
        super(emulator);
    }

    @Override
    protected int getLengthForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return (firstByte == 0xF0 && secondByte == 0x00) ? 4 : 2;
    }

    @Override
    protected int getBytecodeForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        if (this.getLengthForInstructionAt(address) == 4) {
            return ((firstByte) << 24) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3));
        } else {
            return (firstByte << 8) | bus.getByte(address + 1);
        }
    }

    @Override
    protected String getTextForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return switch (firstByte >>> 4) {
            case 0x0 -> {
                if (getX(firstByte, secondByte) == 0x0 && getY(firstByte, secondByte) == 0xD) {
                    yield "scroll-up " + getNFormatted(firstByte, secondByte);
                } else {
                    yield super.getTextForInstructionAt(address);
                }
            }
            case 0x5 -> switch (getN(firstByte, secondByte)) {
                case 0x2 -> "save v" + getXFormatted(firstByte, secondByte) + " - v" + getYFormatted(firstByte, secondByte);
                case 0x3 -> "load v" + getXFormatted(firstByte, secondByte) + " - v" + getYFormatted(firstByte, secondByte);
                default -> super.getTextForInstructionAt(address);
            };
            case 0xF -> switch (secondByte) {
                case 0x00 -> {
                    if (getX(firstByte, secondByte) == 0x0) {
                        yield "i := long " + String.format("0x%04X", (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
                    } else {
                        yield super.getTextForInstructionAt(address);
                    }
                }
                case 0x01 -> "plane 0x" + getXFormatted(firstByte, secondByte);
                case 0x02 -> "audio";
                case 0x3A -> "pitch := v" + getXFormatted(firstByte, secondByte);
                default -> super.getTextForInstructionAt(address);
            };
            default -> super.getTextForInstructionAt(address);
        };
    }

}
