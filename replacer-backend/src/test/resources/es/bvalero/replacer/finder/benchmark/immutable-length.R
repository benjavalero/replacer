library(tidyverse)

immutables <- read_delim('~/Developer/replacer/replacer-backend/file-name2.txt', na = " ",
    delim = '\n', col_names = FALSE, col_types = "c", trim_ws = TRUE, skip_empty_rows = TRUE) %>%
  mutate(L = str_length(X1))

immutables %>%
  # mutate(id = row_number()) %>%
  filter(is.na(L))

immutables %>%
  filter(!is.na(L)) %>%
  summarise(
    Media = mean(L),
    Mínimo = min(L),
    Mediana = median(L),
    Q99999 = quantile(L, .99999),
    Máximo = max(L))

immutables %>%
  filter(L > 200) %>%
  print(n = 50) %>%
  count()

immutables %>%
  filter(L < 3) %>%
  print(n = 50) %>%
  count()
