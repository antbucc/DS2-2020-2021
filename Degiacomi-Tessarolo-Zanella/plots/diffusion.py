import csv
import os
from os import path
import sys
import argparse
import matplotlib.pyplot as plt 
import statistics

'''
Script that plots the diffusion of the different events during the simulation, calculating the persons reached.
'''

def parse_arg(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, help='Path to the log file')
    return parser.parse_args(argv)

#read the file and create a dictionary 
def read_events(file):
    file_events = open(str(file))
    csv_events = list(csv.DictReader(file_events, delimiter=','))

    events = {}

    for row in csv_events:
        if row['createdEvents'] != "":
            created_events = row['createdEvents'].split(",")

            for event in created_events:
                events[event] = [float(row['tick']), {}]

    for row in csv_events:
        if row['arrivedEvents'] != "":
            arrived_events = row['arrivedEvents'].split(",")

            for event in arrived_events:
                if row['id'] not in events[event][1]:
                    events[event][1][row['id']]=float(row['tick'])

    #return a dict events: <creation_tick, [arrival_tick1, arrival_tick2, ...]>
    return events

def plot(events):
    for event in events:
        x = []
        x.append(events[event][0])
        x.extend(list(events[event][1].values()))
        y = [i for i in range(len(events[event][1])+1)]

        plt.plot(x, y, label = event)
    
    plt.xlabel('Tick')
    # Set the y axis label of the current axis.
    plt.ylabel('Diffusion of events')
    # Set a title of the current axes.
    plt.title('Events diffusion during simulation')
    # Display a figure.
    plt.show()

def main():
    args = parse_arg(sys.argv[1:])

    events = read_events(args.file)
    plot(events)

if __name__ == "__main__":
    main()