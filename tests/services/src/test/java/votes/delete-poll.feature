@ignore
Feature: Delete poll used for votes

  Background:
    * url baseUrl
    * karate.logger.info('Deleting subject for polls: ', subjectId)

  Scenario: Delete poll
    When path 'subjects', subjectId, 'polls', pollId
    * method delete
    Then status 204
