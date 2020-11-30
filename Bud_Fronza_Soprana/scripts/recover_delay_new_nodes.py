import pandas as pd
import sys
import os

baseFolder=sys.argv[1] #"..\\results\\2020-11-28T09-25-34-3121826

SUB_FOLDER='r'
SUB_SUB_FOLDER='application_received'

"""
# Read node_creation.csv
fname = os.path.join(baseFolder, SUB_FOLDER, "node_creation.csv")


# Read file and load all entries to internal datastructure
try:
    file = open(fname, "r")
    # Do something with the file
except IOError:
    print("Could not open file: " + fname);  
    sys.exit()

new_nodes=[]
for line in file:
    split = line.split(",")
    node_created = split[1]
    timestamp = split[1]
    if [timestamp] > 0.0:
        new_nodes.append(split)

print(new_nodes)
"""

node_creation = pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, "node_creation.csv"))
new_nodes = node_creation[node_creation['timestamp'] > 0.0]

# Read original_sendDelay.csv to get older messages 
#original_send_delay =  pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, "original_sendDelay.csv"))

# Take older messages from application instead
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
#print(all_mess.to_csv(index=False))

print(new_nodes) 

result_row = {
    'node':'',
    'avg_time':'',
    'var_rec':'',
    'min':'',
    'max':'',
    'percent_rec':''
}
#results = []
avg_times = []
vars = []
mins = []
maxs = []
percents = []

# get info for every node
for index, row in new_nodes.iterrows(): 
    print(row)
    timestamp = row['timestamp']
    node = int(row[' node_created'])
    node_csv = f'A{node}.csv'

    #get the list of IDs to recover
    to_recover = all_mess[all_mess['timestamp'] < timestamp]
    print (f"To recover, length:{len(to_recover)}")
   # print(to_recover)
    
    received = read_node(node_csv)
    #print("Received:")
    #print(received)
    # Recovered messages: intersection of the messages that had to be recovered with the ones that were received
    recovered = received[received['id'].isin(to_recover['id'])]
    recovered['time_needed'] = recovered['timestamp'] - timestamp
    # Get percentage of recovered messages vs the ones that were to recover
    percent_rec = (len(recovered) * 100)/len(to_recover)
    percents.append(percent_rec)
    print(f'Percentage recovered: {percent_rec}')
    #print("Recovered:")
    #print(recovered)
    print()

    mean = recovered['time_needed'].mean()
    avg_times.append(mean)
    var = recovered.var()['time_needed']
    vars.append(var)
    min = recovered['time_needed'].min()
    mins.append(min)
    max = recovered['time_needed'].max()
    maxs.append(max)

    print(f'mean {mean}, var {var}, min {min}, max {max}')

    result_row = {'node': node,'avg_time':mean, 'var':var, 'min':min,'max':max, 'percent_rec':percent_rec }
    #results.append(result_row)
    # Get average and var of recovered['time_needed']

    # What about network discovery?
    #to_discover_src = to_recover.drop_duplicates(['source'])['source'].unique()
    #print("To discover src:")
    #print(to_discover_src)

    # discovered_src = received[received['source'].isin(to_discover_src['source'])] #not working
    #print("Discovered src:")
   # print(discovered_src)
    #recovered = data[data['id'].isin(to_recover['id']) & data['source'] == row[' node_created']] 
    
    print()

results =  {
    'node' : new_nodes[' node_created'],
    'avg_time' : avg_times,
    'var' : vars, 
    'min' : mins, 
    'max' : maxs, 
    'percent_rec' : percents
}
df = pd.DataFrame(results, columns = ['node', 'avg_time','var','min','max','percent_rec'])
print(df.to_csv(index=False))


"""

# Read original_broadcast_send.csv -> for every node created after 0.0, have a list of ids to recover 
node_list_to_rec = {"node": "", "list" : "MAX ID" }
# nodo 0 
# nodo 1 ...
# oppure nodo 0, 1, ...

# Read application received of nodes; check when the ids in the list are received
for entry in node_list_to_rec:
    node = entry['node']
    to_rec = entry['list']
    id_received = read(f'A{}', node) 
    for id < to_rec: 
        timestamp_received = timestamp of id_received['id']
        # nodo 0 timestamp_received -> ce l'ho già probabilmente

        when all ids are received, put 
        node_creation, up_to_date   
        t1,         t2 

        time to recover all = t2 - t1 

        Do this for all the nodes and then take  a mean over the number of nodes 
"""