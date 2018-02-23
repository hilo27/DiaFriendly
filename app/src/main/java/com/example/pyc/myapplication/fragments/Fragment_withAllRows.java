package com.example.pyc.myapplication.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

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
    SQLiteDatabase db;
    SimpleCursorTreeAdapter sctAdapter;
    ExpandableListView allRows;
    int openedGroup = -1; // показатель открытой группы, начинается с 0

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

        //init();
        // возвращаю переменной v которая есть View чтобы работать вне метода
        return v;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        // Создаём контекстное меню, только для подпунктов
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            // запоминаем какая группа раскрыта
            openedGroup = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            // что нажато определяется по 2 параметру
            menu.add(0, 0, 1, "Удалить запись");
            menu.add(0, 1, 0, "Редактировать запись");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        db = dbHelper.getWritableDatabase();
        Fragment_today fragment_today = new Fragment_today();
        // получаем из пункта контекстного меню данные по пункту списка
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        // УДАЛЕНИЕ
        if (item.getItemId() == 0) {
            // извлекаем id записи и удаляем соответствующую запись в БД
            fragment_today.delRec(info.id, db);
            this.onResume();

            // разворачиваем повторно открытую группу
            if (openedGroup > -1) {
                allRows.expandGroup(openedGroup);
            }

            return true;
        }

        // РЕДАКТИРОВАНИЕ
        if (item.getItemId() == 1) {
            String[] columns = new String[]{"rowid AS _id", DESCRIPTION, COLOR};
            Cursor discript = db.query(DATABASE_TABLE, columns, "id" + " = " + info.id, null, null, null, null);
            discript.moveToFirst();
            String oldItem = discript.getString(discript.getColumnIndex(DESCRIPTION));
            String oldColor = discript.getString(discript.getColumnIndex(COLOR));

            // Show input box
            showInputBox(info.id, oldItem, oldColor);
            discript.close();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();

        // если в текстовом поле есть текст, выполняем фильтрацию
        // данная проверка нужна при переходе от одной ориентации экрана к другой
        if (!userFilter.getText().toString().isEmpty()){
            sctAdapter.getFilter().filter(userFilter.getText().toString());
        }

    }


    private void init() {
        allRows = (ExpandableListView) v.findViewById(R.id.allRows);

        //выбираю с какими колонками работать и в каком порядке выводить
        final String[] columns = new String[]{"rowid AS _id", DATA};

        // подключаемся к БД
        db = dbHelper.getWritableDatabase();

        //выбираю с какими колонками работать и в каком порядке выводить
        final String groupBy = DATA;
        //сортирую по дате, разбив дату на отдельные цифры
        final String orderBy = "substr(date, 7, 4) DESC, substr(date, 4, 2) DESC, substr(date, 1, 2) DESC";

        // данные по названиям групп для адаптера
        Cursor cursor = db.query(DATABASE_TABLE, columns, null, null, groupBy, null, orderBy);

        // сопоставление данных и View для групп
        String[] groupFrom = {DATA};
        int[] groupTo = {android.R.id.text1};
        // сопоставление данных и View для элементов
        String[] childFrom = {TIME, DESCRIPTION};
        int[] childTo = {R.id.text2, R.id.text3};

        userFilter = (EditText) v.findViewById(R.id.userFilter);

        sctAdapter = new ExpListCursorAdaper(db, userFilter, getActivity().getBaseContext(), cursor,
                android.R.layout.simple_expandable_list_item_1, groupFrom,
                groupTo, R.layout.items, childFrom,
                childTo);

        View empty = v.findViewById(R.id.emptyListElem);   // значение для пустого listView
        allRows.setAdapter(sctAdapter);
        allRows.setEmptyView(empty);

        // добавляем контекстное меню к списку
        registerForContextMenu(allRows);

        try {
            // устанавливаем провайдер фильтрации
            sctAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    if (constraint == null || constraint.length() == 0) {
                        return db.query(DATABASE_TABLE, columns, null, null, groupBy, null, orderBy);
                    } else {
                        return db.rawQuery("select id as _id, " + DATA + ", " + TIME + ", " + DESCRIPTION + " from " + DATABASE_TABLE +
                                        " where " + DESCRIPTION + " like ? GROUP BY " + DATA + " ORDER BY " + orderBy,
                                new String[]{"%" + constraint.toString() + "%"});
                    }
                }
            });

            // установка слушателя изменения текста
            userFilter.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    sctAdapter.getFilter().filter(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

        } catch (SQLException ex) {
            db.close();
        }
    }

    @Override
    public void onClick(View v) {

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
                    public void onClick(View v) {
                        btcolor.setText("#ffff8d");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //красный
                Button red = (Button) dialogColor.findViewById(R.id.red);
                red.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#ff8a80");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //синий
                Button blue = (Button) dialogColor.findViewById(R.id.blue);
                blue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#80d8ff");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //оранжевый
                Button orange = (Button) dialogColor.findViewById(R.id.orange);
                orange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#b1ff7f00");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //
                Button none = (Button) dialogColor.findViewById(R.id.none);
                none.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#000D00FE");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //azure
                Button azure = (Button) dialogColor.findViewById(R.id.azure);
                azure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#a2007fff");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //dark green
                Button dark_green = (Button) dialogColor.findViewById(R.id.dark_green);
                dark_green.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#a9139429");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //magenta
                Button magenta = (Button) dialogColor.findViewById(R.id.magenta);
                magenta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#c1ff00ff");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //pink
                Button pink = (Button) dialogColor.findViewById(R.id.pink);
                pink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#ffd180");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });
                //salat
                Button salat = (Button) dialogColor.findViewById(R.id.salat);
                salat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btcolor.setText("#ccff90");
                        btcolor.setBackgroundColor(Color.parseColor(btcolor.getText().toString()));
                        dialogColor.dismiss();
                    }
                });

                //закрывание диалога выбора цвета, возврат к редактору
                dialogColor.show();
            }
        });


        Button bt = (Button) dialog.findViewById(R.id.btdone);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment_today fragment_today = new Fragment_today();
                fragment_today.editRec(id, editText.getText().toString(), btcolor.getText().toString(), db); // второй параметр для editRec(change_text)

                init();

                if (!userFilter.getText().toString().isEmpty()){
                    sctAdapter.getFilter().filter(userFilter.getText().toString());
                }

                // разворачиваем повторно открытую группу
                if (openedGroup > -1) {
                    allRows.expandGroup(openedGroup);
                }


                dialog.dismiss();
            }
        });
        dialog.show();
    }

}