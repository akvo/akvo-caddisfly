v0.61 alpha
-----------
1. Added option to disable interrupting test if device is shaken
2. EC Sensor: Round off EC and Temperature values

v0.58 - v0.60 alpha
-----------
1. Fixes for EC Sensor
2. Avoid displaying a -1 EC value

v0.57 alpha
-----------
1. DevMode: Toast messages for ec sensor readings
2. Refresh settings screen when dev mode is selected
3. UI changes

v0.56 alpha
-----------
1. Camera flash detection only for colorimetric tests
2. Allow landscape mode for tablets
3. UI changes for tablet
4. Fixed: Update did not give option to launch app after install

v0.55 alpha
-----------
1. Increased number of interpolated color swatches
2. Moved load/save buttons to dev mode
3. Removed result values in calibration message
4. Fixed: Settings not opening on first click
5. Load file names sorted

v0.54 alpha
-----------
1. White is a valid swatch color
2. Cancel button removed from dilution dialog
3. Dilution label in start screen
4. Removed calibrate detail screen
5. Minor bug fixes

v0.53 alpha
-----------
1. EC Reference value displayed in bolder font
2. Start screen for colorimetric tests added
3. Calibrate EC sensor option in settings
4. Minor UI changes

v0.52 alpha
-----------
1. Minor UI changes and bug fixes

v0.51 alpha
-----------
1. Fix dilution warning calculation bug
2. Grouping of preferences in settings screen

v0.50 alpha
-----------
1. Dilution message if Fluoride level is above 1.8 ppm
2. Attempt to focus camera at infinity

v0.49 alpha
-----------
1. 'Developer mode' for installing cartridge, extracting images

v0.48 alpha
-----------
1. EC Sensor connectivity
2. 'Select Dilution' dialog on start of Fluoride test (hard coded for now but will work with json file in future)
3. No fixed interval required for swatch ranges. Sample json file attached. example:`"ranges": "0, 10, 20, 30, 50",`
4. The calibration swatch file to load can be created manually like below
example:
    0.0 = 255  88  177
    0.5 = 255  110  157
    1.0 = 255  139  137
    1.5 = 253  174  74
    2.0 = 244  180  86
    2.5 = 236  172  81
    3.0 = 254  169  61
5. Small circle on camera preview screen for aligning cartridge
6. Longer delay before starting test and in between camera shots to allow for flash to cool down.
7. Swatch validation disabled
8. Removed reliance on separate developer app for enabling controls 
9. Link to demo video on vimeo
10. For now EC and EC temperature are two separate tests and thereby two questions in survey

**Known Issues**
1. Warning if dilution is required is currently not working. To be fixed

v0.44 alpha
-----------
1. Multiple range testing to cater for higher ranges
2. Save calibration to file

v0.43 alpha
-----------
1. Moved test properties to json file
2. Test type selection dropdown added on calibration screen
3. Integration with new Flow App 2+
4. Removed one step calibration
5. Test name added on test progress screen
6. Added save original photo preference
7. Trim data folders to max of 20 folders

v0.42 alpha
-----------
1. High Range test added
2. UI changes for small screens
3. Check for camera flash
4. Sound effect on calibration

v0.41 alpha
-----------
1. Beep sounds during test
2. Success sound on valid result
3. Error sound on invalid test
4. Shake disabled
5. Show start page option
6. One step calibration fix
7. Translations added
8. Retry issue fix

v0.36 - v0.40 alpha
-------------------
1. Green UI styles
2. Version update loop bug fixed
3. Screen display refresh on calibration bug fixed
4. Seven step calibration is default
5. Hide developer options if Geek version is not installed
6. Removed code for pH, bacteria, etc...
7. Saves images and results in data folder for later analysis
8. Ignore first photo taken during analysis
9. Error result if there is too much variation in photos during analysis
10. Training video added

v0.35 alpha
-----------
1. Added Preparation screen with instructions steps
2. Sliding animation added for progress steps screens
3. Photo Sample Dimension default set to 100px
4. About dialog added to Settings screen options
5. Developer: Setting to use 7 step calibration

v0.34 alpha
-----------
1. Fix: detect interruption of test by phone movement
2. Revert to previous version option
3. One minute image displayed on screen to user
4. User App: Preferences screen added

v0.33 alpha
-----------
1. Two step calibration for fluoride will be default
2. Stop test if phone is shaken during test
3. Calibration error will now reset the color to null
4. Display brightness value for calibration and test images
5. User App: Welcome dialog if calibration not done
6. User App: ppm values for calibration changed to color names

v0.32 alpha
-----------
1. Photo Sample Dimension default set to 150px
2. Tweaks: Detection of Shake
3. User app: Fix: Test failed error message not showing
4. Load and Save icons brightened up for dark theme

v0.31 alpha
-----------
1. User app: Light theme
2. Fix: Calibration not saving

v0.30 alpha
-----------
1. Gradient distance displayed along with swatch rgb value
2. Overwrite message on save calibration
3. Long click to delete saved calibration
4. Tweaks: Calibration validation
5. User app: Result popup message before returning to survey after test
    
v0.29 alpha
-----------
1. Shake phone feature added to calibration
2. Esperanto, Hebrew, Kannada language translations added
3. Save and Load calibrations 
4. Tweaks: Detect phone position is flat to the ground
5. Tweaks: Image quality minimum reduced to 50%
6. Tweaks: Calibration validation
7. User app: Language menu added to home page

v0.28 alpha
-----------
1. Two variants of the app released (Internal and External User)
2. Basic validation of calibration. Colors too similar or very different will be considered invalid
3. Error number badge on Calibrate button on home screen
4. Setting: Ignore calibration errors (default false)
5. Setting: Crop to square only - (default false)
6. Tweaks: Image quality algorithm

v0.27 alpha
-----------
1. Prevent camera preview from closing and opening during multiple analysis
2. Check if phone is placed in a level position on test start (as of now only works if shake is on)
3. Initial delay on start of test increased by a few seconds
4. Prevent invalid entry of rgb value on edit
5. Some minor UI changes (changed: icons, margins, settings summary...)
6. Cloudy settings defaults to true
7. Minimum photo quality defaults to 60%
8. Added alert with exception message on send fail
9. Updated Library : android-async-http 1.4.5
10. Split code into two flavors (Internal (for research purpose) and External (for actual users))

v0.26 alpha
-----------
1. Multiple sampling for calibration (result will be an average color)
2. Cropping photo to circle before analysis (to match the cartridge shape)
3. Setting : Photo sample dimension, default: 200
4. Setting : Save original photo, default: false (to avoid running out of space)
5. Some UI changes on calibration and result screens
6. Fix: Photo quality check
7. Removed Speedometer UI
8. Removed line chart

v0.25 alpha
-----------
1. Setting: Sampling Count - number of takes for each analysis (ignored for Bacteria test)
2. Edit option in calibrate screen to enter rgb manually
3. Result rounded to two decimal places instead of one
4. Result of each take displayed in details page
5. Simple countdown display on bacteria test
6. Hindi language
7. Fix: Calibrate screen sometimes crashing
8. Fix: Phone sometimes not sleeping on bacteria test

v0.24 alpha
-----------
1. Auto click analyze on preview (option in settings)
2. color analysis ignores white, black and gray in the photo
3. Fluoride easy type- just as a test (calibrate 0 and 3 only)
4. fix: Bacteria test fails on phone sleep

v0.23 alpha
-----------
1. Changed photo sampling dimension 

v0.22 alpha
-----------
1. Zoom setting ignored (assumption: digital zoom may ruin image). The setting remains in settings for preview purpose
2. Attempt to force focus to center of image (essentially disabling auto focus)
3. Camera shutter sound setting
4. A square highlight on preview screen as a guide for part of the image used for analysis

v0.21 alpha
-----------
1. Camera Preview added (to fix camera not working on Nexus and MotoG)
2. Removed preset calibration swatches (as each phone camera sees colors differently)
3. Show error message if device has not been calibrated when user starts a test

v0.20 alpha
-----------
1. Logo changed (Insect wings logo)
2. pH test added

v0.19 alpha
-----------
1. Bug fixes and stability issues

v0.18 alpha
-----------
1. Bug fixes and stability issues

v0.17 alpha
-----------
1. Photo quality range 0 - 100%
2. Analysis count 2 - 100
3. Ignores quality check on bacteria test
4. Images cropped to 600x600 in small folder

v0.16 alpha
-----------
1. Logo changed
2. Navigation menu changes

v0.15 alpha
-----------
1. Logo changed
 
v0.14 alpha
-----------
1. Setting: Minimum photo quality

v0.13 alpha
-----------
1. Speedometer UI for photo quality on calibration screen

v0.12 alpha
-----------
1. Photo quality check on calibration and test

v0.11 alpha
-----------
1. Calibration details screen

v0.10 alpha
-----------
1. UI enhancements
2. Stability issues fixed

v0.9 alpha
-----------
1. SQLite DB to hold test and location information
2. Change dashboard UI to first select or add location before starting a test
3. Settings: Cloudy option for camera 
4. Settings: Torch option for camera  
5. Settings: Infinity option for camera (doesn't seem to affect anything)
6. Settings: Zoom setting with preview 
7. Location entry form window
8. Get location via GPS
9. Option to calibrate different test types

v0.8 alpha
-----------
1. Buttons for different types of test (Fluoride, E.coli, Turbidity, Nitrate, Iron, Arsenic)
2. Language translation
3. Select/Add location before starting a test

v0.7 alpha
-----------
1. Light and Dark themes
2. Shake device to start a test
3. Alarm sound when shake is required
4. Help screen

v0.6 alpha
-----------
1. Bacteria interval changed to minutes instead of seconds
2. Camera sound setting

v0.5 alpha
-----------
1. Location entry dialog

v0.4 alpha
-----------
1. Navigation drawer menu
2. Select type of test dialog on start test
3. Calibrate Screen

v0.3 alpha
-----------
1. Bacteria test
2. Test history list
3. Preferences/Settings screen
4. Check update option
5. About Screen
6. Result Details Screen
7. Delete test result

v0.2 alpha
-----------
1. Simple calibrate option

v0.1 alpha
-----------
1. Simple Colorimeter
2. Result Screen
3. Swatches List
