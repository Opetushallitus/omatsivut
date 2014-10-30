package fi.vm.sade.omatsivut.tarjonta

object HakuTyyppi extends Enumeration {
  type HakuTyyppi = Value
  val Yhteishaku = Value("YHTEISHAKU")
  val Erillishaku = Value("ERILLISHAKU")
  val JatkuvaHaku = Value("JATKUVA_HAKU")
  val Lisahaku = Value("LISAHAKU")

  def apply(tarjontaHaku: TarjontaHaku) = {
    if(tarjontaHaku.hakutyyppiUri.contains("hakutyyppi_03")) {
      Lisahaku
    } else {
      if(tarjontaHaku.hakutapaUri.contains("hakutapa_01")) {
        Yhteishaku
      } else if (tarjontaHaku.hakutapaUri.contains("hakutapa_02")) {
        Erillishaku
      } else if(tarjontaHaku.hakutapaUri.contains("hakutapa_03")) {
        JatkuvaHaku
      } else {
        throw new IllegalArgumentException("Unsupported type for haku: " + tarjontaHaku.oid + " - " + tarjontaHaku.hakutyyppiUri + "," + tarjontaHaku.hakutapaUri)
      }
    }
  }
}
