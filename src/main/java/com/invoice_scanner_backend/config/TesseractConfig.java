package com.invoice_scanner_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TesseractConfig {

    @Value("${tesseract.path:C:\\Program Files\\Tesseract-OCR\\tesseract.exe}")
    private String tesseractPath;

    @Value("${tesseract.data-path:C:\\Program Files\\Tesseract-OCR\\tessdata}")
    private String tesseractDataPath;

    @Value("${tesseract.language:eng}")
    private String tesseractLanguage;

    @Value("${tesseract.dpi:300}")
    private int tesseractDpi;

    @Value("${tesseract.psm:3}")
    private int tesseractPsm;

    public String getTesseractPath() {
        return tesseractPath;
    }

    public String getTesseractDataPath() {
        return tesseractDataPath;
    }

    public String getTesseractLanguage() {
        return tesseractLanguage;
    }

    public int getTesseractDpi() {
        return tesseractDpi;
    }

    public int getTesseractPsm() {
        return tesseractPsm;
    }
}
