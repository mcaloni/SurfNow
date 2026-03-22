import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useRouter } from 'expo-router';
import type { BeachResponse } from '@/services/beaches.service';

interface Props {
  beach: BeachResponse;
}

export default function BeachCard({ beach }: Props) {
  const router = useRouter();

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={() => router.push(`/beach/${beach.id}`)}
    >
      <View style={styles.header}>
        <View>
          <Text style={styles.name}>{beach.name}</Text>
          <Text style={styles.location}>{beach.city}, {beach.state}</Text>
        </View>
        <ScoreBadge score={beach.score} label={beach.scoreLabel} />
      </View>

      <View style={styles.conditions}>
        <ConditionItem label="Onda" value={`${beach.conditions.waveHeight.toFixed(1)}m`} />
        <ConditionItem label="Período" value={`${beach.conditions.wavePeriod.toFixed(0)}s`} />
        <ConditionItem label="Vento" value={`${beach.conditions.windSpeed.toFixed(0)} km/h`} />
      </View>
    </TouchableOpacity>
  );
}

function ScoreBadge({ score, label }: { score: number; label: string }) {
  const color = score >= 8 ? '#00C853' : score >= 5 ? '#FFD600' : '#FF5252';
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
  name: { fontSize: 18, fontWeight: '700', color: '#1A1A2E' },
  location: { fontSize: 13, color: '#666', marginTop: 2 },
  badge: { borderRadius: 8, padding: 8, alignItems: 'center', minWidth: 60 },
  badgeScore: { fontSize: 20, fontWeight: '800', color: '#fff' },
  badgeLabel: { fontSize: 11, color: '#fff', fontWeight: '600' },
  conditions: { flexDirection: 'row', marginTop: 12, gap: 16 },
  conditionItem: { alignItems: 'center' },
  conditionLabel: { fontSize: 11, color: '#999' },
  conditionValue: { fontSize: 14, fontWeight: '600', color: '#333', marginTop: 2 },
});
