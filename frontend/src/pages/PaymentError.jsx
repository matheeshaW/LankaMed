import React from "react";

const PaymentError = ({ onRetry, onBack }) => {
  // Mock error scenarios
  const errorScenarios = {
    CARD: [
      {
        title: "Card Declined",
        message:
          "Your card was declined by the bank. Please try a different card or contact your bank.",
        suggestion:
          "Try using a different payment method or contact your bank for assistance.",
      },
      {
        title: "Insufficient Funds",
        message:
          "There are insufficient funds in your account to complete this transaction.",
        suggestion:
          "Please check your account balance or try a different payment method.",
      },
      {
        title: "Card Expired",
        message: "The card you entered has expired. Please use a valid card.",
        suggestion:
          "Check your card's expiry date and try again with a valid card.",
      },
      {
        title: "Network Error",
        message:
          "There was a network error while processing your payment. Please try again.",
        suggestion: "Check your internet connection and try again.",
      },
    ],
    INSURANCE: [
      {
        title: "Insurance Not Recognized",
        message:
          "The insurance ID you entered is not recognized in our system.",
        suggestion:
          "Please verify your insurance ID and try again, or contact your insurance provider.",
      },
      {
        title: "Coverage Exceeded",
        message:
          "Your insurance coverage limit has been exceeded for this service.",
        suggestion:
          "Contact your insurance provider or try a different payment method.",
      },
      {
        title: "Insurance Verification Failed",
        message:
          "We could not verify your insurance details. Please try again.",
        suggestion:
          "Double-check your insurance information or contact your provider.",
      },
    ],
    CASH: [
      {
        title: "Reference Generation Failed",
        message: "We could not generate a payment reference at this time.",
        suggestion: "Please try again or contact support for assistance.",
      },
    ],
  };

  // Select random error based on payment method
  const getRandomError = (paymentMethod) => {
    const errors = errorScenarios[paymentMethod] || errorScenarios.CARD;
    return errors[Math.floor(Math.random() * errors.length)];
  };

  const error = getRandomError("CARD"); // Default to CARD for demo

  const getPaymentMethodIcon = (paymentMethod) => {
    const icons = {
      INSURANCE: "🛡️",
      CARD: "💳",
      CASH: "💵",
    };
    return icons[paymentMethod] || "💳";
  };

  const getPaymentMethodName = (paymentMethod) => {
    const names = {
      INSURANCE: "Insurance",
      CARD: "Credit/Debit Card",
      CASH: "Cash",
    };
    return names[paymentMethod] || "Unknown";
  };

  return (
    <div className="max-w-2xl mx-auto">
      {/* Error Header */}
      <div className="text-center mb-8">
        <div className="inline-flex items-center justify-center w-20 h-20 bg-red-100 rounded-full mb-6">
          <div className="text-4xl">❌</div>
        </div>

        <h2 className="text-3xl font-bold text-red-800 mb-4">Payment Failed</h2>

        <p className="text-red-700 text-lg">We couldn't process your payment</p>
      </div>

      {/* Error Details Card */}
      <div className="bg-white border-2 border-red-200 rounded-lg p-8 mb-8 shadow-lg">
        <div className="text-center mb-6">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-red-100 rounded-full mb-4">
            <div className="text-2xl">💳</div>
          </div>

          <h3 className="text-2xl font-bold text-gray-800 mb-2">
            {error.title}
          </h3>
        </div>

        {/* Error Message */}
        <div className="bg-red-50 border-l-4 border-red-400 p-4 mb-6">
          <div className="flex">
            <div className="text-red-600 text-xl mr-3">⚠️</div>
            <div className="text-left">
              <p className="text-red-800 font-medium">Error Details</p>
              <p className="text-red-700 mt-1">{error.message}</p>
            </div>
          </div>
        </div>

        {/* Suggestion */}
        <div className="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
          <div className="flex">
            <div className="text-blue-600 text-xl mr-3">💡</div>
            <div className="text-left">
              <p className="text-blue-800 font-medium">Suggestion</p>
              <p className="text-blue-700 mt-1">{error.suggestion}</p>
            </div>
          </div>
        </div>

        {/* What You Can Do */}
        <div className="bg-gray-50 rounded-lg p-6">
          <h4 className="text-lg font-semibold text-gray-800 mb-4">
            What you can do:
          </h4>

          <ul className="space-y-3 text-gray-700">
            <li className="flex items-start">
              <div className="text-green-600 mr-3">✓</div>
              <span>Try again with the same payment method</span>
            </li>
            <li className="flex items-start">
              <div className="text-green-600 mr-3">✓</div>
              <span>Use a different payment method</span>
            </li>
            <li className="flex items-start">
              <div className="text-green-600 mr-3">✓</div>
              <span>Contact your bank or insurance provider</span>
            </li>
            <li className="flex items-start">
              <div className="text-green-600 mr-3">✓</div>
              <span>Contact LankaMed support for assistance</span>
            </li>
          </ul>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <button
          onClick={onRetry}
          className="px-8 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors duration-200 flex items-center justify-center"
        >
          🔄 Try Again
        </button>

        <button
          onClick={onBack}
          className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 font-medium transition-colors duration-200 flex items-center justify-center"
        >
          ← Back to Payment Options
        </button>
      </div>

      {/* Support Information */}
      <div className="text-center mt-8 p-4 bg-gray-50 rounded-lg">
        <h4 className="font-semibold text-gray-800 mb-2">Need Help?</h4>
        <p className="text-gray-600 text-sm mb-2">
          If you continue to experience issues, please contact our support team:
        </p>
        <div className="text-sm text-gray-600">
          <p>📞 Phone: +94 (11) 123-4567</p>
          <p>📧 Email: support@lankamed.lk</p>
          <p>💬 Live Chat: Available 24/7</p>
        </div>
      </div>

      {/* Security Notice */}
      <div className="text-center mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
        <div className="flex items-center justify-center">
          <div className="text-green-600 text-xl mr-3">🔒</div>
          <div className="text-left">
            <p className="text-green-800 font-medium">
              Your Information is Safe
            </p>
            <p className="text-green-700 text-sm">
              No payment information was stored due to this error
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentError;
