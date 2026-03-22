import { useQuery } from '@tanstack/react-query';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import BeachCard from '@/features/beaches/BeachCard';
import { fetchBeaches } from '@/services/beaches.service';
import type { BeachResponse } from '@/services/beaches.service';

export default function BeachListScreen() {
  const { data: beaches, isLoading, error } = useQuery<BeachResponse[]>({
    queryKey: ['beaches'],
    queryFn: () => fetchBeaches(),
  });

  if (isLoading) {
    return <View style={styles.center}><Text>Carregando condições...</Text></View>;
  }

  if (error) {
    return <View style={styles.center}><Text>Erro ao carregar praias</Text></View>;
  }

  return (
    <FlatList
      data={beaches}
      keyExtractor={(item) => item.id}
      renderItem={({ item }) => <BeachCard beach={item} />}
      contentContainerStyle={styles.list}
    />
  );
}

const styles = StyleSheet.create({
  list: { padding: 16 },
  center: { flex: 1, justifyContent: 'center', alignItems: 'center' },
});
