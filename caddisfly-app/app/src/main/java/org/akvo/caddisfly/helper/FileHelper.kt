/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */
package org.akvo.caddisfly.helper

import android.os.Environment
import android.widget.Toast
import org.akvo.caddisfly.app.CaddisflyApp
import java.io.File
import java.util.*

object FileHelper {

    /**
     * Folder where survey result images and photos are stored temporarily
     */
    @JvmStatic
    fun getFormImagesFolder(): File {
        return getFolder("form")
    }

    /**
     * Folder for strip test color card images for unit testing
     */
    @JvmStatic
    fun getUnitTestImagesFolder(): File {
        return getFolder("qa")
    }

    /**
     * Folder for screenshots when running instrumented tests
     */
    fun getScreenshotFolder(): File {
        return getFolder("screenshots")
    }

    private fun getFolder(name: String): File {
        val storageDir = Objects.requireNonNull(CaddisflyApp.getApp())
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val dir = File(storageDir!!.absolutePath + File.separator + name)
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(
                CaddisflyApp.getApp(),
                "Error creating folder.", Toast.LENGTH_SHORT
            ).show()
        }
        return dir
    }

    /**
     * To clear the temporary survey form images and photos
     */
    fun cleanResultImagesFolder() {
        val imagesFolder = getFormImagesFolder()
        val files = imagesFolder.listFiles()
        if (files != null) {
            for (tempFile in imagesFolder.listFiles()!!) {
                tempFile.delete()
            }
        }
    }
}