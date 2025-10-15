import React from 'react';

const CriteriaForm = ({ criteria, onChange, onNext }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-2xl">
        <h2 className="text-3xl font-bold text-gray-800 mb-8 text-center">
          Select Date Range
        </h2>
        
        <div className="space-y-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Start Date
              </label>
              <div className="relative">
                <input
                  type="date"
                  value={criteria.from || ''}
                  onChange={e => onChange({ ...criteria, from: e.target.value })}
                  data-testid="from-input"
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                  <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                End Date
              </label>
              <div className="relative">
                <input
                  type="date"
                  value={criteria.to || ''}
                  onChange={e => onChange({ ...criteria, to: e.target.value })}
                  data-testid="to-input"
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                  <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {criteria.from && criteria.to && (
            <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-4 flex items-start gap-3">
              <svg className="w-5 h-5 text-indigo-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div className="flex-1">
                <p className="text-sm font-medium text-indigo-900">
                  Selected Range
                </p>
                <p className="text-sm text-indigo-700 mt-1">
                  {new Date(criteria.from).toLocaleDateString()} - {new Date(criteria.to).toLocaleDateString()}
                </p>
              </div>
            </div>
          )}
        </div>

        <button
          onClick={onNext}
          disabled={!criteria.from || !criteria.to}
          data-testid="next-button"
          className="w-full bg-indigo-600 text-white font-semibold py-3 px-6 rounded-xl transition-all hover:bg-indigo-700 active:scale-95 disabled:bg-gray-300 disabled:cursor-not-allowed disabled:hover:bg-gray-300 disabled:active:scale-100 shadow-lg disabled:shadow-none"
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default CriteriaForm;