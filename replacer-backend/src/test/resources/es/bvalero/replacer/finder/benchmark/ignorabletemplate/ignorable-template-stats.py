#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import seaborn as sns

matplotlib.use("TkAgg")

# Import data
words = pd.read_csv('ignorable-template-benchmark.csv', sep='\t')

# Box Plot (Log)
f, (ax) = plt.subplots(1, 1, figsize=(12, 4))
ax.set_xscale('log')
sns.boxplot(y="FINDER", x="TIME", data=words, ax=ax)
plt.show()
