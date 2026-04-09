package com.caftans.mobile.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.caftans.mobile.R;
import com.caftans.mobile.data.models.Caftan;
import com.caftans.mobile.data.models.Reservation;
import com.caftans.mobile.data.room.AppDatabase;
import com.caftans.mobile.data.room.CartItem;
import com.caftans.mobile.data.room.CartDao;
import com.caftans.mobile.ui.payment.PaymentActivity;
import com.caftans.mobile.ui.reservation.ReservationAdapter;
import com.caftans.mobile.utils.TokenManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ReservationAdapter adapter;
    private List<Reservation> cartReservations = new ArrayList<>();
    private TokenManager tokenManager;
    private TextView tvTotal;
    private Button btnPay;
    private CartDao cartDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Mon Panier");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tokenManager = TokenManager.getInstance(this);
        cartDao = AppDatabase.getInstance(this).cartDao();

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvTotal = findViewById(R.id.tvTotal);
        btnPay = findViewById(R.id.btnPay);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(cartReservations);
        adapter.setOnDeleteClickListener(this::removeReservationFromCart);
        recyclerView.setAdapter(adapter);

        btnPay.setOnClickListener(v -> processPayment());

        // Hide progress bar as local load is instant
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCartItems() {
        cartReservations.clear();
        List<CartItem> items = cartDao.getAll();

        for (CartItem item : items) {
            Reservation r = new Reservation();
            r.setId(item.id); // Storing local Room ID in reservation ID for deletion mapping
            r.setCaftanId(item.caftanId);
            r.setStartDate(item.startDate);
            r.setEndDate(item.endDate);
            r.setNotes(item.notes);
            r.setStatus("pending");

            Caftan c = new Caftan();
            c.setId(item.caftanId);
            c.setName(item.caftanName);
            c.setImageUrl(item.caftanImage);
            c.setPricePerDay(item.pricePerDay);
            r.setCaftan(c);

            cartReservations.add(r);
        }

        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private void removeReservationFromCart(Reservation reservation, int position) {
        // Here reservation.getId() corresponds to CartItem.id because of how we mapped
        // it in loadCartItems
        CartItem itemToDelete = new CartItem(0, null, null, 0, null, null, null);
        itemToDelete.id = reservation.getId();
        cartDao.delete(itemToDelete);

        cartReservations.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, cartReservations.size());
        updateTotal();
        Toast.makeText(this, "Article retiré du panier", Toast.LENGTH_SHORT).show();
    }

    private void updateTotal() {
        double total = adapter.getTotalPrice(); // This iterates and calculates based on Caftan price in Reservation
                                                // object

        NumberFormat format = NumberFormat.getNumberInstance(Locale.getDefault());
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        tvTotal.setText(format.format(total) + " MAD");
        btnPay.setEnabled(!cartReservations.isEmpty() && total > 0);
    }

    private void processPayment() {
        double total = adapter.getTotalPrice();
        int count = cartReservations.size();

        if (count == 0 || total == 0) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check login before payment?
        if (tokenManager.getAuthHeader() == null) {
            Toast.makeText(this, "Veuillez vous connecter pour procéder au paiement", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, com.caftans.mobile.ui.auth.LoginActivity.class);
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("total_amount", total);
        intent.putExtra("reservation_count", count);
        startActivity(intent);
    }
}
