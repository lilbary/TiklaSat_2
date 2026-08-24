import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'

const TABS = [
  { id: 'account', label: 'Account' },
  { id: 'addresses', label: 'Addresses' },
  { id: 'payment', label: 'Payment' },
  { id: 'emails', label: 'Emails & Notifications' },
  { id: 'verification', label: 'Verification' },
]

export default function SettingsPage() {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('account')
  const [userProfile, setUserProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem('token')
        if (!token) {
          setLoading(false)
          return
        }

        const res = await fetch('/api/users/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })

        if (res.ok) {
          const data = await res.json()
          setUserProfile(data)
        }
      } catch (error) {
        console.error('Failed to fetch user profile', error)
      } finally {
        setLoading(false)
      }
    }

    if (user) {
      fetchProfile()
    } else {
      setLoading(false)
    }
  }, [user])

  // Extract a readable name from userProfile or user.sub
  const displayName = userProfile?.fullName 
    ? userProfile.fullName.split(' ')[0] 
    : user?.sub 
      ? user.sub.split('@')[0] 
      : 'User'

  if (loading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8 bg-white min-h-[calc(100vh-73px)] flex justify-center items-center">
        <div className="text-slate-500">Loading...</div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8 bg-white min-h-[calc(100vh-73px)]">
      <h1 className="mb-10 text-4xl font-bold tracking-tight text-slate-900 font-serif">
        Hello {displayName.charAt(0).toUpperCase() + displayName.slice(1)}
      </h1>

      <div className="flex flex-col md:flex-row gap-12">
        {/* Sidebar */}
        <aside className="w-full md:w-64 shrink-0">
          <nav className="flex flex-col gap-1 relative">
            {/* Simple indicator line for active tab */}
            <div 
              className="absolute left-0 w-0.5 bg-blue-600 transition-all duration-300"
              style={{
                top: `${TABS.findIndex(t => t.id === activeTab) * 40}px`,
                height: '32px'
              }}
            />
            
            {TABS.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex h-10 items-center px-4 text-sm font-medium transition-colors ${
                  activeTab === tab.id
                    ? 'text-slate-900 font-semibold'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </nav>
        </aside>

        {/* Content */}
        <main className="flex-1 max-w-3xl">
          {activeTab === 'account' && (
            <div>
              <h2 className="mb-8 text-2xl font-semibold text-slate-900">Account</h2>

              <div className="space-y-8 divide-y divide-slate-200">
                {/* NAME */}
                <div className="pt-8 first:pt-0">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Name</h3>
                      <p className="text-base text-slate-900 font-medium">
                        {userProfile?.fullName || 'Not set'}
                      </p>
                      <button className="mt-2 text-sm font-medium text-blue-600 hover:text-blue-700">
                        Change
                      </button>
                    </div>
                  </div>
                </div>

                {/* USERNAME */}
                <div className="pt-8">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Username</h3>
                      <p className="text-base text-slate-900 font-medium">
                        {userProfile?.id ? `user-${userProfile.id.split('-')[0]}` : 'Not available'}
                      </p>
                    </div>
                    <div className="text-sm text-slate-500 max-w-xs text-right">
                      You can't edit your username.
                    </div>
                  </div>
                </div>

                {/* EMAIL */}
                <div className="pt-8">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Email</h3>
                      <p className="text-base text-slate-900 font-medium">
                        {userProfile?.email || user?.sub || 'Not set'}
                      </p>
                      <button className="mt-2 text-sm font-medium text-blue-600 hover:text-blue-700">
                        Change
                      </button>
                    </div>
                    <div className="text-sm text-slate-500 max-w-xs text-right">
                      You can choose which emails you get in your email and notification settings.
                    </div>
                  </div>
                </div>

                {/* PASSWORD */}
                <div className="pt-8">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Password</h3>
                      <p className="text-base text-slate-900 font-medium">********</p>
                      <button className="mt-2 text-sm font-medium text-blue-600 hover:text-blue-700">
                        Change
                      </button>
                    </div>
                  </div>
                </div>
                
                {/* PHONE NUMBER */}
                <div className="pt-8">
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500 mb-2">Phone Number</h3>
                      <p className="text-base text-slate-900 font-medium">
                        {userProfile?.phone || 'Not set'}
                      </p>
                      <button className="mt-2 text-sm font-medium text-blue-600 hover:text-blue-700">
                        Change
                      </button>
                    </div>
                  </div>
                </div>

              </div>
            </div>
          )}

          {activeTab !== 'account' && (
            <div>
              <h2 className="mb-8 text-2xl font-semibold text-slate-900">
                {TABS.find(t => t.id === activeTab)?.label}
              </h2>
              <p className="text-slate-500">This section is currently under construction.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}
