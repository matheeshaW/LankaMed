import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import AppointmentForm from '../../appointment/AppointmentForm';

jest.mock('../../../services/api', () => ({
  __esModule: true,
  default: { get: jest.fn(), post: jest.fn() },
  appointmentAPI: {
    getPatientAppointments: jest.fn(),
    createAppointment: jest.fn().mockResolvedValue({ data: { appointmentId: 123 } }),
  },
}));

jest.mock('../../../data/mockData', () => ({
  getAvailableSlots: jest.fn().mockImplementation((_doctorId, _date) => [
    { time: '09:00' },
    { time: '10:00' },
  ]),
}));

jest.mock('../../../utils/auth', () => ({
  getCurrentUser: jest.fn().mockReturnValue({
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    contactNumber: '+123456789',
  }),
}));

function tomorrowISODate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().split('T')[0];
}

describe('AppointmentForm', () => {
  test('books appointment and calls onSuccess', async () => {
    const doctor = { id: 1, name: 'Dr. Jane Smith', specialization: 'Cardiology', hospital: 'City General', fee: 1500, image: '👨‍⚕️' };
    const onClose = jest.fn();
    const onSuccess = jest.fn();

    render(<AppointmentForm doctor={doctor} onClose={onClose} onSuccess={onSuccess} />);

    // Fill form
    fireEvent.change(screen.getByLabelText(/Preferred Date/i), { target: { value: tomorrowISODate() } });
    // time options appear after date change
    fireEvent.change(screen.getByLabelText(/Time Slot/i), { target: { value: '09:00' } });
    fireEvent.change(screen.getByLabelText(/Reason for Appointment/i), { target: { value: 'Chest pain' } });

    // Submit
    fireEvent.click(screen.getByRole('button', { name: /Book Appointment/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalled();
    });
  });
});

