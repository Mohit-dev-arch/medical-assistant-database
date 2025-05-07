// ... [unchanged imports]
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicalAssistantGUI extends JFrame {
    private JTextField nameField;
    private JTextField idField;
    private JTextArea symptomsArea;
    private JTable resultTable;
    private JButton diagnoseButton;
    private JButton addSymptomButton;
    private JTextField symptomField;
    private List<String> symptomsList;
    private MedicalDataBase db;
    private DefaultTableModel tableModel;

    public MedicalAssistantGUI() {
        try {
            db = new MedicalDataBase();
            symptomsList = new ArrayList<>();
            initializeGUI();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error initializing database: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeGUI() {
        setTitle("Medical Assistant System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel patientPanel = createPatientPanel();
        mainPanel.add(patientPanel, BorderLayout.NORTH);

        JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerPanel.setDividerLocation(300);
        centerPanel.setResizeWeight(0.5);

        JPanel symptomsPanel = createSymptomsPanel();
        centerPanel.setTopComponent(symptomsPanel);

        JPanel resultsPanel = createResultsPanel();
        centerPanel.setBottomComponent(resultsPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Patient Information",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Patient ID:"), gbc);

        gbc.gridx = 1;
        idField = new JTextField(20);
        idField.setEditable(false);  // ID is auto-generated
        try {
            int newId = db.getNextPatientId();
            idField.setText(String.valueOf(newId));
        } catch (IOException ex) {
            idField.setText("Error");
        }
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);

        return panel;
    }

    private JPanel createSymptomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Symptoms",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        symptomField = new JTextField();
        addSymptomButton = new JButton("Add Symptom");
        inputPanel.add(symptomField, BorderLayout.CENTER);
        inputPanel.add(addSymptomButton, BorderLayout.EAST);

        symptomsArea = new JTextArea();
        symptomsArea.setEditable(false);
        symptomsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(symptomsArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        addSymptomButton.addActionListener(e -> addSymptom());

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "High Probability Diagnoses (>50%)",
                TitledBorder.LEFT,
                TitledBorder.TOP));

        String[] columnNames = {"Disease", "Probability", "Matching Symptoms"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Arial", Font.PLAIN, 14));
        resultTable.setRowHeight(25);
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(400);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        diagnoseButton = new JButton("Diagnose");
        diagnoseButton.setFont(new Font("Arial", Font.BOLD, 14));
        diagnoseButton.addActionListener(e -> performDiagnosis());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(diagnoseButton, BorderLayout.SOUTH);

        return panel;
    }

    private void addSymptom() {
        String symptom = symptomField.getText().trim();
        if (!symptom.isEmpty()) {
            try {
                db.addSymptom(symptom);
                symptomsList.add(symptom);
                symptomsArea.append(symptom + "\n");
                symptomField.setText("");
            } catch (MedicalDataBase.UnrecognizedSymptomException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        "Invalid Symptom", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private String getMatchingSymptoms(String disease) {
        List<String> matched = new ArrayList<>();
        List<String> requiredSymptoms = db.getSymptomsForDisease(disease);
        for (String s : symptomsList) {
            if (requiredSymptoms.contains(s.toLowerCase())) {
                matched.add(s);
            }
        }
        return String.join(", ", matched);
    }

    private void performDiagnosis() {
        String name = nameField.getText().trim();
        String id = idField.getText().trim();

        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter patient ID.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter patient name.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (symptomsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one symptom.",
                    "No Symptoms", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int patientId = Integer.parseInt(id);
            MedicalDataBase.DiagnosisResult[] diagnoses = db.diagnosis();
            tableModel.setRowCount(0);
            int maxToShow = Math.min(2, diagnoses.length);
            if (maxToShow == 0) {
                tableModel.addRow(new Object[]{"No high probability matches found", "", ""});
            } else {
                for (int i = 0; i < maxToShow; i++) {
                    MedicalDataBase.DiagnosisResult diagnosis = diagnoses[i];
                    String probability = String.format("%.1f%%", diagnosis.getProbability());
                    String matchingSymptoms = getMatchingSymptoms(diagnosis.getDisease());
                    tableModel.addRow(new Object[]{
                        diagnosis.getDisease(),
                        probability,
                        matchingSymptoms
                    });
                }
                db.savePatientRecord(patientId, name, diagnoses);
                JOptionPane.showMessageDialog(this, "Record saved successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid patient ID (numbers only).",
                    "Invalid ID", JOptionPane.WARNING_MESSAGE);
        } catch (MedicalDataBase.UnrecognizedDiseaseException e) {
            tableModel.setRowCount(0);
            tableModel.addRow(new Object[]{"No high probability matches found", "", ""});
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving record: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MedicalAssistantGUI gui = new MedicalAssistantGUI();
            gui.setVisible(true);
        });
    }
}
