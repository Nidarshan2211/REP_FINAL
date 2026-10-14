import React, { createContext, useState, useEffect } from 'react';
import API from '../services/api';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const token = localStorage.getItem('jwtToken');
    if (storedUser && token) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password, selectedRole) => {
    const res = await API.post('/auth/login', { email, password, selectedRole });
    const { token, userId, name, email: userEmail, role, status } = res.data;
    const userData = { userId, name, email: userEmail, role, status };
    
    localStorage.setItem('jwtToken', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const registerOwner = async (data) => {
    const res = await API.post('/auth/register-owner', data);
    return res.data;
  };

  const registerResident = async (data) => {
    const res = await API.post('/auth/register-resident', data);
    const { token, userId, name, email: userEmail, role, status } = res.data;
    const userData = { userId, name, email: userEmail, role, status };
    if (token) {
      localStorage.setItem('jwtToken', token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
    }
    return res.data;
  };

  const acceptInvitation = async (payload) => {
    const body = typeof payload === 'object' ? payload : { token: payload };
    const res = await API.post('/auth/accept-invitation', body);
    return res.data;
  };

  const logout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, registerOwner, registerResident, acceptInvitation, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
