package analyzer;

import analyzer.complexity.BigOEquation;
import analyzer.complexity.Complexity;
import analyzer.complexity.ComplexityModifier;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import java.util.*;

public class ComplexityVisitor extends GenericVisitorAdapter<Complexity, Map<String, BigOEquation>> {
    private static UniqueLetterGenerator letterGenerator = new UniqueLetterGenerator();
    private Map<Integer, BigOEquation> lineTimeComplexity;
    private Map<String, String> fieldToParam;
    private Map<String, String> methodToParam;
    private Map<String, String> argumentToParam;
    private BigOEquation methodFinalComplexity;
    private List<List<String>> varsInLoops = new ArrayList<>();
    private List<String> currVars; // these names are confusing by melly knows that they mean :p
    private List<Boolean> hasDivisionInLoops = new ArrayList<>();
    private boolean isConditionalInLoop = false;
    private Map<String, BigOEquation> equivalencies = new HashMap<>();

    public ComplexityVisitor() {
        this.lineTimeComplexity = new HashMap<>();
        this.fieldToParam = new HashMap<>();
        this.methodToParam = new HashMap<>();
        this.argumentToParam = new HashMap<>();
    }

    // Fields primarily add params to variable to Param
    @Override
    public Complexity visit(FieldDeclaration n, Map<String, BigOEquation> arg) {
        for (var declarator: n.getVariables()) {
            fieldToParam.put(declarator.getNameAsString(), letterGenerator.getNextLetter());
        }
        return null;
    }

    @Override
    public Complexity visit(MethodDeclaration n, Map<String, BigOEquation> arg) {
        // add params
        n.getParameters().forEach(p -> argumentToParam.put(p.getNameAsString(), letterGenerator.getNextLetter()));

        Optional<BlockStmt> optionalBody = n.getBody();
        if (optionalBody.isEmpty()) {
            lineTimeComplexity.put(getLineNum(n), new BigOEquation());
            // dumb way to reset
//            argumentToParam = new HashMap<>();
            return new Complexity(new BigOEquation(), new BigOEquation());
        } else {
            Complexity worseCase = optionalBody.get().accept(this, arg);
            worseCase = worseCase.getFinalComplexity();
            methodFinalComplexity = worseCase.timeComplexity;
            lineTimeComplexity.put(getLineNum(n), worseCase.timeComplexity);
            // dumb way to reset
//            argumentToParam = new HashMap<>();
            return worseCase;
        }
    }

    @Override
    public Complexity visit(BlockStmt n, Map<String, BigOEquation> arg) {
        BigOEquation worstCase = new BigOEquation();
        for (var statement: n.getStatements()) {
            worstCase.addBigOEquation(statement.accept(this, arg).timeComplexity);
        }
        lineTimeComplexity.put(getLineNum(n), worstCase.getFinalComplexity());
        return new Complexity(worstCase, new BigOEquation());
    }

    @Override
    public Complexity visit(ExpressionStmt n, Map<String, BigOEquation> arg) {
        Complexity worstCase = n.getExpression().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
        return worstCase;
    }

    @Override
    public Complexity visit(VariableDeclarationExpr n, Map<String, BigOEquation> arg) {
        Complexity worstCase = n.getVariables().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
        return worstCase;
    }

    @Override
    public Complexity visit(VariableDeclarator n, Map<String, BigOEquation> arg) {
        Optional<Expression> optionalRHS = n.getInitializer();
        if (optionalRHS.isEmpty()) {
            lineTimeComplexity.put(getLineNum(n), new BigOEquation());
            return new Complexity();
        }
        Complexity childComplexity = optionalRHS.get().accept(this, arg);
        childComplexity = childComplexity.getFinalComplexity();
        lineTimeComplexity.put(getLineNum(n), childComplexity.timeComplexity.getFinalComplexity());
        arg.put(n.getNameAsString(), childComplexity.spaceComplexity);
        return new Complexity(childComplexity.timeComplexity, childComplexity.spaceComplexity);
    }

    @Override
    public Complexity visit(AssignExpr n, Map<String, BigOEquation> arg) {
        Complexity rhsComplexity = n.getValue().accept(this, arg);
        BigOEquation lhsSpace = n.getTarget().accept(this, arg).spaceComplexity;
        BigOEquation rhsSpace = rhsComplexity.spaceComplexity;
        BigOEquation time = rhsComplexity.timeComplexity;

        String targetName = (n.getTarget() instanceof NameExpr) ? n.getTarget().asNameExpr().getName().asString() : "";
        String letter = letterGenerator.getNextLetter();
        switch (n.getOperator()) {
            case ASSIGN:
                // check if target is in the array and if the target is being set and divided by some value
                if (containsVarInVarsInLoops(targetName) &&
                    (n.getValue() instanceof BinaryExpr) &&
                    ((((BinaryExpr) n.getValue()).getOperator() == BinaryExpr.Operator.DIVIDE) ||
                    (((BinaryExpr) n.getValue()).getOperator() == BinaryExpr.Operator.MULTIPLY)) &&
                    (((BinaryExpr) n.getValue()).getLeft() instanceof NameExpr) &&
                    (((NameExpr) ((BinaryExpr) n.getValue()).getLeft()).getNameAsString().equals(targetName))) {

                    hasDivisionInLoops.set(hasDivisionInLoops.size() - 1, true);
                }
                lhsSpace = rhsSpace;
                break;
            case REMAINDER:
                lhsSpace = rhsSpace;
                break;
            case PLUS:
                lhsSpace = lhsSpace.addBigOEquation(rhsSpace);
                break;
            case MINUS:
                lhsSpace = lhsSpace.subtractBigOEquation(rhsSpace);
                break;
            case MULTIPLY:
                // check if target is in the array
                if (containsVarInVarsInLoops(targetName)) {
                    hasDivisionInLoops.set(hasDivisionInLoops.size() - 1, true);
                }
                lhsSpace = lhsSpace.multiplyBigOEquation(rhsSpace);
                break;
            case DIVIDE:
                // check if target is in the array
                if (containsVarInVarsInLoops(targetName)) {
                    hasDivisionInLoops.set(hasDivisionInLoops.size() - 1, true);
                }
                lhsSpace = lhsSpace.divideBigOEquation(rhsSpace);
                break;
            case BINARY_AND:
            case BINARY_OR:
            case XOR:
                break;
            case LEFT_SHIFT:
                equivalencies.put(letter, rhsSpace);
                lhsSpace = lhsSpace.multiplyBigOEquation(new BigOEquation(letter, ComplexityModifier.EXP_2, 1));
                break;
            case SIGNED_RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
                equivalencies.put(letter, rhsSpace);
                lhsSpace = lhsSpace.divideBigOEquation(new BigOEquation(letter, ComplexityModifier.EXP_2, 1));
                break;
            default:
                break; // shouldn't hit here because that's mean and default wants to be left alone for once in their life please
        }
        arg.put(n.getTarget().toString(), lhsSpace);
        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, lhsSpace);
    }

    @Override
    public Complexity visit(FieldAccessExpr n, Map<String, BigOEquation> arg) {
        BigOEquation time = new BigOEquation();
        BigOEquation space = new BigOEquation();
        String name = n.getScope().toString();

        if (arg.containsKey(name)) {
            space = arg.get(name).copy();
        } else if (argumentToParam.containsKey(name)) {
            space = new BigOEquation(argumentToParam.get(name), ComplexityModifier.POLY, 1);
        } else if (fieldToParam.containsKey(name)) {
            space = new BigOEquation(fieldToParam.get(name), ComplexityModifier.POLY, 1);
        }

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, space);
    }

    @Override
    public Complexity visit(NameExpr n, Map<String, BigOEquation> arg) {
        BigOEquation time = new BigOEquation();
        BigOEquation space = new BigOEquation();
        String name = n.getNameAsString();

        if (isConditionalInLoop) {
            currVars.add(n.getNameAsString());
        }

        if (arg.containsKey(name)) {
            space = arg.get(name).copy();
        } else if (argumentToParam.containsKey(name)) {
            space = new BigOEquation(argumentToParam.get(name), ComplexityModifier.POLY, 1);
        } else if (fieldToParam.containsKey(name)) {
            space = new BigOEquation(fieldToParam.get(name), ComplexityModifier.POLY, 1);
        }

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, space);
    }

    @Override
    public Complexity visit(BinaryExpr n, Map<String, BigOEquation> arg) {
        Complexity lhs = n.getLeft().accept(this, arg);
        Complexity rhs = n.getRight().accept(this, arg);

        Complexity result = new Complexity();
        String letter = letterGenerator.getNextLetter();
        switch (n.getOperator()) { // the anti william switch block (prof if you read this, it is a joke don't worry)
            case XOR:
            case BINARY_OR:
            case BINARY_AND:
                result.spaceComplexity = lhs.spaceComplexity;
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.spaceComplexity);
                break;
            case PLUS:
                result.spaceComplexity = lhs.spaceComplexity.addBigOEquation(rhs.spaceComplexity);
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case MINUS:
                result.spaceComplexity = lhs.spaceComplexity.subtractBigOEquation(rhs.spaceComplexity);
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case OR:
            case AND:
                result.spaceComplexity = new BigOEquation();
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case EQUALS:
            case NOT_EQUALS:
            case LESS:
            case GREATER:
            case LESS_EQUALS:
            case GREATER_EQUALS: // k < b
                result.spaceComplexity = lhs.spaceComplexity.getWorseComplexity(rhs.spaceComplexity);
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case LEFT_SHIFT:
                equivalencies.put(letter, rhs.spaceComplexity);
                result.spaceComplexity = lhs.spaceComplexity.multiplyBigOEquation(new BigOEquation(letter, ComplexityModifier.EXP_2, 1));
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case SIGNED_RIGHT_SHIFT:
            case UNSIGNED_RIGHT_SHIFT:
                equivalencies.put(letter, rhs.spaceComplexity);
                result.spaceComplexity = lhs.spaceComplexity.divideBigOEquation(new BigOEquation(letter, ComplexityModifier.EXP_2, 1));
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case MULTIPLY:
                result.spaceComplexity = lhs.spaceComplexity.multiplyBigOEquation(rhs.spaceComplexity);
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case DIVIDE:
                result.spaceComplexity = lhs.spaceComplexity.divideBigOEquation(rhs.spaceComplexity);
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            case REMAINDER:
                result.spaceComplexity = rhs.spaceComplexity;
                result.timeComplexity = lhs.timeComplexity.addBigOEquation(rhs.timeComplexity);
                break;
            default:
                break; // just because default wants to be left alone doesn't mean you shouldn't invite them to the party, william
        }

        lineTimeComplexity.put(getLineNum(n), result.timeComplexity.getFinalComplexity());
        return result;
    }

    @Override
    public Complexity visit(UnaryExpr n, Map<String, BigOEquation> arg) {
        Complexity worstCase = n.getExpression().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
        return worstCase;
    }

    @Override
    public Complexity visit(IntegerLiteralExpr n, Map<String, BigOEquation> arg) {
        // constant time and space
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    @Override
    public Complexity visit(StringLiteralExpr n, Map<String, BigOEquation> arg) {
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    @Override
    public Complexity visit(BooleanLiteralExpr n, Map<String, BigOEquation> arg) {
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    @Override
    public Complexity visit(ArrayCreationExpr n, Map<String, BigOEquation> arg) {
        Optional<ArrayCreationLevel> level = n.getLevels().getFirst(); // only support 1D arrays
        if (n.getInitializer().isEmpty()) {
            Complexity worstCase = level.get().accept(this, arg);
            lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
            return worstCase;
        }

        Complexity worstCase = n.getInitializer().get().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
        return worstCase;
    }

    @Override
    public Complexity visit(ArrayCreationLevel n, Map<String, BigOEquation> arg) {
        return super.visit(n, arg);
    }

    @Override
    public Complexity visit(ArrayInitializerExpr n, Map<String, BigOEquation> arg) {
        BigOEquation timeComplexity = new BigOEquation();
        BigOEquation spaceComplexity = new BigOEquation();
        for (Expression value : n.getValues()) {
            timeComplexity.addBigOEquation(value.accept(this, arg).timeComplexity);
             spaceComplexity = spaceComplexity.getWorseComplexity(value.accept(this, arg).spaceComplexity);
        }

        lineTimeComplexity.put(getLineNum(n), timeComplexity.getFinalComplexity());
        return new Complexity(timeComplexity, spaceComplexity);
    }

    @Override
    public Complexity visit(ArrayAccessExpr n, Map<String, BigOEquation> arg) {
        String name = n.getName().toString();
        BigOEquation space = new BigOEquation();

        if (arg.containsKey(name)) {
            space = arg.get(name);
        }

        return new Complexity(new BigOEquation(), space);
    }

    @Override
    public Complexity visit(ObjectCreationExpr n, Map<String, BigOEquation> arg) {
        // constant time and space
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    @Override
    public Complexity visit(MethodCallExpr n, Map<String, BigOEquation> arg) {
        String name = n.getName().getIdentifier();
        String param;
        BigOEquation bigOEquation = new BigOEquation();

        if (methodToParam.containsKey(name)) {
            param = methodToParam.get(name);
        } else {
            MethodOwOdentifier identifier = new MethodOwOdentifier();
            if (identifier.hasComplexity(n.getNameAsString())) {
                BigOEquation maxTimeBigO = new BigOEquation();
                BigOEquation maxSpaceBigO = new BigOEquation();
                // Node list is horrible so i do it here
                for (Expression argument : n.getArguments()) {
                    Complexity nodeComplexity = argument.accept(this, arg);
                    maxTimeBigO = nodeComplexity.timeComplexity.getWorseComplexity(maxTimeBigO);
                    maxSpaceBigO = nodeComplexity.spaceComplexity.getWorseComplexity(maxSpaceBigO);
                }
                Complexity argumentComplexity = new Complexity(maxTimeBigO, maxSpaceBigO);
                var scopeOption = n.getScope();
                int availableParams = argumentComplexity.spaceComplexity.getAvailableParams().size();
                if (availableParams == 0 && scopeOption.isPresent()) {
                    // Check if scope has something
                    Complexity scopeComplexity = scopeOption.get().accept(this, arg);
                    if (scopeComplexity.spaceComplexity.getAvailableParams().size() == 1) {
                        bigOEquation = identifier.getComplexity(n.getNameAsString(), scopeComplexity.spaceComplexity.getAvailableParams().iterator().next());
                    }
                }
                else if (argumentComplexity.spaceComplexity.getAvailableParams().size() == 1) {
                    // Get the first argument
                    bigOEquation = identifier.getComplexity(n.getNameAsString(), argumentComplexity.spaceComplexity.getAvailableParams().iterator().next());
                } else {
                    String varName = letterGenerator.getNextLetter();
                    equivalencies.put(varName, argumentComplexity.spaceComplexity.getFinalComplexity());
                    bigOEquation = identifier.getComplexity(n.getNameAsString(), varName);
                }
            } else {
                param = letterGenerator.getNextLetter();
                methodToParam.put(name, param);
                bigOEquation = new BigOEquation(param, ComplexityModifier.POLY, 1);
            }
        }

        lineTimeComplexity.put(getLineNum(n), bigOEquation.getFinalComplexity());
        return new Complexity(bigOEquation, bigOEquation);
    }

//    @Override
//    public Complexity visit(NodeList<Expression> n, Map<String, BigOEquation> arg) {
//        BigOEquation maxTimeBigO = new BigOEquation();
//        BigOEquation maxSpaceBigO = new BigOEquation();
//        n.forEach(node -> {
//            Complexity nodeComplexity = (Expression) node.accept(this, arg);
//            maxTimeBigO = nodeComplexity.timeComplexity.getWorseComplexity(maxTimeBigO);
//            maxSpaceBigO = nodeComplexity.spaceComplexity.getWorseComplexity(maxSpaceBigO);
//        });
//        return new Complexity(maxTimeBigO, maxSpaceBigO);
//    }

    @Override
    public Complexity visit(ReturnStmt n, Map<String, BigOEquation> arg) {
        Optional<Expression> value = n.getExpression();

        if (value.isPresent()) {
            return value.get().accept(this, arg);
        }

        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    // IF-STATEMENT

    @Override
    public Complexity visit(ConditionalExpr n, Map<String, BigOEquation> arg) {
        Complexity condComplexity = n.getCondition().accept(this, arg);
        Complexity thenComplexity = n.getThenExpr().accept(this, arg);
        Complexity elseComplexity = n.getElseExpr().accept(this, arg);

        BigOEquation time = thenComplexity.timeComplexity.getWorseComplexity(elseComplexity.timeComplexity);
        BigOEquation space = thenComplexity.spaceComplexity.getWorseComplexity(elseComplexity.spaceComplexity);

        time = time.addBigOEquation(condComplexity.timeComplexity);
        space = space.addBigOEquation(condComplexity.spaceComplexity);

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, space);
    }

    @Override
    public Complexity visit(IfStmt n, Map<String, BigOEquation> arg) {
        Complexity condComplexity = n.getCondition().accept(this, arg);
        Complexity thenComplexity = n.getThenStmt().asBlockStmt().accept(this, arg);

        Optional<Statement> optionalElseStmt = n.getElseStmt();
        Complexity elseComplexity = new Complexity();

        if (optionalElseStmt.isPresent()) {
            elseComplexity = optionalElseStmt.get().accept(this, arg);
        }

        BigOEquation time = thenComplexity.timeComplexity.getWorseComplexity(elseComplexity.timeComplexity);
        time.addBigOEquation(condComplexity.timeComplexity);

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, new BigOEquation());
    }

    // TRY-STATEMENT

    @Override
    public Complexity visit(TryStmt n, Map<String, BigOEquation> arg) {
        List<Complexity> catchComplexities = new ArrayList<>();
        for (CatchClause catchClause : n.getCatchClauses()) {
            catchComplexities.add(catchClause.accept(this, arg));
        }

        BigOEquation worstCatchTime = new BigOEquation();
        for (Complexity catchComplexity : catchComplexities) {
            worstCatchTime = worstCatchTime.getWorseComplexity(catchComplexity.timeComplexity);
        }

        BigOEquation finallyTime = new BigOEquation();
        if (n.getFinallyBlock().isPresent()) {
            finallyTime = n.getFinallyBlock().get().accept(this, arg).timeComplexity;
        }

        BigOEquation resourceTime = new BigOEquation();
        for (Expression resource : n.getResources()) {
            resourceTime.addBigOEquation(resource.accept(this, arg).timeComplexity);
        }

        BigOEquation tryTime = n.getTryBlock().accept(this, arg).timeComplexity;

        BigOEquation time = worstCatchTime;
        time.addBigOEquation(finallyTime).addBigOEquation(resourceTime).addBigOEquation(tryTime);

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, new BigOEquation());
    }

    @Override
    public Complexity visit(CatchClause n, Map<String, BigOEquation> arg) {
        Complexity worseCase = n.getBody().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worseCase.timeComplexity.getFinalComplexity());
        return worseCase;
    }

    @Override
    public Complexity visit(ThrowStmt n, Map<String, BigOEquation> arg) {
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    // SWITCH-STATEMENT

    @Override
    public Complexity visit(SwitchExpr n, Map<String, BigOEquation> arg) {
        List<Complexity> complexities = new ArrayList<>();
        n.getEntries().forEach(e -> complexities.add(e.accept(this, arg)));
        BigOEquation worstTime = new BigOEquation();
        BigOEquation worstSpace = new BigOEquation();
        for (Complexity complexity : complexities) {
            worstTime = worstTime.getWorseComplexity(complexity.timeComplexity);
            worstSpace = worstSpace.getWorseComplexity(complexity.spaceComplexity);
        }

        worstTime.addBigOEquation(n.getSelector().accept(this, arg).timeComplexity);
        lineTimeComplexity.put(getLineNum(n), worstTime.getFinalComplexity());
        return new Complexity(worstTime, worstSpace);
    }

    @Override
    public Complexity visit(SwitchStmt n, Map<String, BigOEquation> arg) {
        List<Complexity> complexities = new ArrayList<>();
        n.getEntries().forEach(e -> complexities.add(e.accept(this, arg)));
        BigOEquation worstTime = new BigOEquation();
        for (Complexity complexity : complexities) {
            worstTime = worstTime.getWorseComplexity(complexity.timeComplexity);
        }

        worstTime = worstTime.addBigOEquation(n.getSelector().accept(this, arg).timeComplexity);
        lineTimeComplexity.put(getLineNum(n), worstTime.getFinalComplexity());
        return new Complexity(worstTime, new BigOEquation());
    }

    @Override
    public Complexity visit(SwitchEntry n, Map<String, BigOEquation> arg) {
        Complexity guardComplexity = new Complexity();
        if (n.getGuard().isPresent()) {
            guardComplexity = n.getGuard().get().accept(this, arg);
        }

        BigOEquation time = guardComplexity.timeComplexity;
        BigOEquation space = new BigOEquation();
        for (Statement statement : n.getStatements()) {
            Complexity complexity = statement.accept(this, arg);
            time.addBigOEquation(complexity.timeComplexity);
            space = complexity.spaceComplexity;
        }

        lineTimeComplexity.put(getLineNum(n), time.getFinalComplexity());
        return new Complexity(time, space);
    }

    @Override
    public Complexity visit(YieldStmt n, Map<String, BigOEquation> arg) {
        Complexity worstCase = n.getExpression().accept(this, arg);
        lineTimeComplexity.put(getLineNum(n), worstCase.timeComplexity.getFinalComplexity());
        return worstCase;
    }

    // LOOPs

    @Override
    public Complexity visit(ForStmt n, Map<String, BigOEquation> arg) {
        var init = n.getInitialization();
        var optionalCompare = n.getCompare();
        var update = n.getUpdate();
        if (optionalCompare.isEmpty() || init.isEmpty() || update.isEmpty())  {
            lineTimeComplexity.put(getLineNum(n), new BigOEquation());
            return new Complexity();
        }
        Complexity initComplexity = init.accept(this, arg);

        isConditionalInLoop = true;
        currVars = new ArrayList<>();
        // terrible coding practices pls ignore :)
        optionalCompare.get().accept(this, arg);
        update.accept(this, arg);
        varsInLoops.add(currVars);
        isConditionalInLoop = false;
        hasDivisionInLoops.add(false);

        Complexity compareComplexity = optionalCompare.get().accept(this, arg);
        update.accept(this, arg);

        Complexity bodyComplexity = n.getBody().asBlockStmt().accept(this, arg);

        boolean divisionInLoopBody = hasDivisionInLoops.get(hasDivisionInLoops.size() - 1);
        varsInLoops.remove(varsInLoops.size() - 1);
        hasDivisionInLoops.remove(hasDivisionInLoops.size() - 1);

        BigOEquation compSpaceComplexity = compareComplexity.spaceComplexity;

        if (divisionInLoopBody) {
            String varName;
            if (compSpaceComplexity.getAvailableParams().size() == 1) {
                varName = (String) compSpaceComplexity.getAvailableParams().toArray()[0];
            } else {
                varName = letterGenerator.getNextLetter();
                equivalencies.put(varName, compareComplexity.spaceComplexity.getFinalComplexity());

            }
            compSpaceComplexity = new BigOEquation(varName, ComplexityModifier.LOG, 1);
        }

        bodyComplexity.timeComplexity.multiplyBigOEquation(compSpaceComplexity);

        lineTimeComplexity.put(getLineNum(n), bodyComplexity.timeComplexity.getFinalComplexity());
        return new Complexity(bodyComplexity.timeComplexity, new BigOEquation());
    }

    @Override
    public Complexity visit(ForEachStmt n, Map<String, BigOEquation> arg) {
        Complexity rhsComplexity = n.getIterable().accept(this, arg);
        Complexity bodyComplexity = n.getBody().asBlockStmt().accept(this, arg);
        BigOEquation newTimeComplexity = bodyComplexity.timeComplexity.multiplyBigOEquation(rhsComplexity.spaceComplexity);
        lineTimeComplexity.put(getLineNum(n), newTimeComplexity.getFinalComplexity());
        return new Complexity(newTimeComplexity, new BigOEquation());
    }

    @Override
    public Complexity visit(WhileStmt n, Map<String, BigOEquation> arg) {
        isConditionalInLoop = true;
        currVars = new ArrayList<>();
        Complexity conditionComplexity = n.getCondition().accept(this, arg);
        varsInLoops.add(currVars);
        isConditionalInLoop = false;
        hasDivisionInLoops.add(false);

        Complexity bodyComplexity = n.getBody().asBlockStmt().accept(this, arg);

        boolean divisionInLoopBody = hasDivisionInLoops.get(hasDivisionInLoops.size() - 1);
        varsInLoops.remove(varsInLoops.size() - 1);
        hasDivisionInLoops.remove(hasDivisionInLoops.size() - 1);

        BigOEquation condSpaceComplexity = conditionComplexity.spaceComplexity;

        if (divisionInLoopBody) {
            String varName;
            if (condSpaceComplexity.getAvailableParams().size() == 1) {
                varName = (String) condSpaceComplexity.getAvailableParams().toArray()[0];
            } else {
                varName = letterGenerator.getNextLetter();
                equivalencies.put(varName, conditionComplexity.spaceComplexity.getFinalComplexity());
            }
            condSpaceComplexity = new BigOEquation(varName, ComplexityModifier.LOG, 1);
        }

        bodyComplexity.timeComplexity.multiplyBigOEquation(condSpaceComplexity);

        lineTimeComplexity.put(getLineNum(n), bodyComplexity.timeComplexity.getFinalComplexity());
        return new Complexity(bodyComplexity.timeComplexity, new BigOEquation());
    }

    public Map<Integer, BigOEquation> getLineTimeComplexity() {
        return lineTimeComplexity;
    }

    @Override
    public Complexity visit(BreakStmt n, Map<String, BigOEquation> arg) {
        // constant time and space
        lineTimeComplexity.put(getLineNum(n), new BigOEquation());
        return new Complexity();
    }

    @Override
    public Complexity visit(ThisExpr n, Map<String, BigOEquation> arg) {
        return new Complexity(new BigOEquation(), new BigOEquation());
    }

    @Override
    public Complexity visit(ContinueStmt n, Map<String, BigOEquation> arg) {
        return new Complexity(new BigOEquation(), new BigOEquation());
    }

    @Override
    public Complexity visit(NullLiteralExpr n, Map<String, BigOEquation> arg) {
        return new Complexity(new BigOEquation(), new BigOEquation());
    }

    public List<String> getAllParameters() {
        List<String> allValues = new ArrayList<>();
        allValues.addAll(argumentToParam.values());
        allValues.addAll(fieldToParam.values());
        allValues.addAll(methodToParam.values());
        return allValues;
    }

//    private Map<String, String> fieldToParam;
//    private Map<String, String> methodToParam;
//    private Map<String, String> argumentToParam;


    public Map<String, String> getFieldToParam() {
        return fieldToParam;
    }

    public Map<String, String> getMethodToParam() {
        return methodToParam;
    }

    public Map<String, String> getArgumentToParam() {
        return argumentToParam;
    }

    public BigOEquation getMethodFinalComplexity() {
        return methodFinalComplexity;
    }

    public Map<String, BigOEquation> getEquivalencies() {
        return equivalencies;
    }

    private static int getLineNum(Node n) {
        return n.getBegin().map(position -> position.line).orElse(-1);
    }

    private boolean containsVarInVarsInLoops(String name) {
        for (var vars : this.varsInLoops) {
            for (var varName : vars) {
                if (varName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // For testing
    public void resetLetterGenerator() {
        letterGenerator.reset();
    }
}
