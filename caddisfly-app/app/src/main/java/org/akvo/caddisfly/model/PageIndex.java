package org.akvo.caddisfly.model;

public class PageIndex {
    private int photoIndex = -1;
    private int inputIndex = -1;
    private int resultIndex = -1;
    private int skipToIndex = -1;

    public int getPhotoIndex() {
        return photoIndex;
    }

    public void setPhotoIndex(int photoIndex) {
        this.photoIndex = photoIndex;
    }

    public int getInputIndex() {
        return inputIndex;
    }

    public void setInputIndex(int inputIndex) {
        this.inputIndex = inputIndex;
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
}
