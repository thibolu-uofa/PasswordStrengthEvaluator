# CLAUDE.md - Password Strength Evaluator & Analyzer

This guide provides instructions and architectural details for building the **Password Strength Evaluator & Swing GUI** project in IntelliJ IDEA.

The project is designed as an educational Java application demonstrating **JUnit 5**, **JaCoCo (Code Coverage)**, and **PIT / Pitest (Mutation Testing)**.

---

## 1. Project Overview & Architecture

### Objectives
1. Build a Java domain service (`PasswordEvaluator`) that assesses password strength, calculates a score (0–100), determines a `StrengthLevel`, and provides actionable feedback.
2. Create a lightweight Java Swing GUI (`PasswordStrengthFrame`) offering real-time visual feedback, progress bar coloring, and requirement checkmarks.
3. Compare **JaCoCo line/branch coverage** with **PIT mutation testing**: show how 100% line coverage can leave boundary mutations undetected until boundary unit tests are added.

### Key Components & Package Structure
```
src/
├── main/
│   └── java/
│       └── com/example/password/
│           ├── model/
│           │   ├── StrengthLevel.java       (Enum: VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG)
│           │   └── PasswordAnalysis.java    (DTO for score, level, boolean flags, feedback list)
│           ├── service/
│           │   └── PasswordEvaluator.java   (Core business logic: rules, scoring, boundaries)
│           └── ui/
│               └── PasswordStrengthFrame.java (Swing GUI window)
└── test/
    └── java/
        └── com/example/password/
            └── PasswordEvaluatorTest.java   (JUnit 5 unit tests targeting mutation killing)
```

---

## 2. Maven Configuration (`pom.xml`)

Ensure your `pom.xml` contains the dependencies for JUnit 5 and the plugins for JaCoCo and Pitest:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>password-strength-evaluator</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.1</junit.version>
    </properties>

    <dependencies>
        <!-- JUnit 5 Jupiter API & Engine -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>

            <!-- JaCoCo Code Coverage Plugin -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

            <!-- PIT / Pitest Mutation Testing Plugin -->
            <plugin>
                <groupId>org.pitest</groupId>
                <artifactId>pitest-maven</artifactId>
                <version>1.15.3</version>
                <dependencies>
                    <dependency>
                        <groupId>org.pitest</groupId>
                        <artifactId>pitest-junit5-plugin</artifactId>
                        <version>1.2.1</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <targetClasses>
                        <param>com.example.password.service.*</param>
                    </targetClasses>
                    <targetTests>
                        <param>com.example.password.*</param>
                    </targetTests>
                    <mutators>
                        <mutator>DEFAULTS</mutator>
                        <mutator>STRONGER</mutator>
                    </mutators>
                    <outputFormats>
                        <outputFormat>HTML</outputFormat>
                    </outputFormats>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Class Implementations

### 3.1 `StrengthLevel.java`
```java
package com.example.password.model;

public enum StrengthLevel {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG
}
```

### 3.2 `PasswordAnalysis.java`
```java
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
```

### 3.3 `PasswordEvaluator.java` (Core Logic)
> **Note for Mutation Testing:** Pay close attention to boundaries (`len >= 8`, `len >= 12`, `score < 30`, `score >= 80`). PIT will mutate `>=` to `>`, `<` to `<=`, and modify arithmetic operators.

```java
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
```

### 3.4 `PasswordStrengthFrame.java` (Swing UI)
```java
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
```

---

## 4. Teaching Guide: JUnit vs. JaCoCo vs. PIT

### Step 1: Write Naive Tests (`PasswordEvaluatorTest.java`)
Create initial tests that pass and yield high JaCoCo coverage:

```java
package com.example.password;

import com.example.password.model.PasswordAnalysis;
import com.example.password.model.StrengthLevel;
import com.example.password.service.PasswordEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEvaluatorTest {

    private PasswordEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PasswordEvaluator();
    }

    @Test
    void testNullPasswordThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(null));
    }

    @Test
    void testWeakPassword() {
        PasswordAnalysis result = evaluator.evaluate("abc");
        assertEquals(StrengthLevel.VERY_WEAK, result.getLevel());
        assertFalse(result.isHasMinLength());
    }

    @Test
    void testStrongPassword() {
        PasswordAnalysis result = evaluator.evaluate("Abcdef123!");
        assertTrue(result.getScore() > 70);
        assertTrue(result.isHasUppercase());
        assertTrue(result.isHasSpecialChar());
    }
}
```

### Step 2: Run JaCoCo Coverage
Execute the Maven command:
```bash
mvn clean test jacoco:report
```
* **Result:** Open `target/site/jacoco/index.html`. You will see **90%+ line and branch coverage**. Students might assume their test suite is complete!

### Step 3: Run PIT Mutation Testing
Execute the Pitest Maven goal:
```bash
mvn pitest:mutationCoverage
```
* **Result:** Open `target/pit-reports/YYYYMMDDHHMM/index.html`.
* **The "Aha!" Moment:** PIT mutates relational operators (e.g., `len >= 8` into `len > 8`).
  * If no test tests a string of **exact length 8** (e.g., `"Abc123!@"`), the mutant survives!
  * If no test checks the exact boundary score of `30`, `50`, `70`, or `85` in `determineLevel()`, mutants survive!

### Step 4: Add Boundary Tests to Kill Mutants
Add exact boundary tests to `PasswordEvaluatorTest.java`:

```java
@Test
void testExactMinLengthBoundary() {
    // Exact length 8 -> must evaluate hasMinLength as true
    PasswordAnalysis result8 = evaluator.evaluate("A1!aaaaa");
    assertTrue(result8.isHasMinLength(), "Length of 8 must satisfy minimum length");

    // Length 7 -> must evaluate hasMinLength as false
    PasswordAnalysis result7 = evaluator.evaluate("A1!aaaa");
    assertFalse(result7.isHasMinLength(), "Length of 7 must fail minimum length");
}

@Test
void testDetermineLevelExactBoundaries() {
    assertEquals(StrengthLevel.VERY_WEAK, evaluator.determineLevel(29));
    assertEquals(StrengthLevel.WEAK, evaluator.determineLevel(30)); // Tests score < 30 vs score <= 30
    assertEquals(StrengthLevel.WEAK, evaluator.determineLevel(49));
    assertEquals(StrengthLevel.MEDIUM, evaluator.determineLevel(50));
    assertEquals(StrengthLevel.MEDIUM, evaluator.determineLevel(69));
    assertEquals(StrengthLevel.STRONG, evaluator.determineLevel(70));
    assertEquals(StrengthLevel.STRONG, evaluator.determineLevel(84));
    assertEquals(StrengthLevel.VERY_STRONG, evaluator.determineLevel(85));
}
```

Re-run `mvn pitest:mutationCoverage`. The mutation score increases to **100% (All Mutants Killed)**.

---

## 5. IntelliJ IDEA Execution Commands & Tips

1. **Import Project:**
   * Open IntelliJ IDEA -> `File` -> `Open` -> Select the folder containing `pom.xml`.
2. **Run GUI Application:**
   * Right-click `PasswordStrengthFrame.java` -> `Run 'PasswordStrengthFrame.main()'`.
3. **Run Unit Tests:**
   * Right-click `src/test/java` -> `Run 'All Tests'`.
4. **Run JaCoCo Report via Terminal / Maven Tool Window:**
   * In IntelliJ Maven panel: Expand `Plugins` -> `jacoco` -> double-click `jacoco:report`.
5. **Run PIT Mutation Testing via Terminal / Maven Tool Window:**
   * In IntelliJ Maven panel: Expand `Plugins` -> `pitest` -> double-click `pitest:mutationCoverage` or run `mvn pitest:mutationCoverage` in Terminal.
