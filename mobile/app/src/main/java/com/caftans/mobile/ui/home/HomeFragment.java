package com.caftans.mobile.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.caftans.mobile.R;
import com.caftans.mobile.data.api.ApiClient;
import com.caftans.mobile.data.api.ApiService;
import com.caftans.mobile.data.models.ApiResponse;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.ui.caftan.CaftanAdapter;
import com.caftans.mobile.ui.caftan.CaftanDetailActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPagerHero;
    private RecyclerView rvRecommended;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewPagerHero = view.findViewById(R.id.viewPagerHero);
        rvRecommended = view.findViewById(R.id.rvRecommended);
        apiService = ApiClient.getClient().create(ApiService.class);

        setupHeroSection();
        setupQuickAccess(view);
        setupRecommendedSection();

        return view;
    }

    private void setupHeroSection() {
        List<HeroItem> heroItems = new ArrayList<>();
        // Dummy data for now, ideally fetch from backend or separate config
        heroItems.add(new HeroItem("New Summer Collection", "Light and breezy caftans for the summer.",
                "https://images.unsplash.com/photo-1589810635657-23294847e66d?q=80&w=2070&auto=format&fit=crop",
                "Explore Now"));
        heroItems.add(new HeroItem("Wedding Season", "Elegant designs for your special day.",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=1888&auto=format&fit=crop",
                "Book Now"));
        heroItems.add(new HeroItem("Traditional Vibes", "Going back to the roots with classic styles.",
                "https://plus.unsplash.com/premium_photo-1664360677158-69325c4efc96?q=80&w=2070&auto=format&fit=crop",
                "View Collection"));

        HeroAdapter heroAdapter = new HeroAdapter(heroItems, item -> {
            // Navigate to categories for now
            if (getActivity() instanceof com.caftans.mobile.ui.main.MainActivity) {
                // Ideally switch tab or open category
                Toast.makeText(getContext(), "Navigating to: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                // Simulating navigation to 'Categories' via bottom nav for demonstration if
                // needed,
                // or just open CategoriesFragment
                ((com.caftans.mobile.ui.main.MainActivity) getActivity()).navigateToCategories();
            }
        });
        viewPagerHero.setAdapter(heroAdapter);
    }

    private void setupQuickAccess(View view) {
        view.findViewById(R.id.cardExplore).setOnClickListener(
                v -> ((com.caftans.mobile.ui.main.MainActivity) getActivity()).navigateToCategories());

        view.findViewById(R.id.cardCart).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.caftans.mobile.ui.cart.CartActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.cardHistory).setOnClickListener(
                v -> ((com.caftans.mobile.ui.main.MainActivity) getActivity()).navigateToReservations());

        view.findViewById(R.id.cardProfile)
                .setOnClickListener(v -> ((com.caftans.mobile.ui.main.MainActivity) getActivity()).navigateToProfile());
    }

    private void setupRecommendedSection() {
        // Fetch random or latest caftans
        ApiService apiService = ApiClient.getApiService();

        apiService.getCaftans(null, null, null, null, null, null).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Caftan> allCaftans = response.body().getCaftans();
                    // Just take first 5 as "Recommended" for now
                    List<Caftan> recommended = allCaftans.size() > 5 ? allCaftans.subList(0, 5) : allCaftans;

                    CaftanAdapter adapter = new CaftanAdapter(recommended, caftan -> {
                        Intent intent = new Intent(getActivity(), CaftanDetailActivity.class);
                        intent.putExtra("caftan_id", caftan.getId());
                        startActivity(intent);
                    });
                    rvRecommended.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Handle failure silently or show error
            }
        });
    }
}
