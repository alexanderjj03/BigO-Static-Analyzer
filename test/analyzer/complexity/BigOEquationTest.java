package analyzer.complexity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigOEquationTest {

    BigOEquation bigOEquation;

    @BeforeEach
    void setUp() {
        bigOEquation = new BigOEquation();
    }

    @Test
    void addBigOTerm() {
        bigOEquation.addBigOTerm("a", ComplexityModifier.POLY, 1);
        System.out.println(bigOEquation.toString());
    }

    @Test
    void multiplyBigOTerm() {
        bigOEquation.addBigOTerm("a", ComplexityModifier.POLY, 1);
        bigOEquation.multiplyBigOTerm("a", ComplexityModifier.LOG, 1);
        bigOEquation.addBigOTerm("a", ComplexityModifier.POLY, 2);
        bigOEquation.addBigOTerm("a", ComplexityModifier.POLY, 5);
        bigOEquation.multiplyBigOTerm("b", ComplexityModifier.LOG, 2);
        bigOEquation.getFinalComplexity();
        System.out.println(bigOEquation.toString());
    }

    @Test
    void multiplyBigOTermPolynomial() {
        bigOEquation.multiplyBigOTerm("a", ComplexityModifier.POLY, 1);
        bigOEquation.multiplyBigOTerm("a", ComplexityModifier.POLY, 2);
        bigOEquation.getFinalComplexity();
        System.out.println(bigOEquation.toString());
    }

    @Test
    void addTwoBigOTerms() {
        List<String> list = List.of("a", "b");
        BigOEquation b1 = new BigOEquation();
        BigOEquation b2 = new BigOEquation();
        b1.addBigOTerm("a", ComplexityModifier.POLY, 1);
        b2.addBigOTerm("b", ComplexityModifier.POLY, 1);
        b1.addBigOEquation(b2);
        b1.getFinalComplexity();
        System.out.println(b1.toString());
    }

    @Test
    void addTwoBigOIrregularTerms() {
        List<String> list = List.of("a", "b");
        BigOEquation b1 = new BigOEquation();
        BigOEquation b2 = new BigOEquation();
        b1.addBigOTerm("a", ComplexityModifier.POLY, 1);
        System.out.println(b1);
        b2.addBigOTerm("b", ComplexityModifier.POLY, 2);
        System.out.println(b2);
        b1.addBigOEquation(b2);
        assertEquals(2, b1.getTerms().size());
        assertEquals(b1.getTerms().get(0).get(0).getParam(), "a");
        assertEquals(b1.getTerms().get(1).get(0).getParam(), "b");
        b1.getFinalComplexity();
        assertEquals(2, b1.getTerms().size());
        System.out.println(b1.toString());
    }

    @Test
    void getFinalComplexity() {
        bigOEquation.addBigOTerm("a", ComplexityModifier.POLY, 1);
        bigOEquation.multiplyBigOTerm("a", ComplexityModifier.LOG, 1);
        bigOEquation.getFinalComplexity();
        System.out.println(bigOEquation.toString());
    }

    @Test
    void testBigOMultiplication() {
        List<String> list = List.of("a", "b");
        BigOEquation b1 = new BigOEquation();
        BigOEquation b2 = new BigOEquation();
        b1.addBigOTerm("a", ComplexityModifier.POLY, 1);
        b1.addBigOTerm("b", ComplexityModifier.POLY, 2);
        b1.addBigOTerm("b", ComplexityModifier.LOG, 1);
        b2.addBigOTerm("b", ComplexityModifier.POLY, 3);
        b1.multiplyBigOEquation(b2);
        b1.getFinalComplexity();
        System.out.println(b1.toString());
    }

    @Test
    void testBigOMultiplicationConstant() {
        List<String> list = List.of("a", "b");
        BigOEquation b1 = new BigOEquation();
        BigOEquation b2 = new BigOEquation();
        b1.addBigOTerm("a", ComplexityModifier.POLY, 1);
        b1.multiplyBigOEquation(b2);
        b1.getFinalComplexity();
        System.out.println(b1.toString());
    }

    @Test
    void testBigOFinalOnConstant() {
        List<String> list = List.of("a", "b");
        BigOEquation b1 = new BigOEquation();
        b1.getFinalComplexity();
        System.out.println(b1.toString());
    }
}