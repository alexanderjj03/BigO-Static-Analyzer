package analyzer.complexity;

// DEPRECATED
public class BigOComplexityResult {
    private int lineNum;
    private String bigOComplexity;
    private String size;
    private BigOEquation bigOEquation;

    public BigOComplexityResult(int lineNum, String bigOComplexity, String size) {
        this.lineNum = lineNum;
        this.bigOComplexity = bigOComplexity;
        this.size = size;
    }

    public BigOComplexityResult(int lineNum, String bigOComplexity, String size, BigOEquation bigOEquation) {
        this.lineNum = lineNum;
        this.bigOComplexity = bigOComplexity;
        this.size = size;
        this.bigOEquation = bigOEquation;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getBigOComplexity() {
        return bigOComplexity;
    }

    public String getSize() {
        return size;
    }

    public BigOEquation getBigOEquation() {
        return bigOEquation;
    }

    public void setBigOEquation(BigOEquation bigOEquation) {
        this.bigOEquation = bigOEquation;
    }
}
