package analyzer;

import analyzer.complexity.BigOEquation;
import com.github.javaparser.ast.CompilationUnit;

import java.util.Map;

public interface Analyzer {
    public void analyze(CompilationUnit cu);
    public void printComplexities();
    public Map<Integer, BigOEquation> getResults();
}
