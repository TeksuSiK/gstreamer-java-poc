package pl.teksusik.gstreamerjavapoc.stream;

import org.springframework.stereotype.Service;
import pl.teksusik.gstreamerjavapoc.gstreamer.GstStreamService;
import pl.teksusik.gstreamerjavapoc.stream.egress.Egress;
import pl.teksusik.gstreamerjavapoc.stream.egress.EgressRepository;
import pl.teksusik.gstreamerjavapoc.stream.ingress.Ingress;
import pl.teksusik.gstreamerjavapoc.stream.ingress.IngressRepository;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMapping;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMappingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StreamManagerService {

    private final IngressRepository ingressRepository;
    private final EgressRepository egressRepository;
    private final StreamMappingRepository mappingRepository;

    private final GstStreamService gstStreamService;

    public StreamManagerService(IngressRepository ingressRepository, EgressRepository egressRepository, StreamMappingRepository mappingRepository, GstStreamService gstStreamService) {
        this.ingressRepository = ingressRepository;
        this.egressRepository = egressRepository;
        this.mappingRepository = mappingRepository;
        this.gstStreamService = gstStreamService;
    }

    public void createIngress(Ingress ingress) {
        if (this.ingressRepository.existsByName(ingress.getName())) {
            throw new IllegalArgumentException("Ingress already exists");
        }

        this.ingressRepository.save(ingress);
    }

    public void deleteIngress(String name) {
        this.ingressRepository.delete(name);
        this.mappingRepository.delete(name);
    }

    public void createEgress(Egress egress) {
        if (this.egressRepository.existsByName(egress.getName())) {
            throw new IllegalArgumentException("Egress already exists");
        }

        this.egressRepository.save(egress);
    }

    public void deleteEgress(String name) {
        this.egressRepository.delete(name);
        this.mappingRepository.findAll().forEach(mapping -> {
            mapping.getEgresses().removeIf(e -> e.getName().equals(name));
            this.mappingRepository.save(mapping);
        });
    }

    public void addEgressToIngress(String ingressName, String egressName) {
        Optional<Ingress> ingress = this.ingressRepository.findByName(ingressName);
        Optional<Egress> egress = this.egressRepository.findByName(egressName);

        if (ingress.isEmpty()) {
            throw new IllegalArgumentException("Ingress not found");
        }

        if (egress.isEmpty()) {
            throw new IllegalArgumentException("Egress not found");
        }

        boolean assigned = this.mappingRepository.findAll().stream()
                .anyMatch(mapping -> mapping.getEgresses().stream()
                        .anyMatch(e -> e.getName().equals(egressName)));
        if (assigned) {
            throw new IllegalArgumentException("Egress already assigned to another ingress");
        }

        StreamMapping mapping = this.mappingRepository.findByIngressName(ingressName)
                .orElse(new StreamMapping(ingress.get()));

        mapping.addEgress(egress.get());
        this.mappingRepository.save(mapping);

        if (this.gstStreamService.isStreaming(ingressName)) {
            this.gstStreamService.addEgress(ingressName, egress.get());
        }
    }

    public void removeEgressFromIngress(String ingressName, String egressName) {
        Optional<StreamMapping> mappingOpt = this.mappingRepository.findByIngressName(ingressName);
        if (mappingOpt.isEmpty()) {
            return;
        }

        StreamMapping mapping = mappingOpt.get();
        Optional<Egress> egressOpt = mapping.getEgresses().stream()
                .filter(e -> e.getName().equals(egressName))
                .findFirst();

        if (egressOpt.isEmpty()) {
            return;
        }

        Egress egress = egressOpt.get();

        mapping.removeEgress(egress);
        this.mappingRepository.save(mapping);

        if (this.gstStreamService.isStreaming(ingressName)) {
            this.gstStreamService.removeEgress(ingressName, egress);
        }
    }

    public List<Ingress> getAllIngress() {
        return this.ingressRepository.findAll();
    }

    public List<Egress> getAllEgress() {
        return this.egressRepository.findAll();
    }

    public Optional<StreamMapping> getMappingForIngress(String ingressName) {
        return this.mappingRepository.findByIngressName(ingressName);
    }
}