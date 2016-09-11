package com.jeckels.photoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
* User: jeckels
* Date: 1/23/13
*/
public abstract class GetPhotosTask extends AsyncTask<Object, Object, GetPhotosTask.DisplayablePhoto>
{
    private PhotoFrame _frame;
    private Timer _timer;

    private final Context _context;
    private final TextView _messageText;
    private boolean _loading = false;

    /** Delay between photos, in seconds */
    private int _delay = 10;

    /** Time in millisecond of when we last changed the photo */
    private long _lastPhotoChange;

    /** Time in milliseconds for when we should hide the message text */
    private Long _hideMessage;

    public GetPhotosTask(Context context, TextView messageText)
    {
        _context = context;
        _messageText = messageText;
    }

    public static class DisplayablePhoto
    {
        private final Bitmap _bitmap;
        private final SimplePhoto _photo;
        private final SimpleCategory _category;
        private final PhotoStore _photoStore;

        public DisplayablePhoto(Bitmap bitmap, SimplePhoto photo, SimpleCategory category, PhotoStore photoStore)
        {
            _bitmap = bitmap;
            _photo = photo;
            _category = category;
            _photoStore = photoStore;
        }

        public Bitmap getBitmap()
        {
            return _bitmap;
        }

        public SimplePhoto getPhoto()
        {
            return _photo;
        }

        public SimpleCategory getCategory()
        {
            return _category;
        }

        public PhotoStore getPhotoStore()
        {
            return _photoStore;
        }
    }

    public void adjustTimer(int units)
    {
        int oldDelay = _delay;
        // Don't allow delay to get too small or negative
        _delay = Math.max(_delay + units, 2);
        if (oldDelay != _delay)
        {
            _messageText.setText("Delay: " + _delay + " seconds");
            _messageText.setVisibility(View.VISIBLE);
            _hideMessage = System.currentTimeMillis() + 3000;
        }
    }

    @Override
    protected DisplayablePhoto doInBackground(Object... params)
    {
        _frame = new PhotoFrame(_context);
        _frame.loadPhotoList();
        if (_frame.hasNext())
        {
            startTimer();
        }

        return _frame.next();
    }

    private void startTimer()
    {
        _timer = new Timer("nextPhoto");
        _lastPhotoChange = System.currentTimeMillis();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                long now = System.currentTimeMillis();

                if (_hideMessage != null && now > _hideMessage.longValue())
                {
                    AsyncTask<Object, Object, Object> asyncTask = new AsyncTask<Object, Object, Object>()
                    {
                        @Override
                        protected Object doInBackground(Object... params)
                        {
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object o)
                        {
                            _messageText.setVisibility(View.INVISIBLE);
                        }
                    };
                    asyncTask.execute();
                    _hideMessage = null;
                }

                // See if it's been long enough that we should swap in a new photo
                if (!_loading && now - _lastPhotoChange >= _delay * 1000)
                {
                    _lastPhotoChange = now;
                    _loading = true;
                    AsyncTask<Object, Object, DisplayablePhoto> asyncTask = new AsyncTask<Object, Object, DisplayablePhoto>()
                    {
                        @Override
                        protected DisplayablePhoto doInBackground(Object... params)
                        {
                            return _frame.next();
                        }

                        @Override
                        protected void onPostExecute(DisplayablePhoto bitmap)
                        {
                            GetPhotosTask.this.onPostExecute(bitmap);
                            _loading = false;
                        }
                    };
                    asyncTask.execute();
                }
            }
        };
        // Fire our task once a second
        _timer.scheduleAtFixedRate(task, 1000, 1000);
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
