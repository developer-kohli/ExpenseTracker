package ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Central UI Theme and Component Factory for ExpenseTracker.
 *
 * <h3>Design System</h3>
 * <ul>
 *   <li><b>Primary Color</b>: Dark Blue ({@code #1E3A8A})</li>
 *   <li><b>Accent Color</b>: Blue ({@code #2563EB}) with interactive hover states</li>
 *   <li><b>Background</b>: Light Slate / Gray ({@code #F8FAFC}) and Pure White cards</li>
 *   <li><b>Typography</b>: Consistent Segoe UI typeface hierarchy</li>
 *   <li><b>Layout Hygiene</b>: Standardized padding, borders, and component heights</li>
 * </ul>
 */
public final class UITheme {

    // ----------------------------------------------------------------
    // Color Palette
    // ----------------------------------------------------------------
    public static final Color PRIMARY_DARK      = new Color(30, 58, 138);  // Deep Navy / Dark Blue (#1E3A8A)
    public static final Color PRIMARY_HOVER     = new Color(23, 37, 84);   // Darker Navy (#172554)
    public static final Color ACCENT_BLUE       = new Color(37, 99, 235);  // Vibrant Blue (#2563EB)
    public static final Color ACCENT_HOVER      = new Color(29, 78, 216);  // Darker Blue (#1D4ED8)
    public static final Color SUCCESS_GREEN     = new Color(16, 185, 129); // Emerald (#10B981)
    public static final Color SUCCESS_HOVER     = new Color(5, 150, 105);  // Darker Emerald (#059669)
    public static final Color DANGER_RED        = new Color(239, 68, 68);  // Rose / Red (#EF4444)
    public static final Color DANGER_HOVER      = new Color(220, 38, 38);  // Darker Red (#DC2626)
    public static final Color NEUTRAL_GRAY      = new Color(100, 116, 139);// Slate-500 (#64748B)
    public static final Color NEUTRAL_HOVER     = new Color(71, 85, 105);  // Slate-600 (#475569)

    public static final Color BG_LIGHT          = new Color(248, 250, 252);// Slate-50 (#F8FAFC)
    public static final Color BG_CARD           = Color.WHITE;
    public static final Color BORDER_COLOR      = new Color(226, 232, 240);// Slate-200 (#E2E8F0)
    public static final Color ROW_ALT_COLOR     = new Color(248, 250, 252);// Slate-50 (#F8FAFC)
    public static final Color SELECTION_COLOR   = new Color(219, 234, 254);// Blue-100 (#DBEAFE)

    public static final Color TEXT_MAIN         = new Color(15, 23, 42);   // Pitch Dark Slate (#0F172A)
    public static final Color TEXT_MUTED        = new Color(51, 65, 85);   // High-Contrast Slate-700 (#334155)
    public static final Color TEXT_LINK         = new Color(30, 58, 138);  // Deep Royal Blue (#1E3A8A)

    // ----------------------------------------------------------------
    // Fonts
    // ----------------------------------------------------------------
    public static final Font FONT_TITLE         = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION       = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_LABEL         = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT         = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON        = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_TABLE_HDR     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE_ROW     = new Font("Segoe UI", Font.PLAIN, 13);

    private UITheme() {}

    // Button Background Tints (High readability with Black Text)
    public static final Color BTN_PRIMARY_BG    = new Color(191, 219, 254);// Light Blue (#BFDBFE)
    public static final Color BTN_PRIMARY_HOVER = new Color(147, 197, 253);// Blue-300 (#93C5FD)
    public static final Color BTN_DARK_BG       = new Color(203, 213, 225);// Slate-300 (#CBD5E1)
    public static final Color BTN_DARK_HOVER    = new Color(148, 163, 184);// Slate-400 (#94A3B8)
    public static final Color BTN_SUCCESS_BG    = new Color(187, 247, 208);// Mint Green (#BBF7D0)
    public static final Color BTN_SUCCESS_HOVER = new Color(134, 239, 172);// Green-300 (#86EFAC)
    public static final Color BTN_DANGER_BG     = new Color(254, 202, 202);// Soft Red (#FECACA)
    public static final Color BTN_DANGER_HOVER  = new Color(252, 165, 165);// Red-300 (#FCA5A5)
    public static final Color BTN_NEUTRAL_BG    = new Color(226, 232, 240);// Slate-200 (#E2E8F0)
    public static final Color BTN_NEUTRAL_HOVER = new Color(203, 213, 225);// Slate-300 (#CBD5E1)

    // ----------------------------------------------------------------
    // Component Factory Methods
    // ----------------------------------------------------------------

    public static JButton createPrimaryButton(String text) {
        return createCustomButton(text, BTN_PRIMARY_BG, BTN_PRIMARY_HOVER, Color.BLACK);
    }

    public static JButton createDarkButton(String text) {
        return createCustomButton(text, BTN_DARK_BG, BTN_DARK_HOVER, Color.BLACK);
    }

    public static JButton createSuccessButton(String text) {
        return createCustomButton(text, BTN_SUCCESS_BG, BTN_SUCCESS_HOVER, Color.BLACK);
    }

    public static JButton createDangerButton(String text) {
        return createCustomButton(text, BTN_DANGER_BG, BTN_DANGER_HOVER, Color.BLACK);
    }

    public static JButton createSecondaryButton(String text) {
        return createCustomButton(text, BTN_NEUTRAL_BG, BTN_NEUTRAL_HOVER, Color.BLACK);
    }

    public static JButton createCustomButton(String text, Color normalBg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(normalBg);
        btn.setForeground(Color.BLACK); // Explicitly Black Text
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(148, 163, 184), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));

        // Hover effect listener
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(normalBg);
                }
            }
        });

        return btn;
    }

    public static final Color INPUT_BORDER      = new Color(148, 163, 184);// Slate-400 for crisp field boundaries

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_INPUT);
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(INPUT_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_INPUT);
        pf.setForeground(Color.BLACK);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(ACCENT_BLUE);
        pf.setBorder(new CompoundBorder(
                new LineBorder(INPUT_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return pf;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE_ROW);
        table.setRowHeight(32);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HDR);
        header.setBackground(new Color(226, 232, 240)); // Slate-200
        header.setForeground(Color.BLACK);             // Black Table Header Text
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
    }
}
