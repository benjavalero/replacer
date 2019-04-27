#!/usr/bin/python
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Import data
pages = pd.read_csv('pages.csv', sep='\t')

# Counts on namespace
ns_percent = pages['NS'].value_counts(normalize=True) * 100
ns_percent.plot.pie(y='NS', figsize=(5, 5), autopct='%1.1f%%')
plt.show()

# Take only the articles
articles = pages[pages.NS == 0]

# Basic stats
pd.set_option('float_format', '{:.2f}'.format)
# print(articles['Length'].describe())

# Box Plot (Log scale)
f, (ax) = plt.subplots(1, 1)
ax.set_yscale('log')
sns.boxplot(y="Length", data=articles, ax=ax)
plt.show()

# Print a random sample of 99 articles
sample_articles = articles.sample(n=99)
print sample_articles['ID'].to_string(index=False)

# Also print the maximum
print articles.iloc[articles['Length'].values.argmax()]['ID']