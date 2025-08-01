package pl.teksusik.gstreamerjavapoc.gstreamer;

import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Element;
import org.freedesktop.gstreamer.ElementFactory;
import org.freedesktop.gstreamer.Pad;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.Structure;
import pl.teksusik.gstreamerjavapoc.stream.egress.Egress;
import pl.teksusik.gstreamerjavapoc.stream.mapping.StreamMapping;

import java.util.HashMap;
import java.util.Map;

public class GstStreamSession {
    private final Pipeline pipeline;

    private final Element rtmpsrc;
    private final Element flvdemux;

    private final Element queueVideoIn;
    private final Element h264parse;

    private final Element queueAudioIn;
    private final Element aacparse;

    private final Element teeVideo;
    private final Element teeAudio;

    private final Map<String, EgressBranch> egresses = new HashMap<>();

    public GstStreamSession(StreamMapping mapping) {
        this.pipeline = new Pipeline("pipeline-" + mapping.getIngress().getName());

        this.rtmpsrc = ElementFactory.make("rtmpsrc", "src");
        this.flvdemux = ElementFactory.make("flvdemux", "demux");

        this.queueVideoIn = ElementFactory.make("queue", "queueVideoIn");
        this.h264parse = ElementFactory.make("h264parse", "h264parse");

        this.queueAudioIn = ElementFactory.make("queue", "queueAudioIn");
        this.aacparse = ElementFactory.make("aacparse", "aacparse");

        this.teeVideo = ElementFactory.make("tee", "teeVideo");
        this.teeAudio = ElementFactory.make("tee", "teeAudio");

        this.rtmpsrc.set("location", mapping.getIngress().getUrl());

        this.pipeline.addMany(this.rtmpsrc, this.flvdemux,
                this.queueVideoIn, this.h264parse, this.teeVideo,
                this.queueAudioIn, this.aacparse, this.teeAudio);

        this.rtmpsrc.link(this.flvdemux);
        this.queueVideoIn.link(this.h264parse);
        this.h264parse.link(this.teeVideo);
        this.queueAudioIn.link(this.aacparse);
        this.aacparse.link(this.teeAudio);

        this.flvdemux.connect((Element.PAD_ADDED) this::handlePadAdded);

        for (Egress egress : mapping.getEgresses()) {
            addEgress(egress);
        }
    }

    private void handlePadAdded(Element src, Pad newPad) {
        Caps caps = newPad.getCurrentCaps();
        Structure struct = caps.getStructure(0);
        String media = struct.getName();

        Pad sinkPad = null;

        if (media.startsWith("video")) {
            sinkPad = this.queueVideoIn.getStaticPad("sink");
        } else if (media.startsWith("audio")) {
            sinkPad = this.queueAudioIn.getStaticPad("sink");
        }

        if (sinkPad != null && !sinkPad.isLinked()) {
            newPad.link(sinkPad);
        }
    }

    public void addEgress(Egress egress) {
        if (this.egresses.containsKey(egress.getName())) {
            return;
        }

        EgressBranch branch = new EgressBranch(egress);
        this.egresses.put(egress.getName(), branch);

        addEgress(branch);
    }

    public void addEgress(EgressBranch branch) {
        this.pipeline.addMany(branch.queueVideo, branch.queueAudio, branch.flvmux, branch.rtmpsink);

        branch.videoPad = this.teeVideo.getRequestPad("src_%u");
        branch.audioPad = this.teeAudio.getRequestPad("src_%u");

        branch.videoPad.link(branch.queueVideo.getStaticPad("sink"));
        branch.audioPad.link(branch.queueAudio.getStaticPad("sink"));

        branch.queueVideo.link(branch.flvmux);
        branch.queueAudio.link(branch.flvmux);
        branch.flvmux.link(branch.rtmpsink);
    }

    public void removeEgress(Egress egress) {
        EgressBranch branch = this.egresses.remove(egress.getName());
        if (branch == null) {
            return;
        }

        removeEgress(branch);
    }

    public void removeEgress(EgressBranch branch) {
        Pad teeVideoPad = branch.videoPad;
        if (teeVideoPad != null) {
            teeVideoPad.unlink(branch.queueVideo.getStaticPad("sink"));
            this.teeVideo.releaseRequestPad(teeVideoPad);
        }
        Pad teeAudioPad = branch.audioPad;
        if (teeAudioPad != null) {
            teeAudioPad.unlink(branch.queueAudio.getStaticPad("sink"));
            this.teeAudio.releaseRequestPad(teeAudioPad);
        }

        branch.queueVideo.unlink(branch.flvmux);
        branch.queueAudio.unlink(branch.flvmux);
        branch.flvmux.unlink(branch.rtmpsink);

        this.pipeline.remove(branch.queueVideo);
        this.pipeline.remove(branch.queueAudio);
        this.pipeline.remove(branch.flvmux);
        this.pipeline.remove(branch.rtmpsink);
    }

    public void start() {
        this.pipeline.play();
    }

    public void stop() {
        this.pipeline.stop();
        this.pipeline.close();
    }

    public static class EgressBranch {
        final Egress egress;
        final Element queueVideo;
        final Element queueAudio;
        final Element flvmux;
        final Element rtmpsink;
        Pad videoPad;
        Pad audioPad;

        EgressBranch(Egress egress) {
            this.egress = egress;

            this.queueVideo = ElementFactory.make("queue", "queueVideoOut_" + egress.getName());
            this.queueAudio = ElementFactory.make("queue", "queueAudioOut_" + egress.getName());

            this.flvmux = ElementFactory.make("flvmux", "flvmux_" + egress.getName());
            this.flvmux.set("streamable", true);

            this.rtmpsink = ElementFactory.make("rtmpsink", "rtmpsink_" + egress.getName());
            this.rtmpsink.set("location", egress.getUrl());
        }
    }
}
