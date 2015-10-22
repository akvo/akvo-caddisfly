v0.92 beta
----------
Fix screen flash when camera alignment preview is started

v0.91 beta
----------
1. Release for Beta
2. Added software licences dialog
3. Fix: Expect 2 letter language code from FLOW

v0.90 beta
----------
1. Change of 'Waiting for phone placement' illustration
2. Fixes in expiry date edit dialog
3. Fix rotation double instance issue on activities
4. Style and refactorings

v0.89 beta
----------
1. Fix camera flash check for mobiles without flash
2. Disable feature for auto detect of USB device
3. Style and theme refactoring

v0.88 beta
----------
1. Release for QA

v0.87 beta
----------
1. Style changes for main screen

v0.86 beta
----------
1. Added shared id, process and task affinity to launch in same process as FLOW
2. Changed protocol for EC Sensor as per new specifications
        R - Will return the current temperature and Compensated EC
3. Fix calibration batch expiry save per test type (WIP)
4. Added Survey button to main screen
5. Added Electrical Conductivity button to main screen
6. Changed EC Result screen to display only EC result or Temperature
7. Diagnostic: Added Pesticide test
8. Diagnostic: Added option to turn off detection of backdrop

v0.85 beta
----------
1. Fix: Camera Preview circle

v0.84 beta
----------
1. Added preview on test start screen to align chamber for generic back case

v0.83 beta
----------
1. Fix: Backdrop detection fixes
2. Check if reagent has expired before running test

v0.82 beta
----------
1. Fix: Sounds not playing fully
2. Diagnostic: Fix: Invalid calibration file load error not showing

v0.81 beta
----------
1. Design changes
3. Save calibration details (WIP)

v0.80 beta
----------
1. Attempt to detect the backdrop in the chamber
2. Design changes
3. Removed Update check feature
4. Diagnostic: Beep sound added on preview test
5. Diagnostic: Removed HSV color analysis
6. Diagnostic: Removed Camera2 api preview

v0.79 beta
----------
1. Main screen design changes
2. Calibrate link moved to main screen
3. 'Check update' link moved to Diagnostic settings
4. Calibration details section added (WIP)
5. Activity transition sliding animation added
6. Buttons ripple effects added
7. Diagnostic: Camera preview run test by placing phone face down
8. Diagnostic: Camera preview single click to run test
9. Diagnostic: Coliforms test added
10. Diagnostic: Camera2 api testing

v0.78 beta
----------
1. Action bar style changed to theme default
2. Start Survey button removed from main activity
3. Removed app from recent task list
4. Some style improvements
5. Diagnostic: Battery level added to image file name
6. Diagnostic: Calibration (C) or Test (T) info added image name

v0.77 beta
----------
1. Diagnostic: Camera preview runs a test if device placed flat face down
2. Diagnostic: Nearest matching color and distance displayed even if test fails

v0.76 beta
----------
1. Fix: update download progress bar
2. Calibration validation and error message
3. Fix: White screen flash on app start
4. External folder name changed to Akvo Caddisfly
5. Update apk file save to internal folder instead of downloads
6. Removed external usb camera code
7. Fix: Alert window not dismissed on activity finish
8. Dilution dialog changed to normal screen
9. Download update apk file and validate checksum
10. Diagnostic: Added device details, date and other info to calibration file save
11. Diagnostic: Added reagent expiry date dialog to save calibration
12. Diagnostic: Sampled image is saved to image folder
13. Diagnostic: Display lab, rgb and hsv results
14. Color hsv value displayed with swatches

v0.75 beta
----------
1. Change alpha to Beta in version number
2. Fix: High level showing % value instead of number of times
3. Fix: EC Sensor recording a -1 value
4. Fix: Use dot (.) separator for app version number in locales using commas
5. Fix: Too many decimal places being recorded. Change to two places
6. Fix: Camera Preview should dismiss only on back button
7. UI: Change Ok and Cancel button in update dialog to 'Update' and 'Not Now'
8. Warning message added for EC Sensor calibrate
9. Diagnostic: Sampling times option
10. Diagnostic: Quick colorimetric test
11. Diagnostic: Max color distance option
12. Changes to text

v0.74 alpha
-----------
1. Changed from 25% dilution to 20% (5 times) dilution

v0.73 alpha
-----------
1. Get dilution levels from json config
2. Removed org logo and link from About screen
3. Fix: High contamination level message box width issue
4. Removed ternup external folder
5. Moved calibrate saved files to akvo external folder
6. Some minor UI enhancements
7. Added UIAutomator to automated tests

v0.72 alpha
-----------
1. Start app with default user language
2. Fix: French decimal comma issue in calibration
3. Fix: Back button not working issue on start test
4. Fix: Rounding issue with EC result to survey
5. Revised French translations
6. Language selection for English and French in settings
7. Removed demo link
8. Removed extra blank line in Test types list
9. Added Back Home button on Calibrate Start screen
10. Diagnostic mode: Added quality value to camera preview
11. Some minor UI enhancements

v0.71 alpha
-----------
1. Change in camera take picture to fix issue with Xiaomi phone
2. Changed Developer mode title to Diagnostic mode
3. Change App language based on Akvo Flow app language
4. Moved language option in settings to Diagnostic settings
5. Diagnostic mode: Changed action bar color in diagnostic mode
6. Diagnostic mode: Further additions to diagnostic camera preview information

v0.70 alpha
-----------
1. Default to Torch Mode (Experimental)
2. Reduced color matching tolerance down to 30 (Experimental)
3. Dev mode: torch mode option changed to Use Flash Mode
4. Dev mode: More info on long click on camera preview
5. Bug Fix: Saving and loading of calibration files
6. Bug Fix: Reduced camera photo resolution to accommodate Asus phones
7. Revised json file

v0.69 alpha
-----------
1. Dev mode: Option to use torch mode for camera
2. Dev mode: Additional info on long click on camera preview
3. Some UI style changes
4. Revised default contaminant test types in json file

v0.68 alpha
-----------
1. Dev mode indication and button on start screen
2. Some UI mods and bug fixes

v0.67 alpha
-----------
1. Test type list moved from dropdown to list screen
4. Selected range value displayed on Test Start screen
2. Instructions removed
3. Calibrate option merged in settings screen

v0.66 alpha
-----------
1. changed logo for launcher icon
2. changed background and logo on action bar
3. removed background and logo from about screen
4. added copyright message on about screen
5. fixed settings title text color

v0.65 alpha
-----------
1. lg low res issue: width parameter of picture taken set to be at least between 800 and 1400
2. ui: display units along with range values on calibrate screen
3. Diagnostic: added take picture for testing
4. Diagnostic: no sound option

v0.64 alpha
-----------
1. added ability to use external usb camera

v0.63 alpha
-----------
1. fixed flash not working on preview
2. increased test start initial delay
3. added option to turn on auto focus under dev settings
4. decreased shake interruption sensitivity
5. fixed checkbox style and settings title style

v0.62 alpha
-----------
1. fixed range rounding issue

v0.61 alpha
-----------
1. added option to disable interrupting test if device is shaken
2. ec sensor: round off ec and temperature values

v0.58 - v0.60 alpha
-----------
1. fixes for ec sensor
2. avoid displaying a -1 ec value

v0.57 alpha
-----------
1. devmode: toast messages for ec sensor readings
2. refresh settings screen when dev mode is selected
3. ui changes

v0.56 alpha
-----------
1. camera flash detection only for colorimetric tests
2. allow landscape mode for tablets
3. ui changes for tablet
4. fixed: update did not give option to launch app after install

v0.55 alpha
-----------
1. increased number of interpolated color swatches
2. moved load/save buttons to dev mode
3. removed result values in calibration message
4. fixed: settings not opening on first click
5. load file names sorted

v0.54 alpha
-----------
1. white is a valid swatch color
2. cancel button removed from dilution dialog
3. dilution label in start screen
4. removed calibrate detail screen
5. minor bug fixes

v0.53 alpha
-----------
1. ec reference value displayed in bolder font
2. start screen for colorimetric tests added
3. calibrate ec sensor option in settings
4. minor ui changes

v0.52 alpha
-----------
1. minor ui changes and bug fixes

v0.51 alpha
-----------
1. fix dilution warning calculation bug
2. grouping of preferences in settings screen

v0.50 alpha
-----------
1. dilution message if fluoride level is above 1.8 ppm
2. attempt to focus camera at infinity

v0.49 alpha
-----------
1. 'developer mode' for installing cartridge, extracting images

v0.48 alpha
-----------
1. ec sensor connectivity
2. 'select dilution' dialog on start of fluoride test (hard coded for now but will work with json file in future)
3. no fixed interval required for swatch ranges. sample json file attached. example:`"ranges": "0, 10, 20, 30, 50",`
4. the calibration swatch file to load can be created manually like below
example:
    0.0 = 255  88  177
    0.5 = 255  110  157
    1.0 = 255  139  137
    1.5 = 253  174  74
    2.0 = 244  180  86
    2.5 = 236  172  81
    3.0 = 254  169  61
5. small circle on camera preview screen for aligning cartridge
6. longer delay before starting test and in between camera shots to allow for flash to cool down.
7. swatch validation disabled
8. removed reliance on separate developer app for enabling controls 
9. link to demo video
10. for now ec and ec temperature are two separate tests and thereby two questions in survey

**known issues**
1. warning if dilution is required is currently not working. to be fixed

v0.44 alpha
-----------
1. multiple range testing to cater for higher ranges
2. save calibration to file

v0.43 alpha
-----------
1. moved test properties to json file
2. test type selection dropdown added on calibration screen
3. integration with new flow app 2+
4. removed one step calibration
5. test name added on test progress screen
6. added save original photo preference
7. trim data folders to max of 20 folders

v0.42 alpha
-----------
1. high range test added
2. ui changes for small screens
3. check for camera flash
4. sound effect on calibration

v0.41 alpha
-----------
1. beep sounds during test
2. success sound on valid result
3. error sound on invalid test
4. shake disabled
5. show start page option
6. one step calibration fix
7. translations added
8. retry issue fix

v0.36 - v0.40 alpha
-------------------
1. green ui styles
2. version update loop bug fixed
3. screen display refresh on calibration bug fixed
4. seven step calibration is default
5. hide developer options if geek version is not installed
6. removed code for ph, bacteria, etc...
7. saves images and results in data folder for later analysis
8. ignore first photo taken during analysis
9. error result if there is too much variation in photos during analysis
10. training video added

v0.35 alpha
-----------
1. added preparation screen with instructions steps
2. sliding animation added for progress steps screens
3. photo sample dimension default set to 100px
4. about dialog added to settings screen options
5. developer: setting to use 7 step calibration

v0.34 alpha
-----------
1. fix: detect interruption of test by phone movement
2. revert to previous version option
3. one minute image displayed on screen to user
4. user app: preferences screen added

v0.33 alpha
-----------
1. two step calibration for fluoride will be default
2. stop test if phone is shaken during test
3. calibration error will now reset the color to null
4. display brightness value for calibration and test images
5. user app: welcome dialog if calibration not done
6. user app: ppm values for calibration changed to color names

v0.32 alpha
-----------
1. photo sample dimension default set to 150px
2. tweaks: detection of shake
3. user app: fix: test failed error message not showing
4. load and save icons brightened up for dark theme

v0.31 alpha
-----------
1. user app: light theme
2. fix: calibration not saving

v0.30 alpha
-----------
1. gradient distance displayed along with swatch rgb value
2. overwrite message on save calibration
3. long click to delete saved calibration
4. tweaks: calibration validation
5. user app: result popup message before returning to survey after test
    
v0.29 alpha
-----------
1. shake phone feature added to calibration
2. esperanto, hebrew, kannada language translations added
3. save and load calibrations 
4. tweaks: detect phone position is flat to the ground
5. tweaks: image quality minimum reduced to 50%
6. tweaks: calibration validation
7. user app: language menu added to home page

v0.28 alpha
-----------
1. two variants of the app released (internal and external user)
2. basic validation of calibration. colors too similar or very different will be considered invalid
3. error number badge on calibrate button on home screen
4. setting: ignore calibration errors (default false)
5. setting: crop to square only - (default false)
6. tweaks: image quality algorithm

v0.27 alpha
-----------
1. prevent camera preview from closing and opening during multiple analysis
2. check if phone is placed in a level position on test start (as of now only works if shake is on)
3. initial delay on start of test increased by a few seconds
4. prevent invalid entry of rgb value on edit
5. some minor ui changes (changed: icons, margins, settings summary...)
6. cloudy settings defaults to true
7. minimum photo quality defaults to 60%
8. added alert with exception message on send fail
9. updated library : android-async-http 1.4.5
10. split code into two flavors (internal (for research purpose) and external (for actual users))

v0.26 alpha
-----------
1. multiple sampling for calibration (result will be an average color)
2. cropping photo to circle before analysis (to match the cartridge shape)
3. setting : photo sample dimension, default: 200
4. setting : save original photo, default: false (to avoid running out of space)
5. some ui changes on calibration and result screens
6. fix: photo quality check
7. removed speedometer ui
8. removed line chart

v0.25 alpha
-----------
1. setting: sampling count - number of takes for each analysis (ignored for bacteria test)
2. edit option in calibrate screen to enter rgb manually
3. result rounded to two decimal places instead of one
4. result of each take displayed in details page
5. simple countdown display on bacteria test
6. hindi language
7. fix: calibrate screen sometimes crashing
8. fix: phone sometimes not sleeping on bacteria test

v0.24 alpha
-----------
1. auto click analyze on preview (option in settings)
2. color analysis ignores white, black and gray in the photo
3. fluoride easy type- just as a test (calibrate 0 and 3 only)
4. fix: bacteria test fails on phone sleep

v0.23 alpha
-----------
1. changed photo sampling dimension 

v0.22 alpha
-----------
1. zoom setting ignored (assumption: digital zoom may ruin image). the setting remains in settings for preview purpose
2. attempt to force focus to center of image (essentially disabling auto focus)
3. camera shutter sound setting
4. a square highlight on preview screen as a guide for part of the image used for analysis

v0.21 alpha
-----------
1. camera preview added (to fix camera not working on nexus and motog)
2. removed preset calibration swatches (as each phone camera sees colors differently)
3. show error message if device has not been calibrated when user starts a test

v0.20 alpha
-----------
1. logo changed (insect wings logo)
2. ph test added

v0.19 alpha
-----------
1. bug fixes and stability issues

v0.18 alpha
-----------
1. bug fixes and stability issues

v0.17 alpha
-----------
1. photo quality range 0 - 100%
2. analysis count 2 - 100
3. ignores quality check on bacteria test
4. images cropped to 600x600 in small folder

v0.16 alpha
-----------
1. logo changed
2. navigation menu changes

v0.15 alpha
-----------
1. logo changed
 
v0.14 alpha
-----------
1. setting: minimum photo quality

v0.13 alpha
-----------
1. speedometer ui for photo quality on calibration screen

v0.12 alpha
-----------
1. photo quality check on calibration and test

v0.11 alpha
-----------
1. calibration details screen

v0.10 alpha
-----------
1. ui enhancements
2. stability issues fixed

v0.9 alpha
-----------
1. sqlite db to hold test and location information
2. change dashboard ui to first select or add location before starting a test
3. settings: cloudy option for camera 
4. settings: torch option for camera  
5. settings: infinity option for camera (doesn't seem to affect anything)
6. settings: zoom setting with preview 
7. location entry form window
8. get location via gps
9. option to calibrate different test types

v0.8 alpha
-----------
1. buttons for different types of test (fluoride, e.coli, turbidity, nitrate, iron, arsenic)
2. language translation
3. select/add location before starting a test

v0.7 alpha
-----------
1. light and dark themes
2. shake device to start a test
3. alarm sound when shake is required
4. help screen

v0.6 alpha
-----------
1. bacteria interval changed to minutes instead of seconds
2. camera sound setting

v0.5 alpha
-----------
1. location entry dialog

v0.4 alpha
-----------
1. navigation drawer menu
2. select type of test dialog on start test
3. calibrate screen

v0.3 alpha
-----------
1. bacteria test
2. test history list
3. preferences/settings screen
4. check update option
5. about screen
6. result details screen
7. delete test result

v0.2 alpha
-----------
1. simple calibrate option

v0.1 alpha
-----------
1. simple colorimeter
2. result screen
3. swatches list
