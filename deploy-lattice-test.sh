#!/bin/sh -e
# This script executes the CI tests for the 5GEx MDO.

RESULT=0

DDO_IP=docker-orchestrator.ci1.5gex
echo "installing java (if needed...)"
ssh ubuntu@$DDO_IP 'sudo apt-get -y install openjdk-8-jre-headless'
echo "stopping java processes (if any)"
ssh ubuntu@$DDO_IP 'sudo killall java'
echo "creating dir..."
ssh ubuntu@$DDO_IP 'mkdir -p lattice-monitoring-framework'
scp -r dist/jars ubuntu@$DDO_IP:~/lattice-monitoring-framework 
scp -r conf ubuntu@$DDO_IP:~/lattice-monitoring-framework 
echo "Running..."
ssh ubuntu@$DDO_IP 'cd lattice-monitoring-framework &&  nohup java -cp jars/monitoring-bin-controller-0.7.1.jar eu.fivegex.monitoring.control.controller.Controller conf/controller.properties > lattice.out 2> lattice.out < /dev/null &'
