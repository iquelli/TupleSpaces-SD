Concurrent tests (with the exception of test1 and inputX) that recreate concurrent executions
what aim at stressing different corner cases of the replicated algorithms developed
in phases 2 and 3 of the project.

- The tests that start with input and test1 are just for one client.
- Tests 1-5 are especially tailored to the Xu-Liskov algorithm (but may also be used
with state-machine replication algorithm).
- Tests 6-8 are especially tailored to the state-machine replication algorithm (but may
also be tested with the Xu-Liskov algorithm).

Usage:
- For each test*, run as many clients as _C*.txt variants.
- In a single terminal, run the multiple clients in parallel, each client receiving test*_C*.txt as input.

Example:

``mvn exec:java -Dexec.args="..." < test5_C1.txt & mvn exec:java -Dexec.args="..." < test5_C2.txt & ``
