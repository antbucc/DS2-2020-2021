import pandas as pd
import sys
import os

baseFolder=sys.argv[1] #"..\\results\\2020-11-28T09-25-34-3121826

SUB_FOLDER='r'
SUB_SUB_FOLDER='application_received'

node_creation = pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, "node_creation.csv"))
new_nodes = node_creation[node_creation['timestamp'] > 0.0]

# Take older messages from application
nodes = [
    f
    for f in os.listdir(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER))
    if os.path.isfile(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER, f)) and f.endswith('.csv')
]

def read_node(node):
    mess = pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLDER, node))
    mess['source'] = int(node[1:-4])

    return mess

data = pd.concat([read_node(node) for node in nodes])

all_mess = data.sort_values('timestamp').drop_duplicates('id')


avg_times = []
devs = []
mins = []
maxs = []
to_recs = []
recovereds = []
percents = []

# get info for every node
for index, row in new_nodes.iterrows(): 
    timestamp = row['timestamp']
    node = int(row[' node_created'])
    node_csv = f'A{node}.csv'

    #get the list of IDs to recover
    to_recover = all_mess[all_mess['timestamp'] < timestamp]
    
    received = read_node(node_csv)
    # Recovered messages: intersection of the messages that had to be recovered with the ones that were received
    recovered = received[received['id'].isin(to_recover['id'])]
    recovered['time_needed'] = recovered['timestamp'] - timestamp

    # Get percentage of recovered messages vs the ones that were to recover
    percent_rec = (len(recovered) * 100)/len(to_recover)
    to_recs.append(len(to_recover))
    recovereds.append(len(recovered))
    percents.append(percent_rec)

    mean = recovered['time_needed'].mean()
    avg_times.append(mean)
    dev = recovered['time_needed'].std()
    devs.append(dev)
    min = recovered['time_needed'].min()
    mins.append(min)
    max = recovered['time_needed'].max()
    maxs.append(max)

    result_row = {'node': node,'avg_time':mean, 'dev':dev, 'min':min,'max':max, 'percent_rec':percent_rec }


results =  {
    'node' : new_nodes[' node_created'],
    'avg_time' : avg_times,
    'dev' : devs, 
    'min' : mins, 
    'max' : maxs, 
    'to_rec' : to_recs,
    'recovered' : recovereds,
    'percent_rec' : percents
}
df = pd.DataFrame(results, columns = ['node', 'avg_time','dev','min','max','to_rec','recovered','percent_rec'])
print(df.to_csv(index=False))