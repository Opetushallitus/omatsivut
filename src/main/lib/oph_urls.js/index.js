"use strict";

/**
 * window.urls.debugLog().loadFromUrls("suoritusrekisteri-web-frontend-url_properties.json", "rest/v1/properties").success(function(){appInit();})
 *
 * window.url("service.info", param1, param2, {key3: value})
 *
 * window.urls(baseUrl).url(key, param)
 * window.urls(baseUrl).noEncode().url(key, param)
 * window.urls({baseUrl: baseUrl}).omitEmptyValuesFromQuerystring().url(key, param)
 * window.urls().omitEmptyValuesFromQuerystring().url(key, param)
 *
 * Config lookup order: urls_config, window.urls.override, window.urls.properties, window.urls.defaults
 * Lookup key order:
 * * for main url window.url's first parameter: "service.info" from all configs
 * * baseUrl: "service.baseUrl" from all configs and "baseUrl" from all configs
 *
 * window.url_properties = {
 *   "service.status": "/rest/status",
 *   "service.payment": "/rest/payment/$1",
 *   "service.order": "/rest/payment/$orderId"
 *   }
 *
 * window.urls.debug = true
 *
 */

(function(exportDest) {
    exportDest.urls = function() {
        var urls_config = {}
        var omitEmptyValuesFromQuerystring = false
        var encode = true

        for (var i = 0; i < arguments.length;  i++) {
            var arg = arguments[i]
            if(typeof arg === "string" || arg == null) {
                urls_config.baseUrl = arg
            } else {
                Object.keys(arg).forEach(function(key){
                    urls_config[key] = arg[key]
                })
            }
        }

        var resolveConfig = function(key, defaultValue) {
            var configs = [urls_config, exportDest.urls.override, exportDest.urls.properties, exportDest.urls.defaults]
            for (var i = 0; i < configs.length; i++) {
                var c = configs[i]
                if(c.hasOwnProperty(key)) {
                    return c[key]
                }
            }
            if(typeof defaultValue == 'function') {
                return defaultValue()
            }
            if(typeof defaultValue == 'undefined') {
                throw new Error("Could not resolve value for '"+key+"'")
            }
            return defaultValue
        }

        var enc = function(arg) {
            arg = [undefined, null].indexOf(arg) > -1 ? "" : arg
            if(encode) {
                arg = encodeURIComponent(arg)
            }
            return arg
        }

        var includeToQuerystring = function(v) {
            if(omitEmptyValuesFromQuerystring) {
                return [undefined, null, ""].indexOf(v) === -1
            } else {
                return [undefined].indexOf(v) === -1
            }
        }

        var ret = {
            omitEmptyValuesFromQuerystring: function () {
                omitEmptyValuesFromQuerystring = true
                return ret
            },
            noEncode: function() {
                encode = false
                return ret
            },
            url: function () {
                var key = Array.prototype.shift.apply(arguments)
                var args = Array.prototype.slice.call(arguments)
                var queryString = "";
                var tmpUrl;
                if (!key) {
                    throw new Error("first parameter 'key' not defined!");
                }
                var url = resolveConfig(key)
                // reverse iteration because $10 needs to be handled first
                for (var i = args.length; i > 0; i--) {
                    var arg = args[i - 1];
                    if (typeof arg === "object") {
                        Object.keys(arg).forEach(function (k) {
                            var originalValue = arg[k];
                            if(!isArray(originalValue)) {
                                tmpUrl = url.replace("$" + k, enc(originalValue))
                            }
                            if (tmpUrl == url && includeToQuerystring(originalValue)) {
                                var values = isArray(originalValue) ? originalValue : [originalValue];
                                for(var j = 0; j < values.length; j++) {
                                    var separator = (queryString.length > 0) ? "&" : "?";
                                    var encodedKeyValue = enc(k) + "=" + enc(values[j]);
                                    queryString = queryString + separator + encodedKeyValue
                                }
                            }
                            url = tmpUrl
                        })
                    } else {
                        url = url.replace("$" + i, enc(arg))
                    }
                }
                var baseUrl = resolveConfig(parseService(key) + ".baseUrl", function () {
                    return resolveConfig("baseUrl", null)
                })
                if (baseUrl) {
                    url = joinUrl(baseUrl, url)
                }
                url = url + queryString
                debug("url:", key, "->", url)
                return url
            }
        }
        return ret
    }

    exportDest.urls.properties = {}
    exportDest.urls.defaults = {}
    exportDest.urls.override = {}
    exportDest.urls.debug = false
    exportDest.urls.debugLog = function() {
        exportDest.urls.debug = true;
        return this;
    }

    function debug() {
        var args = Array.prototype.slice.call(arguments)
        args.unshift("OphProperties")
        if(exportDest.urls.debug && exportDest.console && exportDest.console.log) {
            exportDest.console.log.apply(exportDest.console, args)
        }
    }

    function ajaxJson(method, url, onload, onerror) {
        var oReq = new XMLHttpRequest();
        oReq.open(method, url, true);
        oReq.onreadystatechange = function() {
            if (oReq.readyState == 4) {
                if(oReq.status == 200) {
                    if(onload) {
                        onload(JSON.parse(oReq.responseText))
                    }
                } else {
                    if(onerror) {
                        onerror(url + " status " +oReq.status + ": " + oReq.responseText)
                    }
                }
            }
        }
        oReq.send(null);
    }

    // minimalist angular Promise implementation, returns object with .success(cb)
    var successCBs = []
    var fulfilled = false, fulfillFailed = false
    var fulfillCount = 0, fulfillCountDest = 0
    function checkfulfill() {
        fulfillCount += 1
        if(fulfillCount == fulfillCountDest) {
            fulfilled = true
            if(!fulfillFailed) {
                successCBs.forEach(function(cb){cb()})
            }
        }
    }
    exportDest.urls.success = function(cb) {
        if(fulfilled) {
            if(!fulfillFailed) {
                cb()
            }
        } else {
            successCBs.push(cb)
        }
    }

    exportDest.urls.loadFromUrls = function() {
        var args = Array.prototype.slice.call(arguments)
        var jsonProperties = []
        successCBs.push(function(){
            jsonProperties.forEach(function(json){merge(exportDest.urls.properties, json)})
        })
        fulfillCountDest += args.length
        args.forEach(function(url, index){
            ajaxJson("GET", url, function(data) {
                jsonProperties.splice(index, 0, data)
                checkfulfill()
            }, function() {
                fulfillFailed = true
                checkfulfill()
            })
        })
        return {
            success: exportDest.urls.success
        };
    }

    function merge(dest, from) {
        Object.keys(from).forEach(function(key){
            dest[key]=from[key];
        })
    }

    exportDest.url = exportDest.urls().url

    function parseService (key) {
        return key.substring(0, key.indexOf("."))
    }

    function joinUrl() {
        var args = Array.prototype.slice.call(arguments)
        if(args.length === 0) {
            throw new Error("no arguments");
        }
        var url = null
        args.forEach(function(arg) {
            if(!url) {
                url = arg
            } else {
                var endsWith = url.endsWith("/");
                var startsWith = arg.startsWith("/");
                if(endsWith && startsWith) {
                    url = url + arg.substring(1)
                } else if(endsWith || startsWith) {
                    url = url + arg
                } else {
                    url = url + "/" + arg
                }
            }
        })
        return url
    }

    function isArray(arr) {
        if(Array.isArray) {
            return Array.isArray(arr);
        } else {
            return arr && arr.constructor === Array;
        }
    }
})(typeof window === 'undefined' ? module.exports : window);

// polyfills for IE

if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
        position = position || 0;
        return this.substr(position, searchString.length) === searchString;
    };
}

if (!String.prototype.endsWith) {
    String.prototype.endsWith = function(searchString, position) {
        var subjectString = this.toString();
        if (typeof position !== 'number' || !isFinite(position) || Math.floor(position) !== position || position > subjectString.length) {
            position = subjectString.length;
        }
        position -= searchString.length;
        var lastIndex = subjectString.indexOf(searchString, position);
        return lastIndex !== -1 && lastIndex === position;
    };
}
