Standalone sanity test for the Bukkit-free core math (Chances/Scaling/WeightedTable/EffectSpec), runnable
without Paper on the classpath:

```
javac -d /tmp/out $(find ../../src/main/java/dev/failxos/failrunes/api ../../src/main/java/dev/failxos/failrunes/core -name '*.java') CoreTest.java
java -cp /tmp/out CoreTest
```
