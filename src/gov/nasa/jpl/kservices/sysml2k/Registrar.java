package gov.nasa.jpl.kservices.sysml2k;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("serial")
public class Registrar<K,V> extends LinkedHashMap<K,Collection<V>> {
  public Registrar<K,V> merge(Registrar<K,V> other) {
    Registrar<K,V> output = new Registrar<K,V>();
    this .forEach( output::registerAll );
    other.forEach( output::registerAll );
    return output;
  }
  
  public void register(K key, V value) {
    if (!this.containsKey(key)) {
      addKey(key);
    }
    this.get(key).add(value);
  }
  
  public void registerAll(K key, Collection<V> values) {
    values.forEach( value -> this.register(key, value) );
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<V> get(Object key) {
    if (!this.containsKey(key)) {
      addKey((K) key);
    }
    return super.get(key);
  }
  
  public Stream<Map.Entry<K,V>> pairingStream() {
    return this.entrySet().stream().flatMap( entry ->
        entry.getValue().stream().map( value ->
            new AbstractMap.SimpleEntry<K, V>(entry.getKey(), value) ));
  }
  
  /// Private helpers
  
  private void addKey(K key) {
    this.put(key, new LinkedList<V>());
  }
}
