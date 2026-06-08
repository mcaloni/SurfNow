import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useRouter } from 'expo-router';
import type { BeachResponse } from '@/services/beaches.service';

interface Props {
  beach: BeachResponse;
  userCoords?: { lat: number; lng: number } | null;
}

function distanceKm(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLng / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

export default function BeachCard({ beach, userCoords }: Props) {
  const router = useRouter();
  const distance = userCoords
    ? distanceKm(userCoords.lat, userCoords.lng, beach.latitude, beach.longitude)
    : null;

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={() => router.push(`/beach/${beach.id}`)}
    >
      <View style={styles.header}>
        <View style={styles.info}>
          <Text style={styles.name}>{beach.name}</Text>
          <Text style={styles.location}>
            {beach.city}, {beach.state}
            {distance != null ? `  ·  ${distance < 1 ? `${(distance * 1000).toFixed(0)}m` : `${distance.toFixed(0)}km`}` : ''}
          </Text>
        </View>
        <ScoreBadge score={beach.score} label={beach.scoreLabel} />
      </View>

      <View style={styles.divider} />

      <View style={styles.conditions}>
        <ConditionItem label="Onda" value={`${beach.conditions.waveHeight.toFixed(1)}m`} />
        <ConditionItem label="Período" value={`${beach.conditions.wavePeriod.toFixed(0)}s`} />
        <ConditionItem label="Vento" value={`${beach.conditions.windSpeed.toFixed(0)} km/h`} />
      </View>
    </TouchableOpacity>
  );
}

function ScoreBadge({ score, label }: { score: number; label: string }) {
  const color = score >= 8 ? '#00C853' : score >= 6 ? '#1976D2' : score >= 4 ? '#F9A825' : '#E53935';
  return (
    <View style={[styles.badge, { backgroundColor: color }]}>
      <Text style={styles.badgeScore}>{score.toFixed(1)}</Text>
      <Text style={styles.badgeLabel}>{label}</Text>
    </View>
  );
}

function ConditionItem({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.conditionItem}>
      <Text style={styles.conditionLabel}>{label}</Text>
      <Text style={styles.conditionValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 4,
    elevation: 3,
  },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' },
  info: { flex: 1, marginRight: 12 },
  name: { fontSize: 18, fontWeight: '700', color: '#1a1a1a' },
  location: { fontSize: 13, color: '#888', marginTop: 3 },
  badge: { borderRadius: 10, padding: 8, alignItems: 'center', minWidth: 64 },
  badgeScore: { fontSize: 20, fontWeight: '800', color: '#fff' },
  badgeLabel: { fontSize: 11, fontWeight: '600', color: '#fff', marginTop: 1 },
  divider: { height: 1, backgroundColor: '#f0f0f0', marginVertical: 12 },
  conditions: { flexDirection: 'row', gap: 20 },
  conditionItem: { alignItems: 'center' },
  conditionLabel: { fontSize: 11, color: '#888' },
  conditionValue: { fontSize: 14, fontWeight: '600', color: '#333', marginTop: 2 },
});
