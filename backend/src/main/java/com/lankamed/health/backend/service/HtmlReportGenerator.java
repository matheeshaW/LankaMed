package com.lankamed.health.backend.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankamed.health.backend.service.interfaces.IReportGenerator;

@Service
public class HtmlReportGenerator implements IReportGenerator {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String generate(Map<String, Object> data, Map<String, Object> meta) {
        try {
            // Extract criteria for display
            @SuppressWarnings("unchecked")
            Map<String, Object> metaCriteria = meta.get("criteria") instanceof Map 
                ? (Map<String, Object>) meta.get("criteria") 
                : Map.of();
            
            String reportTitle = (String) meta.getOrDefault("title", "Statistical Report");
            String chartPayload = objectMapper.writeValueAsString(data);
            
            // Build professional HTML report
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ")
                .append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n")
                .append("<head>\n")
                .append("  <meta charset=\"UTF-8\"/>\n")
                .append("  <title>").append(reportTitle).append("</title>\n")
                .append("  <style>\n")
                .append("    body { font-family: Arial, Helvetica, sans-serif; margin: 30px; color: #333; }\n")
                .append("    .header { text-align: center; border-bottom: 3px solid #4F46E5; padding-bottom: 20px; margin-bottom: 30px; }\n")
                .append("    .header h1 { color: #4F46E5; font-size: 28px; margin: 10px 0; }\n")
                .append("    .header p { color: #666; font-size: 14px; margin: 5px 0; }\n")
                .append("    .section { margin-bottom: 25px; page-break-inside: avoid; }\n")
                .append("    .section h2 { color: #1F2937; font-size: 20px; border-bottom: 2px solid #E5E7EB; padding-bottom: 8px; margin-bottom: 15px; }\n")
                .append("    .criteria-grid { width: 100%; border-collapse: collapse; border: 1px solid #E5E7EB; margin-bottom: 20px; }\n")
                .append("    .criteria-grid td { padding: 10px; border: 1px solid #E5E7EB; }\n")
                .append("    .criteria-label { background-color: #F9FAFB; font-weight: bold; width: 30%; }\n")
                .append("    .criteria-value { background-color: white; }\n")
                .append("    .kpi-table { width: 100%; border-collapse: collapse; }\n")
                .append("    .kpi-table td { width: 48%; padding: 20px; margin: 5px; background-color: #667EEA; color: white; border: 2px solid #4F46E5; text-align: center; vertical-align: top; }\n")
                .append("    .kpi-label { font-size: 16px; font-weight: bold; margin-bottom: 10px; display: block; }\n")
                .append("    .kpi-value { font-size: 36px; font-weight: bold; display: block; margin-top: 10px; }\n")
                .append("    .footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #E5E7EB; text-align: center; color: #6B7280; font-size: 12px; }\n")
                .append("    pre#chartData { display: none; }\n")
                .append("  </style>\n")
                .append("</head>\n")
                .append("<body>\n");
            
            // Header
            html.append("  <div class=\"header\">\n")
                .append("    <h1>").append(reportTitle).append("</h1>\n")
                .append("    <p>Generated on: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"))).append("</p>\n")
                .append("  </div>\n");
            
            // Report Criteria Section
            html.append("  <div class=\"section\">\n")
                .append("    <h2>Report Criteria</h2>\n")
                .append("    <table class=\"criteria-grid\">\n");
            
            appendCriteriaRowTable(html, "Date Range", 
                metaCriteria.getOrDefault("from", "N/A") + " to " + metaCriteria.getOrDefault("to", "N/A"));
            appendCriteriaRowTable(html, "Hospital ID", metaCriteria.getOrDefault("hospitalId", "All"));
            appendCriteriaRowTable(html, "Service Category", metaCriteria.getOrDefault("serviceCategory", "All"));
            appendCriteriaRowTable(html, "Patient Category", metaCriteria.getOrDefault("patientCategory", "All"));
            appendCriteriaRowTable(html, "Gender", metaCriteria.getOrDefault("gender", "All"));
            
            Object minAge = metaCriteria.get("minAge");
            Object maxAge = metaCriteria.get("maxAge");
            String ageRange = (minAge != null || maxAge != null) 
                ? (minAge != null ? minAge : "0") + " - " + (maxAge != null ? maxAge : "∞")
                : "All";
            appendCriteriaRowTable(html, "Age Range", ageRange);
            
            html.append("    </table>\n")
                .append("  </div>\n");
            
            // KPI Section
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
                        // Add empty cell if odd number of KPIs
                        html.append("        <td></td>\n");
                    }
                    html.append("      </tr>\n");
                }
            }
            
            html.append("    </table>\n")
                .append("  </div>\n");
            
            // Hidden chart data for frontend rendering
            html.append("  <pre id=\"chartData\">").append(chartPayload).append("</pre>\n");
            
            // Footer
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
     * Helper method to append a criteria row to HTML table
     */
    private void appendCriteriaRowTable(StringBuilder html, String label, Object value) {
        html.append("      <tr class=\"criteria-row\">\n")
            .append("        <td class=\"criteria-label\">").append(label).append(":</td>\n")
            .append("        <td class=\"criteria-value\">").append(value != null ? value : "N/A").append("</td>\n")
            .append("      </tr>\n");
    }
    
    /**
     * Formats camelCase or snake_case labels to Title Case
     */
    private String formatLabel(String key) {
        // Convert camelCase to spaces
        String formatted = key.replaceAll("([A-Z])", " $1").trim();
        // Convert snake_case to spaces
        formatted = formatted.replace("_", " ");
        // Capitalize first letter of each word
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