package com.example.wouldyoumemerather;

public class Meme {
    private String id;
    private String name;
    private String url;

    public Meme(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
}
