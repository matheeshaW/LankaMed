import React, { useState } from 'react';
import { Link, Routes, Route, Navigate } from 'react-router-dom';
import AdminUsers from './AdminUsers';
import CategoryPage from './CategoryPage';
import AdminAppointments from '../components/admin/AdminAppointments';
import AdminWaitlist from '../components/admin/AdminWaitlist';
import { getRole } from '../utils/auth';

// Report components
import ReportSelector from '../components/ReportSelector';
import CriteriaForm from '../components/CriteriaForm';
import AdvancedFilters from '../components/AdvancedFilters';
import ReportViewer from '../components/ReportViewer';
import { generateReport, downloadReport } from '../services/api';

export default function AdminDashboard() {
  // --- Modal and Report State ---
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [step, setStep] = useState(0);
  const [reportType, setReportType] = useState('');
  const [criteria, setCriteria] = useState({});
  const [filters, setFilters] = useState({});
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const role = getRole();
  if (role !== 'ADMIN') return <Navigate to="/" replace />;

  const nextStep = () => setStep((s) => s + 1);
  const prevStep = () => setStep((s) => s - 1);
  const resetFlow = () => {
    setStep(0);
    setReportType('');
    setCriteria({});
    setFilters({});
    setReport(null);
    setError('');
  };
  const openModal = () => {
    resetFlow();
    setIsModalOpen(true);
  };
  const closeModal = () => {
    setIsModalOpen(false);
    resetFlow();
  };

  const handleGenerate = async () => {
    setLoading(true);
    setError('');
    try {
      const mergedCriteria = {
        ...criteria,
        gender: filters.gender || criteria.gender,
        minAge: filters.minAge ? parseInt(filters.minAge, 10) : (criteria.minAge ? parseInt(criteria.minAge, 10) : null),
        maxAge: filters.maxAge ? parseInt(filters.maxAge, 10) : (criteria.maxAge ? parseInt(criteria.maxAge, 10) : null),
      };
      Object.keys(mergedCriteria).forEach((key) => {
        if (!mergedCriteria[key]) delete mergedCriteria[key];
      });

      const resp = await generateReport({ reportType, criteria: mergedCriteria, filters });
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
    setError('');
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

  // --- UI Layout ---
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
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">
                  Generate Reports
                </span>
              </button>
              <Link
                to="users"
                className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <span className="text-xl">👥</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">User Management</span>
              </Link>
              <Link
                to="categories"
                className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <span className="text-xl">📂</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">Categories</span>
              </Link>
              <Link
                to="appointments"
                className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <span className="text-xl">📅</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">Appointments</span>
              </Link>
              <Link
                to="waitlist"
                className="flex items-center space-x-3 px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <span className="text-xl">⏳</span>
                <span className="font-medium group-hover:translate-x-1 transition-transform duration-200">Waiting List</span>
              </Link>
            </nav>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          <Routes>
            <Route path="users" element={<AdminUsers />} />
            <Route path="categories" element={<CategoryPage />} />
            <Route path="appointments" element={<AdminAppointments />} />
            <Route path="waitlist" element={<AdminWaitlist />} />
            <Route
              index
              element={
                <div className="max-w-4xl mx-auto">
                  <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
                    <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-8 py-6">
                      <h1 className="text-3xl font-bold text-white">Welcome, Admin</h1>
                      <p className="text-blue-100 mt-2">Manage your platform from the admin dashboard</p>
                    </div>
                    <div className="p-8">
                      <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
                        {/* cards */}
                        {[
                          { color: 'blue', icon: '👥', title: 'User Management', desc: 'Manage user accounts, reset passwords, and view user activity', to: 'users' },
                          { color: 'green', icon: '📂', title: 'Categories', desc: 'Manage service categories and organize your platform content', to: 'categories' },
                          { color: 'purple', icon: '📅', title: 'Appointments', desc: 'View and manage all patient appointments and their status', to: 'appointments' },
                          { color: 'orange', icon: '⏳', title: 'Waiting List', desc: 'Manage patient waiting list entries and approvals', to: 'waitlist' },
                        ].map(({ color, icon, title, desc, to }) => (
                          <div key={title} className={`bg-gradient-to-br from-${color}-50 to-${color}-100 p-6 rounded-xl border border-${color}-200`}>
                            <div className="flex items-center space-x-4">
                              <div className={`w-12 h-12 bg-${color}-600 rounded-lg flex items-center justify-center`}>
                                <span className="text-white text-2xl">{icon}</span>
                              </div>
                              <div>
                                <h3 className="text-lg font-semibold text-gray-800">{title}</h3>
                                <p className="text-gray-600">{desc}</p>
                              </div>
                            </div>
                            <Link
                              to={to}
                              className={`inline-flex items-center mt-4 text-${color}-600 hover:text-${color}-800 font-medium transition-colors duration-200`}
                            >
                              Go to {title.split(' ')[0]} <span className="ml-1">→</span>
                            </Link>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              }
            />
          </Routes>
        </main>
      </div>

      {/* --- Report Generation Modal --- */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto relative">
            <div className="sticky top-0 bg-gradient-to-r from-indigo-600 to-blue-600 px-6 py-4 flex items-center justify-between rounded-t-2xl z-20 shadow-md">
              <h2 className="text-2xl font-bold text-white">Generate & View Reports</h2>
              <button onClick={closeModal} className="text-white hover:bg-white hover:bg-opacity-20 rounded-full p-2 transition-all">
                ✖
              </button>
            </div>

            {/* Stepper */}
            <div className="sticky top-[72px] bg-white px-6 py-4 z-10 border-b border-gray-200">
              <div className="flex items-center justify-center">
                {[0, 1, 2, 3].map((s) => (
                  <React.Fragment key={s}>
                    <div
                      className={`flex items-center justify-center w-10 h-10 rounded-full font-semibold transition-all ${
                        step === s
                          ? 'bg-indigo-600 text-white scale-110'
                          : step > s
                          ? 'bg-green-500 text-white'
                          : 'bg-gray-200 text-gray-500'
                      }`}
                    >
                      {step > s ? '✓' : s + 1}
                    </div>
                    {s < 3 && <div className={`w-16 h-1 mx-2 ${step > s ? 'bg-green-500' : 'bg-gray-200'}`} />}
                  </React.Fragment>
                ))}
              </div>
            </div>

            {/* Step Content */}
            <div className="p-6 relative">
              {error && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl mb-4">{error}</div>}

              {loading && (
                <div className="absolute inset-0 bg-white bg-opacity-75 flex flex-col items-center justify-center z-50">
                  <div className="animate-spin border-4 border-indigo-300 border-t-indigo-600 rounded-full w-12 h-12 mb-4"></div>
                  <span className="font-semibold text-indigo-700">Generating report...</span>
                </div>
              )}

              {step === 0 && <ReportSelector reportType={reportType} onChange={setReportType} onNext={() => reportType && nextStep()} />}
              {step === 1 && (
                <CriteriaForm criteria={criteria} onChange={setCriteria} onNext={() => (criteria.from && criteria.to) && nextStep()} />
              )}
              {step === 2 && <AdvancedFilters filters={filters} onChange={setFilters} onNext={handleGenerate} />}
              {step === 3 && (
                <ReportViewer html={report?.html} meta={report?.meta} loading={loading} onDownload={handleDownload} />
              )}

              {step > 0 && step < 3 && (
                <div className="mt-6 pt-6 border-t border-gray-200">
                  <button
                    onClick={prevStep}
                    className="text-gray-600 hover:text-gray-800 font-medium flex items-center gap-2 transition-all hover:gap-3"
                  >
                    ← Back
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
