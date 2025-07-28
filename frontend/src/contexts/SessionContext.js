import React, { createContext, useContext, useState, useEffect } from 'react';
import SessionService from '../service/SessionService';

const SessionContext = createContext();

export const useSession = () => {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
};

export const SessionProvider = ({ children }) => {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadSession();
  }, []);

  const loadSession = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await SessionService.getSessionInfo();
      setSession(response.data);
    } catch (err) {
      console.log('Session load failed:', err.response?.status, err.message);
      setError(err.message);
      setSession(null);
    } finally {
      setLoading(false);
    }
  };

  const updateSession = (updates) => {
    setSession(prev => prev ? { ...prev, ...updates } : null);
  };

  const clearSession = () => {
    setSession(null);
  };

  const isAuthenticated = () => {
    return session && session.loggedInUser;
  };

  const hasRole = (role) => {
    return session && session.loggedInUser && 
           ((role === 'ADMIN' && session.loggedInUser.isAdmin) || 
            (role === 'USER' && !session.loggedInUser.isAdmin));
  };

  const canAccessTimeReport = (userId) => {
    if (!session || !session.loggedInUser) return false;
    return session.loggedInUser.isAdmin || session.loggedInUser.userId === userId;
  };

  const value = {
    session,
    loading,
    error,
    loadSession,
    updateSession,
    clearSession,
    isAuthenticated,
    hasRole,
    canAccessTimeReport
  };

  return (
    <SessionContext.Provider value={value}>
      {children}
    </SessionContext.Provider>
  );
};

export default SessionContext;