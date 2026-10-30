import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BankingSystem {

    // =============== DESIGN COLORS ===============
    private final Color NAVY = new Color(17, 25, 52);
    private final Color SIDEBAR = new Color(23, 34, 68);
    private final Color BLUE = new Color(59, 91, 219);
    private final Color PURPLE = new Color(121, 75, 220);
    private final Color GREEN = new Color(16, 160, 115);
    private final Color ORANGE = new Color(240, 145, 48);
    private final Color RED = new Color(220, 65, 85);
    private final Color BG = new Color(244, 246, 252);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT = new Color(40, 48, 70);
    private final String RUPEE = "\u20B9";

    private final double DAILY_LIMIT = 20000.0;
    private final String DATA_FILE = "bank_data.dat";

    private JFrame frame;
    private JPanel contentPanel;
    private JLabel statusLabel;

    private Map<String, Account> accounts = new LinkedHashMap<>();

    // =============== TRANSACTION CLASS ===============
    static class Transaction implements Serializable {
        private static final long serialVersionUID = 1L;

        String type;
        double amount;
        double balanceAfter;
        String date;

        Transaction(String type, double amount, double balanceAfter) {
            this.type = type;
            this.amount = amount;
            this.balanceAfter = balanceAfter;
            this.date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            );
        }
    }

    // =============== ACCOUNT CLASS ===============
    static class Account implements Serializable {
        private static final long serialVersionUID = 1L;

        String accountNumber;
        String name;
        String mobile;
        double balance;

        double withdrawnToday = 0;
        LocalDate withdrawalDate = LocalDate.now();

        ArrayList<Transaction> transactions = new ArrayList<>();

        Account(String number, String name, String mobile, double balance) {
            this.accountNumber = number;
            this.name = name;
            this.mobile = mobile;
            this.balance = balance;

            transactions.add(
                new Transaction("Account Opening", balance, balance)
            );
        }
    }

    // =============== MAIN ===============
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
                );
            } catch (Exception ignored) {}

            new BankingSystem();
        });
    }

    public BankingSystem() {
        loadData();
        createGUI();
    }

    // =============== MAIN GUI ===============
    private void createGUI() {
        frame = new JFrame("NEXUS BANK | Banking Management System");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1250, 800);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveData();
                frame.dispose();
            }
        });

        // LEFT SIDEBAR
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR);
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 16, 20, 16));

        JLabel logo = new JLabel("  NEXUS BANK");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 23));
        logo.setForeground(WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("  SMART BANKING SYSTEM");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        logoSub.setForeground(new Color(160, 180, 225));
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(logoSub);
        sidebar.add(Box.createVerticalStrut(38));

        JLabel menuTitle = new JLabel("  MAIN MENU");
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menuTitle.setForeground(new Color(135, 155, 200));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(12));

        addMenu(sidebar, "Dashboard", this::showDashboard, BLUE);
        addMenu(sidebar, "Create Account", this::createAccount, SIDEBAR);
        addMenu(sidebar, "Deposit Money", this::depositMoney, SIDEBAR);
        addMenu(sidebar, "Withdraw Money", this::withdrawMoney, SIDEBAR);
        addMenu(sidebar, "Check Balance", this::checkBalance, SIDEBAR);
        addMenu(sidebar, "Account Details", this::accountDetails, SIDEBAR);
        addMenu(sidebar, "Print Passbook", this::printPassbook, SIDEBAR);
        addMenu(sidebar, "Transaction History", this::transactionHistory, SIDEBAR);

        sidebar.add(Box.createVerticalGlue());

        JPanel limitCard = new JPanel();
        limitCard.setLayout(new BoxLayout(limitCard, BoxLayout.Y_AXIS));
        limitCard.setBackground(new Color(35, 49, 88));
        limitCard.setBorder(new EmptyBorder(15, 12, 15, 12));
        limitCard.setMaximumSize(new Dimension(210, 85));
        limitCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel limitTitle = new JLabel("DAILY WITHDRAWAL LIMIT");
        limitTitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        limitTitle.setForeground(new Color(190, 205, 240));
        limitTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel limitAmount = new JLabel(RUPEE + " 20,000");
        limitAmount.setFont(new Font("Segoe UI", Font.BOLD, 22));
        limitAmount.setForeground(WHITE);
        limitAmount.setAlignmentX(Component.CENTER_ALIGNMENT);

        limitCard.add(limitTitle);
        limitCard.add(Box.createVerticalStrut(5));
        limitCard.add(limitAmount);

        sidebar.add(limitCard);
        sidebar.add(Box.createVerticalStrut(12));

        JButton exit = new JButton("Exit Application");
        styleButton(exit, RED);
        exit.setMaximumSize(new Dimension(210, 42));
        exit.setAlignmentX(Component.LEFT_ALIGNMENT);
        exit.addActionListener(e -> exitApplication());
        sidebar.add(exit);

        // RIGHT SIDE
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WHITE);
        header.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel pageTitle = new JLabel("Banking Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        pageTitle.setForeground(NAVY);

        JLabel profile = new JLabel("ADMIN  |  ONLINE");
        profile.setFont(new Font("Segoe UI", Font.BOLD, 12));
        profile.setForeground(BLUE);

        header.add(pageTitle, BorderLayout.WEST);
        header.add(profile, BorderLayout.EAST);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG);
        contentPanel.setBorder(new EmptyBorder(25, 28, 20, 28));

        statusLabel = new JLabel("  SYSTEM READY");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(110, 120, 145));
        statusLabel.setBorder(new EmptyBorder(8, 20, 12, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(WHITE);

        right.add(header, BorderLayout.NORTH);
        right.add(contentPanel, BorderLayout.CENTER);
        right.add(statusLabel, BorderLayout.SOUTH);

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(right, BorderLayout.CENTER);

        showDashboard();
        frame.setVisible(true);
    }

    // =============== SIDEBAR BUTTONS ===============
    private void addMenu(JPanel panel, String text,
                         Runnable action, Color color) {

        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(213, 43));
        button.setPreferredSize(new Dimension(213, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 5));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        panel.add(button);
        panel.add(Box.createVerticalStrut(5));
    }

    // =============== BUTTON STYLE ===============
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(12, 16, 12, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // =============== DASHBOARD ===============
    private void showDashboard() {
        contentPanel.removeAll();

        JPanel dashboard = new JPanel(new BorderLayout(0, 22));
        dashboard.setBackground(BG);

        // Welcome section
        JPanel welcome = new JPanel();
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        welcome.setBackground(BG);

        JLabel heading = new JLabel("Welcome to Nexus Bank");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 25));
        heading.setForeground(NAVY);

        JLabel subtitle = new JLabel(
            "Your banking activities, organised in one place."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(105, 115, 140));

        welcome.add(heading);
        welcome.add(Box.createVerticalStrut(8));
        welcome.add(subtitle);

        // Statistics cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setBackground(BG);

        cards.add(createStatCard(
            "TOTAL ACCOUNTS",
            String.valueOf(accounts.size()),
            BLUE,
            "Registered customers"
        ));

        cards.add(createStatCard(
            "TOTAL BANK BALANCE",
            money(getTotalBalance()),
            GREEN,
            "Combined account balance"
        ));

        cards.add(createStatCard(
            "TOTAL TRANSACTIONS",
            String.valueOf(getTransactionCount()),
            PURPLE,
            "Recorded transactions"
        ));

        // Quick operations
        JPanel operations = new JPanel(new GridLayout(3, 2, 14, 14));
        operations.setBackground(BG);

        String[] labels = {
            "Create Account",
            "Deposit Money",
            "Withdraw Money",
            "Check Balance",
            "Account Details",
            "Transaction History"
        };

        String[] descriptions = {
            "Register a new customer",
            "Add money to an account",
            "Withdraw money securely",
            "View available balance",
            "View customer information",
            "Review account transactions"
        };

        Color[] colors = {
            BLUE, GREEN, ORANGE, PURPLE, NAVY, RED
        };

        Runnable[] actions = {
            this::createAccount,
            this::depositMoney,
            this::withdrawMoney,
            this::checkBalance,
            this::accountDetails,
            this::transactionHistory
        };

        for (int i = 0; i < labels.length; i++) {
            final Runnable action = actions[i];

            JPanel card = new JPanel(new BorderLayout(5, 8));
            card.setBackground(WHITE);
            card.setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 230, 242), 1, true),
                new EmptyBorder(14, 15, 14, 15)
            ));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(WHITE);

            JLabel accent = new JLabel("  ");
            accent.setOpaque(true);
            accent.setBackground(colors[i]);
            accent.setPreferredSize(new Dimension(5, 35));

            JLabel icon = new JLabel(String.format("%02d", i + 1));
            icon.setForeground(colors[i]);
            icon.setFont(new Font("Segoe UI", Font.BOLD, 18));

            top.add(accent, BorderLayout.WEST);
            top.add(icon, BorderLayout.EAST);

            JLabel name = new JLabel(labels[i]);
            name.setForeground(NAVY);
            name.setFont(new Font("Segoe UI", Font.BOLD, 15));

            JLabel desc = new JLabel(descriptions[i]);
            desc.setForeground(new Color(110, 120, 140));
            desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            card.add(top, BorderLayout.NORTH);
            card.add(name, BorderLayout.CENTER);
            card.add(desc, BorderLayout.SOUTH);

            card.setToolTipText("Click to " + labels[i]);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    action.run();
                }
            });

            operations.add(card);
        }

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(BG);
        center.add(welcome, BorderLayout.NORTH);
        center.add(cards, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 12));
        bottom.setBackground(BG);

        JLabel quick = new JLabel("QUICK OPERATIONS");
        quick.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quick.setForeground(NAVY);

        bottom.add(quick, BorderLayout.NORTH);
        bottom.add(operations, BorderLayout.CENTER);

        dashboard.add(center, BorderLayout.NORTH);
        dashboard.add(bottom, BorderLayout.CENTER);

        contentPanel.add(dashboard, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        statusLabel.setText("  NEXUS BANK  |  Dashboard updated");
    }

    private JPanel createStatCard(String title, String value,
                                  Color accent, String description) {

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(225, 230, 242), 1, true),
            new EmptyBorder(17, 17, 17, 17)
        ));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(5, 4));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(110, 120, 145));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        valueLabel.setForeground(accent);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        descLabel.setForeground(new Color(130, 140, 160));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(WHITE);
        body.add(titleLabel);
        body.add(Box.createVerticalStrut(8));
        body.add(valueLabel);
        body.add(Box.createVerticalStrut(5));
        body.add(descLabel);

        card.add(stripe, BorderLayout.WEST);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    // =============== CREATE ACCOUNT ===============
    private void createAccount() {
        JTextField number = new JTextField();
        JTextField name = new JTextField();
        JTextField mobile = new JTextField();
        JTextField deposit = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 12));
        panel.add(new JLabel("Account Number:"));
        panel.add(number);
        panel.add(new JLabel("Account Holder Name:"));
        panel.add(name);
        panel.add(new JLabel("Mobile Number:"));
        panel.add(mobile);
        panel.add(new JLabel("Initial Deposit:"));
        panel.add(deposit);

        int result = JOptionPane.showConfirmDialog(
            frame, panel, "CREATE NEW ACCOUNT",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String no = number.getText().trim();
        String nm = name.getText().trim();
        String mob = mobile.getText().trim();

        if (no.isEmpty() || nm.isEmpty() || mob.isEmpty()
                || deposit.getText().trim().isEmpty()) {
            error("Please fill in all fields.");
            return;
        }

        if (accounts.containsKey(no)) {
            error("This account number already exists.");
            return;
        }

        if (!mob.matches("[0-9]{10}")) {
            error("Enter a valid 10-digit mobile number.");
            return;
        }

        try {
            double amount = Double.parseDouble(deposit.getText().trim());

            if (!Double.isFinite(amount) || amount < 0) {
                error("Initial deposit cannot be negative.");
                return;
            }

            accounts.put(no, new Account(no, nm, mob, amount));
            saveData();
            showDashboard();

            JOptionPane.showMessageDialog(
                frame,
                "Account created successfully!\n\n"
                + "Account Number: " + no + "\n"
                + "Name: " + nm + "\n"
                + "Balance: " + money(amount),
                "SUCCESS",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {
            error("Enter a valid initial deposit.");
        }
    }

    // =============== DEPOSIT MONEY ===============
    private void depositMoney() {
        Account a = askAccount();
        if (a == null) return;

        String input = JOptionPane.showInputDialog(
            frame, "Enter deposit amount:", "DEPOSIT MONEY",
            JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            double amount = Double.parseDouble(input.trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                error("Amount must be greater than zero.");
                return;
            }

            if (amount > Double.MAX_VALUE - a.balance) {
                error("Amount is too large.");
                return;
            }

            a.balance += amount;
            a.transactions.add(
                new Transaction("Deposit", amount, a.balance)
            );

            saveData();
            showDashboard();

            JOptionPane.showMessageDialog(
                frame,
                "Deposit successful!\n"
                + "Deposited: " + money(amount) + "\n"
                + "New Balance: " + money(a.balance)
            );

        } catch (NumberFormatException e) {
            error("Enter a valid amount.");
        }
    }

    // =============== WITHDRAW MONEY ===============
    private void withdrawMoney() {
        Account a = askAccount();
        if (a == null) return;

        String input = JOptionPane.showInputDialog(
            frame,
            "Available Balance: " + money(a.balance)
            + "\nDaily Limit: " + money(DAILY_LIMIT)
            + "\n\nEnter withdrawal amount:",
            "WITHDRAW MONEY",
            JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        try {
            double amount = Double.parseDouble(input.trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                error("Amount must be greater than zero.");
                return;
            }

            if (amount > a.balance) {
                error("Insufficient balance.");
                return;
            }

            if (!LocalDate.now().equals(a.withdrawalDate)) {
                a.withdrawnToday = 0;
                a.withdrawalDate = LocalDate.now();
            }

            if (a.withdrawnToday + amount > DAILY_LIMIT) {
                double remaining = DAILY_LIMIT - a.withdrawnToday;

                error("Daily withdrawal limit exceeded.\nRemaining today: "
                    + money(remaining));
                return;
            }

            a.balance -= amount;
            a.withdrawnToday += amount;
            a.transactions.add(
                new Transaction("Withdrawal", amount, a.balance)
            );

            saveData();
            showDashboard();

            JOptionPane.showMessageDialog(
                frame,
                "Withdrawal successful!\n"
                + "Withdrawn: " + money(amount) + "\n"
                + "Remaining Balance: " + money(a.balance)
            );

        } catch (NumberFormatException e) {
            error("Enter a valid amount.");
        }
    }

    // =============== CHECK BALANCE ===============
    private void checkBalance() {
        Account a = askAccount();
        if (a == null) return;

        JOptionPane.showMessageDialog(
            frame,
            "ACCOUNT BALANCE\n\n"
            + "Account Holder: " + a.name + "\n"
            + "Account Number: " + a.accountNumber + "\n"
            + "Available Balance: " + money(a.balance),
            "CHECK BALANCE",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // =============== ACCOUNT DETAILS ===============
    private void accountDetails() {
        Account a = askAccount();
        if (a == null) return;

        JOptionPane.showMessageDialog(
            frame,
            "ACCOUNT DETAILS\n"
            + "--------------------------------\n"
            + "Account Number: " + a.accountNumber + "\n"
            + "Account Holder: " + a.name + "\n"
            + "Mobile Number: " + a.mobile + "\n"
            + "Current Balance: " + money(a.balance) + "\n"
            + "Daily Withdrawal Limit: " + money(DAILY_LIMIT) + "\n"
            + "Withdrawn Today: " + money(getWithdrawnToday(a)),
            "ACCOUNT DETAILS",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private double getWithdrawnToday(Account a) {
        if (!LocalDate.now().equals(a.withdrawalDate)) {
            a.withdrawnToday = 0;
            a.withdrawalDate = LocalDate.now();
            saveData();
        }
        return a.withdrawnToday;
    }

    // =============== TRANSACTION HISTORY ===============
    private void transactionHistory() {
        Account a = askAccount();
        if (a == null) return;

        String[] columns = {
            "Date & Time", "Transaction Type", "Amount", "Balance"
        };

        Object[][] data = new Object[a.transactions.size()][4];

        for (int i = 0; i < a.transactions.size(); i++) {
            Transaction t = a.transactions.get(i);
            data[i][0] = t.date;
            data[i][1] = t.type;
            data[i][2] = money(t.amount);
            data[i][3] = money(t.balanceAfter);
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(27);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(
            new Font("Segoe UI", Font.BOLD, 12)
        );

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(730, 320));

        JOptionPane.showMessageDialog(
            frame, scroll, "TRANSACTION HISTORY",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // =============== PRINT PASSBOOK ===============
    private void printPassbook() {
        Account a = askAccount();
        if (a == null) return;

        StringBuilder sb = new StringBuilder();

        sb.append("                NEXUS BANK\n");
        sb.append("                  PASSBOOK\n");
        sb.append("============================================\n");
        sb.append("Account Number: ").append(a.accountNumber).append("\n");
        sb.append("Account Holder: ").append(a.name).append("\n");
        sb.append("Mobile Number: ").append(a.mobile).append("\n");
        sb.append("============================================\n\n");

        sb.append(String.format(
            "%-20s %-18s %-12s %-12s%n",
            "Date & Time", "Type", "Amount", "Balance"
        ));

        sb.append(
            "--------------------------------------------------------------\n"
        );

        for (Transaction t : a.transactions) {
            sb.append(String.format(
                "%-20s %-18s %-12.2f %-12.2f%n",
                t.date, t.type, t.amount, t.balanceAfter
            ));
        }

        sb.append("\nCurrent Balance: ").append(money(a.balance));
        sb.append("\n============================================");
        sb.append("\n          Thank you for banking with us!");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(750, 400));

        Object[] options = {"Print", "Close"};

        int choice = JOptionPane.showOptionDialog(
            frame, scroll, "PASSBOOK",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]
        );

        if (choice == 0) {
            try {
                area.print();
            } catch (PrinterException e) {
                error("Printing failed: " + e.getMessage());
            }
        }
    }

    // =============== FIND ACCOUNT ===============
    private Account askAccount() {
        if (accounts.isEmpty()) {
            error("No accounts found. Create an account first.");
            return null;
        }

        String no = JOptionPane.showInputDialog(
            frame, "Enter Account Number:", "FIND ACCOUNT",
            JOptionPane.QUESTION_MESSAGE
        );

        if (no == null) return null;

        Account a = accounts.get(no.trim());

        if (a == null) {
            error("Account not found. Check the account number.");
            return null;
        }

        return a;
    }

    // =============== TOTAL BALANCE ===============
    private double getTotalBalance() {
        double total = 0;

        for (Account a : accounts.values()) {
            total += a.balance;
        }

        return total;
    }

    private int getTransactionCount() {
        int count = 0;

        for (Account a : accounts.values()) {
            count += a.transactions.size();
        }

        return count;
    }

    // =============== SAVE DATA ===============
    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(DATA_FILE))) {
            out.writeObject(accounts);
        } catch (IOException e) {
            error("Unable to save data: " + e.getMessage());
        }
    }

    // =============== LOAD DATA ===============
    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);

        if (!file.exists()) return;

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {

            Object object = in.readObject();

            if (object instanceof Map<?, ?>) {
                accounts = (Map<String, Account>) object;
            }

        } catch (IOException | ClassNotFoundException e) {
            accounts = new LinkedHashMap<>();
            JOptionPane.showMessageDialog(
                null,
                "Could not load saved data.\n" + e.getMessage(),
                "Data Warning",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // =============== HELPER METHODS ===============
    private String money(double amount) {
        return RUPEE + String.format("%,.2f", amount);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(
            frame, message, "NEXUS BANK",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // =============== EXIT ===============
    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(
            frame,
            "Are you sure you want to exit Nexus Bank?",
            "CONFIRM EXIT",
            JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            saveData();
            frame.dispose();
        }
    }
}