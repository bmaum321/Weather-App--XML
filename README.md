# Weather Tracking App
Weather application in Kotlin for Android using Clean Architecture


## Features
- Search for locations to track weather, the API will display results in an autocomplete textview
- Select location to view daily and hourly forecasts
- Add locations to a watch list. List has touch helper for swipe to delete and drag to rearrange 
- Notifications for daily forecast based off device location
- Notifications for precipitation for tracked locations
- Settings menu to manipulate UI components and notification behavior

## Libraries
This application uses the following libraries
- Retrofit for web service calls
- Moshi for parsing JSON
- Room for database access
- Coil for image loading from API
- Preferences for settings menu
- Workmanager for scheduling background API calls and sending notifications


## Clean Architecture

The application is structured to follow the clean architecture design pattern. Inner layers such as
the domain and data/use cases are located in the core module.
The outer framework and presentation layers reside in the app module utilizing the MVVM pattern with flow

Some Highlights include: 

-Reactive UIs using Flow and coroutines for asynchronous operations.
-User Interface built with traditional XML
-A single-activity architecture, using Jetpack navigation.
-A presentation layer that contains a screen (View) and a ViewModel per screen (or feature).
-A data layer with a repository 
