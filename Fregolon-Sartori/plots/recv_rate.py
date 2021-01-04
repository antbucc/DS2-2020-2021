import matplotlib.pyplot as plt


data = open('../output/recEvents.csv').readlines()[:-1]
header = [f[1:-1] for f in data[0].strip().split(',')]
data = [[float(f[1:-1]) for f in d.strip().split(',')] for d in data[1:]]

indexes = {}

for event in data:
    tick, recv, id, idx = event
    if recv not in indexes:
        indexes[recv] = {}
    if id not in indexes[recv]:
        indexes[recv][id] = []

    indexes[recv][id].append((tick, idx))

for node, store in indexes.items():
    for n, vals in store.items():
        plt.plot(list(v[0] for v in vals), list(v[1] for v in vals), label="Node " + str(node) + ' index of ' + str(n))
    break

plt.legend()
plt.show()
