package com.example.pyc.myapplication;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.pyc.myapplication.fragments.DBHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupAndRestore {

    //метод ИМПОРТА данных
    public static void importDB(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                //Здесь логика привязана к названию базы по умолчанию
                //String backupDBPath = String.format("%s.bak", DBHelper.getDBName());
                //File currentDB = new File(sd, backupDBPath);

                File backupDB = context.getDatabasePath(DBHelper.getDBName()); //рабочее название файла базы
                File currentDB = new File(sd, "diaFriendly.db.bak"); //имя файла бекапа

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Toast.makeText(context, R.string.import_successful, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.import_faild, Toast.LENGTH_LONG).show();
        }
    }

    //метод ЭКСПОРТА данных
    public static void exportDB(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            //File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                //Здесь логика привязана к названию базы по умолчанию
                //String backupDBPath = String.format("%s.bak", DBHelper.getDBName());
                //File backupDB = new File(sd, backupDBPath);

                File currentDB = context.getDatabasePath(DBHelper.getDBName()); //рабочее название файла базы
                File backupDB = new File(sd, "diaFriendly.db.bak");  //имя файла бекапа

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                Toast.makeText(context, R.string.backup_successful, Toast.LENGTH_LONG).show();

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.backup_faild, Toast.LENGTH_LONG).show();
        }
    }

}
