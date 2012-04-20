var JSFUnit = JSFUnit || {};

JSFUnit.requestEnrichment = (function() {

    var statechangeInterceptor = null;
    var openInterceptor = null;
    
    var requestEnrichment = null;
    var responseEnrichment = null;

    function createStatechangeInterceptor() {
        return function(context, args) {
            if (this.readyState == 4) {
                var extracted = extractAssertion(this.responseText);
                this.responseText = extracted.responseText;
                responseEnrichment = extracted.assertion;
            }
            context.proceed(args);
        };
    }

    function extractAssertion(responseText) {
        var result = {};
        var separator = 'X-Arq-Enrichment-Response=';
        var index = responseText.indexOf(separator);
        result.responseText = responseText.substring(0, index);
        result.assertion = responseText.substring(index + separator.length, responseText.length);
        return result;
    }

    function createOpenInterceptor() {
        return function(context, args) {
            var url = args[1];
            url += (url.indexOf('?') > -1) ? '&' : '?';
            url += 'X-Arq-Enrichment-Request=' + requestEnrichment;
            args[1] = url;
            context.proceed(args);
        };
    }

    function registerXhrInterceptor() {
        openInterceptor = createOpenInterceptor();
        Graphene.xhrInjection.onOpen(openInterceptor);

        statechangeInterceptor = createStatechangeInterceptor();
        Graphene.xhrInjection.onreadystatechange(statechangeInterceptor);
    }

    // PUBLIC METHODS
    return {
        install : function() {
            if (!openInterceptor) {
                registerXhrInterceptor();
            }
        },
        setRequestEnrichment: function(enrichment) {
            requestEnrichment = enrichment;
        },
        getResponseEnrichment: function() {
            return responseEnrichment;
        },
        clearEnrichment: function() {
            requestEnrichment = null;
            responseEnrichment = null;
        }
    };

})();
