package com.lankamed.health.backend.service.interfaces;

import java.util.Map;

/**
 * Interface for transforming report data and metadata into a final HTML report output.
 * Adheres to SOLID by separating report data logic from presentation generation logic.
 */
public interface IReportGenerator {
    /**
     * Generates an HTML (or other format) report from structured data.
     * @param data the raw report data and KPIs
     * @param meta meta-data (e.g., title, report period, user)
     * @return the generated HTML report as a String
     */
    String generate(Map<String, Object> data, Map<String, Object> meta);
}