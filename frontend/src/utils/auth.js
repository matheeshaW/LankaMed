export function getToken() {
	return localStorage.getItem('token') || '';
}

export function parseJwt(token) {
	try {
		const base64Url = token.split('.')[1];
		if (!base64Url) return null;
		const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
		const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
			return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
		}).join(''));
		return JSON.parse(jsonPayload);
	} catch (e) {
		return null;
	}
}

export function getRole() {
	const user = getCurrentUser();
	return user?.role || '';
}

export function isLoggedIn() {
	return Boolean(getToken());
}

export function logout() {
	localStorage.removeItem('token');
	localStorage.removeItem('user');
}

// Get current logged-in user details
export function getCurrentUser() {
	const userStr = localStorage.getItem('user');
	if (userStr) {
		try {
			return JSON.parse(userStr);
		} catch (e) {
			return null;
		}
	}
	return null;
}

// Set current user details
export function setCurrentUser(user) {
	localStorage.setItem('user', JSON.stringify(user));
}

// Mock login function for demonstration
export function mockLogin(userData) {
	const mockToken = 'mock-jwt-token-' + Date.now();
	localStorage.setItem('token', mockToken);
	localStorage.setItem('user', JSON.stringify(userData));
	return { token: mockToken, user: userData };
}


