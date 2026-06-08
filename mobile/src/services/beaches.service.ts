import api from './api';

export interface BeachResponse {
  id: string;
  name: string;
  city: string;
  state: string;
  latitude: number;
  longitude: number;
  score: number;
  scoreLabel: string;
  conditions: {
    waveHeight: number;
    wavePeriod: number;
    windSpeed: number;
    windDirection: number;
    swellDirection: number;
    lastUpdated: string;
  };
}

export const fetchBeaches = (params?: { state?: string; lat?: number; lng?: number }): Promise<BeachResponse[]> =>
  api.get('/beaches', { params }).then((r) => r.data);

export const fetchBeach = (id: string): Promise<BeachResponse> =>
  api.get(`/beaches/${id}`).then((r) => r.data);
