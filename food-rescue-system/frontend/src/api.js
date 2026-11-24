const BASE_URL = 'http://localhost:8080/api';

async function handleResponse(res) {
  if (!res.ok) {
    let message = 'Request failed';
    try {
      const data = await res.json();
      message = data.message || data.error || message;
    } catch (_) {}
    throw new Error(message);
  }
  return res.json();
}

export const api = {
  getFoods: () => fetch(`${BASE_URL}/foods`).then(handleResponse),
  getShelters: () => fetch(`${BASE_URL}/shelters`).then(handleResponse),
  postFood: (payload) =>
    fetch(`${BASE_URL}/foods`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).then(handleResponse),
  claimFood: (foodId, shelterId) =>
    fetch(`${BASE_URL}/foods/${foodId}/claim/${shelterId}`, {
      method: 'POST',
    }).then(handleResponse),
  confirmPickup: (foodId) =>
    fetch(`${BASE_URL}/foods/${foodId}/pickup`, {
      method: 'POST',
    }).then(handleResponse),
  reportNoShow: (foodId) =>
    fetch(`${BASE_URL}/foods/${foodId}/noshow`, {
      method: 'POST',
    }).then(handleResponse),
};
