// Gulp
let gulp = require('gulp'),
  concat = require('gulp-concat'),
  less = require('gulp-less'),
  uglify = require('gulp-uglify'),
  sourcemaps = require('gulp-sourcemaps'),
  source = require('vinyl-source-stream'),
  buffer = require('vinyl-buffer'),

  // Browserify
  browserify = require('browserify'),
  watchify = require('watchify'),
  ngAnnotate = require('browserify-ngannotate'),
  ngify = require('ngify'),
  babelify = require('babelify');

function handleError(err) {
  console.log(err.toString());
  this.emit('end');
}

function compile(watch) {
  let bundler = browserify('./src/main/js/app.js',
    {
      insertGlobals: true,
      debug: true,
      cache: {},
      packageCache: {},
      plugin: [watchify]
    })
    .transform(babelify,
    {
      presets: ["env"],
      sourceMaps: true
    })
    .transform(ngAnnotate)
    .transform(ngify);

  function rebundle() {
    bundler.bundle()
      .on('error', handleError)
      .pipe(source('bundle.js'))
      .pipe(buffer())
      .pipe(sourcemaps.init({loadMaps: true}))
      .pipe(uglify({compress: true}))
      .pipe(sourcemaps.write('./'))
      .pipe(gulp.dest('src/main/webapp'))
  }

  if (watch) {
    bundler.on('update', function () {
      console.log('-> bundling...');
      rebundle();
    });
  }

  rebundle();
}

function watch() {
  return compile(true);
}

gulp.task('build', ['less'], function () {
  return compile();
});

gulp.task('watch', ['less'], function () {
  return watch();
});

gulp.task('less', function () {
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
});

gulp.task('default', ['watch']);
