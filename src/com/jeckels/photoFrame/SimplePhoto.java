package com.jeckels.photoFrame;

import android.graphics.Point;

/**
 * Created by Josh on 1/28/2015.
 */
public class SimplePhoto extends Scoreable<Point>
{
    private int photoId;
    private int height;
    private int width;
    private String filename;
    private String _caption;

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getUrl() {
        return PhotoStore.BASE_URL + "/imageMapper/" + filename;
    }

    public String getCaption()
    {
        return _caption;
    }

    public void setCaption(String caption)
    {
        _caption = caption;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePhoto that = (SimplePhoto) o;

        if (photoId != that.photoId) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return photoId;
    }

    @Override
    public int getScore(Point scoringKey)
    {
        if (height > width * 2.5 || width > height * 2.5)
        {
            // Panoramas won't render all that well
            return 1;
        }

        // Prefer images that are in the right aspect ratio for our orientation
        if (scoringKey.y > scoringKey.x)
        {
            return height >= width ? 5 : 3;
        }
        return width >= height ? 5 : 3;
    }
}
