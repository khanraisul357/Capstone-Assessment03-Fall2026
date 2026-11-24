const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

async function handle(res) {
  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      message = data.message || message;
    } catch (_) {
      /* no JSON body */
    }
    throw new Error(message);
  }
  return res.json();
}

const get = (path) => fetch(`${BASE_URL}${path}`).then(handle);

const post = (path, body) =>
  fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  }).then(handle);

export const api = {
  getFoods: () => get('/foods'),
  getShelters: () => get('/shelters'),
  getOffers: (shelterId) => get(`/shelters/${shelterId}/offers`),

  postFood: (payload) => post('/foods', payload),
  confirmPickup: (foodId) => post(`/foods/${foodId}/pickup`),
  reportNoShow: (foodId) => post(`/foods/${foodId}/noshow`),
  rematch: (foodId) => post(`/foods/${foodId}/rematch`),

  acceptOffer: (foodId, shelterId) => post(`/foods/${foodId}/accept/${shelterId}`),
  declineOffer: (foodId, shelterId) => post(`/foods/${foodId}/decline/${shelterId}`),
};
