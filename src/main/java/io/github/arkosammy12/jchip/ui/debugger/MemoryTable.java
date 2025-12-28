package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;
import io.github.arkosammy12.jchip.ui.MainWindow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Objects;

public class MemoryTable extends JTable {

    private static final int MAX_SHOWN_BYTES = 0xFFFFFF + 1;

    private static final int MEMORY_COLUMN_WIDTH = 26;
    private static final int ADDRESS_COLUMN_WIDTH = 72;
    private static final int ROW_HEIGHT = 15;

    private final Model model;

    public MemoryTable() {
        super();
        this.model = new Model();
        this.setModel(model);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.setRowHeight(ROW_HEIGHT);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setFocusable(false);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setTableHeader(null);
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                int currentBytesPerRow = model.getBytesPerRow();
                int newBytesPerRow = 8;

                if (getSize().width > (ADDRESS_COLUMN_WIDTH + (MEMORY_COLUMN_WIDTH * 32))) {
                    newBytesPerRow = 32;
                } else if (getSize().width > (ADDRESS_COLUMN_WIDTH + (MEMORY_COLUMN_WIDTH * 16))) {
                    newBytesPerRow = 16;
                }
                if (newBytesPerRow != currentBytesPerRow) {
                    model.setBytesPerRow(newBytesPerRow);
                    buildTable();
                }
            }

        });
        this.buildTable();

    }

    public int getCurrentMaximumAddress() {
        return this.model.memory == null ? MAX_SHOWN_BYTES - 1 : this.model.memory.getMaximumAddress();
    }

    private void buildTable() {
        this.model.rebuildColumns();
        TableColumnModel colModel = this.getColumnModel();

        TableColumn addressColumn = colModel.getColumn(0);
        addressColumn.setPreferredWidth(ADDRESS_COLUMN_WIDTH);
        addressColumn.setMinWidth(ADDRESS_COLUMN_WIDTH);

        for (int i = 1; i < colModel.getColumnCount(); i++) {
            TableColumn col = colModel.getColumn(i);
            col.setPreferredWidth(MEMORY_COLUMN_WIDTH);
            col.setMinWidth(MEMORY_COLUMN_WIDTH);
        }

        this.revalidate();
        this.repaint();
    }

    public void update(Emulator emulator) {
        this.model.update(emulator);
    }

    public void clear() {
        this.model.clear();
        this.scrollToAddress(0);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return this.getPreferredSize().getWidth() <= this.getParent().getWidth();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        Color baseColor = getBackground();
        if (row % 2 != 0) {
            baseColor = baseColor.darker();
        }
        c.setBackground(baseColor);
        return c;
    }

    public void scrollToAddress(int address) {
        int targetY = (address / this.model.getBytesPerRow()) * this.getRowHeight();
        if (targetY < 0) {
            targetY = 0;
        }
        ((JViewport) this.getParent()).setViewPosition(new Point(0, targetY));
    }

    private class Model extends DefaultTableModel {

        private Bus memory;

        private int bytesPerRow = 8;
        private int rowCount;

        public Model() {
            super();
            this.rowCount = (int) Math.ceil(MAX_SHOWN_BYTES / (double) this.bytesPerRow);
        }

        public void setBytesPerRow(int bytesPerRow) {
            this.bytesPerRow = bytesPerRow;
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

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        void rebuildColumns() {
            this.updateRowCount();
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
            MainWindow.fireVisibleRowsUpdated(MemoryTable.this);
        }

        public void clear() {
            this.memory = null;
            this.fireTableDataChanged();
        }

        private void updateRowCount() {
            if (this.memory == null) {
                this.rowCount = (int) Math.ceil(MAX_SHOWN_BYTES / (double) this.bytesPerRow);
            } else {
                this.rowCount = (int) Math.ceil(this.memory.getMemorySize() / (double) this.bytesPerRow);
            }
        }

    }

}
