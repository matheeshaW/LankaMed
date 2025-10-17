import java.util.HashMap;
import java.util.Map;
import com.lankamed.health.backend.service.HtmlReportGenerator;

public class DebugTest {
    public static void main(String[] args) {
        // Test data similar to what PatientVisitDataProvider would return
        Map<String, Object> data = new HashMap<>();
        data.put("totalVisits", 39L);
        data.put("uniquePatients", 10L);
        data.put("reportPeriod", "2023-01-01 - 2025-10-01");
        
        Map<String, Object> meta = new HashMap<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("from", "2023-01-01");
        criteria.put("to", "2025-10-01");
        criteria.put("hospitalId", "C001");
        criteria.put("serviceCategory", "OPD");
        criteria.put("patientCategory", "OUTPATIENT");
        criteria.put("gender", "All");
        criteria.put("minAge", null);
        criteria.put("maxAge", null);
        meta.put("criteria", criteria);
        meta.put("title", "PATIENT_VISIT Report");
        
        HtmlReportGenerator generator = new HtmlReportGenerator();
        String html = generator.generate(data, meta);
        
        System.out.println("=== Generated HTML ===");
        System.out.println(html);
        
        // Check if KPI values are in the HTML
        System.out.println("\n=== Checking for KPI values ===");
        System.out.println("Contains '39': " + html.contains("39"));
        System.out.println("Contains '10': " + html.contains("10"));
        System.out.println("Contains 'Total Visits': " + html.contains("Total Visits"));
        System.out.println("Contains 'Unique Patients': " + html.contains("Unique Patients"));
    }
}
