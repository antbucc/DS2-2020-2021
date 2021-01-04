#!/usr/bin/env python3
import os

SUB_FOLDER = 'r'

# First let's parse the arguments from the command line
import argparse

parser = argparse.ArgumentParser(description='Graph the diffusion time')
parser.add_argument('input', metavar='INPUT', type=str, help='input folder')
parser.add_argument('--output', metavar='OUTPUT', type=str, help='output image filename', default=None)
parser.add_argument('--nodes', type=int, help='number of nodes', default=None)

args = parser.parse_args()

# Then create the plot
import pandas as pd
from matplotlib import pyplot as plt

data = pd.read_csv(os.path.join(args.input, SUB_FOLDER, 'diff_time.csv'))

fig, ax = plt.subplots()

ax.plot(data['timestamp'], data['c_mean'], color='tab:blue', label=f'Mean')
ax.fill_between(data['timestamp'], data['c_mean'] + data['c_std'], data['c_mean'] - data['c_std'], color='tab:blue', alpha=0.2, label=f'Standard deviation')

ax.set_xlabel('time from generation(s)')
ax.set_ylabel('Nodes')

ax.set_xlim([0, data['timestamp'].max()])

if args.nodes is None:
    with open(os.path.join(args.input, SUB_FOLDER, 'num_protocols.txt')) as w:
        args.nodes = int(w.readline())

ax.axhline(args.nodes, linestyle='--', c='k', linewidth=1)

ax.legend(loc="lower right")

if args.output is None:
    plt.show()
else:
    plt.savefig(args.output, dpi=300)
