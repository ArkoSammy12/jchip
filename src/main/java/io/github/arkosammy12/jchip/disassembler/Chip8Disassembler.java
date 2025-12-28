package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.*;

public class Chip8Disassembler extends AbstractDisassembler {

    public Chip8Disassembler(Emulator emulator) {
        super(emulator);
    }

    @Override
    public void disassembleAt(int address) {
        if (!this.isEnabled()) {
            return;
        }
        Bus bus = this.emulator.getBus();
        this.addDisassemblerEntry(address, 2, (bus.getByte(address) << 8) | bus.getByte(address + 1));
    }

    @Override
    protected String getTextForEntry(Entry entry) {
        String text = "invalid";
        int address = entry.getInstructionAddress();
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        switch (firstByte >>> 4) {
            case 0x0 -> {
                switch (secondByte) {
                    case 0xE0 -> text = "clear";
                    case 0xEE -> text = "return";
                }
            }
            case 0x1 -> text = "jump " + getNNNFormatted(firstByte, secondByte);
            case 0x2 -> text = ":call " + getNNNFormatted(firstByte, secondByte);
            case 0x3 -> text = "if v" + getXFormatted(firstByte, secondByte) + " != " + getNNFormatted(firstByte, secondByte) + " then";
            case 0x4 -> text = "if v" + getXFormatted(firstByte, secondByte) + " == " + getNNFormatted(firstByte, secondByte) + " then";
            case 0x5 -> {
                if (getN(firstByte, secondByte) == 0x0) {
                    text = "if v" + getXFormatted(firstByte, secondByte) + " != v" + getYFormatted(firstByte, secondByte) + " then";
                }
            }
            case 0x6 -> text = "v" + getXFormatted(firstByte, secondByte) + " := " + getNNFormatted(firstByte, secondByte);
            case 0x7 -> text = "v" + getXFormatted(firstByte, secondByte) + " += " + getNNFormatted(firstByte, secondByte);
            case 0x8 -> {
                switch (getN(firstByte, secondByte)) {
                    case 0x0 -> text = "v" + getXFormatted(firstByte, secondByte) + " := v" + getYFormatted(firstByte, secondByte);
                    case 0x1 -> text = "v" + getXFormatted(firstByte, secondByte) + " |= v" + getYFormatted(firstByte, secondByte);
                    case 0x2 -> text = "v" + getXFormatted(firstByte, secondByte) + " &= v" + getYFormatted(firstByte, secondByte);
                    case 0x3 -> text = "v" + getXFormatted(firstByte, secondByte) + " ^= v" + getYFormatted(firstByte, secondByte);
                    case 0x4 -> text = "v" + getXFormatted(firstByte, secondByte) + " += v" + getYFormatted(firstByte, secondByte);
                    case 0x5 -> text = "v" + getXFormatted(firstByte, secondByte) + " -= v" + getYFormatted(firstByte, secondByte);
                    case 0x6 -> text = "v" + getXFormatted(firstByte, secondByte) + " >>= v" + getYFormatted(firstByte, secondByte);
                    case 0x7 -> text = "v" + getXFormatted(firstByte, secondByte) + " =- v" + getYFormatted(firstByte, secondByte);
                    case 0xE -> text = "v" + getXFormatted(firstByte, secondByte) + " <<= v" + getYFormatted(firstByte, secondByte);
                }
            }
            case 0x9 -> {
                if (getN(firstByte, secondByte) == 0x0) {
                    text = "if v" + getXFormatted(firstByte, secondByte) + " == v" + getYFormatted(firstByte, secondByte) + " then";
                }
            }
            case 0xA -> text = "i := " + getNNNFormatted(firstByte, secondByte);
            case 0xB -> text = "jump0 " + getNNNFormatted(firstByte, secondByte);
            case 0xC -> text = "v" + getXFormatted(firstByte, secondByte) + " := random " + getNNFormatted(firstByte, secondByte);
            case 0xD -> text = "sprite v" + getXFormatted(firstByte, secondByte) + " v" + getYFormatted(firstByte, secondByte) + " " + getNFormatted(firstByte, secondByte);
            case 0xE -> {
                switch (secondByte) {
                    case 0x9E -> text = "if v" + getXFormatted(firstByte, secondByte) + " -key then";
                    case 0xA1 -> text = "if v" + getXFormatted(firstByte, secondByte) + " key then";
                }
            }
            case 0xF -> {
                switch (secondByte) {
                    case 0x07 -> text = "v" + getXFormatted(firstByte, secondByte) + " := delay";
                    case 0x0A -> text = "v" + getXFormatted(firstByte, secondByte) + " := key";
                    case 0x15 -> text = "delay := v" + getXFormatted(firstByte, secondByte);
                    case 0x18 -> text = "buzzer := v" + getXFormatted(firstByte, secondByte);
                    case 0x1E -> text = "i += v" + getXFormatted(firstByte, secondByte);
                    case 0x29 -> text = "i := hex v" + getXFormatted(firstByte, secondByte);
                    case 0x33 -> text = "bcd v" + getXFormatted(firstByte, secondByte);
                    case 0x55 -> text = "save v" + getXFormatted(firstByte, secondByte);
                    case 0x65 -> text = "load v" + getXFormatted(firstByte, secondByte);
                }
            }
        }
        return text;
    }

    public static String getXFormatted(int firstByte, int secondByte) {
        return String.format("%01X", getX(firstByte, secondByte));
    }

    public static String getYFormatted(int firstByte, int secondByte) {
        return String.format("%01X", getY(firstByte, secondByte));
    }

    public String getNFormatted(int firstByte, int secondByte) {
        return String.format("0x%01X", getN(firstByte, secondByte));
    }

    public String getNNFormatted(int firstByte, int secondByte) {
        return String.format("0x%02X", secondByte);
    }

    public String getNNNFormatted(int firstByte, int secondByte) {
        return String.format("0x%03X", getNNN(firstByte, secondByte));
    }

}
