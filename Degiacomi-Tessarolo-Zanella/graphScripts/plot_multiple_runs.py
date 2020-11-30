import csv
import os
import sys
import argparse
import matplotlib.pyplot as plt 
import statistics
from matplotlib.pyplot import figure

'''
    Script used to plot the data in the csv file created via create_plots.py (run in mode (2))
    This script will take the data in the csv file and draw a plot showing the different median latencies
    for the different batch runs on each protocol
'''

def parse_arg(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--input_csv', type=str, help='Path to csv file generated via create_plots.py')
    parser.add_argument('--max_tick', type=int, help='Maximum tick to plot')
    return parser.parse_args(argv)


def plot_latency(latencies, x, x_labels, output_basename):
    for scenario in sorted(latencies):
        # plotting the points  
        plt.plot(x, latencies[scenario][:len(x)], label=scenario, alpha=0.7)
    
    # naming the x axis 
    plt.xlabel('Perturbations')
    plt.xticks(range(len(x_labels)), x_labels)
    
    # naming the y axis 
    plt.ylabel('Latency (in ticks)') 
    
    # show the legend
    plt.legend()
    
    # set y min value to 0
    axes = plt.gca()
    axes.set_ylim([0, None])
    
    # giving a title to my graph 
    plt.title('Latency graph') 
    
    # set layout config (to avoid overlapping of labels)
    plt.tight_layout()
    
    # function to show the plot 
    #plt.show()
    
    # save the plot
    plt.savefig(output_basename + '_latency.jpg')
    
    # clear the current figure
    plt.clf()


def plot_relays_reached(relays_reached, x, x_labels, output_basename):
    for scenario in sorted(relays_reached):
        # plotting the points  
        plt.plot(x, relays_reached[scenario][:len(x)], label=scenario, alpha=0.7)
    
    # naming the x axis 
    plt.xlabel('Perturbations')
    plt.xticks(range(len(x_labels)), x_labels)
    
    # naming the y axis 
    plt.ylabel('Reached relays')
    
    # show the legend
    plt.legend()
    
    # set y min value to 0
    axes = plt.gca()
    axes.set_ylim([0, None])
    
    # giving a title to my graph 
    plt.title('Reached relays graph') 
    
    # set layout config (to avoid overlapping of labels)
    plt.tight_layout()
    
    # function to show the plot 
    #plt.show()
    
    # save the plot
    plt.savefig(output_basename + '_relays.jpg')
    
    # clear the current figure
    plt.clf()


def main():
    args = parse_arg(sys.argv[1:])
    
    max_tick = int(args.max_tick)
    
    output_basename = str(args.input_csv)[:-4]
    
    file_runs = open(str(args.input_csv))
    csv_runs = csv.DictReader(file_runs, delimiter=',')

    latencies = {}
    relays_reached = {}

    # sort the values
    csv_runs = list(sorted(csv_runs, key=lambda x: (float(x['Tick']), x['Perturbation'])))
    # remove the unwanted ticks
    csv_runs = list(filter(lambda x: float(x['Tick']) <= max_tick, csv_runs))

    x_labels = {}
    x = []

    for row in csv_runs:
        scenario = row['Scenario']
        # get a list ['ref', 'Station0']
        pert = row['Perturbation'][1:-1].split(', ')
            
        station_num = pert[0][8:-1]
        ref = int(pert[1])
        
        if scenario not in latencies.keys():
            latencies[scenario] = {}
            relays_reached[scenario] = {}
        if ref not in latencies[scenario]:
            latencies[scenario][ref] = []
            relays_reached[scenario][ref] = []

        latencies[scenario][ref].append(float(row['Latency']))
        relays_reached[scenario][ref].append(float(row['Count']))
        
        if ref not in x:
            x.append(ref)
            x_labels[ref] = set()
        x_labels[ref].add(station_num)
       
    for scen in latencies.keys():
        latencies[scen] = [statistics.mean(latencies[scen][ref]) for ref in latencies[scen].keys()]
    for scen in relays_reached.keys():
        relays_reached[scen] = [statistics.mean(relays_reached[scen][ref]) for ref in relays_reached[scen].keys()]
    
    def make_label(ref, stations):
        if len(stations) > 1:
            return '{}\n[s{}, ..., s{}]'.format(ref, min(x_labels[ref]), max(x_labels[ref]))
        else:
            return '{}\n[s{}]'.format(ref, min(x_labels[ref]))            
    x_labels = [make_label(ref, x_labels[ref]) for ref in x_labels.keys()]
       
       
    # set matplotlib figure sizes
    figure(num=None, figsize=(10, 6), dpi=300, facecolor='w', edgecolor='k')
    
    plot_latency(latencies, x, x_labels, output_basename)
    plot_relays_reached(relays_reached, x, x_labels, output_basename)


if __name__ == "__main__":
    main()
