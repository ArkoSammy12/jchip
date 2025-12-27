package io.github.arkosammy12.jchip.disassembler;

import io.github.arkosammy12.jchip.emulators.Emulator;
import it.unimi.dsi.fastutil.ints.*;
import org.apache.commons.collections4.SortedBidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntSupplier;

public abstract class AbstractDisassembler implements Disassembler {

    protected final Emulator emulator;
    private final Int2ObjectSortedMap<Disassembler.Entry> disassemblyEntries = new Int2ObjectAVLTreeMap<>();
    protected final SortedBidiMap<Integer, Integer> ordinalToEntry = new DualTreeBidiMap<>();

    private IntSupplier currentAddressSupplier;

    private boolean enabled;

    public AbstractDisassembler(Emulator emulator) {
        this.emulator = emulator;
        DisassemblerEntry currentEntry = null;
        for (int i = 0; i < this.emulator.getBus().getMemorySize(); i++) {
            if (i % 2 == 0) {
                currentEntry = new DisassemblerEntry(i, 2, 0, "invalid");
            }
            this.disassemblyEntries.put(i, currentEntry);
        }
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
        int i = 1;
        Disassembler.Entry currentEntry = this.disassemblyEntries.firstEntry().getValue();
        for (Disassembler.Entry entry : this.disassemblyEntries.values()) {
            if (!Objects.equals(currentEntry, entry)) {
                currentEntry = entry;
                i++;
            }
        }
        return i;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    abstract public void disassembleAt(int address);

    protected void addDisassemblerEntry(int address, int length, int bytecode, String text) {
        Disassembler.Entry existingEntry = this.disassemblyEntries.get(address);
        if (existingEntry.getInstructionAddress() != address || existingEntry.getLength() != length || existingEntry.getByteCode() != bytecode || !existingEntry.getText().equals(text)) {
            DisassemblerEntry entry = new DisassemblerEntry(address, length, bytecode, text);
            for (int i = entry.getInstructionAddress(); i < entry.getInstructionAddress() + entry.getLength(); i++) {
                this.disassemblyEntries.put(i, entry);
            }
        }
    }

    @Override
    @Nullable
    public Entry getEntry(int index) {
        int i = 0;
        Disassembler.Entry currentEntry = this.disassemblyEntries.firstEntry().getValue();
        for (Disassembler.Entry entry : this.disassemblyEntries.values()) {
            if (i == index) {
                break;
            }
            if (!Objects.equals(currentEntry, entry)) {
                currentEntry = entry;
                i++;
            }
        }
        return currentEntry;
    }

    @Override
    public int getIndexForAddress(int address) {
        int ordinal = 0;
        Disassembler.Entry last = null;

        for (Disassembler.Entry entry : disassemblyEntries.values()) {
            if (!Objects.equals(last, entry)) {
                if (entry.getInstructionAddress() == address) {
                    return ordinal;
                }
                ordinal++;
                last = entry;
            }
        }

        return -1;
    }

}
