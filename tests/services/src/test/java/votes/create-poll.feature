@ignore
Feature: Create poll used for votes

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def randString = IdUtils.randomString()
    * url baseUrl
    * karate.logger.info('Creating subject for polls: ', subjectId)

  Scenario: Create poll
    * def poll = { name: '#("poll " + randString)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 201
    * def pollId = response.id
