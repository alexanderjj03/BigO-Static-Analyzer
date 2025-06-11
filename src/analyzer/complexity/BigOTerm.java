package analyzer.complexity;

import java.util.Objects;

public class BigOTerm {
    // Simplified Modifiers, such as log, poly, factorial, exp
    private String param;
    private ComplexityModifier mod;
    private int modParam;

    public BigOTerm(String param, ComplexityModifier mod, int modParam) {
        this.param = param;
        this.mod = mod;
        this.modParam = modParam;
    }

    // Copy constructor
    public BigOTerm(BigOTerm other) {
        this.param = other.getParam();
        this.mod = other.getMod();
        this.modParam = other.getModParam();
    }

    public ComplexityModifier getMod() {
        return mod;
    }

    public void setMod(ComplexityModifier mod) {
        this.mod = mod;
    }

    public int getModParam() {
        return modParam;
    }

    public void setModParam(int modParam) {
        this.modParam = modParam;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigOTerm bigOTerm = (BigOTerm) o;
        return modParam == bigOTerm.modParam &&
                Objects.equals(param, bigOTerm.param) &&
                mod == bigOTerm.mod;
    }

    @Override
    public int hashCode() {
        return Objects.hash(param, mod, modParam);
    }

    @Override
    public String toString() {
        String result = "";
        switch (mod) {
            case CONSTANT:
                result = "1";
                break;
            case LOG:
                result = "log(" + param + ")";
                break;
            case EXP_2:
                result = "exp(" + param + ")";
                break;
            case FACTORIAL:
                result = param + "!";
                break;
            case POLY:
                result = param;
                if (modParam > 1) {
                    result += "^" + modParam;
                }
                break;
            default:
                break;
        }
        return result;
    }
}
