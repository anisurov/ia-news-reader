package com.x54.interactivenewsreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Anisur Rahman on 11/20/18.
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "AudioReader.db";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;
    final static String NEWS_SITE_TABLE="NewsSite";
    final static String ITEM_ID_COLUMN="_id";
    final static String NAME_COLUMN="name";
    final static String HOME_COLUMN="home";
    final static String INTL_COLUMN="intl";
    final static String SPORTS_COLUMN="sports";
    final static String ENTERTAIN_COLUMN="entertain";
    final static String EDITORIAL_COLUMN="editorial";


    private SQLiteDatabase mDataBase;
    private final Context mContext;
    private boolean mNeedUpdate = false;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        if(copyDataBase()){
            Log.d("READER", "DBOpenHelper: db copied successfully ");
        }



        this.getReadableDatabase();
    }

    public void updateDataBase() throws IOException {
        if (mNeedUpdate) {
            File dbFile = new File(DB_PATH + DB_NAME);
            if (dbFile.exists())
                dbFile.delete();

            copyDataBase();

            mNeedUpdate = false;
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        if (dbFile.exists()){
            Log.d("READING", "checkDataBase: db file exists");
        }else {
            Log.d("READING", "checkDataBase: db file doesnot exists");
        }
        return dbFile.exists();
    }

    private boolean copyDataBase() {
        if (!checkDataBase()) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDBFile();
                return true;
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
        return false;
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public boolean openDataBase() throws SQLException {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            mNeedUpdate = true;
    }


    public ArrayList<NewsSiteDetails> getAllNewsSite() {
        ArrayList<NewsSiteDetails> arrayList=new ArrayList<NewsSiteDetails>();
        SQLiteDatabase database=this.getReadableDatabase();

        String selectAllQueryString="SELECT * FROM "+NEWS_SITE_TABLE;
        Cursor cursor=database.rawQuery(selectAllQueryString, null);
        if (cursor==null){
            Log.d("READER", "getAllNewsSite: result is empty");
        }
        if (cursor.moveToFirst()) {
            do {
                NewsSiteDetails newsSiteDetails=new NewsSiteDetails(
                        cursor.getString(cursor.getColumnIndex(NAME_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(HOME_COLUMN)));
                arrayList.add(newsSiteDetails);
            } while (cursor.moveToNext());
        }
        //cursor.close();
        return arrayList;
    }

    public ArrayList<NewsSiteDetails> getNewsSite(String url) {
        ArrayList<NewsSiteDetails> arrayList=new ArrayList<NewsSiteDetails>();
        SQLiteDatabase database=this.getReadableDatabase();

        String selectAllQueryString="SELECT * FROM "+NEWS_SITE_TABLE+" WHERE "+HOME_COLUMN+" = '"+url+"'";
        Cursor cursor=database.rawQuery(selectAllQueryString, null);
        if (cursor==null){
            Log.d("READER", "getAllNewsSite: result is empty");
        }
        if (cursor.moveToFirst()) {
            do {
                NewsSiteDetails newsSiteDetails=new NewsSiteDetails(
                        cursor.getString(cursor.getColumnIndex(NAME_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(HOME_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(INTL_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(EDITORIAL_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(SPORTS_COLUMN)),
                        cursor.getString(cursor.getColumnIndex(ENTERTAIN_COLUMN)));
                arrayList.add(newsSiteDetails);
            } while (cursor.moveToNext());
        }
        //cursor.close();
        return arrayList;
    }

    public long insertData(String name, String home) {
        SQLiteDatabase database=this.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put(NAME_COLUMN, name);
        values.put(HOME_COLUMN, home);

        return database.insert(NEWS_SITE_TABLE, null, values);

    }
}
