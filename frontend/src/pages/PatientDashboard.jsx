import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getRole, logout } from "../utils/auth";
import PersonalInformationCard from "../components/patient/PersonalInformationCard";
import EmergencyContactCard from "../components/patient/EmergencyContactCard";
import MedicalHistoryCard from "../components/patient/MedicalHistoryCard";
import HealthMetricsCard from "../components/patient/HealthMetricsCard";
import DownloadReportsButton from "../components/patient/DownloadReportsButton";
import UserAppointments from "../components/patient/UserAppointments";
import WaitingListCard from "../components/patient/WaitingListCard";
import { patientAPI, paymentAPI, appointmentAPI } from "../services/api";

const PatientDashboard = () => {
  const navigate = useNavigate();
  const [pendingBills, setPendingBills] = useState([]);
  const [patientProfile, setPatientProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  useEffect(() => {
    const fetchConfirmedAppointments = async () => {
      try {
        setLoading(true);
        setError(null);

        // Get patient profile - no fallback data
        const profileResponse = await patientAPI.getMyProfile();
        const currentProfile = profileResponse.data;
        setPatientProfile(currentProfile);

        // Then get confirmed appointments for the patient
        if (currentProfile && currentProfile.patientId) {
          try {
            // Use the existing appointmentAPI to get patient appointments
            const appointmentsResponse =
              await appointmentAPI.getPatientAppointments();
            const appointmentsData = appointmentsResponse.data;

            if (Array.isArray(appointmentsData)) {
              console.log(
                "PatientDashboard: Received appointments data:",
                appointmentsData
              );

              // Filter for confirmed appointments and use actual payment amounts
              const confirmedAppointments = appointmentsData
                .filter((apt) => apt.status === "CONFIRMED")
                .map((apt) => {
                  console.log("PatientDashboard: Processing appointment:", {
                    appointmentId: apt.appointmentId,
                    paymentAmount: apt.paymentAmount,
                    doctorFee: apt.doctorFee,
                    status: apt.status,
                  });

                  return {
                    ...apt,
                    amount: apt.paymentAmount || apt.doctorFee || 1500, // Use actual payment amount, doctor's fee, or fallback
                    paymentMethod: "Medical Service",
                    paymentTimestamp: new Date().toISOString(),
                  };
                });
              console.log(
                "PatientDashboard: Confirmed appointments for billing:",
                confirmedAppointments
              );
              setPendingBills(confirmedAppointments);
            } else if (
              appointmentsData &&
              appointmentsData.success &&
              Array.isArray(appointmentsData.appointments)
            ) {
              // Handle case where response is wrapped in success object
              const confirmedAppointments = appointmentsData.appointments
                .filter((apt) => apt.status === "CONFIRMED")
                .map((apt) => ({
                  ...apt,
                  amount: apt.paymentAmount || apt.doctorFee || 1500, // Use actual payment amount, doctor's fee, or fallback
                  paymentMethod: "Medical Service",
                  paymentTimestamp: new Date().toISOString(),
                }));
              setPendingBills(confirmedAppointments);
            } else {
              throw new Error("No appointments data");
            }
          } catch (appointmentError) {
            console.error("Error fetching appointments:", appointmentError);
            // Set empty array instead of mock data
            setPendingBills([]);
            setError(
              "Failed to load appointments. Please try refreshing the page."
            );
          }
        } else {
          setError(
            "No patient profile available. Please ensure you're logged in or contact support."
          );
          setPendingBills([]);
        }
      } catch (err) {
        console.error("Error fetching confirmed appointments:", err);
        console.error("Error response:", err.response?.data);
        console.error("Error status:", err.response?.status);

        if (err.response?.status === 401) {
          setError("Authentication failed. Please log in again.");
          // Redirect to login after a short delay
          setTimeout(() => {
            localStorage.removeItem("token");
            navigate("/login");
          }, 2000);
        } else if (err.response?.status === 403) {
          setError(
            "Access denied. You don't have permission to view this data."
          );
        } else if (err.response?.status === 500) {
          setError(
            "Server error while loading appointments. Please check if the backend server is running."
          );
        } else if (err.response?.status === 404) {
          setError("Patient not found or no confirmed appointments available.");
        } else {
          setError(
            "Failed to load confirmed appointments. Please try again later."
          );
        }
        // Set empty array as fallback
        setPendingBills([]);
      } finally {
        setLoading(false);
      }
    };

    fetchConfirmedAppointments();
  }, []);

  const handleMakePayment = () => {
    navigate("/patient/payment");
  };

  const handlePayBill = (billId, amount, description, appointmentId) => {
    // Store bill details in localStorage to pass to payment flow
    localStorage.setItem(
      "pendingBillData",
      JSON.stringify({
        billId,
        amount,
        description,
        appointmentId,
        fromPendingBills: true,
      })
    );
    console.log("Navigating to payment with data:", {
      billId,
      amount,
      description,
      appointmentId,
    });
    navigate("/patient/payment");
  };

  // Calculate total amount from pending bills
  const totalAmount = pendingBills.reduce((sum, bill) => sum + bill.amount, 0);

  // Format amount to LKR currency
  const formatCurrency = (amount) => {
    return `LKR ${amount.toLocaleString("en-US", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
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
                  <div className="text-2xl mr-3">📅</div>
                  <div>
                    <h3 className="text-lg font-semibold text-gray-800">
                      Pending Bills
                    </h3>
                    <p className="text-gray-600 text-sm">
                      Upcoming appointments with payment due
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xl font-bold text-blue-600">
                    {formatCurrency(totalAmount)}
                  </div>
                  <div className="text-xs text-gray-600">Total Amount Due</div>
                </div>
              </div>

              {/* Loading State */}
              {loading && (
                <div className="text-center py-4">
                  <div className="text-gray-600">Loading Pending Bills...</div>
                </div>
              )}

              {/* Error State */}
              {error && (
                <div className="text-center py-4">
                  <div className="text-red-600 text-sm">{error}</div>
                </div>
              )}

              {/* Appointments List - Dynamic Version */}
              {!loading && !error && (
                <div className="space-y-3">
                  {pendingBills.length === 0 ? (
                    <div className="text-center py-4">
                      <div className="text-gray-600 text-sm">
                        No Pending Bills
                      </div>
                    </div>
                  ) : (
                    pendingBills.map((bill, index) => (
                      <div
                        key={bill.transactionId || index}
                        className={`p-3 rounded-lg border ${
                          index < pendingBills.length - 1
                            ? "border-gray-100"
                            : ""
                        }`}
                      >
                        <div className="flex justify-between items-start">
                          <div className="flex-1">
                            <div className="font-medium text-gray-800 text-sm mb-1">
                              {bill.appointmentDescription ||
                                bill.description ||
                                `Payment ${bill.transactionId || ""}`}
                            </div>

                            {/* Appointment Details */}
                            {bill.appointmentDateTime && (
                              <div className="text-xs text-blue-600 mb-1">
                                📅{" "}
                                {new Date(
                                  bill.appointmentDateTime
                                ).toLocaleDateString()}{" "}
                                at{" "}
                                {new Date(
                                  bill.appointmentDateTime
                                ).toLocaleTimeString([], {
                                  hour: "2-digit",
                                  minute: "2-digit",
                                })}
                              </div>
                            )}

                            <div className="text-xs text-gray-600 mb-1">
                              🏥 {bill.hospitalName || "Hospital"}
                              {bill.doctorName &&
                                ` • 👨‍⚕️ Dr. ${bill.doctorName}`}
                            </div>

                            <div className="text-xs text-gray-600">
                              💳 {bill.paymentMethod || "Medical Service"} •{" "}
                              {bill.paymentTimestamp
                                ? new Date(
                                    bill.paymentTimestamp
                                  ).toLocaleDateString()
                                : "Recent"}
                            </div>
                          </div>

                          <div className="text-right ml-4">
                            <div className="text-sm font-bold text-red-600 mb-2">
                              {formatCurrency(bill.amount)}
                            </div>
                            <button
                              onClick={() =>
                                handlePayBill(
                                  bill.transactionId || `BILL-${index}`,
                                  bill.amount,
                                  bill.appointmentDescription ||
                                    bill.description ||
                                    `Payment ${bill.transactionId || ""}`,
                                  bill.appointmentId
                                )
                              }
                              className="text-xs bg-blue-600 text-white px-3 py-2 rounded hover:bg-blue-700 transition-colors duration-200"
                            >
                              Pay Now
                            </button>
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              )}
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
