
# Source Codes
All source codes contains in the location : InteractiveNewsReader/app/src/main

## java source
All java code contains in the location: InteractiveNewsReader/app/src/main/java/com/x54/interactivenewsreader/

java source codes are 

 1. MainActivity.java
 2. NewsActivity.java
 3. DBOpenHelper.java
 4. NewsSiteDetails.java
 5. NewsDetails.java
 

    ### MainActivity.java
    This code do background task for the first activity of the application. This code contains following functions:

 - onCreate() : This function when this activity launch. In this function contains code for  startButton and here we retrive all newspaper information from database. Here we initialize tts engine by calling initializeTTS() function.
 
 - initializeTTS() : This function intialize the tts engine by creating an instance of TextToSpeech class of android.speech.tts  package.
 
 - speechInputScreen() : This function show prompt to user using RecognizerIntent class from package  android.speech
 
 - selectNews() : This function check newspaper selected by user to retrieved data from database. siteArrayList is a arraylist which contains all newspaper information. 

- onActivityResult() : Get the speech recognition result . resultString is a string which contains the result. In this function, start NewsActivity when newspaper is selected.

### NewsActivity.java
This class contains following functions:
- loadDataToWebView(): This function load website to the webview.
- promptSpeechInput(): This function shows prompt to user.
- onActivityResult(): This function retrieve speech recognition result.
- selectNews(): This function read news headlines.
- selectCategory(): This function select news category
- initializeTextToSpeech(): This function initialize tts engine.
- startTts(): This function extract news content from downloaded html using boilerpiper and jsoup library.
- DownloadData: This  class download html of news using jsoup library.

 ### DBOpenHelper.java
 This class helps to access and query on the embedded SQLite database. This class contains the following functions:
 - getAllNewsSite() :This function retrieve all newspaper information.
 - getNewsSite(): This function retrieve news category information from selected newspaper.
 ### NewsSiteDetails.java
	 This class helps to set news site details to arraylist.
 ### NewsDetails.java
	 This class helps to set news details to arraylist.


## Resource files

All the resource files contains in the location: InteractiveNewsReader/app/src/main/res

The resources are :
1. layout : Layouts are user interface design of application.
		Location: InteractiveNewsReader/app/src/main/res/layout
2. values : Values contains strings,dimens,colors which used in the application. Location: InteractiveNewsReader/app/src/main/res/values

## assets

Assets contains the database of the application. 
Location: InteractiveNewsReader/app/src/main/assets

## libs 
libs contains library used in the application.
	1. boilerpipe-1.2.2
		dependencies: nekohtml, xercesImpl
	2. Jsoup

# Configuration files
1. build.gradle : which contains version of gradle . Version gradle on my android studio is 3.2.1
	
	Location: InteractiveNewsReader/build.gradle
 2. build.gradle : Which contains configuration for compiling the application. 
	 Location: InteractiveNewsReader/app/build.gradle
	 
	 compileSdkVersion 27
        minSdkVersion 16
        targetSdkVersion 27
		 
 

		

