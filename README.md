Hello ! This java application is still under dev.
It works for oflyers.isae.fr only.
Once all steps below are done, launch the program through the main in src/FlightCalendar.java

Before, complete the following files with your information :

Site.java Ligne 55

// Enter logins in the form
            login.sendKeys(""); //enter login   ex: "gmordelet"
            pass.sendKeys(""); //enter password ex: "password"
	
You need first to create a new calendar for the flights to be stored in, via Google Calendar.
Then you need to obtain the CALENDAR_ID : https://yabdab.zendesk.com/hc/en-us/articles/205945926-Find-Google-Calendar-ID
Put the ID between the "" of the file explained below

FlightCalendar.java Ligne 37
/**
     * This ID is the one of the calendar of
     * the script author (to REPLACE with yours cf. Google API doc to list calendars by ID)
     */
private static final String CALENDAR_ID = "";

You also need to allow your application to access this api. For that you need an authentication with OAuth 2.0

In order to obtain the file credentials.json to place in src/ visit : https://cloud.google.com/docs/authentication/getting-started?hl=fr
It explains the procedure : you have to create an account for Google Developper, a project and authorize it
Place the JSON file in src/

It should be good now !

For any issues contact me through gitHub (or Facebook)