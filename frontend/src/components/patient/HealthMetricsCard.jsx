import React, { useEffect, useState } from 'react';
import api from '../../services/api';

const MetricBox = ({ icon, label, value, unit, color }) => (
  <div className={`flex-1 bg-gradient-to-br ${color} rounded-xl p-5 shadow text-center mx-2`}>
    <div className="flex justify-center mb-2 text-2xl">{icon}</div>
    <div className="text-2xl font-bold text-gray-900">{value} <span className="text-base font-medium text-gray-500">{unit}</span></div>
    <div className="text-gray-700 text-sm mt-1">{label}</div>
  </div>
);

const HealthMetricsCard = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/patients/me/health-metrics/latest')
      .then(res => setMetrics(res.data))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-5">Health Metrics</h3>
      {loading ? (
        <div className="flex justify-center py-8 text-gray-400">Loading...</div>
      ) : metrics ? (
        <div className="flex flex-col md:flex-row justify-between items-center gap-4">
          <MetricBox icon='🩸' label='Blood Pressure' value={metrics.systolic + ' / ' + metrics.diastolic} unit="mmHg" color="from-blue-50 to-blue-100" />
          <MetricBox icon='💓' label='Heart Rate' value={metrics.heartRate} unit="bpm" color="from-red-50 to-red-100" />
          <MetricBox icon='🫁' label='SpO₂' value={metrics.spo2} unit="%" color="from-green-50 to-green-100" />
        </div>
      ) : (
        <div className="text-gray-400 text-center py-8">No metrics data</div>
      )}
    </div>
  );
};

export default HealthMetricsCard;
