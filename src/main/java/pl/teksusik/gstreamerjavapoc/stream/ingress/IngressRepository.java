package pl.teksusik.gstreamerjavapoc.stream.ingress;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class IngressRepository {
    private final Map<String, Ingress> ingressMap = new HashMap<>();

    public Optional<Ingress> findByName(String name) {
        return Optional.ofNullable(this.ingressMap.get(name));
    }

    public List<Ingress> findAll() {
        return new ArrayList<>(this.ingressMap.values());
    }

    public void save(Ingress ingress) {
        this.ingressMap.put(ingress.getName(), ingress);
    }

    public void delete(String name) {
        this.ingressMap.remove(name);
    }

    public boolean existsByName(String name) {
        return this.ingressMap.containsKey(name);
    }
}