import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRole } from '../utils/auth';
import PersonalInformationCard from '../components/patient/PersonalInformationCard';
import EmergencyContactCard from '../components/patient/EmergencyContactCard';
import MedicalHistoryCard from '../components/patient/MedicalHistoryCard';
import DownloadReportsButton from '../components/patient/DownloadReportsButton';

const PatientDashboard = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const role = getRole();
        if (role !== 'PATIENT') {
            navigate('/login');
        }
    }, [navigate]);

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* Two Column Layout */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Left Column - Personal Information + Emergency Contact */}
                    <div className="lg:col-span-1 space-y-8">
                        <PersonalInformationCard />
                        <EmergencyContactCard />
                        <DownloadReportsButton />
                    </div>
                    
                    {/* Middle-Right Column - Medical History */}
                    <div className="lg:col-span-2">
                        <MedicalHistoryCard />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PatientDashboard;
