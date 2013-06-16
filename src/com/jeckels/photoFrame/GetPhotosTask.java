package com.jeckels.photoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.util.Timer;
import java.util.TimerTask;

/**
* User: jeckels
* Date: 1/23/13
*/
public abstract class GetPhotosTask extends AsyncTask<Object, Object, Bitmap>
{
    private PhotoFrame _frame;
    private Timer _timer;

    private final Context _context;

    public GetPhotosTask(Context context)
    {
        _context = context;
    }

    @Override
    protected Bitmap doInBackground(Object... params)
    {
        try
        {
            _frame = new PhotoFrame(_context);
            _frame.loadPhotoList();
            if (_frame.hasNext())
            {
                _timer = new Timer("nextPhoto");
                _timer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        AsyncTask<Object, Object, Bitmap> asyncTask = new AsyncTask<Object, Object, Bitmap>()
                        {
                            @Override
                            protected Bitmap doInBackground(Object... params)
                            {
                                return _frame.next();
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap)
                            {
                                GetPhotosTask.this.onPostExecute(bitmap);
                            }
                        };
                        asyncTask.execute();
                    }
                }, 5000, 5000);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return _frame.next();
    }

    public void cancel()
    {
        cancel(true);
        if (_timer != null)
        {
            _timer.cancel();
            _timer = null;
        }
    }
}
