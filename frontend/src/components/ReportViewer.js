import React from 'react';
import DOMPurify from 'dompurify';

const ReportViewer = ({ html, meta, onDownload, loading }) => {
  return (
    <div className="w-full">
      <div className="max-w-6xl mx-auto">
        {/* Header Section */}
        <div className="bg-white rounded-t-2xl shadow-lg px-8 py-6 border-b-2 border-indigo-200">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-3xl font-bold text-gray-800 flex items-center gap-3">
                <svg className="w-8 h-8 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                Report Preview
              </h2>
              {meta && meta.title && (
                <p className="text-indigo-600 font-medium mt-1">{meta.title}</p>
              )}
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-500">Generated on</p>
              <p className="text-sm font-semibold text-gray-700">
                {new Date().toLocaleDateString('en-US', { 
                  year: 'numeric', 
                  month: 'long', 
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </p>
            </div>
          </div>

          {/* Meta Information Bar */}
          {meta && meta.criteria && (
            <div className="mt-4 flex flex-wrap gap-4 bg-indigo-50 p-4 rounded-lg">
              <div className="flex items-center gap-2">
                <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <span className="text-sm font-medium text-gray-700">
                  {meta.criteria.from} to {meta.criteria.to}
                </span>
              </div>
              {meta.criteria.hospitalId && (
                <div className="flex items-center gap-2">
                  <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                  <span className="text-sm text-gray-600">Hospital: {meta.criteria.hospitalId}</span>
                </div>
              )}
              {meta.criteria.serviceCategory && (
                <div className="flex items-center gap-2">
                  <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  <span className="text-sm text-gray-600">Service: {meta.criteria.serviceCategory}</span>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Content Area */}
        <div className="bg-white shadow-lg">
          {loading && (
            <div className="flex flex-col items-center justify-center py-20">
              <div className="relative w-20 h-20">
                <div className="absolute top-0 left-0 w-full h-full border-4 border-indigo-200 rounded-full"></div>
                <div className="absolute top-0 left-0 w-full h-full border-4 border-indigo-600 rounded-full border-t-transparent animate-spin"></div>
              </div>
              <p className="mt-6 text-gray-700 font-semibold text-lg">Generating Report...</p>
              <p className="mt-2 text-gray-500 text-sm">Please wait while we process your data</p>
            </div>
          )}

          {!loading && html && (
            <div className="p-8">
              <style>{`
                /* Custom styles for report content */
                .report-html-container h1 {
                  font-size: 1.875rem;
                  font-weight: 700;
                  color: #1f2937;
                  margin-bottom: 1rem;
                  text-align: center;
                  border-bottom: 3px solid #4f46e5;
                  padding-bottom: 1rem;
                }
                
                .report-html-container h2 {
                  font-size: 1.5rem;
                  font-weight: 600;
                  color: #374151;
                  margin-top: 2rem;
                  margin-bottom: 1rem;
                  border-bottom: 2px solid #e5e7eb;
                  padding-bottom: 0.5rem;
                }
                
                .report-html-container table {
                  width: 100%;
                  border-collapse: collapse;
                  margin: 1rem 0;
                  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                }
                
                .report-html-container table td {
                  padding: 1rem;
                  border: 1px solid #e5e7eb;
                }
                
                .report-html-container .criteria-label {
                  background-color: #f9fafb;
                  font-weight: 600;
                  color: #374151;
                }
                
                .report-html-container .criteria-value {
                  background-color: white;
                  color: #1f2937;
                }
                
                .report-html-container .kpi-table {
                  margin: 2rem 0;
                  border-spacing: 0;
                }
                
                .report-html-container .kpi-table td {
                  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                  color: white;
                  text-align: center;
                  padding: 2.5rem 2rem;
                  border: none;
                  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
                  position: relative;
                  overflow: hidden;
                }
                
                .report-html-container .kpi-table td::before {
                  content: '';
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, rgba(255,255,255,0) 100%);
                  pointer-events: none;
                }
                
                .report-html-container .kpi-label {
                  font-size: 1.125rem;
                  font-weight: 600;
                  margin-bottom: 1rem;
                  opacity: 0.95;
                  letter-spacing: 0.5px;
                  text-transform: uppercase;
                  font-size: 0.875rem;
                }
                
                .report-html-container .kpi-value {
                  font-size: 3rem;
                  font-weight: 800;
                  margin-top: 0.5rem;
                  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
                  line-height: 1;
                }
                
                .report-html-container p {
                  margin: 0.5rem 0;
                  line-height: 1.6;
                }
                
                /* Hide raw JSON text */
                .report-html-container p:empty {
                  display: none;
                }
                
                .report-html-container > p {
                  display: none;
                }
                
                /* Hide paragraphs that look like JSON */
                .report-html-container p:not([class]):not([id]) {
                  display: none;
                }
                
                /* Keep only styled elements visible */
                .report-html-container .kpi-table + p,
                .report-html-container .footer p {
                  display: block;
                }
                
                .report-html-container .footer {
                  margin-top: 3rem;
                  padding-top: 1.5rem;
                  border-top: 2px solid #e5e7eb;
                  text-align: center;
                  color: #6b7280;
                  font-size: 0.875rem;
                }
              `}</style>
              <div 
                className="report-html-container bg-white rounded-lg"
                dangerouslySetInnerHTML={{ __html: DOMPurify.sanitize(html) }}
              />
            </div>
          )}

          {!loading && !html && (
            <div className="text-center py-20">
              <div className="inline-flex items-center justify-center w-24 h-24 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full mb-6 shadow-inner">
                <svg
                  className="w-12 h-12 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                  />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-700 mb-2">No Report Generated</h3>
              <p className="text-gray-500">Complete the previous steps to generate your report</p>
            </div>
          )}
        </div>

        {/* Action Buttons Footer */}
        <div className="bg-white rounded-b-2xl shadow-lg px-8 py-6 border-t-2 border-gray-200">
          <div className="flex gap-4">
            <button
              onClick={onDownload}
              disabled={!html || loading}
              data-testid="download-button"
              className="w-full bg-gradient-to-r from-indigo-600 to-blue-600 text-white font-semibold py-4 px-6 rounded-xl transition-all hover:from-indigo-700 hover:to-blue-700 active:scale-98 disabled:from-gray-300 disabled:to-gray-400 disabled:cursor-not-allowed shadow-lg hover:shadow-xl flex items-center justify-center gap-3 group"
            >
              <svg
                className="w-6 h-6 group-hover:animate-bounce"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
              <span className="text-lg">Download as PDF</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReportViewer;
