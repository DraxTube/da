package com.streamapp.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private EpisodeAdapter adapter;
    private TextView tvSeriesTitle;
    private TextView tvSeriesDescription;
    private View featuredOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        tvSeriesTitle = findViewById(R.id.tvSeriesTitle);
        tvSeriesDescription = findViewById(R.id.tvSeriesDescription);
        featuredOverlay = findViewById(R.id.featuredOverlay);

        // Setup RecyclerView
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);

        List<Integer> seasons = EpisodeLoader.getSeasons(this);

        // Featured header info
        tvSeriesTitle.setText("Batman: The Brave and the Bold");
        tvSeriesDescription.setText("3 Stagioni • 65 Episodi • Animazione");

        // Setup season tabs
        for (int season : seasons) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText("Stagione " + season);
            tabLayout.addTab(tab);
        }

        // Initial load - Season 1
        List<Episode> firstSeason = EpisodeLoader.getBySeason(this, seasons.get(0));
        adapter = new EpisodeAdapter(this, firstSeason, episode -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("link", episode.getLink());
            intent.putExtra("title", episode.getTitle());
            intent.putExtra("number", episode.getNumber());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int idx = tab.getPosition();
                int season = seasons.get(idx);
                adapter.updateEpisodes(EpisodeLoader.getBySeason(MainActivity.this, season));
                recyclerView.scrollToPosition(0);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
