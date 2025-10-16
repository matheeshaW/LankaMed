package com.lankamed.health.backend.service;

import com.lankamed.health.backend.service.interfaces.IPdfExporter;

/**
 * Temporary PDF exporter for testing purposes.
 * Returns HTML as bytes instead of actual PDF conversion.
 * 
 * Usage: Uncomment @Service and @Primary annotations to use this instead of PdfExporter
 */
// @Service
// @Primary
public class SimplePdfExporter implements IPdfExporter {
    
    @Override
    public byte[] export(String html) {
        try {
            return html.getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("PDF export failed: " + e.getMessage(), e);
        }
    }
}
