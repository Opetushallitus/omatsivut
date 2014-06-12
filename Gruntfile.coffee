module.exports = (grunt) ->
  require('jit-grunt')(grunt)
  grunt.loadNpmTasks('grunt-run')
  grunt.loadNpmTasks('grunt-contrib-connect')
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    clean: ['build'],
    watch: {
      options: {
        atBegin: true
      }
      scripts: {
        files: [
          'Gruntfile.coffee',
          'package.json',
        ],
      }
    },
    mocha_phantomjs: {
      options: {
        'reporter': 'xunit',
        'output': 'target/mocha-phantomjs/test-reports/result.xml'
      },
      all: {
        options: {
          urls: [
            'http://localhost:8080/test/runner.html'
          ]
        }
      }
    }
  })
  grunt.registerTask('test', ['mocha_phantomjs'])

  grunt.registerTask('default', ['test']);
