import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import AllergyModal from '../AllergyModal';
import api from '../../../services/api';

// Mock the API module
jest.mock('../../../services/api');

describe('AllergyModal', () => {
  const mockOnSave = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders add new allergy modal', () => {
    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    expect(screen.getByText('Add New Allergy')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter allergy name')).toBeInTheDocument();
    expect(screen.getByText('Mild')).toBeInTheDocument(); // Default severity option
    expect(screen.getByText('Add')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  test('renders edit allergy modal with existing data', () => {
    const existingAllergy = {
      allergyId: 1,
      allergyName: 'Penicillin',
      severity: 'SEVERE',
      notes: 'Causes severe reaction'
    };

    render(<AllergyModal allergy={existingAllergy} onSave={mockOnSave} onClose={mockOnClose} />);

    expect(screen.getByText('Edit Allergy')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Penicillin')).toBeInTheDocument();
    expect(screen.getByDisplayValue('SEVERE')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Causes severe reaction')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
  });

  test('validates required fields', async () => {
    api.post.mockResolvedValue({ data: { allergyId: 1, allergyName: 'Test' } });

    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Try to submit without filling required field
    fireEvent.click(screen.getByText('Add'));

    // Should not call API or onSave
    expect(api.post).not.toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('creates new allergy successfully', async () => {
    const newAllergy = {
      allergyId: 1,
      allergyName: 'Peanuts',
      severity: 'MODERATE',
      notes: 'Causes mild reaction'
    };

    api.post.mockResolvedValue({ data: newAllergy });

    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill form
    fireEvent.change(screen.getByPlaceholderText('Enter allergy name'), {
      target: { value: 'Peanuts' }
    });
    fireEvent.change(screen.getByDisplayValue('Mild'), {
      target: { value: 'MODERATE' }
    });
    fireEvent.change(screen.getByPlaceholderText('Additional notes about the allergy'), {
      target: { value: 'Causes mild reaction' }
    });

    // Submit
    fireEvent.click(screen.getByText('Add'));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/patients/me/allergies', {
        allergyName: 'Peanuts',
        severity: 'MODERATE',
        notes: 'Causes mild reaction'
      });
      expect(mockOnSave).toHaveBeenCalledWith(newAllergy);
    });
  });

  test('updates existing allergy successfully', async () => {
    const existingAllergy = {
      allergyId: 1,
      allergyName: 'Penicillin',
      severity: 'SEVERE',
      notes: 'Causes severe reaction'
    };

    const updatedAllergy = {
      ...existingAllergy,
      allergyName: 'Updated Penicillin',
      severity: 'MODERATE',
      notes: 'Updated notes'
    };

    api.put.mockResolvedValue({ data: updatedAllergy });

    render(<AllergyModal allergy={existingAllergy} onSave={mockOnSave} onClose={mockOnClose} />);

    // Update form
    fireEvent.change(screen.getByDisplayValue('Penicillin'), {
      target: { value: 'Updated Penicillin' }
    });
    fireEvent.change(screen.getByDisplayValue('SEVERE'), {
      target: { value: 'MODERATE' }
    });
    fireEvent.change(screen.getByDisplayValue('Causes severe reaction'), {
      target: { value: 'Updated notes' }
    });

    // Submit
    fireEvent.click(screen.getByText('Update'));

    await waitFor(() => {
      expect(api.put).toHaveBeenCalledWith('/api/patients/me/allergies/1', {
        allergyName: 'Updated Penicillin',
        severity: 'MODERATE',
        notes: 'Updated notes'
      });
      expect(mockOnSave).toHaveBeenCalledWith(updatedAllergy);
    });
  });

  test('handles API errors gracefully', async () => {
    api.post.mockRejectedValue(new Error('API Error'));

    window.alert = jest.fn();

    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill and submit form
    fireEvent.change(screen.getByPlaceholderText('Enter allergy name'), {
      target: { value: 'Test Allergy' }
    });
    fireEvent.click(screen.getByText('Add'));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Failed to save allergy. Please try again.');
      expect(mockOnSave).not.toHaveBeenCalled();
    });
  });

  test('closes modal when Cancel is clicked', () => {
    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    fireEvent.click(screen.getByText('Cancel'));

    expect(mockOnClose).toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('closes modal when X button is clicked', () => {
    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    fireEvent.click(screen.getByRole('button', { name: /close/i }));

    expect(mockOnClose).toHaveBeenCalled();
  });

  test('shows loading state while saving', async () => {
    api.post.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill and submit form
    fireEvent.change(screen.getByPlaceholderText('Enter allergy name'), {
      target: { value: 'Test Allergy' }
    });
    fireEvent.click(screen.getByText('Add'));

    // Should show loading state
    expect(screen.getByText('Saving...')).toBeInTheDocument();
    expect(screen.getByText('Add')).toBeDisabled();
  });

  test('has all severity options available', () => {
    render(<AllergyModal onSave={mockOnSave} onClose={mockOnClose} />);

    const severitySelect = screen.getByDisplayValue('Mild');
    expect(severitySelect).toBeInTheDocument();

    // Check all options are present
    fireEvent.click(severitySelect);
    expect(screen.getByText('Mild')).toBeInTheDocument();
    expect(screen.getByText('Moderate')).toBeInTheDocument();
    expect(screen.getByText('Severe')).toBeInTheDocument();
  });
});
