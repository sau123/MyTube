package com.mytube.helper;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.mytube.R;
import com.mytube.pojo.VideoItem;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;      // to fetch attributes other than viewCount
    private YouTube.Videos.List queryView; // to fetch statistics/viewCount

    public static final String KEY
            = "AIzaSyBBHbRhe57X9dCkyRvWqVV4pX_BU4MZCpA";

    public YoutubeConnector(Context content) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(content.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/publishedAt)");

            queryView = youtube.videos().list("statistics");
            queryView.setKey(KEY);
            queryView.setFields("items(statistics/viewCount)");


        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }


    public List<VideoItem> search(String keywords){
        Log.d("To be Searched ",keywords);
        query.setQ(keywords);
        Log.d("Saumeel",query.toString());
        try{
            Log.d("Saumeel ","Before query execute");
            SearchListResponse response = query.execute();
            Log.d("Saumeel ","After query execute");
            Log.d("Query Result ",response.toString());

            List<SearchResult> results = response.getItems();
            Log.d("Query Result in List ", results.toString());

            // results array, get 5 id

            String videoIds = "";
            for (SearchResult r :results)
                videoIds+=r.getId().getVideoId()+",";

            videoIds=videoIds.substring(0,videoIds.length()-1);
            queryView.setId(videoIds);
            VideoListResponse response2 = queryView.execute();

            List<Video> results2 = response2.getItems();

            for (Video v : results2)
            {
                v.getStatistics().getViewCount();
            }
            Log.d("ViewCount", response2.toString());

            List<VideoItem> items = new ArrayList<VideoItem>();

                for (int i =0;i<results.size();i++){

                Log.d("Saumeel ", "Inside for loop of search of YC");
                VideoItem item = new VideoItem();
                item.setTitle(results.get(i).getSnippet().getTitle());
                item.setDescription(results.get(i).getSnippet().getDescription());
                item.setThumbnailURL(results.get(i).getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(results.get(i).getId().getVideoId());
                item.setViewCount(results2.get(i).getStatistics().getViewCount().toString());    // viewCount converted to String.

                // item.setViewCount(results2);
                /* start of logic to convert to date format from ISO 8601 format */
                // First parse string in pattern "yyyy-MM-dd'T'HH:mm:ss'Z'" to date object.

                String dateString1 = results.get(i).getSnippet().getPublishedAt().toString();

                // Then format date object to string in pattern "MM/dd/yy 'at' h:mma".
                String dateString2 = new SimpleDateFormat("MM/dd/yyyy").format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.kkk'Z'").parse(dateString1));
                System.out.println(dateString2); // 08/24/12
                /* end of logic to convert to date format from ISO 8601 format */

                item.setDate(dateString2);
                items.add(item);
            }

            return items;
        }catch(Exception e){
            Log.d("YC", "Could not search: " + e);
            return null;
        }
    }

}
