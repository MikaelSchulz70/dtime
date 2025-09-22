import React, { createContext, useContext, useState } from 'react';
import SessionService from '../service/SessionService';

const SessionContext = createContext();

// Global state to prevent multiple session loads
let sessionLoadPromise = null;
let sessionState = {
  session: null,
  loading: true,
  error: null
};

export const useSession = () => {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
};

export const SessionProvider = ({ children }) => {
  const [session, setSession] = useState(sessionState.session);
  const [loading, setLoading] = useState(sessionState.loading);
  const [error, setError] = useState(sessionState.error);

  // Load session function that updates global state
  const loadSession = async () => {
    if (sessionLoadPromise) {
      return sessionLoadPromise;
    }

    sessionLoadPromise = (async () => {
      try {
        console.log('Loading session...');
        sessionState.loading = true;
        sessionState.error = null;
        setLoading(true);
        setError(null);

        const sessionService = new SessionService();
        const response = await sessionService.getSessionInfo();
        
        sessionState.session = response.data;
        sessionState.loading = false;
        setSession(response.data);
        setLoading(false);
        console.log('Session loaded:', response.data);
      } catch (err) {
        console.log('Session load error:', err.response?.status, err.message);
        sessionState.error = err.message;
        sessionState.session = null;
        sessionState.loading = false;
        setError(err.message);
        setSession(null);
        setLoading(false);
      }
    })();

    return sessionLoadPromise;
  };

  // Initialize session on first render if not already done
  React.useEffect(() => {
    if (!sessionLoadPromise) {
      // Check for login success parameter
      const urlParams = new URLSearchParams(window.location.search);
      const loginSuccess = urlParams.get('loginSuccess');
      
      if (loginSuccess) {
        // Remove the parameter from URL
        const newUrl = window.location.pathname;
        window.history.replaceState({}, '', newUrl);
        // Wait for login to complete
        setTimeout(() => loadSession(), 1500);
      } else {
        loadSession();
      }
    } else {
      // Use existing session state
      setSession(sessionState.session);
      setLoading(sessionState.loading);
      setError(sessionState.error);
    }
  }, []); // Empty dependency - runs once per component mount

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