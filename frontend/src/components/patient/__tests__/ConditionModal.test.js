import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConditionModal from '../ConditionModal';
import api from '../../../services/api';

// Mock the API module
jest.mock('../../../services/api');

describe('ConditionModal', () => {
  const mockOnSave = jest.fn();
  const mockOnClose = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders add new condition modal', () => {
    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    expect(screen.getByText('Add New Condition')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Enter condition name')).toBeInTheDocument();
    expect(screen.getByText('Add')).toBeInTheDocument();
    expect(screen.getByText('Cancel')).toBeInTheDocument();
  });

  test('renders edit condition modal with existing data', () => {
    const existingCondition = {
      conditionId: 1,
      conditionName: 'Diabetes',
      diagnosedDate: '2020-01-01',
      notes: 'Type 2 diabetes'
    };

    render(<ConditionModal condition={existingCondition} onSave={mockOnSave} onClose={mockOnClose} />);

    expect(screen.getByText('Edit Condition')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Diabetes')).toBeInTheDocument();
    expect(screen.getByDisplayValue('2020-01-01')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Type 2 diabetes')).toBeInTheDocument();
    expect(screen.getByText('Update')).toBeInTheDocument();
  });

  test('validates required fields', async () => {
    api.post.mockResolvedValue({ data: { conditionId: 1, conditionName: 'Test' } });

    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Try to submit without filling required field
    fireEvent.click(screen.getByText('Add'));

    // Should not call API or onSave
    expect(api.post).not.toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('creates new condition successfully', async () => {
    const newCondition = {
      conditionId: 1,
      conditionName: 'Hypertension',
      diagnosedDate: '2021-01-01',
      notes: 'High blood pressure'
    };

    api.post.mockResolvedValue({ data: newCondition });

    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill form
    fireEvent.change(screen.getByPlaceholderText('Enter condition name'), {
      target: { value: 'Hypertension' }
    });
    fireEvent.change(screen.getByLabelText('Diagnosed Date'), {
      target: { value: '2021-01-01' }
    });
    fireEvent.change(screen.getByPlaceholderText('Additional notes about the condition'), {
      target: { value: 'High blood pressure' }
    });

    // Submit
    fireEvent.click(screen.getByText('Add'));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/patients/me/conditions', {
        conditionName: 'Hypertension',
        diagnosedDate: '2021-01-01',
        notes: 'High blood pressure'
      });
      expect(mockOnSave).toHaveBeenCalledWith(newCondition);
    });
  });

  test('updates existing condition successfully', async () => {
    const existingCondition = {
      conditionId: 1,
      conditionName: 'Diabetes',
      diagnosedDate: '2020-01-01',
      notes: 'Type 2 diabetes'
    };

    const updatedCondition = {
      ...existingCondition,
      conditionName: 'Updated Diabetes',
      notes: 'Updated notes'
    };

    api.put.mockResolvedValue({ data: updatedCondition });

    render(<ConditionModal condition={existingCondition} onSave={mockOnSave} onClose={mockOnClose} />);

    // Update form
    fireEvent.change(screen.getByDisplayValue('Diabetes'), {
      target: { value: 'Updated Diabetes' }
    });
    fireEvent.change(screen.getByDisplayValue('Type 2 diabetes'), {
      target: { value: 'Updated notes' }
    });

    // Submit
    fireEvent.click(screen.getByText('Update'));

    await waitFor(() => {
      expect(api.put).toHaveBeenCalledWith('/api/patients/me/conditions/1', {
        conditionName: 'Updated Diabetes',
        diagnosedDate: '2020-01-01',
        notes: 'Updated notes'
      });
      expect(mockOnSave).toHaveBeenCalledWith(updatedCondition);
    });
  });

  test('handles API errors gracefully', async () => {
    api.post.mockRejectedValue(new Error('API Error'));

    window.alert = jest.fn();

    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill and submit form
    fireEvent.change(screen.getByPlaceholderText('Enter condition name'), {
      target: { value: 'Test Condition' }
    });
    fireEvent.click(screen.getByText('Add'));

    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Failed to save condition. Please try again.');
      expect(mockOnSave).not.toHaveBeenCalled();
    });
  });

  test('closes modal when Cancel is clicked', () => {
    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    fireEvent.click(screen.getByText('Cancel'));

    expect(mockOnClose).toHaveBeenCalled();
    expect(mockOnSave).not.toHaveBeenCalled();
  });

  test('closes modal when X button is clicked', () => {
    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    fireEvent.click(screen.getByRole('button', { name: /close/i }));

    expect(mockOnClose).toHaveBeenCalled();
  });

  test('shows loading state while saving', async () => {
    api.post.mockImplementation(() => new Promise(() => {})); // Never resolves

    render(<ConditionModal onSave={mockOnSave} onClose={mockOnClose} />);

    // Fill and submit form
    fireEvent.change(screen.getByPlaceholderText('Enter condition name'), {
      target: { value: 'Test Condition' }
    });
    fireEvent.click(screen.getByText('Add'));

    // Should show loading state
    expect(screen.getByText('Saving...')).toBeInTheDocument();
    expect(screen.getByText('Add')).toBeDisabled();
  });
});
