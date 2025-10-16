import React, { useState } from 'react';

const AdvancedFilters = ({ filters, onChange, onNext }) => {
  const [showFilters, setShowFilters] = useState(false);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        {/* Step 1: Popup Message */}
        {!showFilters ? (
          <div className="text-center">
            <h3 className="text-2xl font-bold text-gray-800 mb-4">
              Refine Your Report
            </h3>
            <p className="text-gray-500 mb-8">
              Would you like to refine your report with filters?
            </p>

            <div className="flex gap-4 justify-center">
              <button
                onClick={onNext}
                className="bg-gray-300 text-gray-700 font-semibold py-2 px-6 rounded-xl hover:bg-gray-400 transition-all"
              >
                Skip
              </button>
              <button
                onClick={() => setShowFilters(true)}
                className="bg-indigo-600 text-white font-semibold py-2 px-6 rounded-xl hover:bg-indigo-700 transition-all"
              >
                Add Filters
              </button>
            </div>
          </div>
        ) : (
          /* Step 2: Filter Inputs */
          <div>
            <h3 className="text-2xl font-bold text-gray-800 mb-6 text-center">
              Add Report Filters
            </h3>

            <div className="space-y-5 mb-8">
              {/* Age Range */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Age Range
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <input
                    type="number"
                    placeholder="Min Age"
                    value={filters.minAge || ''}
                    onChange={(e) =>
                      onChange({ ...filters, minAge: e.target.value })
                    }
                    className="w-full px-3 py-2 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none"
                  />
                  <input
                    type="number"
                    placeholder="Max Age"
                    value={filters.maxAge || ''}
                    onChange={(e) =>
                      onChange({ ...filters, maxAge: e.target.value })
                    }
                    className="w-full px-3 py-2 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none"
                  />
                </div>
              </div>

              {/* Gender */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Gender
                </label>
                <select
                  value={filters.gender || ''}
                  onChange={(e) =>
                    onChange({ ...filters, gender: e.target.value })
                  }
                  className="w-full px-3 py-2 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none bg-white"
                >
                  <option value="">All</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              </div>
            </div>

            {/* Apply Filters Button */}
            <div className="flex gap-4 justify-center">
              <button
                onClick={onNext}
                className="flex-1 bg-indigo-600 text-white font-semibold py-3 rounded-xl hover:bg-indigo-700 transition-all"
              >
                Apply Filters
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdvancedFilters;
