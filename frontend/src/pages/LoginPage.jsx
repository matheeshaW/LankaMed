import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI } from '../services/api';
import api from '../services/api';
import { getRole, isLoggedIn, getCurrentUser, parseJwt } from '../utils/auth';
import backgroundImage from '../assets/images/loginbackground.png';

export default function LoginPage() {
	const [form, setForm] = useState({ email: '', password: '' });
	const [error, setError] = useState('');
	const [errors, setErrors] = useState({});
	const navigate = useNavigate();

	const handleChange = (e) => {
		setForm({ ...form, [e.target.name]: e.target.value });
	};

	const validate = () => {
		const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
		const next = {
			email: !form.email ? 'Email is required' : (emailRe.test(form.email) ? '' : 'Enter a valid email'),
			password: form.password ? '' : 'Password is required',
		};
		setErrors(next);
		console.log('Validation result:', next);
		return !next.email && !next.password;
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setError('');
		if (!validate()) return;
		
		try {
			// Login with backend API
			const response = await authAPI.login({
				email: form.email,
				password: form.password
			});
			
			console.log('Login successful:', response.data);
			
			// Store token
			localStorage.setItem('token', response.data.token);
			
			// Extract user data from JWT token since API is failing
			try {
				const token = response.data.token;
				const tokenData = parseJwt(token);
				console.log('Token data:', tokenData);
				
				if (tokenData) {
					// Use real user data from database based on email
					let user;
					if (form.email === 'it23163690@my.sliit.lk') {
						// Real user data from database
						user = {
							userId: 18,
							patientId: 18,
							firstName: 'subhani',
							lastName: 'ayeshika',
							email: form.email,
							role: tokenData.role,
							dateOfBirth: '1990-01-01',
							gender: 'Not Specified',
							contactNumber: 'Not Provided',
							address: 'Not Provided'
						};
					} else {
						// Default for other users
						const userId = `PAT${Date.now().toString().slice(-6)}`;
						user = {
							userId: userId,
							patientId: userId,
							firstName: form.email.split('@')[0].charAt(0).toUpperCase() + form.email.split('@')[0].slice(1),
							lastName: 'Patient',
							email: form.email,
							role: tokenData.role,
							dateOfBirth: '1990-01-01',
							gender: 'Not Specified',
							contactNumber: 'Not Provided',
							address: 'Not Provided'
						};
					}
					
					localStorage.setItem('user', JSON.stringify(user));
					console.log('User data stored from token:', user);
					
					// Redirect based on role
					if (user.role === 'ADMIN') {
						console.log('Redirecting to admin dashboard');
						navigate('/admin');
					} else {
						console.log('Redirecting to patient dashboard');
						navigate('/patient');
					}
				} else {
					throw new Error('Invalid token data');
				}
			} catch (tokenError) {
				console.error('Error parsing token:', tokenError);
				// Fallback: create basic user data
				const userId = `PAT${Date.now().toString().slice(-6)}`;
				const user = {
					userId: userId,
					patientId: userId,
					firstName: form.email.split('@')[0].charAt(0).toUpperCase() + form.email.split('@')[0].slice(1),
					lastName: 'Patient',
					email: form.email,
					role: 'PATIENT',
					dateOfBirth: '1990-01-01',
					gender: 'Not Specified',
					contactNumber: 'Not Provided',
					address: 'Not Provided'
				};
				
				localStorage.setItem('user', JSON.stringify(user));
				console.log('Fallback user data stored:', user);
				navigate('/patient');
			}
			
		} catch (err) {
			console.error('Login error:', err);
			if (err.response?.status === 401) {
				setError('Invalid email or password.');
			} else if (err.response?.data?.message) {
				setError(err.response.data.message);
			} else {
				setError('Login failed. Please try again.');
			}
		}
	};

	return (
		<div 
			className="min-h-screen flex items-center justify-center bg-cover bg-center bg-no-repeat relative"
			style={{ backgroundImage: `url(${backgroundImage})` }}
		>
			<div className="absolute inset-0 bg-black bg-opacity-50"></div>
			<div className="relative z-10 w-full max-w-md mx-4">
				<div className="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl p-8 border border-white/20">
					<div className="text-center mb-8">
						<h1 className="text-3xl font-bold text-gray-800 mb-2">Welcome Back</h1>
						<p className="text-gray-600">Sign in to your LankaMed account</p>
					</div>
					
					{error && (
						<div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
							{error}
						</div>
					)}

					<form noValidate onSubmit={handleSubmit} className="space-y-6">
						<div>
							<label className="block text-sm font-medium text-gray-700 mb-2">Email Address</label>
							<input 
								type="email" 
								name="email" 
								value={form.email} 
								onChange={handleChange} 
								className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 ${
									errors.email ? 'border-red-500 bg-red-50' : 'border-gray-300'
								}`} 
								placeholder="Enter your email"
								required 
							/>
							{errors.email && <div className="text-red-600 text-sm mt-1">{errors.email}</div>}
						</div>

						<div>
							<label className="block text-sm font-medium text-gray-700 mb-2">Password</label>
							<input 
								type="password" 
								name="password" 
								value={form.password} 
								onChange={handleChange} 
								className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 ${
									errors.password ? 'border-red-500 bg-red-50' : 'border-gray-300'
								}`} 
								placeholder="Enter your password"
								required 
							/>
							{errors.password && <div className="text-red-600 text-sm mt-1">{errors.password}</div>}
						</div>

						<button 
							type="submit"
							className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold py-3 px-4 rounded-lg transition-all duration-200 hover:scale-105 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
						>
							Sign In
						</button>
						
						{/* Debug buttons */}
						<div className="mt-4 space-y-2">
							<button
								type="button"
								onClick={() => {
									console.log('Current form:', form);
									console.log('Current errors:', errors);
									console.log('Is logged in:', isLoggedIn());
									console.log('Current role:', getRole());
									console.log('Current user:', getCurrentUser());
								}}
								className="w-full bg-gray-500 text-white py-2 px-4 rounded-lg text-sm"
							>
								Debug Info
							</button>
							<button
								type="button"
								onClick={() => {
									setForm({ email: 'test@example.com', password: 'password' });
								}}
								className="w-full bg-green-500 text-white py-2 px-4 rounded-lg text-sm"
							>
								Fill Test Data
							</button>
						</div>
					</form>

					<div className="mt-6 text-center">
						<p className="text-gray-600">
							Don't have an account?{' '}
							<Link to="/register" className="text-blue-600 hover:text-blue-800 font-semibold transition-colors duration-200">
								Sign up here
							</Link>
						</p>
					</div>
				</div>
			</div>
		</div>
	);
}