#!/bin/sh -e
# This script executes the CI tests for the 5GEx MDO.

RESULT=0

DDO_IP=docker-orchestrator.ci1.5gex

ssh ubuntu@$DDO_IP 'sudo apt-get -y install openjdk-8-jre-headless'
ssh ubuntu@$DDO_IP 'mkdir -p lattice-monitoring-framework' 
ssh ubuntu@$DDO_IP 'cd lattice-monitoring-framework'
ssh ubuntu@$DDO_IP 'wget -r -l1 --no-parent  https://5gex.tmit.bme.hu/jenkins/job/lattice-pb/lastStableBuild/artifact/jars/'
ssh ubuntu@$DDO_IP 'wget -r -l1 --no-parent  https://5gex.tmit.bme.hu/jenkins/job/lattice-pb/lastStableBuild/artifact/conf/'
ssh ubuntu@$DDO_IP 'java -cp monitoring-bin-controller.jar eu.fivegex.monitoring.control.controller.Controller controller.properties' 
