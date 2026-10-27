package com.fooddelivery.ui.components;

import com.fooddelivery.ui.AppTheme;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

/**
 * StyledTable: Desktop table with Buttercream header row (#F3E3C0), 56px row height,
 * Serenity Blue hover highlight, and clean cell padding.
 */
public class StyledTable extends JTable {

    private int hoveredRow = -1;

    public StyledTable(TableModel model) {
        super(model);
        init();
    }

    public StyledTable() {
        super();
        init();
    }

    private void init() {
        setFont(AppTheme.fontBody());
        setRowHeight(56);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setSelectionBackground(AppTheme.SERENITY_BLUE_LIGHT);
        setSelectionForeground(AppTheme.TEXT_PRIMARY);
        setBackground(AppTheme.SURFACE_CARD);
        setForeground(AppTheme.TEXT_PRIMARY);

        // Header
        JTableHeader header = getTableHeader();
        header.setFont(AppTheme.fontBodyBold());
        header.setBackground(AppTheme.BUTTERCREAM);
        header.setForeground(AppTheme.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 48));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setBackground(AppTheme.BUTTERCREAM);
                l.setForeground(AppTheme.TEXT_PRIMARY);
                l.setFont(AppTheme.fontBodyBold());
                l.setBorder(new EmptyBorder(0, 18, 0, 18));
                return l;
            }
        });

        // Hover tracking
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (c instanceof JComponent jc) {
            jc.setBorder(new EmptyBorder(0, 18, 0, 18));
        }
        if (!isRowSelected(row)) {
            if (row == hoveredRow) {
                c.setBackground(AppTheme.SERENITY_BLUE_TINT);
            } else {
                c.setBackground(row % 2 == 0 ? AppTheme.SURFACE_CARD : AppTheme.WARM_OFF_WHITE);
            }
        }
        return c;
    }

    public static JScrollPane wrap(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_LIGHT, 1));
        sp.getViewport().setBackground(AppTheme.SURFACE_CARD);
        sp.setBackground(AppTheme.SURFACE_CARD);
        return sp;
    }
}
