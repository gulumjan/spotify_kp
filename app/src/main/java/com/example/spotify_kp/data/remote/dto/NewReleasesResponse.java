package com.example.spotify_kp.data.remote.dto;

import java.util.List;

public class NewReleasesResponse {
    private Albums albums;

    public NewReleasesResponse() {}

    public Albums getAlbums() {
        return albums;
    }

    public void setAlbums(Albums albums) {
        this.albums = albums;
    }

    public static class Albums {
        private String href;
        private List<AlbumDto> items;
        private int limit;
        private int offset;
        private int total;

        public Albums() {}

        public String getHref() { return href; }
        public void setHref(String href) { this.href = href; }

        public List<AlbumDto> getItems() { return items; }
        public void setItems(List<AlbumDto> items) { this.items = items; }

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }

        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }
}