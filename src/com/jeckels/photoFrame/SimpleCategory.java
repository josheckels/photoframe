package com.jeckels.photoFrame;

import java.util.*;

/**
 * Created by Josh on 1/29/2015.
 */
public class SimpleCategory extends Scoreable<Calendar>
{
    private int _categoryId;
    private String _description;
    private Calendar _createdOn;
    private int _photoCount;

    private Integer _score;

    private List<SimplePhoto> _photos;
    private Integer _parentCategoryId;

    public SimpleCategory()
    {
    }

    public int getCategoryId() {
        return _categoryId;
    }

    public void setCategoryId(int categoryId) {
        _categoryId = categoryId;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setParentCategoryId(Integer parentCategoryId)
    {
        _parentCategoryId = parentCategoryId;
    }

    public Integer getParentCategoryId()
    {
        return _parentCategoryId;
    }

    public Calendar getCreatedOn()
    {
        return _createdOn;
    }

    public void setCreatedOn(Date createdOn)
    {
        _createdOn = new GregorianCalendar();
        _createdOn.setTime(createdOn);
    }

    public int getPhotoCount()
    {
        return _photoCount;
    }

    public void setPhotoCount(int photoCount)
    {
        _photoCount = photoCount;
    }

    public List<SimplePhoto> getPhotos(PhotoStore photoStore)
    {
        if (_photos == null)
        {
            _photos = Collections.unmodifiableList(photoStore.loadPhotos(this));
        }
        return _photos;
    }

    public int getScore(Calendar calendar)
    {
        if (_score == null)
        {
            // Baseline score
            int score = 1;

            // Prefer photos taken around the same date
            int dayDifference = ((calendar.get(Calendar.YEAR) - _createdOn.get(Calendar.YEAR)) * 365) + (calendar.get(Calendar.DAY_OF_YEAR) - _createdOn.get(Calendar.DAY_OF_YEAR));
            dayDifference = Math.abs(dayDifference);
            score += Math.max(0, 365 * 2 - dayDifference) / 30;

            _score = score;
        }
        return _score;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleCategory that = (SimpleCategory) o;

        if (_categoryId != that._categoryId) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return _categoryId;
    }

    @Override
    public String toString()
    {
        return "Category " + _categoryId + ": " + _description;
    }
}
