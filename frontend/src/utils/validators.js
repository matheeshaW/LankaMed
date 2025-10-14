export const isNonEmpty = (value) => typeof value === 'string' && value.trim().length > 0;

export const minLength = (value, len) => typeof value === 'string' && value.trim().length >= len;

export const isPhone = (value) => {
  if (!value) return true; // optional
  const re = /^[0-9+\-()\s]{7,20}$/;
  return re.test(value);
};

export const isEmail = (value) => {
  if (!value) return false;
  const re = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
  return re.test(value);
};

export const isPastDate = (value) => {
  if (!value) return true; // optional
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return false;
  const today = new Date();
  // remove time
  d.setHours(0,0,0,0);
  today.setHours(0,0,0,0);
  return d <= today;
};

export const isValidGender = (value) => {
  if (!value) return false; // gender should be required for patients
  const allowed = ['MALE','FEMALE','OTHER'];
  return allowed.includes(value.toUpperCase());
};

export const validatePatientForm = (form) => {
  const errors = {};
  // firstName & lastName required, min 2
  errors.firstName = !isNonEmpty(form.firstName) ? 'First name is required' : (!minLength(form.firstName,2) ? 'First name must be at least 2 characters' : '');
  errors.lastName = !isNonEmpty(form.lastName) ? 'Last name is required' : (!minLength(form.lastName,2) ? 'Last name must be at least 2 characters' : '');
  errors.email = !isEmail(form.email) ? 'Enter a valid email' : '';
  errors.contactNumber = !isPhone(form.contactNumber) ? 'Enter a valid phone number' : '';
  errors.dateOfBirth = !isPastDate(form.dateOfBirth) ? 'Date of birth must be in the past' : '';
  errors.gender = !isValidGender(form.gender) ? 'Select a valid gender' : '';
  return errors;
};
