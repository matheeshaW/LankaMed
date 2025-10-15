import React from 'react';

const ReportSelector = ({ reportType, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
        <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center">
          Select Report Type
        </h2>
        
        <div className="space-y-4 mb-8">
          <label className="flex items-center p-4 border-2 border-gray-200 rounded-xl cursor-pointer transition-all hover:border-indigo-400 hover:bg-indigo-50 group">
            <input
              type="radio"
              value="PATIENT_VISIT"
              checked={reportType === 'PATIENT_VISIT'}
              onChange={() => onChange('PATIENT_VISIT')}
              className="w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
            />
            <div className="ml-4 flex-1">
              <span className="text-lg font-semibold text-gray-800 group-hover:text-indigo-600">
                Patient Visit Report
              </span>
              <p className="text-sm text-gray-500 mt-1">
                Track patient appointments and visits
              </p>
            </div>
          </label>

          <label className="flex items-center p-4 border-2 border-gray-200 rounded-xl cursor-pointer transition-all hover:border-indigo-400 hover:bg-indigo-50 group">
            <input
              type="radio"
              value="SERVICE_UTILIZATION"
              checked={reportType === 'SERVICE_UTILIZATION'}
              onChange={() => onChange('SERVICE_UTILIZATION')}
              className="w-5 h-5 text-indigo-600 focus:ring-2 focus:ring-indigo-500 cursor-pointer"
            />
            <div className="ml-4 flex-1">
              <span className="text-lg font-semibold text-gray-800 group-hover:text-indigo-600">
                Service Utilization Report
              </span>
              <p className="text-sm text-gray-500 mt-1">
                Analyze service usage and trends
              </p>
            </div>
          </label>
        </div>

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