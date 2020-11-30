# Broadcast-Only Communication Model Based on Replicated Append-Only Logs
This project implements the broadcast-only communication model based on replicated append-only logs, as specified in the [assigned paper](https://bucchiarone.bitbucket.io/papers/acmdl19-295.pdf)

## Requirements
* Java 11
* Eclipse IDE 2020-06 (4.16.0)
* Repast Symphony 2.8.0

Optionally, for running the plotting scripts:
* Python 3

## Installation
You have two ways to do it; first one:

1. Download the `installer.jar` file from [this link](https://drive.google.com/file/d/15TYa1B3HanApnJTkXwoHeKTWVrQ3GNu1/view?usp=sharing)
2. Execute it (`java -jar installer.jar`) and follow the installation wizard
3. Run the `start_model.bat` or `start_model.command` file

Second one:
1. Clone the repository
2. Import the project as Repast Project into Eclipse
3. Run the simulator

## Simulator parameters
* Maximum number of relays: the maximum number of relays that can be present on a given run
* Minimum number of relays: the minimum number of relays that can be present on a given run
* New relay probability: the probability that a new relay will be spawned in a period of 100 ticks
* Relay crash probability: the probability that a new relay will be deleted in a period of 100 ticks
* Number of relays: initial number of relays
* Number of stations: initial number of stations
* Packet size: size of packets sent via perturbations
* Transmission speed: available bandwidth per tick
* Perturbation drop probability: probability that a perturbation is dropped
* Protocol version: defines which protocol version (from those presented in the paper) the simulator will run
* Stop at: the simulation will stop once it reaches the indicated amount of ticks

## Plotting script parameters
###### create_plots.py
* --stations: path to stations log file (can be batch file)
* --relays: path to relays log file (can be batch file)
* --scenario: if using batch log files, define the protocol used in the simulation
* --print_only_to: doesn't draw graph, but appends latency results to csv file

###### plot_multiple_runs.py
* --input_csv: path to csv file generated via create_plots.py

Alternatively, if you don't generate the csv file via create_plots.py, you can create a two-columns csv, where the columns follow the pattern "Scenario","Latency"

## Authors
* **Simone Degiacomi** 211458
* **Davide Tessarolo** 211457
* **Davide Zanella** 211463

## Reference
Christian F. Tschudin: Broadcast-Only Communication Model Based on Replicated Append-Only Logs, ACM SIGCOMM Computer Communication Review, Volume 49 Issue 2, April 2019