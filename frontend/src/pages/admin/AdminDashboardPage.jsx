import React, { useEffect, useState } from 'react';
import { getDashboardStats, getPendingAuctions, approveAuction, rejectAuction } from '../../services/adminService';
import { Link } from 'react-router-dom';

const AdminDashboardPage = () => {
    const [stats, setStats] = useState({
        totalSales: 0,
        totalUsers: 0,
        activeAuctions: 0,
        dailyBids: 0
    });
    const [pendingAuctions, setPendingAuctions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [statsData, pendingData] = await Promise.all([
                    getDashboardStats(),
                    getPendingAuctions()
                ]);
                setStats(statsData);
                setPendingAuctions(pendingData);
            } catch (error) {
                console.error("Veriler çekilemedi", error);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const handleApprove = async (id) => {
        if(window.confirm("Bu ilanı onaylamak istediğinize emin misiniz?")) {
            try {
                await approveAuction(id);
                setPendingAuctions(pendingAuctions.filter(a => a.id !== id));
                // İstatistikleri güncelle (Aktif ilan sayısı +1)
                setStats(prev => ({...prev, activeAuctions: prev.activeAuctions + 1}));
            } catch (error) {
                alert("Onaylama sırasında hata oluştu!");
            }
        }
    };

    const handleReject = async (id) => {
        if(window.confirm("Bu ilanı reddetmek istediğinize emin misiniz?")) {
            try {
                await rejectAuction(id);
                setPendingAuctions(pendingAuctions.filter(a => a.id !== id));
            } catch (error) {
                alert("Reddetme sırasında hata oluştu!");
            }
        }
    };

    if (loading) return <div className="p-10 text-center">Yükleniyor...</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
                <Link to="/admin/kategoriler" className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 shadow-sm">
                    Kategori Yönetimi
                </Link>
            </div>
            
            {/* 1. ÖZET KARTLARI */}
            <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
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
            
            {/* 2. ONAY BEKLEYEN İLANLAR (MODERASYON) */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
                    <h2 className="text-lg font-bold text-gray-800">Onay Bekleyen İlanlar ({pendingAuctions.length})</h2>
                </div>
                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-white">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">İlan Adı</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Başlangıç Fiyatı</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tarih</th>
                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">İşlemler</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {pendingAuctions.length === 0 ? (
                                <tr>
                                    <td colSpan="4" className="px-6 py-8 text-center text-gray-500">
                                        <svg className="mx-auto h-12 w-12 text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                        Şu an onay bekleyen hiçbir ilan yok. Harika iş!
                                    </td>
                                </tr>
                            ) : (
                                pendingAuctions.map((auction) => (
                                    <tr key={auction.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                            {/* Admin, ilanın detayına gidip bakabilir */}
                                            <Link to={`/artirma/${auction.id}`} className="text-blue-600 hover:text-blue-800 hover:underline" target="_blank">
                                                {auction.listingTitle}
                                            </Link>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-semibold">
                                            ₺{auction.startingPrice.toLocaleString('tr-TR')}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                            {new Date(auction.startTime).toLocaleDateString('tr-TR')}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            <button 
                                                onClick={() => handleApprove(auction.id)}
                                                className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-green-700 bg-green-100 hover:bg-green-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 mr-2 transition-colors"
                                            >
                                                Onayla
                                            </button>
                                            <button 
                                                onClick={() => handleReject(auction.id)}
                                                className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded text-red-700 bg-red-100 hover:bg-red-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-colors"
                                            >
                                                Reddet
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboardPage;
