package io.github.arkosammy12.jchip.ui.debugger;

import io.github.arkosammy12.jchip.emulators.Emulator;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

import static io.github.arkosammy12.jchip.ui.debugger.MemoryTableModel.BYTES_PER_ROW;

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
        //this.setMaximumSize(new Dimension(260, Integer.M));

        TableColumn addressColumn = this.getColumnModel().getColumn(0);
        addressColumn.setPreferredWidth(ADDRESS_COLUMN_WIDTH);
        addressColumn.setMinWidth(ADDRESS_COLUMN_WIDTH);
        //addressColumn.setMaxWidth(ADDRESS_COLUMN_WIDTH);

        for (int i = 1; i < this.getColumnModel().getColumnCount(); i++) {
            TableColumn column = this.getColumnModel().getColumn(i);
            column.setPreferredWidth(MEMORY_COLUMN_WIDTH);
            column.setMinWidth(MEMORY_COLUMN_WIDTH);
            //column.setMaxWidth(MEMORY_COLUMN_WIDTH);
        }


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
        int targetY = (address / BYTES_PER_ROW) * this.getRowHeight();
        if (targetY < 0) {
            targetY = 0;
        }
        ((JViewport) this.getParent()).setViewPosition(new Point(0, targetY));
    }

}
