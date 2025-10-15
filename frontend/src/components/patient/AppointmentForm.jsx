import React, { useState, useEffect } from 'react';
import { getAvailableSlots, bookAppointment } from '../../data/mockData';
import { getCurrentUser } from '../../utils/auth';

const AppointmentForm = ({ doctor, onClose, onSuccess }) => {
  const currentUser = getCurrentUser();
  
  // Debug: Log current user data
  console.log('Current user in AppointmentForm:', currentUser);
  
  const [formData, setFormData] = useState({
    patientName: currentUser?.name || '',
    patientEmail: currentUser?.email || '',
    patientPhone: currentUser?.phone || '',
    appointmentDate: '',
    appointmentTime: '',
    reason: '',
    notes: ''
  });
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (formData.appointmentDate) {
      const slots = getAvailableSlots(doctor.id, formData.appointmentDate);
      console.log('Available slots for doctor', doctor.id, 'on date', formData.appointmentDate, ':', slots);
      setAvailableSlots(slots);
      // Reset time when date changes
      setFormData(prev => ({ ...prev, appointmentTime: '' }));
    }
  }, [formData.appointmentDate, doctor.id]);

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.patientName.trim()) {
      newErrors.patientName = 'Patient name is required';
    }

    if (!formData.patientEmail.trim()) {
      newErrors.patientEmail = 'Email is required';
    } else if (!/\S+@\S+\.\S+/.test(formData.patientEmail)) {
      newErrors.patientEmail = 'Email is invalid';
    }

    if (!formData.patientPhone.trim()) {
      newErrors.patientPhone = 'Phone number is required';
    }

    if (!formData.appointmentDate) {
      newErrors.appointmentDate = 'Please select a date';
    }

    if (!formData.appointmentTime) {
      newErrors.appointmentTime = 'Please select a time slot';
    }

    if (!formData.reason.trim()) {
      newErrors.reason = 'Please provide a reason for the appointment';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    
    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const appointmentData = {
        patientId: currentUser?.id || 1,
        patientName: formData.patientName,
        doctorId: doctor.id,
        doctorName: doctor.name,
        doctorSpecialization: doctor.specialization,
        hospitalName: doctor.hospital,
        appointmentDate: formData.appointmentDate,
        appointmentTime: formData.appointmentTime,
        reason: formData.reason,
        notes: formData.notes
      };

      const newAppointment = bookAppointment(appointmentData);
      
      onSuccess(`Appointment booked successfully! Your appointment ID is #${newAppointment.id}. You will receive a confirmation email shortly.`);
      onClose();
    } catch (error) {
      console.error('Error booking appointment:', error);
      setErrors({ submit: 'Failed to book appointment. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const getMinDate = () => {
    const today = new Date();
    today.setDate(today.getDate() + 1); // Minimum tomorrow
    return today.toISOString().split('T')[0];
  };

  const getMaxDate = () => {
    const maxDate = new Date();
    maxDate.setDate(maxDate.getDate() + 30); // Maximum 30 days from now
    return maxDate.toISOString().split('T')[0];
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 rounded-t-2xl">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-white mb-2">Book Appointment</h2>
              <p className="text-blue-100">Complete the form below to book your appointment</p>
            </div>
            <button
              onClick={onClose}
              className="text-white hover:text-gray-200 transition-colors duration-200"
            >
              <svg className="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Doctor Info */}
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center space-x-4">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-2xl">
              {doctor.image}
            </div>
            <div>
              <h3 className="text-xl font-bold text-gray-800">{doctor.name}</h3>
              <p className="text-blue-600 font-semibold">{doctor.specialization}</p>
              <p className="text-gray-600">{doctor.hospital}</p>
            </div>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {errors.submit && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
              {errors.submit}
            </div>
          )}

          {/* Patient Information */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-gray-800 border-b border-gray-200 pb-2">
              Patient Information
            </h4>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Full Name *
                </label>
                <input
                  type="text"
                  value={formData.patientName}
                  onChange={(e) => handleInputChange('patientName', e.target.value)}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.patientName ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="Enter your full name"
                />
                {errors.patientName && (
                  <p className="text-red-500 text-sm mt-1">{errors.patientName}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email Address *
                </label>
                <input
                  type="email"
                  value={formData.patientEmail}
                  onChange={(e) => handleInputChange('patientEmail', e.target.value)}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.patientEmail ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="Enter your email"
                />
                {errors.patientEmail && (
                  <p className="text-red-500 text-sm mt-1">{errors.patientEmail}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Phone Number *
                </label>
                <input
                  type="tel"
                  value={formData.patientPhone}
                  onChange={(e) => handleInputChange('patientPhone', e.target.value)}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.patientPhone ? 'border-red-500' : 'border-gray-300'
                  }`}
                  placeholder="Enter your phone number"
                />
                {errors.patientPhone && (
                  <p className="text-red-500 text-sm mt-1">{errors.patientPhone}</p>
                )}
              </div>
            </div>
          </div>

          {/* Appointment Details */}
          <div className="space-y-4">
            <h4 className="text-lg font-semibold text-gray-800 border-b border-gray-200 pb-2">
              Appointment Details
            </h4>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Preferred Date *
                </label>
                <input
                  type="date"
                  value={formData.appointmentDate}
                  onChange={(e) => handleInputChange('appointmentDate', e.target.value)}
                  min={getMinDate()}
                  max={getMaxDate()}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.appointmentDate ? 'border-red-500' : 'border-gray-300'
                  }`}
                />
                {errors.appointmentDate && (
                  <p className="text-red-500 text-sm mt-1">{errors.appointmentDate}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Time Slot *
                </label>
                <select
                  value={formData.appointmentTime}
                  onChange={(e) => handleInputChange('appointmentTime', e.target.value)}
                  className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                    errors.appointmentTime ? 'border-red-500' : 'border-gray-300'
                  }`}
                  disabled={!formData.appointmentDate}
                >
                  <option value="">Select a time slot</option>
                  {availableSlots.map((slot, index) => (
                    <option key={index} value={slot.time}>
                      {slot.time}
                    </option>
                  ))}
                </select>
                {errors.appointmentTime && (
                  <p className="text-red-500 text-sm mt-1">{errors.appointmentTime}</p>
                )}
                {formData.appointmentDate && availableSlots.length === 0 && (
                  <p className="text-orange-500 text-sm mt-1">
                    No available slots for this date. Please select another date.
                  </p>
                )}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Reason for Appointment *
              </label>
              <textarea
                value={formData.reason}
                onChange={(e) => handleInputChange('reason', e.target.value)}
                rows={3}
                className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                  errors.reason ? 'border-red-500' : 'border-gray-300'
                }`}
                placeholder="Please describe your symptoms or reason for the appointment"
              />
              {errors.reason && (
                <p className="text-red-500 text-sm mt-1">{errors.reason}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Additional Notes (Optional)
              </label>
              <textarea
                value={formData.notes}
                onChange={(e) => handleInputChange('notes', e.target.value)}
                rows={2}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                placeholder="Any additional information you'd like to share"
              />
            </div>
          </div>

          {/* Fee Information */}
          <div className="bg-blue-50 rounded-lg p-4">
            <div className="flex items-center justify-between">
              <span className="text-lg font-semibold text-gray-800">Consultation Fee:</span>
              <span className="text-2xl font-bold text-blue-600">Rs. {doctor.fee.toLocaleString()}</span>
            </div>
            <p className="text-sm text-gray-600 mt-1">
              Payment will be collected at the hospital on the day of your appointment.
            </p>
          </div>

          {/* Action Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-lg hover:from-blue-700 hover:to-indigo-700 transition-all duration-200 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Booking...
                </div>
              ) : (
                'Book Appointment'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AppointmentForm;
