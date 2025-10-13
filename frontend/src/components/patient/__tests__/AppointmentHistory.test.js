import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import AppointmentHistory from '../AppointmentHistory';
import api from '../../../services/api';

// Mock the API module
jest.mock('../../../services/api');

describe('AppointmentHistory', () => {
  const mockAppointments = [
    {
      appointmentId: 1,
      appointmentDateTime: '2024-01-15T10:00:00',
      status: 'SCHEDULED',
      doctorName: 'Dr. Jane Smith',
      doctorSpecialization: 'Cardiologist',
      hospitalName: 'City General Hospital',
      serviceCategoryName: 'Cardiology'
    },
    {
      appointmentId: 2,
      appointmentDateTime: '2023-12-01T14:00:00',
      status: 'COMPLETED',
      doctorName: 'Dr. John Doe',
      doctorSpecialization: 'Dermatologist',
      hospitalName: 'City General Hospital',
      serviceCategoryName: 'Dermatology'
    },
    {
      appointmentId: 3,
      appointmentDateTime: '2024-02-01T09:00:00',
      status: 'SCHEDULED',
      doctorName: 'Dr. Alice Johnson',
      doctorSpecialization: 'Neurologist',
      hospitalName: 'City General Hospital',
      serviceCategoryName: 'Neurology'
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    api.get.mockResolvedValue({ data: mockAppointments });
  });

  test('renders appointment history correctly', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Appointment History')).toBeInTheDocument();
      expect(screen.getByText('Download PDF')).toBeInTheDocument();
    });
  });

  test('displays appointments with correct information', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('City General Hospital')).toBeInTheDocument();
      expect(screen.getByText('Cardiology')).toBeInTheDocument();
      expect(screen.getByText('Dr. Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Cardiologist')).toBeInTheDocument();
    });
  });

  test('shows correct status badges', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Scheduled')).toBeInTheDocument();
      expect(screen.getByText('Completed')).toBeInTheDocument();
    });
  });

  test('filters appointments by upcoming', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Upcoming')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Upcoming'));

    // Should show only future appointments
    await waitFor(() => {
      expect(screen.getByText('Dr. Alice Johnson')).toBeInTheDocument();
      expect(screen.queryByText('Dr. John Doe')).not.toBeInTheDocument();
    });
  });

  test('filters appointments by past', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Past')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Past'));

    // Should show only past appointments
    await waitFor(() => {
      expect(screen.getByText('Dr. John Doe')).toBeInTheDocument();
      expect(screen.queryByText('Dr. Alice Johnson')).not.toBeInTheDocument();
    });
  });

  test('shows all appointments when All Appointments is selected', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('All Appointments')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('All Appointments'));

    // Should show all appointments
    await waitFor(() => {
      expect(screen.getByText('Dr. Jane Smith')).toBeInTheDocument();
      expect(screen.getByText('Dr. John Doe')).toBeInTheDocument();
      expect(screen.getByText('Dr. Alice Johnson')).toBeInTheDocument();
    });
  });

  test('shows empty state when no appointments exist', async () => {
    api.get.mockResolvedValue({ data: [] });

    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('No appointments found')).toBeInTheDocument();
      expect(screen.getByText("You don't have any appointments yet.")).toBeInTheDocument();
    });
  });

  test('shows empty state for upcoming filter when no upcoming appointments', async () => {
    const pastAppointments = [mockAppointments[1]]; // Only completed appointment
    api.get.mockResolvedValue({ data: pastAppointments });

    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Upcoming')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Upcoming'));

    await waitFor(() => {
      expect(screen.getByText("You don't have any upcoming appointments.")).toBeInTheDocument();
    });
  });

  test('downloads appointment history as PDF', async () => {
    const mockBlob = new Blob(['PDF content'], { type: 'application/pdf' });
    api.get.mockImplementation((url) => {
      if (url.includes('/download')) {
        return Promise.resolve({ data: mockBlob });
      }
      return Promise.resolve({ data: mockAppointments });
    });

    // Mock URL.createObjectURL and related DOM methods
    global.URL.createObjectURL = jest.fn(() => 'mock-url');
    global.URL.revokeObjectURL = jest.fn();
    const mockLink = {
      href: '',
      setAttribute: jest.fn(),
      click: jest.fn(),
      remove: jest.fn()
    };
    jest.spyOn(document, 'createElement').mockReturnValue(mockLink);
    jest.spyOn(document.body, 'appendChild').mockImplementation(() => {});
    jest.spyOn(document.body, 'removeChild').mockImplementation(() => {});

    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Download PDF')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Download PDF'));

    await waitFor(() => {
      expect(api.get).toHaveBeenCalledWith('/api/patients/me/appointments/download', {
        responseType: 'blob'
      });
      expect(mockLink.setAttribute).toHaveBeenCalledWith('download', 'appointment-history.pdf');
      expect(mockLink.click).toHaveBeenCalled();
    });
  });

  test('handles download error gracefully', async () => {
    api.get.mockImplementation((url) => {
      if (url.includes('/download')) {
        return Promise.reject(new Error('Download failed'));
      }
      return Promise.resolve({ data: mockAppointments });
    });

    window.alert = jest.fn();

    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('Download PDF')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Download PDF'));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Failed to download appointment history. Please try again.');
    });
  });

  test('displays summary statistics correctly', async () => {
    render(<AppointmentHistory />);

    await waitFor(() => {
      expect(screen.getByText('2')).toBeInTheDocument(); // Scheduled count
      expect(screen.getByText('1')).toBeInTheDocument(); // Completed count
      expect(screen.getByText('3')).toBeInTheDocument(); // Total count
    });
  });

  test('handles API errors gracefully', async () => {
    api.get.mockRejectedValue(new Error('API Error'));

    render(<AppointmentHistory />);

    await waitFor(() => {
      // Should not crash, just show loading or empty state
      expect(screen.queryByText('Appointment History')).not.toBeInTheDocument();
    });
  });
});
