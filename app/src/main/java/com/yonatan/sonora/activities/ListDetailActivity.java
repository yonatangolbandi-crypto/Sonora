package com.yonatan.sonora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.adapters.MusicItemAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.MusicList;

import java.util.List;

/**
 * מסך פירוט רשימת מוזיקה (List Detail Activity).
 * מציג את כל השירים והאלבומים המשויכים לרשימה מסוימת.
 *
 * Music list detail view.
 */
public class ListDetailActivity extends AppCompatActivity {

    private MusicList musicList;
    private ImageButton btnBack;
    private TextView tvHeader, tvTitle, tvAuthor, tvDesc;
    private RecyclerView rvItems;
    private MusicItemAdapter adapter;
    private SonoraRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_detail);

        repository = SonoraRepository.getInstance(this);

        musicList = (MusicList) getIntent().getSerializableExtra("music_list");
        if (musicList == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadListItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnListDetailBack);
        tvHeader = findViewById(R.id.tvListDetailHeaderTitle);
        tvTitle = findViewById(R.id.tvListTitleMain);
        tvAuthor = findViewById(R.id.tvListAuthorMain);
        tvDesc = findViewById(R.id.tvListDescMain);
        rvItems = findViewById(R.id.rvListItems);

        tvHeader.setText(musicList.getTitle());
        tvTitle.setText(musicList.getTitle());
        tvAuthor.setText("נוצר על ידי " + musicList.getUsername());
        tvDesc.setText(musicList.getDescription());

        rvItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MusicItemAdapter(MusicItemAdapter.VIEW_TYPE_LINEAR);
        adapter.setOnItemClickListener((item, position) -> {
            Intent intent = new Intent(this, MusicDetailActivity.class);
            intent.putExtra("music_item", item);
            startActivity(intent);
        });
        rvItems.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadListItems() {
        repository.getMusicItemsForList(musicList, items -> {
            adapter.setItems(items);
            if (items.isEmpty()) {
                Toast.makeText(this, "אין עדיין פריטים ברשימה זו", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
