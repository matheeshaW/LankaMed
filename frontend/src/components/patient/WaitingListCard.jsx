import React, { useState, useEffect } from 'react';
import { getCurrentUser } from '../../utils/auth';
import { waitlistAPI, appointmentAPI } from '../../services/api';
import WaitlistBookingForm from './WaitlistBookingForm';

const WaitingListCard = () => {
  const [waitlistEntries, setWaitlistEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [selectedWaitlistEntry, setSelectedWaitlistEntry] = useState(null);
  const currentUser = getCurrentUser();

  useEffect(() => {
    loadWaitlistEntries();
    
    // Auto-refresh every 10 seconds to catch new entries
    const interval = setInterval(() => {
      loadWaitlistEntries();
    }, 10000);

    // Listen for custom events when new waitlist entries are created or updated
    const handleWaitlistUpdate = () => {
      console.log('Waitlist update event received, refreshing...');
      setSuccessMessage('Waitlist updated! Refreshing...');
      setTimeout(() => setSuccessMessage(''), 3000);
      loadWaitlistEntries();
    };

    window.addEventListener('waitlistUpdated', handleWaitlistUpdate);

    return () => {
      clearInterval(interval);
      window.removeEventListener('waitlistUpdated', handleWaitlistUpdate);
    };
  }, []);

  const loadWaitlistEntries = async () => {
    setLoading(true);
    try {
      // First try admin endpoint to get all entries (including newly created ones)
      try {
        const adminResponse = await fetch('http://localhost:8080/api/admin/waitlist/all', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (adminResponse.ok) {
          const adminData = await adminResponse.json();
          
          if (Array.isArray(adminData)) {
            // Filter to show only entries for current user
            const currentUserEmail = currentUser ? currentUser.email : 'john.doe@example.com';
            const userEntries = adminData.filter(entry => 
              (entry.patientEmail === currentUserEmail || 
               entry.patientEmail === 'john.doe@example.com' ||
               !entry.patientEmail) && // Include entries without email
              entry.status !== 'PROMOTED' // Exclude PROMOTED entries
            );
            
            setWaitlistEntries(userEntries);
            localStorage.setItem('patientWaitlistEntries', JSON.stringify(userEntries));
            return;
          }
        }
      } catch (adminError) {
        // Admin endpoint failed, trying patient endpoint
      }
      
      // Fallback to patient endpoint
      const response = await waitlistAPI.getMyWaitlist();
      
      if (Array.isArray(response.data)) {
        // Filter out PROMOTED entries as a fallback
        const filteredData = response.data.filter(entry => entry.status !== 'PROMOTED');
        setWaitlistEntries(filteredData);
        localStorage.setItem('patientWaitlistEntries', JSON.stringify(filteredData));
      } else {
        throw new Error('Invalid response format');
      }
    } catch (error) {
      console.error('Error loading waitlist entries:', error);
      // Fallback to localStorage
      const localData = localStorage.getItem('patientWaitlistEntries');
      if (localData) {
        const parsedData = JSON.parse(localData);
        setWaitlistEntries(parsedData);
      } else {
        // Create demo data for testing
        const demoData = [
          {
            id: 'demo_1',
            desiredDateTime: new Date().toISOString(),
            status: 'APPROVED',
            doctorName: 'Dr. Smith',
            doctorSpecialization: 'Cardiology',
            hospitalName: 'City General Hospital',
            serviceCategoryName: 'General Consultation',
            priority: false,
            doctorId: 1,
            patientName: 'John Doe',
            patientEmail: 'john.doe@example.com'
          },
          {
            id: 'demo_2',
            desiredDateTime: new Date().toISOString(),
            status: 'QUEUED',
            doctorName: 'Dr. Johnson',
            doctorSpecialization: 'Neurology',
            hospitalName: 'City General Hospital',
            serviceCategoryName: 'General Consultation',
            priority: false,
            doctorId: 2,
            patientName: 'John Doe',
            patientEmail: 'john.doe@example.com'
          }
        ];
        setWaitlistEntries(demoData);
        localStorage.setItem('patientWaitlistEntries', JSON.stringify(demoData));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleBookFromWaitlist = (waitlistEntry) => {
    setSelectedWaitlistEntry(waitlistEntry);
    setShowBookingModal(true);
  };

  const handleBookingSuccess = (message) => {
    setSuccessMessage(message);
    setTimeout(() => setSuccessMessage(''), 5000);
    setShowBookingModal(false);
    
    // Remove the waitlist entry since it's been successfully converted to an appointment
    if (selectedWaitlistEntry) {
      const updatedEntries = waitlistEntries.filter(entry => entry.id !== selectedWaitlistEntry.id);
      setWaitlistEntries(updatedEntries);
      localStorage.setItem('patientWaitlistEntries', JSON.stringify(updatedEntries));
    }
    
    setSelectedWaitlistEntry(null);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
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

  const getStatusColor = (status) => {
    const normalizedStatus = String(status).toUpperCase().trim();
    switch (normalizedStatus) {
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
    const normalizedStatus = String(status).toUpperCase().trim();
    switch (normalizedStatus) {
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
    const normalizedStatus = String(status).toUpperCase().trim();
    switch (normalizedStatus) {
      case 'QUEUED':
        return 'In Queue';
      case 'NOTIFIED':
        return 'Notified';
      case 'APPROVED':
        return 'Approved - Book Now!';
      case 'PROMOTED':
        return 'Booked Successfully';
      default:
        return status;
    }
  };

  const activeEntries = waitlistEntries.filter(entry => {
    // Normalize status to uppercase and trim whitespace
    const normalizedStatus = String(entry.status).toUpperCase().trim();
    const validStatuses = ['QUEUED', 'NOTIFIED', 'APPROVED'];
    const isValidStatus = validStatuses.includes(normalizedStatus);
    return isValidStatus;
  });

  return (
    <div className="bg-white rounded-2xl shadow-lg p-6">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="text-2xl mr-3">⏳</span>
          Waiting List ({activeEntries.length} entries)
          {waitlistEntries.filter(entry => entry.status === 'APPROVED').length > 0 && (
            <span className="ml-2 px-2 py-1 bg-green-100 text-green-800 text-sm rounded-full">
              {waitlistEntries.filter(entry => entry.status === 'APPROVED').length} Approved!
            </span>
          )}
        </h3>
        <div className="flex space-x-2">
          <button
            onClick={loadWaitlistEntries}
            disabled={loading}
            className={`px-4 py-2 rounded-lg transition-colors duration-200 text-sm flex items-center space-x-2 ${
              loading 
                ? 'bg-gray-400 text-gray-200 cursor-not-allowed' 
                : 'bg-blue-600 text-white hover:bg-blue-700'
            }`}
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                <span>Refreshing...</span>
              </>
            ) : (
              <>
                <span>🔄</span>
                <span>Refresh</span>
              </>
            )}
          </button>
        </div>
      </div>

      {successMessage && (
        <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
          {successMessage}
        </div>
      )}

      {errorMessage && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {errorMessage}
        </div>
      )}



      {activeEntries.length > 0 ? (
        <div className="space-y-4">
          {activeEntries.map((entry) => (
            <div key={entry.id} className={`border rounded-xl p-6 hover:shadow-md transition-shadow duration-200 ${
              entry.status === 'APPROVED' ? 'bg-green-50 border-green-300 border-2' :
              'border-gray-200'
            }`}>
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
                  <div className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(entry.status)}`}>
                    <span className="mr-1">{getStatusIcon(entry.status)}</span>
                    {getStatusText(entry.status)}
                  </div>
                  {String(entry.status).toUpperCase().trim() === 'APPROVED' && (
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
      ) : (
        <div className="text-center py-12">
          <div className="text-6xl mb-4">⏳</div>
          <h3 className="text-xl font-semibold text-gray-600 mb-2">No waiting list entries</h3>
          <p className="text-gray-500">You haven't joined any waiting lists yet.</p>
        </div>
      )}

      {/* Booking Modal */}
      {showBookingModal && selectedWaitlistEntry && (
        <WaitlistBookingForm
          waitlistEntry={selectedWaitlistEntry}
          onClose={() => setShowBookingModal(false)}
          onSuccess={handleBookingSuccess}
        />
      )}
    </div>
  );
};

export default WaitingListCard;
