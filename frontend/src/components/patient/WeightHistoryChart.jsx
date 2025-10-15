import React, { useEffect, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import api from '../../services/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const WeightHistoryChart = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/patients/me/weight-records')
      .then(res => setData(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6 text-gray-400 text-center">Loading...</div>
  );

  if (!data || data.length === 0) return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6 text-gray-400 text-center">No weight data</div>
  );

  const labels = data.map(dp => (new Date(dp.timestamp)).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }));
  return (
    <div className="bg-white rounded-lg shadow-md p-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">Weight History</h3>
      <Bar
        height={300}
        data={{
          labels,
          datasets: [
            {
              label: 'Weight (kg)',
              data: data.map(dp => dp.weightKg),
              backgroundColor: '#6366f1',
              borderRadius: 6,
              maxBarThickness: 24,
            }
          ]
        }}
        options={{
          responsive: true,
          plugins: {
            legend: { display: false },
            tooltip: { mode: 'index', intersect: false }
          },
          scales: {
            y: {
              title: { display: true, text: 'kg' },
              min: 0
            }
          },
        }}
      />
    </div>
  );
};

export default WeightHistoryChart;
