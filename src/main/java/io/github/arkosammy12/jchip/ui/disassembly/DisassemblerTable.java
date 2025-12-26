package io.github.arkosammy12.jchip.ui.disassembly;

import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.memory.Bus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Objects;

public class DisassemblerTable extends JTable {

    private static final int ROW_HEIGHT = 15;

    private final Model model;

    public DisassemblerTable() {
        Model model = new Model();
        super(model);
        this.model = model;
        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.setRowHeight(ROW_HEIGHT);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setFocusable(false);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setTableHeader(null);
        this.buildTable();
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

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return this.getPreferredSize().getWidth() <= this.getParent().getWidth();
    }

    private void buildTable() {
        this.model.rebuildColumns();
        this.revalidate();
        this.repaint();
    }

    public void update(Emulator emulator) {
        this.model.update(emulator);
    }

    public void clear() {
        this.model.clear();
    }

    private static class Model extends DefaultTableModel {

        private Model() {

        }

        @Override
        public int getRowCount() {
            return 20;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        private void rebuildColumns() {

        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return Integer.toHexString(row * 2).toUpperCase();
            } else if (col == 1) {
                return "0000";
            } else {
                return "invalid";
            }
        }

        public void update(Emulator emulator) {
            this.fireTableDataChanged();
        }

        public void clear() {
            this.fireTableDataChanged();
        }

    }

}
