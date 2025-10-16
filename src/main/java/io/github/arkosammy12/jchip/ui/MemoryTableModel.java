package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;

public class MemoryTableModel extends AbstractTableModel {

    private static final int TOTAL_SHOWN_BYTES = 0xFFFF + 1;
    private static final int BYTES_PER_ROW = 8;

    private final int[] memoryView = new int[TOTAL_SHOWN_BYTES];

    public MemoryTableModel() {
    }

    @Override
    public int getRowCount() {
        return (int) Math.ceil(memoryView.length / (double) BYTES_PER_ROW);
    }

    @Override
    public int getColumnCount() {
        return BYTES_PER_ROW + 1;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return String.format("%04X", row * BYTES_PER_ROW);
        } else {
            int idx = row * BYTES_PER_ROW + (col - 1);
            return idx < memoryView.length ? String.format("%02X", memoryView[idx]) : "";
        }
    }

    public void updateState(Chip8Emulator<?, ?> emulator) {
        emulator.getMemory().getMemoryView(this.memoryView);
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    public void clear() {
        Arrays.fill(this.memoryView, 0);
    }

}
