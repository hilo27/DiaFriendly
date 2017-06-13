package com.example.pyc.myapplication.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorTreeAdapter;

import com.example.pyc.myapplication.R;

import static com.example.pyc.myapplication.fragments.DBHelper.COLOR;
import static com.example.pyc.myapplication.fragments.DBHelper.DATA;
import static com.example.pyc.myapplication.fragments.DBHelper.DATABASE_TABLE;
import static com.example.pyc.myapplication.fragments.DBHelper.DESCRIPTION;
import static com.example.pyc.myapplication.fragments.DBHelper.FAVAORITE_TABLE;
import static com.example.pyc.myapplication.fragments.DBHelper.PHRASE;
import static com.example.pyc.myapplication.fragments.DBHelper.TIME;

public class Fragment_withAllRows extends Fragment implements View.OnClickListener {
    // делаю переменную v в качестве view чтобы нормально работать с фрагментом
    View v;
    DBHelper dbHelper;
    EditText userFilter;
    //SimpleCursorAdapter userAdapter;
    SQLiteDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // чтобы заработало findViewById
        v = inflater.inflate(R.layout.fragment_allrows, container, false);

        // Заголовок фрагмента
        getActivity().setTitle("Все записи");

        init();
        // возвращаю переменной v которая есть View чтобы работать вне метода
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ExpandableListView allRows = (ExpandableListView) v.findViewById(R.id.allRows);

        //выбираю с какими колонками работать и в каком порядке выводить
        final String[] columns = new String[] {"rowid AS _id", DATA };

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        //выбираю с какими колонками работать и в каком порядке выводить
        final String groupBy = DATA;
        //сортирую по дате, разбив дату на отдельные цифры
        final String orderBy =  "substr(date, 7, 4) DESC, substr(date, 4, 2) DESC, substr(date, 1, 2) DESC";

        // данные по названиям групп для адаптера
        Cursor cursor = db.query(DATABASE_TABLE, columns, null, null, groupBy, null, orderBy);

        // сопоставление данных и View для групп
        String[] groupFrom = { DATA };
        int[] groupTo = { android.R.id.text1 };
        // сопоставление данных и View для элементов
        String[] childFrom = { TIME, DESCRIPTION };
        int[] childTo = { R.id.text2, R.id.text3 };

        final SimpleCursorTreeAdapter sctAdapter2 = new MyAdapter2(getActivity().getBaseContext(), cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, R.layout.items, childFrom,
                childTo);

        userFilter = (EditText)v.findViewById(R.id.userFilter);
        //final ListView listView = (ListView) v.findViewById(R.id.sortedListView);
        allRows.setAdapter(sctAdapter2);

        // если в текстовом поле есть текст, выполняем фильтрацию
        // данная проверка нужна при переходе от одной ориентации экрана к другой
        if(!userFilter.getText().toString().isEmpty())
            sctAdapter2.getFilter().filter(userFilter.getText().toString());

        try {
            // устанавливаем провайдер фильтрации
            sctAdapter2.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    if (constraint == null || constraint.length() == 0) {
                        return db.query(DATABASE_TABLE, columns, null, null, groupBy, null, orderBy);
                    }
                    else {
                        return db.rawQuery("select id as _id, "+DATA+", "+TIME+", "+DESCRIPTION+" from " + DATABASE_TABLE +
                                " where "+DESCRIPTION+" like ? GROUP BY "+DATA+" ORDER BY "+orderBy,
                                new String[]{"%" + constraint.toString() + "%"});
                    }
                }
            });

        // установка слушателя изменения текста
        userFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                sctAdapter2.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        }
        catch (SQLException ex){}
    }

    private void init() {
        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        //выбираю с какими колонками работать и в каком порядке выводить
        String[] columns = new String[] {"rowid AS _id", DATA };
        String groupBy = DATA;
        //сортирую по дате, разбив дату на отдельные цифры
        String orderBy =  "substr(date, 7, 4) DESC, substr(date, 4, 2) DESC, substr(date, 1, 2) DESC";

        // данные по названиям групп для адаптера
        Cursor cursor = db.query(DATABASE_TABLE, columns, null, null, groupBy, null, orderBy);

        // сопоставление данных и View для групп
        String[] groupFrom = { DATA };
        int[] groupTo = { android.R.id.text1 };
        // сопоставление данных и View для элементов
        String[] childFrom = { TIME, DESCRIPTION };
        int[] childTo = { R.id.text2, R.id.text3 };

        // создаем адаптер и настраиваем список
        SimpleCursorTreeAdapter sctAdapter = new MyAdapter(getActivity().getBaseContext(), cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, R.layout.items, childFrom,
                childTo);

        ExpandableListView allRows = (ExpandableListView) v.findViewById(R.id.allRows);
        View empty = v.findViewById(R.id.emptyListElem);   // значение для пустого listView

        allRows.setAdapter(sctAdapter);
        allRows.setEmptyView(empty);

        // закрываем подключение к БД
        dbHelper.close();
    }

    private class MyAdapter extends SimpleCursorTreeAdapter {
        public MyAdapter(Context context, Cursor cursor, int groupLayout,
                         String[] groupFrom, int[] groupTo, int childLayout,
                         String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo,
                    childLayout, childFrom, childTo);
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
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // тут обязательно нужны все колонки т.к в дальнейшем нужны COLOR & DESCRIPTION
            String[] columns = new String[] {"rowid AS _id", DATA, TIME, COLOR, DESCRIPTION };
            String selection = "date = ?";  // выбирпаю дату = selectionArgs который беру у groupCursor
            String[] selectionArgs = new String[] { groupCursor.getString(groupCursor.getColumnIndex(DATA)) };
            String orderBy = TIME + " DESC"; //сортирую по времени

            return db.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, orderBy);
        }
    }

    private class MyAdapter2 extends SimpleCursorTreeAdapter {
        public MyAdapter2(Context context, Cursor cursor, int groupLayout,
                          String[] groupFrom, int[] groupTo, int childLayout,
                          String[] childFrom, int[] childTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo,
                    childLayout, childFrom, childTo);
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
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            userFilter = (EditText)v.findViewById(R.id.userFilter);

            // тут обязательно нужны все колонки т.к в дальнейшем нужны COLOR & DESCRIPTION
            String[] columns = new String[] {"rowid AS _id", DATA, TIME, COLOR, DESCRIPTION };
            String selection = "date = ? and "+DESCRIPTION+" like ?";  // выбирпаю дату = selectionArgs который беру у groupCursor
            String[] selectionArgs = new String[] { groupCursor.getString(groupCursor.getColumnIndex(DATA)),
                    "%" + userFilter.getText().toString() + "%" };
            String orderBy = TIME + " DESC"; //сортирую по времени

            return db.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, orderBy);
        }
    }

    @Override
    public void onClick(View v) {

    }
}