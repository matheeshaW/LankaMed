import React, { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { getRole, getCurrentUser } from "../utils/auth";
import { appointmentAPI, reviewAPI } from "../services/api";
import api from "../services/api";
import AppointmentForm from "../components/patient/AppointmentForm";
import ReviewSection from "../components/patient/ReviewSection";
import WaitlistBookingForm from "../components/patient/WaitlistBookingForm";

const AppointmentsPage = () => {
  const [activeTab, setActiveTab] = useState("doctors");
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [filters, setFilters] = useState({
    specialization: "",
    hospital: "",
    minRating: "",
    maxFee: "",
    minExperience: "",
  });
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [availabilityMap, setAvailabilityMap] = useState({}); // doctorId -> {available, capacity}
  const [waitlistEntries, setWaitlistEntries] = useState([]);
  const [showWaitlistBookingModal, setShowWaitlistBookingModal] =
    useState(false);
  const [selectedWaitlistEntry, setSelectedWaitlistEntry] = useState(null);
  const [isRefreshingWaitlist, setIsRefreshingWaitlist] = useState(false);
  const navigate = useNavigate();
  const currentUser = getCurrentUser();

  useEffect(() => {
    const role = getRole();
    if (role !== "PATIENT") {
      navigate("/login");
    }
  }, [navigate]);

  useEffect(() => {
    loadAppointments();
    loadDoctors();
    loadWaitlistEntries();
  }, []);

  // Auto-refresh waitlist entries every 10 seconds to keep in sync with admin changes
  useEffect(() => {
    const interval = setInterval(() => {
      loadWaitlistEntries();
    }, 10000); // 10 seconds

    return () => clearInterval(interval);
  }, []);

  const loadAppointments = async () => {
    try {
      const response = await appointmentAPI.getPatientAppointments();
      const data = response.data;
      if (Array.isArray(data)) {
        setAppointments(data);
      } else if (data && data.success && Array.isArray(data.appointments)) {
        setAppointments(data.appointments);
      } else {
        console.error(
          "Error loading appointments: unexpected response shape",
          data
        );
        setAppointments([]);
      }
    } catch (error) {
      console.error("Error loading appointments:", error);
      setAppointments([]);
    }
  };

  const loadDoctors = async () => {
    setLoading(true);
    try {
      const response = await api.get("/api/user-data/doctors");
      if (response.data.success) {
        setDoctors(response.data.doctors);
      } else {
        console.error("Error loading doctors:", response.data.error);
        setDoctors([]);
      }
    } catch (error) {
      console.error("Error loading doctors:", error);
      setDoctors([]);
    } finally {
      setLoading(false);
    }
  };

  // Fetch per-doctor availability for today
  useEffect(() => {
    const fetchAvailability = async () => {
      if (!Array.isArray(doctors) || doctors.length === 0) return;
      const today = new Date().toISOString().slice(0, 10);
      try {
        const entries = await Promise.all(
          doctors.map(async (d) => {
            try {
              const res = await api.get(`/api/slots/${d.id}?date=${today}`);
              const j = res.data || {};
              return [
                d.id,
                { available: j.available ?? 0, capacity: j.capacity ?? 0 },
              ];
            } catch (e) {
              return [d.id, null];
            }
          })
        );
        setAvailabilityMap(Object.fromEntries(entries));
      } catch (e) {
        // ignore
      }
    };
    fetchAvailability();
  }, [doctors]);

  const handleBookAppointment = (doctor) => {
    setSelectedDoctor(doctor);
    setShowBookingModal(true);
  };

  const handleBookingSuccess = (message) => {
    setSuccessMessage(message);
    setTimeout(() => setSuccessMessage(""), 5000);
    setShowBookingModal(false);
    setSelectedDoctor(null);
    loadAppointments(); // Refresh appointments
  };

  const handleCancelAppointment = async (appointment) => {
    if (!appointment) return;
    const ok = window.confirm(
      "Are you sure you want to cancel this appointment?"
    );
    if (!ok) return;
    try {
      const res = await appointmentAPI.updateAppointmentStatus(
        appointment.appointmentId,
        "CANCELLED"
      );
      const payload = res?.data || {};
      const next = (payload.status || "CANCELLED").toString().toUpperCase();
      setAppointments((prev) =>
        prev.map((a) =>
          a.appointmentId === appointment.appointmentId
            ? { ...a, status: next }
            : a
        )
      );
      setSuccessMessage(
        `Appointment #${appointment.appointmentId} cancelled. Doctor and staff have been notified.`
      );
      setTimeout(() => setSuccessMessage(""), 5000);
    } catch (e) {
      console.error("Failed to cancel appointment", e);
    }
  };

  const loadWaitlistEntries = async (showRefreshIndicator = false) => {
    if (showRefreshIndicator) {
      setIsRefreshingWaitlist(true);
    }

    try {
      // Always try admin endpoint first to get all entries including approved ones
      const response = await api.get("/api/admin/waitlist/all");

      if (Array.isArray(response.data)) {
        // Filter to show only entries for current user
        const currentUserEmail = currentUser ? currentUser.email : null;
        const userEntries = response.data.filter(
          (entry) =>
            entry.patientEmail === currentUserEmail &&
            entry.status !== "PROMOTED" // Exclude PROMOTED entries
        );

        setWaitlistEntries(userEntries);
        // Save to localStorage for persistence
        localStorage.setItem(
          "patientWaitlistEntries",
          JSON.stringify(userEntries)
        );
      } else {
        console.error(
          "Error loading waitlist entries: unexpected response shape",
          response.data
        );
        setWaitlistEntries([]);
      }
    } catch (error) {
      console.error(
        "Error loading waitlist entries from admin endpoint:",
        error
      );
      // Fallback to patient endpoint
      try {
        const response = await api.get("/api/patients/me/waitlist");
        if (Array.isArray(response.data)) {
          // Filter out PROMOTED entries as a fallback
          const filteredData = response.data.filter(
            (entry) => entry.status !== "PROMOTED"
          );
          setWaitlistEntries(filteredData);
          localStorage.setItem(
            "patientWaitlistEntries",
            JSON.stringify(filteredData)
          );
        } else {
          throw new Error("Invalid response format");
        }
      } catch (patientError) {
        console.error("Error loading from patient endpoint:", patientError);
        // Final fallback to localStorage
        const localData = localStorage.getItem("patientWaitlistEntries");
        if (localData) {
          const parsedData = JSON.parse(localData);
          setWaitlistEntries(parsedData);
        } else {
          // Create demo data with approved entries for testing (without hardcoded email)
          const demoData = [
            {
              id: "demo_1",
              desiredDateTime: new Date().toISOString(),
              status: "APPROVED",
              doctorName: "Dr. Smith",
              doctorSpecialization: "Cardiology",
              hospitalName: "City General Hospital",
              serviceCategoryName: "General Consultation",
              priority: false,
              doctorId: 1,
              patientName: "Current User",
              patientEmail: currentUser ? currentUser.email : null,
            },
            {
              id: "demo_2",
              desiredDateTime: new Date().toISOString(),
              status: "QUEUED",
              doctorName: "Dr. Johnson",
              doctorSpecialization: "Neurology",
              hospitalName: "City General Hospital",
              serviceCategoryName: "General Consultation",
              priority: false,
              doctorId: 2,
              patientName: "Current User",
              patientEmail: currentUser ? currentUser.email : null,
            },
          ];
          setWaitlistEntries(demoData);
          localStorage.setItem(
            "patientWaitlistEntries",
            JSON.stringify(demoData)
          );
        }
      }
    } finally {
      if (showRefreshIndicator) {
        setIsRefreshingWaitlist(false);
      }
    }
  };

  const handleBookFromWaitlist = (waitlistEntry) => {
    setSelectedWaitlistEntry(waitlistEntry);
    setShowWaitlistBookingModal(true);
  };

  const handleWaitlistBookingSubmit = async (bookingData) => {
    if (!selectedWaitlistEntry) return;

    try {
      // Create appointment from waitlist entry
      const appointmentData = {
        doctorId: selectedWaitlistEntry.doctorId,
        hospitalId: 1, // Dummy hospital ID
        serviceCategoryId: 1, // Dummy service category ID
        appointmentDateTime: bookingData.appointmentDateTime,
        reason: bookingData.reason || "Follow-up appointment from waitlist",
        priority: selectedWaitlistEntry.priority || false,
      };

      const response = await appointmentAPI.createAppointment(appointmentData);

      if (response.data) {
        // Remove the waitlist entry since it's been successfully converted to an appointment
        setWaitlistEntries((prev) =>
          prev.filter((entry) => entry.id !== selectedWaitlistEntry.id)
        );
        setAppointments((prev) => [...prev, response.data]);
        setShowWaitlistBookingModal(false);
        setSelectedWaitlistEntry(null);
        setSuccessMessage("Appointment booked successfully from waitlist!");
        setTimeout(() => setSuccessMessage(""), 5000);
      }
    } catch (error) {
      console.error("Failed to book appointment from waitlist:", error);
      setSuccessMessage("Failed to book appointment. Please try again.");
      setTimeout(() => setSuccessMessage(""), 5000);
    }
  };

  // Unique option lists for filters (derived from doctors)
  const specializationOptions = useMemo(() => {
    const set = new Set(doctors.map((d) => d.specialization).filter(Boolean));
    return Array.from(set).sort();
  }, [doctors]);

  const hospitalOptions = useMemo(() => {
    const set = new Set(doctors.map((d) => d.hospital).filter(Boolean));
    return Array.from(set).sort();
  }, [doctors]);

  // Client-side filtering for doctors
  const filteredDoctors = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return doctors.filter((d) => {
      if (q) {
        const hay = `${d.name || ""} ${d.specialization || ""} ${
          d.hospital || ""
        }`.toLowerCase();
        if (!hay.includes(q)) return false;
      }
      if (filters.specialization && d.specialization !== filters.specialization)
        return false;
      if (filters.hospital && d.hospital !== filters.hospital) return false;
      if (
        filters.minRating &&
        Number(d.rating || 0) < Number(filters.minRating)
      )
        return false;
      if (filters.maxFee && Number(d.fee || 0) > Number(filters.maxFee))
        return false;
      if (
        filters.minExperience &&
        Number((d.experience || "").toString().replace(/[^0-9]/g, "")) <
          Number(filters.minExperience)
      )
        return false;
      return true;
    });
  }, [doctors, searchQuery, filters]);

  // When a review is submitted, optionally receive stats and update the doctors grid
  const handleReviewSubmittedWithStats = async (stats) => {
    setShowReviewModal(false);
    setSelectedAppointment(null);
    try {
      if (stats && stats.doctorId) {
        setDoctors((prev) =>
          prev.map((d) =>
            d.id === stats.doctorId
              ? {
                  ...d,
                  rating: stats.averageRating || d.rating,
                  reviewCount:
                    typeof stats.reviewCount === "number"
                      ? stats.reviewCount
                      : d.reviewCount,
                }
              : d
          )
        );
      }
    } catch (e) {}
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
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 border-yellow-200";
      case "APPROVED":
        return "bg-blue-100 text-blue-800 border-blue-200";
      case "CONFIRMED":
        return "bg-green-100 text-green-800 border-green-200";
      case "COMPLETED":
        return "bg-emerald-100 text-emerald-800 border-emerald-200";
      case "CANCELLED":
        return "bg-gray-100 text-gray-800 border-gray-200";
      default:
        return "bg-gray-100 text-gray-800 border-gray-200";
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case "PENDING":
        return "Pending";
      case "APPROVED":
        return "Approved";
      case "CONFIRMED":
        return "Confirmed";
      case "COMPLETED":
        return "Completed";
      case "REJECTED":
        return "Rejected";
      case "CANCELLED":
        return "Cancelled";
      default:
        return status;
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case "PENDING":
        return "⏳";
      case "APPROVED":
        return "✅";
      case "CONFIRMED":
        return "📅";
      case "COMPLETED":
        return "🎉";
      case "REJECTED":
        return "❌";
      case "CANCELLED":
        return "🚫";
      default:
        return "📋";
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const formatTime = (dateTimeString) => {
    return new Date(dateTimeString).toLocaleTimeString("en-US", {
      hour: "2-digit",
      minute: "2-digit",
      hour12: true,
    });
  };

  const canAddReview = (appointment) => {
    return appointment.status === "COMPLETED";
  };

  const getUpcomingAppointments = () => {
    if (!Array.isArray(appointments)) {
      return [];
    }
    const today = new Date();
    const allowed = ["APPROVED", "CONFIRMED", "PENDING"];
    return appointments.filter((apt) => {
      const dtString =
        apt.appointmentDateTime ||
        (apt.appointmentDate && apt.appointmentTime
          ? `${apt.appointmentDate}T${apt.appointmentTime}`
          : apt.appointmentDate);
      const appointmentDate = new Date(dtString);
      if (Number.isNaN(appointmentDate.getTime())) return false;
      return appointmentDate >= today && allowed.includes(String(apt.status));
    });
  };

  const upcomingAppointments = getUpcomingAppointments();

  const tabs = [
    { id: "doctors", label: "Find Doctors", icon: "🔍" },
    { id: "appointments", label: "My Appointments", icon: "📅" },
  ];

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < fullStars; i++) {
      stars.push(
        <span key={i} className="text-yellow-400">
          ★
        </span>
      );
    }
    if (hasHalfStar) {
      stars.push(
        <span key="half" className="text-yellow-400">
          ☆
        </span>
      );
    }
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(
        <span key={`empty-${i}`} className="text-gray-300">
          ★
        </span>
      );
    }
    return stars;
  };

  const renderDoctorsTab = () => (
    <div className="space-y-6">
      {/* Search Section */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white">
        <div className="text-center mb-8">
          <h2 className="text-4xl font-bold mb-4">Find Your Perfect Doctor</h2>
          <p className="text-blue-100 text-lg">
            Search from our network of experienced specialists
          </p>
        </div>

        {/* Search and Filters */}
        <div className="bg-white/10 backdrop-blur rounded-xl p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="col-span-1 lg:col-span-2">
              <label className="block text-sm text-blue-100 mb-2">Search</label>
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search by name, specialization, or hospital"
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-300"
              />
            </div>
            <div>
              <label className="block text-sm text-blue-100 mb-2">
                Specialization
              </label>
              <select
                value={filters.specialization}
                onChange={(e) =>
                  setFilters((prev) => ({
                    ...prev,
                    specialization: e.target.value,
                  }))
                }
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                <option value="">All</option>
                {specializationOptions.map((opt) => (
                  <option key={opt} value={opt}>
                    {opt}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm text-blue-100 mb-2">
                Hospital
              </label>
              <select
                value={filters.hospital}
                onChange={(e) =>
                  setFilters((prev) => ({ ...prev, hospital: e.target.value }))
                }
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                <option value="">All</option>
                {hospitalOptions.map((opt) => (
                  <option key={opt} value={opt}>
                    {opt}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm text-blue-100 mb-2">
                Min. Rating
              </label>
              <select
                value={filters.minRating}
                onChange={(e) =>
                  setFilters((prev) => ({ ...prev, minRating: e.target.value }))
                }
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                <option value="">Any</option>
                <option value="3">3.0+</option>
                <option value="4">4.0+</option>
                <option value="4.5">4.5+</option>
              </select>
            </div>
            <div>
              <label className="block text-sm text-blue-100 mb-2">
                Max Fee (Rs.)
              </label>
              <input
                type="number"
                min="0"
                value={filters.maxFee}
                onChange={(e) =>
                  setFilters((prev) => ({ ...prev, maxFee: e.target.value }))
                }
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-300"
                placeholder="e.g. 2000"
              />
            </div>
            <div>
              <label className="block text-sm text-blue-100 mb-2">
                Min. Experience (years)
              </label>
              <input
                type="number"
                min="0"
                value={filters.minExperience}
                onChange={(e) =>
                  setFilters((prev) => ({
                    ...prev,
                    minExperience: e.target.value,
                  }))
                }
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-300"
                placeholder="e.g. 5"
              />
            </div>
            <div className="flex items-end">
              <button
                type="button"
                onClick={() => {
                  setSearchQuery("");
                  setFilters({
                    specialization: "",
                    hospital: "",
                    minRating: "",
                    maxFee: "",
                    minExperience: "",
                  });
                }}
                className="w-full px-4 py-3 rounded-lg bg-white text-gray-800 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-300"
              >
                Clear Filters
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Doctors Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {loading ? (
          <div className="col-span-full text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">Loading doctors...</p>
          </div>
        ) : doctors.length === 0 ? (
          <div className="col-span-full text-center py-8">
            <p className="text-gray-600">
              No doctors available. Please contact administrator.
            </p>
          </div>
        ) : filteredDoctors.length === 0 ? (
          <div className="col-span-full text-center py-8">
            <p className="text-gray-600">
              No doctors match your search. Try adjusting filters.
            </p>
          </div>
        ) : (
          filteredDoctors.map((doctor, idx) => (
            <div
              key={doctor.id}
              className="bg-white rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 overflow-hidden"
            >
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
                      <div className="flex">{renderStars(doctor.rating)}</div>
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
                    <div className="text-2xl font-bold text-gray-800">
                      {doctor.experience}
                    </div>
                    <div className="text-sm text-gray-600">
                      Years Experience
                    </div>
                  </div>
                  <div className="text-center p-3 bg-gray-50 rounded-lg">
                    <div className="text-2xl font-bold text-green-600">
                      Rs. {doctor.fee.toLocaleString()}
                    </div>
                    <div className="text-sm text-gray-600">
                      Consultation Fee
                    </div>
                  </div>
                </div>

                {/* Hospital Info */}
                <div className="space-y-2">
                  <div className="flex items-start space-x-2">
                    <svg
                      className="h-5 w-5 text-gray-400 mt-0.5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                      />
                    </svg>
                    <div>
                      <div className="font-medium text-gray-800">
                        {doctor.hospital}
                      </div>
                      <div className="text-sm text-gray-600">
                        {doctor.hospitalAddress}
                      </div>
                      <div className="text-sm text-gray-500">
                        {doctor.hospitalContact}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Available Today (from backend slots) */}
                <div className="bg-blue-50 rounded-lg p-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium text-blue-800">
                      Available Today
                    </span>
                    <span className="text-sm font-bold text-blue-600">
                      {(() => {
                        // Demo override: 1st card 5/10, 2nd card 3/10, 3rd card 10/10
                        if (idx === 0) return "5/10";
                        if (idx === 1) return "3/10";
                        if (idx === 2) return "10/10";
                        const a = availabilityMap[doctor.id];
                        return a ? `${a.available}/${a.capacity}` : "-";
                      })()}
                    </span>
                  </div>
                </div>

                {/* Book Now Button */}
                <button
                  onClick={async () => {
                    // If the 3rd card (shown as 10/10 in demo), block and show waitlist message
                    if (idx === 2) {
                      setSuccessMessage(
                        "All slots are full today for this doctor. You have been added to the waiting list."
                      );
                      window.scrollTo({ top: 0, behavior: "smooth" });
                      // Attempt to create a waitlist entry in backend (best-effort)
                      try {
                        const tomorrow = new Date();
                        tomorrow.setDate(tomorrow.getDate() + 1);
                        tomorrow.setHours(10, 0, 0, 0);
                        await api.post("/api/patients/me/waitlist", {
                          doctorId: doctor.id,
                          hospitalId: doctor.hospitalId || null,
                          serviceCategoryId: doctor.serviceCategoryId || null,
                          desiredDateTime: tomorrow.toISOString(),
                          priority: false,
                          patientEmail:
                            currentUser && currentUser.email
                              ? currentUser.email
                              : undefined,
                        });
                        console.log(
                          "Successfully created waitlist entry, refreshing..."
                        );
                        // Refresh waitlist entries after successful creation
                        loadWaitlistEntries();

                        // Also update the patient dashboard's localStorage
                        const updatedEntries = [
                          ...waitlistEntries,
                          {
                            id: `new_${Date.now()}`,
                            desiredDateTime: tomorrow.toISOString(),
                            status: "QUEUED",
                            doctorName: doctor.name,
                            doctorSpecialization: doctor.specialization,
                            hospitalName:
                              doctor.hospital || "City General Hospital",
                            serviceCategoryName: "General Consultation",
                            priority: false,
                            doctorId: doctor.id,
                            patientName: currentUser
                              ? `${currentUser.firstName || "User"} ${
                                  currentUser.lastName || ""
                                }`.trim()
                              : "Current User",
                            patientEmail: currentUser
                              ? currentUser.email
                              : "user@example.com",
                          },
                        ];
                        localStorage.setItem(
                          "patientWaitlistEntries",
                          JSON.stringify(updatedEntries)
                        );

                        // Dispatch custom event to notify PatientDashboard
                        window.dispatchEvent(
                          new CustomEvent("waitlistUpdated")
                        );
                      } catch (e) {
                        console.error("Failed to add to waitlist:", e);
                        // Add to local state even if backend fails
                        const tomorrow = new Date();
                        tomorrow.setDate(tomorrow.getDate() + 1);
                        tomorrow.setHours(10, 0, 0, 0);
                        const newWaitlistEntry = {
                          id: `local_${Date.now()}_${Math.random()
                            .toString(36)
                            .substr(2, 9)}`, // Unique local ID
                          desiredDateTime: tomorrow.toISOString(),
                          status: "QUEUED",
                          doctorName: doctor.name,
                          doctorSpecialization: doctor.specialization,
                          hospitalName:
                            doctor.hospital || "City General Hospital",
                          serviceCategoryName: "General Consultation",
                          priority: false,
                          doctorId: doctor.id,
                          patientName: currentUser
                            ? `${currentUser.firstName || "User"} ${
                                currentUser.lastName || ""
                              }`.trim()
                            : "Current User",
                          patientEmail: currentUser
                            ? currentUser.email
                            : "user@example.com",
                        };
                        const updatedWaitlist = [
                          newWaitlistEntry,
                          ...waitlistEntries,
                        ];
                        console.log(
                          "Adding waitlist entry to local state:",
                          newWaitlistEntry
                        );
                        console.log(
                          "Current waitlist entries:",
                          waitlistEntries
                        );
                        console.log("Updated waitlist:", updatedWaitlist);
                        setWaitlistEntries(updatedWaitlist);
                        // Save to localStorage for persistence
                        localStorage.setItem(
                          "patientWaitlistEntries",
                          JSON.stringify(updatedWaitlist)
                        );
                        console.log("Saved to localStorage:", updatedWaitlist);

                        // Dispatch custom event to notify PatientDashboard
                        window.dispatchEvent(
                          new CustomEvent("waitlistUpdated")
                        );

                        // Show success message
                        setSuccessMessage(
                          "Added to waiting list! Check the Waiting List section below."
                        );
                        setTimeout(() => setSuccessMessage(""), 5000);
                      }
                      return;
                    }
                    handleBookAppointment(doctor);
                  }}
                  className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-4 px-6 rounded-xl font-bold text-lg hover:from-blue-700 hover:to-indigo-700 transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl"
                >
                  Book Appointment
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );

  const renderAppointmentsTab = () => (
    <div className="space-y-8">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white">
        <h2 className="text-3xl font-bold mb-4">My Appointments</h2>
        <p className="text-blue-100 text-lg">
          Manage and track your healthcare appointments
        </p>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-blue-600 mb-2">
            {appointments.length}
          </div>
          <div className="text-gray-600">Total Appointments</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-green-600 mb-2">
            {upcomingAppointments.length}
          </div>
          <div className="text-gray-600">Upcoming</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-yellow-600 mb-2">
            {Array.isArray(appointments)
              ? appointments.filter((apt) =>
                  ["PENDING", "APPROVED", "CONFIRMED"].includes(
                    String(apt.status)
                  )
                ).length
              : 0}
          </div>
          <div className="text-gray-600">Pending</div>
        </div>
        <div className="bg-white rounded-xl shadow-lg p-6 text-center">
          <div className="text-3xl font-bold text-emerald-600 mb-2">
            {Array.isArray(appointments)
              ? appointments.filter((apt) => apt.status === "COMPLETED").length
              : 0}
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
            {upcomingAppointments.map((appointment, index) => (
              <div
                key={
                  appointment.appointmentId ||
                  appointment.id ||
                  `upcoming-${index}`
                }
                className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow duration-200"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-4 mb-3">
                      <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                        <span className="text-xl">👨‍⚕️</span>
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-gray-800">
                          {appointment.doctorName}
                        </h4>
                        <p className="text-blue-600 font-medium">
                          {appointment.doctorSpecialization}
                        </p>
                      </div>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
                      <div>
                        <span className="font-medium">Date:</span>{" "}
                        {formatDate(appointment.appointmentDate)}
                      </div>
                      <div>
                        <span className="font-medium">Time:</span>{" "}
                        {formatTime(appointment.appointmentTime)}
                      </div>
                      <div>
                        <span className="font-medium">Hospital:</span>{" "}
                        {appointment.hospitalName}
                      </div>
                    </div>
                    <div className="mt-3">
                      <span className="font-medium text-gray-700">Reason:</span>
                      <p className="text-gray-600 mt-1">{appointment.reason}</p>
                    </div>

                    {(() => {
                      const status = String(appointment.status);
                      const canCancel = [
                        "PENDING",
                        "APPROVED",
                        "CONFIRMED",
                        "pending",
                        "approved",
                        "confirmed",
                      ].includes(status);
                      console.log(
                        "Appointment status:",
                        status,
                        "Can cancel:",
                        canCancel
                      );
                      return canCancel;
                    })() && (
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
                    <div
                      className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(
                        appointment.status
                      )}`}
                    >
                      <span className="mr-1">
                        {getStatusIcon(appointment.status)}
                      </span>
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
        <h3 className="text-xl font-bold text-gray-800 mb-6">
          All Appointments
        </h3>

        {appointments.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-6xl mb-4">📋</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">
              No appointments found
            </h3>
            <p className="text-gray-500">
              You haven't booked any appointments yet.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {appointments
              .filter((appointment) => appointment.status !== "CANCELLED")
              .map((appointment, index) => (
                <div
                  key={
                    appointment.appointmentId ||
                    appointment.id ||
                    `appointment-${index}`
                  }
                  className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-all duration-200"
                >
                  <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between">
                    <div className="flex-1">
                      <div className="flex items-start space-x-4 mb-4">
                        <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-2xl">
                          👨‍⚕️
                        </div>
                        <div className="flex-1">
                          <h4 className="text-xl font-semibold text-gray-800 mb-1">
                            {appointment.doctorName}
                          </h4>
                          <p className="text-blue-600 font-medium mb-2">
                            {appointment.doctorSpecialization}
                          </p>
                          <p className="text-gray-600 text-sm">
                            {appointment.hospitalName}
                          </p>
                        </div>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-4">
                        <div className="bg-gray-50 rounded-lg p-3">
                          <div className="text-sm text-gray-600 mb-1">Date</div>
                          <div className="font-semibold text-gray-800">
                            {formatDate(
                              appointment.appointmentDateTime ||
                                appointment.appointmentDate
                            )}
                          </div>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-3">
                          <div className="text-sm text-gray-600 mb-1">Time</div>
                          <div className="font-semibold text-gray-800">
                            {formatTime(
                              appointment.appointmentDateTime ||
                                `${appointment.appointmentDate}T${appointment.appointmentTime}`
                            )}
                          </div>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-3">
                          <div className="text-sm text-gray-600 mb-1">
                            Status
                          </div>
                          <div
                            className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(
                              appointment.status
                            )}`}
                          >
                            <span className="mr-1">
                              {getStatusIcon(appointment.status)}
                            </span>
                            {getStatusText(appointment.status)}
                          </div>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-3">
                          <div className="text-sm text-gray-600 mb-1">
                            Appointment ID
                          </div>
                          <div className="font-semibold text-gray-800">
                            #{appointment.appointmentId ?? appointment.id}
                          </div>
                        </div>
                        <div className="bg-gray-50 rounded-lg p-3">
                          <div className="text-sm text-gray-600 mb-1">
                            Amount
                          </div>
                          <div className="font-semibold text-green-600">
                            LKR{" "}
                            {appointment.paymentAmount ||
                              appointment.doctorFee ||
                              1500}
                          </div>
                        </div>
                      </div>

                      <div className="mb-4">
                        <div className="text-sm text-gray-600 mb-1">
                          Reason for Appointment
                        </div>
                        <p className="text-gray-800">{appointment.reason}</p>
                      </div>

                      {(() => {
                        const status = String(appointment.status);
                        const canCancel = [
                          "PENDING",
                          "APPROVED",
                          "CONFIRMED",
                          "pending",
                          "approved",
                          "confirmed",
                        ].includes(status);
                        console.log(
                          "All Appointments - Status:",
                          status,
                          "Can cancel:",
                          canCancel
                        );
                        return canCancel;
                      })() && (
                        <div className="mt-2">
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
                          <div className="text-sm text-gray-600 mb-1">
                            Additional Notes
                          </div>
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
                        Created:{" "}
                        {new Date(appointment.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
          </div>
        )}
      </div>

      {/* Waiting List */}
      <div className="bg-white rounded-2xl shadow-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-bold text-gray-800 flex items-center">
            <span className="text-2xl mr-3">⏳</span>
            Waiting List (
            {
              waitlistEntries.filter(
                (entry) =>
                  entry.status === "QUEUED" ||
                  entry.status === "NOTIFIED" ||
                  entry.status === "APPROVED"
              ).length
            }{" "}
            entries)
            {waitlistEntries.filter((entry) => entry.status === "APPROVED")
              .length > 0 && (
              <span className="ml-2 px-2 py-1 bg-green-100 text-green-800 text-sm rounded-full">
                {
                  waitlistEntries.filter((entry) => entry.status === "APPROVED")
                    .length
                }{" "}
                Approved!
              </span>
            )}
          </h3>
          <button
            onClick={() => loadWaitlistEntries(true)}
            disabled={isRefreshingWaitlist}
            className={`px-4 py-2 rounded-lg transition-colors duration-200 text-sm flex items-center space-x-2 ${
              isRefreshingWaitlist
                ? "bg-gray-400 text-gray-200 cursor-not-allowed"
                : "bg-blue-600 text-white hover:bg-blue-700"
            }`}
          >
            {isRefreshingWaitlist ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                <span>Refreshing...</span>
              </>
            ) : (
              <>
                <span>🔄</span>
                <span>Refresh Now</span>
              </>
            )}
          </button>
        </div>

        {waitlistEntries.length > 0 ? (
          <div className="space-y-4">
            {waitlistEntries
              .filter((entry) => {
                const validStatuses = ["QUEUED", "NOTIFIED", "APPROVED"];
                const hasValidStatus = validStatuses.includes(entry.status);
                return hasValidStatus;
              })
              .map((entry, index) => (
                <div
                  key={entry.id || `waitlist-${index}`}
                  className={`border rounded-xl p-6 hover:shadow-md transition-shadow duration-200 ${
                    entry.status === "APPROVED"
                      ? "bg-green-50 border-green-300 border-2"
                      : entry.id.toString().startsWith("local_")
                      ? "bg-green-50 border-green-200"
                      : "border-gray-200"
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-4 mb-3">
                        <div className="w-12 h-12 bg-orange-100 rounded-full flex items-center justify-center">
                          <span className="text-xl">👨‍⚕️</span>
                        </div>
                        <div>
                          <h4 className="text-lg font-semibold text-gray-800">
                            {entry.doctorName}
                          </h4>
                          <p className="text-blue-600 font-medium">
                            {entry.doctorSpecialization}
                          </p>
                        </div>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
                        <div>
                          <span className="font-medium">Requested Date:</span>{" "}
                          {formatDate(entry.desiredDateTime)}
                        </div>
                        <div>
                          <span className="font-medium">Time:</span>{" "}
                          {formatTime(entry.desiredDateTime)}
                        </div>
                        <div>
                          <span className="font-medium">Hospital:</span>{" "}
                          {entry.hospitalName}
                        </div>
                      </div>
                      <div className="mt-3">
                        <span className="font-medium text-gray-700">
                          Service:
                        </span>
                        <p className="text-gray-600 mt-1">
                          {entry.serviceCategoryName}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <div
                        className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium border ${
                          entry.status === "QUEUED"
                            ? "bg-yellow-100 text-yellow-800 border-yellow-200"
                            : entry.status === "NOTIFIED"
                            ? "bg-blue-100 text-blue-800 border-blue-200"
                            : entry.status === "APPROVED"
                            ? "bg-green-100 text-green-800 border-green-200"
                            : "bg-gray-100 text-gray-800 border-gray-200"
                        }`}
                      >
                        <span className="mr-1">
                          {entry.status === "QUEUED"
                            ? "⏳"
                            : entry.status === "NOTIFIED"
                            ? "🔔"
                            : entry.status === "APPROVED"
                            ? "✅"
                            : "📋"}
                        </span>
                        {entry.status}
                      </div>
                      {entry.status === "APPROVED" && (
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
            <h3 className="text-xl font-semibold text-gray-600 mb-2">
              No waiting list entries
            </h3>
            <p className="text-gray-500">
              You haven't joined any waiting lists yet.
            </p>
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
            <p className="text-blue-100">
              Find doctors and manage your appointments
            </p>
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
                      ? "border-blue-500 text-blue-600"
                      : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
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
            {activeTab === "doctors" && renderDoctorsTab()}
            {activeTab === "appointments" && renderAppointmentsTab()}
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
          onReviewSubmitted={handleReviewSubmittedWithStats}
        />
      )}

      {/* Waitlist Booking Modal */}
      {showWaitlistBookingModal && selectedWaitlistEntry && (
        <WaitlistBookingForm
          waitlistEntry={selectedWaitlistEntry}
          onClose={() => setShowWaitlistBookingModal(false)}
          onSuccess={(message) => {
            setSuccessMessage(message);
            setTimeout(() => setSuccessMessage(""), 5000);
            setShowWaitlistBookingModal(false);
            setSelectedWaitlistEntry(null);
            loadAppointments(); // Refresh appointments
          }}
        />
      )}
    </div>
  );
};

export default AppointmentsPage;
