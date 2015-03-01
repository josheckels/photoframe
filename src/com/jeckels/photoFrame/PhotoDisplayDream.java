package com.jeckels.photoFrame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoDisplayDream extends DreamService
{
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
            _task = new GetPhotosTask(getBaseContext(), (TextView)findViewById(R.id.messageText))
            {
                @Override
                protected void onPostExecute(DisplayablePhoto bitmap)
                {
                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    imageView.setImageBitmap(bitmap.getBitmap());
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
