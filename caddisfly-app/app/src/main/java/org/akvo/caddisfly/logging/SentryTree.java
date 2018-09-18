package org.akvo.caddisfly.logging;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;
import timber.log.Timber;

public class SentryTree extends Timber.Tree {
    @Override
    protected void log(int priority, @Nullable String tag, @NotNull String message,
                       @Nullable Throwable t) {

        switch (priority) {

            case Log.INFO:
                Sentry.getContext().recordBreadcrumb(new BreadcrumbBuilder()
                        .setMessage(message)
                        .build());
                break;

            case Log.ERROR:
                if (t == null)
                    Sentry.capture(message);
                else
                    Sentry.capture(t);
                break;
        }

    }
}
