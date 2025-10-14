import React, { useState, useEffect } from 'react';
import { appointmentAPI } from '../../services/api';

const AppointmentBooking = ({ doctor, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        doctorId: doctor?.doctorId || '',
        hospitalId: '',
        serviceCategoryId: '',
        appointmentDateTime: '',
        reason: ''
    });
    const [hospitals, setHospitals] = useState([]);
    const [serviceCategories, setServiceCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (doctor) {
            setFormData(prev => ({
                ...prev,
                doctorId: doctor.doctorId
            }));
        }
        // In a real app, you would fetch hospitals and service categories from API
        // For now, using mock data
        setHospitals([
            { hospitalId: 1, name: 'Central Medical Center' },
            { hospitalId: 2, name: 'City General Hospital' },
            { hospitalId: 3, name: 'Metro Health Clinic' }
        ]);
        setServiceCategories([
            { serviceCategoryId: 1, name: 'General Consultation' },
            { serviceCategoryId: 2, name: 'Specialist Consultation' },
            { serviceCategoryId: 3, name: 'Follow-up Visit' }
        ]);
    }, [doctor]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const appointmentData = {
                ...formData,
                appointmentDateTime: new Date(formData.appointmentDateTime).toISOString()
            };
            
            await appointmentAPI.createAppointment(appointmentData);
            onSuccess('Appointment booked successfully!');
            onClose();
        } catch (error) {
            setError(error.response?.data?.message || 'Failed to book appointment');
        } finally {
            setLoading(false);
        }
    };

    if (!doctor) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
                <div className="p-6">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-2xl font-bold text-gray-800">Book Appointment</h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    {/* Doctor Info */}
                    <div className="bg-blue-50 rounded-lg p-4 mb-6">
                        <div className="flex items-center space-x-4">
                            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                                <span className="text-lg font-bold text-blue-600">
                                    {doctor.firstName.charAt(0)}{doctor.lastName.charAt(0)}
                                </span>
                            </div>
                            <div>
                                <h3 className="font-semibold text-gray-800">Dr. {doctor.fullName}</h3>
                                <p className="text-blue-600">{doctor.specialization}</p>
                                <p className="text-sm text-gray-600">{doctor.hospitalName}</p>
                            </div>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Hospital
                                </label>
                                <select
                                    name="hospitalId"
                                    value={formData.hospitalId}
                                    onChange={handleInputChange}
                                    required
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select Hospital</option>
                                    {hospitals.map(hospital => (
                                        <option key={hospital.hospitalId} value={hospital.hospitalId}>
                                            {hospital.name}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Service Type
                                </label>
                                <select
                                    name="serviceCategoryId"
                                    value={formData.serviceCategoryId}
                                    onChange={handleInputChange}
                                    required
                                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select Service</option>
                                    {serviceCategories.map(category => (
                                        <option key={category.serviceCategoryId} value={category.serviceCategoryId}>
                                            {category.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Date & Time
                            </label>
                            <input
                                type="datetime-local"
                                name="appointmentDateTime"
                                value={formData.appointmentDateTime}
                                onChange={handleInputChange}
                                required
                                min={new Date().toISOString().slice(0, 16)}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Reason for Visit (Optional)
                            </label>
                            <textarea
                                name="reason"
                                value={formData.reason}
                                onChange={handleInputChange}
                                rows={3}
                                placeholder="Briefly describe your symptoms or reason for the appointment"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                        </div>

                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                                {error}
                            </div>
                        )}

                        <div className="flex space-x-4 pt-4">
                            <button
                                type="button"
                                onClick={onClose}
                                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {loading ? 'Booking...' : 'Book Appointment'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AppointmentBooking;
