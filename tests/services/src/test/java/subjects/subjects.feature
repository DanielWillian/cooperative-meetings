Feature: Tests for subjects APIs

  Background:
    * def IdUtils = Java.type('IdUtils')
    * def randLong = IdUtils.randomLong()
    * def randString = IdUtils.randomString()
    * url baseUrl

  Scenario: Create, retrieve and delete a subject
    * def subject = { id: #(randLong), name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method post
    Then status 201
    * match header Content-Type == 'application/json'
    * match response $ == subject
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path location
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $ == subject
    When path location
    * method delete
    Then status 204

  Scenario: Create subject negative id
    * def subject = { id: -1, name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method post
    Then status 400
    * match header Content-Type == 'application/json'
    * match response $.error == '#present'

  Scenario: Create subject that already exists
    * def subject = { id: #(randLong), name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method post
    Then status 201
    * match header Content-Type == 'application/json'
    * match response $ == subject
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path 'subjects'
    * request subject
    * method post
    Then status 409
    When path location
    * method delete
    Then status 204

  Scenario: Create, update and delete a subject
    * def subject = { id: #(randLong), name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method post
    Then status 201
    * def location = responseHeaders['Location'][0]
    * match location == '#notnull'
    When path 'subjects'
    * def subject = { id: #(randLong), name: '#("subject updated " + randString)'}
    * request subject
    * method put
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $ == subject
    When path location
    * method delete
    Then status 204

  Scenario: Update non existent subject
    * def subject = { id: #(randLong), name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method put
    Then status 404

  Scenario: Update invalid subject
    * def subject = { id: 'id', name: '#("subject " + randString)'}
    When path 'subjects'
    * request subject
    * method put
    Then status 400

  Scenario: Delete non existent subject
    When path 'subjects', randLong
    * method delete
    Then status 404

  Scenario: Delete invalid subject
    When path 'subjects/99999999999999999999'
    * method delete
    Then status 400

  Scenario: Get all subjects
    When path 'subjects'
    * method get
    Then status 200
    * match header Content-Type == 'application/json'
    * match response $ == '#array'

