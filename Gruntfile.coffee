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
      all: {
        options: {
          urls: [
            'http://localhost:8888/runner.html'
          ]
        }
      }
    },
    connect: {
      server: {
        options: {
          port: 8888,
          base: 'src/test/resources/html'
        }
      }
    }
    run: {
      omatsivut_backend: {
        cmd: './start.sh'
      }
    }
  })
  grunt.registerTask('test', ['run', 'connect', 'mocha_phantomjs'])
