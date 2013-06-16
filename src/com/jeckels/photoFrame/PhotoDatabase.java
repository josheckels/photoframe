package com.jeckels.photoFrame;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.googlecode.flickrjandroid.photos.Photo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * User: jeckels
 * Date: 1/29/13
 */
public class PhotoDatabase extends SQLiteOpenHelper
{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Photos.db";

    private static final String FLICKR_PHOTO_TABLE_NAME = "FlickrPhoto";

    public PhotoDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(getSQL("createTables.sql"));
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(getSQL("deleteTables.sql"));
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    private String getSQL(String fileName)
    {
        InputStream in = getClass().getClassLoader().getResourceAsStream("com/jeckels/photoFrame/sql/" + fileName);
        StringBuilder result = new StringBuilder();
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
                result.append(line);
                result.append("\n");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            try { in.close(); } catch (IOException ignored) {}
        }
        return result.toString();
    }

    public void insert(Photo photo)
    {
        ContentValues values = new ContentValues();
        values.put("URL", photo.getMediumSize().getSource());
        values.put("FlickrID", photo.getId());
        values.put("Height", photo.getOriginalHeight());
        values.put("Width", photo.getOriginalWidth());
        getWritableDatabase().insert(FLICKR_PHOTO_TABLE_NAME, null, values);
    }
}
