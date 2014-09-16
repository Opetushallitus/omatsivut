package fi.vm.sade.omatsivut.hakemus

/**
 * Created by singen on 16.9.2014.
 */
object FlatAnswers {
  type FlatAnswers = Map[String, String]
  def flatten(answers: Map[String, Map[String, String]]): FlatAnswers = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }
}
