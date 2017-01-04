#Generate OOM 3

Sample application to generate Out of memory error. 

##Compile

        javac GenerateOOM3.java

##Run

        java GenerateOOM3
or

        java GenerateOOM3 <int> numberOfIteration <int> memoryGrowthTime <int> memoryFreeTime <int> sleepTime



| Options  |  Description  | Default |
| --------|---------|-------|
|***\<int> numberOfIteration*** | Number Of Iteration | 5 |
| ***\<int> memoryGrowthTime*** | Memory Growth Time (ms) | 5000 |
| ***\<int> memoryFreeTime*** | Memory Free Time (ms) | 5000 |
| ***\<int> sleepTime*** | Sleep Time (ms) | 0 |

---

**CPU and Memory behaviour of this App**

![Generate OOM 3]
(https://github.com/wso2-incubator/automatic-anomaly-detection/blob/master/jvm-monitor-agent/src/samples/applications/GenerateOOM3/GenerateOOM3.jpg)*Draw a graph using [VisualVM](https://visualvm.java.net)*
