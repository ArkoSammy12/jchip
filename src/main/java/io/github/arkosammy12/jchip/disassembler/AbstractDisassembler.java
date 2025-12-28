package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntSupplier;

public abstract class AbstractDisassembler implements Disassembler {

    protected final Emulator emulator;
    private final Int2ObjectSortedMap<Entry> disassemblyEntries = new Int2ObjectAVLTreeMap<>();
    private final SortedBidiMap<Integer, Integer> ordinalToEntry = new DualTreeBidiMap<>();

    private IntSupplier currentAddressSupplier;
    private boolean enabled;

    public AbstractDisassembler(Emulator emulator) {
        this.emulator = emulator;

        Entry currentEntry = null;
        for (int i = 0; i < this.emulator.getBus().getMemorySize(); i++) {
            if (i % 2 == 0) {
                currentEntry = new Entry(i, 2, 0);
            }
            this.disassemblyEntries.put(i, currentEntry);
        }

        this.rebuildOrdinalMap();
    }

    public void setCurrentAddressSupplier(IntSupplier addressSupplier) {
        this.currentAddressSupplier = addressSupplier;
    }

    @Override
    public Optional<IntSupplier> getCurrentAddressSupplier() {
        return Optional.ofNullable(this.currentAddressSupplier);
    }

    @Override
    public int getSize() {
        return this.ordinalToEntry.size();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    @Nullable
    public Entry getEntry(int index) {
        if (!this.ordinalToEntry.containsKey(index)) {
            return null;
        }
        Entry entry = this.disassemblyEntries.get((int)this.ordinalToEntry.get(index));
        if (entry.getText() == null) {
            entry.setText(this.getTextForEntry(entry));
        }
        return entry;
    }

    @Override
    public int getOrdinalForAddress(int address) {
        Integer ordinal = this.ordinalToEntry.getKey(address);
        return ordinal != null ? ordinal : -1;
    }

    public abstract void disassembleAt(int address);

    protected void addDisassemblerEntry(int address, int length, int bytecode) {
        boolean rebuildOrdinals = false;
        Entry existingEntry = this.disassemblyEntries.get(address);
        if (existingEntry == null || existingEntry.getLength() != length) {
            existingEntry = new Entry(address, length, bytecode);
            for (int i = address; i < address + length; i++) {
                this.disassemblyEntries.put(i, existingEntry);
            }
            rebuildOrdinals = true;
        }
        rebuildOrdinals |= existingEntry.setInstructionAddress(address);
        rebuildOrdinals |= existingEntry.setLength(length);
        if (rebuildOrdinals) {
            this.rebuildOrdinalMap();
        }
        existingEntry.setBytecode(bytecode);
    }

    protected abstract String getTextForEntry(Entry entry);

    private void rebuildOrdinalMap() {
        this.ordinalToEntry.clear();

        int ordinal = 0;
        for (Int2ObjectMap.Entry<Entry> mapEntry : this.disassemblyEntries.int2ObjectEntrySet()) {
            int address = mapEntry.getIntKey();
            Entry entry = mapEntry.getValue();

            if (entry.getInstructionAddress() == address) {
                this.ordinalToEntry.put(ordinal, address);
                ordinal++;
            }
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
            return this.instructionAddress;
        }

        private boolean setLength(int length) {
            if (length != this.length) {
                this.text = null;
                this.length = length;
                return true;
            }
            return false;
        }

        @Override
        public int getLength() {
            return this.length;
        }

        private void setBytecode(int bytecode) {
            if (this.bytecode != bytecode) {
                this.text = null;
                this.bytecode = bytecode;
            }
        }

        @Override
        public int getByteCode() {
            return this.bytecode;
        }

        private void setText(String text) {
            this.text = text;
        }

        @Override
        public String getText() {
            return this.text;
        }

    }

}

