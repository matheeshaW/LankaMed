# Appointment Management System

This directory contains the new appointment management components for the LankaMed system.

## Components

### DoctorList.jsx
- Displays a list of available doctors with search and filter functionality
- Features modern card-based UI with doctor information
- Includes search by name, specialization, and date
- Sorting options by rating, experience, fee, and name
- "Book Appointment" button for each doctor

### AppointmentForm.jsx
- Modal form for booking appointments
- Patient information collection
- Date and time slot selection
- Reason for appointment input
- Form validation and error handling
- Integration with mock data

### UserAppointments.jsx
- Patient's appointment management dashboard
- Displays upcoming and all appointments
- Status tracking (Pending, Approved, Confirmed, Completed, etc.)
- Quick stats and filtering
- Review functionality for completed appointments

### ReviewSection.jsx
- Modal for adding doctor reviews
- Star rating system (1-5 stars)
- Review comment input
- Displays existing reviews for the doctor
- Average rating calculation

## Data Management

### mockData.js
- Comprehensive dummy data for doctors, appointments, and reviews
- Helper functions for data manipulation
- Mock user and admin data
- Functions for booking appointments and updating status

## Features

### Patient Side
1. **Doctor Search & Filter**: Search doctors by name, specialization, and available date
2. **Appointment Booking**: Complete booking form with validation
3. **Appointment Management**: View all appointments with status tracking
4. **Review System**: Add reviews for completed appointments

### Admin Side
1. **Appointment Overview**: View all appointments with statistics
2. **Status Management**: Update appointment status (Pending → Approved → Confirmed → Completed)
3. **Search & Filter**: Advanced filtering and sorting options
4. **Quick Actions**: Bulk operations and status summaries

## UI/UX Features

- **Modern Design**: Clean, professional interface with gradient backgrounds
- **Responsive Layout**: Works on desktop, tablet, and mobile devices
- **Interactive Elements**: Hover effects, animations, and smooth transitions
- **Status Indicators**: Color-coded status badges with icons
- **Form Validation**: Real-time validation with error messages
- **Loading States**: Loading indicators for better user experience

## Status Flow

1. **PENDING**: Initial status when appointment is booked
2. **APPROVED**: Admin approves the appointment
3. **CONFIRMED**: Patient confirms the appointment
4. **COMPLETED**: Appointment is finished
5. **REJECTED**: Admin rejects the appointment
6. **CANCELLED**: Appointment is cancelled

## Usage

The components are integrated into the PatientDashboard and AdminDashboard pages. The appointment flow works entirely with mock data, making it perfect for demonstration and testing purposes.

## Dependencies

- React 19.1.1
- React Router DOM 6.26.2
- Tailwind CSS 3.4.13
- No external API dependencies (uses mock data)
