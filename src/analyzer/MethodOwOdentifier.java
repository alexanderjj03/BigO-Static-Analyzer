package analyzer;

import analyzer.complexity.BigOEquation;
import analyzer.complexity.ComplexityModifier;

import java.util.HashMap;
import java.util.Map;

// Fetches time complexity for library methods for strings, lists, arrays, hashmaps
// e.g. object.method(args) (args arent relevant here). Each map stores {method: runtime}

// (not needed for ints/bools since theyre all O(1))
// If the method isn't any of these, returns O(?)
// N is the length of the string/list/array/hashmap

// Some methods rely on m (input length) but for our purposes I'll assume that m is in O(1)
// unless its known/a function of the parent method's arguments.
// Make analyzer able to pass this info down the AST

// AT SOME POINT (to make this look less horrible) add json files containing the k/v pairs
public class MethodOwOdentifier {
    private Map<String, String> strMethods; // maps method name to big o complexity
    private Map<String, String> arrayListMethods;
    private Map<String, String> linkedListMethods;
    private Map<String, String> arrMethods;
    private Map<String, String> hashMapMethods;

    public MethodOwOdentifier() {
        initializeStrMap();
        initializeArrListMap();
        initializeArrMap();
        initializeHashhMap();
    }

    public void initializeStrMap() {
        strMethods = new HashMap<>();
        strMethods.put("println", "O(1)");
        strMethods.put("print", "O(1)");
        strMethods.put("length", "O(1)");
        strMethods.put("charAt", "O(1)");
        strMethods.put("isEmpty", "O(1)");
        strMethods.put("indexOf", "O(n)"); // if the input is long the analyzer should know
        strMethods.put("lastIndexOf", "O(n)");
        strMethods.put("contentEquals", "O(n)");
        strMethods.put("equals", "O(n)");
        strMethods.put("equalsIgnoreCase", "O(n)");
        strMethods.put("compareTo", "O(n)");
        strMethods.put("compareToIgnoreCase", "O(n)");
        strMethods.put("matches", "O(n)");
        strMethods.put("substring", "O(n)");
        strMethods.put("subSequence", "O(n)");
        strMethods.put("toLowerCase", "O(n)");
        strMethods.put("toUpperCase", "O(n)");
        strMethods.put("trim", "O(n)");
        strMethods.put("replaceAll", "O(n)");
        strMethods.put("replaceFirst", "O(n)");
        strMethods.put("split", "O(n)");
        strMethods.put("join", "O(n)");
        strMethods.put("strip", "O(n)");
        strMethods.put("stripLeading", "O(n)");
        strMethods.put("stripTrailing", "O(n)");
        strMethods.put("stripIndent", "O(n)");
        strMethods.put("toCharArray", "O(n)");
        strMethods.put("getBytes", "O(n)");
        strMethods.put("format", "O(n)");
        strMethods.put("repeat", "O(n)");
        strMethods.put("lines", "O(n)");
        strMethods.put("isBlank", "O(n)");
        strMethods.put("translateEscapes", "O(n)");
        strMethods.put("formatted", "O(n)");
        strMethods.put("indent", "O(n)");
        strMethods.put("transform", "O(n)");
    }

    public void initializeArrListMap() {
        arrayListMethods = new HashMap<>();
        arrayListMethods.put("add", "O(n)");
        arrayListMethods.put("remove", "O(n)");
        arrayListMethods.put("removeFirst", "O(n)");
        arrayListMethods.put("removeLast", "O(n)");
        arrayListMethods.put("get", "O(1)");
        arrayListMethods.put("set", "O(1)");
        arrayListMethods.put("contains", "O(n)");
        arrayListMethods.put("indexOf", "O(n)");
        arrayListMethods.put("lastIndexOf", "O(n)");
        arrayListMethods.put("clear", "O(n)");
        arrayListMethods.put("size", "O(1)");
        arrayListMethods.put("isEmpty", "O(1)");
        arrayListMethods.put("toArray", "O(n)");
        arrayListMethods.put("iterator", "O(1)");
        arrayListMethods.put("listIterator", "O(1)");
        arrayListMethods.put("forEach", "O(n)");
        arrayListMethods.put("ensureCapacity", "O(n)");
        arrayListMethods.put("trimToSize", "O(n)");
        arrayListMethods.put("removeIf", "O(n)");
        arrayListMethods.put("replaceAll", "O(n)");
        arrayListMethods.put("sort", "O(n log n)");
        arrayListMethods.put("spliterator", "O(1)");
    }

    public void initializeArrMap() {
        arrMethods = new HashMap<>();
        arrMethods.put("length", "O(1)");
        arrMethods.put("clone", "O(n)");
        arrMethods.put("equals", "O(n)");
        arrMethods.put("hashCode", "O(n)");
        arrMethods.put("toString", "O(n)");
    }

    public void initializeHashhMap() {
        hashMapMethods = new HashMap<>();
        hashMapMethods.put("put", "O(1)");
        hashMapMethods.put("get", "O(1)");
        hashMapMethods.put("remove", "O(1)");
        hashMapMethods.put("containsKey", "O(1)");
        hashMapMethods.put("containsValue", "O(n)");
        hashMapMethods.put("size", "O(1)");
        hashMapMethods.put("isEmpty", "O(1)");
        hashMapMethods.put("clear", "O(n)");
        hashMapMethods.put("keySet", "O(1)");
        hashMapMethods.put("values", "O(1)");
        hashMapMethods.put("entrySet", "O(1)");
        hashMapMethods.put("forEach", "O(n)");
        hashMapMethods.put("getOrDefault", "O(1)");
        hashMapMethods.put("putIfAbsent", "O(1)");
        hashMapMethods.put("replace", "O(1)");
        hashMapMethods.put("compute", "O(1)");
        hashMapMethods.put("computeIfAbsent", "O(1)");
        hashMapMethods.put("computeIfPresent", "O(1)");
        hashMapMethods.put("merge", "O(1)");
        hashMapMethods.put("hashCode", "O(1)");
    }

    public boolean hasComplexity(String methodName) {
        return strMethods.containsKey(methodName) || arrayListMethods.containsKey(methodName) || arrMethods.containsKey(methodName) || hashMapMethods.containsKey(methodName);
    }


    // Must use equivalency in visitor and returns null if nothing is found
    public BigOEquation getComplexity(String methodName, String param) {
        if (strMethods.containsKey(methodName)) {
            return getBigOFromString(methodName, param, strMethods);
        }
        if (arrayListMethods.containsKey(methodName)) {
            return getBigOFromString(methodName, param, arrayListMethods);
        }
        if (arrMethods.containsKey(methodName)) {
            return getBigOFromString(methodName, param, arrMethods);
        }
        if (hashMapMethods.containsKey(methodName)) {
            return getBigOFromString(methodName, param, hashMapMethods);
        }
        return null;
    }

    private static BigOEquation getBigOFromString(String methodName, String param, Map<String, String> fieldToCheck) {
        String complexity = fieldToCheck.get(methodName);
        switch (complexity) {
            case "O(1)": {
                return new BigOEquation();
            } case "O(n)": {
                return new BigOEquation(param, ComplexityModifier.POLY, 1);
            } case "O(n log n)": {
                BigOEquation ret = new BigOEquation(param, ComplexityModifier.POLY, 1);
                ret.multiplyBigOTerm(param, ComplexityModifier.LOG, 1);
                return ret;
            } case "O(n^2)": {
                return new BigOEquation(param, ComplexityModifier.POLY, 2);
            } default: {
                return null;
            }
        }
    }
}