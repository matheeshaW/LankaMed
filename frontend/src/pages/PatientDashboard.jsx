import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRole, logout } from '../utils/auth';
import PersonalInformationCard from '../components/patient/PersonalInformationCard';
import EmergencyContactCard from '../components/patient/EmergencyContactCard';
import MedicalHistoryCard from '../components/patient/MedicalHistoryCard';
import DownloadReportsButton from '../components/patient/DownloadReportsButton';
import HealthMetricsCard from '../components/patient/HealthMetricsCard';
import UserAppointments from '../components/patient/UserAppointments';
import WaitingListCard from '../components/patient/WaitingListCard';

const PatientDashboard = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const role = getRole();
        const isLoggedIn = Boolean(localStorage.getItem('token'));
        console.log('PatientDashboard - checking role:', role, 'isLoggedIn:', isLoggedIn);
        
        if (!isLoggedIn) {
            console.log('Not logged in, redirecting to login');
            navigate('/login');
        } else if (role && role !== 'PATIENT') {
            console.log('Not a patient, redirecting to appropriate dashboard');
            if (role === 'ADMIN') {
                navigate('/admin');
            } else {
                navigate('/login');
            }
        }
    }, [navigate]);

    const [activeTab, setActiveTab] = React.useState('profile');

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const tabs = [
        { id: 'profile', label: 'My Profile', icon: '👤' },
        { id: 'medical', label: 'Medical History', icon: '🏥' },
        { id: 'appointments', label: 'Appointments', icon: '📅' },
        { id: 'waiting', label: 'Waiting List', icon: '⏳' },
        { id: 'health', label: 'Health Metrics', icon: '📊' }
    ];

    const renderActiveTab = () => {
        switch (activeTab) {
            case 'profile':
                return (
                    <div className="space-y-6">
                        <PersonalInformationCard />
                        <EmergencyContactCard />
                        <DownloadReportsButton />
                    </div>
                );
            case 'medical':
                return <MedicalHistoryCard />;
            case 'appointments':
                return <UserAppointments />;
            case 'waiting':
                return <WaitingListCard />;
            case 'health':
                return <HealthMetricsCard />;
            default:
                return (
                    <div className="space-y-6">
                        <PersonalInformationCard />
                        <EmergencyContactCard />
                        <DownloadReportsButton />
                    </div>
                );
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
