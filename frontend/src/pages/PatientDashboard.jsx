import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRole, logout } from '../utils/auth';
import PatientProfile from '../components/patient/PatientProfile';
import MedicalHistory from '../components/patient/MedicalHistory';
import UserAppointments from '../components/patient/UserAppointments';
import DoctorList from '../components/patient/DoctorList';
import AppointmentForm from '../components/patient/AppointmentForm';

const PatientDashboard = () => {
    const [activeTab, setActiveTab] = useState('profile');
    const [showBookingModal, setShowBookingModal] = useState(false);
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        const role = getRole();
        console.log('PatientDashboard - checking role:', role);
        if (role !== 'PATIENT') {
            console.log('Not a patient, redirecting to login');
            navigate('/login');
        }
    }, [navigate]);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleBookAppointment = (doctor) => {
        setSelectedDoctor(doctor);
        setShowBookingModal(true);
    };

    const handleBookingSuccess = (message) => {
        setSuccessMessage(message);
        setTimeout(() => setSuccessMessage(''), 5000);
        setShowBookingModal(false);
        setSelectedDoctor(null);
    };

    const tabs = [
        { id: 'profile', label: 'My Profile', icon: '👤' },
        { id: 'doctors', label: 'Find Doctors', icon: '🔍' },
        { id: 'medical', label: 'Medical History', icon: '🏥' },
        { id: 'appointments', label: 'My Appointments', icon: '📅' }
    ];

    const renderActiveTab = () => {
        switch (activeTab) {
            case 'profile':
                return <PatientProfile />;
            case 'doctors':
                return <DoctorList onBookAppointment={handleBookAppointment} />;
            case 'medical':
                return <MedicalHistory />;
            case 'appointments':
                return <UserAppointments />;
            default:
                return <PatientProfile />;
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                    {/* Header */}
                    <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8">
                        <h1 className="text-3xl font-bold text-white mb-2">Patient Dashboard</h1>
                        <p className="text-blue-100">Manage your health information and appointments</p>
                    </div>

                    {/* Tab Navigation */}
                    <div className="border-b border-gray-200">
                        <nav className="flex space-x-8 px-6" aria-label="Tabs">
                            {tabs.map((tab) => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`${
                                        activeTab === tab.id
                                            ? 'border-blue-500 text-blue-600'
                                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center space-x-2`}
                                >
                                    <span className="text-lg">{tab.icon}</span>
                                    <span>{tab.label}</span>
                                </button>
                            ))}
                        </nav>
                    </div>

                    {/* Tab Content */}
                    <div className="p-6">
                        {successMessage && (
                            <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                                {successMessage}
                            </div>
                        )}
                        {renderActiveTab()}
                    </div>
                </div>
            </div>

            {/* Booking Modal */}
            {showBookingModal && selectedDoctor && (
                <AppointmentForm
                    doctor={selectedDoctor}
                    onClose={() => setShowBookingModal(false)}
                    onSuccess={handleBookingSuccess}
                />
            )}
        </div>
    );
};

export default PatientDashboard;
