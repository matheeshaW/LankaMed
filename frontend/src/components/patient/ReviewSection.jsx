import React, { useState } from 'react';
import { addReview, getReviewsByDoctorId } from '../../data/mockData';

const ReviewSection = ({ appointment, onClose, onReviewSubmitted }) => {
  const [reviewData, setReviewData] = useState({
    rating: 5,
    comment: ''
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [existingReviews, setExistingReviews] = useState([]);

  React.useEffect(() => {
    if (appointment) {
      const reviews = getReviewsByDoctorId(appointment.doctorId);
      setExistingReviews(reviews);
    }
  }, [appointment]);

  const handleInputChange = (field, value) => {
    setReviewData(prev => ({
      ...prev,
      [field]: value
    }));
    
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({
        ...prev,
        [field]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (reviewData.rating < 1 || reviewData.rating > 5) {
      newErrors.rating = 'Please select a rating between 1 and 5';
    }

    if (!reviewData.comment.trim()) {
      newErrors.comment = 'Please write a review comment';
    } else if (reviewData.comment.trim().length < 10) {
      newErrors.comment = 'Review comment must be at least 10 characters long';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    
    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const reviewDataToSubmit = {
        appointmentId: appointment.id,
        doctorId: appointment.doctorId,
        patientName: appointment.patientName,
        rating: reviewData.rating,
        comment: reviewData.comment.trim()
      };

      addReview(reviewDataToSubmit);
      
      onReviewSubmitted();
    } catch (error) {
      console.error('Error submitting review:', error);
      setErrors({ submit: 'Failed to submit review. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const renderStars = (rating, interactive = false, onStarClick = null) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(
        <button
          key={i}
          type={interactive ? "button" : undefined}
          onClick={interactive && onStarClick ? () => onStarClick(i) : undefined}
          className={`text-2xl ${
            i <= rating 
              ? 'text-yellow-400' 
              : 'text-gray-300'
          } ${interactive ? 'hover:text-yellow-300 transition-colors duration-200' : ''}`}
          disabled={!interactive}
        >
          ★
        </button>
      );
    }
    return stars;
  };

  const getRatingText = (rating) => {
    switch (rating) {
      case 1: return 'Poor';
      case 2: return 'Fair';
      case 3: return 'Good';
      case 4: return 'Very Good';
      case 5: return 'Excellent';
      default: return '';
    }
  };

  const getAverageRating = () => {
    if (existingReviews.length === 0) return 0;
    const total = existingReviews.reduce((sum, review) => sum + review.rating, 0);
    return (total / existingReviews.length).toFixed(1);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="bg-gradient-to-r from-green-600 to-emerald-600 p-6 rounded-t-2xl">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-white mb-2">Rate Your Experience</h2>
              <p className="text-green-100">Help others by sharing your experience with this doctor</p>
            </div>
            <button
              onClick={onClose}
              className="text-white hover:text-gray-200 transition-colors duration-200"
            >
              <svg className="h-8 w-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <div className="p-6">
          {/* Doctor Info */}
          <div className="bg-gray-50 rounded-xl p-6 mb-6">
            <div className="flex items-center space-x-4">
              <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center text-2xl">
                👨‍⚕️
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-800">{appointment.doctorName}</h3>
                <p className="text-blue-600 font-semibold">{appointment.doctorSpecialization}</p>
                <p className="text-gray-600">{appointment.hospitalName}</p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Review Form */}
            <div>
              <h3 className="text-lg font-semibold text-gray-800 mb-6">Write Your Review</h3>
              
              <form onSubmit={handleSubmit} className="space-y-6">
                {errors.submit && (
                  <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                    {errors.submit}
                  </div>
                )}

                {/* Rating */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Overall Rating *
                  </label>
                  <div className="flex items-center space-x-2">
                    <div className="flex space-x-1">
                      {renderStars(reviewData.rating, true, (rating) => 
                        handleInputChange('rating', rating)
                      )}
                    </div>
                    <span className="text-lg font-semibold text-gray-800 ml-3">
                      {getRatingText(reviewData.rating)}
                    </span>
                  </div>
                  {errors.rating && (
                    <p className="text-red-500 text-sm mt-1">{errors.rating}</p>
                  )}
                </div>

                {/* Comment */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-3">
                    Your Review *
                  </label>
                  <textarea
                    value={reviewData.comment}
                    onChange={(e) => handleInputChange('comment', e.target.value)}
                    rows={6}
                    className={`w-full px-4 py-3 border rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent resize-none ${
                      errors.comment ? 'border-red-500' : 'border-gray-300'
                    }`}
                    placeholder="Share your experience with this doctor. What did you like? What could be improved?"
                  />
                  <div className="flex justify-between items-center mt-2">
                    {errors.comment && (
                      <p className="text-red-500 text-sm">{errors.comment}</p>
                    )}
                    <p className="text-gray-500 text-sm ml-auto">
                      {reviewData.comment.length} characters
                    </p>
                  </div>
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full px-6 py-3 bg-gradient-to-r from-green-600 to-emerald-600 text-white rounded-lg hover:from-green-700 hover:to-emerald-700 transition-all duration-200 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? (
                    <div className="flex items-center justify-center">
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                      Submitting Review...
                    </div>
                  ) : (
                    'Submit Review'
                  )}
                </button>
              </form>
            </div>

            {/* Existing Reviews */}
            <div>
              <h3 className="text-lg font-semibold text-gray-800 mb-6">Other Reviews</h3>
              
              {existingReviews.length === 0 ? (
                <div className="text-center py-8">
                  <div className="text-4xl mb-4">⭐</div>
                  <p className="text-gray-500">No reviews yet. Be the first to review this doctor!</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* Average Rating */}
                  <div className="bg-blue-50 rounded-lg p-4 mb-6">
                    <div className="flex items-center space-x-4">
                      <div className="text-center">
                        <div className="text-3xl font-bold text-blue-600">{getAverageRating()}</div>
                        <div className="flex space-x-1">
                          {renderStars(Math.round(parseFloat(getAverageRating())))}
                        </div>
                      </div>
                      <div>
                        <div className="text-lg font-semibold text-gray-800">Average Rating</div>
                        <div className="text-gray-600">{existingReviews.length} review{existingReviews.length !== 1 ? 's' : ''}</div>
                      </div>
                    </div>
                  </div>

                  {/* Individual Reviews */}
                  {existingReviews.map((review) => (
                    <div key={review.id} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex items-start justify-between mb-3">
                        <div>
                          <div className="font-semibold text-gray-800">{review.patientName}</div>
                          <div className="flex space-x-1">
                            {renderStars(review.rating)}
                          </div>
                        </div>
                        <div className="text-sm text-gray-500">
                          {new Date(review.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                      <p className="text-gray-700">{review.comment}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-4 mt-8 pt-6 border-t border-gray-200">
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors duration-200"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewSection;
