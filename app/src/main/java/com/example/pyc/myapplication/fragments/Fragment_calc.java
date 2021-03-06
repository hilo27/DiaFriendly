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
    public EditText targetmass, defgrams;
    TextView result;
    double c, d, x, y;
    public boolean clear = false;

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (clear){
            defgrams.setText("");
            targetmass.setText("");
            clear = false;
            d=0;
            c=0;
            init();
        }
    }

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public double getC() {
        return c;
    }

    public double getD() {
        return d;
    }

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
        //проверяю на пустоту, потом загружаю
        if (xS.equalsIgnoreCase("")) x=2; else x=Double.parseDouble(settings.getString("prefINSto1XE", ""));
        if (yS.equalsIgnoreCase("")) y=12; else y=Double.parseDouble(settings.getString("prefYGLIto1XE", ""));
    }

    public void init() {
        // проверка на пустоту
        if (TextUtils.isEmpty(defgrams.getText().toString())|| TextUtils.isEmpty(targetmass.getText().toString())) {
            d=0;
            c=0;
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

        result.setText(new StringBuilder()
                .append("Вы съедите ").append(round(d, 1))
                .append("XE \n")
                .append("Нужно подколоть ").append(round(c, 1)).append(" ед. инсулина \n \n")
                .append("1ХЕ = ").append(x).append(" ед. инсулина \n ")
                .append("1ХЕ = ").append(y).append(" грамм углеводов").toString());
    }

    // метод округления, где scale кол-во знаков после запятой
    public double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
    }
}