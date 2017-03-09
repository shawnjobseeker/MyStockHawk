# MyStockHawk
Based on the Udacity project specifications: https://classroom.udacity.com/courses/ud855/lessons/3993648704/concepts/42882736840923

My solution:
-Arabic language support added. Data from Yahoo Finance API not available in other languages
-Support for TalkBack screen reader added
-Home screen widget built 
-Added a dialog showing a stock's price over time - appearing when a stock in the list is clicked
-Fixed the bug regarding stock quotes that don't exist. Toast message appears in its place
-Added toast messages that inform user if internet connection is unavailable
-Replaced JobScheduler with GCMNetworkManager in order to backport app to earlier versions of Android (since API 11).
