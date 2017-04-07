#!/bin/sh -e
# This script executes the CI tests for the 5GEx MDO.

RESULT=0

DDO_IP=docker-orchestrator.ci1.5gex
echo "installing java (if needed...)" >> log.txt
ssh ubuntu@$DDO_IP 'sudo apt-get -y install openjdk-8-jre-headless'
echo "creating dir..." >> log.txt
ssh ubuntu@$DDO_IP 'mkdir -p lattice-monitoring-framework'
scp -r jars ubuntu@$DDO_IP:~/lattice-monitoring-framework 
scp -r config ubuntu@$DDO_IP:~/lattice-monitoring-framework 
echo "Running..." >> log.txt
ssh ubuntu@$DDO_IP 'cd lattice-monitoring-framework && java -cp jars/monitoring-bin-controller-0.7.1.jar eu.fivegex.monitoring.control.controller.Controller config/controller.properties'
