Feature: Tests for polls APIs

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def subjectId = IdUtils.randomLong()
    * def randLong = IdUtils.randomLong()
    * def randString = IdUtils.randomString()
    * def DateTimeUtils = Java.type('DateTimeUtils')
    * def endDate = DateTimeUtils.getFutureOffsetDateTimeString()
    * url baseUrl

  Scenario: Create, retrieve and delete a poll
    * call read('create-subject.feature')
    * def poll = { name: '#("poll " + randString)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 201
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == '#present'
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path location
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == '#present'
    When path location
    * method delete
    Then status 204
    * call read('delete-subject.feature')

  Scenario: Create with end date, retrieve and delete a poll
    * call read('create-subject.feature')
    * def poll = { name: '#("poll " + randString)', endDate: '#(endDate)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 201
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == '#present'
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path location
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == '#present'
    When path location
    * method delete
    Then status 204
    * call read('delete-subject.feature')

  Scenario: Create invalid end date
    * call read('create-subject.feature')
    * def endDate = DateTimeUtils.getPastOffsetDateTimeString()
    * def poll = { name: '#("poll " + randString)', endDate: '#(endDate)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 400
    * call read('delete-subject.feature')

  Scenario: Create, update, retrieve and delete a poll
    * call read('create-subject.feature')
    * def poll = { name: '#("poll " + randString)', endDate: '#(endDate)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method post
    Then status 201
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    * def id = response.id
    When path 'subjects', subjectId, 'polls'
    * def poll = { id: '#(id)', name: '#("poll updated " + randString)', endDate: '#(endDate)' }
    * request poll
    * method put
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == id
    When path location
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $.name == poll.name
    * match response $.id == id
    When path location
    * method delete
    Then status 204
    * call read('delete-subject.feature')

  Scenario: Update non existent poll
    * call read('create-subject.feature')
    * def poll = { id: '#(randLong)', name: '#("poll " + randString)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method put
    Then status 404
    * call read('delete-subject.feature')

  Scenario: Update non existent poll on non existent subject
    * def poll = { id: '#(randLong)', name: '#("poll " + randString)' }
    When path 'subjects', subjectId, 'polls'
    * request poll
    * method put
    Then status 404

  Scenario: Delete non existent poll
    * call read('create-subject.feature')
    When path 'subjects', subjectId, 'polls', randLong
    * method delete
    Then status 404
    * call read('delete-subject.feature')

  Scenario: Get all polls of subject
    * call read('create-subject.feature')
    When path 'subjects', subjectId, 'polls'
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $ == '#array'
    * call read('delete-subject.feature')

