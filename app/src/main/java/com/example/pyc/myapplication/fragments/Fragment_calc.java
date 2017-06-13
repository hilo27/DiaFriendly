package com.example.pyc.myapplication.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pyc.myapplication.R;

public class Fragment_calc extends Fragment {

    View v;
    EditText targetmass, defgrams;
    TextView result;
    double c, d, x, y;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // чтобы заработало findViewById
        v = inflater.inflate(R.layout.fragment_calc, container, false);

        // Заголовок фрагмента
        getActivity().setTitle("Калькулятор");
        defgrams = (EditText) v.findViewById(R.id.defgramms);
        targetmass = (EditText)v.findViewById(R.id.target_mass_prod);
        result = (TextView)v.findViewById(R.id.result);

        loadSettings(); // загрузка настроек из файла preference.xml

        targetmass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {init();}
        });
        defgrams.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {init();}
        });


        // возвращаю переменной v которая есть View чтобы работать вне метода
        return v;
    }

    private void loadSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        String xS,yS;
        //сначала загружаю настройки в String
        xS = settings.getString("prefINSto1XE", "");
        yS = settings.getString("prefYGLIto1XE", "");
        //проверяю на пустотуб потом загружаю
        if (xS =="") x=2; else x=Double.parseDouble(settings.getString("prefINSto1XE", ""));
        if (yS =="") y=12; else y=Double.parseDouble(settings.getString("prefYGLIto1XE", ""));
    }

    private void init() {
        // проверка на пустоту
        if (TextUtils.isEmpty(defgrams.getText().toString())|| TextUtils.isEmpty(targetmass.getText().toString())) {
            result.setText("Результат");
          return;
        }

        double a = Float.parseFloat(defgrams.getText().toString());
        double b = Float.parseFloat(targetmass.getText().toString());

        // а - колличество углеводов в 100г продукта
        // b - сколько хочет съесть
        // x=2 коэффициент сколько инса на 1хе
        // y=12 коэффициент сколько углеводов на 1хе

        c= (((a /100)* b)/y)*x;
        d= ((a /100)* b)/y;

        result.setText("Вы съедите "+round(d,1)+"XE \n"
                      +"Нужно подколоть " +round(c,1)+ " ед. инсулина \n \n"
                        +"1ХЕ = "+x+" ед. инсулина"
                        +"\n 1ХЕ = "+y+" грамм углеводов");
    }

    // метод округления, где scale кол-во знаков после запятой
    private double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
    }
}