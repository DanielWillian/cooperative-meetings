Feature: Tests for votes APIs

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def subjectId = IdUtils.randomLong()
    * def randUuid = IdUtils.randomUuid()
    * def DateTimeUtils = Java.type('DateTimeUtils')
    * def endDate = DateTimeUtils.getFutureOffsetDateTimeString()
    * def wait =
    """
    function(pause) { java.lang.Thread.sleep(pause) }
    """
    * url baseUrl

  Scenario: Create, retrieve a vote
    * call read('../polls/create-subject.feature')
    * call read('create-poll.feature')
    * def vote = { voter: '#(randUuid)', agree: true }
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * request vote
    * method post
    Then status 201
    * match header Content-Type == 'application/json'
    * match response $.voter == vote.voter
    * match response $.agree == true
    * match response $.voteDate == '#present'
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path location
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $.voter == vote.voter
    * match response $.agree == true
    * match response $.voteDate == '#present'

  Scenario: Try a vote after voting ended
    * call read('../polls/create-subject.feature')
    * def endDate = DateTimeUtils.getRecentFutureOffsetDateTimeString()
    * def poll = { name: '#("poll " + randString)', endDate: '#(endDate)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 201
    * def pollId = response.id
    * call wait 1000
    * def vote = { voter: '#(randUuid)', agree: true }
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * request vote
    * method post
    Then status 422

  Scenario: Try to add two votes for someone
    * call read('../polls/create-subject.feature')
    * call read('create-poll.feature')
    * def vote = { voter: '#(randUuid)', agree: true }
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * request vote
    * method post
    Then status 201
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * request vote
    * method post
    Then status 409

  Scenario: Get all votes of poll
    * call read('../polls/create-subject.feature')
    * call read('create-poll.feature')
    When path 'subjects', subjectId, 'polls', pollId, 'votes'
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $ == '#array'

