package com.jeckels.photoFrame;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by Josh on 1/29/2015.
 */
public class PhotoStore
{
    private Map<Integer, SimpleCategory> _categories = new HashMap<>();
    private Map<Integer, SimplePhoto> _photos = new HashMap<>();

    public static final String BASE_URL = "http://jeckels.com";

    public PhotoStore()
    {
    }

    public Collection<SimpleCategory> getCategories()
    {
        return Collections.unmodifiableCollection(_categories.values());
    }

    public void load()
    {
        try
        {
            List<SimpleCategory> categories = fromJSON(new TypeReference<List<SimpleCategory>>(){}, new URL(BASE_URL + "/categoryList?json=true"));

            for (SimpleCategory category : categories)
            {
                _categories.put(category.getCategoryId(), category);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<SimplePhoto> loadPhotos(SimpleCategory category)
    {
        try
        {
            List<SimplePhoto> photos = fromJSON(new TypeReference<List<SimplePhoto>>()
            {
            }, new URL(BASE_URL + "/category/" + category.getCategoryId() + ".json"));
            for (SimplePhoto photo : photos)
            {
                _photos.put(photo.getPhotoId(), photo);
            }
            return photos;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJSON(final TypeReference<T> type, final URL url) throws IOException
    {
        return new ObjectMapper().readValue(url, type);
    }

    public SimpleCategory getCategory(int i)
    {
        return _categories.get(i);
    }
}
