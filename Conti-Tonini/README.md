# Gossiping with Append-Only Logs in Secure-Scuttlebutt

Implementation of the broadcast protocol described by [Anne-Marie Kermarrec, Erick Lavoie,
 and Christian F. Tschudin](https://bucchiarone.bitbucket.io/papers/dicg2020.pdf).

For the full report of this project please refer to `report.pdf`.

## Authors
- Conti Alessandro - 215034
- Tonini Francesco - 211961

## Build
To build this model you will need:
- Repast Simphony 2.7
- Eclipse 4.12
- Java JDK 11

Once everything has been set up, import `src` into Eclipse and run the *ssb-broadcast Model* configuration.

## Install
If you wish to run this model directly, download and run the [installation wizard](https://drive.google.com/file/d/10TQRvdJhsMvt19iLADNZ2lntxjLu4gV8/view?usp=sharing). The installation procedure is made of 9 steps
1. Select language.
2. A message welcomes the user to the setup of the Model. Press *Next* to continue.
3. On the project description page, press *Next*.
4. Accept the license of the project, then press *Next*.
5. Select an installation path that the running user has access to. Then, press *Next*
6. By default the setup imports executable, documentation and source of the model. You may want to uncheck documentation and source if you like to run the model only. Then, press *Next*.
7. The setup will begin copying the required file on the system. Once finished, press *Next*.
8. By default the setup creates a shortcut on the Start-Menu. You may opt-out of this by un-checking the checkbox on the top left corner of the setup window. Then, press *Next*.
9. Setup is now completed. You may now close the installation wizard by pressing *Close*.

You can now run the model through the shortcut created, or by running `start_model` available on the installation directory.

## Run
Once the Repast GUI is loaded, you can run the model immediately by pressing the blue *Play* button on the top left corner of the window. If you like to change some parameters, select the *Parameters* panel so to change values of the model such as:
- `Clique`: number of observers that are known from the start of the simulation. This option is valid for transitive-interest gossip only.
- `Create event probability`: the likelihood to create a local event.
- `Follow/Block probability`: the likelihood to follow or block an observer. This option is valid for transitive-interest gossip only.
- `Grid size`: side of the simulation space; the area is evaluated as (grid size)x(grid size).
- `Number of observers`: the number of observers that populates the space.
- `Kill observer probability`: the likelihood that an observer crashes.
- `New observer probability`: the likelihood that a new observer joins the simulation.
- `Perturbation radius`: radius of the perturbation.
- `Perturbation speed`: speed of the perturbation.
- `Topology`: defines how to position each observer in the space. Allowed values are: *random* and *normal*.

## Evaluate
The Repast GUI offers some graphs so to easily evaluate performance of the simulation. You can observe the total number of payloads sent and the number of active, new or dead observers. You can also observe the average latency and number of updates stored. Moreover, two visual representations of the model let you visualize the evolution of the network w.r.t gossip exchange and followed/blocked sets of each participant (valid for transitive-interest gossip only).
