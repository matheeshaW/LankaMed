import React, { useState, useEffect, useCallback } from 'react';
import { appointmentAPI } from '../../services/api';
import ReviewSection from './ReviewSection';
import api from '../../services/api';

const UserAppointments = () => {
  const [appointments, setAppointments] = useState([]);
  const [filteredAppointments, setFilteredAppointments] = useState([]);
  const [filter, setFilter] = useState('all');
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showReschedule, setShowReschedule] = useState(false);
  const [rescheduleDate, setRescheduleDate] = useState('');
  const [rescheduleTime, setRescheduleTime] = useState('');
  const [waitlistEntries, setWaitlistEntries] = useState([]);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [selectedWaitlistEntry, setSelectedWaitlistEntry] = useState(null);

  useEffect(() => {
    loadAppointments();
    loadWaitlistEntries();
  }, []);

  useEffect(() => {
    filterAppointments();
  }, [filterAppointments]);

  const loadAppointments = async () => {
    try {
      const response = await appointmentAPI.getPatientAppointments();
      const data = response.data;
      if (Array.isArray(data)) {
        // /api/patients/me/appointments returns a plain array of AppointmentDto
        setAppointments(data);
      } else if (data && data.success && Array.isArray(data.appointments)) {
        // fallback shape if backend returns wrapped payload
        setAppointments(data.appointments);
      } else {
        console.error('Error loading appointments: unexpected response shape', data);
        setAppointments([]);
      }
    } catch (error) {
      console.error('Error loading appointments:', error);
      setAppointments([]);
    }
  };

  const loadWaitlistEntries = async () => {
    try {
      const response = await api.get('/api/patients/me/waitlist');
      if (Array.isArray(response.data)) {
        setWaitlistEntries(response.data);
      } else {
        console.error('Error loading waitlist entries: unexpected response shape', response.data);
        setWaitlistEntries([]);
      }
    } catch (error) {
      console.error('Error loading waitlist entries:', error);
      setWaitlistEntries([]);
    }
  };

  const filterAppointments = useCallback(() => {
    if (!Array.isArray(appointments)) {
      setFilteredAppointments([]);
      return;
    }
    if (filter === 'all') {
      setFilteredAppointments(appointments);
    } else {
      setFilteredAppointments(appointments.filter(apt => apt.status === filter));
    }
  }, [appointments, filter]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'APPROVED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'COMPLETED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'PENDING':
        return 'Pending';
      case 'APPROVED':
        return 'Approved';
      case 'CONFIRMED':
        return 'Confirmed';
      case 'COMPLETED':
        return 'Completed';
      case 'REJECTED':
        return 'Rejected';
      case 'CANCELLED':
        return 'Cancelled';
      default:
        return status;
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING':
        return '⏳';
      case 'APPROVED':
        return '✅';
      case 'CONFIRMED':
        return '📅';
      case 'COMPLETED':
        return '🎉';
      case 'REJECTED':
        return '❌';
      case 'CANCELLED':
        return '🚫';
      default:
        return '📋';
    }
  };

  const formatDate = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const canAddReview = (appointment) => {
    return appointment.status === 'COMPLETED';
  };

  const handleAddReview = (appointment) => {
    setSelectedAppointment(appointment);
    setShowReviewModal(true);
  };

  const canReschedule = (appointment) => appointment.status === 'CONFIRMED';

  const openReschedule = (appointment) => {
    setSelectedAppointment(appointment);
    const dt = appointment.appointmentDateTime ? new Date(appointment.appointmentDateTime) : null;
    if (dt) {
      setRescheduleDate(dt.toISOString().slice(0,10));
      const hh = String(dt.getHours()).padStart(2,'0');
      const mm = String(dt.getMinutes()).padStart(2,'0');
      setRescheduleTime(`${hh}:${mm}`);
    }
    setShowReschedule(true);
  };

  const submitReschedule = async () => {
    if (!selectedAppointment) return;
    try {
      const iso = `${rescheduleDate}T${rescheduleTime}:00`;
      await appointmentAPI.updateAppointment(selectedAppointment.appointmentId, { appointmentDateTime: iso });
      setAppointments(prev => prev.map(apt => (
        apt.appointmentId === selectedAppointment.appointmentId
          ? { ...apt, appointmentDateTime: iso }
          : apt
      )));
      setShowReschedule(false);
      setSelectedAppointment(null);
    } catch (e) {
      console.error('Failed to reschedule', e);
    }
  };

  const handleReviewSubmitted = () => {
    setShowReviewModal(false);
    setSelectedAppointment(null);
    // You could refresh the appointments here if needed
  };

  const handleCancelAppointment = async (appointment) => {
    if (!appointment) return;
    const ok = window.confirm('Are you sure you want to cancel this appointment?');
    if (!ok) return;
    try {
      const res = await appointmentAPI.updateAppointmentStatus(appointment.appointmentId, 'CANCELLED');
      const payload = res?.data || {};
      const next = (payload.status || 'CANCELLED').toString().toUpperCase();
      setAppointments(prev => prev.map(a => (
        a.appointmentId === appointment.appointmentId ? { ...a, status: next } : a
      )));
      alert(`Appointment #${appointment.appointmentId} cancelled. Doctor and staff have been notified.`);
    } catch (e) {
      console.error('Failed to cancel appointment', e);
      alert('Failed to cancel appointment. Please try again.');
    }
  };

  const handleBookFromWaitlist = (waitlistEntry) => {
    setSelectedWaitlistEntry(waitlistEntry);
    setShowBookingModal(true);
  };

  const handleBookingSubmit = async (bookingData) => {
    if (!selectedWaitlistEntry) return;
    
    try {
      // Create appointment from waitlist entry
      const appointmentData = {
        doctorId: selectedWaitlistEntry.doctorId,
        hospitalId: 1, // Dummy hospital ID
        serviceCategoryId: 1, // Dummy service category ID
        appointmentDateTime: bookingData.appointmentDateTime,
        reason: bookingData.reason || 'Follow-up appointment from waitlist',
        priority: selectedWaitlistEntry.priority || false
      };

      const response = await appointmentAPI.createAppointment(appointmentData);
      
      if (response.data) {
        // Remove from waitlist and add to appointments
        setWaitlistEntries(prev => prev.filter(entry => entry.id !== selectedWaitlistEntry.id));
        setAppointments(prev => [...prev, response.data]);
        setShowBookingModal(false);
        setSelectedWaitlistEntry(null);
        alert('Appointment booked successfully!');
      }
    } catch (error) {
      console.error('Failed to book appointment from waitlist:', error);
      alert('Failed to book appointment. Please try again.');
    }
  };

  const getUpcomingAppointments = () => {
    if (!Array.isArray(appointments)) {
      return [];
    }
    const now = new Date();
    const allowed = ['APPROVED', 'CONFIRMED', 'PENDING'];
    return appointments.filter(apt => {
      const dtString = apt.appointmentDateTime || (apt.appointmentDate && apt.appointmentTime ? `${apt.appointmentDate}T${apt.appointmentTime}` : apt.appointmentDate);
      const appointmentDate = new Date(dtString);
      if (Number.isNaN(appointmentDate.getTime())) return false;
      return appointmentDate >= now && allowed.includes(String(apt.status));
    });
  };

  const upcomingAppointments = getUpcomingAppointments();

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white">
        <h2 className="text-3xl font-bold mb-4">My Appointments</h2>
        <p className="text-blue-100 text-lg">Manage and track your healthcare appointments</p>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-blue-600 mb-2">{appointments.length}</div>
          <div className="text-gray-600">Total Appointments</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-green-600 mb-2">{upcomingAppointments.length}</div>
          <div className="text-gray-600">Upcoming</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-yellow-600 mb-2">
            {Array.isArray(appointments) ? appointments.filter(apt => ['APPROVED','CONFIRMED','PENDING'].includes(String(apt.status))).length : 0}
          </div>
          <div className="text-gray-600">Pending</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-emerald-600 mb-2">
            {Array.isArray(appointments) ? appointments.filter(apt => apt.status === 'COMPLETED').length : 0}
          </div>
          <div className="text-gray-600">Completed</div>
        </div>
      </div>

      {/* Upcoming Appointments */}
      {upcomingAppointments.length > 0 && (
        <div className="bg-white rounded-2xl shadow-lg p-6">
          <h3 className="text-xl font-bold text-gray-800 mb-6 flex items-center">
            <span className="text-2xl mr-3">📅</span>
            Upcoming Appointments
          </h3>
          <div className="space-y-4">
            {upcomingAppointments.map((appointment) => (
              <div key={appointment.id} className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow duration-200">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-4 mb-3">
                      <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-xl">👨‍⚕️</span>
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-gray-800">{appointment.doctorName}</h4>
                        <p className="text-blue-600 font-medium">{appointment.doctorSpecialization}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
                      <div>
                        <span className="font-medium">Date:</span> {formatDate(appointment.appointmentDateTime)}
                      </div>
                      <div>
                        <span className="font-medium">Time:</span> {formatTime(appointment.appointmentDateTime)}
                      </div>
                      <div>
                        <span className="font-medium">Hospital:</span> {appointment.hospitalName}
                      </div>
                    </div>
                    <div className="mt-3">
                      <span className="font-medium text-gray-700">Reason:</span>
                      <p className="text-gray-600 mt-1">{appointment.reason}</p>
                    </div>
                    
                    {['PENDING','APPROVED','CONFIRMED','pending','approved','confirmed'].includes(String(appointment.status)) && (
                      <div className="mt-3">
                        <button
                          onClick={() => handleCancelAppointment(appointment)}
                          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors duration-200 font-medium"
                        >
                          Cancel Appointment
                        </button>
                      </div>
                    )}
                  </div>
                  <div className="text-right">
                    <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(appointment.status)}`}>
                      <span className="mr-1">{getStatusIcon(appointment.status)}</span>
                      {getStatusText(appointment.status)}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Waiting List */}
      {waitlistEntries.length > 0 && (
        <div className="bg-white rounded-2xl shadow-lg p-6">
          <h3 className="text-xl font-bold text-gray-800 mb-6 flex items-center">
            <span className="text-2xl mr-3">⏳</span>
            Waiting List
          </h3>
          <div className="space-y-4">
            {waitlistEntries.map((entry) => (
              <div key={entry.id} className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow duration-200">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-4 mb-3">
                      <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                        <span className="text-xl">👨‍⚕️</span>
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-gray-800">{entry.doctorName}</h4>
                        <p className="text-blue-600 font-medium">{entry.doctorSpecialization}</p>
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
                      <div>
                        <span className="font-medium">Requested Date:</span> {formatDate(entry.desiredDateTime)}
                      </div>
                      <div>
                        <span className="font-medium">Time:</span> {formatTime(entry.desiredDateTime)}
                      </div>
                      <div>
                        <span className="font-medium">Hospital:</span> {entry.hospitalName}
                      </div>
                    </div>
                    <div className="mt-3">
                      <span className="font-medium text-gray-700">Service:</span>
                      <p className="text-gray-600 mt-1">{entry.serviceCategoryName}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${
                      entry.status === 'QUEUED' ? 'bg-yellow-100 text-yellow-800 border-yellow-200' :
                      entry.status === 'NOTIFIED' ? 'bg-blue-100 text-blue-800 border-blue-200' :
                      entry.status === 'APPROVED' ? 'bg-green-100 text-green-800 border-green-200' :
                      'bg-gray-100 text-gray-800 border-gray-200'
                    }`}>
                      <span className="mr-1">
                        {entry.status === 'QUEUED' ? '⏳' :
                         entry.status === 'NOTIFIED' ? '🔔' :
                         entry.status === 'APPROVED' ? '✅' : '📋'}
                      </span>
                      {entry.status}
                    </div>
                    {entry.status === 'APPROVED' && (
                      <div className="mt-3">
                        <button
                          onClick={() => handleBookFromWaitlist(entry)}
                          className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200 font-medium"
                        >
                          Book Now
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* All Appointments */}
      <div className="bg-white rounded-2xl shadow-lg p-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
          <h3 className="text-xl font-bold text-gray-800 mb-4 sm:mb-0">All Appointments</h3>
          <div className="flex items-center space-x-4">
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value="SCHEDULED">Scheduled</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
            <button
              onClick={loadAppointments}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200"
            >
              Refresh
            </button>
          </div>
        </div>

        {filteredAppointments.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">No appointments found</h3>
            <p className="text-gray-500">
              {filter === 'all' 
                ? "You haven't booked any appointments yet." 
                : `No appointments with status "${filter.toLowerCase()}".`
              }
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredAppointments.map((appointment) => (
              <div key={appointment.id} className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-all duration-200">
                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between">
                  <div className="flex-1">
                    <div className="flex items-start space-x-4 mb-4">
                      <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-2xl">
                        👨‍⚕️
                      </div>
                      <div className="flex-1">
                        <h4 className="text-xl font-semibold text-gray-800 mb-1">{appointment.doctorName}</h4>
                        <p className="text-blue-600 font-medium mb-2">{appointment.doctorSpecialization}</p>
                        <p className="text-gray-600 text-sm">{appointment.hospitalName}</p>
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
                      <div className="bg-gray-50 rounded-lg p-3">
                        <div className="text-sm text-gray-600 mb-1">Date</div>
                        <div className="font-semibold text-gray-800">{formatDate(appointment.appointmentDateTime)}</div>
                      </div>
                      <div className="bg-gray-50 rounded-lg p-3">
                        <div className="text-sm text-gray-600 mb-1">Time</div>
                        <div className="font-semibold text-gray-800">{formatTime(appointment.appointmentDateTime)}</div>
                      </div>
                      <div className="bg-gray-50 rounded-lg p-3">
                        <div className="text-sm text-gray-600 mb-1">Status</div>
                        <div className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(appointment.status)}`}>
                          <span className="mr-1">{getStatusIcon(appointment.status)}</span>
                          {getStatusText(appointment.status)}
                        </div>
                      </div>
                      <div className="bg-gray-50 rounded-lg p-3">
                        <div className="text-sm text-gray-600 mb-1">Appointment ID</div>
                        <div className="font-semibold text-gray-800">#{appointment.appointmentId}</div>
                      </div>
                    </div>

                    <div className="mb-4">
                      <div className="text-sm text-gray-600 mb-1">Reason for Appointment</div>
                      <p className="text-gray-800">{appointment.reason}</p>
                    </div>

                    {['PENDING','APPROVED','CONFIRMED','pending','approved','confirmed'].includes(String(appointment.status)) && (
                      <div className="mb-4">
                        <button
                          onClick={() => handleCancelAppointment(appointment)}
                          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors duration-200 font-medium"
                        >
                          Cancel Appointment
                        </button>
                      </div>
                    )}

                    {appointment.notes && (
                      <div className="mb-4">
                        <div className="text-sm text-gray-600 mb-1">Additional Notes</div>
                        <p className="text-gray-800">{appointment.notes}</p>
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col space-y-2 lg:ml-6">
                    {canAddReview(appointment) && (
                      <button
                        onClick={() => handleAddReview(appointment)}
                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200 font-medium"
                      >
                        Add Review
                      </button>
                    )}
                    {canReschedule(appointment) && (
                      <button
                        onClick={() => openReschedule(appointment)}
                        className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors duration-200 font-medium"
                      >
                        Reschedule
                      </button>
                    )}
                    <div className="text-xs text-gray-500 text-right">
                      Created: {new Date(appointment.createdAt).toLocaleDateString()}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Review Modal */}
      {showReviewModal && selectedAppointment && (
        <ReviewSection
          appointment={selectedAppointment}
          onClose={() => setShowReviewModal(false)}
          onReviewSubmitted={handleReviewSubmitted}
        />
      )}

      {showReschedule && selectedAppointment && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <h3 className="text-xl font-bold mb-4">Reschedule Appointment #{selectedAppointment.appointmentId}</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
                <input type="date" value={rescheduleDate} onChange={(e)=>setRescheduleDate(e.target.value)} className="w-full border rounded px-3 py-2" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Time</label>
                <input type="time" value={rescheduleTime} onChange={(e)=>setRescheduleTime(e.target.value)} className="w-full border rounded px-3 py-2" />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button onClick={()=>setShowReschedule(false)} className="px-4 py-2 border rounded">Cancel</button>
                <button onClick={submitReschedule} className="px-4 py-2 bg-indigo-600 text-white rounded">Save</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Booking Modal for Waitlist */}
      {showBookingModal && selectedWaitlistEntry && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <h3 className="text-xl font-bold mb-4">Book Appointment - {selectedWaitlistEntry.doctorName}</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Date & Time</label>
                <input 
                  type="datetime-local" 
                  id="appointmentDateTime"
                  className="w-full border rounded px-3 py-2" 
                  min={new Date().toISOString().slice(0, 16)}
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Reason for Appointment</label>
                <textarea 
                  id="reason"
                  className="w-full border rounded px-3 py-2" 
                  rows="3"
                  placeholder="Please describe your symptoms or reason for the appointment"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button 
                  onClick={() => setShowBookingModal(false)} 
                  className="px-4 py-2 border rounded hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button 
                  onClick={() => {
                    const dateTime = document.getElementById('appointmentDateTime').value;
                    const reason = document.getElementById('reason').value;
                    if (dateTime) {
                      handleBookingSubmit({
                        appointmentDateTime: new Date(dateTime).toISOString(),
                        reason: reason
                      });
                    } else {
                      alert('Please select a date and time');
                    }
                  }} 
                  className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  Book Appointment
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserAppointments;
