# Gossiping with Append-Only Logs in Secure-Scuttlebutt
This project implements the gossiping with append-only logs in secure-scuttlebutt, as specified in the [assigned paper](https://bucchiarone.bitbucket.io/papers/dicg2020.pdf)

## Requirements
* Java 11
* Eclipse IDE 2020-06 (4.16.0)
* Repast Symphony 2.8.0

Optionally, for running the plotting scripts:
* Python 3

## Installation
You have two ways to do it; first one:

1. Download the `installer.jar` file from [this link](https://drive.google.com/file/d/16aHYfVXW9Npf1NKRYq2iniWpAWUxKI0d/view?usp=sharing)
2. Execute it (`java -jar installer.jar`) and follow the installation wizard
3. Run the `start_model.bat` or `start_model.command` file

Second one:
1. Clone the repository
2. Import the project as Repast Project into Eclipse
3. Run the simulator

## Simulator parameters
* Num LANs: the number of LANs that will be present on the simulation. This is a fixed parameter that cannot vary during the run of the simulation. 
* Num People: the number of Person actors in the simulation. Similarly to the previous parameter, this number cannot change during the simulation run.
* Motion Strategy: the different motion strategies are described in paragraph \ref{subsectionmotion} and define the way the Person actors move in the simulation's grid.
* Synchronization Protocol: the synchronization protocol defines how events are exchange between persons. The two different ways of synchronizing event are explain in detail in paragraph \ref{subsectionsync}.
* Gaussian mean preferred LANs: used in the Habit Motion strategy, indicates the mean number of favourite LANs that a persons has.
* Gaussian std preferred LANs: used in the Habit Motion strategy, indicates the standard deviation number of favourite LANs that a persons has.
* Gaussian mean ticks waiting: used in the Habit Motion strategy, indicates the mean number of ticks a person will stay in one of his favourite LANs (and at home).
* Gaussian std ticks waiting: used in the Habit Motion strategy, indicates the standard deviation number of ticks a person will stay in one of his favourite LANs (and at home).

## Plotting scripts parameters
###### countEvent.py
Simple script that plots the number of events created during the simulation.
* --file: input log file
* --group: rate at which ticks are grouped (useful for having a cleaner graph with longer simulations)

###### countEventDelivery.py
Simple script that plots the number of events delivered during the simulation.
* --file: input log file
* --group: rate at which ticks are grouped (useful for having a cleaner graph with longer simulations)

###### diffusion.py
Script that plots the number of time each event has been delivered during the simulation.
* --file: input log file

###### latency.py
Script that plots the latency of the different events during the simulation.

* --file: input log file
* --lans: number of LANs in the simulation
* --persons: number of persons in the simulation
* --mu_lans: mean of the normal distribution of the preferred LANs
* --std_lans: std of the normal distribution of the preferred LANs
* --mu_wait: mean of the normal distribution of the waiting time
* --std_wait: std of the normal distribution of the waiting time
* --target: csv to append result to. If this parameter is specified then the graph won't be plotted.

###### plot_latency.py
Script that plots the 3d graph of LAN/Persons/Latency.

* --file: input log file
* --type: type of plot in [Basic, MeanPrefLANs, MeanWait]

## Authors
* **Simone Degiacomi** 211458
* **Davide Tessarolo** 211457
* **Davide Zanella** 211463

## Reference
Anne-Marie Kermarrec, Erick Lavoie, and Christian Tschudin. 2020.Gossiping with Append-Only Logs in Secure-Scuttlebutt. In1stInternational Workshop on Distributed Infrastructure for CommonGood (DICG’20), December 7–11, 2020, Delft, Netherlands.ACM, NewYork, NY, USA, 6 pages.
