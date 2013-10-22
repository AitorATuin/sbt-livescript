take = (n, [x, ...xs]:list) -->
  | n <= 0     => []
  | empty list => []
  | otherwise  => [x] ++ take n - 1, xs

take 2, [1 2 3 4 5] #=> [1, 2]
