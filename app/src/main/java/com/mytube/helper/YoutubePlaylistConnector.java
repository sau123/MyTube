package com.mytube.helper;

import android.content.Context;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.mytube.R;
import com.mytube.pojo.VideoItem;

import java.io.IOException;
import java.util.List;


public class YoutubePlaylistConnector {

    GoogleAccountCredential credential;
    private final String TAG = "YCP";
    private YouTube youtube;
    private YouTube.Playlists.List queryPlaylist;
    private YouTube.Playlists.Insert queryCreatePlaylist;
    public static final String KEY
            = "AIzaSyBBHbRhe57X9dCkyRvWqVV4pX_BU4MZCpA";

    public YoutubePlaylistConnector(Context context) {


        // Google Accounts

        credential = GoogleAccountCredential.usingOAuth2(context, YouTubeScopes.all());

        credential.setSelectedAccountName(CONSTANTS.USER_ACCESS_TOKEN);
        Log.d(TAG,"NAME"+CONSTANTS.USER_ACCESS_TOKEN);
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), credential).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            queryPlaylist = youtube.playlists().list("id,snippet");
            queryPlaylist.setKey(KEY);
            queryPlaylist.setFields("id,snippet");
        }catch(IOException e){
            Log.d(TAG, "Could not initialize: " + e);
        }
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
