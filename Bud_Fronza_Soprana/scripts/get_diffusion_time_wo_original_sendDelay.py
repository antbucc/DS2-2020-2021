#!/usr/bin/env python3

SUB_FOLDER='r'
SUB_SUB_FOLDER='application_received'

import pandas as pd
import numpy as np
import scipy.interpolate

import os
import sys

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
sendDelay = pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, 'original_sendDelay.csv'))
sendDelay = sendDelay.sort_values('id').set_index('id')

timestamps = []

separated_by_id = {}
for i in data['id'].unique():
    tmp = data[data['id'] == i].copy()

    tmp = tmp.sort_values('timestamp')

    tmp['timestamp'] -= sendDelay.loc[i]['t_max']

    tmp['timestamp'][tmp['timestamp'] < 0] = 0

    tmp['count'] = list(range(1, len(tmp)+1))

    timestamps.append(tmp['timestamp'])

    tmp = tmp[['timestamp', 'id', 'count']]

    tmp = tmp.append(pd.DataFrame([[float('inf'), i, tmp['count'].max()]], columns=['timestamp', 'id', 'count']))

    separated_by_id[i] = scipy.interpolate.interp1d(tmp['timestamp'], tmp['count'], kind='previous')

all_timestamps = pd.concat(timestamps).unique()

del data # No longer used, we can delete it

end_data = pd.concat([
    pd.DataFrame(data={
        'id': i,
        'timestamp': all_timestamps,
        'count': f(all_timestamps)
    })
    for i, f in separated_by_id.items()])

x = end_data.groupby('timestamp').agg({'count': ['mean', 'std']}).reset_index()
x.columns = ['timestamp', 'c_mean', 'c_std']

print(x.to_csv(sys.argv[2], index=False))
