import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudentController {

    private static void styleOptionPane() {
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Button.background", new Color(37, 99, 235));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.focus", new Color(37, 99, 235));
        UIManager.put("Button.border", new EmptyBorder(6, 18, 6, 18));
    }

    private static final String FILE_PATH = "studentrecords.txt";

    private final StudentGUI gui;
    private final StudentBST tree = new StudentBST();

    StudentController(StudentGUI gui) {
        this.gui = gui;
        loadFromFile();
        attachListeners();
        refreshDashboard();
        refreshTable();
        refreshBstTraversals();
        gui.treePanel.refreshTree(tree);
    }

    // ---- connects every button/nav click to its action ----
    private void attachListeners() {

        gui.navDashboard.addActionListener(e -> {
            refreshDashboard();
            gui.showPage("DASHBOARD");
        });
        gui.navRecords.addActionListener(e -> {
            refreshTable();
            gui.showPage("RECORDS");
        });
        gui.navAddStudent.addActionListener(e -> gui.showPage("ADD_STUDENT"));
        gui.navBSTView.addActionListener(e -> {
            gui.treePanel.refreshTree(tree);
            refreshBstTraversals();
            gui.showPage("BST_VIEW");
        });

        gui.idField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.nameField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.courseField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));
        gui.gradeField.getDocument().addDocumentListener(new SimpleDocListener(this::updatePreview));

        gui.addStudentBtn.addActionListener(e -> handleAddStudent());
        gui.cancelBtn.addActionListener(e -> {
            clearAddForm();
            gui.showPage("DASHBOARD");
        });

        gui.searchField.getDocument().addDocumentListener(new SimpleDocListener(this::handleSearch));

        // Traversal buttons (Student Records page)
        Color INORDER_COLOR   = new Color(37, 99, 235);
        Color PREORDER_COLOR  = new Color(22, 163, 74);
        Color POSTORDER_COLOR = new Color(168, 85, 247);
        gui.inorderBtn.addActionListener(e -> {
            gui.setActiveTraversalButton(gui.inorderBtn, INORDER_COLOR);
            showTraversal("Inorder  (Left \u2192 Root \u2192 Right)", tree.inorder(), INORDER_COLOR);
        });
        gui.preorderBtn.addActionListener(e -> {
            gui.setActiveTraversalButton(gui.preorderBtn, PREORDER_COLOR);
            showTraversal("Preorder  (Root \u2192 Left \u2192 Right)", tree.preorder(), PREORDER_COLOR);
        });
        gui.postorderBtn.addActionListener(e -> {
            gui.setActiveTraversalButton(gui.postorderBtn, POSTORDER_COLOR);
            showTraversal("Postorder  (Left \u2192 Right \u2192 Root)", tree.postorder(), POSTORDER_COLOR);
        });

        // Traversal buttons (BST Visualizer page)
        gui.bstInorderBtn.addActionListener(e -> {
            gui.setActiveBstTraversalButton(gui.bstInorderBtn, INORDER_COLOR);
            List<StudentBST.Node> nodes = tree.inorder();
            showBstTraversal("Inorder  (Left \u2192 Root \u2192 Right)", nodes, INORDER_COLOR);
            List<Integer> ids = new ArrayList<>();
            for (StudentBST.Node n : nodes) ids.add(n.rollNumber);
            gui.treePanel.setTraversal(ids, INORDER_COLOR);
        });
        gui.bstPreorderBtn.addActionListener(e -> {
            gui.setActiveBstTraversalButton(gui.bstPreorderBtn, PREORDER_COLOR);
            List<StudentBST.Node> nodes = tree.preorder();
            showBstTraversal("Preorder  (Root \u2192 Left \u2192 Right)", nodes, PREORDER_COLOR);
            List<Integer> ids = new ArrayList<>();
            for (StudentBST.Node n : nodes) ids.add(n.rollNumber);
            gui.treePanel.setTraversal(ids, PREORDER_COLOR);
        });
        gui.bstPostorderBtn.addActionListener(e -> {
            gui.setActiveBstTraversalButton(gui.bstPostorderBtn, POSTORDER_COLOR);
            List<StudentBST.Node> nodes = tree.postorder();
            showBstTraversal("Postorder  (Left \u2192 Right \u2192 Root)", nodes, POSTORDER_COLOR);
            List<Integer> ids = new ArrayList<>();
            for (StudentBST.Node n : nodes) ids.add(n.rollNumber);
            gui.treePanel.setTraversal(ids, POSTORDER_COLOR);
        });
    }

    // ---- ADD STUDENT button logic ----
    private void handleAddStudent() {
        String idText = gui.idField.getText().trim();
        String name = gui.nameField.getText().trim();
        String course = gui.courseField.getText().trim();
        String gradeText = gui.gradeField.getText().trim();

        if (idText.isEmpty() || name.isEmpty() || course.isEmpty() || gradeText.isEmpty()) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.");
            return;
        }

        int rollNumber;
        try {
            rollNumber = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "Student ID must be a number.");
            return;
        }

        if (tree.rollNumberExists(rollNumber)) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "This Student ID already exists.");
            return;
        }

        tree.insert(rollNumber, name, course + "|" + gradeText);
        saveToFile();

        clearAddForm();
        refreshDashboard();
        refreshTable();
        gui.treePanel.refreshTree(tree);
        gui.showPage("RECORDS");
    }

    // ---- live preview panel on Add Student page ----
    private void updatePreview() {
        gui.previewId.setText(orDash(gui.idField.getText()));
        gui.previewName.setText(orDash(gui.nameField.getText()));
        gui.previewCourse.setText(orDash(gui.courseField.getText()));
        gui.previewGrade.setText(orDash(gui.gradeField.getText()));
    }

    private String orDash(String text) {
        return text.trim().isEmpty() ? "\u2014" : text.trim();
    }

    private void clearAddForm() {
        gui.idField.setText("");
        gui.nameField.setText("");
        gui.courseField.setText("");
        gui.gradeField.setText("");
        updatePreview();
    }

    private void cancelTableEditing() {
        if (gui.table != null && gui.table.isEditing()) {
            TableCellEditor editor = gui.table.getCellEditor();
            if (editor != null) {
                editor.cancelCellEditing();
            }
        }
    }

    // ---- SEARCH bar logic (filters table by id or name) ----
    private void handleSearch() {
        cancelTableEditing();
        String query = gui.searchField.getText().trim();
        if (query.isEmpty()) {
            refreshTable();
            return;
        }
        gui.tableModel.setRowCount(0);

        try {
            int roll = Integer.parseInt(query);
            StudentBST.Node found = tree.searchByRoll(roll);
            if (found != null) {
                addRowToTable(found);
            }
        } catch (NumberFormatException ex) {
            List<StudentBST.Node> matches = tree.searchByName(query);
            for (StudentBST.Node n : matches) {
                addRowToTable(n);
            }
        }
        gui.recordCountLabel.setText(gui.tableModel.getRowCount() + " students found");
    }

    // ---- VIEW button logic (per row, shown as a details dialog) ----
    private void handleView(int rollNumber) {
        cancelTableEditing();
        StudentBST.Node n = tree.searchByRoll(rollNumber);
        if (n == null) return;
        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String grade = parts.length > 1 ? parts[1] : "";
        styleOptionPane();
        JOptionPane.showMessageDialog(gui,
                "Student ID: " + n.rollNumber + "\n" +
                        "Name: " + n.name + "\n" +
                        "Course: " + course + "\n" +
                        "Grade: " + grade,
                "Student Details", JOptionPane.INFORMATION_MESSAGE);
    }

    // ---- EDIT / UPDATE button logic (per row) ----
    private void handleEdit(int rollNumber) {
        cancelTableEditing();
        StudentBST.Node n = tree.searchByRoll(rollNumber);
        if (n == null) return;

        String[] parts = n.grade.split("\\|");
        String currentCourse = parts[0];
        String currentGrade = parts.length > 1 ? parts[1] : "";

        JTextField idField = new JTextField(String.valueOf(n.rollNumber));
        JTextField nameField = new JTextField(n.name);
        JTextField courseField = new JTextField(currentCourse);
        JTextField gradeField = new JTextField(currentGrade);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

        JLabel idLabel = new JLabel("Student ID:");
        idLabel.setFont(labelFont);
        idField.setFont(fieldFont);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(labelFont);
        nameField.setFont(fieldFont);

        JLabel courseLabel = new JLabel("Course / Program:");
        courseLabel.setFont(labelFont);
        courseField.setFont(fieldFont);

        JLabel gradeLabel = new JLabel("Grade (0-100):");
        gradeLabel.setFont(labelFont);
        gradeField.setFont(fieldFont);

        panel.add(idLabel);
        panel.add(idField);
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(courseLabel);
        panel.add(courseField);
        panel.add(gradeLabel);
        panel.add(gradeField);

        styleOptionPane();
        int result = JOptionPane.showConfirmDialog(gui, panel,
                "Edit Student Record", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String idText = idField.getText().trim();
        String newName = nameField.getText().trim();
        String newCourse = courseField.getText().trim();
        String newGradeText = gradeField.getText().trim();

        if (idText.isEmpty() || newName.isEmpty() || newCourse.isEmpty() || newGradeText.isEmpty()) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "Please fill in all fields.");
            return;
        }

        int newRoll;
        try {
            newRoll = Integer.parseInt(idText);
        } catch (NumberFormatException ex) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "Student ID must be a number.");
            return;
        }

        if (newRoll != rollNumber && tree.rollNumberExists(newRoll)) {
            styleOptionPane();
            JOptionPane.showMessageDialog(gui, "This Student ID already exists.");
            return;
        }

        if (newRoll != rollNumber) {
            tree.deleteByRoll(rollNumber);
        }
        tree.insert(newRoll, newName, newCourse + "|" + newGradeText);

        saveToFile();
        refreshDashboard();
        refreshTable();
        refreshBstTraversals();
        gui.treePanel.refreshTree(tree);
        styleOptionPane();
        JOptionPane.showMessageDialog(gui, "Student record updated successfully!");
    }

    // ---- DELETE button logic (per row) ----
    private void handleDelete(int rollNumber) {
        cancelTableEditing();
        styleOptionPane();
        int confirm = JOptionPane.showConfirmDialog(gui,
                "Are you sure you want to delete this record?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        tree.deleteByRoll(rollNumber);
        saveToFile();
        refreshDashboard();
        refreshTable();
        gui.treePanel.refreshTree(tree);
    }

    // ---- refreshes dashboard stat cards + recent students list ----
    private void refreshDashboard() {
        List<StudentBST.Node> all = tree.inorder();
        gui.totalStudentsValue.setText(String.valueOf(all.size()));

        if (all.isEmpty()) {
            gui.avgGradeValue.setText("0.0");
            gui.topGradeValue.setText("0");
            gui.topGradeName.setText("-");
        } else {
            double sum = 0;
            int topGrade = -1;
            String topName = "-";
            for (StudentBST.Node n : all) {
                int grade = extractGrade(n.grade);
                sum += grade;
                if (grade > topGrade) {
                    topGrade = grade;
                    topName = n.name;
                }
            }
            gui.avgGradeValue.setText(String.format("%.1f", sum / all.size()));
            gui.topGradeValue.setText(String.valueOf(topGrade));
            gui.topGradeName.setText(topName);
        }

        gui.recentStudentsPanel.removeAll();
        int count = 0;
        for (int i = all.size() - 1; i >= 0 && count < 3; i--, count++) {
            gui.recentStudentsPanel.add(buildRecentRow(all.get(i)));
        }
        gui.recentStudentsPanel.revalidate();
        gui.recentStudentsPanel.repaint();
    }

    private JPanel buildRecentRow(StudentBST.Node n) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, StudentGUI.BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String gradeText = parts.length > 1 ? parts[1] : "";

        // left side: avatar circle + name/course stacked
        JPanel leftBox = new JPanel(new BorderLayout(10, 0));
        leftBox.setBackground(Color.WHITE);
        leftBox.add(buildAvatar(getInitials(n.name)), BorderLayout.WEST);

        JPanel textBox = new JPanel();
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(n.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel courseLabel = new JLabel(course);
        courseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        courseLabel.setForeground(StudentGUI.TEXT_GRAY);
        textBox.add(nameLabel);
        textBox.add(courseLabel);
        leftBox.add(textBox, BorderLayout.CENTER);
        row.add(leftBox, BorderLayout.WEST);

        // right side: roll number + color-coded grade badge
        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBox.setBackground(Color.WHITE);
        JLabel rollLabel = new JLabel(String.valueOf(n.rollNumber));
        rollLabel.setForeground(StudentGUI.TEXT_GRAY);
        rollLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightBox.add(rollLabel);
        rightBox.add(buildGradeBadge(gradeText));
        row.add(rightBox, BorderLayout.EAST);

        return row;
    }

    // circular avatar with the student's initials, colored to match the sidebar accent
    private JPanel buildAvatar(String initials) {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(224, 234, 255));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(StudentGUI.BLUE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(initials);
                g2.drawString(initials, (getWidth() - tw) / 2, getHeight() / 2 + fm.getAscent() / 2 - 2);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(36, 36));
        return avatar;
    }

    private String getInitials(String name) {
        String[] words = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
            if (sb.length() >= 2) break;
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    // rounded pill showing the grade, colored by performance tier
    private JComponent buildGradeBadge(String gradeText) {
        int gradeValue;
        try {
            gradeValue = Integer.parseInt(gradeText.trim());
        } catch (NumberFormatException ex) {
            gradeValue = 0;
        }

        final Color bg, fg;
        if (gradeValue >= 90) {
            bg = new Color(220, 252, 231);
            fg = StudentGUI.GREEN;
        } else if (gradeValue >= 75) {
            bg = new Color(219, 234, 254);
            fg = StudentGUI.BLUE;
        } else {
            bg = new Color(254, 226, 226);
            fg = StudentGUI.RED;
        }

        JLabel badge = new JLabel(gradeText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setForeground(fg);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return badge;
    }

    // ---- refreshes the full records table ----
    private void refreshTable() {
        cancelTableEditing();
        gui.tableModel.setRowCount(0);
        List<StudentBST.Node> all = tree.inorder();
        for (StudentBST.Node n : all) {
            addRowToTable(n);
        }
        gui.recordCountLabel.setText(all.size() + " students found");
        refreshTraversals();
    }

    // ---- updates the traversal output panel with node chips ----
    private void showTraversal(String title, List<StudentBST.Node> nodes, Color accent) {
        gui.traversalTitleLabel.setText(title);
        gui.traversalTitleLabel.setForeground(accent);
        gui.traversalOutputPanel.removeAll();

        if (nodes.isEmpty()) {
            JLabel empty = new JLabel("  (no student records in the tree)");
            empty.setForeground(StudentGUI.TEXT_GRAY);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gui.traversalOutputPanel.add(empty);
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                StudentBST.Node n = nodes.get(i);
                String[] parts = n.grade.split("\\|");
                String grade = parts.length > 1 ? parts[1] : parts[0];
                gui.traversalOutputPanel.add(buildNodeChip(n.rollNumber, n.name, grade, accent));
                if (i < nodes.size() - 1) {
                    JLabel arrow = new JLabel("\u2192");
                    arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    arrow.setForeground(new Color(180, 185, 195));
                    gui.traversalOutputPanel.add(arrow);
                }
            }
        }
        gui.traversalOutputPanel.revalidate();
        gui.traversalOutputPanel.repaint();
    }

    /** Builds a styled node chip showing ID, name, and grade. */
    private JPanel buildNodeChip(int id, String name, String grade, Color accent) {
        Color bgColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 18);

        JPanel chip = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));

        JLabel idLabel = new JLabel("ID: " + id);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        idLabel.setForeground(accent);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setForeground(StudentGUI.TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel gradeLabel = new JLabel("Grade: " + grade);
        gradeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        gradeLabel.setForeground(StudentGUI.TEXT_GRAY);
        gradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        chip.add(idLabel);
        chip.add(nameLabel);
        chip.add(gradeLabel);
        return chip;
    }

    // ---- re-runs whichever traversal is currently active after data changes ----
    private void refreshTraversals() {
        // Reset the display so stale data isn't shown after add/delete
        gui.traversalOutputPanel.removeAll();
        JLabel msg = new JLabel("  Data updated — click a traversal button to refresh.");
        msg.setForeground(StudentGUI.TEXT_GRAY);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gui.traversalOutputPanel.add(msg);
        gui.traversalTitleLabel.setText("Select a traversal type above");
        gui.traversalTitleLabel.setForeground(StudentGUI.TEXT_GRAY);
        // Reset all buttons to outline state
        gui.setActiveTraversalButton(null, null);
        gui.traversalOutputPanel.revalidate();
        gui.traversalOutputPanel.repaint();
    }

    private void showBstTraversal(String title, List<StudentBST.Node> nodes, Color accent) {
        gui.bstTraversalTitleLabel.setText(title);
        gui.bstTraversalTitleLabel.setForeground(accent);
        gui.bstTraversalOutputPanel.removeAll();

        if (nodes.isEmpty()) {
            JLabel empty = new JLabel("  (no student records in the tree)");
            empty.setForeground(StudentGUI.TEXT_GRAY);
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            gui.bstTraversalOutputPanel.add(empty);
        } else {
            for (int i = 0; i < nodes.size(); i++) {
                StudentBST.Node n = nodes.get(i);
                String[] parts = n.grade.split("\\|");
                String grade = parts.length > 1 ? parts[1] : parts[0];
                gui.bstTraversalOutputPanel.add(buildNodeChip(n.rollNumber, n.name, grade, accent));
                if (i < nodes.size() - 1) {
                    JLabel arrow = new JLabel("\u2192");
                    arrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    arrow.setForeground(new Color(180, 185, 195));
                    gui.bstTraversalOutputPanel.add(arrow);
                }
            }
        }
        gui.bstTraversalOutputPanel.revalidate();
        gui.bstTraversalOutputPanel.repaint();
    }

    private void refreshBstTraversals() {
        if (gui.bstTraversalOutputPanel == null) return;
        gui.bstTraversalOutputPanel.removeAll();
        JLabel msg = new JLabel("  Click Inorder, Preorder, or Postorder above to highlight the traversal order.");
        msg.setForeground(StudentGUI.TEXT_GRAY);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gui.bstTraversalOutputPanel.add(msg);
        gui.bstTraversalTitleLabel.setText("Select a traversal type above");
        gui.bstTraversalTitleLabel.setForeground(StudentGUI.TEXT_GRAY);
        gui.setActiveBstTraversalButton(null, null);
        gui.treePanel.setTraversal(null, null);
        gui.bstTraversalOutputPanel.revalidate();
        gui.bstTraversalOutputPanel.repaint();
    }

    private void addRowToTable(StudentBST.Node n) {
        String[] parts = n.grade.split("\\|");
        String course = parts[0];
        String grade = parts.length > 1 ? parts[1] : "";

        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        JButton viewBtn = new JButton("View");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");

        // Small button size
        Dimension buttonSize = new Dimension(50, 25);

        for (JButton btn : new JButton[]{viewBtn, editBtn, deleteBtn}) {
            btn.setPreferredSize(buttonSize);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 200), 1));
            btn.setMargin(new Insets(0, 1, 0, 1));
        }

        // View - soft blue
        viewBtn.setBackground(new Color(235, 244, 255));
        viewBtn.setForeground(new Color(40, 100, 180));

        // Edit - soft amber
        editBtn.setBackground(new Color(254, 243, 199));
        editBtn.setForeground(new Color(180, 83, 9));

        // Delete - soft red
        deleteBtn.setBackground(new Color(255, 238, 238));
        deleteBtn.setForeground(new Color(200, 60, 60));

        viewBtn.addActionListener(e -> handleView(n.rollNumber));
        editBtn.addActionListener(e -> handleEdit(n.rollNumber));
        deleteBtn.addActionListener(e -> handleDelete(n.rollNumber));

        actionPanel.add(viewBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);

        gui.tableModel.addRow(new Object[] {
                n.rollNumber, n.name, course, grade, actionPanel
        });
    }

    private int extractGrade(String stored) {
        String[] parts = stored.split("\\|");
        try {
            return Integer.parseInt(parts.length > 1 ? parts[1] : parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---- FILE persistence: save all records ----
    // Saves in PREORDER so the BST shape is identical when records are reloaded.
    private void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (StudentBST.Node n : tree.preorder()) {
                writer.write(n.rollNumber + "," + n.name + "," + n.grade);
                writer.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ---- FILE persistence: load records on startup ----
    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", 3);
                if (data.length < 3) continue;
                int roll = Integer.parseInt(data[0]);
                tree.insert(roll, data[1], data[2]);
            }
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
}