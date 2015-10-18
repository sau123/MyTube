package com.mytube.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mytube.R;
import com.mytube.helper.PlayerActivity;
import com.mytube.helper.YoutubePlaylistConnector;
import com.mytube.pojo.VideoItem;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by shivang on 10/17/15.
 */
public class PlaylistFragment extends Fragment {


    private ListView videosFound;

    private List<VideoItem> searchResults;
    private Handler handler;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist,container,false);

        videosFound = (ListView)v.findViewById(R.id.videos_found);

        handler = new Handler();

        searchOnYoutube(inflater);
        addClickListener();
        return v;
    }

    private void searchOnYoutube(final LayoutInflater inflater){

        new Thread(){
            public void run(){
                YoutubePlaylistConnector ypc = new YoutubePlaylistConnector(getActivity());
                searchResults = ypc.createPlaylist("SJSU-CMPE-277");
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound(inflater);
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(final LayoutInflater inflater){
        if(searchResults == null || searchResults.size() == 0){
            return;
        }
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity(), R.layout.video_item, searchResults){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = inflater.inflate(R.layout.video_item, parent, false);
                }

                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);
                TextView date = (TextView)convertView.findViewById(R.id.video_publishedAt);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_viewCount);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                date.setText("Published at : "+searchResult.getDate());
                viewCount.setText(searchResult.getViewCount()+" views");

                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void addClickListener(){
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });
    }
}
