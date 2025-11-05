package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryTableModel extends AbstractTableModel {

    public static final int BYTES_PER_ROW = 8;

    private Chip8Memory memory;
    private int rowCount = (int) Math.ceil(0xFFFFFF + 1 / (double) BYTES_PER_ROW);

    public MemoryTableModel() {
        super();
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return BYTES_PER_ROW + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return String.format("%06X", row * BYTES_PER_ROW);
        } else if (this.memory != null) {
            int idx = row * BYTES_PER_ROW + (col - 1);
            return idx < this.memory.getMemorySize() ? String.format("%02X", this.memory.readByte(idx)) : "";
        } else {
            return "00";
        }
    }

    public void update(Chip8Emulator<?, ?> emulator) {
        Chip8Memory memory = emulator.getMemory();
        if (!Objects.equals(memory, this.memory)) {
            this.memory = memory;
            this.rowCount = (int) Math.ceil(this.memory.getMemorySize() / (double) BYTES_PER_ROW);
        }
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    public void clear() {
        this.memory = null;
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

}
