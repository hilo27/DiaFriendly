package com.example.pyc.myapplication.fragments;

// Created by Рус on 23.02.2018.

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorTreeAdapter;
import com.example.pyc.myapplication.R;

import static com.example.pyc.myapplication.fragments.DBHelper.*;
import static com.example.pyc.myapplication.fragments.DBHelper.DATABASE_TABLE;

// использую в Fragment_withAllRows
public class ExpListCursorAdaper extends SimpleCursorTreeAdapter {
    private SQLiteDatabase db;
    private EditText userFilter;


    ExpListCursorAdaper(SQLiteDatabase db, EditText userFilter, Context context, Cursor cursor, int groupLayout,
                        String[] groupFrom, int[] groupTo, int childLayout,
                        String[] childFrom, int[] childTo) {

        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        this.userFilter = userFilter;
        this.db = db;
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor getChildrenCursor, boolean isLastChild) {
        super.bindChildView(view, context, getChildrenCursor, isLastChild);
        // забираю значение у childrenCursor'a хотя можно и у просто cursor
        String color = getChildrenCursor.getString(getChildrenCursor.getColumnIndex(COLOR));
        //String color ="#1B3F51B5";
        view.setBackgroundColor(Color.parseColor(color));
    }

    protected Cursor getChildrenCursor(Cursor groupCursor) {
        // тут обязательно нужны все колонки т.к в дальнейшем нужны COLOR & DESCRIPTION
        String[] columns = new String[]{"rowid AS _id", DATA, TIME, COLOR, DESCRIPTION};
        String selection = "date = ? and " + DESCRIPTION + " like ?";  // выбираю дату = selectionArgs который беру у groupCursor
        String[] selectionArgs = new String[]{groupCursor.getString(groupCursor.getColumnIndex(DATA)),
                "%" + userFilter.getText().toString() + "%"};
        String orderBy = TIME + " DESC"; //сортирую по времени

        return db.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, orderBy);
    }
}