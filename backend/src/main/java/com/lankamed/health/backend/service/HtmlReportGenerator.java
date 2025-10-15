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
        // Simple HTML with KPIs and JS chart data as JSON string
        try {
            String kpiHtml = "";
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                kpiHtml += String.format("<div><b>%s:</b> %s</div>", entry.getKey(), entry.getValue());
            }
            String chartPayload = objectMapper.writeValueAsString(data); // Example: send all data for chart
            String html = "<html><head><title>Report</title></head><body>" +
                          "<h2>" + meta.getOrDefault("title", "Report") + "</h2>" +
                          kpiHtml +
                          "<pre id='chartData' style='display:none;'>" + chartPayload + "</pre>" +
                          "<div id='chart-placeholder'></div>" +
                          "<script>// Chart.js or your preferred lib: parse chartPayload for rendering</script>" +
                          "</body></html>";
            return html;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HTML report", e);
        }
    }
}