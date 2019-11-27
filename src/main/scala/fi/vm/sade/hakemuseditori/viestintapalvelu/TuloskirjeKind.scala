package fi.vm.sade.hakemuseditori.viestintapalvelu

sealed trait TuloskirjeKind

case object Pdf extends TuloskirjeKind
case object AccessibleHtml extends TuloskirjeKind
