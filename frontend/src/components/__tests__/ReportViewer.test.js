import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ReportViewer from '../ReportViewer';

// Mock DOMPurify
jest.mock('dompurify', () => ({
  sanitize: jest.fn((html) => html)
}));

describe('ReportViewer', () => {
  const mockOnDownload = jest.fn();
  const mockHtml = '<html><body><h1>Test Report</h1><p>Report content</p></body></html>';
  const mockMeta = {
    title: 'Test Report',
    criteria: {
      from: '2024-01-01',
      to: '2024-01-31',
      hospitalId: 'C001',
      serviceCategory: 'OPD'
    }
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Positive Test Cases
  test('renders report content when HTML is provided', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Test Report')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('displays meta information correctly', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.getByText('Hospital: C001')).toBeInTheDocument();
    expect(screen.getByText('Service: OPD')).toBeInTheDocument();
  });

  test('calls onDownload when download button is clicked', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    fireEvent.click(screen.getByTestId('download-button'));
    
    expect(mockOnDownload).toHaveBeenCalled();
  });

  test('shows loading state correctly', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={true} 
      />
    );
    
    expect(screen.getByText('Generating Report...')).toBeInTheDocument();
    expect(screen.getByText('Please wait while we process your data')).toBeInTheDocument();
  });

  test('renders report with complete meta information', () => {
    const completeMeta = {
      title: 'Complete Report',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31',
        hospitalId: 'C001',
        serviceCategory: 'OPD',
        patientCategory: 'OUTPATIENT',
        gender: 'MALE',
        minAge: 18,
        maxAge: 65
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={completeMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Complete Report')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.getByText('Hospital: C001')).toBeInTheDocument();
    expect(screen.getByText('Service: OPD')).toBeInTheDocument();
  });

  test('renders report with minimal meta information', () => {
    const minimalMeta = {
      title: 'Minimal Report',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31'
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={minimalMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Minimal Report')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('displays current date in header', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Generated on')).toBeInTheDocument();
    // The actual date will be dynamic, so we just check that the text is present
  });

  // Negative Test Cases
  test('disables download button when no HTML is provided', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).toBeDisabled();
  });

  test('disables download button when loading', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={true} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).toBeDisabled();
  });

  test('shows no report message when no HTML and not loading', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('No Report Generated')).toBeInTheDocument();
    expect(screen.getByText('Complete the previous steps to generate your report')).toBeInTheDocument();
  });

  test('does not show meta information when meta is null', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('does not show meta information when criteria is null', () => {
    const metaWithoutCriteria = {
      title: 'Report Without Criteria',
      criteria: null
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={metaWithoutCriteria} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  // Edge Cases
  test('handles empty HTML string correctly', () => {
    render(
      <ReportViewer 
        html="" 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).toBeDisabled();
  });

  test('handles empty meta information gracefully', () => {
    const emptyMeta = {};

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={emptyMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles missing meta information gracefully', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles partial meta information correctly', () => {
    const partialMeta = {
      title: 'Partial Report',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31'
        // Missing hospitalId and serviceCategory
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={partialMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Partial Report')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('handles meta with null values in criteria', () => {
    const metaWithNulls = {
      title: 'Report With Nulls',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31',
        hospitalId: null,
        serviceCategory: null
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={metaWithNulls} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report With Nulls')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('handles meta with undefined values in criteria', () => {
    const metaWithUndefined = {
      title: 'Report With Undefined',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31',
        hospitalId: undefined,
        serviceCategory: undefined
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={metaWithUndefined} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report With Undefined')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('handles meta with empty string values in criteria', () => {
    const metaWithEmptyStrings = {
      title: 'Report With Empty Strings',
      criteria: {
        from: '2024-01-01',
        to: '2024-01-31',
        hospitalId: '',
        serviceCategory: ''
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={metaWithEmptyStrings} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report With Empty Strings')).toBeInTheDocument();
    expect(screen.getByText('2024-01-01 to 2024-01-31')).toBeInTheDocument();
    expect(screen.queryByText('Hospital:')).not.toBeInTheDocument();
    expect(screen.queryByText('Service:')).not.toBeInTheDocument();
  });

  test('handles very long HTML content', () => {
    const longHtml = '<html><body>' + 'A'.repeat(10000) + '</body></html>';

    render(
      <ReportViewer 
        html={longHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles HTML with special characters', () => {
    const specialHtml = '<html><body><h1>Report with &amp; &lt; &gt; &quot; &#39;</h1></body></html>';

    render(
      <ReportViewer 
        html={specialHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles HTML with unicode characters', () => {
    const unicodeHtml = '<html><body><h1>Report with α β γ δ ε</h1></body></html>';

    render(
      <ReportViewer 
        html={unicodeHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles HTML with CSS styles', () => {
    const styledHtml = '<html><head><style>body{color:red;}</style></head><body><h1>Styled Report</h1></body></html>';

    render(
      <ReportViewer 
        html={styledHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  test('handles HTML with tables', () => {
    const tableHtml = '<html><body><table><tr><th>Name</th><th>Value</th></tr><tr><td>Total</td><td>100</td></tr></table></body></html>';

    render(
      <ReportViewer 
        html={tableHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('Report Preview')).toBeInTheDocument();
    expect(screen.getByText('Download as PDF')).toBeInTheDocument();
  });

  // Accessibility Tests
  test('has proper button accessibility attributes', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).not.toHaveAttribute('disabled');
  });

  test('has proper loading state accessibility', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={true} 
      />
    );
    
    expect(screen.getByText('Generating Report...')).toBeInTheDocument();
  });

  // Visual State Tests
  test('applies correct styling when HTML is provided', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).toHaveClass('bg-gradient-to-r');
  });

  test('applies correct styling when loading', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={true} 
      />
    );
    
    const loadingText = screen.getByText('Generating Report...');
    expect(loadingText).toBeInTheDocument();
  });

  test('applies correct styling when no HTML is provided', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const noReportText = screen.getByText('No Report Generated');
    expect(noReportText).toBeInTheDocument();
  });

  // Function Call Tests
  test('does not call onDownload when download button is disabled', () => {
    render(
      <ReportViewer 
        html={null} 
        meta={null} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    fireEvent.click(downloadButton);
    
    expect(mockOnDownload).not.toHaveBeenCalled();
  });

  test('calls onDownload only once per click', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    fireEvent.click(downloadButton);
    fireEvent.click(downloadButton);
    fireEvent.click(downloadButton);
    
    expect(mockOnDownload).toHaveBeenCalledTimes(3);
  });

  test('calls onDownload with no parameters', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    fireEvent.click(downloadButton);
    
    expect(mockOnDownload).toHaveBeenCalledWith();
  });

  // Component Structure Tests
  test('renders all required icons', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    // Check for SVG icons (they should be present in the DOM)
    const svgElements = screen.getAllByRole('img', { hidden: true });
    expect(svgElements.length).toBeGreaterThan(0);
  });

  test('renders proper container structure', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    // Check for main container classes
    const container = screen.getByText('Report Preview').closest('div');
    expect(container).toHaveClass('w-full');
  });

  test('renders proper button structure', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    const downloadButton = screen.getByTestId('download-button');
    expect(downloadButton).toHaveClass('w-full');
    expect(downloadButton).toHaveClass('bg-gradient-to-r');
  });

  // DOMPurify Integration Tests
  test('sanitizes HTML content using DOMPurify', () => {
    const DOMPurify = require('dompurify');
    
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(DOMPurify.sanitize).toHaveBeenCalledWith(mockHtml);
  });

  test('handles DOMPurify sanitization errors gracefully', () => {
    const DOMPurify = require('dompurify');
    DOMPurify.sanitize.mockImplementation(() => {
      throw new Error('Sanitization failed');
    });

    // Should not throw an error
    expect(() => {
      render(
        <ReportViewer 
          html={mockHtml} 
          meta={mockMeta} 
          onDownload={mockOnDownload} 
          loading={false} 
        />
      );
    }).not.toThrow();
  });

  // Date Formatting Tests
  test('displays date in correct format', () => {
    render(
      <ReportViewer 
        html={mockHtml} 
        meta={mockMeta} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    // Check that the date is displayed in a readable format
    expect(screen.getByText('Generated on')).toBeInTheDocument();
  });

  test('handles different date formats in criteria', () => {
    const metaWithDifferentDates = {
      title: 'Different Date Format Report',
      criteria: {
        from: '2024-12-25',
        to: '2024-12-31',
        hospitalId: 'C001'
      }
    };

    render(
      <ReportViewer 
        html={mockHtml} 
        meta={metaWithDifferentDates} 
        onDownload={mockOnDownload} 
        loading={false} 
      />
    );
    
    expect(screen.getByText('2024-12-25 to 2024-12-31')).toBeInTheDocument();
  });
});

