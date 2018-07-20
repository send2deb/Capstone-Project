package com.debdroid.tinru.datamodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AmadeusInterestDetails {
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("wiki_page_link")
    @Expose
    private String wikiPageLink;
    @SerializedName("short_description")
    @Expose
    private String shortDescription;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWikiPageLink() {
        return wikiPageLink;
    }

    public void setWikiPageLink(String wikiPageLink) {
        this.wikiPageLink = wikiPageLink;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
