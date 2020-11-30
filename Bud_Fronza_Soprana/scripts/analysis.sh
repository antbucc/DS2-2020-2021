RES_PATH=$(realpath $1)

#Original broadcast send of message
./get_original_broadcast_send.py $RES_PATH/r/broadcast.csv > $RES_PATH/r/original_broadcast.csv

#Send delay
./get_sendDelay.py $RES_PATH/r/broadcast.csv > $RES_PATH/r/sendDelay.csv

#Original delay
./get_sendDelay.py $RES_PATH/r/original_broadcast.csv > $RES_PATH/r/original_sendDelay.csv

#Diffusion
./get_diffusion_time.py $RES_PATH > $RES_PATH/r/diff_time.csv

#Diffusion without delay
./get_diffusion_time_wo_original_sendDelay.py $RES_PATH > $RES_PATH/r/diff_time_wo_original_sendDelay.csv
