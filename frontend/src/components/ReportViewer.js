import React from 'react';
import DOMPurify from 'dompurify';

const ReportViewer = ({ html, meta, onDownload, loading }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-6">
      <div className="max-w-5xl mx-auto">
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-indigo-600 to-blue-600 px-8 py-6">
            <h2 className="text-3xl font-bold text-white">Report Output</h2>
            {meta && (
              <p className="text-indigo-100 mt-2">
                Generated on {new Date().toLocaleDateString()}
              </p>
            )}
          </div>

          {/* Content Area */}
          <div className="p-8">
            {loading && (
              <div className="flex flex-col items-center justify-center py-16">
                <div className="relative w-16 h-16">
                  <div className="absolute top-0 left-0 w-full h-full border-4 border-indigo-200 rounded-full"></div>
                  <div className="absolute top-0 left-0 w-full h-full border-4 border-indigo-600 rounded-full border-t-transparent animate-spin"></div>
                </div>
                <p className="mt-4 text-gray-600 font-medium">Generating report...</p>
              </div>
            )}

            {!loading && html && (
              <div className="report-html-container bg-gray-50 rounded-xl p-8 border border-gray-200 shadow-inner">
                <div
                  className="prose prose-indigo max-w-none"
                  dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(html) }}
                />
              </div>
            )}

            {!loading && !html && (
              <div className="text-center py-16">
                <div className="inline-flex items-center justify-center w-20 h-20 bg-gray-100 rounded-full mb-4">
                  <svg className="w-10 h-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <p className="text-gray-500">No report generated yet</p>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="px-8 pb-8">
            <button
              onClick={onDownload}
              disabled={!html || loading}
              data-testid="download-button"
              className="w-full bg-indigo-600 text-white font-semibold py-3 px-6 rounded-xl transition-all hover:bg-indigo-700 active:scale-95 disabled:bg-gray-300 disabled:cursor-not-allowed disabled:hover:bg-gray-300 disabled:active:scale-100 shadow-lg disabled:shadow-none flex items-center justify-center gap-2"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Download PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReportViewer;