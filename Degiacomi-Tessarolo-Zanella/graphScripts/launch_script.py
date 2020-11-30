from os import listdir, system
from os.path import isfile


protocol_folder_name = "1"

output_path = '../output/'
tmp_path = output_path + protocol_folder_name
scenarios_dir = listdir(tmp_path)

csv_filename = protocol_folder_name + ".csv"

for scenario in scenarios_dir:
    print(scenario)
    path = tmp_path + "/" + scenario + "/output/"
    txt_files = listdir(path)
    relay, station = '', ''
    for txt in txt_files:
        if 'OutputRelay' in txt and 'batch_param_map.' not in txt:
            relay = path + '/' + txt
        if 'OutputStation' in txt and 'batch_param_map.' not in txt:
            station = path + '/' + txt

    cmd = "python3 create_plots.py --relays {} --stations {} --scenario {} --print_only_to {}".format(relay, station, scenario, csv_filename)
    system(cmd)

cmd = "python3 plot_multiple_runs.py --input_csv {} --max_tick 1500".format(csv_filename)
system(cmd)
