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
            System.out.println("=== SimplePdfExporter: Exporting HTML ===");
            System.out.println("HTML length: " + html.length());
            
            byte[] bytes = html.getBytes("UTF-8");
            
            System.out.println("=== SimplePdfExporter: Export successful ===");
            System.out.println("Bytes length: " + bytes.length);
            
            return bytes;
            
        } catch (Exception e) {
            System.err.println("=== SimplePdfExporter: Export FAILED ===");
            e.printStackTrace();
            throw new RuntimeException("PDF export failed: " + e.getMessage(), e);
        }
    }
}
