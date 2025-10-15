package com.lankamed.health.backend.service.interfaces;

/**
 * Interface for exporting HTML (or other markup) to PDF bytes.
 * Allows pluggable PDF exporters and testing via mocks.
 */
public interface IPdfExporter {
    /**
     * Exports an HTML string to a PDF byte array.
     * @param html report HTML
     * @return PDF as byte[]
     */
    byte[] export(String html);
}