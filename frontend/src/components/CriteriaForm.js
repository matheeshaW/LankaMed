import React from 'react';

const CriteriaForm = ({ criteria, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl shadow-2xl p-10 w-full max-w-2xl transform transition-all duration-300 hover:shadow-3xl border border-gray-100">
        {/* Header with Icon */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl mb-4 shadow-lg">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h2 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-2">
            Report Criteria Selection
          </h2>
          <p className="text-gray-500 text-sm">Configure your report parameters</p>
        </div>

        <div className="space-y-6 mb-8">
          {/* Date Range */}
          <div className="bg-gradient-to-br from-indigo-50 to-purple-50 p-6 rounded-2xl border border-indigo-100">
            <div className="flex items-center gap-2 mb-4">
              <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span className="font-bold text-gray-800">Date Range</span>
              <span className="ml-auto text-xs bg-indigo-600 text-white px-2 py-1 rounded-full">Required</span>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="relative group">
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  Start Date
                </label>
                <input
                  type="date"
                  value={criteria.from || ''}
                  onChange={e => onChange({ ...criteria, from: e.target.value })}
                  data-testid="from-input"
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200 bg-white group-hover:border-indigo-300"
                />
              </div>

              <div className="relative group">
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  End Date
                </label>
                <input
                  type="date"
                  value={criteria.to || ''}
                  onChange={e => onChange({ ...criteria, to: e.target.value })}
                  data-testid="to-input"
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200 bg-white group-hover:border-indigo-300"
                />
              </div>
            </div>
          </div>

          {/* Hospital Selection */}
          <div className="relative group">
            <div className="flex items-center gap-2 mb-2">
              <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              <label className="block text-sm font-semibold text-gray-700">
                Select Hospital
              </label>
              <span className="ml-auto text-xs bg-red-500 text-white px-2 py-1 rounded-full">Required</span>
            </div>
            <select
              value={criteria.hospitalId || ''}
              onChange={e => onChange({ ...criteria, hospitalId: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200 bg-white group-hover:border-indigo-300 cursor-pointer appearance-none bg-no-repeat bg-right pr-10"
              style={{backgroundImage: "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236366f1'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E\")", backgroundSize: "1.5rem", backgroundPosition: "right 0.5rem center"}}
            >
              <option value="">Choose a hospital...</option>
              <option value="C001">Colombo National Hospital</option>
              <option value="C002">Castle Street Women's Hospital</option>
              <option value="C003">Sri Jayewardenepura General Hospital</option>
              <option value="C004">Asiri Central Hospital</option>
              <option value="C005">Lanka Hospitals</option>
              <option value="C006">Nawaloka Hospital</option>
            </select>
          </div>

          {/* Service Category */}
          <div className="relative group">
            <div className="flex items-center gap-2 mb-2">
              <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <label className="block text-sm font-semibold text-gray-700">
                Service Category
              </label>
              <span className="ml-auto text-xs bg-gray-400 text-white px-2 py-1 rounded-full">Optional</span>
            </div>
            <select
              value={criteria.serviceCategory || ''}
              onChange={e => onChange({ ...criteria, serviceCategory: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200 bg-white group-hover:border-indigo-300 cursor-pointer appearance-none bg-no-repeat bg-right pr-10"
              style={{backgroundImage: "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236366f1'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E\")", backgroundSize: "1.5rem", backgroundPosition: "right 0.5rem center"}}
            >
              <option value="">All Categories</option>
              <option value="OPD">Outpatient Department (OPD)</option>
              <option value="LAB">Laboratory</option>
              <option value="SURGERY">Surgery</option>
              <option value="PHARMACY">Pharmacy</option>
              <option value="RADIOLOGY">Radiology</option>
            </select>
          </div>

          {/* Patient Category */}
          <div className="relative group">
            <div className="flex items-center gap-2 mb-2">
              <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <label className="block text-sm font-semibold text-gray-700">
                Patient Category
              </label>
              <span className="ml-auto text-xs bg-gray-400 text-white px-2 py-1 rounded-full">Optional</span>
            </div>
            <select
              value={criteria.patientCategory || ''}
              onChange={e => onChange({ ...criteria, patientCategory: e.target.value })}
              className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all duration-200 bg-white group-hover:border-indigo-300 cursor-pointer appearance-none bg-no-repeat bg-right pr-10"
              style={{backgroundImage: "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%236366f1'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E\")", backgroundSize: "1.5rem", backgroundPosition: "right 0.5rem center"}}
            >
              <option value="">All Patients</option>
              <option value="INPATIENT">Inpatient</option>
              <option value="OUTPATIENT">Outpatient</option>
              <option value="EMERGENCY">Emergency</option>
            </select>
          </div>

          {/* Selected Summary */}
          {criteria.from && criteria.to && (
            <div className="bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-200 rounded-2xl p-5 flex items-start gap-3 animate-fadeIn shadow-sm">
              <div className="flex-shrink-0 w-10 h-10 bg-green-500 rounded-full flex items-center justify-center">
                <svg
                  className="w-5 h-5 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M5 13l4 4L19 7"
                  />
                </svg>
              </div>
              <div className="flex-1">
                <p className="text-sm font-bold text-green-900">
                  Date Range Selected
                </p>
                <p className="text-sm text-green-700 mt-1 font-medium">
                  {new Date(criteria.from).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })} -{' '}
                  {new Date(criteria.to).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Next Button */}
        <div className="relative">
          <button
            onClick={onNext}
            disabled={!criteria.from || !criteria.to || !criteria.hospitalId}
            data-testid="next-button"
            className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold py-4 px-6 rounded-xl transition-all duration-300 hover:from-indigo-700 hover:to-purple-700 active:scale-95 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed shadow-xl hover:shadow-2xl flex items-center justify-center gap-2 group relative overflow-hidden"
          >
            <span className="relative z-10">Continue to Filters</span>
            <svg className="w-5 h-5 relative z-10 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
            <div className="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          </button>
        </div>
      </div>
    </div>
  );
};

export default CriteriaForm;
