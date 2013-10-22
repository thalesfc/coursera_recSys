import numpy
import scipy.stats

user_list = [] # id => name
movie_list = [] # id => name

data = numpy.empty((100,25), dtype='float32')

def loadData(path):

  with open(path) as f:

    # get users
    udic = {}
    count = 0
    txt = f.readline()
    txt = txt.replace('"', '')
    tokens = txt.split(',')
    for token in tokens:
      if len(token) != 0:
        user = int(token)
        print "!", len(token), user
        try:
          udic[user]
        except KeyError:
          udic[user] = count
          count += 1
          user_list.append(user)
    
    # get data
    mdic = {}
    count = 0
    for line in f:
      tokens = line.strip().split(',')
      #header
      movie = int(tokens[0].split('"')[1].split(':')[0])
      try:
        mdic[movie]
      except KeyError:
        mdic[movie] = count
        count += 1
        movie_list.append(movie)

      # movie-id
      mid = mdic[movie]

      # data
      for i in xrange(1, len(tokens)):
        rating = tokens[i]
        if len(rating) != 0:
          data[mid, i-1] = float(rating)
#end fun

def correl():

  pass

def main():
  loadData('data/recsys-data-sample-rating-matrix.csv')
  correl()

if __name__ == "__main__":
  main()
