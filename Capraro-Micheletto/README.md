# DS2-SecondAssignment

This repository contains an implementation of the Secure Scuttlebutt protocol as presented in **Gossiping with Append-Only Logs inSecure-Scuttlebutt** [1].

## The team

- Riccardo Capraro - 203796
- Riccardo Micheletto - 215033

## The protocol

The Secure Scuttlebutt (SSB) was proposed as a secure and reliable replication mechanism for social applications based on the use of append-only logs.

The protocol allows for two replication strategies:

- OpenGossip for total replication
- TransitiveInterest for interest-based logs replication

For a thorough description please refer to the PDF document contained in this folder.

For the network layer underlying the SSB implementation we used our implementation of the protocol the Wavecast protocol that can be found [here](https://github.com/antbucc/DS2-2020-2021/blob/FirstAssignment/Capraro-Micheletto).

## How to run

To run the code you can either import the project in eclipse or run _java -jar installer.jar_, where the installer.jar can be downloaded from [here](https://drive.google.com/file/d/1C4QKHOmLCdd8p6zBUNOJzhC9yVjv8Akk/view?usp=sharing). Since the project was compiled with Java11, be sure to use a compatible version. When you run the command above, an installer will allow you to install the program on your system: choose a folder and there you will find the program **start_model** that you can use to start the model.

The list of parameters that can be modified to change the simulation specifications can be found in the PDF report contained in this repository.

## References

[1] C. T. Anne-Marie Kermarrec, Erick Lavoie, Gossiping with append-only logs in secure-scuttlebutt,International Workshop on Distributed In-frastructure for CommonGood (DICG'20)(2020).<https://doi.org/10.1145/3428662.3428794>.
