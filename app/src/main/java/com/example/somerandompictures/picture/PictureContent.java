package com.example.somerandompictures.picture;


public class PictureContent {
    private final String id;
    private final String description;
    private final String url;

    public PictureContent(String id, String url, String description) {
        this.id = id;
        this.url = url;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }
}
