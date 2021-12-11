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

import android.widget.Toast
import org.akvo.caddisfly.app.CaddisflyApp
import org.akvo.caddisfly.common.BuildConstants
import org.akvo.caddisfly.preference.AppPreferences
import org.akvo.caddisfly.util.FileUtil
import java.io.File

/**
 * The different types of files.
 */
enum class FileType {
    CARD, TEST_IMAGE, RESULT_IMAGE
}

object FileHelper {
    /**
     * The user created configuration file name.
     */
// Folders
    private val ROOT_DIRECTORY = File.separator + BuildConstants.APP_FOLDER
    private val DIR_TEST_IMAGE = (ROOT_DIRECTORY
            + File.separator + "qa" + File.separator + "test-image") // Images saved for testing
    private val DIR_CARD = (ROOT_DIRECTORY
            + File.separator + "qa" + File.separator + "color-card") // Color card for debugging
    private val DIR_RESULT_IMAGES = (ROOT_DIRECTORY
            + File.separator + "result-images") // Images to be sent with result to dashboard

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type FileType to determine the type of resource attempting to use.
     * @return File representing the root directory for the given FileType.
     */
    @JvmStatic
    fun getFilesDir(type: FileType): File {
        return getFilesDir(type, "")
    }

    /**
     * Get the appropriate files directory for the given FileType. The directory may or may
     * not be in the app-specific External Storage. The caller cannot assume anything about
     * the location.
     *
     * @param type    FileType to determine the type of resource attempting to use.
     * @param subPath a sub directory to be created
     * @return File representing the root directory for the given FileType.
     */
    @JvmStatic
    fun getFilesDir(type: FileType, subPath: String): File {
        val path: String = when (type) {
            FileType.CARD -> FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + DIR_CARD
            FileType.RESULT_IMAGE -> FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + DIR_RESULT_IMAGES
            FileType.TEST_IMAGE -> FileUtil.getFilesStorageDir(CaddisflyApp.getApp(), false) + DIR_TEST_IMAGE
        }
        var dir = File(path)
        if (subPath.isNotEmpty()) {
            dir = File(dir, subPath)
        }
        // create folder if it does not exist
        if (!dir.exists() && !dir.mkdirs() && AppPreferences.getShowDebugInfo()) {
            Toast.makeText(CaddisflyApp.getApp(),
                    "Error creating folder: " + dir.absolutePath, Toast.LENGTH_SHORT).show()
        }
        return dir
    }

    fun cleanResultImagesFolder() {
        val imagesFolder = getFilesDir(FileType.RESULT_IMAGE)
        val files = imagesFolder.listFiles()
        if (files != null) {
            for (tempFile in imagesFolder.listFiles()!!) {
                tempFile.delete()
            }
        }
    }
}