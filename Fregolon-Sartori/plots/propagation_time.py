import matplotlib.pyplot as plt


def get_prop_times(file):
    data = open(file).readlines()[:-1]
    header = [f[1:-1] for f in data[0].strip().split(',')]
    data = [[f[1:-1] for f in d.strip().split(',')] for d in data[1:]]

    stores = {}
    followers_of = {}
    blockers_of = {}
    propagation_times = {}

    for event in data:
        tick, recv, id, idx, _, _, _, _, e_data = event
        tick = float(tick)
        recv = int(recv)
        id = int(id)
        idx = int(idx)

        if id not in followers_of:
            followers_of[id] = []
        if id not in blockers_of:
            blockers_of[id] = []

        if e_data == '-':
            if recv not in stores:
                stores[recv] = {}
            stores[recv][id] = idx

            if id not in propagation_times:
                propagation_times[id] = {}
            elif idx not in propagation_times[id]:
                propagation_times[id][idx] = [tick]
            else:
                propagation_complete = True
                for node in followers_of[id]:
                    relevant = (node not in blockers_of[id])
                    if relevant and (node not in stores or id not in stores[node] or stores[node][id] < idx):
                        propagation_complete = False
                        break
                if propagation_complete:
                    propagation_times[id][idx].append(tick)
        else:
            action, target = e_data.split(' ')
            target = int(target)

            if target not in followers_of:
                followers_of[target] = []
            if target not in blockers_of:
                blockers_of[target] = []

            if action == 'FOLLOW':
                followers_of[target].append(id)
            elif action == 'UNFOLLOW':
                followers_of[target].remove(id)
            elif action == 'BLOCK':
                blockers_of[target].append(id)
            elif action == 'UNBLOCK':
                blockers_of[target].remove(id)
    return propagation_times


### Plot data

boxplot_data, boxplot_labels = [], []

for label, mark, file in [
        ('1 follower each', 'o', 'datasets/tt_30nodes_1follow_1block.csv'),
        ('5 followers each', '^', 'datasets/tt_30nodes_5follow_1block.csv'),
        ('10 followers each', '^', 'datasets/tt_30nodes_10follow_1block.csv')
    ]:
    x_vals = []
    y_vals = []
    propagation_times = get_prop_times(file)

    for node in propagation_times.keys():
        for times in propagation_times[node].values():
            if len(times) == 1:
                continue
            x_vals.append(times[1])
            y_vals.append(times[1] - times[0])

    boxplot_data.append(y_vals)
    boxplot_labels.append(label)
    plt.scatter(x_vals, y_vals, label=label, marker=mark, alpha=0.8)

plt.title("Propagation time of each event")
plt.xlabel("Simulation time (ms)")
plt.ylabel("Propagation time (ms)")
# plt.ylim([0, 2000])
plt.legend()
plt.figure()

plt.title("Propagation times of events")
plt.ylabel("Propagation time (ms)")
plt.boxplot(boxplot_data, labels=boxplot_labels)
plt.show()
