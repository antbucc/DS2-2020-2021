import csv
import os
from os import path
import sys
import argparse
import matplotlib.pyplot as plt 
import statistics

'''
This script has two run modes:
(1)Plotting latencies - Input [Stations logs, relays logs]
   If the print_only_to param isn't provided, the script draw the graph of the latency
   of each perturbation of a single simulation
(2)Calculating latency - Input [Batch stations logs, batch relays logs, kind of sim, path to csv file with the latencies]
   The script will append to a file the mean latency of a run, in the format
   [RunType - latency], for future use with another script 
'''

def parse_arg(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--stations', type=str, help='Path to stations log file')
    parser.add_argument('--relays', type=str, help='Path to relays log file')
    parser.add_argument('--scenario', type=str, help="Kind of simulation run")
    parser.add_argument('--print_only_to', type=str, help='Path to print the latency result')
    return parser.parse_args(argv)

def read_stations(stations_path):
    file_station = open(str(stations_path))
    csv_station = csv.DictReader(file_station, delimiter=',')

    stations = {}

    #create a dict stations[station_id: [<tick, sent_ref, value>]]
    for row in csv_station:
        station_id = row['StationId']
        if station_id not in stations.keys():
            stations[station_id] = []
        if row['NewPerturbationValue'] != "":
            stations[station_id].append({
                'station': row['StationId'],
                'tick': float(row['tick']),
                'sent_ref': int(row['CurrentRef'])-1,
                'value': row['NewPerturbationValue']
            })

    return stations

def read_stations_batch(stations_path):
    file_station = open(str(stations_path))
    csv_station = csv.DictReader(file_station, delimiter=',')

    run_stations = {}

    #create a dict stations [run: [station_id: [<tick, sent_ref, value>]]]
    for row in csv_station:
        run_id = row['run']
        station_id = row['StationId']
        if run_id not in run_stations.keys():
            run_stations[run_id] = {}
        if station_id not in run_stations[run_id].keys():
            run_stations[run_id][station_id] = []
        if row['NewPerturbationValue'] != "":
            run_stations[run_id][station_id].append({
                'station': row['StationId'],
                'tick': float(row['tick']),
                'sent_ref': int(row['CurrentRef'])-1,
                'value': row['NewPerturbationValue']
            })
    return run_stations

def read_relays(relays_path):
    file_relay = open(str(relays_path))
    csv_relay = csv.DictReader(file_relay, delimiter=',')

    relays = {}

    #create a dict relays[relay_id: [<tick, station, ref>]]
    for row in csv_relay:
        relay_id = row['RelayId']
        if relay_id not in relays.keys():
            relays[relay_id] = []
        if row['ArrivedPerturbations'] != "":
            perturbations = row['ArrivedPerturbations'].split(',')
            for pert in perturbations:
                splitted = list(filter(None, pert.split(' ')))
                relays[relay_id].append({
                    'tick': float(row['tick']),
                    'station': splitted[0],
                    'ref': int(splitted[1])
                })

    return relays

def read_relays_batch(relays_path):
    file_relay = open(str(relays_path))
    csv_relay = csv.DictReader(file_relay, delimiter=',')

    run_relays = {}

    #create a dict relays [run: [relay_id: [<tick, station, ref>]]]
    for row in csv_relay:
        run_id = row['run']
        relay_id = row['RelayId']
        if run_id not in run_relays.keys():
            run_relays[run_id] = {}
        if relay_id not in run_relays[run_id].keys():
            run_relays[run_id][relay_id] = []
        if row['ArrivedPerturbations'] != "":
            perturbations = row['ArrivedPerturbations'].split(',')
            for pert in perturbations:
                splitted = list(filter(None, pert.split(' ')))
                run_relays[run_id][relay_id].append({
                    'tick': float(row['tick']),
                    'station': splitted[0],
                    'ref': int(splitted[1])
                })
    
    return run_relays

def draw_latency(x, y):
    # plotting the points  
    plt.plot(x, y) 
    
    # naming the x axis 
    plt.xlabel('Station - Ref') 
    # naming the y axis 
    plt.ylabel('Latency (in ticks)') 
    
    # giving a title to my graph 
    plt.title('Latency graph') 
    
    # function to show the plot 
    plt.show()

def main():
    args = parse_arg(sys.argv[1:])

    if(args.scenario == None and args.print_only_to == None):
        #usage mode (1), plot from single run
        perturbations = {}

        stations = read_stations(args.stations)
        relays = read_relays(args.relays)

        #create a dict station, sent_ref: tick, {}, value
        for station in stations:
            for pert in stations[station]:
                perturbations[pert["station"], pert["sent_ref"]]=[pert["tick"], {}, pert["value"]]

        print("Perturbations received by relays")
        #make a list of all sent perturbations
        for relay in relays:
            #arr_p means arrived_perturbation, it is not pirate language
            for arr_p in relays[relay]:
                old_v = None
                if relay in perturbations[arr_p["station"], arr_p["ref"]][1].keys():  # another value already inserted
                    old_v = perturbations[arr_p["station"], arr_p["ref"]][1][relay]
                    
                if old_v is None or old_v > arr_p["tick"]:
                    perturbations[arr_p["station"], arr_p["ref"]][1][relay] = arr_p["tick"]

        #compute the mean latency for each pert and draw graph
        x = [] #x axis
        y = [] #y axis

        #for each perturbation, calculate the mean latency
        for pert in perturbations:
            x.append(str(pert))
            count = 0
            latency = 0
            for arr_tick in perturbations[pert][1].values():
                count+=1
                latency+=arr_tick
            
            latency = latency / count
            perturbations[pert][1] = latency
            y.append(perturbations[pert][1] - perturbations[pert][0])

        draw_latency(x,y)
    elif(args.scenario != None and args.print_only_to != None):
        #usage mode (2), load multiple scenario from batch log
        stations = read_stations_batch(args.stations)
        relays = read_relays_batch(args.relays)

        #cumulative latency for all runs
        pert_latency = {}
        
        #now that we have all data, compute the median latency for all runs
        for run in stations:
            perturbations = {}

            #create a dict station, sent_ref: tick, {}, value
            for station in stations[run]:
                for pert in stations[run][station]:
                    perturbations[pert["station"], pert["sent_ref"]]=[pert["tick"], {}, pert["value"]]

            for relay in relays[run]:
                #arr_p means arrived_perturbation, it is not pirate language, yes this code was copy-pasted
                for arr_p in relays[run][relay]:
                    old_v = perturbations[arr_p["station"], arr_p["ref"]][1].get(relay)  # if another value is already inserted

                    if old_v is None or old_v > arr_p["tick"]:
                        perturbations[arr_p["station"], arr_p["ref"]][1][relay] = arr_p["tick"]

            for pert in perturbations:
                if len(perturbations[pert][1].values()) > 0:
                    mean_latency = statistics.mean(perturbations[pert][1].values())  - perturbations[pert][0]
                else:
                    mean_latency = 0
                
                # save the latency of the perturbation for this run
                if pert not in pert_latency.keys():
                    pert_latency[pert] = [[], [], []] # sending_tick, latency
                
                pert_latency[pert][0].append(perturbations[pert][0])
                pert_latency[pert][1].append(mean_latency)
                pert_latency[pert][2].append(len(perturbations[pert][1].values()))

        rows = []

        for p in pert_latency:
            rows.append({
                    'Scenario': str(args.scenario),
                    'Perturbation': str(p),
                    'Latency': str(statistics.mean(pert_latency[p][1])),
                    'Count': str(statistics.mean(pert_latency[p][2])),
                    'Tick': str(statistics.mean(pert_latency[p][0]))
                })

        file_exists = path.exists(args.print_only_to)
        with open(args.print_only_to, 'a') as fd:
            writer = csv.DictWriter(fd, fieldnames=rows[0].keys())

            if not file_exists:
                writer.writeheader()

            writer.writerows(rows)
    else:
        print("Missing arguments!")
        

if __name__ == "__main__":
    main()






