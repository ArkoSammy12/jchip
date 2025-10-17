package io.github.arkosammy12.jchip.ui;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class MemoryTableModel extends AbstractTableModel {

    public static final int BYTES_PER_ROW = 8;
    private static final int TOTAL_SHOWN_BYTES = 0xFFFF + 1;
    private static final int ROW_COUNT = (int) Math.ceil(TOTAL_SHOWN_BYTES / (double) BYTES_PER_ROW);

    private final int[] memoryView = new int[TOTAL_SHOWN_BYTES];
    private final AtomicBoolean cleared = new AtomicBoolean(false);

    public MemoryTableModel() {
        super();
    }

    @Override
    public int getRowCount() {
        return ROW_COUNT;
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

    public void update(Chip8Emulator<?, ?> emulator) {
        emulator.getMemory().getMemoryView(this.memoryView);
        this.cleared.set(false);
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    public void clear() {
        if (!this.cleared.get()) {
            Arrays.fill(this.memoryView, 0);
        }
        this.cleared.set(true);
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

}
