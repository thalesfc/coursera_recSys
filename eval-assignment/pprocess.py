import pandas
import matplotlib.pyplot as plt

udata = pandas.read_csv('target/analysis/eval-user.csv', header = 0, index_col = 0)

# getting the mean per-user RMSE (RMSE.ByUser) for each algorithm
PM_RMSE = udata.loc['PersMean'].mean()['RMSE']
IM_RMSE = udata.loc['ItemMean'].mean()['RMSE']
GB_RMSE = udata.loc['GlobalMean'].mean()['RMSE']

print 'PersMean', PM_RMSE
print 'ItemMean', IM_RMSE
print 'GlobalMean', GB_RMSE

x = [PM_RMSE, GB_RMSE, IM_RMSE]
y = [2, 0, 1]
#print x, y
#y_label = ['PersMean', 'GlobalMean', 'ItemMean']
#plt.scatter(x, y)
#plt.savefig('uRMSE.pdf')

POP_TnDCG = udata.loc['Popular']['TopN.nDCG'].mean()
PM_TnDCG = udata.loc['PersMean']['TopN.nDCG'].mean()
IM_TnDCG = udata.loc['ItemMean']['TopN.nDCG'].mean()

x = [POP_TnDCG, PM_TnDCG, IM_TnDCG]
print x
plt.scatter(x, [0, 1, 2])
plt.savefig('TOPnDCG.pdf')
plt.show()
