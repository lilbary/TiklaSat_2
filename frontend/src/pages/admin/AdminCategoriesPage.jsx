import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

const AdminCategoriesPage = () => {
    const [categories, setCategories] = useState([]);
    const [subCategories, setSubCategories] = useState({});
    const [expandedIds, setExpandedIds] = useState(new Set());
    const [loading, setLoading] = useState(true);

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
    const [formData, setFormData] = useState({ id: null, name: '', parentId: '', parentName: '' });

    // Fetch initial root categories
    useEffect(() => {
        fetchRootCategories();
    }, []);

    const fetchRootCategories = async () => {
        try {
            const res = await fetch('/api/categories');
            const data = await res.json();
            setCategories(data);
        } catch (error) {
            console.error('Kategoriler çekilemedi', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchSubCategories = async (parentId) => {
        try {
            const res = await fetch(`/api/categories/${parentId}/subcategories`);
            const data = await res.json();
            setSubCategories(prev => ({ ...prev, [parentId]: data }));
        } catch (error) {
            console.error('Alt kategoriler çekilemedi', error);
        }
    };

    const toggleExpand = (id) => {
        const newExpanded = new Set(expandedIds);
        if (newExpanded.has(id)) {
            newExpanded.delete(id);
        } else {
            newExpanded.add(id);
            if (!subCategories[id]) {
                fetchSubCategories(id);
            }
        }
        setExpandedIds(newExpanded);
    };

    const handleDelete = async (id, name) => {
        if (window.confirm(`"${name}" kategorisini silmek (veya pasife almak) istediğinize emin misiniz?\n(Aktif ilanı varsa sadece pasife alınır, yeni ilanlarda görünmez.)`)) {
            try {
                const token = localStorage.getItem('token');
                const res = await fetch(`/api/categories/${id}`, {
                    method: 'DELETE',
                    headers: { Authorization: `Bearer ${token}` }
                });
                
                if (res.ok) {
                    alert('Kategori başarıyla silindi/pasife alındı.');
                    window.location.reload(); 
                } else {
                    alert('Bir hata oluştu.');
                }
            } catch (error) {
                console.error(error);
                alert('Silme işlemi başarısız.');
            }
        }
    };

    const openAddModal = (parentId = '', parentName = '') => {
        setModalMode('add');
        setFormData({ id: null, name: '', parentId, parentName });
        setIsModalOpen(true);
    };

    const openEditModal = (category, parentId = '', parentName = '') => {
        setModalMode('edit');
        setFormData({ id: category.id, name: category.name, parentId, parentName });
        setIsModalOpen(true);
    };

    const handleFormSubmit = async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('token');
        
        const payload = {
            name: formData.name,
            parentId: formData.parentId || null
        };

        try {
            let res;
            if (modalMode === 'add') {
                res = await fetch('/api/categories', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                    body: JSON.stringify(payload)
                });
            } else {
                res = await fetch(`/api/categories/${formData.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                    body: JSON.stringify(payload)
                });
            }

            if (res.ok) {
                setIsModalOpen(false);
                alert(modalMode === 'add' ? 'Kategori eklendi!' : 'Kategori güncellendi!');
                window.location.reload();
            } else {
                alert('İşlem başarısız oldu.');
            }
        } catch (error) {
            console.error(error);
            alert('Sunucu hatası.');
        }
    };

    const renderCategoryRow = (category, depth = 0, parentId = '', parentName = '') => {
        const isExpanded = expandedIds.has(category.id);
        const children = subCategories[category.id] || [];

        return (
            <div key={category.id}>
                <div className={`flex items-center justify-between py-3 px-4 border-b border-gray-100 hover:bg-gray-50`} style={{ paddingLeft: `${depth * 2 + 1}rem` }}>
                    <div className="flex items-center gap-2">
                        <button 
                            onClick={() => toggleExpand(category.id)}
                            className="w-6 h-6 flex items-center justify-center text-gray-400 hover:text-red-600 bg-gray-100 rounded-full"
                        >
                            {isExpanded ? '-' : '+'}
                        </button>
                        <span className="font-medium text-gray-800">{category.name}</span>
                    </div>
                    
                    <div className="flex items-center gap-2">
                        <button onClick={() => openAddModal(category.id, category.name)} className="px-2 py-1 text-xs font-semibold text-blue-600 bg-blue-50 rounded hover:bg-blue-100">
                            Alt Kategori Ekle
                        </button>
                        <button onClick={() => openEditModal(category, parentId, parentName)} className="px-2 py-1 text-xs font-semibold text-amber-600 bg-amber-50 rounded hover:bg-amber-100">
                            Düzenle
                        </button>
                        <button onClick={() => handleDelete(category.id, category.name)} className="px-2 py-1 text-xs font-semibold text-red-600 bg-red-50 rounded hover:bg-red-100">
                            Sil
                        </button>
                    </div>
                </div>
                
                {isExpanded && children.length > 0 && (
                    <div className="bg-gray-50/50">
                        {children.map(child => renderCategoryRow(child, depth + 1, category.id, category.name))}
                    </div>
                )}
                {isExpanded && children.length === 0 && (
                    <div className="py-2 text-sm text-gray-400 italic" style={{ paddingLeft: `${(depth + 1) * 2 + 1}rem` }}>
                        Alt kategori yok.
                    </div>
                )}
            </div>
        );
    };

    if (loading) return <div className="p-10 text-center">Yükleniyor...</div>;

    return (
        <div className="p-8 bg-gray-50 min-h-screen">
            <div className="flex justify-between items-center mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-800">Kategori Yönetimi</h1>
                    <p className="text-sm text-gray-500 mt-1">Sistemdeki tüm kategorileri buradan yönetebilirsiniz.</p>
                </div>
                <div className="flex gap-3">
                    <Link to="/admin/dashboard" className="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50">
                        Dashboard'a Dön
                    </Link>
                    <button onClick={() => openAddModal('')} className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 shadow-sm">
                        + Yeni Ana Kategori
                    </button>
                </div>
            </div>

            <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                <div className="px-4 py-3 bg-gray-50 border-b border-gray-200 font-semibold text-gray-700">
                    Kategori Ağacı
                </div>
                <div className="divide-y divide-gray-100">
                    {categories.length === 0 ? (
                        <div className="p-8 text-center text-gray-500">Sistemde henüz kategori bulunmuyor.</div>
                    ) : (
                        categories.map(cat => renderCategoryRow(cat))
                    )}
                </div>
            </div>

            {/* MODAL */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
                        <div className="px-6 py-4 border-b border-gray-100">
                            <h3 className="text-lg font-bold text-gray-900">
                                {modalMode === 'add' ? 'Kategori Ekle' : 'Kategoriyi Düzenle'}
                            </h3>
                        </div>
                        <form onSubmit={handleFormSubmit} className="p-6 space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Kategori Adı</label>
                                <input 
                                    type="text" 
                                    required 
                                    value={formData.name} 
                                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                                    className="w-full rounded-lg border border-gray-300 px-4 py-2 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500"
                                    placeholder="Örn: Vasıta"
                                />
                            </div>
                            
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">Üst Kategori</label>
                                {formData.parentId ? (
                                    <div className="w-full rounded-lg border border-gray-200 px-4 py-2 bg-gray-50 text-gray-600 text-sm">
                                        <span className="font-semibold text-red-600">{formData.parentName}</span> kategorisinin altına eklenecek.
                                    </div>
                                ) : (
                                    <div className="w-full rounded-lg border border-gray-200 px-4 py-2 bg-gray-50 text-gray-600 text-sm italic">
                                        Bağımsız bir Ana Kategori olarak oluşturuluyor.
                                    </div>
                                )}
                            </div>

                            <div className="pt-4 flex gap-3">
                                <button type="button" onClick={() => setIsModalOpen(false)} className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200">
                                    İptal
                                </button>
                                <button type="submit" className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg font-medium hover:bg-red-700">
                                    Kaydet
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminCategoriesPage;
