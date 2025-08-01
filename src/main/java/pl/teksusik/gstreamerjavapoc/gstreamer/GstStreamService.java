package pl.teksusik.gstreamerjavapoc.gstreamer;

import org.springframework.stereotype.Service;
import pl.teksusik.gstreamerjavapoc.stream.egress.Egress;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GstStreamService {
    private final Map<String, GstStreamSession> activeSessions = new ConcurrentHashMap<>();

    public void startStream(StreamMapping mapping) {
        String ingressName = mapping.getIngress().getName();
        if (this.activeSessions.containsKey(ingressName)) {
            return;
        }

        GstStreamSession session = new GstStreamSession(mapping);
        session.start();
        this.activeSessions.put(ingressName, session);
    }

    public void stopStream(String ingressName) {
        GstStreamSession session = this.activeSessions.remove(ingressName);
        if (session != null) {
            session.stop();
        }
    }

    public boolean isStreaming(String ingressName) {
        return this.activeSessions.containsKey(ingressName);
    }

    public void addEgress(String ingressName, Egress egress) {
        GstStreamSession session = this.activeSessions.get(ingressName);
        if (session != null) {
            session.addEgress(egress);
            session.start();
        }
    }

    public void removeEgress(String ingressName, Egress egress) {
        GstStreamSession session = this.activeSessions.get(ingressName);
        if (session != null) {
            session.removeEgress(egress);
        }
    }
}
