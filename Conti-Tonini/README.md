# A Broadcast-Only Communication Model Based on Replicated Append-Only Logs

Implementation of the broadcast protocol described by [Christian F. Tschudin](https://bucchiarone.bitbucket.io/papers/acmdl19-295.pdf)

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
If you wish to run this model directly, download and run the [installation wizard](https://drive.google.com/file/d/1Yugv9PSOrUVFFHBw88ynIKwMavphy3-O/view?usp=sharing). The installation procedure is made of 9 steps
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
- `Communication type`: defines the type of communication of the simulation. Allowed values are *broadcast* and *p2p*.
- `Drop probability`: the chance that a payload gets discarded by an observer.
- `Grid size`: side of the simulation space; the area is evaluated as (grid size)x(grid size).
- `Number of observers`: the number of observers that populates the space.
- `Perturbation radius`: radius of the perturbation.
- `Perturbation speed`: speed of the perturbation.
- `Send probability`: the likelihood that an observer sends a payload when triggered.
- `Topology`: defines how to position each observer in the space. Allowed values are: *random*, *grid*, *circle* and *normal*

## Evaluate
The Repast GUI offers some graphs so to easily evaluate performance of the simulation. You can observe the total number of messages exchanged during the simulation. Moreover, two visual representations of the model let you visualize the dissemination of both a normal perturbation and and ARQ perturbation.

