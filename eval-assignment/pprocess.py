import pandas
import numpy
import matplotlib.pyplot as plt

udata = pandas.read_csv('target/analysis/eval-user.csv', header = 0, index_col = 0)

algorithms = ['PersMean', 'ItemMean', 'GlobalMean']
x =[]

# for each algorithm
for algo in algorithms:
    #values array stores the user-mean RMSE of each algo.
    values = []
    # for each fold
    for fold in range(5):
        values.append(udata[udata['Partition'] == fold].loc[algo]['RMSE'].mean())
    # compute the mean
    print values
    me = numpy.mean(values)
    print me
    x.append(me)

y = [2, 1, 0]
plt.scatter(x, y)
plt.savefig('uRMSE.pdf')
