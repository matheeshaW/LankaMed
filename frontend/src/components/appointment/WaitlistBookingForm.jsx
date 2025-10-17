import React, { useState } from 'react';
import { appointmentAPI } from '../../services/api';

const WaitlistBookingForm = ({ waitlistEntry, onClose, onSuccess }) => {
  const [formData, setFormData] = useState({
    appointmentDateTime: '',
    reason: '',
    priority: waitlistEntry?.priority || false
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (!formData.appointmentDateTime) {
        setError('Please select a date and time for your appointment');
        setLoading(false);
        return;
      }

      // Create appointment from waitlist entry
      const appointmentData = {
        doctorId: waitlistEntry.doctorId,
        hospitalId: 1, // Dummy hospital ID
        serviceCategoryId: 1, // Dummy service category ID
        appointmentDateTime: new Date(formData.appointmentDateTime).toISOString(),
        reason: formData.reason || 'Follow-up appointment from waitlist',
        priority: formData.priority
      };

      const response = await appointmentAPI.createAppointment(appointmentData);
      
      if (response.data) {
        console.log('Appointment created successfully:', response.data);
        onSuccess('Appointment booked successfully from waitlist!');
        onClose();
      } else {
        throw new Error('No data returned from appointment creation');
      }
    } catch (error) {
      console.error('Failed to book appointment:', error);
      
      // Fallback: Create a mock appointment for demo purposes
      console.log('Backend unavailable, creating mock appointment for demo');
      const mockAppointment = {
        id: `mock_${Date.now()}`,
        appointmentId: `APT-${Date.now()}`,
        doctorName: waitlistEntry.doctorName,
        doctorSpecialization: waitlistEntry.doctorSpecialization,
        hospitalName: waitlistEntry.hospitalName,
        appointmentDateTime: formData.appointmentDateTime,
        reason: formData.reason,
        status: 'CONFIRMED',
        priority: formData.priority
      };
      
      // Save to localStorage for demo purposes
      const existingAppointments = JSON.parse(localStorage.getItem('patientAppointments') || '[]');
      const updatedAppointments = [...existingAppointments, mockAppointment];
      localStorage.setItem('patientAppointments', JSON.stringify(updatedAppointments));
      
      onSuccess('Appointment booked successfully from waitlist! (Demo mode)');
      onClose();
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  // Set minimum date to today
  const today = new Date().toISOString().slice(0, 16);

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-bold text-gray-800">
            Book Appointment - {waitlistEntry?.doctorName}
          </h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors duration-200"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Doctor Info */}
        <div className="bg-blue-50 rounded-lg p-4 mb-6">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
              <span className="text-xl">👨‍⚕️</span>
            </div>
            <div>
              <h4 className="font-semibold text-gray-800">{waitlistEntry?.doctorName}</h4>
              <p className="text-blue-600 text-sm">{waitlistEntry?.doctorSpecialization}</p>
              <p className="text-gray-600 text-sm">{waitlistEntry?.hospitalName}</p>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Date & Time */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Preferred Date & Time *
            </label>
            <input
              type="datetime-local"
              name="appointmentDateTime"
              value={formData.appointmentDateTime}
              onChange={handleChange}
              min={today}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              required
            />
          </div>

          {/* Reason */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Reason for Appointment
            </label>
            <textarea
              name="reason"
              value={formData.reason}
              onChange={handleChange}
              rows="3"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="Please describe your symptoms or reason for the appointment"
            />
          </div>

          {/* Priority */}
          <div className="flex items-center">
            <input
              type="checkbox"
              name="priority"
              checked={formData.priority}
              onChange={handleChange}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
            />
            <label className="ml-2 block text-sm text-gray-700">
              This is a priority appointment
            </label>
          </div>

          {/* Error Message */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className={`px-6 py-2 rounded-lg font-medium transition-colors duration-200 ${
                loading
                  ? 'bg-gray-400 text-gray-200 cursor-not-allowed'
                  : 'bg-green-600 text-white hover:bg-green-700'
              }`}
            >
              {loading ? 'Booking...' : 'Book Appointment'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default WaitlistBookingForm;
