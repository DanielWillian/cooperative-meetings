@ignore
Feature: Create vote

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def randString = IdUtils.randomString()
    * url baseUrl
    * karate.logger.info('Creating subject for polls: ', subjectId)

  Scenario: Create vote
    * def randUuid = IdUtils.randomUuid()
    * def vote = { voter: '#(randUuid)', agree: true }
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * request vote
    * method post
    Then status 201
