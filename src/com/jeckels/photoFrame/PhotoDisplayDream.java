package com.jeckels.photoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.widget.ImageView;
import org.slf4j.LoggerFactory;

public class PhotoDisplayDream extends DreamService
{
    private LoggerFactory _loggerFactory;
    private GetPhotosTask _task;

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.main);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            _task = new GetPhotosTask(getBaseContext())
            {
                @Override
                protected void onPostExecute(Bitmap bitmap)
                {
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap);
                }
            };
            _task.execute();
        }
        else
        {
            // display error
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        _task.cancel();
    }
}
