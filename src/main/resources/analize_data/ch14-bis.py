# Sebastian Raschka, 2015 (http://sebastianraschka.com)
# Python Machine Learning - Code Examples
#
# Chapter 10 - Predicting Continuous Target Variables with Regression Analysis
#
# S. Raschka. Python Machine Learning. Packt Publishing Ltd., 2015.
# GitHub Repo: https://github.com/rasbt/python-machine-learning-book
#
# License: MIT
# https://github.com/rasbt/python-machine-learning-book/blob/master/LICENSE.txt


import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LinearRegression
from sklearn.linear_model import RANSACRegressor
#from sklearn.cross_validation import train_test_split
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score
from sklearn.metrics import mean_squared_error
from sklearn.linear_model import Lasso
from sklearn.preprocessing import PolynomialFeatures
from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor

# Added version check for recent scikit-learn 0.18 checks
from distutils.version import LooseVersion as Version
from sklearn import __version__ as sklearn_version
if Version(sklearn_version) < '0.18':
    from sklearn.cross_validation import train_test_split
else:
    from sklearn.model_selection import train_test_split

#############################################################################
'''
TIMESTAMP,SYMBOL,
CLOSE,
OPEN,HIGH,LOW,BB_UPPER,
BB_MIDDLE,BB_LOWER,EMA,RSI,
INTERVAL_MIN
'''
print(50 * '=')
print('Section: Exploring the Housing dataset')
print(50 * '-')

df = pd.read_csv('analyze-prices-split-adjusted.csv',
                 header=0,
                 #usecols=[2,3,4,5,6],
                 #usecols=[2,3,4,5,6],
                 usecols=[2,6,7,8,9,10],
                 sep=',')

cols = ['CLOSE', 
        #'A_CLOSE','B_CLOSE','C_CLOSE','D_CLOSE'
        #'OPEN','HIGH','LOW','BB_UPPER'
        'BB_UPPER','BB_MIDDLE','BB_LOWER','EMA','RSI'
        ]

df.columns = cols
print('Dataset excerpt:\n\n', df.head())


#############################################################################
print(50 * '=')
print('Section: Visualizing the important characteristics of a dataset')
print(50 * '-')

sns.set(style='whitegrid', context='notebook')

sns.pairplot(df[cols], height=2.5)
# plt.tight_layout()
# plt.savefig('./figures/scatter.png', dpi=300)
plt.show()


cm = np.corrcoef(df[cols].values.T)
sns.set(font_scale=1.5)
hm = sns.heatmap(cm,
                 cbar=True,
                 annot=True,
                 square=True,
                 fmt='.2f',
                 annot_kws={'size': 15},
                 yticklabels=cols,
                 xticklabels=cols)

# plt.tight_layout()
# plt.savefig('./figures/corr_mat.png', dpi=300)
plt.show()

sns.reset_orig()


