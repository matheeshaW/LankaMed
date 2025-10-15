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
 * @param {string} token Optional: Bearer JWT
 * @returns {Promise<{html, meta}>}
 */
export async function generateReport(reportRequest, token) {
	const headers = token ? { Authorization: `Bearer ${token}` } : {};
	const res = await api.post('/api/reports/generate', reportRequest, { headers });
	return res.data;
}

/**
 * Downloads a generated report as PDF (admin-only).
 * @param {object} htmlOrMeta { html } or {reportType,...}
 * @param {string} token Optional: Bearer JWT
 * @returns {Promise<Blob>} PDF blob
 */
export async function downloadReport(htmlOrMeta, token) {
	const headers = {
		'Accept': 'application/pdf',
	};
	if (token) headers['Authorization'] = `Bearer ${token}`;
	const res = await api.post('/api/reports/download', htmlOrMeta, {
		headers,
		responseType: 'blob',
	});
	return res.data;
}

export default api;


