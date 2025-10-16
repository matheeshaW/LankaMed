import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRole, logout } from '../utils/auth';
import PatientProfile from '../components/patient/PatientProfile';
import MedicalHistory from '../components/patient/MedicalHistory';
import PersonalInformationCard from '../components/patient/PersonalInformationCard';
import EmergencyContactCard from '../components/patient/EmergencyContactCard';
import MedicalHistoryCard from '../components/patient/MedicalHistoryCard';
import DownloadReportsButton from '../components/patient/DownloadReportsButton';

const PatientDashboard = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const role = getRole();
        console.log('PatientDashboard - checking role:', role);
        if (role !== 'PATIENT') {
            console.log('Not a patient, redirecting to login');
            navigate('/login');
        }
    }, [navigate]);

    const [activeTab, setActiveTab] = React.useState('profile');

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const tabs = [
        { id: 'profile', label: 'My Profile', icon: '👤' },
        { id: 'medical', label: 'Medical History', icon: '🏥' }
    ];

    const renderActiveTab = () => {
        switch (activeTab) {
            case 'profile':
                return <PatientProfile />;
            case 'medical':
                return <MedicalHistory />;
            default:
                return <PatientProfile />;
        }
    };
    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="bg-white rounded-xl shadow-lg overflow-hidden">
                    {/* Header */}
                    <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8">
                        <h1 className="text-3xl font-bold text-white mb-2">Patient Dashboard</h1>
                        <p className="text-blue-100">Manage your health information and medical records</p>
                    </div>
                    
                    {/* Tab Navigation */}
                    <div className="border-b border-gray-200">
                        <nav className="flex space-x-8 px-6">
                            {tabs.map((tab) => (
                                <button
                                    key={tab.id}
                                    onClick={() => setActiveTab(tab.id)}
                                    className={`py-4 px-1 border-b-2 font-medium text-sm ${
                                        activeTab === tab.id
                                            ? 'border-blue-500 text-blue-600'
                                            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                                    }`}
                                >
                                    <span className="mr-2">{tab.icon}</span>
                                    {tab.label}
                                </button>
                            ))}
                        </nav>
                    </div>
                    
                    {/* Tab Content */}
                    <div className="p-6">
                        {renderActiveTab()}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PatientDashboard;
