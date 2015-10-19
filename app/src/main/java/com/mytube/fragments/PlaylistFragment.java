package com.mytube.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mytube.R;
import com.mytube.helper.CONSTANTS;
import com.mytube.helper.PlayerActivity;
import com.mytube.helper.YoutubePlaylistConnector;
import com.mytube.pojo.VideoItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by shivang on 10/17/15.
 */
public class PlaylistFragment extends Fragment {

    private final String TAG = "PlaylistFragment";
    private ListView videosFound;

    private List<VideoItem> searchResults;
    private Handler handler;
    final private ArrayList<String> videosSelected = new ArrayList<>();
    MenuItem fav;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist,container,false);
        setHasOptionsMenu(true);
        videosFound = (ListView)v.findViewById(R.id.videos_found);

        handler = new Handler();

        searchOnYoutube(inflater);
        addClickListener();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(new Runnable() {
            public void run() {
                searchOnYoutube(getActivity().getLayoutInflater());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        fav = menu.add("Delete");
        fav.setIcon(R.mipmap.ic_delete);
        Log.d(TAG, "onCreateOPtionMenu");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.toString().equalsIgnoreCase("Delete")){
            for(String video : videosSelected){
                Log.d(TAG, "delete: " + video);
                removeFromPlaylist(video);
            }
            videosSelected.clear();
        }

        return false;
    }
    private void searchOnYoutube(final LayoutInflater inflater){
        new Thread(){
            public void run(){
                YoutubePlaylistConnector ypc = new YoutubePlaylistConnector(getActivity());
                searchResults = ypc.getPlaylistVideos(ypc.createPlaylist(CONSTANTS.PLAYLIST_NAME));
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound(inflater);
                    }
                });
            }
        }.start();
    }

    private void removeFromPlaylist(final String selectedVideoId){
        new Thread(){
            public void run(){
                YoutubePlaylistConnector yc = new YoutubePlaylistConnector(getActivity());
                yc.removeFromPlaylist(selectedVideoId);

                handler.post(new Runnable() {
                    public void run() {
                        searchOnYoutube(getActivity().getLayoutInflater());
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(final LayoutInflater inflater){
        if(searchResults == null || searchResults.size() == 0){
            Log.d(TAG, "updateVideo: null" );
            videosFound.setAdapter(null);
            return;
        }
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity(), R.layout.video_item, searchResults){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = inflater.inflate(R.layout.playlist_video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView date = (TextView)convertView.findViewById(R.id.video_publishedAt);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_viewCount);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                date.setText("Published at : "+searchResult.getDate());
                viewCount.setText(searchResult.getViewCount()+" views");
                final String playListVideoId = searchResult.getGlobalId();
                CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkbox);
                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                      @Override
                      public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                            if(isChecked)
                                videosSelected.add(playListVideoId);
                            else
                                videosSelected.remove(playListVideoId);
                      }
                  }
                );

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
