package com.example.password.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UI tests for {@link PasswordStrengthFrame}.
 *
 * The frame exposes no public getters for its Swing components, so these tests
 * reach into the private fields via reflection and drive the real DocumentListener
 * by mutating the password field's text, exactly as a user typing would.
 */
class PasswordStrengthFrameTest {

    private PasswordStrengthFrame frame;
    private JPasswordField passwordField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JCheckBox chkLength;
    private JCheckBox chkUpper;
    private JCheckBox chkLower;
    private JCheckBox chkDigit;
    private JCheckBox chkSpecial;

    @BeforeAll
    static void requireDisplay() {
        // JFrame is a heavyweight AWT Window and throws HeadlessException the moment
        // it's constructed on a machine with no display. Skip rather than fail on CI
        // boxes that don't run an X server (e.g. no Xvfb installed).
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "Skipping UI tests: no display available in this environment");
    }

    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> frame = new PasswordStrengthFrame());

        passwordField = field("passwordField");
        progressBar = field("progressBar");
        statusLabel = field("statusLabel");
        chkLength = field("chkLength");
        chkUpper = field("chkUpper");
        chkLower = field("chkLower");
        chkDigit = field("chkDigit");
        chkSpecial = field("chkSpecial");
    }

    @AfterEach
    void tearDown() {
        frame.dispose();
    }

    @SuppressWarnings("unchecked")
    private <T> T field(String name) throws Exception {
        Field f = PasswordStrengthFrame.class.getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(frame);
    }

    /** Sets the password field's text, which synchronously fires the DocumentListener. */
    private void type(String password) throws Exception {
        SwingUtilities.invokeAndWait(() -> passwordField.setText(password));
    }

    @Test
    void testWindowConfiguration() {
        assertEquals("Password Strength Evaluator", frame.getTitle());
        assertEquals(JFrame.EXIT_ON_CLOSE, frame.getDefaultCloseOperation());
    }

    @Test
    void testInitialState() {
        assertEquals(0, progressBar.getValue());
        assertEquals("Enter a password", statusLabel.getText());
        assertFalse(chkLength.isSelected());
        assertFalse(chkUpper.isSelected());
        assertFalse(chkLower.isSelected());
        assertFalse(chkDigit.isSelected());
        assertFalse(chkSpecial.isSelected());
    }

    @Test
    void testCheckboxesAreDisplayOnlyAndNotUserEditable() {
        assertFalse(chkLength.isEnabled());
        assertFalse(chkUpper.isEnabled());
        assertFalse(chkLower.isEnabled());
        assertFalse(chkDigit.isEnabled());
        assertFalse(chkSpecial.isEnabled());
    }

    @Test
    void testWeakPasswordShowsRedBarAndCorrectStatus() throws Exception {
        type("abc");

        assertEquals(Color.RED, progressBar.getForeground());
        assertEquals("Strength: VERY_WEAK (Score: 15)", statusLabel.getText());
        assertFalse(chkLength.isSelected());
        assertTrue(chkLower.isSelected());
        assertFalse(chkUpper.isSelected());
        assertFalse(chkDigit.isSelected());
        assertFalse(chkSpecial.isSelected());
    }

    @Test
    void testMediumPasswordShowsOrangeBar() throws Exception {
        // length 8, upper+lower+digit, no special -> 20 + 15 + 15 + 15 = 65 (MEDIUM)
        type("Abcdef1a");

        assertEquals(65, progressBar.getValue());
        assertEquals(Color.ORANGE, progressBar.getForeground());
        assertEquals("Strength: MEDIUM (Score: 65)", statusLabel.getText());
    }

    @Test
    void testStrongPasswordShowsForestGreenBar() throws Exception {
        // length 8, upper+lower+special but no digit -> 20+15+15+20 = 70 (exact STRONG boundary)
        type("Abcdefg!");

        assertEquals(70, progressBar.getValue());
        assertEquals(new Color(34, 139, 34), progressBar.getForeground());
        assertEquals("Strength: STRONG (Score: 70)", statusLabel.getText());
        assertFalse(chkDigit.isSelected());
    }

    @Test
    void testVeryStrongPasswordShowsDarkGreenBar() throws Exception {
        // length 12+, every character class -> 100 (VERY_STRONG)
        type("Abcdef123!@#");

        assertEquals(100, progressBar.getValue());
        assertEquals(new Color(0, 100, 0), progressBar.getForeground());
        assertEquals("Strength: VERY_STRONG (Score: 100)", statusLabel.getText());
        assertTrue(chkLength.isSelected());
        assertTrue(chkUpper.isSelected());
        assertTrue(chkLower.isSelected());
        assertTrue(chkDigit.isSelected());
        assertTrue(chkSpecial.isSelected());
    }

    @Test
    void testMinLengthBoundaryReflectedInCheckbox() throws Exception {
        type("A1!aaaaa"); // length 8
        assertTrue(chkLength.isSelected());

        type("A1!aaaa"); // length 7
        assertFalse(chkLength.isSelected());
    }

    @Test
    void testProgressBarUpdatesAsUserTypes() throws Exception {
        type("a");
        assertEquals(15, progressBar.getValue());

        type("ab");
        assertEquals(15, progressBar.getValue());

        type("");
        assertEquals(0, progressBar.getValue());
        assertEquals("Strength: VERY_WEAK (Score: 0)", statusLabel.getText());
    }

    @Test
    void testEachCharacterClassTogglesItsOwnCheckbox() throws Exception {
        type("aaaaaaaa"); // lowercase only
        assertTrue(chkLower.isSelected());
        assertFalse(chkUpper.isSelected());
        assertFalse(chkDigit.isSelected());
        assertFalse(chkSpecial.isSelected());

        type("AAAAAAAA"); // uppercase only
        assertTrue(chkUpper.isSelected());
        assertFalse(chkLower.isSelected());

        type("12345678"); // digits only
        assertTrue(chkDigit.isSelected());
        assertFalse(chkUpper.isSelected());
        assertFalse(chkLower.isSelected());

        type("!!!!!!!!"); // special only
        assertTrue(chkSpecial.isSelected());
        assertFalse(chkDigit.isSelected());
    }
}
