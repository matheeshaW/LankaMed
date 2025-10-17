package com.lankamed.health.backend.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PdfExporterTest {

    @InjectMocks
    private PdfExporter pdfExporter;

    // Positive Test Cases(Valid HTML to PDF conversion)
    @Test
    void export_WithValidHtml_ReturnsPdfBytes() {
        // Given
        String validHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Test Report</title></head>
                <body>
                    <h1>Test Report</h1>
                    <p>This is a test report content.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(validHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // PDF files start with %PDF
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithSimpleHtml_WrapsInXhtmlStructure() {
        // Given
        String simpleHtml = "<h1>Simple Report</h1><p>Content</p>";

        // When
        byte[] result = pdfExporter.export(simpleHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithComplexHtml_ReturnsPdfBytes() {
        // Given
        String complexHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Complex Report</title>
                    <style>
                        body { font-family: Arial, sans-serif; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <h1>Complex Report</h1>
                    <table>
                        <tr><th>Metric</th><th>Value</th></tr>
                        <tr><td>Total Visits</td><td>150</td></tr>
                        <tr><td>Unique Patients</td><td>120</td></tr>
                    </table>
                    <p>This is a complex report with styling and tables.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(complexHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithXhtmlDoctype_ReturnsPdfBytes() {
        // Given
        String xhtmlHtml = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head><title>XHTML Report</title></head>
                <body>
                    <h1>XHTML Report</h1>
                    <p>This is an XHTML report.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(xhtmlHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithHtmlDoctype_ReturnsPdfBytes() {
        // Given
        String htmlDoctype = """
                <!DOCTYPE html>
                <html>
                <head><title>HTML5 Report</title></head>
                <body>
                    <h1>HTML5 Report</h1>
                    <p>This is an HTML5 report.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(htmlDoctype);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithHtmlTagOnly_ReturnsPdfBytes() {
        // Given
        String htmlTagOnly = """
                <html>
                <head><title>HTML Tag Report</title></head>
                <body>
                    <h1>HTML Tag Report</h1>
                    <p>This report starts with html tag.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(htmlTagOnly);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithSpecialCharacters_ReturnsPdfBytes() {
        // Given
        String specialCharsHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Special Characters Report</title></head>
                <body>
                    <h1>Special Characters Report</h1>
                    <p>This report contains special characters: &amp; &lt; &gt; &quot; &#39;</p>
                    <p>Unicode characters: α β γ δ ε</p>
                    <p>Currency symbols: $ € £ ¥</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(specialCharsHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithCssStyles_ReturnsPdfBytes() {
        // Given
        String cssHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>CSS Report</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .header { color: #333; font-size: 24px; }
                        .content { color: #666; font-size: 14px; }
                        .highlight { background-color: #ffff00; }
                    </style>
                </head>
                <body>
                    <div class="header">CSS Styled Report</div>
                    <div class="content">This report has CSS styling.</div>
                    <div class="highlight">This text is highlighted.</div>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(cssHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithTables_ReturnsPdfBytes() {
        // Given
        String tableHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Table Report</title></head>
                <body>
                    <h1>Table Report</h1>
                    <table border="1">
                        <tr><th>Name</th><th>Value</th></tr>
                        <tr><td>Total Visits</td><td>150</td></tr>
                        <tr><td>Unique Patients</td><td>120</td></tr>
                        <tr><td>Average Visits</td><td>1.25</td></tr>
                    </table>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(tableHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    // Edge Cases(Empty HTML handling)
    @Test
    void export_WithEmptyHtml_ReturnsPdf() {
        // Given
        String emptyHtml = "";

        // When
        byte[] result = pdfExporter.export(emptyHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithWhitespaceOnly_ReturnsPdf() {
        // Given
        String whitespaceHtml = "   \n\t  ";

        // When
        byte[] result = pdfExporter.export(whitespaceHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithPlainText_ReturnsPdf() {
        // Given
        String plainText = "This is just plain text without any HTML tags.";

        // When
        byte[] result = pdfExporter.export(plainText);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithSingleTag_ReturnsPdf() {
        // Given
        String singleTag = "<p>Single paragraph</p>";

        // When
        byte[] result = pdfExporter.export(singleTag);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithNestedTags_ReturnsPdf() {
        // Given
        String nestedHtml = """
                <div>
                    <h1>Nested Report</h1>
                    <div>
                        <p>This is a nested structure.</p>
                        <ul>
                            <li>Item 1</li>
                            <li>Item 2</li>
                        </ul>
                    </div>
                </div>
                """;

        // When
        byte[] result = pdfExporter.export(nestedHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithLongContent_ReturnsPdf() {
        // Given
        StringBuilder longHtml = new StringBuilder();
        longHtml.append("<!DOCTYPE html><html><head><title>Long Report</title></head><body>");
        longHtml.append("<h1>Long Report</h1>");
        
        // Add many paragraphs to create a long document
        for (int i = 0; i < 100; i++) {
            longHtml.append("<p>This is paragraph number ").append(i).append(" with some content.</p>");
        }
        
        longHtml.append("</body></html>");

        // When
        byte[] result = pdfExporter.export(longHtml.toString());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithUnicodeContent_ReturnsPdf() {
        // Given
        String unicodeHtml = """
                <!DOCTYPE html>
                <html>
                <head><title>Unicode Report</title></head>
                <body>
                    <h1>Unicode Report</h1>
                    <p>English: Hello World</p>
                    <p>Chinese: 你好世界</p>
                    <p>Arabic: مرحبا بالعالم</p>
                    <p>Russian: Привет мир</p>
                    <p>Japanese: こんにちは世界</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(unicodeHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    // Error Cases(Null HTML handling)
    @Test
    void export_WithNullHtml_ThrowsException() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            pdfExporter.export(null);
        });
    }

    @Test
    void export_WithMalformedHtml_ThrowsException() {
        // Given
        String malformedHtml = "<html><head><title>Test</title></head><body><h1>Unclosed tag</h2></body>";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            pdfExporter.export(malformedHtml);
        });
    }

    @Test
    void export_WithInvalidDoctype_HandlesGracefully() {
        // Given
        String invalidDoctype = "<!DOCTYPE invalid><html><body>Invalid DOCTYPE</body></html>";

        // When
        byte[] result = pdfExporter.export(invalidDoctype);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithUnclosedTags_ThrowsException() {
        // Given
        String unclosedTags = "<html><head><title>Test</title></head><body><h1>Unclosed tag</body></html>";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            pdfExporter.export(unclosedTags);
        });
    }

    @Test
    void export_WithInvalidCss_HandlesGracefully() {
        // Given
        String invalidCssHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Invalid CSS Report</title>
                    <style>
                        body { invalid-property: invalid-value; }
                        .class { color: invalid-color; }
                    </style>
                </head>
                <body>
                    <h1>Invalid CSS Report</h1>
                    <p>This report has invalid CSS.</p>
                </body>
                </html>
                """;

        // When
        byte[] result = pdfExporter.export(invalidCssHtml);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }

    @Test
    void export_WithVeryLargeHtml_HandlesGracefully() {
        // Given
        StringBuilder veryLargeHtml = new StringBuilder();
        veryLargeHtml.append("<!DOCTYPE html><html><head><title>Very Large Report</title></head><body>");
        veryLargeHtml.append("<h1>Very Large Report</h1>");
        
        // Add a very large amount of content
        for (int i = 0; i < 10000; i++) {
            veryLargeHtml.append("<p>This is paragraph number ").append(i).append(" with some content.</p>");
        }
        
        veryLargeHtml.append("</body></html>");

        // When
        byte[] result = pdfExporter.export(veryLargeHtml.toString());

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result, 0, Math.min(4, result.length)).startsWith("%PDF"));
    }
}

