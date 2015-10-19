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
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mytube.R;
import com.mytube.pojo.VideoItem;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class YoutubePlaylistConnector {

    private final String TAG = "YCP";
    private YouTube youtube;
    private YouTube.Playlists.List queryPlaylist;
    private YouTube.Playlists.Insert queryCreatePlaylist;
    private YouTube.PlaylistItems.List queryPlaylistVideos;
    private YouTube.Videos.List queryView; // to fetch statistics/viewCount
    private YouTube.PlaylistItems.Insert queryInsert;
    private YouTube.PlaylistItems.Delete queryDelete;
    public static final String KEY = CONSTANTS.YOUTUBE_API_KEY;


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

    private String playlistExists(String playlistName){
        try {
            queryPlaylist = youtube.playlists().list("id, snippet");
            queryPlaylist.setKey(KEY);
            queryPlaylist.setMine(true);
            PlaylistListResponse plr = queryPlaylist.execute();
            if(plr.isEmpty())
                return null;
            List<Playlist> playlists = plr.getItems();
            for(Playlist playlist : playlists){
                Log.d(TAG, playlist.getSnippet().getTitle());
                if(playlist.getSnippet().getTitle().equalsIgnoreCase(playlistName)) {
                    CONSTANTS.PLAYLIST_ID = playlist.getId();
                    return CONSTANTS.PLAYLIST_ID;
                }

            }
        }catch (UserRecoverableAuthIOException e){
            Log.d(TAG, e.getIntent().getPackage());
        } catch (IOException e) {
            Log.d(TAG, "Could not retrieve: " + e);
        }
        return null;
    }

    public String createPlaylist(String playlistName){
        try {

            String pId = playlistExists(playlistName);
            if(pId != null)
                return pId;

            Playlist pl = new Playlist();
            PlaylistSnippet pls = new PlaylistSnippet();

            pls.setTitle(playlistName);
            pl.setSnippet(pls);

            queryCreatePlaylist = youtube.playlists().insert("id,snippet", pl);
            queryCreatePlaylist.setKey(KEY);
            queryCreatePlaylist.setFields("id,snippet");

            Log.d(TAG, queryCreatePlaylist.toString());
            Playlist response = queryCreatePlaylist.execute();
            CONSTANTS.PLAYLIST_ID = response.getId();
            Log.d("Query Result ", response.toString());
            return CONSTANTS.PLAYLIST_ID;
        } catch (UserRecoverableAuthIOException e){
            Log.d(TAG, e.getIntent().getPackage());
        } catch (IOException e) {
            Log.d(TAG, "Could not create: " + e);
            return null;
        }
        return null;
    }

    public void addToPlaylist(String videoId){
        PlaylistItem pli = new PlaylistItem();

        pli.setSnippet(new PlaylistItemSnippet()
                    .setResourceId(new ResourceId()
                                    .setVideoId(videoId)
                                    .setKind("youtube#video"))
                    .setPlaylistId(CONSTANTS.PLAYLIST_ID));
        try {
            queryInsert = youtube.playlistItems().insert("snippet",pli);
            queryInsert.setKey(KEY);
            Log.d("YPC","video id "+ videoId + "  playlistid "+CONSTANTS.PLAYLIST_ID);
            PlaylistItem response = queryInsert.execute();
            Log.d("YTC", "addto playlist: "+response.toPrettyString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeFromPlaylist(String videoId){

        try {
            queryDelete = youtube.playlistItems().delete("id");
            queryDelete.setKey(KEY);
            queryDelete.setId(videoId);
            queryDelete.execute();
            Log.d("YTC", "remove from playlist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<VideoItem> getPlaylistVideos(String pId){

        try {
            CONSTANTS.PLAYLIST_ID = pId;
            queryPlaylistVideos = youtube.playlistItems().list("id,snippet");
            queryPlaylistVideos.setKey(KEY);
            queryPlaylistVideos.setPlaylistId(pId);

            queryView = youtube.videos().list("statistics");
            queryView.setKey(KEY);
            queryView.setFields("items(statistics/viewCount)");

            PlaylistItemListResponse response = queryPlaylistVideos.execute();
            // get view count for all videos in playlist

            if(response == null || response.size() == 0)
                return null;

            String videoIds = "";
            List<PlaylistItem> results = response.getItems();

            if(results == null || results.size() == 0)
                return null;

            for (PlaylistItem r :results){
                videoIds+=r.getSnippet().getResourceId().getVideoId()+",";
            }

            videoIds=videoIds.substring(0,videoIds.length()-1);
            queryView.setId(videoIds);
            VideoListResponse response2 = queryView.execute();

            List<VideoItem> items = new ArrayList<VideoItem>();
            List<Video> results2 = response2.getItems();

            for (int i =0;i<results.size();i++){

                VideoItem item = new VideoItem();
                item.setTitle(results.get(i).getSnippet().getTitle());
                item.setDescription(results.get(i).getSnippet().getDescription());
                item.setThumbnailURL(results.get(i).getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(results.get(i).getSnippet().getResourceId().getVideoId());
                item.setViewCount(results2.get(i).getStatistics().getViewCount().toString());    // viewCount converted to String.
                item.setGlobalId(results.get(i).getId());
                String dateString1 = results.get(i).getSnippet().getPublishedAt().toString();

                // Then format date object to string in pattern "MM/dd/yy 'at' h:mma".
                String dateString2 = null;
                dateString2 = new SimpleDateFormat("MM/dd/yyyy").format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.kkk'Z'").parse(dateString1));

                Log.d(TAG,"Date: "+dateString2); // 08/24/12
                /* end of logic to convert to date format from ISO 8601 format */

                item.setDate(dateString2);
                items.add(item);
            }

            return items;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

}
