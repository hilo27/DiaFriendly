package com.example.pyc.myapplication.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.pyc.myapplication.R;

import java.text.SimpleDateFormat;

import static com.example.pyc.myapplication.fragments.DBHelper.COLOR;
import static com.example.pyc.myapplication.fragments.DBHelper.DATA;
import static com.example.pyc.myapplication.fragments.DBHelper.DATABASE_TABLE;
import static com.example.pyc.myapplication.fragments.DBHelper.DESCRIPTION;
import static com.example.pyc.myapplication.fragments.DBHelper.TIME;

public class Fragment_today extends Fragment {
    // делаю переменную v в качестве view чтобы нормально работать с фрагментом
    View v;
    DBHelper dbHelper;

    // форматы вывода даты
    long date = System.currentTimeMillis();
    SimpleDateFormat lsdf = new SimpleDateFormat("EE, d MMMM");  //для заголовка окна
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");  //для базыданных

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // чтобы заработало findViewById
        v = inflater.inflate(R.layout.fragment_today, container, false);
        String dateString = lsdf.format(date);  // сеголня

        // Заголовок фрагмента
        getActivity().setTitle("Сегодня "+ dateString);

        init();
        // возвращаю переменной v которая есть View чтобы работать вне метода
        return v;
    }

    private void init() {
        // получение даты в формате 27.05.1990
        long date = System.currentTimeMillis();
        String dateString = sdf.format(date);  // сеголня

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //выбираю с какими колонками работать и в каком порядке выводить
        String[] columns = new String[] {"rowid AS _id", DATA, TIME, COLOR, DESCRIPTION };
        String selection = "date = ?";  // выбирпаю дату = selectionArgs
        String[] selectionArgs = new String[] { dateString };  //dateString = Сегодня
        String orderBy = DATA + " DESC, " + TIME + " DESC";  //сортирую по дате, потом по времени

        // привязка к элементам формы
        ListView listView = (ListView) v.findViewById(R.id.lvData);
        View empty = v.findViewById(R.id.emptyListElem);   // значение для пустого listView

        // создаю адаптер, задаю параметры
        SimpleCursorAdapter adapter = new CursorAdapter(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_2,  // android.R.layout.simple_list_item_2
                db.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, orderBy),
                new String[] {TIME, DESCRIPTION},
                new int[] { android.R.id.text1, android.R.id.text2 });

        // сюда будет выводится инфа
        listView.setAdapter(adapter);
        listView.setEmptyView(empty);

        // добавляем контекстное меню к списку
        registerForContextMenu(listView);

        // закрываем подключение к БД
        dbHelper.close();
    }

    // кусок кода отвечающий за меню выбора
    public void delRec(long id) {
        // удалить запись из базы, сама функция
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DATABASE_TABLE, "id" + " = " + id, null);
    }
    public void editRec(long id, String change_text, String new_color) {
        // удалить запись из базы, сама функция
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DESCRIPTION, change_text);
        values.put(COLOR, new_color);
        db.update(DATABASE_TABLE, values, "id" + " = " + id, null);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // что нажато определяется по 2 параметру
        menu.add(0, 0, 1, "Удалить запись");
        menu.add(0, 1, 0, "Редактировать запись");
    }
    public boolean onContextItemSelected(MenuItem item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (item.getItemId() == 0) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи и удаляем соответствующую запись в БД
            delRec(acmi.id);
            init();  // обновляем основное окно
            return true;
        }

        if (item.getItemId() == 1) {
            // получаем из пункта контекстного меню данные по пункту списка
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            // извлекаем id записи
            //editRec(acmi.id);
            long i = acmi.id;

            String[] columns = new String[] {"rowid AS _id", DESCRIPTION, COLOR };
            Cursor discript = db.query(DATABASE_TABLE, columns, "id" + " = " + i, null, null, null, null);
            discript.moveToFirst();
            String oldItem = discript.getString(discript.getColumnIndex(DESCRIPTION));
            String oldColor = discript.getString(discript.getColumnIndex(COLOR));

            // Show input box
            showInputBox(acmi.id, oldItem, oldColor);
            discript.close();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    public void showInputBox(final long id, String oldItem, String oldColor) {
        final Dialog dialog = new Dialog(getActivity());
        final Dialog dialogColor = new Dialog(getActivity());

        // заголовки диалогов
        dialog.setTitle(R.string.edit_row_tittle_box);
        dialogColor.setTitle(R.string.choose_color);

        // view диалогов
        dialog.setContentView(R.layout.edit_box);
        dialogColor.setContentView(R.layout.color_picker_dialog);

        final EditText editText = (EditText) dialog.findViewById(R.id.txtinput);
        final TextView btcolor = (TextView) dialog.findViewById(R.id.btcolor);

        editText.setText(oldItem);
        btcolor.setText(oldColor);
        btcolor.setBackgroundColor(Color.parseColor(oldColor));

        // нажатие на цвет
        btcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //желтый
                Button yellow = (Button) dialogColor.findViewById(R.id.btn_yellow);
                yellow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#ffff8d") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //красный
                Button red = (Button) dialogColor.findViewById(R.id.red);
                red.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#ff8a80") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //синий
                Button blue = (Button) dialogColor.findViewById(R.id.blue);
                blue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#80d8ff") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //оранжевый
                Button orange = (Button) dialogColor.findViewById(R.id.orange);
                orange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#b1ff7f00") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //
                Button none = (Button) dialogColor.findViewById(R.id.none);
                none.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#000D00FE") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //azure
                Button azure = (Button) dialogColor.findViewById(R.id.azure);
                azure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#a2007fff") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //dark green
                Button dark_green = (Button) dialogColor.findViewById(R.id.dark_green);
                dark_green.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#a9139429") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //magenta
                Button magenta = (Button) dialogColor.findViewById(R.id.magenta);
                magenta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#c1ff00ff") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //pink
                Button pink = (Button) dialogColor.findViewById(R.id.pink);
                pink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#ffd180") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});
            //salat
                Button salat = (Button) dialogColor.findViewById(R.id.salat);
                salat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {btcolor.setText("#ccff90") ;
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();}});

                //закрывание диалога выбора цвета, возврат к редактору
                dialogColor.show();
            }
        });


        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editRec(id, editText.getText().toString(), btcolor.getText().toString()); // второй параметр для editRec(change_text)
                init();  // обновляем основное окно
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}