import matplotlib.pyplot as plt


def compute_stores(file):
    data = open(file).readlines()[:-1]
    header = [f[1:-1] for f in data[0].strip().split(',')]
    data = [[f[1:-1] for f in d.strip().split(',')] for d in data[1:]]

    stores = {}

    for event in data:
        tick, recv, id, idx, _, _, _ = event
        tick = float(tick)
        recv = int(recv)
        id = int(id)
        idx = int(idx)

        if recv not in stores:
            stores[recv] = {}
        stores[recv][id] = idx

    return stores


### Plot data

boxplot = []
boxplot_labels = []
for label, file in [
        ('Open Gossip', 'datasets/og_30nodes.csv'),
        ('Transitive-Interest, 10 followers', 'datasets/tt_30nodes_10follow_1block.csv'),
        ('Transitive Interest, 5 followers', 'datasets/tt_30nodes_5follow_1block.csv'),
        ('Transitive-Interest, 1 follower', 'datasets/tt_30nodes_1follow_1block.csv')
    ]:

    stores = compute_stores(file)
    sizes = []
    for node, store in stores.items():
        sizes.append(sum(idx for idx in store.values()))

    boxplot.append(sizes)
    boxplot_labels.append(label)

plt.boxplot(boxplot, labels=boxplot_labels, autorange=True)
plt.title("Store size of nodes")
plt.ylabel("Store size (events)")
plt.show()
