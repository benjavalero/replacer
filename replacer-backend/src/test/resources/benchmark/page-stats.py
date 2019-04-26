#!/usr/bin/python
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Import data
pages = pd.read_csv('pages.csv', sep='\t')

# matplotlib histogram
# plt.hist(pages['Length'])
# plt.show()

# Density Plot and Histogram of all arrival delays
# sns.distplot(pages['Length'], hist=True, kde=True)
# plt.show()

# sns.kdeplot(pages['Length'])
# plt.show()

#Box-plot
# f, (ax) = plt.subplots(1, 1)
# sns.boxplot(x="NS", y="Length", data=pages, ax=ax)
# plt.show()