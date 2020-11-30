RES_PATH=$(realpath $1)

echo "Creating results directory..."
if ! mkdir -p "$RES_PATH/r"
then
	echo 'The "r" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

./get_broadcast_send.sh $RES_PATH/Oracle.zip > $RES_PATH/r/broadcast.csv
./get_original_broadcast_send.py $RES_PATH/r/broadcast.csv > $RES_PATH/r/original_broadcast.csv

if ! mkdir $RES_PATH/r/application_received
then
	echo 'The "r/application_received" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

for X in $(ls $RES_PATH/A*.zip)
do
	NAME=$(basename "$X" | cut -f 1 -d '.')
	./get_application_received_ids.sh $X > "$RES_PATH/r/application_received/$NAME.csv"
done

if ! mkdir $RES_PATH/r/collision_type
then
	echo 'The "r/collision_type" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

for X in $(ls $RES_PATH/A*.zip); do
	NAME=$(basename "$X" | cut -f 1 -d '.')
	./get_collision_type.sh $X > "$RES_PATH/r/collision_type/$NAME.csv"
done

./get_sendDelay.py $RES_PATH/r/broadcast.csv > $RES_PATH/r/sendDelay.csv
./get_sendDelay.py $RES_PATH/r/original_broadcast.csv > $RES_PATH/r/original_sendDelay.csv

./get_diffusion_time.py $RES_PATH $RES_PATH/r/diff_time.csv
./get_diffusion_time_wo_original_sendDelay.py $RES_PATH $RES_PATH/r/diff_time_wo_original_sendDelay.csv

./get_node_creation.sh $RES_PATH/Oracle.zip  > $RES_PATH/r/node_creation.csv



./new_nodes_recovery.py $RES_PATH > $RES_PATH/r/new_node_stats.csv

