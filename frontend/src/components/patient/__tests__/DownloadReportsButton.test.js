import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import DownloadReportsButton from '../DownloadReportsButton';

// Mock html2canvas and jsPDF
jest.mock('html2canvas', () => jest.fn(() => Promise.resolve({
  toDataURL: () => 'data:image/png;base64,FAKEPNG'
})));

const mockSave = jest.fn();
const mockAddImage = jest.fn();
const mockText = jest.fn();

jest.mock('jspdf', () => {
  return jest.fn().mockImplementation(() => ({
    save: mockSave,
    addImage: mockAddImage,
    text: mockText,
    addPage: jest.fn()
  }));
});

describe('DownloadReportsButton', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders button and starts download flow when clicked', async () => {
    render(<DownloadReportsButton patientId={1} />);

    const btn = screen.getByRole('button', { name: /download full report/i });
    expect(btn).toBeInTheDocument();

    fireEvent.click(btn);

    // The implementation begins async operations (html2canvas + jsPDF). Wait briefly for calls
    await waitFor(() => {
      expect(mockSave).toHaveBeenCalled();
    });
  });
});
