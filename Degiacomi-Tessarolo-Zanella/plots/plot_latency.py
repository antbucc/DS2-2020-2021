import csv
import os
from os import path
import sys
import argparse
import matplotlib.pyplot as plt 
from mpl_toolkits.mplot3d import Axes3D
import statistics

'''
Script that plots the 3d graph of LAN/Persons/Latency.
'''

def parse_arg(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, help='Path to the log file', required=True)
    parser.add_argument('--type', type=str, help='Type of plot in [Basic, MeanPrefLANs, MeanWait]', required=True)
    return parser.parse_args(argv)


def get_plot_key_and_subkey(row, plot_type):
    if plot_type == 'Basic':
        key = (row['MeanPrefLANs'], row['StdPrefLANs'], row['MeanWait'], row['StdWait'])
        subkey = (row['LANs'], row['Persons'])
    elif plot_type == 'MeanPrefLANs':
        key = (row['LANs'], row['Persons'], row['StdPrefLANs'], row['MeanWait'], row['StdWait'])
        subkey = (row['MeanPrefLANs'],)
    elif plot_type == 'MeanWait':
        key = (row['LANs'], row['Persons'], row['MeanPrefLANs'], row['StdPrefLANs'], row['StdWait'])
        subkey = (row['MeanWait'],)
    
    return key, subkey


def get_plot_series_label(key, plot_type):
    if plot_type == 'Basic':
        return r'$\mathcal{{N}}_{{prefLANs}}({}, {}) - \mathcal{{N}}_{{wait}}({}, {})$'.format(*key)
    elif plot_type == 'MeanPrefLANs':
        return ''
    elif plot_type == 'MeanWait':
        return ''


def get_plot_xy_label(plot_type):
    if plot_type == 'Basic':
        return 'LANs number', 'Mean latency'
    elif plot_type == 'MeanPrefLANs':
        return r'$\mu_{prefLANs}$', 'Mean latency'
    elif plot_type == 'MeanWait':
        return r'$\mu_{wait}$', 'Mean latency'


def read_latencies(file, plot_type):
    file_latencies = open(str(file))
    csv_latencies = list(csv.DictReader(file_latencies, delimiter=','))

    latencies = {}
    reached_pers = {}

    for row in csv_latencies:
        key, subkey = get_plot_key_and_subkey(row, plot_type)

        if key not in latencies:
            latencies[key] = {}
            reached_pers[key] = {}
        if subkey not in latencies[key]:
            latencies[key][subkey] = []
            reached_pers[key][subkey] = []

        latencies[key][subkey].append(float(row['Latency']))
        reached_pers[key][subkey].append(float(row['MeanReachedPersons']))

    for label in latencies:
        for key in latencies[label]:
            latencies[label][key] = (statistics.mean(latencies[label][key]), statistics.stdev(latencies[label][key]))
            reached_pers[label][key] = (statistics.mean(reached_pers[label][key]), statistics.stdev(reached_pers[label][key]))

    return latencies, reached_pers


def plot(latencies, reached_pers, plot_type):
    fig = plt.figure()
    ax = fig.gca()
    if plot_type != "Basic":
        ax2 = ax.twinx()
        ax2_color = 'tab:orange'

    for label in latencies:
        xs, ys, zs = [], [], []
        rp = []
        err_lat, err_reach = [], []
        for key, value in latencies[label].items():
            xs.append(int(key[0]))
            ys.append(value[0])
            err_lat.append(value[1])
            rp.append(reached_pers[label][key][0])
            err_reach.append(reached_pers[label][key][1])
            if len(key) > 1:
                zs.append(int(key[1]))
            else:
                zs.append(1)

        label_txt = get_plot_series_label(label, plot_type)
        ax.plot(xs, ys, label=label_txt, alpha=0.4)
        ax.scatter(xs, ys, s=zs, marker='o')
        if plot_type != "Basic":
            ax2.plot(xs, rp, color=ax2_color, alpha=0.8)

    if plot_type != "Basic":
        ax2.set_ylabel('Number of reached people', color=ax2_color)
        ax2.tick_params(axis='y', labelcolor=ax2_color)

    x_lbl, y_lbl = get_plot_xy_label(plot_type)
    ax.set_xlabel(x_lbl)
    ax.set_ylabel(y_lbl)
    
    plt.title('Latency plot')
    plt.legend()
    plt.show()
    

def main():
    args = parse_arg(sys.argv[1:])

    latencies, reached_pers = read_latencies(args.file, args.type)
    plot(latencies, reached_pers, args.type)


if __name__ == "__main__":
    main()
