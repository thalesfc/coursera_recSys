import pandas
import numpy
import matplotlib.pyplot as plt

udata = pandas.read_csv('target/analysis/eval-user.csv', header = 0, index_col = 0)

#########################################
def operate(algorithms, field_name):
    x = []
    # for each algorithm
    for algo in algorithms:
        #values array stores the user-mean RMSE of each algo.
        values = []
        # for each fold
        for fold in range(5):
            values.append(udata[udata['Partition'] == fold].loc[algo][field_name].mean())
        # compute the mean
        me = numpy.mean(values)
        x.append(me)
    return x
#########################################

#############
### user-mean RMSE
algorithms = ['PersMean', 'ItemMean', 'GlobalMean']
x = operate(algorithms, 'RMSE') 
y = [2, 1, 0]
plt.scatter(x, y)
plt.savefig('uRMSE.pdf')

########################
# top nDCG
plt.clf()
algorithms = ['Popular', 'PersMean', 'ItemMean']
x = operate(algorithms, 'TopN.nDCG')
plt.scatter(x, y)
plt.savefig('TOPnDCG.pdf')
