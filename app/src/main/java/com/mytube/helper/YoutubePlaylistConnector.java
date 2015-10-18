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
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
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
    public static final String KEY = CONSTANTS.YOUTUBE_API_KEY;
    public static String playlistId;

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
                    playlistId = playlist.getId();
                    Log.d(TAG, "PlaylistID: " + playlistId);
                    return playlistId;
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

            Log.d(TAG,queryCreatePlaylist.toString());
            Playlist response = queryCreatePlaylist.execute();
            playlistId = response.getId();
            Log.d("Query Result ", response.toString());
            return playlistId;
        } catch (UserRecoverableAuthIOException e){
            Log.d(TAG, e.getIntent().getPackage());
        } catch (IOException e) {
            Log.d(TAG, "Could not create: " + e);
            return null;
        }
        return null;
    }

    public List<VideoItem> getPlaylistVideos(String pId){

        try {
            queryPlaylistVideos = youtube.playlistItems().list("id,snippet");
            queryPlaylistVideos.setKey(KEY);
            queryPlaylistVideos.setPlaylistId(pId);

            queryView = youtube.videos().list("statistics");
            queryView.setKey(KEY);
            queryView.setFields("items(statistics/viewCount)");

            PlaylistItemListResponse response = queryPlaylistVideos.execute();
            // get view count for all videos in playlist
            String videoIds = "";
            List<PlaylistItem> results = response.getItems();
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

                // item.setViewCount(results2);
                /* start of logic to convert to date format from ISO 8601 format */
                // First parse string in pattern "yyyy-MM-dd'T'HH:mm:ss'Z'" to date object.

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
