import React, { useEffect, useState } from 'react';
import { getPendingAuctions, approveAuction, rejectAuction } from '../../services/adminService';
import { Link } from 'react-router-dom';

const AdminModerationPage = () => {
    const [auctions, setAuctions] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchAuctions = async () => {
        try {
            const data = await getPendingAuctions();
            setAuctions(data);
        } catch (error) {
            console.error("Bekleyen ilanlar çekilemedi", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAuctions();
    }, []);

    const handleApprove = async (id) => {
        if(window.confirm("Bu ilanı onaylamak istediğinize emin misiniz?")) {
            try {
                await approveAuction(id);
                setAuctions(auctions.filter(a => a.id !== id));
            } catch (error) {
                alert("Onaylama sırasında hata oluştu!");
            }
        }
    };

    const handleReject = async (id) => {
        if(window.confirm("Bu ilanı reddetmek istediğinize emin misiniz?")) {
            try {
                await rejectAuction(id);
                setAuctions(auctions.filter(a => a.id !== id));
            } catch (error) {
                alert("Reddetme sırasında hata oluştu!");
            }
        }
    };

    if (loading) return <div className="p-10 text-center">Yükleniyor...</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <h1 className="text-2xl font-bold mb-6 text-gray-800">İlan Moderasyonu</h1>
            
            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">İlan Adı</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fiyat</th>
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">İşlemler</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {auctions.length === 0 ? (
                            <tr><td colSpan="3" className="px-6 py-4 text-center text-gray-500">Onay bekleyen ilan yok.</td></tr>
                        ) : (
                            auctions.map((auction) => (
                                <tr key={auction.id}>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                        <Link to={`/artirma/${auction.id}`} className="text-blue-600 hover:underline" target="_blank">
                                            {auction.listingTitle}
                                        </Link>
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">₺{auction.startingPrice}</td>
                                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                        <button 
                                            onClick={() => handleApprove(auction.id)}
                                            className="text-green-600 hover:text-green-900 bg-green-50 px-3 py-1 rounded mr-2"
                                        >
                                            Onayla
                                        </button>
                                        <button 
                                            onClick={() => handleReject(auction.id)}
                                            className="text-red-600 hover:text-red-900 bg-red-50 px-3 py-1 rounded"
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
    );
};

export default AdminModerationPage;
