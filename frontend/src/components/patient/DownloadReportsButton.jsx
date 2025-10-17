import React, { useState } from 'react';
import api from '../../services/api';
// Libraries: jspdf, jspdf-autotable, html2canvas
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import html2canvas from 'html2canvas';
import JsBarcode from 'jsbarcode';

/**
 * DownloadReportsButton
 * - Fetches patient-related data from backend endpoints
 * - Captures chart DOM elements by id (if present) using html2canvas
 * - Generates a multi-page professional PDF using jsPDF and autoTable
 *
 * Adheres to SOLID principles:
 * - Single Responsibility: this component only orchestrates download flow
 * - Open/Closed: data fetching endpoints are kept in small functions and can be extended
 * - Dependency Inversion: uses `api` abstraction for HTTP
 */
const endpoints = {
  profile: '/api/user-data/current',
  emergencyContacts: '/api/patients/me/emergency-contacts',
  conditions: '/api/patients/me/conditions',
  allergies: '/api/patients/me/allergies',
  prescriptions: '/api/patients/me/prescriptions',
  healthMetrics: '/api/patients/me/health-metrics/latest',
  bpRecords: '/api/patients/me/blood-pressure-records',
  weightRecords: '/api/patients/me/weight-records'
};

const fetchAll = async () => {
  const requests = Object.entries(endpoints).map(([key, url]) => (
    api.get(url).then(r => ({ key, data: r.data })).catch(err => {
      console.error(`Error fetching ${key}:`, err);
      return { key, data: null };
    })
  ));
  const results = await Promise.all(requests);
  return results.reduce((acc, r) => ({ ...acc, [r.key]: r.data }), {});
};

const captureElement = async (selector) => {
  const el = document.querySelector(selector);
  if (!el) return null;
  try {
    const canvas = await html2canvas(el, { scale: 2 });
    return canvas.toDataURL('image/png');
  } catch (e) {
    console.error('captureElement error', e);
    return null;
  }
};

const addSectionHeader = (doc, title, y, pageWidth) => {
  // Add top spacing before header so headings don't sit too close to previous content
  const topSpacing = 12;
  y += topSpacing;
  // Prominent, bold section header with extra spacing; no blue accent line
  doc.setFontSize(14);
  doc.setTextColor('#111827');
  doc.setFont('helvetica', 'bold');
  doc.text(title, 40, y + 6);
  doc.setFont('helvetica', 'normal');
  // subtle divider
  doc.setDrawColor(220);
  doc.setLineWidth(0.5);
  doc.line(40, y + 12, pageWidth - 40, y + 12);
  return y + 28; // larger spacing after header
};

const ensureSpace = (doc, y, needed) => {
  const pageHeight = doc.internal.pageSize.getHeight();
  if (y + needed > pageHeight - 60) {
    doc.addPage();
    return 60; // top margin on new page
  }
  return y;
};

const DownloadReportsButton = () => {
  const [loading, setLoading] = useState(false);

  const handleDownload = async () => {
    setLoading(true);
    try {
      const data = await fetchAll();

      // Capture charts if present
      const bpImage = await captureElement('#bp-trend-chart');
      const weightImage = await captureElement('#weight-history-chart');

      const doc = new jsPDF({ unit: 'pt', format: 'a4' });
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      const marginLeft = 40;
      let y = 60;

      // Header band
      doc.setFillColor(37, 99, 235); // indigo-600
      doc.rect(0, 0, pageWidth, 48, 'F');
      doc.setFontSize(20);
      doc.setTextColor('#ffffff');
      doc.text('Patient Health Report', pageWidth / 2, 32, { align: 'center' });

      // Patient summary card (right aligned small box)
      const p = data.profile?.data || data.profile || {};
      // Generate barcode SVG for patient ID and insert on right side of header
      const patientId = p.patientId || p.userId || '';
      if (patientId) {
        try {
          const svgNS = 'http://www.w3.org/2000/svg';
          const svg = document.createElementNS(svgNS, 'svg');
          JsBarcode(svg, String(patientId), { format: 'CODE128', displayValue: false, height: 40, margin: 0 });
          // convert svg to png via canvas
          const svgData = new XMLSerializer().serializeToString(svg);
          const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
          const url = URL.createObjectURL(svgBlob);
          /* eslint-disable no-await-in-loop */
          const img = await new Promise((resolve, reject) => {
            const image = new Image();
            image.onload = () => resolve(image);
            image.onerror = reject;
            image.src = url;
          });
          // draw to canvas to get a PNG data URL (ensures compatibility)
          const canvas = document.createElement('canvas');
          canvas.width = img.width || 300;
          canvas.height = img.height || 80;
          const ctx = canvas.getContext('2d');
          ctx.fillStyle = '#ffffff';
          ctx.fillRect(0, 0, canvas.width, canvas.height);
          ctx.drawImage(img, 0, 0);
          const dataUrl = canvas.toDataURL('image/png');
          URL.revokeObjectURL(url);
          // draw the barcode image on header (right side)
          const barW = 120;
          const barH = 32;
          doc.addImage(dataUrl, 'PNG', pageWidth - marginLeft - barW, 8, barW, barH);
        } catch (e) {
          console.error('Error generating barcode for PDF', e);
        }
      }
      y = 70;
      y = addSectionHeader(doc, 'Personal Information', y, pageWidth);
      doc.setFontSize(11);
      doc.setTextColor('#0f172a');

      const infoLines = [
        `Name: ${p.firstName || ''} ${p.lastName || ''}`,
        `Patient ID: ${p.patientId || 'N/A'}`,
        `DOB: ${p.dateOfBirth || 'N/A'}`,
        `Gender: ${p.gender || 'N/A'}`,
        `Phone: ${p.contactNumber || 'N/A'}`,
        `Email: ${p.email || 'N/A'}`,
      ];

      // render as two-column layout to save space and avoid overlap
      const leftX = marginLeft;
      const rightX = pageWidth / 2 + 10;
      let lx = leftX;
      let rx = rightX;
      let ly = y;

      infoLines.forEach((line, idx) => {
        if (idx % 2 === 0) {
          doc.text(line, lx, ly);
        } else {
          doc.text(line, rx, ly);
          ly += 16;
        }
      });
      y = ly + 14;

      // Address (longer block)
      const addr = p.address || 'N/A';
      const addrLines = doc.splitTextToSize(`Address: ${addr}`, pageWidth - marginLeft * 2);
      doc.text(addrLines, leftX, y);
      y += addrLines.length * 12 + 6;

      // Emergency contacts
      y = ensureSpace(doc, y, 120);
      y = addSectionHeader(doc, 'Emergency Contacts', y, pageWidth);
      const contacts = data.emergencyContacts || [];
      if (contacts.length === 0) {
        doc.setFontSize(11); doc.text('No emergency contacts', marginLeft, y); y += 18;
      } else {
        // styled autoTable
        // @ts-ignore
        doc.autoTable({
          startY: y,
          margin: { left: marginLeft, right: marginLeft },
          head: [['Name', 'Relationship', 'Phone', 'Email']],
          body: contacts.map(c => [c.fullName || '', c.relationship || '', c.phone || '', c.email || '']),
          theme: 'striped',
          headStyles: { fillColor: [37, 99, 235], textColor: 255, halign: 'left' },
          styles: { fontSize: 10, cellPadding: 6, overflow: 'linebreak' },
          columnStyles: { 3: { cellWidth: 140 } }
        });
        y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 10 : y + 80;
      }

      // Conditions
      y = ensureSpace(doc, y, 120);
      y = addSectionHeader(doc, 'Conditions', y, pageWidth);
      const conditions = data.conditions || [];
      if (conditions.length === 0) {
        doc.text('No conditions recorded', marginLeft, y); y += 18;
      } else {
        // @ts-ignore
        doc.autoTable({ startY: y, margin: { left: marginLeft, right: marginLeft }, head: [['Condition', 'Diagnosed', 'Notes']], body: conditions.map(c => [c.conditionName || '', c.diagnosedDate || '', c.notes || '']), headStyles: { fillColor: [75, 85, 99], textColor: 255 }, styles: { fontSize: 10, overflow: 'linebreak' }, columnStyles: { 2: { cellWidth: 200 } } });
        y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 10 : y + 80;
      }

      // Allergies
      y = ensureSpace(doc, y, 120);
      y = addSectionHeader(doc, 'Allergies', y, pageWidth);
      const allergies = data.allergies || [];
      if (allergies.length === 0) {
        doc.text('No allergies recorded', marginLeft, y); y += 18;
      } else {
        // @ts-ignore
        doc.autoTable({ startY: y, margin: { left: marginLeft, right: marginLeft }, head: [['Allergy', 'Severity', 'Notes']], body: allergies.map(a => [a.allergyName || '', a.severity || '', a.notes || '']), headStyles: { fillColor: [75, 85, 99], textColor: 255 }, styles: { fontSize: 10, overflow: 'linebreak' }, columnStyles: { 2: { cellWidth: 220 } } });
        y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 10 : y + 80;
      }

      // Prescriptions
      y = ensureSpace(doc, y, 120);
      y = addSectionHeader(doc, 'Prescriptions', y, pageWidth);
      const prescriptions = data.prescriptions || [];
      if (prescriptions.length === 0) {
        doc.text('No prescriptions recorded', marginLeft, y); y += 18;
      } else {
        // @ts-ignore
        doc.autoTable({ startY: y, margin: { left: marginLeft, right: marginLeft }, head: [['Medication', 'Dosage', 'Frequency', 'Start Date']], body: prescriptions.map(pr => [pr.medicationName || '', pr.dosage || '', pr.frequency || '', pr.startDate || '']), headStyles: { fillColor: [37, 99, 235], textColor: 255 }, styles: { fontSize: 10, overflow: 'linebreak' } });
        y = doc.lastAutoTable ? doc.lastAutoTable.finalY + 10 : y + 80;
      }

      // Health metrics summary as three cards in a row with emoji icons
      y = ensureSpace(doc, y, 160);
      y = addSectionHeader(doc, 'Latest Health Metrics', y, pageWidth);
      const metrics = data.healthMetrics || null;
      if (!metrics) {
        doc.text('No health metrics available', marginLeft, y); y += 18;
      } else {
        const cardGap = 12;
        const cardW = (pageWidth - marginLeft * 2 - cardGap * 2) / 3; // three cards
        const cardH = 80;
        const baseY = y;

        const drawMetricCard = (x, colorRgb, title, value, unit) => {
          // background card
          doc.setFillColor(255, 255, 255);
          doc.roundedRect(x, baseY, cardW, cardH, 6, 6, 'F');
          // drawn circular icon
          const circleX = x + 22;
          const circleY = baseY + 28;
          const circleR = 16;
          doc.setDrawColor(0);
          doc.setFillColor(...colorRgb);
          doc.circle(circleX, circleY, circleR, 'F');
          // small label inside circle (initial)
          doc.setFont('helvetica', 'bold');
          doc.setFontSize(10);
          const label = (title === 'Blood Pressure') ? 'BP' : (title === 'Heart Rate' ? 'HR' : 'OS');
          doc.setTextColor(255, 255, 255);
          doc.text(label, circleX - 6, circleY + 4);

          // title
          doc.setFont('helvetica', 'normal');
          doc.setFontSize(10);
          doc.setTextColor('#6b7280');
          doc.text(title, x + 56, baseY + 20);
          // value
          doc.setFont('helvetica', 'bold');
          doc.setFontSize(14);
          doc.setTextColor('#111827');
          doc.text(`${value}${unit ? ' ' + unit : ''}`, x + 56, baseY + 44);
          doc.setFont('helvetica', 'normal');
        };

        const bpX = marginLeft;
        const hrX = marginLeft + cardW + cardGap;
        const spoX = marginLeft + (cardW + cardGap) * 2;

        const systolic = metrics.systolic || '-';
        const diastolic = metrics.diastolic || '-';
  drawMetricCard(bpX, [239, 68, 68], 'Blood Pressure', `${systolic} / ${diastolic}`, 'mmHg');
  drawMetricCard(hrX, [236, 72, 153], 'Heart Rate', metrics.heartRate || '-', 'bpm');
  drawMetricCard(spoX, [34, 197, 94], 'SpO2', metrics.spo2 || '-', '%');

        y += cardH + 18;
      }

      // Charts images
      if (bpImage) {
        y = ensureSpace(doc, y, 260);
        y = addSectionHeader(doc, 'Blood Pressure Trend', y, pageWidth);
        const imgW = pageWidth - marginLeft * 2;
        const imgH = (imgW * 9) / 16;
        doc.addImage(bpImage, 'PNG', marginLeft, y, imgW, imgH);
        y += imgH + 12;
      }

      if (weightImage) {
        y = ensureSpace(doc, y, 260);
        y = addSectionHeader(doc, 'Weight History', y, pageWidth);
        const imgW = pageWidth - marginLeft * 2;
        const imgH = (imgW * 9) / 16;
        doc.addImage(weightImage, 'PNG', marginLeft, y, imgW, imgH);
        y += imgH + 12;
      }

      // Footer with generation time and page number
      const generatedAt = new Date().toLocaleString();
      const pageCount = doc.internal.getNumberOfPages();
      for (let i = 1; i <= pageCount; i++) {
        doc.setPage(i);
        doc.setFontSize(9);
        doc.setTextColor('#6b7280');
        doc.text(`Generated: ${generatedAt}`, marginLeft, pageHeight - 30);
        doc.text(`Page ${i} / ${pageCount}`, pageWidth - marginLeft, pageHeight - 30, { align: 'right' });
      }

      doc.save(`patient-report-${p.patientId || p.userId || 'unknown'}.pdf`);
    } catch (e) {
      console.error('DownloadReportsButton error', e);
      alert('Failed to generate report. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <button
        onClick={handleDownload}
        disabled={loading}
        className="w-full mt-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
      >
        {loading ? 'Preparing Report...' : 'Download Full Report (PDF)'}
      </button>
    </div>
  );
};

export default DownloadReportsButton;
