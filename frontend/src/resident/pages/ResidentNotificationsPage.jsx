import React, { useState, useEffect } from 'react';
import API from '../../shared/services/api';
import LoadingSpinner from '../../shared/components/LoadingSpinner';

export default function ResidentNotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = () => {
    API.get('/resident/notifications')
      .then(res => setNotifications(res.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkRead = async (id) => {
    try {
      await API.put(`/resident/notifications/${id}/read`);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <LoadingSpinner text="Fetching notifications..." />;

  return (
    <div className="container-fluid py-4">
      <div className="mb-4">
        <h2 className="fw-bold mb-1 text-dark">Notifications Inbox</h2>
        <p className="text-muted small mb-0">Stay informed with water bill alerts and payment confirmations</p>
      </div>

      <div className="row g-3">
        {notifications.length ? (
          notifications.map(n => (
            <div className="col-12" key={n.notificationId}>
              <div className={`card glass-card border p-3 shadow-sm bg-white ${!n.isRead ? 'border-start border-dark border-4' : ''}`}>
                <div className="card-body d-flex align-items-start justify-content-between">
                  <div className="d-flex align-items-start gap-3">
                    <div className={`rounded-3 p-2.5 fs-4 ${!n.isRead ? 'bg-light text-dark' : 'bg-light text-muted'}`}>
                      <i className="bi bi-bell-fill"></i>
                    </div>
                    <div>
                      <h6 className="fw-bold mb-1 text-dark">{n.title} {!n.isRead && <span className="badge bg-dark text-white ms-2">New</span>}</h6>
                      <p className="text-muted mb-1 fs-7">{n.message}</p>
                      <small className="text-secondary fs-8">{n.createdAt ? n.createdAt.replace('T', ' ').substring(0, 16) : 'N/A'}</small>
                    </div>
                  </div>

                  {!n.isRead && (
                    <button className="btn btn-sm btn-outline-dark fw-bold" onClick={() => handleMarkRead(n.notificationId)}>
                      Mark Read
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="col-12">
            <div className="card glass-card border p-5 text-center bg-white">
              <i className="bi bi-bell-slash text-muted display-4 d-block mb-3"></i>
              <h5 className="fw-bold text-dark">No Notifications</h5>
              <p className="text-muted mb-0">Your notification inbox is clean.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
