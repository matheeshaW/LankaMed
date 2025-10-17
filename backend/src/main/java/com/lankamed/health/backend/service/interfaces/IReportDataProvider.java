package com.lankamed.health.backend.service.interfaces;

import java.util.Map;

/**
 * Provides report-specific data for the report generation process.
 * Implementations should encapsulate all query and business logic for fetching report data,
 * adhering to the Single Responsibility principle (SOLID).
 *
 * Example: PatientVisitDataProvider, ServiceUtilizationDataProvider
 */

 //open/closed principle
 //Interface Segregation Principle
public interface IReportDataProvider {
    /**
     * Fetches report data based on the given criteria map.
     * @param criteria filter and parameter values for the report (dates, IDs, etc)
     * @return a map representing raw data and KPIs to be passed to the report generator
     */
    Map<String, Object> fetchData(Map<String, Object> criteria);
}