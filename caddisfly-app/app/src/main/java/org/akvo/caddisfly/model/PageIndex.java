package org.akvo.caddisfly.model;

import android.util.SparseArray;

import java.util.ArrayList;

public class PageIndex {
    private SparseArray<PageType> pages = new SparseArray<>();
    private ArrayList<Integer> inputIndexes = new ArrayList<>();
    private ArrayList<Integer> photoIndexes = new ArrayList<>();
    private int resultIndex = -1;
    private int skipToIndex = -1;
    private int skipToIndex2 = -1;

    public void setPhotoIndex(int index) {
        pages.put(index, PageType.PHOTO);
        photoIndexes.add(index);
    }

    public void setInputIndex(int index) {
        pages.put(index, PageType.INPUT);
        inputIndexes.add(index);
    }

    public int getPhotoPageIndex(int index) {
        if (photoIndexes.size() > index) {
            return photoIndexes.get(index);
        } else {
            return -1;
        }
    }

    public int getInputPageIndex(int index) {
        if (inputIndexes.size() > index) {
            return inputIndexes.get(index);
        } else {
            return -1;
        }
    }

    public int getResultIndex() {
        return resultIndex;
    }

    public void setResultIndex(int index) {
        pages.put(index, PageType.RESULT);
        resultIndex = index;
    }

    public int getSkipToIndex() {
        return skipToIndex;
    }

    public void setSkipToIndex(int value) {
        skipToIndex = value;
    }

    public int getSkipToIndex2() {
        return skipToIndex2;
    }

    public void setSkipToIndex2(int value) {
        skipToIndex2 = value;
    }

    public PageType getType(int position) {
        if (pages.indexOfKey(position) < 0) {
            return PageType.DEFAULT;
        } else {
            return pages.get(position);
        }
    }

    public void clear() {
        pages.clear();
        photoIndexes.clear();
        inputIndexes.clear();
    }
}
