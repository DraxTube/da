package com.streamapp.player;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EpisodeLoader {

    private static List<Episode> allEpisodes = null;

    public static List<Episode> loadAll(Context context) {
        if (allEpisodes != null) return allEpisodes;
        try {
            InputStream is = context.getAssets().open("episodes.json");
            byte[] bytes = is.readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);
            is.close();
            Type listType = new TypeToken<List<Episode>>() {}.getType();
            allEpisodes = new Gson().fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
            allEpisodes = new ArrayList<>();
        }
        return allEpisodes;
    }

    public static List<Integer> getSeasons(Context context) {
        return loadAll(context).stream()
            .map(Episode::getSeason)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    public static List<Episode> getBySeason(Context context, int season) {
        return loadAll(context).stream()
            .filter(e -> e.getSeason() == season)
            .collect(Collectors.toList());
    }
}
