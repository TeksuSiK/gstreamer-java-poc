package pl.teksusik.gstreamerjavapoc.stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.teksusik.gstreamerjavapoc.stream.egress.Egress;
import pl.teksusik.gstreamerjavapoc.stream.ingress.Ingress;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMapping;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StreamManagerController {
    private final StreamManagerService mappingService;

    public StreamManagerController(StreamManagerService mappingService) {
        this.mappingService = mappingService;
    }

    @PostMapping("/ingress")
    public ResponseEntity<String> createIngress(@RequestBody Ingress ingress) {
        try {
            this.mappingService.createIngress(ingress);
            return ResponseEntity.ok("Ingress created");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/ingress/{name}")
    public ResponseEntity<String> deleteIngress(@PathVariable String name) {
        this.mappingService.deleteIngress(name);
        return ResponseEntity.ok("Ingress deleted");
    }

    @GetMapping("/ingress")
    public List<Ingress> getAllIngress() {
        return this.mappingService.getAllIngress();
    }

    @PostMapping("/egress")
    public ResponseEntity<String> createEgress(@RequestBody Egress egress) {
        try {
            this.mappingService.createEgress(egress);
            return ResponseEntity.ok("Egress created");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/egress/{name}")
    public ResponseEntity<String> deleteEgress(@PathVariable String name) {
        this.mappingService.deleteEgress(name);
        return ResponseEntity.ok("Egress deleted");
    }

    @GetMapping("/egress")
    public List<Egress> getAllEgress() {
        return this.mappingService.getAllEgress();
    }

    @PostMapping("/mapping/{ingressName}/add/{egressName}")
    public ResponseEntity<String> addEgressToIngress(@PathVariable String ingressName, @PathVariable String egressName) {
        try {
            this.mappingService.addEgressToIngress(ingressName, egressName);
            return ResponseEntity.ok("Egress added to ingress");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mapping/{ingressName}/remove/{egressName}")
    public ResponseEntity<String> removeEgressFromIngress(@PathVariable String ingressName, @PathVariable String egressName) {
        this.mappingService.removeEgressFromIngress(ingressName, egressName);
        return ResponseEntity.ok("Egress removed from ingress");
    }

    @GetMapping("/mapping/{ingressName}")
    public ResponseEntity<StreamMapping> getMapping(@PathVariable String ingressName) {
        return this.mappingService.getMappingForIngress(ingressName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
