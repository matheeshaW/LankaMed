import React, { useState, useEffect } from 'react';
import { appointmentAPI } from '../../services/api';
import AddReview from './AddReview';

const AppointmentHistory = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('all'); // all, upcoming, past
    const [showReviewModal, setShowReviewModal] = useState(false);
    const [selectedAppointment, setSelectedAppointment] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');

    useEffect(() => {
        fetchAppointments();
    }, []);

    const fetchAppointments = async () => {
        try {
            const response = await appointmentAPI.getPatientAppointments();
            setAppointments(response.data);
        } catch (error) {
            console.error('Error fetching appointments:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleDownloadHistory = async () => {
        try {
            const response = await api.get('/api/patients/me/appointments/download', {
                responseType: 'blob'
            });
            
            // Create blob link to download
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'appointment-history.pdf');
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error downloading appointment history:', error);
            alert('Failed to download appointment history. Please try again.');
        }
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING':
                return 'bg-yellow-100 text-yellow-800';
            case 'APPROVED':
                return 'bg-blue-100 text-blue-800';
            case 'CONFIRMED':
                return 'bg-green-100 text-green-800';
            case 'REJECTED':
                return 'bg-red-100 text-red-800';
            case 'COMPLETED':
                return 'bg-green-100 text-green-800';
            case 'CANCELLED':
                return 'bg-red-100 text-red-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'PENDING':
                return 'Pending';
            case 'APPROVED':
                return 'Approved';
            case 'CONFIRMED':
                return 'Confirmed';
            case 'REJECTED':
                return 'Rejected';
            case 'COMPLETED':
                return 'Completed';
            case 'CANCELLED':
                return 'Cancelled';
            default:
                return status;
        }
    };

    const handleAddReview = (appointment) => {
        setSelectedAppointment(appointment);
        setShowReviewModal(true);
    };

    const handleReviewSuccess = (message) => {
        setSuccessMessage(message);
        fetchAppointments(); // Refresh appointments
        setTimeout(() => setSuccessMessage(''), 3000);
    };

    const filteredAppointments = appointments.filter(appointment => {
        const now = new Date();
        const appointmentDate = new Date(appointment.appointmentDateTime);
        
        switch (filter) {
            case 'upcoming':
                return appointmentDate > now && ['PENDING', 'APPROVED', 'CONFIRMED'].includes(appointment.status);
            case 'past':
                return appointmentDate <= now || ['COMPLETED', 'CANCELLED', 'REJECTED'].includes(appointment.status);
            default:
                return true;
        }
    });

    if (loading) {
        return (
            <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto">
            {successMessage && (
                <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                    {successMessage}
                </div>
            )}
            <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-2xl font-bold text-gray-800">Appointment History</h2>
                    <button
                        onClick={handleDownloadHistory}
                        className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center space-x-2"
                    >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <span>Download PDF</span>
                    </button>
                </div>

                {/* Filter Tabs */}
                <div className="border-b border-gray-200 mb-6">
                    <nav className="flex space-x-8">
                        {[
                            { id: 'all', label: 'All Appointments' },
                            { id: 'upcoming', label: 'Upcoming' },
                            { id: 'past', label: 'Past' }
                        ].map((tab) => (
                            <button
                                key={tab.id}
                                onClick={() => setFilter(tab.id)}
                                className={`${
                                    filter === tab.id
                                        ? 'border-blue-500 text-blue-600'
                                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm`}
                            >
                                {tab.label}
                            </button>
                        ))}
                    </nav>
                </div>

                {/* Appointments List */}
                <div className="space-y-4">
                    {filteredAppointments.length === 0 ? (
                        <div className="text-center py-12">
                            <div className="text-6xl mb-4">📅</div>
                            <h3 className="text-lg font-medium text-gray-900 mb-2">No appointments found</h3>
                            <p className="text-gray-500">
                                {filter === 'all' 
                                    ? 'You don\'t have any appointments yet.'
                                    : filter === 'upcoming'
                                    ? 'You don\'t have any upcoming appointments.'
                                    : 'You don\'t have any past appointments.'
                                }
                            </p>
                        </div>
                    ) : (
                        filteredAppointments.map((appointment) => (
                            <div key={appointment.appointmentId} className="border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow">
                                <div className="flex justify-between items-start">
                                    <div className="flex-1">
                                        <div className="flex items-center space-x-4 mb-3">
                                            <div className="text-2xl">🏥</div>
                                            <div>
                                                <h3 className="text-lg font-semibold text-gray-900">
                                                    {appointment.hospitalName}
                                                </h3>
                                                <p className="text-sm text-gray-600">
                                                    {appointment.serviceCategoryName}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                                            <div>
                                                <p className="text-sm font-medium text-gray-700">Date & Time</p>
                                                <p className="text-sm text-gray-600">
                                                    {new Date(appointment.appointmentDateTime).toLocaleString()}
                                                </p>
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium text-gray-700">Doctor</p>
                                                <p className="text-sm text-gray-600">
                                                    {appointment.doctorName}
                                                    {appointment.doctorSpecialization && (
                                                        <span className="text-gray-500"> - {appointment.doctorSpecialization}</span>
                                                    )}
                                                </p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="ml-4 flex flex-col items-end space-y-2">
                                        <span className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(appointment.status)}`}>
                                            {getStatusText(appointment.status)}
                                        </span>
                                        {appointment.status === 'COMPLETED' && (
                                            <button
                                                onClick={() => handleAddReview(appointment)}
                                                className="text-sm bg-blue-600 text-white px-3 py-1 rounded-lg hover:bg-blue-700 transition-colors"
                                            >
                                                Add Review
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {/* Summary Stats */}
                {appointments.length > 0 && (
                    <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="bg-blue-50 p-4 rounded-lg">
                            <div className="text-2xl font-bold text-blue-600">
                                {appointments.filter(a => ['PENDING', 'APPROVED', 'CONFIRMED'].includes(a.status)).length}
                            </div>
                            <div className="text-sm text-blue-800">Active</div>
                        </div>
                        <div className="bg-green-50 p-4 rounded-lg">
                            <div className="text-2xl font-bold text-green-600">
                                {appointments.filter(a => a.status === 'COMPLETED').length}
                            </div>
                            <div className="text-sm text-green-800">Completed</div>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <div className="text-2xl font-bold text-gray-600">
                                {appointments.length}
                            </div>
                            <div className="text-sm text-gray-800">Total</div>
                        </div>
                    </div>
                )}
            </div>

            {/* Review Modal */}
            {showReviewModal && (
                <AddReview
                    appointment={selectedAppointment}
                    onClose={() => setShowReviewModal(false)}
                    onSuccess={handleReviewSuccess}
                />
            )}
        </div>
    );
};

export default AppointmentHistory;
