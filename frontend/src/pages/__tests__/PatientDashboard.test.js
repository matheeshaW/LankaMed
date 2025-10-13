import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import PatientDashboard from '../PatientDashboard';
import { getRole } from '../../utils/auth';

// Mock the auth utility
jest.mock('../../utils/auth', () => ({
  getRole: jest.fn(),
  logout: jest.fn()
}));

// Mock the child components
jest.mock('../../components/patient/PatientProfile', () => {
  return function MockPatientProfile() {
    return <div data-testid="patient-profile">Patient Profile Component</div>;
  };
});

jest.mock('../../components/patient/MedicalHistory', () => {
  return function MockMedicalHistory() {
    return <div data-testid="medical-history">Medical History Component</div>;
  };
});

jest.mock('../../components/patient/AppointmentHistory', () => {
  return function MockAppointmentHistory() {
    return <div data-testid="appointment-history">Appointment History Component</div>;
  };
});

jest.mock('../../components/Navbar', () => {
  return function MockNavbar() {
    return <div data-testid="navbar">Navbar Component</div>;
  };
});

const PatientDashboardWrapper = () => (
  <BrowserRouter>
    <PatientDashboard />
  </BrowserRouter>
);

describe('PatientDashboard', () => {
  const mockNavigate = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    // Mock useNavigate
    jest.doMock('react-router-dom', () => ({
      ...jest.requireActual('react-router-dom'),
      useNavigate: () => mockNavigate
    }));
  });

  test('renders dashboard with all tabs', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    expect(screen.getByText('Patient Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Manage your health information and appointments')).toBeInTheDocument();
    expect(screen.getByText('My Profile')).toBeInTheDocument();
    expect(screen.getByText('Medical History')).toBeInTheDocument();
    expect(screen.getByText('Appointments')).toBeInTheDocument();
  });

  test('shows patient profile by default', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    expect(screen.getByTestId('patient-profile')).toBeInTheDocument();
    expect(screen.queryByTestId('medical-history')).not.toBeInTheDocument();
    expect(screen.queryByTestId('appointment-history')).not.toBeInTheDocument();
  });

  test('switches to medical history tab when clicked', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    fireEvent.click(screen.getByText('Medical History'));

    expect(screen.getByTestId('medical-history')).toBeInTheDocument();
    expect(screen.queryByTestId('patient-profile')).not.toBeInTheDocument();
    expect(screen.queryByTestId('appointment-history')).not.toBeInTheDocument();
  });

  test('switches to appointments tab when clicked', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    fireEvent.click(screen.getByText('Appointments'));

    expect(screen.getByTestId('appointment-history')).toBeInTheDocument();
    expect(screen.queryByTestId('patient-profile')).not.toBeInTheDocument();
    expect(screen.queryByTestId('medical-history')).not.toBeInTheDocument();
  });

  test('switches back to profile tab when clicked', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    // Switch to medical history first
    fireEvent.click(screen.getByText('Medical History'));
    expect(screen.getByTestId('medical-history')).toBeInTheDocument();

    // Switch back to profile
    fireEvent.click(screen.getByText('My Profile'));
    expect(screen.getByTestId('patient-profile')).toBeInTheDocument();
    expect(screen.queryByTestId('medical-history')).not.toBeInTheDocument();
  });

  test('redirects to login if user is not a patient', () => {
    getRole.mockReturnValue('ADMIN');

    render(<PatientDashboardWrapper />);

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('redirects to login if user has no role', () => {
    getRole.mockReturnValue('');

    render(<PatientDashboardWrapper />);

    expect(mockNavigate).toHaveBeenCalledWith('/login');
  });

  test('shows correct tab styling for active tab', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    const profileTab = screen.getByText('My Profile').closest('button');
    const medicalTab = screen.getByText('Medical History').closest('button');
    const appointmentsTab = screen.getByText('Appointments').closest('button');

    // Profile tab should be active by default
    expect(profileTab).toHaveClass('border-blue-500', 'text-blue-600');
    expect(medicalTab).toHaveClass('border-transparent', 'text-gray-500');
    expect(appointmentsTab).toHaveClass('border-transparent', 'text-gray-500');
  });

  test('updates tab styling when switching tabs', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    const profileTab = screen.getByText('My Profile').closest('button');
    const medicalTab = screen.getByText('Medical History').closest('button');

    // Switch to medical history
    fireEvent.click(screen.getByText('Medical History'));

    expect(medicalTab).toHaveClass('border-blue-500', 'text-blue-600');
    expect(profileTab).toHaveClass('border-transparent', 'text-gray-500');
  });

  test('renders navbar component', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    expect(screen.getByTestId('navbar')).toBeInTheDocument();
  });

  test('has proper accessibility attributes for tabs', () => {
    getRole.mockReturnValue('PATIENT');

    render(<PatientDashboardWrapper />);

    const tabList = screen.getByRole('tablist');
    expect(tabList).toBeInTheDocument();

    const tabs = screen.getAllByRole('tab');
    expect(tabs).toHaveLength(3);
  });
});
