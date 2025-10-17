import React, { useState, useEffect } from "react";
import { mockDoctors } from "../../data/mockData";

const DoctorList = ({ onBookAppointment }) => {
  const [doctors, setDoctors] = useState(mockDoctors);
  const [filteredDoctors, setFilteredDoctors] = useState(mockDoctors);
  const [searchFilters, setSearchFilters] = useState({
    name: "",
    specialization: "",
    date: "",
  });
  const [sortBy, setSortBy] = useState("rating");
  const [availabilityMap, setAvailabilityMap] = useState({}); // doctorId -> {available, capacity}

  useEffect(() => {
    filterDoctors();
  }, [searchFilters, doctors, sortBy]);

  useEffect(() => {
    // Fetch today's availability for each doctor from backend
    const today = new Date().toISOString().slice(0, 10);
    async function loadAvailability() {
      try {
        const entries = await Promise.all(
          doctors.map(async (d) => {
            try {
              const res = await fetch(`/api/slots/${d.id}?date=${today}`);
              if (!res.ok) return [d.id, null];
              const json = await res.json();
              return [
                d.id,
                { available: json.available, capacity: json.capacity },
              ];
            } catch (e) {
              return [d.id, null];
            }
          })
        );
        const map = Object.fromEntries(entries);
        setAvailabilityMap(map);
      } catch (e) {
        // ignore; keep mock fallback
      }
    }
    loadAvailability();
  }, [doctors]);

  const filterDoctors = () => {
    let filtered = [...doctors];

    // Filter by name
    if (searchFilters.name) {
      filtered = filtered.filter((doctor) =>
        doctor.name.toLowerCase().includes(searchFilters.name.toLowerCase())
      );
    }

    // Filter by specialization
    if (searchFilters.specialization) {
      filtered = filtered.filter((doctor) =>
        doctor.specialization
          .toLowerCase()
          .includes(searchFilters.specialization.toLowerCase())
      );
    }

    // Filter by available date
    if (searchFilters.date) {
      filtered = filtered.filter((doctor) =>
        doctor.availableSlots.some(
          (slot) => slot.date === searchFilters.date && slot.available
        )
      );
    }

    // Sort doctors
    filtered.sort((a, b) => {
      switch (sortBy) {
        case "rating":
          return b.rating - a.rating;
        case "experience":
          return b.experience - a.experience;
        case "fee":
          return a.fee - b.fee;
        case "name":
          return a.name.localeCompare(b.name);
        default:
          return 0;
      }
    });

    setFilteredDoctors(filtered);
  };

  const handleFilterChange = (field, value) => {
    setSearchFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    filterDoctors();
  };

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

  const getAvailableSlotsCount = (doctor) => {
    return doctor.availableSlots.filter((slot) => slot.available).length;
  };

  return (
    <div className="space-y-6">
      {/* Search and Filter Section */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl p-8 text-white shadow-xl">
        <div className="text-center mb-8">
          <h2 className="text-4xl font-bold mb-4">Find Your Perfect Doctor</h2>
          <p className="text-blue-100 text-lg">
            Search from our network of experienced specialists
          </p>
        </div>

        <form onSubmit={handleSearch} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Doctor Name Search */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg
                  className="h-6 w-6 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <input
                type="text"
                placeholder="Search by doctor name"
                value={searchFilters.name}
                onChange={(e) => handleFilterChange("name", e.target.value)}
                className="w-full pl-12 pr-4 py-4 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 text-lg"
              />
            </div>

            {/* Specialization Filter */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg
                  className="h-6 w-6 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"
                  />
                </svg>
              </div>
              <input
                type="text"
                placeholder="Specialization (e.g., Cardiology)"
                value={searchFilters.specialization}
                onChange={(e) =>
                  handleFilterChange("specialization", e.target.value)
                }
                className="w-full pl-12 pr-4 py-4 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 text-lg"
              />
            </div>

            {/* Date Filter */}
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg
                  className="h-6 w-6 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                  />
                </svg>
              </div>
              <input
                type="date"
                value={searchFilters.date}
                onChange={(e) => handleFilterChange("date", e.target.value)}
                className="w-full pl-12 pr-4 py-4 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900 text-lg"
              />
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 items-center justify-between">
            <button
              type="submit"
              className="px-8 py-4 bg-white text-blue-600 font-bold rounded-xl hover:bg-gray-50 transition-all duration-300 flex items-center justify-center shadow-lg hover:shadow-xl transform hover:-translate-y-1"
            >
              <svg
                className="h-6 w-6 mr-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
              Search Doctors
            </button>

            {/* Sort Options */}
            <div className="flex items-center space-x-4">
              <label className="text-white font-medium">Sort by:</label>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-transparent text-gray-900"
              >
                <option value="rating">Rating</option>
                <option value="experience">Experience</option>
                <option value="fee">Fee (Low to High)</option>
                <option value="name">Name</option>
              </select>
            </div>
          </div>
        </form>
      </div>

      {/* Results Summary */}
      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center justify-between">
          <h3 className="text-2xl font-bold text-gray-800">
            Available Doctors ({filteredDoctors.length})
          </h3>
          <div className="text-sm text-gray-600">
            Showing {filteredDoctors.length} of {doctors.length} doctors
          </div>
        </div>
      </div>

      {/* Doctors Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {filteredDoctors.map((doctor) => (
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
                  <div className="text-sm text-gray-600">Years Experience</div>
                </div>
                <div className="text-center p-3 bg-gray-50 rounded-lg">
                  <div className="text-2xl font-bold text-green-600">
                    Rs. {doctor.fee.toLocaleString()}
                  </div>
                  <div className="text-sm text-gray-600">Consultation Fee</div>
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

              {/* Available Slots */}
              <div className="bg-blue-50 rounded-lg p-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-blue-800">
                    Available Slots
                  </span>
                  <span className="text-sm font-bold text-blue-600">
                    {availabilityMap[doctor.id]
                      ? `${availabilityMap[doctor.id].available}/${
                          availabilityMap[doctor.id].capacity
                        } today`
                      : `${getAvailableSlotsCount(doctor)} slots`}
                  </span>
                </div>
              </div>

              {/* Book Now Button */}
              <button
                onClick={() => onBookAppointment(doctor)}
                className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-4 px-6 rounded-xl font-bold text-lg hover:from-blue-700 hover:to-indigo-700 transition-all duration-300 transform hover:scale-105 shadow-lg hover:shadow-xl"
              >
                Book Appointment
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* No Results */}
      {filteredDoctors.length === 0 && (
        <div className="text-center py-16">
          <div className="text-8xl mb-6">🔍</div>
          <h3 className="text-3xl font-bold text-gray-600 mb-4">
            No doctors found
          </h3>
          <p className="text-xl text-gray-500 mb-8">
            Try adjusting your search criteria or filters
          </p>
          <button
            onClick={() => {
              setSearchFilters({ name: "", specialization: "", date: "" });
              setSortBy("rating");
            }}
            className="px-8 py-4 bg-blue-600 text-white rounded-xl font-bold hover:bg-blue-700 transition-colors duration-300"
          >
            Clear Filters
          </button>
        </div>
      )}
    </div>
  );
};

export default DoctorList;
