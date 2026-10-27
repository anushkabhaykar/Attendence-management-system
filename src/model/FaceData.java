package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing face biometric encoding templates for a student.
 * Location: src/model/FaceData.java
 */
public class FaceData {
    private int id;
    private int studentId;
    private String encoding; // Comma-separated or serialized numeric feature vector
    private int sampleCount;
    private Timestamp createdAt;

    public FaceData() {
    }

    public FaceData(int id, int studentId, String encoding, int sampleCount, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.encoding = encoding;
        this.sampleCount = sampleCount;
        this.createdAt = createdAt;
    }

    public FaceData(int studentId, String encoding, int sampleCount) {
        this.studentId = studentId;
        this.encoding = encoding;
        this.sampleCount = sampleCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Parses the stored encoding string into a double array of biometric features.
     */
    public double[] getFeatureVector() {
        if (encoding == null || encoding.trim().isEmpty()) {
            return new double[0];
        }
        String[] parts = encoding.split(",");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                vector[i] = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                vector[i] = 0.0;
            }
        }
        return vector;
    }

    /**
     * Serializes a double array of features into the storage encoding string.
     */
    public static String serializeFeatures(double[] vector) {
        if (vector == null || vector.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(String.format(java.util.Locale.US, "%.5f", vector[i]));
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
