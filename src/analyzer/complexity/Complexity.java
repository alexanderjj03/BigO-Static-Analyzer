package analyzer.complexity;

public class Complexity {
    // Default to constant time
    public BigOEquation timeComplexity = new BigOEquation();
    public BigOEquation spaceComplexity = new BigOEquation();

    public Complexity(BigOEquation timeComplexity, BigOEquation spaceComplexity) {
        this.timeComplexity = timeComplexity;
        this.spaceComplexity = spaceComplexity;
    }

    // Constant time constructor
    public Complexity() {
    }

    public Complexity getFinalComplexity() {
        BigOEquation time = timeComplexity.getFinalComplexity();
        BigOEquation space = spaceComplexity.getFinalComplexity();

        return new Complexity(time, space);
    }
}
