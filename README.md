# Medical Assistant - Java Project

**Medical Assistant** is a Java application that simulates a simple medical diagnosis assistant. It uses symptom and disease databases to identify possible diagnoses based on user input and exports patient records to a CSV file.

## Features

- Readable and extensible symptom and disease databases via `symptom.txt` and `disease.txt`.
- Exception handling for unrecognized symptoms or diseases.
- Exports patient records into `medicalDatabase.csv` in the following format:
- patient id | patient name | symptoms list | possible disease
- Validates filename and filetype for CSV output.

## File Structure

```plaintext
MedicalAssistant/
├── src/
│   ├── MedicalAssistant.java
│   ├── MedicalDatabase.java
│   ├── UnrecognizedSymptomException.java
│   ├── UnrecognizedDiseaseException.java
├── data/
│   ├── symptom.txt
│   ├── disease.txt
├── output/
│   ├── medicalDatabase.csv
├── README.md
- Validates filename and filetype for CSV output.

##Requirements:

Java 8 or later

symptom.txt and disease.txt files should be present in the data/ directory.

Each disease entry in disease.txt must list required symptoms.

Usage
1.compile java files:
javac -d out src/*.java

2.run the program:
java -cp out MedicalAssistant

3.The program will:

Allow entering symptoms for a patient.

Match them against known diseases.

Export the result into a CSV file.

Sample methods:
void addSymptom(String symptom) throws UnrecognizedSymptomException
String[] diagnosis() throws UnrecognizedDiseaseException

Exception Handling:
UnrecognizedSymptomException - thrown if a symptom is not listed in symptom.txt.

UnrecognizedDiseaseException - thrown if diagnosis does not match any disease from disease.txt.
