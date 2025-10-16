import React, { useState } from 'react';
import { Link, Routes, Route, Navigate } from 'react-router-dom';
import AdminUsers from './AdminUsers';
import CategoryPage from './CategoryPage';
import { getRole } from '../utils/auth';
import ReportSelector from '../components/ReportSelector';
import CriteriaForm from '../components/CriteriaForm';
import AdvancedFilters from '../components/AdvancedFilters';
import ReportViewer from '../components/ReportViewer';
import { generateReport, downloadReport } from '../services/api';

export default function AdminDashboard() {
  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  // Report wizard state
  const [step, setStep] = useState(0);
  const [reportType, setReportType] = useState('');
  const [criteria, setCriteria] = useState({});
  const [filters, setFilters] = useState({});
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  
  const role = getRole();
  
  if (role !== 'ADMIN') return <Navigate to="/" replace />;
  
  const nextStep = () => setStep(s => s + 1);
  const prevStep = () => setStep(s => s - 1);
  
  const resetFlow = () => {
    setStep(0);
    setReportType('');
    setCriteria({});
    setFilters({});
    setReport(null);
    setError("");
  };
  
  const closeModal = () => {
    setIsModalOpen(false);
    resetFlow();
  };
  
  const openModal = () => {
    resetFlow();
    setIsModalOpen(true);
  };
  
  const handleGenerate = async () => {
    setLoading(true);
    setError("");
    try {
      // Merge filters into criteria and convert age strings to integers
      const mergedCriteria = {
        ...criteria,
        gender: filters.gender || criteria.gender,
        minAge: filters.minAge ? parseInt(filters.minAge, 10) : (criteria.minAge ? parseInt(criteria.minAge, 10) : null),
        maxAge: filters.maxAge ? parseInt(filters.maxAge, 10) : (criteria.maxAge ? parseInt(criteria.maxAge, 10) : null)
      };
      
      // Remove null/undefined values
      Object.keys(mergedCriteria).forEach(key => {
        if (mergedCriteria[key] === null || mergedCriteria[key] === undefined || mergedCriteria[key] === '') {
          delete mergedCriteria[key];
        }
      });
      
      const resp = await generateReport({ 
        reportType, 
        criteria: mergedCriteria, 
        filters: filters 
      });
      setReport(resp);
      setStep(3);
    } catch (e) {
      setError(e.message || 'Failed to generate report');
    } finally {
      setLoading(false);
    }
  };
  
  const handleDownload = async () => {
    setLoading(true);
    setError("");
    try {
      const resp = await downloadReport({ html: report.html });
      const blob = new Blob([resp], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'report.pdf';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setTimeout(() => URL.revokeObjectURL(url), 2000);
    } catch (e) {
      setError(e.message || 'PDF download failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
      <div className="flex">
        {/* Sidebar */}
        <aside className="w-64 bg-white shadow-xl border-r border-gray-200 min-h-screen">
          <div className="p-6">
            <div className="flex items-center space-x-3 mb-8">
              <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">⚙️</span>
              </div>
              <h2 className="text-xl font-bold text-gray-800">Admin Panel</h2>
            </div>
            <nav className="space-y-2">
              <button
                onClick={openModal}
                className="w-full flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <span className="text-xl">📊</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">Generate Reports</span>
              </button>
              <Link to="users" className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group">
                <span className="text-xl">👥</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">User Management</span>
              </Link>
              <Link to="categories" className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group">
                <span className="text-xl">📂</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">Categories</span>
              </Link>
            </nav>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          <Routes>
            <Route path="users" element={<AdminUsers />} />
            <Route path="categories" element={<CategoryPage />} />
            <Route index element={
              <div className="text-center py-20">
                <h1 className="text-4xl font-bold text-gray-800 mb-4">Welcome to Admin Dashboard</h1>
                <p className="text-gray-600 text-lg">Select an option from the sidebar to get started</p>
              </div>
            } />
          </Routes>
        </main>
      </div>

      {/* Report Generation Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            {/* Modal Header */}
            <div className="sticky top-0 bg-gradient-to-r from-indigo-600 to-blue-600 px-6 py-4 flex items-center justify-between rounded-t-2xl">
              <h2 className="text-2xl font-bold text-white">Generate & View Reports</h2>
              <button
                onClick={closeModal}
                className="text-white hover:bg-white hover:bg-opacity-20 rounded-full p-2 transition-all"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 relative"> {/* add relative here for overlay positioning */}
              {error && (
                <div className="sticky top-0 z-50 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl mb-4 flex items-start gap-2">
                  <svg className="w-5 h-5 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span className="font-semibold">{error}</span>
                </div>
              )}

              {loading && (
                <div className="absolute inset-0 z-50 bg-white bg-opacity-75 flex flex-col items-center justify-center">
                  <div className="animate-spin border-4 border-indigo-300 border-t-indigo-600 rounded-full w-12 h-12 mb-4"></div>
                  <span className="font-semibold text-indigo-700">Generating report...</span>
                </div>
              )}

              {/* Step Indicator */}
              <div className="flex items-center justify-center mb-8">
                {[0, 1, 2, 3].map((s) => (
                  <React.Fragment key={s}>
                    <div className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold transition-all ${
                      step === s ? 'bg-indigo-600 text-white scale-110' : 
                      step > s ? 'bg-green-500 text-white' : 
                      'bg-gray-200 text-gray-500'
                    }`}>
                      {step > s ? '✓' : s + 1}
                    </div>
                    {s < 3 && (
                      <div className={`w-16 h-1 mx-2 transition-all ${
                        step > s ? 'bg-green-500' : 'bg-gray-200'
                      }`} />
                    )}
                  </React.Fragment>
                ))}
              </div>

              {/* Step Content */}
              {step === 0 && (
                <ReportSelector
                  reportType={reportType}
                  onChange={setReportType}
                  onNext={() => reportType && nextStep()}
                />
              )}
              {step === 1 && (
                <CriteriaForm
                  criteria={criteria}
                  onChange={setCriteria}
                  onNext={() => (criteria.from && criteria.to) ? nextStep() : null}
                />
              )}
              {step === 2 && (
                <AdvancedFilters
                  filters={filters}
                  onChange={setFilters}
                  onNext={handleGenerate}
                />
              )}
              {step === 3 && (
                <ReportViewer
                  html={report && report.html}
                  meta={report && report.meta}
                  loading={loading}
                  onDownload={handleDownload}
                />
              )}

              {/* Navigation Buttons */}
              {step > 0 && step < 3 && (
                <div className="mt-6 pt-6 border-t border-gray-200">
                  <button
                    onClick={prevStep}
                    className="text-gray-600 hover:text-gray-800 font-medium flex items-center gap-2 transition-all hover:gap-3"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                    Back
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}