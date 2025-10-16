# Test appointment creation
$body = @{
    doctorId = 1
    hospitalId = 1
    serviceCategoryId = 1
    appointmentDateTime = "2024-01-20T10:00:00"
    reason = "Test appointment"
} | ConvertTo-Json

Write-Host "Testing appointment creation..."
Write-Host "Request body: $body"

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/patients/me/appointments" -Method POST -Headers @{"Content-Type"="application/json"} -Body $body -UseBasicParsing
    Write-Host "SUCCESS!"
    Write-Host "Status Code: $($response.StatusCode)"
    Write-Host "Response Content: $($response.Content)"
} catch {
    Write-Host "ERROR!"
    Write-Host "Error Message: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response Body: $responseBody"
    }
}


