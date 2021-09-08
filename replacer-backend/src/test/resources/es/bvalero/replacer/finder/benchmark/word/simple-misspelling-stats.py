#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Import data
words = pd.read_csv('simple-misspelling-benchmark.csv', sep='\t')

# Basic stats
pd.set_option('float_format', '{:.2f}'.format)
print(words.groupby('FINDER').describe())

# Box Plot (Log)
f, (ax) = plt.subplots(1, 1, figsize=(12, 5))
# ax.set_xscale('log')
sns.boxplot(y="FINDER", x="TIME", data=words, ax=ax)
plt.show()
