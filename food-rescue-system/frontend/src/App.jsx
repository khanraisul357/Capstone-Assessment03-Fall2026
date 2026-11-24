import React, { useEffect, useState } from 'react';
import { api } from './api';

const QUICK_PRESETS = [
  'about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate',
  '25 packs vegetable khichuri, no trays, hold until 11:00pm, front counter',
  '45 plates beef tehari and 3 trays borhani, hold until 11:45pm, back gate',
];

export default function App() {
  const [foods, setFoods] = useState([]);
  const [shelters, setShelters] = useState([]);
  const [restaurantName, setRestaurantName] = useState("Tanvir's Restaurant (Dhanmondi)");
  const [rawDescription, setRawDescription] = useState('');
  const [activeShelterId, setActiveShelterId] = useState('');
  const [banner, setBanner] = useState(null);
  const [loading, setLoading] = useState(false);

  const loadAll = async () => {
    try {
      const [foodData, shelterData] = await Promise.all([api.getFoods(), api.getShelters()]);
      setFoods(foodData);
      setShelters(shelterData);
      if (!activeShelterId && shelterData.length > 0) {
        setActiveShelterId(String(shelterData[0].id));
      }
    } catch (err) {
      setBanner({ type: 'error', text: 'Backend not reachable on port 8080: ' + err.message });
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handlePostFood = async (e) => {
    e.preventDefault();
    if (!rawDescription.trim()) return;
    setLoading(true);
    setBanner(null);
    try {
      await api.postFood({ restaurantName, rawDescription });
      setRawDescription('');
      setBanner({ type: 'success', text: 'Surplus food posted & matched with compatible shelters in seconds!' });
      await loadAll();
    } catch (err) {
      setBanner({ type: 'error', text: err.message });
    } finally {
      setLoading(false);
    }
  };

  const handleClaim = async (foodId, shelterId) => {
    setBanner(null);
    try {
      await api.claimFood(foodId, shelterId);
      setBanner({ type: 'success', text: 'Claim locked! Other shelters are blocked from making a wasted trip.' });
      await loadAll();
    } catch (err) {
      setBanner({ type: 'error', text: err.message });
      await loadAll();
    }
  };

  const handlePickup = async (foodId) => {
    setBanner(null);
    try {
      await api.confirmPickup(foodId);
      setBanner({ type: 'success', text: 'Pickup confirmed! Shelter reliability score increased.' });
      await loadAll();
    } catch (err) {
      setBanner({ type: 'error', text: err.message });
    }
  };

  const handleNoShow = async (foodId) => {
    setBanner(null);
    try {
      await api.reportNoShow(foodId);
      setBanner({
        type: 'warning',
        text: 'No-show recorded! Shelter trust penalized & food re-opened immediately for backup pickup.',
      });
      await loadAll();
    } catch (err) {
      setBanner({ type: 'error', text: err.message });
    }
  };

  const openCount = foods.filter((f) => f.status === 'OPEN').length;
  const claimedCount = foods.filter((f) => f.status === 'CLAIMED').length;
  const rescuedCount = foods.filter((f) => f.status === 'PICKED_UP').length;

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>Closing-Time Food Rescue</h1>
          <p className="subtitle">
            15-second posting for Tanvir • Exclusive single-shelter lock • Trays, cutoff &amp; reliability aware
          </p>
        </div>
        <div className="stats">
          <div className="stat-box open">
            <span>{openCount}</span>
            <small>Open Now</small>
          </div>
          <div className="stat-box claimed">
            <span>{claimedCount}</span>
            <small>En Route</small>
          </div>
          <div className="stat-box rescued">
            <span>{rescuedCount}</span>
            <small>Rescued</small>
          </div>
        </div>
      </header>

      {banner && <div className={`banner ${banner.type}`}>{banner.text}</div>}

      <div className="grid">
        {/* Left Column: 15-Second Restaurant Post + Shelter Reliability Board */}
        <div className="left-col">
          <section className="card">
            <h2>1. Quick Post (No Phone Calls)</h2>
            <p className="hint">Type naturally just like Tanvir writes—units, trays &amp; cutoff auto-parsed.</p>
            <form onSubmit={handlePostFood}>
              <label>Restaurant</label>
              <input
                type="text"
                value={restaurantName}
                onChange={(e) => setRestaurantName(e.target.value)}
              />
              <label>What is left &amp; until when?</label>
              <textarea
                rows="3"
                placeholder='e.g. "about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate"'
                value={rawDescription}
                onChange={(e) => setRawDescription(e.target.value)}
              />
              <button type="submit" className="btn primary" disabled={loading}>
                {loading ? 'Posting...' : 'Post Surplus Food'}
              </button>
            </form>

            <div className="presets">
              <small>One-click demo presets:</small>
              {QUICK_PRESETS.map((p, idx) => (
                <button
                  key={idx}
                  type="button"
                  className="preset-btn"
                  onClick={() => setRawDescription(p)}
                >
                  {p}
                </button>
              ))}
            </div>
          </section>

          <section className="card">
            <h2>Shelter Profiles &amp; Reliability</h2>
            <p className="hint">Simulate acting as a shelter or inspect their track record:</p>
            <label>Simulate Claiming As Shelter:</label>
            <select
              value={activeShelterId}
              onChange={(e) => setActiveShelterId(e.target.value)}
            >
              {shelters.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} ({s.transportType}, max {s.maxPortions}, trust {s.reliabilityScore}%)
                </option>
              ))}
            </select>

            <div className="shelter-list">
              {shelters.map((s) => (
                <div key={s.id} className="shelter-item">
                  <div className="shelter-top">
                    <strong>{s.name}</strong>
                    <span className={`badge ${s.reliabilityScore >= 80 ? 'good' : 'warn'}`}>
                      {s.reliabilityScore}% Trust
                    </span>
                  </div>
                  <div className="shelter-meta">
                    {s.transportType} • {s.canTakeTrays ? 'Takes Trays' : 'No Trays'} • Max {s.maxPortions} portions • Cutoff {s.latestPickupFormatted} • No-shows: {s.noShows}
                  </div>
                </div>
              ))}
            </div>
          </section>
        </div>

        {/* Right Column: Live Surplus Feed & Conflict-Free Claiming */}
        <div className="right-col">
          <h2>2. Live Surplus Batches &amp; Smart Shelter Matching</h2>
          {foods.map((food) => (
            <div key={food.id} className={`card food-card status-${food.status.toLowerCase()}`}>
              <div className="food-header">
                <div>
                  <span className={`status-pill ${food.status.toLowerCase()}`}>{food.status}</span>
                  <h3>{food.restaurantName}</h3>
                </div>
                <div className="deadline">Hold until {food.holdUntilText}</div>
              </div>

              <blockquote className="raw-quote">"{food.rawDescription}"</blockquote>

              <div className="parsed-tags">
                <span>~{food.estimatedPortions} portions</span>
                <span>{food.hasTrays ? 'Contains Catering Trays (Van Needed)' : 'Packed / Rickshaw OK'}</span>
                <span>Pickup: {food.pickupNote}</span>
              </div>

              {food.status === 'OPEN' && (
                <div className="match-section">
                  <div className="quick-claim-bar">
                    <button
                      className="btn primary"
                      onClick={() => handleClaim(food.id, activeShelterId)}
                    >
                      Claim as Selected Shelter
                    </button>
                  </div>

                  <h4>Eligible &amp; Ranked Shelters (Best Fit First):</h4>
                  <div className="match-list">
                    {food.shelterMatches.map((m) => (
                      <div key={m.shelterId} className={`match-row ${m.eligible ? '' : 'ineligible'}`}>
                        <div>
                          <strong>{m.shelterName}</strong>{' '}
                          <span className={`badge ${m.reliabilityScore >= 80 ? 'good' : 'warn'}`}>
                            {m.reliabilityScore}% Trust
                          </span>
                          {m.warnings.length > 0 && (
                            <div className="warning-text">{m.warnings.join(' • ')}</div>
                          )}
                        </div>
                        <button
                          className="btn small"
                          disabled={!m.eligible}
                          onClick={() => handleClaim(food.id, m.shelterId)}
                        >
                          {m.eligible ? 'Assign / Claim' : 'Incompatible'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {food.status === 'CLAIMED' && (
                <div className="claimed-box">
                  <div>
                    <strong>Locked for: {food.claimedByShelterName}</strong> ({food.claimedByReliability}% trust)
                    <div className="hint">Other shelters are blocked so nobody wastes a 30-minute trip.</div>
                  </div>
                  <div className="action-row">
                    <button className="btn success" onClick={() => handlePickup(food.id)}>
                      Confirm Picked Up
                    </button>
                    <button className="btn danger" onClick={() => handleNoShow(food.id)}>
                      Report No-Show &amp; Re-open
                    </button>
                  </div>
                </div>
              )}

              {food.status === 'PICKED_UP' && (
                <div className="rescued-box">
                  Collected by <strong>{food.claimedByShelterName}</strong> — Zero food wasted!
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
