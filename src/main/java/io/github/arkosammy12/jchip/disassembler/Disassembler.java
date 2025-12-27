package io.github.arkosammy12.jchip.disassembler;

import java.util.Optional;
import java.util.function.IntSupplier;

public interface Disassembler {

    Entry getEntry(int ordinal);

    int getSize();

    int getIndexForAddress(int address);

    void setEnabled(boolean enabled);

    boolean isEnabled();

    Optional<IntSupplier> getCurrentAddressSupplier();

    interface Entry {

        int getInstructionAddress();

        int getLength();

        int getByteCode();

        String getText();

    }

}
