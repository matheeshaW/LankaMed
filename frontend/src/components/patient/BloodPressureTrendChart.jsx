import React, { useEffect, useState } from 'react';
import { Line } from 'react-chartjs-2';
import api from '../../services/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend, Filler);

const BloodPressureTrendChart = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/patients/me/blood-pressure-records')
      .then(res => setData(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6 text-gray-400 text-center">Loading...</div>
  );

  if (!data || data.length === 0) return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6 text-gray-400 text-center">No blood pressure data</div>
  );

  // Use most recent year (x: month labels if available, else ISO date)
  const labels = data.map(dp => (new Date(dp.timestamp)).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }));

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">Blood Pressure Trend</h3>
      <Line
        height={300}
        data={{
          labels,
          datasets: [
            {
              label: 'Systolic',
              data: data.map(dp => dp.systolic),
              fill: true,
              backgroundColor: 'rgba(59,130,246,0.08)',
              borderColor: '#2563eb',
              tension: 0.3,
              pointRadius: 3,
            },
            {
              label: 'Diastolic',
              data: data.map(dp => dp.diastolic),
              fill: true,
              backgroundColor: 'rgba(16,185,129,0.06)',
              borderColor: '#10b981',
              tension: 0.3,
              pointRadius: 3,
            }
          ]
        }}
        options={{
          responsive: true,
          plugins: {
            legend: { position: 'top' },
            tooltip: { mode: 'index', intersect: false }
          },
          scales: {
            y: {
              title: { display: true, text: 'mmHg' },
              min: 40
            }
          },
        }}
      />
    </div>
  );
};

export default BloodPressureTrendChart;
