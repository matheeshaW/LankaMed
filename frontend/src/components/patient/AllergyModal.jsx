import React, { useState, useEffect } from 'react';
import api from '../../services/api';

const AllergyModal = ({ allergy, onSave, onClose }) => {
    const [formData, setFormData] = useState({
        allergyName: '',
        severity: 'MILD',
        notes: ''
    });
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (allergy) {
            setFormData({
                allergyName: allergy.allergyName || '',
                severity: allergy.severity || 'MILD',
                notes: allergy.notes || ''
            });
        }
    }, [allergy]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            let savedAllergy;
            if (allergy) {
                // Update existing allergy
                const response = await api.put(`/api/patients/me/allergies/${allergy.allergyId}`, formData);
                savedAllergy = response.data;
            } else {
                // Create new allergy
                const response = await api.post('/api/patients/me/allergies', formData);
                savedAllergy = response.data;
            }
            onSave(savedAllergy);
        } catch (error) {
            console.error('Error saving allergy:', error);
            alert('Failed to save allergy. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
            <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
                <div className="mt-3">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-lg font-medium text-gray-900">
                            {allergy ? 'Edit Allergy' : 'Add New Allergy'}
                        </h3>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600"
                        >
                            <span className="sr-only">Close</span>
                            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Allergy Name *
                            </label>
                            <input
                                type="text"
                                name="allergyName"
                                value={formData.allergyName}
                                onChange={handleInputChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                placeholder="Enter allergy name"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Severity *
                            </label>
                            <select
                                name="severity"
                                value={formData.severity}
                                onChange={handleInputChange}
                                required
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            >
                                <option value="MILD">Mild</option>
                                <option value="MODERATE">Moderate</option>
                                <option value="SEVERE">Severe</option>
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Notes
                            </label>
                            <textarea
                                name="notes"
                                value={formData.notes}
                                onChange={handleInputChange}
                                rows={3}
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                placeholder="Additional notes about the allergy"
                            />
                        </div>

                        <div className="flex justify-end space-x-3 pt-4">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={saving}
                                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                            >
                                {saving ? 'Saving...' : (allergy ? 'Update' : 'Add')}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AllergyModal;
