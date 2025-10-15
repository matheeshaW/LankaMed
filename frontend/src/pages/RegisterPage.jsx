import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { mockLogin } from '../utils/auth';
import backgroundImage from '../assets/images/loginbackground.png';

export default function RegisterPage() {
	const [form, setForm] = useState({ firstName: '', lastName: '', email: '', role: 'PATIENT', password: '' });
	const [errors, setErrors] = useState({});
	const [error, setError] = useState('');
	const [success, setSuccess] = useState('');
	const [submitting, setSubmitting] = useState(false);
	const navigate = useNavigate();

	const validators = {
		firstName: (v) => {
			if (!v?.trim()) return 'First name is required';
			if (v.trim().length < 2) return 'First name must be at least 2 characters';
			return '';
		},
		lastName: (v) => {
			if (!v?.trim()) return 'Last name is required';
			if (v.trim().length < 2) return 'Last name must be at least 2 characters';
			return '';
		},
		email: (v) => {
			if (!v) return 'Email is required';
			const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
			return re.test(v) ? '' : 'Enter a valid email address';
		},
		role: (v) => (!v ? 'Role is required' : ''),
		password: (v) => {
			if (!v) return 'Password is required';
			if (v.length < 8) return 'Password must be at least 8 characters';
			if (!/[A-Z]/.test(v) || !/[a-z]/.test(v) || !/[0-9]/.test(v)) return 'Use upper, lower, and a number';
			return '';
		},
	};

	const validateField = (name, value) => {
		const msg = validators[name] ? validators[name](value) : '';
		setErrors((prev) => ({ ...prev, [name]: msg }));
		return msg;
	};

	const validateAll = () => {
		const nextErrors = Object.keys(form).reduce((acc, k) => {
			acc[k] = validators[k] ? validators[k](form[k]) : '';
			return acc;
		}, {});
		setErrors(nextErrors);
		return Object.values(nextErrors).every((m) => !m);
	};

	const handleChange = (e) => {
		const { name, value } = e.target;
		setForm((f) => ({ ...f, [name]: value }));
		validateField(name, value);
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setError(''); setSuccess('');
		if (!validateAll()) return;
		setSubmitting(true);
		
		try {
			// Mock registration - store user data and auto-login
			const newUser = {
				id: Date.now(), // Simple ID generation
				name: `${form.firstName} ${form.lastName}`,
				email: form.email,
				role: form.role,
				phone: '+94 77 000 0000', // Default phone
				address: 'Colombo, Sri Lanka' // Default address
			};
			
			// Store user data in localStorage
			const existingUsers = JSON.parse(localStorage.getItem('lankamed_users') || '[]');
			existingUsers.push(newUser);
			localStorage.setItem('lankamed_users', JSON.stringify(existingUsers));
			
			// Auto-login the user
			mockLogin(newUser);
			
			setSuccess('Registration successful! Redirecting to dashboard...');
			
			// Redirect to appropriate dashboard
			setTimeout(() => {
				if (form.role === 'ADMIN') {
					navigate('/admin');
				} else {
					navigate('/patient');
				}
			}, 1500);
			
		} catch (err) {
			console.error('Registration error:', err);
			setError('Registration failed. Please try again.');
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<div 
			className="min-h-screen flex items-center justify-center bg-cover bg-center bg-no-repeat relative py-12"
			style={{ backgroundImage: `url(${backgroundImage})` }}
		>
			<div className="absolute inset-0 bg-black bg-opacity-50"></div>
			<div className="relative z-10 w-full max-w-md mx-4">
				<div className="bg-white/95 backdrop-blur-sm rounded-2xl shadow-2xl p-8 border border-white/20">
					<div className="text-center mb-8">
						<h1 className="text-3xl font-bold text-gray-800 mb-2">Join LankaMed</h1>
						<p className="text-gray-600">Create your healthcare account</p>
					</div>
					
					{error && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">{error}</div>}
					{success && <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-6">{success}</div>}

					<form noValidate onSubmit={handleSubmit} className="space-y-6">
						<div className="grid grid-cols-2 gap-4">
							<div>
								<label className="block text-sm font-medium text-gray-700 mb-2">First Name</label>
								<input 
									name="firstName" 
									value={form.firstName} 
									onChange={handleChange} 
									className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 ${
										errors.firstName ? 'border-red-500 bg-red-50' : 'border-gray-300'
									}`} 
									placeholder="First name"
									required 
								/>
								{errors.firstName && <div className="text-red-600 text-sm mt-1">{errors.firstName}</div>}
							</div>
							<div>
								<label className="block text-sm font-medium text-gray-700 mb-2">Last Name</label>
								<input 
									name="lastName" 
									value={form.lastName} 
									onChange={handleChange} 
									className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 ${
										errors.lastName ? 'border-red-500 bg-red-50' : 'border-gray-300'
									}`} 
									placeholder="Last name"
									required 
								/>
								{errors.lastName && <div className="text-red-600 text-sm mt-1">{errors.lastName}</div>}
							</div>
						</div>

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
							<label className="block text-sm font-medium text-gray-700 mb-2">Role</label>
							<select 
								name="role" 
								value={form.role} 
								onChange={handleChange} 
								className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200 ${
									errors.role ? 'border-red-500 bg-red-50' : 'border-gray-300'
								}`} 
								required
							>
								<option value="PATIENT">Patient</option>
								<option value="DOCTOR">Doctor</option>
								<option value="STAFF">Staff</option>
								<option value="ADMIN">Admin</option>
							</select>
							{errors.role && <div className="text-red-600 text-sm mt-1">{errors.role}</div>}
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
								placeholder="Create a strong password"
								required 
							/>
							{errors.password && <div className="text-red-600 text-sm mt-1">{errors.password}</div>}
						</div>

						<button 
							type="submit"
							disabled={submitting}
							className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 disabled:from-gray-400 disabled:to-gray-500 text-white font-semibold py-3 px-4 rounded-lg transition-all duration-200 hover:scale-105 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:hover:scale-100"
						>
							{submitting ? 'Creating Account...' : 'Create Account'}
						</button>
					</form>

					<div className="mt-6 text-center">
						<p className="text-gray-600">
							Already have an account?{' '}
							<Link to="/login" className="text-blue-600 hover:text-blue-800 font-semibold transition-colors duration-200">
								Sign in here
							</Link>
						</p>
					</div>
				</div>
			</div>
		</div>
	);
}