package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jctools.queues.MpscArrayQueue;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;

public abstract class AbstractDisassembler<E extends Emulator> implements Disassembler {

    protected final E emulator;
    private final AtomicReference<IntSupplier> currentAddressSupplier = new AtomicReference<>(null);

    private final ConcurrentSkipListMap<Integer, Entry> entries = new ConcurrentSkipListMap<>();
    private final IntArrayList addressOrdinalList = new IntArrayList();

    private final MpscArrayQueue<Integer> addressQueue = new MpscArrayQueue<>(10000);
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final Thread disassemblerThread;

    private int currentStaticDisassemblerPointer = 0;
    private boolean staticDisassemblyFinished = false;

    public AbstractDisassembler(E emulator) {
        this.emulator = emulator;
        this.disassemblerThread = new Thread(this::disassemblerLoop, "jchip-disassembler-thread");
        this.disassemblerThread.setDaemon(true);
        this.disassemblerThread.start();
    }

    @Override
    @Nullable
    public Disassembler.Entry getEntry(int ordinal) {
        if (ordinal < 0 || ordinal >= this.addressOrdinalList.size()) {
            return null;
        }
        Entry entry = this.entries.get(this.addressOrdinalList.get(ordinal));
        if (entry == null) {
            return null;
        }
        this.validateEntry(entry);
        return entry;
    }

    @Override
    public int getSize() {
        return this.addressOrdinalList.size();
    }

    @Override
    public int getOrdinalForAddress(int address) {
        return this.addressOrdinalList.indexOf(address);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled.get();
    }

    @Override
    public Optional<IntSupplier> getCurrentAddressSupplier() {
        return Optional.ofNullable(this.currentAddressSupplier.get());
    }

    @Override
    public void close() throws IOException {
        this.running.set(false);
        this.disassemblerThread.interrupt();
        try {
            this.disassemblerThread.join();
        } catch (InterruptedException _) {}
    }


    public void disassemble(int address) {
        if (!this.isEnabled()) {
            return;
        }
        this.addressQueue.offer(address);
    }

    public void disassembleRange(int address, int range) {
        if (!this.isEnabled()) {
            return;
        }
        int currentAddress = address;
        for (int i = 0; i < range; i++) {
            this.addressQueue.offer(currentAddress);
            currentAddress += this.getLengthForInstructionAt(currentAddress);
        }
    }

    public void setCurrentAddressSupplier(IntSupplier supplier) {
        this.currentAddressSupplier.set(supplier);
    }

    private void disassemblerLoop() {
        while (this.running.get()) {
            Integer address = this.addressQueue.poll();
            if (address == null) {
                if (this.staticDisassemblyFinished) {
                    Thread.onSpinWait();
                } else {
                    this.disassembleStatic();
                }
            } else {
                this.disassembleAt(address);
            }
        }
    }

    private void disassembleStatic() {
        Bus bus = this.emulator.getBus();
        if (this.currentStaticDisassemblerPointer >= bus.getMemorySize()) {
            this.staticDisassemblyFinished = true;
            return;
        }
        int length = this.getLengthForInstructionAt(this.currentStaticDisassemblerPointer);
        for (int i = this.currentStaticDisassemblerPointer; i < this.currentStaticDisassemblerPointer + length; i++) {
            if (this.entries.containsKey(i)) {
                this.currentStaticDisassemblerPointer += length;
                return;
            }
        }
        this.addEntry(new Entry(this.currentStaticDisassemblerPointer, length, this.getBytecodeForInstructionAt(this.currentStaticDisassemblerPointer)));
        this.currentStaticDisassemblerPointer += length;
    }

    private void disassembleAt(int address) {
        Entry entry = this.entries.get(address);
        int length = this.getLengthForInstructionAt(address);
        int bytecode = this.getBytecodeForInstructionAt(address);
        if (entry == null || entry.getLength() != length || entry.getAddress() != address) {
            entry = new Entry(address, length, bytecode);
            this.addEntry(entry);
        }
        entry.setAddress(address);
        entry.setLength(length);
        entry.setBytecode(bytecode);
    }

    protected abstract int getLengthForInstructionAt(int address);

    protected abstract int getBytecodeForInstructionAt(int address);

    protected abstract String getTextForInstructionAt(int address);

    private void addEntry(Entry entry) {
        int address = entry.getAddress();
        int length = entry.getLength();
        this.removeOverlappingEntries(address, length);
        this.entries.put(address, entry);
        this.addOrdinalAddress(address);
    }

    private void removeOverlappingEntries(int start, int length) {
        int end = start + length;

        Map.Entry<Integer, Entry> lower = entries.floorEntry(start);
        if (lower != null) {
            Entry e = lower.getValue();
            int eEnd = e.getAddress() + e.getLength();
            if (eEnd > start) {
                this.removeEntry(e);
            }
        }

        Map.Entry<Integer, Entry> curr = entries.ceilingEntry(start);
        while (curr != null && curr.getKey() < end) {
            Entry e = curr.getValue();
            curr = entries.higherEntry(curr.getKey());
            this.removeEntry(e);
        }
    }

    private void removeEntry(Entry entry) {
        int start = entry.getAddress();
        int end = start + entry.getLength();

        this.entries.remove(start);
        this.addressOrdinalList.removeIf(a -> a >= start && a < end);
    }


    private void addOrdinalAddress(int address) {
        int idx = Collections.binarySearch(this.addressOrdinalList, address);
        if (idx >= 0) {
            return;
        }
        this.addressOrdinalList.add(-idx - 1, address);
    }

    private void validateEntry(Entry entry) {
        int address = entry.getAddress();
        int length = entry.getLength();
        int bytecode = entry.getByteCode();

        int currentLength = this.getLengthForInstructionAt(address);
        int currentBytecode = this.getBytecodeForInstructionAt(address);

        if (length != currentLength) {
            this.disassemble(address);
        } else if (bytecode != currentBytecode) {
            entry.setBytecode(currentBytecode);
        }
        if (entry.getText() == null) {
            entry.setText(this.getTextForInstructionAt(address));
        }
    }

    protected static class Entry implements Disassembler.Entry {

        private volatile int instructionAddress;
        private volatile int length;
        private volatile int bytecode;
        private volatile String text;

        public Entry(int address, int length, int bytecode) {
            this.instructionAddress = address;
            this.length = length;
            this.bytecode = bytecode;
        }

        private void setAddress(int address) {
            if (this.instructionAddress != address) {
                this.text = null;
                this.instructionAddress = address;
            }
        }

        @Override
        public int getAddress() {
            return instructionAddress;
        }

        private void setLength(int length) {
            if (this.length != length) {
                this.text = null;
                this.length = length;
            }
        }

        @Override
        public int getLength() {
            return length;
        }

        private void setBytecode(int bytecode) {
            if (this.bytecode != bytecode) {
                this.text = null;
                this.bytecode = bytecode;
            }
        }

        @Override
        public int getByteCode() {
            return bytecode;
        }

        private void setText(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

    }

}
