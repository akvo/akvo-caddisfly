/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.opencv.android;

/**
 * Installation callback interface.
 */
public interface InstallCallbackInterface {
    /**
     * New package installation is required.
     */
    int NEW_INSTALLATION = 0;
    /**
     * Current package installation is in progress.
     */
    int INSTALLATION_PROGRESS = 1;

    /**
     * Target package name.
     *
     * @return Return target package name.
     */
    String getPackageName();

    /**
     * Installation is approved.
     */
    void install();

    /**
     * Installation is canceled.
     */
    void cancel();

    /**
     * Wait for package installation.
     */
    void wait_install();
}
