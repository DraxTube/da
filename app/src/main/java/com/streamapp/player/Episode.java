package com.streamapp.player;

import com.google.gson.annotations.SerializedName;

public class Episode {
    @SerializedName("season")
    private int season;

    @SerializedName("episode")
    private int episode;

    @SerializedName("number")
    private String number;

    @SerializedName("title")
    private String title;

    @SerializedName("link")
    private String link;

    public int getSeason() { return season; }
    public int getEpisodeNumber() { return episode; }
    public String getNumber() { return number; }
    public String getTitle() { return title; }
    public String getLink() { return link; }

    public String getShortTitle() {
        // Truncate after colon if too long
        if (title != null && title.contains(":")) {
            return title.substring(0, title.indexOf(":")).trim();
        }
        return title != null ? title : "";
    }

    public String getDescription() {
        if (title != null && title.contains(":")) {
            return title.substring(title.indexOf(":") + 1).trim();
        }
        return "";
    }
}
