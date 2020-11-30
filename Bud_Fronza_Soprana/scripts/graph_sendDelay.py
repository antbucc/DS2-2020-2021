#!/usr/bin/env python3

import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
import scipy.interpolate
import sys

GROUP_BY_SECONDS=50
QUANTILES=[(0.10, 0.90, 'tab:orange')]

data = pd.read_csv(sys.argv[1])

data['t_min_i'] = (data['t_min']/GROUP_BY_SECONDS).astype(np.int)

sent_wait_delay = data.groupby('t_min_i').agg({'t_diff': ['mean', 'std'], 't_min': ['count']})

sent_wait_delay = sent_wait_delay.reset_index()
sent_wait_delay.columns = ['timestamp', 'delay_mean', 'delay_std', 'count']
sent_wait_delay['timestamp'] *= GROUP_BY_SECONDS
sent_wait_delay['timestamp'] += GROUP_BY_SECONDS/2

fig, ax = plt.subplots()
ax.scatter(data['t_min'], data['t_diff'], color='tab:blue', marker='x', s=5, label='raw data')
ax.plot(sent_wait_delay['timestamp'], sent_wait_delay['delay_mean'], color='tab:blue', label=f'Mean (grouping by {GROUP_BY_SECONDS} seconds)')
ax.fill_between(sent_wait_delay['timestamp'], sent_wait_delay['delay_mean'] + sent_wait_delay['delay_std'], sent_wait_delay['delay_mean'] - sent_wait_delay['delay_std'], color='tab:blue', alpha=0.2, label=f'Standard deviation (grouping by {GROUP_BY_SECONDS} seconds)')

for m, M, c in QUANTILES:
    sent_wait_delay[f'q{m}'] = data.groupby('t_min_i').quantile(m)['t_diff']
    sent_wait_delay[f'q{M}'] = data.groupby('t_min_i').quantile(M)['t_diff']

    ax.fill_between(sent_wait_delay['timestamp'], sent_wait_delay[f'q{m}'], sent_wait_delay[f'q{M}'], color=c, alpha=0.2, label=f'{m}-{M} quantile (grouping by {GROUP_BY_SECONDS} seconds)')

ax.set_xlabel('time(s)')
ax.set_ylabel('Delay due send * collissions (s)')

ax.set_xlim([0, data['t_max'].max()])
ax.set_ylim([-0.25, data['t_diff'].max()*1.05])

ax.legend(loc="lower center", bbox_to_anchor=(0.5, -0.65))
fig.subplots_adjust(bottom=0.35)

plt.show()

print(sent_wait_delay.to_csv())

