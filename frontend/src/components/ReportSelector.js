import React from 'react';

const ReportSelector = ({ reportType, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center">
          Select Report Type
        </h2>

        <div className="space-y-4 mb-8">
          {/* Patient Visit Report */}
          <label className={`flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all ${
            reportType === 'PATIENT_VISIT'
              ? 'border-indigo-500 bg-indigo-50'
              : 'border-gray-200 hover:border-indigo-400 hover:bg-indigo-50'
          }`}>
            <input
              type="radio"
              value="PATIENT_VISIT"
              checked={reportType === 'PATIENT_VISIT'}
              onChange={() => onChange('PATIENT_VISIT')}
              className="w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
            />
            <div className="ml-4 flex-1">
              <span className="text-lg font-semibold text-gray-800">
                Patient Visit Report
              </span>
              <p className="text-sm text-gray-500 mt-1">
                View details of patient visits and appointments.
              </p>
            </div>
          </label>

          {/* Service Utilization Report */}
          <label className={`flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all ${
            reportType === 'SERVICE_UTILIZATION'
              ? 'border-indigo-500 bg-indigo-50'
              : 'border-gray-200 hover:border-indigo-400 hover:bg-indigo-50'
          }`}>
            <input
              type="radio"
              value="SERVICE_UTILIZATION"
              checked={reportType === 'SERVICE_UTILIZATION'}
              onChange={() => onChange('SERVICE_UTILIZATION')}
              className="w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
            />
            <div className="ml-4 flex-1">
              <span className="text-lg font-semibold text-gray-800">
                Service Utilization Report
              </span>
              <p className="text-sm text-gray-500 mt-1">
                Analyze usage statistics for hospital services.
              </p>
            </div>
          </label>
        </div>

        {/* Next Button */}
        <button
          onClick={onNext}
          disabled={!reportType}
          data-testid="next-button"
          className="w-full bg-indigo-600 text-white font-semibold py-3 px-6 rounded-xl transition-all hover:bg-indigo-700 active:scale-95 disabled:bg-gray-300 disabled:cursor-not-allowed disabled:hover:bg-gray-300 disabled:active:scale-100 shadow-lg disabled:shadow-none"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default ReportSelector;
