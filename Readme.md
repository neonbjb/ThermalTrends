# Thermal Trend Analyzer

This is an application which processes glider flights that are generated from the KML export feature of [OnlineContest](https://www.onlinecontest.org).

The application can scan a batch of flights and find parts of the flight which exhibit the traits of thermal climbing. These portions are then appended together into one massive output file. The idea is that pilots can use this output file during the flight planning phase to get an idea of what areas are frequently visited by the "locals" and thus have an idea of where lift might be found.

The output file can be organized in a variety of ways to aid analysis. These include, or will include:
* Month
* Time of day
* Climb Rate
* Wind direction
* Glider Class

The application also generates trend lines for the thermal down to the ground. This can aid in analyzing the ground source of thermals.

If you are interested in simply using this application, visit the Releases page. It is written in Java and is released as an executable JAR. If you have Java 1.8 installed on your computer, you should be able to launch the application by simply double clicking the ThermalTrends.jar file.
