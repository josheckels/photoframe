package com.jeckels.photoFrame;

/**
 * Created by Josh on 1/31/2015.
 */
public abstract class Scoreable<K>
{
    public abstract int getScore(K scoringKey);
}