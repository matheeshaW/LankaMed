import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import ConditionModal from './ConditionModal';
import AllergyModal from './AllergyModal';

const MedicalHistory = () => {
    const [activeSection, setActiveSection] = useState('conditions');
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

    const sections = [
        { id: 'conditions', label: 'Medical Conditions', icon: '🩺' },
        { id: 'allergies', label: 'Allergies', icon: '⚠️' },
        { id: 'prescriptions', label: 'Prescriptions', icon: '💊' }
    ];

    const renderConditions = () => (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-800">Medical Conditions</h3>
                <button
                    onClick={handleAddCondition}
                    className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
                >
                    Add Condition
                </button>
            </div>
            <div className="space-y-4">
                {conditions.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No medical conditions recorded</p>
                ) : (
                    conditions.map((condition) => (
                        <div key={condition.conditionId} className="bg-gray-50 p-4 rounded-lg">
                            <div className="flex justify-between items-start">
                                <div className="flex-1">
                                    <h4 className="font-semibold text-gray-800">{condition.conditionName}</h4>
                                    {condition.diagnosedDate && (
                                        <p className="text-sm text-gray-600">
                                            Diagnosed: {new Date(condition.diagnosedDate).toLocaleDateString()}
                                        </p>
                                    )}
                                    {condition.notes && (
                                        <p className="text-sm text-gray-600 mt-1">{condition.notes}</p>
                                    )}
                                </div>
                                <div className="flex space-x-2">
                                    <button
                                        onClick={() => handleEditCondition(condition)}
                                        className="text-blue-600 hover:text-blue-800 text-sm"
                                    >
                                        Edit
                                    </button>
                                    <button
                                        onClick={() => handleDeleteCondition(condition.conditionId)}
                                        className="text-red-600 hover:text-red-800 text-sm"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    const renderAllergies = () => (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-800">Allergies</h3>
                <button
                    onClick={handleAddAllergy}
                    className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors"
                >
                    Add Allergy
                </button>
            </div>
            <div className="space-y-4">
                {allergies.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No allergies recorded</p>
                ) : (
                    allergies.map((allergy) => (
                        <div key={allergy.allergyId} className="bg-gray-50 p-4 rounded-lg">
                            <div className="flex justify-between items-start">
                                <div className="flex-1">
                                    <div className="flex items-center space-x-2">
                                        <h4 className="font-semibold text-gray-800">{allergy.allergyName}</h4>
                                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                            allergy.severity === 'SEVERE' ? 'bg-red-100 text-red-800' :
                                            allergy.severity === 'MODERATE' ? 'bg-yellow-100 text-yellow-800' :
                                            'bg-green-100 text-green-800'
                                        }`}>
                                            {allergy.severity}
                                        </span>
                                    </div>
                                    {allergy.notes && (
                                        <p className="text-sm text-gray-600 mt-1">{allergy.notes}</p>
                                    )}
                                </div>
                                <div className="flex space-x-2">
                                    <button
                                        onClick={() => handleEditAllergy(allergy)}
                                        className="text-blue-600 hover:text-blue-800 text-sm"
                                    >
                                        Edit
                                    </button>
                                    <button
                                        onClick={() => handleDeleteAllergy(allergy.allergyId)}
                                        className="text-red-600 hover:text-red-800 text-sm"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    const renderPrescriptions = () => (
        <div>
            <div className="flex justify-between items-center mb-4">
                <h3 className="text-lg font-semibold text-gray-800">Prescriptions</h3>
                <span className="text-sm text-gray-500">Read-only (managed by doctors)</span>
            </div>
            <div className="space-y-4">
                {prescriptions.length === 0 ? (
                    <p className="text-gray-500 text-center py-8">No prescriptions found</p>
                ) : (
                    prescriptions.map((prescription) => (
                        <div key={prescription.prescriptionId} className="bg-gray-50 p-4 rounded-lg">
                            <div className="flex justify-between items-start">
                                <div className="flex-1">
                                    <h4 className="font-semibold text-gray-800">{prescription.medicationName}</h4>
                                    <div className="grid grid-cols-2 gap-4 mt-2 text-sm text-gray-600">
                                        <div>
                                            <span className="font-medium">Dosage:</span> {prescription.dosage || 'N/A'}
                                        </div>
                                        <div>
                                            <span className="font-medium">Frequency:</span> {prescription.frequency || 'N/A'}
                                        </div>
                                        <div>
                                            <span className="font-medium">Start Date:</span> {new Date(prescription.startDate).toLocaleDateString()}
                                        </div>
                                        <div>
                                            <span className="font-medium">End Date:</span> {prescription.endDate ? new Date(prescription.endDate).toLocaleDateString() : 'Ongoing'}
                                        </div>
                                    </div>
                                    <div className="mt-2 text-sm text-gray-600">
                                        <span className="font-medium">Prescribed by:</span> {prescription.doctorName}
                                        {prescription.doctorSpecialization && ` (${prescription.doctorSpecialization})`}
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    if (loading) {
        return (
            <div className="flex justify-center items-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
            </div>
        );
    }

    return (
        <div className="max-w-6xl mx-auto">
            {/* Section Tabs */}
            <div className="border-b border-gray-200 mb-6">
                <nav className="flex space-x-8">
                    {sections.map((section) => (
                        <button
                            key={section.id}
                            onClick={() => setActiveSection(section.id)}
                            className={`${
                                activeSection === section.id
                                    ? 'border-blue-500 text-blue-600'
                                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                            } whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm flex items-center space-x-2`}
                        >
                            <span>{section.icon}</span>
                            <span>{section.label}</span>
                        </button>
                    ))}
                </nav>
            </div>

            {/* Section Content */}
            {activeSection === 'conditions' && renderConditions()}
            {activeSection === 'allergies' && renderAllergies()}
            {activeSection === 'prescriptions' && renderPrescriptions()}

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
        </div>
    );
};

export default MedicalHistory;
