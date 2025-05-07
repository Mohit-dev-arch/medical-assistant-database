import java.io.*;
import java.util.*;

public class MedicalDataBase {
    private Set<String> validSymptoms;
    private Map<String, Set<String>> diseaseToSymptoms;
    private Map<String, Set<String>> symptomToDiseases;
    private List<String> currentSymptoms;
    private static final String SYMPTOM_FILE = "symptom.txt";
    private static final String DISEASE_FILE = "disease.txt";
    private static final String DB_FILE = "medicalDatabase.csv";

    public MedicalDataBase() throws IOException {
        validSymptoms = new HashSet<>();
        diseaseToSymptoms = new HashMap<>();
        symptomToDiseases = new HashMap<>();
        currentSymptoms = new ArrayList<>();
        loadSymptoms();
        loadDiseases();
    }

    private void loadSymptoms() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(SYMPTOM_FILE));
        String line;
        while ((line = br.readLine()) != null) {
            String symptom = line.trim().toLowerCase();
            if (!symptom.isEmpty()) {
                validSymptoms.add(symptom);
            }
        }
        br.close();
    }

    private void loadDiseases() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(DISEASE_FILE));
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String disease = parts[0].trim();
                String[] symptomsArr = parts[1].split(",");
                Set<String> symptomsSet = new HashSet<>();
                for (String s : symptomsArr) {
                    String symptom = s.trim().toLowerCase();
                    symptomsSet.add(symptom);
                    // Build reverse map
                    symptomToDiseases.computeIfAbsent(symptom, k -> new HashSet<>()).add(disease);
                }
                diseaseToSymptoms.put(disease, symptomsSet);
            }
        }
        br.close();
    }

    public void addSymptom(String symptom) throws UnrecognizedSymptomException {
        String s = symptom.trim().toLowerCase();
        if (!validSymptoms.contains(s)) {
            throw new UnrecognizedSymptomException("Symptom '" + symptom + "' not recognized.");
        }
        if (!currentSymptoms.contains(s)) {
            currentSymptoms.add(s);
        }
    }

    public DiagnosisResult[] diagnosis() throws UnrecognizedDiseaseException {
        List<DiagnosisResult> results = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : diseaseToSymptoms.entrySet()) {
            String disease = entry.getKey();
            Set<String> requiredSymptoms = entry.getValue();
            int matchCount = 0;
            for (String s : currentSymptoms) {
                if (requiredSymptoms.contains(s)) {
                    matchCount++;
                }
            }
            double probability = (double) matchCount / requiredSymptoms.size();
            if (probability > 0.5) {
                results.add(new DiagnosisResult(disease, probability));
            }
        }
        if (results.isEmpty()) {
            throw new UnrecognizedDiseaseException("No disease matched the provided symptoms with probability > 50%.");
        }
        // Sort by probability descending, return all
        results.sort((a, b) -> Double.compare(b.getProbability(), a.getProbability()));
        return results.toArray(new DiagnosisResult[0]);
    }

    public void savePatientRecord(int patientId, String name, DiagnosisResult[] diagnoses) throws IOException {
        validateCsvFile(DB_FILE);
        File file = new File(DB_FILE);
        boolean header = !file.exists();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        
        if (header) {
            writer.write("patient id,patient name,symptoms list,disease 1,disease 2");
            writer.newLine();
        }
        
        String disease1 = diagnoses.length > 0 ? diagnoses[0].getDisease() : "";
        String disease2 = diagnoses.length > 1 ? diagnoses[1].getDisease() : "";
        
        // Quote the symptoms list since it contains commas
        String symptomsCell = "\"" + String.join(",", currentSymptoms) + "\"";
        
        writer.write(String.format("%d,%s,%s,%s,%s",
                patientId,
                name,
                symptomsCell,  // quoted symptoms list
                disease1,
                disease2
        ));
        writer.newLine();
        writer.close();
        currentSymptoms.clear();
    }

    private void validateCsvFile(String filename) throws IOException {
        if (!filename.equals(DB_FILE) || !filename.endsWith(".csv")) {
            throw new IOException("Invalid file. Must be named 'medicalDatabase.csv' and have .csv extension.");
        }
    }

    public int getNextPatientId() throws IOException {
        File file = new File(DB_FILE);
        if (!file.exists()) return 1;
        BufferedReader br = new BufferedReader(new FileReader(file));
        int maxId = 0;
        String line;
        boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {
            if (isFirstLine) { isFirstLine = false; continue; }
            String[] parts = line.split(",");
            if (parts.length > 0) {
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    if (id > maxId) {
                        maxId = id;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        br.close();
        return maxId + 1;
    }

    public List<String> getSymptomsForDisease(String disease) {
        Set<String> set = diseaseToSymptoms.getOrDefault(disease, new HashSet<>());
        return new ArrayList<>(set);
    }

    public static class UnrecognizedSymptomException extends Exception {
        public UnrecognizedSymptomException(String message) {
            super(message);
        }
    }

    public static class UnrecognizedDiseaseException extends Exception {
        public UnrecognizedDiseaseException(String message) {
            super(message);
        }
    }

    public static class DiagnosisResult {
        private final String disease;
        private final double probability;
        public DiagnosisResult(String disease, double probability) {
            this.disease = disease;
            this.probability = probability;
        }
        public String getDisease() {
            return disease;
        }
        public double getProbability() {
            return probability * 100; // as percentage
        }
    }
}
