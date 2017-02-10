Akvo Caddisfly
==============

[![Build Status](https://travis-ci.org/akvo/akvo-caddisfly.svg?branch=develop)](https://travis-ci.org/akvo/akvo-caddisfly) [![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](http://www.gnu.org/licenses/gpl-3.0)

Akvo Caddisfly is a simple, low cost, open source, smartphone-based drinking water testing system connected to an online data platform.


Build with Android Studio
-------------------------

1. Clone Project

2. Open project in Android Studio

3. Build / Run


Build from Command Line
-----------------------

1. Clone project and create a file named `local.properties` in the project folder with
   the below configuration setting (ensure correct path to local sdk)

    ```
    sdk.dir=path/to/android/sdk
    ```

2. Run build command in project folder

    ```
    gradlew assembleDebug
    ```

3. Install apk to device from `Caddisfly/build/apk` folder



