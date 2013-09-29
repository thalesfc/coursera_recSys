import numpy

ffile = open('saida', 'w')

def get_names(array, k, recommendations):
  returned = []
  for i in range(k):
    score, mid = recommendations[i]
    returned.append((array[mid],score))
  return returned


# given a test movie, return the top-5 ranked itens
def top_movies(test_movie_id, matrix):
  size = matrix.shape
  x = URM[:, test_movie_id] > 0 # array with positive ratings for the test movie

  rec_list = []

  for movie_id in xrange(size[1]):
    if movie_id == test_movie_id:
      continue
    
    y = URM[:, movie_id] > 0 # array with positive ratings for the given movie
    sim = fun_score(x, y)
    rec_list.append((sim, movie_id))

  # sorting
  sorted_list = sorted(rec_list, reverse=True)

  return sorted_list


def fun_score(x, y):
  # simple formula: (x and y) / x
  #return float(sum(x*y))/ float(sum(x))

  # advanced formula: ((x and y) / x) / ((!x and y) / !x)
  # == p1 / p2
  p1 = float(sum(x*y))/ float(sum(x))
  p2 = float(sum(~x*y))/ float(sum(~x))
  return p1/p2


# main
user_dic = {} 
count_u = 0

movie_dic = {}
movie_id_array = []
count_m = 0

data = []

with open('recsys-data-ratings.csv') as f:
  for line in f:
    line = line.strip()
    tk1, tk2, tk3 = line.split(',') # user, movie, rating
    # loading the user => user_id dic
    try:
      user_id = user_dic[tk1]
    except KeyError:
      user_dic[tk1] = count_u
      user_id = count_u
      count_u += 1

    #loading the movie => movie_id dic
    try:
      movie_id = movie_dic[tk2]
    except KeyError:
      movie_dic[tk2] = count_m
      movie_id = count_m
      movie_id_array.append(tk2)
      count_m += 1
    
    # computing rating
    rating = float(tk3)

    # savind to data array
    data.append((user_id, movie_id, rating))

# create the numpy matrix with the number of movies and user 
# user versus movie  URM[user_id][movie_id] = rating
URM = numpy.zeros((len(user_dic), len(movie_dic)), dtype=float)
for (user_id, movie_id, rating) in data:
  URM[user_id, movie_id] = rating

# few testings ==> OK
#assert URM[user_dic['1'], movie_dic['809']] == 4., "URM['1', '809'] != 4"
#assert URM[user_dic['199'], movie_dic['107']] == 4.5, "URM['199', '107' = 4.5 -> however it is *= " +  URM[user_dic['199'], movie_dic['107']]

# getting the recommendations
# assignments: 807 329 557
for test in ['807', '329', '557']:
  s_list = top_movies(movie_dic[test], URM)
  rec_list = get_names(movie_id_array, 5, s_list)
  ffile.write(test)
  for rec in rec_list:
    movie, score = rec
    ffile.write(',{0},{1}'.format(movie, score))
  ffile.write("\n")

ffile.close()
