#!/usr/bin/env python3
SUB_FOLDER='r'
SUB_SUB_FOLDER='collision_type'

import pandas as pd
import numpy as np
import scipy.interpolate

import os
import sys

from matplotlib import pyplot as plt

baseFolder=sys.argv[1]

nodes = [
    f
    for f in os.listdir(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER))
    if os.path.isfile(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER, f)) and f.endswith('.csv')
]

def read_node(node):
    data = pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER, node))
    data['source'] = int(node[1:-4])

    return data

GROUP_BY_SECONDS = 30

data = pd.concat([read_node(node) for node in nodes])

data['t_i'] = (data['timestamp']/GROUP_BY_SECONDS).astype(np.int)

coll = data.groupby(['t_i', 'collision_type']).agg({'timestamp': ['count']}).reset_index()
coll.columns = ['timestamp', 'collision_type', 'count']

coll['timestamp'] *= GROUP_BY_SECONDS
coll['timestamp'] += GROUP_BY_SECONDS/2

types = {
    'TR': 'Two received at the same time',
    'RS': 'Receiving while also sending',
    'N': 'Immediate transmission (completely free network)',
    'CT': 'Immediate transmission (currently free network)',
    'SW': 'Delayed transmissoin (due currently receiving)'
}

per_type = {
    t: coll[coll['collision_type'] == t]
    for t in types.keys()
}

timestamps = coll['timestamp'].unique()

for t in per_type.keys():
    per_type[t] = per_type[t].set_index('timestamp').reindex(timestamps).reset_index()
    per_type[t]['collision_type'] = t
    per_type[t] = per_type[t].fillna(0)

fig, ax = plt.subplots()
for t, d in per_type.items():
    print(t)

ax.stackplot(timestamps, *[v['count'] for v in per_type.values()], labels=types.values())

ax.legend(loc="lower center", bbox_to_anchor=(0.5, -0.65))
fig.subplots_adjust(bottom=0.35)

plt.show()

print(coll.to_csv())
