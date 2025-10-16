import React from 'react';

const CriteriaForm = ({ criteria, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-2xl">
        <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center">
          Report Criteria Selection
        </h2>

        <div className="space-y-8 mb-8">
          {/* Date Range */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Start Date
              </label>
              <input
                type="date"
                value={criteria.from || ''}
                onChange={e => onChange({ ...criteria, from: e.target.value })}
                data-testid="from-input"
                className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                End Date
              </label>
              <input
                type="date"
                value={criteria.to || ''}
                onChange={e => onChange({ ...criteria, to: e.target.value })}
                data-testid="to-input"
                className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
              />
            </div>
          </div>

          {/* Hospital Selection */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Select Hospital
            </label>
            <select
              value={criteria.hospitalId || ''}
              onChange={e => onChange({ ...criteria, hospitalId: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all bg-white"
            >
              <option value="">Select Hospital</option>
              <option value="C001">Colombo National Hospital</option>
              <option value="C002">Castle Street Women’s Hospital</option>
              <option value="C003">Sri Jayewardenepura General Hospital</option>
              <option value="C004">Asiri Central Hospital</option>
              <option value="C005">Lanka Hospitals</option>
              <option value="C006">Nawaloka Hospital</option>
            </select>
          </div>

          {/* Service Category */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Service Category
            </label>
            <select
              value={criteria.serviceCategory || ''}
              onChange={e => onChange({ ...criteria, serviceCategory: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all bg-white"
            >
              <option value="">Select Category</option>
              <option value="OPD">Outpatient Department (OPD)</option>
              <option value="LAB">Laboratory</option>
              <option value="SURGERY">Surgery</option>
              <option value="PHARMACY">Pharmacy</option>
              <option value="RADIOLOGY">Radiology</option>
            </select>
          </div>

          {/* Patient Category */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Patient Category
            </label>
            <select
              value={criteria.patientCategory || ''}
              onChange={e => onChange({ ...criteria, patientCategory: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all bg-white"
            >
              <option value="">Select Category</option>
              <option value="INPATIENT">Inpatient</option>
              <option value="OUTPATIENT">Outpatient</option>
              <option value="EMERGENCY">Emergency</option>
            </select>
          </div>

          {/* Selected Summary */}
          {criteria.from && criteria.to && (
            <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4 flex items-start gap-3">
              <svg
                className="w-5 h-5 text-indigo-600 mt-0.5 flex-shrink-0"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              <div className="flex-1">
                <p className="text-sm font-medium text-indigo-900">
                  Selected Range
                </p>
                <p className="text-sm text-indigo-700 mt-1">
                  {new Date(criteria.from).toLocaleDateString()} -{' '}
                  {new Date(criteria.to).toLocaleDateString()}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Next Button */}
        <button
          onClick={onNext}
          disabled={!criteria.from || !criteria.to || !criteria.hospitalId}
          data-testid="next-button"
          className="w-full bg-indigo-600 text-white font-semibold py-3 px-6 rounded-xl transition-all hover:bg-indigo-700 active:scale-95 disabled:bg-gray-300 disabled:cursor-not-allowed shadow-lg"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default CriteriaForm;
