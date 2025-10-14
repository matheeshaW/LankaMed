import axios from 'axios';

const api = axios.create({
	baseURL: 'http://localhost:8080',
});

api.interceptors.request.use((config) => {
	const token = localStorage.getItem('token');
	if (token) {
		config.headers.Authorization = `Bearer ${token}`;
	}
	return config;
});

// Doctor API endpoints
export const doctorAPI = {
	searchDoctors: (name, specialization) => 
		api.get('/api/doctors', { params: { name, specialization } }),
};

// Appointment API endpoints
export const appointmentAPI = {
	getPatientAppointments: () => 
		api.get('/api/patients/me/appointments'),
	createAppointment: (appointmentData) => 
		api.post('/api/patients/me/appointments', appointmentData),
	getAllAppointments: () => 
		api.get('/api/admin/appointments'),
	updateAppointmentStatus: (appointmentId, status) => 
		api.put(`/api/admin/appointments/${appointmentId}/status`, { status }),
};

// Review API endpoints
export const reviewAPI = {
	createReview: (reviewData) => 
		api.post('/api/patients/me/reviews', reviewData),
	getDoctorReviews: (doctorId) => 
		api.get(`/api/patients/me/doctors/${doctorId}/reviews`),
};

export default api;


