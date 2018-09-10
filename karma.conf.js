// Karma configuration
// Generated on Wed Aug 22 2018 12:55:45 GMT+0300 (EEST)

module.exports = function(config) {
  config.set({

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '',


    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['mocha'],


    // list of files / patterns to load in the browser
    files: [
      //'src/main/webapp/test/page/*.js',
      //'src/main/webapp/test/*.js',
      //'src/main/webapp/test/spec/*.js'


    'src/main/webapp/test/lib/mocha.js',
    'src/main/webapp/test/lib/chai.js',
    'src/main/webapp/test/lib/jquery-3.3.1.min.js',
    'src/main/webapp/test/lib/chai-jquery.js',
    'src/main/webapp/test/lib/underscore-min.js',
    'src/main/webapp/test/lib/q.js',
      //code.jquery.com/jquery-1.11.1.min.js',
    'src/main/webapp/test/util/testHelpers.js',
    'src/main/webapp/test/page/applicationApi.js',
    'src/main/webapp/test/page/applicationListPage.js',
    'src/main/webapp/test/spec/applicationListSpec.js'
    ],

    // mocha plugin config
    client: {
      mocha: {
        // change Karma's debug.html to the mocha web reporter
        reporter: 'html',

        // require specific files after Mocha is initialized
        //require: [require.resolve('bdd/bdd')],

        // custom ui, defined in required file above
        ui: 'bdd',

        timeout: 60000
      }
    },

    // list of files / patterns to exclude
    exclude: [
    ],


    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
    },


    // test results reporter to use
    // possible values: 'dots', 'progress'
    // available reporters: https://npmjs.org/browse/keyword/karma-reporter
    reporters: ['progress'],


    // web server port
    port: 9876,


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,


    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['ChromeHeadless'],


    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: false,

    // Concurrency level
    // how many browser should be started simultaneous
    concurrency: Infinity
  })
}
