package com.example.pyc.myapplication.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.pyc.myapplication.BackupAndRestore;
import com.example.pyc.myapplication.R;

public class Settings extends PreferenceFragment {

    private static final int RECORD_REQUEST_CODE = 101;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Настройки");

        addPreferencesFromResource(R.xml.preference);

        //BackupAndRestore.exportDB(getActivity());
        //BackupAndRestore.importDB(getActivity());

        //запрос прав
        makeRequestPermission();

        //кнопка Импорта записей
        Preference buttonImport = findPreference(getString(R.string.importDB));
        buttonImport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            //переменная для проверки двойного нажатия
            private  boolean doubleTapPressedOnce=false;

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (!doubleTapPressedOnce) {
                    this.doubleTapPressedOnce = true;
                    Toast.makeText(getActivity(), R.string.warning_all_erase, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doubleTapPressedOnce = false;
                        }
                    }, 2500);

                } else {
                    //ссылка на меотод с контекстом
                    BackupAndRestore.importDB(getActivity());

                    //перезапус приложения чтобы обновить базу и записи?
                    //getActivity().recreate();

                }
                return true;
            }
        });

        //кнопка Экспорта записей
        Preference buttonExport = findPreference(getString(R.string.exportDB));
        buttonExport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //ссылка на меотод с контекстом
                BackupAndRestore.exportDB(getActivity());
                return true;
            }
        });
    }

    protected void makeRequestPermission() {
        int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_REQUEST_CODE);
        }
    }


}
