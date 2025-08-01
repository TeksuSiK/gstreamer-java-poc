package pl.teksusik.gstreamerjavapoc.stream.egress;

public final class Egress {
    private final String name;
    private final String url;

    public Egress(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
