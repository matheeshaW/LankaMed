import React, { useState, useEffect } from "react";
import api from "../services/api";

const PaymentForm = ({ paymentData, setPaymentData, onBack, onSubmit }) => {
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    // Check if we have pending bill data from the dashboard
    const pendingBillData = localStorage.getItem("pendingBillData");
    if (pendingBillData) {
      try {
        const billInfo = JSON.parse(pendingBillData);
        if (billInfo.fromPendingBills) {
          // Pre-populate form with bill information
          setPaymentData((prevData) => ({
            ...prevData,
            amount: billInfo.amount,
            description:
              billInfo.description || `Payment for ${billInfo.billId}`,
          }));

          // Clear the stored data after using it
          localStorage.removeItem("pendingBillData");
        }
      } catch (error) {
        console.error("Error parsing pending bill data:", error);
      }
    }
  }, [setPaymentData]);

  useEffect(() => {
    // Auto-fill payment form with dummy data for testing
    setPaymentData((prevData) => ({
      ...prevData,
      insuranceId: "INS123456", // Valid insurance ID for testing
      cardDetails: {
        cardNumber: "4532 1234 5678 9012", // Valid test card number
        expiryDate: "12/25", // Valid expiry date
        cvv: "123", // Valid CVV
        cardholderName: "Nirmana Herath", // Test cardholder name
      },
    }));
  }, []);

  const validateForm = () => {
    const newErrors = {};

    if (!paymentData.amount || paymentData.amount <= 0) {
      newErrors.amount = "Please enter a valid amount";
    }

    if (!paymentData.description.trim()) {
      newErrors.description = "Please provide a description";
    }

    if (paymentData.paymentMethod === "INSURANCE") {
      if (!paymentData.insuranceId.trim()) {
        newErrors.insuranceId = "Please enter your insurance ID";
      }
    }

    if (paymentData.paymentMethod === "CARD") {
      if (!paymentData.cardDetails.cardNumber.trim()) {
        newErrors.cardNumber = "Please enter your card number";
      } else if (
        !/^\d{16}$/.test(paymentData.cardDetails.cardNumber.replace(/\s/g, ""))
      ) {
        newErrors.cardNumber = "Please enter a valid 16-digit card number";
      }

      if (!paymentData.cardDetails.expiryDate.trim()) {
        newErrors.expiryDate = "Please enter expiry date";
      } else if (
        !/^(0[1-9]|1[0-2])\/\d{2}$/.test(paymentData.cardDetails.expiryDate)
      ) {
        newErrors.expiryDate = "Please enter expiry date in MM/YY format";
      }

      if (!paymentData.cardDetails.cvv.trim()) {
        newErrors.cvv = "Please enter CVV";
      } else if (!/^\d{3,4}$/.test(paymentData.cardDetails.cvv)) {
        newErrors.cvv = "Please enter a valid CVV";
      }

      if (!paymentData.cardDetails.cardholderName.trim()) {
        newErrors.cardholderName = "Please enter cardholder name";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const formatCardNumber = (value) => {
    const v = value.replace(/\s+/g, "").replace(/[^0-9]/gi, "");
    const matches = v.match(/\d{4,16}/g);
    const match = (matches && matches[0]) || "";
    const parts = [];
    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }
    if (parts.length) {
      return parts.join(" ");
    } else {
      return v;
    }
  };

  const formatExpiryDate = (value) => {
    const v = value.replace(/\s+/g, "").replace(/[^0-9]/gi, "");
    if (v.length >= 2) {
      return v.substring(0, 2) + "/" + v.substring(2, 4);
    }
    return v;
  };

  const handleInputChange = (field, value) => {
    if (field.startsWith("cardDetails.")) {
      const cardField = field.split(".")[1];
      setPaymentData({
        ...paymentData,
        cardDetails: {
          ...paymentData.cardDetails,
          [cardField]: value,
        },
      });
    } else {
      setPaymentData({
        ...paymentData,
        [field]: value,
      });
    }

    // Clear error when user starts typing
    if (errors[field]) {
      setErrors({
        ...errors,
        [field]: "",
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    // Check if dummy data has been modified
    if (!validateDummyData()) {
      setErrors({
        cardNumber: "Payment failed: Invalid payment information",
        insuranceId: "Payment failed: Invalid payment information",
      });
      return;
    }

    setIsSubmitting(true);
    try {
      // Here you would make the API call to process payment
      // For now, we'll simulate a successful payment
      await new Promise((resolve) => setTimeout(resolve, 2000));
      onSubmit();
    } catch (error) {
      console.error("Payment error:", error);
      // Handle payment error
    } finally {
      setIsSubmitting(false);
    }
  };

  const validateDummyData = () => {
    const correctDummyData = {
      insuranceId: "INS123456",
      cardDetails: {
        cardNumber: "4532 1234 5678 9012",
        expiryDate: "12/25",
        cvv: "123",
        cardholderName: "Nirmana Herath",
      },
    };

    // Check insurance payment data
    if (paymentData.paymentMethod === "INSURANCE") {
      if (paymentData.insuranceId !== correctDummyData.insuranceId) {
        return false;
      }
    }

    // Check card payment data
    if (paymentData.paymentMethod === "CARD") {
      if (
        paymentData.cardDetails.cardNumber !==
        correctDummyData.cardDetails.cardNumber
      ) {
        return false;
      }
      if (
        paymentData.cardDetails.expiryDate !==
        correctDummyData.cardDetails.expiryDate
      ) {
        return false;
      }
      if (paymentData.cardDetails.cvv !== correctDummyData.cardDetails.cvv) {
        return false;
      }
      if (
        paymentData.cardDetails.cardholderName !==
        correctDummyData.cardDetails.cardholderName
      ) {
        return false;
      }
    }

    return true;
  };

  const renderInsuranceForm = () => (
    <div className="space-y-6">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Insurance ID
        </label>
        <input
          type="text"
          value={paymentData.insuranceId}
          onChange={(e) => handleInputChange("insuranceId", e.target.value)}
          className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.insuranceId ? "border-red-500" : "border-gray-300"
          }`}
          placeholder="Enter your insurance ID"
        />
        {errors.insuranceId && (
          <p className="mt-1 text-sm text-red-600">{errors.insuranceId}</p>
        )}
      </div>
    </div>
  );

  const renderCardForm = () => (
    <div className="space-y-6">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Card Number
        </label>
        <div className="relative">
          <input
            type="text"
            value={paymentData.cardDetails.cardNumber}
            onChange={(e) =>
              handleInputChange(
                "cardDetails.cardNumber",
                formatCardNumber(e.target.value)
              )
            }
            className={`w-full px-4 py-3 pr-12 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.cardNumber ? "border-red-500" : "border-gray-300"
            }`}
            placeholder="1234 5678 9012 3456"
            maxLength="19"
          />
          <div className="absolute right-3 top-3 flex space-x-2">
            <div className="w-8 h-6 bg-blue-600 rounded text-white text-xs flex items-center justify-center">
              V
            </div>
            <div className="w-8 h-6 bg-red-600 rounded text-white text-xs flex items-center justify-center">
              M
            </div>
          </div>
        </div>
        {errors.cardNumber && (
          <p className="mt-1 text-sm text-red-600">{errors.cardNumber}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Expiry Date
          </label>
          <input
            type="text"
            value={paymentData.cardDetails.expiryDate}
            onChange={(e) =>
              handleInputChange(
                "cardDetails.expiryDate",
                formatExpiryDate(e.target.value)
              )
            }
            className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.expiryDate ? "border-red-500" : "border-gray-300"
            }`}
            placeholder="MM/YY"
            maxLength="5"
          />
          {errors.expiryDate && (
            <p className="mt-1 text-sm text-red-600">{errors.expiryDate}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            CVV
          </label>
          <input
            type="text"
            value={paymentData.cardDetails.cvv}
            onChange={(e) =>
              handleInputChange(
                "cardDetails.cvv",
                e.target.value.replace(/\D/g, "")
              )
            }
            className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.cvv ? "border-red-500" : "border-gray-300"
            }`}
            placeholder="123"
            maxLength="4"
          />
          {errors.cvv && (
            <p className="mt-1 text-sm text-red-600">{errors.cvv}</p>
          )}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Cardholder Name
        </label>
        <input
          type="text"
          value={paymentData.cardDetails.cardholderName}
          onChange={(e) =>
            handleInputChange("cardDetails.cardholderName", e.target.value)
          }
          className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
            errors.cardholderName ? "border-red-500" : "border-gray-300"
          }`}
          placeholder="John Doe"
        />
        {errors.cardholderName && (
          <p className="mt-1 text-sm text-red-600">{errors.cardholderName}</p>
        )}
      </div>
    </div>
  );

  const renderCashForm = () => (
    <div className="bg-blue-50 border-l-4 border-blue-400 p-4 rounded-r-lg">
      <div className="flex">
        <div className="text-blue-600 text-2xl mr-3">💵</div>
        <div>
          <h3 className="text-lg font-medium text-blue-800">Cash Payment</h3>
          <p className="text-blue-700 mt-1">
            You have selected to pay with cash. Please visit the hospital
            reception to complete your payment. A reference number will be
            generated for you.
          </p>
        </div>
      </div>
    </div>
  );

  const getPaymentMethodName = () => {
    const methods = {
      INSURANCE: "Insurance",
      CARD: "Credit/Debit Card",
      CASH: "Cash",
    };
    return methods[paymentData.paymentMethod] || "Unknown";
  };

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-800 mb-2">
          Payment Details
        </h2>
        <p className="text-gray-600">
          Payment Method:{" "}
          <span className="font-medium">{getPaymentMethodName()}</span>
        </p>

        {/* Test Data Notice */}
        <div className="mt-4 bg-yellow-50 border border-yellow-200 rounded-lg p-3">
          <div className="flex items-center">
            <div className="text-yellow-600 text-lg mr-2">⚠️</div>
            <div className="text-sm">
              <p className="text-yellow-800 font-medium">Test Environment</p>
              <p className="text-yellow-700">
                Form is pre-filled with test data. Payment will only succeed
                with original test values.
              </p>
            </div>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Amount and Description */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Amount (LKR)
            </label>
            <input
              type="number"
              value={paymentData.amount === 0 ? "" : paymentData.amount}
              onChange={(e) => {
                const value = e.target.value;
                const numValue = value === "" ? 0 : parseFloat(value) || 0;
                handleInputChange("amount", numValue);
              }}
              className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.amount ? "border-red-500" : "border-gray-300"
              }`}
              placeholder="0.00"
              step="0.01"
              min="0"
            />
            {errors.amount && (
              <p className="mt-1 text-sm text-red-600">{errors.amount}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description
            </label>
            <input
              type="text"
              value={paymentData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.description ? "border-red-500" : "border-gray-300"
              }`}
              placeholder="Payment description"
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-600">{errors.description}</p>
            )}
          </div>
        </div>

        {/* Payment Method Specific Form */}
        {paymentData.paymentMethod === "INSURANCE" && renderInsuranceForm()}
        {paymentData.paymentMethod === "CARD" && renderCardForm()}
        {paymentData.paymentMethod === "CASH" && renderCashForm()}

        {/* Form Actions */}
        <div className="flex justify-between pt-6">
          <button
            type="button"
            onClick={onBack}
            className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
          >
            ← Back
          </button>

          {paymentData.paymentMethod !== "CASH" && (
            <button
              type="submit"
              disabled={isSubmitting}
              className={`px-8 py-3 rounded-lg font-medium transition-colors duration-200 ${
                isSubmitting
                  ? "bg-gray-400 text-gray-200 cursor-not-allowed"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }`}
            >
              {isSubmitting ? "Processing..." : "Proceed to Payment"}
            </button>
          )}

          {paymentData.paymentMethod === "CASH" && (
            <button
              type="button"
              onClick={onSubmit}
              className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors duration-200"
            >
              Generate Reference
            </button>
          )}
        </div>
      </form>
    </div>
  );
};

export default PaymentForm;
