package com.live.holyquranmp3.quran;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;


public class Sellings extends BaseActivity {


    ArrayList<SettingItem> fullsongpath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.menu);
        }

        fullsongpath.add(new SettingItem(getResources().getString(R.string.Languages), R.drawable.ic_language));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.separeteapp), R.drawable.ic_share));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.rateapp), R.drawable.ic_star));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.about), R.drawable.ic_info));
        fullsongpath.add(new SettingItem(getResources().getString(R.string.privacy_policy), R.drawable.ic_privacy));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyCustomAdapter());
    }

    private void showAboutDialog() {
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(getString(R.string.app_version, version))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void openPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://htmlpreview.github.io/?https://raw.githubusercontent.com/AliMahmoodM/app-privacy-policies/refs/heads/main/quran-recitations-privacy-policy.html"));
        startActivity(browserIntent);
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "العربية", "Română", "Español", "Français", "Deutsch", "Русский", "Italiano"};
        final String[] langCodes = {"en", "ar", "ro", "es", "fr", "de", "ru", "it"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Languages);
        builder.setItems(languages, (dialog, which) -> {
            setLocale(langCodes[which]);
        });
        builder.create().show();
    }

    private void setLocale(String langCode) {
        SaveSettings sv = new SaveSettings(this);
        sv.LanguageSelect = getLanguageCode(langCode);
        sv.SaveData();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Restart the main activity with a clear stack
        Intent intent = new Intent(this, RecitesName.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int getLanguageCode(String lang) {
        switch (lang) {
            case "ar":
                return 1;
            case "ro":
                return 2;
            case "es":
                return 3;
            case "fr":
                return 4;
            case "de":
                return 5;
            case "ru":
                return 6;
            case "it":
                return 7;
            default:
                return 0;
        }
    }

    private void shareApp(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String shareSubject = "Check out this great Quran app!";
        String shareBody = "I'm using this Quran app. You should try it! https://play.google.com/store/apps/details?id=" + context.getPackageName();

        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        context.startActivity(Intent.createChooser(shareIntent, "Share App Using"));
    }

    private void rateApp(Context context) {
        try {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName()));
            context.startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName()));
            context.startActivity(rateIntent);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder> {

        @Override
        public int getItemCount() {
            return fullsongpath.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View myView = LayoutInflater.from(parent.getContext()).inflate(R.layout.settingitem, parent, false);
            return new ViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final SettingItem s = fullsongpath.get(position);
            holder.textView.setText(s.Name);
            holder.img.setImageResource(s.ImageURL);

            holder.itemView.setOnClickListener(v -> {
                try {
                    switch (position) {
                        case 0: // Language
                            showLanguageDialog();
                            break;
                        case 1: // Share App
                            shareApp(Sellings.this);
                            break;
                        case 2: // Rate App
                            rateApp(Sellings.this);
                            break;
                        case 3: // About
                            showAboutDialog();
                            break;
                        case 4: // Privacy Policy
                            openPrivacyPolicy();
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView img;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
                img = itemView.findViewById(R.id.imgchannel);
            }
        }
    }
}
