package pl.teksusik.gstreamerjavapoc.stream.egress;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EgressRepository {
    private final Map<String, Egress> egressMap = new HashMap<>();

    public Optional<Egress> findByName(String name) {
        return Optional.ofNullable(this.egressMap.get(name));
    }

    public List<Egress> findAll() {
        return new ArrayList<>(this.egressMap.values());
    }

    public void save(Egress egress) {
        this.egressMap.put(egress.getName(), egress);
    }

    public void delete(String name) {
        this.egressMap.remove(name);
    }

    public boolean existsByName(String name) {
        return this.egressMap.containsKey(name);
    }
}
