@ignore
Feature: Create subject used for polls

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def randString = IdUtils.randomString()
    * url baseUrl
    * karate.logger.info('Creating subject for polls: ', subjectId)

  Scenario: Create subject
    * def subject = { id: #(subjectId), name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method post
    Then status 201
