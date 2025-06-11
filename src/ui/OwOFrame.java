package ui;

import analyzer.complexity.BigOEquation;
import analyzer.complexity.BigOTerm;
import analyzer.complexity.ComplexityModifier;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OwOFrame extends JFrame {
    private final int WINDOW_WIDTH = 650;
    private final int WINDOW_HEIGHT = 800;
    private final String methodName;
    private final BigOEquation methodApproxFn;
    private final Map<String, String> fieldToParamMap;
    private final Map<String, String> methodToParamMap;
    private final Map<String, String> argToParamMap;
    private final Map<String, BigOEquation> equivalenciesMap;
    private final String fnCode;
    private final Map<Integer, BigOEquation> lineTimeComplexity;
    private Map<Integer, Color> lineColorMap;

    private JPanel graphPanel;
    private JLabel approxFnLabel;
    private ArrayList<JComboBox<Object>> methodComboBoxList;
    private JTextPane codePane;
//    private Map<String, Color> bigOColourLegend;

    private static final Map<String, Color> TIME_COMPLEXITY_COLORS = new LinkedHashMap<>() {
        {
            put("O(1)", Color.decode("#00AA00"));          // Constant time - Green
            put("O(log n)", Color.decode("#55BB33"));      // Logarithmic time - Light green
            put("O(n)", Color.decode("#AACC44"));          // Linear time - Yellow-green
            put("O(n log n)", Color.decode("#DDDD55"));    // Linearithmic time - Yellow
            put("O(n^2)", Color.decode("#FFAA00"));        // Quadratic time - Orange
            put("O(n^3)", Color.decode("#FF7700"));        // Cubic time - Dark orange
            put("O(n^4)", Color.decode("#FF4400"));        // Quartic time - Orange-red
            put("O(n^5)", Color.decode("#FF2200"));        // Quintic time - Near red
            put("O(2^n)", Color.decode("#FF1100"));        // Exponential time - Almost red
            put("O(n!)", Color.decode("#FF0000"));         // Factorial time - Red
        }};


    public OwOFrame(String methodName, BigOEquation methodFinalComplexity, Map<String, String> fieldMap, Map<String, String> methodMap, Map<String, String> argMap, Map<String, BigOEquation> equivalenciesMap, String fnCode, Map<Integer, BigOEquation> lineTimeComplexity) {
        super("OwO Analysis of " + methodName);
        this.methodName = methodName;
        this.fnCode = fnCode;
        this.fieldToParamMap = fieldMap;
        this.methodToParamMap = methodMap;
        this.argToParamMap = argMap;
        this.lineTimeComplexity = lineTimeComplexity;
        this.methodApproxFn = methodFinalComplexity;
        this.equivalenciesMap = equivalenciesMap;


        graphPanel = new JPanel();
        methodComboBoxList = new ArrayList<>();


        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        // Use BorderLayout as the main layout
        setLayout(new BorderLayout());

        initLineColourMap();


        // Initialize all components and add them to the frame
        initComponents();

        setVisible(true);
    }



    private String findWorstComplexityFunction(BigOEquation bigOEquation) {
        BigOTerm worstSoFar = bigOEquation.getTerms().get(0).get(0);
        for (List<BigOTerm> list: bigOEquation.getTerms()) {
            for (BigOTerm bigOTerm: list) {
                // does bigOTerm have higher order than worstSoFar??
                if (isHigherOrderFunc(bigOTerm, worstSoFar)) {
                    worstSoFar = bigOTerm;
                }
            }
        }

        return getOrder(worstSoFar);
    }


    private void initLineColourMap() {
        lineColorMap = new HashMap<>();
        lineTimeComplexity.forEach((lineNum, bigOEquation) -> {
            String domTerm = findWorstComplexityFunction(bigOEquation);

            lineColorMap.put(lineNum, TIME_COMPLEXITY_COLORS.get(domTerm));
        });
    }


    private void initComponents() {
        // Main panel with vertical BoxLayout to hold all sections
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(194, 212, 229));

        // Runtime Analysis Section
        JPanel runtimePanel = createRuntimePanel();
        mainPanel.add(runtimePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Variable Mapping Section
        JPanel variablePanel = createVariablePanel();
        mainPanel.add(variablePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // External Method Runtime Section (placeholder for future implementation)
        JPanel methodCallPanel = createMethodCallPanel();
        mainPanel.add(methodCallPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Heat Map Section
        JPanel heatMapPanel = createHeatMapPanel();
        mainPanel.add(heatMapPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Code Display Section
        // TRY/CATCH IS FOR TROUBLESHOOTING

//        try {
            JPanel codePanel = createCodePanel();
            mainPanel.add(codePanel);
//        }
//        catch (Exception e) {
//            System.out.println("Oh nooooo");
//        }

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Graph Section
        JPanel graphPanel = createGraphPanel();
        mainPanel.add(graphPanel);

        // Add the main panel to a scroll pane with optimized scrolling
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        // Optimize scrolling speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createRuntimePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("Runtime Analysis"));

        JLabel title = new JLabel("Big O runtime of " + methodName + ":");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setFont(new Font("Dialog", Font.BOLD, 14));
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        this.approxFnLabel = new JLabel(methodApproxFn.toString());
        approxFnLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        approxFnLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        approxFnLabel.setForeground(new Color(0, 102, 204));
        panel.add(approxFnLabel);

        return panel;
    }

    private JPanel createVariablePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("Variable Definitions"));

        JLabel whereLabel = new JLabel("Where:");
        whereLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        whereLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        panel.add(whereLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel varGrid = new JPanel(new GridLayout(0, 1, 5, 5));
        varGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel argLabel = new JLabel("Arguments:");
        argLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        argLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        varGrid.add(argLabel);

        if (!argToParamMap.isEmpty()) {
            argToParamMap.forEach((argName, mappedLtr) -> {
                JLabel varAlphMapLabel = new JLabel(mappedLtr + " = size of " + argName);
                varAlphMapLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
                varGrid.add(varAlphMapLabel);
            });
        } else {
            JLabel noArgs = new JLabel("No arguments detected");
            varGrid.add(noArgs);
        }



        JLabel fieldLabel = new JLabel("Class Fields:");
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        varGrid.add(fieldLabel);

        if (!fieldToParamMap.isEmpty()) {
            fieldToParamMap.forEach((fieldName, mappedLtr) -> {
                JLabel varAlphMapLabel = new JLabel(mappedLtr + " = size of " + fieldName);
                varAlphMapLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
                varGrid.add(varAlphMapLabel);
            });
        } else {
            JLabel noFields = new JLabel("No fields detected");
            varGrid.add(noFields);
        }



        JLabel equivalenciesLabel = new JLabel("Equivalencies:");
        equivalenciesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        equivalenciesLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        varGrid.add(equivalenciesLabel);

        if (!equivalenciesMap.isEmpty()) {
            equivalenciesMap.forEach((mappedLtr, bigO) -> {
                JLabel varEquivMapLabel = new JLabel(mappedLtr + " = " + bigO.toString());
                varEquivMapLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
                varGrid.add(varEquivMapLabel);
            });
        } else {
            JLabel noFields = new JLabel("No fields detected");
            varGrid.add(noFields);
        }


        panel.add(varGrid);
        return panel;
    }

    private JPanel createMethodCallPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("External Method Runtime (optional)"));

        if (!methodToParamMap.isEmpty()) {
            JLabel methodsLabel = new JLabel("External method calls:");
            methodsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            methodsLabel.setFont(new Font("Dialog", Font.BOLD, 14));
            panel.add(methodsLabel);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));


            ArrayList<String> complexityList = new ArrayList<>();
            complexityList.add("DEFAULT");
            complexityList.add("O(1)");
            complexityList.add("O(log n)");
            complexityList.add("O(n)");
            complexityList.add("O(nlog(n))");
            complexityList.add("O(n^2)");
            complexityList.add("O(n^3)");
            complexityList.add("O(2^n)");
            complexityList.add("O(3^n)");
            complexityList.add("O(4^n)");
            complexityList.add("O(n!)");

            methodToParamMap.forEach((methodName, mappedLtr) -> {
                JComboBox<Object> comboBox = new JComboBox<>(complexityList.toArray());
                JLabel methodCallLabel = new JLabel("Complexity of method call " + methodName + " = " + mappedLtr);
                methodCallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                comboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
                panel.add(methodCallLabel);
                panel.add(comboBox);
                comboBox.putClientProperty("letterMap", mappedLtr);
                methodComboBoxList.add(comboBox);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JButton runTimeUpdate = new JButton("Update Runtime");
            runTimeUpdate.addActionListener(e -> externalMethodRuntimeUpdated());
            buttonPanel.add(runTimeUpdate);

            panel.add(buttonPanel);
        } else {
            JLabel noMethodsLabel = new JLabel("No external method calls detected");
            noMethodsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            noMethodsLabel.setFont(new Font("Dialog", Font.ITALIC, 12));
            noMethodsLabel.setForeground(Color.GRAY);
            panel.add(noMethodsLabel);
        }

        return panel;
    }

    private JPanel createHeatMapPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("Runtime Heat Map of " + methodName + ":"));

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Heat map legend - single row, no wrapping
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.X_AXIS));
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        TIME_COMPLEXITY_COLORS.forEach((order, colour) -> {
            // Individual complexity item panel
            JPanel orderPanel = new JPanel();
            orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
            orderPanel.setAlignmentY(Component.TOP_ALIGNMENT);

            // Color swatch
            JPanel colorSwatch = new JPanel();
            colorSwatch.setPreferredSize(new Dimension(20, 15)); // Smaller size
            colorSwatch.setMaximumSize(new Dimension(20, 15));   // Ensure fixed size
            colorSwatch.setBackground(colour);
            colorSwatch.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            colorSwatch.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Label
            JLabel orderLabel = new JLabel(order);
            orderLabel.setFont(new Font("Monospaced", Font.PLAIN, 10)); // Smaller font
            orderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add components to the item panel
            orderPanel.add(colorSwatch);
            orderPanel.add(Box.createRigidArea(new Dimension(0, 2)));
            orderPanel.add(orderLabel);

            // Add small spacing between complexity items
            if (legendPanel.getComponentCount() > 0) {
                legendPanel.add(Box.createRigidArea(new Dimension(8, 0)));
            }

            // Add this complexity item to the legend
            legendPanel.add(orderPanel);
        });

        // Wrap the legend in a panel that prevents wrapping
        JPanel legendContainer = new JPanel(new BorderLayout());
        legendContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        legendContainer.add(legendPanel, BorderLayout.WEST);

        panel.add(legendContainer);

        return panel;
    }

    private void addBigOComplexityToLine(int lineNumber, StyledDocument doc, Element lineToEdit) {
        int startOffset = lineToEdit.getStartOffset();
        int endOffset = lineToEdit.getEndOffset();
        int length = endOffset - startOffset;

        BigOEquation complexity = lineTimeComplexity.get(lineNumber);
        if (complexity != null) {
            String complexityLabel = " [" + complexity + "]";
            try {
                String lineText = doc.getText(startOffset, length);

                // Insert before new line characters
                int insertPosition = length;
                if (insertPosition > 0 && lineText.endsWith("\n")) {
                    insertPosition--;
                }
                if (insertPosition > 0 && lineText.charAt(insertPosition-1) == '\r') {
                    insertPosition--;
                }

                doc.insertString(startOffset + insertPosition, complexityLabel, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } // else do nothing
    }


    private void refreshLineColors() {
        StyledDocument doc = codePane.getStyledDocument();
        Element root = doc.getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element line = root.getElement(i);
            int startOffset = line.getStartOffset();
            int endOffset = line.getEndOffset();
            int length = endOffset - startOffset;

            Style style = codePane.addStyle("lineColour", null);
            Color lineColour = lineColorMap.get(i+1);
            if (lineColour != null) {
                StyleConstants.setBackground(style, lineColour);
            } else {
                // Optional: reset to default background if no color specified
                StyleConstants.setBackground(style, codePane.getBackground());
            }
            doc.setCharacterAttributes(startOffset, length, style, false);
        }
    }

    private void bigOComplexityLabels() {
        StyledDocument doc = codePane.getStyledDocument();
        Element root = doc.getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element line = root.getElement(i);
            addBigOComplexityToLine(i + 1, doc, line);
        }
    }

    private JPanel createCodePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(createTitledBorder("Code with Heat Map"));

        codePane = new JTextPane();
        codePane.setEditable(false);
        codePane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        codePane.setMargin(new Insets(5, 5, 5, 5));

        // Set the text content
        codePane.setText(fnCode);




        refreshLineColors();
        bigOComplexityLabels();



        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attr, "Monospaced");
        StyleConstants.setFontSize(attr, 12);



        JScrollPane scrollPane = new JScrollPane(codePane);
        scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH - 80, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGraphPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(createTitledBorder("Runtime Graph"));

        JLabel graphLabel = new JLabel("Runtime graph for " + methodName + " variables:");
        graphLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        graphLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        panel.add(graphLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Options for graph display
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup bg = new ButtonGroup();

        AtomicBoolean first = new AtomicBoolean(true);



        fieldToParamMap.forEach((fieldName, mappedLtr) -> {
            JRadioButton orderButton = new JRadioButton(mappedLtr);
            orderButton.addActionListener(e -> updateGraph(mappedLtr));
            orderButton.setFont(new Font("Dialog", Font.PLAIN, 12));
            bg.add(orderButton);
            radioPanel.add(orderButton);
        });

        argToParamMap.forEach((argName, mappedLtr) -> {
            JRadioButton orderButton = new JRadioButton(mappedLtr);
            orderButton.addActionListener(e -> updateGraph(mappedLtr));
            orderButton.setFont(new Font("Dialog", Font.PLAIN, 12));
            bg.add(orderButton);
            radioPanel.add(orderButton);
        });

        panel.add(radioPanel);



        panel.add(Box.createRigidArea(new Dimension(0, 10)));



        panel.add(graphPanel);
//        ChartPanel cp = updateGraph(varNames.get(0));
//
//        graphPanel.add(cp);

        return panel;
    }

    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                title
        );
        border.setTitleFont(new Font("Dialog", Font.BOLD, 14));
        border.setTitleColor(new Color(70, 70, 70));
        return border;
    }


    private void updateOverallRuntimeDisplay() {
        String currComplexity = methodApproxFn.toString();
        for (JComboBox<Object> jcb: methodComboBoxList) {
            // get the lettermap of each method
            char alph = jcb.getClientProperty("letterMap").toString().charAt(0);
            // get the function that was selected for that lettermap
            String selectedFn = jcb.getSelectedItem().toString();
            if (selectedFn.equals("DEFAULT")) {
                String alphTarget = "\\b" + Pattern.quote(String.valueOf(alph)) + "\\b";
                currComplexity = currComplexity.replaceAll(alphTarget, Matcher.quoteReplacement(String.valueOf(alph)));
            } else {
                // replace the n with the assigned alphabet character
                selectedFn = selectedFn.replace('n', alph);
                // strip off the 'O' and the brackets
                String custFn = selectedFn.substring(2, selectedFn.length()-1);
                // insert the custom function in the place of the placeholder alphabet
                String alphTarget = "\\b" + Pattern.quote(String.valueOf(alph)) + "\\b";
                currComplexity = currComplexity.replaceAll(alphTarget, Matcher.quoteReplacement(custFn));
            }

        }

        approxFnLabel.setText(currComplexity);
    }





    private void externalMethodRuntimeUpdated() {
        updateOverallRuntimeDisplay();
    }


// Checks if newTerm is higherOrder than highestSoFar
    private Boolean isHigherOrderFunc(BigOTerm newTerm, BigOTerm highestSoFar) {
        ComplexityModifier modNew = newTerm.getMod();
        ComplexityModifier modHigh = highestSoFar.getMod();
        if (modNew.getRank() > modHigh.getRank()) {
            return true;
        } else if (modNew.getRank() < modHigh.getRank()) {
            return false;
        } else {
            return newTerm.getModParam() > highestSoFar.getModParam();
        }
    }

    // Helper method to calculate factorial
    private double factorial(double n) {
        if (n <= 1) return 1;
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private String getOrder(BigOTerm bot) {
        if (bot.getMod() == ComplexityModifier.CONSTANT) {
            return "O(1)";
        } else if (bot.getMod() == ComplexityModifier.POLY) {
            if (bot.getModParam()%2 == 0) {
                if (bot.getModParam() == 2) {
                    return "O(n^2)";
                } else { // square is even and greater than 2
                    return "O(n^4)";
                }
            } else {
                if (bot.getModParam() == 1) {
                    return "O(n)";
                } else if (bot.getModParam() == 3) {
                    return "O(n^3)";
                } else { // this means the power is odd and greated than 3
                    return "O(n^5)";
                }
            }
        } else if (bot.getMod() == ComplexityModifier.LOG) {
            return "O(log n)";
        } else if (bot.getMod() == ComplexityModifier.EXP_2) {
            return "O(2^n)";
        } else { // this can only be factorial, nothing else!
            return "O(n!)";
        }
    }

    private String findFuncType(ArrayList<BigOTerm> terms) {
        BigOTerm highestSoFar = terms.get(0);
        for (BigOTerm term: terms) {
            if (isHigherOrderFunc(term, highestSoFar)) {
                highestSoFar = term;
            }
        }
        return getOrder(highestSoFar);
    }

    private JFreeChart renderChart(String funcType, String alph) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries(alph);

        // Define ranges and plotting points based on function type
        int maxX = 100;
        int numPoints = 50;

        System.out.println(alph);
        System.out.println(funcType);


        switch (funcType) {
            case "O(1)":
                maxX = 100;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, 1);  // Constant time
                }
                break;

            case "O(log n)":
                maxX = 1000;
                for (int i = 1; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, Math.log(x) / Math.log(2));  // log base 2
                }
                break;

            case "O(n)":
                maxX = 1000;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, x);  // Linear
                }
                break;

            case "O(n log n)":
                maxX = 500;
                for (int i = 1; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, x * Math.log(x) / Math.log(2));  // n log n
                }
                break;

            case "O(n^2)":
                maxX = 100;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, x * x);  // Quadratic
                }
                break;

            case "O(n^3)":
                maxX = 40;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, x * x * x);  // Cubic
                }
                break;

            case "O(n^4)": // for even powers greater than 2 the graph is similar-looking so this is our approximation
                maxX = 20;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, Math.pow(x, 4));  // Quartic
                }
                break;

            case "O(n^5)": // for odd powers greater than 3 the graph is similar-looking so this is our approximation
                maxX = 15;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, Math.pow(x, 5));  // Quintic
                }
                break;

            case "O(2^n)":
                maxX = 20;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, Math.pow(2, x));  // Exponential
                }
                break;

            case "O(n!)":
                maxX = 10;
                for (int i = 0; i <= numPoints; i++) {
                    double x = Math.floor((maxX * i) / (double) numPoints);
                    series.add(x, factorial(x));  // Factorial approx fn
                }
                break;

            default:  // Default case
                maxX = 50;
                for (int i = 0; i <= numPoints; i++) {
                    double x = (maxX * i) / (double) numPoints;
                    series.add(x, x * x);  // Default to quadratic
                }
        }

        dataset.addSeries(series);
        return ChartFactory.createXYLineChart(
                "Time complexity of " + alph, "Input Size", "Complexity", dataset,
                PlotOrientation.VERTICAL, false, false, false
        );
    }



    private void updateGraph(String alph) {
        ArrayList<BigOTerm> termsSeen = new ArrayList<>();
        for (List<BigOTerm> bigOSegment: methodApproxFn.getTerms()) {
            for (BigOTerm bigOTerm: bigOSegment) {
                if (bigOTerm.getParam().equals(alph)) {
                    termsSeen.add(bigOTerm);
                }
            }
        }

        String funcType;
        if (!termsSeen.isEmpty()) {
            funcType = findFuncType(termsSeen);
        } else {
            funcType = "O(1)";
        }



        JFreeChart chart = renderChart(funcType, alph);


        XYPlot plot = (XYPlot) chart.getPlot();


        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Remove numbered axes
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();

// Hide X-axis tick marks and labels
        xAxis.setTickMarksVisible(false);
        xAxis.setTickLabelsVisible(false);

// Hide Y-axis tick marks and labels
        yAxis.setTickMarksVisible(false);
        yAxis.setTickLabelsVisible(false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(WINDOW_WIDTH - 80, 200));
        chartPanel.setMinimumDrawWidth(0);
        chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
        chartPanel.setMinimumDrawHeight(0);
        chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

        graphPanel.removeAll();
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }
}