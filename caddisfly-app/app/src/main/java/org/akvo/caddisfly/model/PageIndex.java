package org.akvo.caddisfly.model;

import android.util.SparseArray;

public class PageIndex {
    private SparseArray<PageType> pages = new SparseArray<>();
    private int resultIndex = -1;
    private int skipToIndex = -1;

    public void setPhotoIndex(int index) {
        pages.put(index, PageType.PHOTO);
    }

    public void setInputIndex(int index) {
        pages.put(index, PageType.INPUT);
    }

    public int getResultIndex() {
        return resultIndex;
    }

    public void setResultIndex(int resultIndex) {
        this.resultIndex = resultIndex;
    }

    public int getSkipToIndex() {
        return skipToIndex;
    }

    public void setSkipToIndex(int skipToIndex) {
        this.skipToIndex = skipToIndex;
    }

    public PageType getType(int position) {
        return pages.get(position);
    }
}
