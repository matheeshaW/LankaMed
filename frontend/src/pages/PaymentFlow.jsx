import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import PaymentForm from "./PaymentForm";
import PaymentProcessing from "./PaymentProcessing";
import PaymentReceipt from "./PaymentReceipt";
import PaymentError from "./PaymentError";

const PaymentFlow = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [paymentData, setPaymentData] = useState({
    hospitalType: "",
    paymentMethod: "",
    amount: 0,
    insuranceId: "",
    cardDetails: {
      cardNumber: "",
      expiryDate: "",
      cvv: "",
      cardholderName: "",
    },
    description: "",
  });

  const hospitalTypes = [
    {
      id: "GOVERNMENT",
      name: "Government Hospital",
      description: "Public healthcare facilities",
    },
    {
      id: "PRIVATE",
      name: "Private Hospital",
      description: "Private healthcare facilities",
    },
    {
      id: "CLINIC",
      name: "Private Clinic",
      description: "Specialized private clinics",
    },
  ];

  const paymentMethods = [
    {
      id: "INSURANCE",
      name: "Insurance",
      icon: "🛡️",
      description: "Pay with insurance coverage",
    },
    {
      id: "CARD",
      name: "Credit/Debit Card",
      icon: "💳",
      description: "Pay with card",
    },
    {
      id: "CASH",
      name: "Cash",
      icon: "💵",
      description: "Pay with cash at hospital",
    },
  ];

  // Handle pending bill data from PatientDashboard
  useEffect(() => {
    const pendingBillData = localStorage.getItem("pendingBillData");
    if (pendingBillData) {
      try {
        const billInfo = JSON.parse(pendingBillData);
        if (billInfo.fromPendingBills) {
          console.log("PaymentFlow: Found pending bill data:", billInfo);

          // Pre-populate form with bill information
          setPaymentData((prevData) => ({
            ...prevData,
            amount: billInfo.amount,
            description:
              billInfo.description || `Payment for ${billInfo.billId}`,
            appointmentId: billInfo.appointmentId,
          }));

          // Clear the stored data after using it
          localStorage.removeItem("pendingBillData");
        }
      } catch (error) {
        console.error("PaymentFlow: Error parsing pending bill data:", error);
      }
    }
  }, []);

  const handleHospitalTypeSelect = (hospitalType) => {
    setPaymentData({ ...paymentData, hospitalType });
    setCurrentStep(2);
  };

  const handlePaymentMethodSelect = (paymentMethod) => {
    setPaymentData({ ...paymentData, paymentMethod });
    setCurrentStep(3);
  };

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    } else {
      navigate("/patient/dashboard");
    }
  };

  const renderStepIndicator = () => (
    <div className="flex items-center justify-center mb-8">
      {[1, 2, 3, 4, 5].map((step) => (
        <div key={step} className="flex items-center">
          <div
            className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${
              step === currentStep
                ? "bg-blue-600 text-white"
                : step < currentStep
                ? "bg-green-600 text-white"
                : "bg-gray-300 text-gray-600"
            }`}
          >
            {step < currentStep ? "✓" : step}
          </div>
          {step < 5 && (
            <div
              className={`w-12 h-1 mx-2 ${
                step < currentStep ? "bg-green-600" : "bg-gray-300"
              }`}
            />
          )}
        </div>
      ))}
    </div>
  );

  const renderCurrentStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <HospitalTypeSelection
            hospitalTypes={hospitalTypes}
            onSelect={handleHospitalTypeSelect}
            onBack={handleBack}
          />
        );
      case 2:
        return (
          <PaymentMethodSelection
            paymentMethods={paymentMethods}
            onSelect={handlePaymentMethodSelect}
            onBack={handleBack}
          />
        );
      case 3:
        return (
          <PaymentForm
            paymentData={paymentData}
            setPaymentData={setPaymentData}
            onBack={handleBack}
            onSubmit={() => setCurrentStep(4)}
          />
        );
      case 4:
        return (
          <PaymentProcessing
            paymentData={paymentData}
            onSuccess={() => setCurrentStep(5)}
            onError={() => setCurrentStep(6)}
          />
        );
      case 5:
        return (
          <PaymentReceipt
            paymentData={paymentData}
            onBackToDashboard={() => navigate("/patient/dashboard")}
          />
        );
      case 6:
        return (
          <PaymentError onRetry={() => setCurrentStep(3)} onBack={handleBack} />
        );
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="bg-white rounded-lg shadow-lg p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              Make Payment
            </h1>
            <p className="text-gray-600">Complete your payment securely</p>
          </div>

          {renderStepIndicator()}
          {renderCurrentStep()}
        </div>
      </div>
    </div>
  );
};

// Hospital Type Selection Component
const HospitalTypeSelection = ({ hospitalTypes, onSelect, onBack }) => (
  <div>
    <h2 className="text-2xl font-semibold text-gray-800 mb-6">
      Select Hospital Type
    </h2>
    <div className="grid md:grid-cols-3 gap-6 mb-6">
      {hospitalTypes.map((type) => (
        <button
          key={type.id}
          onClick={() => onSelect(type.id)}
          className="p-6 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors duration-200 text-left"
        >
          <div className="text-4xl mb-4">🏥</div>
          <h3 className="text-lg font-semibold text-gray-800 mb-2">
            {type.name}
          </h3>
          <p className="text-gray-600 text-sm">{type.description}</p>
        </button>
      ))}
    </div>
    <div className="flex justify-start">
      <button
        onClick={onBack}
        className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
      >
        ← Back
      </button>
    </div>
  </div>
);

// Payment Method Selection Component
const PaymentMethodSelection = ({ paymentMethods, onSelect, onBack }) => (
  <div>
    <h2 className="text-2xl font-semibold text-gray-800 mb-6">
      Select Payment Method
    </h2>
    <div className="grid md:grid-cols-3 gap-6 mb-6">
      {paymentMethods.map((method) => (
        <button
          key={method.id}
          onClick={() => onSelect(method.id)}
          className="p-6 border-2 border-gray-200 rounded-lg hover:border-blue-500 hover:bg-blue-50 transition-colors duration-200 text-left"
        >
          <div className="text-4xl mb-4">{method.icon}</div>
          <h3 className="text-lg font-semibold text-gray-800 mb-2">
            {method.name}
          </h3>
          <p className="text-gray-600 text-sm">{method.description}</p>
        </button>
      ))}
    </div>
    <div className="flex justify-start">
      <button
        onClick={onBack}
        className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
      >
        ← Back
      </button>
    </div>
  </div>
);

export default PaymentFlow;
