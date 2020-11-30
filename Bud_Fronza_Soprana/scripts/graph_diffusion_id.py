#!/usr/bin/env python3

import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
import scipy.interpolate
import sys

NODE_NUMBERS = 25

QUANTILES=[(0.10, 0.90, 'tab:orange')]

data = pd.read_csv(sys.argv[1])

fig, ax = plt.subplots()

ax.plot(data['timestamp'], data['c_mean'], color='tab:blue', label=f'Mean')
ax.fill_between(data['timestamp'], data['c_mean'] + data['c_std'], data['c_mean'] - data['c_std'], color='tab:blue', alpha=0.2, label=f'Standard deviation')

ax.set_xlabel('time from generation(s)')
ax.set_ylabel('Nodes')

ax.set_xlim([0, data['timestamp'].max()])

ax.axhline(NODE_NUMBERS, linestyle='--', c='k')

ax.legend(loc="lower right")

plt.show()
