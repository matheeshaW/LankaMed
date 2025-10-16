import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import AdminDashboard from "./pages/AdminDashboard";
import PatientDashboard from "./pages/PatientDashboard";
import PaymentFlow from "./pages/PaymentFlow";
import Navbar from "./components/Navbar";

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
        <Navbar />
        <Routes>
          <Route
            path="/"
            element={
              <div className="min-h-screen flex items-center justify-center">
                <div className="text-center max-w-4xl mx-auto px-6">
                  <h1 className="text-6xl font-bold text-gray-800 mb-6">
                    Welcome to <span className="text-blue-600">LankaMed</span>
                  </h1>
                  <p className="text-xl text-gray-600 mb-8 leading-relaxed">
                    Your trusted healthcare companion. Manage your medical
                    records, appointments, and health information in one secure
                    platform.
                  </p>
                  <div className="grid md:grid-cols-3 gap-8 mt-12">
                    <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
                      <div className="text-4xl mb-4">🏥</div>
                      <h3 className="text-xl font-semibold text-gray-800 mb-2">
                        Medical Records
                      </h3>
                      <p className="text-gray-600">
                        Keep track of your medical conditions, allergies, and
                        prescriptions.
                      </p>
                    </div>
                    <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
                      <div className="text-4xl mb-4">📅</div>
                      <h3 className="text-xl font-semibold text-gray-800 mb-2">
                        Appointments
                      </h3>
                      <p className="text-gray-600">
                        View your appointment history and manage your healthcare
                        schedule.
                      </p>
                    </div>
                    <div className="bg-white p-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow duration-300">
                      <div className="text-4xl mb-4">🔒</div>
                      <h3 className="text-xl font-semibold text-gray-800 mb-2">
                        Secure & Private
                      </h3>
                      <p className="text-gray-600">
                        Your health information is protected with
                        enterprise-grade security.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            }
          />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route
            path="/patient"
            element={<Navigate to="/patient/dashboard" replace />}
          />
          <Route path="/patient/dashboard" element={<PatientDashboard />} />
          <Route path="/patient/payment" element={<PaymentFlow />} />
          <Route path="/admin/*" element={<AdminDashboard />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
