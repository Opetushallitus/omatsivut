// Gulp
let gulp = require('gulp'),
  flatten = require('gulp-flatten'),
  templates = require('gulp-angular-templatecache'),
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
  babelify = require('babelify');

function handleError(err) {
  console.error(err);
  this.emit('end');
  //if (!watch) throw err;
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
    .transform(ngAnnotate);

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

gulp.task('templates', function() {
  return gulp.src(['src/main/templates/**/*.html', 'src/main/components/**/*.html'])
    .pipe(flatten())
    .pipe(templates('templates.js'))
    .pipe(gulp.dest('src/main/templates'))
});

gulp.task('build', ['less', 'templates'], function () {
  return compile();
});

gulp.task('watch', ['less', 'templates'], function () {
  return watch();
});

gulp.task('default', ['build']);
