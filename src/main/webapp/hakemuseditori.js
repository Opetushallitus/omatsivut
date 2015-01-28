require=(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);throw new Error("Cannot find module '"+o+"'")}var f=n[o]={exports:{}};t[o][0].call(f.exports,function(e){var n=t[o][1][e];return s(n?n:e)},f,f.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
(function() {
  var Bacon, BufferingSource, Bus, CompositeUnsubscribe, ConsumingSource, Desc, Dispatcher, End, Error, Event, EventStream, Exception, Initial, Next, None, Observable, Property, PropertyDispatcher, Some, Source, UpdateBarrier, addPropertyInitValueToStream, assert, assertArray, assertEventStream, assertFunction, assertNoArguments, assertObservable, assertString, cloneArray, compositeUnsubscribe, constantToFunction, containsDuplicateDeps, convertArgsToFunction, describe, end, eventIdCounter, findDeps, flatMap_, former, idCounter, initial, isArray, isFieldKey, isFunction, isObservable, latter, liftCallback, makeFunction, makeFunctionArgs, makeFunction_, makeObservable, makeSpawner, next, nop, partiallyApplied, recursionDepth, registerObs, spys, toCombinator, toEvent, toFieldExtractor, toFieldKey, toOption, toSimpleExtractor, withDescription, withMethodCallSupport, _, _ref,
    __hasProp = {}.hasOwnProperty,
    __slice = [].slice,
    __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; },
    __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };

  Bacon = {
    toString: function() {
      return "Bacon";
    }
  };

  Bacon.version = '0.7.42';

  Exception = (typeof global !== "undefined" && global !== null ? global : this).Error;

  _ = {
    indexOf: Array.prototype.indexOf ? function(xs, x) {
      return xs.indexOf(x);
    } : function(xs, x) {
      var i, y, _i, _len;
      for (i = _i = 0, _len = xs.length; _i < _len; i = ++_i) {
        y = xs[i];
        if (x === y) {
          return i;
        }
      }
      return -1;
    },
    indexWhere: function(xs, f) {
      var i, y, _i, _len;
      for (i = _i = 0, _len = xs.length; _i < _len; i = ++_i) {
        y = xs[i];
        if (f(y)) {
          return i;
        }
      }
      return -1;
    },
    head: function(xs) {
      return xs[0];
    },
    always: function(x) {
      return function() {
        return x;
      };
    },
    negate: function(f) {
      return function(x) {
        return !f(x);
      };
    },
    empty: function(xs) {
      return xs.length === 0;
    },
    tail: function(xs) {
      return xs.slice(1, xs.length);
    },
    filter: function(f, xs) {
      var filtered, x, _i, _len;
      filtered = [];
      for (_i = 0, _len = xs.length; _i < _len; _i++) {
        x = xs[_i];
        if (f(x)) {
          filtered.push(x);
        }
      }
      return filtered;
    },
    map: function(f, xs) {
      var x, _i, _len, _results;
      _results = [];
      for (_i = 0, _len = xs.length; _i < _len; _i++) {
        x = xs[_i];
        _results.push(f(x));
      }
      return _results;
    },
    each: function(xs, f) {
      var key, value;
      for (key in xs) {
        value = xs[key];
        f(key, value);
      }
      return void 0;
    },
    toArray: function(xs) {
      if (isArray(xs)) {
        return xs;
      } else {
        return [xs];
      }
    },
    contains: function(xs, x) {
      return _.indexOf(xs, x) !== -1;
    },
    id: function(x) {
      return x;
    },
    last: function(xs) {
      return xs[xs.length - 1];
    },
    all: function(xs, f) {
      var x, _i, _len;
      if (f == null) {
        f = _.id;
      }
      for (_i = 0, _len = xs.length; _i < _len; _i++) {
        x = xs[_i];
        if (!f(x)) {
          return false;
        }
      }
      return true;
    },
    any: function(xs, f) {
      var x, _i, _len;
      if (f == null) {
        f = _.id;
      }
      for (_i = 0, _len = xs.length; _i < _len; _i++) {
        x = xs[_i];
        if (f(x)) {
          return true;
        }
      }
      return false;
    },
    without: function(x, xs) {
      return _.filter((function(y) {
        return y !== x;
      }), xs);
    },
    remove: function(x, xs) {
      var i;
      i = _.indexOf(xs, x);
      if (i >= 0) {
        return xs.splice(i, 1);
      }
    },
    fold: function(xs, seed, f) {
      var x, _i, _len;
      for (_i = 0, _len = xs.length; _i < _len; _i++) {
        x = xs[_i];
        seed = f(seed, x);
      }
      return seed;
    },
    flatMap: function(f, xs) {
      return _.fold(xs, [], (function(ys, x) {
        return ys.concat(f(x));
      }));
    },
    cached: function(f) {
      var value;
      value = None;
      return function() {
        if (value === None) {
          value = f();
          f = void 0;
        }
        return value;
      };
    },
    toString: function(obj) {
      var ex, internals, key, value;
      try {
        recursionDepth++;
        if (obj == null) {
          return "undefined";
        } else if (isFunction(obj)) {
          return "function";
        } else if (isArray(obj)) {
          if (recursionDepth > 5) {
            return "[..]";
          }
          return "[" + _.map(_.toString, obj).toString() + "]";
        } else if (((obj != null ? obj.toString : void 0) != null) && obj.toString !== Object.prototype.toString) {
          return obj.toString();
        } else if (typeof obj === "object") {
          if (recursionDepth > 5) {
            return "{..}";
          }
          internals = (function() {
            var _results;
            _results = [];
            for (key in obj) {
              if (!__hasProp.call(obj, key)) continue;
              value = (function() {
                try {
                  return obj[key];
                } catch (_error) {
                  ex = _error;
                  return ex;
                }
              })();
              _results.push(_.toString(key) + ":" + _.toString(value));
            }
            return _results;
          })();
          return "{" + internals + "}";
        } else {
          return obj;
        }
      } finally {
        recursionDepth--;
      }
    }
  };

  recursionDepth = 0;

  Bacon._ = _;

  Bacon.scheduler = {
    setTimeout: function(f, d) {
      return setTimeout(f, d);
    },
    setInterval: function(f, i) {
      return setInterval(f, i);
    },
    clearInterval: function(id) {
      return clearInterval(id);
    },
    now: function() {
      return new Date().getTime();
    }
  };

  Bacon.fromBinder = function(binder, eventTransformer) {
    if (eventTransformer == null) {
      eventTransformer = _.id;
    }
    return new EventStream(describe(Bacon, "fromBinder", binder, eventTransformer), function(sink) {
      var needsUnbind, unbind, unbinder, unbound;
      unbound = false;
      needsUnbind = false;
      unbind = function() {
        if (!unbound) {
          if (typeof unbinder !== "undefined" && unbinder !== null) {
            unbinder();
            return unbound = true;
          } else {
            return needsUnbind = true;
          }
        }
      };
      unbinder = binder(function() {
        var args, event, reply, value, _i, _len;
        args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
        value = eventTransformer.apply(this, args);
        if (!(isArray(value) && _.last(value) instanceof Event)) {
          value = [value];
        }
        reply = Bacon.more;
        for (_i = 0, _len = value.length; _i < _len; _i++) {
          event = value[_i];
          reply = sink(event = toEvent(event));
          if (reply === Bacon.noMore || event.isEnd()) {
            unbind();
            return reply;
          }
        }
        return reply;
      });
      if (needsUnbind) {
        unbind();
      }
      return unbind;
    });
  };

  Bacon.$ = {};

  Bacon.$.asEventStream = function(eventName, selector, eventTransformer) {
    var _ref;
    if (isFunction(selector)) {
      _ref = [selector, void 0], eventTransformer = _ref[0], selector = _ref[1];
    }
    return withDescription(this.selector || this, "asEventStream", eventName, Bacon.fromBinder((function(_this) {
      return function(handler) {
        _this.on(eventName, selector, handler);
        return function() {
          return _this.off(eventName, selector, handler);
        };
      };
    })(this), eventTransformer));
  };

  if ((_ref = typeof jQuery !== "undefined" && jQuery !== null ? jQuery : typeof Zepto !== "undefined" && Zepto !== null ? Zepto : void 0) != null) {
    _ref.fn.asEventStream = Bacon.$.asEventStream;
  }

  Bacon.fromEventTarget = function(target, eventName, eventTransformer) {
    var sub, unsub, _ref1, _ref2, _ref3, _ref4, _ref5, _ref6;
    sub = (_ref1 = (_ref2 = (_ref3 = target.addEventListener) != null ? _ref3 : target.addListener) != null ? _ref2 : target.bind) != null ? _ref1 : target.on;
    unsub = (_ref4 = (_ref5 = (_ref6 = target.removeEventListener) != null ? _ref6 : target.removeListener) != null ? _ref5 : target.unbind) != null ? _ref4 : target.off;
    return withDescription(Bacon, "fromEventTarget", target, eventName, Bacon.fromBinder(function(handler) {
      sub.call(target, eventName, handler);
      return function() {
        return unsub.call(target, eventName, handler);
      };
    }, eventTransformer));
  };

  Bacon.fromPromise = function(promise, abort) {
    return withDescription(Bacon, "fromPromise", promise, Bacon.fromBinder(function(handler) {
      promise.then(handler, function(e) {
        return handler(new Error(e));
      });
      return function() {
        if (abort) {
          return typeof promise.abort === "function" ? promise.abort() : void 0;
        }
      };
    }, (function(value) {
      return [value, end()];
    })));
  };

  Bacon.noMore = ["<no-more>"];

  Bacon.more = ["<more>"];

  Bacon.later = function(delay, value) {
    return withDescription(Bacon, "later", delay, value, Bacon.fromPoll(delay, function() {
      return [value, end()];
    }));
  };

  Bacon.sequentially = function(delay, values) {
    var index;
    index = 0;
    return withDescription(Bacon, "sequentially", delay, values, Bacon.fromPoll(delay, function() {
      var value;
      value = values[index++];
      if (index < values.length) {
        return value;
      } else if (index === values.length) {
        return [value, end()];
      } else {
        return end();
      }
    }));
  };

  Bacon.repeatedly = function(delay, values) {
    var index;
    index = 0;
    return withDescription(Bacon, "repeatedly", delay, values, Bacon.fromPoll(delay, function() {
      return values[index++ % values.length];
    }));
  };

  Bacon.spy = function(spy) {
    return spys.push(spy);
  };

  spys = [];

  registerObs = function(obs) {
    var spy, _i, _len;
    if (spys.length) {
      if (!registerObs.running) {
        try {
          registerObs.running = true;
          for (_i = 0, _len = spys.length; _i < _len; _i++) {
            spy = spys[_i];
            spy(obs);
          }
        } finally {
          delete registerObs.running;
        }
      }
    }
    return void 0;
  };

  withMethodCallSupport = function(wrapped) {
    return function() {
      var args, context, f, methodName;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      if (typeof f === "object" && args.length) {
        context = f;
        methodName = args[0];
        f = function() {
          return context[methodName].apply(context, arguments);
        };
        args = args.slice(1);
      }
      return wrapped.apply(null, [f].concat(__slice.call(args)));
    };
  };

  liftCallback = function(desc, wrapped) {
    return withMethodCallSupport(function() {
      var args, f, stream;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      stream = partiallyApplied(wrapped, [
        function(values, callback) {
          return f.apply(null, __slice.call(values).concat([callback]));
        }
      ]);
      return withDescription.apply(null, [Bacon, desc, f].concat(__slice.call(args), [Bacon.combineAsArray(args).flatMap(stream)]));
    });
  };

  Bacon.fromCallback = liftCallback("fromCallback", function() {
    var args, f;
    f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    return Bacon.fromBinder(function(handler) {
      makeFunction(f, args)(handler);
      return nop;
    }, (function(value) {
      return [value, end()];
    }));
  });

  Bacon.fromNodeCallback = liftCallback("fromNodeCallback", function() {
    var args, f;
    f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    return Bacon.fromBinder(function(handler) {
      makeFunction(f, args)(handler);
      return nop;
    }, function(error, value) {
      if (error) {
        return [new Error(error), end()];
      }
      return [value, end()];
    });
  });

  Bacon.fromPoll = function(delay, poll) {
    return withDescription(Bacon, "fromPoll", delay, poll, Bacon.fromBinder((function(handler) {
      var id;
      id = Bacon.scheduler.setInterval(handler, delay);
      return function() {
        return Bacon.scheduler.clearInterval(id);
      };
    }), poll));
  };

  Bacon.interval = function(delay, value) {
    if (value == null) {
      value = {};
    }
    return withDescription(Bacon, "interval", delay, value, Bacon.fromPoll(delay, function() {
      return next(value);
    }));
  };

  Bacon.constant = function(value) {
    return new Property(describe(Bacon, "constant", value), function(sink) {
      sink(initial(value));
      sink(end());
      return nop;
    });
  };

  Bacon.never = function() {
    return new EventStream(describe(Bacon, "never"), function(sink) {
      sink(end());
      return nop;
    });
  };

  Bacon.once = function(value) {
    return new EventStream(describe(Bacon, "once", value), function(sink) {
      sink(toEvent(value));
      sink(end());
      return nop;
    });
  };

  Bacon.fromArray = function(values) {
    var i;
    assertArray(values);
    if (!values.length) {
      return withDescription(Bacon, "fromArray", values, Bacon.never());
    } else {
      i = 0;
      return new EventStream(describe(Bacon, "fromArray", values), function(sink) {
        var push, reply, unsubd;
        unsubd = false;
        reply = Bacon.more;
        push = function() {
          var value;
          if ((reply !== Bacon.noMore) && !unsubd) {
            value = values[i++];
            reply = sink(toEvent(value));
            if (reply !== Bacon.noMore) {
              if (i === values.length) {
                return sink(end());
              } else {
                return UpdateBarrier.afterTransaction(push);
              }
            }
          }
        };
        push();
        return function() {
          return unsubd = true;
        };
      });
    }
  };

  Bacon.mergeAll = function() {
    var streams;
    streams = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (isArray(streams[0])) {
      streams = streams[0];
    }
    if (streams.length) {
      return new EventStream(describe.apply(null, [Bacon, "mergeAll"].concat(__slice.call(streams))), function(sink) {
        var ends, sinks, smartSink;
        ends = 0;
        smartSink = function(obs) {
          return function(unsubBoth) {
            return obs.dispatcher.subscribe(function(event) {
              var reply;
              if (event.isEnd()) {
                ends++;
                if (ends === streams.length) {
                  return sink(end());
                } else {
                  return Bacon.more;
                }
              } else {
                reply = sink(event);
                if (reply === Bacon.noMore) {
                  unsubBoth();
                }
                return reply;
              }
            });
          };
        };
        sinks = _.map(smartSink, streams);
        return compositeUnsubscribe.apply(null, sinks);
      });
    } else {
      return Bacon.never();
    }
  };

  Bacon.zipAsArray = function() {
    var streams;
    streams = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (isArray(streams[0])) {
      streams = streams[0];
    }
    return withDescription.apply(null, [Bacon, "zipAsArray"].concat(__slice.call(streams), [Bacon.zipWith(streams, function() {
      var xs;
      xs = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
      return xs;
    })]));
  };

  Bacon.zipWith = function() {
    var f, streams, _ref1;
    f = arguments[0], streams = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    if (!isFunction(f)) {
      _ref1 = [f, streams[0]], streams = _ref1[0], f = _ref1[1];
    }
    streams = _.map((function(s) {
      return s.toEventStream();
    }), streams);
    return withDescription.apply(null, [Bacon, "zipWith", f].concat(__slice.call(streams), [Bacon.when(streams, f)]));
  };

  Bacon.groupSimultaneous = function() {
    var s, sources, streams;
    streams = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (streams.length === 1 && isArray(streams[0])) {
      streams = streams[0];
    }
    sources = (function() {
      var _i, _len, _results;
      _results = [];
      for (_i = 0, _len = streams.length; _i < _len; _i++) {
        s = streams[_i];
        _results.push(new BufferingSource(s));
      }
      return _results;
    })();
    return withDescription.apply(null, [Bacon, "groupSimultaneous"].concat(__slice.call(streams), [Bacon.when(sources, (function() {
      var xs;
      xs = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
      return xs;
    }))]));
  };

  Bacon.combineAsArray = function() {
    var index, s, sources, stream, streams, _i, _len;
    streams = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    if (streams.length === 1 && isArray(streams[0])) {
      streams = streams[0];
    }
    for (index = _i = 0, _len = streams.length; _i < _len; index = ++_i) {
      stream = streams[index];
      if (!(isObservable(stream))) {
        streams[index] = Bacon.constant(stream);
      }
    }
    if (streams.length) {
      sources = (function() {
        var _j, _len1, _results;
        _results = [];
        for (_j = 0, _len1 = streams.length; _j < _len1; _j++) {
          s = streams[_j];
          _results.push(new Source(s, true));
        }
        return _results;
      })();
      return withDescription.apply(null, [Bacon, "combineAsArray"].concat(__slice.call(streams), [Bacon.when(sources, (function() {
        var xs;
        xs = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
        return xs;
      })).toProperty()]));
    } else {
      return Bacon.constant([]);
    }
  };

  Bacon.onValues = function() {
    var f, streams, _i;
    streams = 2 <= arguments.length ? __slice.call(arguments, 0, _i = arguments.length - 1) : (_i = 0, []), f = arguments[_i++];
    return Bacon.combineAsArray(streams).onValues(f);
  };

  Bacon.combineWith = function() {
    var f, streams;
    f = arguments[0], streams = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    return withDescription.apply(null, [Bacon, "combineWith", f].concat(__slice.call(streams), [Bacon.combineAsArray(streams).map(function(values) {
      return f.apply(null, values);
    })]));
  };

  Bacon.combineTemplate = function(template) {
    var applyStreamValue, combinator, compile, compileTemplate, constantValue, current, funcs, mkContext, setValue, streams;
    funcs = [];
    streams = [];
    current = function(ctxStack) {
      return ctxStack[ctxStack.length - 1];
    };
    setValue = function(ctxStack, key, value) {
      return current(ctxStack)[key] = value;
    };
    applyStreamValue = function(key, index) {
      return function(ctxStack, values) {
        return setValue(ctxStack, key, values[index]);
      };
    };
    constantValue = function(key, value) {
      return function(ctxStack) {
        return setValue(ctxStack, key, value);
      };
    };
    mkContext = function(template) {
      if (isArray(template)) {
        return [];
      } else {
        return {};
      }
    };
    compile = function(key, value) {
      var popContext, pushContext;
      if (isObservable(value)) {
        streams.push(value);
        return funcs.push(applyStreamValue(key, streams.length - 1));
      } else if (value === Object(value) && typeof value !== "function" && !(value instanceof RegExp) && !(value instanceof Date)) {
        pushContext = function(key) {
          return function(ctxStack) {
            var newContext;
            newContext = mkContext(value);
            setValue(ctxStack, key, newContext);
            return ctxStack.push(newContext);
          };
        };
        popContext = function(ctxStack) {
          return ctxStack.pop();
        };
        funcs.push(pushContext(key));
        compileTemplate(value);
        return funcs.push(popContext);
      } else {
        return funcs.push(constantValue(key, value));
      }
    };
    compileTemplate = function(template) {
      return _.each(template, compile);
    };
    compileTemplate(template);
    combinator = function(values) {
      var ctxStack, f, rootContext, _i, _len;
      rootContext = mkContext(template);
      ctxStack = [rootContext];
      for (_i = 0, _len = funcs.length; _i < _len; _i++) {
        f = funcs[_i];
        f(ctxStack, values);
      }
      return rootContext;
    };
    return withDescription(Bacon, "combineTemplate", template, Bacon.combineAsArray(streams).map(combinator));
  };

  Bacon.retry = function(options) {
    var delay, isRetryable, maxRetries, retries, retry, source;
    if (!isFunction(options.source)) {
      throw new Exception("'source' option has to be a function");
    }
    source = options.source;
    retries = options.retries || 0;
    maxRetries = options.maxRetries || retries;
    delay = options.delay || function() {
      return 0;
    };
    isRetryable = options.isRetryable || function() {
      return true;
    };
    retry = function(context) {
      var delayedRetry, nextAttemptOptions;
      nextAttemptOptions = {
        source: source,
        retries: retries - 1,
        maxRetries: maxRetries,
        delay: delay,
        isRetryable: isRetryable
      };
      delayedRetry = function() {
        return Bacon.retry(nextAttemptOptions);
      };
      return Bacon.later(delay(context)).filter(false).concat(Bacon.once().flatMap(delayedRetry));
    };
    return withDescription(Bacon, "retry", options, source().flatMapError(function(e) {
      if (isRetryable(e) && retries > 0) {
        return retry({
          error: e,
          retriesDone: maxRetries - retries
        });
      } else {
        return Bacon.once(new Error(e));
      }
    }));
  };

  eventIdCounter = 0;

  Event = (function() {
    function Event() {
      this.id = ++eventIdCounter;
    }

    Event.prototype.isEvent = function() {
      return true;
    };

    Event.prototype.isEnd = function() {
      return false;
    };

    Event.prototype.isInitial = function() {
      return false;
    };

    Event.prototype.isNext = function() {
      return false;
    };

    Event.prototype.isError = function() {
      return false;
    };

    Event.prototype.hasValue = function() {
      return false;
    };

    Event.prototype.filter = function() {
      return true;
    };

    Event.prototype.inspect = function() {
      return this.toString();
    };

    Event.prototype.log = function() {
      return this.toString();
    };

    return Event;

  })();

  Next = (function(_super) {
    __extends(Next, _super);

    function Next(valueF, eager) {
      Next.__super__.constructor.call(this);
      if (!eager && isFunction(valueF) || valueF instanceof Next) {
        this.valueF = valueF;
        this.valueInternal = void 0;
      } else {
        this.valueF = void 0;
        this.valueInternal = valueF;
      }
    }

    Next.prototype.isNext = function() {
      return true;
    };

    Next.prototype.hasValue = function() {
      return true;
    };

    Next.prototype.value = function() {
      if (this.valueF instanceof Next) {
        this.valueInternal = this.valueF.value();
        this.valueF = void 0;
      } else if (this.valueF) {
        this.valueInternal = this.valueF();
        this.valueF = void 0;
      }
      return this.valueInternal;
    };

    Next.prototype.fmap = function(f) {
      var event, value;
      if (this.valueInternal) {
        value = this.valueInternal;
        return this.apply(function() {
          return f(value);
        });
      } else {
        event = this;
        return this.apply(function() {
          return f(event.value());
        });
      }
    };

    Next.prototype.apply = function(value) {
      return new Next(value);
    };

    Next.prototype.filter = function(f) {
      return f(this.value());
    };

    Next.prototype.toString = function() {
      return _.toString(this.value());
    };

    Next.prototype.log = function() {
      return this.value();
    };

    return Next;

  })(Event);

  Initial = (function(_super) {
    __extends(Initial, _super);

    function Initial() {
      return Initial.__super__.constructor.apply(this, arguments);
    }

    Initial.prototype.isInitial = function() {
      return true;
    };

    Initial.prototype.isNext = function() {
      return false;
    };

    Initial.prototype.apply = function(value) {
      return new Initial(value);
    };

    Initial.prototype.toNext = function() {
      return new Next(this);
    };

    return Initial;

  })(Next);

  End = (function(_super) {
    __extends(End, _super);

    function End() {
      return End.__super__.constructor.apply(this, arguments);
    }

    End.prototype.isEnd = function() {
      return true;
    };

    End.prototype.fmap = function() {
      return this;
    };

    End.prototype.apply = function() {
      return this;
    };

    End.prototype.toString = function() {
      return "<end>";
    };

    return End;

  })(Event);

  Error = (function(_super) {
    __extends(Error, _super);

    function Error(error) {
      this.error = error;
    }

    Error.prototype.isError = function() {
      return true;
    };

    Error.prototype.fmap = function() {
      return this;
    };

    Error.prototype.apply = function() {
      return this;
    };

    Error.prototype.toString = function() {
      return "<error> " + _.toString(this.error);
    };

    return Error;

  })(Event);

  idCounter = 0;

  Observable = (function() {
    function Observable(desc) {
      this.id = ++idCounter;
      withDescription(desc, this);
      this.initialDesc = this.desc;
    }

    Observable.prototype.subscribe = function(sink) {
      return UpdateBarrier.wrappedSubscribe(this, sink);
    };

    Observable.prototype.subscribeInternal = function(sink) {
      return this.dispatcher.subscribe(sink);
    };

    Observable.prototype.onValue = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return this.subscribe(function(event) {
        if (event.hasValue()) {
          return f(event.value());
        }
      });
    };

    Observable.prototype.onValues = function(f) {
      return this.onValue(function(args) {
        return f.apply(null, args);
      });
    };

    Observable.prototype.onError = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return this.subscribe(function(event) {
        if (event.isError()) {
          return f(event.error);
        }
      });
    };

    Observable.prototype.onEnd = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return this.subscribe(function(event) {
        if (event.isEnd()) {
          return f();
        }
      });
    };

    Observable.prototype.errors = function() {
      return withDescription(this, "errors", this.filter(function() {
        return false;
      }));
    };

    Observable.prototype.filter = function() {
      var args, f;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      return convertArgsToFunction(this, f, args, function(f) {
        return withDescription(this, "filter", f, this.withHandler(function(event) {
          if (event.filter(f)) {
            return this.push(event);
          } else {
            return Bacon.more;
          }
        }));
      });
    };

    Observable.prototype.takeWhile = function() {
      var args, f;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      return convertArgsToFunction(this, f, args, function(f) {
        return withDescription(this, "takeWhile", f, this.withHandler(function(event) {
          if (event.filter(f)) {
            return this.push(event);
          } else {
            this.push(end());
            return Bacon.noMore;
          }
        }));
      });
    };

    Observable.prototype.endOnError = function() {
      var args, f;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      if (f == null) {
        f = true;
      }
      return convertArgsToFunction(this, f, args, function(f) {
        return withDescription(this, "endOnError", this.withHandler(function(event) {
          if (event.isError() && f(event.error)) {
            this.push(event);
            return this.push(end());
          } else {
            return this.push(event);
          }
        }));
      });
    };

    Observable.prototype.take = function(count) {
      if (count <= 0) {
        return Bacon.never();
      }
      return withDescription(this, "take", count, this.withHandler(function(event) {
        if (!event.hasValue()) {
          return this.push(event);
        } else {
          count--;
          if (count > 0) {
            return this.push(event);
          } else {
            if (count === 0) {
              this.push(event);
            }
            this.push(end());
            return Bacon.noMore;
          }
        }
      }));
    };

    Observable.prototype.map = function() {
      var args, p;
      p = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      if (p instanceof Property) {
        return p.sampledBy(this, former);
      } else {
        return convertArgsToFunction(this, p, args, function(f) {
          return withDescription(this, "map", f, this.withHandler(function(event) {
            return this.push(event.fmap(f));
          }));
        });
      }
    };

    Observable.prototype.mapError = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return withDescription(this, "mapError", f, this.withHandler(function(event) {
        if (event.isError()) {
          return this.push(next(f(event.error)));
        } else {
          return this.push(event);
        }
      }));
    };

    Observable.prototype.mapEnd = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return withDescription(this, "mapEnd", f, this.withHandler(function(event) {
        if (event.isEnd()) {
          this.push(next(f(event)));
          this.push(end());
          return Bacon.noMore;
        } else {
          return this.push(event);
        }
      }));
    };

    Observable.prototype.doAction = function() {
      var f;
      f = makeFunctionArgs(arguments);
      return withDescription(this, "doAction", f, this.withHandler(function(event) {
        if (event.hasValue()) {
          f(event.value());
        }
        return this.push(event);
      }));
    };

    Observable.prototype.skip = function(count) {
      return withDescription(this, "skip", count, this.withHandler(function(event) {
        if (!event.hasValue()) {
          return this.push(event);
        } else if (count > 0) {
          count--;
          return Bacon.more;
        } else {
          return this.push(event);
        }
      }));
    };

    Observable.prototype.skipDuplicates = function(isEqual) {
      if (isEqual == null) {
        isEqual = function(a, b) {
          return a === b;
        };
      }
      return withDescription(this, "skipDuplicates", this.withStateMachine(None, function(prev, event) {
        if (!event.hasValue()) {
          return [prev, [event]];
        } else if (event.isInitial() || prev === None || !isEqual(prev.get(), event.value())) {
          return [new Some(event.value()), [event]];
        } else {
          return [prev, []];
        }
      }));
    };

    Observable.prototype.skipErrors = function() {
      return withDescription(this, "skipErrors", this.withHandler(function(event) {
        if (event.isError()) {
          return Bacon.more;
        } else {
          return this.push(event);
        }
      }));
    };

    Observable.prototype.withStateMachine = function(initState, f) {
      var state;
      state = initState;
      return withDescription(this, "withStateMachine", initState, f, this.withHandler(function(event) {
        var fromF, newState, output, outputs, reply, _i, _len;
        fromF = f(state, event);
        newState = fromF[0], outputs = fromF[1];
        state = newState;
        reply = Bacon.more;
        for (_i = 0, _len = outputs.length; _i < _len; _i++) {
          output = outputs[_i];
          reply = this.push(output);
          if (reply === Bacon.noMore) {
            return reply;
          }
        }
        return reply;
      }));
    };

    Observable.prototype.scan = function(seed, f) {
      var acc, resultProperty, subscribe;
      f = toCombinator(f);
      acc = toOption(seed);
      subscribe = (function(_this) {
        return function(sink) {
          var initSent, reply, sendInit, unsub;
          initSent = false;
          unsub = nop;
          reply = Bacon.more;
          sendInit = function() {
            if (!initSent) {
              return acc.forEach(function(value) {
                initSent = true;
                reply = sink(new Initial(function() {
                  return value;
                }));
                if (reply === Bacon.noMore) {
                  unsub();
                  return unsub = nop;
                }
              });
            }
          };
          unsub = _this.dispatcher.subscribe(function(event) {
            var next, prev;
            if (event.hasValue()) {
              if (initSent && event.isInitial()) {
                return Bacon.more;
              } else {
                if (!event.isInitial()) {
                  sendInit();
                }
                initSent = true;
                prev = acc.getOrElse(void 0);
                next = f(prev, event.value());
                acc = new Some(next);
                return sink(event.apply(function() {
                  return next;
                }));
              }
            } else {
              if (event.isEnd()) {
                reply = sendInit();
              }
              if (reply !== Bacon.noMore) {
                return sink(event);
              }
            }
          });
          UpdateBarrier.whenDoneWith(resultProperty, sendInit);
          return unsub;
        };
      })(this);
      return resultProperty = new Property(describe(this, "scan", seed, f), subscribe);
    };

    Observable.prototype.fold = function(seed, f) {
      return withDescription(this, "fold", seed, f, this.scan(seed, f).sampledBy(this.filter(false).mapEnd().toProperty()));
    };

    Observable.prototype.zip = function(other, f) {
      if (f == null) {
        f = Array;
      }
      return withDescription(this, "zip", other, Bacon.zipWith([this, other], f));
    };

    Observable.prototype.diff = function(start, f) {
      f = toCombinator(f);
      return withDescription(this, "diff", start, f, this.scan([start], function(prevTuple, next) {
        return [next, f(prevTuple[0], next)];
      }).filter(function(tuple) {
        return tuple.length === 2;
      }).map(function(tuple) {
        return tuple[1];
      }));
    };

    Observable.prototype.flatMap = function() {
      return flatMap_(this, makeSpawner(arguments));
    };

    Observable.prototype.flatMapFirst = function() {
      return flatMap_(this, makeSpawner(arguments), true);
    };

    Observable.prototype.flatMapWithConcurrencyLimit = function() {
      var args, limit;
      limit = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      return withDescription.apply(null, [this, "flatMapWithConcurrencyLimit", limit].concat(__slice.call(args), [flatMap_(this, makeSpawner(args), false, limit)]));
    };

    Observable.prototype.flatMapLatest = function() {
      var f, stream;
      f = makeSpawner(arguments);
      stream = this.toEventStream();
      return withDescription(this, "flatMapLatest", f, stream.flatMap(function(value) {
        return makeObservable(f(value)).takeUntil(stream);
      }));
    };

    Observable.prototype.flatMapError = function(fn) {
      return withDescription(this, "flatMapError", fn, this.mapError(function(err) {
        return new Error(err);
      }).flatMap(function(x) {
        if (x instanceof Error) {
          return fn(x.error);
        } else {
          return Bacon.once(x);
        }
      }));
    };

    Observable.prototype.flatMapConcat = function() {
      return withDescription.apply(null, [this, "flatMapConcat"].concat(__slice.call(arguments), [this.flatMapWithConcurrencyLimit.apply(this, [1].concat(__slice.call(arguments)))]));
    };

    Observable.prototype.bufferingThrottle = function(minimumInterval) {
      return withDescription(this, "bufferingThrottle", minimumInterval, this.flatMapConcat(function(x) {
        return Bacon.once(x).concat(Bacon.later(minimumInterval).filter(false));
      }));
    };

    Observable.prototype.not = function() {
      return withDescription(this, "not", this.map(function(x) {
        return !x;
      }));
    };

    Observable.prototype.log = function() {
      var args;
      args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
      this.subscribe(function(event) {
        return typeof console !== "undefined" && console !== null ? typeof console.log === "function" ? console.log.apply(console, __slice.call(args).concat([event.log()])) : void 0 : void 0;
      });
      return this;
    };

    Observable.prototype.slidingWindow = function(n, minValues) {
      if (minValues == null) {
        minValues = 0;
      }
      return withDescription(this, "slidingWindow", n, minValues, this.scan([], (function(window, value) {
        return window.concat([value]).slice(-n);
      })).filter((function(values) {
        return values.length >= minValues;
      })));
    };

    Observable.prototype.combine = function(other, f) {
      var combinator;
      combinator = toCombinator(f);
      return withDescription(this, "combine", other, f, Bacon.combineAsArray(this, other).map(function(values) {
        return combinator(values[0], values[1]);
      }));
    };

    Observable.prototype.decode = function(cases) {
      return withDescription(this, "decode", cases, this.combine(Bacon.combineTemplate(cases), function(key, values) {
        return values[key];
      }));
    };

    Observable.prototype.awaiting = function(other) {
      return withDescription(this, "awaiting", other, Bacon.groupSimultaneous(this, other).map(function(_arg) {
        var myValues, otherValues;
        myValues = _arg[0], otherValues = _arg[1];
        return otherValues.length === 0;
      }).toProperty(false).skipDuplicates());
    };

    Observable.prototype.name = function(name) {
      this._name = name;
      return this;
    };

    Observable.prototype.withDescription = function() {
      return describe.apply(null, arguments).apply(this);
    };

    Observable.prototype.toString = function() {
      if (this._name) {
        return this._name;
      } else {
        return this.desc.toString();
      }
    };

    Observable.prototype.internalDeps = function() {
      return this.initialDesc.deps();
    };

    return Observable;

  })();

  Observable.prototype.reduce = Observable.prototype.fold;

  Observable.prototype.assign = Observable.prototype.onValue;

  Observable.prototype.inspect = Observable.prototype.toString;

  flatMap_ = function(root, f, firstOnly, limit) {
    var childDeps, result, rootDep;
    rootDep = [root];
    childDeps = [];
    result = new EventStream(describe(root, "flatMap" + (firstOnly ? "First" : ""), f), function(sink) {
      var checkEnd, checkQueue, composite, queue, spawn;
      composite = new CompositeUnsubscribe();
      queue = [];
      spawn = function(event) {
        var child;
        child = makeObservable(f(event.value()));
        childDeps.push(child);
        return composite.add(function(unsubAll, unsubMe) {
          return child.dispatcher.subscribe(function(event) {
            var reply;
            if (event.isEnd()) {
              _.remove(child, childDeps);
              checkQueue();
              checkEnd(unsubMe);
              return Bacon.noMore;
            } else {
              if (event instanceof Initial) {
                event = event.toNext();
              }
              reply = sink(event);
              if (reply === Bacon.noMore) {
                unsubAll();
              }
              return reply;
            }
          });
        });
      };
      checkQueue = function() {
        var event;
        event = queue.shift();
        if (event) {
          return spawn(event);
        }
      };
      checkEnd = function(unsub) {
        unsub();
        if (composite.empty()) {
          return sink(end());
        }
      };
      composite.add(function(__, unsubRoot) {
        return root.dispatcher.subscribe(function(event) {
          if (event.isEnd()) {
            return checkEnd(unsubRoot);
          } else if (event.isError()) {
            return sink(event);
          } else if (firstOnly && composite.count() > 1) {
            return Bacon.more;
          } else {
            if (composite.unsubscribed) {
              return Bacon.noMore;
            }
            if (limit && composite.count() > limit) {
              return queue.push(event);
            } else {
              return spawn(event);
            }
          }
        });
      });
      return composite.unsubscribe;
    });
    result.internalDeps = function() {
      if (childDeps.length) {
        return rootDep.concat(childDeps);
      } else {
        return rootDep;
      }
    };
    return result;
  };

  EventStream = (function(_super) {
    __extends(EventStream, _super);

    function EventStream(desc, subscribe, handler) {
      if (isFunction(desc)) {
        handler = subscribe;
        subscribe = desc;
        desc = [];
      }
      EventStream.__super__.constructor.call(this, desc);
      assertFunction(subscribe);
      this.dispatcher = new Dispatcher(subscribe, handler);
      registerObs(this);
    }

    EventStream.prototype.delay = function(delay) {
      return withDescription(this, "delay", delay, this.flatMap(function(value) {
        return Bacon.later(delay, value);
      }));
    };

    EventStream.prototype.debounce = function(delay) {
      return withDescription(this, "debounce", delay, this.flatMapLatest(function(value) {
        return Bacon.later(delay, value);
      }));
    };

    EventStream.prototype.debounceImmediate = function(delay) {
      return withDescription(this, "debounceImmediate", delay, this.flatMapFirst(function(value) {
        return Bacon.once(value).concat(Bacon.later(delay).filter(false));
      }));
    };

    EventStream.prototype.throttle = function(delay) {
      return withDescription(this, "throttle", delay, this.bufferWithTime(delay).map(function(values) {
        return values[values.length - 1];
      }));
    };

    EventStream.prototype.bufferWithTime = function(delay) {
      return withDescription(this, "bufferWithTime", delay, this.bufferWithTimeOrCount(delay, Number.MAX_VALUE));
    };

    EventStream.prototype.bufferWithCount = function(count) {
      return withDescription(this, "bufferWithCount", count, this.bufferWithTimeOrCount(void 0, count));
    };

    EventStream.prototype.bufferWithTimeOrCount = function(delay, count) {
      var flushOrSchedule;
      flushOrSchedule = function(buffer) {
        if (buffer.values.length === count) {
          return buffer.flush();
        } else if (delay !== void 0) {
          return buffer.schedule();
        }
      };
      return withDescription(this, "bufferWithTimeOrCount", delay, count, this.buffer(delay, flushOrSchedule, flushOrSchedule));
    };

    EventStream.prototype.buffer = function(delay, onInput, onFlush) {
      var buffer, delayMs, reply;
      if (onInput == null) {
        onInput = nop;
      }
      if (onFlush == null) {
        onFlush = nop;
      }
      buffer = {
        scheduled: false,
        end: void 0,
        values: [],
        flush: function() {
          var reply;
          this.scheduled = false;
          if (this.values.length > 0) {
            reply = this.push(next(this.values));
            this.values = [];
            if (this.end != null) {
              return this.push(this.end);
            } else if (reply !== Bacon.noMore) {
              return onFlush(this);
            }
          } else {
            if (this.end != null) {
              return this.push(this.end);
            }
          }
        },
        schedule: function() {
          if (!this.scheduled) {
            this.scheduled = true;
            return delay((function(_this) {
              return function() {
                return _this.flush();
              };
            })(this));
          }
        }
      };
      reply = Bacon.more;
      if (!isFunction(delay)) {
        delayMs = delay;
        delay = function(f) {
          return Bacon.scheduler.setTimeout(f, delayMs);
        };
      }
      return withDescription(this, "buffer", this.withHandler(function(event) {
        buffer.push = (function(_this) {
          return function(event) {
            return _this.push(event);
          };
        })(this);
        if (event.isError()) {
          reply = this.push(event);
        } else if (event.isEnd()) {
          buffer.end = event;
          if (!buffer.scheduled) {
            buffer.flush();
          }
        } else {
          buffer.values.push(event.value());
          onInput(buffer);
        }
        return reply;
      }));
    };

    EventStream.prototype.merge = function(right) {
      var left;
      assertEventStream(right);
      left = this;
      return withDescription(left, "merge", right, Bacon.mergeAll(this, right));
    };

    EventStream.prototype.toProperty = function(initValue_) {
      var disp, initValue;
      initValue = arguments.length === 0 ? None : toOption(function() {
        return initValue_;
      });
      disp = this.dispatcher;
      return new Property(describe(this, "toProperty", initValue_), function(sink) {
        var initSent, reply, sendInit, unsub;
        initSent = false;
        unsub = nop;
        reply = Bacon.more;
        sendInit = function() {
          if (!initSent) {
            return initValue.forEach(function(value) {
              initSent = true;
              reply = sink(new Initial(value));
              if (reply === Bacon.noMore) {
                unsub();
                return unsub = nop;
              }
            });
          }
        };
        unsub = disp.subscribe(function(event) {
          if (event.hasValue()) {
            if (initSent && event.isInitial()) {
              return Bacon.more;
            } else {
              if (!event.isInitial()) {
                sendInit();
              }
              initSent = true;
              initValue = new Some(event);
              return sink(event);
            }
          } else {
            if (event.isEnd()) {
              reply = sendInit();
            }
            if (reply !== Bacon.noMore) {
              return sink(event);
            }
          }
        });
        sendInit();
        return unsub;
      });
    };

    EventStream.prototype.toEventStream = function() {
      return this;
    };

    EventStream.prototype.sampledBy = function(sampler, combinator) {
      return withDescription(this, "sampledBy", sampler, combinator, this.toProperty().sampledBy(sampler, combinator));
    };

    EventStream.prototype.concat = function(right) {
      var left;
      left = this;
      return new EventStream(describe(left, "concat", right), function(sink) {
        var unsubLeft, unsubRight;
        unsubRight = nop;
        unsubLeft = left.dispatcher.subscribe(function(e) {
          if (e.isEnd()) {
            return unsubRight = right.dispatcher.subscribe(sink);
          } else {
            return sink(e);
          }
        });
        return function() {
          unsubLeft();
          return unsubRight();
        };
      });
    };

    EventStream.prototype.takeUntil = function(stopper) {
      var endMarker;
      endMarker = {};
      return withDescription(this, "takeUntil", stopper, Bacon.groupSimultaneous(this.mapEnd(endMarker), stopper.skipErrors()).withHandler(function(event) {
        var data, reply, value, _i, _len, _ref1;
        if (!event.hasValue()) {
          return this.push(event);
        } else {
          _ref1 = event.value(), data = _ref1[0], stopper = _ref1[1];
          if (stopper.length) {
            return this.push(end());
          } else {
            reply = Bacon.more;
            for (_i = 0, _len = data.length; _i < _len; _i++) {
              value = data[_i];
              if (value === endMarker) {
                reply = this.push(end());
              } else {
                reply = this.push(next(value));
              }
            }
            return reply;
          }
        }
      }));
    };

    EventStream.prototype.skipUntil = function(starter) {
      var started;
      started = starter.take(1).map(true).toProperty(false);
      return withDescription(this, "skipUntil", starter, this.filter(started));
    };

    EventStream.prototype.skipWhile = function() {
      var args, f, ok;
      f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
      ok = false;
      return convertArgsToFunction(this, f, args, function(f) {
        return withDescription(this, "skipWhile", f, this.withHandler(function(event) {
          if (ok || !event.hasValue() || !f(event.value())) {
            if (event.hasValue()) {
              ok = true;
            }
            return this.push(event);
          } else {
            return Bacon.more;
          }
        }));
      });
    };

    EventStream.prototype.holdWhen = function(valve) {
      var putToHold, releaseHold, valve_;
      valve_ = valve.startWith(false);
      releaseHold = valve_.filter(function(x) {
        return !x;
      });
      putToHold = valve_.filter(_.id);
      return withDescription(this, "holdWhen", valve, this.filter(false).merge(valve_.flatMapConcat((function(_this) {
        return function(shouldHold) {
          if (!shouldHold) {
            return _this.takeUntil(putToHold);
          } else {
            return _this.scan([], (function(xs, x) {
              return xs.concat(x);
            })).sampledBy(releaseHold).take(1).flatMap(Bacon.fromArray);
          }
        };
      })(this))));
    };

    EventStream.prototype.startWith = function(seed) {
      return withDescription(this, "startWith", seed, Bacon.once(seed).concat(this));
    };

    EventStream.prototype.withHandler = function(handler) {
      return new EventStream(describe(this, "withHandler", handler), this.dispatcher.subscribe, handler);
    };

    return EventStream;

  })(Observable);

  Property = (function(_super) {
    __extends(Property, _super);

    function Property(desc, subscribe, handler) {
      if (isFunction(desc)) {
        handler = subscribe;
        subscribe = desc;
        desc = [];
      }
      Property.__super__.constructor.call(this, desc);
      assertFunction(subscribe);
      this.dispatcher = new PropertyDispatcher(this, subscribe, handler);
      registerObs(this);
    }

    Property.prototype.sampledBy = function(sampler, combinator) {
      var lazy, result, samplerSource, stream, thisSource;
      if (combinator != null) {
        combinator = toCombinator(combinator);
      } else {
        lazy = true;
        combinator = function(f) {
          return f.value();
        };
      }
      thisSource = new Source(this, false, lazy);
      samplerSource = new Source(sampler, true, lazy);
      stream = Bacon.when([thisSource, samplerSource], combinator);
      result = sampler instanceof Property ? stream.toProperty() : stream;
      return withDescription(this, "sampledBy", sampler, combinator, result);
    };

    Property.prototype.sample = function(interval) {
      return withDescription(this, "sample", interval, this.sampledBy(Bacon.interval(interval, {})));
    };

    Property.prototype.changes = function() {
      return new EventStream(describe(this, "changes"), (function(_this) {
        return function(sink) {
          return _this.dispatcher.subscribe(function(event) {
            if (!event.isInitial()) {
              return sink(event);
            }
          });
        };
      })(this));
    };

    Property.prototype.withHandler = function(handler) {
      return new Property(describe(this, "withHandler", handler), this.dispatcher.subscribe, handler);
    };

    Property.prototype.toProperty = function() {
      assertNoArguments(arguments);
      return this;
    };

    Property.prototype.toEventStream = function() {
      return new EventStream(describe(this, "toEventStream"), (function(_this) {
        return function(sink) {
          return _this.dispatcher.subscribe(function(event) {
            if (event.isInitial()) {
              event = event.toNext();
            }
            return sink(event);
          });
        };
      })(this));
    };

    Property.prototype.and = function(other) {
      return withDescription(this, "and", other, this.combine(other, function(x, y) {
        return x && y;
      }));
    };

    Property.prototype.or = function(other) {
      return withDescription(this, "or", other, this.combine(other, function(x, y) {
        return x || y;
      }));
    };

    Property.prototype.delay = function(delay) {
      return this.delayChanges("delay", delay, function(changes) {
        return changes.delay(delay);
      });
    };

    Property.prototype.debounce = function(delay) {
      return this.delayChanges("debounce", delay, function(changes) {
        return changes.debounce(delay);
      });
    };

    Property.prototype.throttle = function(delay) {
      return this.delayChanges("throttle", delay, function(changes) {
        return changes.throttle(delay);
      });
    };

    Property.prototype.delayChanges = function() {
      var desc, f, _i;
      desc = 2 <= arguments.length ? __slice.call(arguments, 0, _i = arguments.length - 1) : (_i = 0, []), f = arguments[_i++];
      return withDescription.apply(null, [this].concat(__slice.call(desc), [addPropertyInitValueToStream(this, f(this.changes()))]));
    };

    Property.prototype.takeUntil = function(stopper) {
      var changes;
      changes = this.changes().takeUntil(stopper);
      return withDescription(this, "takeUntil", stopper, addPropertyInitValueToStream(this, changes));
    };

    Property.prototype.startWith = function(value) {
      return withDescription(this, "startWith", value, this.scan(value, function(prev, next) {
        return next;
      }));
    };

    Property.prototype.bufferingThrottle = function() {
      var _ref1;
      return (_ref1 = Property.__super__.bufferingThrottle.apply(this, arguments)).bufferingThrottle.apply(_ref1, arguments).toProperty();
    };

    return Property;

  })(Observable);

  convertArgsToFunction = function(obs, f, args, method) {
    var sampled;
    if (f instanceof Property) {
      sampled = f.sampledBy(obs, function(p, s) {
        return [p, s];
      });
      return method.call(sampled, function(_arg) {
        var p, s;
        p = _arg[0], s = _arg[1];
        return p;
      }).map(function(_arg) {
        var p, s;
        p = _arg[0], s = _arg[1];
        return s;
      });
    } else {
      f = makeFunction(f, args);
      return method.call(obs, f);
    }
  };

  addPropertyInitValueToStream = function(property, stream) {
    var justInitValue;
    justInitValue = new EventStream(describe(property, "justInitValue"), function(sink) {
      var unsub, value;
      value = void 0;
      unsub = property.dispatcher.subscribe(function(event) {
        if (!event.isEnd()) {
          value = event;
        }
        return Bacon.noMore;
      });
      UpdateBarrier.whenDoneWith(justInitValue, function() {
        if (value != null) {
          sink(value);
        }
        return sink(end());
      });
      return unsub;
    });
    return justInitValue.concat(stream).toProperty();
  };

  Dispatcher = (function() {
    function Dispatcher(_subscribe, _handleEvent) {
      this._subscribe = _subscribe;
      this._handleEvent = _handleEvent;
      this.subscribe = __bind(this.subscribe, this);
      this.handleEvent = __bind(this.handleEvent, this);
      this.subscriptions = [];
      this.queue = [];
      this.pushing = false;
      this.ended = false;
      this.prevError = void 0;
      this.unsubSrc = void 0;
    }

    Dispatcher.prototype.hasSubscribers = function() {
      return this.subscriptions.length > 0;
    };

    Dispatcher.prototype.removeSub = function(subscription) {
      return this.subscriptions = _.without(subscription, this.subscriptions);
    };

    Dispatcher.prototype.push = function(event) {
      if (event.isEnd()) {
        this.ended = true;
      }
      return UpdateBarrier.inTransaction(event, this, this.pushIt, [event]);
    };

    Dispatcher.prototype.pushToSubscriptions = function(event) {
      var e, reply, sub, tmp, _i, _len;
      try {
        tmp = this.subscriptions;
        for (_i = 0, _len = tmp.length; _i < _len; _i++) {
          sub = tmp[_i];
          reply = sub.sink(event);
          if (reply === Bacon.noMore || event.isEnd()) {
            this.removeSub(sub);
          }
        }
        return true;
      } catch (_error) {
        e = _error;
        this.pushing = false;
        this.queue = [];
        throw e;
      }
    };

    Dispatcher.prototype.pushIt = function(event) {
      if (!this.pushing) {
        if (event === this.prevError) {
          return;
        }
        if (event.isError()) {
          this.prevError = event;
        }
        this.pushing = true;
        this.pushToSubscriptions(event);
        this.pushing = false;
        while (this.queue.length) {
          event = this.queue.shift();
          this.push(event);
        }
        if (this.hasSubscribers()) {
          return Bacon.more;
        } else {
          this.unsubscribeFromSource();
          return Bacon.noMore;
        }
      } else {
        this.queue.push(event);
        return Bacon.more;
      }
    };

    Dispatcher.prototype.handleEvent = function(event) {
      if (this._handleEvent) {
        return this._handleEvent(event);
      } else {
        return this.push(event);
      }
    };

    Dispatcher.prototype.unsubscribeFromSource = function() {
      if (this.unsubSrc) {
        this.unsubSrc();
      }
      return this.unsubSrc = void 0;
    };

    Dispatcher.prototype.subscribe = function(sink) {
      var subscription;
      if (this.ended) {
        sink(end());
        return nop;
      } else {
        assertFunction(sink);
        subscription = {
          sink: sink
        };
        this.subscriptions.push(subscription);
        if (this.subscriptions.length === 1) {
          this.unsubSrc = this._subscribe(this.handleEvent);
          assertFunction(this.unsubSrc);
        }
        return (function(_this) {
          return function() {
            _this.removeSub(subscription);
            if (!_this.hasSubscribers()) {
              return _this.unsubscribeFromSource();
            }
          };
        })(this);
      }
    };

    return Dispatcher;

  })();

  PropertyDispatcher = (function(_super) {
    __extends(PropertyDispatcher, _super);

    function PropertyDispatcher(property, subscribe, handleEvent) {
      this.property = property;
      this.subscribe = __bind(this.subscribe, this);
      PropertyDispatcher.__super__.constructor.call(this, subscribe, handleEvent);
      this.current = None;
      this.currentValueRootId = void 0;
      this.propertyEnded = false;
    }

    PropertyDispatcher.prototype.push = function(event) {
      if (event.isEnd()) {
        this.propertyEnded = true;
      }
      if (event.hasValue()) {
        this.current = new Some(event);
        this.currentValueRootId = UpdateBarrier.currentEventId();
      }
      return PropertyDispatcher.__super__.push.call(this, event);
    };

    PropertyDispatcher.prototype.maybeSubSource = function(sink, reply) {
      if (reply === Bacon.noMore) {
        return nop;
      } else if (this.propertyEnded) {
        sink(end());
        return nop;
      } else {
        return Dispatcher.prototype.subscribe.call(this, sink);
      }
    };

    PropertyDispatcher.prototype.subscribe = function(sink) {
      var dispatchingId, initSent, reply, valId;
      initSent = false;
      reply = Bacon.more;
      if (this.current.isDefined && (this.hasSubscribers() || this.propertyEnded)) {
        dispatchingId = UpdateBarrier.currentEventId();
        valId = this.currentValueRootId;
        if (!this.propertyEnded && valId && dispatchingId && dispatchingId !== valId) {
          UpdateBarrier.whenDoneWith(this.property, (function(_this) {
            return function() {
              if (_this.currentValueRootId === valId) {
                return sink(initial(_this.current.get().value()));
              }
            };
          })(this));
          return this.maybeSubSource(sink, reply);
        } else {
          UpdateBarrier.inTransaction(void 0, this, (function() {
            return reply = sink(initial(this.current.get().value()));
          }), []);
          return this.maybeSubSource(sink, reply);
        }
      } else {
        return this.maybeSubSource(sink, reply);
      }
    };

    return PropertyDispatcher;

  })(Dispatcher);

  Bus = (function(_super) {
    __extends(Bus, _super);

    function Bus() {
      this.guardedSink = __bind(this.guardedSink, this);
      this.subscribeAll = __bind(this.subscribeAll, this);
      this.unsubAll = __bind(this.unsubAll, this);
      this.sink = void 0;
      this.subscriptions = [];
      this.ended = false;
      Bus.__super__.constructor.call(this, describe(Bacon, "Bus"), this.subscribeAll);
    }

    Bus.prototype.unsubAll = function() {
      var sub, _i, _len, _ref1;
      _ref1 = this.subscriptions;
      for (_i = 0, _len = _ref1.length; _i < _len; _i++) {
        sub = _ref1[_i];
        if (typeof sub.unsub === "function") {
          sub.unsub();
        }
      }
      return void 0;
    };

    Bus.prototype.subscribeAll = function(newSink) {
      var subscription, _i, _len, _ref1;
      this.sink = newSink;
      _ref1 = cloneArray(this.subscriptions);
      for (_i = 0, _len = _ref1.length; _i < _len; _i++) {
        subscription = _ref1[_i];
        this.subscribeInput(subscription);
      }
      return this.unsubAll;
    };

    Bus.prototype.guardedSink = function(input) {
      return (function(_this) {
        return function(event) {
          if (event.isEnd()) {
            _this.unsubscribeInput(input);
            return Bacon.noMore;
          } else {
            return _this.sink(event);
          }
        };
      })(this);
    };

    Bus.prototype.subscribeInput = function(subscription) {
      return subscription.unsub = subscription.input.dispatcher.subscribe(this.guardedSink(subscription.input));
    };

    Bus.prototype.unsubscribeInput = function(input) {
      var i, sub, _i, _len, _ref1;
      _ref1 = this.subscriptions;
      for (i = _i = 0, _len = _ref1.length; _i < _len; i = ++_i) {
        sub = _ref1[i];
        if (sub.input === input) {
          if (typeof sub.unsub === "function") {
            sub.unsub();
          }
          this.subscriptions.splice(i, 1);
          return;
        }
      }
    };

    Bus.prototype.plug = function(input) {
      var sub;
      assertObservable(input);
      if (this.ended) {
        return;
      }
      sub = {
        input: input
      };
      this.subscriptions.push(sub);
      if ((this.sink != null)) {
        this.subscribeInput(sub);
      }
      return (function(_this) {
        return function() {
          return _this.unsubscribeInput(input);
        };
      })(this);
    };

    Bus.prototype.end = function() {
      this.ended = true;
      this.unsubAll();
      return typeof this.sink === "function" ? this.sink(end()) : void 0;
    };

    Bus.prototype.push = function(value) {
      return typeof this.sink === "function" ? this.sink(next(value)) : void 0;
    };

    Bus.prototype.error = function(error) {
      return typeof this.sink === "function" ? this.sink(new Error(error)) : void 0;
    };

    return Bus;

  })(EventStream);

  Source = (function() {
    function Source(obs, sync, lazy) {
      this.obs = obs;
      this.sync = sync;
      this.lazy = lazy != null ? lazy : false;
      this.queue = [];
    }

    Source.prototype.subscribe = function(sink) {
      return this.obs.dispatcher.subscribe(sink);
    };

    Source.prototype.toString = function() {
      return this.obs.toString();
    };

    Source.prototype.markEnded = function() {
      return this.ended = true;
    };

    Source.prototype.consume = function() {
      if (this.lazy) {
        return {
          value: _.always(this.queue[0])
        };
      } else {
        return this.queue[0];
      }
    };

    Source.prototype.push = function(x) {
      return this.queue = [x];
    };

    Source.prototype.mayHave = function() {
      return true;
    };

    Source.prototype.hasAtLeast = function() {
      return this.queue.length;
    };

    Source.prototype.flatten = true;

    return Source;

  })();

  ConsumingSource = (function(_super) {
    __extends(ConsumingSource, _super);

    function ConsumingSource() {
      return ConsumingSource.__super__.constructor.apply(this, arguments);
    }

    ConsumingSource.prototype.consume = function() {
      return this.queue.shift();
    };

    ConsumingSource.prototype.push = function(x) {
      return this.queue.push(x);
    };

    ConsumingSource.prototype.mayHave = function(c) {
      return !this.ended || this.queue.length >= c;
    };

    ConsumingSource.prototype.hasAtLeast = function(c) {
      return this.queue.length >= c;
    };

    ConsumingSource.prototype.flatten = false;

    return ConsumingSource;

  })(Source);

  BufferingSource = (function(_super) {
    __extends(BufferingSource, _super);

    function BufferingSource(obs) {
      BufferingSource.__super__.constructor.call(this, obs, true);
    }

    BufferingSource.prototype.consume = function() {
      var values;
      values = this.queue;
      this.queue = [];
      return {
        value: function() {
          return values;
        }
      };
    };

    BufferingSource.prototype.push = function(x) {
      return this.queue.push(x.value());
    };

    BufferingSource.prototype.hasAtLeast = function() {
      return true;
    };

    return BufferingSource;

  })(Source);

  Source.isTrigger = function(s) {
    if (s instanceof Source) {
      return s.sync;
    } else {
      return s instanceof EventStream;
    }
  };

  Source.fromObservable = function(s) {
    if (s instanceof Source) {
      return s;
    } else if (s instanceof Property) {
      return new Source(s, false);
    } else {
      return new ConsumingSource(s, true);
    }
  };

  describe = function() {
    var args, context, method;
    context = arguments[0], method = arguments[1], args = 3 <= arguments.length ? __slice.call(arguments, 2) : [];
    if ((context || method) instanceof Desc) {
      return context || method;
    } else {
      return new Desc(context, method, args);
    }
  };

  findDeps = function(x) {
    if (isArray(x)) {
      return _.flatMap(findDeps, x);
    } else if (isObservable(x)) {
      return [x];
    } else if (x instanceof Source) {
      return [x.obs];
    } else {
      return [];
    }
  };

  Desc = (function() {
    function Desc(context, method, args) {
      this.context = context;
      this.method = method;
      this.args = args;
      this.cached = void 0;
    }

    Desc.prototype.deps = function() {
      return this.cached || (this.cached = findDeps([this.context].concat(this.args)));
    };

    Desc.prototype.apply = function(obs) {
      obs.desc = this;
      return obs;
    };

    Desc.prototype.toString = function() {
      return _.toString(this.context) + "." + _.toString(this.method) + "(" + _.map(_.toString, this.args) + ")";
    };

    return Desc;

  })();

  withDescription = function() {
    var desc, obs, _i;
    desc = 2 <= arguments.length ? __slice.call(arguments, 0, _i = arguments.length - 1) : (_i = 0, []), obs = arguments[_i++];
    return describe.apply(null, desc).apply(obs);
  };

  Bacon.when = function() {
    var f, i, index, ix, len, needsBarrier, pat, patSources, pats, patterns, resultStream, s, sources, triggerFound, usage, _i, _j, _len, _len1, _ref1;
    if (arguments.length === 0) {
      return Bacon.never();
    }
    len = arguments.length;
    usage = "when: expecting arguments in the form (Observable+,function)+";
    assert(usage, len % 2 === 0);
    sources = [];
    pats = [];
    i = 0;
    patterns = [];
    while (i < len) {
      patterns[i] = arguments[i];
      patterns[i + 1] = arguments[i + 1];
      patSources = _.toArray(arguments[i]);
      f = constantToFunction(arguments[i + 1]);
      pat = {
        f: f,
        ixs: []
      };
      triggerFound = false;
      for (_i = 0, _len = patSources.length; _i < _len; _i++) {
        s = patSources[_i];
        index = _.indexOf(sources, s);
        if (!triggerFound) {
          triggerFound = Source.isTrigger(s);
        }
        if (index < 0) {
          sources.push(s);
          index = sources.length - 1;
        }
        _ref1 = pat.ixs;
        for (_j = 0, _len1 = _ref1.length; _j < _len1; _j++) {
          ix = _ref1[_j];
          if (ix.index === index) {
            ix.count++;
          }
        }
        pat.ixs.push({
          index: index,
          count: 1
        });
      }
      assert("At least one EventStream required", triggerFound || (!patSources.length));
      if (patSources.length > 0) {
        pats.push(pat);
      }
      i = i + 2;
    }
    if (!sources.length) {
      return Bacon.never();
    }
    sources = _.map(Source.fromObservable, sources);
    needsBarrier = (_.any(sources, function(s) {
      return s.flatten;
    })) && (containsDuplicateDeps(_.map((function(s) {
      return s.obs;
    }), sources)));
    return resultStream = new EventStream(describe.apply(null, [Bacon, "when"].concat(__slice.call(patterns))), function(sink) {
      var cannotMatch, cannotSync, ends, match, nonFlattened, part, triggers;
      triggers = [];
      ends = false;
      match = function(p) {
        var _k, _len2, _ref2;
        _ref2 = p.ixs;
        for (_k = 0, _len2 = _ref2.length; _k < _len2; _k++) {
          i = _ref2[_k];
          if (!sources[i.index].hasAtLeast(i.count)) {
            return false;
          }
        }
        return true;
      };
      cannotSync = function(source) {
        return !source.sync || source.ended;
      };
      cannotMatch = function(p) {
        var _k, _len2, _ref2;
        _ref2 = p.ixs;
        for (_k = 0, _len2 = _ref2.length; _k < _len2; _k++) {
          i = _ref2[_k];
          if (!sources[i.index].mayHave(i.count)) {
            return true;
          }
        }
      };
      nonFlattened = function(trigger) {
        return !trigger.source.flatten;
      };
      part = function(source) {
        return function(unsubAll) {
          var flush, flushLater, flushWhileTriggers;
          flushLater = function() {
            return UpdateBarrier.whenDoneWith(resultStream, flush);
          };
          flushWhileTriggers = function() {
            var events, p, reply, trigger, _k, _len2;
            if (triggers.length > 0) {
              reply = Bacon.more;
              trigger = triggers.pop();
              for (_k = 0, _len2 = pats.length; _k < _len2; _k++) {
                p = pats[_k];
                if (match(p)) {
                  events = (function() {
                    var _l, _len3, _ref2, _results;
                    _ref2 = p.ixs;
                    _results = [];
                    for (_l = 0, _len3 = _ref2.length; _l < _len3; _l++) {
                      i = _ref2[_l];
                      _results.push(sources[i.index].consume());
                    }
                    return _results;
                  })();
                  reply = sink(trigger.e.apply(function() {
                    var event, values;
                    values = (function() {
                      var _l, _len3, _results;
                      _results = [];
                      for (_l = 0, _len3 = events.length; _l < _len3; _l++) {
                        event = events[_l];
                        _results.push(event.value());
                      }
                      return _results;
                    })();
                    return p.f.apply(p, values);
                  }));
                  if (triggers.length) {
                    triggers = _.filter(nonFlattened, triggers);
                  }
                  if (reply === Bacon.noMore) {
                    return reply;
                  } else {
                    return flushWhileTriggers();
                  }
                }
              }
            } else {
              return Bacon.more;
            }
          };
          flush = function() {
            var reply;
            reply = flushWhileTriggers();
            if (ends) {
              ends = false;
              if (_.all(sources, cannotSync) || _.all(pats, cannotMatch)) {
                reply = Bacon.noMore;
                sink(end());
              }
            }
            if (reply === Bacon.noMore) {
              unsubAll();
            }
            return reply;
          };
          return source.subscribe(function(e) {
            var reply;
            if (e.isEnd()) {
              ends = true;
              source.markEnded();
              flushLater();
            } else if (e.isError()) {
              reply = sink(e);
            } else {
              source.push(e);
              if (source.sync) {
                triggers.push({
                  source: source,
                  e: e
                });
                if (needsBarrier || UpdateBarrier.hasWaiters()) {
                  flushLater();
                } else {
                  flush();
                }
              }
            }
            if (reply === Bacon.noMore) {
              unsubAll();
            }
            return reply || Bacon.more;
          });
        };
      };
      return compositeUnsubscribe.apply(null, (function() {
        var _k, _len2, _results;
        _results = [];
        for (_k = 0, _len2 = sources.length; _k < _len2; _k++) {
          s = sources[_k];
          _results.push(part(s));
        }
        return _results;
      })());
    });
  };

  containsDuplicateDeps = function(observables, state) {
    var checkObservable;
    if (state == null) {
      state = [];
    }
    checkObservable = function(obs) {
      var deps;
      if (_.contains(state, obs)) {
        return true;
      } else {
        deps = obs.internalDeps();
        if (deps.length) {
          state.push(obs);
          return _.any(deps, checkObservable);
        } else {
          state.push(obs);
          return false;
        }
      }
    };
    return _.any(observables, checkObservable);
  };

  Bacon.update = function() {
    var i, initial, lateBindFirst, patterns;
    initial = arguments[0], patterns = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    lateBindFirst = function(f) {
      return function() {
        var args;
        args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
        return function(i) {
          return f.apply(null, [i].concat(args));
        };
      };
    };
    i = patterns.length - 1;
    while (i > 0) {
      if (!(patterns[i] instanceof Function)) {
        patterns[i] = (function(x) {
          return function() {
            return x;
          };
        })(patterns[i]);
      }
      patterns[i] = lateBindFirst(patterns[i]);
      i = i - 2;
    }
    return withDescription.apply(null, [Bacon, "update", initial].concat(__slice.call(patterns), [Bacon.when.apply(Bacon, patterns).scan(initial, (function(x, f) {
      return f(x);
    }))]));
  };

  compositeUnsubscribe = function() {
    var ss;
    ss = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
    return new CompositeUnsubscribe(ss).unsubscribe;
  };

  CompositeUnsubscribe = (function() {
    function CompositeUnsubscribe(ss) {
      var s, _i, _len;
      if (ss == null) {
        ss = [];
      }
      this.unsubscribe = __bind(this.unsubscribe, this);
      this.unsubscribed = false;
      this.subscriptions = [];
      this.starting = [];
      for (_i = 0, _len = ss.length; _i < _len; _i++) {
        s = ss[_i];
        this.add(s);
      }
    }

    CompositeUnsubscribe.prototype.add = function(subscription) {
      var ended, unsub, unsubMe;
      if (this.unsubscribed) {
        return;
      }
      ended = false;
      unsub = nop;
      this.starting.push(subscription);
      unsubMe = (function(_this) {
        return function() {
          if (_this.unsubscribed) {
            return;
          }
          ended = true;
          _this.remove(unsub);
          return _.remove(subscription, _this.starting);
        };
      })(this);
      unsub = subscription(this.unsubscribe, unsubMe);
      if (!(this.unsubscribed || ended)) {
        this.subscriptions.push(unsub);
      }
      _.remove(subscription, this.starting);
      return unsub;
    };

    CompositeUnsubscribe.prototype.remove = function(unsub) {
      if (this.unsubscribed) {
        return;
      }
      if ((_.remove(unsub, this.subscriptions)) !== void 0) {
        return unsub();
      }
    };

    CompositeUnsubscribe.prototype.unsubscribe = function() {
      var s, _i, _len, _ref1;
      if (this.unsubscribed) {
        return;
      }
      this.unsubscribed = true;
      _ref1 = this.subscriptions;
      for (_i = 0, _len = _ref1.length; _i < _len; _i++) {
        s = _ref1[_i];
        s();
      }
      this.subscriptions = [];
      return this.starting = [];
    };

    CompositeUnsubscribe.prototype.count = function() {
      if (this.unsubscribed) {
        return 0;
      }
      return this.subscriptions.length + this.starting.length;
    };

    CompositeUnsubscribe.prototype.empty = function() {
      return this.count() === 0;
    };

    return CompositeUnsubscribe;

  })();

  Bacon.CompositeUnsubscribe = CompositeUnsubscribe;

  Some = (function() {
    function Some(value) {
      this.value = value;
    }

    Some.prototype.getOrElse = function() {
      return this.value;
    };

    Some.prototype.get = function() {
      return this.value;
    };

    Some.prototype.filter = function(f) {
      if (f(this.value)) {
        return new Some(this.value);
      } else {
        return None;
      }
    };

    Some.prototype.map = function(f) {
      return new Some(f(this.value));
    };

    Some.prototype.forEach = function(f) {
      return f(this.value);
    };

    Some.prototype.isDefined = true;

    Some.prototype.toArray = function() {
      return [this.value];
    };

    Some.prototype.inspect = function() {
      return "Some(" + this.value + ")";
    };

    Some.prototype.toString = function() {
      return this.inspect();
    };

    return Some;

  })();

  None = {
    getOrElse: function(value) {
      return value;
    },
    filter: function() {
      return None;
    },
    map: function() {
      return None;
    },
    forEach: function() {},
    isDefined: false,
    toArray: function() {
      return [];
    },
    inspect: function() {
      return "None";
    },
    toString: function() {
      return this.inspect();
    }
  };

  UpdateBarrier = (function() {
    var afterTransaction, afters, aftersIndex, currentEventId, flush, flushDepsOf, flushWaiters, hasWaiters, inTransaction, rootEvent, waiterObs, waiters, whenDoneWith, wrappedSubscribe;
    rootEvent = void 0;
    waiterObs = [];
    waiters = {};
    afters = [];
    aftersIndex = 0;
    afterTransaction = function(f) {
      if (rootEvent) {
        return afters.push(f);
      } else {
        return f();
      }
    };
    whenDoneWith = function(obs, f) {
      var obsWaiters;
      if (rootEvent) {
        obsWaiters = waiters[obs.id];
        if (obsWaiters == null) {
          obsWaiters = waiters[obs.id] = [f];
          return waiterObs.push(obs);
        } else {
          return obsWaiters.push(f);
        }
      } else {
        return f();
      }
    };
    flush = function() {
      while (waiterObs.length > 0) {
        flushWaiters(0);
      }
      return void 0;
    };
    flushWaiters = function(index) {
      var f, obs, obsId, obsWaiters, _i, _len;
      obs = waiterObs[index];
      obsId = obs.id;
      obsWaiters = waiters[obsId];
      waiterObs.splice(index, 1);
      delete waiters[obsId];
      flushDepsOf(obs);
      for (_i = 0, _len = obsWaiters.length; _i < _len; _i++) {
        f = obsWaiters[_i];
        f();
      }
      return void 0;
    };
    flushDepsOf = function(obs) {
      var dep, deps, index, _i, _len;
      deps = obs.internalDeps();
      for (_i = 0, _len = deps.length; _i < _len; _i++) {
        dep = deps[_i];
        flushDepsOf(dep);
        if (waiters[dep.id]) {
          index = _.indexOf(waiterObs, dep);
          flushWaiters(index);
        }
      }
      return void 0;
    };
    inTransaction = function(event, context, f, args) {
      var after, result;
      if (rootEvent) {
        return f.apply(context, args);
      } else {
        rootEvent = event;
        try {
          result = f.apply(context, args);
          flush();
        } finally {
          rootEvent = void 0;
          while (aftersIndex < afters.length) {
            after = afters[aftersIndex];
            aftersIndex++;
            after();
          }
          aftersIndex = 0;
          afters = [];
        }
        return result;
      }
    };
    currentEventId = function() {
      if (rootEvent) {
        return rootEvent.id;
      } else {
        return void 0;
      }
    };
    wrappedSubscribe = function(obs, sink) {
      var doUnsub, shouldUnsub, unsub, unsubd;
      unsubd = false;
      shouldUnsub = false;
      doUnsub = function() {
        return shouldUnsub = true;
      };
      unsub = function() {
        unsubd = true;
        return doUnsub();
      };
      doUnsub = obs.dispatcher.subscribe(function(event) {
        return afterTransaction(function() {
          var reply;
          if (!unsubd) {
            reply = sink(event);
            if (reply === Bacon.noMore) {
              return unsub();
            }
          }
        });
      });
      if (shouldUnsub) {
        doUnsub();
      }
      return unsub;
    };
    hasWaiters = function() {
      return waiterObs.length > 0;
    };
    return {
      whenDoneWith: whenDoneWith,
      hasWaiters: hasWaiters,
      inTransaction: inTransaction,
      currentEventId: currentEventId,
      wrappedSubscribe: wrappedSubscribe,
      afterTransaction: afterTransaction
    };
  })();

  Bacon.EventStream = EventStream;

  Bacon.Property = Property;

  Bacon.Observable = Observable;

  Bacon.Bus = Bus;

  Bacon.Initial = Initial;

  Bacon.Next = Next;

  Bacon.End = End;

  Bacon.Error = Error;

  nop = function() {};

  latter = function(_, x) {
    return x;
  };

  former = function(x, _) {
    return x;
  };

  initial = function(value) {
    return new Initial(value, true);
  };

  next = function(value) {
    return new Next(value, true);
  };

  end = function() {
    return new End();
  };

  toEvent = function(x) {
    if (x instanceof Event) {
      return x;
    } else {
      return next(x);
    }
  };

  cloneArray = function(xs) {
    return xs.slice(0);
  };

  assert = function(message, condition) {
    if (!condition) {
      throw new Exception(message);
    }
  };

  assertEventStream = function(event) {
    if (!(event instanceof EventStream)) {
      throw new Exception("not an EventStream : " + event);
    }
  };

  assertObservable = function(event) {
    if (!(event instanceof Observable)) {
      throw new Exception("not an Observable : " + event);
    }
  };

  assertFunction = function(f) {
    return assert("not a function : " + f, isFunction(f));
  };

  isFunction = function(f) {
    return typeof f === "function";
  };

  isArray = function(xs) {
    return xs instanceof Array;
  };

  isObservable = function(x) {
    return x instanceof Observable;
  };

  assertArray = function(xs) {
    if (!isArray(xs)) {
      throw new Exception("not an array : " + xs);
    }
  };

  assertNoArguments = function(args) {
    return assert("no arguments supported", args.length === 0);
  };

  assertString = function(x) {
    if (typeof x !== "string") {
      throw new Exception("not a string : " + x);
    }
  };

  partiallyApplied = function(f, applied) {
    return function() {
      var args;
      args = 1 <= arguments.length ? __slice.call(arguments, 0) : [];
      return f.apply(null, applied.concat(args));
    };
  };

  makeSpawner = function(args) {
    if (args.length === 1 && isObservable(args[0])) {
      return _.always(args[0]);
    } else {
      return makeFunctionArgs(args);
    }
  };

  makeFunctionArgs = function(args) {
    args = Array.prototype.slice.call(args);
    return makeFunction_.apply(null, args);
  };

  makeFunction_ = withMethodCallSupport(function() {
    var args, f;
    f = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
    if (isFunction(f)) {
      if (args.length) {
        return partiallyApplied(f, args);
      } else {
        return f;
      }
    } else if (isFieldKey(f)) {
      return toFieldExtractor(f, args);
    } else {
      return _.always(f);
    }
  });

  makeFunction = function(f, args) {
    return makeFunction_.apply(null, [f].concat(__slice.call(args)));
  };

  constantToFunction = function(f) {
    if (isFunction(f)) {
      return f;
    } else {
      return _.always(f);
    }
  };

  makeObservable = function(x) {
    if (isObservable(x)) {
      return x;
    } else {
      return Bacon.once(x);
    }
  };

  isFieldKey = function(f) {
    return (typeof f === "string") && f.length > 1 && f.charAt(0) === ".";
  };

  Bacon.isFieldKey = isFieldKey;

  toFieldExtractor = function(f, args) {
    var partFuncs, parts;
    parts = f.slice(1).split(".");
    partFuncs = _.map(toSimpleExtractor(args), parts);
    return function(value) {
      var _i, _len;
      for (_i = 0, _len = partFuncs.length; _i < _len; _i++) {
        f = partFuncs[_i];
        value = f(value);
      }
      return value;
    };
  };

  toSimpleExtractor = function(args) {
    return function(key) {
      return function(value) {
        var fieldValue;
        if (value == null) {
          return void 0;
        } else {
          fieldValue = value[key];
          if (isFunction(fieldValue)) {
            return fieldValue.apply(value, args);
          } else {
            return fieldValue;
          }
        }
      };
    };
  };

  toFieldKey = function(f) {
    return f.slice(1);
  };

  toCombinator = function(f) {
    var key;
    if (isFunction(f)) {
      return f;
    } else if (isFieldKey(f)) {
      key = toFieldKey(f);
      return function(left, right) {
        return left[key](right);
      };
    } else {
      return assert("not a function or a field key: " + f, false);
    }
  };

  toOption = function(v) {
    if (v instanceof Some || v === None) {
      return v;
    } else {
      return new Some(v);
    }
  };

  if ((typeof define !== "undefined" && define !== null) && (define.amd != null)) {
    define([], function() {
      return Bacon;
    });
    this.Bacon = Bacon;
  } else if ((typeof module !== "undefined" && module !== null) && (module.exports != null)) {
    module.exports = Bacon;
    Bacon.Bacon = Bacon;
  } else {
    this.Bacon = Bacon;
  }

}).call(this);

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/../../../node_modules/baconjs/dist/Bacon.js","/../../../node_modules/baconjs/dist")
},{"+7ZJp0":5,"buffer":2}],2:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
/*!
 * The buffer module from node.js, for the browser.
 *
 * @author   Feross Aboukhadijeh <feross@feross.org> <http://feross.org>
 * @license  MIT
 */

var base64 = require('base64-js')
var ieee754 = require('ieee754')

exports.Buffer = Buffer
exports.SlowBuffer = Buffer
exports.INSPECT_MAX_BYTES = 50
Buffer.poolSize = 8192

/**
 * If `Buffer._useTypedArrays`:
 *   === true    Use Uint8Array implementation (fastest)
 *   === false   Use Object implementation (compatible down to IE6)
 */
Buffer._useTypedArrays = (function () {
  // Detect if browser supports Typed Arrays. Supported browsers are IE 10+, Firefox 4+,
  // Chrome 7+, Safari 5.1+, Opera 11.6+, iOS 4.2+. If the browser does not support adding
  // properties to `Uint8Array` instances, then that's the same as no `Uint8Array` support
  // because we need to be able to add all the node Buffer API methods. This is an issue
  // in Firefox 4-29. Now fixed: https://bugzilla.mozilla.org/show_bug.cgi?id=695438
  try {
    var buf = new ArrayBuffer(0)
    var arr = new Uint8Array(buf)
    arr.foo = function () { return 42 }
    return 42 === arr.foo() &&
        typeof arr.subarray === 'function' // Chrome 9-10 lack `subarray`
  } catch (e) {
    return false
  }
})()

/**
 * Class: Buffer
 * =============
 *
 * The Buffer constructor returns instances of `Uint8Array` that are augmented
 * with function properties for all the node `Buffer` API functions. We use
 * `Uint8Array` so that square bracket notation works as expected -- it returns
 * a single octet.
 *
 * By augmenting the instances, we can avoid modifying the `Uint8Array`
 * prototype.
 */
function Buffer (subject, encoding, noZero) {
  if (!(this instanceof Buffer))
    return new Buffer(subject, encoding, noZero)

  var type = typeof subject

  // Workaround: node's base64 implementation allows for non-padded strings
  // while base64-js does not.
  if (encoding === 'base64' && type === 'string') {
    subject = stringtrim(subject)
    while (subject.length % 4 !== 0) {
      subject = subject + '='
    }
  }

  // Find the length
  var length
  if (type === 'number')
    length = coerce(subject)
  else if (type === 'string')
    length = Buffer.byteLength(subject, encoding)
  else if (type === 'object')
    length = coerce(subject.length) // assume that object is array-like
  else
    throw new Error('First argument needs to be a number, array or string.')

  var buf
  if (Buffer._useTypedArrays) {
    // Preferred: Return an augmented `Uint8Array` instance for best performance
    buf = Buffer._augment(new Uint8Array(length))
  } else {
    // Fallback: Return THIS instance of Buffer (created by `new`)
    buf = this
    buf.length = length
    buf._isBuffer = true
  }

  var i
  if (Buffer._useTypedArrays && typeof subject.byteLength === 'number') {
    // Speed optimization -- use set if we're copying from a typed array
    buf._set(subject)
  } else if (isArrayish(subject)) {
    // Treat array-ish objects as a byte array
    for (i = 0; i < length; i++) {
      if (Buffer.isBuffer(subject))
        buf[i] = subject.readUInt8(i)
      else
        buf[i] = subject[i]
    }
  } else if (type === 'string') {
    buf.write(subject, 0, encoding)
  } else if (type === 'number' && !Buffer._useTypedArrays && !noZero) {
    for (i = 0; i < length; i++) {
      buf[i] = 0
    }
  }

  return buf
}

// STATIC METHODS
// ==============

Buffer.isEncoding = function (encoding) {
  switch (String(encoding).toLowerCase()) {
    case 'hex':
    case 'utf8':
    case 'utf-8':
    case 'ascii':
    case 'binary':
    case 'base64':
    case 'raw':
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      return true
    default:
      return false
  }
}

Buffer.isBuffer = function (b) {
  return !!(b !== null && b !== undefined && b._isBuffer)
}

Buffer.byteLength = function (str, encoding) {
  var ret
  str = str + ''
  switch (encoding || 'utf8') {
    case 'hex':
      ret = str.length / 2
      break
    case 'utf8':
    case 'utf-8':
      ret = utf8ToBytes(str).length
      break
    case 'ascii':
    case 'binary':
    case 'raw':
      ret = str.length
      break
    case 'base64':
      ret = base64ToBytes(str).length
      break
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      ret = str.length * 2
      break
    default:
      throw new Error('Unknown encoding')
  }
  return ret
}

Buffer.concat = function (list, totalLength) {
  assert(isArray(list), 'Usage: Buffer.concat(list, [totalLength])\n' +
      'list should be an Array.')

  if (list.length === 0) {
    return new Buffer(0)
  } else if (list.length === 1) {
    return list[0]
  }

  var i
  if (typeof totalLength !== 'number') {
    totalLength = 0
    for (i = 0; i < list.length; i++) {
      totalLength += list[i].length
    }
  }

  var buf = new Buffer(totalLength)
  var pos = 0
  for (i = 0; i < list.length; i++) {
    var item = list[i]
    item.copy(buf, pos)
    pos += item.length
  }
  return buf
}

// BUFFER INSTANCE METHODS
// =======================

function _hexWrite (buf, string, offset, length) {
  offset = Number(offset) || 0
  var remaining = buf.length - offset
  if (!length) {
    length = remaining
  } else {
    length = Number(length)
    if (length > remaining) {
      length = remaining
    }
  }

  // must be an even number of digits
  var strLen = string.length
  assert(strLen % 2 === 0, 'Invalid hex string')

  if (length > strLen / 2) {
    length = strLen / 2
  }
  for (var i = 0; i < length; i++) {
    var byte = parseInt(string.substr(i * 2, 2), 16)
    assert(!isNaN(byte), 'Invalid hex string')
    buf[offset + i] = byte
  }
  Buffer._charsWritten = i * 2
  return i
}

function _utf8Write (buf, string, offset, length) {
  var charsWritten = Buffer._charsWritten =
    blitBuffer(utf8ToBytes(string), buf, offset, length)
  return charsWritten
}

function _asciiWrite (buf, string, offset, length) {
  var charsWritten = Buffer._charsWritten =
    blitBuffer(asciiToBytes(string), buf, offset, length)
  return charsWritten
}

function _binaryWrite (buf, string, offset, length) {
  return _asciiWrite(buf, string, offset, length)
}

function _base64Write (buf, string, offset, length) {
  var charsWritten = Buffer._charsWritten =
    blitBuffer(base64ToBytes(string), buf, offset, length)
  return charsWritten
}

function _utf16leWrite (buf, string, offset, length) {
  var charsWritten = Buffer._charsWritten =
    blitBuffer(utf16leToBytes(string), buf, offset, length)
  return charsWritten
}

Buffer.prototype.write = function (string, offset, length, encoding) {
  // Support both (string, offset, length, encoding)
  // and the legacy (string, encoding, offset, length)
  if (isFinite(offset)) {
    if (!isFinite(length)) {
      encoding = length
      length = undefined
    }
  } else {  // legacy
    var swap = encoding
    encoding = offset
    offset = length
    length = swap
  }

  offset = Number(offset) || 0
  var remaining = this.length - offset
  if (!length) {
    length = remaining
  } else {
    length = Number(length)
    if (length > remaining) {
      length = remaining
    }
  }
  encoding = String(encoding || 'utf8').toLowerCase()

  var ret
  switch (encoding) {
    case 'hex':
      ret = _hexWrite(this, string, offset, length)
      break
    case 'utf8':
    case 'utf-8':
      ret = _utf8Write(this, string, offset, length)
      break
    case 'ascii':
      ret = _asciiWrite(this, string, offset, length)
      break
    case 'binary':
      ret = _binaryWrite(this, string, offset, length)
      break
    case 'base64':
      ret = _base64Write(this, string, offset, length)
      break
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      ret = _utf16leWrite(this, string, offset, length)
      break
    default:
      throw new Error('Unknown encoding')
  }
  return ret
}

Buffer.prototype.toString = function (encoding, start, end) {
  var self = this

  encoding = String(encoding || 'utf8').toLowerCase()
  start = Number(start) || 0
  end = (end !== undefined)
    ? Number(end)
    : end = self.length

  // Fastpath empty strings
  if (end === start)
    return ''

  var ret
  switch (encoding) {
    case 'hex':
      ret = _hexSlice(self, start, end)
      break
    case 'utf8':
    case 'utf-8':
      ret = _utf8Slice(self, start, end)
      break
    case 'ascii':
      ret = _asciiSlice(self, start, end)
      break
    case 'binary':
      ret = _binarySlice(self, start, end)
      break
    case 'base64':
      ret = _base64Slice(self, start, end)
      break
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      ret = _utf16leSlice(self, start, end)
      break
    default:
      throw new Error('Unknown encoding')
  }
  return ret
}

Buffer.prototype.toJSON = function () {
  return {
    type: 'Buffer',
    data: Array.prototype.slice.call(this._arr || this, 0)
  }
}

// copy(targetBuffer, targetStart=0, sourceStart=0, sourceEnd=buffer.length)
Buffer.prototype.copy = function (target, target_start, start, end) {
  var source = this

  if (!start) start = 0
  if (!end && end !== 0) end = this.length
  if (!target_start) target_start = 0

  // Copy 0 bytes; we're done
  if (end === start) return
  if (target.length === 0 || source.length === 0) return

  // Fatal error conditions
  assert(end >= start, 'sourceEnd < sourceStart')
  assert(target_start >= 0 && target_start < target.length,
      'targetStart out of bounds')
  assert(start >= 0 && start < source.length, 'sourceStart out of bounds')
  assert(end >= 0 && end <= source.length, 'sourceEnd out of bounds')

  // Are we oob?
  if (end > this.length)
    end = this.length
  if (target.length - target_start < end - start)
    end = target.length - target_start + start

  var len = end - start

  if (len < 100 || !Buffer._useTypedArrays) {
    for (var i = 0; i < len; i++)
      target[i + target_start] = this[i + start]
  } else {
    target._set(this.subarray(start, start + len), target_start)
  }
}

function _base64Slice (buf, start, end) {
  if (start === 0 && end === buf.length) {
    return base64.fromByteArray(buf)
  } else {
    return base64.fromByteArray(buf.slice(start, end))
  }
}

function _utf8Slice (buf, start, end) {
  var res = ''
  var tmp = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; i++) {
    if (buf[i] <= 0x7F) {
      res += decodeUtf8Char(tmp) + String.fromCharCode(buf[i])
      tmp = ''
    } else {
      tmp += '%' + buf[i].toString(16)
    }
  }

  return res + decodeUtf8Char(tmp)
}

function _asciiSlice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; i++)
    ret += String.fromCharCode(buf[i])
  return ret
}

function _binarySlice (buf, start, end) {
  return _asciiSlice(buf, start, end)
}

function _hexSlice (buf, start, end) {
  var len = buf.length

  if (!start || start < 0) start = 0
  if (!end || end < 0 || end > len) end = len

  var out = ''
  for (var i = start; i < end; i++) {
    out += toHex(buf[i])
  }
  return out
}

function _utf16leSlice (buf, start, end) {
  var bytes = buf.slice(start, end)
  var res = ''
  for (var i = 0; i < bytes.length; i += 2) {
    res += String.fromCharCode(bytes[i] + bytes[i+1] * 256)
  }
  return res
}

Buffer.prototype.slice = function (start, end) {
  var len = this.length
  start = clamp(start, len, 0)
  end = clamp(end, len, len)

  if (Buffer._useTypedArrays) {
    return Buffer._augment(this.subarray(start, end))
  } else {
    var sliceLen = end - start
    var newBuf = new Buffer(sliceLen, undefined, true)
    for (var i = 0; i < sliceLen; i++) {
      newBuf[i] = this[i + start]
    }
    return newBuf
  }
}

// `get` will be removed in Node 0.13+
Buffer.prototype.get = function (offset) {
  console.log('.get() is deprecated. Access using array indexes instead.')
  return this.readUInt8(offset)
}

// `set` will be removed in Node 0.13+
Buffer.prototype.set = function (v, offset) {
  console.log('.set() is deprecated. Access using array indexes instead.')
  return this.writeUInt8(v, offset)
}

Buffer.prototype.readUInt8 = function (offset, noAssert) {
  if (!noAssert) {
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset < this.length, 'Trying to read beyond buffer length')
  }

  if (offset >= this.length)
    return

  return this[offset]
}

function _readUInt16 (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 1 < buf.length, 'Trying to read beyond buffer length')
  }

  var len = buf.length
  if (offset >= len)
    return

  var val
  if (littleEndian) {
    val = buf[offset]
    if (offset + 1 < len)
      val |= buf[offset + 1] << 8
  } else {
    val = buf[offset] << 8
    if (offset + 1 < len)
      val |= buf[offset + 1]
  }
  return val
}

Buffer.prototype.readUInt16LE = function (offset, noAssert) {
  return _readUInt16(this, offset, true, noAssert)
}

Buffer.prototype.readUInt16BE = function (offset, noAssert) {
  return _readUInt16(this, offset, false, noAssert)
}

function _readUInt32 (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 3 < buf.length, 'Trying to read beyond buffer length')
  }

  var len = buf.length
  if (offset >= len)
    return

  var val
  if (littleEndian) {
    if (offset + 2 < len)
      val = buf[offset + 2] << 16
    if (offset + 1 < len)
      val |= buf[offset + 1] << 8
    val |= buf[offset]
    if (offset + 3 < len)
      val = val + (buf[offset + 3] << 24 >>> 0)
  } else {
    if (offset + 1 < len)
      val = buf[offset + 1] << 16
    if (offset + 2 < len)
      val |= buf[offset + 2] << 8
    if (offset + 3 < len)
      val |= buf[offset + 3]
    val = val + (buf[offset] << 24 >>> 0)
  }
  return val
}

Buffer.prototype.readUInt32LE = function (offset, noAssert) {
  return _readUInt32(this, offset, true, noAssert)
}

Buffer.prototype.readUInt32BE = function (offset, noAssert) {
  return _readUInt32(this, offset, false, noAssert)
}

Buffer.prototype.readInt8 = function (offset, noAssert) {
  if (!noAssert) {
    assert(offset !== undefined && offset !== null,
        'missing offset')
    assert(offset < this.length, 'Trying to read beyond buffer length')
  }

  if (offset >= this.length)
    return

  var neg = this[offset] & 0x80
  if (neg)
    return (0xff - this[offset] + 1) * -1
  else
    return this[offset]
}

function _readInt16 (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 1 < buf.length, 'Trying to read beyond buffer length')
  }

  var len = buf.length
  if (offset >= len)
    return

  var val = _readUInt16(buf, offset, littleEndian, true)
  var neg = val & 0x8000
  if (neg)
    return (0xffff - val + 1) * -1
  else
    return val
}

Buffer.prototype.readInt16LE = function (offset, noAssert) {
  return _readInt16(this, offset, true, noAssert)
}

Buffer.prototype.readInt16BE = function (offset, noAssert) {
  return _readInt16(this, offset, false, noAssert)
}

function _readInt32 (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 3 < buf.length, 'Trying to read beyond buffer length')
  }

  var len = buf.length
  if (offset >= len)
    return

  var val = _readUInt32(buf, offset, littleEndian, true)
  var neg = val & 0x80000000
  if (neg)
    return (0xffffffff - val + 1) * -1
  else
    return val
}

Buffer.prototype.readInt32LE = function (offset, noAssert) {
  return _readInt32(this, offset, true, noAssert)
}

Buffer.prototype.readInt32BE = function (offset, noAssert) {
  return _readInt32(this, offset, false, noAssert)
}

function _readFloat (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset + 3 < buf.length, 'Trying to read beyond buffer length')
  }

  return ieee754.read(buf, offset, littleEndian, 23, 4)
}

Buffer.prototype.readFloatLE = function (offset, noAssert) {
  return _readFloat(this, offset, true, noAssert)
}

Buffer.prototype.readFloatBE = function (offset, noAssert) {
  return _readFloat(this, offset, false, noAssert)
}

function _readDouble (buf, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset + 7 < buf.length, 'Trying to read beyond buffer length')
  }

  return ieee754.read(buf, offset, littleEndian, 52, 8)
}

Buffer.prototype.readDoubleLE = function (offset, noAssert) {
  return _readDouble(this, offset, true, noAssert)
}

Buffer.prototype.readDoubleBE = function (offset, noAssert) {
  return _readDouble(this, offset, false, noAssert)
}

Buffer.prototype.writeUInt8 = function (value, offset, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset < this.length, 'trying to write beyond buffer length')
    verifuint(value, 0xff)
  }

  if (offset >= this.length) return

  this[offset] = value
}

function _writeUInt16 (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 1 < buf.length, 'trying to write beyond buffer length')
    verifuint(value, 0xffff)
  }

  var len = buf.length
  if (offset >= len)
    return

  for (var i = 0, j = Math.min(len - offset, 2); i < j; i++) {
    buf[offset + i] =
        (value & (0xff << (8 * (littleEndian ? i : 1 - i)))) >>>
            (littleEndian ? i : 1 - i) * 8
  }
}

Buffer.prototype.writeUInt16LE = function (value, offset, noAssert) {
  _writeUInt16(this, value, offset, true, noAssert)
}

Buffer.prototype.writeUInt16BE = function (value, offset, noAssert) {
  _writeUInt16(this, value, offset, false, noAssert)
}

function _writeUInt32 (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 3 < buf.length, 'trying to write beyond buffer length')
    verifuint(value, 0xffffffff)
  }

  var len = buf.length
  if (offset >= len)
    return

  for (var i = 0, j = Math.min(len - offset, 4); i < j; i++) {
    buf[offset + i] =
        (value >>> (littleEndian ? i : 3 - i) * 8) & 0xff
  }
}

Buffer.prototype.writeUInt32LE = function (value, offset, noAssert) {
  _writeUInt32(this, value, offset, true, noAssert)
}

Buffer.prototype.writeUInt32BE = function (value, offset, noAssert) {
  _writeUInt32(this, value, offset, false, noAssert)
}

Buffer.prototype.writeInt8 = function (value, offset, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset < this.length, 'Trying to write beyond buffer length')
    verifsint(value, 0x7f, -0x80)
  }

  if (offset >= this.length)
    return

  if (value >= 0)
    this.writeUInt8(value, offset, noAssert)
  else
    this.writeUInt8(0xff + value + 1, offset, noAssert)
}

function _writeInt16 (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 1 < buf.length, 'Trying to write beyond buffer length')
    verifsint(value, 0x7fff, -0x8000)
  }

  var len = buf.length
  if (offset >= len)
    return

  if (value >= 0)
    _writeUInt16(buf, value, offset, littleEndian, noAssert)
  else
    _writeUInt16(buf, 0xffff + value + 1, offset, littleEndian, noAssert)
}

Buffer.prototype.writeInt16LE = function (value, offset, noAssert) {
  _writeInt16(this, value, offset, true, noAssert)
}

Buffer.prototype.writeInt16BE = function (value, offset, noAssert) {
  _writeInt16(this, value, offset, false, noAssert)
}

function _writeInt32 (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 3 < buf.length, 'Trying to write beyond buffer length')
    verifsint(value, 0x7fffffff, -0x80000000)
  }

  var len = buf.length
  if (offset >= len)
    return

  if (value >= 0)
    _writeUInt32(buf, value, offset, littleEndian, noAssert)
  else
    _writeUInt32(buf, 0xffffffff + value + 1, offset, littleEndian, noAssert)
}

Buffer.prototype.writeInt32LE = function (value, offset, noAssert) {
  _writeInt32(this, value, offset, true, noAssert)
}

Buffer.prototype.writeInt32BE = function (value, offset, noAssert) {
  _writeInt32(this, value, offset, false, noAssert)
}

function _writeFloat (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 3 < buf.length, 'Trying to write beyond buffer length')
    verifIEEE754(value, 3.4028234663852886e+38, -3.4028234663852886e+38)
  }

  var len = buf.length
  if (offset >= len)
    return

  ieee754.write(buf, value, offset, littleEndian, 23, 4)
}

Buffer.prototype.writeFloatLE = function (value, offset, noAssert) {
  _writeFloat(this, value, offset, true, noAssert)
}

Buffer.prototype.writeFloatBE = function (value, offset, noAssert) {
  _writeFloat(this, value, offset, false, noAssert)
}

function _writeDouble (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    assert(value !== undefined && value !== null, 'missing value')
    assert(typeof littleEndian === 'boolean', 'missing or invalid endian')
    assert(offset !== undefined && offset !== null, 'missing offset')
    assert(offset + 7 < buf.length,
        'Trying to write beyond buffer length')
    verifIEEE754(value, 1.7976931348623157E+308, -1.7976931348623157E+308)
  }

  var len = buf.length
  if (offset >= len)
    return

  ieee754.write(buf, value, offset, littleEndian, 52, 8)
}

Buffer.prototype.writeDoubleLE = function (value, offset, noAssert) {
  _writeDouble(this, value, offset, true, noAssert)
}

Buffer.prototype.writeDoubleBE = function (value, offset, noAssert) {
  _writeDouble(this, value, offset, false, noAssert)
}

// fill(value, start=0, end=buffer.length)
Buffer.prototype.fill = function (value, start, end) {
  if (!value) value = 0
  if (!start) start = 0
  if (!end) end = this.length

  if (typeof value === 'string') {
    value = value.charCodeAt(0)
  }

  assert(typeof value === 'number' && !isNaN(value), 'value is not a number')
  assert(end >= start, 'end < start')

  // Fill 0 bytes; we're done
  if (end === start) return
  if (this.length === 0) return

  assert(start >= 0 && start < this.length, 'start out of bounds')
  assert(end >= 0 && end <= this.length, 'end out of bounds')

  for (var i = start; i < end; i++) {
    this[i] = value
  }
}

Buffer.prototype.inspect = function () {
  var out = []
  var len = this.length
  for (var i = 0; i < len; i++) {
    out[i] = toHex(this[i])
    if (i === exports.INSPECT_MAX_BYTES) {
      out[i + 1] = '...'
      break
    }
  }
  return '<Buffer ' + out.join(' ') + '>'
}

/**
 * Creates a new `ArrayBuffer` with the *copied* memory of the buffer instance.
 * Added in Node 0.12. Only available in browsers that support ArrayBuffer.
 */
Buffer.prototype.toArrayBuffer = function () {
  if (typeof Uint8Array !== 'undefined') {
    if (Buffer._useTypedArrays) {
      return (new Buffer(this)).buffer
    } else {
      var buf = new Uint8Array(this.length)
      for (var i = 0, len = buf.length; i < len; i += 1)
        buf[i] = this[i]
      return buf.buffer
    }
  } else {
    throw new Error('Buffer.toArrayBuffer not supported in this browser')
  }
}

// HELPER FUNCTIONS
// ================

function stringtrim (str) {
  if (str.trim) return str.trim()
  return str.replace(/^\s+|\s+$/g, '')
}

var BP = Buffer.prototype

/**
 * Augment a Uint8Array *instance* (not the Uint8Array class!) with Buffer methods
 */
Buffer._augment = function (arr) {
  arr._isBuffer = true

  // save reference to original Uint8Array get/set methods before overwriting
  arr._get = arr.get
  arr._set = arr.set

  // deprecated, will be removed in node 0.13+
  arr.get = BP.get
  arr.set = BP.set

  arr.write = BP.write
  arr.toString = BP.toString
  arr.toLocaleString = BP.toString
  arr.toJSON = BP.toJSON
  arr.copy = BP.copy
  arr.slice = BP.slice
  arr.readUInt8 = BP.readUInt8
  arr.readUInt16LE = BP.readUInt16LE
  arr.readUInt16BE = BP.readUInt16BE
  arr.readUInt32LE = BP.readUInt32LE
  arr.readUInt32BE = BP.readUInt32BE
  arr.readInt8 = BP.readInt8
  arr.readInt16LE = BP.readInt16LE
  arr.readInt16BE = BP.readInt16BE
  arr.readInt32LE = BP.readInt32LE
  arr.readInt32BE = BP.readInt32BE
  arr.readFloatLE = BP.readFloatLE
  arr.readFloatBE = BP.readFloatBE
  arr.readDoubleLE = BP.readDoubleLE
  arr.readDoubleBE = BP.readDoubleBE
  arr.writeUInt8 = BP.writeUInt8
  arr.writeUInt16LE = BP.writeUInt16LE
  arr.writeUInt16BE = BP.writeUInt16BE
  arr.writeUInt32LE = BP.writeUInt32LE
  arr.writeUInt32BE = BP.writeUInt32BE
  arr.writeInt8 = BP.writeInt8
  arr.writeInt16LE = BP.writeInt16LE
  arr.writeInt16BE = BP.writeInt16BE
  arr.writeInt32LE = BP.writeInt32LE
  arr.writeInt32BE = BP.writeInt32BE
  arr.writeFloatLE = BP.writeFloatLE
  arr.writeFloatBE = BP.writeFloatBE
  arr.writeDoubleLE = BP.writeDoubleLE
  arr.writeDoubleBE = BP.writeDoubleBE
  arr.fill = BP.fill
  arr.inspect = BP.inspect
  arr.toArrayBuffer = BP.toArrayBuffer

  return arr
}

// slice(start, end)
function clamp (index, len, defaultValue) {
  if (typeof index !== 'number') return defaultValue
  index = ~~index;  // Coerce to integer.
  if (index >= len) return len
  if (index >= 0) return index
  index += len
  if (index >= 0) return index
  return 0
}

function coerce (length) {
  // Coerce length to a number (possibly NaN), round up
  // in case it's fractional (e.g. 123.456) then do a
  // double negate to coerce a NaN to 0. Easy, right?
  length = ~~Math.ceil(+length)
  return length < 0 ? 0 : length
}

function isArray (subject) {
  return (Array.isArray || function (subject) {
    return Object.prototype.toString.call(subject) === '[object Array]'
  })(subject)
}

function isArrayish (subject) {
  return isArray(subject) || Buffer.isBuffer(subject) ||
      subject && typeof subject === 'object' &&
      typeof subject.length === 'number'
}

function toHex (n) {
  if (n < 16) return '0' + n.toString(16)
  return n.toString(16)
}

function utf8ToBytes (str) {
  var byteArray = []
  for (var i = 0; i < str.length; i++) {
    var b = str.charCodeAt(i)
    if (b <= 0x7F)
      byteArray.push(str.charCodeAt(i))
    else {
      var start = i
      if (b >= 0xD800 && b <= 0xDFFF) i++
      var h = encodeURIComponent(str.slice(start, i+1)).substr(1).split('%')
      for (var j = 0; j < h.length; j++)
        byteArray.push(parseInt(h[j], 16))
    }
  }
  return byteArray
}

function asciiToBytes (str) {
  var byteArray = []
  for (var i = 0; i < str.length; i++) {
    // Node's code seems to be doing this and not & 0x7F..
    byteArray.push(str.charCodeAt(i) & 0xFF)
  }
  return byteArray
}

function utf16leToBytes (str) {
  var c, hi, lo
  var byteArray = []
  for (var i = 0; i < str.length; i++) {
    c = str.charCodeAt(i)
    hi = c >> 8
    lo = c % 256
    byteArray.push(lo)
    byteArray.push(hi)
  }

  return byteArray
}

function base64ToBytes (str) {
  return base64.toByteArray(str)
}

function blitBuffer (src, dst, offset, length) {
  var pos
  for (var i = 0; i < length; i++) {
    if ((i + offset >= dst.length) || (i >= src.length))
      break
    dst[i + offset] = src[i]
  }
  return i
}

function decodeUtf8Char (str) {
  try {
    return decodeURIComponent(str)
  } catch (err) {
    return String.fromCharCode(0xFFFD) // UTF 8 invalid char
  }
}

/*
 * We have to make sure that the value is a valid integer. This means that it
 * is non-negative. It has no fractional component and that it does not
 * exceed the maximum allowed value.
 */
function verifuint (value, max) {
  assert(typeof value === 'number', 'cannot write a non-number as a number')
  assert(value >= 0, 'specified a negative value for writing an unsigned value')
  assert(value <= max, 'value is larger than maximum value for type')
  assert(Math.floor(value) === value, 'value has a fractional component')
}

function verifsint (value, max, min) {
  assert(typeof value === 'number', 'cannot write a non-number as a number')
  assert(value <= max, 'value larger than maximum allowed value')
  assert(value >= min, 'value smaller than minimum allowed value')
  assert(Math.floor(value) === value, 'value has a fractional component')
}

function verifIEEE754 (value, max, min) {
  assert(typeof value === 'number', 'cannot write a non-number as a number')
  assert(value <= max, 'value larger than maximum allowed value')
  assert(value >= min, 'value smaller than minimum allowed value')
}

function assert (test, message) {
  if (!test) throw new Error(message || 'Failed assertion')
}

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer/index.js","/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer")
},{"+7ZJp0":5,"base64-js":3,"buffer":2,"ieee754":4}],3:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var lookup = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

;(function (exports) {
	'use strict';

  var Arr = (typeof Uint8Array !== 'undefined')
    ? Uint8Array
    : Array

	var PLUS   = '+'.charCodeAt(0)
	var SLASH  = '/'.charCodeAt(0)
	var NUMBER = '0'.charCodeAt(0)
	var LOWER  = 'a'.charCodeAt(0)
	var UPPER  = 'A'.charCodeAt(0)
	var PLUS_URL_SAFE = '-'.charCodeAt(0)
	var SLASH_URL_SAFE = '_'.charCodeAt(0)

	function decode (elt) {
		var code = elt.charCodeAt(0)
		if (code === PLUS ||
		    code === PLUS_URL_SAFE)
			return 62 // '+'
		if (code === SLASH ||
		    code === SLASH_URL_SAFE)
			return 63 // '/'
		if (code < NUMBER)
			return -1 //no match
		if (code < NUMBER + 10)
			return code - NUMBER + 26 + 26
		if (code < UPPER + 26)
			return code - UPPER
		if (code < LOWER + 26)
			return code - LOWER + 26
	}

	function b64ToByteArray (b64) {
		var i, j, l, tmp, placeHolders, arr

		if (b64.length % 4 > 0) {
			throw new Error('Invalid string. Length must be a multiple of 4')
		}

		// the number of equal signs (place holders)
		// if there are two placeholders, than the two characters before it
		// represent one byte
		// if there is only one, then the three characters before it represent 2 bytes
		// this is just a cheap hack to not do indexOf twice
		var len = b64.length
		placeHolders = '=' === b64.charAt(len - 2) ? 2 : '=' === b64.charAt(len - 1) ? 1 : 0

		// base64 is 4/3 + up to two characters of the original data
		arr = new Arr(b64.length * 3 / 4 - placeHolders)

		// if there are placeholders, only get up to the last complete 4 chars
		l = placeHolders > 0 ? b64.length - 4 : b64.length

		var L = 0

		function push (v) {
			arr[L++] = v
		}

		for (i = 0, j = 0; i < l; i += 4, j += 3) {
			tmp = (decode(b64.charAt(i)) << 18) | (decode(b64.charAt(i + 1)) << 12) | (decode(b64.charAt(i + 2)) << 6) | decode(b64.charAt(i + 3))
			push((tmp & 0xFF0000) >> 16)
			push((tmp & 0xFF00) >> 8)
			push(tmp & 0xFF)
		}

		if (placeHolders === 2) {
			tmp = (decode(b64.charAt(i)) << 2) | (decode(b64.charAt(i + 1)) >> 4)
			push(tmp & 0xFF)
		} else if (placeHolders === 1) {
			tmp = (decode(b64.charAt(i)) << 10) | (decode(b64.charAt(i + 1)) << 4) | (decode(b64.charAt(i + 2)) >> 2)
			push((tmp >> 8) & 0xFF)
			push(tmp & 0xFF)
		}

		return arr
	}

	function uint8ToBase64 (uint8) {
		var i,
			extraBytes = uint8.length % 3, // if we have 1 byte left, pad 2 bytes
			output = "",
			temp, length

		function encode (num) {
			return lookup.charAt(num)
		}

		function tripletToBase64 (num) {
			return encode(num >> 18 & 0x3F) + encode(num >> 12 & 0x3F) + encode(num >> 6 & 0x3F) + encode(num & 0x3F)
		}

		// go through the array every three bytes, we'll deal with trailing stuff later
		for (i = 0, length = uint8.length - extraBytes; i < length; i += 3) {
			temp = (uint8[i] << 16) + (uint8[i + 1] << 8) + (uint8[i + 2])
			output += tripletToBase64(temp)
		}

		// pad the end with zeros, but make sure to not forget the extra bytes
		switch (extraBytes) {
			case 1:
				temp = uint8[uint8.length - 1]
				output += encode(temp >> 2)
				output += encode((temp << 4) & 0x3F)
				output += '=='
				break
			case 2:
				temp = (uint8[uint8.length - 2] << 8) + (uint8[uint8.length - 1])
				output += encode(temp >> 10)
				output += encode((temp >> 4) & 0x3F)
				output += encode((temp << 2) & 0x3F)
				output += '='
				break
		}

		return output
	}

	exports.toByteArray = b64ToByteArray
	exports.fromByteArray = uint8ToBase64
}(typeof exports === 'undefined' ? (this.base64js = {}) : exports))

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer/node_modules/base64-js/lib/b64.js","/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer/node_modules/base64-js/lib")
},{"+7ZJp0":5,"buffer":2}],4:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
exports.read = function(buffer, offset, isLE, mLen, nBytes) {
  var e, m,
      eLen = nBytes * 8 - mLen - 1,
      eMax = (1 << eLen) - 1,
      eBias = eMax >> 1,
      nBits = -7,
      i = isLE ? (nBytes - 1) : 0,
      d = isLE ? -1 : 1,
      s = buffer[offset + i];

  i += d;

  e = s & ((1 << (-nBits)) - 1);
  s >>= (-nBits);
  nBits += eLen;
  for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8);

  m = e & ((1 << (-nBits)) - 1);
  e >>= (-nBits);
  nBits += mLen;
  for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8);

  if (e === 0) {
    e = 1 - eBias;
  } else if (e === eMax) {
    return m ? NaN : ((s ? -1 : 1) * Infinity);
  } else {
    m = m + Math.pow(2, mLen);
    e = e - eBias;
  }
  return (s ? -1 : 1) * m * Math.pow(2, e - mLen);
};

exports.write = function(buffer, value, offset, isLE, mLen, nBytes) {
  var e, m, c,
      eLen = nBytes * 8 - mLen - 1,
      eMax = (1 << eLen) - 1,
      eBias = eMax >> 1,
      rt = (mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0),
      i = isLE ? 0 : (nBytes - 1),
      d = isLE ? 1 : -1,
      s = value < 0 || (value === 0 && 1 / value < 0) ? 1 : 0;

  value = Math.abs(value);

  if (isNaN(value) || value === Infinity) {
    m = isNaN(value) ? 1 : 0;
    e = eMax;
  } else {
    e = Math.floor(Math.log(value) / Math.LN2);
    if (value * (c = Math.pow(2, -e)) < 1) {
      e--;
      c *= 2;
    }
    if (e + eBias >= 1) {
      value += rt / c;
    } else {
      value += rt * Math.pow(2, 1 - eBias);
    }
    if (value * c >= 2) {
      e++;
      c /= 2;
    }

    if (e + eBias >= eMax) {
      m = 0;
      e = eMax;
    } else if (e + eBias >= 1) {
      m = (value * c - 1) * Math.pow(2, mLen);
      e = e + eBias;
    } else {
      m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen);
      e = 0;
    }
  }

  for (; mLen >= 8; buffer[offset + i] = m & 0xff, i += d, m /= 256, mLen -= 8);

  e = (e << mLen) | m;
  eLen += mLen;
  for (; eLen > 0; buffer[offset + i] = e & 0xff, i += d, e /= 256, eLen -= 8);

  buffer[offset + i - d] |= s * 128;
};

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer/node_modules/ieee754/index.js","/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/buffer/node_modules/ieee754")
},{"+7ZJp0":5,"buffer":2}],5:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
// shim for using process in browser

var process = module.exports = {};

process.nextTick = (function () {
    var canSetImmediate = typeof window !== 'undefined'
    && window.setImmediate;
    var canPost = typeof window !== 'undefined'
    && window.postMessage && window.addEventListener
    ;

    if (canSetImmediate) {
        return function (f) { return window.setImmediate(f) };
    }

    if (canPost) {
        var queue = [];
        window.addEventListener('message', function (ev) {
            var source = ev.source;
            if ((source === window || source === null) && ev.data === 'process-tick') {
                ev.stopPropagation();
                if (queue.length > 0) {
                    var fn = queue.shift();
                    fn();
                }
            }
        }, true);

        return function nextTick(fn) {
            queue.push(fn);
            window.postMessage('process-tick', '*');
        };
    }

    return function nextTick(fn) {
        setTimeout(fn, 0);
    };
})();

process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;

process.binding = function (name) {
    throw new Error('process.binding is not supported');
}

// TODO(shtylman)
process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/process/browser.js","/../../../node_modules/gulp-browserify/node_modules/browserify/node_modules/process")
},{"+7ZJp0":5,"buffer":2}],6:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var Bacon = require("baconjs")

module.exports = function(listApp, isTestMode) {
  listApp.factory("angularBacon", function () {
    return {
      watch: function(scope, expression) {
        var stream = new Bacon.Bus()
        scope.$watch(expression, function(val, prevVal) { if (val !== prevVal) stream.push(val) })
        return stream
      },

      resource: function(resource) {
        return function(queryParams, body) {
          return Bacon.fromNodeCallback(function (callback) {
            resource(queryParams, body, success, error)

            function success(value) {
              callback(null, value)
            }

            function error(response) {
              callback(response)
            }
          })
        }
      }
    }
  })
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/angularBacon.js","/")
},{"+7ZJp0":5,"baconjs":1,"buffer":2}],7:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var Question = require('./question').Question
var QuestionGroup = require('./question').QuestionGroup

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {

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

      var responsePromise = $http.post("/omatsivut/secure/applications/validate/" + application.oid, application.toJson())
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
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/applicationValidator.js","/")
},{"+7ZJp0":5,"./question":28,"buffer":2}],8:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var Hakemus = require("../hakemus")
var Hakutoive = require("../hakutoive")
var util = require("../util")
var Question = require("../question").Question

module.exports = function(listApp) {
  listApp.directive("application", ["$http", "$sce", "restResources", "applicationValidator", "settings", "debounce", "localization", "$timeout", function ($http, $sce, restResources, applicationValidator, settings, debounce, localization, $timeout) {
    return {
      restrict: 'E',
      scope: {
        application: "=application"
      },
      templateUrl: 'templates/application.html',

      link: function ($scope, $element, attrs) {
        $scope.localization = localization
        var applicationValidatorBounced = debounce(applicationValidator(), settings.modelDebounce)
        $scope.isSaveable = true
        $scope.isValidating = false

        $scope.timestampLabel = function() {
          if ($scope.application.received == $scope.application.updated)
            return localization("label.applicationReceived")
          else
            return localization("label.applicationUpdated")
        }

        $scope.shouldSaveButtonBeDisabled = function() {
          return $scope.applicationForm.$pristine || $scope.statusMessageType=='pending' || !$scope.isSaveable || $scope.isValidating
        }

        $scope.statusMessageStyleModifier = function() {
          return {'ajax-spinner': $scope.statusMessageType=='pending', error: $scope.statusMessageType=='error'}
        }

        function getHakutoiveet() {
          return _($scope.application.hakutoiveet).map(function(hakutoive) {
            return {
              "Koulutus": hakutoive.data["Koulutus"],
              "Koulutus-id": hakutoive.data["Koulutus-id"],
              "Opetuspiste": hakutoive.data["Opetuspiste"],
              "Opetuspiste-id": hakutoive.data["Opetuspiste-id"]
            }
          })
        }

        function getAnswers() {
          var answersToAdditionalQuestions =  _(Question.questionMap($scope.application.additionalQuestions)).map(function(item, key) { return item.answer })
          var otherAnswers = _($scope.application.henkilotiedot).map(function(item) { return item.answer })
          return answersToAdditionalQuestions.concat(otherAnswers)
        }

        $scope.$watch(getHakutoiveet, function(hakutoiveet, oldHakutoiveet) {
          // Skip initial values angular style
          if (!_.isEqual(hakutoiveet, oldHakutoiveet)) {
            applicationChanged()
            validateHakutoiveet(true)
          }
        }, true)

        $scope.$watch(getAnswers, function(answers, oldAnswers) {
          if (!_.isEqual(oldAnswers, answers)) {
            applicationChanged()
          }
        }, true)

        $scope.$on("questionAnswered", function() {
          validateHakutoiveet(false)
        })

        $scope.hakutoiveVastaanotettu = function(hakutoive, updated) {
          $scope.application.mergeSavedApplication(updated.hakemus)
          $timeout(function() {
            $scope.$broadcast("hakutoive-vastaanotettu", hakutoive)
          }, 0)
        }

        function applicationChanged() {
          $scope.applicationForm.$setDirty()
          if ($scope.statusMessageType == "success")
            setStatusMessage("")
        }

        function validateHakutoiveet(skipQuestions) {
          applicationValidatorBounced($scope.application, beforeBackendValidation, success, error)

          function beforeBackendValidation() {
            setValidatingIndicator(true)
          }

          function success(data) {
            setStatusMessage(localization("message.validationOk"), "info")
            $scope.isSaveable = true
            setValidatingIndicator(false)
            $scope.application.importQuestions(data.questions)
            $scope.application.importHakuajat(data.response.hakemus.hakutoiveet)
            updateValidationMessages([], skipQuestions)
          }

          function error(data) {
            setValidatingIndicator(false)
            if (!data.statusCode) { // validointi epäonnistui frontendissä
              $scope.isSaveable = false
              setStatusMessage(localization("error.validationFailed"), "error")
            } else if (data.statusCode === 200) {
              $scope.isSaveable = !Hakutoive.hasHakutoiveErrors(data.errors)
              setStatusMessage(localization("error.validationFailed"), "error")
            } else if (data.statusCode == 401) {
              $scope.isSaveable = true
              setStatusMessage(localization("error.sessionExpired"), "error")
            } else if (data.statusCode == 500) {
              $scope.isSaveable = true
              setStatusMessage(localization("error.serverError"), "error")
            } else {
              $scope.isSaveable = false
              setStatusMessage(localization("error.validationFailed_httpError"), "error")
            }

            var updateQuestions = data.questions != null && !Hakutoive.hasHakutoiveErrors(data.errors)

            if (updateQuestions) // frontside validation does not include questions -> don't update
              $scope.application.importQuestions(data.questions)

            if (data.response != null && data.response.hakemus != null)
              $scope.application.importHakuajat(data.response.hakemus.hakutoiveet)

            updateValidationMessages(data.errors, skipQuestions)
          }
        }

        $scope.preferenceMoved = function() {
          setStatusMessage("")
        }

        function setStatusMessage(msg, type) {
          $scope.statusMessage = msg
          $scope.statusMessageType = type || ""
        }

        var setValidatingIndicator = debounce(function(isVisible) {
          $scope.isValidating = isVisible
        }, settings.uiIndicatorDebounce)

        $scope.saveApplication = function() {
          restResources.applications.update({id: $scope.application.oid }, $scope.application.toJson(), onSuccess, onError)
          setStatusMessage("", "pending")

          function onSuccess(savedApplication) {
            highlightSavedItems($scope.application.getChangedPreferences())
            $scope.$broadcast("show-callout", "attachments", savedApplication.requiresAdditionalInfo === true && $scope.application.getChangedPreferences().length > 0)
            $scope.application.mergeSavedApplication(savedApplication)
            $scope.applicationForm.$setPristine()
            setStatusMessage(localization("message.changesSaved"), "success")
            updateValidationMessages([])
            if($scope.application.editHakutoiveetEnabled()) scrollToTop()
          }

          function onError(err) {
            var saveError = (function() {
              if (err.status == 400 && (_.isArray(err.data) && err.data.length > 0))
                return "error.saveFailed_validationError"
              else if (err.status == 400 && !(_.isArray(err.data) && err.data.length > 0))
                return "error.serverError"
              else if (err.status == 401)
                return "error.saveFailed_sessionExpired"
              else if (err.status == 500)
                return "error.serverError"
              else
                return "error.saveFailed"
            })()

            setStatusMessage(localization(saveError), "error")
            if (err.status == 400) // Validointivirhe
              updateValidationMessages(err.data)
          }
        }

        function highlightSavedItems(indexes) {
          var items = $element.find(".preference-list-item")

          _.each(indexes, function(index) {
            items.eq(index).addClass("saved")
          })

          $element.find(".timestamp-row").addClass("saved")
          $element.find(".yhteystiedot .ng-dirty").addClass("saved") // edited contact details

          window.setTimeout(function() {
            $element.find(".saved").removeClass("saved")
            $(".timestamp-row").removeClass("saved")
          }, 3000)
        }

        function scrollToTop() {
          var $applicationHeader = $($element[0]).find(".application-header")
          if ($applicationHeader.get(0) != null && $(window).scrollTop() > $applicationHeader.get(0).offsetTop) $applicationHeader.get(0).scrollIntoView()
        }

        function updateValidationMessages(errors, skipQuestions) {
          var unhandledMessages = $scope.application.updateValidationMessages(errors, skipQuestions)
          unhandledMessages = hideErrorIfAlreadyShowsKoulutusError(unhandledMessages)

          if (unhandledMessages.length > 0) {
            _(unhandledMessages).each(function(item) {
              console.log("Validaatiovirhettä ei käsitelty:", item.questionId, item.errors)
            })

            setStatusMessage(localization("error.serverError"), "error")
          }

          function hideErrorIfAlreadyShowsKoulutusError(messages) {
            return _(messages).filter(function(message) {
              var index = Hakutoive.parseHakutoiveIndex(message.questionId)
              var relatedErrorShown = _(errors).any(function(error) {
                return Hakutoive.isHakutoiveError(error.key) && Hakutoive.parseHakutoiveIndex(error.key) == index
              })
              return !relatedErrorShown
            })
          }
        }
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/application.js","/directives")
},{"+7ZJp0":5,"../hakemus":24,"../hakutoive":27,"../question":28,"../util":29,"buffer":2}],9:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var util = require("../util")

module.exports = function(listApp) {
  listApp.directive("applicationPeriods", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        haku: '&haku'
      },
      templateUrl: 'templates/applicationPeriods.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization
        $scope.periods = function() { return $scope.haku().applicationPeriods }

        $scope.applicationPeriodString = function(index) {
          if ($scope.periods().length === 1)
            return localization("label.applicationPeriod")
          else
            return (index+1) + ". " + localization("label.applicationPeriod").toLowerCase()
        }

        $scope.statusString = function(period) {
          if (period.active)
            return localization("label.applicationPeriodActive")
          else if (period.end < new Date().getTime())
            return localization("label.applicationPeriodPassed")
          else
            return localization("label.applicationPeriodNotStarted")
        }
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/applicationPeriods.js","/directives")
},{"+7ZJp0":5,"../util":29,"buffer":2}],10:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("callout", function () {
    return {
      link: function (scope, element, attrs) {
        element.addClass("callout")
        element.prepend($("<div/>", {class: "callout-close"}))
        element.on("click", ".callout-close", function () {
          element.fadeOut("fast")
        })
        scope.$on("show-callout", function (evt, calloutId, toggle) {
          if (attrs.callout == calloutId) {
            if (toggle)
              element.fadeIn("fast")
            else
              element.fadeOut("fast")
          }
        })
      }
    }
  })
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/callout.js","/directives")
},{"+7ZJp0":5,"buffer":2}],11:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("clearableInput", function ($parse) {
    return {
      link: function (scope, element, attrs) {
        var wrapper = $("<div/>", { class: "clearable-input" })
        var clearBtn = $("<div/>", { class: "clear-btn" })
        var container = element.wrap(wrapper).parent()
        container.prepend(clearBtn)

        clearBtn.on("click", function() {
          scope.$apply(function() {
            var model = $parse(attrs.ngModel)
            model.assign(scope, "")
          })
        })

        scope.$watch(attrs.ngModel, function(val) {
          clearBtn.toggle(val && (val.length > 0))
        })
      }
    }
  })
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/clearableInput.js","/directives")
},{"+7ZJp0":5,"buffer":2}],12:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("confirm", function () {
    return {
      scope: {
        callback: '&confirmAction'
      },
      link: function (scope, element, attrs) {
        var originalText = ""

        function cancel() {
          element.removeClass("confirm")
          element.text(originalText)
          element.off(".cancelConfirm")
          $("body").off(".cancelConfirm")
        }

        element.on("click", function () {
          if (element.hasClass("confirm")) {
            scope.$apply(scope.callback)
            cancel()
          } else {
            element.hide()
            element.addClass("confirm")
            originalText = element.text()
            element.text(attrs.confirmText)
            $("body").one("click.cancelConfirm", cancel)
            element.fadeIn(100)
          }
          return false
        })
      }
    }
  })
}

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/confirm.js","/directives")
},{"+7ZJp0":5,"buffer":2}],13:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("disableClickFocus", function() {
    return {
      link: function (scope, element) {
        element.on("mousedown", function(event) {
          event.preventDefault()
        })
      }
    }
  })
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/disableClickFocus.js","/directives")
},{"+7ZJp0":5,"buffer":2}],14:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("formattedTime", ["$sce", "localization", function ($sce, localization) {
    return {
      restrict: "A",
      template: "",
      scope: {
        formattedTime: "=formattedTime"
      },
      link: function (scope, element, attrs) {
        scope.$watch("formattedTime", function(dt) {
          if (dt == null) {
            return ""
          } else {
            var m = moment(dt)
            element.attr("datetime", m.format())
            element.text(m.format(attrs.format || 'LLLL').replace(/,/g, ""))
          }
        })
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/formattedTime.js","/directives")
},{"+7ZJp0":5,"buffer":2}],15:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: {
        applicationOid: '&applicationOid',
        haku: '&haku',
        hakutoiveet: '&hakutoiveet',
        callback: '=callback'
      },
      templateUrl: 'templates/hakutoiveenVastaanotto.html',
      link: function (scope, element, attrs) {
        scope.vastaanottotila = ""
        scope.localization = localization
        scope.ajaxPending = false
        scope.error = ""

        scope.formatTimestamp = function(dt) {
            return moment(dt).format('LLL').replace(/,/g, "")
        }

        scope.vastaanotaHakutoive = function(hakutoive) {
          var scope = this
          scope.error = ""
          scope.ajaxPending = true

          restResources.vastaanota.post({applicationOid: scope.applicationOid(), hakuOid: scope.haku().oid}, {
            hakukohdeOid: hakutoive.koulutus.oid,
            tila: this.vastaanottotila
          }, onSuccess, onError)

          function onSuccess(updatedApplication) {
            scope.ajaxPending = false
            scope.error = ""
            scope.callback(hakutoive, updatedApplication)
          }

          function onError(err) {
            var saveError = (function() {
             if (err.status == 401)
                return "error.saveFailed_sessionExpired"
              else if (err.status == 500)
                return "error.serverError"
              else
                return "error.saveFailed"
            })()
            scope.error = localization(saveError)
            scope.ajaxPending = false
          }
        }
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/hakutoiveenVastaanotto.js","/directives")
},{"+7ZJp0":5,"buffer":2}],16:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var Hakutoive = require('../hakutoive')

module.exports = function(listApp) {
  listApp.directive("hakutoiveet", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        application: "=application",
        preferenceMoved: "=preferenceMoved",
        validating: "=validating"
      },
      templateUrl: 'templates/hakutoiveet.html',

      link: function ($scope, element, attrs) {
        $scope.localization = localization

        $scope.movePreference = function(from, to) {
          if (to >= 0 && to < this.application.hakutoiveet.length) {
            this.application.movePreference(from, to)
            this.preferenceMoved()
          }
        }
      }
    }
  }])

  listApp.controller("additionalQuestionController", ["$scope", "localization", function($scope, localization) {
    $scope.localization = localization
    $scope.questionAnswered = function() {
      $scope.$emit("questionAnswered")
    }
  }])

  listApp.controller("hakutoiveController", ["$scope", "$http", "$timeout", "settings", "restResources", function($scope, $http, $timeout, settings, restResources) {
    $scope.isEditingDisabled = function() { return !$scope.hakutoive.isNew || !$scope.application.isEditable($scope.$index) }

    $scope.isKoulutusSelectable = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && !_.isEmpty($scope.koulutusList) }

    $scope.isLoadingKoulutusList = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && _.isEmpty($scope.koulutusList) }

    $scope.isNonPrioritizedAndEditable = function () { return this.application.haku.usePriority || $scope.application.isEditable($scope.$index) }

    $scope.opetuspisteValittu = function($item, $model, $label) {
      this.hakutoive.setOpetuspiste($item.id, $item.name)
      $scope.koulutusList = []

      restResources.koulutukset.query({
        asId: this.application.haku.oid,
        opetuspisteId: $item.id,
        baseEducation: this.application.educationBackground.baseEducation,
        vocational: this.application.educationBackground.vocational,
        uiLang: $scope.localization("languageId")
      }, function(koulutukset) {
        $scope.koulutusList = koulutukset
        if (koulutukset.length === 1) {
          $scope.valittuKoulutus = koulutukset[0]
          $scope.hakutoive.setKoulutus(koulutukset[0])
        }
      })
    }

    $scope.opetuspisteModified = function() {
      if (_.isEmpty(this.hakutoive.data.Opetuspiste))
        this.hakutoive.clear()
      else
        this.hakutoive.removeOpetuspisteData()
    }

    $scope.removeHakutoive = function(index) {
      $scope.application.removePreference(index)
      $scope.application.addPreference(new Hakutoive({}))
    }

    $scope.canRemovePreference = function(index) {
      return $scope.application.hasPreference(index) && !$scope.application.preferenceLocked(index)
    }

    $scope.koulutusValittu = function(index) {
      this.hakutoive.setKoulutus(this["valittuKoulutus"])
    }

    $scope.findOpetuspiste = function(val) {
      return restResources.opetuspisteet.query({
        query: val,
        asId: $scope.application.haku.oid,
        lang: $scope.localization("languageId")
      }).$promise
    };
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/hakutoiveet.js","/directives")
},{"+7ZJp0":5,"../hakutoive":27,"buffer":2}],17:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("henkilotiedot", ["localization", "restResources", "debounce", "settings", "angularBacon", function (localization, restResources, debounce, settings, angularBacon) {
    return {
      restrict: 'E',
      scope: {
        application: '=application'
      },
      templateUrl: 'templates/henkilotiedot.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization
        $scope.yhteystiedot = $scope.application.henkilotiedot

        var postalCode = angularBacon.watch($scope, "yhteystiedot['Postinumero'].answer").debounce(settings.modelDebounce).skipDuplicates().map(".trim")
        var query = angularBacon.resource(restResources.postOffice.get)

        function length(len) { return function(str) { return str.length === len } }
        function not(f) { return function(val) { return !f(val) }}

        var responses = postalCode.filter(length(5))
          .flatMapLatest(function(code) {
            return query({postalCode: code })
          })

        responses.onValue(function(response) {
          $scope.application.calculatedValues.postOffice = response.postOffice
          $scope.yhteystiedot.Postinumero.setErrors([])
        })

        responses.onError(function() {
          $scope.application.calculatedValues.postOffice = ""
        })

        postalCode.filter(not(length(5))).onValue(function() {
          $scope.$apply(function() {
            $scope.application.calculatedValues.postOffice = ""
          })
        })
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/henkilotiedot.js","/directives")
},{"+7ZJp0":5,"buffer":2}],18:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("ilmoittautuminen", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet'
      },
      templateUrl: 'templates/ilmoittautuminen.html',
      link: function (scope, element, attrs) {
        scope.localization = localization
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/ilmoittautuminen.js","/directives")
},{"+7ZJp0":5,"buffer":2}],19:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("localizedLink", ["$sce", "localization", function ($sce, localization) {
    return {
      restrict: "E",
      template: "",
      link: function (scope, element, attrs) {
        var linkString = localization(attrs["key"])
        var link = $("<span>" + linkString + "</span>")
          .find("a")
          .attr("href", attrs["href"])
          .attr("target", attrs["target"])
          .end()
        element.append(link)
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/localizedLink.js","/directives")
},{"+7ZJp0":5,"buffer":2}],20:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive("question", function (RecursionHelper) {
    return {
      restrict: 'E',
      scope: {
        questionNode: '=questionNode',
        application: '=application',
        level: '=level'
      },
      templateUrl: 'templates/question.html',
      compile: function (element) {
        return RecursionHelper.compile(element, function ($scope, iElement, iAttrs, controller, transcludeFn) {
          $scope.isGroup = function () {
            return $scope.questionNode && !_.isEmpty($scope.questionNode.questionNodes)
          }
        })
      }
    }
  })
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/question.js","/directives")
},{"+7ZJp0":5,"buffer":2}],21:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  listApp.directive('sortable', ["settings", function(settings) {
    return function($scope, $element, attrs) {
      var slide = function(el, offset) {
        el.css("transition", "all 0.5s")
        el.css("transform", "translate3d(0px, " + offset + "px, 0px)")
      }

      var resetSlide = function(el) {
        el.css({
          "transition": "",
          "transform": ""
        })
      }

      var switchPlaces = function(element1, element2) {
        var diffY = Math.abs(element1.offset().top - element2.offset().top)

        if (element1.index() < element2.index()) {
          slide(element1, element2.outerHeight() + diffY-element1.outerHeight())
          slide(element2, -diffY)
        } else {
          slide(element2, element1.outerHeight() + diffY-element2.outerHeight())
          slide(element1, -diffY)
        }

        setTimeout(function() {
          $scope.$apply(function(self) {
            var items = $element.find(attrs.sortableItem)
            self[attrs.sortableMoved](items.index(element1), items.index(element2))
            resetSlide(element1)
            resetSlide(element2)
          })
        }, settings.uiTransitionTime)
      }

      var arrowClicked = function(elementF) {
        return function(evt) {
          var btn = $(evt.target)
          if (!btn.hasClass("disabled")) {
            var element1 = btn.closest(attrs.sortableItem)
            var element2 = element1[elementF]()
            switchPlaces(element1, element2)
          }
        }
      }

      $element.on("click", ".sort-arrow-down", arrowClicked("next"))
      $element.on("click", ".sort-arrow-up", arrowClicked("prev"))
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/sortable.js","/directives")
},{"+7ZJp0":5,"buffer":2}],22:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var util = require("../util")

module.exports = function(listApp) {
  listApp.directive("valintatulos", ["localization", "restResources", "settings", function (localization, restResources, settings) {
    return {
      restrict: 'E',
      scope: {
        valintatulos: '&data',
        isFinal: '&final'
      },
      templateUrl: 'templates/valintatulos.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization

        $scope.formatDate = function(dt) {
          if (dt == null)
            return ""
          else
            return moment(dt).format('LL').replace(/,/g, "")
        }

        $scope.$watch("isFinal()", function(value) {
          $scope.status = value ? localization("label.resultsFinal") : localization("label.resultsPending")
        })

        $scope.$on("hakutoive-vastaanotettu", function(e, hakutoive) {
          var item = $(_(element.find("tbody tr")).find(function(tr) {
            return angular.element(tr).scope().tulos.koulutus.oid === hakutoive.koulutus.oid
          }))

          item.css({ "opacity": 0 })

          window.setTimeout(function() {
            item.animate({"opacity": 1}, settings.uiTransitionTime)
          }, settings.uiTransitionTime*2)
        })

        $scope.valintatulosText = function(valintatulos) {
          var key = util.underscoreToCamelCase(valintatulos.tila)
          if (["VASTAANOTTANUT", "EI_VASTAANOTETTU_MAARA_AIKANA", "EHDOLLISESTI_VASTAANOTTANUT"].indexOf(valintatulos.vastaanottotila) >= 0) {
            key = util.underscoreToCamelCase(valintatulos.vastaanottotila)
            return localization("label.resultState." + key)
          } else if (!_.isEmpty(valintatulos.tilankuvaus)) {
            if(valintatulos.tila === "HYLATTY"){
              return localization("label.resultState." + key) + " " + valintatulos.tilankuvaus
            } else {
              return valintatulos.tilankuvaus
            }
          } else if (valintatulos.tila === "VARALLA" && valintatulos.varasijojaTaytetaanAsti != null) {
            return localization("label.resultState.VarallaPvm", {
              varasija: valintatulos.varasijanumero,
              varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
            })
          } else {
            return localization("label.resultState." + key, {
              varasija: valintatulos.varasijanumero
            })
          }
        }

        $scope.valintatulosStyle = function(valintatulos) {
          if (valintatulos.tila == "HYVAKSYTTY" || valintatulos.tila == "HYVAKSYTTY_EHDOLLISESTI" || valintatulos.tila == "VARASIJALTA_HYVAKSYTTY")
            return "accepted"
        }
      }
    }
  }])
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/directives/valintatulos.js","/directives")
},{"+7ZJp0":5,"../util":29,"buffer":2}],23:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  require('./applicationValidator')(listApp)
  require('./angularBacon')(listApp)
  require('./directives/confirm')(listApp)
  require('./directives/question')(listApp)
  require('./directives/localizedLink')(listApp)
  require('./directives/formattedTime')(listApp)
  require('./directives/sortable')(listApp)
  require('./directives/disableClickFocus')(listApp)
  require('./directives/application')(listApp)
  require('./directives/hakutoiveenVastaanotto')(listApp)
  require('./directives/ilmoittautuminen')(listApp)
  require('./directives/hakutoiveet')(listApp)
  require('./directives/valintatulos')(listApp)
  require('./directives/henkilotiedot')(listApp)
  require('./directives/applicationPeriods')(listApp)
  require('./directives/clearableInput')(listApp)
  require('./directives/callout')(listApp)
}

module.exports.Hakemus = require("./hakemus")

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/fake_6f84bea1.js","/")
},{"+7ZJp0":5,"./angularBacon":6,"./applicationValidator":7,"./directives/application":8,"./directives/applicationPeriods":9,"./directives/callout":10,"./directives/clearableInput":11,"./directives/confirm":12,"./directives/disableClickFocus":13,"./directives/formattedTime":14,"./directives/hakutoiveenVastaanotto":15,"./directives/hakutoiveet":16,"./directives/henkilotiedot":17,"./directives/ilmoittautuminen":18,"./directives/localizedLink":19,"./directives/question":20,"./directives/sortable":21,"./directives/valintatulos":22,"./hakemus":24,"buffer":2}],24:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var Hakutoive = require('./hakutoive')
var Question = require('./question').Question
var util = require('./util')

function Hakemus(json) {
  this.oid = json.hakemus.oid
  this.updated = json.hakemus.updated
  this.haku = copy(json.hakemus.haku)
  this.state = copy(json.hakemus.state)
  this.hasForm = json.hakemus.hasForm
  this.educationBackground = copy(json.hakemus.educationBackground)

  this.hakutoiveet = convertHakutoiveet(json.hakemus.hakutoiveet)
  this.henkilotiedot = convertHenkilotiedot(json.hakemus.answers.henkilotiedot)
  this.persistedAnswers = json.hakemus.answers
  this.additionalQuestions = Question.getQuestions(json.questions, this)
  this.tulosOk = json.tulosOk
  this.calculatedValues = {
    postOffice: json.hakemus.postOffice
  }
}

function copy(json) { return $.extend(true, {}, json) }

function convertHenkilotiedot(json) {
  var fields = ["Sähköposti", "matkapuhelinnumero1", "asuinmaa", "lahiosoite", "Postinumero"]
  return _(fields).reduce(function(memo, key) {
    memo[key] = new Question({ id: key }, json[key])
    return memo
  } , {})
}

function convertHakutoiveet(hakutoiveet) {
  return _(hakutoiveet).map(function(hakutoive) { return new Hakutoive(hakutoive) })
}

function updatePreferenceQuestionIds(manipulationF) {
  var newIndexes = (function getNewIndexes() {
    var arr = _.range(1, this.hakutoiveet.length+1)
    manipulationF(arr)
    var indexes = _(arr).map(function(val, index) { return [val, index+1] })
    return _.object(indexes)
  }).call(this)

  _(Question.questionMap(this.additionalQuestions)).each(function(question, id) {
    var questionIdParts = /^(preference)(\d+)([-_].+)/.exec(id)
    if (questionIdParts != null) {
      var newId = questionIdParts[1] + newIndexes[questionIdParts[2]] + questionIdParts[3]
      question.id.questionId = newId
    }
  })
}

Hakemus.prototype = {
  removePreference: function(index) {
    this.hakutoiveet.splice(index, 1)
    updatePreferenceQuestionIds.call(this, function(arr) {
      arr.splice(index, 1)
    })
  },

  addPreference: function(hakutoive) {
    this.hakutoiveet.push(hakutoive)
  },

  hasPreference: function(index) {
    return index >= 0 && index <= this.hakutoiveet.length-1 && this.hakutoiveet[index].hasData()
  },

  movePreference: function(from, to) {
    this.hakutoiveet[from].setAsModified()
    this.hakutoiveet[to].setAsModified()
    this.hakutoiveet.splice(to, 0, this.hakutoiveet.splice(from, 1)[0])

    updatePreferenceQuestionIds.call(this,
      function(arr) {
        arr.splice(to, 0, arr.splice(from, 1)[0])
      }
    )
  },

  canMovePreference: function(from, to) {
    var lastFilledItem = (function getLastFilled(hakutoiveet) {
      for (var i=hakutoiveet.length-1; i>=0; i--)
        if (hakutoiveet[i].hasData())
          return i
      return -1
    })(this.hakutoiveet)

    return !this.preferenceLocked(from) && this.hakutoiveet[from].hasData() && from >= 0 && to <= lastFilledItem && to >= 0
  },
  
  preferenceLocked: function(index) {
    var hakutoive = this.hakutoiveet[index]
    var hakuaikaId = hakutoive.hakuaikaId
    var self = this

    function isPeriodActive(applicationPeriodId) {
      var period = _(self.haku.applicationPeriods).find(function(period) { return period.id === applicationPeriodId })
      return period !== undefined ? period.active : self.haku.active
    }

    if (hakutoive.addedDuringCurrentSession) {
      return false
    } else if (!_.isEmpty(hakutoive.kohdekohtainenHakuaika)) {
      return !hakutoive.kohdekohtainenHakuaika.active
    } else if (!_.isEmpty(hakuaikaId)) {
      return !isPeriodActive(hakuaikaId)
    } else {
      return !this.haku.active
    }
  },

  allResultsAvailable: function() {
    return !this.hasResultState(["KESKEN", "VARALLA"]) && this.valintatulosHakutoiveet().length > 0
  },

  resultFetchedSuccessfully: function() {
    return this.tulosOk
  },

  hasSomeResults: function() {
    var hakutoiveet = this.valintatulosHakutoiveet()
    return hakutoiveet.length > 0 && _(hakutoiveet).some(function(hakutoive) { return hakutoive.tila != "KESKEN" })
  },

  valintatulosHakutoiveet: function() {
    return this.state && this.state.valintatulos ? this.state.valintatulos.hakutoiveet : []
  },

  applicationPeriodsInactive: function() {
    return _(this.haku.applicationPeriods).every(function(period) { return !period.active })
  },

  editHakutoiveetEnabled: function() {
    return this.state && (this.state.id == 'ACTIVE' || this.state.id == 'INCOMPLETE')
  },

  editHenkilotiedotEnabled: function() {
    return this.editHakutoiveetEnabled() || (this.state && this.state.id == "HAKUKAUSIPAATTYNYT")
  },

  vastaanotettavatHakutoiveet: function() {
    return _(this.valintatulosHakutoiveet()).filter(function(hakutoive) {
      return hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_SITOVASTI" || hakutoive.vastaanotettavuustila === "VASTAANOTETTAVISSA_EHDOLLISESTI"
    })
  },

  ilmoittautumisLinkit: function() {
    return _(this.valintatulosHakutoiveet()).filter(function(tulos) {
      return tulos.ilmoittautumistila != null &&
        tulos.ilmoittautumistila.ilmoittauduttavissa &&
        tulos.ilmoittautumistila.ilmoittautumistapa != null && tulos.ilmoittautumistila.ilmoittautumistapa.url
    })
  },

  hasResultState: function(resultStates) {
    if (!_.isArray(resultStates))
      resultStates = [resultStates]

    return _(this.valintatulosHakutoiveet()).any(function(hakutoive) {
      return _(resultStates).contains(hakutoive.tila)}
    )
  },

  toJson: function() {
    var self = this

    return {
      oid: self.oid,
      hakuOid: self.haku.oid,
      hakutoiveet: _(this.hakutoiveet).map(function(hakutoive) { return hakutoive.toJson() }),
      answers: removeFalseBooleans(getAnswers())
    }

    function getAnswers() {
      var contactDetails = _(self.henkilotiedot).reduce(function(answers, question, id) {
        answers.henkilotiedot[id] = question.answer
        return answers
      }, { henkilotiedot: {}})

      var additionalQuestionAnswers = _(Question.questionMap(self.additionalQuestions)).reduce(function(answers, questionNode) {
        answers[questionNode.id.phaseId] = answers[questionNode.id.phaseId] || {}
        var answer = questionNode.answer
        if (_.isObject(answer)) {
          _(answer).each(function(val, key) {
            answers[questionNode.id.phaseId][key] = val
          })
        } else {
          answers[questionNode.id.phaseId][questionNode.id.questionId] = answer
        }
        return answers
      }, {})

      return _.extend({}, contactDetails, additionalQuestionAnswers)
    }

    function removeFalseBooleans(obj) {
      _.each(obj, function(val, key) {
        if (_.isBoolean(val) && val === false)
          obj[key] = ""
        else if (_.isObject(val))
          removeFalseBooleans(val)
      })
      return obj;
    }
  },

  mergeSavedApplication: function(savedApplication) {
    this.updated = savedApplication.updated

    if (!_.isEqual(util.withoutAngularFields(this.state), savedApplication.state))
      this.state = $.extend(true, {}, savedApplication.state)

    for (var i=0; i<this.hakutoiveet.length && i<savedApplication.hakutoiveet.length; i++) {
      hakutoive = this.hakutoiveet[i]
      hakutoive.importJson(savedApplication.hakutoiveet[i])
      if (hakutoive.hasData())
        hakutoive.setAsSaved()
    }
  },

  importHakuajat: function(hakukohteet) {
    if (hakukohteet != null) {
      for (var i = 0; i < this.hakutoiveet.length && i < hakukohteet.length; i++) {
        this.hakutoiveet[i].kohdekohtainenHakuaika = hakukohteet[i].kohdekohtainenHakuaika
      }
    }
  },

  validatePreferences: function() {
    return (this.hakutoiveet.length > 0 && this.hakutoiveet[0].hasData()) &&
      _(this.hakutoiveet).every(function(hakutoive) {
        return hakutoive.isValid()
      }) && !_(this.hakutoiveet.slice(0, this.lastIndexWithData() + 1)).any(function(hakutoive) {
        return !hakutoive.hasData()
      })
  },

  getChangedPreferences: function() {
    return _(this.hakutoiveet).chain()
      .map(function(hakutoive, index) { return hakutoive.isModified ? index : null })
      .without(null)
      .value()
  },

  isEditable: function(index) {
    return !this.applicationPeriodsInactive() &&  index <= this.lastIndexWithData() + 1
  },

  lastIndexWithData: function() {
    for (var i = this.hakutoiveet.length - 1; i >= 0; i--) {
      if (this.hakutoiveet[i].hasData()) return i
    }
    return -1
  },

  importQuestions: function(questions) {
    this.additionalQuestions = (function mergeOldAnswers(old, questions) {
      var oldQuestions = Question.questionMap(old)
      _(Question.questionMap(questions)).each(function(newQuestion, id) {
        if (oldQuestions[id] != null)
          newQuestion.answer = oldQuestions[id].answer
      })
      return questions
    })(this.additionalQuestions, questions)
  },

  updateValidationMessages: function(errors, skipQuestions) {
    var errorMap = util.mapArray(errors, "key", "message")
    var hakutoiveMap = Hakutoive.hakutoiveMap(this.hakutoiveet)
    var questionMap = _.extend({}, Question.questionMap(this.additionalQuestions), this.henkilotiedot)
    var unhandled = []

    clearErrors()

    _(errorMap).each(function(errorList, key) {
      if (!updateErrors(key, errorList))
        unhandled.push({questionId: key, errors: errorList})
    })

    return unhandled

    function clearErrors() {
      _(hakutoiveMap).each(function(item) { item.setErrors() })
      if (!skipQuestions)
        _(questionMap).each(function(item) { item.setErrors() })
    }

    function updateErrors(questionId, errors) {
      if (questionMap[questionId] != null) {
        if (!skipQuestions)
          questionMap[questionId].appendErrors(errors)
        return true
      } else if (Hakutoive.isHakutoiveError(questionId)) {
        hakutoiveMap[Hakutoive.questionIdToHakutoiveId(questionId)].appendErrors(errors)
        return true
      } else {
        return false
      }
    }
  }
}

module.exports = Hakemus
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/hakemus.js","/")
},{"+7ZJp0":5,"./hakutoive":27,"./question":28,"./util":29,"buffer":2}],"./hakemuseditori":[function(require,module,exports){
module.exports=require('sBSSnn');
},{}],"sBSSnn":[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
module.exports = function(listApp) {
  require('./applicationValidator')(listApp)
  require('./angularBacon')(listApp)
  require('./directives/confirm')(listApp)
  require('./directives/question')(listApp)
  require('./directives/localizedLink')(listApp)
  require('./directives/formattedTime')(listApp)
  require('./directives/sortable')(listApp)
  require('./directives/disableClickFocus')(listApp)
  require('./directives/application')(listApp)
  require('./directives/hakutoiveenVastaanotto')(listApp)
  require('./directives/ilmoittautuminen')(listApp)
  require('./directives/hakutoiveet')(listApp)
  require('./directives/valintatulos')(listApp)
  require('./directives/henkilotiedot')(listApp)
  require('./directives/applicationPeriods')(listApp)
  require('./directives/clearableInput')(listApp)
  require('./directives/callout')(listApp)
}

module.exports.Hakemus = require("./hakemus")

}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/hakemuseditori.js","/")
},{"+7ZJp0":5,"./angularBacon":6,"./applicationValidator":7,"./directives/application":8,"./directives/applicationPeriods":9,"./directives/callout":10,"./directives/clearableInput":11,"./directives/confirm":12,"./directives/disableClickFocus":13,"./directives/formattedTime":14,"./directives/hakutoiveenVastaanotto":15,"./directives/hakutoiveet":16,"./directives/henkilotiedot":17,"./directives/ilmoittautuminen":18,"./directives/localizedLink":19,"./directives/question":20,"./directives/sortable":21,"./directives/valintatulos":22,"./hakemus":24,"buffer":2}],27:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var util = require('./util')

function Hakutoive(json) {
  this.importJson(json)
  this.isModified = false
  this.isNew = _.isEmpty(json)
  this.addedDuringCurrentSession = _.isEmpty(json)
  this.errors = []
}

Hakutoive.prototype = {
  toJson: function() {
    return this.data
  },

  importJson: function(json) {
      this.data = json.hakemusData || {}
      this.hakuaikaId = json.hakuaikaId
      this.kohdekohtainenHakuaika = json.kohdekohtainenHakuaika
  },

  clear: function() {
    this.data = {}
    this.isNew = true
    this.isModified = false
  },

  hasData: function() {
    return !_.isEmpty(this.data)
  },

  setOpetuspiste: function(id, name) {
    this.data["Opetuspiste"] = name
    this.data["Opetuspiste-id"] = id
    this.isModified = true
    this.setErrors([])
  },

  setKoulutus: function(koulutus) {
    this.data["Koulutus"] = toString(koulutus.name)
    this.data["Koulutus-id"] = toString(koulutus.id)
    this.data["Koulutus-educationDegree"] = toString(koulutus.educationDegree)
    this.data["Koulutus-id-lang"] = toString(koulutus.teachingLanguages[0])
    this.data["Koulutus-id-sora"] = toString(koulutus.sora)
    this.data["Koulutus-id-aoIdentifier"] = toString(koulutus.aoIdentifier)
    this.data["Koulutus-id-kaksoistutkinto"] = toString(koulutus.kaksoistutkinto)
    this.data["Koulutus-id-vocational"] = toString(koulutus.vocational)
    this.data["Koulutus-id-educationcode"] = toString(koulutus.educationCodeUri)
    this.data["Koulutus-id-athlete"] = toString(koulutus.athleteEducation)
    this.data["Koulutus-id-attachments"] = toString(koulutus.attachments != null && koulutus.attachments.length > 0)
    this.addGroupInfo(koulutus)
    this.isModified = true
    this.setErrors([])
    function toString(x) {
      return (x==null) ? "" : x.toString()
    }
  },

  addGroupInfo: function(koulutus) {
    var attachmentGroups = [];
    var aoGroups = [];
    if (koulutus.organizationGroups instanceof Array) {
      for (var i = 0; i < koulutus.organizationGroups.length; i++) {
        var group = koulutus.organizationGroups[i];
        if(group.groupTypes.indexOf("hakukohde") >= 0) {
          aoGroups.push(group.oid);
          if(group.usageGroups.indexOf("hakukohde_liiteosoite") >= 0) {
            attachmentGroups.push(group.oid);
          }
        }
      }
    }
    if(aoGroups.length > 0) {
      this.data["Koulutus-id-ao-groups"] = aoGroups.join(",")
    }
    if(attachmentGroups.length > 0) {
      this.data["Koulutus-id-attachmentgroups"] = attachmentGroups.join(",")
    }
  },

  hasOpetuspiste: function() {
    return !_.isEmpty(this.data["Opetuspiste-id"])
  },

  removeOpetuspisteData: function() {
    var self = this
    _.each(this.data, function(value, key) {
      if (key.indexOf("$")!==0 && key != "Opetuspiste")
        delete self.data[key]
    })
    delete this.kohdekohtainenHakuaika
  },

  isValid: function() {
    return (_.isEmpty(this.data["Opetuspiste"]) || !_.isEmpty(this.data["Koulutus-id"]))
  },

  setErrors: function(errors) {
    this.errors = errors || []
  },

  appendErrors: function(errors) {
    this.errors = this.errors.concat(errors)
  },

  setAsSaved: function() {
    this.isNew = false
    this.isModified = false
  },

  setAsModified: function() {
    this.isModified = true
  }
}

var hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/
Hakutoive.isHakutoiveError = function(questionId) {
  return hakutoiveErrorRegexp.test(questionId)
}

Hakutoive.parseHakutoiveIndex = function(questionId) {
  var result = /^preference(\d+)/.exec(questionId)
  if (result)
    return Number(result[1])
  else
    return null
}

Hakutoive.hasHakutoiveErrors = function(errorsJson) {
  var errorMap = util.mapArray(errorsJson, "key", "message");
  var self = this
  return _(errorMap).any(function(val, key) {
    return self.isHakutoiveError(key) && val.length > 0
  })
}

Hakutoive.hakutoiveMap = function(hakutoiveet) {
  return util.indexBy(hakutoiveet, function(hakutoive, index) { return "preference" + (index+1) })
}
Hakutoive.questionIdToHakutoiveId = function(questionId) {
  return _.chain(hakutoiveErrorRegexp.exec(questionId)).rest().without(undefined).first().value()
}
module.exports = Hakutoive
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/hakutoive.js","/")
},{"+7ZJp0":5,"./util":29,"buffer":2}],28:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var util = require('./util')

var questionDefaults = {
  help: "",
  verboseHelp: "",
  required: false,
  questionType: "",
  id: null,
  title: ""
}

function params(obj) {
  var questionParameters = _.extend({}, questionDefaults, obj)
  _(questionParameters).each(function(val, key) {
    if (val == null)
      if(key == "title") {
        questionParameters.title = "?"
      }
      else {
        throw new Error("question parameter " + key + " null")
      }
  })
  return questionParameters
}

function Question(question, answer, validationErrors) {
  _.extend(this, params(question))
  this.answer = answer
  this.errors = validationErrors || []
}

Question.fromJson = function(json, application) {
  return new Question(json, initialValue(json, application), json.required ? ["*"] : [])
}

Question.getQuestions = function(jsonQuestions, application) {
  return convertToItems(jsonQuestions, new QuestionGroup())

  function convertToItems(questions, results) {
    _(questions).each(function (questionNode) {
      if (questionNode.questions != null) {
        results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title)))
      } else {
        results.questionNodes.push(Question.fromJson(questionNode, application))
      }
    })
    return results
  }
}

Question.prototype = {
  setErrors: function(errors) {
    this.errors = errors || []
  },

  appendErrors: function(errors) {
    this.errors = this.errors.concat(errors)
  }
}

Question.questionMap = function(questions) {
  questions = util.flattenTree(questions, "questionNodes")
  return util.indexBy(questions, function(node) { return node.id.questionId })
}

function initialValue(question, application) {
  function defaultValue() {
    var defaultOption = _(question.options).find(function(option) { return option.default })
    return defaultOption == null ? "" : defaultOption.value
  }
  function getOldValue(questionId) {
    var questionIdParts = /^(preference)(\d+)([-_].+)/.exec(questionId)
    if (questionIdParts != null && application.hakutoiveet[questionIdParts[2] - 1] != null && application.hakutoiveet[questionIdParts[2] - 1].isNew) {
      return null
    }
    var phaseAnswers = application.persistedAnswers[question.id.phaseId]
    if(phaseAnswers == null) {
      return null
    }
    return phaseAnswers[questionId]
  }

  var oldAnswer = getOldValue(question.id.questionId)
  if (question.options != null) {
    if (question.questionType == "Checkbox") {
      return _(question.options).chain().map(function(option) {
        oldAnswer = getOldValue(option.value)
        return [option.value, oldAnswer == null ? false : Boolean(oldAnswer)]
      }).object().value()
    } else {
      // Aseta default-arvo vain monivalinnoille
      return oldAnswer == null ? defaultValue() : oldAnswer
    }
  }
  if(oldAnswer != null) {
    return  oldAnswer
  }
}

function QuestionGroup(title) {
  this.title = title
  this.questionNodes = []
}

module.exports = {
  Question: Question,
  QuestionGroup: QuestionGroup
}
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/question.js","/")
},{"+7ZJp0":5,"./util":29,"buffer":2}],29:[function(require,module,exports){
(function (process,global,Buffer,__argument0,__argument1,__argument2,__argument3,__filename,__dirname){
var utils = {
  mapArray: function (array, keyField, valueField) {
    return _.reduce(array, function (memo, item) {
      var key = item[keyField]
      if (memo[key] == null)
        memo[key] = []
      memo[key].push(item[valueField])
      return memo
    }, {});
  },

  indexBy: function (array, keyFunction) {
    return _.reduce(array, function (memo, item, index) {
      memo[keyFunction(item, index)] = item
      return memo
    }, {})
  },

  flattenTree: function (rootNode, childrenAttribute) {
    return (function flatten(node, list) {
      if (node != null) {
        if (node[childrenAttribute] == null)
          list.push(node)
        else
          _(node[childrenAttribute]).each(function (subnode) {
            flatten(subnode, list)
          })
      }
      return list
    })(rootNode, [])
  },

  underscoreToCamelCase: function (str) {
    return str.toLowerCase().replace(/^(.)|_(.)/g, function (match, char1, char2) {
      return (char1 ? char1 : "" + char2 ? char2 : "").toUpperCase()
    })
  },

  withoutAngularFields: function (obj) {
    if (_.isArray(obj)) {
      return _(obj).map(utils.withoutAngularFields)
    } else if (_.isObject(obj)) {
      return _(obj).reduce(function (memo, val, key) {
        if (key.indexOf("$$") < 0)
          memo[key] = utils.withoutAngularFields(val)
        return memo
      }, {})
    } else
      return obj
  }
}

module.exports = utils
}).call(this,require("+7ZJp0"),typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {},require("buffer").Buffer,arguments[3],arguments[4],arguments[5],arguments[6],"/util.js","/")
},{"+7ZJp0":5,"buffer":2}]},{},[23])
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3Nlci1wYWNrL19wcmVsdWRlLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9ub2RlX21vZHVsZXMvYmFjb25qcy9kaXN0L0JhY29uLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9ub2RlX21vZHVsZXMvZ3VscC1icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9idWZmZXIvaW5kZXguanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL25vZGVfbW9kdWxlcy9ndWxwLWJyb3dzZXJpZnkvbm9kZV9tb2R1bGVzL2Jyb3dzZXJpZnkvbm9kZV9tb2R1bGVzL2J1ZmZlci9ub2RlX21vZHVsZXMvYmFzZTY0LWpzL2xpYi9iNjQuanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL25vZGVfbW9kdWxlcy9ndWxwLWJyb3dzZXJpZnkvbm9kZV9tb2R1bGVzL2Jyb3dzZXJpZnkvbm9kZV9tb2R1bGVzL2J1ZmZlci9ub2RlX21vZHVsZXMvaWVlZTc1NC9pbmRleC5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9hbmd1bGFyQmFjb24uanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL2FwcGxpY2F0aW9uVmFsaWRhdG9yLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2FwcGxpY2F0aW9uLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2FwcGxpY2F0aW9uUGVyaW9kcy5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvZGlyZWN0aXZlcy9jYWxsb3V0LmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2NsZWFyYWJsZUlucHV0LmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2NvbmZpcm0uanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL2RpcmVjdGl2ZXMvZGlzYWJsZUNsaWNrRm9jdXMuanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL2RpcmVjdGl2ZXMvZm9ybWF0dGVkVGltZS5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvZGlyZWN0aXZlcy9oYWt1dG9pdmVlblZhc3RhYW5vdHRvLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2hha3V0b2l2ZWV0LmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL2hlbmtpbG90aWVkb3QuanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL2RpcmVjdGl2ZXMvaWxtb2l0dGF1dHVtaW5lbi5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvZGlyZWN0aXZlcy9sb2NhbGl6ZWRMaW5rLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL3F1ZXN0aW9uLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL3NvcnRhYmxlLmpzIiwiL1VzZXJzL2pwYWFuYW5lL3JlYWt0b3Ivb3BoL29tYXRzaXZ1dC9oYWtlbXVzZWRpdG9yaS9zcmMvbWFpbi9qcy9kaXJlY3RpdmVzL3ZhbGludGF0dWxvcy5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvZmFrZV82Zjg0YmVhMS5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvaGFrZW11cy5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvaGFrZW11c2VkaXRvcmkuanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL2hha3V0b2l2ZS5qcyIsIi9Vc2Vycy9qcGFhbmFuZS9yZWFrdG9yL29waC9vbWF0c2l2dXQvaGFrZW11c2VkaXRvcmkvc3JjL21haW4vanMvcXVlc3Rpb24uanMiLCIvVXNlcnMvanBhYW5hbmUvcmVha3Rvci9vcGgvb21hdHNpdnV0L2hha2VtdXNlZGl0b3JpL3NyYy9tYWluL2pzL3V0aWwuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3BtR0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2bENBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzlIQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdEZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNqRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDOUJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDaEVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2pPQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2xDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3RCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN4QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDcENBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ1pBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2QkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUN2REE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUMzRkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDMUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2ZBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2xCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNyQkE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3JEQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUNyRUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ3ZCQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7OztBQ2xUQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FDdkJBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQ2hKQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQzdHQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQSIsImZpbGUiOiJnZW5lcmF0ZWQuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlc0NvbnRlbnQiOlsiKGZ1bmN0aW9uIGUodCxuLHIpe2Z1bmN0aW9uIHMobyx1KXtpZighbltvXSl7aWYoIXRbb10pe3ZhciBhPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7aWYoIXUmJmEpcmV0dXJuIGEobywhMCk7aWYoaSlyZXR1cm4gaShvLCEwKTt0aHJvdyBuZXcgRXJyb3IoXCJDYW5ub3QgZmluZCBtb2R1bGUgJ1wiK28rXCInXCIpfXZhciBmPW5bb109e2V4cG9ydHM6e319O3Rbb11bMF0uY2FsbChmLmV4cG9ydHMsZnVuY3Rpb24oZSl7dmFyIG49dFtvXVsxXVtlXTtyZXR1cm4gcyhuP246ZSl9LGYsZi5leHBvcnRzLGUsdCxuLHIpfXJldHVybiBuW29dLmV4cG9ydHN9dmFyIGk9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtmb3IodmFyIG89MDtvPHIubGVuZ3RoO28rKylzKHJbb10pO3JldHVybiBzfSkiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG4oZnVuY3Rpb24oKSB7XG4gIHZhciBCYWNvbiwgQnVmZmVyaW5nU291cmNlLCBCdXMsIENvbXBvc2l0ZVVuc3Vic2NyaWJlLCBDb25zdW1pbmdTb3VyY2UsIERlc2MsIERpc3BhdGNoZXIsIEVuZCwgRXJyb3IsIEV2ZW50LCBFdmVudFN0cmVhbSwgRXhjZXB0aW9uLCBJbml0aWFsLCBOZXh0LCBOb25lLCBPYnNlcnZhYmxlLCBQcm9wZXJ0eSwgUHJvcGVydHlEaXNwYXRjaGVyLCBTb21lLCBTb3VyY2UsIFVwZGF0ZUJhcnJpZXIsIGFkZFByb3BlcnR5SW5pdFZhbHVlVG9TdHJlYW0sIGFzc2VydCwgYXNzZXJ0QXJyYXksIGFzc2VydEV2ZW50U3RyZWFtLCBhc3NlcnRGdW5jdGlvbiwgYXNzZXJ0Tm9Bcmd1bWVudHMsIGFzc2VydE9ic2VydmFibGUsIGFzc2VydFN0cmluZywgY2xvbmVBcnJheSwgY29tcG9zaXRlVW5zdWJzY3JpYmUsIGNvbnN0YW50VG9GdW5jdGlvbiwgY29udGFpbnNEdXBsaWNhdGVEZXBzLCBjb252ZXJ0QXJnc1RvRnVuY3Rpb24sIGRlc2NyaWJlLCBlbmQsIGV2ZW50SWRDb3VudGVyLCBmaW5kRGVwcywgZmxhdE1hcF8sIGZvcm1lciwgaWRDb3VudGVyLCBpbml0aWFsLCBpc0FycmF5LCBpc0ZpZWxkS2V5LCBpc0Z1bmN0aW9uLCBpc09ic2VydmFibGUsIGxhdHRlciwgbGlmdENhbGxiYWNrLCBtYWtlRnVuY3Rpb24sIG1ha2VGdW5jdGlvbkFyZ3MsIG1ha2VGdW5jdGlvbl8sIG1ha2VPYnNlcnZhYmxlLCBtYWtlU3Bhd25lciwgbmV4dCwgbm9wLCBwYXJ0aWFsbHlBcHBsaWVkLCByZWN1cnNpb25EZXB0aCwgcmVnaXN0ZXJPYnMsIHNweXMsIHRvQ29tYmluYXRvciwgdG9FdmVudCwgdG9GaWVsZEV4dHJhY3RvciwgdG9GaWVsZEtleSwgdG9PcHRpb24sIHRvU2ltcGxlRXh0cmFjdG9yLCB3aXRoRGVzY3JpcHRpb24sIHdpdGhNZXRob2RDYWxsU3VwcG9ydCwgXywgX3JlZixcbiAgICBfX2hhc1Byb3AgPSB7fS5oYXNPd25Qcm9wZXJ0eSxcbiAgICBfX3NsaWNlID0gW10uc2xpY2UsXG4gICAgX19leHRlbmRzID0gZnVuY3Rpb24oY2hpbGQsIHBhcmVudCkgeyBmb3IgKHZhciBrZXkgaW4gcGFyZW50KSB7IGlmIChfX2hhc1Byb3AuY2FsbChwYXJlbnQsIGtleSkpIGNoaWxkW2tleV0gPSBwYXJlbnRba2V5XTsgfSBmdW5jdGlvbiBjdG9yKCkgeyB0aGlzLmNvbnN0cnVjdG9yID0gY2hpbGQ7IH0gY3Rvci5wcm90b3R5cGUgPSBwYXJlbnQucHJvdG90eXBlOyBjaGlsZC5wcm90b3R5cGUgPSBuZXcgY3RvcigpOyBjaGlsZC5fX3N1cGVyX18gPSBwYXJlbnQucHJvdG90eXBlOyByZXR1cm4gY2hpbGQ7IH0sXG4gICAgX19iaW5kID0gZnVuY3Rpb24oZm4sIG1lKXsgcmV0dXJuIGZ1bmN0aW9uKCl7IHJldHVybiBmbi5hcHBseShtZSwgYXJndW1lbnRzKTsgfTsgfTtcblxuICBCYWNvbiA9IHtcbiAgICB0b1N0cmluZzogZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gXCJCYWNvblwiO1xuICAgIH1cbiAgfTtcblxuICBCYWNvbi52ZXJzaW9uID0gJzAuNy40Mic7XG5cbiAgRXhjZXB0aW9uID0gKHR5cGVvZiBnbG9iYWwgIT09IFwidW5kZWZpbmVkXCIgJiYgZ2xvYmFsICE9PSBudWxsID8gZ2xvYmFsIDogdGhpcykuRXJyb3I7XG5cbiAgXyA9IHtcbiAgICBpbmRleE9mOiBBcnJheS5wcm90b3R5cGUuaW5kZXhPZiA/IGZ1bmN0aW9uKHhzLCB4KSB7XG4gICAgICByZXR1cm4geHMuaW5kZXhPZih4KTtcbiAgICB9IDogZnVuY3Rpb24oeHMsIHgpIHtcbiAgICAgIHZhciBpLCB5LCBfaSwgX2xlbjtcbiAgICAgIGZvciAoaSA9IF9pID0gMCwgX2xlbiA9IHhzLmxlbmd0aDsgX2kgPCBfbGVuOyBpID0gKytfaSkge1xuICAgICAgICB5ID0geHNbaV07XG4gICAgICAgIGlmICh4ID09PSB5KSB7XG4gICAgICAgICAgcmV0dXJuIGk7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICAgIHJldHVybiAtMTtcbiAgICB9LFxuICAgIGluZGV4V2hlcmU6IGZ1bmN0aW9uKHhzLCBmKSB7XG4gICAgICB2YXIgaSwgeSwgX2ksIF9sZW47XG4gICAgICBmb3IgKGkgPSBfaSA9IDAsIF9sZW4gPSB4cy5sZW5ndGg7IF9pIDwgX2xlbjsgaSA9ICsrX2kpIHtcbiAgICAgICAgeSA9IHhzW2ldO1xuICAgICAgICBpZiAoZih5KSkge1xuICAgICAgICAgIHJldHVybiBpO1xuICAgICAgICB9XG4gICAgICB9XG4gICAgICByZXR1cm4gLTE7XG4gICAgfSxcbiAgICBoZWFkOiBmdW5jdGlvbih4cykge1xuICAgICAgcmV0dXJuIHhzWzBdO1xuICAgIH0sXG4gICAgYWx3YXlzOiBmdW5jdGlvbih4KSB7XG4gICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiB4O1xuICAgICAgfTtcbiAgICB9LFxuICAgIG5lZ2F0ZTogZnVuY3Rpb24oZikge1xuICAgICAgcmV0dXJuIGZ1bmN0aW9uKHgpIHtcbiAgICAgICAgcmV0dXJuICFmKHgpO1xuICAgICAgfTtcbiAgICB9LFxuICAgIGVtcHR5OiBmdW5jdGlvbih4cykge1xuICAgICAgcmV0dXJuIHhzLmxlbmd0aCA9PT0gMDtcbiAgICB9LFxuICAgIHRhaWw6IGZ1bmN0aW9uKHhzKSB7XG4gICAgICByZXR1cm4geHMuc2xpY2UoMSwgeHMubGVuZ3RoKTtcbiAgICB9LFxuICAgIGZpbHRlcjogZnVuY3Rpb24oZiwgeHMpIHtcbiAgICAgIHZhciBmaWx0ZXJlZCwgeCwgX2ksIF9sZW47XG4gICAgICBmaWx0ZXJlZCA9IFtdO1xuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSB4cy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICB4ID0geHNbX2ldO1xuICAgICAgICBpZiAoZih4KSkge1xuICAgICAgICAgIGZpbHRlcmVkLnB1c2goeCk7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICAgIHJldHVybiBmaWx0ZXJlZDtcbiAgICB9LFxuICAgIG1hcDogZnVuY3Rpb24oZiwgeHMpIHtcbiAgICAgIHZhciB4LCBfaSwgX2xlbiwgX3Jlc3VsdHM7XG4gICAgICBfcmVzdWx0cyA9IFtdO1xuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSB4cy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICB4ID0geHNbX2ldO1xuICAgICAgICBfcmVzdWx0cy5wdXNoKGYoeCkpO1xuICAgICAgfVxuICAgICAgcmV0dXJuIF9yZXN1bHRzO1xuICAgIH0sXG4gICAgZWFjaDogZnVuY3Rpb24oeHMsIGYpIHtcbiAgICAgIHZhciBrZXksIHZhbHVlO1xuICAgICAgZm9yIChrZXkgaW4geHMpIHtcbiAgICAgICAgdmFsdWUgPSB4c1trZXldO1xuICAgICAgICBmKGtleSwgdmFsdWUpO1xuICAgICAgfVxuICAgICAgcmV0dXJuIHZvaWQgMDtcbiAgICB9LFxuICAgIHRvQXJyYXk6IGZ1bmN0aW9uKHhzKSB7XG4gICAgICBpZiAoaXNBcnJheSh4cykpIHtcbiAgICAgICAgcmV0dXJuIHhzO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmV0dXJuIFt4c107XG4gICAgICB9XG4gICAgfSxcbiAgICBjb250YWluczogZnVuY3Rpb24oeHMsIHgpIHtcbiAgICAgIHJldHVybiBfLmluZGV4T2YoeHMsIHgpICE9PSAtMTtcbiAgICB9LFxuICAgIGlkOiBmdW5jdGlvbih4KSB7XG4gICAgICByZXR1cm4geDtcbiAgICB9LFxuICAgIGxhc3Q6IGZ1bmN0aW9uKHhzKSB7XG4gICAgICByZXR1cm4geHNbeHMubGVuZ3RoIC0gMV07XG4gICAgfSxcbiAgICBhbGw6IGZ1bmN0aW9uKHhzLCBmKSB7XG4gICAgICB2YXIgeCwgX2ksIF9sZW47XG4gICAgICBpZiAoZiA9PSBudWxsKSB7XG4gICAgICAgIGYgPSBfLmlkO1xuICAgICAgfVxuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSB4cy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICB4ID0geHNbX2ldO1xuICAgICAgICBpZiAoIWYoeCkpIHtcbiAgICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH0sXG4gICAgYW55OiBmdW5jdGlvbih4cywgZikge1xuICAgICAgdmFyIHgsIF9pLCBfbGVuO1xuICAgICAgaWYgKGYgPT0gbnVsbCkge1xuICAgICAgICBmID0gXy5pZDtcbiAgICAgIH1cbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0geHMubGVuZ3RoOyBfaSA8IF9sZW47IF9pKyspIHtcbiAgICAgICAgeCA9IHhzW19pXTtcbiAgICAgICAgaWYgKGYoeCkpIHtcbiAgICAgICAgICByZXR1cm4gdHJ1ZTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH0sXG4gICAgd2l0aG91dDogZnVuY3Rpb24oeCwgeHMpIHtcbiAgICAgIHJldHVybiBfLmZpbHRlcigoZnVuY3Rpb24oeSkge1xuICAgICAgICByZXR1cm4geSAhPT0geDtcbiAgICAgIH0pLCB4cyk7XG4gICAgfSxcbiAgICByZW1vdmU6IGZ1bmN0aW9uKHgsIHhzKSB7XG4gICAgICB2YXIgaTtcbiAgICAgIGkgPSBfLmluZGV4T2YoeHMsIHgpO1xuICAgICAgaWYgKGkgPj0gMCkge1xuICAgICAgICByZXR1cm4geHMuc3BsaWNlKGksIDEpO1xuICAgICAgfVxuICAgIH0sXG4gICAgZm9sZDogZnVuY3Rpb24oeHMsIHNlZWQsIGYpIHtcbiAgICAgIHZhciB4LCBfaSwgX2xlbjtcbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0geHMubGVuZ3RoOyBfaSA8IF9sZW47IF9pKyspIHtcbiAgICAgICAgeCA9IHhzW19pXTtcbiAgICAgICAgc2VlZCA9IGYoc2VlZCwgeCk7XG4gICAgICB9XG4gICAgICByZXR1cm4gc2VlZDtcbiAgICB9LFxuICAgIGZsYXRNYXA6IGZ1bmN0aW9uKGYsIHhzKSB7XG4gICAgICByZXR1cm4gXy5mb2xkKHhzLCBbXSwgKGZ1bmN0aW9uKHlzLCB4KSB7XG4gICAgICAgIHJldHVybiB5cy5jb25jYXQoZih4KSk7XG4gICAgICB9KSk7XG4gICAgfSxcbiAgICBjYWNoZWQ6IGZ1bmN0aW9uKGYpIHtcbiAgICAgIHZhciB2YWx1ZTtcbiAgICAgIHZhbHVlID0gTm9uZTtcbiAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgaWYgKHZhbHVlID09PSBOb25lKSB7XG4gICAgICAgICAgdmFsdWUgPSBmKCk7XG4gICAgICAgICAgZiA9IHZvaWQgMDtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gdmFsdWU7XG4gICAgICB9O1xuICAgIH0sXG4gICAgdG9TdHJpbmc6IGZ1bmN0aW9uKG9iaikge1xuICAgICAgdmFyIGV4LCBpbnRlcm5hbHMsIGtleSwgdmFsdWU7XG4gICAgICB0cnkge1xuICAgICAgICByZWN1cnNpb25EZXB0aCsrO1xuICAgICAgICBpZiAob2JqID09IG51bGwpIHtcbiAgICAgICAgICByZXR1cm4gXCJ1bmRlZmluZWRcIjtcbiAgICAgICAgfSBlbHNlIGlmIChpc0Z1bmN0aW9uKG9iaikpIHtcbiAgICAgICAgICByZXR1cm4gXCJmdW5jdGlvblwiO1xuICAgICAgICB9IGVsc2UgaWYgKGlzQXJyYXkob2JqKSkge1xuICAgICAgICAgIGlmIChyZWN1cnNpb25EZXB0aCA+IDUpIHtcbiAgICAgICAgICAgIHJldHVybiBcIlsuLl1cIjtcbiAgICAgICAgICB9XG4gICAgICAgICAgcmV0dXJuIFwiW1wiICsgXy5tYXAoXy50b1N0cmluZywgb2JqKS50b1N0cmluZygpICsgXCJdXCI7XG4gICAgICAgIH0gZWxzZSBpZiAoKChvYmogIT0gbnVsbCA/IG9iai50b1N0cmluZyA6IHZvaWQgMCkgIT0gbnVsbCkgJiYgb2JqLnRvU3RyaW5nICE9PSBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nKSB7XG4gICAgICAgICAgcmV0dXJuIG9iai50b1N0cmluZygpO1xuICAgICAgICB9IGVsc2UgaWYgKHR5cGVvZiBvYmogPT09IFwib2JqZWN0XCIpIHtcbiAgICAgICAgICBpZiAocmVjdXJzaW9uRGVwdGggPiA1KSB7XG4gICAgICAgICAgICByZXR1cm4gXCJ7Li59XCI7XG4gICAgICAgICAgfVxuICAgICAgICAgIGludGVybmFscyA9IChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIHZhciBfcmVzdWx0cztcbiAgICAgICAgICAgIF9yZXN1bHRzID0gW107XG4gICAgICAgICAgICBmb3IgKGtleSBpbiBvYmopIHtcbiAgICAgICAgICAgICAgaWYgKCFfX2hhc1Byb3AuY2FsbChvYmosIGtleSkpIGNvbnRpbnVlO1xuICAgICAgICAgICAgICB2YWx1ZSA9IChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgICAgICB0cnkge1xuICAgICAgICAgICAgICAgICAgcmV0dXJuIG9ialtrZXldO1xuICAgICAgICAgICAgICAgIH0gY2F0Y2ggKF9lcnJvcikge1xuICAgICAgICAgICAgICAgICAgZXggPSBfZXJyb3I7XG4gICAgICAgICAgICAgICAgICByZXR1cm4gZXg7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICB9KSgpO1xuICAgICAgICAgICAgICBfcmVzdWx0cy5wdXNoKF8udG9TdHJpbmcoa2V5KSArIFwiOlwiICsgXy50b1N0cmluZyh2YWx1ZSkpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgICAgcmV0dXJuIF9yZXN1bHRzO1xuICAgICAgICAgIH0pKCk7XG4gICAgICAgICAgcmV0dXJuIFwie1wiICsgaW50ZXJuYWxzICsgXCJ9XCI7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmV0dXJuIG9iajtcbiAgICAgICAgfVxuICAgICAgfSBmaW5hbGx5IHtcbiAgICAgICAgcmVjdXJzaW9uRGVwdGgtLTtcbiAgICAgIH1cbiAgICB9XG4gIH07XG5cbiAgcmVjdXJzaW9uRGVwdGggPSAwO1xuXG4gIEJhY29uLl8gPSBfO1xuXG4gIEJhY29uLnNjaGVkdWxlciA9IHtcbiAgICBzZXRUaW1lb3V0OiBmdW5jdGlvbihmLCBkKSB7XG4gICAgICByZXR1cm4gc2V0VGltZW91dChmLCBkKTtcbiAgICB9LFxuICAgIHNldEludGVydmFsOiBmdW5jdGlvbihmLCBpKSB7XG4gICAgICByZXR1cm4gc2V0SW50ZXJ2YWwoZiwgaSk7XG4gICAgfSxcbiAgICBjbGVhckludGVydmFsOiBmdW5jdGlvbihpZCkge1xuICAgICAgcmV0dXJuIGNsZWFySW50ZXJ2YWwoaWQpO1xuICAgIH0sXG4gICAgbm93OiBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBuZXcgRGF0ZSgpLmdldFRpbWUoKTtcbiAgICB9XG4gIH07XG5cbiAgQmFjb24uZnJvbUJpbmRlciA9IGZ1bmN0aW9uKGJpbmRlciwgZXZlbnRUcmFuc2Zvcm1lcikge1xuICAgIGlmIChldmVudFRyYW5zZm9ybWVyID09IG51bGwpIHtcbiAgICAgIGV2ZW50VHJhbnNmb3JtZXIgPSBfLmlkO1xuICAgIH1cbiAgICByZXR1cm4gbmV3IEV2ZW50U3RyZWFtKGRlc2NyaWJlKEJhY29uLCBcImZyb21CaW5kZXJcIiwgYmluZGVyLCBldmVudFRyYW5zZm9ybWVyKSwgZnVuY3Rpb24oc2luaykge1xuICAgICAgdmFyIG5lZWRzVW5iaW5kLCB1bmJpbmQsIHVuYmluZGVyLCB1bmJvdW5kO1xuICAgICAgdW5ib3VuZCA9IGZhbHNlO1xuICAgICAgbmVlZHNVbmJpbmQgPSBmYWxzZTtcbiAgICAgIHVuYmluZCA9IGZ1bmN0aW9uKCkge1xuICAgICAgICBpZiAoIXVuYm91bmQpIHtcbiAgICAgICAgICBpZiAodHlwZW9mIHVuYmluZGVyICE9PSBcInVuZGVmaW5lZFwiICYmIHVuYmluZGVyICE9PSBudWxsKSB7XG4gICAgICAgICAgICB1bmJpbmRlcigpO1xuICAgICAgICAgICAgcmV0dXJuIHVuYm91bmQgPSB0cnVlO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4gbmVlZHNVbmJpbmQgPSB0cnVlO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIHVuYmluZGVyID0gYmluZGVyKGZ1bmN0aW9uKCkge1xuICAgICAgICB2YXIgYXJncywgZXZlbnQsIHJlcGx5LCB2YWx1ZSwgX2ksIF9sZW47XG4gICAgICAgIGFyZ3MgPSAxIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAwKSA6IFtdO1xuICAgICAgICB2YWx1ZSA9IGV2ZW50VHJhbnNmb3JtZXIuYXBwbHkodGhpcywgYXJncyk7XG4gICAgICAgIGlmICghKGlzQXJyYXkodmFsdWUpICYmIF8ubGFzdCh2YWx1ZSkgaW5zdGFuY2VvZiBFdmVudCkpIHtcbiAgICAgICAgICB2YWx1ZSA9IFt2YWx1ZV07XG4gICAgICAgIH1cbiAgICAgICAgcmVwbHkgPSBCYWNvbi5tb3JlO1xuICAgICAgICBmb3IgKF9pID0gMCwgX2xlbiA9IHZhbHVlLmxlbmd0aDsgX2kgPCBfbGVuOyBfaSsrKSB7XG4gICAgICAgICAgZXZlbnQgPSB2YWx1ZVtfaV07XG4gICAgICAgICAgcmVwbHkgPSBzaW5rKGV2ZW50ID0gdG9FdmVudChldmVudCkpO1xuICAgICAgICAgIGlmIChyZXBseSA9PT0gQmFjb24ubm9Nb3JlIHx8IGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgIHVuYmluZCgpO1xuICAgICAgICAgICAgcmV0dXJuIHJlcGx5O1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gcmVwbHk7XG4gICAgICB9KTtcbiAgICAgIGlmIChuZWVkc1VuYmluZCkge1xuICAgICAgICB1bmJpbmQoKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiB1bmJpbmQ7XG4gICAgfSk7XG4gIH07XG5cbiAgQmFjb24uJCA9IHt9O1xuXG4gIEJhY29uLiQuYXNFdmVudFN0cmVhbSA9IGZ1bmN0aW9uKGV2ZW50TmFtZSwgc2VsZWN0b3IsIGV2ZW50VHJhbnNmb3JtZXIpIHtcbiAgICB2YXIgX3JlZjtcbiAgICBpZiAoaXNGdW5jdGlvbihzZWxlY3RvcikpIHtcbiAgICAgIF9yZWYgPSBbc2VsZWN0b3IsIHZvaWQgMF0sIGV2ZW50VHJhbnNmb3JtZXIgPSBfcmVmWzBdLCBzZWxlY3RvciA9IF9yZWZbMV07XG4gICAgfVxuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcy5zZWxlY3RvciB8fCB0aGlzLCBcImFzRXZlbnRTdHJlYW1cIiwgZXZlbnROYW1lLCBCYWNvbi5mcm9tQmluZGVyKChmdW5jdGlvbihfdGhpcykge1xuICAgICAgcmV0dXJuIGZ1bmN0aW9uKGhhbmRsZXIpIHtcbiAgICAgICAgX3RoaXMub24oZXZlbnROYW1lLCBzZWxlY3RvciwgaGFuZGxlcik7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gX3RoaXMub2ZmKGV2ZW50TmFtZSwgc2VsZWN0b3IsIGhhbmRsZXIpO1xuICAgICAgICB9O1xuICAgICAgfTtcbiAgICB9KSh0aGlzKSwgZXZlbnRUcmFuc2Zvcm1lcikpO1xuICB9O1xuXG4gIGlmICgoX3JlZiA9IHR5cGVvZiBqUXVlcnkgIT09IFwidW5kZWZpbmVkXCIgJiYgalF1ZXJ5ICE9PSBudWxsID8galF1ZXJ5IDogdHlwZW9mIFplcHRvICE9PSBcInVuZGVmaW5lZFwiICYmIFplcHRvICE9PSBudWxsID8gWmVwdG8gOiB2b2lkIDApICE9IG51bGwpIHtcbiAgICBfcmVmLmZuLmFzRXZlbnRTdHJlYW0gPSBCYWNvbi4kLmFzRXZlbnRTdHJlYW07XG4gIH1cblxuICBCYWNvbi5mcm9tRXZlbnRUYXJnZXQgPSBmdW5jdGlvbih0YXJnZXQsIGV2ZW50TmFtZSwgZXZlbnRUcmFuc2Zvcm1lcikge1xuICAgIHZhciBzdWIsIHVuc3ViLCBfcmVmMSwgX3JlZjIsIF9yZWYzLCBfcmVmNCwgX3JlZjUsIF9yZWY2O1xuICAgIHN1YiA9IChfcmVmMSA9IChfcmVmMiA9IChfcmVmMyA9IHRhcmdldC5hZGRFdmVudExpc3RlbmVyKSAhPSBudWxsID8gX3JlZjMgOiB0YXJnZXQuYWRkTGlzdGVuZXIpICE9IG51bGwgPyBfcmVmMiA6IHRhcmdldC5iaW5kKSAhPSBudWxsID8gX3JlZjEgOiB0YXJnZXQub247XG4gICAgdW5zdWIgPSAoX3JlZjQgPSAoX3JlZjUgPSAoX3JlZjYgPSB0YXJnZXQucmVtb3ZlRXZlbnRMaXN0ZW5lcikgIT0gbnVsbCA/IF9yZWY2IDogdGFyZ2V0LnJlbW92ZUxpc3RlbmVyKSAhPSBudWxsID8gX3JlZjUgOiB0YXJnZXQudW5iaW5kKSAhPSBudWxsID8gX3JlZjQgOiB0YXJnZXQub2ZmO1xuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24oQmFjb24sIFwiZnJvbUV2ZW50VGFyZ2V0XCIsIHRhcmdldCwgZXZlbnROYW1lLCBCYWNvbi5mcm9tQmluZGVyKGZ1bmN0aW9uKGhhbmRsZXIpIHtcbiAgICAgIHN1Yi5jYWxsKHRhcmdldCwgZXZlbnROYW1lLCBoYW5kbGVyKTtcbiAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgcmV0dXJuIHVuc3ViLmNhbGwodGFyZ2V0LCBldmVudE5hbWUsIGhhbmRsZXIpO1xuICAgICAgfTtcbiAgICB9LCBldmVudFRyYW5zZm9ybWVyKSk7XG4gIH07XG5cbiAgQmFjb24uZnJvbVByb21pc2UgPSBmdW5jdGlvbihwcm9taXNlLCBhYm9ydCkge1xuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24oQmFjb24sIFwiZnJvbVByb21pc2VcIiwgcHJvbWlzZSwgQmFjb24uZnJvbUJpbmRlcihmdW5jdGlvbihoYW5kbGVyKSB7XG4gICAgICBwcm9taXNlLnRoZW4oaGFuZGxlciwgZnVuY3Rpb24oZSkge1xuICAgICAgICByZXR1cm4gaGFuZGxlcihuZXcgRXJyb3IoZSkpO1xuICAgICAgfSk7XG4gICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgIGlmIChhYm9ydCkge1xuICAgICAgICAgIHJldHVybiB0eXBlb2YgcHJvbWlzZS5hYm9ydCA9PT0gXCJmdW5jdGlvblwiID8gcHJvbWlzZS5hYm9ydCgpIDogdm9pZCAwO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgIH0sIChmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgcmV0dXJuIFt2YWx1ZSwgZW5kKCldO1xuICAgIH0pKSk7XG4gIH07XG5cbiAgQmFjb24ubm9Nb3JlID0gW1wiPG5vLW1vcmU+XCJdO1xuXG4gIEJhY29uLm1vcmUgPSBbXCI8bW9yZT5cIl07XG5cbiAgQmFjb24ubGF0ZXIgPSBmdW5jdGlvbihkZWxheSwgdmFsdWUpIHtcbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKEJhY29uLCBcImxhdGVyXCIsIGRlbGF5LCB2YWx1ZSwgQmFjb24uZnJvbVBvbGwoZGVsYXksIGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIFt2YWx1ZSwgZW5kKCldO1xuICAgIH0pKTtcbiAgfTtcblxuICBCYWNvbi5zZXF1ZW50aWFsbHkgPSBmdW5jdGlvbihkZWxheSwgdmFsdWVzKSB7XG4gICAgdmFyIGluZGV4O1xuICAgIGluZGV4ID0gMDtcbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKEJhY29uLCBcInNlcXVlbnRpYWxseVwiLCBkZWxheSwgdmFsdWVzLCBCYWNvbi5mcm9tUG9sbChkZWxheSwgZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgdmFsdWU7XG4gICAgICB2YWx1ZSA9IHZhbHVlc1tpbmRleCsrXTtcbiAgICAgIGlmIChpbmRleCA8IHZhbHVlcy5sZW5ndGgpIHtcbiAgICAgICAgcmV0dXJuIHZhbHVlO1xuICAgICAgfSBlbHNlIGlmIChpbmRleCA9PT0gdmFsdWVzLmxlbmd0aCkge1xuICAgICAgICByZXR1cm4gW3ZhbHVlLCBlbmQoKV07XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gZW5kKCk7XG4gICAgICB9XG4gICAgfSkpO1xuICB9O1xuXG4gIEJhY29uLnJlcGVhdGVkbHkgPSBmdW5jdGlvbihkZWxheSwgdmFsdWVzKSB7XG4gICAgdmFyIGluZGV4O1xuICAgIGluZGV4ID0gMDtcbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKEJhY29uLCBcInJlcGVhdGVkbHlcIiwgZGVsYXksIHZhbHVlcywgQmFjb24uZnJvbVBvbGwoZGVsYXksIGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHZhbHVlc1tpbmRleCsrICUgdmFsdWVzLmxlbmd0aF07XG4gICAgfSkpO1xuICB9O1xuXG4gIEJhY29uLnNweSA9IGZ1bmN0aW9uKHNweSkge1xuICAgIHJldHVybiBzcHlzLnB1c2goc3B5KTtcbiAgfTtcblxuICBzcHlzID0gW107XG5cbiAgcmVnaXN0ZXJPYnMgPSBmdW5jdGlvbihvYnMpIHtcbiAgICB2YXIgc3B5LCBfaSwgX2xlbjtcbiAgICBpZiAoc3B5cy5sZW5ndGgpIHtcbiAgICAgIGlmICghcmVnaXN0ZXJPYnMucnVubmluZykge1xuICAgICAgICB0cnkge1xuICAgICAgICAgIHJlZ2lzdGVyT2JzLnJ1bm5pbmcgPSB0cnVlO1xuICAgICAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gc3B5cy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICAgICAgc3B5ID0gc3B5c1tfaV07XG4gICAgICAgICAgICBzcHkob2JzKTtcbiAgICAgICAgICB9XG4gICAgICAgIH0gZmluYWxseSB7XG4gICAgICAgICAgZGVsZXRlIHJlZ2lzdGVyT2JzLnJ1bm5pbmc7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9XG4gICAgcmV0dXJuIHZvaWQgMDtcbiAgfTtcblxuICB3aXRoTWV0aG9kQ2FsbFN1cHBvcnQgPSBmdW5jdGlvbih3cmFwcGVkKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKCkge1xuICAgICAgdmFyIGFyZ3MsIGNvbnRleHQsIGYsIG1ldGhvZE5hbWU7XG4gICAgICBmID0gYXJndW1lbnRzWzBdLCBhcmdzID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMSkgOiBbXTtcbiAgICAgIGlmICh0eXBlb2YgZiA9PT0gXCJvYmplY3RcIiAmJiBhcmdzLmxlbmd0aCkge1xuICAgICAgICBjb250ZXh0ID0gZjtcbiAgICAgICAgbWV0aG9kTmFtZSA9IGFyZ3NbMF07XG4gICAgICAgIGYgPSBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gY29udGV4dFttZXRob2ROYW1lXS5hcHBseShjb250ZXh0LCBhcmd1bWVudHMpO1xuICAgICAgICB9O1xuICAgICAgICBhcmdzID0gYXJncy5zbGljZSgxKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiB3cmFwcGVkLmFwcGx5KG51bGwsIFtmXS5jb25jYXQoX19zbGljZS5jYWxsKGFyZ3MpKSk7XG4gICAgfTtcbiAgfTtcblxuICBsaWZ0Q2FsbGJhY2sgPSBmdW5jdGlvbihkZXNjLCB3cmFwcGVkKSB7XG4gICAgcmV0dXJuIHdpdGhNZXRob2RDYWxsU3VwcG9ydChmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzLCBmLCBzdHJlYW07XG4gICAgICBmID0gYXJndW1lbnRzWzBdLCBhcmdzID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMSkgOiBbXTtcbiAgICAgIHN0cmVhbSA9IHBhcnRpYWxseUFwcGxpZWQod3JhcHBlZCwgW1xuICAgICAgICBmdW5jdGlvbih2YWx1ZXMsIGNhbGxiYWNrKSB7XG4gICAgICAgICAgcmV0dXJuIGYuYXBwbHkobnVsbCwgX19zbGljZS5jYWxsKHZhbHVlcykuY29uY2F0KFtjYWxsYmFja10pKTtcbiAgICAgICAgfVxuICAgICAgXSk7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uLmFwcGx5KG51bGwsIFtCYWNvbiwgZGVzYywgZl0uY29uY2F0KF9fc2xpY2UuY2FsbChhcmdzKSwgW0JhY29uLmNvbWJpbmVBc0FycmF5KGFyZ3MpLmZsYXRNYXAoc3RyZWFtKV0pKTtcbiAgICB9KTtcbiAgfTtcblxuICBCYWNvbi5mcm9tQ2FsbGJhY2sgPSBsaWZ0Q2FsbGJhY2soXCJmcm9tQ2FsbGJhY2tcIiwgZnVuY3Rpb24oKSB7XG4gICAgdmFyIGFyZ3MsIGY7XG4gICAgZiA9IGFyZ3VtZW50c1swXSwgYXJncyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgcmV0dXJuIEJhY29uLmZyb21CaW5kZXIoZnVuY3Rpb24oaGFuZGxlcikge1xuICAgICAgbWFrZUZ1bmN0aW9uKGYsIGFyZ3MpKGhhbmRsZXIpO1xuICAgICAgcmV0dXJuIG5vcDtcbiAgICB9LCAoZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgIHJldHVybiBbdmFsdWUsIGVuZCgpXTtcbiAgICB9KSk7XG4gIH0pO1xuXG4gIEJhY29uLmZyb21Ob2RlQ2FsbGJhY2sgPSBsaWZ0Q2FsbGJhY2soXCJmcm9tTm9kZUNhbGxiYWNrXCIsIGZ1bmN0aW9uKCkge1xuICAgIHZhciBhcmdzLCBmO1xuICAgIGYgPSBhcmd1bWVudHNbMF0sIGFyZ3MgPSAyIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAxKSA6IFtdO1xuICAgIHJldHVybiBCYWNvbi5mcm9tQmluZGVyKGZ1bmN0aW9uKGhhbmRsZXIpIHtcbiAgICAgIG1ha2VGdW5jdGlvbihmLCBhcmdzKShoYW5kbGVyKTtcbiAgICAgIHJldHVybiBub3A7XG4gICAgfSwgZnVuY3Rpb24oZXJyb3IsIHZhbHVlKSB7XG4gICAgICBpZiAoZXJyb3IpIHtcbiAgICAgICAgcmV0dXJuIFtuZXcgRXJyb3IoZXJyb3IpLCBlbmQoKV07XG4gICAgICB9XG4gICAgICByZXR1cm4gW3ZhbHVlLCBlbmQoKV07XG4gICAgfSk7XG4gIH0pO1xuXG4gIEJhY29uLmZyb21Qb2xsID0gZnVuY3Rpb24oZGVsYXksIHBvbGwpIHtcbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKEJhY29uLCBcImZyb21Qb2xsXCIsIGRlbGF5LCBwb2xsLCBCYWNvbi5mcm9tQmluZGVyKChmdW5jdGlvbihoYW5kbGVyKSB7XG4gICAgICB2YXIgaWQ7XG4gICAgICBpZCA9IEJhY29uLnNjaGVkdWxlci5zZXRJbnRlcnZhbChoYW5kbGVyLCBkZWxheSk7XG4gICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiBCYWNvbi5zY2hlZHVsZXIuY2xlYXJJbnRlcnZhbChpZCk7XG4gICAgICB9O1xuICAgIH0pLCBwb2xsKSk7XG4gIH07XG5cbiAgQmFjb24uaW50ZXJ2YWwgPSBmdW5jdGlvbihkZWxheSwgdmFsdWUpIHtcbiAgICBpZiAodmFsdWUgPT0gbnVsbCkge1xuICAgICAgdmFsdWUgPSB7fTtcbiAgICB9XG4gICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbihCYWNvbiwgXCJpbnRlcnZhbFwiLCBkZWxheSwgdmFsdWUsIEJhY29uLmZyb21Qb2xsKGRlbGF5LCBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBuZXh0KHZhbHVlKTtcbiAgICB9KSk7XG4gIH07XG5cbiAgQmFjb24uY29uc3RhbnQgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgIHJldHVybiBuZXcgUHJvcGVydHkoZGVzY3JpYmUoQmFjb24sIFwiY29uc3RhbnRcIiwgdmFsdWUpLCBmdW5jdGlvbihzaW5rKSB7XG4gICAgICBzaW5rKGluaXRpYWwodmFsdWUpKTtcbiAgICAgIHNpbmsoZW5kKCkpO1xuICAgICAgcmV0dXJuIG5vcDtcbiAgICB9KTtcbiAgfTtcblxuICBCYWNvbi5uZXZlciA9IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUoQmFjb24sIFwibmV2ZXJcIiksIGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgIHNpbmsoZW5kKCkpO1xuICAgICAgcmV0dXJuIG5vcDtcbiAgICB9KTtcbiAgfTtcblxuICBCYWNvbi5vbmNlID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gbmV3IEV2ZW50U3RyZWFtKGRlc2NyaWJlKEJhY29uLCBcIm9uY2VcIiwgdmFsdWUpLCBmdW5jdGlvbihzaW5rKSB7XG4gICAgICBzaW5rKHRvRXZlbnQodmFsdWUpKTtcbiAgICAgIHNpbmsoZW5kKCkpO1xuICAgICAgcmV0dXJuIG5vcDtcbiAgICB9KTtcbiAgfTtcblxuICBCYWNvbi5mcm9tQXJyYXkgPSBmdW5jdGlvbih2YWx1ZXMpIHtcbiAgICB2YXIgaTtcbiAgICBhc3NlcnRBcnJheSh2YWx1ZXMpO1xuICAgIGlmICghdmFsdWVzLmxlbmd0aCkge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbihCYWNvbiwgXCJmcm9tQXJyYXlcIiwgdmFsdWVzLCBCYWNvbi5uZXZlcigpKTtcbiAgICB9IGVsc2Uge1xuICAgICAgaSA9IDA7XG4gICAgICByZXR1cm4gbmV3IEV2ZW50U3RyZWFtKGRlc2NyaWJlKEJhY29uLCBcImZyb21BcnJheVwiLCB2YWx1ZXMpLCBmdW5jdGlvbihzaW5rKSB7XG4gICAgICAgIHZhciBwdXNoLCByZXBseSwgdW5zdWJkO1xuICAgICAgICB1bnN1YmQgPSBmYWxzZTtcbiAgICAgICAgcmVwbHkgPSBCYWNvbi5tb3JlO1xuICAgICAgICBwdXNoID0gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgdmFyIHZhbHVlO1xuICAgICAgICAgIGlmICgocmVwbHkgIT09IEJhY29uLm5vTW9yZSkgJiYgIXVuc3ViZCkge1xuICAgICAgICAgICAgdmFsdWUgPSB2YWx1ZXNbaSsrXTtcbiAgICAgICAgICAgIHJlcGx5ID0gc2luayh0b0V2ZW50KHZhbHVlKSk7XG4gICAgICAgICAgICBpZiAocmVwbHkgIT09IEJhY29uLm5vTW9yZSkge1xuICAgICAgICAgICAgICBpZiAoaSA9PT0gdmFsdWVzLmxlbmd0aCkge1xuICAgICAgICAgICAgICAgIHJldHVybiBzaW5rKGVuZCgpKTtcbiAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gVXBkYXRlQmFycmllci5hZnRlclRyYW5zYWN0aW9uKHB1c2gpO1xuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfVxuICAgICAgICB9O1xuICAgICAgICBwdXNoKCk7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gdW5zdWJkID0gdHJ1ZTtcbiAgICAgICAgfTtcbiAgICAgIH0pO1xuICAgIH1cbiAgfTtcblxuICBCYWNvbi5tZXJnZUFsbCA9IGZ1bmN0aW9uKCkge1xuICAgIHZhciBzdHJlYW1zO1xuICAgIHN0cmVhbXMgPSAxIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAwKSA6IFtdO1xuICAgIGlmIChpc0FycmF5KHN0cmVhbXNbMF0pKSB7XG4gICAgICBzdHJlYW1zID0gc3RyZWFtc1swXTtcbiAgICB9XG4gICAgaWYgKHN0cmVhbXMubGVuZ3RoKSB7XG4gICAgICByZXR1cm4gbmV3IEV2ZW50U3RyZWFtKGRlc2NyaWJlLmFwcGx5KG51bGwsIFtCYWNvbiwgXCJtZXJnZUFsbFwiXS5jb25jYXQoX19zbGljZS5jYWxsKHN0cmVhbXMpKSksIGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgICAgdmFyIGVuZHMsIHNpbmtzLCBzbWFydFNpbms7XG4gICAgICAgIGVuZHMgPSAwO1xuICAgICAgICBzbWFydFNpbmsgPSBmdW5jdGlvbihvYnMpIHtcbiAgICAgICAgICByZXR1cm4gZnVuY3Rpb24odW5zdWJCb3RoKSB7XG4gICAgICAgICAgICByZXR1cm4gb2JzLmRpc3BhdGNoZXIuc3Vic2NyaWJlKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgICAgIHZhciByZXBseTtcbiAgICAgICAgICAgICAgaWYgKGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgICAgICBlbmRzKys7XG4gICAgICAgICAgICAgICAgaWYgKGVuZHMgPT09IHN0cmVhbXMubGVuZ3RoKSB7XG4gICAgICAgICAgICAgICAgICByZXR1cm4gc2luayhlbmQoKSk7XG4gICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgIHJldHVybiBCYWNvbi5tb3JlO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICByZXBseSA9IHNpbmsoZXZlbnQpO1xuICAgICAgICAgICAgICAgIGlmIChyZXBseSA9PT0gQmFjb24ubm9Nb3JlKSB7XG4gICAgICAgICAgICAgICAgICB1bnN1YkJvdGgoKTtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgICAgcmV0dXJuIHJlcGx5O1xuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgICB9O1xuICAgICAgICB9O1xuICAgICAgICBzaW5rcyA9IF8ubWFwKHNtYXJ0U2luaywgc3RyZWFtcyk7XG4gICAgICAgIHJldHVybiBjb21wb3NpdGVVbnN1YnNjcmliZS5hcHBseShudWxsLCBzaW5rcyk7XG4gICAgICB9KTtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIEJhY29uLm5ldmVyKCk7XG4gICAgfVxuICB9O1xuXG4gIEJhY29uLnppcEFzQXJyYXkgPSBmdW5jdGlvbigpIHtcbiAgICB2YXIgc3RyZWFtcztcbiAgICBzdHJlYW1zID0gMSA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCkgOiBbXTtcbiAgICBpZiAoaXNBcnJheShzdHJlYW1zWzBdKSkge1xuICAgICAgc3RyZWFtcyA9IHN0cmVhbXNbMF07XG4gICAgfVxuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24uYXBwbHkobnVsbCwgW0JhY29uLCBcInppcEFzQXJyYXlcIl0uY29uY2F0KF9fc2xpY2UuY2FsbChzdHJlYW1zKSwgW0JhY29uLnppcFdpdGgoc3RyZWFtcywgZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgeHM7XG4gICAgICB4cyA9IDEgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDApIDogW107XG4gICAgICByZXR1cm4geHM7XG4gICAgfSldKSk7XG4gIH07XG5cbiAgQmFjb24uemlwV2l0aCA9IGZ1bmN0aW9uKCkge1xuICAgIHZhciBmLCBzdHJlYW1zLCBfcmVmMTtcbiAgICBmID0gYXJndW1lbnRzWzBdLCBzdHJlYW1zID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMSkgOiBbXTtcbiAgICBpZiAoIWlzRnVuY3Rpb24oZikpIHtcbiAgICAgIF9yZWYxID0gW2YsIHN0cmVhbXNbMF1dLCBzdHJlYW1zID0gX3JlZjFbMF0sIGYgPSBfcmVmMVsxXTtcbiAgICB9XG4gICAgc3RyZWFtcyA9IF8ubWFwKChmdW5jdGlvbihzKSB7XG4gICAgICByZXR1cm4gcy50b0V2ZW50U3RyZWFtKCk7XG4gICAgfSksIHN0cmVhbXMpO1xuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24uYXBwbHkobnVsbCwgW0JhY29uLCBcInppcFdpdGhcIiwgZl0uY29uY2F0KF9fc2xpY2UuY2FsbChzdHJlYW1zKSwgW0JhY29uLndoZW4oc3RyZWFtcywgZildKSk7XG4gIH07XG5cbiAgQmFjb24uZ3JvdXBTaW11bHRhbmVvdXMgPSBmdW5jdGlvbigpIHtcbiAgICB2YXIgcywgc291cmNlcywgc3RyZWFtcztcbiAgICBzdHJlYW1zID0gMSA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCkgOiBbXTtcbiAgICBpZiAoc3RyZWFtcy5sZW5ndGggPT09IDEgJiYgaXNBcnJheShzdHJlYW1zWzBdKSkge1xuICAgICAgc3RyZWFtcyA9IHN0cmVhbXNbMF07XG4gICAgfVxuICAgIHNvdXJjZXMgPSAoZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgX2ksIF9sZW4sIF9yZXN1bHRzO1xuICAgICAgX3Jlc3VsdHMgPSBbXTtcbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gc3RyZWFtcy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBzID0gc3RyZWFtc1tfaV07XG4gICAgICAgIF9yZXN1bHRzLnB1c2gobmV3IEJ1ZmZlcmluZ1NvdXJjZShzKSk7XG4gICAgICB9XG4gICAgICByZXR1cm4gX3Jlc3VsdHM7XG4gICAgfSkoKTtcbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uLmFwcGx5KG51bGwsIFtCYWNvbiwgXCJncm91cFNpbXVsdGFuZW91c1wiXS5jb25jYXQoX19zbGljZS5jYWxsKHN0cmVhbXMpLCBbQmFjb24ud2hlbihzb3VyY2VzLCAoZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgeHM7XG4gICAgICB4cyA9IDEgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDApIDogW107XG4gICAgICByZXR1cm4geHM7XG4gICAgfSkpXSkpO1xuICB9O1xuXG4gIEJhY29uLmNvbWJpbmVBc0FycmF5ID0gZnVuY3Rpb24oKSB7XG4gICAgdmFyIGluZGV4LCBzLCBzb3VyY2VzLCBzdHJlYW0sIHN0cmVhbXMsIF9pLCBfbGVuO1xuICAgIHN0cmVhbXMgPSAxIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAwKSA6IFtdO1xuICAgIGlmIChzdHJlYW1zLmxlbmd0aCA9PT0gMSAmJiBpc0FycmF5KHN0cmVhbXNbMF0pKSB7XG4gICAgICBzdHJlYW1zID0gc3RyZWFtc1swXTtcbiAgICB9XG4gICAgZm9yIChpbmRleCA9IF9pID0gMCwgX2xlbiA9IHN0cmVhbXMubGVuZ3RoOyBfaSA8IF9sZW47IGluZGV4ID0gKytfaSkge1xuICAgICAgc3RyZWFtID0gc3RyZWFtc1tpbmRleF07XG4gICAgICBpZiAoIShpc09ic2VydmFibGUoc3RyZWFtKSkpIHtcbiAgICAgICAgc3RyZWFtc1tpbmRleF0gPSBCYWNvbi5jb25zdGFudChzdHJlYW0pO1xuICAgICAgfVxuICAgIH1cbiAgICBpZiAoc3RyZWFtcy5sZW5ndGgpIHtcbiAgICAgIHNvdXJjZXMgPSAoZnVuY3Rpb24oKSB7XG4gICAgICAgIHZhciBfaiwgX2xlbjEsIF9yZXN1bHRzO1xuICAgICAgICBfcmVzdWx0cyA9IFtdO1xuICAgICAgICBmb3IgKF9qID0gMCwgX2xlbjEgPSBzdHJlYW1zLmxlbmd0aDsgX2ogPCBfbGVuMTsgX2orKykge1xuICAgICAgICAgIHMgPSBzdHJlYW1zW19qXTtcbiAgICAgICAgICBfcmVzdWx0cy5wdXNoKG5ldyBTb3VyY2UocywgdHJ1ZSkpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiBfcmVzdWx0cztcbiAgICAgIH0pKCk7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uLmFwcGx5KG51bGwsIFtCYWNvbiwgXCJjb21iaW5lQXNBcnJheVwiXS5jb25jYXQoX19zbGljZS5jYWxsKHN0cmVhbXMpLCBbQmFjb24ud2hlbihzb3VyY2VzLCAoZnVuY3Rpb24oKSB7XG4gICAgICAgIHZhciB4cztcbiAgICAgICAgeHMgPSAxIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAwKSA6IFtdO1xuICAgICAgICByZXR1cm4geHM7XG4gICAgICB9KSkudG9Qcm9wZXJ0eSgpXSkpO1xuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gQmFjb24uY29uc3RhbnQoW10pO1xuICAgIH1cbiAgfTtcblxuICBCYWNvbi5vblZhbHVlcyA9IGZ1bmN0aW9uKCkge1xuICAgIHZhciBmLCBzdHJlYW1zLCBfaTtcbiAgICBzdHJlYW1zID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCwgX2kgPSBhcmd1bWVudHMubGVuZ3RoIC0gMSkgOiAoX2kgPSAwLCBbXSksIGYgPSBhcmd1bWVudHNbX2krK107XG4gICAgcmV0dXJuIEJhY29uLmNvbWJpbmVBc0FycmF5KHN0cmVhbXMpLm9uVmFsdWVzKGYpO1xuICB9O1xuXG4gIEJhY29uLmNvbWJpbmVXaXRoID0gZnVuY3Rpb24oKSB7XG4gICAgdmFyIGYsIHN0cmVhbXM7XG4gICAgZiA9IGFyZ3VtZW50c1swXSwgc3RyZWFtcyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbi5hcHBseShudWxsLCBbQmFjb24sIFwiY29tYmluZVdpdGhcIiwgZl0uY29uY2F0KF9fc2xpY2UuY2FsbChzdHJlYW1zKSwgW0JhY29uLmNvbWJpbmVBc0FycmF5KHN0cmVhbXMpLm1hcChmdW5jdGlvbih2YWx1ZXMpIHtcbiAgICAgIHJldHVybiBmLmFwcGx5KG51bGwsIHZhbHVlcyk7XG4gICAgfSldKSk7XG4gIH07XG5cbiAgQmFjb24uY29tYmluZVRlbXBsYXRlID0gZnVuY3Rpb24odGVtcGxhdGUpIHtcbiAgICB2YXIgYXBwbHlTdHJlYW1WYWx1ZSwgY29tYmluYXRvciwgY29tcGlsZSwgY29tcGlsZVRlbXBsYXRlLCBjb25zdGFudFZhbHVlLCBjdXJyZW50LCBmdW5jcywgbWtDb250ZXh0LCBzZXRWYWx1ZSwgc3RyZWFtcztcbiAgICBmdW5jcyA9IFtdO1xuICAgIHN0cmVhbXMgPSBbXTtcbiAgICBjdXJyZW50ID0gZnVuY3Rpb24oY3R4U3RhY2spIHtcbiAgICAgIHJldHVybiBjdHhTdGFja1tjdHhTdGFjay5sZW5ndGggLSAxXTtcbiAgICB9O1xuICAgIHNldFZhbHVlID0gZnVuY3Rpb24oY3R4U3RhY2ssIGtleSwgdmFsdWUpIHtcbiAgICAgIHJldHVybiBjdXJyZW50KGN0eFN0YWNrKVtrZXldID0gdmFsdWU7XG4gICAgfTtcbiAgICBhcHBseVN0cmVhbVZhbHVlID0gZnVuY3Rpb24oa2V5LCBpbmRleCkge1xuICAgICAgcmV0dXJuIGZ1bmN0aW9uKGN0eFN0YWNrLCB2YWx1ZXMpIHtcbiAgICAgICAgcmV0dXJuIHNldFZhbHVlKGN0eFN0YWNrLCBrZXksIHZhbHVlc1tpbmRleF0pO1xuICAgICAgfTtcbiAgICB9O1xuICAgIGNvbnN0YW50VmFsdWUgPSBmdW5jdGlvbihrZXksIHZhbHVlKSB7XG4gICAgICByZXR1cm4gZnVuY3Rpb24oY3R4U3RhY2spIHtcbiAgICAgICAgcmV0dXJuIHNldFZhbHVlKGN0eFN0YWNrLCBrZXksIHZhbHVlKTtcbiAgICAgIH07XG4gICAgfTtcbiAgICBta0NvbnRleHQgPSBmdW5jdGlvbih0ZW1wbGF0ZSkge1xuICAgICAgaWYgKGlzQXJyYXkodGVtcGxhdGUpKSB7XG4gICAgICAgIHJldHVybiBbXTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiB7fTtcbiAgICAgIH1cbiAgICB9O1xuICAgIGNvbXBpbGUgPSBmdW5jdGlvbihrZXksIHZhbHVlKSB7XG4gICAgICB2YXIgcG9wQ29udGV4dCwgcHVzaENvbnRleHQ7XG4gICAgICBpZiAoaXNPYnNlcnZhYmxlKHZhbHVlKSkge1xuICAgICAgICBzdHJlYW1zLnB1c2godmFsdWUpO1xuICAgICAgICByZXR1cm4gZnVuY3MucHVzaChhcHBseVN0cmVhbVZhbHVlKGtleSwgc3RyZWFtcy5sZW5ndGggLSAxKSk7XG4gICAgICB9IGVsc2UgaWYgKHZhbHVlID09PSBPYmplY3QodmFsdWUpICYmIHR5cGVvZiB2YWx1ZSAhPT0gXCJmdW5jdGlvblwiICYmICEodmFsdWUgaW5zdGFuY2VvZiBSZWdFeHApICYmICEodmFsdWUgaW5zdGFuY2VvZiBEYXRlKSkge1xuICAgICAgICBwdXNoQ29udGV4dCA9IGZ1bmN0aW9uKGtleSkge1xuICAgICAgICAgIHJldHVybiBmdW5jdGlvbihjdHhTdGFjaykge1xuICAgICAgICAgICAgdmFyIG5ld0NvbnRleHQ7XG4gICAgICAgICAgICBuZXdDb250ZXh0ID0gbWtDb250ZXh0KHZhbHVlKTtcbiAgICAgICAgICAgIHNldFZhbHVlKGN0eFN0YWNrLCBrZXksIG5ld0NvbnRleHQpO1xuICAgICAgICAgICAgcmV0dXJuIGN0eFN0YWNrLnB1c2gobmV3Q29udGV4dCk7XG4gICAgICAgICAgfTtcbiAgICAgICAgfTtcbiAgICAgICAgcG9wQ29udGV4dCA9IGZ1bmN0aW9uKGN0eFN0YWNrKSB7XG4gICAgICAgICAgcmV0dXJuIGN0eFN0YWNrLnBvcCgpO1xuICAgICAgICB9O1xuICAgICAgICBmdW5jcy5wdXNoKHB1c2hDb250ZXh0KGtleSkpO1xuICAgICAgICBjb21waWxlVGVtcGxhdGUodmFsdWUpO1xuICAgICAgICByZXR1cm4gZnVuY3MucHVzaChwb3BDb250ZXh0KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBmdW5jcy5wdXNoKGNvbnN0YW50VmFsdWUoa2V5LCB2YWx1ZSkpO1xuICAgICAgfVxuICAgIH07XG4gICAgY29tcGlsZVRlbXBsYXRlID0gZnVuY3Rpb24odGVtcGxhdGUpIHtcbiAgICAgIHJldHVybiBfLmVhY2godGVtcGxhdGUsIGNvbXBpbGUpO1xuICAgIH07XG4gICAgY29tcGlsZVRlbXBsYXRlKHRlbXBsYXRlKTtcbiAgICBjb21iaW5hdG9yID0gZnVuY3Rpb24odmFsdWVzKSB7XG4gICAgICB2YXIgY3R4U3RhY2ssIGYsIHJvb3RDb250ZXh0LCBfaSwgX2xlbjtcbiAgICAgIHJvb3RDb250ZXh0ID0gbWtDb250ZXh0KHRlbXBsYXRlKTtcbiAgICAgIGN0eFN0YWNrID0gW3Jvb3RDb250ZXh0XTtcbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gZnVuY3MubGVuZ3RoOyBfaSA8IF9sZW47IF9pKyspIHtcbiAgICAgICAgZiA9IGZ1bmNzW19pXTtcbiAgICAgICAgZihjdHhTdGFjaywgdmFsdWVzKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiByb290Q29udGV4dDtcbiAgICB9O1xuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24oQmFjb24sIFwiY29tYmluZVRlbXBsYXRlXCIsIHRlbXBsYXRlLCBCYWNvbi5jb21iaW5lQXNBcnJheShzdHJlYW1zKS5tYXAoY29tYmluYXRvcikpO1xuICB9O1xuXG4gIEJhY29uLnJldHJ5ID0gZnVuY3Rpb24ob3B0aW9ucykge1xuICAgIHZhciBkZWxheSwgaXNSZXRyeWFibGUsIG1heFJldHJpZXMsIHJldHJpZXMsIHJldHJ5LCBzb3VyY2U7XG4gICAgaWYgKCFpc0Z1bmN0aW9uKG9wdGlvbnMuc291cmNlKSkge1xuICAgICAgdGhyb3cgbmV3IEV4Y2VwdGlvbihcIidzb3VyY2UnIG9wdGlvbiBoYXMgdG8gYmUgYSBmdW5jdGlvblwiKTtcbiAgICB9XG4gICAgc291cmNlID0gb3B0aW9ucy5zb3VyY2U7XG4gICAgcmV0cmllcyA9IG9wdGlvbnMucmV0cmllcyB8fCAwO1xuICAgIG1heFJldHJpZXMgPSBvcHRpb25zLm1heFJldHJpZXMgfHwgcmV0cmllcztcbiAgICBkZWxheSA9IG9wdGlvbnMuZGVsYXkgfHwgZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gMDtcbiAgICB9O1xuICAgIGlzUmV0cnlhYmxlID0gb3B0aW9ucy5pc1JldHJ5YWJsZSB8fCBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH07XG4gICAgcmV0cnkgPSBmdW5jdGlvbihjb250ZXh0KSB7XG4gICAgICB2YXIgZGVsYXllZFJldHJ5LCBuZXh0QXR0ZW1wdE9wdGlvbnM7XG4gICAgICBuZXh0QXR0ZW1wdE9wdGlvbnMgPSB7XG4gICAgICAgIHNvdXJjZTogc291cmNlLFxuICAgICAgICByZXRyaWVzOiByZXRyaWVzIC0gMSxcbiAgICAgICAgbWF4UmV0cmllczogbWF4UmV0cmllcyxcbiAgICAgICAgZGVsYXk6IGRlbGF5LFxuICAgICAgICBpc1JldHJ5YWJsZTogaXNSZXRyeWFibGVcbiAgICAgIH07XG4gICAgICBkZWxheWVkUmV0cnkgPSBmdW5jdGlvbigpIHtcbiAgICAgICAgcmV0dXJuIEJhY29uLnJldHJ5KG5leHRBdHRlbXB0T3B0aW9ucyk7XG4gICAgICB9O1xuICAgICAgcmV0dXJuIEJhY29uLmxhdGVyKGRlbGF5KGNvbnRleHQpKS5maWx0ZXIoZmFsc2UpLmNvbmNhdChCYWNvbi5vbmNlKCkuZmxhdE1hcChkZWxheWVkUmV0cnkpKTtcbiAgICB9O1xuICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24oQmFjb24sIFwicmV0cnlcIiwgb3B0aW9ucywgc291cmNlKCkuZmxhdE1hcEVycm9yKGZ1bmN0aW9uKGUpIHtcbiAgICAgIGlmIChpc1JldHJ5YWJsZShlKSAmJiByZXRyaWVzID4gMCkge1xuICAgICAgICByZXR1cm4gcmV0cnkoe1xuICAgICAgICAgIGVycm9yOiBlLFxuICAgICAgICAgIHJldHJpZXNEb25lOiBtYXhSZXRyaWVzIC0gcmV0cmllc1xuICAgICAgICB9KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBCYWNvbi5vbmNlKG5ldyBFcnJvcihlKSk7XG4gICAgICB9XG4gICAgfSkpO1xuICB9O1xuXG4gIGV2ZW50SWRDb3VudGVyID0gMDtcblxuICBFdmVudCA9IChmdW5jdGlvbigpIHtcbiAgICBmdW5jdGlvbiBFdmVudCgpIHtcbiAgICAgIHRoaXMuaWQgPSArK2V2ZW50SWRDb3VudGVyO1xuICAgIH1cblxuICAgIEV2ZW50LnByb3RvdHlwZS5pc0V2ZW50ID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdHJ1ZTtcbiAgICB9O1xuXG4gICAgRXZlbnQucHJvdG90eXBlLmlzRW5kID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gZmFsc2U7XG4gICAgfTtcblxuICAgIEV2ZW50LnByb3RvdHlwZS5pc0luaXRpYWwgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9O1xuXG4gICAgRXZlbnQucHJvdG90eXBlLmlzTmV4dCA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH07XG5cbiAgICBFdmVudC5wcm90b3R5cGUuaXNFcnJvciA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIGZhbHNlO1xuICAgIH07XG5cbiAgICBFdmVudC5wcm90b3R5cGUuaGFzVmFsdWUgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9O1xuXG4gICAgRXZlbnQucHJvdG90eXBlLmZpbHRlciA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRydWU7XG4gICAgfTtcblxuICAgIEV2ZW50LnByb3RvdHlwZS5pbnNwZWN0ID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy50b1N0cmluZygpO1xuICAgIH07XG5cbiAgICBFdmVudC5wcm90b3R5cGUubG9nID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy50b1N0cmluZygpO1xuICAgIH07XG5cbiAgICByZXR1cm4gRXZlbnQ7XG5cbiAgfSkoKTtcblxuICBOZXh0ID0gKGZ1bmN0aW9uKF9zdXBlcikge1xuICAgIF9fZXh0ZW5kcyhOZXh0LCBfc3VwZXIpO1xuXG4gICAgZnVuY3Rpb24gTmV4dCh2YWx1ZUYsIGVhZ2VyKSB7XG4gICAgICBOZXh0Ll9fc3VwZXJfXy5jb25zdHJ1Y3Rvci5jYWxsKHRoaXMpO1xuICAgICAgaWYgKCFlYWdlciAmJiBpc0Z1bmN0aW9uKHZhbHVlRikgfHwgdmFsdWVGIGluc3RhbmNlb2YgTmV4dCkge1xuICAgICAgICB0aGlzLnZhbHVlRiA9IHZhbHVlRjtcbiAgICAgICAgdGhpcy52YWx1ZUludGVybmFsID0gdm9pZCAwO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgdGhpcy52YWx1ZUYgPSB2b2lkIDA7XG4gICAgICAgIHRoaXMudmFsdWVJbnRlcm5hbCA9IHZhbHVlRjtcbiAgICAgIH1cbiAgICB9XG5cbiAgICBOZXh0LnByb3RvdHlwZS5pc05leHQgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH07XG5cbiAgICBOZXh0LnByb3RvdHlwZS5oYXNWYWx1ZSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRydWU7XG4gICAgfTtcblxuICAgIE5leHQucHJvdG90eXBlLnZhbHVlID0gZnVuY3Rpb24oKSB7XG4gICAgICBpZiAodGhpcy52YWx1ZUYgaW5zdGFuY2VvZiBOZXh0KSB7XG4gICAgICAgIHRoaXMudmFsdWVJbnRlcm5hbCA9IHRoaXMudmFsdWVGLnZhbHVlKCk7XG4gICAgICAgIHRoaXMudmFsdWVGID0gdm9pZCAwO1xuICAgICAgfSBlbHNlIGlmICh0aGlzLnZhbHVlRikge1xuICAgICAgICB0aGlzLnZhbHVlSW50ZXJuYWwgPSB0aGlzLnZhbHVlRigpO1xuICAgICAgICB0aGlzLnZhbHVlRiA9IHZvaWQgMDtcbiAgICAgIH1cbiAgICAgIHJldHVybiB0aGlzLnZhbHVlSW50ZXJuYWw7XG4gICAgfTtcblxuICAgIE5leHQucHJvdG90eXBlLmZtYXAgPSBmdW5jdGlvbihmKSB7XG4gICAgICB2YXIgZXZlbnQsIHZhbHVlO1xuICAgICAgaWYgKHRoaXMudmFsdWVJbnRlcm5hbCkge1xuICAgICAgICB2YWx1ZSA9IHRoaXMudmFsdWVJbnRlcm5hbDtcbiAgICAgICAgcmV0dXJuIHRoaXMuYXBwbHkoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuIGYodmFsdWUpO1xuICAgICAgICB9KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGV2ZW50ID0gdGhpcztcbiAgICAgICAgcmV0dXJuIHRoaXMuYXBwbHkoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuIGYoZXZlbnQudmFsdWUoKSk7XG4gICAgICAgIH0pO1xuICAgICAgfVxuICAgIH07XG5cbiAgICBOZXh0LnByb3RvdHlwZS5hcHBseSA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgICByZXR1cm4gbmV3IE5leHQodmFsdWUpO1xuICAgIH07XG5cbiAgICBOZXh0LnByb3RvdHlwZS5maWx0ZXIgPSBmdW5jdGlvbihmKSB7XG4gICAgICByZXR1cm4gZih0aGlzLnZhbHVlKCkpO1xuICAgIH07XG5cbiAgICBOZXh0LnByb3RvdHlwZS50b1N0cmluZyA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIF8udG9TdHJpbmcodGhpcy52YWx1ZSgpKTtcbiAgICB9O1xuXG4gICAgTmV4dC5wcm90b3R5cGUubG9nID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy52YWx1ZSgpO1xuICAgIH07XG5cbiAgICByZXR1cm4gTmV4dDtcblxuICB9KShFdmVudCk7XG5cbiAgSW5pdGlhbCA9IChmdW5jdGlvbihfc3VwZXIpIHtcbiAgICBfX2V4dGVuZHMoSW5pdGlhbCwgX3N1cGVyKTtcblxuICAgIGZ1bmN0aW9uIEluaXRpYWwoKSB7XG4gICAgICByZXR1cm4gSW5pdGlhbC5fX3N1cGVyX18uY29uc3RydWN0b3IuYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgICB9XG5cbiAgICBJbml0aWFsLnByb3RvdHlwZS5pc0luaXRpYWwgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH07XG5cbiAgICBJbml0aWFsLnByb3RvdHlwZS5pc05leHQgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBmYWxzZTtcbiAgICB9O1xuXG4gICAgSW5pdGlhbC5wcm90b3R5cGUuYXBwbHkgPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgcmV0dXJuIG5ldyBJbml0aWFsKHZhbHVlKTtcbiAgICB9O1xuXG4gICAgSW5pdGlhbC5wcm90b3R5cGUudG9OZXh0ID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gbmV3IE5leHQodGhpcyk7XG4gICAgfTtcblxuICAgIHJldHVybiBJbml0aWFsO1xuXG4gIH0pKE5leHQpO1xuXG4gIEVuZCA9IChmdW5jdGlvbihfc3VwZXIpIHtcbiAgICBfX2V4dGVuZHMoRW5kLCBfc3VwZXIpO1xuXG4gICAgZnVuY3Rpb24gRW5kKCkge1xuICAgICAgcmV0dXJuIEVuZC5fX3N1cGVyX18uY29uc3RydWN0b3IuYXBwbHkodGhpcywgYXJndW1lbnRzKTtcbiAgICB9XG5cbiAgICBFbmQucHJvdG90eXBlLmlzRW5kID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdHJ1ZTtcbiAgICB9O1xuXG4gICAgRW5kLnByb3RvdHlwZS5mbWFwID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcztcbiAgICB9O1xuXG4gICAgRW5kLnByb3RvdHlwZS5hcHBseSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIEVuZC5wcm90b3R5cGUudG9TdHJpbmcgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBcIjxlbmQ+XCI7XG4gICAgfTtcblxuICAgIHJldHVybiBFbmQ7XG5cbiAgfSkoRXZlbnQpO1xuXG4gIEVycm9yID0gKGZ1bmN0aW9uKF9zdXBlcikge1xuICAgIF9fZXh0ZW5kcyhFcnJvciwgX3N1cGVyKTtcblxuICAgIGZ1bmN0aW9uIEVycm9yKGVycm9yKSB7XG4gICAgICB0aGlzLmVycm9yID0gZXJyb3I7XG4gICAgfVxuXG4gICAgRXJyb3IucHJvdG90eXBlLmlzRXJyb3IgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH07XG5cbiAgICBFcnJvci5wcm90b3R5cGUuZm1hcCA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIEVycm9yLnByb3RvdHlwZS5hcHBseSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIEVycm9yLnByb3RvdHlwZS50b1N0cmluZyA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIFwiPGVycm9yPiBcIiArIF8udG9TdHJpbmcodGhpcy5lcnJvcik7XG4gICAgfTtcblxuICAgIHJldHVybiBFcnJvcjtcblxuICB9KShFdmVudCk7XG5cbiAgaWRDb3VudGVyID0gMDtcblxuICBPYnNlcnZhYmxlID0gKGZ1bmN0aW9uKCkge1xuICAgIGZ1bmN0aW9uIE9ic2VydmFibGUoZGVzYykge1xuICAgICAgdGhpcy5pZCA9ICsraWRDb3VudGVyO1xuICAgICAgd2l0aERlc2NyaXB0aW9uKGRlc2MsIHRoaXMpO1xuICAgICAgdGhpcy5pbml0aWFsRGVzYyA9IHRoaXMuZGVzYztcbiAgICB9XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5zdWJzY3JpYmUgPSBmdW5jdGlvbihzaW5rKSB7XG4gICAgICByZXR1cm4gVXBkYXRlQmFycmllci53cmFwcGVkU3Vic2NyaWJlKHRoaXMsIHNpbmspO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5zdWJzY3JpYmVJbnRlcm5hbCA9IGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgIHJldHVybiB0aGlzLmRpc3BhdGNoZXIuc3Vic2NyaWJlKHNpbmspO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5vblZhbHVlID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgZjtcbiAgICAgIGYgPSBtYWtlRnVuY3Rpb25BcmdzKGFyZ3VtZW50cyk7XG4gICAgICByZXR1cm4gdGhpcy5zdWJzY3JpYmUoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgaWYgKGV2ZW50Lmhhc1ZhbHVlKCkpIHtcbiAgICAgICAgICByZXR1cm4gZihldmVudC52YWx1ZSgpKTtcbiAgICAgICAgfVxuICAgICAgfSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLm9uVmFsdWVzID0gZnVuY3Rpb24oZikge1xuICAgICAgcmV0dXJuIHRoaXMub25WYWx1ZShmdW5jdGlvbihhcmdzKSB7XG4gICAgICAgIHJldHVybiBmLmFwcGx5KG51bGwsIGFyZ3MpO1xuICAgICAgfSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLm9uRXJyb3IgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBmO1xuICAgICAgZiA9IG1ha2VGdW5jdGlvbkFyZ3MoYXJndW1lbnRzKTtcbiAgICAgIHJldHVybiB0aGlzLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICBpZiAoZXZlbnQuaXNFcnJvcigpKSB7XG4gICAgICAgICAgcmV0dXJuIGYoZXZlbnQuZXJyb3IpO1xuICAgICAgICB9XG4gICAgICB9KTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUub25FbmQgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBmO1xuICAgICAgZiA9IG1ha2VGdW5jdGlvbkFyZ3MoYXJndW1lbnRzKTtcbiAgICAgIHJldHVybiB0aGlzLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICBpZiAoZXZlbnQuaXNFbmQoKSkge1xuICAgICAgICAgIHJldHVybiBmKCk7XG4gICAgICAgIH1cbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5lcnJvcnMgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJlcnJvcnNcIiwgdGhpcy5maWx0ZXIoZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuZmlsdGVyID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgYXJncywgZjtcbiAgICAgIGYgPSBhcmd1bWVudHNbMF0sIGFyZ3MgPSAyIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAxKSA6IFtdO1xuICAgICAgcmV0dXJuIGNvbnZlcnRBcmdzVG9GdW5jdGlvbih0aGlzLCBmLCBhcmdzLCBmdW5jdGlvbihmKSB7XG4gICAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJmaWx0ZXJcIiwgZiwgdGhpcy53aXRoSGFuZGxlcihmdW5jdGlvbihldmVudCkge1xuICAgICAgICAgIGlmIChldmVudC5maWx0ZXIoZikpIHtcbiAgICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4gQmFjb24ubW9yZTtcbiAgICAgICAgICB9XG4gICAgICAgIH0pKTtcbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS50YWtlV2hpbGUgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzLCBmO1xuICAgICAgZiA9IGFyZ3VtZW50c1swXSwgYXJncyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgICByZXR1cm4gY29udmVydEFyZ3NUb0Z1bmN0aW9uKHRoaXMsIGYsIGFyZ3MsIGZ1bmN0aW9uKGYpIHtcbiAgICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcInRha2VXaGlsZVwiLCBmLCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgaWYgKGV2ZW50LmZpbHRlcihmKSkge1xuICAgICAgICAgICAgcmV0dXJuIHRoaXMucHVzaChldmVudCk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHRoaXMucHVzaChlbmQoKSk7XG4gICAgICAgICAgICByZXR1cm4gQmFjb24ubm9Nb3JlO1xuICAgICAgICAgIH1cbiAgICAgICAgfSkpO1xuICAgICAgfSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLmVuZE9uRXJyb3IgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzLCBmO1xuICAgICAgZiA9IGFyZ3VtZW50c1swXSwgYXJncyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgICBpZiAoZiA9PSBudWxsKSB7XG4gICAgICAgIGYgPSB0cnVlO1xuICAgICAgfVxuICAgICAgcmV0dXJuIGNvbnZlcnRBcmdzVG9GdW5jdGlvbih0aGlzLCBmLCBhcmdzLCBmdW5jdGlvbihmKSB7XG4gICAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJlbmRPbkVycm9yXCIsIHRoaXMud2l0aEhhbmRsZXIoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgICBpZiAoZXZlbnQuaXNFcnJvcigpICYmIGYoZXZlbnQuZXJyb3IpKSB7XG4gICAgICAgICAgICB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICAgICAgcmV0dXJuIHRoaXMucHVzaChlbmQoKSk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSkpO1xuICAgICAgfSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLnRha2UgPSBmdW5jdGlvbihjb3VudCkge1xuICAgICAgaWYgKGNvdW50IDw9IDApIHtcbiAgICAgICAgcmV0dXJuIEJhY29uLm5ldmVyKCk7XG4gICAgICB9XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwidGFrZVwiLCBjb3VudCwgdGhpcy53aXRoSGFuZGxlcihmdW5jdGlvbihldmVudCkge1xuICAgICAgICBpZiAoIWV2ZW50Lmhhc1ZhbHVlKCkpIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKGV2ZW50KTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBjb3VudC0tO1xuICAgICAgICAgIGlmIChjb3VudCA+IDApIHtcbiAgICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBpZiAoY291bnQgPT09IDApIHtcbiAgICAgICAgICAgICAgdGhpcy5wdXNoKGV2ZW50KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICAgIHRoaXMucHVzaChlbmQoKSk7XG4gICAgICAgICAgICByZXR1cm4gQmFjb24ubm9Nb3JlO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5tYXAgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzLCBwO1xuICAgICAgcCA9IGFyZ3VtZW50c1swXSwgYXJncyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgICBpZiAocCBpbnN0YW5jZW9mIFByb3BlcnR5KSB7XG4gICAgICAgIHJldHVybiBwLnNhbXBsZWRCeSh0aGlzLCBmb3JtZXIpO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmV0dXJuIGNvbnZlcnRBcmdzVG9GdW5jdGlvbih0aGlzLCBwLCBhcmdzLCBmdW5jdGlvbihmKSB7XG4gICAgICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcIm1hcFwiLCBmLCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKGV2ZW50LmZtYXAoZikpO1xuICAgICAgICAgIH0pKTtcbiAgICAgICAgfSk7XG4gICAgICB9XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLm1hcEVycm9yID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgZjtcbiAgICAgIGYgPSBtYWtlRnVuY3Rpb25BcmdzKGFyZ3VtZW50cyk7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwibWFwRXJyb3JcIiwgZiwgdGhpcy53aXRoSGFuZGxlcihmdW5jdGlvbihldmVudCkge1xuICAgICAgICBpZiAoZXZlbnQuaXNFcnJvcigpKSB7XG4gICAgICAgICAgcmV0dXJuIHRoaXMucHVzaChuZXh0KGYoZXZlbnQuZXJyb3IpKSk7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmV0dXJuIHRoaXMucHVzaChldmVudCk7XG4gICAgICAgIH1cbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUubWFwRW5kID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgZjtcbiAgICAgIGYgPSBtYWtlRnVuY3Rpb25BcmdzKGFyZ3VtZW50cyk7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwibWFwRW5kXCIsIGYsIHRoaXMud2l0aEhhbmRsZXIoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgaWYgKGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICB0aGlzLnB1c2gobmV4dChmKGV2ZW50KSkpO1xuICAgICAgICAgIHRoaXMucHVzaChlbmQoKSk7XG4gICAgICAgICAgcmV0dXJuIEJhY29uLm5vTW9yZTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKGV2ZW50KTtcbiAgICAgICAgfVxuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5kb0FjdGlvbiA9IGZ1bmN0aW9uKCkge1xuICAgICAgdmFyIGY7XG4gICAgICBmID0gbWFrZUZ1bmN0aW9uQXJncyhhcmd1bWVudHMpO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImRvQWN0aW9uXCIsIGYsIHRoaXMud2l0aEhhbmRsZXIoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgaWYgKGV2ZW50Lmhhc1ZhbHVlKCkpIHtcbiAgICAgICAgICBmKGV2ZW50LnZhbHVlKCkpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5za2lwID0gZnVuY3Rpb24oY291bnQpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJza2lwXCIsIGNvdW50LCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgIGlmICghZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICB9IGVsc2UgaWYgKGNvdW50ID4gMCkge1xuICAgICAgICAgIGNvdW50LS07XG4gICAgICAgICAgcmV0dXJuIEJhY29uLm1vcmU7XG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgcmV0dXJuIHRoaXMucHVzaChldmVudCk7XG4gICAgICAgIH1cbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuc2tpcER1cGxpY2F0ZXMgPSBmdW5jdGlvbihpc0VxdWFsKSB7XG4gICAgICBpZiAoaXNFcXVhbCA9PSBudWxsKSB7XG4gICAgICAgIGlzRXF1YWwgPSBmdW5jdGlvbihhLCBiKSB7XG4gICAgICAgICAgcmV0dXJuIGEgPT09IGI7XG4gICAgICAgIH07XG4gICAgICB9XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwic2tpcER1cGxpY2F0ZXNcIiwgdGhpcy53aXRoU3RhdGVNYWNoaW5lKE5vbmUsIGZ1bmN0aW9uKHByZXYsIGV2ZW50KSB7XG4gICAgICAgIGlmICghZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgIHJldHVybiBbcHJldiwgW2V2ZW50XV07XG4gICAgICAgIH0gZWxzZSBpZiAoZXZlbnQuaXNJbml0aWFsKCkgfHwgcHJldiA9PT0gTm9uZSB8fCAhaXNFcXVhbChwcmV2LmdldCgpLCBldmVudC52YWx1ZSgpKSkge1xuICAgICAgICAgIHJldHVybiBbbmV3IFNvbWUoZXZlbnQudmFsdWUoKSksIFtldmVudF1dO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHJldHVybiBbcHJldiwgW11dO1xuICAgICAgICB9XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLnNraXBFcnJvcnMgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJza2lwRXJyb3JzXCIsIHRoaXMud2l0aEhhbmRsZXIoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgaWYgKGV2ZW50LmlzRXJyb3IoKSkge1xuICAgICAgICAgIHJldHVybiBCYWNvbi5tb3JlO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICB9XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLndpdGhTdGF0ZU1hY2hpbmUgPSBmdW5jdGlvbihpbml0U3RhdGUsIGYpIHtcbiAgICAgIHZhciBzdGF0ZTtcbiAgICAgIHN0YXRlID0gaW5pdFN0YXRlO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcIndpdGhTdGF0ZU1hY2hpbmVcIiwgaW5pdFN0YXRlLCBmLCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgIHZhciBmcm9tRiwgbmV3U3RhdGUsIG91dHB1dCwgb3V0cHV0cywgcmVwbHksIF9pLCBfbGVuO1xuICAgICAgICBmcm9tRiA9IGYoc3RhdGUsIGV2ZW50KTtcbiAgICAgICAgbmV3U3RhdGUgPSBmcm9tRlswXSwgb3V0cHV0cyA9IGZyb21GWzFdO1xuICAgICAgICBzdGF0ZSA9IG5ld1N0YXRlO1xuICAgICAgICByZXBseSA9IEJhY29uLm1vcmU7XG4gICAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gb3V0cHV0cy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICAgIG91dHB1dCA9IG91dHB1dHNbX2ldO1xuICAgICAgICAgIHJlcGx5ID0gdGhpcy5wdXNoKG91dHB1dCk7XG4gICAgICAgICAgaWYgKHJlcGx5ID09PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgICAgIHJldHVybiByZXBseTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIHJlcGx5O1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5zY2FuID0gZnVuY3Rpb24oc2VlZCwgZikge1xuICAgICAgdmFyIGFjYywgcmVzdWx0UHJvcGVydHksIHN1YnNjcmliZTtcbiAgICAgIGYgPSB0b0NvbWJpbmF0b3IoZik7XG4gICAgICBhY2MgPSB0b09wdGlvbihzZWVkKTtcbiAgICAgIHN1YnNjcmliZSA9IChmdW5jdGlvbihfdGhpcykge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24oc2luaykge1xuICAgICAgICAgIHZhciBpbml0U2VudCwgcmVwbHksIHNlbmRJbml0LCB1bnN1YjtcbiAgICAgICAgICBpbml0U2VudCA9IGZhbHNlO1xuICAgICAgICAgIHVuc3ViID0gbm9wO1xuICAgICAgICAgIHJlcGx5ID0gQmFjb24ubW9yZTtcbiAgICAgICAgICBzZW5kSW5pdCA9IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgaWYgKCFpbml0U2VudCkge1xuICAgICAgICAgICAgICByZXR1cm4gYWNjLmZvckVhY2goZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgICAgICAgICAgICBpbml0U2VudCA9IHRydWU7XG4gICAgICAgICAgICAgICAgcmVwbHkgPSBzaW5rKG5ldyBJbml0aWFsKGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgICAgICAgcmV0dXJuIHZhbHVlO1xuICAgICAgICAgICAgICAgIH0pKTtcbiAgICAgICAgICAgICAgICBpZiAocmVwbHkgPT09IEJhY29uLm5vTW9yZSkge1xuICAgICAgICAgICAgICAgICAgdW5zdWIoKTtcbiAgICAgICAgICAgICAgICAgIHJldHVybiB1bnN1YiA9IG5vcDtcbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgIH0pO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH07XG4gICAgICAgICAgdW5zdWIgPSBfdGhpcy5kaXNwYXRjaGVyLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICAgICAgdmFyIG5leHQsIHByZXY7XG4gICAgICAgICAgICBpZiAoZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgICAgICBpZiAoaW5pdFNlbnQgJiYgZXZlbnQuaXNJbml0aWFsKCkpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gQmFjb24ubW9yZTtcbiAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICBpZiAoIWV2ZW50LmlzSW5pdGlhbCgpKSB7XG4gICAgICAgICAgICAgICAgICBzZW5kSW5pdCgpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICBpbml0U2VudCA9IHRydWU7XG4gICAgICAgICAgICAgICAgcHJldiA9IGFjYy5nZXRPckVsc2Uodm9pZCAwKTtcbiAgICAgICAgICAgICAgICBuZXh0ID0gZihwcmV2LCBldmVudC52YWx1ZSgpKTtcbiAgICAgICAgICAgICAgICBhY2MgPSBuZXcgU29tZShuZXh0KTtcbiAgICAgICAgICAgICAgICByZXR1cm4gc2luayhldmVudC5hcHBseShmdW5jdGlvbigpIHtcbiAgICAgICAgICAgICAgICAgIHJldHVybiBuZXh0O1xuICAgICAgICAgICAgICAgIH0pKTtcbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgaWYgKGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgICAgICByZXBseSA9IHNlbmRJbml0KCk7XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgaWYgKHJlcGx5ICE9PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gc2luayhldmVudCk7XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9KTtcbiAgICAgICAgICBVcGRhdGVCYXJyaWVyLndoZW5Eb25lV2l0aChyZXN1bHRQcm9wZXJ0eSwgc2VuZEluaXQpO1xuICAgICAgICAgIHJldHVybiB1bnN1YjtcbiAgICAgICAgfTtcbiAgICAgIH0pKHRoaXMpO1xuICAgICAgcmV0dXJuIHJlc3VsdFByb3BlcnR5ID0gbmV3IFByb3BlcnR5KGRlc2NyaWJlKHRoaXMsIFwic2NhblwiLCBzZWVkLCBmKSwgc3Vic2NyaWJlKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuZm9sZCA9IGZ1bmN0aW9uKHNlZWQsIGYpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJmb2xkXCIsIHNlZWQsIGYsIHRoaXMuc2NhbihzZWVkLCBmKS5zYW1wbGVkQnkodGhpcy5maWx0ZXIoZmFsc2UpLm1hcEVuZCgpLnRvUHJvcGVydHkoKSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS56aXAgPSBmdW5jdGlvbihvdGhlciwgZikge1xuICAgICAgaWYgKGYgPT0gbnVsbCkge1xuICAgICAgICBmID0gQXJyYXk7XG4gICAgICB9XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwiemlwXCIsIG90aGVyLCBCYWNvbi56aXBXaXRoKFt0aGlzLCBvdGhlcl0sIGYpKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuZGlmZiA9IGZ1bmN0aW9uKHN0YXJ0LCBmKSB7XG4gICAgICBmID0gdG9Db21iaW5hdG9yKGYpO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImRpZmZcIiwgc3RhcnQsIGYsIHRoaXMuc2Nhbihbc3RhcnRdLCBmdW5jdGlvbihwcmV2VHVwbGUsIG5leHQpIHtcbiAgICAgICAgcmV0dXJuIFtuZXh0LCBmKHByZXZUdXBsZVswXSwgbmV4dCldO1xuICAgICAgfSkuZmlsdGVyKGZ1bmN0aW9uKHR1cGxlKSB7XG4gICAgICAgIHJldHVybiB0dXBsZS5sZW5ndGggPT09IDI7XG4gICAgICB9KS5tYXAoZnVuY3Rpb24odHVwbGUpIHtcbiAgICAgICAgcmV0dXJuIHR1cGxlWzFdO1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5mbGF0TWFwID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gZmxhdE1hcF8odGhpcywgbWFrZVNwYXduZXIoYXJndW1lbnRzKSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLmZsYXRNYXBGaXJzdCA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIGZsYXRNYXBfKHRoaXMsIG1ha2VTcGF3bmVyKGFyZ3VtZW50cyksIHRydWUpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5mbGF0TWFwV2l0aENvbmN1cnJlbmN5TGltaXQgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzLCBsaW1pdDtcbiAgICAgIGxpbWl0ID0gYXJndW1lbnRzWzBdLCBhcmdzID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMSkgOiBbXTtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24uYXBwbHkobnVsbCwgW3RoaXMsIFwiZmxhdE1hcFdpdGhDb25jdXJyZW5jeUxpbWl0XCIsIGxpbWl0XS5jb25jYXQoX19zbGljZS5jYWxsKGFyZ3MpLCBbZmxhdE1hcF8odGhpcywgbWFrZVNwYXduZXIoYXJncyksIGZhbHNlLCBsaW1pdCldKSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLmZsYXRNYXBMYXRlc3QgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBmLCBzdHJlYW07XG4gICAgICBmID0gbWFrZVNwYXduZXIoYXJndW1lbnRzKTtcbiAgICAgIHN0cmVhbSA9IHRoaXMudG9FdmVudFN0cmVhbSgpO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImZsYXRNYXBMYXRlc3RcIiwgZiwgc3RyZWFtLmZsYXRNYXAoZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgICAgcmV0dXJuIG1ha2VPYnNlcnZhYmxlKGYodmFsdWUpKS50YWtlVW50aWwoc3RyZWFtKTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuZmxhdE1hcEVycm9yID0gZnVuY3Rpb24oZm4pIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJmbGF0TWFwRXJyb3JcIiwgZm4sIHRoaXMubWFwRXJyb3IoZnVuY3Rpb24oZXJyKSB7XG4gICAgICAgIHJldHVybiBuZXcgRXJyb3IoZXJyKTtcbiAgICAgIH0pLmZsYXRNYXAoZnVuY3Rpb24oeCkge1xuICAgICAgICBpZiAoeCBpbnN0YW5jZW9mIEVycm9yKSB7XG4gICAgICAgICAgcmV0dXJuIGZuKHguZXJyb3IpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHJldHVybiBCYWNvbi5vbmNlKHgpO1xuICAgICAgICB9XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLmZsYXRNYXBDb25jYXQgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24uYXBwbHkobnVsbCwgW3RoaXMsIFwiZmxhdE1hcENvbmNhdFwiXS5jb25jYXQoX19zbGljZS5jYWxsKGFyZ3VtZW50cyksIFt0aGlzLmZsYXRNYXBXaXRoQ29uY3VycmVuY3lMaW1pdC5hcHBseSh0aGlzLCBbMV0uY29uY2F0KF9fc2xpY2UuY2FsbChhcmd1bWVudHMpKSldKSk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLmJ1ZmZlcmluZ1Rocm90dGxlID0gZnVuY3Rpb24obWluaW11bUludGVydmFsKSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwiYnVmZmVyaW5nVGhyb3R0bGVcIiwgbWluaW11bUludGVydmFsLCB0aGlzLmZsYXRNYXBDb25jYXQoZnVuY3Rpb24oeCkge1xuICAgICAgICByZXR1cm4gQmFjb24ub25jZSh4KS5jb25jYXQoQmFjb24ubGF0ZXIobWluaW11bUludGVydmFsKS5maWx0ZXIoZmFsc2UpKTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUubm90ID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwibm90XCIsIHRoaXMubWFwKGZ1bmN0aW9uKHgpIHtcbiAgICAgICAgcmV0dXJuICF4O1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5sb2cgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBhcmdzO1xuICAgICAgYXJncyA9IDEgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDApIDogW107XG4gICAgICB0aGlzLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICByZXR1cm4gdHlwZW9mIGNvbnNvbGUgIT09IFwidW5kZWZpbmVkXCIgJiYgY29uc29sZSAhPT0gbnVsbCA/IHR5cGVvZiBjb25zb2xlLmxvZyA9PT0gXCJmdW5jdGlvblwiID8gY29uc29sZS5sb2cuYXBwbHkoY29uc29sZSwgX19zbGljZS5jYWxsKGFyZ3MpLmNvbmNhdChbZXZlbnQubG9nKCldKSkgOiB2b2lkIDAgOiB2b2lkIDA7XG4gICAgICB9KTtcbiAgICAgIHJldHVybiB0aGlzO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5zbGlkaW5nV2luZG93ID0gZnVuY3Rpb24obiwgbWluVmFsdWVzKSB7XG4gICAgICBpZiAobWluVmFsdWVzID09IG51bGwpIHtcbiAgICAgICAgbWluVmFsdWVzID0gMDtcbiAgICAgIH1cbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJzbGlkaW5nV2luZG93XCIsIG4sIG1pblZhbHVlcywgdGhpcy5zY2FuKFtdLCAoZnVuY3Rpb24od2luZG93LCB2YWx1ZSkge1xuICAgICAgICByZXR1cm4gd2luZG93LmNvbmNhdChbdmFsdWVdKS5zbGljZSgtbik7XG4gICAgICB9KSkuZmlsdGVyKChmdW5jdGlvbih2YWx1ZXMpIHtcbiAgICAgICAgcmV0dXJuIHZhbHVlcy5sZW5ndGggPj0gbWluVmFsdWVzO1xuICAgICAgfSkpKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuY29tYmluZSA9IGZ1bmN0aW9uKG90aGVyLCBmKSB7XG4gICAgICB2YXIgY29tYmluYXRvcjtcbiAgICAgIGNvbWJpbmF0b3IgPSB0b0NvbWJpbmF0b3IoZik7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwiY29tYmluZVwiLCBvdGhlciwgZiwgQmFjb24uY29tYmluZUFzQXJyYXkodGhpcywgb3RoZXIpLm1hcChmdW5jdGlvbih2YWx1ZXMpIHtcbiAgICAgICAgcmV0dXJuIGNvbWJpbmF0b3IodmFsdWVzWzBdLCB2YWx1ZXNbMV0pO1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBPYnNlcnZhYmxlLnByb3RvdHlwZS5kZWNvZGUgPSBmdW5jdGlvbihjYXNlcykge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImRlY29kZVwiLCBjYXNlcywgdGhpcy5jb21iaW5lKEJhY29uLmNvbWJpbmVUZW1wbGF0ZShjYXNlcyksIGZ1bmN0aW9uKGtleSwgdmFsdWVzKSB7XG4gICAgICAgIHJldHVybiB2YWx1ZXNba2V5XTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuYXdhaXRpbmcgPSBmdW5jdGlvbihvdGhlcikge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImF3YWl0aW5nXCIsIG90aGVyLCBCYWNvbi5ncm91cFNpbXVsdGFuZW91cyh0aGlzLCBvdGhlcikubWFwKGZ1bmN0aW9uKF9hcmcpIHtcbiAgICAgICAgdmFyIG15VmFsdWVzLCBvdGhlclZhbHVlcztcbiAgICAgICAgbXlWYWx1ZXMgPSBfYXJnWzBdLCBvdGhlclZhbHVlcyA9IF9hcmdbMV07XG4gICAgICAgIHJldHVybiBvdGhlclZhbHVlcy5sZW5ndGggPT09IDA7XG4gICAgICB9KS50b1Byb3BlcnR5KGZhbHNlKS5za2lwRHVwbGljYXRlcygpKTtcbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUubmFtZSA9IGZ1bmN0aW9uKG5hbWUpIHtcbiAgICAgIHRoaXMuX25hbWUgPSBuYW1lO1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLndpdGhEZXNjcmlwdGlvbiA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIGRlc2NyaWJlLmFwcGx5KG51bGwsIGFyZ3VtZW50cykuYXBwbHkodGhpcyk7XG4gICAgfTtcblxuICAgIE9ic2VydmFibGUucHJvdG90eXBlLnRvU3RyaW5nID0gZnVuY3Rpb24oKSB7XG4gICAgICBpZiAodGhpcy5fbmFtZSkge1xuICAgICAgICByZXR1cm4gdGhpcy5fbmFtZTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiB0aGlzLmRlc2MudG9TdHJpbmcoKTtcbiAgICAgIH1cbiAgICB9O1xuXG4gICAgT2JzZXJ2YWJsZS5wcm90b3R5cGUuaW50ZXJuYWxEZXBzID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5pbml0aWFsRGVzYy5kZXBzKCk7XG4gICAgfTtcblxuICAgIHJldHVybiBPYnNlcnZhYmxlO1xuXG4gIH0pKCk7XG5cbiAgT2JzZXJ2YWJsZS5wcm90b3R5cGUucmVkdWNlID0gT2JzZXJ2YWJsZS5wcm90b3R5cGUuZm9sZDtcblxuICBPYnNlcnZhYmxlLnByb3RvdHlwZS5hc3NpZ24gPSBPYnNlcnZhYmxlLnByb3RvdHlwZS5vblZhbHVlO1xuXG4gIE9ic2VydmFibGUucHJvdG90eXBlLmluc3BlY3QgPSBPYnNlcnZhYmxlLnByb3RvdHlwZS50b1N0cmluZztcblxuICBmbGF0TWFwXyA9IGZ1bmN0aW9uKHJvb3QsIGYsIGZpcnN0T25seSwgbGltaXQpIHtcbiAgICB2YXIgY2hpbGREZXBzLCByZXN1bHQsIHJvb3REZXA7XG4gICAgcm9vdERlcCA9IFtyb290XTtcbiAgICBjaGlsZERlcHMgPSBbXTtcbiAgICByZXN1bHQgPSBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUocm9vdCwgXCJmbGF0TWFwXCIgKyAoZmlyc3RPbmx5ID8gXCJGaXJzdFwiIDogXCJcIiksIGYpLCBmdW5jdGlvbihzaW5rKSB7XG4gICAgICB2YXIgY2hlY2tFbmQsIGNoZWNrUXVldWUsIGNvbXBvc2l0ZSwgcXVldWUsIHNwYXduO1xuICAgICAgY29tcG9zaXRlID0gbmV3IENvbXBvc2l0ZVVuc3Vic2NyaWJlKCk7XG4gICAgICBxdWV1ZSA9IFtdO1xuICAgICAgc3Bhd24gPSBmdW5jdGlvbihldmVudCkge1xuICAgICAgICB2YXIgY2hpbGQ7XG4gICAgICAgIGNoaWxkID0gbWFrZU9ic2VydmFibGUoZihldmVudC52YWx1ZSgpKSk7XG4gICAgICAgIGNoaWxkRGVwcy5wdXNoKGNoaWxkKTtcbiAgICAgICAgcmV0dXJuIGNvbXBvc2l0ZS5hZGQoZnVuY3Rpb24odW5zdWJBbGwsIHVuc3ViTWUpIHtcbiAgICAgICAgICByZXR1cm4gY2hpbGQuZGlzcGF0Y2hlci5zdWJzY3JpYmUoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgICAgIHZhciByZXBseTtcbiAgICAgICAgICAgIGlmIChldmVudC5pc0VuZCgpKSB7XG4gICAgICAgICAgICAgIF8ucmVtb3ZlKGNoaWxkLCBjaGlsZERlcHMpO1xuICAgICAgICAgICAgICBjaGVja1F1ZXVlKCk7XG4gICAgICAgICAgICAgIGNoZWNrRW5kKHVuc3ViTWUpO1xuICAgICAgICAgICAgICByZXR1cm4gQmFjb24ubm9Nb3JlO1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgaWYgKGV2ZW50IGluc3RhbmNlb2YgSW5pdGlhbCkge1xuICAgICAgICAgICAgICAgIGV2ZW50ID0gZXZlbnQudG9OZXh0KCk7XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgcmVwbHkgPSBzaW5rKGV2ZW50KTtcbiAgICAgICAgICAgICAgaWYgKHJlcGx5ID09PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgICAgICAgICB1bnN1YkFsbCgpO1xuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgIHJldHVybiByZXBseTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9KTtcbiAgICAgICAgfSk7XG4gICAgICB9O1xuICAgICAgY2hlY2tRdWV1ZSA9IGZ1bmN0aW9uKCkge1xuICAgICAgICB2YXIgZXZlbnQ7XG4gICAgICAgIGV2ZW50ID0gcXVldWUuc2hpZnQoKTtcbiAgICAgICAgaWYgKGV2ZW50KSB7XG4gICAgICAgICAgcmV0dXJuIHNwYXduKGV2ZW50KTtcbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIGNoZWNrRW5kID0gZnVuY3Rpb24odW5zdWIpIHtcbiAgICAgICAgdW5zdWIoKTtcbiAgICAgICAgaWYgKGNvbXBvc2l0ZS5lbXB0eSgpKSB7XG4gICAgICAgICAgcmV0dXJuIHNpbmsoZW5kKCkpO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgICAgY29tcG9zaXRlLmFkZChmdW5jdGlvbihfXywgdW5zdWJSb290KSB7XG4gICAgICAgIHJldHVybiByb290LmRpc3BhdGNoZXIuc3Vic2NyaWJlKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgaWYgKGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgIHJldHVybiBjaGVja0VuZCh1bnN1YlJvb3QpO1xuICAgICAgICAgIH0gZWxzZSBpZiAoZXZlbnQuaXNFcnJvcigpKSB7XG4gICAgICAgICAgICByZXR1cm4gc2luayhldmVudCk7XG4gICAgICAgICAgfSBlbHNlIGlmIChmaXJzdE9ubHkgJiYgY29tcG9zaXRlLmNvdW50KCkgPiAxKSB7XG4gICAgICAgICAgICByZXR1cm4gQmFjb24ubW9yZTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgaWYgKGNvbXBvc2l0ZS51bnN1YnNjcmliZWQpIHtcbiAgICAgICAgICAgICAgcmV0dXJuIEJhY29uLm5vTW9yZTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICAgIGlmIChsaW1pdCAmJiBjb21wb3NpdGUuY291bnQoKSA+IGxpbWl0KSB7XG4gICAgICAgICAgICAgIHJldHVybiBxdWV1ZS5wdXNoKGV2ZW50KTtcbiAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgIHJldHVybiBzcGF3bihldmVudCk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgIH0pO1xuICAgICAgcmV0dXJuIGNvbXBvc2l0ZS51bnN1YnNjcmliZTtcbiAgICB9KTtcbiAgICByZXN1bHQuaW50ZXJuYWxEZXBzID0gZnVuY3Rpb24oKSB7XG4gICAgICBpZiAoY2hpbGREZXBzLmxlbmd0aCkge1xuICAgICAgICByZXR1cm4gcm9vdERlcC5jb25jYXQoY2hpbGREZXBzKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiByb290RGVwO1xuICAgICAgfVxuICAgIH07XG4gICAgcmV0dXJuIHJlc3VsdDtcbiAgfTtcblxuICBFdmVudFN0cmVhbSA9IChmdW5jdGlvbihfc3VwZXIpIHtcbiAgICBfX2V4dGVuZHMoRXZlbnRTdHJlYW0sIF9zdXBlcik7XG5cbiAgICBmdW5jdGlvbiBFdmVudFN0cmVhbShkZXNjLCBzdWJzY3JpYmUsIGhhbmRsZXIpIHtcbiAgICAgIGlmIChpc0Z1bmN0aW9uKGRlc2MpKSB7XG4gICAgICAgIGhhbmRsZXIgPSBzdWJzY3JpYmU7XG4gICAgICAgIHN1YnNjcmliZSA9IGRlc2M7XG4gICAgICAgIGRlc2MgPSBbXTtcbiAgICAgIH1cbiAgICAgIEV2ZW50U3RyZWFtLl9fc3VwZXJfXy5jb25zdHJ1Y3Rvci5jYWxsKHRoaXMsIGRlc2MpO1xuICAgICAgYXNzZXJ0RnVuY3Rpb24oc3Vic2NyaWJlKTtcbiAgICAgIHRoaXMuZGlzcGF0Y2hlciA9IG5ldyBEaXNwYXRjaGVyKHN1YnNjcmliZSwgaGFuZGxlcik7XG4gICAgICByZWdpc3Rlck9icyh0aGlzKTtcbiAgICB9XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUuZGVsYXkgPSBmdW5jdGlvbihkZWxheSkge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImRlbGF5XCIsIGRlbGF5LCB0aGlzLmZsYXRNYXAoZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgICAgcmV0dXJuIEJhY29uLmxhdGVyKGRlbGF5LCB2YWx1ZSk7XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS5kZWJvdW5jZSA9IGZ1bmN0aW9uKGRlbGF5KSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwiZGVib3VuY2VcIiwgZGVsYXksIHRoaXMuZmxhdE1hcExhdGVzdChmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgICByZXR1cm4gQmFjb24ubGF0ZXIoZGVsYXksIHZhbHVlKTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgRXZlbnRTdHJlYW0ucHJvdG90eXBlLmRlYm91bmNlSW1tZWRpYXRlID0gZnVuY3Rpb24oZGVsYXkpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJkZWJvdW5jZUltbWVkaWF0ZVwiLCBkZWxheSwgdGhpcy5mbGF0TWFwRmlyc3QoZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgICAgcmV0dXJuIEJhY29uLm9uY2UodmFsdWUpLmNvbmNhdChCYWNvbi5sYXRlcihkZWxheSkuZmlsdGVyKGZhbHNlKSk7XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS50aHJvdHRsZSA9IGZ1bmN0aW9uKGRlbGF5KSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwidGhyb3R0bGVcIiwgZGVsYXksIHRoaXMuYnVmZmVyV2l0aFRpbWUoZGVsYXkpLm1hcChmdW5jdGlvbih2YWx1ZXMpIHtcbiAgICAgICAgcmV0dXJuIHZhbHVlc1t2YWx1ZXMubGVuZ3RoIC0gMV07XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS5idWZmZXJXaXRoVGltZSA9IGZ1bmN0aW9uKGRlbGF5KSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwiYnVmZmVyV2l0aFRpbWVcIiwgZGVsYXksIHRoaXMuYnVmZmVyV2l0aFRpbWVPckNvdW50KGRlbGF5LCBOdW1iZXIuTUFYX1ZBTFVFKSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS5idWZmZXJXaXRoQ291bnQgPSBmdW5jdGlvbihjb3VudCkge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImJ1ZmZlcldpdGhDb3VudFwiLCBjb3VudCwgdGhpcy5idWZmZXJXaXRoVGltZU9yQ291bnQodm9pZCAwLCBjb3VudCkpO1xuICAgIH07XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUuYnVmZmVyV2l0aFRpbWVPckNvdW50ID0gZnVuY3Rpb24oZGVsYXksIGNvdW50KSB7XG4gICAgICB2YXIgZmx1c2hPclNjaGVkdWxlO1xuICAgICAgZmx1c2hPclNjaGVkdWxlID0gZnVuY3Rpb24oYnVmZmVyKSB7XG4gICAgICAgIGlmIChidWZmZXIudmFsdWVzLmxlbmd0aCA9PT0gY291bnQpIHtcbiAgICAgICAgICByZXR1cm4gYnVmZmVyLmZsdXNoKCk7XG4gICAgICAgIH0gZWxzZSBpZiAoZGVsYXkgIT09IHZvaWQgMCkge1xuICAgICAgICAgIHJldHVybiBidWZmZXIuc2NoZWR1bGUoKTtcbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJidWZmZXJXaXRoVGltZU9yQ291bnRcIiwgZGVsYXksIGNvdW50LCB0aGlzLmJ1ZmZlcihkZWxheSwgZmx1c2hPclNjaGVkdWxlLCBmbHVzaE9yU2NoZWR1bGUpKTtcbiAgICB9O1xuXG4gICAgRXZlbnRTdHJlYW0ucHJvdG90eXBlLmJ1ZmZlciA9IGZ1bmN0aW9uKGRlbGF5LCBvbklucHV0LCBvbkZsdXNoKSB7XG4gICAgICB2YXIgYnVmZmVyLCBkZWxheU1zLCByZXBseTtcbiAgICAgIGlmIChvbklucHV0ID09IG51bGwpIHtcbiAgICAgICAgb25JbnB1dCA9IG5vcDtcbiAgICAgIH1cbiAgICAgIGlmIChvbkZsdXNoID09IG51bGwpIHtcbiAgICAgICAgb25GbHVzaCA9IG5vcDtcbiAgICAgIH1cbiAgICAgIGJ1ZmZlciA9IHtcbiAgICAgICAgc2NoZWR1bGVkOiBmYWxzZSxcbiAgICAgICAgZW5kOiB2b2lkIDAsXG4gICAgICAgIHZhbHVlczogW10sXG4gICAgICAgIGZsdXNoOiBmdW5jdGlvbigpIHtcbiAgICAgICAgICB2YXIgcmVwbHk7XG4gICAgICAgICAgdGhpcy5zY2hlZHVsZWQgPSBmYWxzZTtcbiAgICAgICAgICBpZiAodGhpcy52YWx1ZXMubGVuZ3RoID4gMCkge1xuICAgICAgICAgICAgcmVwbHkgPSB0aGlzLnB1c2gobmV4dCh0aGlzLnZhbHVlcykpO1xuICAgICAgICAgICAgdGhpcy52YWx1ZXMgPSBbXTtcbiAgICAgICAgICAgIGlmICh0aGlzLmVuZCAhPSBudWxsKSB7XG4gICAgICAgICAgICAgIHJldHVybiB0aGlzLnB1c2godGhpcy5lbmQpO1xuICAgICAgICAgICAgfSBlbHNlIGlmIChyZXBseSAhPT0gQmFjb24ubm9Nb3JlKSB7XG4gICAgICAgICAgICAgIHJldHVybiBvbkZsdXNoKHRoaXMpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICBpZiAodGhpcy5lbmQgIT0gbnVsbCkge1xuICAgICAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKHRoaXMuZW5kKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9XG4gICAgICAgIH0sXG4gICAgICAgIHNjaGVkdWxlOiBmdW5jdGlvbigpIHtcbiAgICAgICAgICBpZiAoIXRoaXMuc2NoZWR1bGVkKSB7XG4gICAgICAgICAgICB0aGlzLnNjaGVkdWxlZCA9IHRydWU7XG4gICAgICAgICAgICByZXR1cm4gZGVsYXkoKGZ1bmN0aW9uKF90aGlzKSB7XG4gICAgICAgICAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4gX3RoaXMuZmx1c2goKTtcbiAgICAgICAgICAgICAgfTtcbiAgICAgICAgICAgIH0pKHRoaXMpKTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgIH07XG4gICAgICByZXBseSA9IEJhY29uLm1vcmU7XG4gICAgICBpZiAoIWlzRnVuY3Rpb24oZGVsYXkpKSB7XG4gICAgICAgIGRlbGF5TXMgPSBkZWxheTtcbiAgICAgICAgZGVsYXkgPSBmdW5jdGlvbihmKSB7XG4gICAgICAgICAgcmV0dXJuIEJhY29uLnNjaGVkdWxlci5zZXRUaW1lb3V0KGYsIGRlbGF5TXMpO1xuICAgICAgICB9O1xuICAgICAgfVxuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImJ1ZmZlclwiLCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgIGJ1ZmZlci5wdXNoID0gKGZ1bmN0aW9uKF90aGlzKSB7XG4gICAgICAgICAgcmV0dXJuIGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXMucHVzaChldmVudCk7XG4gICAgICAgICAgfTtcbiAgICAgICAgfSkodGhpcyk7XG4gICAgICAgIGlmIChldmVudC5pc0Vycm9yKCkpIHtcbiAgICAgICAgICByZXBseSA9IHRoaXMucHVzaChldmVudCk7XG4gICAgICAgIH0gZWxzZSBpZiAoZXZlbnQuaXNFbmQoKSkge1xuICAgICAgICAgIGJ1ZmZlci5lbmQgPSBldmVudDtcbiAgICAgICAgICBpZiAoIWJ1ZmZlci5zY2hlZHVsZWQpIHtcbiAgICAgICAgICAgIGJ1ZmZlci5mbHVzaCgpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBidWZmZXIudmFsdWVzLnB1c2goZXZlbnQudmFsdWUoKSk7XG4gICAgICAgICAgb25JbnB1dChidWZmZXIpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiByZXBseTtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgRXZlbnRTdHJlYW0ucHJvdG90eXBlLm1lcmdlID0gZnVuY3Rpb24ocmlnaHQpIHtcbiAgICAgIHZhciBsZWZ0O1xuICAgICAgYXNzZXJ0RXZlbnRTdHJlYW0ocmlnaHQpO1xuICAgICAgbGVmdCA9IHRoaXM7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKGxlZnQsIFwibWVyZ2VcIiwgcmlnaHQsIEJhY29uLm1lcmdlQWxsKHRoaXMsIHJpZ2h0KSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS50b1Byb3BlcnR5ID0gZnVuY3Rpb24oaW5pdFZhbHVlXykge1xuICAgICAgdmFyIGRpc3AsIGluaXRWYWx1ZTtcbiAgICAgIGluaXRWYWx1ZSA9IGFyZ3VtZW50cy5sZW5ndGggPT09IDAgPyBOb25lIDogdG9PcHRpb24oZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiBpbml0VmFsdWVfO1xuICAgICAgfSk7XG4gICAgICBkaXNwID0gdGhpcy5kaXNwYXRjaGVyO1xuICAgICAgcmV0dXJuIG5ldyBQcm9wZXJ0eShkZXNjcmliZSh0aGlzLCBcInRvUHJvcGVydHlcIiwgaW5pdFZhbHVlXyksIGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgICAgdmFyIGluaXRTZW50LCByZXBseSwgc2VuZEluaXQsIHVuc3ViO1xuICAgICAgICBpbml0U2VudCA9IGZhbHNlO1xuICAgICAgICB1bnN1YiA9IG5vcDtcbiAgICAgICAgcmVwbHkgPSBCYWNvbi5tb3JlO1xuICAgICAgICBzZW5kSW5pdCA9IGZ1bmN0aW9uKCkge1xuICAgICAgICAgIGlmICghaW5pdFNlbnQpIHtcbiAgICAgICAgICAgIHJldHVybiBpbml0VmFsdWUuZm9yRWFjaChmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgICAgICAgICBpbml0U2VudCA9IHRydWU7XG4gICAgICAgICAgICAgIHJlcGx5ID0gc2luayhuZXcgSW5pdGlhbCh2YWx1ZSkpO1xuICAgICAgICAgICAgICBpZiAocmVwbHkgPT09IEJhY29uLm5vTW9yZSkge1xuICAgICAgICAgICAgICAgIHVuc3ViKCk7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHVuc3ViID0gbm9wO1xuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9KTtcbiAgICAgICAgICB9XG4gICAgICAgIH07XG4gICAgICAgIHVuc3ViID0gZGlzcC5zdWJzY3JpYmUoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgICBpZiAoZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgICAgaWYgKGluaXRTZW50ICYmIGV2ZW50LmlzSW5pdGlhbCgpKSB7XG4gICAgICAgICAgICAgIHJldHVybiBCYWNvbi5tb3JlO1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgaWYgKCFldmVudC5pc0luaXRpYWwoKSkge1xuICAgICAgICAgICAgICAgIHNlbmRJbml0KCk7XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgaW5pdFNlbnQgPSB0cnVlO1xuICAgICAgICAgICAgICBpbml0VmFsdWUgPSBuZXcgU29tZShldmVudCk7XG4gICAgICAgICAgICAgIHJldHVybiBzaW5rKGV2ZW50KTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgaWYgKGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgICAgcmVwbHkgPSBzZW5kSW5pdCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgICAgaWYgKHJlcGx5ICE9PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgICAgICAgcmV0dXJuIHNpbmsoZXZlbnQpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICAgIHNlbmRJbml0KCk7XG4gICAgICAgIHJldHVybiB1bnN1YjtcbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUudG9FdmVudFN0cmVhbSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXM7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS5zYW1wbGVkQnkgPSBmdW5jdGlvbihzYW1wbGVyLCBjb21iaW5hdG9yKSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwic2FtcGxlZEJ5XCIsIHNhbXBsZXIsIGNvbWJpbmF0b3IsIHRoaXMudG9Qcm9wZXJ0eSgpLnNhbXBsZWRCeShzYW1wbGVyLCBjb21iaW5hdG9yKSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS5jb25jYXQgPSBmdW5jdGlvbihyaWdodCkge1xuICAgICAgdmFyIGxlZnQ7XG4gICAgICBsZWZ0ID0gdGhpcztcbiAgICAgIHJldHVybiBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUobGVmdCwgXCJjb25jYXRcIiwgcmlnaHQpLCBmdW5jdGlvbihzaW5rKSB7XG4gICAgICAgIHZhciB1bnN1YkxlZnQsIHVuc3ViUmlnaHQ7XG4gICAgICAgIHVuc3ViUmlnaHQgPSBub3A7XG4gICAgICAgIHVuc3ViTGVmdCA9IGxlZnQuZGlzcGF0Y2hlci5zdWJzY3JpYmUoZnVuY3Rpb24oZSkge1xuICAgICAgICAgIGlmIChlLmlzRW5kKCkpIHtcbiAgICAgICAgICAgIHJldHVybiB1bnN1YlJpZ2h0ID0gcmlnaHQuZGlzcGF0Y2hlci5zdWJzY3JpYmUoc2luayk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHJldHVybiBzaW5rKGUpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgICB1bnN1YkxlZnQoKTtcbiAgICAgICAgICByZXR1cm4gdW5zdWJSaWdodCgpO1xuICAgICAgICB9O1xuICAgICAgfSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS50YWtlVW50aWwgPSBmdW5jdGlvbihzdG9wcGVyKSB7XG4gICAgICB2YXIgZW5kTWFya2VyO1xuICAgICAgZW5kTWFya2VyID0ge307XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwidGFrZVVudGlsXCIsIHN0b3BwZXIsIEJhY29uLmdyb3VwU2ltdWx0YW5lb3VzKHRoaXMubWFwRW5kKGVuZE1hcmtlciksIHN0b3BwZXIuc2tpcEVycm9ycygpKS53aXRoSGFuZGxlcihmdW5jdGlvbihldmVudCkge1xuICAgICAgICB2YXIgZGF0YSwgcmVwbHksIHZhbHVlLCBfaSwgX2xlbiwgX3JlZjE7XG4gICAgICAgIGlmICghZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIF9yZWYxID0gZXZlbnQudmFsdWUoKSwgZGF0YSA9IF9yZWYxWzBdLCBzdG9wcGVyID0gX3JlZjFbMV07XG4gICAgICAgICAgaWYgKHN0b3BwZXIubGVuZ3RoKSB7XG4gICAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKGVuZCgpKTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgcmVwbHkgPSBCYWNvbi5tb3JlO1xuICAgICAgICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSBkYXRhLmxlbmd0aDsgX2kgPCBfbGVuOyBfaSsrKSB7XG4gICAgICAgICAgICAgIHZhbHVlID0gZGF0YVtfaV07XG4gICAgICAgICAgICAgIGlmICh2YWx1ZSA9PT0gZW5kTWFya2VyKSB7XG4gICAgICAgICAgICAgICAgcmVwbHkgPSB0aGlzLnB1c2goZW5kKCkpO1xuICAgICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICAgIHJlcGx5ID0gdGhpcy5wdXNoKG5leHQodmFsdWUpKTtcbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICAgICAgcmV0dXJuIHJlcGx5O1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUuc2tpcFVudGlsID0gZnVuY3Rpb24oc3RhcnRlcikge1xuICAgICAgdmFyIHN0YXJ0ZWQ7XG4gICAgICBzdGFydGVkID0gc3RhcnRlci50YWtlKDEpLm1hcCh0cnVlKS50b1Byb3BlcnR5KGZhbHNlKTtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJza2lwVW50aWxcIiwgc3RhcnRlciwgdGhpcy5maWx0ZXIoc3RhcnRlZCkpO1xuICAgIH07XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUuc2tpcFdoaWxlID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgYXJncywgZiwgb2s7XG4gICAgICBmID0gYXJndW1lbnRzWzBdLCBhcmdzID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMSkgOiBbXTtcbiAgICAgIG9rID0gZmFsc2U7XG4gICAgICByZXR1cm4gY29udmVydEFyZ3NUb0Z1bmN0aW9uKHRoaXMsIGYsIGFyZ3MsIGZ1bmN0aW9uKGYpIHtcbiAgICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcInNraXBXaGlsZVwiLCBmLCB0aGlzLndpdGhIYW5kbGVyKGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgaWYgKG9rIHx8ICFldmVudC5oYXNWYWx1ZSgpIHx8ICFmKGV2ZW50LnZhbHVlKCkpKSB7XG4gICAgICAgICAgICBpZiAoZXZlbnQuaGFzVmFsdWUoKSkge1xuICAgICAgICAgICAgICBvayA9IHRydWU7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICByZXR1cm4gdGhpcy5wdXNoKGV2ZW50KTtcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgcmV0dXJuIEJhY29uLm1vcmU7XG4gICAgICAgICAgfVxuICAgICAgICB9KSk7XG4gICAgICB9KTtcbiAgICB9O1xuXG4gICAgRXZlbnRTdHJlYW0ucHJvdG90eXBlLmhvbGRXaGVuID0gZnVuY3Rpb24odmFsdmUpIHtcbiAgICAgIHZhciBwdXRUb0hvbGQsIHJlbGVhc2VIb2xkLCB2YWx2ZV87XG4gICAgICB2YWx2ZV8gPSB2YWx2ZS5zdGFydFdpdGgoZmFsc2UpO1xuICAgICAgcmVsZWFzZUhvbGQgPSB2YWx2ZV8uZmlsdGVyKGZ1bmN0aW9uKHgpIHtcbiAgICAgICAgcmV0dXJuICF4O1xuICAgICAgfSk7XG4gICAgICBwdXRUb0hvbGQgPSB2YWx2ZV8uZmlsdGVyKF8uaWQpO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImhvbGRXaGVuXCIsIHZhbHZlLCB0aGlzLmZpbHRlcihmYWxzZSkubWVyZ2UodmFsdmVfLmZsYXRNYXBDb25jYXQoKGZ1bmN0aW9uKF90aGlzKSB7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbihzaG91bGRIb2xkKSB7XG4gICAgICAgICAgaWYgKCFzaG91bGRIb2xkKSB7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXMudGFrZVVudGlsKHB1dFRvSG9sZCk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHJldHVybiBfdGhpcy5zY2FuKFtdLCAoZnVuY3Rpb24oeHMsIHgpIHtcbiAgICAgICAgICAgICAgcmV0dXJuIHhzLmNvbmNhdCh4KTtcbiAgICAgICAgICAgIH0pKS5zYW1wbGVkQnkocmVsZWFzZUhvbGQpLnRha2UoMSkuZmxhdE1hcChCYWNvbi5mcm9tQXJyYXkpO1xuICAgICAgICAgIH1cbiAgICAgICAgfTtcbiAgICAgIH0pKHRoaXMpKSkpO1xuICAgIH07XG5cbiAgICBFdmVudFN0cmVhbS5wcm90b3R5cGUuc3RhcnRXaXRoID0gZnVuY3Rpb24oc2VlZCkge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcInN0YXJ0V2l0aFwiLCBzZWVkLCBCYWNvbi5vbmNlKHNlZWQpLmNvbmNhdCh0aGlzKSk7XG4gICAgfTtcblxuICAgIEV2ZW50U3RyZWFtLnByb3RvdHlwZS53aXRoSGFuZGxlciA9IGZ1bmN0aW9uKGhhbmRsZXIpIHtcbiAgICAgIHJldHVybiBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUodGhpcywgXCJ3aXRoSGFuZGxlclwiLCBoYW5kbGVyKSwgdGhpcy5kaXNwYXRjaGVyLnN1YnNjcmliZSwgaGFuZGxlcik7XG4gICAgfTtcblxuICAgIHJldHVybiBFdmVudFN0cmVhbTtcblxuICB9KShPYnNlcnZhYmxlKTtcblxuICBQcm9wZXJ0eSA9IChmdW5jdGlvbihfc3VwZXIpIHtcbiAgICBfX2V4dGVuZHMoUHJvcGVydHksIF9zdXBlcik7XG5cbiAgICBmdW5jdGlvbiBQcm9wZXJ0eShkZXNjLCBzdWJzY3JpYmUsIGhhbmRsZXIpIHtcbiAgICAgIGlmIChpc0Z1bmN0aW9uKGRlc2MpKSB7XG4gICAgICAgIGhhbmRsZXIgPSBzdWJzY3JpYmU7XG4gICAgICAgIHN1YnNjcmliZSA9IGRlc2M7XG4gICAgICAgIGRlc2MgPSBbXTtcbiAgICAgIH1cbiAgICAgIFByb3BlcnR5Ll9fc3VwZXJfXy5jb25zdHJ1Y3Rvci5jYWxsKHRoaXMsIGRlc2MpO1xuICAgICAgYXNzZXJ0RnVuY3Rpb24oc3Vic2NyaWJlKTtcbiAgICAgIHRoaXMuZGlzcGF0Y2hlciA9IG5ldyBQcm9wZXJ0eURpc3BhdGNoZXIodGhpcywgc3Vic2NyaWJlLCBoYW5kbGVyKTtcbiAgICAgIHJlZ2lzdGVyT2JzKHRoaXMpO1xuICAgIH1cblxuICAgIFByb3BlcnR5LnByb3RvdHlwZS5zYW1wbGVkQnkgPSBmdW5jdGlvbihzYW1wbGVyLCBjb21iaW5hdG9yKSB7XG4gICAgICB2YXIgbGF6eSwgcmVzdWx0LCBzYW1wbGVyU291cmNlLCBzdHJlYW0sIHRoaXNTb3VyY2U7XG4gICAgICBpZiAoY29tYmluYXRvciAhPSBudWxsKSB7XG4gICAgICAgIGNvbWJpbmF0b3IgPSB0b0NvbWJpbmF0b3IoY29tYmluYXRvcik7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBsYXp5ID0gdHJ1ZTtcbiAgICAgICAgY29tYmluYXRvciA9IGZ1bmN0aW9uKGYpIHtcbiAgICAgICAgICByZXR1cm4gZi52YWx1ZSgpO1xuICAgICAgICB9O1xuICAgICAgfVxuICAgICAgdGhpc1NvdXJjZSA9IG5ldyBTb3VyY2UodGhpcywgZmFsc2UsIGxhenkpO1xuICAgICAgc2FtcGxlclNvdXJjZSA9IG5ldyBTb3VyY2Uoc2FtcGxlciwgdHJ1ZSwgbGF6eSk7XG4gICAgICBzdHJlYW0gPSBCYWNvbi53aGVuKFt0aGlzU291cmNlLCBzYW1wbGVyU291cmNlXSwgY29tYmluYXRvcik7XG4gICAgICByZXN1bHQgPSBzYW1wbGVyIGluc3RhbmNlb2YgUHJvcGVydHkgPyBzdHJlYW0udG9Qcm9wZXJ0eSgpIDogc3RyZWFtO1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcInNhbXBsZWRCeVwiLCBzYW1wbGVyLCBjb21iaW5hdG9yLCByZXN1bHQpO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUuc2FtcGxlID0gZnVuY3Rpb24oaW50ZXJ2YWwpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJzYW1wbGVcIiwgaW50ZXJ2YWwsIHRoaXMuc2FtcGxlZEJ5KEJhY29uLmludGVydmFsKGludGVydmFsLCB7fSkpKTtcbiAgICB9O1xuXG4gICAgUHJvcGVydHkucHJvdG90eXBlLmNoYW5nZXMgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUodGhpcywgXCJjaGFuZ2VzXCIpLCAoZnVuY3Rpb24oX3RoaXMpIHtcbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgICAgICByZXR1cm4gX3RoaXMuZGlzcGF0Y2hlci5zdWJzY3JpYmUoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgICAgIGlmICghZXZlbnQuaXNJbml0aWFsKCkpIHtcbiAgICAgICAgICAgICAgcmV0dXJuIHNpbmsoZXZlbnQpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH0pO1xuICAgICAgICB9O1xuICAgICAgfSkodGhpcykpO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUud2l0aEhhbmRsZXIgPSBmdW5jdGlvbihoYW5kbGVyKSB7XG4gICAgICByZXR1cm4gbmV3IFByb3BlcnR5KGRlc2NyaWJlKHRoaXMsIFwid2l0aEhhbmRsZXJcIiwgaGFuZGxlciksIHRoaXMuZGlzcGF0Y2hlci5zdWJzY3JpYmUsIGhhbmRsZXIpO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUudG9Qcm9wZXJ0eSA9IGZ1bmN0aW9uKCkge1xuICAgICAgYXNzZXJ0Tm9Bcmd1bWVudHMoYXJndW1lbnRzKTtcbiAgICAgIHJldHVybiB0aGlzO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUudG9FdmVudFN0cmVhbSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIG5ldyBFdmVudFN0cmVhbShkZXNjcmliZSh0aGlzLCBcInRvRXZlbnRTdHJlYW1cIiksIChmdW5jdGlvbihfdGhpcykge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24oc2luaykge1xuICAgICAgICAgIHJldHVybiBfdGhpcy5kaXNwYXRjaGVyLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICAgICAgaWYgKGV2ZW50LmlzSW5pdGlhbCgpKSB7XG4gICAgICAgICAgICAgIGV2ZW50ID0gZXZlbnQudG9OZXh0KCk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICByZXR1cm4gc2luayhldmVudCk7XG4gICAgICAgICAgfSk7XG4gICAgICAgIH07XG4gICAgICB9KSh0aGlzKSk7XG4gICAgfTtcblxuICAgIFByb3BlcnR5LnByb3RvdHlwZS5hbmQgPSBmdW5jdGlvbihvdGhlcikge1xuICAgICAgcmV0dXJuIHdpdGhEZXNjcmlwdGlvbih0aGlzLCBcImFuZFwiLCBvdGhlciwgdGhpcy5jb21iaW5lKG90aGVyLCBmdW5jdGlvbih4LCB5KSB7XG4gICAgICAgIHJldHVybiB4ICYmIHk7XG4gICAgICB9KSk7XG4gICAgfTtcblxuICAgIFByb3BlcnR5LnByb3RvdHlwZS5vciA9IGZ1bmN0aW9uKG90aGVyKSB7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwib3JcIiwgb3RoZXIsIHRoaXMuY29tYmluZShvdGhlciwgZnVuY3Rpb24oeCwgeSkge1xuICAgICAgICByZXR1cm4geCB8fCB5O1xuICAgICAgfSkpO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUuZGVsYXkgPSBmdW5jdGlvbihkZWxheSkge1xuICAgICAgcmV0dXJuIHRoaXMuZGVsYXlDaGFuZ2VzKFwiZGVsYXlcIiwgZGVsYXksIGZ1bmN0aW9uKGNoYW5nZXMpIHtcbiAgICAgICAgcmV0dXJuIGNoYW5nZXMuZGVsYXkoZGVsYXkpO1xuICAgICAgfSk7XG4gICAgfTtcblxuICAgIFByb3BlcnR5LnByb3RvdHlwZS5kZWJvdW5jZSA9IGZ1bmN0aW9uKGRlbGF5KSB7XG4gICAgICByZXR1cm4gdGhpcy5kZWxheUNoYW5nZXMoXCJkZWJvdW5jZVwiLCBkZWxheSwgZnVuY3Rpb24oY2hhbmdlcykge1xuICAgICAgICByZXR1cm4gY2hhbmdlcy5kZWJvdW5jZShkZWxheSk7XG4gICAgICB9KTtcbiAgICB9O1xuXG4gICAgUHJvcGVydHkucHJvdG90eXBlLnRocm90dGxlID0gZnVuY3Rpb24oZGVsYXkpIHtcbiAgICAgIHJldHVybiB0aGlzLmRlbGF5Q2hhbmdlcyhcInRocm90dGxlXCIsIGRlbGF5LCBmdW5jdGlvbihjaGFuZ2VzKSB7XG4gICAgICAgIHJldHVybiBjaGFuZ2VzLnRocm90dGxlKGRlbGF5KTtcbiAgICAgIH0pO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUuZGVsYXlDaGFuZ2VzID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgZGVzYywgZiwgX2k7XG4gICAgICBkZXNjID0gMiA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCwgX2kgPSBhcmd1bWVudHMubGVuZ3RoIC0gMSkgOiAoX2kgPSAwLCBbXSksIGYgPSBhcmd1bWVudHNbX2krK107XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uLmFwcGx5KG51bGwsIFt0aGlzXS5jb25jYXQoX19zbGljZS5jYWxsKGRlc2MpLCBbYWRkUHJvcGVydHlJbml0VmFsdWVUb1N0cmVhbSh0aGlzLCBmKHRoaXMuY2hhbmdlcygpKSldKSk7XG4gICAgfTtcblxuICAgIFByb3BlcnR5LnByb3RvdHlwZS50YWtlVW50aWwgPSBmdW5jdGlvbihzdG9wcGVyKSB7XG4gICAgICB2YXIgY2hhbmdlcztcbiAgICAgIGNoYW5nZXMgPSB0aGlzLmNoYW5nZXMoKS50YWtlVW50aWwoc3RvcHBlcik7XG4gICAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uKHRoaXMsIFwidGFrZVVudGlsXCIsIHN0b3BwZXIsIGFkZFByb3BlcnR5SW5pdFZhbHVlVG9TdHJlYW0odGhpcywgY2hhbmdlcykpO1xuICAgIH07XG5cbiAgICBQcm9wZXJ0eS5wcm90b3R5cGUuc3RhcnRXaXRoID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICAgIHJldHVybiB3aXRoRGVzY3JpcHRpb24odGhpcywgXCJzdGFydFdpdGhcIiwgdmFsdWUsIHRoaXMuc2Nhbih2YWx1ZSwgZnVuY3Rpb24ocHJldiwgbmV4dCkge1xuICAgICAgICByZXR1cm4gbmV4dDtcbiAgICAgIH0pKTtcbiAgICB9O1xuXG4gICAgUHJvcGVydHkucHJvdG90eXBlLmJ1ZmZlcmluZ1Rocm90dGxlID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgX3JlZjE7XG4gICAgICByZXR1cm4gKF9yZWYxID0gUHJvcGVydHkuX19zdXBlcl9fLmJ1ZmZlcmluZ1Rocm90dGxlLmFwcGx5KHRoaXMsIGFyZ3VtZW50cykpLmJ1ZmZlcmluZ1Rocm90dGxlLmFwcGx5KF9yZWYxLCBhcmd1bWVudHMpLnRvUHJvcGVydHkoKTtcbiAgICB9O1xuXG4gICAgcmV0dXJuIFByb3BlcnR5O1xuXG4gIH0pKE9ic2VydmFibGUpO1xuXG4gIGNvbnZlcnRBcmdzVG9GdW5jdGlvbiA9IGZ1bmN0aW9uKG9icywgZiwgYXJncywgbWV0aG9kKSB7XG4gICAgdmFyIHNhbXBsZWQ7XG4gICAgaWYgKGYgaW5zdGFuY2VvZiBQcm9wZXJ0eSkge1xuICAgICAgc2FtcGxlZCA9IGYuc2FtcGxlZEJ5KG9icywgZnVuY3Rpb24ocCwgcykge1xuICAgICAgICByZXR1cm4gW3AsIHNdO1xuICAgICAgfSk7XG4gICAgICByZXR1cm4gbWV0aG9kLmNhbGwoc2FtcGxlZCwgZnVuY3Rpb24oX2FyZykge1xuICAgICAgICB2YXIgcCwgcztcbiAgICAgICAgcCA9IF9hcmdbMF0sIHMgPSBfYXJnWzFdO1xuICAgICAgICByZXR1cm4gcDtcbiAgICAgIH0pLm1hcChmdW5jdGlvbihfYXJnKSB7XG4gICAgICAgIHZhciBwLCBzO1xuICAgICAgICBwID0gX2FyZ1swXSwgcyA9IF9hcmdbMV07XG4gICAgICAgIHJldHVybiBzO1xuICAgICAgfSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIGYgPSBtYWtlRnVuY3Rpb24oZiwgYXJncyk7XG4gICAgICByZXR1cm4gbWV0aG9kLmNhbGwob2JzLCBmKTtcbiAgICB9XG4gIH07XG5cbiAgYWRkUHJvcGVydHlJbml0VmFsdWVUb1N0cmVhbSA9IGZ1bmN0aW9uKHByb3BlcnR5LCBzdHJlYW0pIHtcbiAgICB2YXIganVzdEluaXRWYWx1ZTtcbiAgICBqdXN0SW5pdFZhbHVlID0gbmV3IEV2ZW50U3RyZWFtKGRlc2NyaWJlKHByb3BlcnR5LCBcImp1c3RJbml0VmFsdWVcIiksIGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgIHZhciB1bnN1YiwgdmFsdWU7XG4gICAgICB2YWx1ZSA9IHZvaWQgMDtcbiAgICAgIHVuc3ViID0gcHJvcGVydHkuZGlzcGF0Y2hlci5zdWJzY3JpYmUoZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgaWYgKCFldmVudC5pc0VuZCgpKSB7XG4gICAgICAgICAgdmFsdWUgPSBldmVudDtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gQmFjb24ubm9Nb3JlO1xuICAgICAgfSk7XG4gICAgICBVcGRhdGVCYXJyaWVyLndoZW5Eb25lV2l0aChqdXN0SW5pdFZhbHVlLCBmdW5jdGlvbigpIHtcbiAgICAgICAgaWYgKHZhbHVlICE9IG51bGwpIHtcbiAgICAgICAgICBzaW5rKHZhbHVlKTtcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gc2luayhlbmQoKSk7XG4gICAgICB9KTtcbiAgICAgIHJldHVybiB1bnN1YjtcbiAgICB9KTtcbiAgICByZXR1cm4ganVzdEluaXRWYWx1ZS5jb25jYXQoc3RyZWFtKS50b1Byb3BlcnR5KCk7XG4gIH07XG5cbiAgRGlzcGF0Y2hlciA9IChmdW5jdGlvbigpIHtcbiAgICBmdW5jdGlvbiBEaXNwYXRjaGVyKF9zdWJzY3JpYmUsIF9oYW5kbGVFdmVudCkge1xuICAgICAgdGhpcy5fc3Vic2NyaWJlID0gX3N1YnNjcmliZTtcbiAgICAgIHRoaXMuX2hhbmRsZUV2ZW50ID0gX2hhbmRsZUV2ZW50O1xuICAgICAgdGhpcy5zdWJzY3JpYmUgPSBfX2JpbmQodGhpcy5zdWJzY3JpYmUsIHRoaXMpO1xuICAgICAgdGhpcy5oYW5kbGVFdmVudCA9IF9fYmluZCh0aGlzLmhhbmRsZUV2ZW50LCB0aGlzKTtcbiAgICAgIHRoaXMuc3Vic2NyaXB0aW9ucyA9IFtdO1xuICAgICAgdGhpcy5xdWV1ZSA9IFtdO1xuICAgICAgdGhpcy5wdXNoaW5nID0gZmFsc2U7XG4gICAgICB0aGlzLmVuZGVkID0gZmFsc2U7XG4gICAgICB0aGlzLnByZXZFcnJvciA9IHZvaWQgMDtcbiAgICAgIHRoaXMudW5zdWJTcmMgPSB2b2lkIDA7XG4gICAgfVxuXG4gICAgRGlzcGF0Y2hlci5wcm90b3R5cGUuaGFzU3Vic2NyaWJlcnMgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0aGlzLnN1YnNjcmlwdGlvbnMubGVuZ3RoID4gMDtcbiAgICB9O1xuXG4gICAgRGlzcGF0Y2hlci5wcm90b3R5cGUucmVtb3ZlU3ViID0gZnVuY3Rpb24oc3Vic2NyaXB0aW9uKSB7XG4gICAgICByZXR1cm4gdGhpcy5zdWJzY3JpcHRpb25zID0gXy53aXRob3V0KHN1YnNjcmlwdGlvbiwgdGhpcy5zdWJzY3JpcHRpb25zKTtcbiAgICB9O1xuXG4gICAgRGlzcGF0Y2hlci5wcm90b3R5cGUucHVzaCA9IGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICBpZiAoZXZlbnQuaXNFbmQoKSkge1xuICAgICAgICB0aGlzLmVuZGVkID0gdHJ1ZTtcbiAgICAgIH1cbiAgICAgIHJldHVybiBVcGRhdGVCYXJyaWVyLmluVHJhbnNhY3Rpb24oZXZlbnQsIHRoaXMsIHRoaXMucHVzaEl0LCBbZXZlbnRdKTtcbiAgICB9O1xuXG4gICAgRGlzcGF0Y2hlci5wcm90b3R5cGUucHVzaFRvU3Vic2NyaXB0aW9ucyA9IGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICB2YXIgZSwgcmVwbHksIHN1YiwgdG1wLCBfaSwgX2xlbjtcbiAgICAgIHRyeSB7XG4gICAgICAgIHRtcCA9IHRoaXMuc3Vic2NyaXB0aW9ucztcbiAgICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSB0bXAubGVuZ3RoOyBfaSA8IF9sZW47IF9pKyspIHtcbiAgICAgICAgICBzdWIgPSB0bXBbX2ldO1xuICAgICAgICAgIHJlcGx5ID0gc3ViLnNpbmsoZXZlbnQpO1xuICAgICAgICAgIGlmIChyZXBseSA9PT0gQmFjb24ubm9Nb3JlIHx8IGV2ZW50LmlzRW5kKCkpIHtcbiAgICAgICAgICAgIHRoaXMucmVtb3ZlU3ViKHN1Yik7XG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfSBjYXRjaCAoX2Vycm9yKSB7XG4gICAgICAgIGUgPSBfZXJyb3I7XG4gICAgICAgIHRoaXMucHVzaGluZyA9IGZhbHNlO1xuICAgICAgICB0aGlzLnF1ZXVlID0gW107XG4gICAgICAgIHRocm93IGU7XG4gICAgICB9XG4gICAgfTtcblxuICAgIERpc3BhdGNoZXIucHJvdG90eXBlLnB1c2hJdCA9IGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICBpZiAoIXRoaXMucHVzaGluZykge1xuICAgICAgICBpZiAoZXZlbnQgPT09IHRoaXMucHJldkVycm9yKSB7XG4gICAgICAgICAgcmV0dXJuO1xuICAgICAgICB9XG4gICAgICAgIGlmIChldmVudC5pc0Vycm9yKCkpIHtcbiAgICAgICAgICB0aGlzLnByZXZFcnJvciA9IGV2ZW50O1xuICAgICAgICB9XG4gICAgICAgIHRoaXMucHVzaGluZyA9IHRydWU7XG4gICAgICAgIHRoaXMucHVzaFRvU3Vic2NyaXB0aW9ucyhldmVudCk7XG4gICAgICAgIHRoaXMucHVzaGluZyA9IGZhbHNlO1xuICAgICAgICB3aGlsZSAodGhpcy5xdWV1ZS5sZW5ndGgpIHtcbiAgICAgICAgICBldmVudCA9IHRoaXMucXVldWUuc2hpZnQoKTtcbiAgICAgICAgICB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgICB9XG4gICAgICAgIGlmICh0aGlzLmhhc1N1YnNjcmliZXJzKCkpIHtcbiAgICAgICAgICByZXR1cm4gQmFjb24ubW9yZTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICB0aGlzLnVuc3Vic2NyaWJlRnJvbVNvdXJjZSgpO1xuICAgICAgICAgIHJldHVybiBCYWNvbi5ub01vcmU7XG4gICAgICAgIH1cbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHRoaXMucXVldWUucHVzaChldmVudCk7XG4gICAgICAgIHJldHVybiBCYWNvbi5tb3JlO1xuICAgICAgfVxuICAgIH07XG5cbiAgICBEaXNwYXRjaGVyLnByb3RvdHlwZS5oYW5kbGVFdmVudCA9IGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICBpZiAodGhpcy5faGFuZGxlRXZlbnQpIHtcbiAgICAgICAgcmV0dXJuIHRoaXMuX2hhbmRsZUV2ZW50KGV2ZW50KTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiB0aGlzLnB1c2goZXZlbnQpO1xuICAgICAgfVxuICAgIH07XG5cbiAgICBEaXNwYXRjaGVyLnByb3RvdHlwZS51bnN1YnNjcmliZUZyb21Tb3VyY2UgPSBmdW5jdGlvbigpIHtcbiAgICAgIGlmICh0aGlzLnVuc3ViU3JjKSB7XG4gICAgICAgIHRoaXMudW5zdWJTcmMoKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiB0aGlzLnVuc3ViU3JjID0gdm9pZCAwO1xuICAgIH07XG5cbiAgICBEaXNwYXRjaGVyLnByb3RvdHlwZS5zdWJzY3JpYmUgPSBmdW5jdGlvbihzaW5rKSB7XG4gICAgICB2YXIgc3Vic2NyaXB0aW9uO1xuICAgICAgaWYgKHRoaXMuZW5kZWQpIHtcbiAgICAgICAgc2luayhlbmQoKSk7XG4gICAgICAgIHJldHVybiBub3A7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICBhc3NlcnRGdW5jdGlvbihzaW5rKTtcbiAgICAgICAgc3Vic2NyaXB0aW9uID0ge1xuICAgICAgICAgIHNpbms6IHNpbmtcbiAgICAgICAgfTtcbiAgICAgICAgdGhpcy5zdWJzY3JpcHRpb25zLnB1c2goc3Vic2NyaXB0aW9uKTtcbiAgICAgICAgaWYgKHRoaXMuc3Vic2NyaXB0aW9ucy5sZW5ndGggPT09IDEpIHtcbiAgICAgICAgICB0aGlzLnVuc3ViU3JjID0gdGhpcy5fc3Vic2NyaWJlKHRoaXMuaGFuZGxlRXZlbnQpO1xuICAgICAgICAgIGFzc2VydEZ1bmN0aW9uKHRoaXMudW5zdWJTcmMpO1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiAoZnVuY3Rpb24oX3RoaXMpIHtcbiAgICAgICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICBfdGhpcy5yZW1vdmVTdWIoc3Vic2NyaXB0aW9uKTtcbiAgICAgICAgICAgIGlmICghX3RoaXMuaGFzU3Vic2NyaWJlcnMoKSkge1xuICAgICAgICAgICAgICByZXR1cm4gX3RoaXMudW5zdWJzY3JpYmVGcm9tU291cmNlKCk7XG4gICAgICAgICAgICB9XG4gICAgICAgICAgfTtcbiAgICAgICAgfSkodGhpcyk7XG4gICAgICB9XG4gICAgfTtcblxuICAgIHJldHVybiBEaXNwYXRjaGVyO1xuXG4gIH0pKCk7XG5cbiAgUHJvcGVydHlEaXNwYXRjaGVyID0gKGZ1bmN0aW9uKF9zdXBlcikge1xuICAgIF9fZXh0ZW5kcyhQcm9wZXJ0eURpc3BhdGNoZXIsIF9zdXBlcik7XG5cbiAgICBmdW5jdGlvbiBQcm9wZXJ0eURpc3BhdGNoZXIocHJvcGVydHksIHN1YnNjcmliZSwgaGFuZGxlRXZlbnQpIHtcbiAgICAgIHRoaXMucHJvcGVydHkgPSBwcm9wZXJ0eTtcbiAgICAgIHRoaXMuc3Vic2NyaWJlID0gX19iaW5kKHRoaXMuc3Vic2NyaWJlLCB0aGlzKTtcbiAgICAgIFByb3BlcnR5RGlzcGF0Y2hlci5fX3N1cGVyX18uY29uc3RydWN0b3IuY2FsbCh0aGlzLCBzdWJzY3JpYmUsIGhhbmRsZUV2ZW50KTtcbiAgICAgIHRoaXMuY3VycmVudCA9IE5vbmU7XG4gICAgICB0aGlzLmN1cnJlbnRWYWx1ZVJvb3RJZCA9IHZvaWQgMDtcbiAgICAgIHRoaXMucHJvcGVydHlFbmRlZCA9IGZhbHNlO1xuICAgIH1cblxuICAgIFByb3BlcnR5RGlzcGF0Y2hlci5wcm90b3R5cGUucHVzaCA9IGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICBpZiAoZXZlbnQuaXNFbmQoKSkge1xuICAgICAgICB0aGlzLnByb3BlcnR5RW5kZWQgPSB0cnVlO1xuICAgICAgfVxuICAgICAgaWYgKGV2ZW50Lmhhc1ZhbHVlKCkpIHtcbiAgICAgICAgdGhpcy5jdXJyZW50ID0gbmV3IFNvbWUoZXZlbnQpO1xuICAgICAgICB0aGlzLmN1cnJlbnRWYWx1ZVJvb3RJZCA9IFVwZGF0ZUJhcnJpZXIuY3VycmVudEV2ZW50SWQoKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiBQcm9wZXJ0eURpc3BhdGNoZXIuX19zdXBlcl9fLnB1c2guY2FsbCh0aGlzLCBldmVudCk7XG4gICAgfTtcblxuICAgIFByb3BlcnR5RGlzcGF0Y2hlci5wcm90b3R5cGUubWF5YmVTdWJTb3VyY2UgPSBmdW5jdGlvbihzaW5rLCByZXBseSkge1xuICAgICAgaWYgKHJlcGx5ID09PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgcmV0dXJuIG5vcDtcbiAgICAgIH0gZWxzZSBpZiAodGhpcy5wcm9wZXJ0eUVuZGVkKSB7XG4gICAgICAgIHNpbmsoZW5kKCkpO1xuICAgICAgICByZXR1cm4gbm9wO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmV0dXJuIERpc3BhdGNoZXIucHJvdG90eXBlLnN1YnNjcmliZS5jYWxsKHRoaXMsIHNpbmspO1xuICAgICAgfVxuICAgIH07XG5cbiAgICBQcm9wZXJ0eURpc3BhdGNoZXIucHJvdG90eXBlLnN1YnNjcmliZSA9IGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgIHZhciBkaXNwYXRjaGluZ0lkLCBpbml0U2VudCwgcmVwbHksIHZhbElkO1xuICAgICAgaW5pdFNlbnQgPSBmYWxzZTtcbiAgICAgIHJlcGx5ID0gQmFjb24ubW9yZTtcbiAgICAgIGlmICh0aGlzLmN1cnJlbnQuaXNEZWZpbmVkICYmICh0aGlzLmhhc1N1YnNjcmliZXJzKCkgfHwgdGhpcy5wcm9wZXJ0eUVuZGVkKSkge1xuICAgICAgICBkaXNwYXRjaGluZ0lkID0gVXBkYXRlQmFycmllci5jdXJyZW50RXZlbnRJZCgpO1xuICAgICAgICB2YWxJZCA9IHRoaXMuY3VycmVudFZhbHVlUm9vdElkO1xuICAgICAgICBpZiAoIXRoaXMucHJvcGVydHlFbmRlZCAmJiB2YWxJZCAmJiBkaXNwYXRjaGluZ0lkICYmIGRpc3BhdGNoaW5nSWQgIT09IHZhbElkKSB7XG4gICAgICAgICAgVXBkYXRlQmFycmllci53aGVuRG9uZVdpdGgodGhpcy5wcm9wZXJ0eSwgKGZ1bmN0aW9uKF90aGlzKSB7XG4gICAgICAgICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICAgIGlmIChfdGhpcy5jdXJyZW50VmFsdWVSb290SWQgPT09IHZhbElkKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIHNpbmsoaW5pdGlhbChfdGhpcy5jdXJyZW50LmdldCgpLnZhbHVlKCkpKTtcbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfTtcbiAgICAgICAgICB9KSh0aGlzKSk7XG4gICAgICAgICAgcmV0dXJuIHRoaXMubWF5YmVTdWJTb3VyY2Uoc2luaywgcmVwbHkpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIFVwZGF0ZUJhcnJpZXIuaW5UcmFuc2FjdGlvbih2b2lkIDAsIHRoaXMsIChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIHJldHVybiByZXBseSA9IHNpbmsoaW5pdGlhbCh0aGlzLmN1cnJlbnQuZ2V0KCkudmFsdWUoKSkpO1xuICAgICAgICAgIH0pLCBbXSk7XG4gICAgICAgICAgcmV0dXJuIHRoaXMubWF5YmVTdWJTb3VyY2Uoc2luaywgcmVwbHkpO1xuICAgICAgICB9XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gdGhpcy5tYXliZVN1YlNvdXJjZShzaW5rLCByZXBseSk7XG4gICAgICB9XG4gICAgfTtcblxuICAgIHJldHVybiBQcm9wZXJ0eURpc3BhdGNoZXI7XG5cbiAgfSkoRGlzcGF0Y2hlcik7XG5cbiAgQnVzID0gKGZ1bmN0aW9uKF9zdXBlcikge1xuICAgIF9fZXh0ZW5kcyhCdXMsIF9zdXBlcik7XG5cbiAgICBmdW5jdGlvbiBCdXMoKSB7XG4gICAgICB0aGlzLmd1YXJkZWRTaW5rID0gX19iaW5kKHRoaXMuZ3VhcmRlZFNpbmssIHRoaXMpO1xuICAgICAgdGhpcy5zdWJzY3JpYmVBbGwgPSBfX2JpbmQodGhpcy5zdWJzY3JpYmVBbGwsIHRoaXMpO1xuICAgICAgdGhpcy51bnN1YkFsbCA9IF9fYmluZCh0aGlzLnVuc3ViQWxsLCB0aGlzKTtcbiAgICAgIHRoaXMuc2luayA9IHZvaWQgMDtcbiAgICAgIHRoaXMuc3Vic2NyaXB0aW9ucyA9IFtdO1xuICAgICAgdGhpcy5lbmRlZCA9IGZhbHNlO1xuICAgICAgQnVzLl9fc3VwZXJfXy5jb25zdHJ1Y3Rvci5jYWxsKHRoaXMsIGRlc2NyaWJlKEJhY29uLCBcIkJ1c1wiKSwgdGhpcy5zdWJzY3JpYmVBbGwpO1xuICAgIH1cblxuICAgIEJ1cy5wcm90b3R5cGUudW5zdWJBbGwgPSBmdW5jdGlvbigpIHtcbiAgICAgIHZhciBzdWIsIF9pLCBfbGVuLCBfcmVmMTtcbiAgICAgIF9yZWYxID0gdGhpcy5zdWJzY3JpcHRpb25zO1xuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSBfcmVmMS5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBzdWIgPSBfcmVmMVtfaV07XG4gICAgICAgIGlmICh0eXBlb2Ygc3ViLnVuc3ViID09PSBcImZ1bmN0aW9uXCIpIHtcbiAgICAgICAgICBzdWIudW5zdWIoKTtcbiAgICAgICAgfVxuICAgICAgfVxuICAgICAgcmV0dXJuIHZvaWQgMDtcbiAgICB9O1xuXG4gICAgQnVzLnByb3RvdHlwZS5zdWJzY3JpYmVBbGwgPSBmdW5jdGlvbihuZXdTaW5rKSB7XG4gICAgICB2YXIgc3Vic2NyaXB0aW9uLCBfaSwgX2xlbiwgX3JlZjE7XG4gICAgICB0aGlzLnNpbmsgPSBuZXdTaW5rO1xuICAgICAgX3JlZjEgPSBjbG9uZUFycmF5KHRoaXMuc3Vic2NyaXB0aW9ucyk7XG4gICAgICBmb3IgKF9pID0gMCwgX2xlbiA9IF9yZWYxLmxlbmd0aDsgX2kgPCBfbGVuOyBfaSsrKSB7XG4gICAgICAgIHN1YnNjcmlwdGlvbiA9IF9yZWYxW19pXTtcbiAgICAgICAgdGhpcy5zdWJzY3JpYmVJbnB1dChzdWJzY3JpcHRpb24pO1xuICAgICAgfVxuICAgICAgcmV0dXJuIHRoaXMudW5zdWJBbGw7XG4gICAgfTtcblxuICAgIEJ1cy5wcm90b3R5cGUuZ3VhcmRlZFNpbmsgPSBmdW5jdGlvbihpbnB1dCkge1xuICAgICAgcmV0dXJuIChmdW5jdGlvbihfdGhpcykge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24oZXZlbnQpIHtcbiAgICAgICAgICBpZiAoZXZlbnQuaXNFbmQoKSkge1xuICAgICAgICAgICAgX3RoaXMudW5zdWJzY3JpYmVJbnB1dChpbnB1dCk7XG4gICAgICAgICAgICByZXR1cm4gQmFjb24ubm9Nb3JlO1xuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4gX3RoaXMuc2luayhldmVudCk7XG4gICAgICAgICAgfVxuICAgICAgICB9O1xuICAgICAgfSkodGhpcyk7XG4gICAgfTtcblxuICAgIEJ1cy5wcm90b3R5cGUuc3Vic2NyaWJlSW5wdXQgPSBmdW5jdGlvbihzdWJzY3JpcHRpb24pIHtcbiAgICAgIHJldHVybiBzdWJzY3JpcHRpb24udW5zdWIgPSBzdWJzY3JpcHRpb24uaW5wdXQuZGlzcGF0Y2hlci5zdWJzY3JpYmUodGhpcy5ndWFyZGVkU2luayhzdWJzY3JpcHRpb24uaW5wdXQpKTtcbiAgICB9O1xuXG4gICAgQnVzLnByb3RvdHlwZS51bnN1YnNjcmliZUlucHV0ID0gZnVuY3Rpb24oaW5wdXQpIHtcbiAgICAgIHZhciBpLCBzdWIsIF9pLCBfbGVuLCBfcmVmMTtcbiAgICAgIF9yZWYxID0gdGhpcy5zdWJzY3JpcHRpb25zO1xuICAgICAgZm9yIChpID0gX2kgPSAwLCBfbGVuID0gX3JlZjEubGVuZ3RoOyBfaSA8IF9sZW47IGkgPSArK19pKSB7XG4gICAgICAgIHN1YiA9IF9yZWYxW2ldO1xuICAgICAgICBpZiAoc3ViLmlucHV0ID09PSBpbnB1dCkge1xuICAgICAgICAgIGlmICh0eXBlb2Ygc3ViLnVuc3ViID09PSBcImZ1bmN0aW9uXCIpIHtcbiAgICAgICAgICAgIHN1Yi51bnN1YigpO1xuICAgICAgICAgIH1cbiAgICAgICAgICB0aGlzLnN1YnNjcmlwdGlvbnMuc3BsaWNlKGksIDEpO1xuICAgICAgICAgIHJldHVybjtcbiAgICAgICAgfVxuICAgICAgfVxuICAgIH07XG5cbiAgICBCdXMucHJvdG90eXBlLnBsdWcgPSBmdW5jdGlvbihpbnB1dCkge1xuICAgICAgdmFyIHN1YjtcbiAgICAgIGFzc2VydE9ic2VydmFibGUoaW5wdXQpO1xuICAgICAgaWYgKHRoaXMuZW5kZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuICAgICAgc3ViID0ge1xuICAgICAgICBpbnB1dDogaW5wdXRcbiAgICAgIH07XG4gICAgICB0aGlzLnN1YnNjcmlwdGlvbnMucHVzaChzdWIpO1xuICAgICAgaWYgKCh0aGlzLnNpbmsgIT0gbnVsbCkpIHtcbiAgICAgICAgdGhpcy5zdWJzY3JpYmVJbnB1dChzdWIpO1xuICAgICAgfVxuICAgICAgcmV0dXJuIChmdW5jdGlvbihfdGhpcykge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuIF90aGlzLnVuc3Vic2NyaWJlSW5wdXQoaW5wdXQpO1xuICAgICAgICB9O1xuICAgICAgfSkodGhpcyk7XG4gICAgfTtcblxuICAgIEJ1cy5wcm90b3R5cGUuZW5kID0gZnVuY3Rpb24oKSB7XG4gICAgICB0aGlzLmVuZGVkID0gdHJ1ZTtcbiAgICAgIHRoaXMudW5zdWJBbGwoKTtcbiAgICAgIHJldHVybiB0eXBlb2YgdGhpcy5zaW5rID09PSBcImZ1bmN0aW9uXCIgPyB0aGlzLnNpbmsoZW5kKCkpIDogdm9pZCAwO1xuICAgIH07XG5cbiAgICBCdXMucHJvdG90eXBlLnB1c2ggPSBmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgcmV0dXJuIHR5cGVvZiB0aGlzLnNpbmsgPT09IFwiZnVuY3Rpb25cIiA/IHRoaXMuc2luayhuZXh0KHZhbHVlKSkgOiB2b2lkIDA7XG4gICAgfTtcblxuICAgIEJ1cy5wcm90b3R5cGUuZXJyb3IgPSBmdW5jdGlvbihlcnJvcikge1xuICAgICAgcmV0dXJuIHR5cGVvZiB0aGlzLnNpbmsgPT09IFwiZnVuY3Rpb25cIiA/IHRoaXMuc2luayhuZXcgRXJyb3IoZXJyb3IpKSA6IHZvaWQgMDtcbiAgICB9O1xuXG4gICAgcmV0dXJuIEJ1cztcblxuICB9KShFdmVudFN0cmVhbSk7XG5cbiAgU291cmNlID0gKGZ1bmN0aW9uKCkge1xuICAgIGZ1bmN0aW9uIFNvdXJjZShvYnMsIHN5bmMsIGxhenkpIHtcbiAgICAgIHRoaXMub2JzID0gb2JzO1xuICAgICAgdGhpcy5zeW5jID0gc3luYztcbiAgICAgIHRoaXMubGF6eSA9IGxhenkgIT0gbnVsbCA/IGxhenkgOiBmYWxzZTtcbiAgICAgIHRoaXMucXVldWUgPSBbXTtcbiAgICB9XG5cbiAgICBTb3VyY2UucHJvdG90eXBlLnN1YnNjcmliZSA9IGZ1bmN0aW9uKHNpbmspIHtcbiAgICAgIHJldHVybiB0aGlzLm9icy5kaXNwYXRjaGVyLnN1YnNjcmliZShzaW5rKTtcbiAgICB9O1xuXG4gICAgU291cmNlLnByb3RvdHlwZS50b1N0cmluZyA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXMub2JzLnRvU3RyaW5nKCk7XG4gICAgfTtcblxuICAgIFNvdXJjZS5wcm90b3R5cGUubWFya0VuZGVkID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5lbmRlZCA9IHRydWU7XG4gICAgfTtcblxuICAgIFNvdXJjZS5wcm90b3R5cGUuY29uc3VtZSA9IGZ1bmN0aW9uKCkge1xuICAgICAgaWYgKHRoaXMubGF6eSkge1xuICAgICAgICByZXR1cm4ge1xuICAgICAgICAgIHZhbHVlOiBfLmFsd2F5cyh0aGlzLnF1ZXVlWzBdKVxuICAgICAgICB9O1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgcmV0dXJuIHRoaXMucXVldWVbMF07XG4gICAgICB9XG4gICAgfTtcblxuICAgIFNvdXJjZS5wcm90b3R5cGUucHVzaCA9IGZ1bmN0aW9uKHgpIHtcbiAgICAgIHJldHVybiB0aGlzLnF1ZXVlID0gW3hdO1xuICAgIH07XG5cbiAgICBTb3VyY2UucHJvdG90eXBlLm1heUhhdmUgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0cnVlO1xuICAgIH07XG5cbiAgICBTb3VyY2UucHJvdG90eXBlLmhhc0F0TGVhc3QgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0aGlzLnF1ZXVlLmxlbmd0aDtcbiAgICB9O1xuXG4gICAgU291cmNlLnByb3RvdHlwZS5mbGF0dGVuID0gdHJ1ZTtcblxuICAgIHJldHVybiBTb3VyY2U7XG5cbiAgfSkoKTtcblxuICBDb25zdW1pbmdTb3VyY2UgPSAoZnVuY3Rpb24oX3N1cGVyKSB7XG4gICAgX19leHRlbmRzKENvbnN1bWluZ1NvdXJjZSwgX3N1cGVyKTtcblxuICAgIGZ1bmN0aW9uIENvbnN1bWluZ1NvdXJjZSgpIHtcbiAgICAgIHJldHVybiBDb25zdW1pbmdTb3VyY2UuX19zdXBlcl9fLmNvbnN0cnVjdG9yLmFwcGx5KHRoaXMsIGFyZ3VtZW50cyk7XG4gICAgfVxuXG4gICAgQ29uc3VtaW5nU291cmNlLnByb3RvdHlwZS5jb25zdW1lID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5xdWV1ZS5zaGlmdCgpO1xuICAgIH07XG5cbiAgICBDb25zdW1pbmdTb3VyY2UucHJvdG90eXBlLnB1c2ggPSBmdW5jdGlvbih4KSB7XG4gICAgICByZXR1cm4gdGhpcy5xdWV1ZS5wdXNoKHgpO1xuICAgIH07XG5cbiAgICBDb25zdW1pbmdTb3VyY2UucHJvdG90eXBlLm1heUhhdmUgPSBmdW5jdGlvbihjKSB7XG4gICAgICByZXR1cm4gIXRoaXMuZW5kZWQgfHwgdGhpcy5xdWV1ZS5sZW5ndGggPj0gYztcbiAgICB9O1xuXG4gICAgQ29uc3VtaW5nU291cmNlLnByb3RvdHlwZS5oYXNBdExlYXN0ID0gZnVuY3Rpb24oYykge1xuICAgICAgcmV0dXJuIHRoaXMucXVldWUubGVuZ3RoID49IGM7XG4gICAgfTtcblxuICAgIENvbnN1bWluZ1NvdXJjZS5wcm90b3R5cGUuZmxhdHRlbiA9IGZhbHNlO1xuXG4gICAgcmV0dXJuIENvbnN1bWluZ1NvdXJjZTtcblxuICB9KShTb3VyY2UpO1xuXG4gIEJ1ZmZlcmluZ1NvdXJjZSA9IChmdW5jdGlvbihfc3VwZXIpIHtcbiAgICBfX2V4dGVuZHMoQnVmZmVyaW5nU291cmNlLCBfc3VwZXIpO1xuXG4gICAgZnVuY3Rpb24gQnVmZmVyaW5nU291cmNlKG9icykge1xuICAgICAgQnVmZmVyaW5nU291cmNlLl9fc3VwZXJfXy5jb25zdHJ1Y3Rvci5jYWxsKHRoaXMsIG9icywgdHJ1ZSk7XG4gICAgfVxuXG4gICAgQnVmZmVyaW5nU291cmNlLnByb3RvdHlwZS5jb25zdW1lID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgdmFsdWVzO1xuICAgICAgdmFsdWVzID0gdGhpcy5xdWV1ZTtcbiAgICAgIHRoaXMucXVldWUgPSBbXTtcbiAgICAgIHJldHVybiB7XG4gICAgICAgIHZhbHVlOiBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4gdmFsdWVzO1xuICAgICAgICB9XG4gICAgICB9O1xuICAgIH07XG5cbiAgICBCdWZmZXJpbmdTb3VyY2UucHJvdG90eXBlLnB1c2ggPSBmdW5jdGlvbih4KSB7XG4gICAgICByZXR1cm4gdGhpcy5xdWV1ZS5wdXNoKHgudmFsdWUoKSk7XG4gICAgfTtcblxuICAgIEJ1ZmZlcmluZ1NvdXJjZS5wcm90b3R5cGUuaGFzQXRMZWFzdCA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRydWU7XG4gICAgfTtcblxuICAgIHJldHVybiBCdWZmZXJpbmdTb3VyY2U7XG5cbiAgfSkoU291cmNlKTtcblxuICBTb3VyY2UuaXNUcmlnZ2VyID0gZnVuY3Rpb24ocykge1xuICAgIGlmIChzIGluc3RhbmNlb2YgU291cmNlKSB7XG4gICAgICByZXR1cm4gcy5zeW5jO1xuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gcyBpbnN0YW5jZW9mIEV2ZW50U3RyZWFtO1xuICAgIH1cbiAgfTtcblxuICBTb3VyY2UuZnJvbU9ic2VydmFibGUgPSBmdW5jdGlvbihzKSB7XG4gICAgaWYgKHMgaW5zdGFuY2VvZiBTb3VyY2UpIHtcbiAgICAgIHJldHVybiBzO1xuICAgIH0gZWxzZSBpZiAocyBpbnN0YW5jZW9mIFByb3BlcnR5KSB7XG4gICAgICByZXR1cm4gbmV3IFNvdXJjZShzLCBmYWxzZSk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiBuZXcgQ29uc3VtaW5nU291cmNlKHMsIHRydWUpO1xuICAgIH1cbiAgfTtcblxuICBkZXNjcmliZSA9IGZ1bmN0aW9uKCkge1xuICAgIHZhciBhcmdzLCBjb250ZXh0LCBtZXRob2Q7XG4gICAgY29udGV4dCA9IGFyZ3VtZW50c1swXSwgbWV0aG9kID0gYXJndW1lbnRzWzFdLCBhcmdzID0gMyA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMikgOiBbXTtcbiAgICBpZiAoKGNvbnRleHQgfHwgbWV0aG9kKSBpbnN0YW5jZW9mIERlc2MpIHtcbiAgICAgIHJldHVybiBjb250ZXh0IHx8IG1ldGhvZDtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIG5ldyBEZXNjKGNvbnRleHQsIG1ldGhvZCwgYXJncyk7XG4gICAgfVxuICB9O1xuXG4gIGZpbmREZXBzID0gZnVuY3Rpb24oeCkge1xuICAgIGlmIChpc0FycmF5KHgpKSB7XG4gICAgICByZXR1cm4gXy5mbGF0TWFwKGZpbmREZXBzLCB4KTtcbiAgICB9IGVsc2UgaWYgKGlzT2JzZXJ2YWJsZSh4KSkge1xuICAgICAgcmV0dXJuIFt4XTtcbiAgICB9IGVsc2UgaWYgKHggaW5zdGFuY2VvZiBTb3VyY2UpIHtcbiAgICAgIHJldHVybiBbeC5vYnNdO1xuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gW107XG4gICAgfVxuICB9O1xuXG4gIERlc2MgPSAoZnVuY3Rpb24oKSB7XG4gICAgZnVuY3Rpb24gRGVzYyhjb250ZXh0LCBtZXRob2QsIGFyZ3MpIHtcbiAgICAgIHRoaXMuY29udGV4dCA9IGNvbnRleHQ7XG4gICAgICB0aGlzLm1ldGhvZCA9IG1ldGhvZDtcbiAgICAgIHRoaXMuYXJncyA9IGFyZ3M7XG4gICAgICB0aGlzLmNhY2hlZCA9IHZvaWQgMDtcbiAgICB9XG5cbiAgICBEZXNjLnByb3RvdHlwZS5kZXBzID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5jYWNoZWQgfHwgKHRoaXMuY2FjaGVkID0gZmluZERlcHMoW3RoaXMuY29udGV4dF0uY29uY2F0KHRoaXMuYXJncykpKTtcbiAgICB9O1xuXG4gICAgRGVzYy5wcm90b3R5cGUuYXBwbHkgPSBmdW5jdGlvbihvYnMpIHtcbiAgICAgIG9icy5kZXNjID0gdGhpcztcbiAgICAgIHJldHVybiBvYnM7XG4gICAgfTtcblxuICAgIERlc2MucHJvdG90eXBlLnRvU3RyaW5nID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gXy50b1N0cmluZyh0aGlzLmNvbnRleHQpICsgXCIuXCIgKyBfLnRvU3RyaW5nKHRoaXMubWV0aG9kKSArIFwiKFwiICsgXy5tYXAoXy50b1N0cmluZywgdGhpcy5hcmdzKSArIFwiKVwiO1xuICAgIH07XG5cbiAgICByZXR1cm4gRGVzYztcblxuICB9KSgpO1xuXG4gIHdpdGhEZXNjcmlwdGlvbiA9IGZ1bmN0aW9uKCkge1xuICAgIHZhciBkZXNjLCBvYnMsIF9pO1xuICAgIGRlc2MgPSAyIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAwLCBfaSA9IGFyZ3VtZW50cy5sZW5ndGggLSAxKSA6IChfaSA9IDAsIFtdKSwgb2JzID0gYXJndW1lbnRzW19pKytdO1xuICAgIHJldHVybiBkZXNjcmliZS5hcHBseShudWxsLCBkZXNjKS5hcHBseShvYnMpO1xuICB9O1xuXG4gIEJhY29uLndoZW4gPSBmdW5jdGlvbigpIHtcbiAgICB2YXIgZiwgaSwgaW5kZXgsIGl4LCBsZW4sIG5lZWRzQmFycmllciwgcGF0LCBwYXRTb3VyY2VzLCBwYXRzLCBwYXR0ZXJucywgcmVzdWx0U3RyZWFtLCBzLCBzb3VyY2VzLCB0cmlnZ2VyRm91bmQsIHVzYWdlLCBfaSwgX2osIF9sZW4sIF9sZW4xLCBfcmVmMTtcbiAgICBpZiAoYXJndW1lbnRzLmxlbmd0aCA9PT0gMCkge1xuICAgICAgcmV0dXJuIEJhY29uLm5ldmVyKCk7XG4gICAgfVxuICAgIGxlbiA9IGFyZ3VtZW50cy5sZW5ndGg7XG4gICAgdXNhZ2UgPSBcIndoZW46IGV4cGVjdGluZyBhcmd1bWVudHMgaW4gdGhlIGZvcm0gKE9ic2VydmFibGUrLGZ1bmN0aW9uKStcIjtcbiAgICBhc3NlcnQodXNhZ2UsIGxlbiAlIDIgPT09IDApO1xuICAgIHNvdXJjZXMgPSBbXTtcbiAgICBwYXRzID0gW107XG4gICAgaSA9IDA7XG4gICAgcGF0dGVybnMgPSBbXTtcbiAgICB3aGlsZSAoaSA8IGxlbikge1xuICAgICAgcGF0dGVybnNbaV0gPSBhcmd1bWVudHNbaV07XG4gICAgICBwYXR0ZXJuc1tpICsgMV0gPSBhcmd1bWVudHNbaSArIDFdO1xuICAgICAgcGF0U291cmNlcyA9IF8udG9BcnJheShhcmd1bWVudHNbaV0pO1xuICAgICAgZiA9IGNvbnN0YW50VG9GdW5jdGlvbihhcmd1bWVudHNbaSArIDFdKTtcbiAgICAgIHBhdCA9IHtcbiAgICAgICAgZjogZixcbiAgICAgICAgaXhzOiBbXVxuICAgICAgfTtcbiAgICAgIHRyaWdnZXJGb3VuZCA9IGZhbHNlO1xuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSBwYXRTb3VyY2VzLmxlbmd0aDsgX2kgPCBfbGVuOyBfaSsrKSB7XG4gICAgICAgIHMgPSBwYXRTb3VyY2VzW19pXTtcbiAgICAgICAgaW5kZXggPSBfLmluZGV4T2Yoc291cmNlcywgcyk7XG4gICAgICAgIGlmICghdHJpZ2dlckZvdW5kKSB7XG4gICAgICAgICAgdHJpZ2dlckZvdW5kID0gU291cmNlLmlzVHJpZ2dlcihzKTtcbiAgICAgICAgfVxuICAgICAgICBpZiAoaW5kZXggPCAwKSB7XG4gICAgICAgICAgc291cmNlcy5wdXNoKHMpO1xuICAgICAgICAgIGluZGV4ID0gc291cmNlcy5sZW5ndGggLSAxO1xuICAgICAgICB9XG4gICAgICAgIF9yZWYxID0gcGF0Lml4cztcbiAgICAgICAgZm9yIChfaiA9IDAsIF9sZW4xID0gX3JlZjEubGVuZ3RoOyBfaiA8IF9sZW4xOyBfaisrKSB7XG4gICAgICAgICAgaXggPSBfcmVmMVtfal07XG4gICAgICAgICAgaWYgKGl4LmluZGV4ID09PSBpbmRleCkge1xuICAgICAgICAgICAgaXguY291bnQrKztcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgICAgcGF0Lml4cy5wdXNoKHtcbiAgICAgICAgICBpbmRleDogaW5kZXgsXG4gICAgICAgICAgY291bnQ6IDFcbiAgICAgICAgfSk7XG4gICAgICB9XG4gICAgICBhc3NlcnQoXCJBdCBsZWFzdCBvbmUgRXZlbnRTdHJlYW0gcmVxdWlyZWRcIiwgdHJpZ2dlckZvdW5kIHx8ICghcGF0U291cmNlcy5sZW5ndGgpKTtcbiAgICAgIGlmIChwYXRTb3VyY2VzLmxlbmd0aCA+IDApIHtcbiAgICAgICAgcGF0cy5wdXNoKHBhdCk7XG4gICAgICB9XG4gICAgICBpID0gaSArIDI7XG4gICAgfVxuICAgIGlmICghc291cmNlcy5sZW5ndGgpIHtcbiAgICAgIHJldHVybiBCYWNvbi5uZXZlcigpO1xuICAgIH1cbiAgICBzb3VyY2VzID0gXy5tYXAoU291cmNlLmZyb21PYnNlcnZhYmxlLCBzb3VyY2VzKTtcbiAgICBuZWVkc0JhcnJpZXIgPSAoXy5hbnkoc291cmNlcywgZnVuY3Rpb24ocykge1xuICAgICAgcmV0dXJuIHMuZmxhdHRlbjtcbiAgICB9KSkgJiYgKGNvbnRhaW5zRHVwbGljYXRlRGVwcyhfLm1hcCgoZnVuY3Rpb24ocykge1xuICAgICAgcmV0dXJuIHMub2JzO1xuICAgIH0pLCBzb3VyY2VzKSkpO1xuICAgIHJldHVybiByZXN1bHRTdHJlYW0gPSBuZXcgRXZlbnRTdHJlYW0oZGVzY3JpYmUuYXBwbHkobnVsbCwgW0JhY29uLCBcIndoZW5cIl0uY29uY2F0KF9fc2xpY2UuY2FsbChwYXR0ZXJucykpKSwgZnVuY3Rpb24oc2luaykge1xuICAgICAgdmFyIGNhbm5vdE1hdGNoLCBjYW5ub3RTeW5jLCBlbmRzLCBtYXRjaCwgbm9uRmxhdHRlbmVkLCBwYXJ0LCB0cmlnZ2VycztcbiAgICAgIHRyaWdnZXJzID0gW107XG4gICAgICBlbmRzID0gZmFsc2U7XG4gICAgICBtYXRjaCA9IGZ1bmN0aW9uKHApIHtcbiAgICAgICAgdmFyIF9rLCBfbGVuMiwgX3JlZjI7XG4gICAgICAgIF9yZWYyID0gcC5peHM7XG4gICAgICAgIGZvciAoX2sgPSAwLCBfbGVuMiA9IF9yZWYyLmxlbmd0aDsgX2sgPCBfbGVuMjsgX2srKykge1xuICAgICAgICAgIGkgPSBfcmVmMltfa107XG4gICAgICAgICAgaWYgKCFzb3VyY2VzW2kuaW5kZXhdLmhhc0F0TGVhc3QoaS5jb3VudCkpIHtcbiAgICAgICAgICAgIHJldHVybiBmYWxzZTtcbiAgICAgICAgICB9XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9O1xuICAgICAgY2Fubm90U3luYyA9IGZ1bmN0aW9uKHNvdXJjZSkge1xuICAgICAgICByZXR1cm4gIXNvdXJjZS5zeW5jIHx8IHNvdXJjZS5lbmRlZDtcbiAgICAgIH07XG4gICAgICBjYW5ub3RNYXRjaCA9IGZ1bmN0aW9uKHApIHtcbiAgICAgICAgdmFyIF9rLCBfbGVuMiwgX3JlZjI7XG4gICAgICAgIF9yZWYyID0gcC5peHM7XG4gICAgICAgIGZvciAoX2sgPSAwLCBfbGVuMiA9IF9yZWYyLmxlbmd0aDsgX2sgPCBfbGVuMjsgX2srKykge1xuICAgICAgICAgIGkgPSBfcmVmMltfa107XG4gICAgICAgICAgaWYgKCFzb3VyY2VzW2kuaW5kZXhdLm1heUhhdmUoaS5jb3VudCkpIHtcbiAgICAgICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfTtcbiAgICAgIG5vbkZsYXR0ZW5lZCA9IGZ1bmN0aW9uKHRyaWdnZXIpIHtcbiAgICAgICAgcmV0dXJuICF0cmlnZ2VyLnNvdXJjZS5mbGF0dGVuO1xuICAgICAgfTtcbiAgICAgIHBhcnQgPSBmdW5jdGlvbihzb3VyY2UpIHtcbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uKHVuc3ViQWxsKSB7XG4gICAgICAgICAgdmFyIGZsdXNoLCBmbHVzaExhdGVyLCBmbHVzaFdoaWxlVHJpZ2dlcnM7XG4gICAgICAgICAgZmx1c2hMYXRlciA9IGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgcmV0dXJuIFVwZGF0ZUJhcnJpZXIud2hlbkRvbmVXaXRoKHJlc3VsdFN0cmVhbSwgZmx1c2gpO1xuICAgICAgICAgIH07XG4gICAgICAgICAgZmx1c2hXaGlsZVRyaWdnZXJzID0gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICB2YXIgZXZlbnRzLCBwLCByZXBseSwgdHJpZ2dlciwgX2ssIF9sZW4yO1xuICAgICAgICAgICAgaWYgKHRyaWdnZXJzLmxlbmd0aCA+IDApIHtcbiAgICAgICAgICAgICAgcmVwbHkgPSBCYWNvbi5tb3JlO1xuICAgICAgICAgICAgICB0cmlnZ2VyID0gdHJpZ2dlcnMucG9wKCk7XG4gICAgICAgICAgICAgIGZvciAoX2sgPSAwLCBfbGVuMiA9IHBhdHMubGVuZ3RoOyBfayA8IF9sZW4yOyBfaysrKSB7XG4gICAgICAgICAgICAgICAgcCA9IHBhdHNbX2tdO1xuICAgICAgICAgICAgICAgIGlmIChtYXRjaChwKSkge1xuICAgICAgICAgICAgICAgICAgZXZlbnRzID0gKGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgICAgICAgICB2YXIgX2wsIF9sZW4zLCBfcmVmMiwgX3Jlc3VsdHM7XG4gICAgICAgICAgICAgICAgICAgIF9yZWYyID0gcC5peHM7XG4gICAgICAgICAgICAgICAgICAgIF9yZXN1bHRzID0gW107XG4gICAgICAgICAgICAgICAgICAgIGZvciAoX2wgPSAwLCBfbGVuMyA9IF9yZWYyLmxlbmd0aDsgX2wgPCBfbGVuMzsgX2wrKykge1xuICAgICAgICAgICAgICAgICAgICAgIGkgPSBfcmVmMltfbF07XG4gICAgICAgICAgICAgICAgICAgICAgX3Jlc3VsdHMucHVzaChzb3VyY2VzW2kuaW5kZXhdLmNvbnN1bWUoKSk7XG4gICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICAgICAgcmV0dXJuIF9yZXN1bHRzO1xuICAgICAgICAgICAgICAgICAgfSkoKTtcbiAgICAgICAgICAgICAgICAgIHJlcGx5ID0gc2luayh0cmlnZ2VyLmUuYXBwbHkoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgICAgIHZhciBldmVudCwgdmFsdWVzO1xuICAgICAgICAgICAgICAgICAgICB2YWx1ZXMgPSAoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICAgICAgICAgICAgdmFyIF9sLCBfbGVuMywgX3Jlc3VsdHM7XG4gICAgICAgICAgICAgICAgICAgICAgX3Jlc3VsdHMgPSBbXTtcbiAgICAgICAgICAgICAgICAgICAgICBmb3IgKF9sID0gMCwgX2xlbjMgPSBldmVudHMubGVuZ3RoOyBfbCA8IF9sZW4zOyBfbCsrKSB7XG4gICAgICAgICAgICAgICAgICAgICAgICBldmVudCA9IGV2ZW50c1tfbF07XG4gICAgICAgICAgICAgICAgICAgICAgICBfcmVzdWx0cy5wdXNoKGV2ZW50LnZhbHVlKCkpO1xuICAgICAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICAgICAgICByZXR1cm4gX3Jlc3VsdHM7XG4gICAgICAgICAgICAgICAgICAgIH0pKCk7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBwLmYuYXBwbHkocCwgdmFsdWVzKTtcbiAgICAgICAgICAgICAgICAgIH0pKTtcbiAgICAgICAgICAgICAgICAgIGlmICh0cmlnZ2Vycy5sZW5ndGgpIHtcbiAgICAgICAgICAgICAgICAgICAgdHJpZ2dlcnMgPSBfLmZpbHRlcihub25GbGF0dGVuZWQsIHRyaWdnZXJzKTtcbiAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICAgIGlmIChyZXBseSA9PT0gQmFjb24ubm9Nb3JlKSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiByZXBseTtcbiAgICAgICAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgICAgICAgIHJldHVybiBmbHVzaFdoaWxlVHJpZ2dlcnMoKTtcbiAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICAgIHJldHVybiBCYWNvbi5tb3JlO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH07XG4gICAgICAgICAgZmx1c2ggPSBmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIHZhciByZXBseTtcbiAgICAgICAgICAgIHJlcGx5ID0gZmx1c2hXaGlsZVRyaWdnZXJzKCk7XG4gICAgICAgICAgICBpZiAoZW5kcykge1xuICAgICAgICAgICAgICBlbmRzID0gZmFsc2U7XG4gICAgICAgICAgICAgIGlmIChfLmFsbChzb3VyY2VzLCBjYW5ub3RTeW5jKSB8fCBfLmFsbChwYXRzLCBjYW5ub3RNYXRjaCkpIHtcbiAgICAgICAgICAgICAgICByZXBseSA9IEJhY29uLm5vTW9yZTtcbiAgICAgICAgICAgICAgICBzaW5rKGVuZCgpKTtcbiAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgfVxuICAgICAgICAgICAgaWYgKHJlcGx5ID09PSBCYWNvbi5ub01vcmUpIHtcbiAgICAgICAgICAgICAgdW5zdWJBbGwoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICAgIHJldHVybiByZXBseTtcbiAgICAgICAgICB9O1xuICAgICAgICAgIHJldHVybiBzb3VyY2Uuc3Vic2NyaWJlKGZ1bmN0aW9uKGUpIHtcbiAgICAgICAgICAgIHZhciByZXBseTtcbiAgICAgICAgICAgIGlmIChlLmlzRW5kKCkpIHtcbiAgICAgICAgICAgICAgZW5kcyA9IHRydWU7XG4gICAgICAgICAgICAgIHNvdXJjZS5tYXJrRW5kZWQoKTtcbiAgICAgICAgICAgICAgZmx1c2hMYXRlcigpO1xuICAgICAgICAgICAgfSBlbHNlIGlmIChlLmlzRXJyb3IoKSkge1xuICAgICAgICAgICAgICByZXBseSA9IHNpbmsoZSk7XG4gICAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgICBzb3VyY2UucHVzaChlKTtcbiAgICAgICAgICAgICAgaWYgKHNvdXJjZS5zeW5jKSB7XG4gICAgICAgICAgICAgICAgdHJpZ2dlcnMucHVzaCh7XG4gICAgICAgICAgICAgICAgICBzb3VyY2U6IHNvdXJjZSxcbiAgICAgICAgICAgICAgICAgIGU6IGVcbiAgICAgICAgICAgICAgICB9KTtcbiAgICAgICAgICAgICAgICBpZiAobmVlZHNCYXJyaWVyIHx8IFVwZGF0ZUJhcnJpZXIuaGFzV2FpdGVycygpKSB7XG4gICAgICAgICAgICAgICAgICBmbHVzaExhdGVyKCk7XG4gICAgICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICAgIGZsdXNoKCk7XG4gICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICB9XG4gICAgICAgICAgICBpZiAocmVwbHkgPT09IEJhY29uLm5vTW9yZSkge1xuICAgICAgICAgICAgICB1bnN1YkFsbCgpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgICAgcmV0dXJuIHJlcGx5IHx8IEJhY29uLm1vcmU7XG4gICAgICAgICAgfSk7XG4gICAgICAgIH07XG4gICAgICB9O1xuICAgICAgcmV0dXJuIGNvbXBvc2l0ZVVuc3Vic2NyaWJlLmFwcGx5KG51bGwsIChmdW5jdGlvbigpIHtcbiAgICAgICAgdmFyIF9rLCBfbGVuMiwgX3Jlc3VsdHM7XG4gICAgICAgIF9yZXN1bHRzID0gW107XG4gICAgICAgIGZvciAoX2sgPSAwLCBfbGVuMiA9IHNvdXJjZXMubGVuZ3RoOyBfayA8IF9sZW4yOyBfaysrKSB7XG4gICAgICAgICAgcyA9IHNvdXJjZXNbX2tdO1xuICAgICAgICAgIF9yZXN1bHRzLnB1c2gocGFydChzKSk7XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIF9yZXN1bHRzO1xuICAgICAgfSkoKSk7XG4gICAgfSk7XG4gIH07XG5cbiAgY29udGFpbnNEdXBsaWNhdGVEZXBzID0gZnVuY3Rpb24ob2JzZXJ2YWJsZXMsIHN0YXRlKSB7XG4gICAgdmFyIGNoZWNrT2JzZXJ2YWJsZTtcbiAgICBpZiAoc3RhdGUgPT0gbnVsbCkge1xuICAgICAgc3RhdGUgPSBbXTtcbiAgICB9XG4gICAgY2hlY2tPYnNlcnZhYmxlID0gZnVuY3Rpb24ob2JzKSB7XG4gICAgICB2YXIgZGVwcztcbiAgICAgIGlmIChfLmNvbnRhaW5zKHN0YXRlLCBvYnMpKSB7XG4gICAgICAgIHJldHVybiB0cnVlO1xuICAgICAgfSBlbHNlIHtcbiAgICAgICAgZGVwcyA9IG9icy5pbnRlcm5hbERlcHMoKTtcbiAgICAgICAgaWYgKGRlcHMubGVuZ3RoKSB7XG4gICAgICAgICAgc3RhdGUucHVzaChvYnMpO1xuICAgICAgICAgIHJldHVybiBfLmFueShkZXBzLCBjaGVja09ic2VydmFibGUpO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIHN0YXRlLnB1c2gob2JzKTtcbiAgICAgICAgICByZXR1cm4gZmFsc2U7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9O1xuICAgIHJldHVybiBfLmFueShvYnNlcnZhYmxlcywgY2hlY2tPYnNlcnZhYmxlKTtcbiAgfTtcblxuICBCYWNvbi51cGRhdGUgPSBmdW5jdGlvbigpIHtcbiAgICB2YXIgaSwgaW5pdGlhbCwgbGF0ZUJpbmRGaXJzdCwgcGF0dGVybnM7XG4gICAgaW5pdGlhbCA9IGFyZ3VtZW50c1swXSwgcGF0dGVybnMgPSAyIDw9IGFyZ3VtZW50cy5sZW5ndGggPyBfX3NsaWNlLmNhbGwoYXJndW1lbnRzLCAxKSA6IFtdO1xuICAgIGxhdGVCaW5kRmlyc3QgPSBmdW5jdGlvbihmKSB7XG4gICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgIHZhciBhcmdzO1xuICAgICAgICBhcmdzID0gMSA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCkgOiBbXTtcbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uKGkpIHtcbiAgICAgICAgICByZXR1cm4gZi5hcHBseShudWxsLCBbaV0uY29uY2F0KGFyZ3MpKTtcbiAgICAgICAgfTtcbiAgICAgIH07XG4gICAgfTtcbiAgICBpID0gcGF0dGVybnMubGVuZ3RoIC0gMTtcbiAgICB3aGlsZSAoaSA+IDApIHtcbiAgICAgIGlmICghKHBhdHRlcm5zW2ldIGluc3RhbmNlb2YgRnVuY3Rpb24pKSB7XG4gICAgICAgIHBhdHRlcm5zW2ldID0gKGZ1bmN0aW9uKHgpIHtcbiAgICAgICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgICByZXR1cm4geDtcbiAgICAgICAgICB9O1xuICAgICAgICB9KShwYXR0ZXJuc1tpXSk7XG4gICAgICB9XG4gICAgICBwYXR0ZXJuc1tpXSA9IGxhdGVCaW5kRmlyc3QocGF0dGVybnNbaV0pO1xuICAgICAgaSA9IGkgLSAyO1xuICAgIH1cbiAgICByZXR1cm4gd2l0aERlc2NyaXB0aW9uLmFwcGx5KG51bGwsIFtCYWNvbiwgXCJ1cGRhdGVcIiwgaW5pdGlhbF0uY29uY2F0KF9fc2xpY2UuY2FsbChwYXR0ZXJucyksIFtCYWNvbi53aGVuLmFwcGx5KEJhY29uLCBwYXR0ZXJucykuc2Nhbihpbml0aWFsLCAoZnVuY3Rpb24oeCwgZikge1xuICAgICAgcmV0dXJuIGYoeCk7XG4gICAgfSkpXSkpO1xuICB9O1xuXG4gIGNvbXBvc2l0ZVVuc3Vic2NyaWJlID0gZnVuY3Rpb24oKSB7XG4gICAgdmFyIHNzO1xuICAgIHNzID0gMSA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCkgOiBbXTtcbiAgICByZXR1cm4gbmV3IENvbXBvc2l0ZVVuc3Vic2NyaWJlKHNzKS51bnN1YnNjcmliZTtcbiAgfTtcblxuICBDb21wb3NpdGVVbnN1YnNjcmliZSA9IChmdW5jdGlvbigpIHtcbiAgICBmdW5jdGlvbiBDb21wb3NpdGVVbnN1YnNjcmliZShzcykge1xuICAgICAgdmFyIHMsIF9pLCBfbGVuO1xuICAgICAgaWYgKHNzID09IG51bGwpIHtcbiAgICAgICAgc3MgPSBbXTtcbiAgICAgIH1cbiAgICAgIHRoaXMudW5zdWJzY3JpYmUgPSBfX2JpbmQodGhpcy51bnN1YnNjcmliZSwgdGhpcyk7XG4gICAgICB0aGlzLnVuc3Vic2NyaWJlZCA9IGZhbHNlO1xuICAgICAgdGhpcy5zdWJzY3JpcHRpb25zID0gW107XG4gICAgICB0aGlzLnN0YXJ0aW5nID0gW107XG4gICAgICBmb3IgKF9pID0gMCwgX2xlbiA9IHNzLmxlbmd0aDsgX2kgPCBfbGVuOyBfaSsrKSB7XG4gICAgICAgIHMgPSBzc1tfaV07XG4gICAgICAgIHRoaXMuYWRkKHMpO1xuICAgICAgfVxuICAgIH1cblxuICAgIENvbXBvc2l0ZVVuc3Vic2NyaWJlLnByb3RvdHlwZS5hZGQgPSBmdW5jdGlvbihzdWJzY3JpcHRpb24pIHtcbiAgICAgIHZhciBlbmRlZCwgdW5zdWIsIHVuc3ViTWU7XG4gICAgICBpZiAodGhpcy51bnN1YnNjcmliZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuICAgICAgZW5kZWQgPSBmYWxzZTtcbiAgICAgIHVuc3ViID0gbm9wO1xuICAgICAgdGhpcy5zdGFydGluZy5wdXNoKHN1YnNjcmlwdGlvbik7XG4gICAgICB1bnN1Yk1lID0gKGZ1bmN0aW9uKF90aGlzKSB7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbigpIHtcbiAgICAgICAgICBpZiAoX3RoaXMudW5zdWJzY3JpYmVkKSB7XG4gICAgICAgICAgICByZXR1cm47XG4gICAgICAgICAgfVxuICAgICAgICAgIGVuZGVkID0gdHJ1ZTtcbiAgICAgICAgICBfdGhpcy5yZW1vdmUodW5zdWIpO1xuICAgICAgICAgIHJldHVybiBfLnJlbW92ZShzdWJzY3JpcHRpb24sIF90aGlzLnN0YXJ0aW5nKTtcbiAgICAgICAgfTtcbiAgICAgIH0pKHRoaXMpO1xuICAgICAgdW5zdWIgPSBzdWJzY3JpcHRpb24odGhpcy51bnN1YnNjcmliZSwgdW5zdWJNZSk7XG4gICAgICBpZiAoISh0aGlzLnVuc3Vic2NyaWJlZCB8fCBlbmRlZCkpIHtcbiAgICAgICAgdGhpcy5zdWJzY3JpcHRpb25zLnB1c2godW5zdWIpO1xuICAgICAgfVxuICAgICAgXy5yZW1vdmUoc3Vic2NyaXB0aW9uLCB0aGlzLnN0YXJ0aW5nKTtcbiAgICAgIHJldHVybiB1bnN1YjtcbiAgICB9O1xuXG4gICAgQ29tcG9zaXRlVW5zdWJzY3JpYmUucHJvdG90eXBlLnJlbW92ZSA9IGZ1bmN0aW9uKHVuc3ViKSB7XG4gICAgICBpZiAodGhpcy51bnN1YnNjcmliZWQpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuICAgICAgaWYgKChfLnJlbW92ZSh1bnN1YiwgdGhpcy5zdWJzY3JpcHRpb25zKSkgIT09IHZvaWQgMCkge1xuICAgICAgICByZXR1cm4gdW5zdWIoKTtcbiAgICAgIH1cbiAgICB9O1xuXG4gICAgQ29tcG9zaXRlVW5zdWJzY3JpYmUucHJvdG90eXBlLnVuc3Vic2NyaWJlID0gZnVuY3Rpb24oKSB7XG4gICAgICB2YXIgcywgX2ksIF9sZW4sIF9yZWYxO1xuICAgICAgaWYgKHRoaXMudW5zdWJzY3JpYmVkKSB7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cbiAgICAgIHRoaXMudW5zdWJzY3JpYmVkID0gdHJ1ZTtcbiAgICAgIF9yZWYxID0gdGhpcy5zdWJzY3JpcHRpb25zO1xuICAgICAgZm9yIChfaSA9IDAsIF9sZW4gPSBfcmVmMS5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBzID0gX3JlZjFbX2ldO1xuICAgICAgICBzKCk7XG4gICAgICB9XG4gICAgICB0aGlzLnN1YnNjcmlwdGlvbnMgPSBbXTtcbiAgICAgIHJldHVybiB0aGlzLnN0YXJ0aW5nID0gW107XG4gICAgfTtcblxuICAgIENvbXBvc2l0ZVVuc3Vic2NyaWJlLnByb3RvdHlwZS5jb3VudCA9IGZ1bmN0aW9uKCkge1xuICAgICAgaWYgKHRoaXMudW5zdWJzY3JpYmVkKSB7XG4gICAgICAgIHJldHVybiAwO1xuICAgICAgfVxuICAgICAgcmV0dXJuIHRoaXMuc3Vic2NyaXB0aW9ucy5sZW5ndGggKyB0aGlzLnN0YXJ0aW5nLmxlbmd0aDtcbiAgICB9O1xuXG4gICAgQ29tcG9zaXRlVW5zdWJzY3JpYmUucHJvdG90eXBlLmVtcHR5ID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5jb3VudCgpID09PSAwO1xuICAgIH07XG5cbiAgICByZXR1cm4gQ29tcG9zaXRlVW5zdWJzY3JpYmU7XG5cbiAgfSkoKTtcblxuICBCYWNvbi5Db21wb3NpdGVVbnN1YnNjcmliZSA9IENvbXBvc2l0ZVVuc3Vic2NyaWJlO1xuXG4gIFNvbWUgPSAoZnVuY3Rpb24oKSB7XG4gICAgZnVuY3Rpb24gU29tZSh2YWx1ZSkge1xuICAgICAgdGhpcy52YWx1ZSA9IHZhbHVlO1xuICAgIH1cblxuICAgIFNvbWUucHJvdG90eXBlLmdldE9yRWxzZSA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXMudmFsdWU7XG4gICAgfTtcblxuICAgIFNvbWUucHJvdG90eXBlLmdldCA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHRoaXMudmFsdWU7XG4gICAgfTtcblxuICAgIFNvbWUucHJvdG90eXBlLmZpbHRlciA9IGZ1bmN0aW9uKGYpIHtcbiAgICAgIGlmIChmKHRoaXMudmFsdWUpKSB7XG4gICAgICAgIHJldHVybiBuZXcgU29tZSh0aGlzLnZhbHVlKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBOb25lO1xuICAgICAgfVxuICAgIH07XG5cbiAgICBTb21lLnByb3RvdHlwZS5tYXAgPSBmdW5jdGlvbihmKSB7XG4gICAgICByZXR1cm4gbmV3IFNvbWUoZih0aGlzLnZhbHVlKSk7XG4gICAgfTtcblxuICAgIFNvbWUucHJvdG90eXBlLmZvckVhY2ggPSBmdW5jdGlvbihmKSB7XG4gICAgICByZXR1cm4gZih0aGlzLnZhbHVlKTtcbiAgICB9O1xuXG4gICAgU29tZS5wcm90b3R5cGUuaXNEZWZpbmVkID0gdHJ1ZTtcblxuICAgIFNvbWUucHJvdG90eXBlLnRvQXJyYXkgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBbdGhpcy52YWx1ZV07XG4gICAgfTtcblxuICAgIFNvbWUucHJvdG90eXBlLmluc3BlY3QgPSBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBcIlNvbWUoXCIgKyB0aGlzLnZhbHVlICsgXCIpXCI7XG4gICAgfTtcblxuICAgIFNvbWUucHJvdG90eXBlLnRvU3RyaW5nID0gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gdGhpcy5pbnNwZWN0KCk7XG4gICAgfTtcblxuICAgIHJldHVybiBTb21lO1xuXG4gIH0pKCk7XG5cbiAgTm9uZSA9IHtcbiAgICBnZXRPckVsc2U6IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgICByZXR1cm4gdmFsdWU7XG4gICAgfSxcbiAgICBmaWx0ZXI6IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIE5vbmU7XG4gICAgfSxcbiAgICBtYXA6IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIE5vbmU7XG4gICAgfSxcbiAgICBmb3JFYWNoOiBmdW5jdGlvbigpIHt9LFxuICAgIGlzRGVmaW5lZDogZmFsc2UsXG4gICAgdG9BcnJheTogZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gW107XG4gICAgfSxcbiAgICBpbnNwZWN0OiBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiBcIk5vbmVcIjtcbiAgICB9LFxuICAgIHRvU3RyaW5nOiBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiB0aGlzLmluc3BlY3QoKTtcbiAgICB9XG4gIH07XG5cbiAgVXBkYXRlQmFycmllciA9IChmdW5jdGlvbigpIHtcbiAgICB2YXIgYWZ0ZXJUcmFuc2FjdGlvbiwgYWZ0ZXJzLCBhZnRlcnNJbmRleCwgY3VycmVudEV2ZW50SWQsIGZsdXNoLCBmbHVzaERlcHNPZiwgZmx1c2hXYWl0ZXJzLCBoYXNXYWl0ZXJzLCBpblRyYW5zYWN0aW9uLCByb290RXZlbnQsIHdhaXRlck9icywgd2FpdGVycywgd2hlbkRvbmVXaXRoLCB3cmFwcGVkU3Vic2NyaWJlO1xuICAgIHJvb3RFdmVudCA9IHZvaWQgMDtcbiAgICB3YWl0ZXJPYnMgPSBbXTtcbiAgICB3YWl0ZXJzID0ge307XG4gICAgYWZ0ZXJzID0gW107XG4gICAgYWZ0ZXJzSW5kZXggPSAwO1xuICAgIGFmdGVyVHJhbnNhY3Rpb24gPSBmdW5jdGlvbihmKSB7XG4gICAgICBpZiAocm9vdEV2ZW50KSB7XG4gICAgICAgIHJldHVybiBhZnRlcnMucHVzaChmKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBmKCk7XG4gICAgICB9XG4gICAgfTtcbiAgICB3aGVuRG9uZVdpdGggPSBmdW5jdGlvbihvYnMsIGYpIHtcbiAgICAgIHZhciBvYnNXYWl0ZXJzO1xuICAgICAgaWYgKHJvb3RFdmVudCkge1xuICAgICAgICBvYnNXYWl0ZXJzID0gd2FpdGVyc1tvYnMuaWRdO1xuICAgICAgICBpZiAob2JzV2FpdGVycyA9PSBudWxsKSB7XG4gICAgICAgICAgb2JzV2FpdGVycyA9IHdhaXRlcnNbb2JzLmlkXSA9IFtmXTtcbiAgICAgICAgICByZXR1cm4gd2FpdGVyT2JzLnB1c2gob2JzKTtcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByZXR1cm4gb2JzV2FpdGVycy5wdXNoKGYpO1xuICAgICAgICB9XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gZigpO1xuICAgICAgfVxuICAgIH07XG4gICAgZmx1c2ggPSBmdW5jdGlvbigpIHtcbiAgICAgIHdoaWxlICh3YWl0ZXJPYnMubGVuZ3RoID4gMCkge1xuICAgICAgICBmbHVzaFdhaXRlcnMoMCk7XG4gICAgICB9XG4gICAgICByZXR1cm4gdm9pZCAwO1xuICAgIH07XG4gICAgZmx1c2hXYWl0ZXJzID0gZnVuY3Rpb24oaW5kZXgpIHtcbiAgICAgIHZhciBmLCBvYnMsIG9ic0lkLCBvYnNXYWl0ZXJzLCBfaSwgX2xlbjtcbiAgICAgIG9icyA9IHdhaXRlck9ic1tpbmRleF07XG4gICAgICBvYnNJZCA9IG9icy5pZDtcbiAgICAgIG9ic1dhaXRlcnMgPSB3YWl0ZXJzW29ic0lkXTtcbiAgICAgIHdhaXRlck9icy5zcGxpY2UoaW5kZXgsIDEpO1xuICAgICAgZGVsZXRlIHdhaXRlcnNbb2JzSWRdO1xuICAgICAgZmx1c2hEZXBzT2Yob2JzKTtcbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gb2JzV2FpdGVycy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBmID0gb2JzV2FpdGVyc1tfaV07XG4gICAgICAgIGYoKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiB2b2lkIDA7XG4gICAgfTtcbiAgICBmbHVzaERlcHNPZiA9IGZ1bmN0aW9uKG9icykge1xuICAgICAgdmFyIGRlcCwgZGVwcywgaW5kZXgsIF9pLCBfbGVuO1xuICAgICAgZGVwcyA9IG9icy5pbnRlcm5hbERlcHMoKTtcbiAgICAgIGZvciAoX2kgPSAwLCBfbGVuID0gZGVwcy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBkZXAgPSBkZXBzW19pXTtcbiAgICAgICAgZmx1c2hEZXBzT2YoZGVwKTtcbiAgICAgICAgaWYgKHdhaXRlcnNbZGVwLmlkXSkge1xuICAgICAgICAgIGluZGV4ID0gXy5pbmRleE9mKHdhaXRlck9icywgZGVwKTtcbiAgICAgICAgICBmbHVzaFdhaXRlcnMoaW5kZXgpO1xuICAgICAgICB9XG4gICAgICB9XG4gICAgICByZXR1cm4gdm9pZCAwO1xuICAgIH07XG4gICAgaW5UcmFuc2FjdGlvbiA9IGZ1bmN0aW9uKGV2ZW50LCBjb250ZXh0LCBmLCBhcmdzKSB7XG4gICAgICB2YXIgYWZ0ZXIsIHJlc3VsdDtcbiAgICAgIGlmIChyb290RXZlbnQpIHtcbiAgICAgICAgcmV0dXJuIGYuYXBwbHkoY29udGV4dCwgYXJncyk7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByb290RXZlbnQgPSBldmVudDtcbiAgICAgICAgdHJ5IHtcbiAgICAgICAgICByZXN1bHQgPSBmLmFwcGx5KGNvbnRleHQsIGFyZ3MpO1xuICAgICAgICAgIGZsdXNoKCk7XG4gICAgICAgIH0gZmluYWxseSB7XG4gICAgICAgICAgcm9vdEV2ZW50ID0gdm9pZCAwO1xuICAgICAgICAgIHdoaWxlIChhZnRlcnNJbmRleCA8IGFmdGVycy5sZW5ndGgpIHtcbiAgICAgICAgICAgIGFmdGVyID0gYWZ0ZXJzW2FmdGVyc0luZGV4XTtcbiAgICAgICAgICAgIGFmdGVyc0luZGV4Kys7XG4gICAgICAgICAgICBhZnRlcigpO1xuICAgICAgICAgIH1cbiAgICAgICAgICBhZnRlcnNJbmRleCA9IDA7XG4gICAgICAgICAgYWZ0ZXJzID0gW107XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIHJlc3VsdDtcbiAgICAgIH1cbiAgICB9O1xuICAgIGN1cnJlbnRFdmVudElkID0gZnVuY3Rpb24oKSB7XG4gICAgICBpZiAocm9vdEV2ZW50KSB7XG4gICAgICAgIHJldHVybiByb290RXZlbnQuaWQ7XG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gdm9pZCAwO1xuICAgICAgfVxuICAgIH07XG4gICAgd3JhcHBlZFN1YnNjcmliZSA9IGZ1bmN0aW9uKG9icywgc2luaykge1xuICAgICAgdmFyIGRvVW5zdWIsIHNob3VsZFVuc3ViLCB1bnN1YiwgdW5zdWJkO1xuICAgICAgdW5zdWJkID0gZmFsc2U7XG4gICAgICBzaG91bGRVbnN1YiA9IGZhbHNlO1xuICAgICAgZG9VbnN1YiA9IGZ1bmN0aW9uKCkge1xuICAgICAgICByZXR1cm4gc2hvdWxkVW5zdWIgPSB0cnVlO1xuICAgICAgfTtcbiAgICAgIHVuc3ViID0gZnVuY3Rpb24oKSB7XG4gICAgICAgIHVuc3ViZCA9IHRydWU7XG4gICAgICAgIHJldHVybiBkb1Vuc3ViKCk7XG4gICAgICB9O1xuICAgICAgZG9VbnN1YiA9IG9icy5kaXNwYXRjaGVyLnN1YnNjcmliZShmdW5jdGlvbihldmVudCkge1xuICAgICAgICByZXR1cm4gYWZ0ZXJUcmFuc2FjdGlvbihmdW5jdGlvbigpIHtcbiAgICAgICAgICB2YXIgcmVwbHk7XG4gICAgICAgICAgaWYgKCF1bnN1YmQpIHtcbiAgICAgICAgICAgIHJlcGx5ID0gc2luayhldmVudCk7XG4gICAgICAgICAgICBpZiAocmVwbHkgPT09IEJhY29uLm5vTW9yZSkge1xuICAgICAgICAgICAgICByZXR1cm4gdW5zdWIoKTtcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9XG4gICAgICAgIH0pO1xuICAgICAgfSk7XG4gICAgICBpZiAoc2hvdWxkVW5zdWIpIHtcbiAgICAgICAgZG9VbnN1YigpO1xuICAgICAgfVxuICAgICAgcmV0dXJuIHVuc3ViO1xuICAgIH07XG4gICAgaGFzV2FpdGVycyA9IGZ1bmN0aW9uKCkge1xuICAgICAgcmV0dXJuIHdhaXRlck9icy5sZW5ndGggPiAwO1xuICAgIH07XG4gICAgcmV0dXJuIHtcbiAgICAgIHdoZW5Eb25lV2l0aDogd2hlbkRvbmVXaXRoLFxuICAgICAgaGFzV2FpdGVyczogaGFzV2FpdGVycyxcbiAgICAgIGluVHJhbnNhY3Rpb246IGluVHJhbnNhY3Rpb24sXG4gICAgICBjdXJyZW50RXZlbnRJZDogY3VycmVudEV2ZW50SWQsXG4gICAgICB3cmFwcGVkU3Vic2NyaWJlOiB3cmFwcGVkU3Vic2NyaWJlLFxuICAgICAgYWZ0ZXJUcmFuc2FjdGlvbjogYWZ0ZXJUcmFuc2FjdGlvblxuICAgIH07XG4gIH0pKCk7XG5cbiAgQmFjb24uRXZlbnRTdHJlYW0gPSBFdmVudFN0cmVhbTtcblxuICBCYWNvbi5Qcm9wZXJ0eSA9IFByb3BlcnR5O1xuXG4gIEJhY29uLk9ic2VydmFibGUgPSBPYnNlcnZhYmxlO1xuXG4gIEJhY29uLkJ1cyA9IEJ1cztcblxuICBCYWNvbi5Jbml0aWFsID0gSW5pdGlhbDtcblxuICBCYWNvbi5OZXh0ID0gTmV4dDtcblxuICBCYWNvbi5FbmQgPSBFbmQ7XG5cbiAgQmFjb24uRXJyb3IgPSBFcnJvcjtcblxuICBub3AgPSBmdW5jdGlvbigpIHt9O1xuXG4gIGxhdHRlciA9IGZ1bmN0aW9uKF8sIHgpIHtcbiAgICByZXR1cm4geDtcbiAgfTtcblxuICBmb3JtZXIgPSBmdW5jdGlvbih4LCBfKSB7XG4gICAgcmV0dXJuIHg7XG4gIH07XG5cbiAgaW5pdGlhbCA9IGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgcmV0dXJuIG5ldyBJbml0aWFsKHZhbHVlLCB0cnVlKTtcbiAgfTtcblxuICBuZXh0ID0gZnVuY3Rpb24odmFsdWUpIHtcbiAgICByZXR1cm4gbmV3IE5leHQodmFsdWUsIHRydWUpO1xuICB9O1xuXG4gIGVuZCA9IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiBuZXcgRW5kKCk7XG4gIH07XG5cbiAgdG9FdmVudCA9IGZ1bmN0aW9uKHgpIHtcbiAgICBpZiAoeCBpbnN0YW5jZW9mIEV2ZW50KSB7XG4gICAgICByZXR1cm4geDtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIG5leHQoeCk7XG4gICAgfVxuICB9O1xuXG4gIGNsb25lQXJyYXkgPSBmdW5jdGlvbih4cykge1xuICAgIHJldHVybiB4cy5zbGljZSgwKTtcbiAgfTtcblxuICBhc3NlcnQgPSBmdW5jdGlvbihtZXNzYWdlLCBjb25kaXRpb24pIHtcbiAgICBpZiAoIWNvbmRpdGlvbikge1xuICAgICAgdGhyb3cgbmV3IEV4Y2VwdGlvbihtZXNzYWdlKTtcbiAgICB9XG4gIH07XG5cbiAgYXNzZXJ0RXZlbnRTdHJlYW0gPSBmdW5jdGlvbihldmVudCkge1xuICAgIGlmICghKGV2ZW50IGluc3RhbmNlb2YgRXZlbnRTdHJlYW0pKSB7XG4gICAgICB0aHJvdyBuZXcgRXhjZXB0aW9uKFwibm90IGFuIEV2ZW50U3RyZWFtIDogXCIgKyBldmVudCk7XG4gICAgfVxuICB9O1xuXG4gIGFzc2VydE9ic2VydmFibGUgPSBmdW5jdGlvbihldmVudCkge1xuICAgIGlmICghKGV2ZW50IGluc3RhbmNlb2YgT2JzZXJ2YWJsZSkpIHtcbiAgICAgIHRocm93IG5ldyBFeGNlcHRpb24oXCJub3QgYW4gT2JzZXJ2YWJsZSA6IFwiICsgZXZlbnQpO1xuICAgIH1cbiAgfTtcblxuICBhc3NlcnRGdW5jdGlvbiA9IGZ1bmN0aW9uKGYpIHtcbiAgICByZXR1cm4gYXNzZXJ0KFwibm90IGEgZnVuY3Rpb24gOiBcIiArIGYsIGlzRnVuY3Rpb24oZikpO1xuICB9O1xuXG4gIGlzRnVuY3Rpb24gPSBmdW5jdGlvbihmKSB7XG4gICAgcmV0dXJuIHR5cGVvZiBmID09PSBcImZ1bmN0aW9uXCI7XG4gIH07XG5cbiAgaXNBcnJheSA9IGZ1bmN0aW9uKHhzKSB7XG4gICAgcmV0dXJuIHhzIGluc3RhbmNlb2YgQXJyYXk7XG4gIH07XG5cbiAgaXNPYnNlcnZhYmxlID0gZnVuY3Rpb24oeCkge1xuICAgIHJldHVybiB4IGluc3RhbmNlb2YgT2JzZXJ2YWJsZTtcbiAgfTtcblxuICBhc3NlcnRBcnJheSA9IGZ1bmN0aW9uKHhzKSB7XG4gICAgaWYgKCFpc0FycmF5KHhzKSkge1xuICAgICAgdGhyb3cgbmV3IEV4Y2VwdGlvbihcIm5vdCBhbiBhcnJheSA6IFwiICsgeHMpO1xuICAgIH1cbiAgfTtcblxuICBhc3NlcnROb0FyZ3VtZW50cyA9IGZ1bmN0aW9uKGFyZ3MpIHtcbiAgICByZXR1cm4gYXNzZXJ0KFwibm8gYXJndW1lbnRzIHN1cHBvcnRlZFwiLCBhcmdzLmxlbmd0aCA9PT0gMCk7XG4gIH07XG5cbiAgYXNzZXJ0U3RyaW5nID0gZnVuY3Rpb24oeCkge1xuICAgIGlmICh0eXBlb2YgeCAhPT0gXCJzdHJpbmdcIikge1xuICAgICAgdGhyb3cgbmV3IEV4Y2VwdGlvbihcIm5vdCBhIHN0cmluZyA6IFwiICsgeCk7XG4gICAgfVxuICB9O1xuXG4gIHBhcnRpYWxseUFwcGxpZWQgPSBmdW5jdGlvbihmLCBhcHBsaWVkKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKCkge1xuICAgICAgdmFyIGFyZ3M7XG4gICAgICBhcmdzID0gMSA8PSBhcmd1bWVudHMubGVuZ3RoID8gX19zbGljZS5jYWxsKGFyZ3VtZW50cywgMCkgOiBbXTtcbiAgICAgIHJldHVybiBmLmFwcGx5KG51bGwsIGFwcGxpZWQuY29uY2F0KGFyZ3MpKTtcbiAgICB9O1xuICB9O1xuXG4gIG1ha2VTcGF3bmVyID0gZnVuY3Rpb24oYXJncykge1xuICAgIGlmIChhcmdzLmxlbmd0aCA9PT0gMSAmJiBpc09ic2VydmFibGUoYXJnc1swXSkpIHtcbiAgICAgIHJldHVybiBfLmFsd2F5cyhhcmdzWzBdKTtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIG1ha2VGdW5jdGlvbkFyZ3MoYXJncyk7XG4gICAgfVxuICB9O1xuXG4gIG1ha2VGdW5jdGlvbkFyZ3MgPSBmdW5jdGlvbihhcmdzKSB7XG4gICAgYXJncyA9IEFycmF5LnByb3RvdHlwZS5zbGljZS5jYWxsKGFyZ3MpO1xuICAgIHJldHVybiBtYWtlRnVuY3Rpb25fLmFwcGx5KG51bGwsIGFyZ3MpO1xuICB9O1xuXG4gIG1ha2VGdW5jdGlvbl8gPSB3aXRoTWV0aG9kQ2FsbFN1cHBvcnQoZnVuY3Rpb24oKSB7XG4gICAgdmFyIGFyZ3MsIGY7XG4gICAgZiA9IGFyZ3VtZW50c1swXSwgYXJncyA9IDIgPD0gYXJndW1lbnRzLmxlbmd0aCA/IF9fc2xpY2UuY2FsbChhcmd1bWVudHMsIDEpIDogW107XG4gICAgaWYgKGlzRnVuY3Rpb24oZikpIHtcbiAgICAgIGlmIChhcmdzLmxlbmd0aCkge1xuICAgICAgICByZXR1cm4gcGFydGlhbGx5QXBwbGllZChmLCBhcmdzKTtcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJldHVybiBmO1xuICAgICAgfVxuICAgIH0gZWxzZSBpZiAoaXNGaWVsZEtleShmKSkge1xuICAgICAgcmV0dXJuIHRvRmllbGRFeHRyYWN0b3IoZiwgYXJncyk7XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiBfLmFsd2F5cyhmKTtcbiAgICB9XG4gIH0pO1xuXG4gIG1ha2VGdW5jdGlvbiA9IGZ1bmN0aW9uKGYsIGFyZ3MpIHtcbiAgICByZXR1cm4gbWFrZUZ1bmN0aW9uXy5hcHBseShudWxsLCBbZl0uY29uY2F0KF9fc2xpY2UuY2FsbChhcmdzKSkpO1xuICB9O1xuXG4gIGNvbnN0YW50VG9GdW5jdGlvbiA9IGZ1bmN0aW9uKGYpIHtcbiAgICBpZiAoaXNGdW5jdGlvbihmKSkge1xuICAgICAgcmV0dXJuIGY7XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiBfLmFsd2F5cyhmKTtcbiAgICB9XG4gIH07XG5cbiAgbWFrZU9ic2VydmFibGUgPSBmdW5jdGlvbih4KSB7XG4gICAgaWYgKGlzT2JzZXJ2YWJsZSh4KSkge1xuICAgICAgcmV0dXJuIHg7XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiBCYWNvbi5vbmNlKHgpO1xuICAgIH1cbiAgfTtcblxuICBpc0ZpZWxkS2V5ID0gZnVuY3Rpb24oZikge1xuICAgIHJldHVybiAodHlwZW9mIGYgPT09IFwic3RyaW5nXCIpICYmIGYubGVuZ3RoID4gMSAmJiBmLmNoYXJBdCgwKSA9PT0gXCIuXCI7XG4gIH07XG5cbiAgQmFjb24uaXNGaWVsZEtleSA9IGlzRmllbGRLZXk7XG5cbiAgdG9GaWVsZEV4dHJhY3RvciA9IGZ1bmN0aW9uKGYsIGFyZ3MpIHtcbiAgICB2YXIgcGFydEZ1bmNzLCBwYXJ0cztcbiAgICBwYXJ0cyA9IGYuc2xpY2UoMSkuc3BsaXQoXCIuXCIpO1xuICAgIHBhcnRGdW5jcyA9IF8ubWFwKHRvU2ltcGxlRXh0cmFjdG9yKGFyZ3MpLCBwYXJ0cyk7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgICB2YXIgX2ksIF9sZW47XG4gICAgICBmb3IgKF9pID0gMCwgX2xlbiA9IHBhcnRGdW5jcy5sZW5ndGg7IF9pIDwgX2xlbjsgX2krKykge1xuICAgICAgICBmID0gcGFydEZ1bmNzW19pXTtcbiAgICAgICAgdmFsdWUgPSBmKHZhbHVlKTtcbiAgICAgIH1cbiAgICAgIHJldHVybiB2YWx1ZTtcbiAgICB9O1xuICB9O1xuXG4gIHRvU2ltcGxlRXh0cmFjdG9yID0gZnVuY3Rpb24oYXJncykge1xuICAgIHJldHVybiBmdW5jdGlvbihrZXkpIHtcbiAgICAgIHJldHVybiBmdW5jdGlvbih2YWx1ZSkge1xuICAgICAgICB2YXIgZmllbGRWYWx1ZTtcbiAgICAgICAgaWYgKHZhbHVlID09IG51bGwpIHtcbiAgICAgICAgICByZXR1cm4gdm9pZCAwO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgIGZpZWxkVmFsdWUgPSB2YWx1ZVtrZXldO1xuICAgICAgICAgIGlmIChpc0Z1bmN0aW9uKGZpZWxkVmFsdWUpKSB7XG4gICAgICAgICAgICByZXR1cm4gZmllbGRWYWx1ZS5hcHBseSh2YWx1ZSwgYXJncyk7XG4gICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgIHJldHVybiBmaWVsZFZhbHVlO1xuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfTtcbiAgICB9O1xuICB9O1xuXG4gIHRvRmllbGRLZXkgPSBmdW5jdGlvbihmKSB7XG4gICAgcmV0dXJuIGYuc2xpY2UoMSk7XG4gIH07XG5cbiAgdG9Db21iaW5hdG9yID0gZnVuY3Rpb24oZikge1xuICAgIHZhciBrZXk7XG4gICAgaWYgKGlzRnVuY3Rpb24oZikpIHtcbiAgICAgIHJldHVybiBmO1xuICAgIH0gZWxzZSBpZiAoaXNGaWVsZEtleShmKSkge1xuICAgICAga2V5ID0gdG9GaWVsZEtleShmKTtcbiAgICAgIHJldHVybiBmdW5jdGlvbihsZWZ0LCByaWdodCkge1xuICAgICAgICByZXR1cm4gbGVmdFtrZXldKHJpZ2h0KTtcbiAgICAgIH07XG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiBhc3NlcnQoXCJub3QgYSBmdW5jdGlvbiBvciBhIGZpZWxkIGtleTogXCIgKyBmLCBmYWxzZSk7XG4gICAgfVxuICB9O1xuXG4gIHRvT3B0aW9uID0gZnVuY3Rpb24odikge1xuICAgIGlmICh2IGluc3RhbmNlb2YgU29tZSB8fCB2ID09PSBOb25lKSB7XG4gICAgICByZXR1cm4gdjtcbiAgICB9IGVsc2Uge1xuICAgICAgcmV0dXJuIG5ldyBTb21lKHYpO1xuICAgIH1cbiAgfTtcblxuICBpZiAoKHR5cGVvZiBkZWZpbmUgIT09IFwidW5kZWZpbmVkXCIgJiYgZGVmaW5lICE9PSBudWxsKSAmJiAoZGVmaW5lLmFtZCAhPSBudWxsKSkge1xuICAgIGRlZmluZShbXSwgZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gQmFjb247XG4gICAgfSk7XG4gICAgdGhpcy5CYWNvbiA9IEJhY29uO1xuICB9IGVsc2UgaWYgKCh0eXBlb2YgbW9kdWxlICE9PSBcInVuZGVmaW5lZFwiICYmIG1vZHVsZSAhPT0gbnVsbCkgJiYgKG1vZHVsZS5leHBvcnRzICE9IG51bGwpKSB7XG4gICAgbW9kdWxlLmV4cG9ydHMgPSBCYWNvbjtcbiAgICBCYWNvbi5CYWNvbiA9IEJhY29uO1xuICB9IGVsc2Uge1xuICAgIHRoaXMuQmFjb24gPSBCYWNvbjtcbiAgfVxuXG59KS5jYWxsKHRoaXMpO1xuXG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiLy4uLy4uLy4uL25vZGVfbW9kdWxlcy9iYWNvbmpzL2Rpc3QvQmFjb24uanNcIixcIi8uLi8uLi8uLi9ub2RlX21vZHVsZXMvYmFjb25qcy9kaXN0XCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xuLyohXG4gKiBUaGUgYnVmZmVyIG1vZHVsZSBmcm9tIG5vZGUuanMsIGZvciB0aGUgYnJvd3Nlci5cbiAqXG4gKiBAYXV0aG9yICAgRmVyb3NzIEFib3VraGFkaWplaCA8ZmVyb3NzQGZlcm9zcy5vcmc+IDxodHRwOi8vZmVyb3NzLm9yZz5cbiAqIEBsaWNlbnNlICBNSVRcbiAqL1xuXG52YXIgYmFzZTY0ID0gcmVxdWlyZSgnYmFzZTY0LWpzJylcbnZhciBpZWVlNzU0ID0gcmVxdWlyZSgnaWVlZTc1NCcpXG5cbmV4cG9ydHMuQnVmZmVyID0gQnVmZmVyXG5leHBvcnRzLlNsb3dCdWZmZXIgPSBCdWZmZXJcbmV4cG9ydHMuSU5TUEVDVF9NQVhfQllURVMgPSA1MFxuQnVmZmVyLnBvb2xTaXplID0gODE5MlxuXG4vKipcbiAqIElmIGBCdWZmZXIuX3VzZVR5cGVkQXJyYXlzYDpcbiAqICAgPT09IHRydWUgICAgVXNlIFVpbnQ4QXJyYXkgaW1wbGVtZW50YXRpb24gKGZhc3Rlc3QpXG4gKiAgID09PSBmYWxzZSAgIFVzZSBPYmplY3QgaW1wbGVtZW50YXRpb24gKGNvbXBhdGlibGUgZG93biB0byBJRTYpXG4gKi9cbkJ1ZmZlci5fdXNlVHlwZWRBcnJheXMgPSAoZnVuY3Rpb24gKCkge1xuICAvLyBEZXRlY3QgaWYgYnJvd3NlciBzdXBwb3J0cyBUeXBlZCBBcnJheXMuIFN1cHBvcnRlZCBicm93c2VycyBhcmUgSUUgMTArLCBGaXJlZm94IDQrLFxuICAvLyBDaHJvbWUgNyssIFNhZmFyaSA1LjErLCBPcGVyYSAxMS42KywgaU9TIDQuMisuIElmIHRoZSBicm93c2VyIGRvZXMgbm90IHN1cHBvcnQgYWRkaW5nXG4gIC8vIHByb3BlcnRpZXMgdG8gYFVpbnQ4QXJyYXlgIGluc3RhbmNlcywgdGhlbiB0aGF0J3MgdGhlIHNhbWUgYXMgbm8gYFVpbnQ4QXJyYXlgIHN1cHBvcnRcbiAgLy8gYmVjYXVzZSB3ZSBuZWVkIHRvIGJlIGFibGUgdG8gYWRkIGFsbCB0aGUgbm9kZSBCdWZmZXIgQVBJIG1ldGhvZHMuIFRoaXMgaXMgYW4gaXNzdWVcbiAgLy8gaW4gRmlyZWZveCA0LTI5LiBOb3cgZml4ZWQ6IGh0dHBzOi8vYnVnemlsbGEubW96aWxsYS5vcmcvc2hvd19idWcuY2dpP2lkPTY5NTQzOFxuICB0cnkge1xuICAgIHZhciBidWYgPSBuZXcgQXJyYXlCdWZmZXIoMClcbiAgICB2YXIgYXJyID0gbmV3IFVpbnQ4QXJyYXkoYnVmKVxuICAgIGFyci5mb28gPSBmdW5jdGlvbiAoKSB7IHJldHVybiA0MiB9XG4gICAgcmV0dXJuIDQyID09PSBhcnIuZm9vKCkgJiZcbiAgICAgICAgdHlwZW9mIGFyci5zdWJhcnJheSA9PT0gJ2Z1bmN0aW9uJyAvLyBDaHJvbWUgOS0xMCBsYWNrIGBzdWJhcnJheWBcbiAgfSBjYXRjaCAoZSkge1xuICAgIHJldHVybiBmYWxzZVxuICB9XG59KSgpXG5cbi8qKlxuICogQ2xhc3M6IEJ1ZmZlclxuICogPT09PT09PT09PT09PVxuICpcbiAqIFRoZSBCdWZmZXIgY29uc3RydWN0b3IgcmV0dXJucyBpbnN0YW5jZXMgb2YgYFVpbnQ4QXJyYXlgIHRoYXQgYXJlIGF1Z21lbnRlZFxuICogd2l0aCBmdW5jdGlvbiBwcm9wZXJ0aWVzIGZvciBhbGwgdGhlIG5vZGUgYEJ1ZmZlcmAgQVBJIGZ1bmN0aW9ucy4gV2UgdXNlXG4gKiBgVWludDhBcnJheWAgc28gdGhhdCBzcXVhcmUgYnJhY2tldCBub3RhdGlvbiB3b3JrcyBhcyBleHBlY3RlZCAtLSBpdCByZXR1cm5zXG4gKiBhIHNpbmdsZSBvY3RldC5cbiAqXG4gKiBCeSBhdWdtZW50aW5nIHRoZSBpbnN0YW5jZXMsIHdlIGNhbiBhdm9pZCBtb2RpZnlpbmcgdGhlIGBVaW50OEFycmF5YFxuICogcHJvdG90eXBlLlxuICovXG5mdW5jdGlvbiBCdWZmZXIgKHN1YmplY3QsIGVuY29kaW5nLCBub1plcm8pIHtcbiAgaWYgKCEodGhpcyBpbnN0YW5jZW9mIEJ1ZmZlcikpXG4gICAgcmV0dXJuIG5ldyBCdWZmZXIoc3ViamVjdCwgZW5jb2RpbmcsIG5vWmVybylcblxuICB2YXIgdHlwZSA9IHR5cGVvZiBzdWJqZWN0XG5cbiAgLy8gV29ya2Fyb3VuZDogbm9kZSdzIGJhc2U2NCBpbXBsZW1lbnRhdGlvbiBhbGxvd3MgZm9yIG5vbi1wYWRkZWQgc3RyaW5nc1xuICAvLyB3aGlsZSBiYXNlNjQtanMgZG9lcyBub3QuXG4gIGlmIChlbmNvZGluZyA9PT0gJ2Jhc2U2NCcgJiYgdHlwZSA9PT0gJ3N0cmluZycpIHtcbiAgICBzdWJqZWN0ID0gc3RyaW5ndHJpbShzdWJqZWN0KVxuICAgIHdoaWxlIChzdWJqZWN0Lmxlbmd0aCAlIDQgIT09IDApIHtcbiAgICAgIHN1YmplY3QgPSBzdWJqZWN0ICsgJz0nXG4gICAgfVxuICB9XG5cbiAgLy8gRmluZCB0aGUgbGVuZ3RoXG4gIHZhciBsZW5ndGhcbiAgaWYgKHR5cGUgPT09ICdudW1iZXInKVxuICAgIGxlbmd0aCA9IGNvZXJjZShzdWJqZWN0KVxuICBlbHNlIGlmICh0eXBlID09PSAnc3RyaW5nJylcbiAgICBsZW5ndGggPSBCdWZmZXIuYnl0ZUxlbmd0aChzdWJqZWN0LCBlbmNvZGluZylcbiAgZWxzZSBpZiAodHlwZSA9PT0gJ29iamVjdCcpXG4gICAgbGVuZ3RoID0gY29lcmNlKHN1YmplY3QubGVuZ3RoKSAvLyBhc3N1bWUgdGhhdCBvYmplY3QgaXMgYXJyYXktbGlrZVxuICBlbHNlXG4gICAgdGhyb3cgbmV3IEVycm9yKCdGaXJzdCBhcmd1bWVudCBuZWVkcyB0byBiZSBhIG51bWJlciwgYXJyYXkgb3Igc3RyaW5nLicpXG5cbiAgdmFyIGJ1ZlxuICBpZiAoQnVmZmVyLl91c2VUeXBlZEFycmF5cykge1xuICAgIC8vIFByZWZlcnJlZDogUmV0dXJuIGFuIGF1Z21lbnRlZCBgVWludDhBcnJheWAgaW5zdGFuY2UgZm9yIGJlc3QgcGVyZm9ybWFuY2VcbiAgICBidWYgPSBCdWZmZXIuX2F1Z21lbnQobmV3IFVpbnQ4QXJyYXkobGVuZ3RoKSlcbiAgfSBlbHNlIHtcbiAgICAvLyBGYWxsYmFjazogUmV0dXJuIFRISVMgaW5zdGFuY2Ugb2YgQnVmZmVyIChjcmVhdGVkIGJ5IGBuZXdgKVxuICAgIGJ1ZiA9IHRoaXNcbiAgICBidWYubGVuZ3RoID0gbGVuZ3RoXG4gICAgYnVmLl9pc0J1ZmZlciA9IHRydWVcbiAgfVxuXG4gIHZhciBpXG4gIGlmIChCdWZmZXIuX3VzZVR5cGVkQXJyYXlzICYmIHR5cGVvZiBzdWJqZWN0LmJ5dGVMZW5ndGggPT09ICdudW1iZXInKSB7XG4gICAgLy8gU3BlZWQgb3B0aW1pemF0aW9uIC0tIHVzZSBzZXQgaWYgd2UncmUgY29weWluZyBmcm9tIGEgdHlwZWQgYXJyYXlcbiAgICBidWYuX3NldChzdWJqZWN0KVxuICB9IGVsc2UgaWYgKGlzQXJyYXlpc2goc3ViamVjdCkpIHtcbiAgICAvLyBUcmVhdCBhcnJheS1pc2ggb2JqZWN0cyBhcyBhIGJ5dGUgYXJyYXlcbiAgICBmb3IgKGkgPSAwOyBpIDwgbGVuZ3RoOyBpKyspIHtcbiAgICAgIGlmIChCdWZmZXIuaXNCdWZmZXIoc3ViamVjdCkpXG4gICAgICAgIGJ1ZltpXSA9IHN1YmplY3QucmVhZFVJbnQ4KGkpXG4gICAgICBlbHNlXG4gICAgICAgIGJ1ZltpXSA9IHN1YmplY3RbaV1cbiAgICB9XG4gIH0gZWxzZSBpZiAodHlwZSA9PT0gJ3N0cmluZycpIHtcbiAgICBidWYud3JpdGUoc3ViamVjdCwgMCwgZW5jb2RpbmcpXG4gIH0gZWxzZSBpZiAodHlwZSA9PT0gJ251bWJlcicgJiYgIUJ1ZmZlci5fdXNlVHlwZWRBcnJheXMgJiYgIW5vWmVybykge1xuICAgIGZvciAoaSA9IDA7IGkgPCBsZW5ndGg7IGkrKykge1xuICAgICAgYnVmW2ldID0gMFxuICAgIH1cbiAgfVxuXG4gIHJldHVybiBidWZcbn1cblxuLy8gU1RBVElDIE1FVEhPRFNcbi8vID09PT09PT09PT09PT09XG5cbkJ1ZmZlci5pc0VuY29kaW5nID0gZnVuY3Rpb24gKGVuY29kaW5nKSB7XG4gIHN3aXRjaCAoU3RyaW5nKGVuY29kaW5nKS50b0xvd2VyQ2FzZSgpKSB7XG4gICAgY2FzZSAnaGV4JzpcbiAgICBjYXNlICd1dGY4JzpcbiAgICBjYXNlICd1dGYtOCc6XG4gICAgY2FzZSAnYXNjaWknOlxuICAgIGNhc2UgJ2JpbmFyeSc6XG4gICAgY2FzZSAnYmFzZTY0JzpcbiAgICBjYXNlICdyYXcnOlxuICAgIGNhc2UgJ3VjczInOlxuICAgIGNhc2UgJ3Vjcy0yJzpcbiAgICBjYXNlICd1dGYxNmxlJzpcbiAgICBjYXNlICd1dGYtMTZsZSc6XG4gICAgICByZXR1cm4gdHJ1ZVxuICAgIGRlZmF1bHQ6XG4gICAgICByZXR1cm4gZmFsc2VcbiAgfVxufVxuXG5CdWZmZXIuaXNCdWZmZXIgPSBmdW5jdGlvbiAoYikge1xuICByZXR1cm4gISEoYiAhPT0gbnVsbCAmJiBiICE9PSB1bmRlZmluZWQgJiYgYi5faXNCdWZmZXIpXG59XG5cbkJ1ZmZlci5ieXRlTGVuZ3RoID0gZnVuY3Rpb24gKHN0ciwgZW5jb2RpbmcpIHtcbiAgdmFyIHJldFxuICBzdHIgPSBzdHIgKyAnJ1xuICBzd2l0Y2ggKGVuY29kaW5nIHx8ICd1dGY4Jykge1xuICAgIGNhc2UgJ2hleCc6XG4gICAgICByZXQgPSBzdHIubGVuZ3RoIC8gMlxuICAgICAgYnJlYWtcbiAgICBjYXNlICd1dGY4JzpcbiAgICBjYXNlICd1dGYtOCc6XG4gICAgICByZXQgPSB1dGY4VG9CeXRlcyhzdHIpLmxlbmd0aFxuICAgICAgYnJlYWtcbiAgICBjYXNlICdhc2NpaSc6XG4gICAgY2FzZSAnYmluYXJ5JzpcbiAgICBjYXNlICdyYXcnOlxuICAgICAgcmV0ID0gc3RyLmxlbmd0aFxuICAgICAgYnJlYWtcbiAgICBjYXNlICdiYXNlNjQnOlxuICAgICAgcmV0ID0gYmFzZTY0VG9CeXRlcyhzdHIpLmxlbmd0aFxuICAgICAgYnJlYWtcbiAgICBjYXNlICd1Y3MyJzpcbiAgICBjYXNlICd1Y3MtMic6XG4gICAgY2FzZSAndXRmMTZsZSc6XG4gICAgY2FzZSAndXRmLTE2bGUnOlxuICAgICAgcmV0ID0gc3RyLmxlbmd0aCAqIDJcbiAgICAgIGJyZWFrXG4gICAgZGVmYXVsdDpcbiAgICAgIHRocm93IG5ldyBFcnJvcignVW5rbm93biBlbmNvZGluZycpXG4gIH1cbiAgcmV0dXJuIHJldFxufVxuXG5CdWZmZXIuY29uY2F0ID0gZnVuY3Rpb24gKGxpc3QsIHRvdGFsTGVuZ3RoKSB7XG4gIGFzc2VydChpc0FycmF5KGxpc3QpLCAnVXNhZ2U6IEJ1ZmZlci5jb25jYXQobGlzdCwgW3RvdGFsTGVuZ3RoXSlcXG4nICtcbiAgICAgICdsaXN0IHNob3VsZCBiZSBhbiBBcnJheS4nKVxuXG4gIGlmIChsaXN0Lmxlbmd0aCA9PT0gMCkge1xuICAgIHJldHVybiBuZXcgQnVmZmVyKDApXG4gIH0gZWxzZSBpZiAobGlzdC5sZW5ndGggPT09IDEpIHtcbiAgICByZXR1cm4gbGlzdFswXVxuICB9XG5cbiAgdmFyIGlcbiAgaWYgKHR5cGVvZiB0b3RhbExlbmd0aCAhPT0gJ251bWJlcicpIHtcbiAgICB0b3RhbExlbmd0aCA9IDBcbiAgICBmb3IgKGkgPSAwOyBpIDwgbGlzdC5sZW5ndGg7IGkrKykge1xuICAgICAgdG90YWxMZW5ndGggKz0gbGlzdFtpXS5sZW5ndGhcbiAgICB9XG4gIH1cblxuICB2YXIgYnVmID0gbmV3IEJ1ZmZlcih0b3RhbExlbmd0aClcbiAgdmFyIHBvcyA9IDBcbiAgZm9yIChpID0gMDsgaSA8IGxpc3QubGVuZ3RoOyBpKyspIHtcbiAgICB2YXIgaXRlbSA9IGxpc3RbaV1cbiAgICBpdGVtLmNvcHkoYnVmLCBwb3MpXG4gICAgcG9zICs9IGl0ZW0ubGVuZ3RoXG4gIH1cbiAgcmV0dXJuIGJ1ZlxufVxuXG4vLyBCVUZGRVIgSU5TVEFOQ0UgTUVUSE9EU1xuLy8gPT09PT09PT09PT09PT09PT09PT09PT1cblxuZnVuY3Rpb24gX2hleFdyaXRlIChidWYsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpIHtcbiAgb2Zmc2V0ID0gTnVtYmVyKG9mZnNldCkgfHwgMFxuICB2YXIgcmVtYWluaW5nID0gYnVmLmxlbmd0aCAtIG9mZnNldFxuICBpZiAoIWxlbmd0aCkge1xuICAgIGxlbmd0aCA9IHJlbWFpbmluZ1xuICB9IGVsc2Uge1xuICAgIGxlbmd0aCA9IE51bWJlcihsZW5ndGgpXG4gICAgaWYgKGxlbmd0aCA+IHJlbWFpbmluZykge1xuICAgICAgbGVuZ3RoID0gcmVtYWluaW5nXG4gICAgfVxuICB9XG5cbiAgLy8gbXVzdCBiZSBhbiBldmVuIG51bWJlciBvZiBkaWdpdHNcbiAgdmFyIHN0ckxlbiA9IHN0cmluZy5sZW5ndGhcbiAgYXNzZXJ0KHN0ckxlbiAlIDIgPT09IDAsICdJbnZhbGlkIGhleCBzdHJpbmcnKVxuXG4gIGlmIChsZW5ndGggPiBzdHJMZW4gLyAyKSB7XG4gICAgbGVuZ3RoID0gc3RyTGVuIC8gMlxuICB9XG4gIGZvciAodmFyIGkgPSAwOyBpIDwgbGVuZ3RoOyBpKyspIHtcbiAgICB2YXIgYnl0ZSA9IHBhcnNlSW50KHN0cmluZy5zdWJzdHIoaSAqIDIsIDIpLCAxNilcbiAgICBhc3NlcnQoIWlzTmFOKGJ5dGUpLCAnSW52YWxpZCBoZXggc3RyaW5nJylcbiAgICBidWZbb2Zmc2V0ICsgaV0gPSBieXRlXG4gIH1cbiAgQnVmZmVyLl9jaGFyc1dyaXR0ZW4gPSBpICogMlxuICByZXR1cm4gaVxufVxuXG5mdW5jdGlvbiBfdXRmOFdyaXRlIChidWYsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpIHtcbiAgdmFyIGNoYXJzV3JpdHRlbiA9IEJ1ZmZlci5fY2hhcnNXcml0dGVuID1cbiAgICBibGl0QnVmZmVyKHV0ZjhUb0J5dGVzKHN0cmluZyksIGJ1Ziwgb2Zmc2V0LCBsZW5ndGgpXG4gIHJldHVybiBjaGFyc1dyaXR0ZW5cbn1cblxuZnVuY3Rpb24gX2FzY2lpV3JpdGUgKGJ1Ziwgc3RyaW5nLCBvZmZzZXQsIGxlbmd0aCkge1xuICB2YXIgY2hhcnNXcml0dGVuID0gQnVmZmVyLl9jaGFyc1dyaXR0ZW4gPVxuICAgIGJsaXRCdWZmZXIoYXNjaWlUb0J5dGVzKHN0cmluZyksIGJ1Ziwgb2Zmc2V0LCBsZW5ndGgpXG4gIHJldHVybiBjaGFyc1dyaXR0ZW5cbn1cblxuZnVuY3Rpb24gX2JpbmFyeVdyaXRlIChidWYsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpIHtcbiAgcmV0dXJuIF9hc2NpaVdyaXRlKGJ1Ziwgc3RyaW5nLCBvZmZzZXQsIGxlbmd0aClcbn1cblxuZnVuY3Rpb24gX2Jhc2U2NFdyaXRlIChidWYsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpIHtcbiAgdmFyIGNoYXJzV3JpdHRlbiA9IEJ1ZmZlci5fY2hhcnNXcml0dGVuID1cbiAgICBibGl0QnVmZmVyKGJhc2U2NFRvQnl0ZXMoc3RyaW5nKSwgYnVmLCBvZmZzZXQsIGxlbmd0aClcbiAgcmV0dXJuIGNoYXJzV3JpdHRlblxufVxuXG5mdW5jdGlvbiBfdXRmMTZsZVdyaXRlIChidWYsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpIHtcbiAgdmFyIGNoYXJzV3JpdHRlbiA9IEJ1ZmZlci5fY2hhcnNXcml0dGVuID1cbiAgICBibGl0QnVmZmVyKHV0ZjE2bGVUb0J5dGVzKHN0cmluZyksIGJ1Ziwgb2Zmc2V0LCBsZW5ndGgpXG4gIHJldHVybiBjaGFyc1dyaXR0ZW5cbn1cblxuQnVmZmVyLnByb3RvdHlwZS53cml0ZSA9IGZ1bmN0aW9uIChzdHJpbmcsIG9mZnNldCwgbGVuZ3RoLCBlbmNvZGluZykge1xuICAvLyBTdXBwb3J0IGJvdGggKHN0cmluZywgb2Zmc2V0LCBsZW5ndGgsIGVuY29kaW5nKVxuICAvLyBhbmQgdGhlIGxlZ2FjeSAoc3RyaW5nLCBlbmNvZGluZywgb2Zmc2V0LCBsZW5ndGgpXG4gIGlmIChpc0Zpbml0ZShvZmZzZXQpKSB7XG4gICAgaWYgKCFpc0Zpbml0ZShsZW5ndGgpKSB7XG4gICAgICBlbmNvZGluZyA9IGxlbmd0aFxuICAgICAgbGVuZ3RoID0gdW5kZWZpbmVkXG4gICAgfVxuICB9IGVsc2UgeyAgLy8gbGVnYWN5XG4gICAgdmFyIHN3YXAgPSBlbmNvZGluZ1xuICAgIGVuY29kaW5nID0gb2Zmc2V0XG4gICAgb2Zmc2V0ID0gbGVuZ3RoXG4gICAgbGVuZ3RoID0gc3dhcFxuICB9XG5cbiAgb2Zmc2V0ID0gTnVtYmVyKG9mZnNldCkgfHwgMFxuICB2YXIgcmVtYWluaW5nID0gdGhpcy5sZW5ndGggLSBvZmZzZXRcbiAgaWYgKCFsZW5ndGgpIHtcbiAgICBsZW5ndGggPSByZW1haW5pbmdcbiAgfSBlbHNlIHtcbiAgICBsZW5ndGggPSBOdW1iZXIobGVuZ3RoKVxuICAgIGlmIChsZW5ndGggPiByZW1haW5pbmcpIHtcbiAgICAgIGxlbmd0aCA9IHJlbWFpbmluZ1xuICAgIH1cbiAgfVxuICBlbmNvZGluZyA9IFN0cmluZyhlbmNvZGluZyB8fCAndXRmOCcpLnRvTG93ZXJDYXNlKClcblxuICB2YXIgcmV0XG4gIHN3aXRjaCAoZW5jb2RpbmcpIHtcbiAgICBjYXNlICdoZXgnOlxuICAgICAgcmV0ID0gX2hleFdyaXRlKHRoaXMsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpXG4gICAgICBicmVha1xuICAgIGNhc2UgJ3V0ZjgnOlxuICAgIGNhc2UgJ3V0Zi04JzpcbiAgICAgIHJldCA9IF91dGY4V3JpdGUodGhpcywgc3RyaW5nLCBvZmZzZXQsIGxlbmd0aClcbiAgICAgIGJyZWFrXG4gICAgY2FzZSAnYXNjaWknOlxuICAgICAgcmV0ID0gX2FzY2lpV3JpdGUodGhpcywgc3RyaW5nLCBvZmZzZXQsIGxlbmd0aClcbiAgICAgIGJyZWFrXG4gICAgY2FzZSAnYmluYXJ5JzpcbiAgICAgIHJldCA9IF9iaW5hcnlXcml0ZSh0aGlzLCBzdHJpbmcsIG9mZnNldCwgbGVuZ3RoKVxuICAgICAgYnJlYWtcbiAgICBjYXNlICdiYXNlNjQnOlxuICAgICAgcmV0ID0gX2Jhc2U2NFdyaXRlKHRoaXMsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpXG4gICAgICBicmVha1xuICAgIGNhc2UgJ3VjczInOlxuICAgIGNhc2UgJ3Vjcy0yJzpcbiAgICBjYXNlICd1dGYxNmxlJzpcbiAgICBjYXNlICd1dGYtMTZsZSc6XG4gICAgICByZXQgPSBfdXRmMTZsZVdyaXRlKHRoaXMsIHN0cmluZywgb2Zmc2V0LCBsZW5ndGgpXG4gICAgICBicmVha1xuICAgIGRlZmF1bHQ6XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1Vua25vd24gZW5jb2RpbmcnKVxuICB9XG4gIHJldHVybiByZXRcbn1cblxuQnVmZmVyLnByb3RvdHlwZS50b1N0cmluZyA9IGZ1bmN0aW9uIChlbmNvZGluZywgc3RhcnQsIGVuZCkge1xuICB2YXIgc2VsZiA9IHRoaXNcblxuICBlbmNvZGluZyA9IFN0cmluZyhlbmNvZGluZyB8fCAndXRmOCcpLnRvTG93ZXJDYXNlKClcbiAgc3RhcnQgPSBOdW1iZXIoc3RhcnQpIHx8IDBcbiAgZW5kID0gKGVuZCAhPT0gdW5kZWZpbmVkKVxuICAgID8gTnVtYmVyKGVuZClcbiAgICA6IGVuZCA9IHNlbGYubGVuZ3RoXG5cbiAgLy8gRmFzdHBhdGggZW1wdHkgc3RyaW5nc1xuICBpZiAoZW5kID09PSBzdGFydClcbiAgICByZXR1cm4gJydcblxuICB2YXIgcmV0XG4gIHN3aXRjaCAoZW5jb2RpbmcpIHtcbiAgICBjYXNlICdoZXgnOlxuICAgICAgcmV0ID0gX2hleFNsaWNlKHNlbGYsIHN0YXJ0LCBlbmQpXG4gICAgICBicmVha1xuICAgIGNhc2UgJ3V0ZjgnOlxuICAgIGNhc2UgJ3V0Zi04JzpcbiAgICAgIHJldCA9IF91dGY4U2xpY2Uoc2VsZiwgc3RhcnQsIGVuZClcbiAgICAgIGJyZWFrXG4gICAgY2FzZSAnYXNjaWknOlxuICAgICAgcmV0ID0gX2FzY2lpU2xpY2Uoc2VsZiwgc3RhcnQsIGVuZClcbiAgICAgIGJyZWFrXG4gICAgY2FzZSAnYmluYXJ5JzpcbiAgICAgIHJldCA9IF9iaW5hcnlTbGljZShzZWxmLCBzdGFydCwgZW5kKVxuICAgICAgYnJlYWtcbiAgICBjYXNlICdiYXNlNjQnOlxuICAgICAgcmV0ID0gX2Jhc2U2NFNsaWNlKHNlbGYsIHN0YXJ0LCBlbmQpXG4gICAgICBicmVha1xuICAgIGNhc2UgJ3VjczInOlxuICAgIGNhc2UgJ3Vjcy0yJzpcbiAgICBjYXNlICd1dGYxNmxlJzpcbiAgICBjYXNlICd1dGYtMTZsZSc6XG4gICAgICByZXQgPSBfdXRmMTZsZVNsaWNlKHNlbGYsIHN0YXJ0LCBlbmQpXG4gICAgICBicmVha1xuICAgIGRlZmF1bHQ6XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1Vua25vd24gZW5jb2RpbmcnKVxuICB9XG4gIHJldHVybiByZXRcbn1cblxuQnVmZmVyLnByb3RvdHlwZS50b0pTT04gPSBmdW5jdGlvbiAoKSB7XG4gIHJldHVybiB7XG4gICAgdHlwZTogJ0J1ZmZlcicsXG4gICAgZGF0YTogQXJyYXkucHJvdG90eXBlLnNsaWNlLmNhbGwodGhpcy5fYXJyIHx8IHRoaXMsIDApXG4gIH1cbn1cblxuLy8gY29weSh0YXJnZXRCdWZmZXIsIHRhcmdldFN0YXJ0PTAsIHNvdXJjZVN0YXJ0PTAsIHNvdXJjZUVuZD1idWZmZXIubGVuZ3RoKVxuQnVmZmVyLnByb3RvdHlwZS5jb3B5ID0gZnVuY3Rpb24gKHRhcmdldCwgdGFyZ2V0X3N0YXJ0LCBzdGFydCwgZW5kKSB7XG4gIHZhciBzb3VyY2UgPSB0aGlzXG5cbiAgaWYgKCFzdGFydCkgc3RhcnQgPSAwXG4gIGlmICghZW5kICYmIGVuZCAhPT0gMCkgZW5kID0gdGhpcy5sZW5ndGhcbiAgaWYgKCF0YXJnZXRfc3RhcnQpIHRhcmdldF9zdGFydCA9IDBcblxuICAvLyBDb3B5IDAgYnl0ZXM7IHdlJ3JlIGRvbmVcbiAgaWYgKGVuZCA9PT0gc3RhcnQpIHJldHVyblxuICBpZiAodGFyZ2V0Lmxlbmd0aCA9PT0gMCB8fCBzb3VyY2UubGVuZ3RoID09PSAwKSByZXR1cm5cblxuICAvLyBGYXRhbCBlcnJvciBjb25kaXRpb25zXG4gIGFzc2VydChlbmQgPj0gc3RhcnQsICdzb3VyY2VFbmQgPCBzb3VyY2VTdGFydCcpXG4gIGFzc2VydCh0YXJnZXRfc3RhcnQgPj0gMCAmJiB0YXJnZXRfc3RhcnQgPCB0YXJnZXQubGVuZ3RoLFxuICAgICAgJ3RhcmdldFN0YXJ0IG91dCBvZiBib3VuZHMnKVxuICBhc3NlcnQoc3RhcnQgPj0gMCAmJiBzdGFydCA8IHNvdXJjZS5sZW5ndGgsICdzb3VyY2VTdGFydCBvdXQgb2YgYm91bmRzJylcbiAgYXNzZXJ0KGVuZCA+PSAwICYmIGVuZCA8PSBzb3VyY2UubGVuZ3RoLCAnc291cmNlRW5kIG91dCBvZiBib3VuZHMnKVxuXG4gIC8vIEFyZSB3ZSBvb2I/XG4gIGlmIChlbmQgPiB0aGlzLmxlbmd0aClcbiAgICBlbmQgPSB0aGlzLmxlbmd0aFxuICBpZiAodGFyZ2V0Lmxlbmd0aCAtIHRhcmdldF9zdGFydCA8IGVuZCAtIHN0YXJ0KVxuICAgIGVuZCA9IHRhcmdldC5sZW5ndGggLSB0YXJnZXRfc3RhcnQgKyBzdGFydFxuXG4gIHZhciBsZW4gPSBlbmQgLSBzdGFydFxuXG4gIGlmIChsZW4gPCAxMDAgfHwgIUJ1ZmZlci5fdXNlVHlwZWRBcnJheXMpIHtcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IGxlbjsgaSsrKVxuICAgICAgdGFyZ2V0W2kgKyB0YXJnZXRfc3RhcnRdID0gdGhpc1tpICsgc3RhcnRdXG4gIH0gZWxzZSB7XG4gICAgdGFyZ2V0Ll9zZXQodGhpcy5zdWJhcnJheShzdGFydCwgc3RhcnQgKyBsZW4pLCB0YXJnZXRfc3RhcnQpXG4gIH1cbn1cblxuZnVuY3Rpb24gX2Jhc2U2NFNsaWNlIChidWYsIHN0YXJ0LCBlbmQpIHtcbiAgaWYgKHN0YXJ0ID09PSAwICYmIGVuZCA9PT0gYnVmLmxlbmd0aCkge1xuICAgIHJldHVybiBiYXNlNjQuZnJvbUJ5dGVBcnJheShidWYpXG4gIH0gZWxzZSB7XG4gICAgcmV0dXJuIGJhc2U2NC5mcm9tQnl0ZUFycmF5KGJ1Zi5zbGljZShzdGFydCwgZW5kKSlcbiAgfVxufVxuXG5mdW5jdGlvbiBfdXRmOFNsaWNlIChidWYsIHN0YXJ0LCBlbmQpIHtcbiAgdmFyIHJlcyA9ICcnXG4gIHZhciB0bXAgPSAnJ1xuICBlbmQgPSBNYXRoLm1pbihidWYubGVuZ3RoLCBlbmQpXG5cbiAgZm9yICh2YXIgaSA9IHN0YXJ0OyBpIDwgZW5kOyBpKyspIHtcbiAgICBpZiAoYnVmW2ldIDw9IDB4N0YpIHtcbiAgICAgIHJlcyArPSBkZWNvZGVVdGY4Q2hhcih0bXApICsgU3RyaW5nLmZyb21DaGFyQ29kZShidWZbaV0pXG4gICAgICB0bXAgPSAnJ1xuICAgIH0gZWxzZSB7XG4gICAgICB0bXAgKz0gJyUnICsgYnVmW2ldLnRvU3RyaW5nKDE2KVxuICAgIH1cbiAgfVxuXG4gIHJldHVybiByZXMgKyBkZWNvZGVVdGY4Q2hhcih0bXApXG59XG5cbmZ1bmN0aW9uIF9hc2NpaVNsaWNlIChidWYsIHN0YXJ0LCBlbmQpIHtcbiAgdmFyIHJldCA9ICcnXG4gIGVuZCA9IE1hdGgubWluKGJ1Zi5sZW5ndGgsIGVuZClcblxuICBmb3IgKHZhciBpID0gc3RhcnQ7IGkgPCBlbmQ7IGkrKylcbiAgICByZXQgKz0gU3RyaW5nLmZyb21DaGFyQ29kZShidWZbaV0pXG4gIHJldHVybiByZXRcbn1cblxuZnVuY3Rpb24gX2JpbmFyeVNsaWNlIChidWYsIHN0YXJ0LCBlbmQpIHtcbiAgcmV0dXJuIF9hc2NpaVNsaWNlKGJ1Ziwgc3RhcnQsIGVuZClcbn1cblxuZnVuY3Rpb24gX2hleFNsaWNlIChidWYsIHN0YXJ0LCBlbmQpIHtcbiAgdmFyIGxlbiA9IGJ1Zi5sZW5ndGhcblxuICBpZiAoIXN0YXJ0IHx8IHN0YXJ0IDwgMCkgc3RhcnQgPSAwXG4gIGlmICghZW5kIHx8IGVuZCA8IDAgfHwgZW5kID4gbGVuKSBlbmQgPSBsZW5cblxuICB2YXIgb3V0ID0gJydcbiAgZm9yICh2YXIgaSA9IHN0YXJ0OyBpIDwgZW5kOyBpKyspIHtcbiAgICBvdXQgKz0gdG9IZXgoYnVmW2ldKVxuICB9XG4gIHJldHVybiBvdXRcbn1cblxuZnVuY3Rpb24gX3V0ZjE2bGVTbGljZSAoYnVmLCBzdGFydCwgZW5kKSB7XG4gIHZhciBieXRlcyA9IGJ1Zi5zbGljZShzdGFydCwgZW5kKVxuICB2YXIgcmVzID0gJydcbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBieXRlcy5sZW5ndGg7IGkgKz0gMikge1xuICAgIHJlcyArPSBTdHJpbmcuZnJvbUNoYXJDb2RlKGJ5dGVzW2ldICsgYnl0ZXNbaSsxXSAqIDI1NilcbiAgfVxuICByZXR1cm4gcmVzXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUuc2xpY2UgPSBmdW5jdGlvbiAoc3RhcnQsIGVuZCkge1xuICB2YXIgbGVuID0gdGhpcy5sZW5ndGhcbiAgc3RhcnQgPSBjbGFtcChzdGFydCwgbGVuLCAwKVxuICBlbmQgPSBjbGFtcChlbmQsIGxlbiwgbGVuKVxuXG4gIGlmIChCdWZmZXIuX3VzZVR5cGVkQXJyYXlzKSB7XG4gICAgcmV0dXJuIEJ1ZmZlci5fYXVnbWVudCh0aGlzLnN1YmFycmF5KHN0YXJ0LCBlbmQpKVxuICB9IGVsc2Uge1xuICAgIHZhciBzbGljZUxlbiA9IGVuZCAtIHN0YXJ0XG4gICAgdmFyIG5ld0J1ZiA9IG5ldyBCdWZmZXIoc2xpY2VMZW4sIHVuZGVmaW5lZCwgdHJ1ZSlcbiAgICBmb3IgKHZhciBpID0gMDsgaSA8IHNsaWNlTGVuOyBpKyspIHtcbiAgICAgIG5ld0J1ZltpXSA9IHRoaXNbaSArIHN0YXJ0XVxuICAgIH1cbiAgICByZXR1cm4gbmV3QnVmXG4gIH1cbn1cblxuLy8gYGdldGAgd2lsbCBiZSByZW1vdmVkIGluIE5vZGUgMC4xMytcbkJ1ZmZlci5wcm90b3R5cGUuZ2V0ID0gZnVuY3Rpb24gKG9mZnNldCkge1xuICBjb25zb2xlLmxvZygnLmdldCgpIGlzIGRlcHJlY2F0ZWQuIEFjY2VzcyB1c2luZyBhcnJheSBpbmRleGVzIGluc3RlYWQuJylcbiAgcmV0dXJuIHRoaXMucmVhZFVJbnQ4KG9mZnNldClcbn1cblxuLy8gYHNldGAgd2lsbCBiZSByZW1vdmVkIGluIE5vZGUgMC4xMytcbkJ1ZmZlci5wcm90b3R5cGUuc2V0ID0gZnVuY3Rpb24gKHYsIG9mZnNldCkge1xuICBjb25zb2xlLmxvZygnLnNldCgpIGlzIGRlcHJlY2F0ZWQuIEFjY2VzcyB1c2luZyBhcnJheSBpbmRleGVzIGluc3RlYWQuJylcbiAgcmV0dXJuIHRoaXMud3JpdGVVSW50OCh2LCBvZmZzZXQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZFVJbnQ4ID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCA8IHRoaXMubGVuZ3RoLCAnVHJ5aW5nIHRvIHJlYWQgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICB9XG5cbiAgaWYgKG9mZnNldCA+PSB0aGlzLmxlbmd0aClcbiAgICByZXR1cm5cblxuICByZXR1cm4gdGhpc1tvZmZzZXRdXG59XG5cbmZ1bmN0aW9uIF9yZWFkVUludDE2IChidWYsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCBub0Fzc2VydCkge1xuICBpZiAoIW5vQXNzZXJ0KSB7XG4gICAgYXNzZXJ0KHR5cGVvZiBsaXR0bGVFbmRpYW4gPT09ICdib29sZWFuJywgJ21pc3Npbmcgb3IgaW52YWxpZCBlbmRpYW4nKVxuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCArIDEgPCBidWYubGVuZ3RoLCAnVHJ5aW5nIHRvIHJlYWQgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICB9XG5cbiAgdmFyIGxlbiA9IGJ1Zi5sZW5ndGhcbiAgaWYgKG9mZnNldCA+PSBsZW4pXG4gICAgcmV0dXJuXG5cbiAgdmFyIHZhbFxuICBpZiAobGl0dGxlRW5kaWFuKSB7XG4gICAgdmFsID0gYnVmW29mZnNldF1cbiAgICBpZiAob2Zmc2V0ICsgMSA8IGxlbilcbiAgICAgIHZhbCB8PSBidWZbb2Zmc2V0ICsgMV0gPDwgOFxuICB9IGVsc2Uge1xuICAgIHZhbCA9IGJ1ZltvZmZzZXRdIDw8IDhcbiAgICBpZiAob2Zmc2V0ICsgMSA8IGxlbilcbiAgICAgIHZhbCB8PSBidWZbb2Zmc2V0ICsgMV1cbiAgfVxuICByZXR1cm4gdmFsXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZFVJbnQxNkxFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkVUludDE2KHRoaXMsIG9mZnNldCwgdHJ1ZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZFVJbnQxNkJFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkVUludDE2KHRoaXMsIG9mZnNldCwgZmFsc2UsIG5vQXNzZXJ0KVxufVxuXG5mdW5jdGlvbiBfcmVhZFVJbnQzMiAoYnVmLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydCh0eXBlb2YgbGl0dGxlRW5kaWFuID09PSAnYm9vbGVhbicsICdtaXNzaW5nIG9yIGludmFsaWQgZW5kaWFuJylcbiAgICBhc3NlcnQob2Zmc2V0ICE9PSB1bmRlZmluZWQgJiYgb2Zmc2V0ICE9PSBudWxsLCAnbWlzc2luZyBvZmZzZXQnKVxuICAgIGFzc2VydChvZmZzZXQgKyAzIDwgYnVmLmxlbmd0aCwgJ1RyeWluZyB0byByZWFkIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgfVxuXG4gIHZhciBsZW4gPSBidWYubGVuZ3RoXG4gIGlmIChvZmZzZXQgPj0gbGVuKVxuICAgIHJldHVyblxuXG4gIHZhciB2YWxcbiAgaWYgKGxpdHRsZUVuZGlhbikge1xuICAgIGlmIChvZmZzZXQgKyAyIDwgbGVuKVxuICAgICAgdmFsID0gYnVmW29mZnNldCArIDJdIDw8IDE2XG4gICAgaWYgKG9mZnNldCArIDEgPCBsZW4pXG4gICAgICB2YWwgfD0gYnVmW29mZnNldCArIDFdIDw8IDhcbiAgICB2YWwgfD0gYnVmW29mZnNldF1cbiAgICBpZiAob2Zmc2V0ICsgMyA8IGxlbilcbiAgICAgIHZhbCA9IHZhbCArIChidWZbb2Zmc2V0ICsgM10gPDwgMjQgPj4+IDApXG4gIH0gZWxzZSB7XG4gICAgaWYgKG9mZnNldCArIDEgPCBsZW4pXG4gICAgICB2YWwgPSBidWZbb2Zmc2V0ICsgMV0gPDwgMTZcbiAgICBpZiAob2Zmc2V0ICsgMiA8IGxlbilcbiAgICAgIHZhbCB8PSBidWZbb2Zmc2V0ICsgMl0gPDwgOFxuICAgIGlmIChvZmZzZXQgKyAzIDwgbGVuKVxuICAgICAgdmFsIHw9IGJ1ZltvZmZzZXQgKyAzXVxuICAgIHZhbCA9IHZhbCArIChidWZbb2Zmc2V0XSA8PCAyNCA+Pj4gMClcbiAgfVxuICByZXR1cm4gdmFsXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZFVJbnQzMkxFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkVUludDMyKHRoaXMsIG9mZnNldCwgdHJ1ZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZFVJbnQzMkJFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkVUludDMyKHRoaXMsIG9mZnNldCwgZmFsc2UsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLnJlYWRJbnQ4ID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsXG4gICAgICAgICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCA8IHRoaXMubGVuZ3RoLCAnVHJ5aW5nIHRvIHJlYWQgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICB9XG5cbiAgaWYgKG9mZnNldCA+PSB0aGlzLmxlbmd0aClcbiAgICByZXR1cm5cblxuICB2YXIgbmVnID0gdGhpc1tvZmZzZXRdICYgMHg4MFxuICBpZiAobmVnKVxuICAgIHJldHVybiAoMHhmZiAtIHRoaXNbb2Zmc2V0XSArIDEpICogLTFcbiAgZWxzZVxuICAgIHJldHVybiB0aGlzW29mZnNldF1cbn1cblxuZnVuY3Rpb24gX3JlYWRJbnQxNiAoYnVmLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydCh0eXBlb2YgbGl0dGxlRW5kaWFuID09PSAnYm9vbGVhbicsICdtaXNzaW5nIG9yIGludmFsaWQgZW5kaWFuJylcbiAgICBhc3NlcnQob2Zmc2V0ICE9PSB1bmRlZmluZWQgJiYgb2Zmc2V0ICE9PSBudWxsLCAnbWlzc2luZyBvZmZzZXQnKVxuICAgIGFzc2VydChvZmZzZXQgKyAxIDwgYnVmLmxlbmd0aCwgJ1RyeWluZyB0byByZWFkIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgfVxuXG4gIHZhciBsZW4gPSBidWYubGVuZ3RoXG4gIGlmIChvZmZzZXQgPj0gbGVuKVxuICAgIHJldHVyblxuXG4gIHZhciB2YWwgPSBfcmVhZFVJbnQxNihidWYsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCB0cnVlKVxuICB2YXIgbmVnID0gdmFsICYgMHg4MDAwXG4gIGlmIChuZWcpXG4gICAgcmV0dXJuICgweGZmZmYgLSB2YWwgKyAxKSAqIC0xXG4gIGVsc2VcbiAgICByZXR1cm4gdmFsXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZEludDE2TEUgPSBmdW5jdGlvbiAob2Zmc2V0LCBub0Fzc2VydCkge1xuICByZXR1cm4gX3JlYWRJbnQxNih0aGlzLCBvZmZzZXQsIHRydWUsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLnJlYWRJbnQxNkJFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkSW50MTYodGhpcywgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbmZ1bmN0aW9uIF9yZWFkSW50MzIgKGJ1Ziwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodHlwZW9mIGxpdHRsZUVuZGlhbiA9PT0gJ2Jvb2xlYW4nLCAnbWlzc2luZyBvciBpbnZhbGlkIGVuZGlhbicpXG4gICAgYXNzZXJ0KG9mZnNldCAhPT0gdW5kZWZpbmVkICYmIG9mZnNldCAhPT0gbnVsbCwgJ21pc3Npbmcgb2Zmc2V0JylcbiAgICBhc3NlcnQob2Zmc2V0ICsgMyA8IGJ1Zi5sZW5ndGgsICdUcnlpbmcgdG8gcmVhZCBiZXlvbmQgYnVmZmVyIGxlbmd0aCcpXG4gIH1cblxuICB2YXIgbGVuID0gYnVmLmxlbmd0aFxuICBpZiAob2Zmc2V0ID49IGxlbilcbiAgICByZXR1cm5cblxuICB2YXIgdmFsID0gX3JlYWRVSW50MzIoYnVmLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgdHJ1ZSlcbiAgdmFyIG5lZyA9IHZhbCAmIDB4ODAwMDAwMDBcbiAgaWYgKG5lZylcbiAgICByZXR1cm4gKDB4ZmZmZmZmZmYgLSB2YWwgKyAxKSAqIC0xXG4gIGVsc2VcbiAgICByZXR1cm4gdmFsXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZEludDMyTEUgPSBmdW5jdGlvbiAob2Zmc2V0LCBub0Fzc2VydCkge1xuICByZXR1cm4gX3JlYWRJbnQzMih0aGlzLCBvZmZzZXQsIHRydWUsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLnJlYWRJbnQzMkJFID0gZnVuY3Rpb24gKG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgcmV0dXJuIF9yZWFkSW50MzIodGhpcywgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbmZ1bmN0aW9uIF9yZWFkRmxvYXQgKGJ1Ziwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodHlwZW9mIGxpdHRsZUVuZGlhbiA9PT0gJ2Jvb2xlYW4nLCAnbWlzc2luZyBvciBpbnZhbGlkIGVuZGlhbicpXG4gICAgYXNzZXJ0KG9mZnNldCArIDMgPCBidWYubGVuZ3RoLCAnVHJ5aW5nIHRvIHJlYWQgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICB9XG5cbiAgcmV0dXJuIGllZWU3NTQucmVhZChidWYsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCAyMywgNClcbn1cblxuQnVmZmVyLnByb3RvdHlwZS5yZWFkRmxvYXRMRSA9IGZ1bmN0aW9uIChvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIHJldHVybiBfcmVhZEZsb2F0KHRoaXMsIG9mZnNldCwgdHJ1ZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUucmVhZEZsb2F0QkUgPSBmdW5jdGlvbiAob2Zmc2V0LCBub0Fzc2VydCkge1xuICByZXR1cm4gX3JlYWRGbG9hdCh0aGlzLCBvZmZzZXQsIGZhbHNlLCBub0Fzc2VydClcbn1cblxuZnVuY3Rpb24gX3JlYWREb3VibGUgKGJ1Ziwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodHlwZW9mIGxpdHRsZUVuZGlhbiA9PT0gJ2Jvb2xlYW4nLCAnbWlzc2luZyBvciBpbnZhbGlkIGVuZGlhbicpXG4gICAgYXNzZXJ0KG9mZnNldCArIDcgPCBidWYubGVuZ3RoLCAnVHJ5aW5nIHRvIHJlYWQgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICB9XG5cbiAgcmV0dXJuIGllZWU3NTQucmVhZChidWYsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCA1MiwgOClcbn1cblxuQnVmZmVyLnByb3RvdHlwZS5yZWFkRG91YmxlTEUgPSBmdW5jdGlvbiAob2Zmc2V0LCBub0Fzc2VydCkge1xuICByZXR1cm4gX3JlYWREb3VibGUodGhpcywgb2Zmc2V0LCB0cnVlLCBub0Fzc2VydClcbn1cblxuQnVmZmVyLnByb3RvdHlwZS5yZWFkRG91YmxlQkUgPSBmdW5jdGlvbiAob2Zmc2V0LCBub0Fzc2VydCkge1xuICByZXR1cm4gX3JlYWREb3VibGUodGhpcywgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVVSW50OCA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBpZiAoIW5vQXNzZXJ0KSB7XG4gICAgYXNzZXJ0KHZhbHVlICE9PSB1bmRlZmluZWQgJiYgdmFsdWUgIT09IG51bGwsICdtaXNzaW5nIHZhbHVlJylcbiAgICBhc3NlcnQob2Zmc2V0ICE9PSB1bmRlZmluZWQgJiYgb2Zmc2V0ICE9PSBudWxsLCAnbWlzc2luZyBvZmZzZXQnKVxuICAgIGFzc2VydChvZmZzZXQgPCB0aGlzLmxlbmd0aCwgJ3RyeWluZyB0byB3cml0ZSBiZXlvbmQgYnVmZmVyIGxlbmd0aCcpXG4gICAgdmVyaWZ1aW50KHZhbHVlLCAweGZmKVxuICB9XG5cbiAgaWYgKG9mZnNldCA+PSB0aGlzLmxlbmd0aCkgcmV0dXJuXG5cbiAgdGhpc1tvZmZzZXRdID0gdmFsdWVcbn1cblxuZnVuY3Rpb24gX3dyaXRlVUludDE2IChidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydCh2YWx1ZSAhPT0gdW5kZWZpbmVkICYmIHZhbHVlICE9PSBudWxsLCAnbWlzc2luZyB2YWx1ZScpXG4gICAgYXNzZXJ0KHR5cGVvZiBsaXR0bGVFbmRpYW4gPT09ICdib29sZWFuJywgJ21pc3Npbmcgb3IgaW52YWxpZCBlbmRpYW4nKVxuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCArIDEgPCBidWYubGVuZ3RoLCAndHJ5aW5nIHRvIHdyaXRlIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgICB2ZXJpZnVpbnQodmFsdWUsIDB4ZmZmZilcbiAgfVxuXG4gIHZhciBsZW4gPSBidWYubGVuZ3RoXG4gIGlmIChvZmZzZXQgPj0gbGVuKVxuICAgIHJldHVyblxuXG4gIGZvciAodmFyIGkgPSAwLCBqID0gTWF0aC5taW4obGVuIC0gb2Zmc2V0LCAyKTsgaSA8IGo7IGkrKykge1xuICAgIGJ1ZltvZmZzZXQgKyBpXSA9XG4gICAgICAgICh2YWx1ZSAmICgweGZmIDw8ICg4ICogKGxpdHRsZUVuZGlhbiA/IGkgOiAxIC0gaSkpKSkgPj4+XG4gICAgICAgICAgICAobGl0dGxlRW5kaWFuID8gaSA6IDEgLSBpKSAqIDhcbiAgfVxufVxuXG5CdWZmZXIucHJvdG90eXBlLndyaXRlVUludDE2TEUgPSBmdW5jdGlvbiAodmFsdWUsIG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgX3dyaXRlVUludDE2KHRoaXMsIHZhbHVlLCBvZmZzZXQsIHRydWUsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLndyaXRlVUludDE2QkUgPSBmdW5jdGlvbiAodmFsdWUsIG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgX3dyaXRlVUludDE2KHRoaXMsIHZhbHVlLCBvZmZzZXQsIGZhbHNlLCBub0Fzc2VydClcbn1cblxuZnVuY3Rpb24gX3dyaXRlVUludDMyIChidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydCh2YWx1ZSAhPT0gdW5kZWZpbmVkICYmIHZhbHVlICE9PSBudWxsLCAnbWlzc2luZyB2YWx1ZScpXG4gICAgYXNzZXJ0KHR5cGVvZiBsaXR0bGVFbmRpYW4gPT09ICdib29sZWFuJywgJ21pc3Npbmcgb3IgaW52YWxpZCBlbmRpYW4nKVxuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCArIDMgPCBidWYubGVuZ3RoLCAndHJ5aW5nIHRvIHdyaXRlIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgICB2ZXJpZnVpbnQodmFsdWUsIDB4ZmZmZmZmZmYpXG4gIH1cblxuICB2YXIgbGVuID0gYnVmLmxlbmd0aFxuICBpZiAob2Zmc2V0ID49IGxlbilcbiAgICByZXR1cm5cblxuICBmb3IgKHZhciBpID0gMCwgaiA9IE1hdGgubWluKGxlbiAtIG9mZnNldCwgNCk7IGkgPCBqOyBpKyspIHtcbiAgICBidWZbb2Zmc2V0ICsgaV0gPVxuICAgICAgICAodmFsdWUgPj4+IChsaXR0bGVFbmRpYW4gPyBpIDogMyAtIGkpICogOCkgJiAweGZmXG4gIH1cbn1cblxuQnVmZmVyLnByb3RvdHlwZS53cml0ZVVJbnQzMkxFID0gZnVuY3Rpb24gKHZhbHVlLCBvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIF93cml0ZVVJbnQzMih0aGlzLCB2YWx1ZSwgb2Zmc2V0LCB0cnVlLCBub0Fzc2VydClcbn1cblxuQnVmZmVyLnByb3RvdHlwZS53cml0ZVVJbnQzMkJFID0gZnVuY3Rpb24gKHZhbHVlLCBvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIF93cml0ZVVJbnQzMih0aGlzLCB2YWx1ZSwgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVJbnQ4ID0gZnVuY3Rpb24gKHZhbHVlLCBvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodmFsdWUgIT09IHVuZGVmaW5lZCAmJiB2YWx1ZSAhPT0gbnVsbCwgJ21pc3NpbmcgdmFsdWUnKVxuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCA8IHRoaXMubGVuZ3RoLCAnVHJ5aW5nIHRvIHdyaXRlIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgICB2ZXJpZnNpbnQodmFsdWUsIDB4N2YsIC0weDgwKVxuICB9XG5cbiAgaWYgKG9mZnNldCA+PSB0aGlzLmxlbmd0aClcbiAgICByZXR1cm5cblxuICBpZiAodmFsdWUgPj0gMClcbiAgICB0aGlzLndyaXRlVUludDgodmFsdWUsIG9mZnNldCwgbm9Bc3NlcnQpXG4gIGVsc2VcbiAgICB0aGlzLndyaXRlVUludDgoMHhmZiArIHZhbHVlICsgMSwgb2Zmc2V0LCBub0Fzc2VydClcbn1cblxuZnVuY3Rpb24gX3dyaXRlSW50MTYgKGJ1ZiwgdmFsdWUsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCBub0Fzc2VydCkge1xuICBpZiAoIW5vQXNzZXJ0KSB7XG4gICAgYXNzZXJ0KHZhbHVlICE9PSB1bmRlZmluZWQgJiYgdmFsdWUgIT09IG51bGwsICdtaXNzaW5nIHZhbHVlJylcbiAgICBhc3NlcnQodHlwZW9mIGxpdHRsZUVuZGlhbiA9PT0gJ2Jvb2xlYW4nLCAnbWlzc2luZyBvciBpbnZhbGlkIGVuZGlhbicpXG4gICAgYXNzZXJ0KG9mZnNldCAhPT0gdW5kZWZpbmVkICYmIG9mZnNldCAhPT0gbnVsbCwgJ21pc3Npbmcgb2Zmc2V0JylcbiAgICBhc3NlcnQob2Zmc2V0ICsgMSA8IGJ1Zi5sZW5ndGgsICdUcnlpbmcgdG8gd3JpdGUgYmV5b25kIGJ1ZmZlciBsZW5ndGgnKVxuICAgIHZlcmlmc2ludCh2YWx1ZSwgMHg3ZmZmLCAtMHg4MDAwKVxuICB9XG5cbiAgdmFyIGxlbiA9IGJ1Zi5sZW5ndGhcbiAgaWYgKG9mZnNldCA+PSBsZW4pXG4gICAgcmV0dXJuXG5cbiAgaWYgKHZhbHVlID49IDApXG4gICAgX3dyaXRlVUludDE2KGJ1ZiwgdmFsdWUsIG9mZnNldCwgbGl0dGxlRW5kaWFuLCBub0Fzc2VydClcbiAgZWxzZVxuICAgIF93cml0ZVVJbnQxNihidWYsIDB4ZmZmZiArIHZhbHVlICsgMSwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLndyaXRlSW50MTZMRSA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBfd3JpdGVJbnQxNih0aGlzLCB2YWx1ZSwgb2Zmc2V0LCB0cnVlLCBub0Fzc2VydClcbn1cblxuQnVmZmVyLnByb3RvdHlwZS53cml0ZUludDE2QkUgPSBmdW5jdGlvbiAodmFsdWUsIG9mZnNldCwgbm9Bc3NlcnQpIHtcbiAgX3dyaXRlSW50MTYodGhpcywgdmFsdWUsIG9mZnNldCwgZmFsc2UsIG5vQXNzZXJ0KVxufVxuXG5mdW5jdGlvbiBfd3JpdGVJbnQzMiAoYnVmLCB2YWx1ZSwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodmFsdWUgIT09IHVuZGVmaW5lZCAmJiB2YWx1ZSAhPT0gbnVsbCwgJ21pc3NpbmcgdmFsdWUnKVxuICAgIGFzc2VydCh0eXBlb2YgbGl0dGxlRW5kaWFuID09PSAnYm9vbGVhbicsICdtaXNzaW5nIG9yIGludmFsaWQgZW5kaWFuJylcbiAgICBhc3NlcnQob2Zmc2V0ICE9PSB1bmRlZmluZWQgJiYgb2Zmc2V0ICE9PSBudWxsLCAnbWlzc2luZyBvZmZzZXQnKVxuICAgIGFzc2VydChvZmZzZXQgKyAzIDwgYnVmLmxlbmd0aCwgJ1RyeWluZyB0byB3cml0ZSBiZXlvbmQgYnVmZmVyIGxlbmd0aCcpXG4gICAgdmVyaWZzaW50KHZhbHVlLCAweDdmZmZmZmZmLCAtMHg4MDAwMDAwMClcbiAgfVxuXG4gIHZhciBsZW4gPSBidWYubGVuZ3RoXG4gIGlmIChvZmZzZXQgPj0gbGVuKVxuICAgIHJldHVyblxuXG4gIGlmICh2YWx1ZSA+PSAwKVxuICAgIF93cml0ZVVJbnQzMihidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpXG4gIGVsc2VcbiAgICBfd3JpdGVVSW50MzIoYnVmLCAweGZmZmZmZmZmICsgdmFsdWUgKyAxLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVJbnQzMkxFID0gZnVuY3Rpb24gKHZhbHVlLCBvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIF93cml0ZUludDMyKHRoaXMsIHZhbHVlLCBvZmZzZXQsIHRydWUsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLndyaXRlSW50MzJCRSA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBfd3JpdGVJbnQzMih0aGlzLCB2YWx1ZSwgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbmZ1bmN0aW9uIF93cml0ZUZsb2F0IChidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgbm9Bc3NlcnQpIHtcbiAgaWYgKCFub0Fzc2VydCkge1xuICAgIGFzc2VydCh2YWx1ZSAhPT0gdW5kZWZpbmVkICYmIHZhbHVlICE9PSBudWxsLCAnbWlzc2luZyB2YWx1ZScpXG4gICAgYXNzZXJ0KHR5cGVvZiBsaXR0bGVFbmRpYW4gPT09ICdib29sZWFuJywgJ21pc3Npbmcgb3IgaW52YWxpZCBlbmRpYW4nKVxuICAgIGFzc2VydChvZmZzZXQgIT09IHVuZGVmaW5lZCAmJiBvZmZzZXQgIT09IG51bGwsICdtaXNzaW5nIG9mZnNldCcpXG4gICAgYXNzZXJ0KG9mZnNldCArIDMgPCBidWYubGVuZ3RoLCAnVHJ5aW5nIHRvIHdyaXRlIGJleW9uZCBidWZmZXIgbGVuZ3RoJylcbiAgICB2ZXJpZklFRUU3NTQodmFsdWUsIDMuNDAyODIzNDY2Mzg1Mjg4NmUrMzgsIC0zLjQwMjgyMzQ2NjM4NTI4ODZlKzM4KVxuICB9XG5cbiAgdmFyIGxlbiA9IGJ1Zi5sZW5ndGhcbiAgaWYgKG9mZnNldCA+PSBsZW4pXG4gICAgcmV0dXJuXG5cbiAgaWVlZTc1NC53cml0ZShidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgMjMsIDQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVGbG9hdExFID0gZnVuY3Rpb24gKHZhbHVlLCBvZmZzZXQsIG5vQXNzZXJ0KSB7XG4gIF93cml0ZUZsb2F0KHRoaXMsIHZhbHVlLCBvZmZzZXQsIHRydWUsIG5vQXNzZXJ0KVxufVxuXG5CdWZmZXIucHJvdG90eXBlLndyaXRlRmxvYXRCRSA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBfd3JpdGVGbG9hdCh0aGlzLCB2YWx1ZSwgb2Zmc2V0LCBmYWxzZSwgbm9Bc3NlcnQpXG59XG5cbmZ1bmN0aW9uIF93cml0ZURvdWJsZSAoYnVmLCB2YWx1ZSwgb2Zmc2V0LCBsaXR0bGVFbmRpYW4sIG5vQXNzZXJ0KSB7XG4gIGlmICghbm9Bc3NlcnQpIHtcbiAgICBhc3NlcnQodmFsdWUgIT09IHVuZGVmaW5lZCAmJiB2YWx1ZSAhPT0gbnVsbCwgJ21pc3NpbmcgdmFsdWUnKVxuICAgIGFzc2VydCh0eXBlb2YgbGl0dGxlRW5kaWFuID09PSAnYm9vbGVhbicsICdtaXNzaW5nIG9yIGludmFsaWQgZW5kaWFuJylcbiAgICBhc3NlcnQob2Zmc2V0ICE9PSB1bmRlZmluZWQgJiYgb2Zmc2V0ICE9PSBudWxsLCAnbWlzc2luZyBvZmZzZXQnKVxuICAgIGFzc2VydChvZmZzZXQgKyA3IDwgYnVmLmxlbmd0aCxcbiAgICAgICAgJ1RyeWluZyB0byB3cml0ZSBiZXlvbmQgYnVmZmVyIGxlbmd0aCcpXG4gICAgdmVyaWZJRUVFNzU0KHZhbHVlLCAxLjc5NzY5MzEzNDg2MjMxNTdFKzMwOCwgLTEuNzk3NjkzMTM0ODYyMzE1N0UrMzA4KVxuICB9XG5cbiAgdmFyIGxlbiA9IGJ1Zi5sZW5ndGhcbiAgaWYgKG9mZnNldCA+PSBsZW4pXG4gICAgcmV0dXJuXG5cbiAgaWVlZTc1NC53cml0ZShidWYsIHZhbHVlLCBvZmZzZXQsIGxpdHRsZUVuZGlhbiwgNTIsIDgpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVEb3VibGVMRSA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBfd3JpdGVEb3VibGUodGhpcywgdmFsdWUsIG9mZnNldCwgdHJ1ZSwgbm9Bc3NlcnQpXG59XG5cbkJ1ZmZlci5wcm90b3R5cGUud3JpdGVEb3VibGVCRSA9IGZ1bmN0aW9uICh2YWx1ZSwgb2Zmc2V0LCBub0Fzc2VydCkge1xuICBfd3JpdGVEb3VibGUodGhpcywgdmFsdWUsIG9mZnNldCwgZmFsc2UsIG5vQXNzZXJ0KVxufVxuXG4vLyBmaWxsKHZhbHVlLCBzdGFydD0wLCBlbmQ9YnVmZmVyLmxlbmd0aClcbkJ1ZmZlci5wcm90b3R5cGUuZmlsbCA9IGZ1bmN0aW9uICh2YWx1ZSwgc3RhcnQsIGVuZCkge1xuICBpZiAoIXZhbHVlKSB2YWx1ZSA9IDBcbiAgaWYgKCFzdGFydCkgc3RhcnQgPSAwXG4gIGlmICghZW5kKSBlbmQgPSB0aGlzLmxlbmd0aFxuXG4gIGlmICh0eXBlb2YgdmFsdWUgPT09ICdzdHJpbmcnKSB7XG4gICAgdmFsdWUgPSB2YWx1ZS5jaGFyQ29kZUF0KDApXG4gIH1cblxuICBhc3NlcnQodHlwZW9mIHZhbHVlID09PSAnbnVtYmVyJyAmJiAhaXNOYU4odmFsdWUpLCAndmFsdWUgaXMgbm90IGEgbnVtYmVyJylcbiAgYXNzZXJ0KGVuZCA+PSBzdGFydCwgJ2VuZCA8IHN0YXJ0JylcblxuICAvLyBGaWxsIDAgYnl0ZXM7IHdlJ3JlIGRvbmVcbiAgaWYgKGVuZCA9PT0gc3RhcnQpIHJldHVyblxuICBpZiAodGhpcy5sZW5ndGggPT09IDApIHJldHVyblxuXG4gIGFzc2VydChzdGFydCA+PSAwICYmIHN0YXJ0IDwgdGhpcy5sZW5ndGgsICdzdGFydCBvdXQgb2YgYm91bmRzJylcbiAgYXNzZXJ0KGVuZCA+PSAwICYmIGVuZCA8PSB0aGlzLmxlbmd0aCwgJ2VuZCBvdXQgb2YgYm91bmRzJylcblxuICBmb3IgKHZhciBpID0gc3RhcnQ7IGkgPCBlbmQ7IGkrKykge1xuICAgIHRoaXNbaV0gPSB2YWx1ZVxuICB9XG59XG5cbkJ1ZmZlci5wcm90b3R5cGUuaW5zcGVjdCA9IGZ1bmN0aW9uICgpIHtcbiAgdmFyIG91dCA9IFtdXG4gIHZhciBsZW4gPSB0aGlzLmxlbmd0aFxuICBmb3IgKHZhciBpID0gMDsgaSA8IGxlbjsgaSsrKSB7XG4gICAgb3V0W2ldID0gdG9IZXgodGhpc1tpXSlcbiAgICBpZiAoaSA9PT0gZXhwb3J0cy5JTlNQRUNUX01BWF9CWVRFUykge1xuICAgICAgb3V0W2kgKyAxXSA9ICcuLi4nXG4gICAgICBicmVha1xuICAgIH1cbiAgfVxuICByZXR1cm4gJzxCdWZmZXIgJyArIG91dC5qb2luKCcgJykgKyAnPidcbn1cblxuLyoqXG4gKiBDcmVhdGVzIGEgbmV3IGBBcnJheUJ1ZmZlcmAgd2l0aCB0aGUgKmNvcGllZCogbWVtb3J5IG9mIHRoZSBidWZmZXIgaW5zdGFuY2UuXG4gKiBBZGRlZCBpbiBOb2RlIDAuMTIuIE9ubHkgYXZhaWxhYmxlIGluIGJyb3dzZXJzIHRoYXQgc3VwcG9ydCBBcnJheUJ1ZmZlci5cbiAqL1xuQnVmZmVyLnByb3RvdHlwZS50b0FycmF5QnVmZmVyID0gZnVuY3Rpb24gKCkge1xuICBpZiAodHlwZW9mIFVpbnQ4QXJyYXkgIT09ICd1bmRlZmluZWQnKSB7XG4gICAgaWYgKEJ1ZmZlci5fdXNlVHlwZWRBcnJheXMpIHtcbiAgICAgIHJldHVybiAobmV3IEJ1ZmZlcih0aGlzKSkuYnVmZmVyXG4gICAgfSBlbHNlIHtcbiAgICAgIHZhciBidWYgPSBuZXcgVWludDhBcnJheSh0aGlzLmxlbmd0aClcbiAgICAgIGZvciAodmFyIGkgPSAwLCBsZW4gPSBidWYubGVuZ3RoOyBpIDwgbGVuOyBpICs9IDEpXG4gICAgICAgIGJ1ZltpXSA9IHRoaXNbaV1cbiAgICAgIHJldHVybiBidWYuYnVmZmVyXG4gICAgfVxuICB9IGVsc2Uge1xuICAgIHRocm93IG5ldyBFcnJvcignQnVmZmVyLnRvQXJyYXlCdWZmZXIgbm90IHN1cHBvcnRlZCBpbiB0aGlzIGJyb3dzZXInKVxuICB9XG59XG5cbi8vIEhFTFBFUiBGVU5DVElPTlNcbi8vID09PT09PT09PT09PT09PT1cblxuZnVuY3Rpb24gc3RyaW5ndHJpbSAoc3RyKSB7XG4gIGlmIChzdHIudHJpbSkgcmV0dXJuIHN0ci50cmltKClcbiAgcmV0dXJuIHN0ci5yZXBsYWNlKC9eXFxzK3xcXHMrJC9nLCAnJylcbn1cblxudmFyIEJQID0gQnVmZmVyLnByb3RvdHlwZVxuXG4vKipcbiAqIEF1Z21lbnQgYSBVaW50OEFycmF5ICppbnN0YW5jZSogKG5vdCB0aGUgVWludDhBcnJheSBjbGFzcyEpIHdpdGggQnVmZmVyIG1ldGhvZHNcbiAqL1xuQnVmZmVyLl9hdWdtZW50ID0gZnVuY3Rpb24gKGFycikge1xuICBhcnIuX2lzQnVmZmVyID0gdHJ1ZVxuXG4gIC8vIHNhdmUgcmVmZXJlbmNlIHRvIG9yaWdpbmFsIFVpbnQ4QXJyYXkgZ2V0L3NldCBtZXRob2RzIGJlZm9yZSBvdmVyd3JpdGluZ1xuICBhcnIuX2dldCA9IGFyci5nZXRcbiAgYXJyLl9zZXQgPSBhcnIuc2V0XG5cbiAgLy8gZGVwcmVjYXRlZCwgd2lsbCBiZSByZW1vdmVkIGluIG5vZGUgMC4xMytcbiAgYXJyLmdldCA9IEJQLmdldFxuICBhcnIuc2V0ID0gQlAuc2V0XG5cbiAgYXJyLndyaXRlID0gQlAud3JpdGVcbiAgYXJyLnRvU3RyaW5nID0gQlAudG9TdHJpbmdcbiAgYXJyLnRvTG9jYWxlU3RyaW5nID0gQlAudG9TdHJpbmdcbiAgYXJyLnRvSlNPTiA9IEJQLnRvSlNPTlxuICBhcnIuY29weSA9IEJQLmNvcHlcbiAgYXJyLnNsaWNlID0gQlAuc2xpY2VcbiAgYXJyLnJlYWRVSW50OCA9IEJQLnJlYWRVSW50OFxuICBhcnIucmVhZFVJbnQxNkxFID0gQlAucmVhZFVJbnQxNkxFXG4gIGFyci5yZWFkVUludDE2QkUgPSBCUC5yZWFkVUludDE2QkVcbiAgYXJyLnJlYWRVSW50MzJMRSA9IEJQLnJlYWRVSW50MzJMRVxuICBhcnIucmVhZFVJbnQzMkJFID0gQlAucmVhZFVJbnQzMkJFXG4gIGFyci5yZWFkSW50OCA9IEJQLnJlYWRJbnQ4XG4gIGFyci5yZWFkSW50MTZMRSA9IEJQLnJlYWRJbnQxNkxFXG4gIGFyci5yZWFkSW50MTZCRSA9IEJQLnJlYWRJbnQxNkJFXG4gIGFyci5yZWFkSW50MzJMRSA9IEJQLnJlYWRJbnQzMkxFXG4gIGFyci5yZWFkSW50MzJCRSA9IEJQLnJlYWRJbnQzMkJFXG4gIGFyci5yZWFkRmxvYXRMRSA9IEJQLnJlYWRGbG9hdExFXG4gIGFyci5yZWFkRmxvYXRCRSA9IEJQLnJlYWRGbG9hdEJFXG4gIGFyci5yZWFkRG91YmxlTEUgPSBCUC5yZWFkRG91YmxlTEVcbiAgYXJyLnJlYWREb3VibGVCRSA9IEJQLnJlYWREb3VibGVCRVxuICBhcnIud3JpdGVVSW50OCA9IEJQLndyaXRlVUludDhcbiAgYXJyLndyaXRlVUludDE2TEUgPSBCUC53cml0ZVVJbnQxNkxFXG4gIGFyci53cml0ZVVJbnQxNkJFID0gQlAud3JpdGVVSW50MTZCRVxuICBhcnIud3JpdGVVSW50MzJMRSA9IEJQLndyaXRlVUludDMyTEVcbiAgYXJyLndyaXRlVUludDMyQkUgPSBCUC53cml0ZVVJbnQzMkJFXG4gIGFyci53cml0ZUludDggPSBCUC53cml0ZUludDhcbiAgYXJyLndyaXRlSW50MTZMRSA9IEJQLndyaXRlSW50MTZMRVxuICBhcnIud3JpdGVJbnQxNkJFID0gQlAud3JpdGVJbnQxNkJFXG4gIGFyci53cml0ZUludDMyTEUgPSBCUC53cml0ZUludDMyTEVcbiAgYXJyLndyaXRlSW50MzJCRSA9IEJQLndyaXRlSW50MzJCRVxuICBhcnIud3JpdGVGbG9hdExFID0gQlAud3JpdGVGbG9hdExFXG4gIGFyci53cml0ZUZsb2F0QkUgPSBCUC53cml0ZUZsb2F0QkVcbiAgYXJyLndyaXRlRG91YmxlTEUgPSBCUC53cml0ZURvdWJsZUxFXG4gIGFyci53cml0ZURvdWJsZUJFID0gQlAud3JpdGVEb3VibGVCRVxuICBhcnIuZmlsbCA9IEJQLmZpbGxcbiAgYXJyLmluc3BlY3QgPSBCUC5pbnNwZWN0XG4gIGFyci50b0FycmF5QnVmZmVyID0gQlAudG9BcnJheUJ1ZmZlclxuXG4gIHJldHVybiBhcnJcbn1cblxuLy8gc2xpY2Uoc3RhcnQsIGVuZClcbmZ1bmN0aW9uIGNsYW1wIChpbmRleCwgbGVuLCBkZWZhdWx0VmFsdWUpIHtcbiAgaWYgKHR5cGVvZiBpbmRleCAhPT0gJ251bWJlcicpIHJldHVybiBkZWZhdWx0VmFsdWVcbiAgaW5kZXggPSB+fmluZGV4OyAgLy8gQ29lcmNlIHRvIGludGVnZXIuXG4gIGlmIChpbmRleCA+PSBsZW4pIHJldHVybiBsZW5cbiAgaWYgKGluZGV4ID49IDApIHJldHVybiBpbmRleFxuICBpbmRleCArPSBsZW5cbiAgaWYgKGluZGV4ID49IDApIHJldHVybiBpbmRleFxuICByZXR1cm4gMFxufVxuXG5mdW5jdGlvbiBjb2VyY2UgKGxlbmd0aCkge1xuICAvLyBDb2VyY2UgbGVuZ3RoIHRvIGEgbnVtYmVyIChwb3NzaWJseSBOYU4pLCByb3VuZCB1cFxuICAvLyBpbiBjYXNlIGl0J3MgZnJhY3Rpb25hbCAoZS5nLiAxMjMuNDU2KSB0aGVuIGRvIGFcbiAgLy8gZG91YmxlIG5lZ2F0ZSB0byBjb2VyY2UgYSBOYU4gdG8gMC4gRWFzeSwgcmlnaHQ/XG4gIGxlbmd0aCA9IH5+TWF0aC5jZWlsKCtsZW5ndGgpXG4gIHJldHVybiBsZW5ndGggPCAwID8gMCA6IGxlbmd0aFxufVxuXG5mdW5jdGlvbiBpc0FycmF5IChzdWJqZWN0KSB7XG4gIHJldHVybiAoQXJyYXkuaXNBcnJheSB8fCBmdW5jdGlvbiAoc3ViamVjdCkge1xuICAgIHJldHVybiBPYmplY3QucHJvdG90eXBlLnRvU3RyaW5nLmNhbGwoc3ViamVjdCkgPT09ICdbb2JqZWN0IEFycmF5XSdcbiAgfSkoc3ViamVjdClcbn1cblxuZnVuY3Rpb24gaXNBcnJheWlzaCAoc3ViamVjdCkge1xuICByZXR1cm4gaXNBcnJheShzdWJqZWN0KSB8fCBCdWZmZXIuaXNCdWZmZXIoc3ViamVjdCkgfHxcbiAgICAgIHN1YmplY3QgJiYgdHlwZW9mIHN1YmplY3QgPT09ICdvYmplY3QnICYmXG4gICAgICB0eXBlb2Ygc3ViamVjdC5sZW5ndGggPT09ICdudW1iZXInXG59XG5cbmZ1bmN0aW9uIHRvSGV4IChuKSB7XG4gIGlmIChuIDwgMTYpIHJldHVybiAnMCcgKyBuLnRvU3RyaW5nKDE2KVxuICByZXR1cm4gbi50b1N0cmluZygxNilcbn1cblxuZnVuY3Rpb24gdXRmOFRvQnl0ZXMgKHN0cikge1xuICB2YXIgYnl0ZUFycmF5ID0gW11cbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBzdHIubGVuZ3RoOyBpKyspIHtcbiAgICB2YXIgYiA9IHN0ci5jaGFyQ29kZUF0KGkpXG4gICAgaWYgKGIgPD0gMHg3RilcbiAgICAgIGJ5dGVBcnJheS5wdXNoKHN0ci5jaGFyQ29kZUF0KGkpKVxuICAgIGVsc2Uge1xuICAgICAgdmFyIHN0YXJ0ID0gaVxuICAgICAgaWYgKGIgPj0gMHhEODAwICYmIGIgPD0gMHhERkZGKSBpKytcbiAgICAgIHZhciBoID0gZW5jb2RlVVJJQ29tcG9uZW50KHN0ci5zbGljZShzdGFydCwgaSsxKSkuc3Vic3RyKDEpLnNwbGl0KCclJylcbiAgICAgIGZvciAodmFyIGogPSAwOyBqIDwgaC5sZW5ndGg7IGorKylcbiAgICAgICAgYnl0ZUFycmF5LnB1c2gocGFyc2VJbnQoaFtqXSwgMTYpKVxuICAgIH1cbiAgfVxuICByZXR1cm4gYnl0ZUFycmF5XG59XG5cbmZ1bmN0aW9uIGFzY2lpVG9CeXRlcyAoc3RyKSB7XG4gIHZhciBieXRlQXJyYXkgPSBbXVxuICBmb3IgKHZhciBpID0gMDsgaSA8IHN0ci5sZW5ndGg7IGkrKykge1xuICAgIC8vIE5vZGUncyBjb2RlIHNlZW1zIHRvIGJlIGRvaW5nIHRoaXMgYW5kIG5vdCAmIDB4N0YuLlxuICAgIGJ5dGVBcnJheS5wdXNoKHN0ci5jaGFyQ29kZUF0KGkpICYgMHhGRilcbiAgfVxuICByZXR1cm4gYnl0ZUFycmF5XG59XG5cbmZ1bmN0aW9uIHV0ZjE2bGVUb0J5dGVzIChzdHIpIHtcbiAgdmFyIGMsIGhpLCBsb1xuICB2YXIgYnl0ZUFycmF5ID0gW11cbiAgZm9yICh2YXIgaSA9IDA7IGkgPCBzdHIubGVuZ3RoOyBpKyspIHtcbiAgICBjID0gc3RyLmNoYXJDb2RlQXQoaSlcbiAgICBoaSA9IGMgPj4gOFxuICAgIGxvID0gYyAlIDI1NlxuICAgIGJ5dGVBcnJheS5wdXNoKGxvKVxuICAgIGJ5dGVBcnJheS5wdXNoKGhpKVxuICB9XG5cbiAgcmV0dXJuIGJ5dGVBcnJheVxufVxuXG5mdW5jdGlvbiBiYXNlNjRUb0J5dGVzIChzdHIpIHtcbiAgcmV0dXJuIGJhc2U2NC50b0J5dGVBcnJheShzdHIpXG59XG5cbmZ1bmN0aW9uIGJsaXRCdWZmZXIgKHNyYywgZHN0LCBvZmZzZXQsIGxlbmd0aCkge1xuICB2YXIgcG9zXG4gIGZvciAodmFyIGkgPSAwOyBpIDwgbGVuZ3RoOyBpKyspIHtcbiAgICBpZiAoKGkgKyBvZmZzZXQgPj0gZHN0Lmxlbmd0aCkgfHwgKGkgPj0gc3JjLmxlbmd0aCkpXG4gICAgICBicmVha1xuICAgIGRzdFtpICsgb2Zmc2V0XSA9IHNyY1tpXVxuICB9XG4gIHJldHVybiBpXG59XG5cbmZ1bmN0aW9uIGRlY29kZVV0ZjhDaGFyIChzdHIpIHtcbiAgdHJ5IHtcbiAgICByZXR1cm4gZGVjb2RlVVJJQ29tcG9uZW50KHN0cilcbiAgfSBjYXRjaCAoZXJyKSB7XG4gICAgcmV0dXJuIFN0cmluZy5mcm9tQ2hhckNvZGUoMHhGRkZEKSAvLyBVVEYgOCBpbnZhbGlkIGNoYXJcbiAgfVxufVxuXG4vKlxuICogV2UgaGF2ZSB0byBtYWtlIHN1cmUgdGhhdCB0aGUgdmFsdWUgaXMgYSB2YWxpZCBpbnRlZ2VyLiBUaGlzIG1lYW5zIHRoYXQgaXRcbiAqIGlzIG5vbi1uZWdhdGl2ZS4gSXQgaGFzIG5vIGZyYWN0aW9uYWwgY29tcG9uZW50IGFuZCB0aGF0IGl0IGRvZXMgbm90XG4gKiBleGNlZWQgdGhlIG1heGltdW0gYWxsb3dlZCB2YWx1ZS5cbiAqL1xuZnVuY3Rpb24gdmVyaWZ1aW50ICh2YWx1ZSwgbWF4KSB7XG4gIGFzc2VydCh0eXBlb2YgdmFsdWUgPT09ICdudW1iZXInLCAnY2Fubm90IHdyaXRlIGEgbm9uLW51bWJlciBhcyBhIG51bWJlcicpXG4gIGFzc2VydCh2YWx1ZSA+PSAwLCAnc3BlY2lmaWVkIGEgbmVnYXRpdmUgdmFsdWUgZm9yIHdyaXRpbmcgYW4gdW5zaWduZWQgdmFsdWUnKVxuICBhc3NlcnQodmFsdWUgPD0gbWF4LCAndmFsdWUgaXMgbGFyZ2VyIHRoYW4gbWF4aW11bSB2YWx1ZSBmb3IgdHlwZScpXG4gIGFzc2VydChNYXRoLmZsb29yKHZhbHVlKSA9PT0gdmFsdWUsICd2YWx1ZSBoYXMgYSBmcmFjdGlvbmFsIGNvbXBvbmVudCcpXG59XG5cbmZ1bmN0aW9uIHZlcmlmc2ludCAodmFsdWUsIG1heCwgbWluKSB7XG4gIGFzc2VydCh0eXBlb2YgdmFsdWUgPT09ICdudW1iZXInLCAnY2Fubm90IHdyaXRlIGEgbm9uLW51bWJlciBhcyBhIG51bWJlcicpXG4gIGFzc2VydCh2YWx1ZSA8PSBtYXgsICd2YWx1ZSBsYXJnZXIgdGhhbiBtYXhpbXVtIGFsbG93ZWQgdmFsdWUnKVxuICBhc3NlcnQodmFsdWUgPj0gbWluLCAndmFsdWUgc21hbGxlciB0aGFuIG1pbmltdW0gYWxsb3dlZCB2YWx1ZScpXG4gIGFzc2VydChNYXRoLmZsb29yKHZhbHVlKSA9PT0gdmFsdWUsICd2YWx1ZSBoYXMgYSBmcmFjdGlvbmFsIGNvbXBvbmVudCcpXG59XG5cbmZ1bmN0aW9uIHZlcmlmSUVFRTc1NCAodmFsdWUsIG1heCwgbWluKSB7XG4gIGFzc2VydCh0eXBlb2YgdmFsdWUgPT09ICdudW1iZXInLCAnY2Fubm90IHdyaXRlIGEgbm9uLW51bWJlciBhcyBhIG51bWJlcicpXG4gIGFzc2VydCh2YWx1ZSA8PSBtYXgsICd2YWx1ZSBsYXJnZXIgdGhhbiBtYXhpbXVtIGFsbG93ZWQgdmFsdWUnKVxuICBhc3NlcnQodmFsdWUgPj0gbWluLCAndmFsdWUgc21hbGxlciB0aGFuIG1pbmltdW0gYWxsb3dlZCB2YWx1ZScpXG59XG5cbmZ1bmN0aW9uIGFzc2VydCAodGVzdCwgbWVzc2FnZSkge1xuICBpZiAoIXRlc3QpIHRocm93IG5ldyBFcnJvcihtZXNzYWdlIHx8ICdGYWlsZWQgYXNzZXJ0aW9uJylcbn1cblxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi8uLi8uLi8uLi9ub2RlX21vZHVsZXMvZ3VscC1icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9idWZmZXIvaW5kZXguanNcIixcIi8uLi8uLi8uLi9ub2RlX21vZHVsZXMvZ3VscC1icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9idWZmZXJcIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG52YXIgbG9va3VwID0gJ0FCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFlaYWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXowMTIzNDU2Nzg5Ky8nO1xuXG47KGZ1bmN0aW9uIChleHBvcnRzKSB7XG5cdCd1c2Ugc3RyaWN0JztcblxuICB2YXIgQXJyID0gKHR5cGVvZiBVaW50OEFycmF5ICE9PSAndW5kZWZpbmVkJylcbiAgICA/IFVpbnQ4QXJyYXlcbiAgICA6IEFycmF5XG5cblx0dmFyIFBMVVMgICA9ICcrJy5jaGFyQ29kZUF0KDApXG5cdHZhciBTTEFTSCAgPSAnLycuY2hhckNvZGVBdCgwKVxuXHR2YXIgTlVNQkVSID0gJzAnLmNoYXJDb2RlQXQoMClcblx0dmFyIExPV0VSICA9ICdhJy5jaGFyQ29kZUF0KDApXG5cdHZhciBVUFBFUiAgPSAnQScuY2hhckNvZGVBdCgwKVxuXHR2YXIgUExVU19VUkxfU0FGRSA9ICctJy5jaGFyQ29kZUF0KDApXG5cdHZhciBTTEFTSF9VUkxfU0FGRSA9ICdfJy5jaGFyQ29kZUF0KDApXG5cblx0ZnVuY3Rpb24gZGVjb2RlIChlbHQpIHtcblx0XHR2YXIgY29kZSA9IGVsdC5jaGFyQ29kZUF0KDApXG5cdFx0aWYgKGNvZGUgPT09IFBMVVMgfHxcblx0XHQgICAgY29kZSA9PT0gUExVU19VUkxfU0FGRSlcblx0XHRcdHJldHVybiA2MiAvLyAnKydcblx0XHRpZiAoY29kZSA9PT0gU0xBU0ggfHxcblx0XHQgICAgY29kZSA9PT0gU0xBU0hfVVJMX1NBRkUpXG5cdFx0XHRyZXR1cm4gNjMgLy8gJy8nXG5cdFx0aWYgKGNvZGUgPCBOVU1CRVIpXG5cdFx0XHRyZXR1cm4gLTEgLy9ubyBtYXRjaFxuXHRcdGlmIChjb2RlIDwgTlVNQkVSICsgMTApXG5cdFx0XHRyZXR1cm4gY29kZSAtIE5VTUJFUiArIDI2ICsgMjZcblx0XHRpZiAoY29kZSA8IFVQUEVSICsgMjYpXG5cdFx0XHRyZXR1cm4gY29kZSAtIFVQUEVSXG5cdFx0aWYgKGNvZGUgPCBMT1dFUiArIDI2KVxuXHRcdFx0cmV0dXJuIGNvZGUgLSBMT1dFUiArIDI2XG5cdH1cblxuXHRmdW5jdGlvbiBiNjRUb0J5dGVBcnJheSAoYjY0KSB7XG5cdFx0dmFyIGksIGosIGwsIHRtcCwgcGxhY2VIb2xkZXJzLCBhcnJcblxuXHRcdGlmIChiNjQubGVuZ3RoICUgNCA+IDApIHtcblx0XHRcdHRocm93IG5ldyBFcnJvcignSW52YWxpZCBzdHJpbmcuIExlbmd0aCBtdXN0IGJlIGEgbXVsdGlwbGUgb2YgNCcpXG5cdFx0fVxuXG5cdFx0Ly8gdGhlIG51bWJlciBvZiBlcXVhbCBzaWducyAocGxhY2UgaG9sZGVycylcblx0XHQvLyBpZiB0aGVyZSBhcmUgdHdvIHBsYWNlaG9sZGVycywgdGhhbiB0aGUgdHdvIGNoYXJhY3RlcnMgYmVmb3JlIGl0XG5cdFx0Ly8gcmVwcmVzZW50IG9uZSBieXRlXG5cdFx0Ly8gaWYgdGhlcmUgaXMgb25seSBvbmUsIHRoZW4gdGhlIHRocmVlIGNoYXJhY3RlcnMgYmVmb3JlIGl0IHJlcHJlc2VudCAyIGJ5dGVzXG5cdFx0Ly8gdGhpcyBpcyBqdXN0IGEgY2hlYXAgaGFjayB0byBub3QgZG8gaW5kZXhPZiB0d2ljZVxuXHRcdHZhciBsZW4gPSBiNjQubGVuZ3RoXG5cdFx0cGxhY2VIb2xkZXJzID0gJz0nID09PSBiNjQuY2hhckF0KGxlbiAtIDIpID8gMiA6ICc9JyA9PT0gYjY0LmNoYXJBdChsZW4gLSAxKSA/IDEgOiAwXG5cblx0XHQvLyBiYXNlNjQgaXMgNC8zICsgdXAgdG8gdHdvIGNoYXJhY3RlcnMgb2YgdGhlIG9yaWdpbmFsIGRhdGFcblx0XHRhcnIgPSBuZXcgQXJyKGI2NC5sZW5ndGggKiAzIC8gNCAtIHBsYWNlSG9sZGVycylcblxuXHRcdC8vIGlmIHRoZXJlIGFyZSBwbGFjZWhvbGRlcnMsIG9ubHkgZ2V0IHVwIHRvIHRoZSBsYXN0IGNvbXBsZXRlIDQgY2hhcnNcblx0XHRsID0gcGxhY2VIb2xkZXJzID4gMCA/IGI2NC5sZW5ndGggLSA0IDogYjY0Lmxlbmd0aFxuXG5cdFx0dmFyIEwgPSAwXG5cblx0XHRmdW5jdGlvbiBwdXNoICh2KSB7XG5cdFx0XHRhcnJbTCsrXSA9IHZcblx0XHR9XG5cblx0XHRmb3IgKGkgPSAwLCBqID0gMDsgaSA8IGw7IGkgKz0gNCwgaiArPSAzKSB7XG5cdFx0XHR0bXAgPSAoZGVjb2RlKGI2NC5jaGFyQXQoaSkpIDw8IDE4KSB8IChkZWNvZGUoYjY0LmNoYXJBdChpICsgMSkpIDw8IDEyKSB8IChkZWNvZGUoYjY0LmNoYXJBdChpICsgMikpIDw8IDYpIHwgZGVjb2RlKGI2NC5jaGFyQXQoaSArIDMpKVxuXHRcdFx0cHVzaCgodG1wICYgMHhGRjAwMDApID4+IDE2KVxuXHRcdFx0cHVzaCgodG1wICYgMHhGRjAwKSA+PiA4KVxuXHRcdFx0cHVzaCh0bXAgJiAweEZGKVxuXHRcdH1cblxuXHRcdGlmIChwbGFjZUhvbGRlcnMgPT09IDIpIHtcblx0XHRcdHRtcCA9IChkZWNvZGUoYjY0LmNoYXJBdChpKSkgPDwgMikgfCAoZGVjb2RlKGI2NC5jaGFyQXQoaSArIDEpKSA+PiA0KVxuXHRcdFx0cHVzaCh0bXAgJiAweEZGKVxuXHRcdH0gZWxzZSBpZiAocGxhY2VIb2xkZXJzID09PSAxKSB7XG5cdFx0XHR0bXAgPSAoZGVjb2RlKGI2NC5jaGFyQXQoaSkpIDw8IDEwKSB8IChkZWNvZGUoYjY0LmNoYXJBdChpICsgMSkpIDw8IDQpIHwgKGRlY29kZShiNjQuY2hhckF0KGkgKyAyKSkgPj4gMilcblx0XHRcdHB1c2goKHRtcCA+PiA4KSAmIDB4RkYpXG5cdFx0XHRwdXNoKHRtcCAmIDB4RkYpXG5cdFx0fVxuXG5cdFx0cmV0dXJuIGFyclxuXHR9XG5cblx0ZnVuY3Rpb24gdWludDhUb0Jhc2U2NCAodWludDgpIHtcblx0XHR2YXIgaSxcblx0XHRcdGV4dHJhQnl0ZXMgPSB1aW50OC5sZW5ndGggJSAzLCAvLyBpZiB3ZSBoYXZlIDEgYnl0ZSBsZWZ0LCBwYWQgMiBieXRlc1xuXHRcdFx0b3V0cHV0ID0gXCJcIixcblx0XHRcdHRlbXAsIGxlbmd0aFxuXG5cdFx0ZnVuY3Rpb24gZW5jb2RlIChudW0pIHtcblx0XHRcdHJldHVybiBsb29rdXAuY2hhckF0KG51bSlcblx0XHR9XG5cblx0XHRmdW5jdGlvbiB0cmlwbGV0VG9CYXNlNjQgKG51bSkge1xuXHRcdFx0cmV0dXJuIGVuY29kZShudW0gPj4gMTggJiAweDNGKSArIGVuY29kZShudW0gPj4gMTIgJiAweDNGKSArIGVuY29kZShudW0gPj4gNiAmIDB4M0YpICsgZW5jb2RlKG51bSAmIDB4M0YpXG5cdFx0fVxuXG5cdFx0Ly8gZ28gdGhyb3VnaCB0aGUgYXJyYXkgZXZlcnkgdGhyZWUgYnl0ZXMsIHdlJ2xsIGRlYWwgd2l0aCB0cmFpbGluZyBzdHVmZiBsYXRlclxuXHRcdGZvciAoaSA9IDAsIGxlbmd0aCA9IHVpbnQ4Lmxlbmd0aCAtIGV4dHJhQnl0ZXM7IGkgPCBsZW5ndGg7IGkgKz0gMykge1xuXHRcdFx0dGVtcCA9ICh1aW50OFtpXSA8PCAxNikgKyAodWludDhbaSArIDFdIDw8IDgpICsgKHVpbnQ4W2kgKyAyXSlcblx0XHRcdG91dHB1dCArPSB0cmlwbGV0VG9CYXNlNjQodGVtcClcblx0XHR9XG5cblx0XHQvLyBwYWQgdGhlIGVuZCB3aXRoIHplcm9zLCBidXQgbWFrZSBzdXJlIHRvIG5vdCBmb3JnZXQgdGhlIGV4dHJhIGJ5dGVzXG5cdFx0c3dpdGNoIChleHRyYUJ5dGVzKSB7XG5cdFx0XHRjYXNlIDE6XG5cdFx0XHRcdHRlbXAgPSB1aW50OFt1aW50OC5sZW5ndGggLSAxXVxuXHRcdFx0XHRvdXRwdXQgKz0gZW5jb2RlKHRlbXAgPj4gMilcblx0XHRcdFx0b3V0cHV0ICs9IGVuY29kZSgodGVtcCA8PCA0KSAmIDB4M0YpXG5cdFx0XHRcdG91dHB1dCArPSAnPT0nXG5cdFx0XHRcdGJyZWFrXG5cdFx0XHRjYXNlIDI6XG5cdFx0XHRcdHRlbXAgPSAodWludDhbdWludDgubGVuZ3RoIC0gMl0gPDwgOCkgKyAodWludDhbdWludDgubGVuZ3RoIC0gMV0pXG5cdFx0XHRcdG91dHB1dCArPSBlbmNvZGUodGVtcCA+PiAxMClcblx0XHRcdFx0b3V0cHV0ICs9IGVuY29kZSgodGVtcCA+PiA0KSAmIDB4M0YpXG5cdFx0XHRcdG91dHB1dCArPSBlbmNvZGUoKHRlbXAgPDwgMikgJiAweDNGKVxuXHRcdFx0XHRvdXRwdXQgKz0gJz0nXG5cdFx0XHRcdGJyZWFrXG5cdFx0fVxuXG5cdFx0cmV0dXJuIG91dHB1dFxuXHR9XG5cblx0ZXhwb3J0cy50b0J5dGVBcnJheSA9IGI2NFRvQnl0ZUFycmF5XG5cdGV4cG9ydHMuZnJvbUJ5dGVBcnJheSA9IHVpbnQ4VG9CYXNlNjRcbn0odHlwZW9mIGV4cG9ydHMgPT09ICd1bmRlZmluZWQnID8gKHRoaXMuYmFzZTY0anMgPSB7fSkgOiBleHBvcnRzKSlcblxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi8uLi8uLi8uLi9ub2RlX21vZHVsZXMvZ3VscC1icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9idWZmZXIvbm9kZV9tb2R1bGVzL2Jhc2U2NC1qcy9saWIvYjY0LmpzXCIsXCIvLi4vLi4vLi4vbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnVmZmVyL25vZGVfbW9kdWxlcy9iYXNlNjQtanMvbGliXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xuZXhwb3J0cy5yZWFkID0gZnVuY3Rpb24oYnVmZmVyLCBvZmZzZXQsIGlzTEUsIG1MZW4sIG5CeXRlcykge1xuICB2YXIgZSwgbSxcbiAgICAgIGVMZW4gPSBuQnl0ZXMgKiA4IC0gbUxlbiAtIDEsXG4gICAgICBlTWF4ID0gKDEgPDwgZUxlbikgLSAxLFxuICAgICAgZUJpYXMgPSBlTWF4ID4+IDEsXG4gICAgICBuQml0cyA9IC03LFxuICAgICAgaSA9IGlzTEUgPyAobkJ5dGVzIC0gMSkgOiAwLFxuICAgICAgZCA9IGlzTEUgPyAtMSA6IDEsXG4gICAgICBzID0gYnVmZmVyW29mZnNldCArIGldO1xuXG4gIGkgKz0gZDtcblxuICBlID0gcyAmICgoMSA8PCAoLW5CaXRzKSkgLSAxKTtcbiAgcyA+Pj0gKC1uQml0cyk7XG4gIG5CaXRzICs9IGVMZW47XG4gIGZvciAoOyBuQml0cyA+IDA7IGUgPSBlICogMjU2ICsgYnVmZmVyW29mZnNldCArIGldLCBpICs9IGQsIG5CaXRzIC09IDgpO1xuXG4gIG0gPSBlICYgKCgxIDw8ICgtbkJpdHMpKSAtIDEpO1xuICBlID4+PSAoLW5CaXRzKTtcbiAgbkJpdHMgKz0gbUxlbjtcbiAgZm9yICg7IG5CaXRzID4gMDsgbSA9IG0gKiAyNTYgKyBidWZmZXJbb2Zmc2V0ICsgaV0sIGkgKz0gZCwgbkJpdHMgLT0gOCk7XG5cbiAgaWYgKGUgPT09IDApIHtcbiAgICBlID0gMSAtIGVCaWFzO1xuICB9IGVsc2UgaWYgKGUgPT09IGVNYXgpIHtcbiAgICByZXR1cm4gbSA/IE5hTiA6ICgocyA/IC0xIDogMSkgKiBJbmZpbml0eSk7XG4gIH0gZWxzZSB7XG4gICAgbSA9IG0gKyBNYXRoLnBvdygyLCBtTGVuKTtcbiAgICBlID0gZSAtIGVCaWFzO1xuICB9XG4gIHJldHVybiAocyA/IC0xIDogMSkgKiBtICogTWF0aC5wb3coMiwgZSAtIG1MZW4pO1xufTtcblxuZXhwb3J0cy53cml0ZSA9IGZ1bmN0aW9uKGJ1ZmZlciwgdmFsdWUsIG9mZnNldCwgaXNMRSwgbUxlbiwgbkJ5dGVzKSB7XG4gIHZhciBlLCBtLCBjLFxuICAgICAgZUxlbiA9IG5CeXRlcyAqIDggLSBtTGVuIC0gMSxcbiAgICAgIGVNYXggPSAoMSA8PCBlTGVuKSAtIDEsXG4gICAgICBlQmlhcyA9IGVNYXggPj4gMSxcbiAgICAgIHJ0ID0gKG1MZW4gPT09IDIzID8gTWF0aC5wb3coMiwgLTI0KSAtIE1hdGgucG93KDIsIC03NykgOiAwKSxcbiAgICAgIGkgPSBpc0xFID8gMCA6IChuQnl0ZXMgLSAxKSxcbiAgICAgIGQgPSBpc0xFID8gMSA6IC0xLFxuICAgICAgcyA9IHZhbHVlIDwgMCB8fCAodmFsdWUgPT09IDAgJiYgMSAvIHZhbHVlIDwgMCkgPyAxIDogMDtcblxuICB2YWx1ZSA9IE1hdGguYWJzKHZhbHVlKTtcblxuICBpZiAoaXNOYU4odmFsdWUpIHx8IHZhbHVlID09PSBJbmZpbml0eSkge1xuICAgIG0gPSBpc05hTih2YWx1ZSkgPyAxIDogMDtcbiAgICBlID0gZU1heDtcbiAgfSBlbHNlIHtcbiAgICBlID0gTWF0aC5mbG9vcihNYXRoLmxvZyh2YWx1ZSkgLyBNYXRoLkxOMik7XG4gICAgaWYgKHZhbHVlICogKGMgPSBNYXRoLnBvdygyLCAtZSkpIDwgMSkge1xuICAgICAgZS0tO1xuICAgICAgYyAqPSAyO1xuICAgIH1cbiAgICBpZiAoZSArIGVCaWFzID49IDEpIHtcbiAgICAgIHZhbHVlICs9IHJ0IC8gYztcbiAgICB9IGVsc2Uge1xuICAgICAgdmFsdWUgKz0gcnQgKiBNYXRoLnBvdygyLCAxIC0gZUJpYXMpO1xuICAgIH1cbiAgICBpZiAodmFsdWUgKiBjID49IDIpIHtcbiAgICAgIGUrKztcbiAgICAgIGMgLz0gMjtcbiAgICB9XG5cbiAgICBpZiAoZSArIGVCaWFzID49IGVNYXgpIHtcbiAgICAgIG0gPSAwO1xuICAgICAgZSA9IGVNYXg7XG4gICAgfSBlbHNlIGlmIChlICsgZUJpYXMgPj0gMSkge1xuICAgICAgbSA9ICh2YWx1ZSAqIGMgLSAxKSAqIE1hdGgucG93KDIsIG1MZW4pO1xuICAgICAgZSA9IGUgKyBlQmlhcztcbiAgICB9IGVsc2Uge1xuICAgICAgbSA9IHZhbHVlICogTWF0aC5wb3coMiwgZUJpYXMgLSAxKSAqIE1hdGgucG93KDIsIG1MZW4pO1xuICAgICAgZSA9IDA7XG4gICAgfVxuICB9XG5cbiAgZm9yICg7IG1MZW4gPj0gODsgYnVmZmVyW29mZnNldCArIGldID0gbSAmIDB4ZmYsIGkgKz0gZCwgbSAvPSAyNTYsIG1MZW4gLT0gOCk7XG5cbiAgZSA9IChlIDw8IG1MZW4pIHwgbTtcbiAgZUxlbiArPSBtTGVuO1xuICBmb3IgKDsgZUxlbiA+IDA7IGJ1ZmZlcltvZmZzZXQgKyBpXSA9IGUgJiAweGZmLCBpICs9IGQsIGUgLz0gMjU2LCBlTGVuIC09IDgpO1xuXG4gIGJ1ZmZlcltvZmZzZXQgKyBpIC0gZF0gfD0gcyAqIDEyODtcbn07XG5cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvLi4vLi4vLi4vbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnVmZmVyL25vZGVfbW9kdWxlcy9pZWVlNzU0L2luZGV4LmpzXCIsXCIvLi4vLi4vLi4vbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnVmZmVyL25vZGVfbW9kdWxlcy9pZWVlNzU0XCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xuLy8gc2hpbSBmb3IgdXNpbmcgcHJvY2VzcyBpbiBicm93c2VyXG5cbnZhciBwcm9jZXNzID0gbW9kdWxlLmV4cG9ydHMgPSB7fTtcblxucHJvY2Vzcy5uZXh0VGljayA9IChmdW5jdGlvbiAoKSB7XG4gICAgdmFyIGNhblNldEltbWVkaWF0ZSA9IHR5cGVvZiB3aW5kb3cgIT09ICd1bmRlZmluZWQnXG4gICAgJiYgd2luZG93LnNldEltbWVkaWF0ZTtcbiAgICB2YXIgY2FuUG9zdCA9IHR5cGVvZiB3aW5kb3cgIT09ICd1bmRlZmluZWQnXG4gICAgJiYgd2luZG93LnBvc3RNZXNzYWdlICYmIHdpbmRvdy5hZGRFdmVudExpc3RlbmVyXG4gICAgO1xuXG4gICAgaWYgKGNhblNldEltbWVkaWF0ZSkge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24gKGYpIHsgcmV0dXJuIHdpbmRvdy5zZXRJbW1lZGlhdGUoZikgfTtcbiAgICB9XG5cbiAgICBpZiAoY2FuUG9zdCkge1xuICAgICAgICB2YXIgcXVldWUgPSBbXTtcbiAgICAgICAgd2luZG93LmFkZEV2ZW50TGlzdGVuZXIoJ21lc3NhZ2UnLCBmdW5jdGlvbiAoZXYpIHtcbiAgICAgICAgICAgIHZhciBzb3VyY2UgPSBldi5zb3VyY2U7XG4gICAgICAgICAgICBpZiAoKHNvdXJjZSA9PT0gd2luZG93IHx8IHNvdXJjZSA9PT0gbnVsbCkgJiYgZXYuZGF0YSA9PT0gJ3Byb2Nlc3MtdGljaycpIHtcbiAgICAgICAgICAgICAgICBldi5zdG9wUHJvcGFnYXRpb24oKTtcbiAgICAgICAgICAgICAgICBpZiAocXVldWUubGVuZ3RoID4gMCkge1xuICAgICAgICAgICAgICAgICAgICB2YXIgZm4gPSBxdWV1ZS5zaGlmdCgpO1xuICAgICAgICAgICAgICAgICAgICBmbigpO1xuICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgIH1cbiAgICAgICAgfSwgdHJ1ZSk7XG5cbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uIG5leHRUaWNrKGZuKSB7XG4gICAgICAgICAgICBxdWV1ZS5wdXNoKGZuKTtcbiAgICAgICAgICAgIHdpbmRvdy5wb3N0TWVzc2FnZSgncHJvY2Vzcy10aWNrJywgJyonKTtcbiAgICAgICAgfTtcbiAgICB9XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gbmV4dFRpY2soZm4pIHtcbiAgICAgICAgc2V0VGltZW91dChmbiwgMCk7XG4gICAgfTtcbn0pKCk7XG5cbnByb2Nlc3MudGl0bGUgPSAnYnJvd3Nlcic7XG5wcm9jZXNzLmJyb3dzZXIgPSB0cnVlO1xucHJvY2Vzcy5lbnYgPSB7fTtcbnByb2Nlc3MuYXJndiA9IFtdO1xuXG5mdW5jdGlvbiBub29wKCkge31cblxucHJvY2Vzcy5vbiA9IG5vb3A7XG5wcm9jZXNzLmFkZExpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3Mub25jZSA9IG5vb3A7XG5wcm9jZXNzLm9mZiA9IG5vb3A7XG5wcm9jZXNzLnJlbW92ZUxpc3RlbmVyID0gbm9vcDtcbnByb2Nlc3MucmVtb3ZlQWxsTGlzdGVuZXJzID0gbm9vcDtcbnByb2Nlc3MuZW1pdCA9IG5vb3A7XG5cbnByb2Nlc3MuYmluZGluZyA9IGZ1bmN0aW9uIChuYW1lKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmJpbmRpbmcgaXMgbm90IHN1cHBvcnRlZCcpO1xufVxuXG4vLyBUT0RPKHNodHlsbWFuKVxucHJvY2Vzcy5jd2QgPSBmdW5jdGlvbiAoKSB7IHJldHVybiAnLycgfTtcbnByb2Nlc3MuY2hkaXIgPSBmdW5jdGlvbiAoZGlyKSB7XG4gICAgdGhyb3cgbmV3IEVycm9yKCdwcm9jZXNzLmNoZGlyIGlzIG5vdCBzdXBwb3J0ZWQnKTtcbn07XG5cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvLi4vLi4vLi4vbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzcy9icm93c2VyLmpzXCIsXCIvLi4vLi4vLi4vbm9kZV9tb2R1bGVzL2d1bHAtYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvYnJvd3NlcmlmeS9ub2RlX21vZHVsZXMvcHJvY2Vzc1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbnZhciBCYWNvbiA9IHJlcXVpcmUoXCJiYWNvbmpzXCIpXG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24obGlzdEFwcCwgaXNUZXN0TW9kZSkge1xuICBsaXN0QXBwLmZhY3RvcnkoXCJhbmd1bGFyQmFjb25cIiwgZnVuY3Rpb24gKCkge1xuICAgIHJldHVybiB7XG4gICAgICB3YXRjaDogZnVuY3Rpb24oc2NvcGUsIGV4cHJlc3Npb24pIHtcbiAgICAgICAgdmFyIHN0cmVhbSA9IG5ldyBCYWNvbi5CdXMoKVxuICAgICAgICBzY29wZS4kd2F0Y2goZXhwcmVzc2lvbiwgZnVuY3Rpb24odmFsLCBwcmV2VmFsKSB7IGlmICh2YWwgIT09IHByZXZWYWwpIHN0cmVhbS5wdXNoKHZhbCkgfSlcbiAgICAgICAgcmV0dXJuIHN0cmVhbVxuICAgICAgfSxcblxuICAgICAgcmVzb3VyY2U6IGZ1bmN0aW9uKHJlc291cmNlKSB7XG4gICAgICAgIHJldHVybiBmdW5jdGlvbihxdWVyeVBhcmFtcywgYm9keSkge1xuICAgICAgICAgIHJldHVybiBCYWNvbi5mcm9tTm9kZUNhbGxiYWNrKGZ1bmN0aW9uIChjYWxsYmFjaykge1xuICAgICAgICAgICAgcmVzb3VyY2UocXVlcnlQYXJhbXMsIGJvZHksIHN1Y2Nlc3MsIGVycm9yKVxuXG4gICAgICAgICAgICBmdW5jdGlvbiBzdWNjZXNzKHZhbHVlKSB7XG4gICAgICAgICAgICAgIGNhbGxiYWNrKG51bGwsIHZhbHVlKVxuICAgICAgICAgICAgfVxuXG4gICAgICAgICAgICBmdW5jdGlvbiBlcnJvcihyZXNwb25zZSkge1xuICAgICAgICAgICAgICBjYWxsYmFjayhyZXNwb25zZSlcbiAgICAgICAgICAgIH1cbiAgICAgICAgICB9KVxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICB9KVxufVxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi9hbmd1bGFyQmFjb24uanNcIixcIi9cIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG52YXIgUXVlc3Rpb24gPSByZXF1aXJlKCcuL3F1ZXN0aW9uJykuUXVlc3Rpb25cbnZhciBRdWVzdGlvbkdyb3VwID0gcmVxdWlyZSgnLi9xdWVzdGlvbicpLlF1ZXN0aW9uR3JvdXBcblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZmFjdG9yeShcImFwcGxpY2F0aW9uVmFsaWRhdG9yXCIsIFtcIiRodHRwXCIsIGZ1bmN0aW9uKCRodHRwKSB7XG5cbiAgICByZXR1cm4gZnVuY3Rpb24gYXBwbGljYXRpb25WYWxpZGF0b3IoKSB7XG4gICAgICB2YXIgY3VycmVudFJlcXVlc3RcblxuICAgICAgZnVuY3Rpb24gb25seUlmQ3VycmVudFJlcXVlc3QoY3VycmVudCwgZikge1xuICAgICAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgaWYgKGN1cnJlbnRSZXF1ZXN0ID09PSBjdXJyZW50KVxuICAgICAgICAgICAgZi5hcHBseSh0aGlzLCBhcmd1bWVudHMpXG4gICAgICAgIH1cbiAgICAgIH1cblxuICAgICAgcmV0dXJuIGZ1bmN0aW9uKGFwcGxpY2F0aW9uLCBiZWZvcmVCYWNrZW5kVmFsaWRhdGlvbiwgc3VjY2VzcywgZXJyb3IpIHtcbiAgICAgICAgY3VycmVudFJlcXVlc3QgPSB7fVxuICAgICAgICBzdWNjZXNzID0gb25seUlmQ3VycmVudFJlcXVlc3QoY3VycmVudFJlcXVlc3QsIHN1Y2Nlc3MpXG4gICAgICAgIGVycm9yID0gb25seUlmQ3VycmVudFJlcXVlc3QoY3VycmVudFJlcXVlc3QsIGVycm9yKVxuXG4gICAgICAgIHZhciBwcmVmZXJlbmNlc1ZhbGlkID0gYXBwbGljYXRpb24udmFsaWRhdGVQcmVmZXJlbmNlcygpXG4gICAgICAgIGlmIChwcmVmZXJlbmNlc1ZhbGlkKSB7XG4gICAgICAgICAgYmVmb3JlQmFja2VuZFZhbGlkYXRpb24oKVxuICAgICAgICAgIHZhbGlkYXRlQmFja2VuZChhcHBsaWNhdGlvbiwgc3VjY2VzcywgZXJyb3IpXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgZXJyb3Ioe1xuICAgICAgICAgICAgZXJyb3JzOiBbXVxuICAgICAgICAgIH0pXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9XG5cbiAgICBmdW5jdGlvbiB2YWxpZGF0ZUJhY2tlbmQoYXBwbGljYXRpb24sIHN1Y2Nlc3MsIGVycm9yKSB7XG5cbiAgICAgIHZhciByZXNwb25zZVByb21pc2UgPSAkaHR0cC5wb3N0KFwiL29tYXRzaXZ1dC9zZWN1cmUvYXBwbGljYXRpb25zL3ZhbGlkYXRlL1wiICsgYXBwbGljYXRpb24ub2lkLCBhcHBsaWNhdGlvbi50b0pzb24oKSlcbiAgICAgIHJlc3BvbnNlUHJvbWlzZS5zdWNjZXNzKGZ1bmN0aW9uKGRhdGEsIHN0YXR1cywgaGVhZGVycywgY29uZmlnKSB7XG4gICAgICAgIGlmIChkYXRhLmVycm9ycy5sZW5ndGggPT09IDApIHtcbiAgICAgICAgICBzdWNjZXNzKHtcbiAgICAgICAgICAgIHF1ZXN0aW9uczogUXVlc3Rpb24uZ2V0UXVlc3Rpb25zKGRhdGEucXVlc3Rpb25zLCBhcHBsaWNhdGlvbiksXG4gICAgICAgICAgICByZXNwb25zZTogZGF0YVxuICAgICAgICAgIH0pXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgZXJyb3Ioe1xuICAgICAgICAgICAgc3RhdHVzQ29kZTogMjAwLFxuICAgICAgICAgICAgZXJyb3JzOiBkYXRhLmVycm9ycyxcbiAgICAgICAgICAgIHF1ZXN0aW9uczogUXVlc3Rpb24uZ2V0UXVlc3Rpb25zKGRhdGEucXVlc3Rpb25zLCBhcHBsaWNhdGlvbiksXG4gICAgICAgICAgICByZXNwb25zZTogZGF0YVxuICAgICAgICAgIH0pXG4gICAgICAgIH1cbiAgICAgIH0pXG5cbiAgICAgIHJlc3BvbnNlUHJvbWlzZS5lcnJvcihmdW5jdGlvbihkYXRhLCBzdGF0dXMpIHtcbiAgICAgICAgZXJyb3Ioe1xuICAgICAgICAgIGVycm9yczogW10sXG4gICAgICAgICAgc3RhdHVzQ29kZTogc3RhdHVzLFxuICAgICAgICAgIGlzU2F2ZWFibGU6IHRydWUsXG4gICAgICAgICAgcmVzcG9uc2U6IGRhdGFcbiAgICAgICAgfSlcbiAgICAgIH0pXG4gICAgfVxuICB9XSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvYXBwbGljYXRpb25WYWxpZGF0b3IuanNcIixcIi9cIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG52YXIgSGFrZW11cyA9IHJlcXVpcmUoXCIuLi9oYWtlbXVzXCIpXG52YXIgSGFrdXRvaXZlID0gcmVxdWlyZShcIi4uL2hha3V0b2l2ZVwiKVxudmFyIHV0aWwgPSByZXF1aXJlKFwiLi4vdXRpbFwiKVxudmFyIFF1ZXN0aW9uID0gcmVxdWlyZShcIi4uL3F1ZXN0aW9uXCIpLlF1ZXN0aW9uXG5cbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24obGlzdEFwcCkge1xuICBsaXN0QXBwLmRpcmVjdGl2ZShcImFwcGxpY2F0aW9uXCIsIFtcIiRodHRwXCIsIFwiJHNjZVwiLCBcInJlc3RSZXNvdXJjZXNcIiwgXCJhcHBsaWNhdGlvblZhbGlkYXRvclwiLCBcInNldHRpbmdzXCIsIFwiZGVib3VuY2VcIiwgXCJsb2NhbGl6YXRpb25cIiwgXCIkdGltZW91dFwiLCBmdW5jdGlvbiAoJGh0dHAsICRzY2UsIHJlc3RSZXNvdXJjZXMsIGFwcGxpY2F0aW9uVmFsaWRhdG9yLCBzZXR0aW5ncywgZGVib3VuY2UsIGxvY2FsaXphdGlvbiwgJHRpbWVvdXQpIHtcbiAgICByZXR1cm4ge1xuICAgICAgcmVzdHJpY3Q6ICdFJyxcbiAgICAgIHNjb3BlOiB7XG4gICAgICAgIGFwcGxpY2F0aW9uOiBcIj1hcHBsaWNhdGlvblwiXG4gICAgICB9LFxuICAgICAgdGVtcGxhdGVVcmw6ICd0ZW1wbGF0ZXMvYXBwbGljYXRpb24uaHRtbCcsXG5cbiAgICAgIGxpbms6IGZ1bmN0aW9uICgkc2NvcGUsICRlbGVtZW50LCBhdHRycykge1xuICAgICAgICAkc2NvcGUubG9jYWxpemF0aW9uID0gbG9jYWxpemF0aW9uXG4gICAgICAgIHZhciBhcHBsaWNhdGlvblZhbGlkYXRvckJvdW5jZWQgPSBkZWJvdW5jZShhcHBsaWNhdGlvblZhbGlkYXRvcigpLCBzZXR0aW5ncy5tb2RlbERlYm91bmNlKVxuICAgICAgICAkc2NvcGUuaXNTYXZlYWJsZSA9IHRydWVcbiAgICAgICAgJHNjb3BlLmlzVmFsaWRhdGluZyA9IGZhbHNlXG5cbiAgICAgICAgJHNjb3BlLnRpbWVzdGFtcExhYmVsID0gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgaWYgKCRzY29wZS5hcHBsaWNhdGlvbi5yZWNlaXZlZCA9PSAkc2NvcGUuYXBwbGljYXRpb24udXBkYXRlZClcbiAgICAgICAgICAgIHJldHVybiBsb2NhbGl6YXRpb24oXCJsYWJlbC5hcHBsaWNhdGlvblJlY2VpdmVkXCIpXG4gICAgICAgICAgZWxzZVxuICAgICAgICAgICAgcmV0dXJuIGxvY2FsaXphdGlvbihcImxhYmVsLmFwcGxpY2F0aW9uVXBkYXRlZFwiKVxuICAgICAgICB9XG5cbiAgICAgICAgJHNjb3BlLnNob3VsZFNhdmVCdXR0b25CZURpc2FibGVkID0gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmV0dXJuICRzY29wZS5hcHBsaWNhdGlvbkZvcm0uJHByaXN0aW5lIHx8ICRzY29wZS5zdGF0dXNNZXNzYWdlVHlwZT09J3BlbmRpbmcnIHx8ICEkc2NvcGUuaXNTYXZlYWJsZSB8fCAkc2NvcGUuaXNWYWxpZGF0aW5nXG4gICAgICAgIH1cblxuICAgICAgICAkc2NvcGUuc3RhdHVzTWVzc2FnZVN0eWxlTW9kaWZpZXIgPSBmdW5jdGlvbigpIHtcbiAgICAgICAgICByZXR1cm4geydhamF4LXNwaW5uZXInOiAkc2NvcGUuc3RhdHVzTWVzc2FnZVR5cGU9PSdwZW5kaW5nJywgZXJyb3I6ICRzY29wZS5zdGF0dXNNZXNzYWdlVHlwZT09J2Vycm9yJ31cbiAgICAgICAgfVxuXG4gICAgICAgIGZ1bmN0aW9uIGdldEhha3V0b2l2ZWV0KCkge1xuICAgICAgICAgIHJldHVybiBfKCRzY29wZS5hcHBsaWNhdGlvbi5oYWt1dG9pdmVldCkubWFwKGZ1bmN0aW9uKGhha3V0b2l2ZSkge1xuICAgICAgICAgICAgcmV0dXJuIHtcbiAgICAgICAgICAgICAgXCJLb3VsdXR1c1wiOiBoYWt1dG9pdmUuZGF0YVtcIktvdWx1dHVzXCJdLFxuICAgICAgICAgICAgICBcIktvdWx1dHVzLWlkXCI6IGhha3V0b2l2ZS5kYXRhW1wiS291bHV0dXMtaWRcIl0sXG4gICAgICAgICAgICAgIFwiT3BldHVzcGlzdGVcIjogaGFrdXRvaXZlLmRhdGFbXCJPcGV0dXNwaXN0ZVwiXSxcbiAgICAgICAgICAgICAgXCJPcGV0dXNwaXN0ZS1pZFwiOiBoYWt1dG9pdmUuZGF0YVtcIk9wZXR1c3Bpc3RlLWlkXCJdXG4gICAgICAgICAgICB9XG4gICAgICAgICAgfSlcbiAgICAgICAgfVxuXG4gICAgICAgIGZ1bmN0aW9uIGdldEFuc3dlcnMoKSB7XG4gICAgICAgICAgdmFyIGFuc3dlcnNUb0FkZGl0aW9uYWxRdWVzdGlvbnMgPSAgXyhRdWVzdGlvbi5xdWVzdGlvbk1hcCgkc2NvcGUuYXBwbGljYXRpb24uYWRkaXRpb25hbFF1ZXN0aW9ucykpLm1hcChmdW5jdGlvbihpdGVtLCBrZXkpIHsgcmV0dXJuIGl0ZW0uYW5zd2VyIH0pXG4gICAgICAgICAgdmFyIG90aGVyQW5zd2VycyA9IF8oJHNjb3BlLmFwcGxpY2F0aW9uLmhlbmtpbG90aWVkb3QpLm1hcChmdW5jdGlvbihpdGVtKcKgeyByZXR1cm4gaXRlbS5hbnN3ZXIgfSlcbiAgICAgICAgICByZXR1cm4gYW5zd2Vyc1RvQWRkaXRpb25hbFF1ZXN0aW9ucy5jb25jYXQob3RoZXJBbnN3ZXJzKVxuICAgICAgICB9XG5cbiAgICAgICAgJHNjb3BlLiR3YXRjaChnZXRIYWt1dG9pdmVldCwgZnVuY3Rpb24oaGFrdXRvaXZlZXQsIG9sZEhha3V0b2l2ZWV0KSB7XG4gICAgICAgICAgLy8gU2tpcCBpbml0aWFsIHZhbHVlcyBhbmd1bGFyIHN0eWxlXG4gICAgICAgICAgaWYgKCFfLmlzRXF1YWwoaGFrdXRvaXZlZXQsIG9sZEhha3V0b2l2ZWV0KSkge1xuICAgICAgICAgICAgYXBwbGljYXRpb25DaGFuZ2VkKClcbiAgICAgICAgICAgIHZhbGlkYXRlSGFrdXRvaXZlZXQodHJ1ZSlcbiAgICAgICAgICB9XG4gICAgICAgIH0sIHRydWUpXG5cbiAgICAgICAgJHNjb3BlLiR3YXRjaChnZXRBbnN3ZXJzLCBmdW5jdGlvbihhbnN3ZXJzLCBvbGRBbnN3ZXJzKSB7XG4gICAgICAgICAgaWYgKCFfLmlzRXF1YWwob2xkQW5zd2VycywgYW5zd2VycykpIHtcbiAgICAgICAgICAgIGFwcGxpY2F0aW9uQ2hhbmdlZCgpXG4gICAgICAgICAgfVxuICAgICAgICB9LCB0cnVlKVxuXG4gICAgICAgICRzY29wZS4kb24oXCJxdWVzdGlvbkFuc3dlcmVkXCIsIGZ1bmN0aW9uKCkge1xuICAgICAgICAgIHZhbGlkYXRlSGFrdXRvaXZlZXQoZmFsc2UpXG4gICAgICAgIH0pXG5cbiAgICAgICAgJHNjb3BlLmhha3V0b2l2ZVZhc3RhYW5vdGV0dHUgPSBmdW5jdGlvbihoYWt1dG9pdmUsIHVwZGF0ZWQpIHtcbiAgICAgICAgICAkc2NvcGUuYXBwbGljYXRpb24ubWVyZ2VTYXZlZEFwcGxpY2F0aW9uKHVwZGF0ZWQuaGFrZW11cylcbiAgICAgICAgICAkdGltZW91dChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgICRzY29wZS4kYnJvYWRjYXN0KFwiaGFrdXRvaXZlLXZhc3RhYW5vdGV0dHVcIiwgaGFrdXRvaXZlKVxuICAgICAgICAgIH0sIDApXG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiBhcHBsaWNhdGlvbkNoYW5nZWQoKSB7XG4gICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uRm9ybS4kc2V0RGlydHkoKVxuICAgICAgICAgIGlmICgkc2NvcGUuc3RhdHVzTWVzc2FnZVR5cGUgPT0gXCJzdWNjZXNzXCIpXG4gICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKFwiXCIpXG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiB2YWxpZGF0ZUhha3V0b2l2ZWV0KHNraXBRdWVzdGlvbnMpIHtcbiAgICAgICAgICBhcHBsaWNhdGlvblZhbGlkYXRvckJvdW5jZWQoJHNjb3BlLmFwcGxpY2F0aW9uLCBiZWZvcmVCYWNrZW5kVmFsaWRhdGlvbiwgc3VjY2VzcywgZXJyb3IpXG5cbiAgICAgICAgICBmdW5jdGlvbiBiZWZvcmVCYWNrZW5kVmFsaWRhdGlvbigpIHtcbiAgICAgICAgICAgIHNldFZhbGlkYXRpbmdJbmRpY2F0b3IodHJ1ZSlcbiAgICAgICAgICB9XG5cbiAgICAgICAgICBmdW5jdGlvbiBzdWNjZXNzKGRhdGEpIHtcbiAgICAgICAgICAgIHNldFN0YXR1c01lc3NhZ2UobG9jYWxpemF0aW9uKFwibWVzc2FnZS52YWxpZGF0aW9uT2tcIiksIFwiaW5mb1wiKVxuICAgICAgICAgICAgJHNjb3BlLmlzU2F2ZWFibGUgPSB0cnVlXG4gICAgICAgICAgICBzZXRWYWxpZGF0aW5nSW5kaWNhdG9yKGZhbHNlKVxuICAgICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLmltcG9ydFF1ZXN0aW9ucyhkYXRhLnF1ZXN0aW9ucylcbiAgICAgICAgICAgICRzY29wZS5hcHBsaWNhdGlvbi5pbXBvcnRIYWt1YWphdChkYXRhLnJlc3BvbnNlLmhha2VtdXMuaGFrdXRvaXZlZXQpXG4gICAgICAgICAgICB1cGRhdGVWYWxpZGF0aW9uTWVzc2FnZXMoW10sIHNraXBRdWVzdGlvbnMpXG4gICAgICAgICAgfVxuXG4gICAgICAgICAgZnVuY3Rpb24gZXJyb3IoZGF0YSkge1xuICAgICAgICAgICAgc2V0VmFsaWRhdGluZ0luZGljYXRvcihmYWxzZSlcbiAgICAgICAgICAgIGlmICghZGF0YS5zdGF0dXNDb2RlKSB7IC8vIHZhbGlkb2ludGkgZXDDpG9ubmlzdHVpIGZyb250ZW5kaXNzw6RcbiAgICAgICAgICAgICAgJHNjb3BlLmlzU2F2ZWFibGUgPSBmYWxzZVxuICAgICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKGxvY2FsaXphdGlvbihcImVycm9yLnZhbGlkYXRpb25GYWlsZWRcIiksIFwiZXJyb3JcIilcbiAgICAgICAgICAgIH0gZWxzZSBpZiAoZGF0YS5zdGF0dXNDb2RlID09PSAyMDApIHtcbiAgICAgICAgICAgICAgJHNjb3BlLmlzU2F2ZWFibGUgPSAhSGFrdXRvaXZlLmhhc0hha3V0b2l2ZUVycm9ycyhkYXRhLmVycm9ycylcbiAgICAgICAgICAgICAgc2V0U3RhdHVzTWVzc2FnZShsb2NhbGl6YXRpb24oXCJlcnJvci52YWxpZGF0aW9uRmFpbGVkXCIpLCBcImVycm9yXCIpXG4gICAgICAgICAgICB9IGVsc2UgaWYgKGRhdGEuc3RhdHVzQ29kZSA9PSA0MDEpIHtcbiAgICAgICAgICAgICAgJHNjb3BlLmlzU2F2ZWFibGUgPSB0cnVlXG4gICAgICAgICAgICAgIHNldFN0YXR1c01lc3NhZ2UobG9jYWxpemF0aW9uKFwiZXJyb3Iuc2Vzc2lvbkV4cGlyZWRcIiksIFwiZXJyb3JcIilcbiAgICAgICAgICAgIH0gZWxzZSBpZiAoZGF0YS5zdGF0dXNDb2RlID09IDUwMCkge1xuICAgICAgICAgICAgICAkc2NvcGUuaXNTYXZlYWJsZSA9IHRydWVcbiAgICAgICAgICAgICAgc2V0U3RhdHVzTWVzc2FnZShsb2NhbGl6YXRpb24oXCJlcnJvci5zZXJ2ZXJFcnJvclwiKSwgXCJlcnJvclwiKVxuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgJHNjb3BlLmlzU2F2ZWFibGUgPSBmYWxzZVxuICAgICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKGxvY2FsaXphdGlvbihcImVycm9yLnZhbGlkYXRpb25GYWlsZWRfaHR0cEVycm9yXCIpLCBcImVycm9yXCIpXG4gICAgICAgICAgICB9XG5cbiAgICAgICAgICAgIHZhciB1cGRhdGVRdWVzdGlvbnMgPSBkYXRhLnF1ZXN0aW9ucyAhPSBudWxsICYmICFIYWt1dG9pdmUuaGFzSGFrdXRvaXZlRXJyb3JzKGRhdGEuZXJyb3JzKVxuXG4gICAgICAgICAgICBpZiAodXBkYXRlUXVlc3Rpb25zKSAvLyBmcm9udHNpZGUgdmFsaWRhdGlvbiBkb2VzIG5vdCBpbmNsdWRlIHF1ZXN0aW9ucyAtPiBkb24ndCB1cGRhdGVcbiAgICAgICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLmltcG9ydFF1ZXN0aW9ucyhkYXRhLnF1ZXN0aW9ucylcblxuICAgICAgICAgICAgaWYgKGRhdGEucmVzcG9uc2UgIT0gbnVsbCAmJiBkYXRhLnJlc3BvbnNlLmhha2VtdXMgIT0gbnVsbClcbiAgICAgICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLmltcG9ydEhha3VhamF0KGRhdGEucmVzcG9uc2UuaGFrZW11cy5oYWt1dG9pdmVldClcblxuICAgICAgICAgICAgdXBkYXRlVmFsaWRhdGlvbk1lc3NhZ2VzKGRhdGEuZXJyb3JzLCBza2lwUXVlc3Rpb25zKVxuICAgICAgICAgIH1cbiAgICAgICAgfVxuXG4gICAgICAgICRzY29wZS5wcmVmZXJlbmNlTW92ZWQgPSBmdW5jdGlvbigpIHtcbiAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKFwiXCIpXG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiBzZXRTdGF0dXNNZXNzYWdlKG1zZywgdHlwZSkge1xuICAgICAgICAgICRzY29wZS5zdGF0dXNNZXNzYWdlID0gbXNnXG4gICAgICAgICAgJHNjb3BlLnN0YXR1c01lc3NhZ2VUeXBlID0gdHlwZSB8fCBcIlwiXG4gICAgICAgIH1cblxuICAgICAgICB2YXIgc2V0VmFsaWRhdGluZ0luZGljYXRvciA9IGRlYm91bmNlKGZ1bmN0aW9uKGlzVmlzaWJsZSkge1xuICAgICAgICAgICRzY29wZS5pc1ZhbGlkYXRpbmcgPSBpc1Zpc2libGVcbiAgICAgICAgfSwgc2V0dGluZ3MudWlJbmRpY2F0b3JEZWJvdW5jZSlcblxuICAgICAgICAkc2NvcGUuc2F2ZUFwcGxpY2F0aW9uID0gZnVuY3Rpb24oKSB7XG4gICAgICAgICAgcmVzdFJlc291cmNlcy5hcHBsaWNhdGlvbnMudXBkYXRlKHtpZDogJHNjb3BlLmFwcGxpY2F0aW9uLm9pZCB9LCAkc2NvcGUuYXBwbGljYXRpb24udG9Kc29uKCksIG9uU3VjY2Vzcywgb25FcnJvcilcbiAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKFwiXCIsIFwicGVuZGluZ1wiKVxuXG4gICAgICAgICAgZnVuY3Rpb24gb25TdWNjZXNzKHNhdmVkQXBwbGljYXRpb24pIHtcbiAgICAgICAgICAgIGhpZ2hsaWdodFNhdmVkSXRlbXMoJHNjb3BlLmFwcGxpY2F0aW9uLmdldENoYW5nZWRQcmVmZXJlbmNlcygpKVxuICAgICAgICAgICAgJHNjb3BlLiRicm9hZGNhc3QoXCJzaG93LWNhbGxvdXRcIiwgXCJhdHRhY2htZW50c1wiLCBzYXZlZEFwcGxpY2F0aW9uLnJlcXVpcmVzQWRkaXRpb25hbEluZm8gPT09IHRydWUgJiYgJHNjb3BlLmFwcGxpY2F0aW9uLmdldENoYW5nZWRQcmVmZXJlbmNlcygpLmxlbmd0aCA+IDApXG4gICAgICAgICAgICAkc2NvcGUuYXBwbGljYXRpb24ubWVyZ2VTYXZlZEFwcGxpY2F0aW9uKHNhdmVkQXBwbGljYXRpb24pXG4gICAgICAgICAgICAkc2NvcGUuYXBwbGljYXRpb25Gb3JtLiRzZXRQcmlzdGluZSgpXG4gICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKGxvY2FsaXphdGlvbihcIm1lc3NhZ2UuY2hhbmdlc1NhdmVkXCIpLCBcInN1Y2Nlc3NcIilcbiAgICAgICAgICAgIHVwZGF0ZVZhbGlkYXRpb25NZXNzYWdlcyhbXSlcbiAgICAgICAgICAgIGlmKCRzY29wZS5hcHBsaWNhdGlvbi5lZGl0SGFrdXRvaXZlZXRFbmFibGVkKCkpIHNjcm9sbFRvVG9wKClcbiAgICAgICAgICB9XG5cbiAgICAgICAgICBmdW5jdGlvbiBvbkVycm9yKGVycikge1xuICAgICAgICAgICAgdmFyIHNhdmVFcnJvciA9IChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgICAgaWYgKGVyci5zdGF0dXMgPT0gNDAwICYmIChfLmlzQXJyYXkoZXJyLmRhdGEpICYmIGVyci5kYXRhLmxlbmd0aCA+IDApKVxuICAgICAgICAgICAgICAgIHJldHVybiBcImVycm9yLnNhdmVGYWlsZWRfdmFsaWRhdGlvbkVycm9yXCJcbiAgICAgICAgICAgICAgZWxzZSBpZiAoZXJyLnN0YXR1cyA9PSA0MDAgJiYgIShfLmlzQXJyYXkoZXJyLmRhdGEpICYmIGVyci5kYXRhLmxlbmd0aCA+IDApKVxuICAgICAgICAgICAgICAgIHJldHVybiBcImVycm9yLnNlcnZlckVycm9yXCJcbiAgICAgICAgICAgICAgZWxzZSBpZiAoZXJyLnN0YXR1cyA9PSA0MDEpXG4gICAgICAgICAgICAgICAgcmV0dXJuIFwiZXJyb3Iuc2F2ZUZhaWxlZF9zZXNzaW9uRXhwaXJlZFwiXG4gICAgICAgICAgICAgIGVsc2UgaWYgKGVyci5zdGF0dXMgPT0gNTAwKVxuICAgICAgICAgICAgICAgIHJldHVybiBcImVycm9yLnNlcnZlckVycm9yXCJcbiAgICAgICAgICAgICAgZWxzZVxuICAgICAgICAgICAgICAgIHJldHVybiBcImVycm9yLnNhdmVGYWlsZWRcIlxuICAgICAgICAgICAgfSkoKVxuXG4gICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKGxvY2FsaXphdGlvbihzYXZlRXJyb3IpLCBcImVycm9yXCIpXG4gICAgICAgICAgICBpZiAoZXJyLnN0YXR1cyA9PSA0MDApIC8vIFZhbGlkb2ludGl2aXJoZVxuICAgICAgICAgICAgICB1cGRhdGVWYWxpZGF0aW9uTWVzc2FnZXMoZXJyLmRhdGEpXG4gICAgICAgICAgfVxuICAgICAgICB9XG5cbiAgICAgICAgZnVuY3Rpb24gaGlnaGxpZ2h0U2F2ZWRJdGVtcyhpbmRleGVzKSB7XG4gICAgICAgICAgdmFyIGl0ZW1zID0gJGVsZW1lbnQuZmluZChcIi5wcmVmZXJlbmNlLWxpc3QtaXRlbVwiKVxuXG4gICAgICAgICAgXy5lYWNoKGluZGV4ZXMsIGZ1bmN0aW9uKGluZGV4KSB7XG4gICAgICAgICAgICBpdGVtcy5lcShpbmRleCkuYWRkQ2xhc3MoXCJzYXZlZFwiKVxuICAgICAgICAgIH0pXG5cbiAgICAgICAgICAkZWxlbWVudC5maW5kKFwiLnRpbWVzdGFtcC1yb3dcIikuYWRkQ2xhc3MoXCJzYXZlZFwiKVxuICAgICAgICAgICRlbGVtZW50LmZpbmQoXCIueWh0ZXlzdGllZG90IC5uZy1kaXJ0eVwiKS5hZGRDbGFzcyhcInNhdmVkXCIpIC8vIGVkaXRlZCBjb250YWN0IGRldGFpbHNcblxuICAgICAgICAgIHdpbmRvdy5zZXRUaW1lb3V0KGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgJGVsZW1lbnQuZmluZChcIi5zYXZlZFwiKS5yZW1vdmVDbGFzcyhcInNhdmVkXCIpXG4gICAgICAgICAgICAkKFwiLnRpbWVzdGFtcC1yb3dcIikucmVtb3ZlQ2xhc3MoXCJzYXZlZFwiKVxuICAgICAgICAgIH0sIDMwMDApXG4gICAgICAgIH1cblxuICAgICAgICBmdW5jdGlvbiBzY3JvbGxUb1RvcCgpIHtcbiAgICAgICAgICB2YXIgJGFwcGxpY2F0aW9uSGVhZGVyID0gJCgkZWxlbWVudFswXSkuZmluZChcIi5hcHBsaWNhdGlvbi1oZWFkZXJcIilcbiAgICAgICAgICBpZiAoJGFwcGxpY2F0aW9uSGVhZGVyLmdldCgwKSAhPSBudWxsICYmICQod2luZG93KS5zY3JvbGxUb3AoKSA+ICRhcHBsaWNhdGlvbkhlYWRlci5nZXQoMCkub2Zmc2V0VG9wKSAkYXBwbGljYXRpb25IZWFkZXIuZ2V0KDApLnNjcm9sbEludG9WaWV3KClcbiAgICAgICAgfVxuXG4gICAgICAgIGZ1bmN0aW9uIHVwZGF0ZVZhbGlkYXRpb25NZXNzYWdlcyhlcnJvcnMsIHNraXBRdWVzdGlvbnMpIHtcbiAgICAgICAgICB2YXIgdW5oYW5kbGVkTWVzc2FnZXMgPSAkc2NvcGUuYXBwbGljYXRpb24udXBkYXRlVmFsaWRhdGlvbk1lc3NhZ2VzKGVycm9ycywgc2tpcFF1ZXN0aW9ucylcbiAgICAgICAgICB1bmhhbmRsZWRNZXNzYWdlcyA9IGhpZGVFcnJvcklmQWxyZWFkeVNob3dzS291bHV0dXNFcnJvcih1bmhhbmRsZWRNZXNzYWdlcylcblxuICAgICAgICAgIGlmICh1bmhhbmRsZWRNZXNzYWdlcy5sZW5ndGggPiAwKSB7XG4gICAgICAgICAgICBfKHVuaGFuZGxlZE1lc3NhZ2VzKS5lYWNoKGZ1bmN0aW9uKGl0ZW0pIHtcbiAgICAgICAgICAgICAgY29uc29sZS5sb2coXCJWYWxpZGFhdGlvdmlyaGV0dMOkIGVpIGvDpHNpdGVsdHk6XCIsIGl0ZW0ucXVlc3Rpb25JZCwgaXRlbS5lcnJvcnMpXG4gICAgICAgICAgICB9KVxuXG4gICAgICAgICAgICBzZXRTdGF0dXNNZXNzYWdlKGxvY2FsaXphdGlvbihcImVycm9yLnNlcnZlckVycm9yXCIpLCBcImVycm9yXCIpXG4gICAgICAgICAgfVxuXG4gICAgICAgICAgZnVuY3Rpb24gaGlkZUVycm9ySWZBbHJlYWR5U2hvd3NLb3VsdXR1c0Vycm9yKG1lc3NhZ2VzKSB7XG4gICAgICAgICAgICByZXR1cm4gXyhtZXNzYWdlcykuZmlsdGVyKGZ1bmN0aW9uKG1lc3NhZ2UpIHtcbiAgICAgICAgICAgICAgdmFyIGluZGV4ID0gSGFrdXRvaXZlLnBhcnNlSGFrdXRvaXZlSW5kZXgobWVzc2FnZS5xdWVzdGlvbklkKVxuICAgICAgICAgICAgICB2YXIgcmVsYXRlZEVycm9yU2hvd24gPSBfKGVycm9ycykuYW55KGZ1bmN0aW9uKGVycm9yKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuIEhha3V0b2l2ZS5pc0hha3V0b2l2ZUVycm9yKGVycm9yLmtleSkgJiYgSGFrdXRvaXZlLnBhcnNlSGFrdXRvaXZlSW5kZXgoZXJyb3Iua2V5KSA9PSBpbmRleFxuICAgICAgICAgICAgICB9KVxuICAgICAgICAgICAgICByZXR1cm4gIXJlbGF0ZWRFcnJvclNob3duXG4gICAgICAgICAgICB9KVxuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfVxuICAgIH1cbiAgfV0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvYXBwbGljYXRpb24uanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xudmFyIHV0aWwgPSByZXF1aXJlKFwiLi4vdXRpbFwiKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgbGlzdEFwcC5kaXJlY3RpdmUoXCJhcHBsaWNhdGlvblBlcmlvZHNcIiwgW1wibG9jYWxpemF0aW9uXCIsIGZ1bmN0aW9uIChsb2NhbGl6YXRpb24pIHtcbiAgICByZXR1cm4ge1xuICAgICAgcmVzdHJpY3Q6ICdFJyxcbiAgICAgIHNjb3BlOiB7XG4gICAgICAgIGhha3U6ICcmaGFrdSdcbiAgICAgIH0sXG4gICAgICB0ZW1wbGF0ZVVybDogJ3RlbXBsYXRlcy9hcHBsaWNhdGlvblBlcmlvZHMuaHRtbCcsXG4gICAgICBsaW5rOiBmdW5jdGlvbiAoJHNjb3BlLCBlbGVtZW50LCBhdHRycykge1xuICAgICAgICAkc2NvcGUubG9jYWxpemF0aW9uID0gbG9jYWxpemF0aW9uXG4gICAgICAgICRzY29wZS5wZXJpb2RzID0gZnVuY3Rpb24oKSB7IHJldHVybiAkc2NvcGUuaGFrdSgpLmFwcGxpY2F0aW9uUGVyaW9kcyB9XG5cbiAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uUGVyaW9kU3RyaW5nID0gZnVuY3Rpb24oaW5kZXgpIHtcbiAgICAgICAgICBpZiAoJHNjb3BlLnBlcmlvZHMoKS5sZW5ndGggPT09IDEpXG4gICAgICAgICAgICByZXR1cm4gbG9jYWxpemF0aW9uKFwibGFiZWwuYXBwbGljYXRpb25QZXJpb2RcIilcbiAgICAgICAgICBlbHNlXG4gICAgICAgICAgICByZXR1cm4gKGluZGV4KzEpICsgXCIuIFwiICsgbG9jYWxpemF0aW9uKFwibGFiZWwuYXBwbGljYXRpb25QZXJpb2RcIikudG9Mb3dlckNhc2UoKVxuICAgICAgICB9XG5cbiAgICAgICAgJHNjb3BlLnN0YXR1c1N0cmluZyA9IGZ1bmN0aW9uKHBlcmlvZCkge1xuICAgICAgICAgIGlmIChwZXJpb2QuYWN0aXZlKVxuICAgICAgICAgICAgcmV0dXJuIGxvY2FsaXphdGlvbihcImxhYmVsLmFwcGxpY2F0aW9uUGVyaW9kQWN0aXZlXCIpXG4gICAgICAgICAgZWxzZSBpZiAocGVyaW9kLmVuZCA8IG5ldyBEYXRlKCkuZ2V0VGltZSgpKVxuICAgICAgICAgICAgcmV0dXJuIGxvY2FsaXphdGlvbihcImxhYmVsLmFwcGxpY2F0aW9uUGVyaW9kUGFzc2VkXCIpXG4gICAgICAgICAgZWxzZVxuICAgICAgICAgICAgcmV0dXJuIGxvY2FsaXphdGlvbihcImxhYmVsLmFwcGxpY2F0aW9uUGVyaW9kTm90U3RhcnRlZFwiKVxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICB9XSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy9hcHBsaWNhdGlvblBlcmlvZHMuanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwiY2FsbG91dFwiLCBmdW5jdGlvbiAoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIGxpbms6IGZ1bmN0aW9uIChzY29wZSwgZWxlbWVudCwgYXR0cnMpIHtcbiAgICAgICAgZWxlbWVudC5hZGRDbGFzcyhcImNhbGxvdXRcIilcbiAgICAgICAgZWxlbWVudC5wcmVwZW5kKCQoXCI8ZGl2Lz5cIiwge2NsYXNzOiBcImNhbGxvdXQtY2xvc2VcIn0pKVxuICAgICAgICBlbGVtZW50Lm9uKFwiY2xpY2tcIiwgXCIuY2FsbG91dC1jbG9zZVwiLCBmdW5jdGlvbiAoKSB7XG4gICAgICAgICAgZWxlbWVudC5mYWRlT3V0KFwiZmFzdFwiKVxuICAgICAgICB9KVxuICAgICAgICBzY29wZS4kb24oXCJzaG93LWNhbGxvdXRcIiwgZnVuY3Rpb24gKGV2dCwgY2FsbG91dElkLCB0b2dnbGUpIHtcbiAgICAgICAgICBpZiAoYXR0cnMuY2FsbG91dCA9PSBjYWxsb3V0SWQpIHtcbiAgICAgICAgICAgIGlmICh0b2dnbGUpXG4gICAgICAgICAgICAgIGVsZW1lbnQuZmFkZUluKFwiZmFzdFwiKVxuICAgICAgICAgICAgZWxzZVxuICAgICAgICAgICAgICBlbGVtZW50LmZhZGVPdXQoXCJmYXN0XCIpXG4gICAgICAgICAgfVxuICAgICAgICB9KVxuICAgICAgfVxuICAgIH1cbiAgfSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy9jYWxsb3V0LmpzXCIsXCIvZGlyZWN0aXZlc1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24obGlzdEFwcCkge1xuICBsaXN0QXBwLmRpcmVjdGl2ZShcImNsZWFyYWJsZUlucHV0XCIsIGZ1bmN0aW9uICgkcGFyc2UpIHtcbiAgICByZXR1cm4ge1xuICAgICAgbGluazogZnVuY3Rpb24gKHNjb3BlLCBlbGVtZW50LCBhdHRycykge1xuICAgICAgICB2YXIgd3JhcHBlciA9ICQoXCI8ZGl2Lz5cIiwgeyBjbGFzczogXCJjbGVhcmFibGUtaW5wdXRcIiB9KVxuICAgICAgICB2YXIgY2xlYXJCdG4gPSAkKFwiPGRpdi8+XCIsIHsgY2xhc3M6IFwiY2xlYXItYnRuXCIgfSlcbiAgICAgICAgdmFyIGNvbnRhaW5lciA9IGVsZW1lbnQud3JhcCh3cmFwcGVyKS5wYXJlbnQoKVxuICAgICAgICBjb250YWluZXIucHJlcGVuZChjbGVhckJ0bilcblxuICAgICAgICBjbGVhckJ0bi5vbihcImNsaWNrXCIsIGZ1bmN0aW9uKCkge1xuICAgICAgICAgIHNjb3BlLiRhcHBseShmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIHZhciBtb2RlbCA9ICRwYXJzZShhdHRycy5uZ01vZGVsKVxuICAgICAgICAgICAgbW9kZWwuYXNzaWduKHNjb3BlLCBcIlwiKVxuICAgICAgICAgIH0pXG4gICAgICAgIH0pXG5cbiAgICAgICAgc2NvcGUuJHdhdGNoKGF0dHJzLm5nTW9kZWwsIGZ1bmN0aW9uKHZhbCkge1xuICAgICAgICAgIGNsZWFyQnRuLnRvZ2dsZSh2YWwgJiYgKHZhbC5sZW5ndGggPiAwKSlcbiAgICAgICAgfSlcbiAgICAgIH1cbiAgICB9XG4gIH0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvY2xlYXJhYmxlSW5wdXQuanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwiY29uZmlybVwiLCBmdW5jdGlvbiAoKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHNjb3BlOiB7XG4gICAgICAgIGNhbGxiYWNrOiAnJmNvbmZpcm1BY3Rpb24nXG4gICAgICB9LFxuICAgICAgbGluazogZnVuY3Rpb24gKHNjb3BlLCBlbGVtZW50LCBhdHRycykge1xuICAgICAgICB2YXIgb3JpZ2luYWxUZXh0ID0gXCJcIlxuXG4gICAgICAgIGZ1bmN0aW9uIGNhbmNlbCgpIHtcbiAgICAgICAgICBlbGVtZW50LnJlbW92ZUNsYXNzKFwiY29uZmlybVwiKVxuICAgICAgICAgIGVsZW1lbnQudGV4dChvcmlnaW5hbFRleHQpXG4gICAgICAgICAgZWxlbWVudC5vZmYoXCIuY2FuY2VsQ29uZmlybVwiKVxuICAgICAgICAgICQoXCJib2R5XCIpLm9mZihcIi5jYW5jZWxDb25maXJtXCIpXG4gICAgICAgIH1cblxuICAgICAgICBlbGVtZW50Lm9uKFwiY2xpY2tcIiwgZnVuY3Rpb24gKCkge1xuICAgICAgICAgIGlmIChlbGVtZW50Lmhhc0NsYXNzKFwiY29uZmlybVwiKSkge1xuICAgICAgICAgICAgc2NvcGUuJGFwcGx5KHNjb3BlLmNhbGxiYWNrKVxuICAgICAgICAgICAgY2FuY2VsKClcbiAgICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgZWxlbWVudC5oaWRlKClcbiAgICAgICAgICAgIGVsZW1lbnQuYWRkQ2xhc3MoXCJjb25maXJtXCIpXG4gICAgICAgICAgICBvcmlnaW5hbFRleHQgPSBlbGVtZW50LnRleHQoKVxuICAgICAgICAgICAgZWxlbWVudC50ZXh0KGF0dHJzLmNvbmZpcm1UZXh0KVxuICAgICAgICAgICAgJChcImJvZHlcIikub25lKFwiY2xpY2suY2FuY2VsQ29uZmlybVwiLCBjYW5jZWwpXG4gICAgICAgICAgICBlbGVtZW50LmZhZGVJbigxMDApXG4gICAgICAgICAgfVxuICAgICAgICAgIHJldHVybiBmYWxzZVxuICAgICAgICB9KVxuICAgICAgfVxuICAgIH1cbiAgfSlcbn1cblxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi9kaXJlY3RpdmVzL2NvbmZpcm0uanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwiZGlzYWJsZUNsaWNrRm9jdXNcIiwgZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIGxpbms6IGZ1bmN0aW9uIChzY29wZSwgZWxlbWVudCkge1xuICAgICAgICBlbGVtZW50Lm9uKFwibW91c2Vkb3duXCIsIGZ1bmN0aW9uKGV2ZW50KSB7XG4gICAgICAgICAgZXZlbnQucHJldmVudERlZmF1bHQoKVxuICAgICAgICB9KVxuICAgICAgfVxuICAgIH1cbiAgfSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy9kaXNhYmxlQ2xpY2tGb2N1cy5qc1wiLFwiL2RpcmVjdGl2ZXNcIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgbGlzdEFwcC5kaXJlY3RpdmUoXCJmb3JtYXR0ZWRUaW1lXCIsIFtcIiRzY2VcIiwgXCJsb2NhbGl6YXRpb25cIiwgZnVuY3Rpb24gKCRzY2UsIGxvY2FsaXphdGlvbikge1xuICAgIHJldHVybiB7XG4gICAgICByZXN0cmljdDogXCJBXCIsXG4gICAgICB0ZW1wbGF0ZTogXCJcIixcbiAgICAgIHNjb3BlOiB7XG4gICAgICAgIGZvcm1hdHRlZFRpbWU6IFwiPWZvcm1hdHRlZFRpbWVcIlxuICAgICAgfSxcbiAgICAgIGxpbms6IGZ1bmN0aW9uIChzY29wZSwgZWxlbWVudCwgYXR0cnMpIHtcbiAgICAgICAgc2NvcGUuJHdhdGNoKFwiZm9ybWF0dGVkVGltZVwiLCBmdW5jdGlvbihkdCkge1xuICAgICAgICAgIGlmIChkdCA9PSBudWxsKSB7XG4gICAgICAgICAgICByZXR1cm4gXCJcIlxuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICB2YXIgbSA9IG1vbWVudChkdClcbiAgICAgICAgICAgIGVsZW1lbnQuYXR0cihcImRhdGV0aW1lXCIsIG0uZm9ybWF0KCkpXG4gICAgICAgICAgICBlbGVtZW50LnRleHQobS5mb3JtYXQoYXR0cnMuZm9ybWF0IHx8ICdMTExMJykucmVwbGFjZSgvLC9nLCBcIlwiKSlcbiAgICAgICAgICB9XG4gICAgICAgIH0pXG4gICAgICB9XG4gICAgfVxuICB9XSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy9mb3JtYXR0ZWRUaW1lLmpzXCIsXCIvZGlyZWN0aXZlc1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbm1vZHVsZS5leHBvcnRzID0gZnVuY3Rpb24obGlzdEFwcCkge1xuICBsaXN0QXBwLmRpcmVjdGl2ZShcImhha3V0b2l2ZWVuVmFzdGFhbm90dG9cIiwgW1wibG9jYWxpemF0aW9uXCIsIFwicmVzdFJlc291cmNlc1wiLCBmdW5jdGlvbiAobG9jYWxpemF0aW9uLCByZXN0UmVzb3VyY2VzKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHJlc3RyaWN0OiAnRScsXG4gICAgICBzY29wZToge1xuICAgICAgICBhcHBsaWNhdGlvbk9pZDogJyZhcHBsaWNhdGlvbk9pZCcsXG4gICAgICAgIGhha3U6ICcmaGFrdScsXG4gICAgICAgIGhha3V0b2l2ZWV0OiAnJmhha3V0b2l2ZWV0JyxcbiAgICAgICAgY2FsbGJhY2s6ICc9Y2FsbGJhY2snXG4gICAgICB9LFxuICAgICAgdGVtcGxhdGVVcmw6ICd0ZW1wbGF0ZXMvaGFrdXRvaXZlZW5WYXN0YWFub3R0by5odG1sJyxcbiAgICAgIGxpbms6IGZ1bmN0aW9uIChzY29wZSwgZWxlbWVudCwgYXR0cnMpIHtcbiAgICAgICAgc2NvcGUudmFzdGFhbm90dG90aWxhID0gXCJcIlxuICAgICAgICBzY29wZS5sb2NhbGl6YXRpb24gPSBsb2NhbGl6YXRpb25cbiAgICAgICAgc2NvcGUuYWpheFBlbmRpbmcgPSBmYWxzZVxuICAgICAgICBzY29wZS5lcnJvciA9IFwiXCJcblxuICAgICAgICBzY29wZS5mb3JtYXRUaW1lc3RhbXAgPSBmdW5jdGlvbihkdCkge1xuICAgICAgICAgICAgcmV0dXJuIG1vbWVudChkdCkuZm9ybWF0KCdMTEwnKS5yZXBsYWNlKC8sL2csIFwiXCIpXG4gICAgICAgIH1cblxuICAgICAgICBzY29wZS52YXN0YWFub3RhSGFrdXRvaXZlID0gZnVuY3Rpb24oaGFrdXRvaXZlKSB7XG4gICAgICAgICAgdmFyIHNjb3BlID0gdGhpc1xuICAgICAgICAgIHNjb3BlLmVycm9yID0gXCJcIlxuICAgICAgICAgIHNjb3BlLmFqYXhQZW5kaW5nID0gdHJ1ZVxuXG4gICAgICAgICAgcmVzdFJlc291cmNlcy52YXN0YWFub3RhLnBvc3Qoe2FwcGxpY2F0aW9uT2lkOiBzY29wZS5hcHBsaWNhdGlvbk9pZCgpLCBoYWt1T2lkOiBzY29wZS5oYWt1KCkub2lkfSwge1xuICAgICAgICAgICAgaGFrdWtvaGRlT2lkOiBoYWt1dG9pdmUua291bHV0dXMub2lkLFxuICAgICAgICAgICAgdGlsYTogdGhpcy52YXN0YWFub3R0b3RpbGFcbiAgICAgICAgICB9LCBvblN1Y2Nlc3MsIG9uRXJyb3IpXG5cbiAgICAgICAgICBmdW5jdGlvbiBvblN1Y2Nlc3ModXBkYXRlZEFwcGxpY2F0aW9uKSB7XG4gICAgICAgICAgICBzY29wZS5hamF4UGVuZGluZyA9IGZhbHNlXG4gICAgICAgICAgICBzY29wZS5lcnJvciA9IFwiXCJcbiAgICAgICAgICAgIHNjb3BlLmNhbGxiYWNrKGhha3V0b2l2ZSwgdXBkYXRlZEFwcGxpY2F0aW9uKVxuICAgICAgICAgIH1cblxuICAgICAgICAgIGZ1bmN0aW9uIG9uRXJyb3IoZXJyKSB7XG4gICAgICAgICAgICB2YXIgc2F2ZUVycm9yID0gKGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgIGlmIChlcnIuc3RhdHVzID09IDQwMSlcbiAgICAgICAgICAgICAgICByZXR1cm4gXCJlcnJvci5zYXZlRmFpbGVkX3Nlc3Npb25FeHBpcmVkXCJcbiAgICAgICAgICAgICAgZWxzZSBpZiAoZXJyLnN0YXR1cyA9PSA1MDApXG4gICAgICAgICAgICAgICAgcmV0dXJuIFwiZXJyb3Iuc2VydmVyRXJyb3JcIlxuICAgICAgICAgICAgICBlbHNlXG4gICAgICAgICAgICAgICAgcmV0dXJuIFwiZXJyb3Iuc2F2ZUZhaWxlZFwiXG4gICAgICAgICAgICB9KSgpXG4gICAgICAgICAgICBzY29wZS5lcnJvciA9IGxvY2FsaXphdGlvbihzYXZlRXJyb3IpXG4gICAgICAgICAgICBzY29wZS5hamF4UGVuZGluZyA9IGZhbHNlXG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICB9XSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy9oYWt1dG9pdmVlblZhc3RhYW5vdHRvLmpzXCIsXCIvZGlyZWN0aXZlc1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbnZhciBIYWt1dG9pdmUgPSByZXF1aXJlKCcuLi9oYWt1dG9pdmUnKVxuXG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgbGlzdEFwcC5kaXJlY3RpdmUoXCJoYWt1dG9pdmVldFwiLCBbXCJsb2NhbGl6YXRpb25cIiwgZnVuY3Rpb24gKGxvY2FsaXphdGlvbikge1xuICAgIHJldHVybiB7XG4gICAgICByZXN0cmljdDogJ0UnLFxuICAgICAgc2NvcGU6IHtcbiAgICAgICAgYXBwbGljYXRpb246IFwiPWFwcGxpY2F0aW9uXCIsXG4gICAgICAgIHByZWZlcmVuY2VNb3ZlZDogXCI9cHJlZmVyZW5jZU1vdmVkXCIsXG4gICAgICAgIHZhbGlkYXRpbmc6IFwiPXZhbGlkYXRpbmdcIlxuICAgICAgfSxcbiAgICAgIHRlbXBsYXRlVXJsOiAndGVtcGxhdGVzL2hha3V0b2l2ZWV0Lmh0bWwnLFxuXG4gICAgICBsaW5rOiBmdW5jdGlvbiAoJHNjb3BlLCBlbGVtZW50LCBhdHRycykge1xuICAgICAgICAkc2NvcGUubG9jYWxpemF0aW9uID0gbG9jYWxpemF0aW9uXG5cbiAgICAgICAgJHNjb3BlLm1vdmVQcmVmZXJlbmNlID0gZnVuY3Rpb24oZnJvbSwgdG8pIHtcbiAgICAgICAgICBpZiAodG8gPj0gMCAmJiB0byA8IHRoaXMuYXBwbGljYXRpb24uaGFrdXRvaXZlZXQubGVuZ3RoKSB7XG4gICAgICAgICAgICB0aGlzLmFwcGxpY2F0aW9uLm1vdmVQcmVmZXJlbmNlKGZyb20sIHRvKVxuICAgICAgICAgICAgdGhpcy5wcmVmZXJlbmNlTW92ZWQoKVxuICAgICAgICAgIH1cbiAgICAgICAgfVxuICAgICAgfVxuICAgIH1cbiAgfV0pXG5cbiAgbGlzdEFwcC5jb250cm9sbGVyKFwiYWRkaXRpb25hbFF1ZXN0aW9uQ29udHJvbGxlclwiLCBbXCIkc2NvcGVcIiwgXCJsb2NhbGl6YXRpb25cIiwgZnVuY3Rpb24oJHNjb3BlLCBsb2NhbGl6YXRpb24pIHtcbiAgICAkc2NvcGUubG9jYWxpemF0aW9uID0gbG9jYWxpemF0aW9uXG4gICAgJHNjb3BlLnF1ZXN0aW9uQW5zd2VyZWQgPSBmdW5jdGlvbigpIHtcbiAgICAgICRzY29wZS4kZW1pdChcInF1ZXN0aW9uQW5zd2VyZWRcIilcbiAgICB9XG4gIH1dKVxuXG4gIGxpc3RBcHAuY29udHJvbGxlcihcImhha3V0b2l2ZUNvbnRyb2xsZXJcIiwgW1wiJHNjb3BlXCIsIFwiJGh0dHBcIiwgXCIkdGltZW91dFwiLCBcInNldHRpbmdzXCIsIFwicmVzdFJlc291cmNlc1wiLCBmdW5jdGlvbigkc2NvcGUsICRodHRwLCAkdGltZW91dCwgc2V0dGluZ3MsIHJlc3RSZXNvdXJjZXMpIHtcbiAgICAkc2NvcGUuaXNFZGl0aW5nRGlzYWJsZWQgPSBmdW5jdGlvbigpIHsgcmV0dXJuICEkc2NvcGUuaGFrdXRvaXZlLmlzTmV3IHx8ICEkc2NvcGUuYXBwbGljYXRpb24uaXNFZGl0YWJsZSgkc2NvcGUuJGluZGV4KSB9XG5cbiAgICAkc2NvcGUuaXNLb3VsdXR1c1NlbGVjdGFibGUgPSBmdW5jdGlvbigpIHsgcmV0dXJuICEkc2NvcGUuaXNFZGl0aW5nRGlzYWJsZWQoKSAmJiB0aGlzLmhha3V0b2l2ZS5oYXNPcGV0dXNwaXN0ZSgpICYmICFfLmlzRW1wdHkoJHNjb3BlLmtvdWx1dHVzTGlzdCkgfVxuXG4gICAgJHNjb3BlLmlzTG9hZGluZ0tvdWx1dHVzTGlzdCA9IGZ1bmN0aW9uKCkgeyByZXR1cm4gISRzY29wZS5pc0VkaXRpbmdEaXNhYmxlZCgpICYmIHRoaXMuaGFrdXRvaXZlLmhhc09wZXR1c3Bpc3RlKCkgJiYgXy5pc0VtcHR5KCRzY29wZS5rb3VsdXR1c0xpc3QpIH1cblxuICAgICRzY29wZS5pc05vblByaW9yaXRpemVkQW5kRWRpdGFibGUgPSBmdW5jdGlvbiAoKSB7IHJldHVybiB0aGlzLmFwcGxpY2F0aW9uLmhha3UudXNlUHJpb3JpdHkgfHwgJHNjb3BlLmFwcGxpY2F0aW9uLmlzRWRpdGFibGUoJHNjb3BlLiRpbmRleCkgfVxuXG4gICAgJHNjb3BlLm9wZXR1c3Bpc3RlVmFsaXR0dSA9IGZ1bmN0aW9uKCRpdGVtLCAkbW9kZWwsICRsYWJlbCkge1xuICAgICAgdGhpcy5oYWt1dG9pdmUuc2V0T3BldHVzcGlzdGUoJGl0ZW0uaWQsICRpdGVtLm5hbWUpXG4gICAgICAkc2NvcGUua291bHV0dXNMaXN0ID0gW11cblxuICAgICAgcmVzdFJlc291cmNlcy5rb3VsdXR1a3NldC5xdWVyeSh7XG4gICAgICAgIGFzSWQ6IHRoaXMuYXBwbGljYXRpb24uaGFrdS5vaWQsXG4gICAgICAgIG9wZXR1c3Bpc3RlSWQ6ICRpdGVtLmlkLFxuICAgICAgICBiYXNlRWR1Y2F0aW9uOiB0aGlzLmFwcGxpY2F0aW9uLmVkdWNhdGlvbkJhY2tncm91bmQuYmFzZUVkdWNhdGlvbixcbiAgICAgICAgdm9jYXRpb25hbDogdGhpcy5hcHBsaWNhdGlvbi5lZHVjYXRpb25CYWNrZ3JvdW5kLnZvY2F0aW9uYWwsXG4gICAgICAgIHVpTGFuZzogJHNjb3BlLmxvY2FsaXphdGlvbihcImxhbmd1YWdlSWRcIilcbiAgICAgIH0sIGZ1bmN0aW9uKGtvdWx1dHVrc2V0KSB7XG4gICAgICAgICRzY29wZS5rb3VsdXR1c0xpc3QgPSBrb3VsdXR1a3NldFxuICAgICAgICBpZiAoa291bHV0dWtzZXQubGVuZ3RoID09PSAxKSB7XG4gICAgICAgICAgJHNjb3BlLnZhbGl0dHVLb3VsdXR1cyA9IGtvdWx1dHVrc2V0WzBdXG4gICAgICAgICAgJHNjb3BlLmhha3V0b2l2ZS5zZXRLb3VsdXR1cyhrb3VsdXR1a3NldFswXSlcbiAgICAgICAgfVxuICAgICAgfSlcbiAgICB9XG5cbiAgICAkc2NvcGUub3BldHVzcGlzdGVNb2RpZmllZCA9IGZ1bmN0aW9uKCkge1xuICAgICAgaWYgKF8uaXNFbXB0eSh0aGlzLmhha3V0b2l2ZS5kYXRhLk9wZXR1c3Bpc3RlKSlcbiAgICAgICAgdGhpcy5oYWt1dG9pdmUuY2xlYXIoKVxuICAgICAgZWxzZVxuICAgICAgICB0aGlzLmhha3V0b2l2ZS5yZW1vdmVPcGV0dXNwaXN0ZURhdGEoKVxuICAgIH1cblxuICAgICRzY29wZS5yZW1vdmVIYWt1dG9pdmUgPSBmdW5jdGlvbihpbmRleCkge1xuICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLnJlbW92ZVByZWZlcmVuY2UoaW5kZXgpXG4gICAgICAkc2NvcGUuYXBwbGljYXRpb24uYWRkUHJlZmVyZW5jZShuZXcgSGFrdXRvaXZlKHt9KSlcbiAgICB9XG5cbiAgICAkc2NvcGUuY2FuUmVtb3ZlUHJlZmVyZW5jZSA9IGZ1bmN0aW9uKGluZGV4KSB7XG4gICAgICByZXR1cm4gJHNjb3BlLmFwcGxpY2F0aW9uLmhhc1ByZWZlcmVuY2UoaW5kZXgpICYmICEkc2NvcGUuYXBwbGljYXRpb24ucHJlZmVyZW5jZUxvY2tlZChpbmRleClcbiAgICB9XG5cbiAgICAkc2NvcGUua291bHV0dXNWYWxpdHR1ID0gZnVuY3Rpb24oaW5kZXgpIHtcbiAgICAgIHRoaXMuaGFrdXRvaXZlLnNldEtvdWx1dHVzKHRoaXNbXCJ2YWxpdHR1S291bHV0dXNcIl0pXG4gICAgfVxuXG4gICAgJHNjb3BlLmZpbmRPcGV0dXNwaXN0ZSA9IGZ1bmN0aW9uKHZhbCkge1xuICAgICAgcmV0dXJuIHJlc3RSZXNvdXJjZXMub3BldHVzcGlzdGVldC5xdWVyeSh7XG4gICAgICAgIHF1ZXJ5OiB2YWwsXG4gICAgICAgIGFzSWQ6ICRzY29wZS5hcHBsaWNhdGlvbi5oYWt1Lm9pZCxcbiAgICAgICAgbGFuZzogJHNjb3BlLmxvY2FsaXphdGlvbihcImxhbmd1YWdlSWRcIilcbiAgICAgIH0pLiRwcm9taXNlXG4gICAgfTtcbiAgfV0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvaGFrdXRvaXZlZXQuanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwiaGVua2lsb3RpZWRvdFwiLCBbXCJsb2NhbGl6YXRpb25cIiwgXCJyZXN0UmVzb3VyY2VzXCIsIFwiZGVib3VuY2VcIiwgXCJzZXR0aW5nc1wiLCBcImFuZ3VsYXJCYWNvblwiLCBmdW5jdGlvbiAobG9jYWxpemF0aW9uLCByZXN0UmVzb3VyY2VzLCBkZWJvdW5jZSwgc2V0dGluZ3MsIGFuZ3VsYXJCYWNvbikge1xuICAgIHJldHVybiB7XG4gICAgICByZXN0cmljdDogJ0UnLFxuICAgICAgc2NvcGU6IHtcbiAgICAgICAgYXBwbGljYXRpb246ICc9YXBwbGljYXRpb24nXG4gICAgICB9LFxuICAgICAgdGVtcGxhdGVVcmw6ICd0ZW1wbGF0ZXMvaGVua2lsb3RpZWRvdC5odG1sJyxcbiAgICAgIGxpbms6IGZ1bmN0aW9uICgkc2NvcGUsIGVsZW1lbnQsIGF0dHJzKSB7XG4gICAgICAgICRzY29wZS5sb2NhbGl6YXRpb24gPSBsb2NhbGl6YXRpb25cbiAgICAgICAgJHNjb3BlLnlodGV5c3RpZWRvdCA9ICRzY29wZS5hcHBsaWNhdGlvbi5oZW5raWxvdGllZG90XG5cbiAgICAgICAgdmFyIHBvc3RhbENvZGUgPSBhbmd1bGFyQmFjb24ud2F0Y2goJHNjb3BlLCBcInlodGV5c3RpZWRvdFsnUG9zdGludW1lcm8nXS5hbnN3ZXJcIikuZGVib3VuY2Uoc2V0dGluZ3MubW9kZWxEZWJvdW5jZSkuc2tpcER1cGxpY2F0ZXMoKS5tYXAoXCIudHJpbVwiKVxuICAgICAgICB2YXIgcXVlcnkgPSBhbmd1bGFyQmFjb24ucmVzb3VyY2UocmVzdFJlc291cmNlcy5wb3N0T2ZmaWNlLmdldClcblxuICAgICAgICBmdW5jdGlvbiBsZW5ndGgobGVuKSB7IHJldHVybiBmdW5jdGlvbihzdHIpIHsgcmV0dXJuIHN0ci5sZW5ndGggPT09IGxlbiB9IH1cbiAgICAgICAgZnVuY3Rpb24gbm90KGYpIHsgcmV0dXJuIGZ1bmN0aW9uKHZhbCkgeyByZXR1cm4gIWYodmFsKSB9fVxuXG4gICAgICAgIHZhciByZXNwb25zZXMgPSBwb3N0YWxDb2RlLmZpbHRlcihsZW5ndGgoNSkpXG4gICAgICAgICAgLmZsYXRNYXBMYXRlc3QoZnVuY3Rpb24oY29kZSkge1xuICAgICAgICAgICAgcmV0dXJuIHF1ZXJ5KHtwb3N0YWxDb2RlOiBjb2RlIH0pXG4gICAgICAgICAgfSlcblxuICAgICAgICByZXNwb25zZXMub25WYWx1ZShmdW5jdGlvbihyZXNwb25zZSkge1xuICAgICAgICAgICRzY29wZS5hcHBsaWNhdGlvbi5jYWxjdWxhdGVkVmFsdWVzLnBvc3RPZmZpY2UgPSByZXNwb25zZS5wb3N0T2ZmaWNlXG4gICAgICAgICAgJHNjb3BlLnlodGV5c3RpZWRvdC5Qb3N0aW51bWVyby5zZXRFcnJvcnMoW10pXG4gICAgICAgIH0pXG5cbiAgICAgICAgcmVzcG9uc2VzLm9uRXJyb3IoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLmNhbGN1bGF0ZWRWYWx1ZXMucG9zdE9mZmljZSA9IFwiXCJcbiAgICAgICAgfSlcblxuICAgICAgICBwb3N0YWxDb2RlLmZpbHRlcihub3QobGVuZ3RoKDUpKSkub25WYWx1ZShmdW5jdGlvbigpIHtcbiAgICAgICAgICAkc2NvcGUuJGFwcGx5KGZ1bmN0aW9uKCkge1xuICAgICAgICAgICAgJHNjb3BlLmFwcGxpY2F0aW9uLmNhbGN1bGF0ZWRWYWx1ZXMucG9zdE9mZmljZSA9IFwiXCJcbiAgICAgICAgICB9KVxuICAgICAgICB9KVxuICAgICAgfVxuICAgIH1cbiAgfV0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvaGVua2lsb3RpZWRvdC5qc1wiLFwiL2RpcmVjdGl2ZXNcIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgbGlzdEFwcC5kaXJlY3RpdmUoXCJpbG1vaXR0YXV0dW1pbmVuXCIsIFtcImxvY2FsaXphdGlvblwiLCBmdW5jdGlvbiAobG9jYWxpemF0aW9uKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHJlc3RyaWN0OiAnRScsXG4gICAgICBzY29wZToge1xuICAgICAgICBoYWt1a29odGVldDogJyZoYWt1a29odGVldCdcbiAgICAgIH0sXG4gICAgICB0ZW1wbGF0ZVVybDogJ3RlbXBsYXRlcy9pbG1vaXR0YXV0dW1pbmVuLmh0bWwnLFxuICAgICAgbGluazogZnVuY3Rpb24gKHNjb3BlLCBlbGVtZW50LCBhdHRycykge1xuICAgICAgICBzY29wZS5sb2NhbGl6YXRpb24gPSBsb2NhbGl6YXRpb25cbiAgICAgIH1cbiAgICB9XG4gIH1dKVxufVxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi9kaXJlY3RpdmVzL2lsbW9pdHRhdXR1bWluZW4uanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwibG9jYWxpemVkTGlua1wiLCBbXCIkc2NlXCIsIFwibG9jYWxpemF0aW9uXCIsIGZ1bmN0aW9uICgkc2NlLCBsb2NhbGl6YXRpb24pIHtcbiAgICByZXR1cm4ge1xuICAgICAgcmVzdHJpY3Q6IFwiRVwiLFxuICAgICAgdGVtcGxhdGU6IFwiXCIsXG4gICAgICBsaW5rOiBmdW5jdGlvbiAoc2NvcGUsIGVsZW1lbnQsIGF0dHJzKSB7XG4gICAgICAgIHZhciBsaW5rU3RyaW5nID0gbG9jYWxpemF0aW9uKGF0dHJzW1wia2V5XCJdKVxuICAgICAgICB2YXIgbGluayA9ICQoXCI8c3Bhbj5cIiArIGxpbmtTdHJpbmcgKyBcIjwvc3Bhbj5cIilcbiAgICAgICAgICAuZmluZChcImFcIilcbiAgICAgICAgICAuYXR0cihcImhyZWZcIiwgYXR0cnNbXCJocmVmXCJdKVxuICAgICAgICAgIC5hdHRyKFwidGFyZ2V0XCIsIGF0dHJzW1widGFyZ2V0XCJdKVxuICAgICAgICAgIC5lbmQoKVxuICAgICAgICBlbGVtZW50LmFwcGVuZChsaW5rKVxuICAgICAgfVxuICAgIH1cbiAgfV0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvbG9jYWxpemVkTGluay5qc1wiLFwiL2RpcmVjdGl2ZXNcIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgbGlzdEFwcC5kaXJlY3RpdmUoXCJxdWVzdGlvblwiLCBmdW5jdGlvbiAoUmVjdXJzaW9uSGVscGVyKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIHJlc3RyaWN0OiAnRScsXG4gICAgICBzY29wZToge1xuICAgICAgICBxdWVzdGlvbk5vZGU6ICc9cXVlc3Rpb25Ob2RlJyxcbiAgICAgICAgYXBwbGljYXRpb246ICc9YXBwbGljYXRpb24nLFxuICAgICAgICBsZXZlbDogJz1sZXZlbCdcbiAgICAgIH0sXG4gICAgICB0ZW1wbGF0ZVVybDogJ3RlbXBsYXRlcy9xdWVzdGlvbi5odG1sJyxcbiAgICAgIGNvbXBpbGU6IGZ1bmN0aW9uIChlbGVtZW50KSB7XG4gICAgICAgIHJldHVybiBSZWN1cnNpb25IZWxwZXIuY29tcGlsZShlbGVtZW50LCBmdW5jdGlvbiAoJHNjb3BlLCBpRWxlbWVudCwgaUF0dHJzLCBjb250cm9sbGVyLCB0cmFuc2NsdWRlRm4pIHtcbiAgICAgICAgICAkc2NvcGUuaXNHcm91cCA9IGZ1bmN0aW9uICgpIHtcbiAgICAgICAgICAgIHJldHVybiAkc2NvcGUucXVlc3Rpb25Ob2RlICYmICFfLmlzRW1wdHkoJHNjb3BlLnF1ZXN0aW9uTm9kZS5xdWVzdGlvbk5vZGVzKVxuICAgICAgICAgIH1cbiAgICAgICAgfSlcbiAgICAgIH1cbiAgICB9XG4gIH0pXG59XG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2RpcmVjdGl2ZXMvcXVlc3Rpb24uanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKCdzb3J0YWJsZScsIFtcInNldHRpbmdzXCIsIGZ1bmN0aW9uKHNldHRpbmdzKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKCRzY29wZSwgJGVsZW1lbnQsIGF0dHJzKSB7XG4gICAgICB2YXIgc2xpZGUgPSBmdW5jdGlvbihlbCwgb2Zmc2V0KSB7XG4gICAgICAgIGVsLmNzcyhcInRyYW5zaXRpb25cIiwgXCJhbGwgMC41c1wiKVxuICAgICAgICBlbC5jc3MoXCJ0cmFuc2Zvcm1cIiwgXCJ0cmFuc2xhdGUzZCgwcHgsIFwiICsgb2Zmc2V0ICsgXCJweCwgMHB4KVwiKVxuICAgICAgfVxuXG4gICAgICB2YXIgcmVzZXRTbGlkZSA9IGZ1bmN0aW9uKGVsKSB7XG4gICAgICAgIGVsLmNzcyh7XG4gICAgICAgICAgXCJ0cmFuc2l0aW9uXCI6IFwiXCIsXG4gICAgICAgICAgXCJ0cmFuc2Zvcm1cIjogXCJcIlxuICAgICAgICB9KVxuICAgICAgfVxuXG4gICAgICB2YXIgc3dpdGNoUGxhY2VzID0gZnVuY3Rpb24oZWxlbWVudDEsIGVsZW1lbnQyKSB7XG4gICAgICAgIHZhciBkaWZmWSA9IE1hdGguYWJzKGVsZW1lbnQxLm9mZnNldCgpLnRvcCAtIGVsZW1lbnQyLm9mZnNldCgpLnRvcClcblxuICAgICAgICBpZiAoZWxlbWVudDEuaW5kZXgoKSA8IGVsZW1lbnQyLmluZGV4KCkpIHtcbiAgICAgICAgICBzbGlkZShlbGVtZW50MSwgZWxlbWVudDIub3V0ZXJIZWlnaHQoKSArIGRpZmZZLWVsZW1lbnQxLm91dGVySGVpZ2h0KCkpXG4gICAgICAgICAgc2xpZGUoZWxlbWVudDIsIC1kaWZmWSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBzbGlkZShlbGVtZW50MiwgZWxlbWVudDEub3V0ZXJIZWlnaHQoKSArIGRpZmZZLWVsZW1lbnQyLm91dGVySGVpZ2h0KCkpXG4gICAgICAgICAgc2xpZGUoZWxlbWVudDEsIC1kaWZmWSlcbiAgICAgICAgfVxuXG4gICAgICAgIHNldFRpbWVvdXQoZnVuY3Rpb24oKSB7XG4gICAgICAgICAgJHNjb3BlLiRhcHBseShmdW5jdGlvbihzZWxmKSB7XG4gICAgICAgICAgICB2YXIgaXRlbXMgPSAkZWxlbWVudC5maW5kKGF0dHJzLnNvcnRhYmxlSXRlbSlcbiAgICAgICAgICAgIHNlbGZbYXR0cnMuc29ydGFibGVNb3ZlZF0oaXRlbXMuaW5kZXgoZWxlbWVudDEpLCBpdGVtcy5pbmRleChlbGVtZW50MikpXG4gICAgICAgICAgICByZXNldFNsaWRlKGVsZW1lbnQxKVxuICAgICAgICAgICAgcmVzZXRTbGlkZShlbGVtZW50MilcbiAgICAgICAgICB9KVxuICAgICAgICB9LCBzZXR0aW5ncy51aVRyYW5zaXRpb25UaW1lKVxuICAgICAgfVxuXG4gICAgICB2YXIgYXJyb3dDbGlja2VkID0gZnVuY3Rpb24oZWxlbWVudEYpIHtcbiAgICAgICAgcmV0dXJuIGZ1bmN0aW9uKGV2dCkge1xuICAgICAgICAgIHZhciBidG4gPSAkKGV2dC50YXJnZXQpXG4gICAgICAgICAgaWYgKCFidG4uaGFzQ2xhc3MoXCJkaXNhYmxlZFwiKSkge1xuICAgICAgICAgICAgdmFyIGVsZW1lbnQxID0gYnRuLmNsb3Nlc3QoYXR0cnMuc29ydGFibGVJdGVtKVxuICAgICAgICAgICAgdmFyIGVsZW1lbnQyID0gZWxlbWVudDFbZWxlbWVudEZdKClcbiAgICAgICAgICAgIHN3aXRjaFBsYWNlcyhlbGVtZW50MSwgZWxlbWVudDIpXG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9XG5cbiAgICAgICRlbGVtZW50Lm9uKFwiY2xpY2tcIiwgXCIuc29ydC1hcnJvdy1kb3duXCIsIGFycm93Q2xpY2tlZChcIm5leHRcIikpXG4gICAgICAkZWxlbWVudC5vbihcImNsaWNrXCIsIFwiLnNvcnQtYXJyb3ctdXBcIiwgYXJyb3dDbGlja2VkKFwicHJldlwiKSlcbiAgICB9XG4gIH1dKVxufVxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi9kaXJlY3RpdmVzL3NvcnRhYmxlLmpzXCIsXCIvZGlyZWN0aXZlc1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbnZhciB1dGlsID0gcmVxdWlyZShcIi4uL3V0aWxcIilcblxubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIGxpc3RBcHAuZGlyZWN0aXZlKFwidmFsaW50YXR1bG9zXCIsIFtcImxvY2FsaXphdGlvblwiLCBcInJlc3RSZXNvdXJjZXNcIiwgXCJzZXR0aW5nc1wiLCBmdW5jdGlvbiAobG9jYWxpemF0aW9uLCByZXN0UmVzb3VyY2VzLCBzZXR0aW5ncykge1xuICAgIHJldHVybiB7XG4gICAgICByZXN0cmljdDogJ0UnLFxuICAgICAgc2NvcGU6IHtcbiAgICAgICAgdmFsaW50YXR1bG9zOiAnJmRhdGEnLFxuICAgICAgICBpc0ZpbmFsOiAnJmZpbmFsJ1xuICAgICAgfSxcbiAgICAgIHRlbXBsYXRlVXJsOiAndGVtcGxhdGVzL3ZhbGludGF0dWxvcy5odG1sJyxcbiAgICAgIGxpbms6IGZ1bmN0aW9uICgkc2NvcGUsIGVsZW1lbnQsIGF0dHJzKSB7XG4gICAgICAgICRzY29wZS5sb2NhbGl6YXRpb24gPSBsb2NhbGl6YXRpb25cblxuICAgICAgICAkc2NvcGUuZm9ybWF0RGF0ZSA9IGZ1bmN0aW9uKGR0KSB7XG4gICAgICAgICAgaWYgKGR0ID09IG51bGwpXG4gICAgICAgICAgICByZXR1cm4gXCJcIlxuICAgICAgICAgIGVsc2VcbiAgICAgICAgICAgIHJldHVybiBtb21lbnQoZHQpLmZvcm1hdCgnTEwnKS5yZXBsYWNlKC8sL2csIFwiXCIpXG4gICAgICAgIH1cblxuICAgICAgICAkc2NvcGUuJHdhdGNoKFwiaXNGaW5hbCgpXCIsIGZ1bmN0aW9uKHZhbHVlKSB7XG4gICAgICAgICAgJHNjb3BlLnN0YXR1cyA9IHZhbHVlID8gbG9jYWxpemF0aW9uKFwibGFiZWwucmVzdWx0c0ZpbmFsXCIpIDogbG9jYWxpemF0aW9uKFwibGFiZWwucmVzdWx0c1BlbmRpbmdcIilcbiAgICAgICAgfSlcblxuICAgICAgICAkc2NvcGUuJG9uKFwiaGFrdXRvaXZlLXZhc3RhYW5vdGV0dHVcIiwgZnVuY3Rpb24oZSwgaGFrdXRvaXZlKSB7XG4gICAgICAgICAgdmFyIGl0ZW0gPSAkKF8oZWxlbWVudC5maW5kKFwidGJvZHkgdHJcIikpLmZpbmQoZnVuY3Rpb24odHIpIHtcbiAgICAgICAgICAgIHJldHVybiBhbmd1bGFyLmVsZW1lbnQodHIpLnNjb3BlKCkudHVsb3Mua291bHV0dXMub2lkID09PSBoYWt1dG9pdmUua291bHV0dXMub2lkXG4gICAgICAgICAgfSkpXG5cbiAgICAgICAgICBpdGVtLmNzcyh7IFwib3BhY2l0eVwiOiAwIH0pXG5cbiAgICAgICAgICB3aW5kb3cuc2V0VGltZW91dChmdW5jdGlvbigpIHtcbiAgICAgICAgICAgIGl0ZW0uYW5pbWF0ZSh7XCJvcGFjaXR5XCI6IDF9LCBzZXR0aW5ncy51aVRyYW5zaXRpb25UaW1lKVxuICAgICAgICAgIH0sIHNldHRpbmdzLnVpVHJhbnNpdGlvblRpbWUqMilcbiAgICAgICAgfSlcblxuICAgICAgICAkc2NvcGUudmFsaW50YXR1bG9zVGV4dCA9IGZ1bmN0aW9uKHZhbGludGF0dWxvcykge1xuICAgICAgICAgIHZhciBrZXkgPSB1dGlsLnVuZGVyc2NvcmVUb0NhbWVsQ2FzZSh2YWxpbnRhdHVsb3MudGlsYSlcbiAgICAgICAgICBpZiAoW1wiVkFTVEFBTk9UVEFOVVRcIiwgXCJFSV9WQVNUQUFOT1RFVFRVX01BQVJBX0FJS0FOQVwiLCBcIkVIRE9MTElTRVNUSV9WQVNUQUFOT1RUQU5VVFwiXS5pbmRleE9mKHZhbGludGF0dWxvcy52YXN0YWFub3R0b3RpbGEpID49IDApIHtcbiAgICAgICAgICAgIGtleSA9IHV0aWwudW5kZXJzY29yZVRvQ2FtZWxDYXNlKHZhbGludGF0dWxvcy52YXN0YWFub3R0b3RpbGEpXG4gICAgICAgICAgICByZXR1cm4gbG9jYWxpemF0aW9uKFwibGFiZWwucmVzdWx0U3RhdGUuXCIgKyBrZXkpXG4gICAgICAgICAgfSBlbHNlIGlmICghXy5pc0VtcHR5KHZhbGludGF0dWxvcy50aWxhbmt1dmF1cykpIHtcbiAgICAgICAgICAgIGlmKHZhbGludGF0dWxvcy50aWxhID09PSBcIkhZTEFUVFlcIil7XG4gICAgICAgICAgICAgIHJldHVybiBsb2NhbGl6YXRpb24oXCJsYWJlbC5yZXN1bHRTdGF0ZS5cIiArIGtleSkgKyBcIiBcIiArIHZhbGludGF0dWxvcy50aWxhbmt1dmF1c1xuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgcmV0dXJuIHZhbGludGF0dWxvcy50aWxhbmt1dmF1c1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH0gZWxzZSBpZiAodmFsaW50YXR1bG9zLnRpbGEgPT09IFwiVkFSQUxMQVwiICYmIHZhbGludGF0dWxvcy52YXJhc2lqb2phVGF5dGV0YWFuQXN0aSAhPSBudWxsKSB7XG4gICAgICAgICAgICByZXR1cm4gbG9jYWxpemF0aW9uKFwibGFiZWwucmVzdWx0U3RhdGUuVmFyYWxsYVB2bVwiLCB7XG4gICAgICAgICAgICAgIHZhcmFzaWphOiB2YWxpbnRhdHVsb3MudmFyYXNpamFudW1lcm8sXG4gICAgICAgICAgICAgIHZhcmFzaWphUHZtOiAkc2NvcGUuZm9ybWF0RGF0ZSh2YWxpbnRhdHVsb3MudmFyYXNpam9qYVRheXRldGFhbkFzdGkpXG4gICAgICAgICAgICB9KVxuICAgICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgICByZXR1cm4gbG9jYWxpemF0aW9uKFwibGFiZWwucmVzdWx0U3RhdGUuXCIgKyBrZXksIHtcbiAgICAgICAgICAgICAgdmFyYXNpamE6IHZhbGludGF0dWxvcy52YXJhc2lqYW51bWVyb1xuICAgICAgICAgICAgfSlcbiAgICAgICAgICB9XG4gICAgICAgIH1cblxuICAgICAgICAkc2NvcGUudmFsaW50YXR1bG9zU3R5bGUgPSBmdW5jdGlvbih2YWxpbnRhdHVsb3MpIHtcbiAgICAgICAgICBpZiAodmFsaW50YXR1bG9zLnRpbGEgPT0gXCJIWVZBS1NZVFRZXCIgfHwgdmFsaW50YXR1bG9zLnRpbGEgPT0gXCJIWVZBS1NZVFRZX0VIRE9MTElTRVNUSVwiIHx8IHZhbGludGF0dWxvcy50aWxhID09IFwiVkFSQVNJSkFMVEFfSFlWQUtTWVRUWVwiKVxuICAgICAgICAgICAgcmV0dXJuIFwiYWNjZXB0ZWRcIlxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICB9XSlcbn1cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvZGlyZWN0aXZlcy92YWxpbnRhdHVsb3MuanNcIixcIi9kaXJlY3RpdmVzXCIpIiwiKGZ1bmN0aW9uIChwcm9jZXNzLGdsb2JhbCxCdWZmZXIsX19hcmd1bWVudDAsX19hcmd1bWVudDEsX19hcmd1bWVudDIsX19hcmd1bWVudDMsX19maWxlbmFtZSxfX2Rpcm5hbWUpe1xubW9kdWxlLmV4cG9ydHMgPSBmdW5jdGlvbihsaXN0QXBwKSB7XG4gIHJlcXVpcmUoJy4vYXBwbGljYXRpb25WYWxpZGF0b3InKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2FuZ3VsYXJCYWNvbicpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9jb25maXJtJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL3F1ZXN0aW9uJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2xvY2FsaXplZExpbmsnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvZm9ybWF0dGVkVGltZScpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9zb3J0YWJsZScpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9kaXNhYmxlQ2xpY2tGb2N1cycpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9hcHBsaWNhdGlvbicpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9oYWt1dG9pdmVlblZhc3RhYW5vdHRvJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2lsbW9pdHRhdXR1bWluZW4nKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaGFrdXRvaXZlZXQnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvdmFsaW50YXR1bG9zJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2hlbmtpbG90aWVkb3QnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvYXBwbGljYXRpb25QZXJpb2RzJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2NsZWFyYWJsZUlucHV0JykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2NhbGxvdXQnKShsaXN0QXBwKVxufVxuXG5tb2R1bGUuZXhwb3J0cy5IYWtlbXVzID0gcmVxdWlyZShcIi4vaGFrZW11c1wiKVxuXG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2Zha2VfNmY4NGJlYTEuanNcIixcIi9cIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG52YXIgSGFrdXRvaXZlID0gcmVxdWlyZSgnLi9oYWt1dG9pdmUnKVxudmFyIFF1ZXN0aW9uID0gcmVxdWlyZSgnLi9xdWVzdGlvbicpLlF1ZXN0aW9uXG52YXIgdXRpbCA9IHJlcXVpcmUoJy4vdXRpbCcpXG5cbmZ1bmN0aW9uIEhha2VtdXMoanNvbikge1xuICB0aGlzLm9pZCA9IGpzb24uaGFrZW11cy5vaWRcbiAgdGhpcy51cGRhdGVkID0ganNvbi5oYWtlbXVzLnVwZGF0ZWRcbiAgdGhpcy5oYWt1ID0gY29weShqc29uLmhha2VtdXMuaGFrdSlcbiAgdGhpcy5zdGF0ZSA9IGNvcHkoanNvbi5oYWtlbXVzLnN0YXRlKVxuICB0aGlzLmhhc0Zvcm0gPSBqc29uLmhha2VtdXMuaGFzRm9ybVxuICB0aGlzLmVkdWNhdGlvbkJhY2tncm91bmQgPSBjb3B5KGpzb24uaGFrZW11cy5lZHVjYXRpb25CYWNrZ3JvdW5kKVxuXG4gIHRoaXMuaGFrdXRvaXZlZXQgPSBjb252ZXJ0SGFrdXRvaXZlZXQoanNvbi5oYWtlbXVzLmhha3V0b2l2ZWV0KVxuICB0aGlzLmhlbmtpbG90aWVkb3QgPSBjb252ZXJ0SGVua2lsb3RpZWRvdChqc29uLmhha2VtdXMuYW5zd2Vycy5oZW5raWxvdGllZG90KVxuICB0aGlzLnBlcnNpc3RlZEFuc3dlcnMgPSBqc29uLmhha2VtdXMuYW5zd2Vyc1xuICB0aGlzLmFkZGl0aW9uYWxRdWVzdGlvbnMgPSBRdWVzdGlvbi5nZXRRdWVzdGlvbnMoanNvbi5xdWVzdGlvbnMsIHRoaXMpXG4gIHRoaXMudHVsb3NPayA9IGpzb24udHVsb3NPa1xuICB0aGlzLmNhbGN1bGF0ZWRWYWx1ZXMgPSB7XG4gICAgcG9zdE9mZmljZToganNvbi5oYWtlbXVzLnBvc3RPZmZpY2VcbiAgfVxufVxuXG5mdW5jdGlvbiBjb3B5KGpzb24pIHsgcmV0dXJuICQuZXh0ZW5kKHRydWUsIHt9LCBqc29uKSB9XG5cbmZ1bmN0aW9uIGNvbnZlcnRIZW5raWxvdGllZG90KGpzb24pIHtcbiAgdmFyIGZpZWxkcyA9IFtcIlPDpGhrw7Zwb3N0aVwiLCBcIm1hdGthcHVoZWxpbm51bWVybzFcIiwgXCJhc3Vpbm1hYVwiLCBcImxhaGlvc29pdGVcIiwgXCJQb3N0aW51bWVyb1wiXVxuICByZXR1cm4gXyhmaWVsZHMpLnJlZHVjZShmdW5jdGlvbihtZW1vLCBrZXkpIHtcbiAgICBtZW1vW2tleV0gPSBuZXcgUXVlc3Rpb24oeyBpZDoga2V5IH0sIGpzb25ba2V5XSlcbiAgICByZXR1cm4gbWVtb1xuICB9ICwge30pXG59XG5cbmZ1bmN0aW9uIGNvbnZlcnRIYWt1dG9pdmVldChoYWt1dG9pdmVldCkge1xuICByZXR1cm4gXyhoYWt1dG9pdmVldCkubWFwKGZ1bmN0aW9uKGhha3V0b2l2ZSkgeyByZXR1cm4gbmV3IEhha3V0b2l2ZShoYWt1dG9pdmUpIH0pXG59XG5cbmZ1bmN0aW9uIHVwZGF0ZVByZWZlcmVuY2VRdWVzdGlvbklkcyhtYW5pcHVsYXRpb25GKSB7XG4gIHZhciBuZXdJbmRleGVzID0gKGZ1bmN0aW9uIGdldE5ld0luZGV4ZXMoKSB7XG4gICAgdmFyIGFyciA9IF8ucmFuZ2UoMSwgdGhpcy5oYWt1dG9pdmVldC5sZW5ndGgrMSlcbiAgICBtYW5pcHVsYXRpb25GKGFycilcbiAgICB2YXIgaW5kZXhlcyA9IF8oYXJyKS5tYXAoZnVuY3Rpb24odmFsLCBpbmRleCkgeyByZXR1cm4gW3ZhbCwgaW5kZXgrMV0gfSlcbiAgICByZXR1cm4gXy5vYmplY3QoaW5kZXhlcylcbiAgfSkuY2FsbCh0aGlzKVxuXG4gIF8oUXVlc3Rpb24ucXVlc3Rpb25NYXAodGhpcy5hZGRpdGlvbmFsUXVlc3Rpb25zKSkuZWFjaChmdW5jdGlvbihxdWVzdGlvbiwgaWQpIHtcbiAgICB2YXIgcXVlc3Rpb25JZFBhcnRzID0gL14ocHJlZmVyZW5jZSkoXFxkKykoWy1fXS4rKS8uZXhlYyhpZClcbiAgICBpZiAocXVlc3Rpb25JZFBhcnRzICE9IG51bGwpIHtcbiAgICAgIHZhciBuZXdJZCA9IHF1ZXN0aW9uSWRQYXJ0c1sxXSArIG5ld0luZGV4ZXNbcXVlc3Rpb25JZFBhcnRzWzJdXSArIHF1ZXN0aW9uSWRQYXJ0c1szXVxuICAgICAgcXVlc3Rpb24uaWQucXVlc3Rpb25JZCA9IG5ld0lkXG4gICAgfVxuICB9KVxufVxuXG5IYWtlbXVzLnByb3RvdHlwZSA9IHtcbiAgcmVtb3ZlUHJlZmVyZW5jZTogZnVuY3Rpb24oaW5kZXgpIHtcbiAgICB0aGlzLmhha3V0b2l2ZWV0LnNwbGljZShpbmRleCwgMSlcbiAgICB1cGRhdGVQcmVmZXJlbmNlUXVlc3Rpb25JZHMuY2FsbCh0aGlzLCBmdW5jdGlvbihhcnIpIHtcbiAgICAgIGFyci5zcGxpY2UoaW5kZXgsIDEpXG4gICAgfSlcbiAgfSxcblxuICBhZGRQcmVmZXJlbmNlOiBmdW5jdGlvbihoYWt1dG9pdmUpIHtcbiAgICB0aGlzLmhha3V0b2l2ZWV0LnB1c2goaGFrdXRvaXZlKVxuICB9LFxuXG4gIGhhc1ByZWZlcmVuY2U6IGZ1bmN0aW9uKGluZGV4KSB7XG4gICAgcmV0dXJuIGluZGV4ID49IDAgJiYgaW5kZXggPD0gdGhpcy5oYWt1dG9pdmVldC5sZW5ndGgtMSAmJiB0aGlzLmhha3V0b2l2ZWV0W2luZGV4XS5oYXNEYXRhKClcbiAgfSxcblxuICBtb3ZlUHJlZmVyZW5jZTogZnVuY3Rpb24oZnJvbSwgdG8pIHtcbiAgICB0aGlzLmhha3V0b2l2ZWV0W2Zyb21dLnNldEFzTW9kaWZpZWQoKVxuICAgIHRoaXMuaGFrdXRvaXZlZXRbdG9dLnNldEFzTW9kaWZpZWQoKVxuICAgIHRoaXMuaGFrdXRvaXZlZXQuc3BsaWNlKHRvLCAwLCB0aGlzLmhha3V0b2l2ZWV0LnNwbGljZShmcm9tLCAxKVswXSlcblxuICAgIHVwZGF0ZVByZWZlcmVuY2VRdWVzdGlvbklkcy5jYWxsKHRoaXMsXG4gICAgICBmdW5jdGlvbihhcnIpIHtcbiAgICAgICAgYXJyLnNwbGljZSh0bywgMCwgYXJyLnNwbGljZShmcm9tLCAxKVswXSlcbiAgICAgIH1cbiAgICApXG4gIH0sXG5cbiAgY2FuTW92ZVByZWZlcmVuY2U6IGZ1bmN0aW9uKGZyb20sIHRvKSB7XG4gICAgdmFyIGxhc3RGaWxsZWRJdGVtID0gKGZ1bmN0aW9uIGdldExhc3RGaWxsZWQoaGFrdXRvaXZlZXQpIHtcbiAgICAgIGZvciAodmFyIGk9aGFrdXRvaXZlZXQubGVuZ3RoLTE7IGk+PTA7IGktLSlcbiAgICAgICAgaWYgKGhha3V0b2l2ZWV0W2ldLmhhc0RhdGEoKSlcbiAgICAgICAgICByZXR1cm4gaVxuICAgICAgcmV0dXJuIC0xXG4gICAgfSkodGhpcy5oYWt1dG9pdmVldClcblxuICAgIHJldHVybiAhdGhpcy5wcmVmZXJlbmNlTG9ja2VkKGZyb20pICYmIHRoaXMuaGFrdXRvaXZlZXRbZnJvbV0uaGFzRGF0YSgpICYmIGZyb20gPj0gMCAmJiB0byA8PSBsYXN0RmlsbGVkSXRlbSAmJiB0byA+PSAwXG4gIH0sXG4gIFxuICBwcmVmZXJlbmNlTG9ja2VkOiBmdW5jdGlvbihpbmRleCkge1xuICAgIHZhciBoYWt1dG9pdmUgPSB0aGlzLmhha3V0b2l2ZWV0W2luZGV4XVxuICAgIHZhciBoYWt1YWlrYUlkID0gaGFrdXRvaXZlLmhha3VhaWthSWRcbiAgICB2YXIgc2VsZiA9IHRoaXNcblxuICAgIGZ1bmN0aW9uIGlzUGVyaW9kQWN0aXZlKGFwcGxpY2F0aW9uUGVyaW9kSWQpIHtcbiAgICAgIHZhciBwZXJpb2QgPSBfKHNlbGYuaGFrdS5hcHBsaWNhdGlvblBlcmlvZHMpLmZpbmQoZnVuY3Rpb24ocGVyaW9kKSB7IHJldHVybiBwZXJpb2QuaWQgPT09IGFwcGxpY2F0aW9uUGVyaW9kSWQgfSlcbiAgICAgIHJldHVybiBwZXJpb2QgIT09IHVuZGVmaW5lZCA/IHBlcmlvZC5hY3RpdmUgOiBzZWxmLmhha3UuYWN0aXZlXG4gICAgfVxuXG4gICAgaWYgKGhha3V0b2l2ZS5hZGRlZER1cmluZ0N1cnJlbnRTZXNzaW9uKSB7XG4gICAgICByZXR1cm4gZmFsc2VcbiAgICB9IGVsc2UgaWYgKCFfLmlzRW1wdHkoaGFrdXRvaXZlLmtvaGRla29odGFpbmVuSGFrdWFpa2EpKSB7XG4gICAgICByZXR1cm4gIWhha3V0b2l2ZS5rb2hkZWtvaHRhaW5lbkhha3VhaWthLmFjdGl2ZVxuICAgIH0gZWxzZSBpZiAoIV8uaXNFbXB0eShoYWt1YWlrYUlkKSkge1xuICAgICAgcmV0dXJuICFpc1BlcmlvZEFjdGl2ZShoYWt1YWlrYUlkKVxuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gIXRoaXMuaGFrdS5hY3RpdmVcbiAgICB9XG4gIH0sXG5cbiAgYWxsUmVzdWx0c0F2YWlsYWJsZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuICF0aGlzLmhhc1Jlc3VsdFN0YXRlKFtcIktFU0tFTlwiLCBcIlZBUkFMTEFcIl0pICYmIHRoaXMudmFsaW50YXR1bG9zSGFrdXRvaXZlZXQoKS5sZW5ndGggPiAwXG4gIH0sXG5cbiAgcmVzdWx0RmV0Y2hlZFN1Y2Nlc3NmdWxseTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHRoaXMudHVsb3NPa1xuICB9LFxuXG4gIGhhc1NvbWVSZXN1bHRzOiBmdW5jdGlvbigpIHtcbiAgICB2YXIgaGFrdXRvaXZlZXQgPSB0aGlzLnZhbGludGF0dWxvc0hha3V0b2l2ZWV0KClcbiAgICByZXR1cm4gaGFrdXRvaXZlZXQubGVuZ3RoID4gMCAmJiBfKGhha3V0b2l2ZWV0KS5zb21lKGZ1bmN0aW9uKGhha3V0b2l2ZSkgeyByZXR1cm4gaGFrdXRvaXZlLnRpbGEgIT0gXCJLRVNLRU5cIiB9KVxuICB9LFxuXG4gIHZhbGludGF0dWxvc0hha3V0b2l2ZWV0OiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gdGhpcy5zdGF0ZSAmJiB0aGlzLnN0YXRlLnZhbGludGF0dWxvcyA/IHRoaXMuc3RhdGUudmFsaW50YXR1bG9zLmhha3V0b2l2ZWV0IDogW11cbiAgfSxcblxuICBhcHBsaWNhdGlvblBlcmlvZHNJbmFjdGl2ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIF8odGhpcy5oYWt1LmFwcGxpY2F0aW9uUGVyaW9kcykuZXZlcnkoZnVuY3Rpb24ocGVyaW9kKSB7IHJldHVybiAhcGVyaW9kLmFjdGl2ZSB9KVxuICB9LFxuXG4gIGVkaXRIYWt1dG9pdmVldEVuYWJsZWQ6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiB0aGlzLnN0YXRlICYmICh0aGlzLnN0YXRlLmlkID09ICdBQ1RJVkUnIHx8IHRoaXMuc3RhdGUuaWQgPT0gJ0lOQ09NUExFVEUnKVxuICB9LFxuXG4gIGVkaXRIZW5raWxvdGllZG90RW5hYmxlZDogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHRoaXMuZWRpdEhha3V0b2l2ZWV0RW5hYmxlZCgpIHx8ICh0aGlzLnN0YXRlICYmIHRoaXMuc3RhdGUuaWQgPT0gXCJIQUtVS0FVU0lQQUFUVFlOWVRcIilcbiAgfSxcblxuICB2YXN0YWFub3RldHRhdmF0SGFrdXRvaXZlZXQ6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiBfKHRoaXMudmFsaW50YXR1bG9zSGFrdXRvaXZlZXQoKSkuZmlsdGVyKGZ1bmN0aW9uKGhha3V0b2l2ZSkge1xuICAgICAgcmV0dXJuIGhha3V0b2l2ZS52YXN0YWFub3RldHRhdnV1c3RpbGEgPT09IFwiVkFTVEFBTk9URVRUQVZJU1NBX1NJVE9WQVNUSVwiIHx8IGhha3V0b2l2ZS52YXN0YWFub3RldHRhdnV1c3RpbGEgPT09IFwiVkFTVEFBTk9URVRUQVZJU1NBX0VIRE9MTElTRVNUSVwiXG4gICAgfSlcbiAgfSxcblxuICBpbG1vaXR0YXV0dW1pc0xpbmtpdDogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIF8odGhpcy52YWxpbnRhdHVsb3NIYWt1dG9pdmVldCgpKS5maWx0ZXIoZnVuY3Rpb24odHVsb3MpIHtcbiAgICAgIHJldHVybiB0dWxvcy5pbG1vaXR0YXV0dW1pc3RpbGEgIT0gbnVsbCAmJlxuICAgICAgICB0dWxvcy5pbG1vaXR0YXV0dW1pc3RpbGEuaWxtb2l0dGF1ZHV0dGF2aXNzYSAmJlxuICAgICAgICB0dWxvcy5pbG1vaXR0YXV0dW1pc3RpbGEuaWxtb2l0dGF1dHVtaXN0YXBhICE9IG51bGwgJiYgdHVsb3MuaWxtb2l0dGF1dHVtaXN0aWxhLmlsbW9pdHRhdXR1bWlzdGFwYS51cmxcbiAgICB9KVxuICB9LFxuXG4gIGhhc1Jlc3VsdFN0YXRlOiBmdW5jdGlvbihyZXN1bHRTdGF0ZXMpIHtcbiAgICBpZiAoIV8uaXNBcnJheShyZXN1bHRTdGF0ZXMpKVxuICAgICAgcmVzdWx0U3RhdGVzID0gW3Jlc3VsdFN0YXRlc11cblxuICAgIHJldHVybiBfKHRoaXMudmFsaW50YXR1bG9zSGFrdXRvaXZlZXQoKSkuYW55KGZ1bmN0aW9uKGhha3V0b2l2ZSkge1xuICAgICAgcmV0dXJuIF8ocmVzdWx0U3RhdGVzKS5jb250YWlucyhoYWt1dG9pdmUudGlsYSl9XG4gICAgKVxuICB9LFxuXG4gIHRvSnNvbjogZnVuY3Rpb24oKSB7XG4gICAgdmFyIHNlbGYgPSB0aGlzXG5cbiAgICByZXR1cm4ge1xuICAgICAgb2lkOiBzZWxmLm9pZCxcbiAgICAgIGhha3VPaWQ6IHNlbGYuaGFrdS5vaWQsXG4gICAgICBoYWt1dG9pdmVldDogXyh0aGlzLmhha3V0b2l2ZWV0KS5tYXAoZnVuY3Rpb24oaGFrdXRvaXZlKSB7IHJldHVybiBoYWt1dG9pdmUudG9Kc29uKCkgfSksXG4gICAgICBhbnN3ZXJzOiByZW1vdmVGYWxzZUJvb2xlYW5zKGdldEFuc3dlcnMoKSlcbiAgICB9XG5cbiAgICBmdW5jdGlvbiBnZXRBbnN3ZXJzKCkge1xuICAgICAgdmFyIGNvbnRhY3REZXRhaWxzID0gXyhzZWxmLmhlbmtpbG90aWVkb3QpLnJlZHVjZShmdW5jdGlvbihhbnN3ZXJzLCBxdWVzdGlvbiwgaWQpIHtcbiAgICAgICAgYW5zd2Vycy5oZW5raWxvdGllZG90W2lkXSA9IHF1ZXN0aW9uLmFuc3dlclxuICAgICAgICByZXR1cm4gYW5zd2Vyc1xuICAgICAgfSwgeyBoZW5raWxvdGllZG90OiB7fX0pXG5cbiAgICAgIHZhciBhZGRpdGlvbmFsUXVlc3Rpb25BbnN3ZXJzID0gXyhRdWVzdGlvbi5xdWVzdGlvbk1hcChzZWxmLmFkZGl0aW9uYWxRdWVzdGlvbnMpKS5yZWR1Y2UoZnVuY3Rpb24oYW5zd2VycywgcXVlc3Rpb25Ob2RlKSB7XG4gICAgICAgIGFuc3dlcnNbcXVlc3Rpb25Ob2RlLmlkLnBoYXNlSWRdID0gYW5zd2Vyc1txdWVzdGlvbk5vZGUuaWQucGhhc2VJZF0gfHwge31cbiAgICAgICAgdmFyIGFuc3dlciA9IHF1ZXN0aW9uTm9kZS5hbnN3ZXJcbiAgICAgICAgaWYgKF8uaXNPYmplY3QoYW5zd2VyKSkge1xuICAgICAgICAgIF8oYW5zd2VyKS5lYWNoKGZ1bmN0aW9uKHZhbCwga2V5KSB7XG4gICAgICAgICAgICBhbnN3ZXJzW3F1ZXN0aW9uTm9kZS5pZC5waGFzZUlkXVtrZXldID0gdmFsXG4gICAgICAgICAgfSlcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICBhbnN3ZXJzW3F1ZXN0aW9uTm9kZS5pZC5waGFzZUlkXVtxdWVzdGlvbk5vZGUuaWQucXVlc3Rpb25JZF0gPSBhbnN3ZXJcbiAgICAgICAgfVxuICAgICAgICByZXR1cm4gYW5zd2Vyc1xuICAgICAgfSwge30pXG5cbiAgICAgIHJldHVybiBfLmV4dGVuZCh7fSwgY29udGFjdERldGFpbHMsIGFkZGl0aW9uYWxRdWVzdGlvbkFuc3dlcnMpXG4gICAgfVxuXG4gICAgZnVuY3Rpb24gcmVtb3ZlRmFsc2VCb29sZWFucyhvYmopIHtcbiAgICAgIF8uZWFjaChvYmosIGZ1bmN0aW9uKHZhbCwga2V5KSB7XG4gICAgICAgIGlmIChfLmlzQm9vbGVhbih2YWwpICYmIHZhbCA9PT0gZmFsc2UpXG4gICAgICAgICAgb2JqW2tleV0gPSBcIlwiXG4gICAgICAgIGVsc2UgaWYgKF8uaXNPYmplY3QodmFsKSlcbiAgICAgICAgICByZW1vdmVGYWxzZUJvb2xlYW5zKHZhbClcbiAgICAgIH0pXG4gICAgICByZXR1cm4gb2JqO1xuICAgIH1cbiAgfSxcblxuICBtZXJnZVNhdmVkQXBwbGljYXRpb246IGZ1bmN0aW9uKHNhdmVkQXBwbGljYXRpb24pIHtcbiAgICB0aGlzLnVwZGF0ZWQgPSBzYXZlZEFwcGxpY2F0aW9uLnVwZGF0ZWRcblxuICAgIGlmICghXy5pc0VxdWFsKHV0aWwud2l0aG91dEFuZ3VsYXJGaWVsZHModGhpcy5zdGF0ZSksIHNhdmVkQXBwbGljYXRpb24uc3RhdGUpKVxuICAgICAgdGhpcy5zdGF0ZSA9ICQuZXh0ZW5kKHRydWUsIHt9LCBzYXZlZEFwcGxpY2F0aW9uLnN0YXRlKVxuXG4gICAgZm9yICh2YXIgaT0wOyBpPHRoaXMuaGFrdXRvaXZlZXQubGVuZ3RoICYmIGk8c2F2ZWRBcHBsaWNhdGlvbi5oYWt1dG9pdmVldC5sZW5ndGg7IGkrKykge1xuICAgICAgaGFrdXRvaXZlID0gdGhpcy5oYWt1dG9pdmVldFtpXVxuICAgICAgaGFrdXRvaXZlLmltcG9ydEpzb24oc2F2ZWRBcHBsaWNhdGlvbi5oYWt1dG9pdmVldFtpXSlcbiAgICAgIGlmIChoYWt1dG9pdmUuaGFzRGF0YSgpKVxuICAgICAgICBoYWt1dG9pdmUuc2V0QXNTYXZlZCgpXG4gICAgfVxuICB9LFxuXG4gIGltcG9ydEhha3VhamF0OiBmdW5jdGlvbihoYWt1a29odGVldCkge1xuICAgIGlmIChoYWt1a29odGVldCAhPSBudWxsKSB7XG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IHRoaXMuaGFrdXRvaXZlZXQubGVuZ3RoICYmIGkgPCBoYWt1a29odGVldC5sZW5ndGg7IGkrKykge1xuICAgICAgICB0aGlzLmhha3V0b2l2ZWV0W2ldLmtvaGRla29odGFpbmVuSGFrdWFpa2EgPSBoYWt1a29odGVldFtpXS5rb2hkZWtvaHRhaW5lbkhha3VhaWthXG4gICAgICB9XG4gICAgfVxuICB9LFxuXG4gIHZhbGlkYXRlUHJlZmVyZW5jZXM6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiAodGhpcy5oYWt1dG9pdmVldC5sZW5ndGggPiAwICYmIHRoaXMuaGFrdXRvaXZlZXRbMF0uaGFzRGF0YSgpKSAmJlxuICAgICAgXyh0aGlzLmhha3V0b2l2ZWV0KS5ldmVyeShmdW5jdGlvbihoYWt1dG9pdmUpIHtcbiAgICAgICAgcmV0dXJuIGhha3V0b2l2ZS5pc1ZhbGlkKClcbiAgICAgIH0pICYmICFfKHRoaXMuaGFrdXRvaXZlZXQuc2xpY2UoMCwgdGhpcy5sYXN0SW5kZXhXaXRoRGF0YSgpICsgMSkpLmFueShmdW5jdGlvbihoYWt1dG9pdmUpIHtcbiAgICAgICAgcmV0dXJuICFoYWt1dG9pdmUuaGFzRGF0YSgpXG4gICAgICB9KVxuICB9LFxuXG4gIGdldENoYW5nZWRQcmVmZXJlbmNlczogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIF8odGhpcy5oYWt1dG9pdmVldCkuY2hhaW4oKVxuICAgICAgLm1hcChmdW5jdGlvbihoYWt1dG9pdmUsIGluZGV4KSB7IHJldHVybiBoYWt1dG9pdmUuaXNNb2RpZmllZCA/IGluZGV4IDogbnVsbCB9KVxuICAgICAgLndpdGhvdXQobnVsbClcbiAgICAgIC52YWx1ZSgpXG4gIH0sXG5cbiAgaXNFZGl0YWJsZTogZnVuY3Rpb24oaW5kZXgpIHtcbiAgICByZXR1cm4gIXRoaXMuYXBwbGljYXRpb25QZXJpb2RzSW5hY3RpdmUoKSAmJiAgaW5kZXggPD0gdGhpcy5sYXN0SW5kZXhXaXRoRGF0YSgpICsgMVxuICB9LFxuXG4gIGxhc3RJbmRleFdpdGhEYXRhOiBmdW5jdGlvbigpIHtcbiAgICBmb3IgKHZhciBpID0gdGhpcy5oYWt1dG9pdmVldC5sZW5ndGggLSAxOyBpID49IDA7IGktLSkge1xuICAgICAgaWYgKHRoaXMuaGFrdXRvaXZlZXRbaV0uaGFzRGF0YSgpKSByZXR1cm4gaVxuICAgIH1cbiAgICByZXR1cm4gLTFcbiAgfSxcblxuICBpbXBvcnRRdWVzdGlvbnM6IGZ1bmN0aW9uKHF1ZXN0aW9ucykge1xuICAgIHRoaXMuYWRkaXRpb25hbFF1ZXN0aW9ucyA9IChmdW5jdGlvbiBtZXJnZU9sZEFuc3dlcnMob2xkLCBxdWVzdGlvbnMpIHtcbiAgICAgIHZhciBvbGRRdWVzdGlvbnMgPSBRdWVzdGlvbi5xdWVzdGlvbk1hcChvbGQpXG4gICAgICBfKFF1ZXN0aW9uLnF1ZXN0aW9uTWFwKHF1ZXN0aW9ucykpLmVhY2goZnVuY3Rpb24obmV3UXVlc3Rpb24sIGlkKSB7XG4gICAgICAgIGlmIChvbGRRdWVzdGlvbnNbaWRdICE9IG51bGwpXG4gICAgICAgICAgbmV3UXVlc3Rpb24uYW5zd2VyID0gb2xkUXVlc3Rpb25zW2lkXS5hbnN3ZXJcbiAgICAgIH0pXG4gICAgICByZXR1cm4gcXVlc3Rpb25zXG4gICAgfSkodGhpcy5hZGRpdGlvbmFsUXVlc3Rpb25zLCBxdWVzdGlvbnMpXG4gIH0sXG5cbiAgdXBkYXRlVmFsaWRhdGlvbk1lc3NhZ2VzOiBmdW5jdGlvbihlcnJvcnMsIHNraXBRdWVzdGlvbnMpIHtcbiAgICB2YXIgZXJyb3JNYXAgPSB1dGlsLm1hcEFycmF5KGVycm9ycywgXCJrZXlcIiwgXCJtZXNzYWdlXCIpXG4gICAgdmFyIGhha3V0b2l2ZU1hcCA9IEhha3V0b2l2ZS5oYWt1dG9pdmVNYXAodGhpcy5oYWt1dG9pdmVldClcbiAgICB2YXIgcXVlc3Rpb25NYXAgPSBfLmV4dGVuZCh7fSwgUXVlc3Rpb24ucXVlc3Rpb25NYXAodGhpcy5hZGRpdGlvbmFsUXVlc3Rpb25zKSwgdGhpcy5oZW5raWxvdGllZG90KVxuICAgIHZhciB1bmhhbmRsZWQgPSBbXVxuXG4gICAgY2xlYXJFcnJvcnMoKVxuXG4gICAgXyhlcnJvck1hcCkuZWFjaChmdW5jdGlvbihlcnJvckxpc3QsIGtleSkge1xuICAgICAgaWYgKCF1cGRhdGVFcnJvcnMoa2V5LCBlcnJvckxpc3QpKVxuICAgICAgICB1bmhhbmRsZWQucHVzaCh7cXVlc3Rpb25JZDoga2V5LCBlcnJvcnM6IGVycm9yTGlzdH0pXG4gICAgfSlcblxuICAgIHJldHVybiB1bmhhbmRsZWRcblxuICAgIGZ1bmN0aW9uIGNsZWFyRXJyb3JzKCkge1xuICAgICAgXyhoYWt1dG9pdmVNYXApLmVhY2goZnVuY3Rpb24oaXRlbSkgeyBpdGVtLnNldEVycm9ycygpIH0pXG4gICAgICBpZiAoIXNraXBRdWVzdGlvbnMpXG4gICAgICAgIF8ocXVlc3Rpb25NYXApLmVhY2goZnVuY3Rpb24oaXRlbSkgeyBpdGVtLnNldEVycm9ycygpIH0pXG4gICAgfVxuXG4gICAgZnVuY3Rpb24gdXBkYXRlRXJyb3JzKHF1ZXN0aW9uSWQsIGVycm9ycykge1xuICAgICAgaWYgKHF1ZXN0aW9uTWFwW3F1ZXN0aW9uSWRdICE9IG51bGwpIHtcbiAgICAgICAgaWYgKCFza2lwUXVlc3Rpb25zKVxuICAgICAgICAgIHF1ZXN0aW9uTWFwW3F1ZXN0aW9uSWRdLmFwcGVuZEVycm9ycyhlcnJvcnMpXG4gICAgICAgIHJldHVybiB0cnVlXG4gICAgICB9IGVsc2UgaWYgKEhha3V0b2l2ZS5pc0hha3V0b2l2ZUVycm9yKHF1ZXN0aW9uSWQpKSB7XG4gICAgICAgIGhha3V0b2l2ZU1hcFtIYWt1dG9pdmUucXVlc3Rpb25JZFRvSGFrdXRvaXZlSWQocXVlc3Rpb25JZCldLmFwcGVuZEVycm9ycyhlcnJvcnMpXG4gICAgICAgIHJldHVybiB0cnVlXG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gZmFsc2VcbiAgICAgIH1cbiAgICB9XG4gIH1cbn1cblxubW9kdWxlLmV4cG9ydHMgPSBIYWtlbXVzXG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2hha2VtdXMuanNcIixcIi9cIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG5tb2R1bGUuZXhwb3J0cyA9IGZ1bmN0aW9uKGxpc3RBcHApIHtcbiAgcmVxdWlyZSgnLi9hcHBsaWNhdGlvblZhbGlkYXRvcicpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vYW5ndWxhckJhY29uJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2NvbmZpcm0nKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvcXVlc3Rpb24nKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvbG9jYWxpemVkTGluaycpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9mb3JtYXR0ZWRUaW1lJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL3NvcnRhYmxlJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2Rpc2FibGVDbGlja0ZvY3VzJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2FwcGxpY2F0aW9uJykobGlzdEFwcClcbiAgcmVxdWlyZSgnLi9kaXJlY3RpdmVzL2hha3V0b2l2ZWVuVmFzdGFhbm90dG8nKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaWxtb2l0dGF1dHVtaW5lbicpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9oYWt1dG9pdmVldCcpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy92YWxpbnRhdHVsb3MnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvaGVua2lsb3RpZWRvdCcpKGxpc3RBcHApXG4gIHJlcXVpcmUoJy4vZGlyZWN0aXZlcy9hcHBsaWNhdGlvblBlcmlvZHMnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvY2xlYXJhYmxlSW5wdXQnKShsaXN0QXBwKVxuICByZXF1aXJlKCcuL2RpcmVjdGl2ZXMvY2FsbG91dCcpKGxpc3RBcHApXG59XG5cbm1vZHVsZS5leHBvcnRzLkhha2VtdXMgPSByZXF1aXJlKFwiLi9oYWtlbXVzXCIpXG5cbn0pLmNhbGwodGhpcyxyZXF1aXJlKFwiKzdaSnAwXCIpLHR5cGVvZiBzZWxmICE9PSBcInVuZGVmaW5lZFwiID8gc2VsZiA6IHR5cGVvZiB3aW5kb3cgIT09IFwidW5kZWZpbmVkXCIgPyB3aW5kb3cgOiB7fSxyZXF1aXJlKFwiYnVmZmVyXCIpLkJ1ZmZlcixhcmd1bWVudHNbM10sYXJndW1lbnRzWzRdLGFyZ3VtZW50c1s1XSxhcmd1bWVudHNbNl0sXCIvaGFrZW11c2VkaXRvcmkuanNcIixcIi9cIikiLCIoZnVuY3Rpb24gKHByb2Nlc3MsZ2xvYmFsLEJ1ZmZlcixfX2FyZ3VtZW50MCxfX2FyZ3VtZW50MSxfX2FyZ3VtZW50MixfX2FyZ3VtZW50MyxfX2ZpbGVuYW1lLF9fZGlybmFtZSl7XG52YXIgdXRpbCA9IHJlcXVpcmUoJy4vdXRpbCcpXG5cbmZ1bmN0aW9uIEhha3V0b2l2ZShqc29uKSB7XG4gIHRoaXMuaW1wb3J0SnNvbihqc29uKVxuICB0aGlzLmlzTW9kaWZpZWQgPSBmYWxzZVxuICB0aGlzLmlzTmV3ID0gXy5pc0VtcHR5KGpzb24pXG4gIHRoaXMuYWRkZWREdXJpbmdDdXJyZW50U2Vzc2lvbiA9IF8uaXNFbXB0eShqc29uKVxuICB0aGlzLmVycm9ycyA9IFtdXG59XG5cbkhha3V0b2l2ZS5wcm90b3R5cGUgPSB7XG4gIHRvSnNvbjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHRoaXMuZGF0YVxuICB9LFxuXG4gIGltcG9ydEpzb246IGZ1bmN0aW9uKGpzb24pIHtcbiAgICAgIHRoaXMuZGF0YSA9IGpzb24uaGFrZW11c0RhdGEgfHwge31cbiAgICAgIHRoaXMuaGFrdWFpa2FJZCA9IGpzb24uaGFrdWFpa2FJZFxuICAgICAgdGhpcy5rb2hkZWtvaHRhaW5lbkhha3VhaWthID0ganNvbi5rb2hkZWtvaHRhaW5lbkhha3VhaWthXG4gIH0sXG5cbiAgY2xlYXI6IGZ1bmN0aW9uKCkge1xuICAgIHRoaXMuZGF0YSA9IHt9XG4gICAgdGhpcy5pc05ldyA9IHRydWVcbiAgICB0aGlzLmlzTW9kaWZpZWQgPSBmYWxzZVxuICB9LFxuXG4gIGhhc0RhdGE6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiAhXy5pc0VtcHR5KHRoaXMuZGF0YSlcbiAgfSxcblxuICBzZXRPcGV0dXNwaXN0ZTogZnVuY3Rpb24oaWQsIG5hbWUpIHtcbiAgICB0aGlzLmRhdGFbXCJPcGV0dXNwaXN0ZVwiXSA9IG5hbWVcbiAgICB0aGlzLmRhdGFbXCJPcGV0dXNwaXN0ZS1pZFwiXSA9IGlkXG4gICAgdGhpcy5pc01vZGlmaWVkID0gdHJ1ZVxuICAgIHRoaXMuc2V0RXJyb3JzKFtdKVxuICB9LFxuXG4gIHNldEtvdWx1dHVzOiBmdW5jdGlvbihrb3VsdXR1cykge1xuICAgIHRoaXMuZGF0YVtcIktvdWx1dHVzXCJdID0gdG9TdHJpbmcoa291bHV0dXMubmFtZSlcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZFwiXSA9IHRvU3RyaW5nKGtvdWx1dHVzLmlkKVxuICAgIHRoaXMuZGF0YVtcIktvdWx1dHVzLWVkdWNhdGlvbkRlZ3JlZVwiXSA9IHRvU3RyaW5nKGtvdWx1dHVzLmVkdWNhdGlvbkRlZ3JlZSlcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZC1sYW5nXCJdID0gdG9TdHJpbmcoa291bHV0dXMudGVhY2hpbmdMYW5ndWFnZXNbMF0pXG4gICAgdGhpcy5kYXRhW1wiS291bHV0dXMtaWQtc29yYVwiXSA9IHRvU3RyaW5nKGtvdWx1dHVzLnNvcmEpXG4gICAgdGhpcy5kYXRhW1wiS291bHV0dXMtaWQtYW9JZGVudGlmaWVyXCJdID0gdG9TdHJpbmcoa291bHV0dXMuYW9JZGVudGlmaWVyKVxuICAgIHRoaXMuZGF0YVtcIktvdWx1dHVzLWlkLWtha3NvaXN0dXRraW50b1wiXSA9IHRvU3RyaW5nKGtvdWx1dHVzLmtha3NvaXN0dXRraW50bylcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZC12b2NhdGlvbmFsXCJdID0gdG9TdHJpbmcoa291bHV0dXMudm9jYXRpb25hbClcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZC1lZHVjYXRpb25jb2RlXCJdID0gdG9TdHJpbmcoa291bHV0dXMuZWR1Y2F0aW9uQ29kZVVyaSlcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZC1hdGhsZXRlXCJdID0gdG9TdHJpbmcoa291bHV0dXMuYXRobGV0ZUVkdWNhdGlvbilcbiAgICB0aGlzLmRhdGFbXCJLb3VsdXR1cy1pZC1hdHRhY2htZW50c1wiXSA9IHRvU3RyaW5nKGtvdWx1dHVzLmF0dGFjaG1lbnRzICE9IG51bGwgJiYga291bHV0dXMuYXR0YWNobWVudHMubGVuZ3RoID4gMClcbiAgICB0aGlzLmFkZEdyb3VwSW5mbyhrb3VsdXR1cylcbiAgICB0aGlzLmlzTW9kaWZpZWQgPSB0cnVlXG4gICAgdGhpcy5zZXRFcnJvcnMoW10pXG4gICAgZnVuY3Rpb24gdG9TdHJpbmcoeCkge1xuICAgICAgcmV0dXJuICh4PT1udWxsKSA/IFwiXCIgOiB4LnRvU3RyaW5nKClcbiAgICB9XG4gIH0sXG5cbiAgYWRkR3JvdXBJbmZvOiBmdW5jdGlvbihrb3VsdXR1cykge1xuICAgIHZhciBhdHRhY2htZW50R3JvdXBzID0gW107XG4gICAgdmFyIGFvR3JvdXBzID0gW107XG4gICAgaWYgKGtvdWx1dHVzLm9yZ2FuaXphdGlvbkdyb3VwcyBpbnN0YW5jZW9mIEFycmF5KSB7XG4gICAgICBmb3IgKHZhciBpID0gMDsgaSA8IGtvdWx1dHVzLm9yZ2FuaXphdGlvbkdyb3Vwcy5sZW5ndGg7IGkrKykge1xuICAgICAgICB2YXIgZ3JvdXAgPSBrb3VsdXR1cy5vcmdhbml6YXRpb25Hcm91cHNbaV07XG4gICAgICAgIGlmKGdyb3VwLmdyb3VwVHlwZXMuaW5kZXhPZihcImhha3Vrb2hkZVwiKSA+PSAwKSB7XG4gICAgICAgICAgYW9Hcm91cHMucHVzaChncm91cC5vaWQpO1xuICAgICAgICAgIGlmKGdyb3VwLnVzYWdlR3JvdXBzLmluZGV4T2YoXCJoYWt1a29oZGVfbGlpdGVvc29pdGVcIikgPj0gMCkge1xuICAgICAgICAgICAgYXR0YWNobWVudEdyb3Vwcy5wdXNoKGdyb3VwLm9pZCk7XG4gICAgICAgICAgfVxuICAgICAgICB9XG4gICAgICB9XG4gICAgfVxuICAgIGlmKGFvR3JvdXBzLmxlbmd0aCA+IDApIHtcbiAgICAgIHRoaXMuZGF0YVtcIktvdWx1dHVzLWlkLWFvLWdyb3Vwc1wiXSA9IGFvR3JvdXBzLmpvaW4oXCIsXCIpXG4gICAgfVxuICAgIGlmKGF0dGFjaG1lbnRHcm91cHMubGVuZ3RoID4gMCkge1xuICAgICAgdGhpcy5kYXRhW1wiS291bHV0dXMtaWQtYXR0YWNobWVudGdyb3Vwc1wiXSA9IGF0dGFjaG1lbnRHcm91cHMuam9pbihcIixcIilcbiAgICB9XG4gIH0sXG5cbiAgaGFzT3BldHVzcGlzdGU6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiAhXy5pc0VtcHR5KHRoaXMuZGF0YVtcIk9wZXR1c3Bpc3RlLWlkXCJdKVxuICB9LFxuXG4gIHJlbW92ZU9wZXR1c3Bpc3RlRGF0YTogZnVuY3Rpb24oKSB7XG4gICAgdmFyIHNlbGYgPSB0aGlzXG4gICAgXy5lYWNoKHRoaXMuZGF0YSwgZnVuY3Rpb24odmFsdWUsIGtleSkge1xuICAgICAgaWYgKGtleS5pbmRleE9mKFwiJFwiKSE9PTAgJiYga2V5ICE9IFwiT3BldHVzcGlzdGVcIilcbiAgICAgICAgZGVsZXRlIHNlbGYuZGF0YVtrZXldXG4gICAgfSlcbiAgICBkZWxldGUgdGhpcy5rb2hkZWtvaHRhaW5lbkhha3VhaWthXG4gIH0sXG5cbiAgaXNWYWxpZDogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChfLmlzRW1wdHkodGhpcy5kYXRhW1wiT3BldHVzcGlzdGVcIl0pIHx8ICFfLmlzRW1wdHkodGhpcy5kYXRhW1wiS291bHV0dXMtaWRcIl0pKVxuICB9LFxuXG4gIHNldEVycm9yczogZnVuY3Rpb24oZXJyb3JzKSB7XG4gICAgdGhpcy5lcnJvcnMgPSBlcnJvcnMgfHwgW11cbiAgfSxcblxuICBhcHBlbmRFcnJvcnM6IGZ1bmN0aW9uKGVycm9ycykge1xuICAgIHRoaXMuZXJyb3JzID0gdGhpcy5lcnJvcnMuY29uY2F0KGVycm9ycylcbiAgfSxcblxuICBzZXRBc1NhdmVkOiBmdW5jdGlvbigpIHtcbiAgICB0aGlzLmlzTmV3ID0gZmFsc2VcbiAgICB0aGlzLmlzTW9kaWZpZWQgPSBmYWxzZVxuICB9LFxuXG4gIHNldEFzTW9kaWZpZWQ6IGZ1bmN0aW9uKCkge1xuICAgIHRoaXMuaXNNb2RpZmllZCA9IHRydWVcbiAgfVxufVxuXG52YXIgaGFrdXRvaXZlRXJyb3JSZWdleHAgPSAvXihwcmVmZXJlbmNlXFxkKSR8XihwcmVmZXJlbmNlXFxkKS1Lb3VsdXR1cyQvXG5IYWt1dG9pdmUuaXNIYWt1dG9pdmVFcnJvciA9IGZ1bmN0aW9uKHF1ZXN0aW9uSWQpIHtcbiAgcmV0dXJuIGhha3V0b2l2ZUVycm9yUmVnZXhwLnRlc3QocXVlc3Rpb25JZClcbn1cblxuSGFrdXRvaXZlLnBhcnNlSGFrdXRvaXZlSW5kZXggPSBmdW5jdGlvbihxdWVzdGlvbklkKSB7XG4gIHZhciByZXN1bHQgPSAvXnByZWZlcmVuY2UoXFxkKykvLmV4ZWMocXVlc3Rpb25JZClcbiAgaWYgKHJlc3VsdClcbiAgICByZXR1cm4gTnVtYmVyKHJlc3VsdFsxXSlcbiAgZWxzZVxuICAgIHJldHVybiBudWxsXG59XG5cbkhha3V0b2l2ZS5oYXNIYWt1dG9pdmVFcnJvcnMgPSBmdW5jdGlvbihlcnJvcnNKc29uKSB7XG4gIHZhciBlcnJvck1hcCA9IHV0aWwubWFwQXJyYXkoZXJyb3JzSnNvbiwgXCJrZXlcIiwgXCJtZXNzYWdlXCIpO1xuICB2YXIgc2VsZiA9IHRoaXNcbiAgcmV0dXJuIF8oZXJyb3JNYXApLmFueShmdW5jdGlvbih2YWwsIGtleSkge1xuICAgIHJldHVybiBzZWxmLmlzSGFrdXRvaXZlRXJyb3Ioa2V5KSAmJiB2YWwubGVuZ3RoID4gMFxuICB9KVxufVxuXG5IYWt1dG9pdmUuaGFrdXRvaXZlTWFwID0gZnVuY3Rpb24oaGFrdXRvaXZlZXQpIHtcbiAgcmV0dXJuIHV0aWwuaW5kZXhCeShoYWt1dG9pdmVldCwgZnVuY3Rpb24oaGFrdXRvaXZlLCBpbmRleCkgeyByZXR1cm4gXCJwcmVmZXJlbmNlXCIgKyAoaW5kZXgrMSkgfSlcbn1cbkhha3V0b2l2ZS5xdWVzdGlvbklkVG9IYWt1dG9pdmVJZCA9IGZ1bmN0aW9uKHF1ZXN0aW9uSWQpIHtcbiAgcmV0dXJuIF8uY2hhaW4oaGFrdXRvaXZlRXJyb3JSZWdleHAuZXhlYyhxdWVzdGlvbklkKSkucmVzdCgpLndpdGhvdXQodW5kZWZpbmVkKS5maXJzdCgpLnZhbHVlKClcbn1cbm1vZHVsZS5leHBvcnRzID0gSGFrdXRvaXZlXG59KS5jYWxsKHRoaXMscmVxdWlyZShcIis3WkpwMFwiKSx0eXBlb2Ygc2VsZiAhPT0gXCJ1bmRlZmluZWRcIiA/IHNlbGYgOiB0eXBlb2Ygd2luZG93ICE9PSBcInVuZGVmaW5lZFwiID8gd2luZG93IDoge30scmVxdWlyZShcImJ1ZmZlclwiKS5CdWZmZXIsYXJndW1lbnRzWzNdLGFyZ3VtZW50c1s0XSxhcmd1bWVudHNbNV0sYXJndW1lbnRzWzZdLFwiL2hha3V0b2l2ZS5qc1wiLFwiL1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbnZhciB1dGlsID0gcmVxdWlyZSgnLi91dGlsJylcblxudmFyIHF1ZXN0aW9uRGVmYXVsdHMgPSB7XG4gIGhlbHA6IFwiXCIsXG4gIHZlcmJvc2VIZWxwOiBcIlwiLFxuICByZXF1aXJlZDogZmFsc2UsXG4gIHF1ZXN0aW9uVHlwZTogXCJcIixcbiAgaWQ6IG51bGwsXG4gIHRpdGxlOiBcIlwiXG59XG5cbmZ1bmN0aW9uIHBhcmFtcyhvYmopIHtcbiAgdmFyIHF1ZXN0aW9uUGFyYW1ldGVycyA9IF8uZXh0ZW5kKHt9LCBxdWVzdGlvbkRlZmF1bHRzLCBvYmopXG4gIF8ocXVlc3Rpb25QYXJhbWV0ZXJzKS5lYWNoKGZ1bmN0aW9uKHZhbCwga2V5KSB7XG4gICAgaWYgKHZhbCA9PSBudWxsKVxuICAgICAgaWYoa2V5ID09IFwidGl0bGVcIikge1xuICAgICAgICBxdWVzdGlvblBhcmFtZXRlcnMudGl0bGUgPSBcIj9cIlxuICAgICAgfVxuICAgICAgZWxzZSB7XG4gICAgICAgIHRocm93IG5ldyBFcnJvcihcInF1ZXN0aW9uIHBhcmFtZXRlciBcIiArIGtleSArIFwiIG51bGxcIilcbiAgICAgIH1cbiAgfSlcbiAgcmV0dXJuIHF1ZXN0aW9uUGFyYW1ldGVyc1xufVxuXG5mdW5jdGlvbiBRdWVzdGlvbihxdWVzdGlvbiwgYW5zd2VyLCB2YWxpZGF0aW9uRXJyb3JzKSB7XG4gIF8uZXh0ZW5kKHRoaXMsIHBhcmFtcyhxdWVzdGlvbikpXG4gIHRoaXMuYW5zd2VyID0gYW5zd2VyXG4gIHRoaXMuZXJyb3JzID0gdmFsaWRhdGlvbkVycm9ycyB8fCBbXVxufVxuXG5RdWVzdGlvbi5mcm9tSnNvbiA9IGZ1bmN0aW9uKGpzb24sIGFwcGxpY2F0aW9uKSB7XG4gIHJldHVybiBuZXcgUXVlc3Rpb24oanNvbiwgaW5pdGlhbFZhbHVlKGpzb24sIGFwcGxpY2F0aW9uKSwganNvbi5yZXF1aXJlZCA/IFtcIipcIl0gOiBbXSlcbn1cblxuUXVlc3Rpb24uZ2V0UXVlc3Rpb25zID0gZnVuY3Rpb24oanNvblF1ZXN0aW9ucywgYXBwbGljYXRpb24pIHtcbiAgcmV0dXJuIGNvbnZlcnRUb0l0ZW1zKGpzb25RdWVzdGlvbnMsIG5ldyBRdWVzdGlvbkdyb3VwKCkpXG5cbiAgZnVuY3Rpb24gY29udmVydFRvSXRlbXMocXVlc3Rpb25zLCByZXN1bHRzKSB7XG4gICAgXyhxdWVzdGlvbnMpLmVhY2goZnVuY3Rpb24gKHF1ZXN0aW9uTm9kZSkge1xuICAgICAgaWYgKHF1ZXN0aW9uTm9kZS5xdWVzdGlvbnMgIT0gbnVsbCkge1xuICAgICAgICByZXN1bHRzLnF1ZXN0aW9uTm9kZXMucHVzaChjb252ZXJ0VG9JdGVtcyhxdWVzdGlvbk5vZGUucXVlc3Rpb25zLCBuZXcgUXVlc3Rpb25Hcm91cChxdWVzdGlvbk5vZGUudGl0bGUpKSlcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIHJlc3VsdHMucXVlc3Rpb25Ob2Rlcy5wdXNoKFF1ZXN0aW9uLmZyb21Kc29uKHF1ZXN0aW9uTm9kZSwgYXBwbGljYXRpb24pKVxuICAgICAgfVxuICAgIH0pXG4gICAgcmV0dXJuIHJlc3VsdHNcbiAgfVxufVxuXG5RdWVzdGlvbi5wcm90b3R5cGUgPSB7XG4gIHNldEVycm9yczogZnVuY3Rpb24oZXJyb3JzKSB7XG4gICAgdGhpcy5lcnJvcnMgPSBlcnJvcnMgfHwgW11cbiAgfSxcblxuICBhcHBlbmRFcnJvcnM6IGZ1bmN0aW9uKGVycm9ycykge1xuICAgIHRoaXMuZXJyb3JzID0gdGhpcy5lcnJvcnMuY29uY2F0KGVycm9ycylcbiAgfVxufVxuXG5RdWVzdGlvbi5xdWVzdGlvbk1hcCA9IGZ1bmN0aW9uKHF1ZXN0aW9ucykge1xuICBxdWVzdGlvbnMgPSB1dGlsLmZsYXR0ZW5UcmVlKHF1ZXN0aW9ucywgXCJxdWVzdGlvbk5vZGVzXCIpXG4gIHJldHVybiB1dGlsLmluZGV4QnkocXVlc3Rpb25zLCBmdW5jdGlvbihub2RlKSB7IHJldHVybiBub2RlLmlkLnF1ZXN0aW9uSWQgfSlcbn1cblxuZnVuY3Rpb24gaW5pdGlhbFZhbHVlKHF1ZXN0aW9uLCBhcHBsaWNhdGlvbikge1xuICBmdW5jdGlvbiBkZWZhdWx0VmFsdWUoKSB7XG4gICAgdmFyIGRlZmF1bHRPcHRpb24gPSBfKHF1ZXN0aW9uLm9wdGlvbnMpLmZpbmQoZnVuY3Rpb24ob3B0aW9uKSB7IHJldHVybiBvcHRpb24uZGVmYXVsdCB9KVxuICAgIHJldHVybiBkZWZhdWx0T3B0aW9uID09IG51bGwgPyBcIlwiIDogZGVmYXVsdE9wdGlvbi52YWx1ZVxuICB9XG4gIGZ1bmN0aW9uIGdldE9sZFZhbHVlKHF1ZXN0aW9uSWQpIHtcbiAgICB2YXIgcXVlc3Rpb25JZFBhcnRzID0gL14ocHJlZmVyZW5jZSkoXFxkKykoWy1fXS4rKS8uZXhlYyhxdWVzdGlvbklkKVxuICAgIGlmIChxdWVzdGlvbklkUGFydHMgIT0gbnVsbCAmJiBhcHBsaWNhdGlvbi5oYWt1dG9pdmVldFtxdWVzdGlvbklkUGFydHNbMl0gLSAxXSAhPSBudWxsICYmIGFwcGxpY2F0aW9uLmhha3V0b2l2ZWV0W3F1ZXN0aW9uSWRQYXJ0c1syXSAtIDFdLmlzTmV3KSB7XG4gICAgICByZXR1cm4gbnVsbFxuICAgIH1cbiAgICB2YXIgcGhhc2VBbnN3ZXJzID0gYXBwbGljYXRpb24ucGVyc2lzdGVkQW5zd2Vyc1txdWVzdGlvbi5pZC5waGFzZUlkXVxuICAgIGlmKHBoYXNlQW5zd2VycyA9PSBudWxsKSB7XG4gICAgICByZXR1cm4gbnVsbFxuICAgIH1cbiAgICByZXR1cm4gcGhhc2VBbnN3ZXJzW3F1ZXN0aW9uSWRdXG4gIH1cblxuICB2YXIgb2xkQW5zd2VyID0gZ2V0T2xkVmFsdWUocXVlc3Rpb24uaWQucXVlc3Rpb25JZClcbiAgaWYgKHF1ZXN0aW9uLm9wdGlvbnMgIT0gbnVsbCkge1xuICAgIGlmIChxdWVzdGlvbi5xdWVzdGlvblR5cGUgPT0gXCJDaGVja2JveFwiKSB7XG4gICAgICByZXR1cm4gXyhxdWVzdGlvbi5vcHRpb25zKS5jaGFpbigpLm1hcChmdW5jdGlvbihvcHRpb24pIHtcbiAgICAgICAgb2xkQW5zd2VyID0gZ2V0T2xkVmFsdWUob3B0aW9uLnZhbHVlKVxuICAgICAgICByZXR1cm4gW29wdGlvbi52YWx1ZSwgb2xkQW5zd2VyID09IG51bGwgPyBmYWxzZSA6IEJvb2xlYW4ob2xkQW5zd2VyKV1cbiAgICAgIH0pLm9iamVjdCgpLnZhbHVlKClcbiAgICB9IGVsc2Uge1xuICAgICAgLy8gQXNldGEgZGVmYXVsdC1hcnZvIHZhaW4gbW9uaXZhbGlubm9pbGxlXG4gICAgICByZXR1cm4gb2xkQW5zd2VyID09IG51bGwgPyBkZWZhdWx0VmFsdWUoKSA6IG9sZEFuc3dlclxuICAgIH1cbiAgfVxuICBpZihvbGRBbnN3ZXIgIT0gbnVsbCkge1xuICAgIHJldHVybiAgb2xkQW5zd2VyXG4gIH1cbn1cblxuZnVuY3Rpb24gUXVlc3Rpb25Hcm91cCh0aXRsZSkge1xuICB0aGlzLnRpdGxlID0gdGl0bGVcbiAgdGhpcy5xdWVzdGlvbk5vZGVzID0gW11cbn1cblxubW9kdWxlLmV4cG9ydHMgPSB7XG4gIFF1ZXN0aW9uOiBRdWVzdGlvbixcbiAgUXVlc3Rpb25Hcm91cDogUXVlc3Rpb25Hcm91cFxufVxufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi9xdWVzdGlvbi5qc1wiLFwiL1wiKSIsIihmdW5jdGlvbiAocHJvY2VzcyxnbG9iYWwsQnVmZmVyLF9fYXJndW1lbnQwLF9fYXJndW1lbnQxLF9fYXJndW1lbnQyLF9fYXJndW1lbnQzLF9fZmlsZW5hbWUsX19kaXJuYW1lKXtcbnZhciB1dGlscyA9IHtcbiAgbWFwQXJyYXk6IGZ1bmN0aW9uIChhcnJheSwga2V5RmllbGQsIHZhbHVlRmllbGQpIHtcbiAgICByZXR1cm4gXy5yZWR1Y2UoYXJyYXksIGZ1bmN0aW9uIChtZW1vLCBpdGVtKSB7XG4gICAgICB2YXIga2V5ID0gaXRlbVtrZXlGaWVsZF1cbiAgICAgIGlmIChtZW1vW2tleV0gPT0gbnVsbClcbiAgICAgICAgbWVtb1trZXldID0gW11cbiAgICAgIG1lbW9ba2V5XS5wdXNoKGl0ZW1bdmFsdWVGaWVsZF0pXG4gICAgICByZXR1cm4gbWVtb1xuICAgIH0sIHt9KTtcbiAgfSxcblxuICBpbmRleEJ5OiBmdW5jdGlvbiAoYXJyYXksIGtleUZ1bmN0aW9uKSB7XG4gICAgcmV0dXJuIF8ucmVkdWNlKGFycmF5LCBmdW5jdGlvbiAobWVtbywgaXRlbSwgaW5kZXgpIHtcbiAgICAgIG1lbW9ba2V5RnVuY3Rpb24oaXRlbSwgaW5kZXgpXSA9IGl0ZW1cbiAgICAgIHJldHVybiBtZW1vXG4gICAgfSwge30pXG4gIH0sXG5cbiAgZmxhdHRlblRyZWU6IGZ1bmN0aW9uIChyb290Tm9kZSwgY2hpbGRyZW5BdHRyaWJ1dGUpIHtcbiAgICByZXR1cm4gKGZ1bmN0aW9uIGZsYXR0ZW4obm9kZSwgbGlzdCkge1xuICAgICAgaWYgKG5vZGUgIT0gbnVsbCkge1xuICAgICAgICBpZiAobm9kZVtjaGlsZHJlbkF0dHJpYnV0ZV0gPT0gbnVsbClcbiAgICAgICAgICBsaXN0LnB1c2gobm9kZSlcbiAgICAgICAgZWxzZVxuICAgICAgICAgIF8obm9kZVtjaGlsZHJlbkF0dHJpYnV0ZV0pLmVhY2goZnVuY3Rpb24gKHN1Ym5vZGUpIHtcbiAgICAgICAgICAgIGZsYXR0ZW4oc3Vibm9kZSwgbGlzdClcbiAgICAgICAgICB9KVxuICAgICAgfVxuICAgICAgcmV0dXJuIGxpc3RcbiAgICB9KShyb290Tm9kZSwgW10pXG4gIH0sXG5cbiAgdW5kZXJzY29yZVRvQ2FtZWxDYXNlOiBmdW5jdGlvbiAoc3RyKSB7XG4gICAgcmV0dXJuIHN0ci50b0xvd2VyQ2FzZSgpLnJlcGxhY2UoL14oLil8XyguKS9nLCBmdW5jdGlvbiAobWF0Y2gsIGNoYXIxLCBjaGFyMikge1xuICAgICAgcmV0dXJuIChjaGFyMSA/IGNoYXIxIDogXCJcIiArIGNoYXIyID8gY2hhcjIgOiBcIlwiKS50b1VwcGVyQ2FzZSgpXG4gICAgfSlcbiAgfSxcblxuICB3aXRob3V0QW5ndWxhckZpZWxkczogZnVuY3Rpb24gKG9iaikge1xuICAgIGlmIChfLmlzQXJyYXkob2JqKSkge1xuICAgICAgcmV0dXJuIF8ob2JqKS5tYXAodXRpbHMud2l0aG91dEFuZ3VsYXJGaWVsZHMpXG4gICAgfSBlbHNlIGlmIChfLmlzT2JqZWN0KG9iaikpIHtcbiAgICAgIHJldHVybiBfKG9iaikucmVkdWNlKGZ1bmN0aW9uIChtZW1vLCB2YWwsIGtleSkge1xuICAgICAgICBpZiAoa2V5LmluZGV4T2YoXCIkJFwiKSA8IDApXG4gICAgICAgICAgbWVtb1trZXldID0gdXRpbHMud2l0aG91dEFuZ3VsYXJGaWVsZHModmFsKVxuICAgICAgICByZXR1cm4gbWVtb1xuICAgICAgfSwge30pXG4gICAgfSBlbHNlXG4gICAgICByZXR1cm4gb2JqXG4gIH1cbn1cblxubW9kdWxlLmV4cG9ydHMgPSB1dGlsc1xufSkuY2FsbCh0aGlzLHJlcXVpcmUoXCIrN1pKcDBcIiksdHlwZW9mIHNlbGYgIT09IFwidW5kZWZpbmVkXCIgPyBzZWxmIDogdHlwZW9mIHdpbmRvdyAhPT0gXCJ1bmRlZmluZWRcIiA/IHdpbmRvdyA6IHt9LHJlcXVpcmUoXCJidWZmZXJcIikuQnVmZmVyLGFyZ3VtZW50c1szXSxhcmd1bWVudHNbNF0sYXJndW1lbnRzWzVdLGFyZ3VtZW50c1s2XSxcIi91dGlsLmpzXCIsXCIvXCIpIl19
