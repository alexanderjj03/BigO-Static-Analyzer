package analyzer.complexity;

import com.github.javaparser.utils.Pair;

import java.util.*;

// Represent the Big O equation for a certain time/space
public class BigOEquation {
    private Set<String> availableParams = new HashSet<>();

    // Terms are all summed together
    private List<List<BigOTerm>> terms = new ArrayList<>(new ArrayList<>());

    // Contains Constant time only
    private boolean constantTime = true;

    private BigOEquation(BigOEquation other) {
        List<List<BigOTerm>> terms = new ArrayList<>();
        for (List<BigOTerm> termList : other.terms) {
            List<BigOTerm> newTerms = new ArrayList<>();
            for (BigOTerm term : termList) {
                newTerms.add(new BigOTerm(term));
            }
            terms.add(newTerms);
        }
        this.terms = terms;
        this.constantTime = other.constantTime;
        this.availableParams  = other.availableParams;
    }

    public BigOEquation(List<List<BigOTerm>> terms) {
        this.terms = terms;
        this.constantTime = false;
        this.availableParams = new HashSet<>();
    }

//    public BigOEquation(List<String> availableParams) {
//        this.availableParams = new HashSet<>(availableParams);
//        List<BigOTerm> constant = new ArrayList<>();
//        constant.add(new BigOTerm("", ComplexityModifier.CONSTANT, 1));
//        terms.add(constant);
//    }

    // Default is constant time
    public BigOEquation() {
        List<BigOTerm> constant = new ArrayList<>();
        constant.add(new BigOTerm("", ComplexityModifier.CONSTANT, 1));
        terms.add(constant);
    }

    public BigOEquation(String param, ComplexityModifier mod, int modParams) {
        constantTime = false;
        addBigOTerm(param, mod, modParams);
    }

    public void addBigOTerm(String param, ComplexityModifier mod, int modParams) {
        if (constantTime) {
            terms = new ArrayList<>(new ArrayList<>());
            constantTime = false;
        }
        List<BigOTerm> term = new ArrayList<>();
        BigOTerm bigOTerm = new BigOTerm(param, mod, modParams);
        term.add(bigOTerm);
        terms.add(term);
        availableParams.add(param);
    }

    public BigOEquation addBigOEquation(BigOEquation other) {
        this.availableParams.addAll(other.availableParams);
        for (var term: other.getTerms()) {
            this.terms.add(term);
        }
        return this;
    }

    public BigOEquation subtractBigOEquation(BigOEquation other) {
        for (var term: other.getTerms()) {
            this.terms.remove(term);
        }

        return this;
    }

    public BigOEquation multiplyBigOEquation(BigOEquation other) {
        // Cross Multiply
        this.availableParams.addAll(other.availableParams);
        List<List<BigOTerm>> newEquation = new ArrayList<>();
        for (var term1: terms) {
            for (var term2: other.getTerms()) {
                newEquation.add(multiplyTwoBigOTerms(term1, term2));
            }
        }
        this.terms = newEquation;
        return this;
    }

    public BigOEquation divideBigOEquation(BigOEquation other) {
        // if both equations have one mult array, then check if top array contans all terms in bottom array,
        //     if so, remove bottom terms from top array and return top array
        // else, return top array

        // TODO

        if (this.terms.size() == 1 && other.terms.size() == 1) {
            BigOEquation bigOEquation = new BigOEquation();
            List<BigOTerm> thisTerms = this.terms.get(0);
            List<BigOTerm> otherTerms = other.terms.get(0);
            for(BigOTerm term: thisTerms) {
                boolean remove = false;
                int newModParam = 1;
                for (BigOTerm term2: otherTerms) {
                    if (term.getMod() == term2.getMod() && term.getParam().equals(term2.getParam())) {
                        switch (term.getMod()) {
                            case CONSTANT:
                            case EXP_2:
                            case FACTORIAL:
                            case LOG:
                                remove = true;
                                break;
                            case POLY:
                                newModParam = term.getModParam() - term2.getModParam();
                                if (newModParam <= 0) {
                                    remove=true;
                                }
                                break;
                            default:
                                break; // pls will dont change
                        }
                    }
                }
                if (!remove) {
                    bigOEquation.multiplyBigOTerm(term.getParam(), term.getMod(), newModParam);
                }
            }
        }

        return this;
    }

    public List<BigOTerm> multiplyTwoBigOTerms(List<BigOTerm> b1, List<BigOTerm> b2) {
        List<BigOTerm> b1Clone = new ArrayList<>();
        for (BigOTerm term: b1) {
            b1Clone.add(new BigOTerm(term));
        }
        List<BigOTerm> b2Clone = new ArrayList<>();
        for (BigOTerm term: b2) {
            b2Clone.add(new BigOTerm(term));
        }

        // handle constant case
        if (b1Clone.size() == 1 && b1Clone.get(0).getMod() == ComplexityModifier.CONSTANT) {
            return b2Clone;
        }
        if (b2Clone.size() == 1 && b2Clone.get(0).getMod() == ComplexityModifier.CONSTANT) {
            return b1Clone;
        }

        for (var paramTerm1: b1Clone) {
            boolean canCombine = false;
            for (var paramTerm2: b2Clone) {
                if (paramTerm1.getParam().equals(paramTerm2.getParam())
                        && paramTerm1.getMod().equals(ComplexityModifier.POLY) && paramTerm2.getMod().equals(ComplexityModifier.POLY)) {
                    paramTerm2.setModParam(paramTerm2.getModParam() + paramTerm1.getModParam());
                    canCombine = true;
                    break;
                }
            }
            if (!canCombine) {
                b2Clone.add(paramTerm1);
            }
        }
        return b2Clone;
    }

    public void multiplyBigOTerm(String param, ComplexityModifier mod, int modParams) {
        if (constantTime && mod == ComplexityModifier.CONSTANT) {
            return;
        }

        if (constantTime) {
            terms = new ArrayList<>(new ArrayList<>());
            List<BigOTerm> firstTerm = new ArrayList<>();
            firstTerm.add(new BigOTerm(param, mod, modParams));
            terms.add(firstTerm);
            constantTime = false;
            return;
        }
        availableParams.add(param);
        for (var term: this.terms) {
            // Check if can combine with other terms
            boolean canCombine = false;
            for (var paramTerm: term) {
                if (paramTerm.getMod().equals(mod)) {
                    switch (mod) {
                        case POLY -> {
                            paramTerm.setModParam(paramTerm.getModParam() + modParams);
                            canCombine = true;
                        }
                    }
                }
            }
            if (!canCombine) {
                term.add(new BigOTerm(param, mod, modParams));
            }
        }
    }

    public void toExponential(BigOTerm term) {
        // TODO
    }

    // Modifies the term in the BigOEquation
    public BigOEquation getFinalComplexity() {
        List<List<BigOTerm>> maxTerms = new ArrayList<>();

        // [log(n), n!]
        // [1,1,1,1]+[1]+[1]+[1]+[1]+[d]+[2^d,e]+[log(d),e,f]+[d^2,e,f]+[d,e^2,f]+[d^2,e,f]+[d,e^2,f]+[d,2^e,f]+[d,e]+[d]+[d]+[d]
        for (List<BigOTerm> multipliedTerms: terms) {
            // check if multipliedTerms is constant
            if (multipliedTerms.size() == 1 && multipliedTerms.get(0).getMod() == ComplexityModifier.CONSTANT) {
                continue;
            }
            // if maxTerm is empty, add multipliedTerms
            if (maxTerms.isEmpty()) {
                maxTerms.add(multipliedTerms);
                continue;
            }

            // make map for multipliedTerms
            Map<String, Integer> numMultTerms = new HashMap<>();
            for (BigOTerm term: multipliedTerms) {
                Integer num = 0;
                switch (term.getMod()) {
                    case POLY:
                        numMultTerms.put(term.getParam(), term.getModParam());
                        break;
                    case LOG:
                        num = numMultTerms.get("log(" + term.getParam() + ")");
                        if (num == null) {
                            num = 0;
                        }
                        numMultTerms.put("log(" + term.getParam() + ")", ++num);
                        break;
                    case EXP_2:
                        num = numMultTerms.get("2^(" + term.getParam() + ")");
                        if (num == null) {
                            num = 0;
                        }
                        numMultTerms.put("2^(" + term.getParam() + ")", ++num);
                        break;
                    case FACTORIAL:
                        num = numMultTerms.get(term.getParam() + "!");
                        if (num == null) {
                            num = 0;
                        }
                        numMultTerms.put(term.getParam() + "!", ++num);
                        break;
                    default: // break! they don't love you like I love you! break!!!! they don't love you like I love you!! (see will, I appreciate them)
                        break;
                }
            }

            // make map for maxMultTerms
            boolean ignoreTerm = false;
            for (List<BigOTerm> maxMultTerms: maxTerms) {
                Map<String, Integer> numMaxTerms = new HashMap<>();
                for (BigOTerm term : maxMultTerms) {
                    Integer num = 0;
                    switch (term.getMod()) {
                        case POLY:
                            numMaxTerms.put(term.getParam(), term.getModParam());
                            break;
                        case CONSTANT:
                            break;
                        case LOG:
                            num = numMaxTerms.get("log(" + term.getParam() + ")");
                            if (num == null) {
                                num = 0;
                            }
                            numMaxTerms.put("log(" + term.getParam() + ")", ++num);
                            break;
                        case EXP_2:
                            num = numMaxTerms.get("2^(" + term.getParam() + ")");
                            if (num == null) {
                                num = 0;
                            }
                            numMaxTerms.put("2^(" + term.getParam() + ")", ++num);
                            break;
                        case FACTORIAL:
                            num = numMaxTerms.get(term.getParam() + "!");
                            if (num == null) {
                                num = 0;
                            }
                            numMaxTerms.put(term.getParam() + "!", ++num);
                            break;
                        default: // there once was a default who put to sea, the name of the default was nah, id live
                            break;
                    }
                }

                // compare maps
                // find way to do da rest

                // check if letters are same, if diff continue

                boolean differentMaxKeys = false;
                for (String maxKey : numMaxTerms.keySet()) {
                    boolean found = false;
                    for (String multKey : numMultTerms.keySet()) {
                        if (maxKey.equals(multKey)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        differentMaxKeys = true;
                        break;
                    }
                }

                // actually check the map values
                boolean atLeastOneLessThan = false;
                boolean atLeatOneGreaterThan = false;
                boolean differentLetter = false;
                for (String multKey : numMultTerms.keySet()) {
                    Integer multValue = numMultTerms.get(multKey);
                    Integer maxValue = numMaxTerms.get(multKey);
                    if (maxValue == null) {
                        differentLetter = true;
                        continue;
                    }
                    if (multValue > maxValue) {
                        atLeatOneGreaterThan = true;
                        continue;
                    }
                    if (multValue < maxValue) {
                        atLeastOneLessThan = true;
                    }
                }
                if (atLeastOneLessThan && atLeatOneGreaterThan) {
                    continue;
                }
                if (atLeastOneLessThan) {
                    if (!differentLetter) {
                        ignoreTerm = true;
                    }
                    break;
                }

                if (atLeatOneGreaterThan) {
                    if (!differentMaxKeys) {
                        maxTerms.remove(maxMultTerms);
                        break;
                    }
                    continue;
                }

                if (differentLetter) {
                    if (!differentMaxKeys) {
                        maxTerms.remove(maxMultTerms);
                        break;
                    }
                    continue;
                }

                ignoreTerm = true;
                break;
            }
            if (!ignoreTerm) {
                maxTerms.add(multipliedTerms);
            }

        }

        if (maxTerms.isEmpty()) {
            return new BigOEquation();
        } else {
            return new BigOEquation(maxTerms);
        }
    }

    // Return the BigOEquation with the worse complexity
    public BigOEquation getWorseComplexity(BigOEquation other) {
        List<Pair<ComplexityModifier, Integer>> otherComplexities = new ArrayList<>();
        for (List<BigOTerm> mulTerms : other.getTerms()) {
            ComplexityModifier mod = ComplexityModifier.CONSTANT;
            int modParams = 1;
            for (BigOTerm mulTerm: mulTerms) {
                if (mulTerm.getMod().compareTo(mod) == 0) {
                    modParams += mulTerm.getModParam();
                } else if (mulTerm.getMod().compareTo(mod) > 0) {
                    mod = mulTerm.getMod();
                    modParams = mulTerm.getModParam();
                }
            }
            otherComplexities.add(new Pair<>(mod, modParams));
        }
        List<Pair<ComplexityModifier, Integer>> thisComplexities = new ArrayList<>();
        for (List<BigOTerm> mulTerms : this.getTerms()) {
            ComplexityModifier mod = ComplexityModifier.CONSTANT;
            int modParams = 1;
            for (BigOTerm mulTerm: mulTerms) {
                if (mulTerm.getMod().compareTo(mod) == 0) {
                    modParams += mulTerm.getModParam();
                } else if (mulTerm.getMod().compareTo(mod) > 0) {
                    mod = mulTerm.getMod();
                    modParams = mulTerm.getModParam();
                }
            }
            thisComplexities.add(new Pair<>(mod, modParams));
        }

        ComplexityModifier otherMaxComplexity = ComplexityModifier.CONSTANT;
        int otherMaxComplexityParam = 1;
        for (Pair<ComplexityModifier, Integer> pair: otherComplexities) {
            if (otherMaxComplexity.compareModifier(pair.a) == 0) {
                otherMaxComplexityParam = Math.max(otherMaxComplexityParam, pair.b);
            } else if (otherMaxComplexity.compareModifier(pair.a) < 0) {
                otherMaxComplexity = pair.a;
                otherMaxComplexityParam = pair.b;
            }
        }
        ComplexityModifier thisMaxComplexity = ComplexityModifier.CONSTANT;
        int thisMaxComplexityParam = 1;
        for (Pair<ComplexityModifier, Integer> pair: thisComplexities) {
            if (thisMaxComplexity.compareModifier(pair.a) == 0) {
                thisMaxComplexityParam = Math.max(thisMaxComplexityParam, pair.b);
            } else if (thisMaxComplexity.compareModifier(pair.a) < 0) {
                thisMaxComplexity = pair.a;
                thisMaxComplexityParam = pair.b;
            }
        }

        if (thisMaxComplexity.compareModifier(otherMaxComplexity) == 0) {
            if (thisMaxComplexityParam == otherMaxComplexityParam) {
                return otherComplexities.size() > thisComplexities.size() ? other : this;
            }
            if (thisMaxComplexityParam >= otherMaxComplexityParam) {
                return this;
            }
            return other;
        }
        if (thisMaxComplexity.compareModifier(otherMaxComplexity) > 0) {
            return this;
        }

        return other;
    }

    // Get the max complexity for a and b for the specific parameter, then compare the max complexities
    // compare number of parameter terms for tie breakers, this is an approximation
    public boolean isWorseComplexity(List<BigOTerm> a, List<BigOTerm> b, String refParam) {
        BigOTerm maxATerm = new BigOTerm("", ComplexityModifier.CONSTANT, 0);
        int countTermsA = 0;
        for (var term: a) {
            if (!refParam.equals(term.getParam())) {
                continue;
            }
            countTermsA++;
            if (term.getMod().compareModifier(maxATerm.getMod()) > 0) {
                maxATerm = new BigOTerm(term);
            }
        }

        // end if first term is constant
        if (maxATerm.equals(new BigOTerm("", ComplexityModifier.CONSTANT, 0))) {
            return false;
        }

        BigOTerm maxBTerm = new BigOTerm("", ComplexityModifier.CONSTANT, 0);
        int countTermsB = 0;
        for (var term: b) {
            if (!refParam.equals(term.getParam())) {
                continue;
            }
            countTermsB++;
            if (term.getMod().compareModifier(maxBTerm.getMod()) > 0) {
                maxATerm = new BigOTerm(term);
            }
        }

        // Compare A and B
        if (maxATerm.getMod().compareModifier(maxBTerm.getMod()) > 0) {
            return true;
        } else if (maxATerm.getMod().equals(maxBTerm.getMod())) {
            if (maxATerm.getMod().equals(ComplexityModifier.POLY)) {
                if (maxATerm.getModParam() > maxBTerm.getModParam()) {
                    return true;
                } else if (maxATerm.getModParam() == maxBTerm.getModParam()){
                    return countTermsA >= countTermsB;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder prefix = new StringBuilder("O(");
        List<String> termList = new ArrayList<>();
        for (var term: terms) {
            StringBuilder currString = new StringBuilder();
            for (var parameters: term) {
                currString.append(parameters.toString());
            }
            termList.add(currString.toString());
        }
        prefix.append(String.join("+", termList));
        prefix.append(")");
        return prefix.toString();
    }

    public List<List<BigOTerm>> getTerms() {
        return terms;
    }

    public boolean isConstantTime() {
        return constantTime;
    }

    public Set<String> getAvailableParams() {
        return availableParams;
    }

    public BigOEquation copy() {
        return new BigOEquation(this);
    }
}
