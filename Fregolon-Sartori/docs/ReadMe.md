## DS2 First Project
### Authors
Luca Fregolon (211511)

Alessandro Sartori (215062)

### How to use

There are two ways to run the simulation

#### 1. Import the source code

1. Clone this repository
2. Import it in Eclipse as Repast Symphony project
3. Build and run the simulator

#### 2. Run the installer

1. Download the jar file from [here](https://drive.google.com/file/d/1sYpyd3P_m6Ilup10-JXhAM-1pcJs9Iv_/view?usp=sharing)
2. Run the jar (e.g. `java -jar installer.jar`)
3. Follow the instructions of the installer
4. Run the program from the specified folder

### Parameters Guide
* sym_type is an integer and sets the ”increment” of the project to use:
  1. Nodes implement what is referred to as ”Relay1” in the paper, i.e. a static network communicating through ”frontiers”
  2. Nodes instantiated as ”Relay 2” make use of a bag variable in order to handle out-of order perturbations and correctly reconstructing the append-only log. This implementation solves the problem of dynamic network, that is nodes entering and leaving at any time
  3. In simulation type 3, a retransmission mechanism is introduced and it allows to recover missing data (both because of failing or because of late entering)
  4. Type 4 is a super-set of Type 3 where nodes communicate through unicast transmissions instead of broadcast ones.
  5. Again a super-set of Type 4 but with asymmetrically encrypted point-to-point links.
  6. A super-set of Type 3 with Multicast, topic-based, message exchanges
* nNodes, indicates the (starting) number of nodes to generate upon initialization of the space
* BroadcastDistance, limits the maximum distance at which a link can be established between two nodes
* propTime, controls the delay to be multiplied with the distance to create a concept of ”propagation delay”
* μ Sending time interval, which corresponds to the average interval that a node waits before generating another perturbation, regulated by a Gaussian distribution
* σ Sending time interval, which corresponds to the variance of the Gaussian distribution aforementioned

For sim_type >= 2
* insertIntervals, the interval at which a new node insertion event is generated
* insertProb, controls the probability with which,at the aforementioned events, the node is actually created and added to the environment
* failInterval works similarly as insertInterval but to schedule node failures
* failProb, as insertProb, controls the probability of node failure upon a failure event
### Reference
Christian F. Tschudin **A Broadcast-Only Communication ModelBased on Replicated Append-Only Logs.** ACM SIGCOMM Com-puter Communication Review
