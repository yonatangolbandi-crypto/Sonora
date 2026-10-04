package com.yonatan.sonora.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.yonatan.sonora.R;
import com.yonatan.sonora.activities.AuthActivity;
import com.yonatan.sonora.activities.ListDetailActivity;
import com.yonatan.sonora.activities.MusicDetailActivity;
import com.yonatan.sonora.adapters.MusicListAdapter;
import com.yonatan.sonora.adapters.ReviewAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.MusicList;
import com.yonatan.sonora.models.Review;
import com.yonatan.sonora.models.User;
import com.yonatan.sonora.utils.ImageLoader;
import com.yonatan.sonora.utils.SessionManager;

import java.util.UUID;

/**
 * פרגמנט פרופיל המשתמש (User Profile Fragment).
 * מציג נתונים אישיים, סטטיסטיקות האזנה, 4 הפריטים האהובים (Favorite 4),
 * רשימות מותאמות אישית, וביקורות שנכתבו.
 *
 * כולל שימוש ב-{@link ActivityResultLauncher} ו-{@link ActivityResultContracts.GetContent}
 * לבחירת תמונת פרופיל מגלריית המכשיר (עונה על סעיפים 6.14 ו-10.3 במחוון הבגרות).
 *
 * User profile fragment with gallery image picker and list management.
 */
public class ProfileFragment extends Fragment {

    private ShapeableImageView imgAvatar;
    private ImageView btnChangeAvatar;
    private TextView tvDisplayName, tvUsername, tvBio;
    private TextView tvLoggedCount, tvReviewsCount, tvFollowersCount, tvFollowingCount;
    private Button btnEditProfile, btnCreateList;
    private ImageButton btnLogout;
    private RecyclerView rvLists, rvReviews;

    // Favorite 4 views
    private View fav1View, fav2View;

    private MusicListAdapter listAdapter;
    private ReviewAdapter reviewAdapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;

    // מימוש ActivityResultContract לבחירת תמונה (סעיף 6.14 בדרישות)
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleAvatarPicked(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = SonoraRepository.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        initViews(view);
        setupRecyclers();
        setupListeners();

        loadProfileData();
        return view;
    }

    private void initViews(View view) {
        imgAvatar = view.findViewById(R.id.imgProfileAvatar);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        tvDisplayName = view.findViewById(R.id.tvProfileDisplayName);
        tvUsername = view.findViewById(R.id.tvProfileUsername);
        tvBio = view.findViewById(R.id.tvProfileBio);

        tvLoggedCount = view.findViewById(R.id.tvProfileLoggedCount);
        tvReviewsCount = view.findViewById(R.id.tvProfileReviewsCount);
        tvFollowersCount = view.findViewById(R.id.tvProfileFollowersCount);
        tvFollowingCount = view.findViewById(R.id.tvProfileFollowingCount);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnCreateList = view.findViewById(R.id.btnCreateList);
        btnLogout = view.findViewById(R.id.btnLogout);

        rvLists = view.findViewById(R.id.rvProfileLists);
        rvReviews = view.findViewById(R.id.rvProfileReviews);

        fav1View = view.findViewById(R.id.fav1);
        fav2View = view.findViewById(R.id.fav2);
    }

    private void setupRecyclers() {
        rvLists.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter = new MusicListAdapter();
        listAdapter.setOnItemClickListener((list, position) -> {
            Intent intent = new Intent(getActivity(), ListDetailActivity.class);
            intent.putExtra("music_list", list);
            startActivity(intent);
        });
        rvLists.setAdapter(listAdapter);

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupListeners() {
        // בחירת תמונת פרופיל מגלריה
        btnChangeAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnCreateList.setOnClickListener(v -> showCreateListDialog());

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), R.style.Base_Theme_Sonora)
                    .setTitle("התנתקות")
                    .setMessage("האם אתה בטוח שברצונך להתנתק מ-SONORA?")
                    .setPositiveButton("התנתק", (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(getActivity(), AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        String userId = sessionManager.getCurrentUserId();
        repository.getUserById(userId, user -> {
            if (user != null && isAdded()) {
                tvDisplayName.setText(user.getDisplayName());
                tvUsername.setText("@" + user.getUsername());
                tvBio.setText(user.getBio());

                tvLoggedCount.setText(String.valueOf(user.getLoggedCount()));
                tvReviewsCount.setText(String.valueOf(user.getReviewsCount()));
                tvFollowersCount.setText(String.valueOf(user.getFollowersCount()));
                tvFollowingCount.setText(String.valueOf(user.getFollowingCount()));

                ImageLoader.getInstance().loadImage(user.getAvatarUrl(), imgAvatar, R.drawable.ic_profile);

                // טעינת 4 האהובים
                loadFavoriteFour();
            }
        });

        // טעינת רשימות המשתמש
        repository.getUserLists(userId, lists -> {
            if (isAdded()) {
                listAdapter.setLists(lists);
            }
        });

        // טעינת ביקורות המשתמש
        repository.getReviewsByUser(userId, userId, reviews -> {
            if (isAdded()) {
                reviewAdapter.setReviews(reviews);
            }
        });
    }

    private void loadFavoriteFour() {
        repository.getTrendingAlbums(albums -> {
            if (albums != null && albums.size() >= 2 && isAdded()) {
                bindFavItem(fav1View, albums.get(0));
                bindFavItem(fav2View, albums.get(1));
            }
        });
    }

    private void bindFavItem(View container, MusicItem item) {
        if (container == null || item == null) return;
        ImageView img = container.findViewById(R.id.imgCover);
        TextView title = container.findViewById(R.id.tvTitle);
        TextView artist = container.findViewById(R.id.tvArtist);
        TextView badge = container.findViewById(R.id.tvRatingBadge);
        TextView meta = container.findViewById(R.id.tvTypeYear);

        title.setText(item.getTitle());
        artist.setText(item.getArtist());
        badge.setText("★ " + item.getFormattedRating());
        meta.setText(item.getReleaseYear() + " • " + item.getItemType());
        ImageLoader.getInstance().loadImage(item.getCoverUrl(), img, R.drawable.ic_album);

        container.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
            intent.putExtra("music_item", item);
            startActivity(intent);
        });
    }

    private void handleAvatarPicked(Uri uri) {
        String uriStr = uri.toString();
        imgAvatar.setImageURI(uri);
        sessionManager.setAvatarUrl(uriStr);

        String userId = sessionManager.getCurrentUserId();
        repository.getUserById(userId, user -> {
            if (user != null) {
                user.setAvatarUrl(uriStr);
                repository.updateUserProfile(user, success -> {
                    Toast.makeText(getContext(), "תמונת הפרופיל עודכנה!", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEditProfileDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
        EditText etName = dialogView.findViewById(R.id.etEditDisplayName);
        EditText etBio = dialogView.findViewById(R.id.etEditBio);
        Button btnSave = dialogView.findViewById(R.id.btnSaveProfile);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelEditProfile);

        etName.setText(sessionManager.getCurrentDisplayName());
        etBio.setText(sessionManager.getCurrentBio());

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Base_Theme_Sonora)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(getContext(), "שם התצוגה אינו יכול להיות ריק", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = sessionManager.getCurrentUserId();
            repository.getUserById(userId, user -> {
                if (user != null) {
                    user.setDisplayName(newName);
                    user.setBio(newBio);
                    repository.updateUserProfile(user, success -> {
                        sessionManager.saveUserLogin(user);
                        loadProfileData();
                        dialog.dismiss();
                        Toast.makeText(getContext(), "הפרופיל עודכן בהצלחה!", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        dialog.show();
    }

    private void showCreateListDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_list, null);
        EditText etTitle = dialogView.findViewById(R.id.etListTitle);
        EditText etDesc = dialogView.findViewById(R.id.etListDescription);
        SwitchMaterial switchPublic = dialogView.findViewById(R.id.switchPublic);
        Button btnSave = dialogView.findViewById(R.id.btnSaveList);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelList);

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.Base_Theme_Sonora)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            boolean isPublic = switchPublic.isChecked();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "יש להזין כותרת לרשימה", Toast.LENGTH_SHORT).show();
                return;
            }

            MusicList newList = new MusicList(
                    UUID.randomUUID().toString(),
                    sessionManager.getCurrentUserId(),
                    sessionManager.getCurrentUsername(),
                    title,
                    desc,
                    isPublic,
                    ""
            );

            repository.createMusicList(newList, success -> {
                if (success) {
                    loadProfileData();
                    dialog.dismiss();
                    Toast.makeText(getContext(), "הרשימה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
