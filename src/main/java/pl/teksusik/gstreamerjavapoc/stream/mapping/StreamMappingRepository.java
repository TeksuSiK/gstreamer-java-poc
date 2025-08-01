package pl.teksusik.gstreamerjavapoc.stream.mapping;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class StreamMappingRepository {
    private final Map<String, StreamMapping> mappingMap = new HashMap<>();

    public Optional<StreamMapping> findByIngressName(String ingressName) {
        return Optional.ofNullable(this.mappingMap.get(ingressName));
    }

    public List<StreamMapping> findAll() {
        return new ArrayList<>(this.mappingMap.values());
    }

    public void save(StreamMapping mapping) {
        this.mappingMap.put(mapping.getIngress().getName(), mapping);
    }

    public void delete(String ingressName) {
        this.mappingMap.remove(ingressName);
    }

    public boolean existsByIngressName(String ingressName) {
        return this.mappingMap.containsKey(ingressName);
    }
}