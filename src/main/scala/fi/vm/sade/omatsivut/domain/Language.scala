package fi.vm.sade.omatsivut.domain

import fi.vm.sade.omatsivut.Logging

object Language extends Enumeration with Logging{
   type Language = Value
   val fi, sv, en = Value
   def parse(lang: String): Option[Language] = {
     try {
       Some(this.withName(lang.trim()))
     }
     catch {
       case e: Exception => {
         logger.warn("Unsupported language '" + lang + "'. Using fi instead", e)
         None
       }
     }
   }
}