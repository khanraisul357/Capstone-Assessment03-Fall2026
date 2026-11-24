import { useEffect, useState } from 'react';

/** Seconds left, counted down locally from the value the server sent at `fetchedAt`. */
export function useCountdown(secondsLeft, fetchedAt) {
  const [now, setNow] = useState(Date.now());

  useEffect(() => {
    const t = setInterval(() => setNow(Date.now()), 1000);
    return () => clearInterval(t);
  }, []);

  if (secondsLeft == null) return null;
  return Math.max(0, secondsLeft - Math.floor((now - fetchedAt) / 1000));
}

export function formatCountdown(sec) {
  if (sec == null) return '';
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}
