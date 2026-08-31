import React, { useEffect, useState } from 'react';
import { getDashboardStats } from '../../services/adminService';

const AdminDashboardPage = () => {
    const [stats, setStats] = useState({
        totalSales: 0,
        totalUsers: 0,
        activeAuctions: 0,
        dailyBids: 0
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await getDashboardStats();
                setStats(data);
            } catch (error) {
                console.error("İstatistikler çekilemedi", error);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (loading) return <div className="p-10 text-center">Yükleniyor...</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <h1 className="text-2xl font-bold mb-6 text-gray-800">Admin Dashboard</h1>
            
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                    <h3 className="text-gray-500 text-sm font-medium">Toplam Kullanıcı</h3>
                    <p className="text-3xl font-bold text-gray-800 mt-2">{stats.totalUsers}</p>
                </div>
                
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                    <h3 className="text-gray-500 text-sm font-medium">Aktif İlanlar</h3>
                    <p className="text-3xl font-bold text-gray-800 mt-2">{stats.activeAuctions}</p>
                </div>
                
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                    <h3 className="text-gray-500 text-sm font-medium">Bugünkü Teklifler</h3>
                    <p className="text-3xl font-bold text-gray-800 mt-2">{stats.dailyBids}</p>
                </div>
                
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                    <h3 className="text-gray-500 text-sm font-medium">Toplam Hacim (Tahmini)</h3>
                    <p className="text-3xl font-bold text-green-600 mt-2">₺{stats.totalSales}</p>
                </div>
            </div>
            
            <div className="mt-8 bg-white p-6 rounded-lg shadow-sm border border-gray-100 h-64 flex items-center justify-center text-gray-400">
                Grafik Alanı (İleride Recharts eklenebilir)
            </div>
        </div>
    );
};

export default AdminDashboardPage;
