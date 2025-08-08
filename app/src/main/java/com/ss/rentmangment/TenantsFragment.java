package com.ss.rentmangment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TenantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTenant;
    private TenantAdapter adapter;
    private List<Tenant> tenantList = new ArrayList<>();
    private DatabaseReference usersRef;
    private String adminMobile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tenants, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewTenants);
        fabAddTenant = view.findViewById(R.id.fabAddTenant);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TenantAdapter(getContext(), tenantList);
        recyclerView.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get admin mobile from session
        SharedPreferences sp = getContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        adminMobile = sp.getString("mobile", "");

        fabAddTenant.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddTenantActivity.class))
        );

        loadTenants();
        return view;
    }

    private void loadTenants() {
        usersRef.child(adminMobile).child("tenants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tenantList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Tenant tenant = ds.getValue(Tenant.class);
                            if (tenant != null) {
                                tenantList.add(tenant);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load tenants", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
