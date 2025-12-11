package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.util.Objects;

public class MemoryTableModel extends DefaultTableModel {

    public static final int BYTES_PER_ROW = 8;
    private static final int MAX_SHOWN_BYTES = 0xFFFFFF + 1;

    private Bus memory;

    private int bytesPerRow = 8;
    private int rowCount;

    public MemoryTableModel() {
        super();
        this.rowCount = (int) Math.ceil(MAX_SHOWN_BYTES / (double) this.bytesPerRow);
    }



    public void setBytesPerRow(int bytesPerRow) {
        this.bytesPerRow = bytesPerRow;
        this.rowCount = (int) Math.ceil(MAX_SHOWN_BYTES / (double) this.bytesPerRow);
    }

    public int getBytesPerRow() {
        return this.bytesPerRow;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return this.bytesPerRow + 1;
    }

    void rebuildColumns() {
        this.rowCount = (int) Math.ceil(MAX_SHOWN_BYTES / (double) this.bytesPerRow);
        this.fireTableStructureChanged();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return String.format("%06X", row * this.bytesPerRow);
        } else if (this.memory != null) {
            int idx = row * this.bytesPerRow + (col - 1);
            return idx < this.memory.getMemorySize() ? String.format("%02X", this.memory.getByte(idx)) : "";
        } else {
            return "00";
        }
    }

    public void update(Emulator emulator) {
        Bus memory = emulator.getBus();
        if (!Objects.equals(memory, this.memory)) {
            this.memory = memory;
            this.rowCount = (int) Math.ceil(this.memory.getMemorySize() / (double) this.bytesPerRow);
        }
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    public void clear() {
        this.memory = null;
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

}
