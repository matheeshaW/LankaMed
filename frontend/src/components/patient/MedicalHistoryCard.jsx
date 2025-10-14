import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import ConditionModal from './ConditionModal';
import AllergyModal from './AllergyModal';

const MedicalHistoryCard = () => {
    const [activeTab, setActiveTab] = useState('all');
    const [conditions, setConditions] = useState([]);
    const [allergies, setAllergies] = useState([]);
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showConditionModal, setShowConditionModal] = useState(false);
    const [showAllergyModal, setShowAllergyModal] = useState(false);
    const [editingCondition, setEditingCondition] = useState(null);
    const [editingAllergy, setEditingAllergy] = useState(null);

    useEffect(() => {
        fetchAllData();
    }, []);

    const fetchAllData = async () => {
        setLoading(true);
        try {
            const [conditionsRes, allergiesRes, prescriptionsRes] = await Promise.all([
                api.get('/api/patients/me/conditions'),
                api.get('/api/patients/me/allergies'),
                api.get('/api/patients/me/prescriptions')
            ]);
            setConditions(conditionsRes.data);
            setAllergies(allergiesRes.data);
            setPrescriptions(prescriptionsRes.data);
        } catch (error) {
            console.error('Error fetching medical history:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddCondition = () => {
        setEditingCondition(null);
        setShowConditionModal(true);
    };

    const handleEditCondition = (condition) => {
        setEditingCondition(condition);
        setShowConditionModal(true);
    };

    const handleDeleteCondition = async (conditionId) => {
        if (window.confirm('Are you sure you want to delete this condition?')) {
            try {
                await api.delete(`/api/patients/me/conditions/${conditionId}`);
                setConditions(conditions.filter(c => c.conditionId !== conditionId));
            } catch (error) {
                console.error('Error deleting condition:', error);
                alert('Failed to delete condition. Please try again.');
            }
        }
    };

    const handleAddAllergy = () => {
        setEditingAllergy(null);
        setShowAllergyModal(true);
    };

    const handleEditAllergy = (allergy) => {
        setEditingAllergy(allergy);
        setShowAllergyModal(true);
    };

    const handleDeleteAllergy = async (allergyId) => {
        if (window.confirm('Are you sure you want to delete this allergy?')) {
            try {
                await api.delete(`/api/patients/me/allergies/${allergyId}`);
                setAllergies(allergies.filter(a => a.allergyId !== allergyId));
            } catch (error) {
                console.error('Error deleting allergy:', error);
                alert('Failed to delete allergy. Please try again.');
            }
        }
    };

    const handleConditionSaved = (savedCondition) => {
        if (editingCondition) {
            setConditions(conditions.map(c => 
                c.conditionId === savedCondition.conditionId ? savedCondition : c
            ));
        } else {
            setConditions([...conditions, savedCondition]);
        }
        setShowConditionModal(false);
    };

    const handleAllergySaved = (savedAllergy) => {
        if (editingAllergy) {
            setAllergies(allergies.map(a => 
                a.allergyId === savedAllergy.allergyId ? savedAllergy : a
            ));
        } else {
            setAllergies([...allergies, savedAllergy]);
        }
        setShowAllergyModal(false);
    };

    const tabs = [
        { id: 'all', label: 'All Records' },
        { id: 'recent', label: 'Recent' },
        { id: 'allergies', label: 'Allergies' },
        { id: 'prescriptions', label: 'Prescriptions' }
    ];

    const getConditionIcon = (conditionName) => {
        const name = conditionName.toLowerCase();
        if (name.includes('heart') || name.includes('cardio')) return '❤️';
        if (name.includes('lung') || name.includes('respiratory')) return '🫁';
        if (name.includes('bone') || name.includes('ortho')) return '🦴';
        if (name.includes('brain') || name.includes('neuro')) return '🧠';
        return '🩺';
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'short', 
            day: 'numeric', 
            year: 'numeric' 
        });
    };

    const getSeverityColor = (severity) => {
        switch (severity) {
            case 'SEVERE': return 'bg-red-100 text-red-800';
            case 'MODERATE': return 'bg-yellow-100 text-yellow-800';
            case 'MILD': return 'bg-green-100 text-green-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    };

    if (loading) {
        return (
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <div className="animate-pulse">
                    <div className="h-6 bg-gray-200 rounded mb-4"></div>
                    <div className="flex space-x-4 mb-6">
                        <div className="h-8 bg-gray-200 rounded w-20"></div>
                        <div className="h-8 bg-gray-200 rounded w-16"></div>
                        <div className="h-8 bg-gray-200 rounded w-24"></div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        {[1, 2, 3, 4].map(i => (
                            <div key={i} className="h-32 bg-gray-200 rounded"></div>
                        ))}
                    </div>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <div className="flex items-center justify-between mb-4">
                    <h3 className="text-lg font-semibold text-gray-800">Medical History</h3>
                    <div className="flex space-x-2">
                        <button
                            onClick={handleAddCondition}
                            className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                        >
                            Add Condition
                        </button>
                        <button
                            onClick={handleAddAllergy}
                            className="bg-blue-600 text-white px-3 py-1 rounded text-sm hover:bg-blue-700"
                        >
                            Add Allergy
                        </button>
                    </div>
                </div>
                
                {/* Tabs */}
                <div className="flex space-x-4 mb-6">
                    {tabs.map((tab) => (
                        <button
                            key={tab.id}
                            onClick={() => setActiveTab(tab.id)}
                            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                                activeTab === tab.id
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                            }`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>

                {/* Medical Records Grid */}
                <div className="grid grid-cols-2 gap-4 mb-6">
                    {activeTab === 'all' && conditions.map((condition) => (
                        <div key={condition.conditionId} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                            <div className="flex items-center mb-2">
                                <span className="text-2xl mr-2">{getConditionIcon(condition.conditionName)}</span>
                                <h4 className="font-semibold text-gray-800 text-sm">{condition.conditionName}</h4>
                            </div>
                            <p className="text-xs text-gray-500 mb-2">
                                Diagnosed: {formatDate(condition.diagnosedDate)}
                            </p>
                            <p className="text-xs text-gray-600 mb-2">{condition.notes || 'No additional notes'}</p>
                            <div className="flex justify-end space-x-1">
                                <button
                                    onClick={() => handleEditCondition(condition)}
                                    className="text-blue-600 hover:text-blue-800 text-xs"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDeleteCondition(condition.conditionId)}
                                    className="text-red-600 hover:text-red-800 text-xs"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                    
                    {activeTab === 'recent' && conditions.slice(0, 4).map((condition) => (
                        <div key={condition.conditionId} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                            <div className="flex items-center mb-2">
                                <span className="text-2xl mr-2">{getConditionIcon(condition.conditionName)}</span>
                                <h4 className="font-semibold text-gray-800 text-sm">{condition.conditionName}</h4>
                            </div>
                            <p className="text-xs text-gray-500 mb-2">
                                Diagnosed: {formatDate(condition.diagnosedDate)}
                            </p>
                            <p className="text-xs text-gray-600">{condition.notes || 'No additional notes'}</p>
                        </div>
                    ))}
                    
                    {activeTab === 'allergies' && allergies.map((allergy) => (
                        <div key={allergy.allergyId} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                            <div className="flex items-center mb-2">
                                <span className="text-2xl mr-2">⚠️</span>
                                <h4 className="font-semibold text-gray-800 text-sm">{allergy.allergyName}</h4>
                                <span className={`ml-2 px-2 py-1 rounded-full text-xs font-medium ${getSeverityColor(allergy.severity)}`}>
                                    {allergy.severity}
                                </span>
                            </div>
                            <p className="text-xs text-gray-500 mb-2">
                                Added: {formatDate(allergy.createdAt || allergy.updatedAt)}
                            </p>
                            <p className="text-xs text-gray-600 mb-2">{allergy.notes || 'No additional notes'}</p>
                            <div className="flex justify-end space-x-1">
                                <button
                                    onClick={() => handleEditAllergy(allergy)}
                                    className="text-blue-600 hover:text-blue-800 text-xs"
                                >
                                    Edit
                                </button>
                                <button
                                    onClick={() => handleDeleteAllergy(allergy.allergyId)}
                                    className="text-red-600 hover:text-red-800 text-xs"
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    ))}
                    
                    {activeTab === 'prescriptions' && prescriptions.map((prescription) => (
                        <div key={prescription.prescriptionId} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
                            <div className="flex items-center mb-2">
                                <span className="text-2xl mr-2">💊</span>
                                <h4 className="font-semibold text-gray-800 text-sm">{prescription.medicationName}</h4>
                            </div>
                            <p className="text-xs text-gray-500 mb-2">
                                Started: {formatDate(prescription.startDate)}
                            </p>
                            <p className="text-xs text-gray-600">
                                {prescription.dosage} - {prescription.frequency}
                            </p>
                        </div>
                    ))}
                </div>

                {/* Chronic Conditions */}
                <div className="mb-6">
                    <h4 className="text-md font-semibold text-gray-800 mb-3">Chronic Conditions</h4>
                    <div className="flex flex-wrap gap-2">
                        {conditions.length === 0 ? (
                            <span className="text-gray-500 text-sm">No chronic conditions recorded</span>
                        ) : (
                            conditions.map((condition) => (
                                <span
                                    key={condition.conditionId}
                                    className="px-3 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800"
                                >
                                    {condition.conditionName}
                                </span>
                            ))
                        )}
                    </div>
                </div>

                {/* Allergies */}
                <div className="mb-6">
                    <h4 className="text-md font-semibold text-gray-800 mb-3">Allergies</h4>
                    <div className="flex flex-wrap gap-2">
                        {allergies.length === 0 ? (
                            <span className="text-gray-500 text-sm">No allergies recorded</span>
                        ) : (
                            allergies.map((allergy) => (
                                <span
                                    key={allergy.allergyId}
                                    className={`px-3 py-1 rounded-full text-xs font-medium ${getSeverityColor(allergy.severity)}`}
                                >
                                    {allergy.allergyName}
                                </span>
                            ))
                        )}
                    </div>
                </div>
            </div>
            
            {/* Placeholder for Health Metrics - to be added later */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Health Metrics</h3>
                <div className="text-center text-gray-500 py-8">
                    <p>Health Metrics section will be added here</p>
                </div>
            </div>
            
            {/* Placeholder for Blood Pressure Trend - to be added later */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Blood Pressure Trend</h3>
                <div className="text-center text-gray-500 py-8">
                    <p>Blood Pressure Trend chart will be added here</p>
                </div>
            </div>
            
            {/* Placeholder for Weight History - to be added later */}
            <div className="bg-white rounded-lg shadow-md p-6">
                <h3 className="text-lg font-semibold text-gray-800 mb-4">Weight History</h3>
                <div className="text-center text-gray-500 py-8">
                    <p>Weight History chart will be added here</p>
                </div>
            </div>

            {/* Modals */}
            {showConditionModal && (
                <ConditionModal
                    condition={editingCondition}
                    onSave={handleConditionSaved}
                    onClose={() => setShowConditionModal(false)}
                />
            )}
            {showAllergyModal && (
                <AllergyModal
                    allergy={editingAllergy}
                    onSave={handleAllergySaved}
                    onClose={() => setShowAllergyModal(false)}
                />
            )}
        </>
    );
};

export default MedicalHistoryCard;
