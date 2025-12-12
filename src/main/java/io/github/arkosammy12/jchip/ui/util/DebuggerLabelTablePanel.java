package io.github.arkosammy12.jchip.ui.util;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class DebuggerLabelTablePanel extends JTable {

    private static final int COLUMN_WIDTH = 60;
    private static final int ROW_HEIGHT = 26;

    private final Model model;
    private final List<? extends DebuggerLabel<?>> labels;
    private final int columnCount;
    private final boolean columnMayor;
    private final int minRows;

    public DebuggerLabelTablePanel(List<? extends DebuggerLabel<?>> labels, int columnCount, int minRows) {
        this(labels, columnCount, false, minRows);
    }

    public DebuggerLabelTablePanel(List<? extends DebuggerLabel<?>> labels, int columnCount, boolean columnMayor, int minRows) {
        super();
        this.labels = labels;
        this.columnCount = columnCount;
        this.columnMayor = columnMayor;
        this.minRows = minRows;

        this.model = new Model();
        this.setModel(this.model);

        this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        this.setRowHeight(ROW_HEIGHT);
        this.getTableHeader().setReorderingAllowed(false);
        this.getTableHeader().setResizingAllowed(false);
        this.setFocusable(false);
        this.setRowSelectionAllowed(false);
        this.setColumnSelectionAllowed(false);
        this.setTableHeader(null);

        TableColumnModel colModel = this.getColumnModel();

        for (int i = 1; i < colModel.getColumnCount(); i++) {
            TableColumn col = colModel.getColumn(i);
            col.setPreferredWidth(COLUMN_WIDTH);
            col.setMinWidth(COLUMN_WIDTH);
        }
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

    public void update() {
        boolean changed = updateColumnWidths();
        if (changed) {
            revalidate();
            repaint();
        }
        model.fireTableDataChanged();
    }

    private boolean updateColumnWidths() {
        FontMetrics fm = getFontMetrics(getFont());

        int maxChars = 0;
        for (DebuggerLabel<?> label : labels) {
            String text = label.getText();
            if (text != null) {
                maxChars = Math.max(maxChars, text.length());
            }
        }

        int charWidth = fm.charWidth('W');
        int preferredWidth = charWidth * maxChars + 10;

        TableColumnModel colModel = getColumnModel();
        boolean changed = false;

        for (int i = 0; i < colModel.getColumnCount(); i++) {
            TableColumn col = colModel.getColumn(i);
            if (col.getPreferredWidth() != preferredWidth) {
                col.setPreferredWidth(preferredWidth);
                col.setMinWidth(preferredWidth);
                changed = true;
            }
        }

        return changed;
    }

    private class Model extends DefaultTableModel {

        @Override
        public int getRowCount() {
            int calculated = (int) Math.ceil((double) labels.size() / columnCount);
            return Math.max(calculated, minRows);
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            int index = columnMayor ? col * getRowCount() + row : row * getColumnCount() + col;
            if (index < 0 || index >= labels.size()) {
                return "";
            }
            return labels.get(index).getText();
        }
    }
}
