package com.live.holyquranmp3.quran;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.live.holyquranmp3.quran.databinding.ActivityRecitesNameBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator; // Added to fix build error
import java.util.List;

public class RecitesName extends BaseActivity {
    public List<AuthorClass> listrecites;
    ReciterAdapter adapter;

    String RecitesName = "";
    String RecitesRealName = ""; // To hold the full name
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private boolean doubleBackToExitPressedOnce = false;
    private ActivityRecitesNameBinding binding;
    private boolean isSorted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRecitesNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_recites_name);
        }

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listrecites = new LnaguageClass().getAuthorList(this);
        adapter = new ReciterAdapter(this, listrecites);
        binding.recyclerView.setAdapter(adapter);

        // Request permissions on launch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finish();
                    return;
                }

                doubleBackToExitPressedOnce = true;
                Toast.makeText(RecitesName.this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();

                new Handler(android.os.Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_ASK_PERMISSIONS);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    void ListAya() {
        try {
            if (RecitesName.length() > 1) {
                Intent intent = new Intent(this, AyaList.class);
                intent.putExtra("RecitesName", RecitesName);
                intent.putExtra("RecitesRealName", RecitesRealName);
                startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recites_name, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu) {
            Intent intent = new Intent(this, Sellings.class);
            startActivity(intent);
        } else if (id == R.id.action_sort) {
            sortReciters();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortReciters() {
        isSorted = !isSorted;
        if (isSorted) {
            // Create a new, modifiable list from the original data to sort it
            List<AuthorClass> sortedList = new ArrayList<>(listrecites);
            Collections.sort(sortedList, Comparator.comparing(o -> o.RealName));
            adapter.updateData(sortedList);
        } else {
            // Restore the original, unsorted list
            adapter.updateData(listrecites);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class ReciterAdapter extends RecyclerView.Adapter<ReciterAdapter.ViewHolder> implements Filterable {

        private final Context context;
        private List<AuthorClass> recitersList; // The original, unfiltered and unsorted list
        private List<AuthorClass> recitersListFiltered; // The list that is currently displayed

        public ReciterAdapter(Context context, List<AuthorClass> recitersList) {
            this.context = context;
            this.recitersList = recitersList;
            this.recitersListFiltered = new ArrayList<>(recitersList);
        }

        public void updateData(List<AuthorClass> newRecitersList) {
            // This will update the list that the filter uses, and then refresh the displayed list
            this.recitersList = newRecitersList;
            this.recitersListFiltered = new ArrayList<>(newRecitersList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.recites_ticket, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuthorClass author = recitersListFiltered.get(position);
            holder.txtRecitesName.setText(author.RealName);

            holder.itemView.setOnClickListener(v -> {
                RecitesName = author.ServerName;
                RecitesRealName = author.RealName;
                ListAya();
            });
        }

        @Override
        public int getItemCount() {
            return recitersListFiltered.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        recitersListFiltered = new ArrayList<>(recitersList);
                    } else {
                        ArrayList<AuthorClass> filteredList = new ArrayList<>();
                        for (AuthorClass row : recitersList) {
                            if (row.RealName.toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                        recitersListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = recitersListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    recitersListFiltered = (ArrayList<AuthorClass>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtRecitesName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                txtRecitesName = itemView.findViewById(R.id.txtRecitesName);
            }
        }
    }
}
