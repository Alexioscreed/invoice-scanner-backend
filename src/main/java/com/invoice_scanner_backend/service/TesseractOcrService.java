package com.invoice_scanner_backend.service;

import com.invoice_scanner_backend.config.TesseractConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TesseractOcrService {

    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrService.class);

    @Autowired
    private TesseractConfig tesseractConfig;

    public String extractTextFromImage(String imagePath) {
        try {
            // Verify Tesseract executable exists
            Path tesseractPath = Paths.get(tesseractConfig.getTesseractPath());
            if (!Files.exists(tesseractPath)) {
                throw new RuntimeException("Tesseract executable not found at: " + tesseractConfig.getTesseractPath());
            }

            // Create temporary output file
            File tempOutputFile = File.createTempFile("tesseract_output", ".txt");
            String outputPath = tempOutputFile.getAbsolutePath().replace(".txt", "");

            // Build Tesseract command
            List<String> command = buildTesseractCommand(imagePath, outputPath);

            logger.info("Executing Tesseract command: {}", String.join(" ", command));

            // Execute Tesseract
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.environment().put("TESSDATA_PREFIX", tesseractConfig.getTesseractDataPath());
            
            Process process = processBuilder.start();
            
            // Wait for process to complete with timeout
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Tesseract process timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = readProcessError(process);
                throw new RuntimeException("Tesseract failed with exit code " + exitCode + ": " + errorOutput);
            }

            // Read the output file
            File outputFile = new File(outputPath + ".txt");
            if (!outputFile.exists()) {
                throw new RuntimeException("Tesseract output file not found");
            }

            String extractedText = Files.readString(outputFile.toPath());
            
            // Clean up temporary files
            tempOutputFile.delete();
            outputFile.delete();

            logger.info("Successfully extracted {} characters of text", extractedText.length());
            return extractedText.trim();

        } catch (Exception e) {
            logger.error("Error extracting text from image: {}", e.getMessage(), e);
            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    public String extractTextFromImageWithPreprocessing(String imagePath) {
        try {
            // For now, use the basic extraction
            // TODO: Add image preprocessing (deskew, denoise, contrast enhancement)
            return extractTextFromImage(imagePath);
        } catch (Exception e) {
            logger.error("Error in OCR with preprocessing: {}", e.getMessage(), e);
            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        }
    }

    private List<String> buildTesseractCommand(String imagePath, String outputPath) {
        List<String> command = new ArrayList<>();
        command.add(tesseractConfig.getTesseractPath());
        command.add(imagePath);
        command.add(outputPath);
        
        // Add language parameter
        command.add("-l");
        command.add(tesseractConfig.getTesseractLanguage());
        
        // Add PSM (Page Segmentation Mode)
        command.add("--psm");
        command.add(String.valueOf(tesseractConfig.getTesseractPsm()));
        
        // Add DPI if needed
        command.add("--dpi");
        command.add(String.valueOf(tesseractConfig.getTesseractDpi()));
        
        // Output configuration for better text extraction
        command.add("-c");
        command.add("preserve_interword_spaces=1");

        return command;
    }

    private String readProcessError(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }
            return errorOutput.toString();
        } catch (IOException e) {
            return "Could not read error output";
        }
    }

    public boolean isTesseractAvailable() {
        try {
            Path tesseractPath = Paths.get(tesseractConfig.getTesseractPath());
            return Files.exists(tesseractPath) && Files.isExecutable(tesseractPath);
        } catch (Exception e) {
            return false;
        }
    }
}
