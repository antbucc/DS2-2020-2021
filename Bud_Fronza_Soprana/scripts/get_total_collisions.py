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

data = pd.concat([read_node(node) for node in nodes])

coll = data.groupby('collision_type').agg({'timestamp': 'count'}).reset_index()
coll.columns = ['collision_type', 'count']

types = {
    'TR': 'Two received at the same time',
    'RS': 'Receiving while also sending',
    'N': 'Immediate transmission (completely free network)',
    'CT': 'Immediate transmission (currently free network)',
    'SW': 'Delayed transmissoin (due currently receiving)'
}

coll = coll.set_index('collision_type').reindex(types.keys()).reset_index().fillna(0)

print(coll.to_csv(index=False))
