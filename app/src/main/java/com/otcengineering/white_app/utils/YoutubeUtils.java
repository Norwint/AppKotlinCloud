package com.otcengineering.white_app.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by cenci7
 */

public class YoutubeUtils {

    public static String getYoutubeVideoThumbnail(String youtubeVideoUrl) {
        String videoId = getYoutubeVideoId(youtubeVideoUrl);
        return String.format(Locale.getDefault(), "http://img.youtube.com/vi/%s/mqdefault.jpg", videoId);
    }

    private static String getYoutubeVideoId(String youtubeVideoUrl) {
        String videoId = "";
        try {
            URL url = new URL(youtubeVideoUrl);
            String query = url.getQuery();
            if (query != null && query.contains("v=")) {
                videoId = query.substring(query.indexOf("v=") + 2);
            } else {
                String path = url.getPath();
                String[] pathParts = path.split("/");
                videoId = pathParts[pathParts.length - 1];
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return videoId;
    }
}
