#!/usr/bin/env python3
import pandas as pd
import sys

data = pd.read_csv(sys.argv[1])
data = data.groupby(['broadcaster', 'originalSource', 'bId','id']).agg({'timestamp': ['min', 'max']}).reset_index()

data.columns = ['broadcaster', 'originalSource', 'bId', 'id', 't_min', 't_max']
data['t_diff'] = data['t_max'] - data['t_min']

print(data.to_csv(index=False))
