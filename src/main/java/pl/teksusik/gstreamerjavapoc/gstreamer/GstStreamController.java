package pl.teksusik.gstreamerjavapoc.gstreamer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.teksusik.gstreamerjavapoc.stream.StreamManagerService;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMapping;

import java.util.Optional;

@RestController
@RequestMapping("/api/stream")
public class GstStreamController {
    private final StreamManagerService mappingService;
    private final GstStreamService gstService;

    public GstStreamController(StreamManagerService mappingService, GstStreamService gstService) {
        this.mappingService = mappingService;
        this.gstService = gstService;
    }

    @PostMapping("/start/{ingressName}")
    public ResponseEntity<String> startStream(@PathVariable String ingressName) {
        Optional<StreamMapping> mappingOpt = this.mappingService.getMappingForIngress(ingressName);
        if (mappingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No mapping found for ingress: " + ingressName);
        }

        if (this.gstService.isStreaming(ingressName)) {
            return ResponseEntity.badRequest().body("Stream already running for ingress: " + ingressName);
        }

        this.gstService.startStream(mappingOpt.get());
        return ResponseEntity.ok("Stream started for ingress " + ingressName);
    }

    @PostMapping("/stop/{ingressName}")
    public ResponseEntity<String> stopStream(@PathVariable String ingressName) {
        if (!this.gstService.isStreaming(ingressName)) {
            return ResponseEntity.badRequest().body("Stream not running for ingress: " + ingressName);
        }

        this.gstService.stopStream(ingressName);
        return ResponseEntity.ok("Stream stopped for ingress " + ingressName);
    }

    @GetMapping("/status/{ingressName}")
    public ResponseEntity<String> checkStatus(@PathVariable String ingressName) {
        boolean running = this.gstService.isStreaming(ingressName);
        return ResponseEntity.ok(running ? "running" : "stopped");
    }
}