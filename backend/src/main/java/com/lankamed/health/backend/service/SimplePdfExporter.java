package com.lankamed.health.backend.service;

import com.lankamed.health.backend.service.interfaces.IPdfExporter;

/**
 * Temporary simple PDF exporter for testing.
 * This just wraps the HTML and returns it.
 * 
 * To test if the issue is with Flying Saucer or something else,
 * use this temporarily, then switch back to the real PdfExporter.
 */
// @Service
// @Primary  // Uncomment these to use this instead of PdfExporter
public class SimplePdfExporter implements IPdfExporter {
    
    @Override
    public byte[] export(String html) {
        try {
            // For now, just return the HTML as bytes
            // This will download as "PDF" but will actually be HTML
            // If this works, then the issue is with Flying Saucer
            // If this still fails, the issue is elsewhere
            
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


