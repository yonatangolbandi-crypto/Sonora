package com.yonatan.sonora.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.activities.MusicDetailActivity;
import com.yonatan.sonora.adapters.DiaryAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.DiaryEntry;
import com.yonatan.sonora.utils.SessionManager;

import java.util.List;
import java.util.Locale;

/**
 * פרגמנט יומן מוזיקה (Music Diary Fragment).
 * מציג את כל האלבומים והשירים שהמשתמש תיעד ביומן, כולל תאריכים, דירוגים ותגים.
 *
 * Listening diary fragment.
 */
public class DiaryFragment extends Fragment {

    private TextView tvTotalCount;
    private TextView tvAvgRating;
    private TextView tvEmptyDiary;
    private RecyclerView rvDiary;

    private DiaryAdapter adapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        repository = SonoraRepository.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        tvTotalCount = view.findViewById(R.id.tvDiaryTotalCount);
        tvAvgRating = view.findViewById(R.id.tvDiaryAvgRating);
        tvEmptyDiary = view.findViewById(R.id.tvEmptyDiary);
        rvDiary = view.findViewById(R.id.rvDiary);

        setupRecycler();
        loadDiary();
        return view;
    }

    private void setupRecycler() {
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DiaryAdapter();
        adapter.setOnItemClickListener((entry, position) -> {
            repository.getMusicItemById(entry.getMusicItemId(), item -> {
                if (item != null && isAdded()) {
                    Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
                    intent.putExtra("music_item", item);
                    startActivity(intent);
                }
            });
        });
        rvDiary.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiary();
    }

    private void loadDiary() {
        String userId = sessionManager.getCurrentUserId();
        repository.getUserDiary(userId, entries -> {
            if (isAdded()) {
                adapter.setEntries(entries);
                tvEmptyDiary.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                updateStats(entries);
            }
        });
    }

    private void updateStats(List<DiaryEntry> entries) {
        tvTotalCount.setText(String.valueOf(entries.size()));

        if (entries.isEmpty()) {
            tvAvgRating.setText("—");
            return;
        }

        double sum = 0.0;
        int count = 0;
        for (DiaryEntry e : entries) {
            if (e.getRating() > 0) {
                sum += e.getRating();
                count++;
            }
        }

        if (count > 0) {
            double avg = sum / count;
            tvAvgRating.setText(String.format(Locale.US, "%.1f", avg));
        } else {
            tvAvgRating.setText("—");
        }
    }
}
