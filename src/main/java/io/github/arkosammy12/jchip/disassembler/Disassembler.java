package io.github.arkosammy12.jchip.disassembler;

import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.IntSupplier;

public interface Disassembler extends Closeable {

    @Nullable
    Entry getEntry(int ordinal);

    int getSize();

    int getOrdinalForAddress(int address);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    Optional<IntSupplier> getCurrentAddressSupplier();

    interface Entry {

        int getAddress();

        int getLength();

        int getByteCode();

        String getText();

    }

}
