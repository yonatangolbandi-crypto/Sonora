package com.yonatan.sonora.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.yonatan.sonora.R;
import com.yonatan.sonora.activities.MusicDetailActivity;
import com.yonatan.sonora.adapters.MusicItemAdapter;
import com.yonatan.sonora.api.ItunesApiClient;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.MusicItem;

import java.util.List;

/**
 * פרגמנט חיפוש וגילוי מוזיקה (Discover & Search Fragment).
 * מאפשר חיפוש שירים, אלבומים ואמנים הן בקטלוג המקומי והן באמצעות חיבור אינטרנטי
 * ל-iTunes REST API (עונה על סעיף 6.2 במחוון הבגרות).
 *
 * Search and discover fragment.
 */
public class DiscoverFragment extends Fragment {

    private EditText etSearch;
    private ImageView btnClear;
    private ChipGroup chipGroup;
    private Chip chipAll, chipAlbums, chipSongs, chipArtists;
    private TextView tvHeader;
    private TextView btnApiToggle;
    private ProgressBar progressBar;
    private RecyclerView rvResults;

    private MusicItemAdapter adapter;
    private SonoraRepository repository;
    private ItunesApiClient apiClient;

    private String currentFilter = "all";
    private boolean isApiMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        repository = SonoraRepository.getInstance(requireContext());
        apiClient = ItunesApiClient.getInstance();

        initViews(view);
        setupRecycler();
        setupListeners();

        loadInitialRecommendations();
        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.etSearchQuery);
        btnClear = view.findViewById(R.id.btnClearSearch);
        chipGroup = view.findViewById(R.id.chipGroupFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipAlbums = view.findViewById(R.id.chipAlbums);
        chipSongs = view.findViewById(R.id.chipSongs);
        chipArtists = view.findViewById(R.id.chipArtists);
        tvHeader = view.findViewById(R.id.tvResultsHeader);
        btnApiToggle = view.findViewById(R.id.btnApiSearchToggle);
        progressBar = view.findViewById(R.id.progressSearch);
        rvResults = view.findViewById(R.id.rvDiscoverResults);
    }

    private void setupRecycler() {
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MusicItemAdapter(MusicItemAdapter.VIEW_TYPE_LINEAR);
        adapter.setOnItemClickListener((item, position) -> {
            // שמירת הפריט למסד הנתונים כדי שנוכל לדרג אותו
            repository.saveMusicItem(item);

            Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
            intent.putExtra("music_item", item);
            startActivity(intent);
        });
        rvResults.setAdapter(adapter);
    }

    private void setupListeners() {
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            loadInitialRecommendations();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() == 0) {
                    loadInitialRecommendations();
                } else if (!isApiMode) {
                    performLocalSearch(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                executeSearch();
                return true;
            }
            return false;
        });

        // החלפה בין חיפוש מקומי לחיפוש רשת API
        btnApiToggle.setOnClickListener(v -> {
            isApiMode = !isApiMode;
            if (isApiMode) {
                btnApiToggle.setText("חיפוש מקומי בלבד 💾");
                btnApiToggle.setBackgroundResource(R.drawable.bg_rounded_card);
                Toast.makeText(getContext(), "מצב חיפוש ברשת (iTunes API) פעיל!", Toast.LENGTH_SHORT).show();
            } else {
                btnApiToggle.setText("חפש ברשת (iTunes API) 🌐");
                btnApiToggle.setBackgroundResource(R.drawable.bg_badge_ai);
            }
            executeSearch();
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipAlbums)) {
                currentFilter = "album";
            } else if (checkedIds.contains(R.id.chipSongs)) {
                currentFilter = "song";
            } else if (checkedIds.contains(R.id.chipArtists)) {
                currentFilter = "artist";
            } else {
                currentFilter = "all";
            }
            executeSearch();
        });
    }

    private void executeSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            loadInitialRecommendations();
            return;
        }

        if (isApiMode) {
            performApiSearch(query);
        } else {
            performLocalSearch(query);
        }
    }

    private void performLocalSearch(String query) {
        tvHeader.setText("תוצאות חיפוש עבור: " + query);
        repository.searchLocalMusic(query, currentFilter, items -> {
            if (isAdded()) {
                adapter.setItems(items);
            }
        });
    }

    private void performApiSearch(String query) {
        tvHeader.setText("תוצאות חיפוש ברשת (iTunes API):");
        progressBar.setVisibility(View.VISIBLE);

        apiClient.searchMusic(query, currentFilter, new ItunesApiClient.ApiCallback<List<MusicItem>>() {
            @Override
            public void onSuccess(List<MusicItem> result) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    adapter.setItems(result);
                    if (result.isEmpty()) {
                        tvHeader.setText("לא נמצאו תוצאות ברשת עבור: " + query);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "שגיאה בחיפוש ברשת: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadInitialRecommendations() {
        tvHeader.setText("אלבומים מומלצים בקהילה");
        repository.getTrendingAlbums(albums -> {
            if (isAdded()) {
                adapter.setItems(albums);
            }
        });
    }
}
