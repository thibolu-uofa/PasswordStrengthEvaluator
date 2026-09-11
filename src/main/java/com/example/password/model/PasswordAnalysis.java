package com.example.password.model;

import java.util.List;

public class PasswordAnalysis {
    private final int score;
    private final StrengthLevel level;
    private final boolean hasMinLength;
    private final boolean hasUppercase;
    private final boolean hasLowercase;
    private final boolean hasDigit;
    private final boolean hasSpecialChar;
    private final List<String> feedbackMessages;

    public PasswordAnalysis(int score, StrengthLevel level, boolean hasMinLength,
                            boolean hasUppercase, boolean hasLowercase,
                            boolean hasDigit, boolean hasSpecialChar,
                            List<String> feedbackMessages) {
        this.score = score;
        this.level = level;
        this.hasMinLength = hasMinLength;
        this.hasUppercase = hasUppercase;
        this.hasLowercase = hasLowercase;
        this.hasDigit = hasDigit;
        this.hasSpecialChar = hasSpecialChar;
        this.feedbackMessages = feedbackMessages;
    }

    public int getScore() { return score; }
    public StrengthLevel getLevel() { return level; }
    public boolean isHasMinLength() { return hasMinLength; }
    public boolean isHasUppercase() { return hasUppercase; }
    public boolean isHasLowercase() { return hasLowercase; }
    public boolean isHasDigit() { return hasDigit; }
    public boolean isHasSpecialChar() { return hasSpecialChar; }
    public List<String> getFeedbackMessages() { return feedbackMessages; }
}
