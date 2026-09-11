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

    @Test
    void testExactMinLengthBoundary() {
        // Length 8 -> must satisfy the minimum length rule
        assertTrue(evaluator.evaluate("A1!aaaaa").isHasMinLength());
        // Length 7 -> must fail the minimum length rule
        assertFalse(evaluator.evaluate("A1!aaaa").isHasMinLength());
    }

    @Test
    void testExactLongLengthBoundary() {
        // Length 12 -> gets the long-length bonus
        assertEquals(50, evaluator.evaluate("aaaaaaaaaaaa").getScore());
        // Length 11 -> does not get the long-length bonus
        assertEquals(35, evaluator.evaluate("aaaaaaaaaaa").getScore());
    }

    @Test
    void testAllCharacterClassesDetected() {
        PasswordAnalysis result = evaluator.evaluate("Abcdefgh1!");
        assertTrue(result.isHasUppercase());
        assertTrue(result.isHasLowercase());
        assertTrue(result.isHasDigit());
        assertTrue(result.isHasSpecialChar());
    }

    @Test
    void testMissingCharacterClassesGenerateFeedback() {
        PasswordAnalysis result = evaluator.evaluate("aaaaaaaa");
        assertFalse(result.isHasUppercase());
        assertFalse(result.isHasDigit());
        assertFalse(result.isHasSpecialChar());
        assertTrue(result.getFeedbackMessages().contains("Add at least one uppercase letter."));
        assertTrue(result.getFeedbackMessages().contains("Add at least one number."));
        assertTrue(result.getFeedbackMessages().contains("Add at least one special character."));
    }

    @Test
    void testMissingLowercaseGeneratesFeedback() {
        PasswordAnalysis result = evaluator.evaluate("ABCDEFGH1!");
        assertFalse(result.isHasLowercase());
        assertTrue(result.getFeedbackMessages().contains("Add at least one lowercase letter."));
    }

    @Test
    void testSpacePenaltyApplied() {
        PasswordAnalysis withSpace = evaluator.evaluate("Aa1! aaaa");
        PasswordAnalysis withoutSpace = evaluator.evaluate("Aa1!aaaaa");
        assertEquals(withoutSpace.getScore() - 10, withSpace.getScore());
        assertTrue(withSpace.getFeedbackMessages().contains("Avoid spaces in the password."));
    }

    @Test
    void testScoreForEachCharacterClassCombination() {
        assertEquals(35, evaluator.evaluate("aaaaaaaa").getScore());        // min length + lowercase only
        assertEquals(35, evaluator.evaluate("AAAAAAAA").getScore());        // min length + uppercase only
        assertEquals(35, evaluator.evaluate("12345678").getScore());        // min length + digit only
        assertEquals(40, evaluator.evaluate("!!!!!!!!").getScore());        // min length + special only
        assertEquals(85, evaluator.evaluate("Aa1!Aa1!").getScore());        // every class, no long bonus
        assertEquals(100, evaluator.evaluate("Aa1!Aa1!Aa1!").getScore());   // every class + long bonus
        assertEquals(0, evaluator.evaluate("").getScore());                 // nothing at all
        assertEquals(15, evaluator.evaluate("short").getScore());          // below min length, lowercase only
    }

    @Test
    void testScoreClampedToOneHundred() {
        PasswordAnalysis result = evaluator.evaluate("Abcdefghijk1!");
        assertEquals(100, result.getScore());
    }

    @Test
    void testDetermineLevelExactBoundaries() {
        assertEquals(StrengthLevel.VERY_WEAK, evaluator.determineLevel(0));
        assertEquals(StrengthLevel.VERY_WEAK, evaluator.determineLevel(29));
        assertEquals(StrengthLevel.WEAK, evaluator.determineLevel(30));
        assertEquals(StrengthLevel.WEAK, evaluator.determineLevel(49));
        assertEquals(StrengthLevel.MEDIUM, evaluator.determineLevel(50));
        assertEquals(StrengthLevel.MEDIUM, evaluator.determineLevel(69));
        assertEquals(StrengthLevel.STRONG, evaluator.determineLevel(70));
        assertEquals(StrengthLevel.STRONG, evaluator.determineLevel(84));
        assertEquals(StrengthLevel.VERY_STRONG, evaluator.determineLevel(85));
        assertEquals(StrengthLevel.VERY_STRONG, evaluator.determineLevel(100));
    }
}
