package com.jeckels.photoFrame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoDisplayActivity extends Activity
{
    private GetPhotosTask _task;

    private GetPhotosTask.DisplayablePhoto _currentPhoto;

    private GestureDetector _gestureDetector;

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return _gestureDetector.onTouchEvent(event);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        setContentView(R.layout.main);

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        final TextView textView = (TextView) findViewById(R.id.imageCaption);
        final TextView messageText = (TextView) findViewById(R.id.messageText);
        messageText.setVisibility(View.INVISIBLE);
        _gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener()
        {
            private int _scroll = 0;

            @Override
            public boolean onSingleTapUp(MotionEvent e)
            {
                textView.setVisibility(textView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://jeckels.com/photoDetail?PhotoId=" + _currentPhoto.getPhoto().getPhotoId() + "&ReferringCategoryId=" + _currentPhoto.getCategory().getCategoryId()));
                startActivity(browserIntent);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
            {
                _scroll += (int)distanceX;
                if (Math.abs(_scroll) > 50)
                {
                    int units = _scroll / 50;
                    _scroll -= units * 50;
                    _task.adjustTimer(units);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });



        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            _task = new GetPhotosTask(getBaseContext(), messageText)
            {
                @Override
                protected void onPostExecute(DisplayablePhoto photo)
                {
                    imageView.setImageBitmap(photo.getBitmap());
                    textView.setText(photo.getPhoto().getCaption());
                    _currentPhoto = photo;
                }
            };
            _task.execute();
//            }
//            else
//            {
//                // display error
//            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        _task.cancel();
        finish();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        _task.cancel();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
