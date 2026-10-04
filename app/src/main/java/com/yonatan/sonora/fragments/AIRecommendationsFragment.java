package com.yonatan.sonora.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.activities.MusicDetailActivity;
import com.yonatan.sonora.adapters.RecommendationAdapter;
import com.yonatan.sonora.ai.SonoraAIEngine;
import com.yonatan.sonora.models.Recommendation;
import com.yonatan.sonora.utils.SessionManager;

/**
 * פרגמנט בינה מלאכותית והמלצות מוזיקליות (Sonora AI Fragment).
 * עונה על דרישת "שילוב AI" במלואה וממחיש מנוע המלצות המבוסס על היסטוריית האזנות ודירוגים.
 *
 * AI Recommendations fragment.
 */
public class AIRecommendationsFragment extends Fragment {

    private EditText etMoodPrompt;
    private Button btnAskAi;
    private TextView presetNight, presetClassicRock, presetEnergy;
    private TextView tvSectionHeader;
    private ProgressBar progressBar;
    private RecyclerView rvRecommendations;

    private RecommendationAdapter adapter;
    private SonoraAIEngine aiEngine;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        aiEngine = SonoraAIEngine.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        initViews(view);
        setupRecycler();
        setupListeners();

        loadPersonalizedRecommendations();
        return view;
    }

    private void initViews(View view) {
        etMoodPrompt = view.findViewById(R.id.etAiMoodPrompt);
        btnAskAi = view.findViewById(R.id.btnAskAi);
        presetNight = view.findViewById(R.id.presetNight);
        presetClassicRock = view.findViewById(R.id.presetClassicRock);
        presetEnergy = view.findViewById(R.id.presetEnergy);
        tvSectionHeader = view.findViewById(R.id.tvAiSectionHeader);
        progressBar = view.findViewById(R.id.progressAi);
        rvRecommendations = view.findViewById(R.id.rvAiRecommendations);
    }

    private void setupRecycler() {
        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecommendationAdapter();
        adapter.setOnItemClickListener((rec, position) -> {
            if (rec.getMusicItem() != null) {
                Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
                intent.putExtra("music_item", rec.getMusicItem());
                startActivity(intent);
            }
        });
        rvRecommendations.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAskAi.setOnClickListener(v -> {
            String prompt = etMoodPrompt.getText().toString().trim();
            if (prompt.isEmpty()) {
                Toast.makeText(getContext(), "נא להקליד בקשה למנוע ה-AI", Toast.LENGTH_SHORT).show();
                return;
            }
            executeMoodAi(prompt);
        });

        presetNight.setOnClickListener(v -> {
            etMoodPrompt.setText("לילה שקט באוזניות");
            executeMoodAi("לילה שקט באוזניות");
        });

        presetClassicRock.setOnClickListener(v -> {
            etMoodPrompt.setText("רוק קלאסי מורכב");
            executeMoodAi("רוק קלאסי מורכב");
        });

        presetEnergy.setOnClickListener(v -> {
            etMoodPrompt.setText("אנרגטי לריצה ואימון");
            executeMoodAi("אנרגטי לריצה ואימון");
        });
    }

    private void loadPersonalizedRecommendations() {
        progressBar.setVisibility(View.VISIBLE);
        tvSectionHeader.setText("🎯 המלצות מובילות מותאמות אישית לפרופיל שלך");

        String userId = sessionManager.getCurrentUserId();
        aiEngine.generatePersonalizedRecommendations(userId, recs -> {
            if (isAdded()) {
                progressBar.setVisibility(View.GONE);
                adapter.setRecommendations(recs);
            }
        });
    }

    private void executeMoodAi(String prompt) {
        progressBar.setVisibility(View.VISIBLE);
        tvSectionHeader.setText("🤖 המלצות AI עבור: \"" + prompt + "\"");

        aiEngine.generateMoodRecommendations(prompt, recs -> {
            if (isAdded()) {
                progressBar.setVisibility(View.GONE);
                adapter.setRecommendations(recs);
            }
        });
    }
}
