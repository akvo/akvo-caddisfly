package org.akvo.caddisfly.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import androidx.appcompat.widget.AppCompatImageView;

//https://medium.com/@felipegi91_89910/thanks-daniele-bottillo-b57caf823e34
public class DrawableMatcher extends TypeSafeMatcher<View> {

    //    private static final int EMPTY = -1;
    private static final int ANY = -2;
    private final int expectedId;
    private String resourceName;

    private DrawableMatcher(int expectedId) {
        super(View.class);
        this.expectedId = expectedId;
    }

    public static Matcher<View> hasDrawable() {
        return new DrawableMatcher(DrawableMatcher.ANY);
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected boolean matchesSafely(View target) {
        Bitmap backgroundBitmap = null;
        Bitmap resourceBitmap = null;
        Class<?> clazz = target.getClass();

        if (clazz == AppCompatImageView.class) {
            AppCompatImageView image = (AppCompatImageView) target;

            if (expectedId == ANY) {
                return image.getDrawable() != null;
            }

            if (expectedId < 0) {
                return image.getBackground() == null;
            }
            resourceBitmap = drawableToBitmap(image.getDrawable());
            backgroundBitmap = drawableToBitmap(image.getBackground());
        }

        if (clazz == ImageView.class) {
            ImageView image = (ImageView) target;

            if (expectedId == ANY) {
                return image.getDrawable() != null;
            }

            if (expectedId < 0) {
                return image.getBackground() == null;
            }
            resourceBitmap = drawableToBitmap(image.getDrawable());
            backgroundBitmap = drawableToBitmap(image.getBackground());
        }

        Resources resources = target.getContext().getResources();
        Drawable expectedDrawable = resources.getDrawable(expectedId);
        resourceName = resources.getResourceEntryName(expectedId);

        if (expectedDrawable == null) {
            return false;
        }

        Bitmap otherBitmap = drawableToBitmap(expectedDrawable);

        return (resourceBitmap != null && resourceBitmap.sameAs(otherBitmap)) ||
                (backgroundBitmap != null && backgroundBitmap.sameAs(otherBitmap));
    }

//    private Bitmap getBitmap(Drawable drawable) {
//        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//        drawable.draw(canvas);
//        return bitmap;
//    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with drawable from resource id: ");
        description.appendValue(expectedId);
        if (resourceName != null) {
            description.appendText("[");
            description.appendText(resourceName);
            description.appendText("]");
        }
    }
}