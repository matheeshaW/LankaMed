import React, { useState, useEffect } from 'react';
import { getAppointmentsByPatientId } from '../../data/mockData';
import { getCurrentUser } from '../../utils/auth';
import ReviewSection from './ReviewSection';

const UserAppointments = () => {
  const currentUser = getCurrentUser();
  const [appointments, setAppointments] = useState([]);
  const [filteredAppointments, setFilteredAppointments] = useState([]);
  const [filter, setFilter] = useState('all');
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [showReviewModal, setShowReviewModal] = useState(false);

  useEffect(() => {
    loadAppointments();
  }, []);

  useEffect(() => {
    filterAppointments();
  }, [appointments, filter]);

  const loadAppointments = () => {
    const userAppointments = getAppointmentsByPatientId(currentUser?.id || 1);
    setAppointments(userAppointments);
  };

  const filterAppointments = () => {
    if (filter === 'all') {
      setFilteredAppointments(appointments);
    } else {
      setFilteredAppointments(appointments.filter(apt => apt.status === filter));
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'APPROVED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'CONFIRMED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'REJECTED':
        return 'bg-red-100 text-red-800 border-red-200';
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
        return 'Pending Approval';
      case 'APPROVED':
        return 'Approved';
      case 'CONFIRMED':
        return 'Confirmed';
      case 'REJECTED':
        return 'Rejected';
      case 'COMPLETED':
        return 'Completed';
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
      case 'REJECTED':
        return '❌';
      case 'COMPLETED':
        return '🎉';
      case 'CANCELLED':
        return '🚫';
      default:
        return '📋';
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatTime = (timeString) => {
    return new Date(`2000-01-01T${timeString}`).toLocaleTimeString('en-US', {
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

  const handleReviewSubmitted = () => {
    setShowReviewModal(false);
    setSelectedAppointment(null);
    // You could refresh the appointments here if needed
  };

  const getUpcomingAppointments = () => {
    const today = new Date();
    return appointments.filter(apt => {
      const appointmentDate = new Date(apt.appointmentDate);
      return appointmentDate >= today && (apt.status === 'APPROVED' || apt.status === 'CONFIRMED');
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
            {appointments.filter(apt => apt.status === 'PENDING').length}
          </div>
          <div className="text-gray-600">Pending</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-emerald-600 mb-2">
            {appointments.filter(apt => apt.status === 'COMPLETED').length}
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
                        <span className="font-medium">Date:</span> {formatDate(appointment.appointmentDate)}
                      </div>
                      <div>
                        <span className="font-medium">Time:</span> {formatTime(appointment.appointmentTime)}
                      </div>
                      <div>
                        <span className="font-medium">Hospital:</span> {appointment.hospitalName}
                      </div>
                    </div>
                    <div className="mt-3">
                      <span className="font-medium text-gray-700">Reason:</span>
                      <p className="text-gray-600 mt-1">{appointment.reason}</p>
                    </div>
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
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="COMPLETED">Completed</option>
              <option value="REJECTED">Rejected</option>
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
                        <div className="font-semibold text-gray-800">{formatDate(appointment.appointmentDate)}</div>
                      </div>
                      <div className="bg-gray-50 rounded-lg p-3">
                        <div className="text-sm text-gray-600 mb-1">Time</div>
                        <div className="font-semibold text-gray-800">{formatTime(appointment.appointmentTime)}</div>
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
                        <div className="font-semibold text-gray-800">#{appointment.id}</div>
                      </div>
                    </div>

                    <div className="mb-4">
                      <div className="text-sm text-gray-600 mb-1">Reason for Appointment</div>
                      <p className="text-gray-800">{appointment.reason}</p>
                    </div>

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
    </div>
  );
};

export default UserAppointments;
