import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ReportSelector from '../ReportSelector';

describe('ReportSelector', () => {
  const mockOnChange = jest.fn();
  const mockOnNext = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Positive Test Cases(Successful report type options rendering)
  test('renders report type options correctly', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    expect(screen.getByText('Select Report Type')).toBeInTheDocument();
    expect(screen.getByText('Patient Visit Report')).toBeInTheDocument();
    expect(screen.getByText('Service Utilization Report')).toBeInTheDocument();
    expect(screen.getByText('Continue to Criteria')).toBeInTheDocument();
  });

  test('calls onChange when report type is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    fireEvent.click(screen.getByText('Patient Visit Report'));
    
    expect(mockOnChange).toHaveBeenCalledWith('PATIENT_VISIT');
  });

  test('calls onChange when service utilization report is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    fireEvent.click(screen.getByText('Service Utilization Report'));
    
    expect(mockOnChange).toHaveBeenCalledWith('SERVICE_UTILIZATION');
  });

  test('calls onNext when continue button is clicked with selected report type', () => {
    render(
      <ReportSelector 
        reportType="PATIENT_VISIT" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.click(screen.getByTestId('next-button'));
    
    expect(mockOnNext).toHaveBeenCalled();
  });

  test('shows selected state for chosen report type', () => {
    render(
      <ReportSelector 
        reportType="PATIENT_VISIT" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Selected')).toBeInTheDocument();
  });

  test('shows selected state for service utilization report type', () => {
    render(
      <ReportSelector 
        reportType="SERVICE_UTILIZATION" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Selected')).toBeInTheDocument();
  });

  test('renders all report descriptions correctly', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    expect(screen.getByText('View comprehensive details of patient visits and appointments with statistical insights.')).toBeInTheDocument();
    expect(screen.getByText('Analyze usage statistics and patterns for various hospital services and departments.')).toBeInTheDocument();
  });

  test('renders header elements correctly', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    expect(screen.getByText('Select Report Type')).toBeInTheDocument();
    expect(screen.getByText('Choose the type of report you want to generate')).toBeInTheDocument();
  });

  // Negative Test Cases(Invalid report type handling)
  test('disables continue button when no report type is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
  });

  test('shows helper text when no report type is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    expect(screen.getByText('Please select a report type to continue')).toBeInTheDocument();
  });

  test('does not show selected badge when no report type is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    expect(screen.queryByText('Selected')).not.toBeInTheDocument();
  });

  // Edge Cases(Multiple report type selections handling)
  test('handles multiple report type selections correctly', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Select first report type
    fireEvent.click(screen.getByText('Patient Visit Report'));
    expect(mockOnChange).toHaveBeenCalledWith('PATIENT_VISIT');
    
    // Select second report type
    fireEvent.click(screen.getByText('Service Utilization Report'));
    expect(mockOnChange).toHaveBeenCalledWith('SERVICE_UTILIZATION');
    
    expect(mockOnChange).toHaveBeenCalledTimes(2);
  });

  test('handles clicking the same report type multiple times', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Click the same report type multiple times
    fireEvent.click(screen.getByText('Patient Visit Report'));
    fireEvent.click(screen.getByText('Patient Visit Report'));
    fireEvent.click(screen.getByText('Patient Visit Report'));
    
    expect(mockOnChange).toHaveBeenCalledTimes(3);
    expect(mockOnChange).toHaveBeenCalledWith('PATIENT_VISIT');
  });

  //Edge Cases(Undefined reportType prop handling)
  test('handles undefined reportType prop gracefully', () => {
    render(
      <ReportSelector 
        reportType={undefined} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
    expect(screen.queryByText('Selected')).not.toBeInTheDocument();
  });

  test('handles null reportType prop gracefully', () => {
    render(
      <ReportSelector 
        reportType={null} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
    expect(screen.queryByText('Selected')).not.toBeInTheDocument();
  });

  test('handles empty string reportType prop gracefully', () => {
    render(
      <ReportSelector 
        reportType="" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toBeDisabled();
    expect(screen.queryByText('Selected')).not.toBeInTheDocument();
  });

  test('handles invalid reportType prop gracefully', () => {
    render(
      <ReportSelector 
        reportType="INVALID_TYPE" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const nextButton = screen.getByTestId('next-button');
    // The component logic only disables when reportType is falsy, not when it's invalid
    // So an invalid but truthy value will not disable the button
    expect(nextButton).not.toBeDisabled();
    expect(screen.queryByText('Selected')).not.toBeInTheDocument();
  });

  // Accessibility Tests
  test('has proper button accessibility attributes', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toHaveAttribute('disabled');
  });

  test('report type options are clickable', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Find the clickable container divs by looking for elements with cursor-pointer class
    const clickableElements = document.querySelectorAll('.cursor-pointer');
    expect(clickableElements.length).toBeGreaterThan(0);
  });

  // Visual State Tests
  test('applies correct styling when report type is selected', () => {
    render(
      <ReportSelector 
        reportType="PATIENT_VISIT" 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Find elements with the border-indigo-500 class
    const selectedElements = document.querySelectorAll('.border-indigo-500');
    expect(selectedElements.length).toBeGreaterThan(0);
  });

  test('applies correct styling when no report type is selected', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Check that the report options have the default gray border styling
    // The elements should have border-gray-200 class when not selected
    const reportOptions = document.querySelectorAll('[class*="border-gray-200"]');
    expect(reportOptions.length).toBeGreaterThan(0);
  });

  // Function Call Tests
  test('does not call onNext when continue button is disabled', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    const nextButton = screen.getByTestId('next-button');
    fireEvent.click(nextButton);
    
    expect(mockOnNext).not.toHaveBeenCalled();
  });

  test('calls onChange with correct parameters for each report type', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    fireEvent.click(screen.getByText('Patient Visit Report'));
    expect(mockOnChange).toHaveBeenLastCalledWith('PATIENT_VISIT');
    
    fireEvent.click(screen.getByText('Service Utilization Report'));
    expect(mockOnChange).toHaveBeenLastCalledWith('SERVICE_UTILIZATION');
  });

  test('calls onNext only once per click', () => {
    render(
      <ReportSelector 
        reportType="PATIENT_VISIT" 
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
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Check for SVG icons by looking for svg elements directly
    const svgElements = document.querySelectorAll('svg');
    expect(svgElements.length).toBeGreaterThan(0);
  });

  test('renders proper container structure', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    // Check for main container classes - the outermost div should have w-full
    const outerContainer = document.querySelector('.w-full');
    expect(outerContainer).toBeInTheDocument();
    expect(outerContainer).toHaveClass('w-full');
  });

  test('renders proper button structure', () => {
    render(<ReportSelector onChange={mockOnChange} onNext={mockOnNext} />);
    
    const nextButton = screen.getByTestId('next-button');
    expect(nextButton).toHaveClass('w-full');
    expect(nextButton).toHaveClass('bg-gradient-to-r');
  });
});

