package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.emulators.CosmacVIPEmulator;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class CDP1802 implements Processor {

    private final CosmacVIPEmulator emulator;

    private final int[] scratchpadRegisters = new int[16];
    private final boolean[] externalFlagInputs = new boolean[4];
    private int accumulator;
    private int xRegister;
    private int type;
    private int n;
    private boolean dataFlag;
    private boolean outputLine;
    private boolean interruptEnable;
    private int programCounterRegisterIndex;

    // TODO: Handle interrupts
    // When an interrupt occurs: X -> T.1, P -> T.0, 1 -> P, 2 -> X, 0 -> IE
    private int temporary;

    // TODO: Handle DMA actions
    // DMA-IN: BUS -> M(R(0)); R(0) + 1
    // DMA-OUT: M(R(0)) -> BUS; R(0) + 1
    // DMA-IN has priority, then DMA-OUT, and then Interrupt.

    public CDP1802(CosmacVIPEmulator emulator) {
        this.emulator = emulator;
    }

    protected void setRegister(int register, int value) {
        this.scratchpadRegisters[register] = value & 0xFFFF;
    }

    protected void incrementRegister(int register) {
        this.setRegister(register, this.getRegister(register) + 1);
    }

    protected void decrementRegister(int register) {
        this.setRegister(register, this.getRegister(register) - 1);
    }

    protected void setRegisterHighOrder(int register, int value) {
        int masked = this.scratchpadRegisters[register] & 0x00FF;
        this.scratchpadRegisters[register] = ((value & 0xFF) << 8) | masked;
    }

    protected void setRegisterLowOrder(int register, int value) {
        int masked = this.scratchpadRegisters[register] & 0xFF00;
        this.scratchpadRegisters[register] = (value & 0xFF) | masked;
    }

    protected int getRegister(int register) {
        return this.scratchpadRegisters[register];
    }

    protected int getRegisterHighOrder(int register) {
        return (this.scratchpadRegisters[register] & 0xFF00) >> 8;
    }

    protected int getRegisterLowOrder(int register) {
        return this.scratchpadRegisters[register] & 0xFF;
    }

    protected void setAccumulator(int value) {
        this.accumulator = value & 0xFF;
    }

    protected int getAccumulator() {
        return this.accumulator;
    }

    protected void incrementCurrentProgramCounterRegister() {
        this.incrementRegister(this.programCounterRegisterIndex);
    }

    public int getCurrentProgramCounterRegister() {
        return this.getRegister(this.programCounterRegisterIndex);
    }

    protected void setXRegister(int value) {
        this.setRegister(this.xRegister, value);
    }

    public int getXRegister() {
        return this.getRegister(this.xRegister);
    }

    public void setExternalFlagInput(int flag, boolean value) {
        this.externalFlagInputs[flag] = !value;
    }

    protected void saveProgramCounterRegisterTemporary(int pc) {
        int mask = this.temporary & 0xF0;
        this.temporary = mask | pc;
    }

    protected void saveXRegisterTemporary(int x) {
        int mask = this.temporary & 0x0F;
        this.temporary = (x << 4) | mask;
    }

    protected int getTemporaryRegister() {
        return this.temporary;
    }

    @Override
    public int cycle() throws InvalidInstructionException {
        int opcode = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex));
        this.incrementCurrentProgramCounterRegister();
        this.type = (opcode & 0xF0) >> 4;
        this.n = opcode & 0xF;

        switch (type) {
            case 0x1 -> { // INC: INCREMENT REG N
                this.incrementRegister(n);
            }
            case 0x2 -> { // DEC: DECREMENT REG N
                this.decrementRegister(n);
            }
            case 0x6 -> {
                switch (this.n) {
                    case 0x0 -> { // IRX: INCREMENT REG X
                        this.incrementRegister(this.xRegister);
                    }
                    default -> {
                        if (n <= 7) { // OUT: OUTPUT
                            // TODO: HANDLE OUTPUT
                            // M(R(X)) -> BUS
                            // send lower order 3 bits of N from CPU to I/O system
                            // The three N lines are low at all times except when an Input/Output instruction is being executed (I = 6)
                            this.incrementRegister(this.xRegister);
                        } else if (n >= 0x9) { // INP: INPUT
                            // TODO: HANDLE INPUT
                            // The three bits of N are simultaneously sent from the CPU to the I/O system during execution
                            // BUS -> M(R(X)); BUS -> D
                        }
                    }
                }
            }
            case 0x8 -> { // GLO: GET LOW REG N
                this.setAccumulator(this.getRegisterLowOrder(n));
            }
            case 0xA -> { // PLO: PUT LOW REG N
                this.setRegisterLowOrder(n, this.getAccumulator());
            }
            case 0x9 -> { // GHI: GET HIGH REG N
                this.setAccumulator(this.getRegisterHighOrder(n));
            }
            case 0xB -> { // PHI: PUT HIGH REG N
                this.setRegisterHighOrder(n, this.getAccumulator());
            }
            case 0x0 -> {
                if (this.n != 0) { // LDN: LOAD VIA N
                    this.setAccumulator(this.emulator.getMemory().readByte(this.getRegister(n)));
                } else { // IDL: IDLE (WAIT FOR DMA OR INTERRUPT M(R(0)) -> BUS)
                    // TODO: HANDLE THIS APPROPRIATELY
                    //this.decrementRegister(this.programCounterRegisterIndex);
                }
            }
            case 0x4 -> { // LDA: LOAD ADVANCE
                this.setAccumulator(this.emulator.getMemory().readByte(this.getRegister(n)));
                this.incrementRegister(n);
            }
            case 0xF -> {
                switch (n) {
                    case 0x0 -> { // LDX: LOAD VIA X
                        this.setAccumulator(this.emulator.getMemory().readByte(this.getRegister(this.xRegister)));
                    }
                    case 0x8 -> { // LDI: LOAD IMMEDIATE
                        this.setAccumulator(this.getRegister(this.programCounterRegisterIndex));
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x1 -> { // OR: OR
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.xRegister)) | this.getAccumulator();
                        this.setAccumulator(value);
                    }
                    case 0x9 -> { // ORI: OR IMMEDIATE
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex)) | this.getAccumulator();
                        this.setAccumulator(value);
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x3 -> { // XOR: EXCLUSIVE-OR
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.xRegister)) ^ this.getAccumulator();
                        this.setAccumulator(value);
                    }
                    case 0xB -> { // XRI: EXCLUSIVE-OR IMMEDIATE
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex)) ^ this.getAccumulator();
                        this.setAccumulator(value);
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x2 -> { // AND: AND
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.xRegister)) & this.getAccumulator();
                        this.setAccumulator(value);
                    }
                    case 0xA -> { // ANI: AND IMMEDIATE
                        int value = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex)) & this.getAccumulator();
                        this.setAccumulator(value);
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x6 -> { // SHR: SHIFT RIGHT
                        int accumulator = this.getAccumulator();
                        boolean shiftedOut = (accumulator & 1) != 0;
                        this.setAccumulator(accumulator >>> 1);
                        this.dataFlag = shiftedOut;
                    }
                    case 0xE -> { // SHL: SHIFT LEFT
                         int accumulator = this.getAccumulator();
                         boolean shiftedOut = (accumulator & 0x80) != 0;
                         this.setAccumulator(accumulator << 1);
                         this.dataFlag = shiftedOut;
                    }
                    case 0x4 -> { // ADD: ADD
                        int accumulator = this.getAccumulator();
                        int addend = this.emulator.getMemory().readByte(this.getRegister(this.xRegister));
                        int sum = accumulator + addend;
                        this.setAccumulator(sum);
                        this.dataFlag = sum > 0xFF;
                    }
                    case 0xC -> { // ADI: ADD IMMEDIATE
                        int accumulator = this.getAccumulator();
                        int addend = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex));
                        int sum = accumulator + addend;
                        this.setAccumulator(sum);
                        this.dataFlag = sum > 0xFF;
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x5 -> { // SD: SUBTRACT D
                        int accumulator = this.getAccumulator();
                        int minuend = this.emulator.getMemory().readByte(this.getRegister(this.xRegister));
                        int difference = minuend - accumulator;
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                    }
                    case 0xD -> { // SDI: SUBTRACT D IMMEDIATE
                        int accumulator = this.getAccumulator();
                        int minuend = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex));
                        int difference = minuend - accumulator;
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x7 -> { // SM: SUBTRACT MEMORY
                        int accumulator = this.getAccumulator();
                        int subtrahend = this.emulator.getMemory().readByte(this.getXRegister());
                        int difference = accumulator - subtrahend;
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                    }
                    case 0xF -> { // SMI: SUBTRACT MEMORY IMMEDIATE
                        int accumulator = this.getAccumulator();
                        int subtrahend = this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister());
                        int difference = accumulator - subtrahend;
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                        this.incrementCurrentProgramCounterRegister();
                    }
                }
            }
            case 0x7 -> {
                switch (n) {
                    case 0x2 -> { // LDXA: LOAD VIA X AND ADVANCE
                        this.setAccumulator(this.emulator.getMemory().readByte(this.getRegister(this.xRegister)));
                        this.incrementRegister(this.xRegister);
                    }
                    case 0x3 -> { // STXD: STORE VIA X AND DECREMENT
                        this.emulator.getMemory().writeByte(this.getRegister(this.xRegister), this.getAccumulator());
                        this.decrementRegister(this.xRegister);
                    }
                    case 0x6 -> { // SHRC: SHIFT RIGHT WITH CARRY
                        int accumulator = this.getAccumulator();
                        boolean shiftedOut = (accumulator & 1) != 0;
                        int shifted = accumulator >>> 1;
                        if (this.dataFlag) {
                            shifted |= 0x80;
                        }
                        this.setAccumulator(shifted);
                        this.dataFlag = shiftedOut;
                    }
                    case 0xE -> { // SHLC: SHIFT LEFT WITH CARRY
                        int accumulator = this.getAccumulator();
                        boolean shiftedOut = (accumulator & 0x80) != 0;
                        int shifted = accumulator << 1;
                        if (this.dataFlag) {
                            shifted |= 1;
                        }
                        this.setAccumulator(shifted);
                        this.dataFlag = shiftedOut;
                    }
                    case 0x4 -> { // ADC: ADD WITH CARRY
                        int accumulator = this.getAccumulator();
                        int addend = this.emulator.getMemory().readByte(this.getRegister(this.xRegister));
                        int sum = accumulator + addend;
                        if (this.dataFlag) {
                            sum += 1;
                        }
                        this.setAccumulator(sum);
                        this.dataFlag = sum > 0xFF;
                    }
                    case 0xC -> { // ADCI: ADD WITH CARRY, IMMEDIATE
                        int accumulator = this.getAccumulator();
                        int addend = this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex));
                        int sum = accumulator + addend;
                        if (this.dataFlag) {
                            sum += 1;
                        }
                        this.setAccumulator(sum);
                        this.dataFlag = sum > 0xFF;
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x5 -> { // SDB: SUBTRACT D WITH BORROW
                        int accumulator = this.getAccumulator();
                        int minuend = this.emulator.getMemory().readByte(this.getRegister(this.xRegister));
                        int difference = minuend - accumulator - (!dataFlag ? 1 : 0);
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                    }
                    case 0xD -> { // SDBI: SUBTRACT D WITH BORROW, IMMEDIATE
                        int accumulator = this.getAccumulator();
                        int minuend = this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister());
                        int difference = minuend - accumulator - (!dataFlag ? 1 : 0);
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x7 -> { // SMB: SUBTRACT MEMORY WITH BORROW
                        int accumulator = this.getAccumulator();
                        int subtrahend = this.emulator.getMemory().readByte(this.getXRegister());
                        int difference = accumulator - subtrahend - (!dataFlag ? 1 : 0);
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                    }
                    case 0xF -> { // SMBI: SUBTRACT MEMORY WITH BORROW
                        int accumulator = this.getAccumulator();
                        int subtrahend = this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister());
                        int difference = accumulator - subtrahend - (!dataFlag ? 1 : 0);
                        this.setAccumulator(difference);
                        this.dataFlag = difference >= 0;
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0xB -> { // SEQ: SET Q
                        this.outputLine = true;
                    }
                    case 0xA -> { // REQ: RESET Q
                        this.outputLine = false;
                    }
                    case 0x8 -> { // SAV: SAVE
                        this.emulator.getMemory().writeByte(this.getXRegister(), this.getTemporaryRegister());
                    }
                    case 0x9 -> { // MARK: PUSH X, P TO STACK
                        this.saveProgramCounterRegisterTemporary(this.programCounterRegisterIndex);
                        this.saveXRegisterTemporary(this.xRegister);
                        this.emulator.getMemory().writeByte(this.getRegister(2), this.getTemporaryRegister());
                        this.setXRegister(this.programCounterRegisterIndex);
                        this.decrementRegister(2);
                    }
                    case 0x0 -> { // RET: RETURN
                        int memoryByte = this.emulator.getMemory().readByte(this.getXRegister());
                        this.programCounterRegisterIndex = memoryByte & 0xF;
                        this.setXRegister((memoryByte >>> 4) & 0xF);
                        this.incrementRegister(this.xRegister);
                        this.interruptEnable = true;
                    }
                    case 0x1 -> { // DIS: DISABLE
                        int memoryByte = this.emulator.getMemory().readByte(this.getXRegister());
                        this.programCounterRegisterIndex = memoryByte & 0xF;
                        this.setXRegister((memoryByte >>> 4) & 0xF);
                        this.incrementRegister(this.xRegister);
                        this.interruptEnable = false;
                    }
                }
            }
            case 0x5 -> { // STR: STORE VIA N
                this.emulator.getMemory().writeByte(this.getRegister(n), this.getAccumulator());
            }
            case 0x3 -> {
                switch (n) {
                    case 0x0 -> { // BR: UNCONDITIONAL SHORT BRANCH
                        this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                    }
                    case 0x8 -> { // NBR: NO SHORT BRANCH
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x2 -> { // BZ: SHORT BRANCH IF D = 0
                        if (this.getAccumulator() == 0) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xA -> { // BNZ: SHORT BRANCH IF D NOT 0
                        if (this.getAccumulator() != 0) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x3 -> { // BDF: SHORT BRANCH IF DF = 1
                        if (this.dataFlag) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xB -> { // BNF: SHORT BRANCH IF DF = 0
                        if (!this.dataFlag) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x1 -> { // BQ: SHORT BRANCH IF Q = 1
                        if (this.outputLine) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x9 -> { // BNQ: SHORT BRANCH IF Q = 0
                        if (!this.outputLine) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x4 -> { // B1: SHORT BRANCH IF EF1 = 1
                        if (this.externalFlagInputs[0]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xC -> { // BN1: SHORT BRANCH IF EF1 = 0
                        if (!this.externalFlagInputs[0]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x5 -> { // B2: SHORT BRANCH IF EF2 = 1
                        if (this.externalFlagInputs[1]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xD -> { // BN2: SHORT BRANCH IF EF2 = 0
                        if (!this.externalFlagInputs[1]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x6 -> { // B3: SHORT BRANCH IF EF3 = 1
                        if (this.externalFlagInputs[2]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xE -> { // BN3: SHORT BRANCH IF EF3 = 0
                        if (!this.externalFlagInputs[2]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x7 -> { // B4: SHORT BRANCH IF EF4 = 1
                        if (this.externalFlagInputs[3]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xF -> { // BN4: SHORT BRANCH IF EF4 = 0
                        if (!this.externalFlagInputs[3]) {
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                }
            }
            case 0xC -> {
                switch (n) {
                    case 0x0 -> { // LBR: LONG BRANCH
                        this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                        this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                    }
                    case 0x8 -> { // NO LONG BRANCH
                        this.incrementCurrentProgramCounterRegister();
                        this.incrementCurrentProgramCounterRegister();
                    }
                    case 0x2 -> { // LBZ: LONG BRANCH IF D = 0
                        if (this.getAccumulator() == 0) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xA -> { // LBNZ: LONG BRANCH IF D NOT 0
                        if (this.getAccumulator() != 0) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x3 -> { // LBDF: LONG BRANCH IF DF = 1
                        if (this.dataFlag) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xB -> { // LBNF: LONG BRANCH IF DF = 0
                        if (!this.dataFlag) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x1 -> { // LBQ: LONG BRANCH IF Q = 1
                        if (this.outputLine) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x9 -> { // LBNQ: LONG BRANCH IF Q = 0
                        if (!this.outputLine) {
                            this.setRegisterHighOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getCurrentProgramCounterRegister()));
                            this.setRegisterLowOrder(this.programCounterRegisterIndex, this.emulator.getMemory().readByte(this.getRegister(this.programCounterRegisterIndex + 1)));
                        } else {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xE -> { // LSZ: LONG SKIP IF D = 0
                        if (this.getAccumulator() == 0) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x6 -> { // LSNZ: LONG SKIP IF D NOT 0
                        if (this.getAccumulator() != 0) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xF -> { // LSDF: LONG SKIP IF DF = 1
                        if (this.dataFlag) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x7 -> { // LSNF: LONG SKIP IF DF = 0
                        if (!this.dataFlag) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xD -> { // LSQ: LONG SKIP IF Q = 1
                        if (this.outputLine) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x5 -> { // LSNQ: LONG SKIP IF Q = 0
                        if (!this.outputLine) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0xC -> { // LSIE
                        if (this.interruptEnable) {
                            this.incrementCurrentProgramCounterRegister();
                            this.incrementCurrentProgramCounterRegister();
                        }
                    }
                    case 0x4 -> { // NOP: NO OPERATION

                    }
                }
            }
            case 0xD -> { // SEP: SET P
                this.programCounterRegisterIndex = n & 0xF;
            }
            case 0xE -> { // SEX: SET X
                this.xRegister = n & 0xF;
            }
        }


        return 0;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
