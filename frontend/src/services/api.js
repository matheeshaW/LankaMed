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


