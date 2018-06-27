import Question from '../models/question';

export default ["restResources", function(restResources) {
  return function applicationValidator() {
    var currentRequest

    function onlyIfCurrentRequest(current, f) {
      return function() {
        if (currentRequest === current)
          f.apply(this, arguments)
      }
    }

    return function(application, beforeBackendValidation, success, error) {
      currentRequest = {}
      success = onlyIfCurrentRequest(currentRequest, success)
      error = onlyIfCurrentRequest(currentRequest, error)

      var preferencesValid = application.validatePreferences()
      if (preferencesValid) {
        beforeBackendValidation()
        validateBackend(application, success, error)
      } else {
        error({
          errors: []
        })
      }
    }
  }

  function validateBackend(application, success, error) {
    var responsePromise = restResources.validate(application)
    responsePromise.success(function(data, status, headers, config) {
      if (data.errors.length === 0) {
        success({
          questions: Question.getQuestions(data.questions, application),
          response: data
        })
      } else {
        error({
          statusCode: 200,
          errors: data.errors,
          questions: Question.getQuestions(data.questions, application),
          response: data
        })
      }
    })

    responsePromise.error(function(data, status) {
      error({
        errors: [],
        statusCode: status,
        isSaveable: true,
        response: data
      })
    })
  }
}]
