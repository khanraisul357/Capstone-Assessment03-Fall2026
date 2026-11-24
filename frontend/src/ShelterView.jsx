import React, { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import { useCountdown, formatCountdown } from './useCountdown';

function OfferCard({ offer, shelterId, fetchedAt, act, reload }) {
  const left = useCountdown(offer.offerSecondsLeft, fetchedAt);
  const run = async (fn, text, type) => {
    await act(fn, text, type);
    await reload();
  };

  return (
    <div className={`card food-card status-${offer.status.toLowerCase()}`}>
      <div className="food-header">
        <div>
          <span className={`status-pill ${offer.status.toLowerCase()}`}>
            {offer.status === 'OFFERED' ? 'New offer' : 'You accepted'}
          </span>
          <h3>{offer.restaurantName}</h3>
        </div>
        <div className="deadline">Safe until {offer.holdUntilText}</div>
      </div>

      <div className="parsed-tags">
        <span>~{offer.estimatedPortions} portions</span>
        <span>{offer.hasTrays ? 'Has trays' : 'No trays'}</span>
        <span>Ready from {offer.readyAtText}</span>
        <span>Pickup: {offer.pickupNote}</span>
      </div>

      {offer.status === 'OFFERED' && (
        <div className="status-box offered-box">
          <div>
            <strong>Can you collect this?</strong>
            <div className="hint">
              Answer within <b>{formatCountdown(left)}</b>. If you say yes, nobody else is sent for it, so please only
              accept if you will really come.
            </div>
          </div>
          <div className="action-row">
            <button
              className="btn success"
              onClick={() => run(() => api.acceptOffer(offer.id, shelterId), 'Accepted. The restaurant knows you are coming.')}
            >
              Accept
            </button>
            <button
              className="btn danger"
              onClick={() => run(() => api.declineOffer(offer.id, shelterId), 'Declined. It was offered to the next shelter.', 'warning')}
            >
              Decline
            </button>
          </div>
        </div>
      )}

      {offer.status === 'ACCEPTED' && (
        <div className="status-box accepted-box">
          <div>
            <strong>Go to: {offer.pickupNote}</strong>
            <div className="hint">Expected arrival around {offer.etaText}. The restaurant will confirm when you collect.</div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function ShelterView({ shelters, fetchedAt, act }) {
  const [shelterId, setShelterId] = useState('');
  const [offers, setOffers] = useState([]);
  const [offersFetchedAt, setOffersFetchedAt] = useState(Date.now());

  useEffect(() => {
    if (!shelterId && shelters.length > 0) setShelterId(String(shelters[0].id));
  }, [shelters, shelterId]);

  const reload = useCallback(async () => {
    if (!shelterId) return;
    try {
      setOffers(await api.getOffers(shelterId));
      setOffersFetchedAt(Date.now());
    } catch (_) {
      /* the main banner already reports connection problems */
    }
  }, [shelterId]);

  // refresh when the shelter changes and whenever the shared board refreshes
  useEffect(() => {
    reload();
  }, [reload, fetchedAt]);

  const shelter = shelters.find((s) => String(s.id) === shelterId);

  return (
    <div className="shelter-view">
      <section className="card">
        <h2>Shelter inbox</h2>
        <p className="hint">Pick which shelter you are acting as. You only see food offered to you.</p>
        <label>Acting as</label>
        <select value={shelterId} onChange={(e) => setShelterId(e.target.value)}>
          {shelters.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
        {shelter && (
          <div className="meta">
            {shelter.area} · {shelter.transportType} · {shelter.canTakeTrays ? 'takes trays' : 'no trays'} · max{' '}
            {shelter.maxPortions} · collects before {shelter.latestPickupFormatted} · {shelter.travelMinutes} min away ·
            trust {shelter.reliabilityScore}% · no-shows {shelter.noShows}
          </div>
        )}
      </section>

      {offers.length === 0 && (
        <div className="card empty">No food offered to this shelter right now. New offers appear here by themselves.</div>
      )}
      {offers.map((o) => (
        <OfferCard key={o.id} offer={o} shelterId={shelterId} fetchedAt={offersFetchedAt} act={act} reload={reload} />
      ))}
    </div>
  );
}
