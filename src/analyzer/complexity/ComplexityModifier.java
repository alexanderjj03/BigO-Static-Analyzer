package analyzer.complexity;

public enum ComplexityModifier implements Comparable<ComplexityModifier>{
    CONSTANT(1),
    LOG(2),
    POLY(3),
    EXP_2(4),
    FACTORIAL(5);

    private final int rank;

    ComplexityModifier(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public int compareModifier(ComplexityModifier other) {
        return Integer.compare(this.rank, other.rank);
    }
}
