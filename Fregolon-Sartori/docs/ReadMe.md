## Assignment 2
### Authors
Luca Fregolon (211511)

Alessandro Sartori (215062)
### How to use
#### 1. Import the source code

1. Clone this repository
2. Import it in Eclipse as Repast Symphony project
3. Build and run the simulator

#### 2. Run the installer

1. Download the jar file from [here](https://drive.google.com/file/d/1dS-HpKTOiRHGCAMK7Xex_tAS0PcvWCDO/view?usp=sharing)
2. Run the jar (e.g. `java -jar installer.jar`)
3. Follow the instructions of the installer
4. Run the program from the specified folder
### Parameters of the simulator
* Bandwith (kbps), defines the max bandwitdh to apply to each link
* Failure Probability, number from 0 to 1 which defines the probability of a node to fail
* Generation of Messages Interval, mean of the Poisson distribution which regulates the generation interval
* Insertion Probability, number from 0 to 1 which defines the probability of a new node to appear
* Interval of Insertion of new Nodes
* Interval of Node Failure
* Number of Nodes, number of __starting nodes__ in the simulation
* Number of nodes that each Node blocks
* Number of Nodes that each Node follows
* Probability to change a block, number from 0 to 1 which defines the probability to change someone a block
* Probability to change a follow, number from 0 to 1 which defines the probability to change a follow
* Simulation Type
  * Open Gossip
  * Transitive Interest
* Store sync interval, the pull interval to synchronize a store with another node
* Time interval between changes in block
* Time interval between changes in follow

### Reference
Kermarrec, A. M., Lavoie, E., & Tschudin, C. **Gossiping with Append-Only Logs in Secure-Scuttlebutt**.
