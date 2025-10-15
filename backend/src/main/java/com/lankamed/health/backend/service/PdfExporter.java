package com.lankamed.health.backend.service;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.service.interfaces.IPdfExporter;

/**
 * PDF Exporter using OpenHTMLToPDF. For testing, we can mock this logic or swap the exporter.
 */
@Service
public class PdfExporter implements IPdfExporter {
    @Override
    public byte[] export(String html) {
        // TODO: Use OpenHTMLToPDF or similar for production
        // For testing, return placeholder PDF bytes
        return html.getBytes(); // Not a real PDF, just demo. Replace with PDF logic.
    }
}