@ignore
Feature: Delete subject used for polls

  Background:
    * url baseUrl
    * karate.logger.info('Deleting subject for polls: ', subjectId)

  Scenario: Delete subject
    When path 'subjects', subjectId
    * method delete
    Then status 204
