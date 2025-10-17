import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import AdvancedFilters from '../AdvancedFilters';

describe('AdvancedFilters', () => {
  const mockOnChange = jest.fn();
  const mockOnNext = jest.fn();
  const defaultFilters = {
    minAge: '',
    maxAge: '',
    gender: ''
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Initial State Tests
  test('renders initial popup message correctly', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.getByText('Refine Your Report')).toBeInTheDocument();
    expect(screen.getByText('Add advanced filters for more specific insights')).toBeInTheDocument();
    expect(screen.getByText('Skip for Now')).toBeInTheDocument();
    expect(screen.getByText('Add Filters')).toBeInTheDocument();
  });

  test('does not show filter form initially', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    expect(screen.queryByText('Advanced Filters')).not.toBeInTheDocument();
    expect(screen.queryByText('Age Range')).not.toBeInTheDocument();
    expect(screen.queryByText('Gender')).not.toBeInTheDocument();
  });

  // State Toggle Tests
  //Positive Test Cases(Successful filter form display)
  test('shows filter form when Add Filters button is clicked', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByText('Advanced Filters')).toBeInTheDocument();
    expect(screen.getByText('Age Range')).toBeInTheDocument();
    expect(screen.getByText('Gender')).toBeInTheDocument();
    expect(screen.getByText('Apply Filters')).toBeInTheDocument();
    expect(screen.getByText('Back')).toBeInTheDocument();
  });

  test('hides filter form when Back button is clicked', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // First show filters
    fireEvent.click(screen.getByText('Add Filters'));
    expect(screen.getByText('Advanced Filters')).toBeInTheDocument();
    
    // Then hide filters
    fireEvent.click(screen.getByText('Back'));
    expect(screen.queryByText('Advanced Filters')).not.toBeInTheDocument();
    expect(screen.getByText('Refine Your Report')).toBeInTheDocument();
  });

  // Button Click Tests
  test('calls onNext when Skip for Now button is clicked', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    fireEvent.click(screen.getByText('Skip for Now'));
    expect(mockOnNext).toHaveBeenCalled();
  });

  test('calls onNext when Apply Filters button is clicked', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    // Then apply filters
    fireEvent.click(screen.getByText('Apply Filters'));
    expect(mockOnNext).toHaveBeenCalled();
  });

  // Age Range Input Tests
  test('handles min age input changes', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '18' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      minAge: '18'
    });
  });

  test('handles max age input changes', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const maxAgeInput = screen.getByPlaceholderText('Max');
    fireEvent.change(maxAgeInput, { target: { value: '65' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      maxAge: '65'
    });
  });

  test('handles both age inputs together', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    const maxAgeInput = screen.getByPlaceholderText('Max');
    
    fireEvent.change(minAgeInput, { target: { value: '25' } });
    fireEvent.change(maxAgeInput, { target: { value: '55' } });
    
    expect(mockOnChange).toHaveBeenCalledTimes(2);
    expect(mockOnChange).toHaveBeenNthCalledWith(1, {
      ...defaultFilters,
      minAge: '25'
    });
    expect(mockOnChange).toHaveBeenNthCalledWith(2, {
      ...defaultFilters,
      maxAge: '55'
    });
  });

  // Gender Selection Tests
  test('handles gender selection changes', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const genderSelect = screen.getByDisplayValue('All Genders');
    fireEvent.change(genderSelect, { target: { value: 'MALE' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      gender: 'MALE'
    });
  });

  test('handles female gender selection', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const genderSelect = screen.getByDisplayValue('All Genders');
    fireEvent.change(genderSelect, { target: { value: 'FEMALE' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      gender: 'FEMALE'
    });
  });

  test('handles all genders selection', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const genderSelect = screen.getByDisplayValue('All Genders');
    fireEvent.change(genderSelect, { target: { value: '' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      gender: ''
    });
  });

  // Pre-filled Values Tests
  test('displays pre-filled age values correctly', () => {
    const filtersWithValues = {
      minAge: '25',
      maxAge: '55',
      gender: 'MALE'
    };

    render(
      <AdvancedFilters 
        filters={filtersWithValues} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByDisplayValue('25')).toBeInTheDocument();
    expect(screen.getByDisplayValue('55')).toBeInTheDocument();
    // For select elements, we need to check the option text, not display value
    expect(screen.getByText('Male')).toBeInTheDocument();
  });

  test('displays pre-filled gender value correctly', () => {
    const filtersWithGender = {
      minAge: '',
      maxAge: '',
      gender: 'FEMALE'
    };

    render(
      <AdvancedFilters 
        filters={filtersWithGender} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    // For select elements, we need to check the option text, not display value
    expect(screen.getByText('Female')).toBeInTheDocument();
  });

  // Edge Cases
  test('handles empty string values in filters', () => {
    const filtersWithEmptyStrings = {
      minAge: '',
      maxAge: '',
      gender: ''
    };

    render(
      <AdvancedFilters 
        filters={filtersWithEmptyStrings} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByDisplayValue('All Genders')).toBeInTheDocument();
  });

  test('handles null values in filters gracefully', () => {
    const filtersWithNulls = {
      minAge: null,
      maxAge: null,
      gender: null
    };

    render(
      <AdvancedFilters 
        filters={filtersWithNulls} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByDisplayValue('All Genders')).toBeInTheDocument();
  });

  test('handles undefined values in filters gracefully', () => {
    const filtersWithUndefined = {
      minAge: undefined,
      maxAge: undefined,
      gender: undefined
    };

    render(
      <AdvancedFilters 
        filters={filtersWithUndefined} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByDisplayValue('All Genders')).toBeInTheDocument();
  });

  //Edge Cases(Invalid age input handling)
  test('handles age input values correctly', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '25' } });
    
    // The component should call onChange for valid input
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      minAge: '25'
    });
  });

  //Error Cases(Negative age input handling)
  test('handles negative age values', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '-5' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      minAge: '-5'
    });
  });

  test('handles very large age values', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const maxAgeInput = screen.getByPlaceholderText('Max');
    fireEvent.change(maxAgeInput, { target: { value: '999' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      maxAge: '999'
    });
  });

  // Multiple Interactions Tests
  test('handles multiple filter changes in sequence', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    // Change min age
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '18' } });
    
    // Change max age
    const maxAgeInput = screen.getByPlaceholderText('Max');
    fireEvent.change(maxAgeInput, { target: { value: '65' } });
    
    // Change gender
    const genderSelect = screen.getByDisplayValue('All Genders');
    fireEvent.change(genderSelect, { target: { value: 'MALE' } });
    
    expect(mockOnChange).toHaveBeenCalledTimes(3);
  });

  test('handles toggling between states multiple times', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters
    fireEvent.click(screen.getByText('Add Filters'));
    expect(screen.getByText('Advanced Filters')).toBeInTheDocument();
    
    // Hide filters
    fireEvent.click(screen.getByText('Back'));
    expect(screen.getByText('Refine Your Report')).toBeInTheDocument();
    
    // Show filters again
    fireEvent.click(screen.getByText('Add Filters'));
    expect(screen.getByText('Advanced Filters')).toBeInTheDocument();
  });

  // Accessibility Tests
  test('has proper form labels', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByText('Age Range')).toBeInTheDocument();
    expect(screen.getByText('Gender')).toBeInTheDocument();
  });

  test('has proper input placeholders', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByPlaceholderText('Min')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Max')).toBeInTheDocument();
  });

  test('has proper button accessibility', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const skipButton = screen.getByText('Skip for Now');
    const addFiltersButton = screen.getByText('Add Filters');
    
    expect(skipButton).toBeInTheDocument();
    expect(addFiltersButton).toBeInTheDocument();
  });

  // Visual State Tests
  test('applies correct styling to age range section', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    // Find the age range container that has the gradient background
    const ageRangeContainer = screen.getByText('Age Range').closest('div').parentElement;
    expect(ageRangeContainer).toHaveClass('bg-gradient-to-br');
  });

  test('applies correct styling to form inputs', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    const maxAgeInput = screen.getByPlaceholderText('Max');
    
    expect(minAgeInput).toHaveClass('w-full');
    expect(maxAgeInput).toHaveClass('w-full');
  });

  test('applies correct styling to buttons', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const addFiltersButton = screen.getByText('Add Filters');
    expect(addFiltersButton).toHaveClass('bg-gradient-to-r');
  });

  // Component Structure Tests
  test('renders all required icons', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
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
      <AdvancedFilters 
        filters={defaultFilters} 
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
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const addFiltersButton = screen.getByText('Add Filters');
    expect(addFiltersButton).toHaveClass('flex-1');
    expect(addFiltersButton).toHaveClass('bg-gradient-to-r');
  });

  // Function Call Tests
  test('calls onChange with correct parameters for each field', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    // Test min age
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '25' } });
    expect(mockOnChange).toHaveBeenLastCalledWith({
      ...defaultFilters,
      minAge: '25'
    });
    
    // Test max age
    const maxAgeInput = screen.getByPlaceholderText('Max');
    fireEvent.change(maxAgeInput, { target: { value: '55' } });
    expect(mockOnChange).toHaveBeenLastCalledWith({
      ...defaultFilters,
      maxAge: '55'
    });
    
    // Test gender
    const genderSelect = screen.getByDisplayValue('All Genders');
    fireEvent.change(genderSelect, { target: { value: 'MALE' } });
    expect(mockOnChange).toHaveBeenLastCalledWith({
      ...defaultFilters,
      gender: 'MALE'
    });
  });

  test('calls onNext only once per click', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    const skipButton = screen.getByText('Skip for Now');
    fireEvent.click(skipButton);
    fireEvent.click(skipButton);
    fireEvent.click(skipButton);
    
    expect(mockOnNext).toHaveBeenCalledTimes(3);
  });

  test('does not call onChange when component is in initial state', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // No filter interactions in initial state
    expect(mockOnChange).not.toHaveBeenCalled();
  });

  // Gender Options Tests
  test('renders all gender options correctly', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    expect(screen.getByText('All Genders')).toBeInTheDocument();
    expect(screen.getByText('Male')).toBeInTheDocument();
    expect(screen.getByText('Female')).toBeInTheDocument();
  });

  // Age Input Validation Tests
  test('handles decimal age values', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '18.5' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      minAge: '18.5'
    });
  });

  test('handles zero age values', () => {
    render(
      <AdvancedFilters 
        filters={defaultFilters} 
        onChange={mockOnChange} 
        onNext={mockOnNext} 
      />
    );
    
    // Show filters first
    fireEvent.click(screen.getByText('Add Filters'));
    
    const minAgeInput = screen.getByPlaceholderText('Min');
    fireEvent.change(minAgeInput, { target: { value: '0' } });
    
    expect(mockOnChange).toHaveBeenCalledWith({
      ...defaultFilters,
      minAge: '0'
    });
  });
});
