# TupleSpaces

Distributed Systems Project 2024

**Group A06**

**Difficulty level: I am Death incarnate!**

### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name                | User                               | Email                                           |
|--------|---------------------|------------------------------------|-------------------------------------------------|
| 102533 | Vasco Paisana       | <https://github.com/vascopaisana>  | <mailto:vasco.paisana@tecnico.ulisboa.pt>       |
| 102624 | Raquel Braunschweig | <https://github.com/iquelli>       | <mailto:raquel.braunschweig@tecnico.ulisboa.pt> |
| 103124 | Gonçalo Bárias      | <https://github.com/goncalobarias> | <mailto:goncalo.barias@tecnico.ulisboa.pt>      |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3).
The clients is in _Client_.
The definition of messages and services is in _Contract_. The future name server
is in _NameServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation

To compile and install all modules:

```s
mvn clean install
```

After that, go to the `Contract` folder and run the following command:

```s
mvn exec:exec
```

### Running

To run the server, the following command can be performed in the respective server folder:

```s
mvn exec:java -Dexec.args="<port> <qualifier>"
```

To run the client, the following command can be performed in the respective client folder:

```s
mvn exec:java -Dexec.args="<client id>"
```

To run the name server, the following command can be performed in the respective name server folder:

```s
python server.py
```

#### Running in Debug Mode

To execute any of the modules in debug mode, add `-Ddebug` to the previous `mvn` commands
and `-log=DEBUG` to the previous `python` command.

### Formatting

We used [Spotless](https://github.com/diffplug/spotless) in this project to ensure code
formatting rules are followed.

It is possible to check if the code adheres to the formatting rules by running:

```s
mvn spotless:check
```

Or, alternatively, actually format the code:

```s
mvn spotless:apply
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
