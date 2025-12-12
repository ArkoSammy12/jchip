package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MemoryTable extends JTable {

    private static final int MEMORY_COLUMN_WIDTH = 24;
    private static final int ADDRESS_COLUMN_WIDTH = 70;

    private final MemoryTableModel memoryTableModel;

    public MemoryTable() {
        MemoryTableModel memoryTableModel = new MemoryTableModel();
        super(memoryTableModel);
        this.memoryTableModel = memoryTableModel;
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.setRowHeight(15);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setFocusable(false);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setTableHeader(null);
        this.buildTable();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int currentBytesPerRow = memoryTableModel.getBytesPerRow();
                int newBytesPerRow = 8;

                if (getSize().width > (ADDRESS_COLUMN_WIDTH + (MEMORY_COLUMN_WIDTH * 32))) {
                    newBytesPerRow = 32;
                } else if (getSize().width > (ADDRESS_COLUMN_WIDTH + (MEMORY_COLUMN_WIDTH * 16))) {
                    newBytesPerRow = 16;
                }
                if (newBytesPerRow != currentBytesPerRow) {
                    memoryTableModel.setBytesPerRow(newBytesPerRow);
                    buildTable();
                }
            }
        });

    }

    private void buildTable() {
        this.memoryTableModel.rebuildColumns();
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
        this.memoryTableModel.update(emulator);
    }

    public void clear() {
        this.memoryTableModel.clear();
        this.scrollToAddress(0);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return getPreferredSize().width <= getParent().getWidth();
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
        int targetY = (address / this.memoryTableModel.getBytesPerRow()) * this.getRowHeight();
        if (targetY < 0) {
            targetY = 0;
        }
        ((JViewport) this.getParent()).setViewPosition(new Point(0, targetY));
    }

}
