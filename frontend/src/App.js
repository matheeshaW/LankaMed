import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";
import ProfilePage from "./pages/ProfilePage";
import AdminDashboard from "./pages/AdminDashboard";
import PatientDashboard from "./pages/PatientDashboard";
import AppointmentsPage from "./pages/AppointmentsPage";
import PaymentFlow from "./pages/PaymentFlow";
import Navbar from "./components/Navbar";
import { getRole, isLoggedIn } from "./utils/auth";

// Component to handle root path redirects
const RootRedirect = () => {
  const role = getRole();
  const loggedIn = isLoggedIn();

  console.log("RootRedirect - loggedIn:", loggedIn, "role:", role);

  if (!loggedIn) {
    console.log("Not logged in, redirecting to login");
    return <Navigate to="/login" replace />;
  }

  if (role === "ADMIN") {
    console.log("Admin user, redirecting to admin dashboard");
    return <Navigate to="/admin" replace />;
  } else if (role === "PATIENT") {
    console.log("Patient user, redirecting to patient dashboard");
    return <Navigate to="/patient" replace />;
  }

  console.log("Unknown role, redirecting to login");
  return <Navigate to="/login" replace />;
};

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gradient-to-br from-gray-50 to-blue-50">
        <Navbar />
        <Routes>
          <Route path="/" element={<RootRedirect />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
          <Route path="/patient/payment" element={<PaymentFlow />} />
          <Route path="/patient/*" element={<PatientDashboard />} />
          <Route path="/admin/*" element={<AdminDashboard />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
