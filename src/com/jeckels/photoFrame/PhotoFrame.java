package com.jeckels.photoFrame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.FlickrException;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * User: jeckels
 * Date: 1/23/13
 */
public class PhotoFrame
{
    public static final String FLICKR_API_KEY = "b9de7402e1865e77aa5effbd5cb1d357";
    public static final String FLICKR_SECRET = "75923ee87e1f4a63";

    private final PhotoDatabase _photoDatabase;

    private Map<Photo, Bitmap> _photos;
    private Iterator<Bitmap> _iterator;
    private Context _context;

    public PhotoFrame(Context context)
    {
        _context = context;
        _photoDatabase = new PhotoDatabase(context);
    }


    public void loadPhotoList() throws JSONException, FlickrException, IOException
    {
        Flickr f = new Flickr(FLICKR_API_KEY);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);
        String flickrId = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, null);

        if (flickrId == null)
        {
            throw new IllegalStateException("No Flickr ID set!");
        }

        Photosets photosets = f.getPhotosetsInterface().getList(flickrId);
        Photoset photoset = photosets.getPhotosets().iterator().next();
        // Limit to 20 for now until we do real caching
        photoset = f.getPhotosetsInterface().getPhotos(photoset.getId(), new HashSet<>(Arrays.asList("url_m")), 1, 20, 0);
        _photos = new LinkedHashMap<>();
        for (Photo photo : photoset.getPhotoList())
        {
            _photos.put(photo, downloadPhoto(photo));
            _photoDatabase.insert(photo);
        }
        resetIterator();
    }

    private void resetIterator()
    {
        _iterator = _photos.values().iterator();
    }

    public boolean hasNext()
    {
        return true;
    }

    public Bitmap next()
    {
        if (!_iterator.hasNext())
        {
            resetIterator();
        }
        return _iterator.next();
    }

    private Bitmap downloadPhoto(Photo photo) throws IOException
    {
        InputStream is = null;

        try
        {
            String photoURL = photo.getMediumSize().getSource();
            URL url = new URL(photoURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            return BitmapFactory.decodeStream(is);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

}
