package com.example.password.ui;

import com.example.password.model.PasswordAnalysis;
import com.example.password.model.StrengthLevel;
import com.example.password.service.PasswordEvaluator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PasswordStrengthFrame extends JFrame {

    private final PasswordEvaluator evaluator = new PasswordEvaluator();
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JLabel statusLabel = new JLabel("Enter a password");
    private final JCheckBox chkLength = new JCheckBox("Min 8 Characters");
    private final JCheckBox chkUpper = new JCheckBox("Uppercase Letter");
    private final JCheckBox chkLower = new JCheckBox("Lowercase Letter");
    private final JCheckBox chkDigit = new JCheckBox("Digit");
    private final JCheckBox chkSpecial = new JCheckBox("Special Character");

    public PasswordStrengthFrame() {
        setTitle("Password Strength Evaluator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Input
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topPanel.add(new JLabel("Password:"));
        topPanel.add(passwordField);

        // Center Panel: Status & Requirements
        JPanel centerPanel = new JPanel(new GridLayout(6, 1, 2, 2));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        chkLength.setEnabled(false);
        chkUpper.setEnabled(false);
        chkLower.setEnabled(false);
        chkDigit.setEnabled(false);
        chkSpecial.setEnabled(false);

        centerPanel.add(chkLength);
        centerPanel.add(chkUpper);
        centerPanel.add(chkLower);
        centerPanel.add(chkDigit);
        centerPanel.add(chkSpecial);

        // Bottom Panel: Progress bar & Label
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        progressBar.setStringPainted(true);
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // DocumentListener for real-time calculation
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
        });
    }

    private void updateStrength() {
        String pwd = new String(passwordField.getPassword());
        PasswordAnalysis analysis = evaluator.evaluate(pwd);

        progressBar.setValue(analysis.getScore());
        chkLength.setSelected(analysis.isHasMinLength());
        chkUpper.setSelected(analysis.isHasUppercase());
        chkLower.setSelected(analysis.isHasLowercase());
        chkDigit.setSelected(analysis.isHasDigit());
        chkSpecial.setSelected(analysis.isHasSpecialChar());

        StrengthLevel level = analysis.getLevel();
        statusLabel.setText("Strength: " + level.name() + " (Score: " + analysis.getScore() + ")");

        switch (level) {
            case VERY_WEAK:
            case WEAK:
                progressBar.setForeground(Color.RED);
                break;
            case MEDIUM:
                progressBar.setForeground(Color.ORANGE);
                break;
            case STRONG:
                progressBar.setForeground(new Color(34, 139, 34)); // Forest Green
                break;
            case VERY_STRONG:
                progressBar.setForeground(new Color(0, 100, 0)); // Dark Green
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PasswordStrengthFrame().setVisible(true);
        });
    }
}
