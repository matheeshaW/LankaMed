import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import MedicalHistory from '../MedicalHistory';
import api from '../../../services/api';

// Mock the API module
jest.mock('../../../services/api');

describe('MedicalHistory', () => {
  const mockConditions = [
    {
      conditionId: 1,
      conditionName: 'Diabetes',
      diagnosedDate: '2020-01-01',
      notes: 'Type 2 diabetes'
    }
  ];

  const mockAllergies = [
    {
      allergyId: 1,
      allergyName: 'Penicillin',
      severity: 'SEVERE',
      notes: 'Causes severe reaction'
    }
  ];

  const mockPrescriptions = [
    {
      prescriptionId: 1,
      medicationName: 'Metformin',
      dosage: '500mg',
      frequency: 'Twice daily',
      startDate: '2020-01-01',
      endDate: '2020-12-31',
      doctorName: 'Dr. Jane Smith',
      doctorSpecialization: 'Cardiology'
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    api.get.mockImplementation((url) => {
      if (url.includes('/conditions')) {
        return Promise.resolve({ data: mockConditions });
      } else if (url.includes('/allergies')) {
        return Promise.resolve({ data: mockAllergies });
      } else if (url.includes('/prescriptions')) {
        return Promise.resolve({ data: mockPrescriptions });
      }
      return Promise.resolve({ data: [] });
    });
  });

  test('renders all three sections with correct tabs', async () => {
    render(<MedicalHistory />);

    await waitFor(() => {
      expect(screen.getByText('Medical Conditions')).toBeInTheDocument();
      expect(screen.getByText('Allergies')).toBeInTheDocument();
      expect(screen.getByText('Prescriptions')).toBeInTheDocument();
    });
  });

  test('displays medical conditions correctly', async () => {
    render(<MedicalHistory />);

    await waitFor(() => {
      expect(screen.getByText('Diabetes')).toBeInTheDocument();
      expect(screen.getByText('Type 2 diabetes')).toBeInTheDocument();
      expect(screen.getByText('Diagnosed: 1/1/2020')).toBeInTheDocument();
    });
  });

  test('displays allergies correctly', async () => {
    render(<MedicalHistory />);

    // Switch to allergies tab
    fireEvent.click(screen.getByText('Allergies'));

    await waitFor(() => {
      expect(screen.getByText('Penicillin')).toBeInTheDocument();
      expect(screen.getByText('Causes severe reaction')).toBeInTheDocument();
      expect(screen.getByText('SEVERE')).toBeInTheDocument();
    });
  });

  test('displays prescriptions correctly', async () => {
    render(<MedicalHistory />);

    // Switch to prescriptions tab
    fireEvent.click(screen.getByText('Prescriptions'));

    await waitFor(() => {
      expect(screen.getByText('Metformin')).toBeInTheDocument();
      expect(screen.getByText('500mg')).toBeInTheDocument();
      expect(screen.getByText('Twice daily')).toBeInTheDocument();
      expect(screen.getByText('Dr. Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Cardiology')).toBeInTheDocument();
    });
  });

  test('opens condition modal when Add Condition is clicked', async () => {
    render(<MedicalHistory />);

    await waitFor(() => {
      expect(screen.getByText('Add Condition')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Condition'));

    expect(screen.getByText('Add New Condition')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter condition name')).toBeInTheDocument();
  });

  test('opens allergy modal when Add Allergy is clicked', async () => {
    render(<MedicalHistory />);

    // Switch to allergies tab
    fireEvent.click(screen.getByText('Allergies'));

    await waitFor(() => {
      expect(screen.getByText('Add Allergy')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Add Allergy'));

    expect(screen.getByText('Add New Allergy')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter allergy name')).toBeInTheDocument();
  });

  test('deletes condition when Delete is clicked', async () => {
    window.confirm = jest.fn(() => true);
    api.delete.mockResolvedValue({});

    render(<MedicalHistory />);

    await waitFor(() => {
      expect(screen.getByText('Diabetes')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Delete'));

    expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete this condition?');
    expect(api.delete).toHaveBeenCalledWith('/api/patients/me/conditions/1');
  });

  test('deletes allergy when Delete is clicked', async () => {
    window.confirm = jest.fn(() => true);
    api.delete.mockResolvedValue({});

    render(<MedicalHistory />);

    // Switch to allergies tab
    fireEvent.click(screen.getByText('Allergies'));

    await waitFor(() => {
      expect(screen.getByText('Penicillin')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Delete'));

    expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete this allergy?');
    expect(api.delete).toHaveBeenCalledWith('/api/patients/me/allergies/1');
  });

  test('shows empty state when no conditions exist', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('/conditions')) {
        return Promise.resolve({ data: [] });
      } else if (url.includes('/allergies')) {
        return Promise.resolve({ data: [] });
      } else if (url.includes('/prescriptions')) {
        return Promise.resolve({ data: [] });
      }
      return Promise.resolve({ data: [] });
    });

    render(<MedicalHistory />);

    await waitFor(() => {
      expect(screen.getByText('No medical conditions recorded')).toBeInTheDocument();
    });
  });

  test('handles API errors gracefully', async () => {
    api.get.mockRejectedValue(new Error('API Error'));

    render(<MedicalHistory />);

    await waitFor(() => {
      // Should not crash, just show loading or empty state
      expect(screen.queryByText('Medical Conditions')).not.toBeInTheDocument();
    });
  });
});
