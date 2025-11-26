package com.example.spotify_kp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {
    @SerializedName("display_name")
    private String displayName;

    @SerializedName("external_urls")
    private ExternalUrls externalUrls;

    private String href;
    private String id;
    private List<Image> images; // Добавили!

    public User() {}

    public String getDisplayName() {
        return displayName;
    }

    public ExternalUrls getExternalUrls() {
        return externalUrls;
    }

    public String getHref() {
        return href;
    }

    public String getId() {
        return id;
    }

    public List<Image> getImages() {
        return images;
    }


    public String getImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl();
        }
        return null;
    }
}