package fi.vm.sade.omatsivut.servlet

import org.scalatra.swagger.{JacksonSwaggerBase, Swagger}
import org.scalatra.ScalatraServlet

class SwaggerServlet(implicit val swagger: Swagger) extends ScalatraServlet with JacksonSwaggerBase
