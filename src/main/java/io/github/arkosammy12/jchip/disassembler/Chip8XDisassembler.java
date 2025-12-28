package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.*;

public class Chip8XDisassembler extends Chip8Disassembler {

    public Chip8XDisassembler(Emulator emulator) {
        super(emulator);
    }

    @Override
    protected String getTextForEntry(Entry entry) {
        int address = entry.getInstructionAddress();
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return switch (firstByte >>> 4) {
            case 0x0 -> {
                if (getNNN(firstByte, secondByte) == 0x2A0) {
                    yield "cycle-bgcol";
                } else {
                    yield super.getTextForEntry(entry);
                }
            }
            case 0x5 -> {
                if (getN(firstByte, secondByte) == 0x1) {
                    yield "0x5" + getXFormatted(firstByte, secondByte) + " 0x" + getYFormatted(firstByte, secondByte) + "1";
                } else {
                    yield super.getTextForEntry(entry);
                }
            }
            case 0xB -> {
                if (getN(firstByte, secondByte) == 0x0) {
                    yield "col-low " + getXFormatted(firstByte, secondByte) + " " + getYFormatted(firstByte, secondByte);
                } else {
                    yield "col-high " + getXFormatted(firstByte, secondByte) + " " + getYFormatted(firstByte, secondByte) + " " + getNFormatted(firstByte, secondByte);
                }
            }
            case 0xE -> switch (secondByte) {
                case 0xF2 -> "0xE" + getXFormatted(firstByte, secondByte) + " 0xF2";
                case 0xF5 -> "0xE" + getXFormatted(firstByte, secondByte) + " 0xF5";
                default -> super.getTextForEntry(entry);
            };
            case 0xF -> switch (secondByte) {
                case 0xF8 -> "0xF" + getXFormatted(firstByte, secondByte) + " 0xF8";
                case 0xFB -> "0xF" + getXFormatted(firstByte, secondByte) + " 0xFB";
                default -> super.getTextForEntry(entry);
            };
            default -> super.getTextForEntry(entry);
        };
    }

}
