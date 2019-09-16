package com.one.geoar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBmanager extends SQLiteOpenHelper {
    public DBmanager(Context context){
        super(context, "MYData",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table Member(num integer primary key autoincrement, name text,lon real,lat real);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists Member;");
        onCreate(sqLiteDatabase);
    }
}
