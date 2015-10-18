package com.mytube.helper;

import android.content.Context;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.mytube.R;
import com.mytube.pojo.VideoItem;

import java.io.IOException;
import java.util.List;


public class YoutubePlaylistConnector {

    private final String TAG = "YCP";
    private YouTube youtube;
    private YouTube.Playlists.List queryPlaylist;
    private YouTube.Playlists.Insert queryCreatePlaylist;
    public static final String KEY
            = CONSTANTS.YOUTUBE_API_KEY;

    public YoutubePlaylistConnector(Context context) {


        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
                HttpHeaders hh = new HttpHeaders();

                hh.setAuthorization("Bearer "+CONSTANTS.USER_ACCESS_TOKEN);
                hr.setHeaders(hh);
            }
        }).setApplicationName(context.getString(R.string.app_name)).build();
    }

    public List<VideoItem> createPlaylist(String playlistName){
        try {
            Playlist pl = new Playlist();
            PlaylistSnippet pls = new PlaylistSnippet();

            pls.setTitle(playlistName);
            pl.setSnippet(pls);
            queryCreatePlaylist = youtube.playlists().insert("id,snippet",pl);
            queryCreatePlaylist.setKey(KEY);
            queryCreatePlaylist.setFields("id,snippet");

            Log.d(TAG,queryCreatePlaylist.toString());
            Playlist response = queryCreatePlaylist.execute();
            Log.d("Query Result ", response.toString());

        } catch (UserRecoverableAuthIOException e){
            Log.d(TAG, e.getIntent().getPackage());
        } catch (IOException e) {
            Log.d(TAG, "Could not create: " + e);
            return null;
        }
        return null;
    }

    public List<VideoItem> search(String keywords){
        try{
            PlaylistListResponse response = queryPlaylist.execute();
            Log.d("Query Result ",response.toString());

            List<Playlist> results = response.getItems();
            Log.d("Query Result in List ", results.toString());

            return null;
        }catch(Exception e){
            Log.d(TAG, "Could not search: " + e);
            return null;
        }
    }
}
