SHELL := /bin/sh

MAKEFLAGS += --no-builtin-rules

PROJECT_DIR := $(dir $(realpath $(lastword $(MAKEFILE_LIST))))
MAVEN_VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

WEB_APP_JAR := $(PROJECT_DIR)app/entrypoints/web-app/target/web-app-$(MAVEN_VERSION).jar
TEST_DOCKER_COMPOSE := $(PROJECT_DIR)distros/integration-test/src/main/resources/docker-compose.yaml
TEST_UTILS_SCRIPT := $(PROJECT_DIR)test-utils.sh
SRC_FILES := $(shell git ls-files app | grep -v test)
SUPPORTING_FILES := $(PROJECT_DIR)pom.xml $(PROJECT_DIR)makefile $(TEST_UTILS_SCRIPT)
K6_SCRIPT := $(PROJECT_DIR)tests/load-balance/vote-api.js

.SUFFIXES:
.PHONY: app-jar build test deploy undeploy deploy-test stress stress-linux deploy-test-all deploy-test-all-linux check integration-test
.DELETE_ON_ERROR:

$(WEB_APP_JAR): $(SRC_FILES) $(SUPPORTING_FILES)
	mvn clean install -DskipTests --also-make --projects :web-app

app-jar: $(WEB_APP_JAR)

build:
	mvn clean install

test:
	mvn clean test

deploy undeploy deploy-test stress deploy-test-all: $(WEB_APP_JAR)
	$(TEST_UTILS_SCRIPT) -j $(WEB_APP_JAR) -d $(TEST_DOCKER_COMPOSE) -k $(K6_SCRIPT) $@

deploy-test-all-linux: $(WEB_APP_JAR)
	$(TEST_UTILS_SCRIPT) -j $(WEB_APP_JAR) -d $(TEST_DOCKER_COMPOSE) -k $(K6_SCRIPT) -l deploy-test-all

stress-linux:
	$(TEST_UTILS_SCRIPT) -k $(K6_SCRIPT) -l stress

check:
	$(TEST_UTILS_SCRIPT) $@

integration-test:
	$(TEST_UTILS_SCRIPT) test

