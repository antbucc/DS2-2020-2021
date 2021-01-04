import argparse
import csv
import statistics
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import numpy as np



def main():
    args = parse_args()
    runs_results = load_runs_results(args.file)
    results_for_configuration = group_runs_for_configuration(runs_results)
    plot(results_for_configuration, args.type)


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, help='Path to the CSV file', required=True)
    parser.add_argument('--type', type=str, help='Type of plot', required=True)
    return parser.parse_args()


def load_runs_results(file_name):
    file = open(file_name)
    return list(csv.DictReader(file, delimiter=','))


def group_runs_for_configuration(runs_results):
    results_by_configuration = {}
    for run in runs_results:
        key = compute_configuration_key(run)
        if key not in results_by_configuration:
            results_by_configuration[key] = []
        results_by_configuration[key].append(run)
    return [group_runs_of_same_configuration(results) for (configuration, results) in results_by_configuration.items()]


def compute_configuration_key(run):
    keys = ["MeanPrefLANs", "StdPrefLANs", "MeanWait", "StdWait", "LANs", "Persons"]
    return '.'.join([run[key] for key in keys])


def group_runs_of_same_configuration(results):
    return {
        **results[0],
        'Latency': statistics.mean([float(result['Latency']) for result in results]),
        'MeanReachedPersons': statistics.mean([float(result['MeanReachedPersons']) for result in results])
    }


def plot(results_for_configuration, type):
    x_key, y_key = 'LANs', 'Persons'
    x_label, y_label = 'LANs', 'Persons'

    if type and type == 'MeanWait':
        x_key = 'MeanWait'
        x_label = r'$\mu_{wait}$'
    if type and type == 'MeanPrefLANs':
        x_key = 'MeanPrefLANs'
        x_label = r'$\mu_{PrefLANs}$'
    if type and type == 'LANs':
        x_key = 'LANs'
        x_label = r'LANs'
        
    x = [int(result[x_key]) for result in results_for_configuration]
    y = [int(result[y_key]) for result in results_for_configuration]
    z = [result['Latency'] for result in results_for_configuration]

    x_coord = sorted(list(set(x)))
    y_coord = sorted(list(set(y)))
    z_final = np.zeros((len(y_coord), len(x_coord)))
    
    for i, val in enumerate(z):
        x_idx = x_coord.index(x[i])
        y_idx = y_coord.index(y[i])
        z_final[y_idx, x_idx] = val
    
    x_coord, y_coord = np.meshgrid(x_coord, y_coord)
    
    fig = plt.figure()
    ax = fig.gca(projection='3d')
    ax.plot_surface(x_coord, y_coord, z_final, cmap='viridis', edgecolor='none')

    #fig.colorbar(surf, shrink=0.5, aspect=5)
    
    ax.set_xlabel(x_label)
    ax.set_ylabel(y_label)
    ax.set_zlabel('Latency')

    plt.show()


if __name__ == '__main__':
    main()
