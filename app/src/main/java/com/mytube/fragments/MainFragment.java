package com.mytube.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mytube.R;
import com.mytube.helper.PlayerActivity;
import com.mytube.helper.StarTracker;
import com.mytube.helper.YoutubeConnector;
import com.mytube.helper.YoutubePlaylistConnector;
import com.mytube.pojo.VideoItem;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by shivang on 10/17/15.
 */
public class MainFragment extends Fragment {

    private EditText searchInput;
    private ListView videosFound;

    private List<VideoItem> searchResults;
    private Handler handler;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main,container,false);

        searchInput = (EditText)v.findViewById(R.id.search_input);
        videosFound = (ListView)v.findViewById(R.id.videos_found);

        handler = new Handler();

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == 0&&EditorInfo.IME_ACTION_DONE==6){
                    searchOnYoutube(v.getText().toString(), inflater);
                    return false;
                }
                return true;
            }
        });
        addClickListener();

        return v;
    }

    private void searchOnYoutube(final String keywords, final LayoutInflater inflater){

        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(getActivity());
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound(inflater);
                    }
                });
            }
        }.start();
    }

    private void sendToPlaylist(final String selectedVideoId){
        new Thread(){
            public void run(){
                YoutubePlaylistConnector yc = new YoutubePlaylistConnector(getActivity());
                yc.addToPlaylist(selectedVideoId);
            }
        }.start();
    }

    private void removeFromPlaylist(final String selectedVideoId){
        new Thread(){
            public void run(){
                YoutubePlaylistConnector yc = new YoutubePlaylistConnector(getActivity());
                yc.removeFromPlaylist(selectedVideoId);
            }
        }.start();
    }

    private void updateVideosFound(final LayoutInflater inflater){
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity(), R.layout.video_item, searchResults){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = inflater.inflate(R.layout.video_item, parent, false);
                }

                final String selectedVideoId = searchResults.get(position).getId();
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);
                TextView date = (TextView)convertView.findViewById(R.id.video_publishedAt);
                TextView viewCount = (TextView)convertView.findViewById(R.id.video_viewCount);
                final ImageButton unfilledStarIcon = (ImageButton)convertView.findViewById(R.id.star_icon);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                date.setText("Published at : "+searchResult.getDate());
                viewCount.setText(searchResult.getViewCount()+" views");

                final StarTracker s = new StarTracker(getContext()); // keep track of star Icon ! // see if you keep it as an attribute in VideoItem

                unfilledStarIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (s.getImageResource() == R.mipmap.ic_star_unfilled) {
                            s.setImageResource(R.mipmap.ic_star_filled);
                            unfilledStarIcon.setImageResource(R.mipmap.ic_star_filled);
                            Toast.makeText(getActivity(),
                                    "Added to SJSU-CMPE 277 favorites list !", Toast.LENGTH_SHORT).show();
                            sendToPlaylist(selectedVideoId);
                        } else {
                            s.setImageResource(R.mipmap.ic_star_unfilled);
                            unfilledStarIcon.setImageResource(R.mipmap.ic_star_unfilled);
                            removeFromPlaylist(selectedVideoId);
                            Toast.makeText(getActivity(),
                                    "Removed from the SJSU-CMPE 277 favorites list !", Toast.LENGTH_SHORT).show();

                        }

                    }

                });

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
