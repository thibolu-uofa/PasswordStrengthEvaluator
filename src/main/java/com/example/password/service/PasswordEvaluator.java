package com.example.password.service;

import com.example.password.model.PasswordAnalysis;
import com.example.password.model.StrengthLevel;

import java.util.ArrayList;
import java.util.List;

public class PasswordEvaluator {

    public PasswordAnalysis evaluate(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        int score = 0;
        List<String> feedback = new ArrayList<>();

        int len = password.length();
        boolean hasMinLength = len >= 8;
        boolean hasLongLength = len >= 12;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        // 1. Length Points
        if (hasMinLength) {
            score += 20;
        } else {
            feedback.add("Password must be at least 8 characters long.");
        }

        if (hasLongLength) {
            score += 15;
        }

        // 2. Character Diversity Points (15 points each)
        if (hasUpper) {
            score += 15;
        } else {
            feedback.add("Add at least one uppercase letter.");
        }

        if (hasLower) {
            score += 15;
        } else {
            feedback.add("Add at least one lowercase letter.");
        }

        if (hasDigit) {
            score += 15;
        } else {
            feedback.add("Add at least one number.");
        }

        if (hasSpecial) {
            score += 20;
        } else {
            feedback.add("Add at least one special character.");
        }

        // Penalty for spaces
        if (password.contains(" ")) {
            score -= 10;
            feedback.add("Avoid spaces in the password.");
        }

        // Clamp score between 0 and 100
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        StrengthLevel level = determineLevel(score);

        return new PasswordAnalysis(
                score, level, hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial, feedback
        );
    }

    public StrengthLevel determineLevel(int score) {
        if (score < 30) {
            return StrengthLevel.VERY_WEAK;
        } else if (score < 50) {
            return StrengthLevel.WEAK;
        } else if (score < 70) {
            return StrengthLevel.MEDIUM;
        } else if (score < 85) {
            return StrengthLevel.STRONG;
        } else {
            return StrengthLevel.VERY_STRONG;
        }
    }
}
