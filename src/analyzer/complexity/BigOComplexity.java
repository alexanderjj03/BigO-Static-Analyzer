package analyzer.complexity;

import java.util.HashMap;

// DEPRECATED
public class BigOComplexity {
    private ComplexityModifier modifier; // n vs log n vs 2^n vs n!, encapsulates all the contents except exponent
    private Integer exponent; // 0 if and only if O(1)
    private String variable; // e.g. len(strvar), n, *name of for loop upper bound*
    // null if isSumorProd is true

    private boolean isSumorProd; // if true, below variables aren't null
    // OPTIONAL
    private BigOComplexity left;
    private BigOComplexity right;
    private String operator; // + or *

    private HashMap<ComplexityModifier, Integer> priority; // reflects the fact that, for example, O(2^n + n^2) = O(2^n)

    // REQUIRES: Last 3 inputs are either all null (var isn't null) or none are null (var is null)
    public BigOComplexity(String mod, Integer exp, String var, BigOComplexity l, BigOComplexity r, String op) {
        switch (mod.toLowerCase()) {
            case "log": {
                modifier = ComplexityModifier.LOG;
                break;
            } case "exp2", "2^": {
                modifier = ComplexityModifier.EXP_2;
                break;
            } case "factorial", "!": {
                modifier = ComplexityModifier.FACTORIAL;
                break;
            } default: {
                modifier = ComplexityModifier.CONSTANT;
            }
        }

        exponent = exp;
        variable = var;
        left = l;
        right = r;
        operator = op;

        isSumorProd = left != null;
        priority = new HashMap<>();
        priority.put(ComplexityModifier.FACTORIAL, 4);
        priority.put(ComplexityModifier.EXP_2, 3);
        priority.put(ComplexityModifier.CONSTANT, 2);
        priority.put(ComplexityModifier.LOG, 1);
    }

    public boolean isSumorProd() {
        return isSumorProd;
    }
    public void setOperator(String operator) {
        this.operator = operator;
    }
    public String getOperator() {
        return operator;
    }
    public BigOComplexity getLeft() {
        return left;
    }
    public BigOComplexity getRight() {
        return right;
    }
    public ComplexityModifier getModifier() {
        return modifier;
    }
    public String getVariable() {
        return variable;
    }
    public Integer getExponent() {
        return exponent;
    }
    public void setExponent(Integer exponent) {
        this.exponent = exponent;
    }
    public void setLeft(BigOComplexity left) {
        this.left = left;
    }
    public void setModifier(ComplexityModifier modifier) {
        this.modifier = modifier;
    }
    public void setVariable(String variable) {
        this.variable = variable;
    }
    public void setSumorProd(boolean sumorProd) {
        isSumorProd = sumorProd;
    }
    public void setRight(BigOComplexity right) {
        this.right = right;
    }
    public void setPriority(HashMap<ComplexityModifier, Integer> priority) {
        this.priority = priority;
    }

    public String print() {
        return "O(" + printNoO() + ")";
    }

    public String printNoO() {
        if (exponent <= 0) {
            return "1";
        }

        String toPrint = "";
        if (isSumorProd) {
            if (left.isSumorProd) {
                toPrint += "(" + left.printNoO() + ")";
            } else {
                toPrint += left.printNoO();
            }
            toPrint += operator;
            if (right.isSumorProd) {
                toPrint += "(" + right.printNoO() + ")";
            } else {
                toPrint += right.printNoO();
            }
        } else {
            toPrint = variable;

        }

        switch (modifier) {
            case EXP_2 -> toPrint = "2^(" + toPrint + ")";
            case FACTORIAL -> toPrint = "(" + toPrint + ")!";
            case LOG -> toPrint = "log(" + toPrint + ")";
        }

        if (exponent > 1) {
            toPrint = "(" + toPrint + ")^" + exponent;
        }

        return toPrint;
    }

    // Counts a big O complexity's "children", in other words the total number of "leaf" BigOComplexity objects that
    // compose a higher-level BigOComplexity object.
    public Integer countChildren() {
        if (isSumorProd()) {
            return getLeft().countChildren() + getRight().countChildren();
        } else {
            return 1;
        }
    }

    // NOTE: If two big O complexities have differing nodifiers (e.g. none vs log), the lower priority one gets ignored.
    // This is a simple but seemingly necessary approximation so big o complexities dont get out of hand.
    public BigOComplexity combineSum(BigOComplexity other) {
        BigOComplexity ret = new BigOComplexity("none", 1, null, null, null, null);

        // By this point, both bigOComplexities have "none" modifier
        if (!isSumorProd) {
            if (!other.isSumorProd()) {
                if (!this.variable.equals(other.getVariable())) {
                    ret.setSumorProd(true);
                    ret.setLeft(this);
                    ret.setRight(other);
                    ret.setOperator("+");
                } else if (priority.get(modifier) > priority.get(other.getModifier())) {
                    return this;
                } else if (priority.get(modifier) < priority.get(other.getModifier())) {
                    return other;
                } else {
                    return (exponent > other.getExponent()) ? this : other;
                }
            } else if (other.getOperator().equals("+")) {
                Integer totalChildren = 1 + other.countChildren();
                BigOComplexity pos1 = combineSum(other.getLeft());
                BigOComplexity pos2 = combineSum(other.getRight());
                Integer pos1Children = pos1.countChildren() + other.getRight().countChildren();
                Integer pos2Children = pos2.countChildren() + other.getLeft().countChildren();

                // Way 1 of evaluating the sum is simpler
                if ((pos1Children < totalChildren) && (pos1Children <= pos2Children)) {
                    ret = other.getRight().combineSum(pos1);
                } else if (pos1Children > pos2Children) { // way 2 is simpler
                    ret = other.getLeft().combineSum(pos2);
                } else { // neither is simpler
                    ret.setSumorProd(true);
                    ret.setLeft(this);
                    ret.setRight(other);
                    ret.setOperator("+");
                }
            } else { // multiplication operator in other
                if (!priority.get(modifier).equals(priority.get(other.getModifier()))) {
                    ret.setSumorProd(true);
                    ret.setLeft(this);
                    ret.setRight(other);
                    ret.setOperator("+");
                } else {
                    BigOComplexity left = other.getLeft();
                    BigOComplexity right = other.getRight();
                    // essentially, if the variable is in the other O complexity's product
                    if (((variable.equals(left.getVariable())) &&
                            (priority.get(modifier) <= priority.get(left.getModifier()))) &&
                            (exponent <= left.getExponent())) {
                        return other;
                    } else if (((variable.equals(right.getVariable())) &&
                            (priority.get(modifier) <= priority.get(right.getModifier()))) &&
                            (exponent <= right.getExponent())) {
                        return other;
                    } else {
                        ret.setSumorProd(true);
                        ret.setLeft(this);
                        ret.setRight(other);
                        ret.setOperator("+");
                    }
                }
            }
        } else if (!other.isSumorProd) {
            return other.combineSum(this);
        } else if (operator.equals("+")) { // if this is a sum or product, and so is other
            if (other.getOperator().equals("+")) {
                return left.combineSum(right.combineSum(other));
            } else {
                return other.combineSum(this);
            }
        } else if (other.getOperator().equals("+")) {
            Integer totalChildren = countChildren() + other.countChildren();
            BigOComplexity pos1 = combineSum(other.getLeft());
            BigOComplexity pos2 = combineSum(other.getRight());
            Integer pos1Children = pos1.countChildren() + other.getRight().countChildren();
            Integer pos2Children = pos2.countChildren() + other.getLeft().countChildren();

            // Way 1 of evaluating the sum is simpler
            if ((pos1Children < totalChildren) && (pos1Children <= pos2Children)) {
                ret = other.getRight().combineSum(pos1);
            } else if (pos1Children > pos2Children) { // way 2 is simpler
                ret = other.getLeft().combineSum(pos2);
            } else { // neither is simpler
                ret.setSumorProd(true);
                ret.setLeft(this);
                ret.setRight(other);
                ret.setOperator("+");
            }
        } else { // sum of two products
            ret.setSumorProd(true);
            ret.setLeft(this);
            ret.setRight(other);
            ret.setOperator("+");
        }

        return ret;
    }

    public BigOComplexity combineMult(BigOComplexity other) {
        BigOComplexity ret = new BigOComplexity("none", 1, null, null, null, null);
        if (!isSumorProd) {
            if (!other.isSumorProd()) {
                if ((!this.variable.equals(other.getVariable())) ||
                        (!priority.get(modifier).equals(priority.get(other.getModifier())))) {
                    ret.setSumorProd(true);
                    ret.setLeft(this);
                    ret.setRight(other);
                    ret.setOperator("*");
                } else {
                    ret.setVariable(this.variable);
                    ret.setExponent(exponent + other.getExponent());
                }
            } else if (other.getOperator().equals("+")) {
                ret.setSumorProd(true);
                ret.setLeft(combineMult(other.getLeft()));
                ret.setRight(combineMult(other.getRight()));
                ret.setOperator("+");
            } else { // multiplication operator in other
                if (!priority.get(modifier).equals(priority.get(other.getModifier()))) {
                    ret.setSumorProd(true);
                    ret.setLeft(this);
                    ret.setRight(other);
                    ret.setOperator("*");
                } else {
                    Integer totalChildren = 1 + other.countChildren();
                    BigOComplexity pos1 = combineMult(other.getLeft());
                    BigOComplexity pos2 = combineMult(other.getRight());
                    Integer pos1Children = pos1.countChildren() + other.getRight().countChildren();
                    Integer pos2Children = pos2.countChildren() + other.getLeft().countChildren();

                    // Way 1 of evaluating the sum is simpler
                    if ((pos1Children < totalChildren) && (pos1Children <= pos2Children)) {
                        ret = other.getRight().combineMult(pos1);
                    } else if (pos1Children > pos2Children) { // way 2 is simpler
                        ret = other.getLeft().combineMult(pos2);
                    } else { // neither is simpler
                        ret.setSumorProd(true);
                        ret.setLeft(this);
                        ret.setRight(other);
                        ret.setOperator("*");
                    }
                }
            }
        } else if (!other.isSumorProd) {
            return other.combineMult(this);
        } else if (operator.equals("+")) { // if this is a sum or product, and so is other
            return left.combineMult(other).combineSum(right.combineMult(other));
        } else if (other.getOperator().equals("+")) { // operator.equals("*")
            return other.combineMult(this);
        } else { // product of two products
            BigOComplexity intermediate = left.combineMult(other);
            return right.combineMult(intermediate);
        }

        return ret;
    }
}
