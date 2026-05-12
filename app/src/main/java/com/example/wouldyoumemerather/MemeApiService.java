package com.example.wouldyoumemerather;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MemeApiService {
    private static final String IMGFLIP_URL = "https://api.imgflip.com/get_memes";
    private static final String REDDIT_URL = "https://www.reddit.com/r/memes/hot.json?limit=50";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(List<Meme> memes);
        void onError(Exception e);
    }

    public void fetchMemes(Callback callback) {
        new Thread(() -> {
            try {
                List<Meme> allMemes = new ArrayList<>();
                allMemes.addAll(fetchImgflipMemes());
                allMemes.addAll(fetchRedditMemes());
                callback.onSuccess(allMemes);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }

    private List<Meme> fetchImgflipMemes() throws IOException {
        Request request = new Request.Builder().url(IMGFLIP_URL).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            List<Meme> memes = new ArrayList<>();
            if (json.get("success").getAsBoolean()) {
                JsonArray array = json.getAsJsonObject("data").getAsJsonArray("memes");
                for (JsonElement element : array) {
                    JsonObject obj = element.getAsJsonObject();
                    memes.add(new Meme(
                            obj.get("id").getAsString(),
                            obj.get("name").getAsString(),
                            obj.get("url").getAsString()
                    ));
                }
            }
            return memes;
        }
    }

    private List<Meme> fetchRedditMemes() throws IOException {
        Request request = new Request.Builder().url(REDDIT_URL).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            List<Meme> memes = new ArrayList<>();
            JsonArray children = json.getAsJsonObject("data").getAsJsonArray("children");
            for (JsonElement element : children) {
                JsonObject post = element.getAsJsonObject().getAsJsonObject("data");
                if (post.get("over_18").getAsBoolean() || post.get("is_video").getAsBoolean()) continue;
                String url = post.get("url").getAsString();
                if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                    memes.add(new Meme(
                            "reddit-" + post.get("id").getAsString(),
                            post.get("title").getAsString(),
                            url
                    ));
                }
            }
            return memes;
        }
    }
}
