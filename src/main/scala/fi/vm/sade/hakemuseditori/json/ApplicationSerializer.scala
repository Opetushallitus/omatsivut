package fi.vm.sade.hakemuseditori.json

import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s
import org.json4s.JsonAST.{JInt, JString}
import org.json4s.{CustomSerializer, Extraction, Formats, JField, JObject, JValue, Serializer, TypeInfo}

import scala.::

class ApplicationSerializer extends CustomSerializer[Application](format => (
{
  case JObject( // TODO state enum, received, updated, answers
    JField("oid", JString(f)) ::
    JField("applicationSystemId", JString(l)) ::
      JField("personOid", JString(d))
  ) => new Application(f, l, d)
},
  {
    case x: Application => JObject(
      JField("oid", JString(x.getOid())),
        JField("applicationSystemId", JString(x.getApplicationSystemId())),
      JField("personOid", JString(x.getPersonOid())))
  }
))

