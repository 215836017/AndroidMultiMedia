package com.cakes.demomediacodec.mediaCodec;

public final class VideoConfiguration {
    public static final int DEFAULT_HEIGHT = 640;
    public static final int DEFAULT_WIDTH = 480;
    public static final int DEFAULT_FPS = 25;
    public static final int DEFAULT_BPS = 400 * 1024;
    public static final int DEFAULT_MAX_BPS = 2048 * 1024;
    public static final int DEFAULT_IFI = 10;
    public static final String DEFAULT_MIME = "video/avc";

    public final int height;
    public final int width;
    public final int bps;
    public final int maxBps;
    public final int fps;
    public final int ifi;
    public final String mime;

    private VideoConfiguration(final Builder builder) {
        height = builder.height;
        width = builder.width;
        bps = builder.bps;
        maxBps = builder.maxBps;
        fps = builder.fps;
        ifi = builder.ifi;
        mime = builder.mime;
    }

    public static VideoConfiguration createDefault() {
        return new Builder().build();
    }

    public static class Builder {
        private int height = DEFAULT_HEIGHT;
        private int width = DEFAULT_WIDTH;
        private int bps = DEFAULT_BPS;
        private int fps = DEFAULT_FPS;
        private int maxBps = DEFAULT_MAX_BPS;
        private int ifi = DEFAULT_IFI;
        private String mime = DEFAULT_MIME;

        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setBps(int bps, int maxBps) {
            this.bps = bps;
            this.maxBps = maxBps;
            return this;
        }

        public Builder setFps(int fps) {
            this.fps = fps;
            return this;
        }

        public Builder setIfi(int ifi) {
            this.ifi = ifi;
            return this;
        }

        public Builder setMime(String mime) {
            this.mime = mime;
            return this;
        }

        public VideoConfiguration build() {
            return new VideoConfiguration(this);
        }
    }
}
