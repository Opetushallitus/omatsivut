var gulp = require('gulp'),
    browserify = require('gulp-browserify'),
    concat = require('gulp-concat'),
    less = require('gulp-less'),
    jshint = require('gulp-jshint'),
    livereload = require('gulp-livereload'),
    templates = require('gulp-angular-templatecache'),
    uglify = require('gulp-uglify'),
    gulpif = require('gulp-if'),
    ngAnnotate = require('gulp-ng-annotate')

var jsFiles = 'src/main/js/**/*.js';
var isWatch
var localEditor = false
var editorLocation = "node_modules/hakemuseditori"

function handleError(err) {
    console.log(err.toString());
    this.emit('end');
    if (!isWatch) {
      throw err
    }
}

gulp.task('lint', function() {
    gulp.src(jsFiles)
        .pipe(jshint({
            globals: {
                require: false,
                angular: false
            }
        }))
        .pipe(jshint.reporter('default'));
});

gulp.task("templates", function() {
  return gulp.src("src/main/templates/**/*.html")
    .pipe(templates("templates.js", { root:"templates/"}))
    .pipe(gulp.dest("src/main/templates"))
})

gulp.task('copy-editor', function() {
    gulp.src(editorLocation + "/src/main/img/**")
      .pipe(gulp.dest('src/main/webapp/img/hakemuseditori'))

    if(!localEditor) {
      gulp.src(editorLocation + "/dist/*.js")
        .pipe(gulp.dest('src/main/webapp'))
    }

    return gulp.src(editorLocation + "/src/main/less/**")
      .pipe(gulp.dest('src/main/less/hakemuseditori'))
})

gulp.task('less', ['copy-editor'], function () {
    gulp.src('src/main/less/main.less')
        .pipe(less().on('error', handleError))
        .pipe(concat('main.css'))
        .pipe(gulp.dest('src/main/webapp/css'));

    gulp.src('src/main/less/hakutoiveidenMuokkaus.less')
        .pipe(less().on('error', handleError))
        .pipe(concat('hakutoiveidenMuokkaus.css'))
        .pipe(gulp.dest('src/main/webapp/css'));

    gulp.src('src/main/less/preview.less')
      .pipe(less().on('error', handleError))
      .pipe(concat('preview.css'))
      .pipe(gulp.dest('src/main/webapp/css'));

    gulp.src('src/main/less/hakutoiveidenMuokkaus.less')
      .pipe(less().on('error', handleError))
      .pipe(concat('hakutoiveidenMuokkaus.css'))
      .pipe(gulp.dest('src/main/webapp/css'));
});

gulp.task('browserify', ["templates"], function() {
  compileJs(false)
})

gulp.task('browserify-min', ["templates"], function() {
  compileJs(true)
})

function compileJs(compress) {
  gulp.src(['src/main/js/app.js'])
    .pipe(browserify({
      external: ["./hakemuseditori"],
      insertGlobals: true,
      debug: true
    }).on('error', handleError))
    .pipe(concat('bundle.js'))
    .pipe(gulpif(compress, ngAnnotate()))
    .pipe(gulpif(compress, uglify({ mangle: true })))
    .pipe(gulp.dest('src/main/webapp'))
}

gulp.task('watch', function() {
    isWatch = true
    livereload.listen();
    gulp.watch(['src/main/webapp/**/*.js', 'src/main/webapp/**/*.css', 'src/main/webapp/**/*.html'], livereload.changed);
    gulp.watch(['src/main/templates/**/*.html'], ['compile-dev'])
    gulp.watch([jsFiles, 'src/main/webapp/hakemuseditori.js', 'src/main/webapp/hakemuseditori-templates.js'],['lint', 'browserify']);
    gulp.watch(['src/main/less/**/*.less'],['less']);
});

gulp.task('local-editor', function() {
    localEditor = true
    editorLocation = "../hakemuseditori"
});

gulp.task('dev-local-editor', ['local-editor', 'dev']);

gulp.task('compile', ['copy-editor', 'templates', 'browserify-min', 'less']);
gulp.task('compile-dev', ['copy-editor', 'templates', 'browserify', 'less']);
gulp.task('dev', ['lint', 'compile-dev', 'watch']);
gulp.task('default', ['compile']);
