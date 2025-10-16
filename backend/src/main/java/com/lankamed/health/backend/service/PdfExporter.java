package com.lankamed.health.backend.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lankamed.health.backend.service.interfaces.IPdfExporter;

/**
 * PDF Exporter using Flying Saucer (xhtmlrenderer) library.
 * Converts HTML reports to PDF format for download.
 * 
 * <p><b>Features:</b></p>
 * <ul>
 *   <li>HTML/CSS to PDF conversion</li>
 *   <li>Preserves styling and layout</li>
 *   <li>Supports embedded fonts and images</li>
 *   <li>Open-source (LGPL license)</li>
 * </ul>
 * 
 * <p><b>Dependencies:</b></p>
 * <ul>
 *   <li>org.xhtmlrenderer:flying-saucer-pdf:9.1.22</li>
 * </ul>
 */
@Service
public class PdfExporter implements IPdfExporter {
    
    /**
     * Converts HTML string to PDF byte array.
     * 
     * @param html The HTML content to convert (must be well-formed XHTML)
     * @return PDF file as byte array
     * @throws RuntimeException if PDF generation fails
     */
    @Override
    public byte[] export(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            System.out.println("=== PdfExporter: Starting PDF export ===");
            System.out.println("Input HTML length: " + html.length());
            
            // Initialize Flying Saucer renderer
            System.out.println("Initializing ITextRenderer...");
            ITextRenderer renderer = new ITextRenderer();
            
            // Wrap HTML in proper XHTML structure if needed
            System.out.println("Ensuring XHTML structure...");
            String xhtml = ensureXhtmlStructure(html);
            System.out.println("XHTML length: " + xhtml.length());
            
            // Set the HTML document
            System.out.println("Setting document from string...");
            renderer.setDocumentFromString(xhtml);
            
            // Layout the document
            System.out.println("Laying out document...");
            renderer.layout();
            
            // Create PDF
            System.out.println("Creating PDF...");
            renderer.createPDF(outputStream);
            
            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("=== PdfExporter: SUCCESS ===");
            System.out.println("PDF size: " + pdfBytes.length + " bytes");
            
            return pdfBytes;
            
        } catch (Exception e) {
            System.err.println("=== PdfExporter: FAILED ===");
            System.err.println("Error: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ensures HTML has proper XHTML structure required by Flying Saucer.
     * Adds DOCTYPE, html, head, and body tags if missing.
     * 
     * @param html The HTML content
     * @return Well-formed XHTML string
     */
    private String ensureXhtmlStructure(String html) {
        // If already has DOCTYPE and html tags, return as-is
        if (html.trim().toLowerCase().startsWith("<!doctype") || 
            html.trim().toLowerCase().startsWith("<html")) {
            return html;
        }
        
        // Wrap in proper XHTML structure
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " +
               "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
               "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
               "<head>\n" +
               "  <meta charset=\"UTF-8\"/>\n" +
               "  <title>Report</title>\n" +
               "  <style>\n" +
               "    body { font-family: Arial, sans-serif; margin: 20px; }\n" +
               "    h1, h2 { color: #333; }\n" +
               "    table { border-collapse: collapse; width: 100%; margin: 10px 0; }\n" +
               "    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n" +
               "    th { background-color: #f2f2f2; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               html +
               "\n</body>\n" +
               "</html>";
    }
}