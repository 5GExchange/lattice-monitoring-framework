# Lattice Monitoring Framework
This project contains both the source code and some pre-compiled binaries of the Lattice Monitoring Framework.

The main components of the framework are:
- The Controller
- Data Sources
- Data Consumers

The Intelligent Resource Monitor (IRM) component in the MdO interacts with the Lattice Controller using a REST API (which represents the I3-Mon interface).

In general we can assume that a single Controller instance is up and running in a given 5GEx technological domain.

### Build
The provided `build.xml` (under `source/`) can be used both for compiling the source code and generating the deployable jar files.
```sh
$ cd source/
$ ant dist
```
The above command generates two different jar binary files in the jars directory:
- `monitoring-bin-controller.jar` containing all the classes and dependencies related to the controller.
- `monitoring-bin-core.jar` containing a subset of classes and dependencies that can be used for instantiating Data Sources and Data Consumers.

and also a jar containing the source code
- `monitoring-src.jar`

### Installation
As soon as the Build process is completed, the controller can be started as follows:
```sh
$ cd jars/
$ java -cp monitoring-bin-controller.jar eu.fivegex.monitoring.control.controller.Controller controller.properties
```
The `controller.properties` file contains the configuration settings for the controller (an example is reported under `conf/`)

### Configuration
```
info.localport = 6699
``` 
is the local port used by the Controller when connecting to the Information Plane. Other Lattice entities (e.g., Data Sources) will connect to this port.

```
restconsole.localport = 6666
```
is the port where the controller will listen for HTTP control requests coming from the orchestration layer.
```
probes.package = eu.fivegex.demo.probes
probes.suffix = Probe
```
When generating the probe catalogue, the Controller will look for all the class files whose package name matches `eu.fivegex.demo.probes` and whose name terminates with `Probe` (e.g., `eu.fivegex.demo.probes.docker.DockerProbe.class`).

```
deployment.enabled = true
```
Can be set either to `true` or `false` and enables/disables respectively the automated Data Sources deployment functionality to a remote host (current implementation is based on SSH with public key authentication)
```
deployment.localJarPath = /Users/lattice
deployment.jarFileName = monitoring-bin-core.jar
deployment.remoteJarPath = /tmp
deployment.dsClassName = eu.fivegex.demo.SimpleDataSourceDaemon
```
The above settings allow to specify (in order):
- the path where the jar (to be used for the Data Source automated remote deployment) is located
- the file name of the above jar file
- the path where the jar will be copied on the remote machine where the Data Source is being deployed
- the class name of the Data Source to be started (it must be in the specified jar)

### Testing
In order to verify that the Controller is up and running, a quick check consists in performing a `GET` request to `/probe/catalogue/`. The expected result is a response code `200` and `Content-Type: application/json`.
