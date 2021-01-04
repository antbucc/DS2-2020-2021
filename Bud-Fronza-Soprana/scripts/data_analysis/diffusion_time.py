#!/usr/bin/env python3

# First let's parse the arguments from the command line
import argparse
import sys

parser = argparse.ArgumentParser(description='Get the mean and std of the diffusion time given a time delay')
parser.add_argument('input', metavar='INPUT', type=str, help='input folder')
parser.add_argument('--output', metavar='OUTPUT', type=str, help='output csv filename', default=sys.stdout)
parser.add_argument('--nodes', type=int, help='number of nodes', default=None)
parser.add_argument('--bufferTime', type=int, help='Time at the end of the simulation in which new events will be ignored', default=150)

args = parser.parse_args()

# Process the data
SUB_FOLDER='r'
SUB_SUB_FOLDER='application_received'

import pandas as pd
import numpy as np
import scipy.interpolate

import os

nodes = [
    os.path.join(args.input, SUB_FOLDER, SUB_SUB_FOLDER, f)
    for f in os.listdir(os.path.join(args.input, SUB_FOLDER, SUB_SUB_FOLDER))
    if os.path.isfile(os.path.join(args.input, SUB_FOLDER, SUB_SUB_FOLDER, f)) and f.endswith('.csv')
]

tmp = pd.concat([pd.read_csv(node) for node in nodes])
data = tmp.astype({'timestamp': np.float32, 'address': 'category', 'port': 'category', 'id': np.int32})

del tmp

creation_time = data.groupby('id').agg({'timestamp': 'min'}).reset_index().rename(columns= {'timestamp': 'tmin'})

data = data.merge(creation_time, on='id')
data['timestamp'] = data['timestamp'] - data['tmin']

max_timestamp = data['timestamp'].max()

separated_by_id = {}
for i in data['id'].unique():
    tmp = data[data['id'] == i]
    tmp = tmp.sort_values('timestamp')
    if tmp['tmin'].min() < data['tmin'].max() - args.bufferTime:
        tmp['count'] = range(1, tmp['timestamp'].count()+1)

        separated_by_id[i] = scipy.interpolate.interp1d(
                [*tmp['timestamp'], max_timestamp],
                [*tmp['count'], tmp['count'].max()],
                kind='previous')

all_timestamps = np.arange(0, max_timestamp, step=0.01)

end_data = pd.concat([
    pd.DataFrame(data={
        'id': i,
        'timestamp': all_timestamps,
        'count': f(all_timestamps)
    })
    for i, f in separated_by_id.items()])

x = end_data.groupby('timestamp').agg({'count': ['mean', 'std']}).reset_index()
x.columns = ['timestamp', 'c_mean', 'c_std']

if args.nodes is None:
    with open(os.path.join(args.input, SUB_FOLDER, 'num_protocols.txt')) as w:
        args.nodes = int(w.readline())

x['p_mean'] = x['c_mean']/args.nodes
x['p_std'] = x['c_std']/args.nodes

del end_data

x.to_csv(args.output, index=False)
