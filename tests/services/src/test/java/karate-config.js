function fn() {
  var baseUrl = java.lang.System.getenv('BASE_URL')
  if (!baseUrl) {
    baseUrl = 'http://localhost:8080/'
  }
  karate.log('baseUrl is: ', baseUrl);
  var config = {}
  config.baseUrl = baseUrl

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 5000);
  return config;
}