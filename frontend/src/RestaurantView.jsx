import React, { useState } from 'react';
import { api } from './api';
import { useCountdown, formatCountdown } from './useCountdown';

const PRESETS = [
  'about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate',
  '25 packs vegetable khichuri, no trays, hold until 11:00pm, front counter',
  '45 plates beef tehari and 3 trays borhani, hold until 11:45pm, back gate',
];

function Trust({ value }) {
  return <span className={`badge ${value >= 80 ? 'good' : value >= 60 ? 'mid' : 'warn'}`}>{value}% trust</span>;
}

function OfferedBox({ food, fetchedAt }) {
  const left = useCountdown(food.offerSecondsLeft, fetchedAt);
  return (
    <div className="status-box offered-box">
      <div>
        <strong>Offered to {food.assignedShelterName}</strong> <Trust value={food.assignedShelterReliability} />
        <div className="hint">
          Only this shelter can see it. If it does not answer in <b>{formatCountdown(left)}</b>, it goes to the next one.
        </div>
      </div>
    </div>
  );
}

function FoodCard({ food, fetchedAt, act }) {
  const [showRanking, setShowRanking] = useState(false);
  const eligibleCount = food.shelterMatches.filter((m) => m.eligible).length;

  return (
    <div className={`card food-card status-${food.status.toLowerCase()}`}>
      <div className="food-header">
        <div>
          <span className={`status-pill ${food.status.toLowerCase()}`}>{food.status.replace('_', ' ')}</span>
          <h3>{food.restaurantName}</h3>
        </div>
        <div className="deadline">
          Ready {food.readyAtText} · safe until {food.holdUntilText}
        </div>
      </div>

      <blockquote className="raw-quote">"{food.rawDescription}"</blockquote>

      <div className="parsed-tags">
        <span>~{food.estimatedPortions} portions</span>
        <span>{food.hasTrays ? 'Has trays (needs a van)' : 'No trays'}</span>
        <span>Pickup: {food.pickupNote}</span>
      </div>

      {food.status === 'OFFERED' && <OfferedBox food={food} fetchedAt={fetchedAt} />}

      {food.status === 'ACCEPTED' && (
        <div className="status-box accepted-box">
          <div>
            <strong>{food.assignedShelterName} is coming</strong> <Trust value={food.assignedShelterReliability} />
            <div className="hint">Expected around {food.etaText}. Pickup: {food.pickupNote}.</div>
          </div>
          <div className="action-row">
            <button className="btn success" onClick={() => act(() => api.confirmPickup(food.id), 'Collected. Their reliability went up.')}>
              Food collected
            </button>
            <button
              className="btn danger"
              onClick={() =>
                act(() => api.reportNoShow(food.id), 'No-show recorded. The food was offered to the next shelter.', 'warning')
              }
            >
              Did not show up
            </button>
          </div>
        </div>
      )}

      {food.status === 'PICKED_UP' && (
        <div className="status-box rescued-box">
          Collected by <strong>{food.assignedShelterName}</strong>. Nothing wasted.
        </div>
      )}

      {food.status === 'UNMATCHED' && (
        <div className="status-box unmatched-box">
          <div>
            <strong>No shelter can collect this in time.</strong>
            <div className="hint">You know now, not at midnight. Check the reasons below or try again.</div>
          </div>
          <button className="btn small" onClick={() => act(() => api.rematch(food.id), 'Matched again.')}>
            Try matching again
          </button>
        </div>
      )}

      <button className="link-btn" onClick={() => setShowRanking(!showRanking)}>
        {showRanking ? 'Hide' : 'Show'} shelter ranking ({eligibleCount} able to collect)
      </button>

      {showRanking && (
        <div className="match-list">
          {food.shelterMatches.map((m, idx) => (
            <div key={m.shelterId} className={`match-row ${m.eligible ? '' : 'ineligible'}`}>
              <div>
                <strong>{idx + 1}. {m.shelterName}</strong> <Trust value={m.reliabilityScore} />
                <div className="meta">
                  {m.transportType} · max {m.maxPortions} · collects before {m.latestPickupFormatted} · {m.travelMinutes} min away
                  {m.eligible ? ` · arrives ~${m.arriveBy}` : ''}
                </div>
                {m.warnings.length > 0 && <div className="warning-text">{m.warnings.join(' · ')}</div>}
              </div>
              <span className={`tag ${m.eligible ? 'ok' : 'no'}`}>{m.eligible ? 'Can collect' : 'Cannot'}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function RestaurantView({ foods, shelters, fetchedAt, act }) {
  const [restaurantName, setRestaurantName] = useState("Tanvir's Restaurant (Dhanmondi)");
  const [rawDescription, setRawDescription] = useState('');
  const [readyAt, setReadyAt] = useState('21:45');
  const [posting, setPosting] = useState(false);
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    if (!rawDescription.trim()) {
      setError('Write what is left, for example: 30 plates rice and dal, hold until 11:30pm');
      return;
    }
    setError('');
    setPosting(true);
    await act(
      async () => {
        await api.postFood({ restaurantName, rawDescription, readyAt });
        setRawDescription('');
      },
      'Posted. The best shelter has been offered the food.'
    );
    setPosting(false);
  };

  return (
    <div className="grid">
      <div className="left-col">
        <section className="card">
          <h2>Post leftover food</h2>
          <p className="hint">Type it the way you would say it. Portions, trays, time and pickup spot are read for you.</p>
          <form onSubmit={submit}>
            <label>What is left, and until when?</label>
            <textarea
              rows="3"
              placeholder="about 30 plates rice and dal, 2 trays chicken curry, can hold until 11:30pm, pickup from back gate"
              value={rawDescription}
              onChange={(e) => {
                setRawDescription(e.target.value);
                setError('');
              }}
            />
            <div className="two-col">
              <div>
                <label>Food ready from</label>
                <input type="time" value={readyAt} onChange={(e) => setReadyAt(e.target.value)} />
              </div>
              <div>
                <label>Restaurant</label>
                <input type="text" value={restaurantName} onChange={(e) => setRestaurantName(e.target.value)} />
              </div>
            </div>
            {error && <div className="field-error">{error}</div>}
            <button type="submit" className="btn primary" disabled={posting}>
              {posting ? 'Posting…' : 'Post and find a shelter'}
            </button>
          </form>

          <div className="presets">
            <small>Tap an example:</small>
            {PRESETS.map((p) => (
              <button key={p} type="button" className="preset-btn" onClick={() => setRawDescription(p)}>
                {p}
              </button>
            ))}
          </div>
        </section>

        <section className="card">
          <h2>Shelters</h2>
          <p className="hint">All different, and the matcher knows it.</p>
          {shelters.map((s) => (
            <div key={s.id} className="shelter-item">
              <div className="shelter-top">
                <strong>{s.name}</strong>
                <Trust value={s.reliabilityScore} />
              </div>
              <div className="meta">
                {s.area} · {s.transportType} · {s.canTakeTrays ? 'takes trays' : 'no trays'} · max {s.maxPortions} · before{' '}
                {s.latestPickupFormatted} · {s.travelMinutes} min away · no-shows {s.noShows}
              </div>
            </div>
          ))}
        </section>
      </div>

      <div className="right-col">
        <h2>Your batches</h2>
        {foods.length === 0 && <div className="card empty">Nothing posted yet. Tap an example and post it.</div>}
        {foods.map((f) => (
          <FoodCard key={f.id} food={f} fetchedAt={fetchedAt} act={act} />
        ))}
      </div>
    </div>
  );
}
