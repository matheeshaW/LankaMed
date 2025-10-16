import React from 'react';

const ReportSelector = ({ reportType, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl shadow-2xl p-10 w-full max-w-2xl transform transition-all duration-300 hover:shadow-3xl border border-gray-100">
        {/* Header with Icon */}
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl mb-4 shadow-lg">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h2 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-2">
            Select Report Type
          </h2>
          <p className="text-gray-500 text-sm">Choose the type of report you want to generate</p>
        </div>

        <div className="space-y-5 mb-10">
          {/* Patient Visit Report */}
          <label className={`relative flex items-start p-6 border-2 rounded-2xl cursor-pointer transition-all duration-200 group ${
            reportType === 'PATIENT_VISIT'
              ? 'border-indigo-500 bg-gradient-to-br from-indigo-50 to-purple-50 shadow-md'
              : 'border-gray-200 hover:border-indigo-400 hover:bg-gradient-to-br hover:from-indigo-50 hover:to-purple-50 hover:shadow-md'
          }`}>
            <div className="flex items-start gap-4 flex-1">
              {/* Icon */}
              <div className={`flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center transition-all ${
                reportType === 'PATIENT_VISIT'
                  ? 'bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg'
                  : 'bg-gray-100 group-hover:bg-gradient-to-br group-hover:from-indigo-500 group-hover:to-purple-600'
              }`}>
                <svg className={`w-6 h-6 transition-colors ${
                  reportType === 'PATIENT_VISIT' ? 'text-white' : 'text-gray-400 group-hover:text-white'
                }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
              
              {/* Content */}
              <div className="flex-1 pt-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xl font-bold text-gray-800">
                    Patient Visit Report
                  </span>
                  {reportType === 'PATIENT_VISIT' && (
                    <span className="text-xs bg-indigo-600 text-white px-2 py-1 rounded-full font-semibold">Selected</span>
                  )}
                </div>
                <p className="text-sm text-gray-600 leading-relaxed">
                  View comprehensive details of patient visits and appointments with statistical insights.
                </p>
              </div>
              
              {/* Radio Button */}
              <input
                type="radio"
                value="PATIENT_VISIT"
                checked={reportType === 'PATIENT_VISIT'}
                onChange={() => onChange('PATIENT_VISIT')}
                className="mt-2 w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
              />
            </div>
          </label>

          {/* Service Utilization Report */}
          <label className={`relative flex items-start p-6 border-2 rounded-2xl cursor-pointer transition-all duration-200 group ${
            reportType === 'SERVICE_UTILIZATION'
              ? 'border-indigo-500 bg-gradient-to-br from-indigo-50 to-purple-50 shadow-md'
              : 'border-gray-200 hover:border-indigo-400 hover:bg-gradient-to-br hover:from-indigo-50 hover:to-purple-50 hover:shadow-md'
          }`}>
            <div className="flex items-start gap-4 flex-1">
              {/* Icon */}
              <div className={`flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center transition-all ${
                reportType === 'SERVICE_UTILIZATION'
                  ? 'bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg'
                  : 'bg-gray-100 group-hover:bg-gradient-to-br group-hover:from-indigo-500 group-hover:to-purple-600'
              }`}>
                <svg className={`w-6 h-6 transition-colors ${
                  reportType === 'SERVICE_UTILIZATION' ? 'text-white' : 'text-gray-400 group-hover:text-white'
                }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              </div>
              
              {/* Content */}
              <div className="flex-1 pt-1">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-xl font-bold text-gray-800">
                    Service Utilization Report
                  </span>
                  {reportType === 'SERVICE_UTILIZATION' && (
                    <span className="text-xs bg-indigo-600 text-white px-2 py-1 rounded-full font-semibold">Selected</span>
                  )}
                </div>
                <p className="text-sm text-gray-600 leading-relaxed">
                  Analyze usage statistics and patterns for various hospital services and departments.
                </p>
              </div>
              
              {/* Radio Button */}
              <input
                type="radio"
                value="SERVICE_UTILIZATION"
                checked={reportType === 'SERVICE_UTILIZATION'}
                onChange={() => onChange('SERVICE_UTILIZATION')}
                className="mt-2 w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
              />
            </div>
          </label>
        </div>

        {/* Next Button */}
        <div className="relative">
          <button
            onClick={onNext}
            disabled={!reportType}
            data-testid="next-button"
            className="w-full bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold py-4 px-6 rounded-xl transition-all duration-300 hover:from-indigo-700 hover:to-purple-700 active:scale-95 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed shadow-xl hover:shadow-2xl flex items-center justify-center gap-2 group relative overflow-hidden"
          >
            <span className="relative z-10">Continue to Criteria</span>
            <svg className="w-5 h-5 relative z-10 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
            </svg>
            <div className="absolute inset-0 bg-gradient-to-r from-purple-600 to-indigo-600 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          </button>
          
          {/* Helper Text */}
          {!reportType && (
            <p className="text-center text-sm text-gray-400 mt-3">
              Please select a report type to continue
            </p>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReportSelector;
