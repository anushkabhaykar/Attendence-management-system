package service;

import dao.FaceDataDAO;
import dao.StudentDAO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import model.FaceData;
import model.Student;
import util.FaceRecognitionEngine;

/**
 * High-performance Biometric Face Recognition Service.
 * Manages enrolled biometric templates, multi-sample feature matching,
 * frame-by-frame identification, and automated enrollment aggregation.
 * Location: src/service/FaceRecognitionService.java
 */
public class FaceRecognitionService {

    private static FaceRecognitionService instance;
    private final FaceDataDAO faceDataDAO;
    private final StudentDAO studentDAO;

    // In-memory cache of enrolled student templates for ultra-fast, multi-sample matching
    public static class EnrolledFaceTemplate {
        public int studentId;
        public String rollNo;
        public String studentName;
        public double[] featureVector;
        public List<double[]> sampleVectors = new ArrayList<>();

        EnrolledFaceTemplate(int studentId, String rollNo, String studentName, double[] featureVector) {
            this.studentId = studentId;
            this.rollNo = rollNo;
            this.studentName = studentName;
            this.featureVector = featureVector;
        }
    }

    private final Map<Integer, EnrolledFaceTemplate> enrolledCache = new ConcurrentHashMap<>();

    private FaceRecognitionService() {
        this.faceDataDAO = new FaceDataDAO();
        this.studentDAO = new StudentDAO();
        reloadEnrolledFaces();
    }

    public static synchronized FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }

    /**
     * Refreshes the in-memory cache of enrolled face models from the database and sample disk cache.
     */
    public synchronized void reloadEnrolledFaces() {
        enrolledCache.clear();
        try {
            List<FaceData> allFaces = faceDataDAO.getAllFaceData();
            for (FaceData fd : allFaces) {
                Student s = studentDAO.findById(fd.getStudentId());
                if (s != null) {
                    double[] features = fd.getFeatureVector();
                    if (features.length > 0) {
                        EnrolledFaceTemplate template =
                                new EnrolledFaceTemplate(s.getId(), s.getRollNo(), s.getName(), features);

                        // Load individual multi-sample angle vectors from disk if available
                        File dir = new File("data/faces/" + s.getRollNo());
                        if (dir.exists() && dir.isDirectory()) {
                            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
                            if (files != null) {
                                for (File f : files) {
                                    try {
                                        BufferedImage img = ImageIO.read(f);
                                        if (img != null) {
                                            double[] sVec = FaceRecognitionEngine.extractLBPHFeatures(img);
                                            template.sampleVectors.add(sVec);
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
                        }

                        enrolledCache.put(s.getId(), template);
                    }
                }
            }
            System.out.println("[FaceRecognitionService] Loaded " + enrolledCache.size() + " enrolled faces into memory cache.");
        } catch (SQLException e) {
            System.err.println("[FaceRecognitionService] Error loading enrolled faces: " + e.getMessage());
        }
    }

    /**
     * Identifies a face in the provided camera frame using multi-sample LBPH nearest-neighbor matching.
     * Returns a RecognitionResult containing match details, confidence, and bounding box.
     */
    public FaceRecognitionEngine.RecognitionResult recognizeFace(BufferedImage frame) {
        if (frame == null) {
            return new FaceRecognitionEngine.RecognitionResult(false, null);
        }

        // 1. Detect face region
        Rectangle faceBox = FaceRecognitionEngine.detectFace(frame);
        if (faceBox == null) {
            return new FaceRecognitionEngine.RecognitionResult(false, null);
        }

        FaceRecognitionEngine.RecognitionResult result =
                new FaceRecognitionEngine.RecognitionResult(true, faceBox);

        // 2. Normalize and crop face
        BufferedImage normalized = FaceRecognitionEngine.normalizeFace(frame, faceBox);
        if (normalized == null) {
            result.statusMessage = "Face Alignment Incomplete";
            return result;
        }

        // 3. Extract LBPH features
        double[] liveFeatures = FaceRecognitionEngine.extractLBPHFeatures(normalized);

        // 4. Compare with enrolled faces in memory (Nearest-neighbor across average + sample angles)
        double lowestDistance = Double.MAX_VALUE;
        EnrolledFaceTemplate bestMatch = null;

        for (EnrolledFaceTemplate template : enrolledCache.values()) {
            double bestStudentDist = FaceRecognitionEngine.computeChiSquareDistance(liveFeatures, template.featureVector);

            for (double[] sVec : template.sampleVectors) {
                double sDist = FaceRecognitionEngine.computeChiSquareDistance(liveFeatures, sVec);
                if (sDist < bestStudentDist) {
                    bestStudentDist = sDist;
                }
            }

            if (bestStudentDist < lowestDistance) {
                lowestDistance = bestStudentDist;
                bestMatch = template;
            }
        }

        double confidence = FaceRecognitionEngine.calculateConfidence(lowestDistance);
        result.confidence = confidence;

        // If distance is within recognition threshold and confidence meets minimum (>= 58.0%)
        if (bestMatch != null && confidence >= 58.0 && lowestDistance <= FaceRecognitionEngine.RECOGNITION_THRESHOLD) {
            result.isRecognized = true;
            result.studentId = bestMatch.studentId;
            result.rollNo = bestMatch.rollNo;
            result.studentName = bestMatch.studentName;
            result.statusMessage = "Student Identified";
        } else {
            result.isRecognized = false;
            result.statusMessage = "Unknown Face";
        }

        return result;
    }

    /**
     * Enrolls face data for a student by aggregating multiple captured samples.
     * Stores sample images to data/faces/{roll_no}/ and saves the LBPH vector in the database.
     */
    public synchronized boolean enrollStudentFace(int studentId, String rollNo, List<BufferedImage> samples) throws Exception {
        if (samples == null || samples.isEmpty()) {
            throw new IllegalArgumentException("No face samples provided for enrollment.");
        }

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }

        // Create directory for student's face samples
        File dir = new File("data/faces/" + rollNo);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        double[] aggregateFeatures = new double[FaceRecognitionEngine.FEATURE_LENGTH];
        int validSamples = 0;

        for (int i = 0; i < samples.size(); i++) {
            BufferedImage sample = samples.get(i);
            Rectangle box = FaceRecognitionEngine.detectFace(sample);
            if (box == null) {
                box = new Rectangle(sample.getWidth() / 4, sample.getHeight() / 4, sample.getWidth() / 2, sample.getHeight() / 2);
            }

            BufferedImage norm = FaceRecognitionEngine.normalizeFace(sample, box);
            if (norm != null) {
                // Save normalized sample image to disk
                File sampleFile = new File(dir, "sample_" + (validSamples + 1) + ".png");
                ImageIO.write(norm, "PNG", sampleFile);

                double[] features = FaceRecognitionEngine.extractLBPHFeatures(norm);
                for (int f = 0; f < features.length; f++) {
                    aggregateFeatures[f] += features[f];
                }
                validSamples++;
            }
        }

        if (validSamples == 0) {
            throw new Exception("Could not extract facial features from provided samples.");
        }

        // Average feature values across all valid samples
        for (int f = 0; f < aggregateFeatures.length; f++) {
            aggregateFeatures[f] /= validSamples;
        }

        // Serialize and save to database
        String encodedStr = FaceData.serializeFeatures(aggregateFeatures);
        FaceData faceData = new FaceData(studentId, encodedStr, validSamples);
        boolean saved = faceDataDAO.saveOrUpdate(faceData);

        if (saved) {
            // Update student's face reference path in students table
            studentDAO.updateFaceReference(studentId, "data/faces/" + rollNo);
            // Refresh in-memory cache
            reloadEnrolledFaces();
            return true;
        }

        return false;
    }

    public List<EnrolledFaceTemplate> getEnrolledTemplates() {
        return new ArrayList<>(enrolledCache.values());
    }

    public int getEnrolledCount() {
        return enrolledCache.size();
    }
}
