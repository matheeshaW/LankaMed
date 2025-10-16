import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import AppointmentsPage from '../AppointmentsPage';

jest.mock('../../utils/auth', () => ({
  getRole: jest.fn(() => 'PATIENT'),
  getCurrentUser: jest.fn(() => ({ email: 'john@example.com', firstName: 'John', lastName: 'Doe' })),
}));

jest.mock('../../services/api', () => {
  const mockApi = { get: jest.fn().mockResolvedValue({ data: { success: true, doctors: [] } }) };
  return {
    __esModule: true,
    default: mockApi,
    appointmentAPI: {
      getPatientAppointments: jest.fn().mockResolvedValue({
        data: [
          {
            appointmentId: 42,
            appointmentDateTime: '2025-12-01T09:00:00',
            status: 'CONFIRMED',
            doctorName: 'Dr. Jane Smith',
            doctorSpecialization: 'Cardiology',
            hospitalName: 'City General Hospital',
            reason: 'Follow-up',
            createdAt: new Date().toISOString(),
          },
        ],
      }),
      createAppointment: jest.fn(),
      updateAppointmentStatus: jest.fn(),
      getAllAppointments: jest.fn(),
      updateAppointment: jest.fn(),
    },
  };
});

describe('AppointmentsPage', () => {
  test('loads and displays patient appointments', async () => {
    render(
      <MemoryRouter>
        <AppointmentsPage />
      </MemoryRouter>
    );

    // Switch to "My Appointments" tab
    await userEvent.click(await screen.findByRole('button', { name: /My Appointments/i }));

    // Shows All Appointments header after switching tab
    expect(await screen.findByText(/All Appointments/i)).toBeInTheDocument();

    // Displays returned appointment data
    await waitFor(() => {
      expect(screen.getByText(/Dr. Jane Smith/)).toBeInTheDocument();
      expect(screen.getByText(/Cardiology/)).toBeInTheDocument();
      expect(screen.getByText(/City General Hospital/)).toBeInTheDocument();
      expect(screen.getByText(/Follow-up/)).toBeInTheDocument();
    });
  });
});
