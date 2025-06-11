package ui;

import analyzer.BigOwOAnalyzer;
import analyzer.complexity.BigOEquation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OwOWelcome extends JFrame {
    private JPanel contentPane;
    private JLabel welcomeLabel;
    private JButton chooseFileButton;
    private JLabel selectedFileLabel;
    private ImageIcon backgroundImage;
    private File selectedFile;
    private JComboBox<Object> methodDropDown;
    private CompilationUnit compUnit;
    private List<MethodDeclaration> methods;
    private JButton submitFileButton;

    public OwOWelcome() {
        // Set up the frame
        setTitle("OwO Welcome Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setResizable(false);
        setLocationRelativeTo(null);

        contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        welcomeLabel = new JLabel("Welcome to the Big OWO Analyzer!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);


        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 0));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);

        chooseFileButton = new JButton("Choose File");
        selectedFileLabel = new JLabel("No file selected", JLabel.CENTER);
        selectedFileLabel.setForeground(Color.WHITE);
        selectedFileLabel.setOpaque(true);
        selectedFileLabel.setBackground(new Color(254, 212, 213));

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(OwOWelcome.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    selectedFileLabel.setText("Selected file: " + selectedFile.getName());
                }
            }
        });

        submitFileButton = new JButton("Submit File");
        submitFileButton.setBackground(new Color(48, 37, 59));
        submitFileButton.setForeground(Color.WHITE);
        submitFileButton.setOpaque(true);
        submitFileButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        submitFileButton.addActionListener(e -> fileSubmitAction());

        // Create panel for file chooser components
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        filePanel.setOpaque(false);
        filePanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 50, 50));

        // Center the button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(chooseFileButton);

        filePanel.add(buttonPanel);
        filePanel.add(Box.createRigidArea(new Dimension(0, 15)));
        filePanel.add(selectedFileLabel);


        JPanel methodPanel = new JPanel();
        methodPanel.setOpaque(false);
        methodDropDown = new JComboBox<>(); // Initialize it empty

        // Create the method submit button
        JButton methodSubmitButton = new JButton("Analyze Method");
        methodSubmitButton.setBackground(Color.WHITE);
        methodSubmitButton.setForeground(new Color(48, 37, 59));
        methodSubmitButton.setOpaque(true);

        // Add action listener to the method submit button
        methodSubmitButton.addActionListener(e -> {
            String selectedMethod = (String) methodDropDown.getSelectedItem();
            if (selectedMethod != null) {
                spawnBigOwOFrame(selectedMethod);
            }
        });

        // Add components to the method panel
        methodPanel.add(methodDropDown);
        methodPanel.add(methodSubmitButton);
        methodPanel.setVisible(false); // Hide it initially

        // Add the method panel between filePanel and submitFileButton
        contentPane.add(methodPanel, BorderLayout.CENTER);
        contentPane.add(filePanel, BorderLayout.NORTH); // Move filePanel to NORTH
        contentPane.add(submitFileButton, BorderLayout.SOUTH);


        setBackgroundImage("src/ui/images/OwOImage.png");

        setVisible(true);
    }

    private MethodDeclaration getTargetMethod(String methodName) {
        for (MethodDeclaration method : methods) {
            if (method.getName().toString().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private void spawnBigOwOFrame(String method) {
        // POSSIBLE ERROR HANDLING NEEDED HERE!
        MethodDeclaration targetMethod = getTargetMethod(method);
        if (!(targetMethod == null)) {
            BigOwOAnalyzer analyzer = new BigOwOAnalyzer();
            CompilationUnit singleMethodUnit = new CompilationUnit();

            compUnit.getImports().forEach(singleMethodUnit::addImport);

            ClassOrInterfaceDeclaration tempClass = singleMethodUnit.addClass("TempClass");

            MethodDeclaration clonedMethod = targetMethod.clone();
            tempClass.addMember(clonedMethod);

            // Now analyze just this compilation unit with your single method
            analyzer.analyze(singleMethodUnit);
            Map<Integer, BigOEquation> result = analyzer.getResults();

            try {
                String code = Files.readString(selectedFile.toPath());
                OwOFrame f = new OwOFrame(method, analyzer.getMethodFinalComplexity(), analyzer.getFieldToParamMap(), analyzer.getMethodToParamMap(), analyzer.getArgumentToParamMap(), analyzer.getEquivalencies(), code, result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private CompilationUnit parseFullFile() throws FileNotFoundException {
        JavaParser javaParser = new JavaParser();

        return javaParser.parse(selectedFile).getResult().orElse(null);
    }

    private void methodSelectionOptions() throws FileNotFoundException {
        compUnit = parseFullFile();

        methods = compUnit.findAll(MethodDeclaration.class);
        ArrayList<String> methodNameList = new ArrayList<>();
        methods.forEach(method -> {methodNameList.add(method.getName().toString());});

        methodDropDown.removeAllItems();
        for (String methodName : methodNameList) {
            methodDropDown.addItem(methodName);
        }

        // Make the panel visible and refresh the UI
        methodDropDown.getParent().setVisible(true);
        revalidate();
        repaint();
    }

    private void fileSubmitAction() {
        if (selectedFile != null) {
            if (selectedFile.getName().endsWith(".java")) {
                selectedFileLabel.setText("Selected file: " + selectedFile.getName());
                try {
                    methodSelectionOptions();
                    submitFileButton.setVisible(false);
                    chooseFileButton.setVisible(false);
                }
                 catch (FileNotFoundException e) {
                    // TODO: Probably do some better logging here
                    e.printStackTrace();
                 }
            } else {
                selectedFileLabel.setText("INVALID FILE SELECTED! CHOOSE A .JAVA FILE");
            }
        } else {
            selectedFileLabel.setText("No file selected");
            // do nothing if no file selected
        }
    }

    // Method to set the background image
    public void setBackgroundImage(String imagePath) {
        backgroundImage = new ImageIcon(imagePath);
        repaint(); // Redraw the panel with the new background
    }

}
