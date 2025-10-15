import React, { useEffect, useState } from 'react';
import api from '../../services/api';

const emptyForm = { fullName: '', relationship: '', phone: '', email: '', address: '' };

const EmergencyContactCard = () => {
    const [contacts, setContacts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [isAdding, setIsAdding] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [formData, setFormData] = useState(emptyForm);

    useEffect(() => { fetchContacts(); }, []);

    const fetchContacts = async () => {
        setLoading(true);
        try {
            const res = await api.get('/api/patients/me/emergency-contacts');
            setContacts(res.data || []);
        } catch (e) {
            console.error('Error loading emergency contacts', e);
        } finally {
            setLoading(false);
        }
    };

    const startAdd = () => {
        setIsAdding(true);
        setEditingId(null);
        setFormData(emptyForm);
    };

    const startEdit = (contact) => {
        setIsAdding(false);
        setEditingId(contact.emergencyContactId);
        setFormData({
            fullName: contact.fullName || '',
            relationship: contact.relationship || '',
            phone: contact.phone || '',
            email: contact.email || '',
            address: contact.address || ''
        });
    };

    const cancel = () => {
        setIsAdding(false);
        setEditingId(null);
        setFormData(emptyForm);
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            if (editingId) {
                const res = await api.put(`/api/patients/me/emergency-contacts/${editingId}`, formData);
                setContacts(contacts.map(c => c.emergencyContactId === editingId ? res.data : c));
            } else {
                const res = await api.post('/api/patients/me/emergency-contacts', formData);
                setContacts([...contacts, res.data]);
            }
            cancel();
        } catch (e) {
            console.error('Error saving emergency contact', e);
            alert('Failed to save emergency contact');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Delete this emergency contact?')) return;
        try {
            await api.delete(`/api/patients/me/emergency-contacts/${id}`);
            setContacts(contacts.filter(c => c.emergencyContactId !== id));
        } catch (e) {
            console.error('Error deleting emergency contact', e);
            alert('Failed to delete emergency contact');
        }
    };

    if (loading) {
        return (
            <div className="bg-white rounded-lg shadow-md p-6">
                <div className="animate-pulse h-6 bg-gray-200 rounded mb-4"></div>
                <div className="animate-pulse h-24 bg-gray-200 rounded"></div>
            </div>
        );
    }

    return (
        <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-800">Emergency Contact</h3>
                {!isAdding && editingId === null && (
                    <button onClick={startAdd} className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700">Add</button>
                )}
            </div>

            {(isAdding || editingId) ? (
                <form onSubmit={handleSubmit} className="space-y-3">
                    <div className="grid grid-cols-1 gap-3">
                        <div>
                            <label className="block text-xs text-gray-600 mb-1">Full Name *</label>
                            <input name="fullName" value={formData.fullName} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded" />
                        </div>
                        <div>
                            <label className="block text-xs text-gray-600 mb-1">Relationship *</label>
                            <input name="relationship" value={formData.relationship} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded" />
                        </div>
                        <div>
                            <label className="block text-xs text-gray-600 mb-1">Phone *</label>
                            <input name="phone" value={formData.phone} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded" />
                        </div>
                        <div>
                            <label className="block text-xs text-gray-600 mb-1">Email</label>
                            <input type="email" name="email" value={formData.email} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded" />
                        </div>
                        <div>
                            <label className="block text-xs text-gray-600 mb-1">Address</label>
                            <textarea name="address" rows={2} value={formData.address} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded" />
                        </div>
                    </div>
                    <div className="flex justify-end space-x-2">
                        <button type="button" onClick={cancel} className="px-3 py-1 border border-gray-300 rounded">Cancel</button>
                        <button type="submit" disabled={saving} className="px-3 py-1 bg-blue-600 text-white rounded disabled:opacity-50">{saving ? 'Saving...' : (editingId ? 'Update' : 'Save')}</button>
                    </div>
                </form>
            ) : (
                <div className="space-y-3">
                    {contacts.length === 0 ? (
                        <p className="text-sm text-gray-500">No emergency contacts added yet.</p>
                    ) : contacts.map(contact => (
                        <div key={contact.emergencyContactId} className="border border-gray-200 rounded p-4">
                            <div className="flex justify-between">
                                <div>
                                    <p className="text-sm text-gray-800 font-medium">{contact.fullName} <span className="text-gray-500 font-normal">({contact.relationship})</span></p>
                                    <p className="text-xs text-gray-600">Phone: {contact.phone}</p>
                                    {contact.email && <p className="text-xs text-gray-600">Email: {contact.email}</p>}
                                    {contact.address && <p className="text-xs text-gray-600">Address: {contact.address}</p>}
                                </div>
                                <div className="flex items-start space-x-2">
                                    <button onClick={() => startEdit(contact)} className="text-blue-600 text-xs hover:text-blue-800">Edit</button>
                                    <button onClick={() => handleDelete(contact.emergencyContactId)} className="text-red-600 text-xs hover:text-red-800">Delete</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default EmergencyContactCard;


