import React, { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import RestaurantView from './RestaurantView.jsx';
import ShelterView from './ShelterView.jsx';

export default function App() {
  const [view, setView] = useState('restaurant'); // 'restaurant' | 'shelter'
  const [foods, setFoods] = useState([]);
  const [shelters, setShelters] = useState([]);
  const [fetchedAt, setFetchedAt] = useState(Date.now());
  const [banner, setBanner] = useState(null);

  const loadAll = useCallback(async () => {
    try {
      const [foodData, shelterData] = await Promise.all([api.getFoods(), api.getShelters()]);
      setFoods(foodData);
      setShelters(shelterData);
      setFetchedAt(Date.now());
    } catch (err) {
      setBanner({ type: 'error', text: 'Cannot reach the backend on port 8080. Is it running? (' + err.message + ')' });
    }
  }, []);

  // Live board: shelters see new offers and the restaurant sees answers without refreshing.
  useEffect(() => {
    loadAll();
    const t = setInterval(loadAll, 4000);
    return () => clearInterval(t);
  }, [loadAll]);

  /** Runs one API action, shows a banner, then refreshes everything. */
  const act = useCallback(
    async (fn, successText, type = 'success') => {
      setBanner(null);
      try {
        await fn();
        if (successText) setBanner({ type, text: successText });
      } catch (err) {
        setBanner({ type: 'error', text: err.message });
      }
      await loadAll();
    },
    [loadAll]
  );

  const count = (s) => foods.filter((f) => f.status === s).length;

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Closing-Time Food Rescue</h1>
          <p className="subtitle">
            Post in seconds. The system offers the food to one shelter that can really collect it in time.
          </p>
        </div>
        <div className="stats">
          <div className="stat-box offered"><span>{count('OFFERED')}</span><small>Offered</small></div>
          <div className="stat-box accepted"><span>{count('ACCEPTED')}</span><small>On the way</small></div>
          <div className="stat-box rescued"><span>{count('PICKED_UP')}</span><small>Rescued</small></div>
          <div className="stat-box unmatched"><span>{count('UNMATCHED')}</span><small>Unmatched</small></div>
        </div>
      </header>

      <nav className="tabs">
        <button
          className={view === 'restaurant' ? 'tab active' : 'tab'}
          onClick={() => {
            setView('restaurant');
            setBanner(null);
          }}
        >
          Restaurant (Tanvir)
        </button>
        <button
          className={view === 'shelter' ? 'tab active' : 'tab'}
          onClick={() => {
            setView('shelter');
            setBanner(null);
          }}
        >
          Shelter inbox
        </button>
      </nav>

      {banner && (
        <div className={`banner ${banner.type}`} onClick={() => setBanner(null)}>
          {banner.text}
        </div>
      )}

      {view === 'restaurant' ? (
        <RestaurantView foods={foods} shelters={shelters} fetchedAt={fetchedAt} act={act} />
      ) : (
        <ShelterView shelters={shelters} fetchedAt={fetchedAt} act={act} />
      )}
    </div>
  );
}
