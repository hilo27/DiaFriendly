package com.example.pyc.myapplication.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.example.pyc.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.pyc.myapplication.fragments.DBHelper.*;

public class FragmentImport extends Fragment implements View.OnClickListener {
    // делаю переменную v в качестве view чтобы нормально работать с фрагментом
    View v;

    Button btnSave, btnCancel, btnColor, btnAdd;

    public EditText txt_description;
    TextView dateView;
    DBHelper dbHelper;
    TimePicker timePicker;

    public static boolean dataset = false;
    public static boolean dropColor = true;
    static String dateString, color;  // делаю поле статичным, чтобы сохранить значение

    String text;

    public void setText(double c, double d) {
        this.text = "Ем "+String.valueOf(d)+"ХЕ, подкалываю "+String.valueOf(c)+" ед. инсулина ";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // чтобы заработало findViewById
        v = inflater.inflate(R.layout.fragment_import, container, false);

        // Заголовок фрагмента
        getActivity().setTitle("Добавление записи");

        // привязываю переменные к кнопкам, создаю слушателя нажатия
        btnSave = (Button) v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        btnCancel = (Button) v.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        // привязываю переменную к тексту
        txt_description = (EditText) v.findViewById(R.id.txt_description);
        txt_description.requestFocus();

        // привязываю переменную даты к тексту, обрабатываю нажатие
        dateView = (TextView) v.findViewById(R.id.dateView);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {DialogFragment picker = new DatePickerFragment();
                picker.show(getFragmentManager(), "datePicker");}});

        //color = "#000D00FE"; // прозрачно
        btnColor=(Button) v.findViewById(R.id.btnColor);
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorDialog();
                dropColor = false;
            }
        });

        btnAdd=(Button) v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        // привязываю timepicker и задаю 24ч формат
        timePicker = (TimePicker) v.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true); // формат 24 часа
        timePicker.clearFocus();

        // логика обновления фрагмента
        init();

        return v;
    }

    private void init() {
        // алгоритм установки даты и цвета
        if (dataset) {dateView.setText(dateString);} else {dateView.setText("Сегодня");}

        if (dropColor) {
            color = "#000D00FE";
            txt_description.setBackgroundColor(Color.parseColor(color));
        } else {
            txt_description.setBackgroundColor(Color.parseColor(color));
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // заполняю значения из калькулятора при восстановлении
        if (text!=null){
            txt_description.setText(text);
        }

        if (txt_description.getText().toString().equals("")){
            Calendar c = Calendar.getInstance();
            timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY)); //hour_of_day = 24ч
            timePicker.setCurrentMinute(c.get(Calendar.MINUTE));

            dataset = false;
            dropColor = true;

            init();
        }
    }

    @Override
    public void onClick(View v) {
        // подготовка данных, переменная values
        ContentValues values = new ContentValues();

        // получаем данные из полей ввода
        String description = txt_description.getText().toString();

        // получение даты в формате 27.05.1990
        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");  // формат вывода даты

        if (dateView.getText().toString().equals("Сегодня")) {
            dateString = sdf.format(date);
        } else {dateString = dateView.getText().toString();}

        // получение выбранного времени
        String time = "";
        int hour = timePicker.getCurrentHour();       // нормальные часы
        String sHour = "00";
        if(hour < 10){sHour = "0"+hour;
        } else {sHour = String.valueOf(hour);}
        int minute = timePicker.getCurrentMinute();  // нормальные минуты
        String sMinute = "00";
        if(minute < 10){sMinute = "0"+minute;
        } else {sMinute = String.valueOf(minute);}
        time = sHour+":"+sMinute;       // переменная для вывода времени 0:00

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // переключатель чтобы понимать какая кнопка нажата
        switch (v.getId()) {
            case R.id.btnSave:
                if (txt_description.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Текст заметки пустой", Toast.LENGTH_LONG).show();

                } else {
                    // подготовим данные для вставки в виде пары: имя столбца - значение
                    values.put(DATA, dateString);
                    values.put(TIME, time);
                    values.put(COLOR, color);
                    values.put(DESCRIPTION, description);
                    // записываю в базу
                    db.insert(DATABASE_TABLE, null, values);

                    // обнуляю поля
                    text = null;
                    txt_description.setText("");

                    // вывожу результат записи
                    Toast.makeText(getActivity(), R.string.item_save, Toast.LENGTH_LONG).show();

                    // алгоритм пересоздания уже есть в логике кнопки назад
                    getActivity().onBackPressed();

                    // чтобы скрыть клавиатуру
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                break;

            case R.id.btnCancel:
                dbHelper.close();
                // обнуляю поля
                text = null;
                txt_description.setText("");

                // алгоритм пересоздания уже есть в логике кнопки назад
                getActivity().onBackPressed();

                Toast.makeText(getActivity(), R.string.not_save, Toast.LENGTH_SHORT).show();
                break;
        }

        // закрываем подключение к БД
        dbHelper.close();
    }

    // класс для показадиалога выбора даты, идёт от picker = new DatePickerFragment
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            int correctM = month+1;
            String fixDay = "00";
            String fixMonth = "00";

            // исправляю отображение 4 на 04
            if(day < 10){fixDay = "0"+day;}
            else {fixDay = String.valueOf(day);}
            // исправляю отображение 4 на 04
            if(correctM < 10){fixMonth = "0"+correctM;}
            else {fixMonth = String.valueOf(correctM);}

            dataset = true;
            dateString = (fixDay+ "." +correctM+ "." +year);
            ((TextView) getActivity().findViewById(R.id.dateView)).setText(fixDay+ "." +fixMonth+ "." +year);
        }
    }

    private void showColorDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setTitle(R.string.choose_color);
        dialog.setContentView(R.layout.color_picker_dialog);

        Button none = (Button) dialog.findViewById(R.id.none);
        Button yellow = (Button) dialog.findViewById(R.id.btn_yellow);
        Button red = (Button) dialog.findViewById(R.id.red);
        Button blue = (Button) dialog.findViewById(R.id.blue);
        Button orange = (Button) dialog.findViewById(R.id.orange);
        Button azure = (Button) dialog.findViewById(R.id.azure);
        Button magenta = (Button) dialog.findViewById(R.id.magenta);
        Button pink = (Button) dialog.findViewById(R.id.pink);
        Button salat = (Button) dialog.findViewById(R.id.salat);
        Button dark_green = (Button) dialog.findViewById(R.id.dark_green);

        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#ffff8d";  //#9bfff700
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#ff8a80";  //#a0ff0400
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#80d8ff";  //#8d0000ff
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#b1ff7f00";
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#000D00FE";
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        azure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#a2007fff";
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        dark_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#a9139429";
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        magenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#c1ff00ff";
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        pink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#ffd180"; //
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});

        salat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {color = "#ccff90"; //#b740ff00
                txt_description.setBackgroundColor(Color.parseColor(color));
                dialog.dismiss();}});
        dialog.show();
    }

    private void showAddDialog(){
        final Dialog addDialog = new Dialog(getActivity());
        addDialog.setTitle("Избранные события");
        addDialog.setContentView(R.layout.add_favorite_dialog);

        final TextView favoriteText = (TextView) addDialog.findViewById(R.id.txt_favorite_phrase);
        final ListView listView = (ListView) addDialog.findViewById(R.id.listView);
        final ContentValues favoritePhraseValue = new ContentValues();

        Button btnAddFavorite = (Button) addDialog.findViewById(R.id.btnAddFavorite);
        btnAddFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoriteText.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Введите текст", Toast.LENGTH_LONG).show();

                } else {
                    // подключаемся к БД
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    favoritePhraseValue.put(PHRASE, favoriteText.getText().toString());
                    // записываю в базу
                    db.insert(FAVAORITE_TABLE, null, favoritePhraseValue);
                    Toast.makeText(getActivity(), "Добавлено", Toast.LENGTH_LONG).show();
                    favoriteText.setText("");
                    // обновляю listView используя метод updateList()
                    listView.setAdapter(updateList());
                }
            }
        });

        // обновляю listView используя метод updateList()
        listView.setAdapter(updateList());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // текст элемента по которому кликнул в листе
                String item = ((TextView)view).getText().toString();
                txt_description.append(item+", ");
                //txt_description.setText(item);
                addDialog.dismiss();
            }
        });

        // добавляем контекстное меню к списку
        registerForContextMenu(listView);

        // показываю диалог
        addDialog.show();
    }

    private SimpleCursorAdapter updateList() {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        //выбираю с какими колонками работать и в каком порядке выводить
        String[] columns = new String[] {"rowid AS _id", PHRASE };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_1,  // android.R.layout.simple_list_item_2
                db.query(FAVAORITE_TABLE, columns, null, null, null, null, null),
                new String[] {PHRASE},
                new int[] { android.R.id.text1});

        return adapter;
    }


    // кусок кода отвечающий за меню выбора
    public void delRec(long id) {
        // удалить запись из базы, сама функция
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(FAVAORITE_TABLE, "id" + " = " + id, null);
        Toast.makeText(getActivity(), "Удалено", Toast.LENGTH_LONG).show();
    }
    public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // что нажато определяется по 2 параметру

        menu.add(0, 0, 1, "Удалить запись").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 0) {
                    // получаем из пункта контекстного меню данные по пункту списка
                    AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    // извлекаем id записи и удаляем соответствующую запись в БД
                    delRec(acmi.id);
                    // обновляем основное окно
                    ListView listView = (ListView) v.findViewById(R.id.listView);
                    listView.setAdapter(updateList());
                    return true;
                }
                return false;
            }
        });
    }
}