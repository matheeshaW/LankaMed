import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import PatientProfile from '../PatientProfile';
import api from '../../../services/api';

// Mock the API module
jest.mock('../../../services/api');

describe('PatientProfile', () => {
  const mockProfile = {
    patientId: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john.doe@example.com',
    dateOfBirth: '1990-01-01',
    gender: 'MALE',
    contactNumber: '+1234567890',
    address: '123 Main St, City'
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders profile information correctly', async () => {
    api.get.mockResolvedValue({ data: mockProfile });

    render(<PatientProfile />);

    await waitFor(() => {
      expect(screen.getByDisplayValue('John')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Doe')).toBeInTheDocument();
      expect(screen.getByDisplayValue('john.doe@example.com')).toBeInTheDocument();
      expect(screen.getByDisplayValue('1990-01-01')).toBeInTheDocument();
      expect(screen.getByDisplayValue('MALE')).toBeInTheDocument();
      expect(screen.getByDisplayValue('+1234567890')).toBeInTheDocument();
      expect(screen.getByDisplayValue('123 Main St, City')).toBeInTheDocument();
    });
  });

  test('shows loading state initially', () => {
    api.get.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<PatientProfile />);

    expect(screen.getByRole('status')).toBeInTheDocument();
  });

  test('enters edit mode when Edit Profile button is clicked', async () => {
    api.get.mockResolvedValue({ data: mockProfile });

    render(<PatientProfile />);

    await waitFor(() => {
      expect(screen.getByText('Edit Profile')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Edit Profile'));

    expect(screen.getByText('Save Changes')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  test('cancels edit mode when Cancel button is clicked', async () => {
    api.get.mockResolvedValue({ data: mockProfile });

    render(<PatientProfile />);

    await waitFor(() => {
      expect(screen.getByText('Edit Profile')).toBeInTheDocument();
    });

    // Enter edit mode
    fireEvent.click(screen.getByText('Edit Profile'));

    // Change a field
    fireEvent.change(screen.getByDisplayValue('John'), { target: { value: 'Jane' } });

    // Cancel edit
    fireEvent.click(screen.getByText('Cancel'));

    // Should revert to original value
    expect(screen.getByDisplayValue('John')).toBeInTheDocument();
    expect(screen.getByText('Edit Profile')).toBeInTheDocument();
  });

  test('saves changes when Save Changes button is clicked', async () => {
    const updatedProfile = { ...mockProfile, firstName: 'Jane' };
    api.get.mockResolvedValue({ data: mockProfile });
    api.put.mockResolvedValue({ data: updatedProfile });

    render(<PatientProfile />);

    await waitFor(() => {
      expect(screen.getByText('Edit Profile')).toBeInTheDocument();
    });

    // Enter edit mode
    fireEvent.click(screen.getByText('Edit Profile'));

    // Change a field
    fireEvent.change(screen.getByDisplayValue('John'), { target: { value: 'Jane' } });

    // Save changes
    fireEvent.click(screen.getByText('Save Changes'));

    await waitFor(() => {
      expect(api.put).toHaveBeenCalledWith('/api/patients/me', expect.objectContaining({
        firstName: 'Jane'
      }));
    });
  });

  test('handles API errors gracefully', async () => {
    api.get.mockRejectedValue(new Error('API Error'));

    render(<PatientProfile />);

    await waitFor(() => {
      // Should not crash, just not show the form
      expect(screen.queryByText('Personal Information')).not.toBeInTheDocument();
    });
  });

  test('handles save errors gracefully', async () => {
    api.get.mockResolvedValue({ data: mockProfile });
    api.put.mockRejectedValue(new Error('Save failed'));

    // Mock window.alert
    window.alert = jest.fn();

    render(<PatientProfile />);

    await waitFor(() => {
      expect(screen.getByText('Edit Profile')).toBeInTheDocument();
    });

    // Enter edit mode and save
    fireEvent.click(screen.getByText('Edit Profile'));
    fireEvent.click(screen.getByText('Save Changes'));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Failed to update profile. Please try again.');
    });
  });
});
