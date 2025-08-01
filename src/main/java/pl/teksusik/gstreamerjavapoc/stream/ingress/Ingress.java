package pl.teksusik.gstreamerjavapoc.stream.ingress;

public final class Ingress {
    private final String name;
    private final String url;

    public Ingress(String name, String url) {
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