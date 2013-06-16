package com.jeckels.photoFrame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ImageView;
import org.slf4j.LoggerFactory;

public class PhotoDisplayActivity extends Activity
{
    private LoggerFactory _loggerFactory;
    private GetPhotosTask _task;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String flickrId = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, null);

        if (flickrId == null)
        {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.main);

            final ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(PhotoDisplayActivity.this, SettingsActivity.class));
                }
            });


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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        _task.cancel();
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
