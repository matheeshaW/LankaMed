import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  console.log("API Request Debug:");
  console.log("  URL:", config.url);
  console.log("  Method:", config.method);
  console.log("  Token exists:", !!token);
  if (token) {
    console.log("  Token (first 50 chars):", token.substring(0, 50) + "...");
    config.headers.Authorization = `Bearer ${token}`;
  } else {
    console.log("  No token found in localStorage");
  }

  // Log full config for debugging
  console.log("  Full request config:", {
    url: config.url,
    method: config.method,
    headers: config.headers,
    data: config.data,
  });

  return config;
});

// Doctor API endpoints
export const doctorAPI = {
  searchDoctors: (name, specialization) =>
    api.get("/api/doctors", { params: { name, specialization } }),
  searchDoctorProfiles: (name, specialization) =>
    api.get("/api/doctors/profiles", { params: { name, specialization } }),
};

// Appointment API endpoints
export const appointmentAPI = {
  getPatientAppointments: () => api.get("/api/patients/me/appointments"),
  createAppointment: (appointmentData) =>
    api.post("/api/patients/me/appointments", appointmentData),
  getAllAppointments: () => api.get("/api/user-data/appointments"),
  updateAppointmentStatus: (appointmentId, status) =>
    api.put(`/api/user-data/appointments/${appointmentId}/status`, { status }),
  updateAppointment: (appointmentId, data) =>
    api.put(`/api/user-data/appointments/${appointmentId}`, data),
};

// Review API endpoints
export const reviewAPI = {
  createReview: (reviewData) =>
    api.post("/api/patients/me/reviews", reviewData),
  getDoctorReviews: (doctorId) =>
    api.get(`/api/patients/me/doctors/${doctorId}/reviews`),
  getDoctorReviewStats: (doctorId) =>
    api.get(`/api/patients/me/doctors/${doctorId}/review-stats`),
};

// Auth API endpoints
export const authAPI = {
  register: (userData) => api.post("/api/auth/register", userData),
  login: (credentials) => api.post("/api/auth/login", credentials),
};

// Waitlist API endpoints
export const waitlistAPI = {
  getMyWaitlist: () => api.get("/api/patients/me/waitlist"),
  addToWaitlist: (waitlistData) =>
    api.post("/api/patients/me/waitlist", waitlistData),
  getAllWaitlist: () => api.get("/api/admin/waitlist/all"),
  updateWaitlistStatus: (waitlistId, status) =>
    api.put(`/api/admin/waitlist/${waitlistId}/status`, { status }),
  promoteToAppointment: (waitlistId) =>
    api.post(`/api/admin/waitlist/${waitlistId}/promote`),
};

// Patient API endpoints
export const patientAPI = {
  getMyProfile: () => api.get("/api/patients/me"),
  getTestPatients: () => api.get("/api/patients/test-patient"),
};

// Payment API endpoints
export const paymentAPI = {
  getPendingPayments: (patientId) => {
    console.log("Fetching pending payments for patient ID:", patientId);
    return api.get(`/payments/pending/${patientId}`);
  },
  makePayment: (paymentData) => api.post("/payments/make", paymentData),
};

/**
 * Generates a report from the server (admin-only).
 * @param {object} reportRequest { reportType, criteria, filters }
 * @returns {Promise<{html, meta}>}
 */
export async function generateReport(reportRequest) {
	try {
		const res = await api.post('/api/reports/generate', reportRequest);
		return res.data;
	} catch (err) {
		if (err.response && err.response.status === 403) {
			throw new Error('You are not authorized to generate reports. Please check your admin login or permissions.');
		}
		throw new Error(err.response?.data?.message || err.message || 'Failed to generate report');
	}
}

/**
 * Downloads a generated report as PDF (admin-only).
 * @param {object} htmlOrMeta { html } or {reportType,...}
 * @returns {Promise<Blob>} PDF blob
 */
export async function downloadReport(htmlOrMeta) {
	try {
		const res = await api.post('/api/reports/download', htmlOrMeta, {
			headers: { 'Accept': 'application/pdf' },
			responseType: 'blob',
		});
		return res.data;
	} catch (err) {
		if (err.response && err.response.status === 403) {
			throw new Error('You are not authorized to download reports. Please check your admin login or permissions.');
		}
		throw new Error(err.response?.data?.message || err.message || 'Failed to download report');
	}
}

export default api;
