package com.example.pyc.myapplication.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.SimpleCursorAdapter;

import static com.example.pyc.myapplication.fragments.DBHelper.COLOR;

// использу в Fragment_today и Fragment_yesterday
public class CursorAdapter extends SimpleCursorAdapter {

    public CursorAdapter(Context context, int list, Cursor query, String[] strings, int[] ints) {
        super(context,list,query,strings,ints);
    }

     @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        String color = cursor.getString(cursor.getColumnIndex(COLOR));
        //String color ="#1B3F51B5";
        view.setBackgroundColor(Color.parseColor(color));
    }
}