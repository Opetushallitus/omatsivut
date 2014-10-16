function ApiDocsPage() {
  var apiDocsPage = openPage("/omatsivut/api-docs/#!/api", visible)

  var api = {
    openPage: apiDocsPage,
    
    endpoints: function () {
      return S("#applications_endpoint_list>li")
        .map(function () {
          return {
            method: $(this).find(".http_method>a").text().trim(),
            path: $(this).find(".path>a").text().trim(),
            description: $(this).find(".options>li>a").text().trim()
          }
        }).toArray()
    }
  }
  return api

  function visible() {
    return api.endpoints().length > 0
  }
  
}
