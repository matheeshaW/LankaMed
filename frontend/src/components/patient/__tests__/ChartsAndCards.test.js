import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

// Mock the API module to provide expected data structures
jest.mock('../../../services/api', () => ({
  __esModule: true,
  default: {
    get: jest.fn((url) => {
      if (url.includes('/api/patients/me/blood-pressure-records')) {
        return Promise.resolve({ data: [{ timestamp: '2025-10-16T10:00:00Z', systolic: 120, diastolic: 80 }] });
      }
      if (url.includes('/api/patients/me/weight-records')) {
        return Promise.resolve({ data: [{ timestamp: '2025-10-16T10:00:00Z', weightKg: 70 }] });
      }
      if (url.includes('/api/patients/me/health-metrics/latest')) {
        return Promise.resolve({ data: { systolic: 120, diastolic: 80, heartRate: 75, spo2: 98 } });
      }
      if (url.includes('/api/patients/me/emergency-contacts')) {
        return Promise.resolve({ data: [{ emergencyContactId: 1, fullName: 'Alice', phone: '+123' }] });
      }
      if (url === '/api/patients/me') {
        return Promise.resolve({ data: { firstName: 'John', lastName: 'Doe', patientId: 1 } });
      }
      // For medical history card (conditions, allergies, prescriptions)
      return Promise.resolve({ data: [] });
    }),
  },
}));

import BloodPressureTrendChart from '../BloodPressureTrendChart';
import WeightHistoryChart from '../WeightHistoryChart';
import HealthMetricsCard from '../HealthMetricsCard';
import EmergencyContactCard from '../EmergencyContactCard';
import PersonalInformationCard from '../PersonalInformationCard';
import MedicalHistoryCard from '../MedicalHistoryCard';

describe('Patient small components smoke tests', () => {

  test('renders BloodPressureTrendChart container', async () => {
    render(<BloodPressureTrendChart />);
    expect(await screen.findByText('Blood Pressure Trend')).toBeInTheDocument();
    expect(document.querySelector('#bp-trend-chart')).toBeInTheDocument();
  });

  test('renders WeightHistoryChart container', async () => {
    render(<WeightHistoryChart />);
    expect(await screen.findByText('Weight History')).toBeInTheDocument();
    expect(document.querySelector('#weight-history-chart')).toBeInTheDocument();
  });

  test('renders HealthMetricsCard with values', async () => {
    render(<HealthMetricsCard />);
    expect(await screen.findByText(/blood pressure/i)).toBeInTheDocument();
  });

  test('renders EmergencyContactCard content', async () => {
    render(<EmergencyContactCard />);
    expect(await screen.findByText('Alice')).toBeInTheDocument();
  });

  test('renders PersonalInformationCard with name', async () => {
    render(<PersonalInformationCard />);
    expect(await screen.findByText(/John/)).toBeInTheDocument();
  });

  test('renders MedicalHistoryCard title', async () => {
    render(<MedicalHistoryCard />);
    expect(await screen.findByText(/medical history/i)).toBeInTheDocument();
  });
});
