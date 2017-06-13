package com.example.pyc.myapplication.fragments;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.example.pyc.myapplication.MainActivity;

public class DBHelper extends SQLiteOpenHelper {

    final String LOG_TAG = "MyLog";

    // характеристики базы данных
    public static final String DATABASE_NAME = "mydatabase.db";
    public static final int DATABASE_VERSION = 3;

    // название таблиц в базе
    public static final String DATABASE_TABLE = "data_time_description";
    public static final String FAVAORITE_TABLE = "favorite_phrase";

    // названия столбцов для основной таблицы data_time_description
    public static final String DATA = "date";
    public static final String TIME = "time";
    public static final String DESCRIPTION = "description";
    public static final String COLOR = "color";

    // названия столбцов для таблицы favorite_phrase
    public static final String PHRASE = "phrase";

    // создание основной таблицы с названиями столбцов
    public static final String DATABASE_CREATE_SCRIPT = "create table "
            + DATABASE_TABLE + " (" + "id integer primary key autoincrement,"
            + DATA + " text,"
            + TIME + " text,"
            + COLOR + " text,"
            + DESCRIPTION + " text);";

    // создание таблицы избранных фраз
    public static final String FAVAORITE_TABLE_CREATE_SCRIPT = "create table "
            + FAVAORITE_TABLE + " (" + "id integer primary key autoincrement,"
            + PHRASE + " text);";


    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getDBName() {
        return DATABASE_NAME;
    }

    public static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создание таблицы с названиями столбцов
        db.execSQL(DATABASE_CREATE_SCRIPT);
        db.execSQL(FAVAORITE_TABLE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        // Заменить на toast
        Log.d(LOG_TAG, "Обновляемся с версии " + oldVersion + " до " + newVersion);


        if (oldVersion == 1) {
            // добавляю новый ряд со значение по умолчанию '#000D00FE'
            db.execSQL("ALTER TABLE data_time_description ADD COLUMN color TEXT NOT NULL DEFAULT '#000D00FE'");
            Log.d(LOG_TAG, "Успешно");
        } else if (oldVersion == 2){
            db.execSQL(FAVAORITE_TABLE_CREATE_SCRIPT);
            Log.d(LOG_TAG, "Успешно");
        }
    }
}