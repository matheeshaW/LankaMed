import React, { useState } from 'react';
import { reviewAPI } from '../../services/api';

const AddReview = ({ appointment, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        appointmentId: appointment?.appointmentId || '',
        rating: 0,
        comment: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleRatingChange = (rating) => {
        setFormData(prev => ({
            ...prev,
            rating
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await reviewAPI.createReview(formData);
            onSuccess('Review submitted successfully!');
            onClose();
        } catch (error) {
            setError(error.response?.data?.message || 'Failed to submit review');
        } finally {
            setLoading(false);
        }
    };

    if (!appointment) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
                <div className="p-6">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-2xl font-bold text-gray-800">Add Review</h2>
                        <button
                            onClick={onClose}
                            className="text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>

                    {/* Appointment Info */}
                    <div className="bg-gray-50 rounded-lg p-4 mb-6">
                        <h3 className="font-semibold text-gray-800 mb-2">Appointment Details</h3>
                        <p className="text-sm text-gray-600">
                            <strong>Doctor:</strong> {appointment.doctorName}
                        </p>
                        <p className="text-sm text-gray-600">
                            <strong>Specialization:</strong> {appointment.doctorSpecialization}
                        </p>
                        <p className="text-sm text-gray-600">
                            <strong>Date:</strong> {new Date(appointment.appointmentDateTime).toLocaleDateString()}
                        </p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Rating *
                            </label>
                            <div className="flex space-x-2">
                                {[1, 2, 3, 4, 5].map((star) => (
                                    <button
                                        key={star}
                                        type="button"
                                        onClick={() => handleRatingChange(star)}
                                        className={`text-3xl transition-colors ${
                                            star <= formData.rating
                                                ? 'text-yellow-400'
                                                : 'text-gray-300 hover:text-yellow-300'
                                        }`}
                                    >
                                        ★
                                    </button>
                                ))}
                            </div>
                            <p className="text-sm text-gray-500 mt-1">
                                {formData.rating === 0 && 'Please select a rating'}
                                {formData.rating === 1 && 'Poor'}
                                {formData.rating === 2 && 'Fair'}
                                {formData.rating === 3 && 'Good'}
                                {formData.rating === 4 && 'Very Good'}
                                {formData.rating === 5 && 'Excellent'}
                            </p>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Comment (Optional)
                            </label>
                            <textarea
                                name="comment"
                                value={formData.comment}
                                onChange={handleInputChange}
                                rows={4}
                                placeholder="Share your experience with this doctor..."
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <p className="text-sm text-gray-500 mt-1">
                                {formData.comment.length}/1000 characters
                            </p>
                        </div>

                        {error && (
                            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                                {error}
                            </div>
                        )}

                        <div className="flex space-x-4 pt-4">
                            <button
                                type="button"
                                onClick={onClose}
                                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={loading || formData.rating === 0}
                                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                            >
                                {loading ? 'Submitting...' : 'Submit Review'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default AddReview;
