package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;

import java.util.Arrays;

import static io.github.arkosammy12.jchip.cpu.CDP1802.State.*;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.HANDLED;
import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isHandled;

public class CDP1802 implements Processor {

    private final CosmacVipEmulator emulator;
    private State currentState = State.S1_RESET;
    private boolean longInstruction = false;
    private boolean idling = false;
    private long machineCycles;

    private int accumulator; // D
    private boolean dataFlagRegister; // DF
    private int auxiliaryHoldingRegister; // B
    private final int[] registers = new int[16]; // R(N | X | P)
    private int programCounterIndex; // P
    private int dataPointerIndex; // X
    private int lowOrderInstructionDigit; // N
    private int highOrderInstructionDigit; // I
    private int temporaryRegister; // T
    private boolean interruptEnable; // IE
    private boolean outputFlipFlop; // Q

    private final boolean[] externalFlagInputs = new boolean[4];

    public CDP1802(CosmacVipEmulator emulator) {
        this.emulator = emulator;
    }

    public State getCurrentState() {
        return this.currentState;
    }

    protected void setD(int value) {
        this.accumulator = value & 0xFF;
    }

    protected void setDF(boolean value) {
        this.dataFlagRegister = value;
    }

    protected void setB(int value) {
        this.auxiliaryHoldingRegister = value & 0xFF;
    }

    protected void setRegister(int index, int value) {
        this.registers[index] = value & 0xFFFF;
    }

    protected void setRegisterLowOrder(int index, int value) {
        this.registers[index] = (this.getRegisterHighOrder(index) << 8) | (value & 0xFF);
    }

    protected void setRegisterHighOrder(int index, int value) {
        this.registers[index] = ((value & 0xFF) << 8) | this.getRegisterLowOrder(index);
    }

    protected void setP(int value) {
        this.programCounterIndex = value & 0xF;
    }

    protected void setX(int value) {
        this.dataPointerIndex = value & 0xF;
    }

    protected void setN(int value) {
        this.lowOrderInstructionDigit = value & 0xF;
    }

    protected void setI(int value) {
        this.highOrderInstructionDigit = value & 0xF;
    }

    protected void setT(int value) {
        this.temporaryRegister = value & 0xFF;
    }

    protected void setInterruptEnable(boolean value) {
        this.interruptEnable = value;
    }

    protected void setQ(boolean value) {
        this.outputFlipFlop = value;
    }

    public void setEF(int index, boolean value) {
        this.externalFlagInputs[index] = value;
    }

    public int getD() {
        return this.accumulator;
    }

    public boolean getDF() {
        return this.dataFlagRegister;
    }

    public int getB() {
        return this.auxiliaryHoldingRegister;
    }

    public int getRegister(int index) {
        return this.registers[index];
    }

    public int getRegisterLowOrder(int index) {
        return this.registers[index] & 0xFF;
    }

    public int getRegisterHighOrder(int index) {
        return (this.registers[index] & 0xFF00) >>> 8;
    }

    public int getP() {
        return this.programCounterIndex;
    }

    public int getX() {
        return this.dataPointerIndex;
    }

    public int getN() {
        return this.lowOrderInstructionDigit;
    }

    public int getI() {
        return this.highOrderInstructionDigit;
    }

    public int getT() {
        return this.temporaryRegister;
    }

    public boolean getInterruptEnable() {
        return this.interruptEnable;
    }

    public boolean getQ() {
        return this.outputFlipFlop;
    }

    public boolean getEF(int index) {
        return this.externalFlagInputs[index];
    }

    public long getMachineCycles() {
        return this.machineCycles;
    }

    @Override
    public int cycle() {
        int flags = switch (currentState) {
            case S1_RESET -> onReset();
            case S1_INIT -> onInit();
            case S0_FETCH -> onFetch();
            case S1_EXECUTE -> onExecute();
            case S2_DMA_IN -> onDmaIn();
            case S2_DMA_OUT -> onDmaOut();
            case S3_INTERRUPT -> onInterrupt();
        };
        if (!isHandled(flags)) {
            throw new InvalidInstructionException((getI() << 4) | getN(), this.emulator.getChip8Variant());
        }
        this.machineCycles++;
        return flags;
    }

    public void nextState() {
        this.currentState = switch (currentState) {
            case S1_RESET -> S1_INIT;
            case S1_INIT, S3_INTERRUPT -> switch (this.emulator.getDmaStatus()) {
                case NONE ->  S0_FETCH;
                case IN -> S2_DMA_IN;
                case OUT -> S2_DMA_OUT;
            };
            case S0_FETCH -> S1_EXECUTE;
            case S1_EXECUTE -> {
                if (this.longInstruction) {
                    yield S1_EXECUTE;
                } else {
                    yield switch (this.emulator.getDmaStatus()) {
                        case NONE -> {
                            if (this.emulator.anyInterrupting() && getInterruptEnable()) {
                                this.idling = false;
                                yield S3_INTERRUPT;
                            } else if (this.idling) {
                                yield S1_EXECUTE;
                            } else {
                                yield S0_FETCH;
                            }
                        }
                        case IN -> {
                            this.idling = false;
                            yield S2_DMA_IN;
                        }
                        case OUT -> {
                            this.idling = false;
                            yield S2_DMA_OUT;
                        }
                    };
                }
            }
            case S2_DMA_IN, S2_DMA_OUT -> switch (this.emulator.getDmaStatus()) {
                case NONE -> {
                    if (this.emulator.anyInterrupting() && getInterruptEnable()) {
                        yield S3_INTERRUPT;
                    } else {
                        yield S0_FETCH;
                    }
                }
                case IN -> S2_DMA_IN;
                case OUT -> S2_DMA_OUT;
            };
        };
    }

    private int onReset() {
        setI(0);
        setN(0);
        setX(0);
        setP(0);
        setQ(false);
        setInterruptEnable(true);
        return HANDLED;
        // reset data bus
    }

    private int onInit() {
        Arrays.fill(this.registers, 0);
        return HANDLED;
    }

    private int onFetch() {
        int opcode = this.emulator.getMemory().readByte(getRegister(getP()));
        setRegister(getP(), getRegister(getP()) + 1);
        setI((opcode & 0xF0) >>> 4);
        setN(opcode & 0x0F);
        //this.logTrace();
        return HANDLED;
    }

    private int onDmaIn() {
        this.emulator.getMemory().writeByte(getRegister(0), this.emulator.dispatchDmaIn());
        setRegister(0, getRegister(0) + 1);
        return HANDLED;
    }

    private int onDmaOut() {
        this.emulator.dispatchDmaOut(this.emulator.getMemory().readByte(getRegister(0)));
        setRegister(0, getRegister(0) + 1);
        return HANDLED;
    }

    private int onInterrupt() {
        setT(getX() << 4 | getP());
        setInterruptEnable(false);
        setP(1);
        setX(2);
        return HANDLED;
    }

    /*
    private void logTrace() {
        String pc = String.format("[%04X]", getRegister(getP()));
        String ins = String.format(" %02X |", getI() << 4 | getN());
        String df = getDF() ? " DF:1" : " DF:0";
        String d = String.format(" D: %02X ", getD());

        StringBuilder regs = new StringBuilder(" ");
        for (int i = 0; i < 16; i++) {
            regs.append(String.format("R%01X:%04X ", i, getRegister(i)));
        }

        Logger.info(pc + ins + regs + " | " + d + df);
    }
     */

    private int onExecute() {
        return switch (getI()) {
            case 0x0 -> {
                if (getN() != 0) { // 0N: LDN | M(R(N)) → D; FOR N not 0
                    setD(this.emulator.getMemory().readByte(getRegister(getN())));
                } else { // 00: IDL. IDLE.
                    this.idling = true;
                    this.emulator.getMemory().readByte(getRegister(0)); // Dummy read for accurate bus activity
                }
                yield HANDLED;
            }
            case 0x1 -> { // 1N: INC | R(N) + 1 → R(N)
                setRegister(getN(), getRegister(getN()) + 1);
                yield HANDLED;
            }
            case 0x2 -> { // 2N: DEC | R(N) - 1 → R(N)
                setRegister(getN(), getRegister(getN()) - 1);
                yield HANDLED;
            }
            case 0x3 -> switch (getN()) {
                case 0x0 -> { // 30: BR | M(R(P)) -> R(P).0
                    setRegisterLowOrder(getP(), this.emulator.getMemory().readByte(getRegister(getP())));
                    yield HANDLED;
                }
                case 0x1 -> { // 31: BQ | IF Q = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getQ()) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x2 -> { // 32: BZ | IF D = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getD() == 0) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x3 -> { // 33: BDF | IF DF = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getDF()) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x4 -> { // 34: B1 | IF EF1 = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getEF(0)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x5 -> { // 35: B2 | IF EF2 = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getEF(1)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x6 -> { // 36: B3 | IF EF3 = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getEF(2)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x7 -> { // 37: B4 | IF EF4 = 1, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getEF(3)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x8 -> { // 38: NBR | R(P) + 1 → R(P)
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0x9 -> { // 39: BNQ | IF Q = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getQ()) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xA -> { // 3A: BNZ | IF D NOT 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (getD() != 0) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xB -> { // 3B: BNF | IF DF = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getDF()) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xC -> { // 3C: BN1 | IF EF1 = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getEF(0)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xD -> { // 3D: BN2 | IF EF2 = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getEF(1)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xE -> { // 3E: BN3 | IF EF3 = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getEF(2)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xF -> { // 3F: BN4 | IF EF4 = 0, M(R(P)) → R(P).0, ELSE R(P) + 1 → R(P)
                    int value = this.emulator.getMemory().readByte(getRegister(getP()));
                    if (!getEF(3)) {
                        setRegisterLowOrder(getP(), value);
                    } else {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                default -> 0;
            };
            case 0x4 -> { // 4N: LDA | M(R(N)) → D; R(N) + 1 → R(N)
                setD(this.emulator.getMemory().readByte(getRegister(getN())));
                setRegister(getN(), getRegister(getN()) + 1);
                yield HANDLED;
            }
            case 0x5 -> { // 5N: STR | D → M(R(N))
                this.emulator.getMemory().writeByte(getRegister(getN()), getD());
                yield HANDLED;
            }
            case 0x6 -> switch (getN()) {
                case 0x0 -> { // 60: IRX | R(X) + 1 → R(X)
                    this.emulator.getMemory().readByte(getRegister(getX())); // Dummy read for accurate bus activity
                    setRegister(getX(), getRegister(getX()) + 1);
                    yield HANDLED;
                }
                default -> {
                    int N = getN();
                    if (N >= 0x1 && N <= 0x7) { // 6N: OUT | M(R(X)) → BUS; R(X) + 1 → R(X)
                        int NX = N & 7;
                        this.emulator.dispatchOutput(NX, this.emulator.getMemory().readByte(getRegister(getX())));
                        setRegister(getX(), getRegister(getX()) + 1);
                    } else if (N >= 0x9 && N <= 0xF) { // 6N: INP | BUS → M(R(X)), D
                        int NX = N & 7;
                        int input = this.emulator.dispatchInput(NX);
                        this.emulator.getMemory().writeByte(getRegister(getX()), input);
                        setD(input);
                        yield HANDLED;
                    }
                    yield HANDLED;
                }
            };
            case 0x7 -> switch (getN()) {
                case 0x0 -> { // 70: RET | M(R(X)) → (X, P); R(X) + 1 → R(X), 1 → IE
                    int value = this.emulator.getMemory().readByte(getRegister(getX()));
                    setRegister(getX(), getRegister(getX()) + 1);
                    setX((value & 0xF0) >>> 4);
                    setP(value & 0x0F);
                    setInterruptEnable(true);
                    yield HANDLED;
                }
                case 0x1 -> { // 71: DIS | M(R(X)) → (X, P); R(X) + 1 → R(X), 0 → IE
                    int value = this.emulator.getMemory().readByte(getRegister(getX()));
                    setRegister(getX(), getRegister(getX()) + 1);
                    setX((value & 0xF0) >>> 4);
                    setP(value & 0x0F);
                    setInterruptEnable(false);
                    yield HANDLED;
                }
                case 0x2 -> { // 72: LDXA | M(R(X)) → D; R(X) + 1 → R(X)
                    setD(this.emulator.getMemory().readByte(getRegister(getX())));
                    setRegister(getX(), getRegister(getX()) + 1);
                    yield HANDLED;
                }
                case 0x3 -> { // 73: STXD | D → M(R(X)); R(X) - 1 → R(X)
                    this.emulator.getMemory().writeByte(getRegister(getX()), getD());
                    setRegister(getX(), getRegister(getX()) - 1);
                    yield HANDLED;
                }
                case 0x4 -> { // 74: ADC | M(R(X)) + D + DF → DF, D
                    int result = this.emulator.getMemory().readByte(getRegister(getX())) + getD() + (getDF() ? 1 : 0);
                    setD(result);
                    setDF(result > 0xFF);
                    yield HANDLED;
                }
                case 0x5 -> { // 75: SBD | M(R(X)) - D - (NOT DF) → DF, D
                    int result = this.emulator.getMemory().readByte(getRegister(getX())) - getD() - (getDF() ? 0 : 1);
                    setD(result);
                    setDF(result >= 0);
                    yield HANDLED;
                }
                case 0x6 -> { // 76: SHRC | SHIFT D RIGHT, LSB(D) → DF, DF → MSB(D)
                    boolean DF = getDF();
                    boolean shiftedOut = (getD() & 1) != 0;
                    setD((DF ? 0x80 : 0x00) | (getD() >>> 1));
                    setDF(shiftedOut);
                    yield HANDLED;
                }
                case 0x7 -> { // 77: SMB | D - M(R(X)) - (NOT DF) → DF, D
                    int result = getD() - this.emulator.getMemory().readByte(getRegister(getX())) - (getDF() ? 0 : 1);
                    setD(result);
                    setDF(result >= 0);
                    yield HANDLED;
                }
                case 0x8 -> { // 78: SAV | T → M(R(X))
                    this.emulator.getMemory().writeByte(getRegister(getX()), getT());
                    yield HANDLED;
                }
                case 0x9 -> { // 79: MARK | (X, P) → T; (X, P) → M(R(2)), THEN P → X; R(2) - 1 → R(2)
                    int value = (getX() << 4) | getP();
                    setT(value);
                    this.emulator.getMemory().writeByte(getRegister(2), value);
                    setX(getP());
                    setRegister(2, getRegister(2) - 1);
                    yield HANDLED;
                }
                case 0xA -> { // 7A: REQ | 0 → Q
                    setQ(false);
                    yield HANDLED;
                }
                case 0xB -> { // 7B: SEQ | 1 → Q
                    setQ(true);
                    yield HANDLED;
                }
                case 0xC -> { // 7C: ADCI | M(R(P)) + D + DF → DF, D; R(P) + 1 → R(P)
                    int result = this.emulator.getMemory().readByte(getRegister(getP())) + getD() + (getDF() ? 1 : 0);
                    setD(result);
                    setDF(result > 0xFF);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xD -> { // 7D: SBDI | M(R(P)) - D - (Not DF) → DF, D; R(P) + 1 → R(P)
                    int result = this.emulator.getMemory().readByte(getRegister(getP())) - getD() - (getDF() ? 0 : 1);
                    setD(result);
                    setDF(result >= 0);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xE -> { // 7E: SHLC | SHIFT D LEFT, MSB(D) → DF, DF → LSB(D)
                    boolean DF = getDF();
                    boolean shiftedOut = (getD() & 0x80) != 0;
                    setD((getD() << 1) | (DF ? 1 : 0));
                    setDF(shiftedOut);
                    yield HANDLED;
                }
                case 0xF -> { // 7F: SMBI | D - M(R(P)) - (NOT DF) → DF, D; R(P) + 1 → R(P)
                    int result = getD() - this.emulator.getMemory().readByte(getRegister(getP())) - (getDF() ? 0 : 1);
                    setD(result);
                    setDF(result >= 0);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                default -> 0;
            };
            case 0x8 -> { // 8N: GLO | R(N).0 → D
                // TODO: Place RN.0 on data bus
                setD(getRegisterLowOrder(getN()));
                yield HANDLED;
            }
            case 0x9 -> { // 9N: GHI | R(N).1 → D
                // TODO: Place RN.1 on data bus
                setD(getRegisterHighOrder(getN()));
                yield HANDLED;
            }
            case 0xA -> { // AN: PLO | D → R(N).0
                // TODO: Place D on data bus
                setRegisterLowOrder(getN(), getD());
                yield HANDLED;
            }
            case 0xB -> { // BN: PHI | D → R(N).1
                // TODO: Place D on data bus
                setRegisterHighOrder(getN(), getD());
                yield HANDLED;
            }
            case 0xC -> switch (getN()) {
                case 0x0 -> { // C0: LBR | M(R(P)) → R(P). 1, M(R(P) + 1) → R(P).0
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        setRegisterHighOrder(getP(), getB());
                        setRegisterLowOrder(getP(), this.emulator.getMemory().readByte(getRegister(getP())));
                    }
                    yield HANDLED;
                }
                case 0x1 -> { // C1: LBQ | IF Q = 1, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE R(P) + 2 → R(P)
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (getQ()) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0x2 -> { // C2: LBZ | IF D = 0, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE R(P) + 2 → R(P)
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (getD() == 0) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0x3 -> { // C3: LBDF | IF DF = 1, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE R(P) + 2 → R(P)
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (getDF()) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0x4 -> { // C4: NOP | NO OPERATION
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    yield HANDLED;
                }
                case 0x5 -> { // C5: LSNQ | IF Q = 0, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (!getQ()) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x6 -> { // LSNZ | IF D Not 0, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (getD() != 0) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x7 -> { // C7: LSNF | IF DF = 0, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (!getDF()) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0x8 -> { // C8: NLBR | R(P) + 2 → R(P)
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0x9 -> { // C9: LBNQ | IF Q = 0, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE R(P) + 2 → R(P)
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (!getQ()) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0xA -> { // CA: LBNZ | IF D Not 0, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE R(P) + 2 → R(P)
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (getD() != 0) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0xB -> { // CB: LBNF - IF DF = 0, M(R(P)) → R(P).1, M(R(P) + 1) → R(P).0, ELSE
                    if (!this.longInstruction) {
                        this.longInstruction = true;
                        setB(this.emulator.getMemory().readByte(getRegister(getP())));
                        setRegister(getP(), getRegister(getP()) + 1);
                    } else {
                        this.longInstruction = false;
                        int lowByte = this.emulator.getMemory().readByte(getRegister(getP()));
                        if (!getDF()) {
                            setRegisterHighOrder(getP(), getB());
                            setRegisterLowOrder(getP(), lowByte);
                        } else {
                            setRegister(getP(), getRegister(getP()) + 1);
                        }
                    }
                    yield HANDLED;
                }
                case 0xC -> { // CC: LSIE | IF IE = 1, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (getInterruptEnable()) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xD -> { // CD: LSQ | IF Q = 1, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (getQ()) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xE -> { // CE: LSZ | IF D = 0, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (getD() == 0) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                case 0xF -> { // CF: LSDF | IF DF = 1, R(P) + 2 → R(P), ELSE CONTINUE
                    this.longInstruction = !this.longInstruction;
                    this.emulator.getMemory().readByte(getRegister(getP())); // Dummy read for accurate bus activity
                    if (getDF()) {
                        setRegister(getP(), getRegister(getP()) + 1);
                    }
                    yield HANDLED;
                }
                default -> 0;
            };
            case 0xD -> { // DN: SEP | N → P
                // TODO: Place NN on the data bus
                setP(getN());
                yield HANDLED;
            }
            case 0xE -> { // EN: SEX | N → X
                // TODO: Place NN on the data bus
                setX(getN());
                yield HANDLED;
            }
            case 0xF -> switch (getN()) {
                case 0x0 -> { // F0: LDX | M(R(X)) → D
                    setD(this.emulator.getMemory().readByte(getRegister(getX())));
                    yield HANDLED;
                }
                case 0x1 -> { // F1: OR | M(R(X)) OR D → D
                    setD(this.emulator.getMemory().readByte(getRegister(getX())) | getD());
                    yield HANDLED;
                }
                case 0x2 -> { // F2: AND | M(R(X)) AND D → D
                    setD(this.emulator.getMemory().readByte(getRegister(getX())) & getD());
                    yield HANDLED;
                }
                case 0x3 -> { // F3: XOR | M(R(X)) XOR D → D
                    setD(this.emulator.getMemory().readByte(getRegister(getX())) ^ getD());
                    yield HANDLED;
                }
                case 0x4 -> { // F4: ADD | M(R(X)) + D → DF, D
                    int result = this.emulator.getMemory().readByte(getRegister(getX())) + getD();
                    setD(result);
                    setDF(result > 0xFF);
                    yield HANDLED;
                }
                case 0x5 -> { // F5: SD | M(R(X)) - D → DF, D
                    int result = this.emulator.getMemory().readByte(getRegister(getX())) - getD();
                    setD(result);
                    setDF(result >= 0);
                    yield HANDLED;
                }
                case 0x6 -> { // F6: SHR | SHIFT D RIGHT, LSB(D) → DF, 0 → MSB(D)
                    boolean shiftedOut = (getD() & 1) != 0;
                    setD(getD() >>> 1);
                    setDF(shiftedOut);
                    yield HANDLED;
                }
                case 0x7 -> { // F7: SM | D - M(R(X)) → DF, D
                    int result = getD() - this.emulator.getMemory().readByte(getRegister(getX()));
                    setD(result);
                    setDF(result >= 0);
                    yield HANDLED;
                }
                case 0x8 -> { // F8: LDI | M(R(P)) → D; R(P) + 1 → R(P)
                    setD(this.emulator.getMemory().readByte(getRegister(getP())));
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0x9 -> { // F9: ORI | M(R(P)) OR D → D; R(P) + 1 → R(P)
                    setD(this.emulator.getMemory().readByte(getRegister(getP())) | getD());
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xA -> { // FA: ANI | M(R(P)) AND D → D; R(P) + 1 → R(P)
                    setD(this.emulator.getMemory().readByte(getRegister(getP())) & getD());
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xB -> { // FB: XRI | M(R(P)) XOR D → D; R(P) + 1 → R(P)
                    setD(this.emulator.getMemory().readByte(getRegister(getP())) ^ getD());
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xC -> { // FC: ADI | M(R(P)) + D → DF, D; R(P) + 1 → R(P)
                    int result = this.emulator.getMemory().readByte(getRegister(getP())) + getD();
                    setD(result);
                    setDF(result > 0xFF);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xD -> { // FD: SDI | M(R(P)) - D → DF, D; R(P) + 1 → R(P)
                    int result = this.emulator.getMemory().readByte(getRegister(getP())) - getD();
                    setD(result);
                    setDF(result >= 0);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                case 0xE -> { // FE: SHL | SHIFT D LEFT, MSB(D) → DF, 0 → LSB(D)
                    boolean shiftedOut = (getD() & 0x80) != 0;
                    setD(getD() << 1);
                    setDF(shiftedOut);
                    yield HANDLED;
                }
                case 0xF -> { // FF: SMI | D - M(R(P)) → DF, D; R(P) + 1 → R(P)
                    int result = getD() - this.emulator.getMemory().readByte(getRegister(getP()));
                    setD(result);
                    setDF(result >= 0);
                    setRegister(getP(), getRegister(getP()) + 1);
                    yield HANDLED;
                }
                default -> 0;
            };
            default -> 0;
        };
    }

    public enum State {
        S0_FETCH,
        S1_RESET,
        S1_INIT,
        S1_EXECUTE,
        S2_DMA_IN,
        S2_DMA_OUT,
        S3_INTERRUPT;

        public boolean isS0Fetch() {
            return this == S0_FETCH;
        }

        public boolean isS1Execute() {
            return this == S1_RESET || this == S1_INIT || this == S1_EXECUTE;
        }

        public boolean isS2Dma() {
            return this == S2_DMA_IN || this == S2_DMA_OUT;
        }

        public boolean isS3Interrupt() {
            return this == S3_INTERRUPT;
        }

        public boolean getSC0() {
            return isS1Execute() || isS3Interrupt();
        }

        public boolean getSC1() {
            return isS2Dma() || isS3Interrupt();
        }

    }

}
