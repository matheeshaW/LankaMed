import React, { useEffect } from "react";
import api from "../services/api";

const PaymentProcessing = ({ paymentData, onSuccess, onError }) => {
  useEffect(() => {
    const processPayment = async () => {
      try {
        // Simulate API call delay
        await new Promise((resolve) => setTimeout(resolve, 3000));

        // Here you would make the actual API call to process the payment
        // For now, we'll simulate different outcomes based on payment method

        if (paymentData.paymentMethod === "CARD") {
          // Simulate card payment processing
          const response = await simulateCardPayment(paymentData);
          if (response.success) {
            onSuccess();
          } else {
            onError();
          }
        } else if (paymentData.paymentMethod === "INSURANCE") {
          // Simulate insurance payment processing
          const response = await simulateInsurancePayment(paymentData);
          if (response.success) {
            onSuccess();
          } else {
            onError();
          }
        } else if (paymentData.paymentMethod === "CASH") {
          // For cash payments, just generate a reference number
          onSuccess();
        }
      } catch (error) {
        console.error("Payment processing error:", error);
        onError();
      }
    };

    processPayment();
  }, [paymentData, onSuccess, onError]);

  const simulateCardPayment = async (data) => {
    // Simulate different card scenarios
    const lastDigit = data.cardDetails.cardNumber.slice(-1);
    const shouldFail = lastDigit === "1" || lastDigit === "2"; // Fail for cards ending with 1 or 2

    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: !shouldFail,
          transactionId: `TXN-${Date.now()}`,
          message: shouldFail ? "Card declined by bank" : "Payment successful",
        });
      }, 2000);
    });
  };

  const simulateInsurancePayment = async (data) => {
    // Simulate insurance verification
    const validInsuranceIds = ["INS123456", "INS789012", "INS345678"];
    const isValidInsurance = validInsuranceIds.includes(data.insuranceId);

    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          success: isValidInsurance,
          transactionId: `TXN-${Date.now()}`,
          message: isValidInsurance
            ? "Insurance payment approved"
            : "Insurance ID not recognized",
        });
      }, 2000);
    });
  };

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

  return (
    <div className="text-center py-8">
      <div className="mb-8">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-blue-100 rounded-full mb-6">
          <div className="text-4xl animate-pulse">{getPaymentMethodIcon()}</div>
        </div>

        <h2 className="text-2xl font-semibold text-gray-800 mb-4">
          Processing Payment
        </h2>

        <div className="flex items-center justify-center space-x-2 text-gray-600">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
          <span>Please wait while we process your payment...</span>
        </div>
      </div>

      {/* Payment Summary */}
      <div className="bg-gray-50 rounded-lg p-6 mb-8 max-w-md mx-auto">
        <h3 className="text-lg font-medium text-gray-800 mb-4">
          Payment Summary
        </h3>

        <div className="space-y-3 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-600">Payment Method:</span>
            <span className="font-medium">{getPaymentMethodName()}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-gray-600">Amount:</span>
            <span className="font-medium">
              LKR {paymentData.amount?.toFixed(2)}
            </span>
          </div>

          <div className="flex justify-between">
            <span className="text-gray-600">Description:</span>
            <span className="font-medium">{paymentData.description}</span>
          </div>

          {paymentData.paymentMethod === "CARD" && (
            <div className="flex justify-between">
              <span className="text-gray-600">Card:</span>
              <span className="font-medium">
                **** **** **** {paymentData.cardDetails.cardNumber.slice(-4)}
              </span>
            </div>
          )}

          {paymentData.paymentMethod === "INSURANCE" && (
            <div className="flex justify-between">
              <span className="text-gray-600">Insurance ID:</span>
              <span className="font-medium">{paymentData.insuranceId}</span>
            </div>
          )}
        </div>
      </div>

      {/* Security Notice */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-4 max-w-md mx-auto">
        <div className="flex items-center">
          <div className="text-green-600 text-xl mr-3">🔒</div>
          <div className="text-left">
            <p className="text-green-800 font-medium">Secure Payment</p>
            <p className="text-green-700 text-sm">
              Your payment information is encrypted and secure
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentProcessing;
