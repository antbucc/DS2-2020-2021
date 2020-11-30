import pandas as pd
import numpy as np
from matplotlib import pyplot as plt
import scipy.interpolate
import sys

data = pd.read_csv(sys.argv[1])
print(data)

fig, ax = plt.subplots()

ax.scatter(data['node'], data['avg_time'], color='tab:blue', marker='x', s=2, label='mean')

ax.set_xlabel('Node ID')
ax.set_ylabel('Time (s)')

ax.legend(loc="lower center", bbox_to_anchor=(0.5, -0.65))
fig.subplots_adjust(bottom=0.35)

fig_name = "new_nodes_opt"
plt.savefig(fig_name)
plt.show()
