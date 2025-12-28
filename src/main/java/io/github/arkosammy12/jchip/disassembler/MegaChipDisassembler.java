package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.getX;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.getY;

public class MegaChipDisassembler<E extends MegaChipEmulator> extends SChip11Disassembler<E> {

    public MegaChipDisassembler(E emulator) {
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
        if (firstByte == 0x01) {
            this.addDisassemblerEntry(address, 4, ((firstByte) << 24) | ((secondByte) << 16) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3)));
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
        if (firstByte == 0x01) {
            return ((firstByte) << 24) | ((secondByte) << 16) | (bus.getByte(address + 2) << 8) | (bus.getByte(address + 3));
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
        if ((firstByte >>> 4) == 0x0) {
            return switch (getX(firstByte, secondByte)) {
                case 0x0 -> switch (secondByte) {
                    case 0x10 -> "megaoff";
                    case 0x11 -> "megaon";
                    default -> {
                        if (getY(firstByte, secondByte) == 0xB) {
                            yield "scroll_up " + getNFormatted(firstByte, secondByte);
                        } else {
                            yield super.getTextForEntry(entry);
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
                        yield super.getTextForEntry(entry);
                    }
                }
                case 0x7 -> {
                    if (secondByte == 0x00) {
                        yield "stopsnd";
                    } else {
                        yield super.getTextForEntry(entry);
                    }
                }
                case 0x8 -> "bmode " + getNFormatted(firstByte, secondByte);
                case 0x9 -> "ccol " + getNNFormatted(firstByte, secondByte);
                default -> super.getTextForEntry(entry);
            };
        } else {
            return super.getTextForEntry(entry);
        }
    }

}
