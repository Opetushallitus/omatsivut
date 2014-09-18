var gulp = require('gulp'),
    browserify = require('gulp-browserify'),
    concat = require('gulp-concat'),
    less = require('gulp-less'),
    jshint = require('gulp-jshint'),
    livereload = require('gulp-livereload'),
    templates = require('gulp-angular-templatecache');

var jsFiles = 'src/main/js/**/*.js';
var isWatch

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
  gulp.src("src/main/templates/**/*.html")
    .pipe(templates("templates.js", { root:"templates/"}))
    .pipe(gulp.dest("src/main/templates"))
})

gulp.task('less', function () {
    gulp.src('src/main/less/**/main.less')
        .pipe(less().on('error', handleError))
        .pipe(concat('main.css'))
        .pipe(gulp.dest('src/main/webapp/css'));
    gulp.src('src/main/less/**/preview.less')
      .pipe(less().on('error', handleError))
      .pipe(concat('preview.css'))
      .pipe(gulp.dest('src/main/webapp/css'));
});

gulp.task('browserify', function() {
    gulp.src(['src/main/js/app.js'])
        .pipe(browserify({
            insertGlobals: true,
            debug: true
        }).on('error', handleError))
        .pipe(concat('bundle.js'))
        .pipe(gulp.dest('src/main/webapp'));
});

gulp.task('watch', function() {
    isWatch = true
    livereload.listen();
    gulp.watch(['src/main/webapp/**/*.js', 'src/main/webapp/**/*.css', 'src/main/webapp/**/*.html'], livereload.changed);
    gulp.watch(['src/main/templates/**/*.html'], ['compile'])
    gulp.watch([jsFiles],['lint', 'browserify']);
    gulp.watch(['src/main/less/**/*.less'],['less']);
});

gulp.task('compile', ['templates', 'browserify', 'less']);
gulp.task('dev', ['lint', 'compile', 'watch']);
gulp.task('default', ['dev']);