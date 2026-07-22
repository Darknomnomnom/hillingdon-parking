import client from './client';

export const simulateArrival = (plate: string) =>
  client.post('/anpr/event', null, { params: { plate, direction: 'ENTRY' } });
