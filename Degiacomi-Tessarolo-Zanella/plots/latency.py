import csv
import os
from os import path
import sys
import argparse
import matplotlib.pyplot as plt 
import statistics

'''
Script that plots the latency of the different events during the simulation.

If --target option is specified, instead of plotting the graph the script will append the results to a csv file and
is intended for use with a batch file. If target is NOT specified, the script will plot the graph of the latencies by
reading a non-batch log file.
'''

def parse_arg(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, help='Path to the log file', required=True)
    parser.add_argument('--lans', type=int, help='Number of LANs in the simulation', required=True)
    parser.add_argument('--persons', type=int, help='Number of persons in the simulation', required=True)
    parser.add_argument('--mu_lans', type=int, help='Mean of the Normal distribution of the preferred LANs', required=True)
    parser.add_argument('--std_lans', type=int, help='Std of the Normal distribution of the preferred LANs', required=True)
    parser.add_argument('--mu_wait', type=int, help='Mean of the Normal distribution of the waiting time', required=True)
    parser.add_argument('--std_wait', type=int, help='Std of the Normal distribution of the waiting time', required=True)
    parser.add_argument('--target', type=str, help='Csv to write to')
    return parser.parse_args(argv)

#read the file and create a dictionary 
def read_events_open(file):
    file_events = open(str(file))
    csv_events = list(csv.DictReader(file_events, delimiter=','))

    events = {}

    for row in csv_events:
        if row['createdEvents'] != "":
            created_events = row['createdEvents'].split(",")

            for event in created_events:
                events[event] = [float(row['tick']), []]

    for row in csv_events:
        if row['arrivedEvents'] != "":
            arrived_events = row['arrivedEvents'].split(",")

            for event in arrived_events:
                events[event][1].append(float(row['tick']))

    #return a dict events: <creation_tick, [arrival_tick1, arrival_tick2, ...]>
    return events

def read_batch_open(file):
    file_events = open(str(file))
    csv_events = list(csv.DictReader(file_events, delimiter=','))

    events = {}

    for row in csv_events:
        run = row['run']

        if(run not in events.keys()):
            events[run] = {}
            
        created_events = []
        if row['createdEvents'] != "":
            created_events = row['createdEvents'].split(",")

        for event in created_events:
            events[run][event] = [float(row['tick']), {}] 

    for row in csv_events:
        run = row['run']

        if row['arrivedEvents'] != "":
            arrived_events = row['arrivedEvents'].split(",")

            for event in arrived_events:
                if row['id'] not in events[run][event][1]:
                    events[run][event][1][row['id']] = float(row['tick'])

    for event in events:
        for e in events[event]:
            events[event][e][1] = list(events[event][e][1].values())

    #return a dict run: { event: <creation_tick, [arrival_tick1, arrival_tick2, ...]>}
    return events



#calculate the latency in case of an open model
def latency_open(events):
    for event in events:
        if len(events[event][1]) > 0:
            mean_arrived_tick = statistics.mean(events[event][1])
            events[event] = mean_arrived_tick - events[event][0]
        else:
            events[event] = -1

    #return a dict event: latency
    return events

def latency_batch_open(events, lans, persons, mean_LANs, std_LANs, mean_wait, std_wait):
    rows = []

    for run in events:
        run_latencies = []
        run_reached_pers = []

        for event in events[run]:
            if len(events[run][event][1]) > 0:
                mean_arrived_tick = statistics.mean(events[run][event][1])
                run_latencies.append(mean_arrived_tick - events[run][event][0])
            run_reached_pers.append(len(events[run][event][1]))
        
        rows.append({
            'MeanPrefLANs': mean_LANs,
            'StdPrefLANs': std_LANs,
            'MeanWait': mean_wait,
            'StdWait': std_wait,
            'LANs': str(lans),
            'Persons': str(persons),
            'Latency': str(statistics.mean(run_latencies)),
            'MeanReachedPersons': str(statistics.mean(run_reached_pers))
        })

    return rows

    
def plot_open(events):
    plt.plot(events.keys(), events.values())
    plt.xlabel('Events')
    plt.ylabel('Latency')
    plt.title('Events diffusion latency')
    plt.show()

def main():
    args = parse_arg(sys.argv[1:])

    if(args.target == None):
        #just plot the graph for a single run
        plot_open(latency_open(read_events_open(args.file)))
    else:
        #calculate mean of batch run and append to file
        rows = latency_batch_open(read_batch_open(args.file), args.lans, args.persons, args.mu_lans, args.std_lans, args.mu_wait, args.std_wait)
        #remove the print when editing this part
        file_exists = path.exists(args.target)
        with open(args.target, 'a') as fd:
            writer = csv.DictWriter(fd, fieldnames=rows[0].keys())

            if not file_exists:
                writer.writeheader()

            writer.writerows(rows)

if __name__ == "__main__":
    main()
