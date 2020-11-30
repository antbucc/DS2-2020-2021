#!/usr/bin/env python3

import pandas as pd
import sys

data = pd.read_csv(sys.argv[1])
data = data[data['originalSource'] == data['broadcaster']]

first_bIds = data.sort_values(['timestamp', 'id']).drop_duplicates(['id'])['bId'].unique()

data = data[data['bId'].isin(set(first_bIds))]

print(data.to_csv(index=False))
