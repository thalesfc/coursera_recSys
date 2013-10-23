import numpy
import pandas
import math

data = pandas.read_csv('data/recsys-data-sample-rating-matrix.csv', index_col = 0)

user = '860'

ru_til = data[user].mean()
similarities = data.corr()[user].copy()
similarities.sort(ascending=False)
rec_list = []
rec_index = []

# for all movies
for movie in data.index:
  up = 0.0
  dp = 0.0
  
  # for the top 5 neighbors
  for i in xrange(1, 6):

    neigh = similarities.index[i]
    wn = similarities.iloc[i]
    rn_til = data[neigh].mean()

    rn = data.ix[movie, neigh]
    if not pandas.isnull(rn):
      value = (rn - rn_til) * wn
#      print "(", rn, "-", rn_til, ") *", wn, "=", value
      up += value
      dp += wn
  
  # this movie score
#  print dp, up
  if dp == 0:
    continue
  m_score = ru_til + up/dp
  rec_list.append(m_score)
  rec_index.append(movie)

  print movie, " score ->", "%.3f" % m_score

# creating the serie and retrieving the top
serie = pandas.Series(rec_list, index=rec_index)
serie.sort(ascending=False)
for i in xrange(5):
  m_score = serie.iloc[i]
  movie = serie.index[i]
  print movie.split(':')[0], "%.3f" % m_score

