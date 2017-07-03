package com.example.pyc.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pyc.myapplication.fragments.FragmentImport;
import com.example.pyc.myapplication.fragments.Fragment_calc;
import com.example.pyc.myapplication.fragments.Fragment_today;
import com.example.pyc.myapplication.fragments.Fragment_withAllRows;
import com.example.pyc.myapplication.fragments.Fragment_yesterday;
import com.example.pyc.myapplication.fragments.Settings;

import java.util.Calendar;

import static com.example.pyc.myapplication.fragments.FragmentImport.dataset;
import static com.example.pyc.myapplication.fragments.FragmentImport.dropColor;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    FragmentImport fimport;
    Fragment_today ftoday;
    Fragment_yesterday fyeaserday;
    Fragment_withAllRows fwithAllRows;
    Fragment_calc fcalc;

    //EditText txt_description;

    boolean doubleBackToExitPressedOnce = false;
    boolean import_view_present = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // обявление соответствий переменных и фрагментов
        fimport = new FragmentImport();
        ftoday = new Fragment_today();
        fyeaserday = new Fragment_yesterday();
        fwithAllRows = new Fragment_withAllRows();
        fcalc = new Fragment_calc();

        // код для nav_bar и его слушатель
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Чтобы приложение всегда запускалось с окна Сегодня
        getFragmentManager().beginTransaction().replace(R.id.container, ftoday).commit();

        // Кнопка создания заметки
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDrawerState(false);
                getFragmentManager().beginTransaction().replace(R.id.container, fimport).addToBackStack(null).commit();
                //меняю переменную т.к открывается окно импорта
                import_view_present = true;
                //делаю кнопку добавления записи срытой
                findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                if (fcalc.getC()>0 && fcalc.getD()>0){
                    // привязываю переменную к тексту
                   fimport.setText(fcalc.round(fcalc.getC(),1),fcalc.round(fcalc.getD(),1));

                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_Today);  // подсвечиваю вкладку Сегодня

        // изменение фона nav_drawer по времени.
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        View layout =  navigationView.inflateHeaderView(R.layout.nav_header_main);

        if(timeOfDay >= 0 && timeOfDay < 5) {
            layout.setBackgroundResource(R.drawable.night);
        }else if (timeOfDay >= 5 && timeOfDay < 10){
            layout.setBackgroundResource(R.drawable.morning);
        }else if(timeOfDay >= 10 && timeOfDay < 18){
            layout.setBackgroundResource(R.drawable.day0);
        }else if(timeOfDay >= 18 && timeOfDay < 22){
            layout.setBackgroundResource(R.drawable.evning);
        }else if(timeOfDay >= 22 && timeOfDay < 24){
            layout.setBackgroundResource(R.drawable.night);
        }
    }

    public void setDrawerState(boolean isEnabled) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        if ( isEnabled ) {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            toggle.setDrawerIndicatorEnabled(true);
            toggle.syncState();

        }
        else {
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            toggle.syncState();
        }
    }

    @Override
    public void onBackPressed() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Проверяем открыто ли боковое меню
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if (import_view_present) {
            setDrawerState(true);
            // присутствие формы импорта
            import_view_present = false;
            // чистим значения калькулятора
            if (this.fimport.txt_description.getText().toString().equals("")){
                fcalc.setClear(true);
            }
            getFragmentManager().popBackStack();

            // кнопка стала видимой
            findViewById(R.id.fab).setVisibility(View.VISIBLE);

        } else if (!doubleBackToExitPressedOnce && !import_view_present) {
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.double_back, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    //@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        android.app.FragmentTransaction ftrans = getFragmentManager().beginTransaction();

        if (id == R.id.nav_Today) {
            ftrans.replace(R.id.container, ftoday);

        } else if (id == R.id.nav_Yesterday) {
            ftrans.replace(R.id.container, fyeaserday);

        } else if (id == R.id.nav_Calendar) {
            ftrans.replace(R.id.container, fwithAllRows);

        } else if (id == R.id.nav_calc) {
            ftrans.replace(R.id.container, fcalc);

        } else if (id == R.id.nav_Settings) {
            ftrans.replace(R.id.container, new Settings());

        } else if (id == R.id.nav_Exit) {
            super.onBackPressed();
        }

        ftrans.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
