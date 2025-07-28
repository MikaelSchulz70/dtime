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
    // Check if we just came from a successful login
    const urlParams = new URLSearchParams(window.location.search);
    const loginSuccess = urlParams.get('loginSuccess');

    if (loginSuccess) {
      // Remove the parameter from URL without refreshing
      window.history.replaceState({}, document.title, window.location.pathname);
      // Wait a bit longer for session to be established
      setTimeout(() => loadSession(), 1500);
    } else {
      loadSession();
    }
  }, []);

  const loadSession = async (retryCount = 0) => {
    try {
      if (retryCount === 0) {
        setLoading(true);
      }
      setError(null);
      const sessionService = new SessionService();
      const response = await sessionService.getSessionInfo();
      setSession(response.data);
      setLoading(false);
    } catch (err) {
      // If it's a 401/403 and we're just coming from login, retry once after a short delay
      if ((err.response?.status === 401 || err.response?.status === 403) && retryCount < 2) {
        setTimeout(() => loadSession(retryCount + 1), 1000);
        return;
      }

      setError(err.message);
      setSession(null);
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