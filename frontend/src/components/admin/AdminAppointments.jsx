import React, { useState, useEffect } from 'react';
import { mockAppointments, updateAppointmentStatus } from '../../data/mockData';

const AdminAppointments = () => {
  const [appointments, setAppointments] = useState(mockAppointments);
  const [filteredAppointments, setFilteredAppointments] = useState(mockAppointments);
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('date');
  const [sortOrder, setSortOrder] = useState('desc');
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    filterAndSortAppointments();
  }, [appointments, filter, searchTerm, sortBy, sortOrder]);

  const filterAndSortAppointments = () => {
    let filtered = [...appointments];

    // Filter by status
    if (filter !== 'all') {
      filtered = filtered.filter(appointment => appointment.status === filter);
    }

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(appointment =>
        appointment.patientName.toLowerCase().includes(term) ||
        appointment.doctorName.toLowerCase().includes(term) ||
        appointment.doctorSpecialization.toLowerCase().includes(term) ||
        appointment.hospitalName.toLowerCase().includes(term) ||
        appointment.reason.toLowerCase().includes(term)
      );
    }

    // Sort appointments
    filtered.sort((a, b) => {
      let aValue, bValue;
      
      switch (sortBy) {
        case 'date':
          aValue = new Date(a.appointmentDate);
          bValue = new Date(b.appointmentDate);
          break;
        case 'patient':
          aValue = a.patientName.toLowerCase();
          bValue = b.patientName.toLowerCase();
          break;
        case 'doctor':
          aValue = a.doctorName.toLowerCase();
          bValue = b.doctorName.toLowerCase();
          break;
        case 'status':
          aValue = a.status;
          bValue = b.status;
          break;
        default:
          return 0;
      }

      if (sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    setFilteredAppointments(filtered);
  };

  const handleStatusUpdate = (appointmentId, newStatus) => {
    try {
      const updatedAppointment = updateAppointmentStatus(appointmentId, newStatus);
      if (updatedAppointment) {
        setAppointments(prev => 
          prev.map(apt => 
            apt.id === appointmentId ? updatedAppointment : apt
          )
        );
        setSuccessMessage(`Appointment #${appointmentId} status updated to ${newStatus}`);
        setTimeout(() => setSuccessMessage(''), 3000);
      }
    } catch (error) {
      console.error('Error updating appointment status:', error);
      setErrorMessage('Failed to update appointment status');
      setTimeout(() => setErrorMessage(''), 3000);
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
        return 'Pending';
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
      weekday: 'short',
      year: 'numeric',
      month: 'short',
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

  const getStatusCounts = () => {
    const counts = {
      PENDING: 0,
      APPROVED: 0,
      CONFIRMED: 0,
      REJECTED: 0,
      COMPLETED: 0,
      CANCELLED: 0
    };

    appointments.forEach(appointment => {
      counts[appointment.status] = (counts[appointment.status] || 0) + 1;
    });

    return counts;
  };

  const statusCounts = getStatusCounts();

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-2xl p-8 text-white">
        <h1 className="text-3xl font-bold mb-4">Appointment Management</h1>
        <p className="text-purple-100 text-lg">Manage and monitor all patient appointments</p>
      </div>

      {/* Success/Error Messages */}
      {successMessage && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
          {successMessage}
        </div>
      )}
      {errorMessage && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {errorMessage}
        </div>
      )}

      {/* Statistics Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {Object.entries(statusCounts).map(([status, count]) => (
          <div key={status} className="bg-white rounded-xl shadow-lg p-6 text-center">
            <div className={`text-3xl font-bold mb-2 ${getStatusColor(status).split(' ')[1]}`}>
              {count}
            </div>
            <div className="text-sm text-gray-600">{getStatusText(status)}</div>
          </div>
        ))}
      </div>

      {/* Filters and Search */}
      <div className="bg-white rounded-2xl shadow-lg p-6">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between space-y-4 lg:space-y-0">
          <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
            {/* Search */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <input
                type="text"
                placeholder="Search appointments..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent w-full sm:w-64"
              />
            </div>

            {/* Status Filter */}
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved</option>
              <option value="CONFIRMED">Confirmed</option>
              <option value="REJECTED">Rejected</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>

          <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-4">
            {/* Sort Options */}
            <div className="flex space-x-2">
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-sm"
              >
                <option value="date">Date</option>
                <option value="patient">Patient</option>
                <option value="doctor">Doctor</option>
                <option value="status">Status</option>
              </select>
              <select
                value={sortOrder}
                onChange={(e) => setSortOrder(e.target.value)}
                className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-sm"
              >
                <option value="desc">Desc</option>
                <option value="asc">Asc</option>
              </select>
            </div>

            <button
              onClick={() => {
                setAppointments([...mockAppointments]);
                setSearchTerm('');
                setFilter('all');
              }}
              className="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors duration-200"
            >
              Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Appointments Table */}
      <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-800">
            Appointments ({filteredAppointments.length})
          </h3>
        </div>

        {filteredAppointments.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">No appointments found</h3>
            <p className="text-gray-500">
              {searchTerm || filter !== 'all' 
                ? 'Try adjusting your search criteria or filters.' 
                : 'No appointments have been booked yet.'
              }
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Patient
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Doctor
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date & Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Hospital
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredAppointments.map((appointment) => (
                  <tr key={appointment.id} className="hover:bg-gray-50 transition-colors duration-200">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-white font-bold mr-3">
                          {appointment.patientName.charAt(0)}
                        </div>
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {appointment.patientName}
                          </div>
                          <div className="text-sm text-gray-500">
                            ID: #{appointment.id}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {appointment.doctorName}
                        </div>
                        <div className="text-sm text-gray-500">
                          {appointment.doctorSpecialization}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {formatDate(appointment.appointmentDate)}
                        </div>
                        <div className="text-sm text-gray-500">
                          {formatTime(appointment.appointmentTime)}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {appointment.hospitalName}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(appointment.status)}`}>
                        <span className="mr-1">{getStatusIcon(appointment.status)}</span>
                        {getStatusText(appointment.status)}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <select
                        value={appointment.status}
                        onChange={(e) => handleStatusUpdate(appointment.id, e.target.value)}
                        className="px-3 py-1 border border-gray-300 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent text-sm"
                      >
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="COMPLETED">Completed</option>
                        <option value="CANCELLED">Cancelled</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Appointment Details Modal (if needed) */}
      {filteredAppointments.length > 0 && (
        <div className="bg-white rounded-2xl shadow-lg p-6">
          <h3 className="text-lg font-semibold text-gray-800 mb-4">Quick Actions</h3>
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => {
                const pendingCount = appointments.filter(apt => apt.status === 'PENDING').length;
                alert(`There are ${pendingCount} pending appointments that need attention.`);
              }}
              className="px-4 py-2 bg-yellow-100 text-yellow-800 rounded-lg hover:bg-yellow-200 transition-colors duration-200"
            >
              View Pending ({statusCounts.PENDING})
            </button>
            <button
              onClick={() => {
                const today = new Date().toISOString().split('T')[0];
                const todayAppointments = appointments.filter(apt => apt.appointmentDate === today);
                alert(`There are ${todayAppointments.length} appointments scheduled for today.`);
              }}
              className="px-4 py-2 bg-blue-100 text-blue-800 rounded-lg hover:bg-blue-200 transition-colors duration-200"
            >
              Today's Appointments
            </button>
            <button
              onClick={() => {
                const completedCount = statusCounts.COMPLETED;
                alert(`Total completed appointments: ${completedCount}`);
              }}
              className="px-4 py-2 bg-green-100 text-green-800 rounded-lg hover:bg-green-200 transition-colors duration-200"
            >
              Completed ({statusCounts.COMPLETED})
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminAppointments;