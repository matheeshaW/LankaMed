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
                .append("    .section { margin-bottom: 25px; }\n")
                .append("    .section h2 { color: #1F2937; font-size: 20px; border-bottom: 2px solid #E5E7EB; padding-bottom: 8px; margin-bottom: 15px; }\n")
                .append("    .criteria-grid { display: table; width: 100%; border: 1px solid #E5E7EB; border-radius: 4px; }\n")
                .append("    .criteria-row { display: table-row; }\n")
                .append("    .criteria-label { display: table-cell; padding: 10px; background-color: #F9FAFB; font-weight: bold; border-bottom: 1px solid #E5E7EB; width: 30%; }\n")
                .append("    .criteria-value { display: table-cell; padding: 10px; border-bottom: 1px solid #E5E7EB; }\n")
                .append("    .kpi-container { display: table; width: 100%; }\n")
                .append("    .kpi-box { display: table-cell; padding: 20px; margin: 10px; background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%); color: white; border-radius: 8px; text-align: center; }\n")
                .append("    .kpi-label { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }\n")
                .append("    .kpi-value { font-size: 32px; font-weight: bold; }\n")
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
                .append("    <div class=\"criteria-grid\">\n");
            
            appendCriteriaRow(html, "Date Range", 
                metaCriteria.getOrDefault("from", "N/A") + " to " + metaCriteria.getOrDefault("to", "N/A"));
            appendCriteriaRow(html, "Hospital ID", metaCriteria.getOrDefault("hospitalId", "All"));
            appendCriteriaRow(html, "Service Category", metaCriteria.getOrDefault("serviceCategory", "All"));
            appendCriteriaRow(html, "Patient Category", metaCriteria.getOrDefault("patientCategory", "All"));
            appendCriteriaRow(html, "Gender", metaCriteria.getOrDefault("gender", "All"));
            
            Object minAge = metaCriteria.get("minAge");
            Object maxAge = metaCriteria.get("maxAge");
            String ageRange = (minAge != null || maxAge != null) 
                ? (minAge != null ? minAge : "0") + " - " + (maxAge != null ? maxAge : "∞")
                : "All";
            appendCriteriaRow(html, "Age Range", ageRange);
            
            html.append("    </div>\n")
                .append("  </div>\n");
            
            // KPI Section
            html.append("  <div class=\"section\">\n")
                .append("    <h2>Key Performance Indicators</h2>\n")
                .append("    <div class=\"kpi-container\">\n");
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String label = formatLabel(entry.getKey());
                String value = String.valueOf(entry.getValue());
                html.append("      <div class=\"kpi-box\">\n")
                    .append("        <div class=\"kpi-label\">").append(label).append("</div>\n")
                    .append("        <div class=\"kpi-value\">").append(value).append("</div>\n")
                    .append("      </div>\n");
            }
            
            html.append("    </div>\n")
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
     * Helper method to append a criteria row to HTML
     */
    private void appendCriteriaRow(StringBuilder html, String label, Object value) {
        html.append("      <div class=\"criteria-row\">\n")
            .append("        <div class=\"criteria-label\">").append(label).append(":</div>\n")
            .append("        <div class=\"criteria-value\">").append(value != null ? value : "N/A").append("</div>\n")
            .append("      </div>\n");
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