import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;

public class OnlineBankingSystem extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;
    JTextField usernameField;
    JPasswordField passwordField;
    JLabel welcomeLabel, balanceLabel;
    double balance;
    String currentUser;
    Connection conn;
    
 
    final String DB_URL = "jdbc:mysql://localhost:3306/banking_system";
    final String DB_USER = "root";
    final String DB_PASS = "reddy123";

    public OnlineBankingSystem() {
        initDatabase();
        setTitle("Online Banking System - Multi-User");
        setSize(550, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(loginPanel(), "Login");
        mainPanel.add(dashboardPanel(), "Dashboard");

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
        setVisible(true);
    }

    private void initDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            
            // Create tables if not exists
            String createUsers = "CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(50), balance DOUBLE DEFAULT 5000.0)";
            String createTransactions = "CREATE TABLE IF NOT EXISTS transactions (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50), type VARCHAR(20), amount DOUBLE, date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            Statement stmt = conn.createStatement();
            stmt.execute(createUsers);
            stmt.execute(createTransactions);
            stmt.close();
            
        
            PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO users (username, password, balance) VALUES ('admin', '1234', 5000.0)");
            ps.executeUpdate();
            ps.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private JPanel loginPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(225, 245, 254));

        JLabel title = new JLabel("Multi-User Banking System", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBounds(120, 30, 310, 30);
        panel.add(title);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(100, 90, 100, 25);
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(200, 90, 200, 25);
        panel.add(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(100, 130, 100, 25);
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 130, 200, 25);
        panel.add(passwordField);

        JButton okBtn = new JButton("Login");
        okBtn.setBounds(200, 190, 100, 30);
        okBtn.setBackground(new Color(30, 136, 229));
        okBtn.setForeground(Color.WHITE);

        okBtn.addActionListener(e -> {
            authenticateUser();
        });
        panel.add(okBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(320, 190, 100, 30);
        registerBtn.setBackground(Color.GREEN);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.addActionListener(e -> registerNewUser());
        panel.add(registerBtn);

        return panel;
    }

    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                currentUser = username;
                balance = rs.getDouble("balance");
                welcomeLabel.setText("Welcome, " + username + "!");
                balanceLabel.setText("Balance: ₹" + String.format("%.2f", balance));
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage());
        }
    }

    private void registerNewUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password");
            return;
        }
        
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password, balance) VALUES (?, ?, 5000.0)");
            ps.setString(1, username);
            ps.setString(2, password);
            int result = ps.executeUpdate();
            
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "User registered successfully! You can now login.");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            }
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
        }
    }

    private JPanel dashboardPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(232, 245, 233));

        welcomeLabel = new JLabel("Welcome!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setBounds(150, 20, 250, 30);
        panel.add(welcomeLabel);

        balanceLabel = new JLabel("Balance: ₹0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setBounds(180, 60, 200, 25);
        panel.add(balanceLabel);

        JButton depositBtn = new JButton("Deposit");
        depositBtn.setBounds(100, 110, 120, 30);
        depositBtn.addActionListener(e -> depositMoney());
        panel.add(depositBtn);

        JButton withdrawBtn = new JButton("Withdraw");
        withdrawBtn.setBounds(280, 110, 120, 30);
        withdrawBtn.addActionListener(e -> withdrawMoney());
        panel.add(withdrawBtn);

        JButton historyBtn = new JButton("Transaction History");
        historyBtn.setBounds(160, 160, 200, 30);
        historyBtn.addActionListener(e -> showTransactionHistory());
        panel.add(historyBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(200, 220, 120, 30);
        logoutBtn.setBackground(Color.LIGHT_GRAY);
        logoutBtn.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            cardLayout.show(mainPanel, "Login");
        });
        panel.add(logoutBtn);

        return panel;
    }

    private void depositMoney() {
        String input = JOptionPane.showInputDialog(this, "Enter deposit amount:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount > 0) {
                    balance += amount;
                    updateBalance();
                    logTransaction("Deposited", amount);
                    JOptionPane.showMessageDialog(this, "₹" + String.format("%.2f", amount) + " deposited successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Enter a valid positive amount.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }
    }

    private void withdrawMoney() {
        String input = JOptionPane.showInputDialog(this, "Enter withdrawal amount:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount > 0 && amount <= balance) {
                    balance -= amount;
                    updateBalance();
                    logTransaction("Withdrew", amount);
                    JOptionPane.showMessageDialog(this, "₹" + String.format("%.2f", amount) + " withdrawn successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid amount or insufficient balance.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }
    }

    private void updateBalance() {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = ? WHERE username = ?");
            ps.setDouble(1, balance);
            ps.setString(2, currentUser);
            ps.executeUpdate();
            ps.close();
            
            balanceLabel.setText("Balance: ₹" + String.format("%.2f", balance));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void logTransaction(String type, double amount) {
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO transactions (username, type, amount) VALUES (?, ?, ?)");
            ps.setString(1, currentUser);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("Transaction log failed: " + ex.getMessage());
        }
    }

    private void showTransactionHistory() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT type, amount, date FROM transactions WHERE username = ? ORDER BY date DESC LIMIT 20");
            ps.setString(1, currentUser);
            ResultSet rs = ps.executeQuery();
            
            StringBuilder history = new StringBuilder("Transaction History for " + currentUser + ":\n\n");
            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                Timestamp date = rs.getTimestamp("date");
                history.append(type).append(" ₹").append(String.format("%.2f", amount))
                      .append(" on ").append(date.toString()).append("\n");
            }
            
            JTextArea area = new JTextArea(history.toString());
            area.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Transaction History", JOptionPane.INFORMATION_MESSAGE);
            
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "History fetch failed: " + ex.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
        super.finalize();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineBankingSystem());
    }
}
