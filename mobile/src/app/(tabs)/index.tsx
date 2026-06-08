import { useQuery } from '@tanstack/react-query';
import * as Location from 'expo-location';
import { useEffect, useState } from 'react';
import { ActivityIndicator, FlatList, StyleSheet, Text, View } from 'react-native';
import BeachCard from '@/features/beaches/BeachCard';
import { fetchBeaches } from '@/services/beaches.service';
import type { BeachResponse } from '@/services/beaches.service';

type Coords = { lat: number; lng: number } | null;

export default function BeachListScreen() {
  const [coords, setCoords] = useState<Coords>(null);
  const [locationError, setLocationError] = useState(false);

  useEffect(() => {
    Location.requestForegroundPermissionsAsync().then(({ status }) => {
      if (status !== 'granted') {
        setLocationError(true);
        return;
      }
      Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced })
        .then((pos) => setCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude }))
        .catch(() => setLocationError(true));
    });
  }, []);

  const { data: beaches, isLoading, error } = useQuery<BeachResponse[]>({
    queryKey: ['beaches', coords],
    queryFn: () => fetchBeaches(coords ? { lat: coords.lat, lng: coords.lng } : undefined),
    enabled: coords !== null || locationError,
  });

  if (!coords && !locationError) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={styles.hint}>Obtendo sua localização...</Text>
      </View>
    );
  }

  if (isLoading) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" />
        <Text style={styles.hint}>Carregando condições...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>Erro ao carregar praias</Text>
        <Text style={styles.errorDetail}>
          {(error as Error)?.message ?? String(error)}
        </Text>
      </View>
    );
  }

  return (
    <FlatList
      data={beaches}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => <BeachCard beach={item} userCoords={coords} />}
      contentContainerStyle={styles.list}
      ListHeaderComponent={
        <Text style={styles.header}>
          {coords ? 'Praias mais próximas de você' : 'Melhores condições agora'}
        </Text>
      }
    />
  );
}

const styles = StyleSheet.create({
  list: { padding: 16 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center', gap: 12 },
  hint: { fontSize: 14, color: '#666' },
  header: { fontSize: 16, fontWeight: '700', marginBottom: 12 },
  errorText: { fontSize: 16, fontWeight: '600' },
  errorDetail: { fontSize: 12, marginTop: 8, textAlign: 'center', paddingHorizontal: 24, color: '#666' },
});
