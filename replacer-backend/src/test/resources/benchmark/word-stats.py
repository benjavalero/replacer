#!/usr/bin/python
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Import data
words = pd.read_csv('word-benchmark.csv', sep='\t')

# Box Plot (Log)
f, (ax) = plt.subplots(1, 1, figsize=(12, 4))
ax.set_xscale('log')
filtered_words = words[words.FINDER != 'WordMatchDotAllCompleteLazyFinder']
sns.boxplot(y="FINDER", x="TIME", data=filtered_words, ax=ax)
plt.show()
