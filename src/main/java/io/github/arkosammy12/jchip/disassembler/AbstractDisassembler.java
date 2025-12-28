package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.IntSupplier;

public abstract class AbstractDisassembler<E extends Emulator> implements Disassembler {

    protected final E emulator;

    private final Int2ObjectSortedMap<Entry> disassemblyEntries = new Int2ObjectAVLTreeMap<>();
    private final IntArrayList ordinalToAddress = new IntArrayList();
    private final Int2IntMap addressToOrdinal = new Int2IntOpenHashMap();

    private IntSupplier currentAddressSupplier;
    private boolean enabled;
    private boolean ordinalsDirty = true;

    public AbstractDisassembler(E emulator) {
        this.emulator = emulator;

        Entry currentEntry = null;
        for (int i = 0; i < emulator.getBus().getMemorySize(); i++) {
            if ((i & 1) == 0) {
                currentEntry = new Entry(i, 2, -1);
            }
            disassemblyEntries.put(i, currentEntry);
        }

        addressToOrdinal.defaultReturnValue(-1);
        recalculateOrdinalsIfNecessary();
    }

    public void setCurrentAddressSupplier(IntSupplier addressSupplier) {
        this.currentAddressSupplier = addressSupplier;
    }

    @Override
    public Optional<IntSupplier> getCurrentAddressSupplier() {
        return Optional.ofNullable(currentAddressSupplier);
    }

    @Override
    public int getSize() {
        recalculateOrdinalsIfNecessary();
        return ordinalToAddress.size();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    @Nullable
    public Entry getEntry(int index) {
        recalculateOrdinalsIfNecessary();
        if (index < 0 || index >= ordinalToAddress.size()) {
            return null;
        }
        int address = ordinalToAddress.getInt(index);
        Entry entry = disassemblyEntries.get(address);
        if (entry.getByteCode() < 0) {
            entry.setBytecode(this.getBytecodeForEntry(entry));
        }
        if (entry.getText() == null) {
            entry.setText(getTextForEntry(entry));
        }
        return entry;
    }

    @Override
    public int getOrdinalForAddress(int address) {
        recalculateOrdinalsIfNecessary();
        return addressToOrdinal.get(address);
    }

    public abstract void disassembleAt(int address);

    protected void addDisassemblerEntry(int address, int length, int bytecode) {
        Entry existingEntry = disassemblyEntries.get(address);
        if (existingEntry == null || existingEntry.getLength() != length) {
            existingEntry = new Entry(address, length, bytecode);
            for (int i = address; i < address + length; i++) {
                disassemblyEntries.put(i, existingEntry);
            }
            ordinalsDirty = true;
        }
        ordinalsDirty |= existingEntry.setInstructionAddress(address);
        ordinalsDirty |= existingEntry.setLength(length);
        existingEntry.setBytecode(bytecode);
    }

    protected abstract String getTextForEntry(Entry entry);

    protected abstract int getBytecodeForEntry(Entry entry);

    private void recalculateOrdinalsIfNecessary() {
        if (!ordinalsDirty) {
            return;
        }
        ordinalsDirty = false;

        ordinalToAddress.clear();
        addressToOrdinal.clear();

        int ordinal = 0;
        for (Int2ObjectMap.Entry<Entry> mapEntry : disassemblyEntries.int2ObjectEntrySet()) {
            int address = mapEntry.getIntKey();
            Entry entry = mapEntry.getValue();

            if (entry.getInstructionAddress() != address) {
                continue;
            }

            ordinalToAddress.add(address);
            addressToOrdinal.put(address, ordinal++);
        }
    }

    public static class Entry implements Disassembler.Entry {

        private int instructionAddress;
        private int length;
        private int bytecode;
        private String text;

        public Entry(int address, int length, int bytecode) {
            this.instructionAddress = address;
            this.length = length;
            this.bytecode = bytecode;
        }

        private boolean setInstructionAddress(int address) {
            if (this.instructionAddress != address) {
                this.text = null;
                this.instructionAddress = address;
                return true;
            }
            return false;
        }

        @Override
        public int getInstructionAddress() {
            return instructionAddress;
        }

        private boolean setLength(int length) {
            if (this.length != length) {
                this.text = null;
                this.length = length;
                return true;
            }
            return false;
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
