LCIECG
=========

Low-Cost intelligent ECG.

This is the work created during the final project by student Alexander Mariel in the Facultad de Informática de San Sebastián.

From the abstract of the documentation:

Throughout this project a low-cost system has been developed for electrocardiogram reading and visualization in an Android app. An artificial intelligence module was also created to generate an automated and reasoned diagnostic on the received data. The project has been developed around open source technologies: Arduino as the base for the electronic system, Android to access the information on a portable device and CLIPS as the engine for the expert system which generates the diagnostic.

File structure and function
---------------------------
The `arduino` folder contains a single .ino file which makes the arduino take a measurement every 2 milliseconds and sends it through the serial port.

The rest of the folders compose an Android project. The structure is the usual one.
Here's a quick list of the contents of each package:
- `connection`: Contains the classes which establish and represent a Bluettoth connection
- `data`: Here lie some classes with different functions: data structures and AsyncTasks to get data from the BT connection
- `main`: The main Activity and the Fragments which are used in here are in this package. Diagnose is the class which represents the diganosing mechanism
- `parser`: The main class which parses the ECG is contained here, joined by the data structures which support it and some additional classes to implement the _Strategy pattern_
- `views`: Contains a custom view to plot the ECG
- `Constants.java`: A class devoid of methods, which holds some constant values for the other classes to access

License
-------
The files created in this project are subject to the GNU LGPL v3 license.

CLIPS code is in public domain.
Android's SDK code is subject to Google's TOS.
Arduino's API code is subject to the GNU GPL v2 license.
