#!/usr/bin/env bash

./gradlew bootRun --args='--spring.batch.job.name=terminatorJob2 missionName=안산_데이터센터_침투,java.lang.String operationCommander=KILL-9 securityLevel=3,java.lang.Integer,false'
