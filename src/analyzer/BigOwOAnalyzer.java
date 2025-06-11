package analyzer;

import analyzer.complexity.BigOEquation;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Analyzes and stores results
// TODO: how do we do parameters
public class BigOwOAnalyzer implements Analyzer {
    // line number to complexity
    private Map<Integer, BigOEquation> resultMap = new HashMap<>();
    private List<ClassOrInterfaceDeclaration> classes = new ArrayList<>();
    private List<FieldDeclaration> fields = new ArrayList<>();
    private List<MethodDeclaration> methods = new ArrayList<>();
    private List<String> allParams;
    private Map<String, String> fieldToParamMap;
    private Map<String, String> methodToParamMap;
    private Map<String, String> argumentToParamMap;
    private BigOEquation methodFinalComplexity;
    private Map<String, BigOEquation> equivalencies;

    public BigOwOAnalyzer() {
    }

    @Override
    // It should also reset when ran again
    public void analyze(CompilationUnit cu) {
        ComplexityVisitor visitor = new ComplexityVisitor();
        visitor.resetLetterGenerator();
        cu.accept(visitor, new HashMap<>());
        allParams = visitor.getAllParameters();
        resultMap = visitor.getLineTimeComplexity();
        fieldToParamMap = visitor.getFieldToParam();
        methodToParamMap = visitor.getMethodToParam();
        argumentToParamMap = visitor.getArgumentToParam();
        methodFinalComplexity = visitor.getMethodFinalComplexity();
        equivalencies = visitor.getEquivalencies();

        System.out.println("RESULT MAP" + resultMap);
        System.out.println("ARGUMENTS TO PARAMS" + argumentToParamMap);
        System.out.println("EQUIVALENCIES" + equivalencies);
    }

    @Override
    public void printComplexities() {
        for (Integer lineNum: this.resultMap.keySet()) {
            System.out.println("Line " + lineNum + ": " + this.resultMap.get(lineNum));
        }
    }

    @Override
    public Map<Integer, BigOEquation> getResults() {
        return resultMap;
    }

    public List<String> getAllParams() {
        return allParams;
    }

    public Map<String, String> getFieldToParamMap() {
        return fieldToParamMap;
    }

    public Map<String, String> getMethodToParamMap() {
        return methodToParamMap;
    }

    public Map<String, String> getArgumentToParamMap() {
        return argumentToParamMap;
    }

    public BigOEquation getMethodFinalComplexity() {
        return methodFinalComplexity;
    }

    public Map<String, BigOEquation> getEquivalencies() {return equivalencies;}
}
