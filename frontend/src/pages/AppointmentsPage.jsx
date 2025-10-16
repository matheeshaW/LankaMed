import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getRole, getCurrentUser } from '../utils/auth';
import { getAppointmentsByPatientId, mockDoctors } from '../data/mockData';
import AppointmentForm from '../components/patient/AppointmentForm';
import ReviewSection from '../components/patient/ReviewSection';

const AppointmentsPage = () => {
  const [activeTab, setActiveTab] = useState('doctors');
  const [appointments, setAppointments] = useState([]);
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [successMessage, setSuccessMessage] = useState('');
  const navigate = useNavigate();
  const currentUser = getCurrentUser();

  useEffect(() => {
    const role = getRole();
    if (role !== 'PATIENT') {
      navigate('/login');
    }
  }, [navigate]);

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = () => {
    const userAppointments = getAppointmentsByPatientId(currentUser?.id || 1);
    setAppointments(userAppointments);
  };

  const handleBookAppointment = (doctor) => {
    setSelectedDoctor(doctor);
    setShowBookingModal(true);
  };

  const handleBookingSuccess = (message) => {
    setSuccessMessage(message);
    setTimeout(() => setSuccessMessage(''), 5000);
    setShowBookingModal(false);
    setSelectedDoctor(null);
    loadAppointments(); // Refresh appointments
  };

  const handleAddReview = (appointment) => {
    setSelectedAppointment(appointment);
    setShowReviewModal(true);
  };

  const handleReviewSubmitted = () => {
    setShowReviewModal(false);
    setSelectedAppointment(null);
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

  const getUpcomingAppointments = () => {
    const today = new Date();
    return appointments.filter(apt => {
      const appointmentDate = new Date(apt.appointmentDate);
      return appointmentDate >= today && (apt.status === 'APPROVED' || apt.status === 'CONFIRMED');
    });
  };

  const upcomingAppointments = getUpcomingAppointments();

  const tabs = [
    { id: 'doctors', label: 'Find Doctors', icon: '🔍' },
    { id: 'appointments', label: 'My Appointments', icon: '📅' }
  ];

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(<span key={i} className="text-yellow-400">★</span>);
    }
    if (hasHalfStar) {
      stars.push(<span key="half" className="text-yellow-400">☆</span>);
    }
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(<span key={`empty-${i}`} className="text-gray-300">★</span>);
    }
    return stars;
  };

  const renderDoctorsTab = () => (
    <div className="space-y-6">
      {/* Search Section */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white">
        <div className="text-center mb-8">
          <h2 className="text-4xl font-bold mb-4">Find Your Perfect Doctor</h2>
          <p className="text-blue-100 text-lg">Search from our network of experienced specialists</p>
        </div>
      </div>

      {/* Doctors Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {mockDoctors.map((doctor) => (
          <div key={doctor.id} className="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 overflow-hidden">
            {/* Doctor Header */}
            <div className="bg-gradient-to-br from-blue-50 to-indigo-50 p-6">
              <div className="flex items-start space-x-4">
                <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-3xl shadow-lg">
                  {doctor.image}
                </div>
                <div className="flex-1">
                  <h4 className="text-xl font-bold text-gray-800 mb-1">
                    {doctor.name}
                  </h4>
                  <p className="text-blue-600 font-semibold text-lg mb-2">
                    {doctor.specialization}
                  </p>
                  <div className="flex items-center space-x-2">
                    <div className="flex">
                      {renderStars(doctor.rating)}
                    </div>
                    <span className="text-sm text-gray-600 font-medium">
                      {doctor.rating} ({doctor.reviewCount} reviews)
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Doctor Details */}
            <div className="p-6 space-y-4">
              {/* Experience and Fee */}
              <div className="grid grid-cols-2 gap-4">
                <div className="text-center p-3 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-gray-800">{doctor.experience}</div>
                  <div className="text-sm text-gray-600">Years Experience</div>
                </div>
                <div className="text-center p-3 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">Rs. {doctor.fee.toLocaleString()}</div>
                  <div className="text-sm text-gray-600">Consultation Fee</div>
                </div>
              </div>

              {/* Hospital Info */}
              <div className="space-y-2">
                <div className="flex items-start space-x-2">
                  <svg className="h-5 w-5 text-gray-400 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                  <div>
                    <div className="font-medium text-gray-800">{doctor.hospital}</div>
                    <div className="text-sm text-gray-600">{doctor.hospitalAddress}</div>
                    <div className="text-sm text-gray-500">{doctor.hospitalContact}</div>
                  </div>
                </div>
              </div>

              {/* Book Now Button */}
              <button
                onClick={() => handleBookAppointment(doctor)}
                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-4 px-6 rounded-xl font-bold text-lg hover:from-blue-700 hover:to-indigo-700 transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl"
              >
                Book Appointment
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );

  const renderAppointmentsTab = () => (
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
        <h3 className="text-xl font-bold text-gray-800 mb-6">All Appointments</h3>
        
        {appointments.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">No appointments found</h3>
            <p className="text-gray-500">You haven't booked any appointments yet.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {appointments.map((appointment) => (
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
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-xl shadow-lg overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8">
            <h1 className="text-3xl font-bold text-white mb-2">Appointments</h1>
            <p className="text-blue-100">Find doctors and manage your appointments</p>
          </div>

          {/* Tab Navigation */}
          <div className="border-b border-gray-200">
            <nav className="flex space-x-8 px-6" aria-label="Tabs">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  } whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm flex items-center space-x-2`}
                >
                  <span className="text-lg">{tab.icon}</span>
                  <span>{tab.label}</span>
                </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          <div className="p-6">
            {successMessage && (
              <div className="mb-6 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
                {successMessage}
              </div>
            )}
            {activeTab === 'doctors' && renderDoctorsTab()}
            {activeTab === 'appointments' && renderAppointmentsTab()}
          </div>
        </div>
      </div>

      {/* Booking Modal */}
      {showBookingModal && selectedDoctor && (
        <AppointmentForm
          doctor={selectedDoctor}
          onClose={() => setShowBookingModal(false)}
          onSuccess={handleBookingSuccess}
        />
      )}

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

export default AppointmentsPage;

