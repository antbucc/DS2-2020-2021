RES_PATH=$(realpath $1)

echo "Creating results directory..."
if ! mkdir -p $RES_PATH/r
then
	echo 'The "r" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

echo "Creating the application_received directory..."
if ! mkdir $RES_PATH/r/application_received
then
	echo 'The "r/application_received" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

echo "Creating the img directory..."
if ! mkdir $RES_PATH/r/img
then
	echo 'The "r/img" folder already exists. Did you already calculated the results of this scripts?'
	exit 1
fi

echo 'Get total number of protocols...'
./data_analysis/get_number_of_protocol.sh $RES_PATH > $RES_PATH/r/num_protocols.txt

echo 'Get ids received by the application...'
for X in $(ls $RES_PATH/A*.zip)
do
	NAME=$(basename "$X" | cut -f 1 -d '.')
	./data_analysis/received_ids.sh $X > "$RES_PATH/r/application_received/$NAME.csv"
done

echo 'Calculate diffusion time...'
./data_analysis/diffusion_time.py $RES_PATH --output $RES_PATH/r/diff_time.csv

echo 'Graph diffusion time...'
./graphs/diffusion.py $RES_PATH --output $RES_PATH/r/img/diffusion.svg
