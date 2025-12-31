package io.github.arkosammy12.jchip.disassembler;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.IntSupplier;

public interface Disassembler extends Closeable {

    int getSize();

    int getOrdinalForAddress(int address);

    Optional<IntSupplier> getCurrentAddressSupplier();

    void setEnabled(boolean enabled);

    boolean isEnabled();

    Collection<Integer> getCurrentBreakpoints();

    void addBreakpoint(int address);

    void removeBreakpoint(int address);

    boolean checkBreakpoint(int address);

    void clearBreakpoints();

    @Nullable
    Entry getEntry(int ordinal);

    interface Entry {

        int getAddress();

        int getLength();

        int getByteCode();

        String getText();

    }

}
