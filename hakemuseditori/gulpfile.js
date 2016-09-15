var gulp = require('gulp'),
    browserify = require('gulp-browserify'),
    concat = require('gulp-concat'),
    less = require('gulp-less'),
    jshint = require('gulp-jshint'),
    templates = require('gulp-angular-templatecache'),
    uglify = require('gulp-uglify'),
    gulpif = require('gulp-if'),
    ngAnnotate = require('gulp-ng-annotate')
    _ = require("underscore")

var editorJsFiles = 'src/main/js/**/*.js';
var isWatch = false;
distTargets = ["dist"]

function isDev() {
  return distTargets.indexOf("dist") < 0
}

function handleError(err) {
    console.log(err.toString());
    this.emit('end');
    if (!isWatch) {
      throw err
    }
}

gulp.task('lint', function() {
    gulp.src(editorJsFiles)
        .pipe(jshint({
            globals: {
                require: false,
                angular: false
            }
        }))
        .pipe(jshint.reporter('default'));
});

gulp.task("templates", function() {
  pipeToTargets(gulp.src("src/main/templates/**/*.html")
    .pipe(templates("hakemuseditori-templates.js", { root:"templates/"}))
  )    
})

gulp.task('less-example', function () {
  gulp.src('example-application/less/main.less')
    .pipe(less().on('error', handleError))
    .pipe(concat('example-application.css'))
    .pipe(gulp.dest('example-application'));
});

gulp.task('browserify-editor', ["templates"], function() {
  var compress = !isDev()
  pipeToTargets(gulp.src(['src/main/js/hakemuseditori.js'])
    .pipe(browserify({
      insertGlobals: true,
      debug: true,
      require: "./hakemuseditori"
    }).on('error', handleError))
    .pipe(concat('hakemuseditori.js'))
    .pipe(gulpif(compress, ngAnnotate()))
    .pipe(gulpif(compress, uglify({ mangle: true })))
  )
  
})

gulp.task('browserify-example', function() {
  gulp.src(['example-application/js/example-application.js'])
    .pipe(browserify({
      insertGlobals: true,
      debug: true,
      external: ["./hakemuseditori"]
    }).on('error', handleError))
    .pipe(concat('bundle.js'))
    .pipe(gulp.dest('example-application'))
})

function pipeToTargets(pipeline) {
  distTargets.forEach(function(target) {
    pipeline.pipe(gulp.dest(target))
  })
}

gulp.task('omatsivut', function() {
  distTargets.push("../omatsivut/src/main/webapp")
})

gulp.task('haku-app', function() {
  distTargets.push("../haku/haku-app/src/main/webapp/hakemuseditori")
})

gulp.task('set-dev', function() {
  distTargets = _.without(distTargets, "dist")
  distTargets.push("dev")
  distTargets = _.uniq(distTargets)
})

gulp.task('watch', function() {
    isWatch = true
    gulp.watch(['src/main/templates/**/*.html'], ['compile-dev'])
    gulp.watch([editorJsFiles],['lint', 'browserify-editor']);
    gulp.watch(['example-application/less/**/*.less'],['less-example']);
    gulp.watch(['example-application/js/**/*.js'],['browserify-example']);
});

gulp.task('compile', ['templates', 'browserify-editor']);
gulp.task('compile-example', ['compile-dev', 'less-example', 'browserify-example'])
gulp.task('compile-dev', ['set-dev', 'compile']);
gulp.task('dev', ['lint', 'compile-example', 'watch']);
gulp.task('default', ['compile']);
