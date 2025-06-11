package analyzer;

import analyzer.complexity.BigOComplexityResult;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.*;
import java.util.stream.Collectors;

// DEPRECATED CLASS
// Visitor Pattern, traverse through the Java AST and get Complexity Results
public class Evaluator extends GenericVisitorAdapter<BigOComplexityResult, Map<Integer, String>> {
    private static UniqueLetterGenerator letterGenerator = new UniqueLetterGenerator();
    private static List<String> params;
    private static String currVarDeclName;
    private static Map<String, String> funcNameToVariable = new HashMap<>();

    @Override
    public BigOComplexityResult visit(MethodDeclaration n, Map<Integer, String> arg) {
        params = n.getParameters().stream().map(Parameter::getNameAsString).collect(Collectors.toList());
        int lineNum = getLineNum(n);
        Optional<BlockStmt> optionalBody = n.getBody();

        if (optionalBody.isEmpty()) {
            return new BigOComplexityResult(lineNum, "1", null);
        }

        return optionalBody.get().accept(this, arg);
    }

    @Override
    public BigOComplexityResult visit(BlockStmt n, Map<Integer, String> arg) {
        List<BigOComplexityResult> results = new ArrayList<>();
        n.getStatements().forEach(stmt ->
            results.add(stmt.accept(this, arg))
        );

        String dominantComplexityTerm = "";
        for (BigOComplexityResult bigO : results) {
            System.out.println("====");
            System.out.println("Line: " + bigO.getLineNum());
            System.out.println("Big O: " + bigO.getBigOComplexity());
            System.out.println("Size : " + bigO.getSize());
        }

        // TODO: find largest term for bigO complexity
        return new BigOComplexityResult(getLineNum(n), results.get(1).getBigOComplexity(), null);
    }

    @Override
    public BigOComplexityResult visit(IfStmt f, Map<Integer, String> arg) {
        List<BigOComplexityResult> results = traverseIfStmt(f, new ArrayList<>(), arg);
        // TODO: (same thing as above)
        return results.get(0);
    }


    // Traverses if statement and all the else statements, returns bigOComplexityResults of each
    private List<BigOComplexityResult> traverseIfStmt(IfStmt f, List<BigOComplexityResult> cur,
                                                      Map<Integer, String> arg) {
        cur.add(f.getThenStmt().accept(this, arg));
        if (f.getElseStmt().isPresent()) {
            if (f.getElseStmt().get() instanceof IfStmt e) {
                traverseIfStmt(e, cur, arg);
            } else {
                cur.add(f.getElseStmt().get().accept(this, arg));
            }
        }

        return cur;
    }

    @Override
    public BigOComplexityResult visit(ExpressionStmt n, Map<Integer, String> arg) {
        return n.getExpression().accept(this, arg);
    }

    @Override
    public BigOComplexityResult visit(VariableDeclarationExpr n, Map<Integer, String> arg) {
        currVarDeclName = n.getVariable(0).getNameAsString();
        int lineNum = getLineNum(n);
        Optional<Expression> optionalRHS = n.getVariable(0).getInitializer();

        if (optionalRHS.isEmpty()) {
            return new BigOComplexityResult(lineNum, "1", null);
        }

        return optionalRHS.get().accept(this, arg);
    }

    @Override
    public BigOComplexityResult visit(BinaryExpr n, Map<Integer, String> arg) {
        BigOComplexityResult rhs = n.getRight().accept(this, arg);
        BigOComplexityResult lhs = n.getLeft().accept(this, arg);

//        System.out.println("BinaryExpr: " + n);
//        System.out.println("Left: " + n.getLeft().toString());
//        System.out.println("Right: " + n.getRight().toString());
//        System.out.println("Operator: " + n.getOperator());

        String currComplexity = "1";
        String currSize = "1";
        switch (n.getOperator()) {
            case OR:
            case AND:
            case LESS:
            case NOT_EQUALS:
            case EQUALS:
            case GREATER:
            case LESS_EQUALS:
            case GREATER_EQUALS:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                break;
            case BINARY_OR:
            case BINARY_AND:
            case XOR:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = lhs.getSize(); // overall size depends on size of LHS
                break;
            case LEFT_SHIFT:
                break;
            case SIGNED_RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
                break;
            case PLUS:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = "(" + lhs.getSize() + " + " +  rhs.getSize() + ")";
                break;
            case MINUS:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = "(" + lhs.getSize() + " - " +  rhs.getSize() + ")";
                break;
            case MULTIPLY:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = "(" + lhs.getSize() + " * " +  rhs.getSize() + ")";
                break;
            case DIVIDE:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = "(" + lhs.getSize() + " / " +  rhs.getSize() + ")";
                break;
            case REMAINDER:
                currComplexity = "(" + lhs.getBigOComplexity() + " + " + rhs.getBigOComplexity() + ")";
                currSize = rhs.getSize();
                break;
            default:
                // never hit
                break;
        }

        // TODO: deal with simplification for complexity and size, and duplicated terms later (+, -, %)
        return new BigOComplexityResult(getLineNum(n), currComplexity, currSize);
    }

    @Override
    public BigOComplexityResult visit(MethodCallExpr n, Map<Integer, String> arg) {
        // TODO: built-in function check
        String funcVar;

        if (funcNameToVariable.containsKey(n.getName().toString())) {
            funcVar = funcNameToVariable.get(n.getNameAsString());
        } else {
            funcVar = letterGenerator.getNextLetter();
            funcNameToVariable.put(n.getNameAsString(), funcVar);
        }

        funcNameToVariable.put(n.getNameAsString(), funcVar);
        return new BigOComplexityResult(getLineNum(n), funcVar, "size(" + funcVar + ")");
    }

    @Override
    public BigOComplexityResult visit(NameExpr n, Map<Integer, String> arg) {
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    @Override
    public BigOComplexityResult visit(UnaryExpr n, Map<Integer, String> arg) { // ex. i++;
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    @Override
    public BigOComplexityResult visit(IntegerLiteralExpr n, Map<Integer, String> arg) {
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    @Override
    public BigOComplexityResult visit(StringLiteralExpr n, Map<Integer, String> arg) {
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    @Override
    public BigOComplexityResult visit(BooleanLiteralExpr n, Map<Integer, String> arg) {
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    @Override
    public BigOComplexityResult visit(ArrayCreationExpr n, Map<Integer, String> arg) {
        System.out.println("ArrayCreationExpr: " + n);

        Optional<ArrayCreationLevel> level = n.getLevels().getFirst();
        if (level.isEmpty()) {
            BigOComplexityResult bigOInit = n.getInitializer().get().accept(this, arg);
            return new BigOComplexityResult(getLineNum(n), bigOInit.getBigOComplexity(), bigOInit.getSize());
        }

        BigOComplexityResult levelBigO = level.get().accept(this, arg);

        String bigOComp = levelBigO.getBigOComplexity();
        if (n.getInitializer().isPresent()) {
            BigOComplexityResult bigOInit = n.getInitializer().get().accept(this, arg);
            bigOComp = "(" + bigOComp + " + " + bigOInit.getBigOComplexity() + ")";
        }

        return new BigOComplexityResult(getLineNum(n), bigOComp, levelBigO.getSize());
    }

    @Override
    public BigOComplexityResult visit(ArrayCreationLevel n, Map<Integer, String> arg) {
        return super.visit(n, arg);
    }

    @Override
    public BigOComplexityResult visit(ArrayInitializerExpr n, Map<Integer, String> arg) {
//        System.out.println("ArrayInitializerExpr: " + n);
        String currRuntime = "1";

        for (Expression value : n.getValues()) {
            currRuntime += " + " + value.accept(this, arg).getBigOComplexity();
        }

        return new BigOComplexityResult(getLineNum(n), currRuntime, "1");
    }

    @Override
    public BigOComplexityResult visit(ObjectCreationExpr n, Map<Integer, String> arg) {
        int lineNum = getLineNum(n);
        String bigOComplexity = "1";
        arg.put(lineNum, bigOComplexity);
        return new BigOComplexityResult(lineNum, bigOComplexity, "1");
    }

    private static int getLineNum(Node n) {
        return n.getBegin().map(position -> position.line).orElse(-1);
    }
}