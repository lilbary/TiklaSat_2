import axios from 'axios';

const API_URL = '/api/admin'; 

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return { headers: { Authorization: `Bearer ${token}` } };
};

export const getDashboardStats = async () => {
    const response = await axios.get(`${API_URL}/dashboard-stats`, getAuthHeader());
    return response.data;
};

export const getPendingAuctions = async () => {
    const response = await axios.get(`${API_URL}/auctions/pending`, getAuthHeader());
    return response.data;
};

export const approveAuction = async (id) => {
    const response = await axios.post(`${API_URL}/auctions/${id}/approve`, {}, getAuthHeader());
    return response.data;
};

export const rejectAuction = async (id) => {
    const response = await axios.post(`${API_URL}/auctions/${id}/reject`, {}, getAuthHeader());
    return response.data;
};
