package analyzer;

import analyzer.complexity.BigOEquation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyzerTest {
    Analyzer analyzer;
    JavaParser javaParser;

    @BeforeEach
    void setUp() {
        analyzer = new BigOwOAnalyzer();
        javaParser = new JavaParser();
    }

    // For the memories...
    @Test
    public void testJUnit() {
        assertTrue(true);
    }

    @Test
    public void testExposeBasicMethodCalls() {
        String sample_code = """
                public class FirstClass {
                    public void test(int n, int m) {
                        int j = new Integer();
                        int i = j;
                        i = n + m;
                        for (int g = 0; g < i; i++) {
                            int p = 0;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(a+b)");
    }

    @Test
    public void basicBigO() { // TODO: boolean operations not supported yet
        String sample_code = """
                public class FirstClass {
                    public void test() {
                        int x = 4;
                        int y = 5 * x + (x + 6) * 7 + 8;
                        int i = 0 + 1 + 2 + 3;
                        int m = someFunction1() + someFunction2();
                        Integer j = new Integer(i) + 2;
                        String k = new String("" + "a" + new String("c" + "d")) + "b";
                        boolean t = false || false;
                        List<String> h = new ArrayList<String>();
                        Map<String, Integer> m = new HashMap<String, Integer>();
                        int[] poo = new int[3] { 1, 2, 3 };
                        int[] poopoo = { 1, 2 };
                        int[] pee = new int[1];
                        int[] peepee = new int[someFunction1()];
                    }
                }
                """;
        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();

        analyzer.printComplexities();
        assertEquals("O(i+j)", result.get(2).toString());
    }


    @Test
    public void testForLoopSimple() {
        String sample_code = """
                public class FirstClass {
                    public void test(int n) {
                        int m = n;
                        for (int i = 0; i < m; i++) {
                            int j = 4;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(a)");
    }

    @Test
    public void testForLoopPoly() {
        String sample_code = """
                public class FirstClass {
                    public void test(int n) {
                        int m = n*n;
                        for (int i = 0; i < m; i++) {
                            int j = 4;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(a^2)");
    }

    @Test
    public void testForEachLoop() {
        String sample_code = """
                public class FirstClass {
                    public void test(List<String> stuffs) {
                        for (String stuff: stuffs) {
                            int j = 4;
                            int z = 5;
                            int p = 7;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(a)");
    }

    @Test
    public void testWhileLoop() {
        String sample_code = """
                public class FirstClass {
                    public void test(int n) {
                        int i = 0;
                        while (i < n) {
                            i++;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(a)");
    }

    @Test
    public void testWhileComplexLoop() {
        String sample_code = """
                public class FirstClass {
                    public void test(int n) {
                        int i = 0;
                        while (i < n) {
                            i = i * 2;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(log(a))");
    }

    @Test
    public void testMethodCalls() {
        String sample_code = """
                public class FirstClass {
                    Map<String, String> poopoopeepee;
                    public void test(List<String> arrayList, String str1, String str2) {
                        sort(arrayList);
                        arrayList.sort();
                        str1.compareTo(str2);
                        Arrays.sort(arrayList);
                        String what = arrayList.get(0);
                        poopoopeepee.put("Help", "Help");
                        while (i < a) {
                            i = i * 2;
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(3).toString(), "O(blog(b)+d+log(h))");
    }

    @Test
    public void testSwitchStatements() {
        String sample_code = """
                public class FirstClass {
                    public static String getNumberName(int number) {
                        switch (number) {
                            case 1:
                                return "One";
                            case 2:
                                return hello();
                            case 3:
                                return "Three";
                            default:
                                return "Other";
                        }
                    }
                }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(b)");
    }

    @Test
    public void testTryCatchStatement() {
        String sample_code = """
        public class FirstClass {
            public void test(String stuff) {
                try {
                  int number = Integer.parseInt(numberStr);
                  System.out.println("Parsed number: " + number);
                } catch (NumberFormatException e) {
                  System.out.println("Error: '" + numberStr + "' is not a valid number.");
                }
            }
        }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(2).toString(), "O(d)");
    }

    @Test
    public void testFields() {
        String sample_code = """
        public class FirstClass {
            int m = 200;
            public void test(int i) {
                for (int p = 0; p < m; p++) {
                    for (int j = i; j > 0; j++) {
                        String str = "Skibidi Toilet";
                    }
                }
            }
        }
                """;

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        analyzer.printComplexities();
        assertEquals(result.get(3).toString(), "O(ab)");
    }

    @Test
    public void testExposeBasicBlock() {
        String sample_code = """
                public class FirstClass {
                    public int s, t = 1;
                    public int a = poopy();
                    public int test(int n, int m) {
                        int i = n;
                        boolean b = true;
                        int r = m;
                        int j = i + m;
                        int w = n.length > 2 ? n : 1;
                
                        // d^2ef
                        
                        while (i < r) { // e^2fd + ed^2f
                            while (r < m) { // e^2f + edf
                                while (someFunction()) { // ef + df
                                    while (j >= i) { // e+d
                                        break;
                                    }
                                    break;
                                }
                                break;
                            }
                            break;
                        }
                        for (int i = 0; i < n; i++) {
                            n--;
                        }
                    }
                }
                """;

        // while(true) {
        // if ...
        // break
        //}

        // my boy while (false)

        // while(var) {

        // }

        // while(i < j) {
        // ...}

        CompilationUnit cu = javaParser.parse(sample_code).getResult().orElse(null);
        if (cu == null) {
            throw new Error("Please check the syntax of your input code");
        }
        analyzer.analyze(cu);
        Map<Integer, BigOEquation> result = analyzer.getResults();
        if (result.isEmpty()) {
            throw new Error("Please check the syntax of your input code");
        }
        System.out.println(result);
        analyzer.printComplexities();
    }
}