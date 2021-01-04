RES_PATH=$(realpath $1)


echo "Creating the get_follow directory..."
if ! mkdir $RES_PATH/r/get_follow
then
	echo 'The "r/get_follow" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

for X in $(ls $RES_PATH/A*.zip)
do
	NAME=$(basename "$X" | cut -f 1 -d '.')
	./data_analysis/get_follow.sh $X > "$RES_PATH/r/get_follow/FOL$NAME.csv"
	#./data_analysis/get_unfollow.sh $X > "$RES_PATH/r/get_follow/UNFOL$NAME.csv"
done

echo "Creating the store_received directory..."
if ! mkdir $RES_PATH/r/store_received
then
	echo 'The "r/store_received" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

for X in $(ls $RES_PATH/A*.zip)
do
	NAME=$(basename "$X" | cut -f 1 -d '.')
	./data_analysis/get_store.sh $X > "$RES_PATH/r/store_received/Store$NAME.csv"
done