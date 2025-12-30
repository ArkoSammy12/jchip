package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.*;

public class Chip8Disassembler<E extends Chip8Emulator> extends AbstractDisassembler<E> {

    public Chip8Disassembler(E emulator) {
        super(emulator);
    }

    @Override
    protected int getLengthForInstructionAt(int address) {
        return 2;
    }

    @Override
    protected int getBytecodeForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        return (bus.getByte(address) << 8) | bus.getByte(address + 1);
    }

    @Override
    protected String getTextForInstructionAt(int address) {
        Bus bus = this.emulator.getBus();
        int firstByte = bus.getByte(address);
        int secondByte = bus.getByte(address + 1);
        return switch (firstByte >>> 4) {
            case 0x0 -> switch (getNNN(firstByte, secondByte)) {
                case 0x0E0 -> "clear";
                case 0x0EE -> "return";
                default -> "invalid";
            };
            case 0x1 -> "jump " + getNNNFormatted(firstByte, secondByte);
            case 0x2 -> ":call " + getNNNFormatted(firstByte, secondByte);
            case 0x3 -> "if v" + getXFormatted(firstByte, secondByte) + " != " + getNNFormatted(firstByte, secondByte) + " then";
            case 0x4 -> "if v" + getXFormatted(firstByte, secondByte) + " == " + getNNFormatted(firstByte, secondByte) + " then";
            case 0x5 -> {
                if (getN(firstByte, secondByte) == 0x0) {
                    yield "if v" + getXFormatted(firstByte, secondByte) + " != v" + getYFormatted(firstByte, secondByte) + " then";
                } else {
                    yield "invalid";
                }
            }
            case 0x6 -> "v" + getXFormatted(firstByte, secondByte) + " := " + getNNFormatted(firstByte, secondByte);
            case 0x7 -> "v" + getXFormatted(firstByte, secondByte) + " += " + getNNFormatted(firstByte, secondByte);
            case 0x8 -> switch (getN(firstByte, secondByte)) {
                case 0x0 -> "v" + getXFormatted(firstByte, secondByte) + " := v" + getYFormatted(firstByte, secondByte);
                case 0x1 -> "v" + getXFormatted(firstByte, secondByte) + " |= v" + getYFormatted(firstByte, secondByte);
                case 0x2 -> "v" + getXFormatted(firstByte, secondByte) + " &= v" + getYFormatted(firstByte, secondByte);
                case 0x3 -> "v" + getXFormatted(firstByte, secondByte) + " ^= v" + getYFormatted(firstByte, secondByte);
                case 0x4 -> "v" + getXFormatted(firstByte, secondByte) + " += v" + getYFormatted(firstByte, secondByte);
                case 0x5 -> "v" + getXFormatted(firstByte, secondByte) + " -= v" + getYFormatted(firstByte, secondByte);
                case 0x6 -> "v" + getXFormatted(firstByte, secondByte) + " >>= v" + getYFormatted(firstByte, secondByte);
                case 0x7 -> "v" + getXFormatted(firstByte, secondByte) + " =- v" + getYFormatted(firstByte, secondByte);
                case 0xE -> "v" + getXFormatted(firstByte, secondByte) + " <<= v" + getYFormatted(firstByte, secondByte);
                default -> "invalid";
            };
            case 0x9 -> {
                if (getN(firstByte, secondByte) == 0x0) {
                    yield "if v" + getXFormatted(firstByte, secondByte) + " == v" + getYFormatted(firstByte, secondByte) + " then";
                } else {
                    yield "invalid";
                }
            }
            case 0xA -> "i := " + getNNNFormatted(firstByte, secondByte);
            case 0xB -> {
                if (emulator.getEmulatorSettings().doJumpWithVX()) {
                    yield "jump0 " + getNNNFormatted(firstByte, secondByte) + " + v" + getXFormatted(firstByte, secondByte);
                } else {
                    yield "jump0 " + getNNNFormatted(firstByte, secondByte);
                }
            }
            case 0xC -> "v" + getXFormatted(firstByte, secondByte) + " := random " + getNNFormatted(firstByte, secondByte);
            case 0xD -> "sprite v" + getXFormatted(firstByte, secondByte) + " v" + getYFormatted(firstByte, secondByte) + " " + getNFormatted(firstByte, secondByte);
            case 0xE -> switch (secondByte) {
                case 0x9E -> "if v" + getXFormatted(firstByte, secondByte) + " -key then";
                case 0xA1 -> "if v" + getXFormatted(firstByte, secondByte) + " key then";
                default -> "invalid";
            };
            case 0xF -> switch (secondByte) {
                case 0x07 -> "v" + getXFormatted(firstByte, secondByte) + " := delay";
                case 0x0A -> "v" + getXFormatted(firstByte, secondByte) + " := key";
                case 0x15 -> "delay := v" + getXFormatted(firstByte, secondByte);
                case 0x18 -> "buzzer := v" + getXFormatted(firstByte, secondByte);
                case 0x1E -> "i += v" + getXFormatted(firstByte, secondByte);
                case 0x29 -> "i := hex v" + getXFormatted(firstByte, secondByte);
                case 0x33 -> "bcd v" + getXFormatted(firstByte, secondByte);
                case 0x55 -> "save v" + getXFormatted(firstByte, secondByte);
                case 0x65 -> "load v" + getXFormatted(firstByte, secondByte);
                default -> "invalid";
            };
            default -> "invalid";
        };
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
