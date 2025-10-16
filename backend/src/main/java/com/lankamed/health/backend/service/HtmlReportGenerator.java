package com.lankamed.health.backend.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.lankamed.health.backend.service.interfaces.IReportGenerator;

@Service
public class HtmlReportGenerator implements IReportGenerator {
    @Override
    public String generate(Map<String, Object> data, Map<String, Object> meta) {
        try {
            // Extract criteria for display
            @SuppressWarnings("unchecked")
            Map<String, Object> metaCriteria = meta.get("criteria") instanceof Map 
                ? (Map<String, Object>) meta.get("criteria") 
                : Map.of();
            
            String reportTitle = (String) meta.getOrDefault("title", "Statistical Report");
            
            // Build professional HTML report with XHTML structure for PDF compatibility
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ")
                .append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n")
                .append("<head>\n")
                .append("  <meta charset=\"UTF-8\"/>\n")
                .append("  <title>").append(reportTitle).append("</title>\n")
                .append("  <style>\n")
                .append("    @page { size: A4; margin: 15mm; }\n")
                .append("    body { font-family: Arial, Helvetica, sans-serif; margin: 0; padding: 15px; color: #333; font-size: 11px; }\n")
                .append("    .header { text-align: center; border-bottom: 3px solid #4F46E5; padding-bottom: 12px; margin-bottom: 15px; }\n")
                .append("    .header h1 { color: #4F46E5; font-size: 22px; margin: 5px 0; }\n")
                .append("    .header p { color: #666; font-size: 11px; margin: 3px 0; }\n")
                .append("    .section { margin-bottom: 15px; page-break-inside: avoid; }\n")
                .append("    .section h2 { color: #1F2937; font-size: 16px; border-bottom: 2px solid #E5E7EB; padding-bottom: 5px; margin: 8px 0 10px 0; }\n")
                .append("    .criteria-grid { width: 100%; border-collapse: collapse; border: 2px solid #4F46E5; margin-bottom: 12px; }\n")
                .append("    .criteria-grid td { padding: 6px 10px; border: 1px solid #E5E7EB; font-size: 11px; }\n")
                .append("    .criteria-label { background-color: #F3F4F6; font-weight: bold; width: 35%; color: #374151; }\n")
                .append("    .criteria-value { background-color: #FFFFFF; color: #1F2937; }\n")
                .append("    .kpi-table { width: 100%; border-collapse: separate; border-spacing: 8px 6px; margin: 8px 0; }\n")
                .append("    .kpi-table td { width: 48%; padding: 20px 15px; background-color: #667EEA; color: #FFFFFF; border: 3px solid #4F46E5; text-align: center; vertical-align: middle; }\n")
                .append("    .kpi-label { font-size: 11px; font-weight: bold; margin-bottom: 8px; display: block; text-transform: uppercase; letter-spacing: 1px; }\n")
                .append("    .kpi-value { font-size: 32px; font-weight: bold; display: block; margin-top: 5px; line-height: 1; }\n")
                .append("    .footer { margin-top: 15px; padding-top: 10px; border-top: 1px solid #E5E7EB; text-align: center; color: #6B7280; font-size: 10px; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n");
            
            // Header section with report title and generation timestamp
            html.append("  <div class=\"header\">\n")
                .append("    <h1>").append(reportTitle).append("</h1>\n")
                .append("    <p>Generated on: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))).append("</p>\n")
                .append("  </div>\n");
            
            // Report criteria section showing filter parameters
            html.append("  <div class=\"section\">\n")
                .append("    <h2>Report Criteria</h2>\n")
                .append("    <table class=\"criteria-grid\">\n");
            
            String fromDate = metaCriteria.get("from") != null ? String.valueOf(metaCriteria.get("from")) : "N/A";
            String toDate = metaCriteria.get("to") != null ? String.valueOf(metaCriteria.get("to")) : "N/A";
            appendCriteriaRowTable(html, "Date Range", fromDate + " to " + toDate);
            
            appendCriteriaRowTable(html, "Hospital ID", 
                metaCriteria.get("hospitalId") != null ? String.valueOf(metaCriteria.get("hospitalId")) : "All");
            appendCriteriaRowTable(html, "Service Category", 
                metaCriteria.get("serviceCategory") != null ? String.valueOf(metaCriteria.get("serviceCategory")) : "All");
            appendCriteriaRowTable(html, "Patient Category", 
                metaCriteria.get("patientCategory") != null ? String.valueOf(metaCriteria.get("patientCategory")) : "All");
            appendCriteriaRowTable(html, "Gender", 
                metaCriteria.get("gender") != null ? String.valueOf(metaCriteria.get("gender")) : "All");
            
            Object minAge = metaCriteria.get("minAge");
            Object maxAge = metaCriteria.get("maxAge");
            String ageRange = (minAge != null || maxAge != null) 
                ? (minAge != null ? String.valueOf(minAge) : "0") + " - " + (maxAge != null ? String.valueOf(maxAge) : "∞")
                : "All";
            appendCriteriaRowTable(html, "Age Range", ageRange);
            
            html.append("    </table>\n")
                .append("  </div>\n");
            
            // KPI section displaying statistical data in 2-column grid
            html.append("  <div class=\"section\">\n")
                .append("    <h2>Key Performance Indicators</h2>\n")
                .append("    <table class=\"kpi-table\">\n");
            
            int count = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (count % 2 == 0) {
                    html.append("      <tr>\n");
                }
                
                String label = formatLabel(entry.getKey());
                String value = String.valueOf(entry.getValue());
                html.append("        <td>\n")
                    .append("          <span class=\"kpi-label\">").append(label).append("</span>\n")
                    .append("          <span class=\"kpi-value\">").append(value).append("</span>\n")
                    .append("        </td>\n");
                
                count++;
                if (count % 2 == 0 || count == data.size()) {
                    // Close row if we have 2 KPIs or it's the last one
                    if (count % 2 == 1 && count == data.size()) {
                        // Add empty cell if odd number of KPIs for proper table structure
                        html.append("        <td></td>\n");
                    }
                    html.append("      </tr>\n");
                }
            }
            
            html.append("    </table>\n")
                .append("  </div>\n");
            
            // Footer with system branding
            html.append("  <div class=\"footer\">\n")
                .append("    <p>LankaMed Healthcare System | Confidential Report</p>\n")
                .append("  </div>\n");
            
            html.append("</body>\n</html>");
            
            return html.toString();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HTML report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Appends a criteria row to the HTML table with proper escaping
     */
    private void appendCriteriaRowTable(StringBuilder html, String label, Object value) {
        String safeLabel = escapeHtml(label);
        String safeValue = escapeHtml(value != null ? String.valueOf(value) : "N/A");
        
        html.append("      <tr class=\"criteria-row\">\n")
            .append("        <td class=\"criteria-label\">").append(safeLabel).append(":</td>\n")
            .append("        <td class=\"criteria-value\">").append(safeValue).append("</td>\n")
            .append("      </tr>\n");
    }
    
    /**
     * Escapes HTML special characters to prevent XSS and ensure XHTML compliance
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Converts camelCase/snake_case keys to human-readable Title Case labels
     */
    private String formatLabel(String key) {
        String formatted = key.replaceAll("([A-Z])", " $1").trim();
        formatted = formatted.replace("_", " ");
        String[] words = formatted.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
}