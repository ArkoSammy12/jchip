package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.HyperWaveChip64Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.getN;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.getX;

public class HyperWaveChip64Disassembler<E extends HyperWaveChip64Emulator> extends XOChipDisassembler<E> {

    public HyperWaveChip64Disassembler(E emulator) {
        super(emulator);
    }

    @Override
    public void disassembleAt(int address) {
        if (!this.isEnabled()) {
            return;
        }
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        if ((firstByte == 0xF0 || firstByte == 0xF1 || firstByte == 0xF2 || firstByte == 0xF3) && secondByte == 0x00) {
            this.addDisassemblerEntry(address, 4, ((firstByte) << 24) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
        } else {
            this.addDisassemblerEntry(address, 2, (bus.getByte(address) << 8) | bus.getByte(address + 1));
        }
    }

    @Override
    protected int getBytecodeForEntry(Entry entry) {
        int address = entry.getInstructionAddress();
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        if ((firstByte == 0xF0 || firstByte == 0xF1 || firstByte == 0xF2 || firstByte == 0xF3) && secondByte == 0x00) {
            return ((firstByte) << 24) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3));
        } else {
            return (bus.getByte(address) << 8) | bus.getByte(address + 1);
        }
    }

    @Override
    protected String getTextForEntry(Entry entry) {
        int address = entry.getInstructionAddress();
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return switch (firstByte >>> 4) {
            case 0x0 -> {
                if (firstByte == 0x00) {
                    yield switch (secondByte) {
                        case 0xE1 -> "INVERT";
                        case 0xF1 -> "OR MODE";
                        case 0xF2 -> "SUBTRACT MODE";
                        case 0xF3 -> "XOR MODE";
                        default -> super.getTextForEntry(entry);
                    };
                } else {
                    yield super.getTextForEntry(entry);
                }
            }
            case 0x5 -> {
                if (getN(firstByte, secondByte) == 0x1) {
                    yield "SKIP V" + getXFormatted(firstByte, secondByte) + " > V" + getYFormatted(firstByte, secondByte);
                } else {
                    yield super.getTextForEntry(entry);
                }
            }
            case 0x8 -> switch (getN(firstByte, secondByte)) {
                case 0xC -> "MULT V" + getXFormatted(firstByte, secondByte) + ", V" + getYFormatted(firstByte, secondByte);
                case 0xD -> "DIV V" + getXFormatted(firstByte, secondByte) + ", V" + getYFormatted(firstByte, secondByte);
                case 0xF -> "DIV V" + getYFormatted(firstByte, secondByte) + ", V" + getXFormatted(firstByte, secondByte);
                default -> super.getTextForEntry(entry);
            };
            case 0xF -> switch (secondByte) {
                case 0x00 -> switch (getX(firstByte, secondByte)) {
                    case 0x1 -> "LONG JUMP " + String.format("0x%04X", (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
                    case 0x2 -> "LONG CALL SUBROUTINE " + String.format("0x%04X", (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
                    case 0x3 -> "LONG JUMP0" + String.format("0x%04X", (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
                    default -> super.getTextForEntry(entry);
                };
                case 0x03 -> "PALETTE 0x" + getNFormatted(firstByte, secondByte);
                case 0x1F -> "SUB I, V" + getXFormatted(firstByte, secondByte);
                default -> super.getTextForEntry(entry);
            };
            default -> super.getTextForEntry(entry);
        };
    }

}
