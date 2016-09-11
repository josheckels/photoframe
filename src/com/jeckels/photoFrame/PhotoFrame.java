package com.jeckels.photoFrame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

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
    private final Context _context;

    private PhotoStore _photoStore;

    private ListIterator<SimpleCategory> _categoryIterator;
    private ListIterator<SimplePhoto> _photoIterator;
    private SimpleCategory _category;

//    private final Random _random  = new Random(54375843);
    private final Random _random  = new Random();

    public PhotoFrame(Context context)
    {
        _context = context;
    }


    public void loadPhotoList()
    {
        _photoStore = new PhotoStore();
        _photoStore.load();

        resetIterator();
    }

    private void resetIterator()
    {
        Calendar cal = new GregorianCalendar();

        Collection<SimpleCategory> selectedCategories = randomlySelect(_photoStore.getCategories(), cal, 10);

        _categoryIterator = new ArrayList<>(selectedCategories).listIterator();
        _category = _categoryIterator.next();
        _photoIterator = createPhotoIterator(_category);
    }

    private <S extends Scoreable<K>, K> Collection<S> randomlySelect(Collection<S> scoreables, K scoringKey, int count)
    {
        List<S> all = new ArrayList<>(scoreables);
        int totalScore = 0;
        for (S item : all)
        {
            totalScore += item.getScore(scoringKey);
        }

        // Pick the items
        Set<S> selected = new LinkedHashSet<>(count);
        while (selected.size() < Math.min(count, all.size()))
        {
            int selectedScore = _random.nextInt(totalScore);
            int cumulativeScore = 0;
            for (S item : all)
            {
                cumulativeScore += item.getScore(scoringKey);
                if (cumulativeScore >= selectedScore)
                {
                    selected.add(item);
                    break;
                }
            }
        }
        return selected;
    }

    public boolean hasNext()
    {
        return true;
    }

    public GetPhotosTask.DisplayablePhoto next()
    {
        if (!_photoIterator.hasNext())
        {
            if (!_categoryIterator.hasNext())
            {
                resetIterator();
            }
            else
            {
                _category = _categoryIterator.next();
                _photoIterator = createPhotoIterator(_category);
                if (!_photoIterator.hasNext())
                {
                    return next();
                }
            }
        }
        try
        {
            SimplePhoto photo = _photoIterator.next();
            Bitmap bitmap = downloadPhoto(photo);

            return new GetPhotosTask.DisplayablePhoto(bitmap, photo, _category, _photoStore);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private ListIterator<SimplePhoto> createPhotoIterator(SimpleCategory category)
    {
        List<SimplePhoto> allPhotos = new ArrayList<>(category.getPhotos(_photoStore));

        List<SimplePhoto> selectedPhotos;
        // Get up to 10 photos from the category
        int targetSize = 3;
        if (allPhotos.size() < 10)
        {
            targetSize = 5;
        }
        else if (allPhotos.size() < 20)
        {
            targetSize = 7;
        }
        else if (allPhotos.size() >= 20)
        {
            targetSize = 10;
        }

        WindowManager wm = (WindowManager) _context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);

        selectedPhotos = new ArrayList<>(randomlySelect(allPhotos, screenSize, targetSize));

        Collections.sort(selectedPhotos, new Comparator<SimplePhoto>()
        {
            @Override
            public int compare(SimplePhoto lhs, SimplePhoto rhs)
            {
                return Integer.valueOf(lhs.getPhotoId()).compareTo(rhs.getPhotoId());
            }
        });
        return selectedPhotos.listIterator();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap downloadPhoto(SimplePhoto photo) throws IOException
    {
        InputStream is = null;

        try
        {
            URL url = new URL(photo.getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outHeight = photo.getHeight();
            options.outWidth = photo.getWidth();
            options.inSampleSize = calculateInSampleSize(options, 1500, 1500);
            return BitmapFactory.decodeStream(is, null, options);
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
