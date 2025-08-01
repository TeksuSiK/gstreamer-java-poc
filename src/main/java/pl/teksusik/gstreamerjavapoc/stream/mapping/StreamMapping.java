package pl.teksusik.gstreamerjavapoc.stream.mapping;

import pl.teksusik.gstreamerjavapoc.stream.egress.Egress;
import pl.teksusik.gstreamerjavapoc.stream.ingress.Ingress;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StreamMapping {
    private final Ingress ingress;
    private final Set<Egress> egresses = new HashSet<>();

    public StreamMapping(Ingress ingress) {
        this.ingress = ingress;
    }

    public Ingress getIngress() {
        return ingress;
    }

    public Set<Egress> getEgresses() {
        return Collections.unmodifiableSet(egresses);
    }

    public boolean containsEgress(Egress egress) {
        return egresses.contains(egress);
    }

    public boolean addEgress(Egress egress) {
        return egresses.add(egress);
    }

    public boolean removeEgress(Egress egress) {
        return egresses.remove(egress);
    }
}
