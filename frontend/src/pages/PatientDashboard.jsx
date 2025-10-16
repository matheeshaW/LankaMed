import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getRole, logout } from "../utils/auth";
import PersonalInformationCard from "../components/patient/PersonalInformationCard";
import EmergencyContactCard from "../components/patient/EmergencyContactCard";
import MedicalHistoryCard from "../components/patient/MedicalHistoryCard";
import HealthMetricsCard from "../components/patient/HealthMetricsCard";

import DownloadReportsButton from "../components/patient/DownloadReportsButton";
import UserAppointments from "../components/patient/UserAppointments";
import WaitingListCard from "../components/patient/WaitingListCard";

const PatientDashboard = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const role = getRole();
    const isLoggedIn = Boolean(localStorage.getItem("token"));
    console.log(
      "PatientDashboard - checking role:",
      role,
      "isLoggedIn:",
      isLoggedIn
    );

    if (!isLoggedIn) {
      console.log("Not logged in, redirecting to login");
      navigate("/login");
    } else if (role && role !== "PATIENT") {
      console.log("Not a patient, redirecting to appropriate dashboard");
      if (role === "ADMIN") {
        navigate("/admin");
      } else {
        navigate("/login");
      }
    }
  }, [navigate]);

  const handleMakePayment = () => {
    navigate("/patient/payment");
  };

  const handlePayBill = (billId, amount, description) => {
    // Store bill details in localStorage to pass to payment flow
    localStorage.setItem(
      "pendingBillData",
      JSON.stringify({
        billId,
        amount,
        description,
        fromPendingBills: true,
      })
    );
    navigate("/patient/payment");
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Two Column Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column - Personal Information + Emergency Contact */}
          <div className="lg:col-span-1 space-y-8">
            <PersonalInformationCard />
            <EmergencyContactCard />
            <DownloadReportsButton />

            {/* Pending Bills Card */}
            <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow duration-200">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center">
                  <div className="text-2xl mr-3">📋</div>
                  <div>
                    <h3 className="text-lg font-semibold text-gray-800">
                      Pending Bills
                    </h3>
                    <p className="text-gray-600 text-sm">
                      Outstanding payments
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xl font-bold text-red-600">
                    LKR 15,750.00
                  </div>
                  <div className="text-xs text-gray-600">Total Outstanding</div>
                </div>
              </div>

              {/* Bills List - Compact Version */}
              <div className="space-y-3">
                {/* Bill 1 */}
                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                  <div className="flex-1">
                    <div className="font-medium text-gray-800 text-sm">
                      General Consultation
                    </div>
                    <div className="text-xs text-gray-600">
                      Dr. Smith • Mar 15
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-red-600">
                      LKR 3,500.00
                    </div>
                    <button
                      onClick={() =>
                        handlePayBill(
                          "BM-2024-00345",
                          3500,
                          "General Consultation - Dr. Smith"
                        )
                      }
                      className="text-xs bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                    >
                      Pay
                    </button>
                  </div>
                </div>

                {/* Bill 2 */}
                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                  <div className="flex-1">
                    <div className="font-medium text-gray-800 text-sm">
                      Blood Test
                    </div>
                    <div className="text-xs text-gray-600">
                      Lab Services • Mar 12
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-red-600">
                      LKR 2,250.00
                    </div>
                    <button
                      onClick={() =>
                        handlePayBill(
                          "BM-2024-00342",
                          2250,
                          "Blood Test - Laboratory Services"
                        )
                      }
                      className="text-xs bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                    >
                      Pay
                    </button>
                  </div>
                </div>

                {/* Bill 3 */}
                <div className="flex justify-between items-center py-2 border-b border-gray-100">
                  <div className="flex-1">
                    <div className="font-medium text-gray-800 text-sm">
                      ECG Test
                    </div>
                    <div className="text-xs text-gray-600">
                      Cardiology • Mar 10
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-red-600">
                      LKR 4,200.00
                    </div>
                    <button
                      onClick={() =>
                        handlePayBill(
                          "BM-2024-00338",
                          4200,
                          "ECG Test - Cardiology Department"
                        )
                      }
                      className="text-xs bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                    >
                      Pay
                    </button>
                  </div>
                </div>

                {/* Bill 4 */}
                <div className="flex justify-between items-center py-2">
                  <div className="flex-1">
                    <div className="font-medium text-gray-800 text-sm">
                      X-Ray
                    </div>
                    <div className="text-xs text-gray-600">
                      Radiology • Mar 8
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-sm font-bold text-red-600">
                      LKR 5,800.00
                    </div>
                    <button
                      onClick={() =>
                        handlePayBill(
                          "BM-2024-00335",
                          5800,
                          "X-Ray - Radiology Department"
                        )
                      }
                      className="text-xs bg-blue-600 text-white px-2 py-1 rounded hover:bg-blue-700"
                    >
                      Pay
                    </button>
                  </div>
                </div>
              </div>

              {/* Footer */}
              <div className="mt-4 pt-3 border-t border-gray-200 text-center">
                <div className="text-xs text-gray-600 mb-2">
                  4 pending payments • Due within 30 days
                </div>
                <button className="w-full bg-gray-100 text-gray-700 py-2 px-4 rounded-lg hover:bg-gray-200 text-sm font-medium transition-colors duration-200">
                  View All Bills
                </button>
              </div>
            </div>
          </div>

          {/* Middle-Right Column - Medical History */}
          <div className="lg:col-span-2">
            <MedicalHistoryCard />
          </div>
        </div>
      </div>
    </div>
  );
};

export default PatientDashboard;
