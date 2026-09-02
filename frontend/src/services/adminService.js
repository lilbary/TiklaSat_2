const API_URL = '/api/admin'; 

const getAuthHeader = () => {
    const token = localStorage.getItem('token');
    return { 
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    };
};

export const getDashboardStats = async () => {
    const response = await fetch(`${API_URL}/dashboard-stats`, { headers: getAuthHeader() });
    if (!response.ok) throw new Error('İstatistikler çekilemedi');
    return response.json();
};

export const getPendingAuctions = async () => {
    const response = await fetch(`${API_URL}/auctions/pending`, { headers: getAuthHeader() });
    if (!response.ok) throw new Error('Bekleyen ilanlar çekilemedi');
    return response.json();
};

export const approveAuction = async (id) => {
    const response = await fetch(`${API_URL}/auctions/${id}/approve`, { 
        method: 'POST', 
        headers: getAuthHeader() 
    });
    if (!response.ok) throw new Error('Onaylama başarısız');
    return response.text();
};

export const rejectAuction = async (id) => {
    const response = await fetch(`${API_URL}/auctions/${id}/reject`, { 
        method: 'POST', 
        headers: getAuthHeader() 
    });
    if (!response.ok) throw new Error('Reddetme başarısız');
    return response.text();
};
