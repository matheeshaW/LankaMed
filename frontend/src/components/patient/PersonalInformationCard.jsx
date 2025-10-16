import React, { useState, useEffect, useRef } from 'react';
import api from '../../services/api';
import { validatePatientForm } from '../../utils/validators';
import JsBarcode from 'jsbarcode';

const PersonalInformationCard = () => {
    const [profile, setProfile] = useState(null);
    const [isEditing, setIsEditing] = useState(false);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        dateOfBirth: '',
        gender: '',
        contactNumber: '',
        address: ''
    });
    const [errors, setErrors] = useState({});
    const barcodeRef = useRef(null);

    useEffect(() => {
        fetchProfile();
    }, []);

    useEffect(() => {
        // render barcode whenever profile loads
        if (barcodeRef.current && profile?.patientId) {
            try {
                // JsBarcode supports SVG element
                JsBarcode(barcodeRef.current, String(profile.patientId), {
                    format: 'CODE128',
                    displayValue: true,
                    fontSize: 10,
                    height: 40,
                    textMargin: 2
                });
            } catch (e) {
                console.error('Barcode render error', e);
            }
        }
    }, [profile]);

    const fetchProfile = async () => {
        try {
            const response = await api.get('/api/patients/me');
            setProfile(response.data);
            setFormData({
                firstName: response.data.firstName || '',
                lastName: response.data.lastName || '',
                email: response.data.email || '',
                dateOfBirth: response.data.dateOfBirth || '',
                gender: response.data.gender || '',
                contactNumber: response.data.contactNumber || '',
                address: response.data.address || ''
            });
        } catch (error) {
            console.error('Error fetching profile:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        const next = {
            ...formData,
            [name]: value
        };
        setFormData(next);
        // validate on change (single responsibility)
        const v = validatePatientForm(next);
        setErrors(v);
    };

    const handleSave = async () => {
        const v = validatePatientForm(formData);
        setErrors(v);
        const hasError = Object.values(v).some(Boolean);
        if (hasError) return;

        setSaving(true);
        try {
            const response = await api.put('/api/patients/me', formData);
            setProfile(response.data);
            setIsEditing(false);
        } catch (error) {
            console.error('Error updating profile:', error);
            alert('Failed to update profile. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    const handleCancel = () => {
        setFormData({
            firstName: profile?.firstName || '',
            lastName: profile?.lastName || '',
            email: profile?.email || '',
            dateOfBirth: profile?.dateOfBirth || '',
            gender: profile?.gender || '',
            contactNumber: profile?.contactNumber || '',
            address: profile?.address || ''
        });
        setIsEditing(false);
    };

    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        });
    };

    const formatPhone = (phone) => {
        if (!phone) return 'N/A';
        // Format phone number to (XXX) XXX-XXXX
        const cleaned = phone.replace(/\D/g, '');
        const match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);
        if (match) {
            return `(${match[1]}) ${match[2]}-${match[3]}`;
        }
        return phone;
    };

    if (loading) {
        return (
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <div className="animate-pulse">
                    <div className="h-6 bg-gray-200 rounded mb-4"></div>
                    <div className="h-32 bg-gray-200 rounded mb-4"></div>
                    <div className="space-y-3">
                        <div className="h-4 bg-gray-200 rounded"></div>
                        <div className="h-4 bg-gray-200 rounded"></div>
                        <div className="h-4 bg-gray-200 rounded"></div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
            <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                    <h3 className="text-lg font-semibold text-gray-800 mr-2">Personal Information</h3>
                    <svg className="w-5 h-5 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                </div>
                {!isEditing && (
                    <button
                        onClick={() => setIsEditing(true)}
                        className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                        Edit
                    </button>
                )}
            </div>
            
            {/* Profile Picture Placeholder */}
            <div className="w-20 h-20 bg-gray-200 rounded-lg mb-4 flex items-center justify-center">
                <svg className="w-8 h-8 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                </svg>
            </div>

            {/* Personal Details */}
            <div className="space-y-3">
                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Name:</span> {isEditing ? (
                            <div className="flex space-x-2">
                                <input
                                    type="text"
                                    name="firstName"
                                    value={formData.firstName}
                                    onChange={handleInputChange}
                                    className="w-20 px-2 py-1 border border-gray-300 rounded text-xs"
                                    placeholder="First"
                                />
                                <input
                                    type="text"
                                    name="lastName"
                                    value={formData.lastName}
                                    onChange={handleInputChange}
                                    className="w-20 px-2 py-1 border border-gray-300 rounded text-xs"
                                    placeholder="Last"
                                />
                            </div>
                        ) : (
                            `${profile?.firstName || 'N/A'} ${profile?.lastName || 'N/A'}`
                        )}
                    </span>
                </div>
                
                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v4a2 2 0 002 2V6h10a2 2 0 00-2-2H4zm2 6a2 2 0 012-2h8a2 2 0 012 2v4a2 2 0 01-2 2H8a2 2 0 01-2-2v-4zm6 4a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Patient ID:</span> {profile?.patientId || 'N/A'}
                    </span>
                </div>

                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Date of Birth:</span> {isEditing ? (
                            <input
                                type="date"
                                name="dateOfBirth"
                                value={formData.dateOfBirth}
                                onChange={handleInputChange}
                                className="px-2 py-1 border border-gray-300 rounded text-xs"
                            />
                        ) : (
                            formatDate(profile?.dateOfBirth)
                        )}
                    </span>
                </div>

                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Gender:</span> {isEditing ? (
                            <select
                                name="gender"
                                value={formData.gender}
                                onChange={handleInputChange}
                                className="px-2 py-1 border border-gray-300 rounded text-xs"
                            >
                                <option value="">Select Gender</option>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        ) : (
                            profile?.gender || 'N/A'
                        )}
                    </span>
                </div>

                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Phone:</span> {isEditing ? (
                            <input
                                type="tel"
                                name="contactNumber"
                                value={formData.contactNumber}
                                onChange={handleInputChange}
                                className="px-2 py-1 border border-gray-300 rounded text-xs"
                                placeholder="Phone number"
                            />
                        ) : (
                            formatPhone(profile?.contactNumber)
                        )}
                    </span>
                </div>

                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                        <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Email:</span> {isEditing ? (
                            <input
                                type="email"
                                name="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                className="px-2 py-1 border border-gray-300 rounded text-xs"
                                placeholder="Email"
                            />
                        ) : (
                            profile?.email || 'N/A'
                        )}
                    </span>
                </div>

                <div className="flex items-center">
                    <svg className="w-4 h-4 text-gray-500 mr-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clipRule="evenodd" />
                    </svg>
                    <span className="text-sm text-gray-600">
                        <span className="font-medium">Address:</span> {isEditing ? (
                            <textarea
                                name="address"
                                value={formData.address}
                                onChange={handleInputChange}
                                rows={2}
                                className="px-2 py-1 border border-gray-300 rounded text-xs w-full"
                                placeholder="Address"
                            />
                        ) : (
                            profile?.address || 'N/A'
                        )}
                    </span>
                </div>
            </div>

            {/* Action Buttons */}
            {isEditing ? (
                <div className="flex justify-end space-x-2 mt-6">
                    <button
                        onClick={handleCancel}
                        className="px-3 py-1 border border-gray-300 text-gray-700 rounded text-sm hover:bg-gray-50"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={saving || Object.values(errors).some(Boolean)}
                        className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 disabled:opacity-50"
                    >
                        {saving ? 'Saving...' : 'Save'}
                    </button>
                </div>
            ) : (
                <div className="flex flex-col items-center mt-6">
                    {/* Visible barcode for the patient ID - staff can scan this */}
                    <svg ref={barcodeRef} />
                    <div className="text-xs text-gray-500 mt-2">ID: {profile?.patientId || 'N/A'}</div>
                </div>
            )}

            {/* Error Display */}
            {Object.keys(errors).length > 0 && (
                <div className="mt-4 text-red-600 text-xs">
                    {Object.values(errors).filter(Boolean).join(', ')}
                </div>
            )}
        </div>
    );
};

export default PersonalInformationCard;
