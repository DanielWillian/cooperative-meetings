#!/bin/sh
#================================================================
# HEADER
#================================================================
#+SYNOPSIS
#+    ${SCRIPT_NAME} -j <jar> -d <docker-compose> [-p <port>] [-a <name>]
#+                    (deploy|undeploy|deploy-test)
#+    ${SCRIPT_NAME} [-p <port>] (test|check)
#+    ${SCRIPT_NAME} (--help|-h|help|)
#+
#+DESCRIPTION
#+    Helper shell script for integration tests.
#+
#+OPTIONS
#+    -j, --jar <jar>
#+          Jar to be used for application.
#+    -d, --docker <docker-compose>
#+          Docker compose file to be used to deploy/undeploy application.
#+    -k, --k6 <file>
#+          File used for stress test.
#+    -p, --port <port>
#+          Port application will expose. Default is 8080.
#+    -a, --app <name>
#+          Name of the application. Default is cooperative.
#+    -h, --help, help
#+          Print this help.
#+
#+COMMANDS
#+    deploy      - Deploys application
#+    undeploy    - Undeploys application
#+    check       - Check if application is up
#+    test        - Check if app is up and run integration test
#+    stress      - Check if app is up and run stress test
#+    deploy-test - Deploys, run tests and undeploys application
#+    deploy-test-all - Deploys, run all tests and undeploys application
#+
#+EXAMPLES
#+    ${SCRIPT_NAME} -j app.jar -d docker-compose.yaml deploy-test
#+    ${SCRIPT_NAME} help
#+
#================================================================
# END_OF_HEADER
#================================================================

SCRIPT_HEADER_SIZE=$(head -200 "${0}" | grep -n "^#.*END_OF_HEADER" | cut -f1 -d:)
SCRIPT_NAME="$(basename "${0}")"
SCRIPT_DIR="$(dirname "${0}")"

show_help() {
  head -"${SCRIPT_HEADER_SIZE:-200}" "${0}" |\
      grep -e "^#[%+]" |\
      sed -e "s/^#[%+-]//g" -e "s/\${SCRIPT_NAME}/${SCRIPT_NAME}/g"
}

echo_error() {
  echo "$1" 1>&2
}

parse_args() {
  if [ $# -le 0 ]; then
    show_help
    exit 0
  fi
  while [ $# -gt 1 ]; do
    case "$1" in
      -j|--jar)
        JAR_FILE="$2"
        shift 2
        ;;
      -d|--docker)
        DOCKER_COMPOSE_FILE="$2"
        shift 2
        ;;
      -k|--k6)
        K6_SCRIPT="$2"
        shift 2
        ;;
      -p|--port)
        PORT="$2"
        shift 2
        ;;
      -a|--app)
        APP_NAME="$2"
        shift 2
        ;;
      -h|--help|help)
        show_help
        exit 0
        ;;
      --)
        shift 1
        break
        ;;
    esac
  done
  case "$1" in
    check) CHECK=true ;;
    deploy) DEPLOY=true ;;
    undeploy) UNDEPLOY=true ;;
    test)
      CHECK=true
      TEST=true
      ;;
    stress)
      CHECK=true
      STRESS=true
      ;;
    deploy-test)
      DEPLOY=true
      CHECK=true
      TEST=true
      UNDEPLOY=true
      ;;
    deploy-test-all)
      DEPLOY=true
      CHECK=true
      TEST=true
      STRESS=true
      UNDEPLOY=true
      ;;
    *)
      echo_error "Unknown option: $1!"
      show_help
      exit 1
      ;;
  esac
}

set_defaults() {
  PORT="${PORT:-8080}"
  APP_NAME="${APP_NAME:-cooperative}"
}

validate_args() {
  if [ -n "${DEPLOY}" ] || [ -n "${UNDEPLOY}" ]; then
    if [ -z "${JAR_FILE}" ]; then
      echo_error "Required option --jar missing!"
      INVALID=true
    elif ! [ -f "${JAR_FILE}" ]; then
      echo_error "--jar: ${JAR_FILE} does not exist!"
      INVALID=true
    fi
    if [ -z "${DOCKER_COMPOSE_FILE}" ]; then
      echo_error "Required option --docker missing!"
      INVALID=true
    elif ! [ -f "${DOCKER_COMPOSE_FILE}" ]; then
      echo_error "--docker: ${DOCKER_COMPOSE_FILE} does not exist!"
      INVALID=true
    fi
    if [ -z "${APP_NAME}" ]; then
      echo_error "--app: name can not be empty!"
      INVALID=true
    fi
  fi
  if [ "${PORT}" -ne "${PORT}" ]; then
    echo_error "--port: ${PORT} is not a number!"
    INVALID=true
  fi
  if [ -n "${INVALID}" ]; then
    exit 1
  fi
}

export_args() {
  if [ -n "${JAR_FILE}" ]; then
    APP_JAR_HOME="$(dirname "${JAR_FILE}")"
    export APP_JAR_HOME
    APP_JAR_NAME="$(basename "${JAR_FILE}")"
    export APP_JAR_NAME
  fi
  export PORT
  BASE_URL="http://localhost:${PORT}/"
  export BASE_URL
}

deploy_app() {
  docker-compose --project-name "${APP_NAME}" \
      --file "${DOCKER_COMPOSE_FILE}" up --detach --force-recreate
}

check_app_up() {
  echo "Checking if app is up: "
  RETRY_COUNT=10
  i=0
  while [ "$i" -lt "${RETRY_COUNT}" ]; do
    if curl --silent --fail "localhost:${PORT}/actuator/health"; then
      echo
      echo "App is up!"
      break
    else
      echo "App is down! Sleeping for 5 seconds."
      i=$((i + 1))
      sleep 5
    fi
  done
  if [ "$i" -eq "${RETRY_COUNT}" ]; then
    echo "App did not get ready, shutting down!"
    return 1
  fi
}

test_app() {
  mvn -f "${SCRIPT_DIR}/pom.xml" -P tests clean test "-Dkarate.options=--tags ~@ignore"
}

stress_test() {
  docker run -i grafana/k6 run - < "${K6_SCRIPT}"
}

undeploy_app() {
  docker-compose --project-name "${APP_NAME}" \
      --file "${DOCKER_COMPOSE_FILE}" down
}

main() {
  parse_args "$@"
  set_defaults
  validate_args
  export_args

  if [ -n "${DEPLOY}" ]; then
    deploy_app
    EXIT_CODE=$?
    [ "${EXIT_CODE}" -ne 0 ] && exit "${EXIT_CODE}"
  fi
  if [ -n "${CHECK}" ]; then
    check_app_up
    CHECK_EXIT_CODE=$?
    [ -z "${DEPLOY}" ] && [ "${CHECK_EXIT_CODE}" -ne 0 ] && exit "${CHECK_EXIT_CODE}"
  fi
  if [ -n "${TEST}" ] && [ "${CHECK_EXIT_CODE}" -eq 0 ]; then
    test_app
    TEST_EXIT_CODE=$?
  fi
  if [ -n "${STRESS}" ] && [ "${CHECK_EXIT_CODE}" -eq 0 ]; then
    stress_test
    STRESS_EXIT_CODE=$?
  fi
  if [ -n "${UNDEPLOY}" ]; then
    undeploy_app
    EXIT_CODE=$?
    [ "${EXIT_CODE}" -ne 0 ] && exit "${EXIT_CODE}"
  fi

  [ -n "${CHECK_EXIT_CODE}" ] && [ "${CHECK_EXIT_CODE}" -ne 0 ] && exit "${CHECK_EXIT_CODE}"
  [ -n "${TEST_EXIT_CODE}" ] && [ "${TEST_EXIT_CODE}" -ne 0 ] && exit "${TEST_EXIT_CODE}"
  [ -n "${STRESS_EXIT_CODE}" ] && [ "${STRESS_EXIT_CODE}" -ne 0 ] && exit "${STRESS_EXIT_CODE}"

  exit 0
}

main "$@"


