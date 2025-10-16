// Mock data for the LankaMed Appointment Management System

// Default data
const defaultAppointments = [
  {
    id: 1,
    patientId: 1,
    patientName: "John Doe",
    doctorId: 1,
    doctorName: "Dr. Sarah Johnson",
    doctorSpecialization: "Cardiology",
    hospitalName: "Colombo General Hospital",
    appointmentDate: "2024-01-15",
    appointmentTime: "09:00",
    status: "PENDING",
    reason: "Chest pain and shortness of breath",
    createdAt: "2024-01-10T10:30:00Z",
    updatedAt: "2024-01-10T10:30:00Z"
  },
  {
    id: 2,
    patientId: 2,
    patientName: "Jane Smith",
    doctorId: 3,
    doctorName: "Dr. Priya Fernando",
    doctorSpecialization: "Pediatrics",
    hospitalName: "Lady Ridgeway Hospital",
    appointmentDate: "2024-01-16",
    appointmentTime: "09:30",
    status: "APPROVED",
    reason: "Regular checkup for 5-year-old child",
    createdAt: "2024-01-08T14:20:00Z",
    updatedAt: "2024-01-09T09:15:00Z"
  },
  {
    id: 3,
    patientId: 3,
    patientName: "Robert Wilson",
    doctorId: 4,
    doctorName: "Dr. David Silva",
    doctorSpecialization: "Orthopedics",
    hospitalName: "Asiri Hospital",
    appointmentDate: "2024-01-15",
    appointmentTime: "10:00",
    status: "CONFIRMED",
    reason: "Knee injury from sports",
    createdAt: "2024-01-05T16:45:00Z",
    updatedAt: "2024-01-12T11:30:00Z"
  },
  {
    id: 4,
    patientId: 4,
    patientName: "Maria Garcia",
    doctorId: 2,
    doctorName: "Dr. Michael Chen",
    doctorSpecialization: "Neurology",
    hospitalName: "National Hospital of Sri Lanka",
    appointmentDate: "2024-01-16",
    appointmentTime: "08:30",
    status: "COMPLETED",
    reason: "Headache and dizziness",
    createdAt: "2024-01-03T12:10:00Z",
    updatedAt: "2024-01-16T17:00:00Z"
  },
  {
    id: 5,
    patientId: 5,
    patientName: "Ahmed Hassan",
    doctorId: 5,
    doctorName: "Dr. Anjali Perera",
    doctorSpecialization: "Dermatology",
    hospitalName: "Nawaloka Hospital",
    appointmentDate: "2024-01-15",
    appointmentTime: "11:00",
    status: "REJECTED",
    reason: "Skin rash consultation",
    createdAt: "2024-01-11T08:20:00Z",
    updatedAt: "2024-01-12T14:45:00Z"
  }
];

const defaultReviews = [
  {
    id: 1,
    appointmentId: 4,
    doctorId: 2,
    patientName: "Maria Garcia",
    rating: 5,
    comment: "Excellent doctor! Very thorough examination and clear explanations. Highly recommended.",
    createdAt: "2024-01-16T18:30:00Z"
  },
  {
    id: 2,
    appointmentId: 3,
    doctorId: 4,
    patientName: "Robert Wilson",
    rating: 4,
    comment: "Good experience overall. Doctor was professional and the treatment helped with my knee pain.",
    createdAt: "2024-01-15T16:20:00Z"
  }
];

// Load data from localStorage or use defaults
const loadAppointmentsFromStorage = () => {
  try {
    const stored = localStorage.getItem('lankamed_appointments');
    return stored ? JSON.parse(stored) : defaultAppointments;
  } catch (error) {
    console.error('Error loading appointments from storage:', error);
    return defaultAppointments;
  }
};

const loadReviewsFromStorage = () => {
  try {
    const stored = localStorage.getItem('lankamed_reviews');
    return stored ? JSON.parse(stored) : defaultReviews;
  } catch (error) {
    console.error('Error loading reviews from storage:', error);
    return defaultReviews;
  }
};

// Initialize data from storage
let mockAppointments = loadAppointmentsFromStorage();
let mockReviews = loadReviewsFromStorage();

// Generate future dates for available slots
const generateAvailableSlots = (doctorId) => {
  const slots = [];
  const today = new Date();
  
  // Generate slots for the next 30 days
  for (let i = 1; i <= 30; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    const dateString = date.toISOString().split('T')[0];
    
    // Different doctors have different availability patterns
    const timeSlots = [
      { time: "09:00", available: Math.random() > 0.3 },
      { time: "10:30", available: Math.random() > 0.2 },
      { time: "11:00", available: Math.random() > 0.4 },
      { time: "14:00", available: Math.random() > 0.1 },
      { time: "15:30", available: Math.random() > 0.3 },
      { time: "16:00", available: Math.random() > 0.2 }
    ];
    
    timeSlots.forEach(slot => {
      slots.push({
        date: dateString,
        time: slot.time,
        available: slot.available
      });
    });
  }
  
  return slots;
};

export const mockDoctors = [
  {
    id: 1,
    name: "Dr. Sarah Johnson",
    specialization: "Cardiology",
    experience: 15,
    rating: 4.8,
    reviewCount: 127,
    fee: 2500,
    hospital: "Colombo General Hospital",
    hospitalAddress: "123 Main Street, Colombo 07",
    hospitalContact: "+94 11 234 5678",
    availableSlots: generateAvailableSlots(1),
    image: "👩‍⚕️"
  },
  {
    id: 2,
    name: "Dr. Michael Chen",
    specialization: "Neurology",
    experience: 12,
    rating: 4.6,
    reviewCount: 89,
    fee: 3000,
    hospital: "National Hospital of Sri Lanka",
    hospitalAddress: "456 Independence Avenue, Colombo 07",
    hospitalContact: "+94 11 345 6789",
    availableSlots: generateAvailableSlots(2),
    image: "👨‍⚕️"
  },
  {
    id: 3,
    name: "Dr. Priya Fernando",
    specialization: "Pediatrics",
    experience: 8,
    rating: 4.9,
    reviewCount: 156,
    fee: 2000,
    hospital: "Lady Ridgeway Hospital",
    hospitalAddress: "789 Hospital Road, Colombo 05",
    hospitalContact: "+94 11 456 7890",
    availableSlots: generateAvailableSlots(3),
    image: "👩‍⚕️"
  },
  {
    id: 4,
    name: "Dr. David Silva",
    specialization: "Orthopedics",
    experience: 20,
    rating: 4.7,
    reviewCount: 203,
    fee: 3500,
    hospital: "Asiri Hospital",
    hospitalAddress: "321 Hospital Lane, Colombo 06",
    hospitalContact: "+94 11 567 8901",
    availableSlots: generateAvailableSlots(4),
    image: "👨‍⚕️"
  },
  {
    id: 5,
    name: "Dr. Anjali Perera",
    specialization: "Dermatology",
    experience: 10,
    rating: 4.5,
    reviewCount: 94,
    fee: 2200,
    hospital: "Nawaloka Hospital",
    hospitalAddress: "654 Commercial Street, Colombo 02",
    hospitalContact: "+94 11 678 9012",
    availableSlots: generateAvailableSlots(5),
    image: "👩‍⚕️"
  },
  {
    id: 6,
    name: "Dr. Rajesh Kumar",
    specialization: "Gastroenterology",
    experience: 18,
    rating: 4.8,
    reviewCount: 142,
    fee: 2800,
    hospital: "Durdans Hospital",
    hospitalAddress: "987 Hospital Drive, Colombo 03",
    hospitalContact: "+94 11 789 0123",
    availableSlots: generateAvailableSlots(6),
    image: "👨‍⚕️"
  }
];

// Export the loaded data
export { mockAppointments, mockReviews };

export const mockUser = {
  id: 1,
  name: "John Doe",
  email: "john.doe@example.com",
  role: "PATIENT",
  phone: "+94 77 123 4567",
  address: "123 Main Street, Colombo 05"
};

export const mockAdmin = {
  id: 1,
  name: "Admin User",
  email: "admin@lankamed.com",
  role: "ADMIN"
};

// Helper functions
export const getDoctorById = (id) => {
  return mockDoctors.find(doctor => doctor.id === id);
};

export const getAppointmentsByPatientId = (patientId) => {
  return mockAppointments.filter(appointment => appointment.patientId === patientId);
};

export const getAppointmentsByDoctorId = (doctorId) => {
  return mockAppointments.filter(appointment => appointment.doctorId === doctorId);
};

export const getReviewsByDoctorId = (doctorId) => {
  return mockReviews.filter(review => review.doctorId === doctorId);
};

export const getAvailableSlots = (doctorId, date) => {
  const doctor = getDoctorById(doctorId);
  if (!doctor) return [];
  
  return doctor.availableSlots.filter(slot => 
    slot.date === date && slot.available
  );
};

export const bookAppointment = (appointmentData) => {
  const newAppointment = {
    id: mockAppointments.length + 1,
    ...appointmentData,
    status: "PENDING",
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  
  mockAppointments.push(newAppointment);
  
  // Save to localStorage for persistence
  localStorage.setItem('lankamed_appointments', JSON.stringify(mockAppointments));
  
  return newAppointment;
};

export const updateAppointmentStatus = (appointmentId, newStatus) => {
  const appointment = mockAppointments.find(apt => apt.id === appointmentId);
  if (appointment) {
    appointment.status = newStatus;
    appointment.updatedAt = new Date().toISOString();
    
    // Save to localStorage for persistence
    localStorage.setItem('lankamed_appointments', JSON.stringify(mockAppointments));
    
    return appointment;
  }
  return null;
};

export const addReview = (reviewData) => {
  const newReview = {
    id: mockReviews.length + 1,
    ...reviewData,
    createdAt: new Date().toISOString()
  };
  
  mockReviews.push(newReview);
  
  // Save to localStorage for persistence
  localStorage.setItem('lankamed_reviews', JSON.stringify(mockReviews));
  
  return newReview;
};
