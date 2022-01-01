#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
import seaborn as sns

matplotlib.use("TkAgg")

# Import data
pages = pd.read_csv('pages.csv', sep='\t')

# Number of pages
print("Number of pages:", pages.shape[0])

# Counts on namespace
ns_percent = pages['NS'].value_counts(normalize=True) * 100
ns_percent.plot.pie(y='NS', figsize=(5, 5), autopct='%1.1f%%')
plt.show()

# Take only the articles
articles = pages[pages.NS == 0]
print("Number of articles:", articles.shape[0])

# Counts on redirect
redirect_percent = articles['Redirect'].value_counts(normalize=True) * 100
redirect_percent.plot.pie(y='Redirect', figsize=(5, 5), autopct='%1.1f%%')
plt.show()

# Skip the redirection articles
articles = articles[articles.Redirect == False]

# Basic stats
pd.set_option('float_format', '{:.2f}'.format)
print(articles['Length'].describe())

# Box Plot (Log scale)
f, (ax) = plt.subplots(1, 1)
ax.set_yscale('log')
sns.boxplot(y="Length", data=articles, ax=ax)
plt.show()

# Print a random sample of 100 articles
sample_articles = articles.sample(n=100)
print(sample_articles['ID'].to_string(index=False))
