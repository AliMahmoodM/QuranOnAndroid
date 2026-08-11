package com.live.holyquranmp3.quran;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class AyaList extends BaseActivity implements LnaguageClass.SurahListListener {
    RecyclerView listAya;
    public static List<AuthorClass> listrecitesAya = new ArrayList<>();
    static String RecitesName = "";
    static String RecitesRealName = "";
    private int savedLanguage;
    private VivzAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load language setting FIRST
        SaveSettings sv = new SaveSettings(this);
        sv.LoadData();
        savedLanguage = sv.LanguageSelect;

        setContentView(R.layout.activity_aya_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            RecitesName = intent.getStringExtra("RecitesName");
            if (RecitesName == null) RecitesName = "";
            RecitesRealName = intent.getStringExtra("RecitesRealName");
            if (RecitesRealName == null) RecitesRealName = "";
        } else {
            RecitesName = "";
            RecitesRealName = "";
            Log.w("AyaList", "No RecitesName or RecitesRealName extra provided to AyaList");
            Toast.makeText(this, "No reciter selected", Toast.LENGTH_SHORT).show();
        }

        if (getSupportActionBar() != null) {
            if (RecitesRealName.length() > 0)
                getSupportActionBar().setTitle(RecitesRealName);
            else
                getSupportActionBar().setTitle(R.string.title_activity_aya_list);
        }

        listAya = findViewById(R.id.recyclerView);
        listAya.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VivzAdapter(new ArrayList<>()); // Start with an empty list
        listAya.setAdapter(adapter);

        // Load the list asynchronously
        LnaguageClass.GuranAyaAsync(RecitesName, savedLanguage, this);
    }

    @Override
    public void onSurahListReady(List<AuthorClass> surahs) {
        listrecitesAya.clear();
        listrecitesAya.addAll(surahs);
        adapter.updateData(listrecitesAya);
    }


    @Override
    protected void onResume() {
        super.onResume();
        SaveSettings sv = new SaveSettings(this);
        sv.LoadData();
        if (savedLanguage != sv.LanguageSelect) {
            recreate();
        }
    }

    private void DisplayAya(int position) {
        Intent intent = new Intent(this, managerdb.class);
        intent.putExtra("RecitesName", RecitesName);
        intent.putExtra("RecitesAYA", String.valueOf(position));
        intent.putExtra("LanguageSelect", savedLanguage);
        startActivity(intent);
    }

    SearchView searchView;
    Menu myMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_aya_list, menu);
        myMenu = menu;
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<AuthorClass> listrecitestemp = new ArrayList<>();
                for (AuthorClass listrecitesitem : listrecitesAya) {
                    if (listrecitesitem.RealName.toLowerCase().contains(newText.toLowerCase())) {
                        listrecitestemp.add(listrecitesitem);
                    }
                }
                adapter.updateData(listrecitestemp);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.gbackmenu) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    class VivzAdapter extends RecyclerView.Adapter<VivzAdapter.ViewHolder> {

        private List<AuthorClass> listrecitesLocal;

        VivzAdapter(List<AuthorClass> listrecites) {
            this.listrecitesLocal = listrecites != null ? new ArrayList<>(listrecites) : new ArrayList<>();
        }

        void updateData(List<AuthorClass> newData) {
            this.listrecitesLocal.clear();
            if (newData != null) {
                this.listrecitesLocal.addAll(newData);
            }
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView title;

            public ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textView1);
            }
        }

        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_surah, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final AuthorClass temp = listrecitesLocal.get(position);
            holder.title.setText((position + 1) + ". " + temp.RealName);

            holder.itemView.setOnClickListener(v -> {
                int originalPosition = listrecitesAya.indexOf(temp);
                DisplayAya(originalPosition);
            });
        }

        @Override
        public int getItemCount() {
            return listrecitesLocal.size();
        }
    }
}
