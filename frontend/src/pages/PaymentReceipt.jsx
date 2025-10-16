import React from "react";

const PaymentReceipt = ({ paymentData, onBackToDashboard }) => {
  // Generate mock transaction details
  const transactionId = `TXN-${Date.now()}`;
  const timestamp = new Date().toLocaleString("en-US", {
    timeZone: "Asia/Colombo",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });

  const getPaymentMethodIcon = () => {
    const icons = {
      INSURANCE: "🛡️",
      CARD: "💳",
      CASH: "💵",
    };
    return icons[paymentData.paymentMethod] || "💳";
  };

  const getPaymentMethodName = () => {
    const names = {
      INSURANCE: "Insurance",
      CARD: "Credit/Debit Card",
      CASH: "Cash",
    };
    return names[paymentData.paymentMethod] || "Unknown";
  };

  const getHospitalTypeName = () => {
    const names = {
      GOVERNMENT: "Government Hospital",
      PRIVATE: "Private Hospital",
      CLINIC: "Private Clinic",
    };
    return names[paymentData.hospitalType] || "Unknown";
  };

  const handlePrint = () => {
    window.print();
  };

  const handleDownload = () => {
    // Create a simple text receipt for download
    const receiptContent = `
LankaMed Payment Receipt
========================

Transaction ID: ${transactionId}
Date & Time: ${timestamp}
Hospital Type: ${getHospitalTypeName()}
Payment Method: ${getPaymentMethodName()}
Amount: LKR ${paymentData.amount?.toFixed(2)}
Description: ${paymentData.description}

Status: PAID

Thank you for your payment!

LankaMed Healthcare System
Generated on: ${new Date().toLocaleDateString()}
    `;

    const blob = new Blob([receiptContent], { type: "text/plain" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `receipt-${transactionId}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="max-w-2xl mx-auto">
      {/* Success Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-green-100 rounded-full mb-6">
          <div className="text-4xl">✅</div>
        </div>

        <h2 className="text-3xl font-bold text-green-800 mb-4">
          Payment Successful!
        </h2>

        <p className="text-green-700 text-lg">
          Your payment has been processed successfully
        </p>
      </div>

      {/* Receipt Card */}
      <div className="bg-white border-2 border-green-200 rounded-lg p-8 mb-8 shadow-lg">
        <div className="text-center mb-6">
          <h3 className="text-2xl font-bold text-gray-800 mb-2">
            Payment Receipt
          </h3>
          <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 rounded-full">
            <div className="text-2xl">{getPaymentMethodIcon()}</div>
          </div>
        </div>

        {/* Receipt Details */}
        <div className="space-y-4">
          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Transaction ID:</span>
            <span className="font-mono text-sm bg-gray-100 px-3 py-1 rounded">
              {transactionId}
            </span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Date & Time:</span>
            <span className="text-gray-800">{timestamp}</span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Hospital Type:</span>
            <span className="text-gray-800">{getHospitalTypeName()}</span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Payment Method:</span>
            <span className="text-gray-800">{getPaymentMethodName()}</span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Amount:</span>
            <span className="text-2xl font-bold text-green-600">
              LKR {paymentData.amount?.toFixed(2)}
            </span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Description:</span>
            <span className="text-gray-800">{paymentData.description}</span>
          </div>

          <div className="flex justify-between items-center py-3 border-b border-gray-200">
            <span className="text-gray-600 font-medium">Status:</span>
            <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
              PAID
            </span>
          </div>

          {/* Payment Method Specific Details */}
          {paymentData.paymentMethod === "CARD" && (
            <div className="flex justify-between items-center py-3 border-b border-gray-200">
              <span className="text-gray-600 font-medium">Card:</span>
              <span className="font-mono text-sm">
                **** **** **** {paymentData.cardDetails.cardNumber.slice(-4)}
              </span>
            </div>
          )}

          {paymentData.paymentMethod === "INSURANCE" && (
            <div className="flex justify-between items-center py-3 border-b border-gray-200">
              <span className="text-gray-600 font-medium">Insurance ID:</span>
              <span className="font-mono text-sm">
                {paymentData.insuranceId}
              </span>
            </div>
          )}

          {paymentData.paymentMethod === "CASH" && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-center">
                <div className="text-blue-600 text-xl mr-3">💵</div>
                <div className="text-left">
                  <p className="text-blue-800 font-medium">
                    Cash Payment Reference
                  </p>
                  <p className="text-blue-700 text-sm">
                    Please present this receipt at the hospital reception
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          onClick={handlePrint}
          className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors duration-200 flex items-center justify-center"
        >
          🖨️ Print Receipt
        </button>

        <button
          onClick={handleDownload}
          className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 font-medium transition-colors duration-200 flex items-center justify-center"
        >
          📥 Download Receipt
        </button>

        <button
          onClick={onBackToDashboard}
          className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 font-medium transition-colors duration-200 flex items-center justify-center"
        >
          🏠 Back to Dashboard
        </button>
      </div>

      {/* Footer Message */}
      <div className="text-center mt-8 p-4 bg-gray-50 rounded-lg">
        <p className="text-gray-600 text-sm">
          Thank you for using LankaMed! If you have any questions about this
          payment, please contact our support team.
        </p>
      </div>
    </div>
  );
};

export default PaymentReceipt;
