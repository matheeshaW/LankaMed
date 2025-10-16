import React, { useState, useEffect } from 'react';
import { waitlistAPI } from '../../services/api';

const AdminWaitlist = () => {
  const [waitlist, setWaitlist] = useState([]);
  const [filteredWaitlist, setFilteredWaitlist] = useState([]);
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadWaitlist();
  }, []);

  useEffect(() => {
    filterWaitlist();
  }, [waitlist, filter, searchTerm]);

  const loadWaitlist = async () => {
    setLoading(true);
    try {
      const res = await waitlistAPI.getAllWaitlist();
      const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
      // Filter out PROMOTED entries as a fallback
      const filteredData = data.filter(entry => entry.status !== 'PROMOTED');
      setWaitlist(filteredData);
      // Also save to localStorage for persistence
      localStorage.setItem('waitlistEntries', JSON.stringify(filteredData));
    } catch (e) {
      console.error('Error loading waitlist:', e);
      // Fallback to localStorage if backend fails
      const localData = localStorage.getItem('waitlistEntries');
      if (localData) {
        setWaitlist(JSON.parse(localData));
        console.log('Loaded waitlist from localStorage');
      } else {
        // Create demo data for testing
        const demoData = [
          {
            id: 1,
            patientName: 'John Doe',
            patientEmail: 'john.doe@example.com',
            doctorName: 'Dr. Smith',
            doctorSpecialization: 'Cardiology',
            hospitalName: 'City General Hospital',
            serviceCategoryName: 'General Consultation',
            desiredDateTime: new Date().toISOString(),
            status: 'QUEUED',
            priority: false,
            createdAt: new Date().toISOString()
          },
          {
            id: 2,
            patientName: 'Jane Smith',
            patientEmail: 'jane.smith@example.com',
            doctorName: 'Dr. Johnson',
            doctorSpecialization: 'Neurology',
            hospitalName: 'City General Hospital',
            serviceCategoryName: 'General Consultation',
            desiredDateTime: new Date().toISOString(),
            status: 'APPROVED',
            priority: false,
            createdAt: new Date().toISOString()
          }
        ];
        setWaitlist(demoData);
        localStorage.setItem('waitlistEntries', JSON.stringify(demoData));
      }
    } finally {
      setLoading(false);
    }
  };

  const filterWaitlist = () => {
    let filtered = [...waitlist];

    // First, filter out PROMOTED entries since they're no longer waiting
    filtered = filtered.filter(entry => entry.status !== 'PROMOTED');

    // Filter by status
    if (filter !== 'all') {
      filtered = filtered.filter(entry => entry.status === filter);
    }

    // Filter by search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(entry =>
        entry.patientName.toLowerCase().includes(term) ||
        entry.patientEmail.toLowerCase().includes(term) ||
        entry.doctorName.toLowerCase().includes(term) ||
        entry.doctorSpecialization.toLowerCase().includes(term) ||
        entry.hospitalName.toLowerCase().includes(term)
      );
    }

    setFilteredWaitlist(filtered);
  };

  const handleStatusUpdate = async (waitlistId, newStatus) => {
    try {
      console.log('Updating waitlist status:', { waitlistId, newStatus });
      
      // Update waitlist entry status
      await waitlistAPI.updateWaitlistStatus(waitlistId, newStatus);
      
      // Update local state and get the updated entries
      const updatedEntries = waitlist.map(entry => 
        entry.id === waitlistId 
          ? { ...entry, status: newStatus }
          : entry
      );
      
      setWaitlist(updatedEntries);
      
      // Also update localStorage and dispatch event for patient dashboard
      localStorage.setItem('waitlistEntries', JSON.stringify(updatedEntries));
      localStorage.setItem('patientWaitlistEntries', JSON.stringify(updatedEntries));
      window.dispatchEvent(new CustomEvent('waitlistUpdated'));
      
      setSuccessMessage(`Waitlist entry status updated to ${newStatus}`);
      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (error) {
      console.error('Error updating waitlist status:', error);
      
      // Fallback: Update local state even if backend fails
      console.log('Backend unavailable, updating local state for demo purposes');
      const updatedWaitlist = waitlist.map(entry => 
        entry.id === waitlistId 
          ? { ...entry, status: newStatus }
          : entry
      );
      setWaitlist(updatedWaitlist);
      // Save to localStorage for persistence
      localStorage.setItem('waitlistEntries', JSON.stringify(updatedWaitlist));
      localStorage.setItem('patientWaitlistEntries', JSON.stringify(updatedWaitlist));
      window.dispatchEvent(new CustomEvent('waitlistUpdated'));
      
      setSuccessMessage(`Waitlist entry status updated to ${newStatus} (demo mode)`);
      setTimeout(() => setSuccessMessage(''), 5000);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'QUEUED':
        return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'NOTIFIED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'APPROVED':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'PROMOTED':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'QUEUED':
        return '⏳';
      case 'NOTIFIED':
        return '🔔';
      case 'APPROVED':
        return '✅';
      case 'PROMOTED':
        return '🎉';
      default:
        return '📋';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'QUEUED':
        return 'In Queue';
      case 'NOTIFIED':
        return 'Notified';
      case 'APPROVED':
        return 'Approved';
      case 'PROMOTED':
        return 'Promoted to Appointment';
      default:
        return status;
    }
  };

  const formatDate = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
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

  const getStatusCounts = () => {
    const counts = {
      QUEUED: 0,
      NOTIFIED: 0,
      APPROVED: 0
    };

    waitlist.forEach(entry => {
      if (entry.status !== 'PROMOTED') {
        counts[entry.status] = (counts[entry.status] || 0) + 1;
      }
    });

    return counts;
  };

  const statusCounts = getStatusCounts();

  return (
    <div className="max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="bg-gradient-to-r from-orange-600 to-red-600 rounded-2xl p-8 text-white">
        <h1 className="text-3xl font-bold mb-4">Waiting List Management</h1>
        <p className="text-orange-100 text-lg">Manage patient waiting list entries and approvals</p>
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
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
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
                placeholder="Search waitlist entries..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent w-full sm:w-64"
              />
            </div>

            {/* Status Filter */}
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            >
              <option value="all">All Status</option>
              <option value="QUEUED">In Queue</option>
              <option value="NOTIFIED">Notified</option>
              <option value="APPROVED">Approved</option>
            </select>
          </div>

          <button
            onClick={() => {
              loadWaitlist();
              setSearchTerm('');
              setFilter('all');
            }}
            className="px-4 py-2 bg-orange-600 text-white rounded-lg hover:bg-orange-700 transition-colors duration-200"
          >
            Refresh
          </button>
        </div>
      </div>

      {/* Waitlist Entries */}
      <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-800">
            Waiting List Entries ({filteredWaitlist.length})
          </h3>
        </div>

        {filteredWaitlist.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">⏳</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">No waitlist entries found</h3>
            <p className="text-gray-500">
              {searchTerm || filter !== 'all' 
                ? 'Try adjusting your search criteria or filters.' 
                : 'No patients are currently on the waiting list.'
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
                    Requested Date
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
                {filteredWaitlist.map((entry) => (
                  <tr key={entry.id} className="hover:bg-gray-50 transition-colors duration-200">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="w-10 h-10 bg-gradient-to-br from-orange-500 to-red-600 rounded-full flex items-center justify-center text-white font-bold mr-3">
                          {entry.patientName.charAt(0)}
                        </div>
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {entry.patientName}
                          </div>
                          <div className="text-sm text-gray-500">
                            {entry.patientEmail}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {entry.doctorName}
                        </div>
                        <div className="text-sm text-gray-500">
                          {entry.doctorSpecialization}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="text-sm font-medium text-gray-900">
                          {formatDate(entry.desiredDateTime)}
                        </div>
                        <div className="text-sm text-gray-500">
                          {formatTime(entry.desiredDateTime)}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {entry.hospitalName}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(entry.status)}`}>
                        <span className="mr-1">{getStatusIcon(entry.status)}</span>
                        {getStatusText(entry.status)}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <div className="flex space-x-2">
                        {entry.status === 'QUEUED' && (
                          <>
                            <button
                              onClick={() => handleStatusUpdate(entry.id, 'NOTIFIED')}
                              className="px-3 py-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 text-sm"
                            >
                              Notify
                            </button>
                            <button
                              onClick={() => handleStatusUpdate(entry.id, 'APPROVED')}
                              className="px-3 py-1 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200 text-sm"
                            >
                              Approve
                            </button>
                          </>
                        )}
                        {entry.status === 'NOTIFIED' && (
                          <button
                            onClick={() => handleStatusUpdate(entry.id, 'APPROVED')}
                            className="px-3 py-1 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-200 text-sm"
                          >
                            Approve
                          </button>
                        )}
                        {entry.status === 'APPROVED' && (
                          <span className="px-3 py-1 bg-green-100 text-green-800 rounded-lg text-sm">
                            Ready to Book
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminWaitlist;
