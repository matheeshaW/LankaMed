import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import CriteriaForm from '../CriteriaForm';

describe('CriteriaForm', () => {
  const mockOnChange = jest.fn();
  const mockOnNext = jest.fn();
  const defaultCriteria = {
    from: '',
    to: '',
    hospitalId: '',
    serviceCategory: '',
    patientCategory: ''
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Positive Test Cases
  test('renders all form fields correctly', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByTestId('from-input')).toBeInTheDocument();
    expect(screen.getByTestId('to-input')).toBeInTheDocument();
    expect(screen.getByDisplayValue('Choose a hospital...')).toBeInTheDocument();
    expect(screen.getByDisplayValue('All Categories')).toBeInTheDocument();
    expect(screen.getByDisplayValue('All Patients')).toBeInTheDocument();
  });

  test('calls onChange when date inputs are changed', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.change(screen.getByTestId('from-input'), { 
      target: { value: '2024-01-01' } 
    });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      from: '2024-01-01'
    });
  });

  test('calls onChange when end date is changed', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.change(screen.getByTestId('to-input'), { 
      target: { value: '2024-01-31' } 
    });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      to: '2024-01-31'
    });
  });

  test('calls onChange when hospital is selected', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const hospitalSelect = screen.getByDisplayValue('Choose a hospital...');
    fireEvent.change(hospitalSelect, { target: { value: 'C001' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      hospitalId: 'C001'
    });
  });

  test('calls onChange when service category is selected', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const serviceSelect = screen.getByDisplayValue('All Categories');
    fireEvent.change(serviceSelect, { target: { value: 'OPD' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      serviceCategory: 'OPD'
    });
  });

  test('calls onChange when patient category is selected', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const patientSelect = screen.getByDisplayValue('All Patients');
    fireEvent.change(patientSelect, { target: { value: 'OUTPATIENT' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      patientCategory: 'OUTPATIENT'
    });
  });

  test('enables next button when required fields are filled', () => {
    const completeCriteria = {
      from: '2024-01-01',
      to: '2024-01-31',
      hospitalId: 'C001',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={completeCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).not.toBeDisabled();
  });

  test('shows date range summary when dates are selected', () => {
    const criteriaWithDates = {
      ...defaultCriteria,
      from: '2024-01-01',
      to: '2024-01-31'
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithDates} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Date Range Selected')).toBeInTheDocument();
    // The date format might vary, so let's check for a more flexible pattern
    expect(screen.getByText(/Jan.*2024.*Jan.*2024/)).toBeInTheDocument();
  });

  test('renders all hospital options correctly', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Colombo National Hospital')).toBeInTheDocument();
    expect(screen.getByText('Castle Street Women\'s Hospital')).toBeInTheDocument();
    expect(screen.getByText('Sri Jayewardenepura General Hospital')).toBeInTheDocument();
    expect(screen.getByText('Asiri Central Hospital')).toBeInTheDocument();
    expect(screen.getByText('Lanka Hospitals')).toBeInTheDocument();
    expect(screen.getByText('Nawaloka Hospital')).toBeInTheDocument();
  });

  test('renders all service category options correctly', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Outpatient Department (OPD)')).toBeInTheDocument();
    expect(screen.getByText('Laboratory')).toBeInTheDocument();
    expect(screen.getByText('Surgery')).toBeInTheDocument();
    expect(screen.getByText('Pharmacy')).toBeInTheDocument();
    expect(screen.getByText('Radiology')).toBeInTheDocument();
  });

  test('renders all patient category options correctly', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Inpatient')).toBeInTheDocument();
    expect(screen.getByText('Outpatient')).toBeInTheDocument();
    expect(screen.getByText('Emergency')).toBeInTheDocument();
  });

  test('calls onNext when continue button is clicked with valid criteria', () => {
    const completeCriteria = {
      from: '2024-01-01',
      to: '2024-01-31',
      hospitalId: 'C001',
      serviceCategory: 'OPD',
      patientCategory: 'OUTPATIENT'
    };

    render(
      <CriteriaForm 
        criteria={completeCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.click(screen.getByTestId('next-button'));
    
    expect(mockOnNext).toHaveBeenCalled();
  });

  // Negative Test Cases
  test('disables next button when required fields are missing', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('disables next button when only dates are filled but hospital is missing', () => {
    const incompleteCriteria = {
      from: '2024-01-01',
      to: '2024-01-31',
      hospitalId: '',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={incompleteCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('disables next button when only from date is filled', () => {
    const incompleteCriteria = {
      from: '2024-01-01',
      to: '',
      hospitalId: '',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={incompleteCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('disables next button when only to date is filled', () => {
    const incompleteCriteria = {
      from: '',
      to: '2024-01-31',
      hospitalId: '',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={incompleteCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('disables next button when only hospital is selected', () => {
    const incompleteCriteria = {
      from: '',
      to: '',
      hospitalId: 'C001',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={incompleteCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('does not show date range summary when dates are not selected', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.queryByText('Date Range Selected')).not.toBeInTheDocument();
  });

  test('does not show date range summary when only one date is selected', () => {
    const criteriaWithOneDate = {
      ...defaultCriteria,
      from: '2024-01-01',
      to: ''
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithOneDate} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.queryByText('Date Range Selected')).not.toBeInTheDocument();
  });

  // Edge Cases
  test('handles date validation correctly', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Test with a valid date format that should trigger onChange
    const fromInput = screen.getByTestId('from-input');
    fireEvent.change(fromInput, { 
      target: { value: '2024-01-01' } 
    });
    
    // The component should call onChange with the date value
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultCriteria,
      from: '2024-01-01'
    });
  });

  test('handles empty string values correctly', () => {
    const criteriaWithEmptyStrings = {
      from: '',
      to: '',
      hospitalId: '',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithEmptyStrings} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('handles null values in criteria gracefully', () => {
    const criteriaWithNulls = {
      from: null,
      to: null,
      hospitalId: null,
      serviceCategory: null,
      patientCategory: null
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithNulls} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('handles undefined values in criteria gracefully', () => {
    const criteriaWithUndefined = {
      from: undefined,
      to: undefined,
      hospitalId: undefined,
      serviceCategory: undefined,
      patientCategory: undefined
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithUndefined} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('handles partial criteria updates correctly', () => {
    const partialCriteria = {
      from: '2024-01-01',
      to: '',
      hospitalId: '',
      serviceCategory: 'OPD',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={partialCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Update only the to date
    fireEvent.change(screen.getByTestId('to-input'), { 
      target: { value: '2024-01-31' } 
    });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...partialCriteria,
      to: '2024-01-31'
    });
  });

  test('handles multiple field changes in sequence', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Change from date
    fireEvent.change(screen.getByTestId('from-input'), { 
      target: { value: '2024-01-01' } 
    });
    
    // Change to date
    fireEvent.change(screen.getByTestId('to-input'), { 
      target: { value: '2024-01-31' } 
    });
    
    // Change hospital
    const hospitalSelect = screen.getByDisplayValue('Choose a hospital...');
    fireEvent.change(hospitalSelect, { target: { value: 'C001' } });
    
    expect(mockOnChange).toHaveBeenCalledTimes(3);
  });

  test('handles date range summary with different date formats', () => {
    const criteriaWithDates = {
      ...defaultCriteria,
      from: '2024-12-25',
      to: '2024-12-31'
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithDates} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Dec 25, 2024 - Dec 31, 2024')).toBeInTheDocument();
  });

  test('handles leap year dates correctly', () => {
    const criteriaWithLeapYear = {
      ...defaultCriteria,
      from: '2024-02-29',
      to: '2024-02-29'
    };

    render(
      <CriteriaForm 
        criteria={criteriaWithLeapYear} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Feb 29, 2024 - Feb 29, 2024')).toBeInTheDocument();
  });

  // Accessibility Tests
  test('has proper form labels', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Start Date')).toBeInTheDocument();
    expect(screen.getByText('End Date')).toBeInTheDocument();
    expect(screen.getByText('Select Hospital')).toBeInTheDocument();
    expect(screen.getByText('Service Category')).toBeInTheDocument();
    expect(screen.getByText('Patient Category')).toBeInTheDocument();
  });

  test('has proper required field indicators', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Check for multiple "Required" indicators
    const requiredElements = screen.getAllByText('Required');
    expect(requiredElements).toHaveLength(2); // Date Range and Hospital
  });

  test('has proper optional field indicators', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Check for multiple "Optional" indicators
    const optionalElements = screen.getAllByText('Optional');
    expect(optionalElements).toHaveLength(2); // Service Category and Patient Category
  });

  // Visual State Tests
  test('applies correct styling to required fields', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Check the date range container has the correct gradient background
    const dateRangeContainer = screen.getByText('Date Range').closest('div').parentElement;
    expect(dateRangeContainer).toHaveClass('bg-gradient-to-br');
  });

  test('applies correct styling to form inputs', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const fromInput = screen.getByTestId('from-input');
    const toInput = screen.getByTestId('to-input');
    
    expect(fromInput).toHaveClass('w-full');
    expect(toInput).toHaveClass('w-full');
  });

  // Function Call Tests
  test('does not call onNext when continue button is disabled', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    fireEvent.click(nextButton);
    
    expect(mockOnNext).not.toHaveBeenCalled();
  });

  test('calls onChange with correct parameters for each field', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Test from date
    fireEvent.change(screen.getByTestId('from-input'), { 
      target: { value: '2024-01-01' } 
    });
    expect(mockOnChange).toHaveBeenLastCalledWith({
      ...defaultCriteria,
      from: '2024-01-01'
    });
    
    // Test to date
    fireEvent.change(screen.getByTestId('to-input'), { 
      target: { value: '2024-01-31' } 
    });
    expect(mockOnChange).toHaveBeenLastCalledWith({
      ...defaultCriteria,
      to: '2024-01-31'
    });
  });

  test('calls onNext only once per click', () => {
    const completeCriteria = {
      from: '2024-01-01',
      to: '2024-01-31',
      hospitalId: 'C001',
      serviceCategory: '',
      patientCategory: ''
    };

    render(
      <CriteriaForm 
        criteria={completeCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    fireEvent.click(nextButton);
    fireEvent.click(nextButton);
    fireEvent.click(nextButton);
    
    expect(mockOnNext).toHaveBeenCalledTimes(3);
  });

  // Component Structure Tests
  test('renders all required icons', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Check for SVG icons by looking for svg elements directly
    const svgElements = document.querySelectorAll('svg');
    expect(svgElements.length).toBeGreaterThan(0);
  });

  test('renders proper container structure', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Check for main container classes - the outermost div should have w-full
    const outerContainer = document.querySelector('.w-full');
    expect(outerContainer).toBeInTheDocument();
    expect(outerContainer).toHaveClass('w-full');
  });

  test('renders proper button structure', () => {
    render(
      <CriteriaForm 
        criteria={defaultCriteria} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toHaveClass('w-full');
    expect(nextButton).toHaveClass('bg-gradient-to-r');
  });
});

