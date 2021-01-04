import pandas as pd
import sys
import os
import numpy as np
import scipy.interpolate
import argparse

# First let's parse the arguments from the command line

parser = argparse.ArgumentParser(description='Get the mean and std of the diffusion time given a time delay')
parser.add_argument('input', metavar='INPUT', type=str, help='input folder')
parser.add_argument('output', metavar='output', type=str, help='output file')
#parser.add_argument('--output', metavar='OUTPUT', type=str, help='output csv filename', default=sys.stdout)
#parser.add_argument('--nodes', type=int, help='number of nodes', default=25)

args = parser.parse_args()


baseFolder=sys.argv[1] #"../results/six..
fname = sys.argv[2]

# Read file and load all entries to internal datastructure
"""
try:
    file = open(fname, "w")
    # Do something with the file
except IOError:
    print("Could not open file: " + fname);  
    sys.exit()
"""
SUB_FOLDER = 'r'
SUB_SUB_APP = 'application_received'
SUB_SUB_FOLLOW = 'get_follow'
SUB_SUB_UNFOLLOW = 'get_unfollow'

follow = 'FOLA'
app = 'A'
ext = '.csv'

n_nodes = 0
node_following = []
nodes_content = []
nodes_data = []
creation_time = []

#[B follow A] -> [A crea 0] ->  [B riceve 0]


nodes = [
    os.path.join(args.input, SUB_FOLDER, SUB_SUB_APP, f)
    for f in os.listdir(os.path.join(args.input, SUB_FOLDER, SUB_SUB_APP))
    if os.path.isfile(os.path.join(args.input, SUB_FOLDER, SUB_SUB_APP, f)) and f.endswith('.csv')
]

n_nodes = len(nodes)

# Read nodes data
for node in nodes:
	#print(node)
	data = pd.read_csv(node)
	nodes_content.append(data)
	tmp = data.astype({'timestamp': np.float32, 'address': 'category', 'port': 'category', 'id': np.int32})

	#entry = { 'target' : nodes_splitted[len(nodes_splitted)['port']], 'value' : tmp.groupby('id').agg({'timestamp': 'min'}).reset_index().rename(columns= {'timestamp': 'tmin'})}
	#creation_time.append(entry)

print(f'Number of nodes (split on addresses): {len(nodes_content)}')

# Split nodes in identities
nodes_splitted = [] # (A1, PUBKEY1; A1, PUBKEY2; ...)
for index in range(len(nodes_content)):
	ports = []

	for port in range(len(nodes_content[index]['port'])):
		
		new_port = nodes_content[index]['port'][port]
		if new_port not in ports:
			#print(f'{nodes[index]} {new_port}')
			ports.append(new_port)
			new_node = nodes_content[index][nodes_content[index]['port'] == nodes_content[index]['port'][port]]
			nodes_splitted.append(new_node)

#{(A1, Pubk1) : (content)}	

print(f'Number of nodes splitted (split on addresses + port): {len(nodes_splitted)}')

# Now collecting followers
for index in range(n_nodes):
	follow_file = follow + str(index) + ext
	node_following.append(pd.read_csv(os.path.join(baseFolder, SUB_FOLDER, SUB_SUB_FOLLOW, follow_file)))

follow_splitted = [] # (A1, Target2; A1, PUBKEY2; ...)
for index in range(len(node_following)):
	ports = []

	for port in range(len(node_following[index]['port'])):
		
		new_port = node_following[index]['port'][port]
		if new_port not in ports:
			#print(f'{nodes[index]} {new_port}')
			ports.append(new_port)
			new_node = node_following[index][node_following[index]['port'] == node_following[index]['port'][port]]
			follow_splitted.append(new_node)
	
print(f'{len(follow_splitted)}')


# Get creation of ids
tmp = pd.concat([pd.read_csv(node) for node in nodes])
data = tmp.astype({'timestamp': np.float32, 'address': 'category', 'port': 'category', 'id': np.int32})
del tmp
creation_time = data.groupby('id').agg({'timestamp': 'min'}).reset_index().rename(columns= {'timestamp': 'tmin'})
nodes_creation_time =  pd.merge(data, creation_time,  how='right', left_on = ['id','timestamp'], right_on = ['id','tmin'])
del creation_time


print(f'App data created:{len(nodes_creation_time)}')
#print(nodes_creation_time)

# Dynamically create set of followers and see when it was received
i = 0
time = {'id' : 'value', 'tmin' : 'creation_time', 'mean' : 'value'}
times = []

# Scrive un file.
out_file = open(fname,"a")
out_file.write('id, creation, mean\n')

for index, entry in nodes_creation_time.iterrows():
	#print(f'Entry: {entry}')
	appdata = entry['id']
	creation = entry['tmin']
	generator = entry['port']
	#print(f'Generator: {generator}')
	#print("Creation", creation)

	if (creation != 0):
		followers = []
		for entity in follow_splitted:
			for j, row in entity.iterrows():
				#print(f'Row of follow: {row}')
				target = row['target']
				#print(f'Target {target}')

				if row['target'] == generator and row['timestamp'] < creation:
					followers.append(row['port'])
		
		all_times = []


		for follower in followers:			
			# take only data that is on port FOLLOWER and with ID appdata
			follower_data = data[data['port'] == follower]
			follower_received = follower_data[follower_data['id'] == appdata]
			if not follower_received.empty:
				timestamp = follower_received.iat[0,0]
				all_times.append(timestamp)

			"""	 
			for node in nodes_splitted:
				matching = node[node['port'] == follower]
				if not matching.empty:
					matching_id = matching[matching['id'] == appdata]
					#print(f"MATCH {matching_id}")
					if not matching_id.empty:
						timestamp = matching_id.iat[0,0]
						#print(f"TIME {timestamp}")
						all_times.append(timestamp)
						
						#print(matching_id)
						# """
		
		#print(f'ALL {all_times} \n')
		id_average = -1
		if len(all_times) != 0:
			id_average = (sum(all_times) - creation * len(all_times)) / len(all_times)
			#print(f'AVERAGE {id_average}\n')

		#print(f'Id: {appdata} Created: {creation} Mean: {id_average}\n')
		out_file.write(f'{appdata}, {creation}, {id_average}\n')

	# Progression
	#print(f'Completed: [{index}/{len(nodes_creation_time)}]', end='\r')
	print(f'Completed: {index/len(nodes_creation_time)*100:.1f} %', end='\r')

	#current_it = {'id' : appdata, 'tmin' : creation, 'mean' : id_average}
	#times.append(current_it)

out_file.close()
#to_recover = all_mess[all_mess['timestamp'] < timestamp]